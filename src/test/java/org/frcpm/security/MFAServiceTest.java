// src/test/java/org/frcpm/security/MFAServiceTest.java

package org.frcpm.security;

import org.frcpm.models.User;
import org.frcpm.models.UserRole;
import org.frcpm.services.AuditService;
import org.frcpm.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
//import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive integration tests for MFAService - Phase 2B Testing
 * 
 * Tests the multi-factor authentication service including:
 * - MFA setup workflow for mentors and admins
 * - Token validation and security
 * - Recovery code generation
 * - Integration with user management
 * - Audit logging
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B Testing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MFA Service Integration Tests")
class MFAServiceTest {
    
    @Mock
    private TOTPService totpService;
    
    @Mock
    private UserService userService;
    
    @Mock
    private AuditService auditService;
    
    @InjectMocks
    private MFAService mfaService;
    
    private User mentorUser;
    private User adminUser;
    private User studentUser;
    private User mfaEnabledMentor;
    
    @BeforeEach
    void setUp() {
        mentorUser = createUser(1L, "mentor1", UserRole.MENTOR, false, null);
        adminUser = createUser(2L, "admin1", UserRole.ADMIN, false, null);
        studentUser = createUser(3L, "student1", UserRole.STUDENT, false, null);
        mfaEnabledMentor = createUser(4L, "mentor2", UserRole.MENTOR, true, "JBSWY3DPEHPK3PXP");
    }
    
    @Nested
    @DisplayName("MFA Setup Initiation Tests")
    class MFASetupInitiationTests {
        
        @Test
        @DisplayName("Should initiate MFA setup for mentor successfully")
        void shouldInitiateMFASetupForMentorSuccessfully() {
            // Given
            when(userService.findById(mentorUser.getId())).thenReturn(mentorUser);
            when(totpService.generateSecret()).thenReturn("JBSWY3DPEHPK3PXP");
            when(totpService.generateQRCodeUrl("JBSWY3DPEHPK3PXP", "mentor1", "FRC Project Management"))
                .thenReturn("otpauth://totp/FRC%20Project%20Management:mentor1?secret=JBSWY3DPEHPK3PXP&issuer=FRC%20Project%20Management");
            
            // When
            MFAService.MFASetupData setupData = mfaService.initiateMFASetup(mentorUser.getId());
            
            // Then
            assertNotNull(setupData, "Setup data should not be null");
            assertEquals("JBSWY3DPEHPK3PXP", setupData.getSecret(), "Secret should match generated value");
            assertTrue(setupData.getQrCodeUrl().contains("mentor1"), "QR URL should contain username");
            assertEquals("FRC Project Management", setupData.getIssuer(), "Issuer should be correct");
            
            verify(auditService).logSecurityEvent(
                eq(mentorUser),
                eq("MFA_SETUP_INITIATED"),
                eq("Multi-factor authentication setup initiated")
            );
        }
        
        @Test
        @DisplayName("Should initiate MFA setup for admin successfully")
        void shouldInitiateMFASetupForAdminSuccessfully() {
            // Given
            when(userService.findById(adminUser.getId())).thenReturn(adminUser);
            when(totpService.generateSecret()).thenReturn("GEZDGNBVGY3TQOJQ");
            when(totpService.generateQRCodeUrl(anyString(), anyString(), anyString()))
                .thenReturn("otpauth://totp/test");
            
            // When
            MFAService.MFASetupData setupData = mfaService.initiateMFASetup(adminUser.getId());
            
            // Then
            assertNotNull(setupData, "Setup data should not be null");
            assertEquals("GEZDGNBVGY3TQOJQ", setupData.getSecret(), "Secret should match generated value");
            
            verify(auditService).logSecurityEvent(
                eq(adminUser),
                eq("MFA_SETUP_INITIATED"),
                eq("Multi-factor authentication setup initiated")
            );
        }
        
        @Test
        @DisplayName("Should reject MFA setup for student role")
        void shouldRejectMFASetupForStudentRole() {
            // Given
            when(userService.findById(studentUser.getId())).thenReturn(studentUser);
            
            // When/Then
            IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> mfaService.initiateMFASetup(studentUser.getId()));
            
            assertTrue(exception.getMessage().contains("MFA not required for user role"));
            verify(totpService, never()).generateSecret();
            verify(auditService, never()).logSecurityEvent(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should reject MFA setup if already enabled")
        void shouldRejectMFASetupIfAlreadyEnabled() {
            // Given
            when(userService.findById(mfaEnabledMentor.getId())).thenReturn(mfaEnabledMentor);
            
            // When/Then
            IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> mfaService.initiateMFASetup(mfaEnabledMentor.getId()));
            
            assertTrue(exception.getMessage().contains("MFA already enabled"));
            verify(totpService, never()).generateSecret();
        }
        
        @Test
        @DisplayName("Should handle non-existent user")
        void shouldHandleNonExistentUser() {
            // Given
            when(userService.findById(999L)).thenReturn(null);
            
            // When/Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> mfaService.initiateMFASetup(999L));
            
            assertTrue(exception.getMessage().contains("User not found"));
        }
        
        @Test
        @DisplayName("Should format secret for manual entry")
        void shouldFormatSecretForManualEntry() {
            // Given
            when(userService.findById(mentorUser.getId())).thenReturn(mentorUser);
            when(totpService.generateSecret()).thenReturn("JBSWY3DPEHPK3PXP");
            when(totpService.generateQRCodeUrl(anyString(), anyString(), anyString()))
                .thenReturn("otpauth://totp/test");
            
            // When
            MFAService.MFASetupData setupData = mfaService.initiateMFASetup(mentorUser.getId());
            
            // Then
            String formattedSecret = setupData.getFormattedSecret();
            assertEquals("JBSW Y3DP EHPK 3PXP", formattedSecret, 
                "Secret should be formatted with spaces every 4 characters");
        }
    }
    
    @Nested
    @DisplayName("MFA Setup Completion Tests")
    class MFASetupCompletionTests {
        
        @Test
        @DisplayName("Should complete MFA setup with valid token")
        void shouldCompleteMFASetupWithValidToken() {
            // Given
            String secret = "JBSWY3DPEHPK3PXP";
            String validToken = "123456";
            
            when(userService.findById(mentorUser.getId())).thenReturn(mentorUser);
            when(totpService.isValidTokenFormat(validToken)).thenReturn(true);
            when(totpService.validateToken(secret, validToken)).thenReturn(true);
            when(userService.enableMFA(mentorUser.getId(), secret)).thenReturn(mentorUser);
            
            // When
            boolean result = mfaService.completeMFASetup(mentorUser.getId(), secret, validToken);
            
            // Then
            assertTrue(result, "MFA setup should complete successfully");
            verify(userService).enableMFA(mentorUser.getId(), secret);
            verify(auditService).logSecurityEvent(
                eq(mentorUser),
                eq("MFA_SETUP_COMPLETED"),
                eq("Multi-factor authentication setup completed successfully")
            );
        }
        
        @Test
        @DisplayName("Should reject MFA setup with invalid token format")
        void shouldRejectMFASetupWithInvalidTokenFormat() {
            // Given
            String secret = "JBSWY3DPEHPK3PXP";
            String invalidToken = "12345"; // Too short
            
            when(userService.findById(mentorUser.getId())).thenReturn(mentorUser);
            when(totpService.isValidTokenFormat(invalidToken)).thenReturn(false);
            
            // When
            boolean result = mfaService.completeMFASetup(mentorUser.getId(), secret, invalidToken);
            
            // Then
            assertFalse(result, "MFA setup should fail with invalid token format");
            verify(totpService, never()).validateToken(any(), any());
            verify(userService, never()).enableMFA(any(), any());
            verify(auditService).logSecurityEvent(
                eq(mentorUser),
                eq("MFA_SETUP_FAILED"),
                eq("Invalid verification code format during MFA setup")
            );
        }
        
        @Test
        @DisplayName("Should reject MFA setup with incorrect token")
        void shouldRejectMFASetupWithIncorrectToken() {
            // Given
            String secret = "JBSWY3DPEHPK3PXP";
            String incorrectToken = "654321";
            
            when(userService.findById(mentorUser.getId())).thenReturn(mentorUser);
            when(totpService.isValidTokenFormat(incorrectToken)).thenReturn(true);
            when(totpService.validateToken(secret, incorrectToken)).thenReturn(false);
            
            // When
            boolean result = mfaService.completeMFASetup(mentorUser.getId(), secret, incorrectToken);
            
            // Then
            assertFalse(result, "MFA setup should fail with incorrect token");
            verify(userService, never()).enableMFA(any(), any());
            verify(auditService).logSecurityEvent(
                eq(mentorUser),
                eq("MFA_SETUP_FAILED"),
                eq("Invalid verification code during MFA setup")
            );
        }
        
        @Test
        @DisplayName("Should handle service exceptions during MFA enablement")
        void shouldHandleServiceExceptionsDuringMFAEnablement() {
            // Given
            String secret = "JBSWY3DPEHPK3PXP";
            String validToken = "123456";
            
            when(userService.findById(mentorUser.getId())).thenReturn(mentorUser);
            when(totpService.isValidTokenFormat(validToken)).thenReturn(true);
            when(totpService.validateToken(secret, validToken)).thenReturn(true);
            when(userService.enableMFA(mentorUser.getId(), secret)).thenThrow(new RuntimeException("Database error"));
            
            // When
            boolean result = mfaService.completeMFASetup(mentorUser.getId(), secret, validToken);
            
            // Then
            assertFalse(result, "MFA setup should fail when service throws exception");
            verify(auditService).logSecurityAlert(
                eq(mentorUser),
                eq("MFA_SETUP_ERROR"),
                contains("Error enabling MFA: Database error")
            );
        }
    }
    
    @Nested
    @DisplayName("MFA Token Validation Tests")
    class MFATokenValidationTests {
        
        @Test
        @DisplayName("Should validate correct MFA token")
        void shouldValidateCorrectMFAToken() {
            // Given
            String validToken = "123456";
            
            when(userService.findById(mfaEnabledMentor.getId())).thenReturn(mfaEnabledMentor);
            when(totpService.isValidTokenFormat(validToken)).thenReturn(true);
            when(totpService.validateToken(mfaEnabledMentor.getTotpSecret(), validToken)).thenReturn(true);
            
            // When
            boolean result = mfaService.validateMFAToken(mfaEnabledMentor.getId(), validToken);
            
            // Then
            assertTrue(result, "Valid MFA token should be accepted");
            verify(auditService).logAction(
                eq(mfaEnabledMentor),
                eq("MFA_VALIDATION_SUCCESS"),
                eq("Multi-factor authentication validated successfully")
            );
        }
        
        @Test
        @DisplayName("Should reject incorrect MFA token")
        void shouldRejectIncorrectMFAToken() {
            // Given
            String incorrectToken = "654321";
            
            when(userService.findById(mfaEnabledMentor.getId())).thenReturn(mfaEnabledMentor);
            when(totpService.isValidTokenFormat(incorrectToken)).thenReturn(true);
            when(totpService.validateToken(mfaEnabledMentor.getTotpSecret(), incorrectToken)).thenReturn(false);
            
            // When
            boolean result = mfaService.validateMFAToken(mfaEnabledMentor.getId(), incorrectToken);
            
            // Then
            assertFalse(result, "Incorrect MFA token should be rejected");
            verify(auditService).logSecurityEvent(
                eq(mfaEnabledMentor),
                eq("MFA_VALIDATION_FAILED"),
                eq("Invalid MFA token provided")
            );
        }
        
        @Test
        @DisplayName("Should reject validation for user without MFA enabled")
        void shouldRejectValidationForUserWithoutMFAEnabled() {
            // Given
            String token = "123456";
            
            when(userService.findById(mentorUser.getId())).thenReturn(mentorUser);
            
            // When
            boolean result = mfaService.validateMFAToken(mentorUser.getId(), token);
            
            // Then
            assertFalse(result, "Validation should fail for user without MFA");
            verify(auditService).logSecurityAlert(
                eq(mentorUser),
                eq("MFA_VALIDATION_ERROR"),
                eq("MFA validation attempted for user without MFA enabled")
            );
        }
        
        @Test
        @DisplayName("Should handle invalid token format during validation")
        void shouldHandleInvalidTokenFormatDuringValidation() {
            // Given
            String invalidToken = "abc123";
            
            when(userService.findById(mfaEnabledMentor.getId())).thenReturn(mfaEnabledMentor);
            when(totpService.isValidTokenFormat(invalidToken)).thenReturn(false);
            
            // When
            boolean result = mfaService.validateMFAToken(mfaEnabledMentor.getId(), invalidToken);
            
            // Then
            assertFalse(result, "Invalid token format should be rejected");
            verify(auditService).logSecurityEvent(
                eq(mfaEnabledMentor),
                eq("MFA_VALIDATION_FAILED"),
                eq("Invalid MFA token format")
            );
        }
        
        @Test
        @DisplayName("Should handle non-existent user during validation")
        void shouldHandleNonExistentUserDuringValidation() {
            // Given
            when(userService.findById(999L)).thenReturn(null);
            
            // When
            boolean result = mfaService.validateMFAToken(999L, "123456");
            
            // Then
            assertFalse(result, "Validation should fail for non-existent user");
            verify(auditService, never()).logAction(any(), any(), any());
        }
    }
    
    @Nested
    @DisplayName("MFA Management Tests")
    class MFAManagementTests {
        
        @Test
        @DisplayName("Should disable MFA for user by admin")
        void shouldDisableMFAForUserByAdmin() {
            // Given
            when(userService.findById(mfaEnabledMentor.getId())).thenReturn(mfaEnabledMentor);
            when(userService.findById(adminUser.getId())).thenReturn(adminUser);
            when(userService.disableMFA(mfaEnabledMentor.getId())).thenReturn(mfaEnabledMentor);
            
            // When
            boolean result = mfaService.disableMFA(mfaEnabledMentor.getId(), adminUser.getId());
            
            // Then
            assertTrue(result, "Admin should be able to disable MFA");
            verify(userService).disableMFA(mfaEnabledMentor.getId());
            verify(auditService).logSecurityEvent(
                eq(adminUser),
                eq("MFA_DISABLED_BY_ADMIN"),
                contains("MFA disabled by admin for user: mentor2")
            );
        }
        
        @Test
        @DisplayName("Should reject MFA disable by non-admin")
        void shouldRejectMFADisableByNonAdmin() {
            // Given
            when(userService.findById(mfaEnabledMentor.getId())).thenReturn(mfaEnabledMentor);
            when(userService.findById(mentorUser.getId())).thenReturn(mentorUser);
            
            // When
            boolean result = mfaService.disableMFA(mfaEnabledMentor.getId(), mentorUser.getId());
            
            // Then
            assertFalse(result, "Non-admin should not be able to disable MFA");
            verify(userService, never()).disableMFA(any());
            verify(auditService).logSecurityAlert(
                eq(mentorUser),
                eq("UNAUTHORIZED_MFA_DISABLE"),
                contains("Non-admin user attempted to disable MFA")
            );
        }
        
        @Test
        @DisplayName("Should find users requiring MFA setup")
        void shouldFindUsersRequiringMFASetup() {
            // Given
            List<User> usersRequiringMFA = List.of(mentorUser, adminUser);
            when(userService.findUsersRequiringMFA()).thenReturn(usersRequiringMFA);
            
            // When
            List<User> result = mfaService.getUsersRequiringMFASetup();
            
            // Then
            assertEquals(2, result.size(), "Should find users requiring MFA setup");
            assertTrue(result.contains(mentorUser), "Should include mentor without MFA");
            assertTrue(result.contains(adminUser), "Should include admin without MFA");
        }
        
        @Test
        @DisplayName("Should determine if user requires MFA setup")
        void shouldDetermineIfUserRequiresMFASetup() {
            // When/Then
            assertTrue(mfaService.requiresMFASetup(mentorUser), 
                "Mentor without MFA should require setup");
            assertTrue(mfaService.requiresMFASetup(adminUser), 
                "Admin without MFA should require setup");
            assertFalse(mfaService.requiresMFASetup(mfaEnabledMentor), 
                "Mentor with MFA should not require setup");
            assertFalse(mfaService.requiresMFASetup(studentUser), 
                "Student should not require MFA setup");
            assertFalse(mfaService.requiresMFASetup(null), 
                "Null user should not require setup");
        }
    }
    
    @Nested
    @DisplayName("Recovery Code Tests")
    class RecoveryCodeTests {
        
        @Test
        @DisplayName("Should generate recovery codes for MFA-enabled user")
        void shouldGenerateRecoveryCodesForMFAEnabledUser() {
            // Given
            when(userService.findById(mfaEnabledMentor.getId())).thenReturn(mfaEnabledMentor);
            
            // When
            String[] recoveryCodes = mfaService.generateRecoveryCodes(mfaEnabledMentor.getId());
            
            // Then
            assertNotNull(recoveryCodes, "Recovery codes should not be null");
            assertEquals(10, recoveryCodes.length, "Should generate 10 recovery codes");
            
            // Verify all codes are valid format (8 character alphanumeric)
            for (String code : recoveryCodes) {
                assertNotNull(code, "Recovery code should not be null");
                assertEquals(8, code.length(), "Recovery code should be 8 characters");
                assertTrue(code.matches("^[A-Z0-9]+$"), "Recovery code should be alphanumeric uppercase");
            }
            
            // Verify all codes are unique
            java.util.Set<String> uniqueCodes = java.util.Set.of(recoveryCodes);
            assertEquals(10, uniqueCodes.size(), "All recovery codes should be unique");
            
            verify(auditService).logSecurityEvent(
                eq(mfaEnabledMentor),
                eq("MFA_RECOVERY_CODES_GENERATED"),
                eq("Recovery codes generated for MFA backup access")
            );
        }
        
        @Test
        @DisplayName("Should reject recovery code generation for user without MFA")
        void shouldRejectRecoveryCodeGenerationForUserWithoutMFA() {
            // Given
            when(userService.findById(mentorUser.getId())).thenReturn(mentorUser);
            
            // When/Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> mfaService.generateRecoveryCodes(mentorUser.getId()));
            
            assertTrue(exception.getMessage().contains("MFA not enabled"));
            verify(auditService, never()).logSecurityEvent(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should handle non-existent user for recovery codes")
        void shouldHandleNonExistentUserForRecoveryCodes() {
            // Given
            when(userService.findById(999L)).thenReturn(null);
            
            // When/Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> mfaService.generateRecoveryCodes(999L));
            
            assertTrue(exception.getMessage().contains("User not found"));
        }
    }
    
    @Nested
    @DisplayName("Token Time Management Tests")
    class TokenTimeManagementTests {
        
        @Test
        @DisplayName("Should provide token time remaining")
        void shouldProvideTokenTimeRemaining() {
            // Given
            when(totpService.getSecondsUntilExpiration()).thenReturn(25);
            
            // When
            int timeRemaining = mfaService.getTokenTimeRemaining();
            
            // Then
            assertEquals(25, timeRemaining, "Should return time from TOTP service");
            verify(totpService).getSecondsUntilExpiration();
        }
        
        @Test
        @DisplayName("Should handle varying token expiration times")
        void shouldHandleVaryingTokenExpirationTimes() {
            // Test different expiration times
            int[] testTimes = {30, 15, 5, 1};
            
            for (int expectedTime : testTimes) {
                when(totpService.getSecondsUntilExpiration()).thenReturn(expectedTime);
                
                int actualTime = mfaService.getTokenTimeRemaining();
                assertEquals(expectedTime, actualTime, 
                    "Should return correct time for " + expectedTime + " seconds");
            }
        }
    }
    
    @Nested
    @DisplayName("Error Handling and Edge Cases Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle null parameters gracefully")
        void shouldHandleNullParametersGracefully() {
            // When/Then
            assertThrows(IllegalArgumentException.class, 
                () -> mfaService.initiateMFASetup(null));
            assertThrows(IllegalArgumentException.class, 
                () -> mfaService.completeMFASetup(null, "secret", "123456"));
            assertThrows(IllegalArgumentException.class, 
                () -> mfaService.completeMFASetup(1L, null, "123456"));
            assertThrows(IllegalArgumentException.class, 
                () -> mfaService.completeMFASetup(1L, "secret", null));
            
            assertFalse(mfaService.validateMFAToken(null, "123456"));
            assertFalse(mfaService.validateMFAToken(1L, null));
            
            assertFalse(mfaService.disableMFA(null, 1L));
            assertFalse(mfaService.disableMFA(1L, null));
        }
        
        @Test
        @DisplayName("Should handle service failures gracefully")
        void shouldHandleServiceFailuresGracefully() {
            // Given
            when(userService.findById(mentorUser.getId())).thenThrow(new RuntimeException("Database error"));
            
            // When/Then
            assertThrows(RuntimeException.class, 
                () -> mfaService.initiateMFASetup(mentorUser.getId()));
            
            // Validation should return false, not throw
            assertFalse(mfaService.validateMFAToken(mentorUser.getId(), "123456"));
        }
        
        @Test
        @DisplayName("Should handle TOTP service failures")
        void shouldHandleTOTPServiceFailures() {
            // Given
            when(userService.findById(mfaEnabledMentor.getId())).thenReturn(mfaEnabledMentor);
            when(totpService.isValidTokenFormat("123456")).thenReturn(true);
            when(totpService.validateToken(anyString(), anyString())).thenThrow(new RuntimeException("Crypto error"));
            
            // When/Then - Should throw exception since MFAService doesn't handle TOTP exceptions
            assertThrows(RuntimeException.class, () -> {
                mfaService.validateMFAToken(mfaEnabledMentor.getId(), "123456");
            }, "MFAService should propagate TOTP service exceptions");
            
            // Should still log the attempt
            verify(auditService, atLeastOnce()).logSecurityEvent(any(), any(), any());
        }
    }
    
    @Nested
    @DisplayName("FRC-Specific Integration Tests")
    class FRCSpecificTests {
        
        @Test
        @DisplayName("Should support multiple mentor MFA setup simultaneously")
        void shouldSupportMultipleMentorMFASetupSimultaneously() {
            // Given - Multiple mentors setting up MFA
            User mentor1 = createUser(10L, "mentor1", UserRole.MENTOR, false, null);
            User mentor2 = createUser(11L, "mentor2", UserRole.MENTOR, false, null);
            User mentor3 = createUser(12L, "mentor3", UserRole.MENTOR, false, null);
            
            when(userService.findById(10L)).thenReturn(mentor1);
            when(userService.findById(11L)).thenReturn(mentor2);
            when(userService.findById(12L)).thenReturn(mentor3);
            
            when(totpService.generateSecret())
                .thenReturn("SECRET1")
                .thenReturn("SECRET2")
                .thenReturn("SECRET3");
            
            when(totpService.generateQRCodeUrl(anyString(), anyString(), anyString()))
                .thenReturn("qr-url-1")
                .thenReturn("qr-url-2")
                .thenReturn("qr-url-3");
            
            // When
            MFAService.MFASetupData setup1 = mfaService.initiateMFASetup(10L);
            MFAService.MFASetupData setup2 = mfaService.initiateMFASetup(11L);
            MFAService.MFASetupData setup3 = mfaService.initiateMFASetup(12L);
            
            // Then
            assertNotEquals(setup1.getSecret(), setup2.getSecret(), "Each mentor should have unique secret");
            assertNotEquals(setup2.getSecret(), setup3.getSecret(), "Each mentor should have unique secret");
            assertNotEquals(setup1.getSecret(), setup3.getSecret(), "Each mentor should have unique secret");
            
            verify(auditService, times(3)).logSecurityEvent(any(), eq("MFA_SETUP_INITIATED"), any());
        }
        
        @Test
        @DisplayName("Should handle build season high-stress authentication")
        void shouldHandleBuildSeasonHighStressAuthentication() {
            // Given - Simulate rapid authentication during busy build season
            when(userService.findById(mfaEnabledMentor.getId())).thenReturn(mfaEnabledMentor);
            when(totpService.isValidTokenFormat(anyString())).thenReturn(true);
            when(totpService.validateToken(anyString(), anyString())).thenReturn(true);
            
            // When - Multiple rapid authentications
            for (int i = 0; i < 20; i++) {
                boolean result = mfaService.validateMFAToken(mfaEnabledMentor.getId(), "123456");
                assertTrue(result, "Authentication should work consistently during high activity");
            }
            
            // Then
            verify(auditService, times(20)).logAction(any(), eq("MFA_VALIDATION_SUCCESS"), any());
        }
        
        @Test
        @DisplayName("Should maintain security during team role transitions")
        void shouldMaintainSecurityDuringTeamRoleTransitions() {
            // Given - Student promoted to mentor (requires MFA)
            User promotedStudent = createUser(20L, "promoted", UserRole.STUDENT, false, null);
            when(userService.findById(20L)).thenReturn(promotedStudent);
            
            // When - Try to setup MFA while still student role
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> mfaService.initiateMFASetup(20L));
            
            // Then
            assertTrue(exception.getMessage().contains("MFA not required for user role"));
            
            // When - After role change to mentor
            promotedStudent.setRole(UserRole.MENTOR);
            when(totpService.generateSecret()).thenReturn("NEWSECRET");
            when(totpService.generateQRCodeUrl(anyString(), anyString(), anyString())).thenReturn("new-qr");
            
            MFAService.MFASetupData setup = mfaService.initiateMFASetup(20L);
            
            // Then
            assertNotNull(setup, "Should be able to setup MFA after role change");
            verify(auditService).logSecurityEvent(eq(promotedStudent), eq("MFA_SETUP_INITIATED"), any());
        }
    }
    
    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    
    private User createUser(Long id, String username, UserRole role, boolean mfaEnabled, String totpSecret) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@frcteam.org");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(role);
        user.setMfaEnabled(mfaEnabled);
        user.setTotpSecret(totpSecret);
        user.setEnabled(true);
        return user;
    }
}