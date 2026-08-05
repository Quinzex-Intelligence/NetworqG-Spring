package com.networq.logging;

import com.networq.NetworqApplication;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupDiagnosticsLogger {

    private static final int REDIS_CONNECT_TIMEOUT_MS = 2_000;

    private final Environment environment;
    private final DataSource dataSource;
    private final ObjectProvider<BuildProperties> buildProperties;
    private final ObjectProvider<EntityManagerFactory> entityManagerFactory;

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupDiagnostics() {
        logApplicationStartup();
        logDatabaseDiagnostics();
        logAwsDiagnostics();
        logMailDiagnostics();
        logRedisDiagnosticsIfConfigured();
        logOAuthDiagnostics();
    }

    private void logApplicationStartup() {
        log.info("""
                ==========================================
                NetworQ Backend Started
                Application  : {}
                Version      : {}
                Environment  : {}
                Java         : {}
                Spring Boot  : {}
                Port         : {}
                ==========================================
                """,
                LoggingUtils.safe(environment.getProperty("spring.application.name")),
                applicationVersion(),
                activeProfiles(),
                System.getProperty("java.version"),
                SpringBootVersion.getVersion(),
                serverPort());
    }

    private void logDatabaseDiagnostics() {
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        LoggingUtils.JdbcUrlDetails jdbcUrlDetails = LoggingUtils.jdbcUrlDetails(datasourceUrl);

        log.info("""
                Database configuration
                Datasource URL    : {}
                Database Host     : {}
                Database Name     : {}
                Username          : {}
                Driver            : {}
                Hibernate Dialect : {}
                ddl-auto          : {}
                """,
                LoggingUtils.safe(datasourceUrl),
                jdbcUrlDetails.host(),
                jdbcUrlDetails.database(),
                LoggingUtils.safe(environment.getProperty("spring.datasource.username")),
                LoggingUtils.safe(environment.getProperty("spring.datasource.driver-class-name", "auto-detected")),
                hibernateDialect(),
                LoggingUtils.safe(environment.getProperty("spring.jpa.hibernate.ddl-auto")));

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            log.info("""
                    Database connection established successfully.
                    Database : {}
                    Version  : {}
                    Driver   : {}
                    """,
                    metadata.getDatabaseProductName(),
                    metadata.getDatabaseProductVersion(),
                    metadata.getDriverName());
        } catch (Exception ex) {
            log.error("""
                    Failed to establish database connection.
                    Datasource URL : {}
                    Reason         : {}
                    Stacktrace     :
                    {}
                    """,
                    LoggingUtils.safe(datasourceUrl),
                    LoggingUtils.rootCauseMessage(ex),
                    LoggingUtils.stackTrace(ex));
        }
    }

    private void logAwsDiagnostics() {
        log.info("""
                AWS configuration
                AWS Region : {}
                S3 Bucket  : {}
                """,
                LoggingUtils.safe(environment.getProperty("aws.region")),
                LoggingUtils.safe(environment.getProperty("aws.s3.bucket-name")));
    }

    private void logMailDiagnostics() {
        log.info("""
                Mail configuration
                Mail Host     : {}
                Mail Port     : {}
                Mail Username : {}
                """,
                LoggingUtils.safe(environment.getProperty("spring.mail.host")),
                LoggingUtils.safe(environment.getProperty("spring.mail.port")),
                LoggingUtils.safe(environment.getProperty("spring.mail.username")));
    }

    private void logRedisDiagnosticsIfConfigured() {
        if (!redisConfigured()) {
            return;
        }

        String host = redisProperty("host", "localhost");
        String configuredPort = redisProperty("port", "6379");
        int port;
        try {
            port = Integer.parseInt(configuredPort);
        } catch (NumberFormatException ex) {
            log.error("""
                    Invalid Redis port configuration.
                    Redis Host : {}
                    Redis Port : {}
                    Stacktrace :
                    {}
                    """,
                    LoggingUtils.safe(host),
                    LoggingUtils.safe(configuredPort),
                    LoggingUtils.stackTrace(ex));
            return;
        }

        log.info("""
                Redis configuration
                Redis Host : {}
                Redis Port : {}
                """, LoggingUtils.safe(host), port);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), REDIS_CONNECT_TIMEOUT_MS);
            log.info("Redis connected successfully.");
        } catch (Exception ex) {
            log.error("""
                            Failed to connect to Redis.
                            Redis Host : {}
                            Redis Port : {}
                            Reason     : {}
                            Stacktrace :
                            {}
                            """,
                    LoggingUtils.safe(host),
                    port,
                    LoggingUtils.rootCauseMessage(ex),
                    LoggingUtils.stackTrace(ex));
        }
    }

    private void logOAuthDiagnostics() {
        log.info("Google OAuth Enabled : {}", googleOAuthEnabled());
    }

    private String applicationVersion() {
        BuildProperties properties = buildProperties.getIfAvailable();
        if (properties != null && properties.getVersion() != null) {
            return properties.getVersion();
        }

        Package applicationPackage = NetworqApplication.class.getPackage();
        String implementationVersion = applicationPackage.getImplementationVersion();
        if (implementationVersion != null && !implementationVersion.isBlank()) {
            return implementationVersion;
        }

        return environment.getProperty("info.app.version", "unknown");
    }

    private String activeProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return "default";
        }
        return String.join(", ", activeProfiles);
    }

    private String serverPort() {
        return environment.getProperty(
                "local.server.port",
                environment.getProperty("server.port", "8080"));
    }

    private String hibernateDialect() {
        EntityManagerFactory factory = entityManagerFactory.getIfAvailable();
        if (factory != null) {
            try {
                SessionFactoryImplementor sessionFactory = factory.unwrap(SessionFactoryImplementor.class);
                return sessionFactory.getJdbcServices().getDialect().getClass().getSimpleName();
            } catch (RuntimeException ex) {
                log.debug("Unable to inspect Hibernate dialect from EntityManagerFactory.", ex);
            }
        }

        return LoggingUtils.safe(environment.getProperty(
                "spring.jpa.properties.hibernate.dialect",
                environment.getProperty("spring.jpa.database-platform", "auto-detected")));
    }

    private boolean redisConfigured() {
        return Arrays.stream(new String[]{
                        "spring.data.redis.host",
                        "spring.redis.host",
                        "spring.data.redis.port",
                        "spring.redis.port"
                })
                .anyMatch(environment::containsProperty);
    }

    private String redisProperty(String propertyName, String defaultValue) {
        String springDataRedisValue = environment.getProperty("spring.data.redis." + propertyName);
        if (springDataRedisValue != null && !springDataRedisValue.isBlank()) {
            return springDataRedisValue;
        }
        return environment.getProperty("spring.redis." + propertyName, defaultValue);
    }

    private boolean googleOAuthEnabled() {
        String clientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id");
        return clientId != null && !clientId.isBlank() && !clientId.startsWith("${");
    }
}
