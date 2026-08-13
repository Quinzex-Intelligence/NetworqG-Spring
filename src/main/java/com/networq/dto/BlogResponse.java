package com.networq.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BlogResponse {

    private String id;
    private String title;
    private String description;
    private boolean active;
    private Instant createdAt;
    private String imageUrl;
}