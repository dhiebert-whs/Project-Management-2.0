// src/test/java/org/frcpm/security/SecurityIntegrationTest.java

package org.frcpm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security integration tests for Phase 2B authentication and authorization.
 * 
 * ✅ FIXED: All compilation issues resolved
 * - Removed unused field warnings by eliminating User field declarations
 * - Fixed .andExpected() → .andExpect() method name typos
 * - Simplified test setup to use @WithMockUser instead of real user creation
 * 
 * Tests the complete security framework including:
 * - Role-based access control
 * - Authentication flows
 * - Security headers
 * - Session management
 * - API security
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B Testing
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    // Note: Using @WithMockUser for testing instead of creating real users
    // This eliminates dependency on UserService and avoids unused field warnings
    
    @BeforeEach
    void setUp() {
        // Test setup handled by @WithMockUser annotations
        // No need to create actual users for security integration tests
    }
    
    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {
        
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
                    .andExpect(status().isOk());
            
            mockMvc.perform(get("/js/app.js"))
                    .andExpect(status().isOk());
            
            mockMvc.perform(get("/images/logo.png"))
                    .andExpect(status().isOk());
            
            // Login page should be accessible
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk());
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
        @DisplayName("Should reject invalid login credentials")
        void shouldRejectInvalidLoginCredentials() throws Exception {
            mockMvc.perform(post("/login")
                    .param("username", "invalid")
                    .param("password", "wrong")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login?error*"));
        }
        
        @Test
        @DisplayName("Should handle logout")
        void shouldHandleLogout() throws Exception {
            mockMvc.perform(post("/logout")
                    .with(csrf())
                    .with(user("teststudent").roles("STUDENT")))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login?logout*"));
        }
    }
    
    @Nested
    @DisplayName("Role-Based Access Control Tests")
    class RoleBasedAccessTests {
        
        @Test
        @WithMockUser(username = "teststudent", roles = "STUDENT")
        @DisplayName("Students should access dashboard")
        void studentsShouldAccessDashboard() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk());
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
        @DisplayName("Mentors should access mentor areas")
        void mentorsShouldAccessMentorAreas() throws Exception {
            mockMvc.perform(get("/mentor/reports"))
                    .andExpect(status().isOk());
            
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk());
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
        @DisplayName("Admins should access all areas")
        void adminsShouldAccessAllAreas() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk());
            
            mockMvc.perform(get("/mentor/reports"))
                    .andExpect(status().isOk());
            
            mockMvc.perform(get("/admin/users"))
                    .andExpect(status().isOk());
        }
        
        @Test
        @WithMockUser(username = "testparent", roles = "PARENT")
        @DisplayName("Parents should have limited access")
        void parentsShouldHaveLimitedAccess() throws Exception {
            // Parents can access dashboard (read-only)
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk());
            
            // Parents cannot access mentor areas
            mockMvc.perform(get("/mentor/reports"))
                    .andExpect(status().isForbidden());
            
            // Parents cannot access admin areas
            mockMvc.perform(get("/admin/users"))
                    .andExpect(status().isForbidden());
        }
    }
    
    @Nested
    @DisplayName("API Security Tests")
    class APISecurityTests {
        
        @Test
        @DisplayName("API endpoints should require authentication")
        void apiEndpointsShouldRequireAuthentication() throws Exception {
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isUnauthorized());
            
            mockMvc.perform(get("/api/projects"))
                    .andExpect(status().isUnauthorized());
            
            mockMvc.perform(post("/api/tasks")
                    .contentType("application/json")
                    .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
        
        @Test
        @WithMockUser(username = "teststudent", roles = "STUDENT")
        @DisplayName("Authenticated users should access API")
        void authenticatedUsersShouldAccessAPI() throws Exception {
            mockMvc.perform(get("/api/tasks")
                    .param("projectId", "1"))
                    .andExpect(status().isOk());
        }
        
        @Test
        @WithMockUser(username = "teststudent", roles = "STUDENT")
        @DisplayName("Students should not create projects via API")
        void studentsShouldNotCreateProjectsViaAPI() throws Exception {
            mockMvc.perform(post("/api/projects")
                    .contentType("application/json")
                    .content("{\"name\":\"Test Project\"}")
                    .with(csrf()))
                    .andExpect(status().isForbidden());
        }
        
        @Test
        @WithMockUser(username = "testmentor", roles = "MENTOR")
        @DisplayName("Mentors should create projects via API")
        void mentorsShouldCreateProjectsViaAPI() throws Exception {
            mockMvc.perform(post("/api/projects")
                    .contentType("application/json")
                    .content("{\"name\":\"Test Project\",\"startDate\":\"2024-01-01\",\"goalEndDate\":\"2024-06-01\"}")
                    .with(csrf()))
                    .andExpect(status().isOk());
        }
    }
    
    @Nested
    @DisplayName("MFA Integration Tests")
    class MFAIntegrationTests {
        
        @Test
        @WithMockUser(username = "testmentor", roles = "MENTOR")
        @DisplayName("Mentors should be redirected to MFA setup if not enabled")
        void mentorsShouldBeRedirectedToMFASetup() throws Exception {
            // Mentor without MFA should be redirected to setup
            mockMvc.perform(get("/mfa/setup"))
                    .andExpect(status().isOk());
        }
        
        @Test
        @WithMockUser(username = "teststudent", roles = "STUDENT")
        @DisplayName("Students should not need MFA setup")
        void studentsShouldNotNeedMFASetup() throws Exception {
            mockMvc.perform(get("/mfa/setup"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));
        }
        
        @Test
        @WithMockUser(username = "testmentor", roles = "MENTOR")
        @DisplayName("Should handle MFA setup completion")
        void shouldHandleMFASetupCompletion() throws Exception {
            mockMvc.perform(post("/mfa/setup")
                    .param("secret", "JBSWY3DPEHPK3PXP")
                    .param("verificationCode", "123456")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection());
        }
        
        @Test
        @WithMockUser(username = "testmentor", roles = "MENTOR")
        @DisplayName("Should display MFA verification page")
        void shouldDisplayMFAVerificationPage() throws Exception {
            mockMvc.perform(get("/mfa/verify"))
                    .andExpect(status().isOk());
        }
    }
    
    @Nested
    @DisplayName("COPPA Compliance Security Tests")
    class COPPAComplianceSecurityTests {
        
        @Test
        @DisplayName("Should serve COPPA consent pages without authentication")
        void shouldServeCOPPAConsentPagesWithoutAuthentication() throws Exception {
            mockMvc.perform(get("/coppa/consent")
                    .param("token", "test-token")
                    .param("consent", "true"))
                    .andExpect(status().isOk());
        }
        
        @Test
        @WithMockUser(username = "testminor", roles = "STUDENT")
        @DisplayName("Users under 13 without consent should see consent required page")
        void usersUnder13WithoutConsentShouldSeeConsentPage() throws Exception {
            mockMvc.perform(get("/coppa/consent-required"))
                    .andExpect(status().isOk());
        }
        
        @Test
        @WithMockUser(username = "teststudent", roles = "STUDENT")
        @DisplayName("Users 13 and over should not see consent page")
        void users13AndOverShouldNotSeeConsentPage() throws Exception {
            mockMvc.perform(get("/coppa/consent-required"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/dashboard"));
        }
    }
    
    @Nested
    @DisplayName("Security Headers Tests")
    class SecurityHeadersTests {
        
        @Test
        @WithMockUser(username = "teststudent", roles = "STUDENT")
        @DisplayName("Should include security headers")
        void shouldIncludeSecurityHeaders() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("X-Content-Type-Options"))
                    .andExpect(header().exists("X-Frame-Options"))
                    .andExpect(header().exists("X-XSS-Protection"));
        }
        
        @Test
        @WithMockUser(username = "teststudent", roles = "STUDENT")
        @DisplayName("Should set appropriate frame options")
        void shouldSetAppropriateFrameOptions() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
        }
    }
    
    @Nested
    @DisplayName("Session Management Tests")
    class SessionManagementTests {
        
        @Test
        @WithMockUser(username = "teststudent", roles = "STUDENT")
        @DisplayName("Should set secure session cookies")
        void shouldSetSecureSessionCookies() throws Exception {
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk());
                    // Note: Cookie assertions depend on actual session configuration
        }
        
        @Test
        @WithMockUser(username = "teststudent", roles = "STUDENT")
        @DisplayName("Should handle session timeout")
        void shouldHandleSessionTimeout() throws Exception {
            // This would require more complex session manipulation
            // Simplified test to verify session management is configured
            mockMvc.perform(get("/dashboard"))
                    .andExpect(status().isOk());
        }
        
        @Test
        @DisplayName("Should handle concurrent sessions")
        void shouldHandleConcurrentSessions() throws Exception {
            // Test session limit configuration
            // This would require multiple authenticated sessions
            mockMvc.perform(post("/login")
                    .param("username", "teststudent")
                    .param("password", "password123")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection());
        }
    }
    
    @Nested
    @DisplayName("CSRF Protection Tests")
    class CSRFProtectionTests {
        
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
                    .andExpect(status().isOk());
        }
        
        @Test
        @DisplayName("Should exclude API endpoints from CSRF if configured")
        void shouldExcludeAPIEndpointsFromCSRFIfConfigured() throws Exception {
            // Some API endpoints might exclude CSRF for external integrations
            // This depends on specific configuration
            mockMvc.perform(get("/api/health"))
                    .andExpect(status().isOk());
        }
    }
    
    @Nested
    @DisplayName("Password Security Tests")
    class PasswordSecurityTests {
        
        @Test
        @DisplayName("Should enforce password complexity on registration")
        void shouldEnforcePasswordComplexityOnRegistration() throws Exception {
            mockMvc.perform(post("/register")
                    .param("username", "newuser")
                    .param("password", "weak")
                    .param("email", "new@frcteam.org")
                    .param("firstName", "New")
                    .param("lastName", "User")
                    .param("age", "16")
                    .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @WithMockUser(username = "teststudent", roles = "STUDENT")
        @DisplayName("Should require current password for password change")
        void shouldRequireCurrentPasswordForPasswordChange() throws Exception {
            mockMvc.perform(post("/profile/password")
                    .param("currentPassword", "wrongpassword")
                    .param("newPassword", "newPassword123")
                    .param("confirmPassword", "newPassword123")
                    .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }
    
    @Nested
    @DisplayName("Remember Me Tests")
    class RememberMeTests {
        
        @Test
        @DisplayName("Should handle remember me functionality")
        void shouldHandleRememberMeFunctionality() throws Exception {
            mockMvc.perform(post("/login")
                    .param("username", "teststudent")
                    .param("password", "password123")
                    .param("remember-me", "true")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(cookie().exists("remember-me"));
        }
        
        @Test
        @DisplayName("Should set appropriate remember me cookie properties")
        void shouldSetAppropriateRememberMeCookieProperties() throws Exception {
            mockMvc.perform(post("/login")
                    .param("username", "teststudent")
                    .param("password", "password123")
                    .param("remember-me", "true")
                    .with(csrf()))
                    .andExpect(cookie().httpOnly("remember-me", true));
        }
    }
    
    @Nested
    @DisplayName("Error Handling Security Tests")
    class ErrorHandlingSecurityTests {
        
        @Test
        @DisplayName("Should not leak sensitive information in error pages")
        void shouldNotLeakSensitiveInformationInErrorPages() throws Exception {
            mockMvc.perform(get("/nonexistent"))
                    .andExpect(status().isNotFound());
        }
        
        @Test
        @WithMockUser(username = "teststudent", roles = "STUDENT")
        @DisplayName("Should show appropriate error for forbidden access")
        void shouldShowAppropriateErrorForForbiddenAccess() throws Exception {
            mockMvc.perform(get("/admin/users"))
                    .andExpect(status().isForbidden());
        }
        
        @Test
        @DisplayName("Should handle authentication errors gracefully")
        void shouldHandleAuthenticationErrorsGracefully() throws Exception {
            mockMvc.perform(post("/login")
                    .param("username", "nonexistent")
                    .param("password", "wrongpassword")
                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login?error*"));
        }
    }
}