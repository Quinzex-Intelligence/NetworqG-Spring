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
        AND (
            :createdAt IS NULL
            OR b.createdAt < :createdAt
            OR (b.createdAt = :createdAt AND b.id < :id)
        )
        ORDER BY b.createdAt DESC, b.id DESC
        """)
    List<Blogs> findActiveBlogs(
            @Param("createdAt") Instant createdAt,
            @Param("id") String id,
            Pageable pageable
    );

    @Query("""
        SELECT b FROM Blogs b
        WHERE b.isActive = false
        AND (
            :createdAt IS NULL
            OR b.createdAt < :createdAt
            OR (b.createdAt = :createdAt AND b.id < :id)
        )
        ORDER BY b.createdAt DESC, b.id DESC
        """)
    List<Blogs> findInactiveBlogs(
            @Param("createdAt") Instant createdAt,
            @Param("id") String id,
            Pageable pageable
    );
}