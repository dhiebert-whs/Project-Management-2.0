// src/main/java/org/frcpm/FrcProjectManagementApplication.java

package org.frcpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot application class for FRC Project Management System.
 * 
 * This class bootstraps the Spring Boot application and enables key features:
 * - Async processing for background tasks
 * - Transaction management for database operations
 * - JPA auditing for entity tracking
 */
@SpringBootApplication(scanBasePackages = "org.frcpm")
@EnableAsync
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "org.frcpm.repositories")
public class FrcProjectManagementApplication {
    
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "development");
        SpringApplication.run(FrcProjectManagementApplication.class, args);
    }
}