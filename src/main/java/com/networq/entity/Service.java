package com.networq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "services")
@Getter
@Setter
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 500)
    private String shortDescription;

    @Lob

    @Column(nullable = false)
    private String longDescription;
    @Column(nullable = false)
    private Boolean active=true;



    @Column(nullable = false)
    private Integer displayOrder=0;


    @OneToMany(
            mappedBy = "service",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("displayOrder ASC")
    private List<ServiceImage> images = new ArrayList<>();

    private Instant createdDate;

    private Instant updatedDate;

    @PrePersist
    public void oncreate() {
        Instant now = Instant.now();
        createdDate = now;
        updatedDate = now;
    }
    @PreUpdate
    public void onupdate() {
        updatedDate = Instant.now();
    }
}
