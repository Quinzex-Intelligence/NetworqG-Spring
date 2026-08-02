package com.networq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ServiceResponse {

    private String id;
    private String title;
    private String shortDescription;
    private String longDescription;
    private Boolean active;
    private Integer displayOrder;
    private List<ServiceImageResponse> images;
}