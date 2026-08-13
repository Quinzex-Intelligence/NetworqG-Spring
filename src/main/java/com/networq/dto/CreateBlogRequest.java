package com.networq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBlogRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Boolean active;
}