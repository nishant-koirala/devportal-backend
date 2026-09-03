package com.fonepay.devportal.startup;

import java.sql.Connection;

import javax.sql.DataSource;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class MysqlConnectionChecker implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MysqlConnectionChecker.class);

    private final DataSource dataSource;

    public MysqlConnectionChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String @NonNull... args) {
        try (Connection connection = dataSource.getConnection()) {
            log.info("MySQL connected successfully to catalog [{}]", connection.getCatalog());
        } catch (Exception e) {
            log.error("MySQL connection FAILED: {}", e.getMessage(), e);
        }
    }
}
