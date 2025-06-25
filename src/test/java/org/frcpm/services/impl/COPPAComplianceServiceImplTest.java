// src/test/java/org/frcpm/services/impl/COPPAComplianceServiceImplTest.java

package org.frcpm.services.impl;

import org.frcpm.models.User;
import org.frcpm.models.UserRole;
import org.frcpm.repositories.spring.UserRepository;
import org.frcpm.services.AuditService;
import org.frcpm.services.COPPAComplianceService;
import org.frcpm.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.frcpm.repositories.spring.UserRepository;
import org.frcpm.services.EmailService;

/**
 * Comprehensive unit tests for COPPAComplianceServiceImpl - Phase 2B Testing
 * 
 * Tests the complete COPPA compliance workflow including:
 * - Age-based protection detection
 * - Parental consent management
 * - Data access controls
 * - Compliance monitoring
 * - Data retention policies
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B Testing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("COPPA Compliance Service Tests")
class COPPAComplianceServiceImplTest {
    
    @Mock
    private UserService userService;
    
    @Mock
    private AuditService auditService;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private COPPAComplianceServiceImpl coppaService;
    
    private User regularStudent;
    private User minorStudent;
    private User mentor;
    private User admin;
    private User parent;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test users
        regularStudent = createUser("regular", UserRole.STUDENT, 16, "parent1@frcteam.org", false);
        minorStudent = createUser("minor", UserRole.STUDENT, 12, "parent2@frcteam.org", true);
        mentor = createUser("mentor", UserRole.MENTOR, 30, null, false);
        admin = createUser("admin", UserRole.ADMIN, 35, null, false);
        parent = createUser("parent", UserRole.PARENT, 40, null, false);
        
        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        // NOTE: The @InjectMocks annotation will handle creating the service with proper dependencies
        // Remove the manual constructor call that was causing issues
    }
    
    @Nested
    @DisplayName("Compliance Checking Tests")
    class ComplianceCheckingTests {
        
        @Test
        @DisplayName("Should correctly identify users requiring parental consent")
        void shouldIdentifyUsersRequiringParentalConsent() {
            // When/Then - Users under 13 require consent
            assertTrue(coppaService.requiresParentalConsent(minorStudent), 
                "12-year-old should require parental consent");
            
            // Users 13 and over don't require consent
            assertFalse(coppaService.requiresParentalConsent(regularStudent), 
                "16-year-old should not require parental consent");
            assertFalse(coppaService.requiresParentalConsent(mentor), 
                "Adults should not require parental consent");
            
            // Null user should not require consent
            assertFalse(coppaService.requiresParentalConsent(null), 
                "Null user should not require consent");
        }
        
        @Test
        @DisplayName("Should validate parental consent status")
        void shouldValidateParentalConsentStatus() {
            // User not requiring consent should be valid
            assertTrue(coppaService.hasValidParentalConsent(regularStudent), 
                "User not requiring consent should have valid status");
            
            // Minor without consent should be invalid
            assertFalse(coppaService.hasValidParentalConsent(minorStudent), 
                "Minor without consent should have invalid status");
            
            // Minor with consent should be valid
            minorStudent.setParentalConsentDate(LocalDateTime.now());
            minorStudent.setRequiresParentalConsent(false);
            assertTrue(coppaService.hasValidParentalConsent(minorStudent), 
                "Minor with consent should have valid status");
        }
        
        @Test
        @DisplayName("Should enforce data minimization for protected users")
        void shouldEnforceDataMinimizationForProtectedUsers() {
            // Setup authentication
            org.frcpm.security.UserPrincipal userPrincipal = mock(org.frcpm.security.UserPrincipal.class);
            when(userPrincipal.getUser()).thenReturn(mentor);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            
            // When
            coppaService.enforceDataMinimization(minorStudent);
            
            // Then
            verify(auditService).logCOPPAAccess(
                eq(mentor), 
                eq(minorStudent), 
                eq("DATA_MINIMIZATION_ENFORCED"), 
                contains("Data minimization rules applied")
            );
        }
        
        @Test
        @DisplayName("Should skip data minimization for non-protected users")
        void shouldSkipDataMinimizationForNonProtectedUsers() {
            // When
            coppaService.enforceDataMinimization(regularStudent);
            
            // Then
            verify(auditService, never()).logCOPPAAccess(any(), any(), any(), any());
        }
    }
    
    @Nested
    @DisplayName("Consent Management Tests")
    class ConsentManagementTests {
        
        @Test
        @DisplayName("Should initiate parental consent process successfully")
        void shouldInitiateParentalConsentProcessSuccessfully() {
            // Given
            when(userService.initiateParentalConsent(minorStudent.getId(), minorStudent.getParentEmail()))
                .thenReturn(true);
            
            // Setup authentication
            org.frcpm.security.UserPrincipal userPrincipal = mock(org.frcpm.security.UserPrincipal.class);
            when(userPrincipal.getUser()).thenReturn(mentor);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            
            // When
            boolean result = coppaService.initiateParentalConsentProcess(minorStudent);
            
            // Then
            assertTrue(result, "Consent process should be initiated successfully");
            verify(userService).initiateParentalConsent(minorStudent.getId(), minorStudent.getParentEmail());
            verify(auditService).logCOPPAAccess(
                eq(mentor), 
                eq(minorStudent), 
                eq("CONSENT_PROCESS_INITIATED"), 
                contains("Parental consent process initiated")
            );
        }
        
        @Test
        @DisplayName("Should fail to initiate consent without parent email")
        void shouldFailToInitiateConsentWithoutParentEmail() {
            // Given
            minorStudent.setParentEmail(null);
            
            // When
            boolean result = coppaService.initiateParentalConsentProcess(minorStudent);
            
            // Then
            assertFalse(result, "Consent process should fail without parent email");
            verify(userService, never()).initiateParentalConsent(any(), any());
            verify(auditService, never()).logCOPPAAccess(any(), any(), any(), any());
        }
        
        @Test
        @DisplayName("Should skip consent initiation for non-protected users")
        void shouldSkipConsentInitiationForNonProtectedUsers() {
            // When
            boolean result = coppaService.initiateParentalConsentProcess(regularStudent);
            
            // Then
            assertFalse(result, "Consent process should not be needed for non-protected users");
            verify(userService, never()).initiateParentalConsent(any(), any());
        }
        
        @Test
        @DisplayName("Should process parental consent successfully")
        void shouldProcessParentalConsentSuccessfully() {
            // Given
            String consentToken = "test-consent-token";
            when(userService.processParentalConsent(consentToken, true)).thenReturn(true);
            
            // When - Grant consent
            boolean grantResult = coppaService.processParentalConsent(consentToken, true);
            
            // Then
            assertTrue(grantResult, "Consent granting should succeed");
            verify(userService).processParentalConsent(consentToken, true);
            verify(auditService).logAction(null, "CONSENT_GRANTED", "Parental consent granted for user under 13");
            
            // When - Deny consent
            when(userService.processParentalConsent(consentToken, false)).thenReturn(true);
            boolean denyResult = coppaService.processParentalConsent(consentToken, false);
            
            // Then
            assertTrue(denyResult, "Consent denial should succeed");
            verify(userService).processParentalConsent(consentToken, false);
            verify(auditService).logAction(null, "CONSENT_DENIED", "Parental consent denied for user under 13");
        }
        
        @Test
        @DisplayName("Should send consent reminders for pending users")
        void shouldSendConsentRemindersForPendingUsers() {
            // Setup authentication
            org.frcpm.security.UserPrincipal userPrincipal = mock(org.frcpm.security.UserPrincipal.class);
            when(userPrincipal.getUser()).thenReturn(mentor);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            
            // When
            coppaService.sendConsentReminder(minorStudent);
            
            // Then
            verify(auditService).logCOPPAAccess(
                eq(mentor), 
                eq(minorStudent), 
                eq("CONSENT_REMINDER_SENT"), 
                eq("Parental consent reminder sent")
            );
        }
        
        @Test
        @DisplayName("Should skip reminders for users with consent")
        void shouldSkipRemindersForUsersWithConsent() {
            // Given - User has consent
            minorStudent.setParentalConsentDate(LocalDateTime.now());
            minorStudent.setRequiresParentalConsent(false);
            
            // When
            coppaService.sendConsentReminder(minorStudent);
            
            // Then
            verify(auditService, never()).logCOPPAAccess(any(), any(), any(), any());
        }
    }
    
    @Nested
    @DisplayName("Access Control Tests")
    class AccessControlTests {
        
        @Test
        @DisplayName("Should allow user to access their own data")
        void shouldAllowUserToAccessOwnData() {
            // When/Then
            assertTrue(coppaService.canAccessUserData(regularStudent, regularStudent), 
                "Users should be able to access their own data");
            assertTrue(coppaService.canAccessUserData(minorStudent, minorStudent), 
                "Even protected users should access their own data");
        }
        
        @Test
        @DisplayName("Should allow admin to access all data")
        void shouldAllowAdminToAccessAllData() {
            // When/Then
            assertTrue(coppaService.canAccessUserData(admin, regularStudent), 
                "Admin should access regular student data");
            assertTrue(coppaService.canAccessUserData(admin, minorStudent), 
                "Admin should access protected student data");
            assertTrue(coppaService.canAccessUserData(admin, mentor), 
                "Admin should access mentor data");
        }
        
        @Test
        @DisplayName("Should allow mentor to access consented student data")
        void shouldAllowMentorToAccessConsentedStudentData() {
            // Given - Student has consent
            minorStudent.setParentalConsentDate(LocalDateTime.now());
            minorStudent.setRequiresParentalConsent(false);
            
            // When/Then
            assertTrue(coppaService.canAccessUserData(mentor, regularStudent), 
                "Mentor should access regular student data");
            assertTrue(coppaService.canAccessUserData(mentor, minorStudent), 
                "Mentor should access consented protected student data");
        }
        
        @Test
        @DisplayName("Should deny mentor access to non-consented student data")
        void shouldDenyMentorAccessToNonConsentedStudentData() {
            // When/Then
            assertFalse(coppaService.canAccessUserData(mentor, minorStudent), 
                "Mentor should not access non-consented protected student data");
        }
        
        @Test
        @DisplayName("Should deny student access to other student data")
        void shouldDenyStudentAccessToOtherStudentData() {
            // When/Then
            assertFalse(coppaService.canAccessUserData(regularStudent, minorStudent), 
                "Students should not access other student data");
            assertFalse(coppaService.canAccessUserData(minorStudent, regularStudent), 
                "Protected students should not access other data");
        }
        
        @Test
        @DisplayName("Should handle null parameters gracefully")
        void shouldHandleNullParametersGracefully() {
            // When/Then
            assertFalse(coppaService.canAccessUserData(null, regularStudent), 
                "Null accessor should be denied");
            assertFalse(coppaService.canAccessUserData(mentor, null), 
                "Null subject should be denied");
            assertFalse(coppaService.canAccessUserData(null, null), 
                "Both null should be denied");
        }
        
        @Test
        @DisplayName("Should log data access for protected users")
        void shouldLogDataAccessForProtectedUsers() {
            // When
            coppaService.logDataAccess(mentor, minorStudent, "VIEW");
            
            // Then
            verify(auditService).logCOPPAAccess(
                eq(mentor), 
                eq(minorStudent), 
                eq("DATA_ACCESS_VIEW"), 
                contains("Accessed data of user under 13: VIEW")
            );
        }
        
        @Test
        @DisplayName("Should log regular data access for non-protected users")
        void shouldLogRegularDataAccessForNonProtectedUsers() {
            // When
            coppaService.logDataAccess(mentor, regularStudent, "VIEW");
            
            // Then
            verify(auditService).logDataAccess(
                eq(mentor), 
                eq(regularStudent), 
                eq("User"), 
                eq(regularStudent.getId()), 
                eq("DATA_ACCESS_VIEW")
            );
        }
    }
    
    @Nested
    @DisplayName("Monitoring and Compliance Tests")
    class MonitoringTests {
        
        @Test
        @DisplayName("Should find users requiring consent review")
        void shouldFindUsersRequiringConsentReview() {
            // Given
            List<User> pendingUsers = List.of(minorStudent);
            when(userService.findUsersRequiringParentalConsent()).thenReturn(pendingUsers);
            
            // When
            List<User> result = coppaService.getUsersRequiringConsentReview();
            
            // Then
            assertEquals(1, result.size(), "Should find users requiring review");
            assertEquals(minorStudent, result.get(0), "Should return the correct user");
        }
        
        @Test
        @DisplayName("Should perform scheduled COPPA review")
        void shouldPerformScheduledCOPPAReview() {
            // Given - User created 8 days ago (past 7-day limit)
            minorStudent.setCreatedAt(LocalDateTime.now().minusDays(8));
            List<User> pendingUsers = List.of(minorStudent);
            when(userService.findUsersRequiringParentalConsent()).thenReturn(pendingUsers);
            
            // When
            coppaService.performScheduledCOPPAReview();
            
            // Then
            verify(userService).disableUser(minorStudent.getId());
            verify(auditService).logCOPPAAccess(
                isNull(), 
                eq(minorStudent), 
                eq("ACCOUNT_DISABLED_NO_CONSENT"), 
                contains("Account disabled due to missing parental consent after 7 days")
            );
        }
        
        @Test
        @DisplayName("Should send reminders for recent pending users")
        void shouldSendRemindersForRecentPendingUsers() {
            // Given - User created 3 days ago (within 7-day limit)
            minorStudent.setCreatedAt(LocalDateTime.now().minusDays(3));
            List<User> pendingUsers = List.of(minorStudent);
            when(userService.findUsersRequiringParentalConsent()).thenReturn(pendingUsers);
            
            // When
            coppaService.performScheduledCOPPAReview();
            
            // Then
            verify(userService, never()).disableUser(any());
            // Reminder sending would be verified if we can mock the sendConsentReminder call
        }
        
        @Test
        @DisplayName("Should generate compliance report")
        void shouldGenerateComplianceReport() {
            // Given
            when(userService.count()).thenReturn(100L);
            when(userService.countMinorsUnder13()).thenReturn(5L);
            when(auditService.getCOPPALogCount()).thenReturn(25L);
            
            // Setup authentication
            org.frcpm.security.UserPrincipal userPrincipal = mock(org.frcpm.security.UserPrincipal.class);
            when(userPrincipal.getUser()).thenReturn(admin);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            
            // When
            coppaService.generateComplianceReport();
            
            // Then
            verify(auditService).logAction(
                eq(admin), 
                eq("COMPLIANCE_REPORT_GENERATED"), 
                contains("Total Users: 100, Users Under 13: 5, COPPA Log Entries: 25")
            );
        }
    }
    
    @Nested
    @DisplayName("Data Retention and Deletion Tests")
    class DataRetentionTests {
        
        @Test
        @DisplayName("Should schedule data retention review for protected users")
        void shouldScheduleDataRetentionReviewForProtectedUsers() {
            // Setup authentication
            org.frcpm.security.UserPrincipal userPrincipal = mock(org.frcpm.security.UserPrincipal.class);
            when(userPrincipal.getUser()).thenReturn(mentor);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            
            // When
            coppaService.scheduleDataRetentionReview(minorStudent);
            
            // Then
            verify(auditService).logCOPPAAccess(
                eq(mentor), 
                eq(minorStudent), 
                eq("DATA_RETENTION_REVIEW_SCHEDULED"), 
                contains("Data retention review scheduled for user under 13")
            );
        }
        
        @Test
        @DisplayName("Should skip retention review for non-protected users")
        void shouldSkipRetentionReviewForNonProtectedUsers() {
            // When
            coppaService.scheduleDataRetentionReview(regularStudent);
            
            // Then
            verify(auditService, never()).logCOPPAAccess(any(), any(), any(), any());
        }
        
        @Test
        @DisplayName("Should allow deletion for COPPA compliance")
        void shouldAllowDeletionForCOPPACompliance() {
            // When/Then
            assertTrue(coppaService.canDeleteUserData(minorStudent), 
                "Should always allow deletion for COPPA compliance");
            assertTrue(coppaService.canDeleteUserData(regularStudent), 
                "Should allow deletion for regular users");
        }
    }
    
    @Nested
    @DisplayName("Additional COPPA Methods Tests")
    class AdditionalCOPPAMethodsTests {
        
        @Test
        @DisplayName("Should validate account for activation with consent")
        void shouldValidateAccountForActivationWithConsent() {
            // Given - User has consent and parent email
            minorStudent.setParentalConsentDate(LocalDateTime.now());
            minorStudent.setRequiresParentalConsent(false);
            minorStudent.setParentEmail("parent@frcteam.org");
            
            // Setup authentication
            org.frcpm.security.UserPrincipal userPrincipal = mock(org.frcpm.security.UserPrincipal.class);
            when(userPrincipal.getUser()).thenReturn(mentor);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            
            // When
            boolean result = ((COPPAComplianceServiceImpl) coppaService).validateAccountForActivation(minorStudent);
            
            // Then
            assertTrue(result, "Account should be valid for activation");
            verify(auditService).logCOPPAAccess(
                eq(mentor), 
                eq(minorStudent), 
                eq("COPPA_VALIDATION_SUCCESS"), 
                contains("Account validated for activation with proper COPPA compliance")
            );
        }
        
        @Test
        @DisplayName("Should reject activation without consent")
        void shouldRejectActivationWithoutConsent() {
            // Given - User lacks consent
            minorStudent.setParentalConsentDate(null);
            minorStudent.setRequiresParentalConsent(true);
            
            // When
            boolean result = ((COPPAComplianceServiceImpl) coppaService).validateAccountForActivation(minorStudent);
            
            // Then
            assertFalse(result, "Account should not be valid for activation without consent");
        }
        
        @Test
        @DisplayName("Should allow non-protected users to activate")
        void shouldAllowNonProtectedUsersToActivate() {
            // When
            boolean result = ((COPPAComplianceServiceImpl) coppaService).validateAccountForActivation(regularStudent);
            
            // Then
            assertTrue(result, "Non-protected users should always be valid for activation");
        }
        
        @Test
        @DisplayName("Should check data operation permissions")
        void shouldCheckDataOperationPermissions() {
            // Given - Minor with consent
            minorStudent.setParentalConsentDate(LocalDateTime.now());
            minorStudent.setRequiresParentalConsent(false);
            
            COPPAComplianceServiceImpl impl = (COPPAComplianceServiceImpl) coppaService;
            
            // When/Then - Test various operations
            assertTrue(impl.isDataOperationPermitted(mentor, minorStudent, "READ"), 
                "Mentor should be able to read consented student data");
            assertTrue(impl.isDataOperationPermitted(mentor, minorStudent, "WRITE"), 
                "Mentor should be able to write consented student data");
            assertFalse(impl.isDataOperationPermitted(mentor, minorStudent, "DELETE"), 
                "Only admin should be able to delete COPPA-protected data");
            assertFalse(impl.isDataOperationPermitted(mentor, minorStudent, "SHARE"), 
                "Sharing COPPA-protected data should require special approval");
            
            assertTrue(impl.isDataOperationPermitted(admin, minorStudent, "DELETE"), 
                "Admin should be able to delete COPPA-protected data");
        }
        
        @Test
        @DisplayName("Should provide correct data retention periods")
        void shouldProvideCorrectDataRetentionPeriods() {
            COPPAComplianceServiceImpl impl = (COPPAComplianceServiceImpl) coppaService;
            
            // When/Then
            assertEquals(2555, impl.getDataRetentionPeriodDays(minorStudent), 
                "COPPA users should have 7-year retention period");
            assertEquals(1825, impl.getDataRetentionPeriodDays(regularStudent), 
                "Regular users should have 5-year retention period");
        }
        
        @Test
        @DisplayName("Should determine anonymization vs deletion")
        void shouldDetermineAnonymizationVsDeletion() {
            COPPAComplianceServiceImpl impl = (COPPAComplianceServiceImpl) coppaService;
            
            // When/Then
            assertTrue(impl.shouldAnonymizeInsteadOfDelete(minorStudent), 
                "COPPA users should be anonymized instead of deleted");
            assertFalse(impl.shouldAnonymizeInsteadOfDelete(regularStudent), 
                "Regular users can have data fully deleted");
        }
    }
    
    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    
    private User createUser(String username, UserRole role, Integer age, String parentEmail, boolean requiresConsent) {
        User user = new User();
        user.setId((long) (Math.random() * 1000)); // Random ID for testing
        user.setUsername(username);
        user.setEmail(username + "@frcteam.org");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(role);
        user.setAge(age);
        user.setParentEmail(parentEmail);
        user.setRequiresParentalConsent(requiresConsent);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
    
    // Add this overloaded version for simpler calls
    private User createUser(String username, UserRole role, Integer age) {
        return createUser(username, role, age, null, age < 13);
    }
}