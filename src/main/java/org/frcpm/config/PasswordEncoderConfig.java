// src/main/java/org/frcpm/config/PasswordEncoderConfig.java

package org.frcpm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Separate configuration for PasswordEncoder to break circular dependency.
 * 
 * ✅ FIXES: Circular dependency between SecurityConfig and UserServiceImpl
 * By extracting PasswordEncoder to its own config, we eliminate the cycle:
 * SecurityConfig → UserDetailsServiceImpl → UserServiceImpl → PasswordEncoder
 */
@Configuration
public class PasswordEncoderConfig {
    
    /**
     * BCrypt password encoder with strength 12 for enhanced security.
     * This is now independent of SecurityConfig to avoid circular dependencies.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}