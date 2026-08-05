package com.networq.security;

import com.networq.logging.LoggingUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class GoogleOAuth2FailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.warn("""
                        Google OAuth authentication failed.
                        method        : {}
                        url           : {}
                        exceptionType : {}
                        message       : {}
                        Stacktrace    :
                        {}
                        """,
                request.getMethod(),
                request.getRequestURI(),
                exception.getClass().getName(),
                LoggingUtils.safe(exception.getMessage()),
                LoggingUtils.stackTrace(exception));
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Google Authentication Failed");
    }
}
