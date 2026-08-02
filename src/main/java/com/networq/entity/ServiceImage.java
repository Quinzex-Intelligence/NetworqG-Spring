package com.networq.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "service_images")
@Getter
@Setter
public class ServiceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    @JsonIgnore
    private Service service;

    @Column(nullable = false)
    private String imageKey;

    @Column(nullable = false)
    private Integer displayOrder;
}
