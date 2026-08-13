package com.networq.config;

import com.networq.security.GoogleOAuth2FailureHandler;
import com.networq.security.GoogleOAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthorizationRequestResolver authorizationRequestResolver;
    private final GoogleOAuth2FailureHandler failureHandler;
    private final GoogleOAuth2SuccessHandler successHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth->auth.requestMatchers( "/login/**",   "/oauth2/**","/error","/api/jobs/public", "/api/blogs/active","/api/blogs/inactive","/api/jobs/*/apply","/api/auth/test").permitAll().anyRequest().authenticated())
                .oauth2Login(oauth->oauth.authorizationEndpoint(endpoint->endpoint.authorizationRequestResolver(authorizationRequestResolver)).successHandler(successHandler).failureHandler(failureHandler))
                .formLogin(formLogin -> formLogin.disable()).httpBasic(basicAuth -> basicAuth.disable());
        return http.build();

    }
}
