package com.networq.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        long startNanos = System.nanoTime();
        String requestUrl = LoggingUtils.requestUrl(request);

        log.info("Incoming Request: {} {}", request.getMethod(), requestUrl);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long executionTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            log.info(
                    "Outgoing Response: {} | {} {} | Execution Time : {} ms",
                    statusText(response.getStatus()),
                    request.getMethod(),
                    requestUrl,
                    executionTimeMs);
        }
    }

    private String statusText(int status) {
        try {
            return HttpStatus.valueOf(status).toString();
        } catch (IllegalArgumentException ex) {
            return String.valueOf(status);
        }
    }
}
