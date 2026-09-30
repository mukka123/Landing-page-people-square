package com.politicalops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the modular monolith (HLD section 5: "one deployable Spring Boot
 * application containing separate modules ... package boundaries"). AI orchestration,
 * notifications, document processing and analytics workers are deployed as separate
 * services from day one and are NOT part of this application.
 */
@SpringBootApplication
public class PoliticalOpsApplication {
    public static void main(String[] args) {
        SpringApplication.run(PoliticalOpsApplication.class, args);
    }
}
