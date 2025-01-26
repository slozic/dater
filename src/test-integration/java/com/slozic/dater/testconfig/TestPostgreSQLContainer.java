package com.slozic.dater.testconfig;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TestPostgreSQLContainer extends PostgreSQLContainer<TestPostgreSQLContainer>
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final String IMAGE_VERSION = PostgreSQLContainer.IMAGE + ":11.2";

    public TestPostgreSQLContainer() {
        super(IMAGE_VERSION);
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", this.getJdbcUrl());
        System.setProperty("DB_USERNAME", this.getUsername());
        System.setProperty("DB_PASSWORD", this.getPassword());
        // Ensure the UUID extension is installed after the container starts
        installUuidExtension();
    }

    private void installUuidExtension() {
        try (Connection conn = DriverManager.getConnection(this.getJdbcUrl(), this.getUsername(), this.getPassword());
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to install uuid-ossp extension", e);
        }
    }

    @Override
    public void stop() {
        // do nothing, JVM handles shut down
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        this.start();
        TestPropertyValues.of(
                "spring.datasource.url=" + this.getJdbcUrl(),
                "spring.datasource.username=" + this.getUsername(),
                "spring.datasource.password=" + this.getPassword())
                .applyTo(applicationContext.getEnvironment());
    }
}
