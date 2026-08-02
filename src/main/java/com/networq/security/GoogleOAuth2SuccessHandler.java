package com.networq.security;

import com.networq.service.AdminService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {
        private final AdminService adminService;
        private final OAuth2AuthorizedClientService auth2AuthorizedClientService;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws IOException, ServletException {
                OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
                OAuth2AuthorizedClient authorizedClient = auth2AuthorizedClientService
                                .loadAuthorizedClient(
                                                token.getAuthorizedClientRegistrationId(),
                                                token.getName());
                if (authorizedClient == null) {
                        response.sendError(
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Google authorization not found.");
                        return;
                }
                OidcUser user = (OidcUser) authentication.getPrincipal();
                String googleSub = user.getAttribute("sub");
                String email = user.getAttribute("email");
                String name = user.getAttribute("name");
                String picture = user.getAttribute("picture");
                OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
                String accessTokenValue = accessToken.getTokenValue();

                String refreshTokenValue = refreshToken != null
                                ? refreshToken.getTokenValue()
                                : null;

                Instant accessTokenExpiresAt = accessToken.getExpiresAt();

                String idToken = user.getIdToken().getTokenValue();

                try {
                        adminService.authenticate(
                                        googleSub,
                                        email,
                                        name,
                                        picture,
                                        accessTokenValue,
                                        refreshTokenValue,
                                        idToken,
                                        accessTokenExpiresAt);
                } catch (AccessDeniedException ex) {
                        response.sendError(
                                        HttpServletResponse.SC_FORBIDDEN,
                                        ex.getMessage());
                        return;
                }
                response.sendRedirect("http://localhost:5050");
        }
}
