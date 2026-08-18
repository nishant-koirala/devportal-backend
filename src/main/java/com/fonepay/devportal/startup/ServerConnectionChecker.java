package com.fonepay.devportal.startup;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ServerConnectionChecker implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ServerConnectionChecker.class);

    @Value("${server.port}")
    private String port;

    @Override
    public void run(String @NonNull... args) {
        log.info("Spring Project Started. URL: http://localhost:{}", port);
    }
}