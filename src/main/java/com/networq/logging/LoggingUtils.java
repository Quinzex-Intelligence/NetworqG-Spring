package com.networq.logging;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LoggingUtils {

    private static final String NOT_CONFIGURED = "not configured";
    private static final Pattern BASIC_AUTH_IN_URL = Pattern.compile("(://[^:/?#]+):([^@/?#]+)@");
    private static final Pattern SENSITIVE_ASSIGNMENT = Pattern.compile(
            "(?i)(password|passwd|pwd|secret|token|access[_-]?key|secret[_-]?key|api[_-]?key|client[_-]?secret|jwt|authorization)=([^&;\\s]+)");
    private static final Pattern JDBC_HOST = Pattern.compile("jdbc:[^:]+://([^/;?]+)");
    private static final Pattern DATABASE_NAME = Pattern.compile("(?i)(?:databaseName|database)=([^;&?]+)");

    private LoggingUtils() {
    }

    public static String requestUrl(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + sanitize(queryString);
    }

    public static String safe(String value) {
        if (value == null || value.isBlank()) {
            return NOT_CONFIGURED;
        }
        return sanitize(value);
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }

        String sanitized = BASIC_AUTH_IN_URL.matcher(value).replaceAll("$1:****@");
        return SENSITIVE_ASSIGNMENT.matcher(sanitized).replaceAll("$1=****");
    }

    public static String rootCauseMessage(Throwable throwable) {
        if (throwable == null) {
            return "unknown";
        }

        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }

        String message = rootCause.getMessage();
        return message == null || message.isBlank()
                ? rootCause.getClass().getName()
                : sanitize(message);
    }

    public static String stackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        appendStackTrace(builder, throwable, "");
        return sanitize(builder.toString());
    }

    public static JdbcUrlDetails jdbcUrlDetails(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return new JdbcUrlDetails(NOT_CONFIGURED, NOT_CONFIGURED);
        }

        JdbcUrlDetails uriDetails = parseUriStyleJdbcUrl(jdbcUrl);
        if (uriDetails.hasAnyValue()) {
            return uriDetails;
        }

        String host = match(JDBC_HOST, jdbcUrl);
        if (host != null && host.contains("@")) {
            host = host.substring(host.indexOf('@') + 1);
        }

        String database = match(DATABASE_NAME, jdbcUrl);

        return new JdbcUrlDetails(
                host == null || host.isBlank() ? NOT_CONFIGURED : sanitize(host),
                database == null || database.isBlank() ? NOT_CONFIGURED : sanitize(database));
    }

    private static JdbcUrlDetails parseUriStyleJdbcUrl(String jdbcUrl) {
        try {
            String uriValue = jdbcUrl.startsWith("jdbc:")
                    ? jdbcUrl.substring("jdbc:".length())
                    : jdbcUrl;

            URI uri = URI.create(uriValue);
            String host = uri.getHost();
            if (host != null && uri.getPort() > -1) {
                host = host + ":" + uri.getPort();
            }

            String database = databaseFromPath(uri.getPath());

            return new JdbcUrlDetails(
                    host == null || host.isBlank() ? NOT_CONFIGURED : sanitize(host),
                    database == null || database.isBlank() ? NOT_CONFIGURED : sanitize(database));
        } catch (IllegalArgumentException ex) {
            return new JdbcUrlDetails(NOT_CONFIGURED, NOT_CONFIGURED);
        }
    }

    private static String databaseFromPath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return null;
        }

        String database = path.startsWith("/") ? path.substring(1) : path;
        int slash = database.indexOf('/');
        if (slash >= 0) {
            database = database.substring(0, slash);
        }
        return database;
    }

    private static String match(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static void appendStackTrace(StringBuilder builder, Throwable throwable, String prefix) {
        builder.append(prefix).append(throwable.getClass().getName());
        String message = throwable.getMessage();
        if (message != null && !message.isBlank()) {
            builder.append(": ").append(message);
        }
        builder.append(System.lineSeparator());

        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            builder.append(prefix).append("\tat ").append(stackTraceElement).append(System.lineSeparator());
        }

        for (Throwable suppressed : throwable.getSuppressed()) {
            builder.append(prefix).append("Suppressed: ");
            appendStackTrace(builder, suppressed, prefix + "\t");
        }

        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            builder.append(prefix).append("Caused by: ");
            appendStackTrace(builder, cause, prefix);
        }
    }

    public record JdbcUrlDetails(String host, String database) {
        private boolean hasAnyValue() {
            return !NOT_CONFIGURED.equals(host) || !NOT_CONFIGURED.equals(database);
        }
    }
}
