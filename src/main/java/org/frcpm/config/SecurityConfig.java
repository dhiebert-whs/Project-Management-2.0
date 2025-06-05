package org.frcpm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security configuration for the FRC Project Management System.
 * 
 * This configuration establishes basic authentication for the web application
 * while preparing for future COPPA compliance and role-based access control.
 * 
 * Current Phase 1 Features:
 * - Basic form-based authentication
 * - Simple admin user (configured in application.yml)
 * - Static resource access (CSS, JS, images)
 * - H2 console access for development
 * 
 * Future Phase 2 Features (planned):
 * - COPPA compliance for student data protection
 * - Role-based access (MENTOR, STUDENT, PARENT)
 * - TOTP MFA for mentors
 * - Session timeout differentiation
 * - Audit logging for student data access
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since 2.0.0 (Spring Boot Migration)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures HTTP security for the application.
     * 
     * Phase 1 provides basic authentication while establishing the
     * foundation for advanced security features in Phase 2.
     * 
     * @param http HttpSecurity configuration object
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Allow public access to static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                
                // Allow access to login page
                .requestMatchers("/login", "/error").permitAll()
                
                // Allow H2 console access in development
                .requestMatchers("/h2-console/**").permitAll()
                
                // Require authentication for all other requests
                .anyRequest().authenticated()
            )
            
            // Configure form-based login
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            
            // Configure logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Configure session management
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            
            // Configure headers for H2 console (development only)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Allow H2 console frames
            )
            
            // Temporarily disable CSRF for easier development
            // TODO: Enable CSRF in Phase 2 with proper token handling
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
                .disable()
            );
            
        return http.build();
    }

    /**
     * Password encoder bean for secure password hashing.
     * 
     * Uses BCrypt with default strength (10 rounds) which provides
     * good security while maintaining reasonable performance for
     * FRC team environments.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * TODO: Phase 2 Security Enhancements
     * 
     * The following features will be implemented in Phase 2:
     * 
     * 1. COPPA Compliance Service:
     *    - Student data protection (under 13 years)
     *    - Parental consent management
     *    - Data minimization enforcement
     *    - Audit logging for student data access
     * 
     * 2. Role-Based Access Control:
     *    - MENTOR: Full access with TOTP MFA
     *    - STUDENT: Limited access with session timeout
     *    - PARENT: View-only access to their child's data
     * 
     * 3. Advanced Authentication:
     *    - TOTP-based MFA for mentors
     *    - Session timeout differentiation by role
     *    - Password complexity requirements
     * 
     * 4. Security Auditing:
     *    - Login/logout tracking
     *    - Data access logging
     *    - Security event monitoring
     * 
     * Example future beans:
     * 
     * @Bean
     * public COPPAComplianceService coppaService() {
     *     return new COPPAComplianceServiceImpl();
     * }
     * 
     * @Bean
     * public TOTPService totpService() {
     *     return new TOTPServiceImpl();
     * }
     * 
     * @Bean
     * public SecurityAuditService auditService() {
     *     return new SecurityAuditServiceImpl();
     * }
     */
}