package com.networq.controller;

import com.networq.dto.BlogCursorResponse;
import com.networq.dto.CreateBlogRequest;
import com.networq.dto.UpdateBlogRequest;
import com.networq.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createBlog(
            @ModelAttribute CreateBlogRequest request,
            @RequestPart("image") MultipartFile image) throws Exception {

        return ResponseEntity.ok(
                blogService.createBlog(request, image)
        );
    }

    @PutMapping(
            value = "/{blogId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> updateBlog(
            @PathVariable String blogId,
            @ModelAttribute UpdateBlogRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws Exception {

        return ResponseEntity.ok(
                blogService.updateBlog(blogId, request, image)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<BlogCursorResponse> getActiveBlogs(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Instant cursorCreatedAt,
            @RequestParam(required = false) String cursorId
    ) {

        return ResponseEntity.ok(
                blogService.getActiveBlogs(
                        limit,
                        cursorCreatedAt,
                        cursorId
                )
        );
    }

    @GetMapping("/inactive")
    public ResponseEntity<BlogCursorResponse> getInactiveBlogs(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Instant cursorCreatedAt,
            @RequestParam(required = false) String cursorId
    ) {

        return ResponseEntity.ok(
                blogService.getInactiveBlogs(
                        limit,
                        cursorCreatedAt,
                        cursorId
                )
        );
    }
}