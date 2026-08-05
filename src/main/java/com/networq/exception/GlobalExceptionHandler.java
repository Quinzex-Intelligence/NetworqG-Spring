package com.networq.exception;

import com.networq.logging.LoggingUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> handleBindException(BindException ex, HttpServletRequest request) {
        String reason = validationReason(ex.getBindingResult());
        logValidationFailure(request, reason);
        return ResponseEntity.badRequest().body(reason);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        String reason = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        logValidationFailure(request, reason);
        return ResponseEntity.badRequest().body(reason);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MultipartException.class
    })
    public ResponseEntity<String> handleBadRequest(Exception ex, HttpServletRequest request) {
        String reason = LoggingUtils.rootCauseMessage(ex);
        logValidationFailure(request, reason);
        return ResponseEntity.badRequest().body(reason);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        log.warn(
                "Resource not found. timestamp={}, method={}, url={}, exceptionType={}, message={}",
                timestamp(),
                request.getMethod(),
                LoggingUtils.requestUrl(request),
                ex.getClass().getName(),
                LoggingUtils.safe(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(LoggingUtils.safe(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn(
                "Access denied. timestamp={}, method={}, url={}, exceptionType={}, message={}",
                timestamp(),
                request.getMethod(),
                LoggingUtils.requestUrl(request),
                ex.getClass().getName(),
                LoggingUtils.safe(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(LoggingUtils.safe(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("""
                Unexpected exception
                Timestamp      : {}
                HTTP Method    : {}
                Request URL    : {}
                Exception Type : {}
                Message        : {}
                Stacktrace     :
                {}
                """,
                timestamp(),
                request.getMethod(),
                LoggingUtils.requestUrl(request),
                ex.getClass().getName(),
                LoggingUtils.safe(ex.getMessage()),
                LoggingUtils.stackTrace(ex));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected server error.");
    }

    private void logValidationFailure(HttpServletRequest request, String reason) {
        log.warn("""
                Validation Failed
                Timestamp   : {}
                HTTP Method : {}
                Request URL : {}
                Reason      : {}
                """,
                timestamp(),
                request.getMethod(),
                LoggingUtils.requestUrl(request),
                reason);
    }

    private String validationReason(BindingResult bindingResult) {
        return bindingResult.getAllErrors()
                .stream()
                .map(this::validationMessage)
                .collect(Collectors.joining("; "));
    }

    private String validationMessage(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return fieldError.getField() + ": " + fieldError.getDefaultMessage();
        }
        return error.getDefaultMessage();
    }

    private String timestamp() {
        return OffsetDateTime.now(ZoneOffset.UTC).toString();
    }
}
