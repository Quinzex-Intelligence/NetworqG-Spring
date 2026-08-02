package com.networq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateServiceRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String shortDescription;

    @NotBlank
    private String longDescription;

    @NotNull
    private Boolean active;

    @NotNull
    private Integer displayOrder;
}