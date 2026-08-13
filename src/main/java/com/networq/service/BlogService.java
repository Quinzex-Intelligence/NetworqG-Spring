package com.networq.service;


import com.networq.dto.BlogCursorResponse;
import com.networq.dto.BlogResponse;
import com.networq.dto.CreateBlogRequest;
import com.networq.dto.UpdateBlogRequest;
import com.networq.entity.Blogs;
import com.networq.repo.BlogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogService {

    private final S3Service s3Service;
    private final BlogRepository blogRepository;

    public String createBlog(CreateBlogRequest request, MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Blog image is required.");
        }
        String imageKey = s3Service.uploadImage(image);
        Blogs blog = new Blogs();
        blog.setTitle(request.getTitle());
        blog.setDescription(request.getDescription());
        blog.setActive(request.getActive());
        blog.setImageKey(imageKey);

        blogRepository.save(blog);

        return "Blog created successfully.";

    }

    public String updateBlog(String blogId,UpdateBlogRequest request, MultipartFile image) throws IOException {
        Blogs blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new EntityNotFoundException("Blog not found."));
        blog.setTitle(request.getTitle());
        blog.setDescription(request.getDescription());
        blog.setActive(request.getActive());
     if(image!=null && !image.isEmpty()) {
         if(blog.getImageKey()!=null&&!blog.getImageKey().isBlank()) {
            s3Service.deleteImage(blog.getImageKey());
         }
         String newImageKey = s3Service.uploadImage(image);
         blog.setImageKey(newImageKey);
     }
     blogRepository.save(blog);
        return "Blog updated successfully.";
    }


    @Transactional(readOnly = true)
    public BlogCursorResponse getActiveBlogs(
            int limit,
            Instant cursorCreatedAt,
            String cursorId) {

        return getBlogs(
                true,
                limit,
                cursorCreatedAt,
                cursorId
        );
    }
    @Transactional(readOnly = true)
    public BlogCursorResponse getInactiveBlogs(
            int limit,
            Instant cursorCreatedAt,
            String cursorId) {

        return getBlogs(
                false,
                limit,
                cursorCreatedAt,
                cursorId
        );
    }
    private BlogCursorResponse getBlogs(
            boolean active,
            int limit,
            Instant cursorCreatedAt,
            String cursorId) {

        if (limit < 1 || limit > 50) {
            throw new IllegalArgumentException(
                    "Limit must be between 1 and 50"
            );
        }

        PageRequest pageRequest = PageRequest.of(0, limit + 1);

        List<Blogs> blogs;

        if (cursorCreatedAt == null
                || cursorId == null
                || cursorId.isBlank()) {

            blogs = active
                    ? blogRepository.findActiveBlogs(pageRequest)
                    : blogRepository.findInactiveBlogs(pageRequest);

        } else {

            blogs = active
                    ? blogRepository.findActiveBlogsAfterCursor(
                    cursorCreatedAt,
                    cursorId,
                    pageRequest
            )
                    : blogRepository.findInactiveBlogsAfterCursor(
                    cursorCreatedAt,
                    cursorId,
                    pageRequest
            );
        }

        boolean hasMore = blogs.size() > limit;

        if (hasMore) {
            blogs = blogs.subList(0, limit);
        }

        String nextCursor = null;

        if (hasMore && !blogs.isEmpty()) {
            Blogs lastBlog = blogs.get(blogs.size() - 1);

            nextCursor = lastBlog.getCreatedAt()
                    + "|" + lastBlog.getId();
        }

        List<BlogResponse> responses = blogs.stream()
                .map(this::mapToResponse)
                .toList();

        return new BlogCursorResponse(
                responses,
                nextCursor,
                hasMore
        );
    }
    private BlogResponse mapToResponse(Blogs blog) {

        String imageUrl = s3Service.generatePresignedUrl(
                blog.getImageKey()
        );

        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .description(blog.getDescription())
                .active(blog.isActive())
                .createdAt(blog.getCreatedAt())
                .imageUrl(imageUrl)
                .build();
    }
}
