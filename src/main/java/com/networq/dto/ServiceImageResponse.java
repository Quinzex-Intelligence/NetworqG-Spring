package com.networq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceImageResponse {

    private String id;
    private String imageUrl;
    private Integer displayOrder;
}