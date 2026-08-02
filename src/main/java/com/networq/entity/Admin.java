package com.networq.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Admin_User")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "google_sub",nullable = false,unique = true,length = 100)
    private String googleSub;

    @Column(nullable = false,unique = true,length = 225)
    private String email;

    @Column(nullable = false,length = 225)
    private String name;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "refresh_token",columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active =true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "id_token", columnDefinition = "TEXT")
    private String idToken;

    @Column(name = "access_token_expires_at")
    private Instant accessTokenExpiresAt;


    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
