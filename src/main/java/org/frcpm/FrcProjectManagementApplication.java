// src/main/java/org/frcpm/FrcProjectManagementApplication.java

package org.frcpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot application class for FRC Project Management System.
 * 
 * ✅ FIXED: Removed @EnableJpaRepositories to avoid conflict with DatabaseConfig
 * Spring Boot will auto-configure JPA repositories based on basePackages scan
 */
@SpringBootApplication(scanBasePackages = "org.frcpm")
@EnableAsync
@EnableTransactionManagement
public class FrcProjectManagementApplication {
    
    public static void main(String[] args) {
        // ✅ FIXED: Removed hardcoded profile setting for flexibility
        SpringApplication.run(FrcProjectManagementApplication.class, args);
    }
}