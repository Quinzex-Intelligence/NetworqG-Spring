package com.networq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "networq_jobs")
public class Jobs {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;


    private String jobName;


    private String jobId;

    private String jobDescription;

    private Instant createdDate;

    private Instant expiryDate;

    @PrePersist
    public void prePersist() {
        createdDate = Instant.now();
    }
}
