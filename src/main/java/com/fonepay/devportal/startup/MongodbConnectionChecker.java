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
        System.out.println("==================================");
        try {
            // Verify MongoDB connection with ping command
            Document pingCommand = new Document("ping", 1);
            Document response = mongoTemplate.getDb().runCommand(pingCommand);
            String dbName = mongoTemplate.getDb().getName();

            System.out.println(" MongoDB Connected Successfully!");
            System.out.println(" Database : " + dbName);
            System.out.println(" Status   : " + response.get("ok"));
            log.info("MongoDB connected successfully to database [{}]", dbName);
        } catch (Exception e) {
            System.err.println(" MongoDB Connection FAILED!");
            System.err.println(" Error    : " + e.getMessage());
            log.error("Failed to connect to MongoDB: ", e);
        }
        System.out.println("==================================");
    }
}
