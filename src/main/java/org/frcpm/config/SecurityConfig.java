// src/main/java/org/frcpm/config/SecurityConfig.java (Enhanced for Phase 2B)

package org.frcpm.config;

import org.frcpm.security.UserDetailsServiceImpl;
import org.frcpm.services.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Enhanced Security Configuration for FRC Project Management System.
 * 
 * Provides comprehensive security including:
 * - COPPA compliance for users under 13
 * - Multi-factor authentication for mentors/admins
 * - Role-based access control
 * - Session management with role-specific timeouts
 * - Comprehensive audit logging
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final UserDetailsServiceImpl userDetailsService;
    private final AuditService auditService;
    
    @Autowired
    public SecurityConfig(UserDetailsServiceImpl userDetailsService, AuditService auditService) {
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Public resources
                .requestMatchers("/css/**", "/js/**", "/images/**", 
                               "/manifest.json", "/sw.js", "/favicon.ico").permitAll()
                
                // Authentication and registration pages
                .requestMatchers("/login", "/register", "/error").permitAll()
                
                // COPPA compliance pages
                .requestMatchers("/coppa/**").permitAll()
                
                // MFA setup and verification (authenticated users only)
                .requestMatchers("/mfa/**").authenticated()
                
                // Admin-only areas
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Mentor and admin areas
                .requestMatchers("/mentor/**", "/reports/**").hasAnyRole("MENTOR", "ADMIN")
                
                // API endpoints (require authentication)
                .requestMatchers("/api/**").authenticated()
                
                // H2 console (development only)
                .requestMatchers("/h2-console/**").permitAll()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Form-based login configuration
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            
            // Logout configuration
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(logoutSuccessHandler())
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .and()
                .sessionFixation().changeSessionId()
            )
            
            // Remember me functionality
            .rememberMe(remember -> remember
                .key("frc-project-management-remember-me")
                .tokenValiditySeconds(86400) // 24 hours
                .userDetailsService(userDetailsService)
                .rememberMeParameter("remember-me")
            )
            
            // Authentication provider
            .authenticationProvider(authenticationProvider())
            
            // CSRF configuration
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
            )
            
            // Headers configuration
            .headers(headers -> headers
                .frameOptions().sameOrigin() // For H2 console
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            );
            
        return http.build();
    }
    
    /**
     * Custom authentication success handler with COPPA compliance and MFA support.
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            org.frcpm.security.UserPrincipal principal = 
                (org.frcpm.security.UserPrincipal) authentication.getPrincipal();
            
            // Log successful login
            auditService.logAction(
                principal.getUser(),
                "LOGIN_SUCCESS",
                "User logged in successfully"
            );
            
            // Update last login time
            principal.getUser().setLastLogin(java.time.LocalDateTime.now());
            
            // Set session timeout based on user role
            request.getSession().setMaxInactiveInterval(principal.getSessionTimeout());
            
            // Determine redirect based on user state
            String redirectUrl = determinePostLoginRedirect(principal);
            
            response.sendRedirect(redirectUrl);
        };
    }
    
    /**
     * Custom authentication failure handler with audit logging.
     */
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String username = request.getParameter("username");
            String ipAddress = getClientIpAddress(request);
            
            // Log failed login attempt
            auditService.logLoginAttempt(username, ipAddress, false);
            
            // Determine failure reason for user feedback
            String errorParam = "error=true";
            
            if (exception.getMessage().contains("parental consent")) {
                errorParam = "error=coppa";
            } else if (exception.getMessage().contains("locked")) {
                errorParam = "error=locked";
            } else if (exception.getMessage().contains("disabled")) {
                errorParam = "error=disabled";
            }
            
            response.sendRedirect("/login?" + errorParam);
        };
    }
    
    /**
     * Custom logout success handler with audit logging.
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication != null) {
                org.frcpm.security.UserPrincipal principal = 
                    (org.frcpm.security.UserPrincipal) authentication.getPrincipal();
                
                auditService.logLogout(principal.getUser());
            }
            
            response.sendRedirect("/login?logout=true");
        };
    }
    
    /**
     * DAO authentication provider with enhanced security.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false); // For better audit logging
        return authProvider;
    }
    
    /**
     * Authentication manager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * Password encoder with strong settings.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // High strength for security
    }
    
    /**
     * HTTP session event publisher for session monitoring.
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
    
    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================
    
    /**
     * Determines the appropriate redirect URL after successful login.
     */
    private String determinePostLoginRedirect(org.frcpm.security.UserPrincipal principal) {
        // Check if MFA is required but not enabled
        if (principal.requiresMFA() && !principal.isMFAEnabled()) {
            return "/mfa/setup";
        }
        
        // Check if MFA verification is needed
        if (principal.requiresMFA() && principal.isMFAEnabled()) {
            // Check if already verified in this session
            // This would be tracked in session attributes
            return "/mfa/verify";
        }
        
        // Check for COPPA compliance issues
        if (principal.requiresCOPPACompliance() && !principal.hasParentalConsent()) {
            return "/coppa/consent-required";
        }
        
        // Default redirect to dashboard
        return "/dashboard";
    }
    
    /**
     * Gets the client IP address from the request.
     */
    private String getClientIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}