package com.networq.service;

import com.networq.entity.Admin;
import com.networq.repo.AdminRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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

                log.info("Authenticating admin via Google OAuth. email={}", email);
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

                log.info("Logging in admin. email={}", email);
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

                Admin savedAdmin = adminRepository.save(admin);
                log.info("Admin login completed. adminId={}, email={}", savedAdmin.getId(), savedAdmin.getEmail());
                return savedAdmin;
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

                log.info("Bootstrapping first admin. email={}", email);
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

                Admin savedAdmin = adminRepository.save(admin);
                log.info("First admin bootstrapped successfully. adminId={}, email={}", savedAdmin.getId(), savedAdmin.getEmail());
                return savedAdmin;
        }

        public Admin getAuthenticatedAdmin(String googleSub) {

                Admin admin = adminRepository.findByGoogleSub(googleSub)
                                .orElseThrow(() -> new AccessDeniedException("Admin not found."));
                log.info("Authenticated admin loaded. adminId={}, email={}", admin.getId(), admin.getEmail());
                return admin;
        }

}
