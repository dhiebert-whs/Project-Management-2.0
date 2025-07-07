// src/test/java/org/frcpm/services/impl/AuditServiceImplTest.java

package org.frcpm.services.impl;

import org.frcpm.models.AuditLevel;
import org.frcpm.models.AuditLog;
import org.frcpm.models.User;
import org.frcpm.models.UserRole;
import org.frcpm.repositories.spring.AuditLogRepository;
//import org.frcpm.services.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.web.context.request.ServletRequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;

/**
 * Comprehensive unit tests for AuditServiceImpl - Phase 2B Testing
 * 
 * Tests the audit logging service for security and COPPA compliance including:
 * - Basic audit logging functionality
 * - COPPA-specific audit requirements
 * - Security event logging
 * - Request context integration
 * - Query methods for compliance reporting
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B Testing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Audit Service Tests")
class AuditServiceImplTest {
    
    @Mock
    private AuditLogRepository auditLogRepository;
    
    @Mock
    private ServletRequestAttributes requestAttributes;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpSession session;
    
    @InjectMocks
    private AuditServiceImpl auditService;
    
    private User testUser;
    private User testMinor;
    private User testMentor;
    
    @BeforeEach
    void setUp() {
        testUser = createUser(1L, "testuser", UserRole.STUDENT, 16);
        testMinor = createUser(2L, "testminor", UserRole.STUDENT, 12);
        testMentor = createUser(3L, "testmentor", UserRole.MENTOR, 35);
        
        // Clear any existing request context
        RequestContextHolder.resetRequestAttributes();
    }
    
    @Nested
    @DisplayName("Basic Logging Tests")
    class BasicLoggingTests {
        
        @Test
        @DisplayName("Should log basic user action")
        void shouldLogBasicUserAction() {
            // When
            auditService.logAction(testUser, "LOGIN", "User logged in successfully");
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertEquals(testUser, savedLog.getUser(), "User should be set correctly");
            assertEquals("LOGIN", savedLog.getAction(), "Action should be set correctly");
            assertEquals("User logged in successfully", savedLog.getDescription(), "Description should be set");
            assertEquals(AuditLevel.INFO, savedLog.getLevel(), "Default level should be INFO");
            assertNull(savedLog.getSubjectUser(), "Subject user should be null for basic action");
        }
        
        @Test
        @DisplayName("Should log action with custom level")
        void shouldLogActionWithCustomLevel() {
            // When
            auditService.logAction(testUser, "SECURITY_ALERT", "Failed login attempt", AuditLevel.SECURITY_ALERT);
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertEquals(AuditLevel.SECURITY_ALERT, savedLog.getLevel(), "Custom level should be set");
            assertEquals("SECURITY_ALERT", savedLog.getAction(), "Action should be set");
        }
        
        @Test
        @DisplayName("Should handle null user gracefully")
        void shouldHandleNullUserGracefully() {
            // When
            auditService.logAction(null, "SYSTEM_ACTION", "System startup");
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertNull(savedLog.getUser(), "User should be null for system actions");
            assertEquals("SYSTEM_ACTION", savedLog.getAction(), "Action should be set");
        }
        
        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            // Given
            when(auditLogRepository.save(any())).thenThrow(new RuntimeException("Database error"));
            
            // When/Then - Should not throw exception
            assertDoesNotThrow(() -> {
                auditService.logAction(testUser, "TEST_ACTION", "Test description");
            }, "Audit service should handle repository exceptions gracefully");
        }
    }
    
    @Nested
    @DisplayName("Data Access Logging Tests")
    class DataAccessLoggingTests {
        
        @Test
        @DisplayName("Should log data access without subject user")
        void shouldLogDataAccessWithoutSubjectUser() {
            // When
            auditService.logDataAccess(testMentor, "Task", 123L, "VIEW_TASK");
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertEquals(testMentor, savedLog.getUser(), "User should be set");
            assertEquals("VIEW_TASK", savedLog.getAction(), "Action should be set");
            assertEquals("Task", savedLog.getResourceType(), "Resource type should be set");
            assertEquals(123L, savedLog.getResourceId(), "Resource ID should be set");
            assertEquals(AuditLevel.INFO, savedLog.getLevel(), "Default level for non-COPPA access");
            assertNull(savedLog.getSubjectUser(), "Subject user should be null");
            assertFalse(savedLog.isCoppaRelevant(), "Should not be COPPA relevant without subject");
        }
        
        @Test
        @DisplayName("Should log COPPA-relevant data access")
        void shouldLogCOPPARelevantDataAccess() {
            // When
            auditService.logDataAccess(testMentor, testMinor, "User", testMinor.getId(), "VIEW_PROFILE");
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertEquals(testMentor, savedLog.getUser(), "Accessor should be set");
            assertEquals(testMinor, savedLog.getSubjectUser(), "Subject user should be set");
            assertEquals("VIEW_PROFILE", savedLog.getAction(), "Action should be set");
            assertEquals("User", savedLog.getResourceType(), "Resource type should be set");
            assertEquals(testMinor.getId(), savedLog.getResourceId(), "Resource ID should be set");
            assertEquals(AuditLevel.COPPA_COMPLIANCE, savedLog.getLevel(), "Should be COPPA compliance level");
            assertTrue(savedLog.isCoppaRelevant(), "Should be marked as COPPA relevant");
        }
        
        @Test
        @DisplayName("Should log non-COPPA data access with subject user")
        void shouldLogNonCOPPADataAccessWithSubjectUser() {
            // When
            auditService.logDataAccess(testMentor, testUser, "User", testUser.getId(), "VIEW_PROFILE");
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertEquals(testMentor, savedLog.getUser(), "Accessor should be set");
            assertEquals(testUser, savedLog.getSubjectUser(), "Subject user should be set");
            assertEquals(AuditLevel.INFO, savedLog.getLevel(), "Should be INFO level for non-COPPA");
            assertFalse(savedLog.isCoppaRelevant(), "Should not be COPPA relevant for regular user");
        }
    }
    
    @Nested
    @DisplayName("Security Event Logging Tests")
    class SecurityEventLoggingTests {
        
        @Test
        @DisplayName("Should log security events")
        void shouldLogSecurityEvents() {
            // When
            auditService.logSecurityEvent(testUser, "FAILED_LOGIN", "Invalid password attempt");
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertEquals(AuditLevel.WARNING, savedLog.getLevel(), "Security events should be WARNING level");
            assertEquals("FAILED_LOGIN", savedLog.getAction(), "Action should be set");
        }
        
        @Test
        @DisplayName("Should log security alerts")
        void shouldLogSecurityAlerts() {
            // When
            auditService.logSecurityAlert(testUser, "BRUTE_FORCE", "Multiple failed login attempts");
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertEquals(AuditLevel.SECURITY_ALERT, savedLog.getLevel(), "Security alerts should be SECURITY_ALERT level");
            assertEquals("BRUTE_FORCE", savedLog.getAction(), "Action should be set");
        }
        
        @Test
        @DisplayName("Should log login attempts")
        void shouldLogLoginAttempts() {
            // When - Successful login
            auditService.logLoginAttempt("testuser", "192.168.1.100", true);
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertNull(savedLog.getUser(), "User should be null for login attempts (user not authenticated yet)");
            assertEquals("LOGIN_SUCCESS", savedLog.getAction(), "Action should be LOGIN_SUCCESS");
            assertEquals("192.168.1.100", savedLog.getIpAddress(), "IP address should be set");
            assertEquals(AuditLevel.INFO, savedLog.getLevel(), "Successful login should be INFO level");
            assertTrue(savedLog.getDescription().contains("testuser"), "Description should contain username");
        }
        
        @Test
        @DisplayName("Should log failed login attempts")
        void shouldLogFailedLoginAttempts() {
            // When - Failed login
            auditService.logLoginAttempt("invaliduser", "192.168.1.200", false);
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertEquals("LOGIN_FAILED", savedLog.getAction(), "Action should be LOGIN_FAILED");
            assertEquals("192.168.1.200", savedLog.getIpAddress(), "IP address should be set");
            assertEquals(AuditLevel.WARNING, savedLog.getLevel(), "Failed login should be WARNING level");
        }
        
        @Test
        @DisplayName("Should log logout events")
        void shouldLogLogoutEvents() {
            // When
            auditService.logLogout(testUser);
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertEquals(testUser, savedLog.getUser(), "User should be set");
            assertEquals("LOGOUT", savedLog.getAction(), "Action should be LOGOUT");
            assertEquals("User logged out successfully", savedLog.getDescription(), "Description should be set");
            assertEquals(AuditLevel.INFO, savedLog.getLevel(), "Logout should be INFO level");
        }
    }
    
    @Nested
    @DisplayName("Request Context Integration Tests")
    class RequestContextIntegrationTests {
        
        @Test
        @DisplayName("Should capture request details when available")
        void shouldCaptureRequestDetailsWhenAvailable() {
            // Given
            User user = createTestUser("testuser", UserRole.STUDENT);
            
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr("192.168.1.50");
            request.addHeader("User-Agent", "Mozilla/5.0 Test Browser");
            
            ServletRequestAttributes attributes = new ServletRequestAttributes(request);
            RequestContextHolder.setRequestAttributes(attributes);
            
            try {
                // When - FIXED: Change from logUserAction to logAction
                auditService.logAction(user, "TEST_ACTION", "Test description");
                
                // Then
                ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
                verify(auditLogRepository).save(logCaptor.capture());
                
                AuditLog savedLog = logCaptor.getValue();
                assertEquals("192.168.1.50", savedLog.getIpAddress());
                assertEquals("Mozilla/5.0 Test Browser", savedLog.getUserAgent());
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        }
    
        @Test
        @DisplayName("Should handle X-Forwarded-For header")
        void shouldHandleXForwardedForHeader() {
            // Given
            User user = createTestUser("testuser", UserRole.STUDENT);
            
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr("10.0.0.1");
            request.addHeader("X-Forwarded-For", "203.0.113.45, 10.0.0.1");
            request.addHeader("User-Agent", "Test Browser");
            
            ServletRequestAttributes attributes = new ServletRequestAttributes(request);
            RequestContextHolder.setRequestAttributes(attributes);
            
            try {
                // When - FIXED: Change from logUserAction to logAction
                auditService.logAction(user, "TEST_ACTION", "Test description");
                
                // Then
                ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
                verify(auditLogRepository).save(logCaptor.capture());
                
                AuditLog savedLog = logCaptor.getValue();
                assertEquals("203.0.113.45", savedLog.getIpAddress()); // Should use first IP from X-Forwarded-For
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        }
    
        @Test
        @DisplayName("Should handle X-Real-IP header")
        void shouldHandleXRealIPHeader() {
            // Given
            User user = createTestUser("testuser", UserRole.STUDENT);
            
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr("10.0.0.1");
            request.addHeader("X-Real-IP", "203.0.113.45");
            request.addHeader("User-Agent", "Test Browser");
            
            ServletRequestAttributes attributes = new ServletRequestAttributes(request);
            RequestContextHolder.setRequestAttributes(attributes);
            
            try {
                // When - FIXED: Change from logUserAction to logAction
                auditService.logAction(user, "TEST_ACTION", "Test description");
                
                // Then
                ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
                verify(auditLogRepository).save(logCaptor.capture());
                
                AuditLog savedLog = logCaptor.getValue();
                assertEquals("203.0.113.45", savedLog.getIpAddress()); // Should use X-Real-IP
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        }
    
        @Test
        @DisplayName("Should truncate long user agent strings")
        void shouldTruncateLongUserAgentStrings() {
            // Given
            User user = createTestUser("testuser", UserRole.STUDENT);
            
            // Create a very long user agent string (over 500 characters)
            String longUserAgent = "Mozilla/5.0 ".repeat(50); // Creates a string much longer than 500 chars
            
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr("192.168.1.100");
            request.addHeader("User-Agent", longUserAgent);
            
            ServletRequestAttributes attributes = new ServletRequestAttributes(request);
            RequestContextHolder.setRequestAttributes(attributes);
            
            try {
                // When - FIXED: Change from logUserAction to logAction
                auditService.logAction(user, "TEST_ACTION", "Test description");
                
                // Then
                ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
                verify(auditLogRepository).save(logCaptor.capture());
                
                AuditLog savedLog = logCaptor.getValue();
                assertNotNull(savedLog.getUserAgent());
                assertTrue(savedLog.getUserAgent().length() <= 500, 
                        "User agent should be truncated to 500 characters or less");
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        }
        
        // This method already uses logAction correctly, so no change needed
        @Test
        @DisplayName("Should handle missing request context gracefully")
        void shouldHandleMissingRequestContextGracefully() {
            // Given - No request context
            RequestContextHolder.resetRequestAttributes();
            
            // When
            auditService.logAction(testUser, "BACKGROUND_TASK", "Scheduled task executed");
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertNull(savedLog.getIpAddress(), "IP address should be null without request context");
            assertNull(savedLog.getUserAgent(), "User agent should be null without request context");
            assertNull(savedLog.getSessionId(), "Session ID should be null without request context");
        }
    }
    
    @Nested
    @DisplayName("Query Methods Tests")
    class QueryMethodsTests {
        
        @Test
        @DisplayName("Should get recent logs")
        void shouldGetRecentLogs() {
            // Given
            List<AuditLog> expectedLogs = List.of(new AuditLog(), new AuditLog());
            when(auditLogRepository.findRecentLogs(any(LocalDateTime.class))).thenReturn(expectedLogs);
            
            // When
            List<AuditLog> result = auditService.getRecentLogs(7);
            
            // Then
            assertEquals(expectedLogs, result, "Should return logs from repository");
            verify(auditLogRepository).findRecentLogs(argThat(date -> 
                date.isBefore(LocalDateTime.now()) && date.isAfter(LocalDateTime.now().minusDays(8))));
        }
        
        @Test
        @DisplayName("Should get recent COPPA logs")
        void shouldGetRecentCOPPALogs() {
            // Given
            List<AuditLog> expectedLogs = List.of(new AuditLog());
            when(auditLogRepository.findRecentCOPPALogs(any(LocalDateTime.class))).thenReturn(expectedLogs);
            
            // When
            List<AuditLog> result = auditService.getRecentCOPPALogs(30);
            
            // Then
            assertEquals(expectedLogs, result, "Should return COPPA logs from repository");
            verify(auditLogRepository).findRecentCOPPALogs(any(LocalDateTime.class));
        }
        
        @Test
        @DisplayName("Should get security events")
        void shouldGetSecurityEvents() {
            // Given
            List<AuditLog> expectedLogs = List.of(new AuditLog());
            when(auditLogRepository.findSecurityEventsSince(anyList(), any(LocalDateTime.class)))
                .thenReturn(expectedLogs);
            
            // When
            List<AuditLog> result = auditService.getSecurityEvents(7);
            
            // Then
            assertEquals(expectedLogs, result, "Should return security events from repository");
            verify(auditLogRepository).findSecurityEventsSince(
                argThat(levels -> levels.contains(AuditLevel.WARNING) && 
                                levels.contains(AuditLevel.SECURITY_ALERT) && 
                                levels.contains(AuditLevel.VIOLATION)),
                any(LocalDateTime.class)
            );
        }
        
        @Test
        @DisplayName("Should get user activity")
        void shouldGetUserActivity() {
            // Given
            List<AuditLog> expectedLogs = List.of(new AuditLog());
            when(auditLogRepository.findByUser(testUser)).thenReturn(expectedLogs);
            
            // When
            List<AuditLog> result = auditService.getUserActivity(testUser, 30);
            
            // Then
            assertEquals(expectedLogs, result, "Should return user activity from repository");
            verify(auditLogRepository).findByUser(testUser);
        }
        
        @Test
        @DisplayName("Should get COPPA log count")
        void shouldGetCOPPALogCount() {
            // Given
            when(auditLogRepository.countCOPPARelevantLogs()).thenReturn(25L);
            
            // When
            long count = auditService.getCOPPALogCount();
            
            // Then
            assertEquals(25L, count, "Should return COPPA log count from repository");
            verify(auditLogRepository).countCOPPARelevantLogs();
        }
        
        @Test
        @DisplayName("Should get security event count")
        void shouldGetSecurityEventCount() {
            // Given
            when(auditLogRepository.countByLevelSince(eq(AuditLevel.SECURITY_ALERT), any(LocalDateTime.class)))
                .thenReturn(5L);
            when(auditLogRepository.countByLevelSince(eq(AuditLevel.VIOLATION), any(LocalDateTime.class)))
                .thenReturn(2L);
            
            // When
            long count = auditService.getSecurityEventCount(30);
            
            // Then
            assertEquals(7L, count, "Should return sum of security alert and violation counts");
            verify(auditLogRepository).countByLevelSince(eq(AuditLevel.SECURITY_ALERT), any(LocalDateTime.class));
            verify(auditLogRepository).countByLevelSince(eq(AuditLevel.VIOLATION), any(LocalDateTime.class));
        }
    }
    
    @Nested
    @DisplayName("COPPA Compliance Specific Tests")
    class COPPAComplianceTests {
        
        @Test
        @DisplayName("Should automatically mark logs as COPPA relevant for minors")
        void shouldAutomaticallyMarkLogsAsCOPPARelevantForMinors() {
            // When
            auditService.logDataAccess(testMentor, testMinor, "Profile", testMinor.getId(), "VIEW");
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertTrue(savedLog.isCoppaRelevant(), "Should automatically mark as COPPA relevant for minor");
            assertEquals(AuditLevel.COPPA_COMPLIANCE, savedLog.getLevel(), "Should use COPPA compliance level");
        }
        
        @Test
        @DisplayName("Should not mark logs as COPPA relevant for adults")
        void shouldNotMarkLogsAsCOPPARelevantForAdults() {
            // When
            auditService.logDataAccess(testMentor, testUser, "Profile", testUser.getId(), "VIEW");
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertFalse(savedLog.isCoppaRelevant(), "Should not mark as COPPA relevant for adult");
            assertEquals(AuditLevel.INFO, savedLog.getLevel(), "Should use INFO level for non-COPPA access");
        }
        
        @Test
        @DisplayName("Should handle COPPA access logging with implementation")
        void shouldHandleCOPPAAccessLoggingWithImplementation() {
            // Given
            User accessor = createTestUser("mentor1", UserRole.MENTOR);
            User protectedUser = createTestUser("student1", UserRole.STUDENT, 12);
            
            // When & Then - Should NOT throw exception since method is implemented
            assertDoesNotThrow(() -> {
                auditService.logCOPPAAccess(accessor, protectedUser, "VIEW_PROFILE", "Mentor viewing student profile");
            });
            
            // Verify the audit log was created
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertEquals("VIEW_PROFILE", savedLog.getAction());
            assertEquals("Mentor viewing student profile", savedLog.getDescription());
            assertEquals(AuditLevel.COPPA_COMPLIANCE, savedLog.getLevel());
            assertTrue(savedLog.isCoppaRelevant());
            assertEquals(accessor.getId(), savedLog.getUser().getId());
            assertEquals(protectedUser.getId(), savedLog.getSubjectUser().getId());
        }
    }
    
    @Nested
    @DisplayName("Error Handling and Edge Cases Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle repository save failures")
        void shouldHandleRepositorySaveFailures() {
            // Given
            when(auditLogRepository.save(any())).thenThrow(new RuntimeException("Database connection failed"));
            
            // When/Then - Should not propagate exception
            assertDoesNotThrow(() -> {
                auditService.logAction(testUser, "TEST_ACTION", "Test description");
            }, "Should handle repository failures gracefully");
            
            // Verify attempt was made
            verify(auditLogRepository).save(any());
        }
        
        @Test
        @DisplayName("Should handle null request attributes")
        void shouldHandleNullRequestAttributes() {
            // Given
            RequestContextHolder.setRequestAttributes(null);
            
            // When
            auditService.logAction(testUser, "NULL_CONTEXT_TEST", "Testing null context");
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertNull(savedLog.getIpAddress(), "IP should be null with null request attributes");
            assertNull(savedLog.getUserAgent(), "User agent should be null with null request attributes");
            assertNull(savedLog.getSessionId(), "Session ID should be null with null request attributes");
        }
        
        @Test
        @DisplayName("Should handle request attribute exceptions")
        void shouldHandleRequestAttributeExceptions() {
            // Given
            RequestContextHolder.setRequestAttributes(requestAttributes);
            when(requestAttributes.getRequest()).thenThrow(new RuntimeException("Request access failed"));
            
            // When/Then - Should not fail
            assertDoesNotThrow(() -> {
                auditService.logAction(testUser, "EXCEPTION_TEST", "Testing exception handling");
            }, "Should handle request attribute exceptions gracefully");
        }
        
        @Test
        @DisplayName("Should handle very long descriptions")
        void shouldHandleVeryLongDescriptions() {
            // Given
            String longDescription = "Very long description ".repeat(100); // Very long string
            
            // When
            auditService.logAction(testUser, "LONG_DESC_TEST", longDescription);
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertEquals(longDescription, savedLog.getDescription(), "Should preserve full description");
        }
        
        @Test
        @DisplayName("Should handle special characters in descriptions")
        void shouldHandleSpecialCharactersInDescriptions() {
            // Given
            String specialChars = "Test with special chars: àáâãäåæçèéêë ñòóôõö ùúûüý ¿¡ €£¥ © ® ™";
            
            // When
            auditService.logAction(testUser, "SPECIAL_CHARS", specialChars);
            
            // Then
            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(logCaptor.capture());
            
            AuditLog savedLog = logCaptor.getValue();
            assertEquals(specialChars, savedLog.getDescription(), "Should handle special characters correctly");
        }
    }
    
    @Nested
    @DisplayName("Performance and Concurrent Access Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should handle concurrent logging efficiently")
        void shouldHandleConcurrentLoggingEfficiently() {
            // When - Simulate multiple concurrent log operations
            for (int i = 0; i < 100; i++) {
                auditService.logAction(testUser, "CONCURRENT_TEST_" + i, "Concurrent operation " + i);
            }
            
            // Then
            verify(auditLogRepository, times(100)).save(any());
        }
        
        @Test
        @DisplayName("Should handle rapid successive logging")
        void shouldHandleRapidSuccessiveLogging() {
            // When - Log many actions quickly
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 50; i++) {
                auditService.logAction(testUser, "RAPID_TEST", "Rapid operation " + i);
            }
            long endTime = System.currentTimeMillis();
            
            // Then
            verify(auditLogRepository, times(50)).save(any());
            assertTrue((endTime - startTime) < 1000, "Should complete 50 operations in under 1 second");
        }
    }
    
    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    
    private User createUser(Long id, String username, UserRole role, Integer age) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@frcteam.org");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(role);
        user.setAge(age);
        user.setEnabled(true);
        return user;
    }

    private User createTestUser(String username, UserRole role) {
        return createUser(null, username, role, 18); // Default age 18 for non-age-specific tests
    }
    
    private User createTestUser(String username, UserRole role, Integer age) {
        return createUser(null, username, role, age);
    }
}