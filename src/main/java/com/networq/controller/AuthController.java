package com.networq.controller;



import com.networq.dto.AuthResponse;
import com.networq.entity.Admin;
import com.networq.service.AdminService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminService adminService;


    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }


    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof OidcUser user)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Admin admin = adminService.getAuthenticatedAdmin(user.getSubject());

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .authenticated(true)
                        .id(admin.getId())
                        .googleSub(admin.getGoogleSub())
                        .email(admin.getEmail())
                        .name(admin.getName())
                        .picture(admin.getProfilePicture())
                        .build()
        );
    }


    @GetMapping("/status")
    public ResponseEntity<Boolean> status(Authentication authentication) {

        boolean authenticated =
                authentication != null
                        && authentication.isAuthenticated()
                        && authentication.getPrincipal() instanceof OidcUser;

        return ResponseEntity.ok(authenticated);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws ServletException {

        request.logout();

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        return ResponseEntity.ok().build();
    }
}