package com.fonepay.devportal.startup;

import org.bson.Document;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class MongodbConnectionChecker implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MongodbConnectionChecker.class);

    private final MongoTemplate mongoTemplate;

    public MongodbConnectionChecker(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String @NonNull... args) {
        try {
            // Verify MongoDB connection with ping command
            Document pingCommand = new Document("ping", 1);
            Document response = mongoTemplate.getDb().runCommand(pingCommand);
            String dbName = mongoTemplate.getDb().getName();

            log.info("MongoDB connected successfully to database [{}] with status [{}]", dbName, response.get("ok"));
        } catch (Exception e) {
            log.error("MongoDB connection FAILED: {}", e.getMessage(), e);
        }
    }
}
