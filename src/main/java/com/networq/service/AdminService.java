package com.networq.service;

import com.networq.entity.Admin;
import com.networq.repo.AdminRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

        private final AdminRepository adminRepository;

        public Admin authenticate(
                        String googleSub,
                        String email,
                        String name,
                        String picture,
                        String accessToken,
                        String refreshToken,
                        String idToken,
                        Instant accessTokenExpiresAt) {

                if (adminRepository.count() == 0) {
                        return bootstrap(
                                        googleSub,
                                        email,
                                        name,
                                        picture,
                                        accessToken,
                                        refreshToken,
                                        idToken,
                                        accessTokenExpiresAt);
                }

                return login(
                                googleSub,
                                email,
                                name,
                                picture,
                                accessToken,
                                refreshToken,
                                idToken,
                                accessTokenExpiresAt);
        }

        private Admin login(
                        String googleSub,
                        String email,
                        String name,
                        String picture,
                        String accessToken,
                        String refreshToken,
                        String idToken,
                        Instant accessTokenExpiresAt) {

                Admin admin = adminRepository.findByGoogleSub(googleSub)
                                .orElseThrow(() -> new AccessDeniedException(
                                                "You are not authorized to access this application."));

                admin.setEmail(email);
                admin.setName(name);
                admin.setProfilePicture(picture);

                admin.setAccessToken(accessToken);

                if (refreshToken != null) {
                        admin.setRefreshToken(refreshToken);
                }

                admin.setIdToken(idToken);
                admin.setAccessTokenExpiresAt(accessTokenExpiresAt);

                return adminRepository.save(admin);
        }

        private Admin bootstrap(
                        String googleSub,
                        String email,
                        String name,
                        String picture,
                        String accessToken,
                        String refreshToken,
                        String idToken,
                        Instant accessTokenExpiresAt) {

                if (adminRepository.count() > 0) {
                        throw new AccessDeniedException("Admin already exists.");
                }

                Admin admin = Admin.builder()
                                .googleSub(googleSub)
                                .email(email)
                                .name(name)
                                .profilePicture(picture)
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .idToken(idToken)
                                .accessTokenExpiresAt(accessTokenExpiresAt)
                                .active(true)
                                .build();

                return adminRepository.save(admin);
        }

        public Admin getAuthenticatedAdmin(String googleSub) {

                return adminRepository.findByGoogleSub(googleSub)
                                .orElseThrow(() -> new AccessDeniedException("Admin not found."));
        }

}