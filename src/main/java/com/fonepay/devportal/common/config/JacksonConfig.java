package com.fonepay.devportal.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {

    /**
     * Explicitly defines the Jackson ObjectMapper bean required by Spring Security 
     * and other components. It also configures the mapper to serialize Java 8 
     * Dates (e.g., LocalDateTime) as readable ISO strings instead of numeric arrays.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Enables Java 8 Date/Time support
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Formats dates as strings
        return mapper;
    }
}
