// src/test/java/org/frcpm/security/SecurityIntegrationTest.java (FIXED)

package org.frcpm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ✅ FIXED: Security integration tests with correct expectations
 * 
 * Fixed Issues:
 * - API endpoints return 403 (CSRF) not 302 when unauthenticated AND posting
 * - GET requests to authenticated endpoints return 302 redirect
 * - POST requests without CSRF return 403 Forbidden
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B Testing - FIXED
 */
@WebMvcTest
@ActiveProfiles("test")
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                    .requestMatchers("/login", "/register", "/coppa/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/mentor/**").hasAnyRole("MENTOR", "ADMIN")
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                    .loginPage("/login")
                    .permitAll()
                )
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .permitAll()
                );
                
            return http.build();
        }
        
        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails student = User.builder()
                .username("teststudent")
                .password(passwordEncoder().encode("password123"))
                .roles("STUDENT")
                .build();
                
            UserDetails mentor = User.builder()
                .username("testmentor")
                .password(passwordEncoder().encode("password123"))
                .roles("MENTOR")
                .build();
                
            UserDetails admin = User.builder()
                .username("testadmin")
                .password(passwordEncoder().encode("password123"))
                .roles("ADMIN")
                .build();
                
            UserDetails parent = User.builder()
                .username("testparent")
                .password(passwordEncoder().encode("password123"))
                .roles("PARENT")
                .build();
                
            return new InMemoryUserDetailsManager(student, mentor, admin, parent);
        }
        
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
    
    @BeforeEach
    void setUp() {
        // Test setup handled by @WithMockUser annotations
    }
    
    @Test
    @DisplayName("Should redirect unauthenticated users to login")
    void shouldRedirectUnauthenticatedUsersToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
    
    @Test
    @DisplayName("Should allow access to public resources")
    void shouldAllowAccessToPublicResources() throws Exception {
        // Static resources should be accessible
        mockMvc.perform(get("/css/app.css"))
                .andExpect(status().isNotFound()); // 404 is fine - means security passed
        
        mockMvc.perform(get("/js/app.js"))
                .andExpect(status().isNotFound()); // 404 is fine - means security passed
        
        mockMvc.perform(get("/images/logo.png"))
                .andExpect(status().isNotFound()); // 404 is fine - means security passed
    }
    
    @Test
    @DisplayName("Should handle login form submission")
    void shouldHandleLoginFormSubmission() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "teststudent")
                .param("password", "password123")
                .param("ageVerification", "13plus")
                .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
    
    @Test
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("Students should not access admin areas")
    void studentsShouldNotAccessAdminAreas() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(get("/admin/settings"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "testmentor", roles = "MENTOR")
    @DisplayName("Mentors should not access admin-only areas")
    void mentorsShouldNotAccessAdminOnlyAreas() throws Exception {
        mockMvc.perform(get("/admin/system"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "testadmin", roles = "ADMIN")
    @DisplayName("Admins should access admin areas")
    void adminsShouldAccessAdminAreas() throws Exception {
        // Admin should be able to access admin URLs (even if they return 404, they're not forbidden)
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isNotFound()); // Not forbidden - security passed
    }
    
    @Test
    @WithMockUser(username = "testparent", roles = "PARENT")
    @DisplayName("Parents should not access mentor areas")
    void parentsShouldNotAccessMentorAreas() throws Exception {
        mockMvc.perform(get("/mentor/reports"))
                .andExpect(status().isForbidden());
        
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("API GET endpoints should require authentication")
    void apiGetEndpointsShouldRequireAuthentication() throws Exception {
        // ✅ FIXED: Corrected method name from andExpected to andExpect
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
    
    @Test
    @DisplayName("API POST endpoints should reject without CSRF")
    void apiPostEndpointsShouldRejectWithoutCSRF() throws Exception {
        // ✅ FIXED: POST requests without authentication AND without CSRF return 403
        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isForbidden()); // This is correct - CSRF protection
    }
    
    @Test
    @WithMockUser(username = "teststudent", roles = "STUDENT")
    @DisplayName("Should reject POST requests without CSRF token")
    void shouldRejectPostRequestsWithoutCSRFToken() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "testmentor", roles = "MENTOR")
    @DisplayName("Should accept POST requests with CSRF token")
    void shouldAcceptPostRequestsWithCSRFToken() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content("{\"title\":\"Test Task\",\"projectId\":1}")
                .with(csrf()))
                .andExpect(status().isNotFound()); // 404 is fine - means security and CSRF passed
    }
}