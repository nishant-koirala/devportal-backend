package com.fonepay.devportal.startup;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ServerConnectionChecker implements CommandLineRunner {

    @Value("${server.port}")
    private String port;

    @Override
    public void run(String @NonNull... args) {

        System.out.println("==================================");
        System.out.println(" Spring Project Started");
        System.out.println(" URL: http://localhost:" + port);
        System.out.println("==================================");
    }
}