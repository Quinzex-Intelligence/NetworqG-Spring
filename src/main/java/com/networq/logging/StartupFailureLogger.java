package com.networq.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

public class StartupFailureLogger implements ApplicationListener<ApplicationFailedEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupFailureLogger.class);

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        Throwable exception = event.getException();

        log.error("""
                Application startup failed.
                Reason     : {}
                Stacktrace :
                {}
                """,
                LoggingUtils.rootCauseMessage(exception),
                LoggingUtils.stackTrace(exception));

        ConfigurableApplicationContext context = event.getApplicationContext();
        if (context == null) {
            return;
        }

        Environment environment = context.getEnvironment();
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        LoggingUtils.JdbcUrlDetails jdbcUrlDetails = LoggingUtils.jdbcUrlDetails(datasourceUrl);

        log.error("""
                Startup failure diagnostics
                Application          : {}
                Active Profiles      : {}
                Server Port          : {}
                Datasource URL       : {}
                Database Host        : {}
                Database Name        : {}
                Datasource Username  : {}
                Hibernate Dialect    : {}
                ddl-auto             : {}
                AWS Region           : {}
                S3 Bucket            : {}
                Mail Host            : {}
                Mail Port            : {}
                Mail Username        : {}
                Google OAuth Enabled : {}
                """,
                LoggingUtils.safe(environment.getProperty("spring.application.name")),
                activeProfiles(environment),
                environment.getProperty("server.port", "8080"),
                LoggingUtils.safe(datasourceUrl),
                jdbcUrlDetails.host(),
                jdbcUrlDetails.database(),
                LoggingUtils.safe(environment.getProperty("spring.datasource.username")),
                LoggingUtils.safe(environment.getProperty(
                        "spring.jpa.properties.hibernate.dialect",
                        environment.getProperty("spring.jpa.database-platform", "auto-detected"))),
                LoggingUtils.safe(environment.getProperty("spring.jpa.hibernate.ddl-auto")),
                LoggingUtils.safe(environment.getProperty("aws.region")),
                LoggingUtils.safe(environment.getProperty("aws.s3.bucket-name")),
                LoggingUtils.safe(environment.getProperty("spring.mail.host")),
                LoggingUtils.safe(environment.getProperty("spring.mail.port")),
                LoggingUtils.safe(environment.getProperty("spring.mail.username")),
                googleOAuthEnabled(environment));

        if (datasourceUrl != null && !datasourceUrl.isBlank()) {
            log.error("""
                    Failed to establish database connection.
                    Datasource URL : {}
                    Reason         : {}
                    Stacktrace     :
                    {}
                    """,
                    LoggingUtils.safe(datasourceUrl),
                    LoggingUtils.rootCauseMessage(exception),
                    LoggingUtils.stackTrace(exception));
        }
    }

    private String activeProfiles(Environment environment) {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return "default";
        }
        return String.join(", ", activeProfiles);
    }

    private boolean googleOAuthEnabled(Environment environment) {
        String clientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id");
        return clientId != null && !clientId.isBlank() && !clientId.startsWith("${");
    }
}
