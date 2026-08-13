package com.networq.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Table(name = "blogs")
@Data
public class Blogs {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT",nullable = false)

    private String description;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private String imageKey;



    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
}
