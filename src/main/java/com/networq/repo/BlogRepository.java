package com.networq.repo;

import com.networq.entity.Blogs;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
public interface BlogRepository extends JpaRepository<Blogs, String> {

    @Query("""
        SELECT b FROM Blogs b
        WHERE b.isActive = true
        ORDER BY b.createdAt DESC, b.id DESC
        """)
    List<Blogs> findActiveBlogs(Pageable pageable);

    @Query("""
        SELECT b FROM Blogs b
        WHERE b.isActive = false
        ORDER BY b.createdAt DESC, b.id DESC
        """)
    List<Blogs> findInactiveBlogs(Pageable pageable);

    @Query("""
        SELECT b FROM Blogs b
        WHERE b.isActive = true
        AND (
            b.createdAt < :cursorCreatedAt
            OR (b.createdAt = :cursorCreatedAt AND b.id < :cursorId)
        )
        ORDER BY b.createdAt DESC, b.id DESC
        """)
    List<Blogs> findActiveBlogsAfterCursor(
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") String cursorId,
            Pageable pageable
    );

    @Query("""
        SELECT b FROM Blogs b
        WHERE b.isActive = false
        AND (
            b.createdAt < :cursorCreatedAt
            OR (b.createdAt = :cursorCreatedAt AND b.id < :cursorId)
        )
        ORDER BY b.createdAt DESC, b.id DESC
        """)
    List<Blogs> findInactiveBlogsAfterCursor(
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") String cursorId,
            Pageable pageable
    );
}
