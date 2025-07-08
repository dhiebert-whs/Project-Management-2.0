// src/main/java/org/frcpm/FrcProjectManagementApplication.java

package org.frcpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot application class for FRC Project Management System.
 * 
 * ✅ FIXED: Removed @EnableJpaAuditing to avoid conflict with DatabaseConfig
 * JPA auditing is configured in DatabaseConfig.java instead
 */
@SpringBootApplication(scanBasePackages = "org.frcpm")
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class FrcProjectManagementApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FrcProjectManagementApplication.class, args);
    }
}