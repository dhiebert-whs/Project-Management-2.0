// src/main/java/org/frcpm/security/MFAService.java

package org.frcpm.security;

import org.frcpm.models.User;
import org.frcpm.services.AuditService;
import org.frcpm.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

/**
 * Multi-Factor Authentication service for managing TOTP setup and validation.
 * 
 * Coordinates between TOTP generation, user management, and audit logging
 * to provide comprehensive MFA functionality for mentor and admin users.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
@Service
@Transactional
public class MFAService {
    
    private static final Logger LOGGER = Logger.getLogger(MFAService.class.getName());
    private static final String MFA_ISSUER = "FRC Project Management";
    
    private final TOTPService totpService;
    private final UserService userService;
    private final AuditService auditService;
    
    @Autowired
    public MFAService(TOTPService totpService, UserService userService, AuditService auditService) {
        this.totpService = totpService;
        this.userService = userService;
        this.auditService = auditService;
    }
    
    /**
     * Initiates MFA setup for a user who requires it.
     * 
     * @param userId the user ID
     * @return MFA setup data containing secret and QR code URL
     */
    public MFASetupData initiateMFASetup(Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        if (!user.getRole().requiresMFA()) {
            throw new IllegalStateException("MFA not required for user role: " + user.getRole());
        }
        
        if (user.isMfaEnabled()) {
            throw new IllegalStateException("MFA already enabled for user: " + user.getUsername());
        }
        
        // Generate new secret
        String secret = totpService.generateSecret();
        String qrCodeUrl = totpService.generateQRCodeUrl(secret, user.getUsername(), MFA_ISSUER);
        
        // Log MFA setup initiation
        auditService.logSecurityEvent(
            user,
            "MFA_SETUP_INITIATED",
            "Multi-factor authentication setup initiated"
        );
        
        return new MFASetupData(secret, qrCodeUrl, MFA_ISSUER);
    }
    
    /**
     * Completes MFA setup by validating the first TOTP code.
     * 
     * @param userId the user ID
     * @param secret the TOTP secret from setup
     * @param verificationCode the 6-digit code from authenticator app
     * @return true if setup completed successfully
     */
    public boolean completeMFASetup(Long userId, String secret, String verificationCode) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        // Validate the verification code
        if (!totpService.isValidTokenFormat(verificationCode)) {
            auditService.logSecurityEvent(
                user,
                "MFA_SETUP_FAILED",
                "Invalid verification code format during MFA setup"
            );
            return false;
        }
        
        if (!totpService.validateToken(secret, verificationCode)) {
            auditService.logSecurityEvent(
                user,
                "MFA_SETUP_FAILED",
                "Invalid verification code during MFA setup"
            );
            return false;
        }
        
        // Enable MFA for the user
        try {
            userService.enableMFA(userId, secret);
            
            auditService.logSecurityEvent(
                user,
                "MFA_SETUP_COMPLETED",
                "Multi-factor authentication setup completed successfully"
            );
            
            LOGGER.info("MFA setup completed for user: " + user.getUsername());
            return true;
            
        } catch (Exception e) {
            auditService.logSecurityAlert(
                user,
                "MFA_SETUP_ERROR",
                "Error enabling MFA: " + e.getMessage()
            );
            LOGGER.severe("Failed to enable MFA for user " + user.getUsername() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates a TOTP code for an MFA-enabled user.
     * 
     * @param userId the user ID
     * @param token the 6-digit TOTP code
     * @return true if the token is valid
     */
    public boolean validateMFAToken(Long userId, String token) {
        User user = userService.findById(userId);
        if (user == null) {
            return false;
        }
        
        if (!user.isMfaEnabled() || user.getTotpSecret() == null) {
            auditService.logSecurityAlert(
                user,
                "MFA_VALIDATION_ERROR",
                "MFA validation attempted for user without MFA enabled"
            );
            return false;
        }
        
        if (!totpService.isValidTokenFormat(token)) {
            auditService.logSecurityEvent(
                user,
                "MFA_VALIDATION_FAILED",
                "Invalid MFA token format"
            );
            return false;
        }
        
        boolean isValid = totpService.validateToken(user.getTotpSecret(), token);
        
        if (isValid) {
            auditService.logAction(
                user,
                "MFA_VALIDATION_SUCCESS",
                "Multi-factor authentication validated successfully"
            );
        } else {
            auditService.logSecurityEvent(
                user,
                "MFA_VALIDATION_FAILED",
                "Invalid MFA token provided"
            );
        }
        
        return isValid;
    }
    
    /**
     * Disables MFA for a user (admin function).
     * 
     * @param userId the user ID
     * @param adminUserId the admin performing the action
     * @return true if MFA was disabled successfully
     */
    public boolean disableMFA(Long userId, Long adminUserId) {
        User user = userService.findById(userId);
        User admin = userService.findById(adminUserId);
        
        if (user == null || admin == null) {
            return false;
        }
        
        if (!admin.getRole().canAdminister()) {
            auditService.logSecurityAlert(
                admin,
                "UNAUTHORIZED_MFA_DISABLE",
                "Non-admin user attempted to disable MFA for user: " + user.getUsername()
            );
            return false;
        }
        
        try {
            userService.disableMFA(userId);
            
            auditService.logSecurityEvent(
                admin,
                "MFA_DISABLED_BY_ADMIN",
                "MFA disabled by admin for user: " + user.getUsername()
            );
            
            LOGGER.warning("MFA disabled by admin " + admin.getUsername() + " for user: " + user.getUsername());
            return true;
            
        } catch (Exception e) {
            auditService.logSecurityAlert(
                admin,
                "MFA_DISABLE_ERROR",
                "Error disabling MFA for user " + user.getUsername() + ": " + e.getMessage()
            );
            return false;
        }
    }
    
    /**
     * Gets users who require MFA but haven't set it up yet.
     * 
     * @return list of users requiring MFA setup
     */
    public List<User> getUsersRequiringMFASetup() {
        return userService.findUsersRequiringMFA();
    }
    
    /**
     * Generates recovery codes for MFA backup access.
     * 
     * @param userId the user ID
     * @return array of recovery codes
     */
    public String[] generateRecoveryCodes(Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        if (!user.isMfaEnabled()) {
            throw new IllegalStateException("MFA not enabled for user: " + user.getUsername());
        }
        
        // Generate 10 random recovery codes
        String[] recoveryCodes = new String[10];
        java.security.SecureRandom random = new java.security.SecureRandom();
        
        for (int i = 0; i < recoveryCodes.length; i++) {
            // Generate 8-character alphanumeric code
            StringBuilder code = new StringBuilder();
            String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            for (int j = 0; j < 8; j++) {
                code.append(charset.charAt(random.nextInt(charset.length())));
            }
            recoveryCodes[i] = code.toString();
        }
        
        auditService.logSecurityEvent(
            user,
            "MFA_RECOVERY_CODES_GENERATED",
            "Recovery codes generated for MFA backup access"
        );
        
        return recoveryCodes;
    }
    
    /**
     * Checks if a user needs to complete MFA setup.
     * 
     * @param user the user to check
     * @return true if MFA setup is required but not completed
     */
    public boolean requiresMFASetup(User user) {
        return user != null && 
               user.getRole().requiresMFA() && 
               !user.isMfaEnabled();
    }
    
    /**
     * Gets the remaining time for the current TOTP token.
     * 
     * @return seconds until current token expires
     */
    public int getTokenTimeRemaining() {
        return totpService.getSecondsUntilExpiration();
    }
    
    /**
     * Data class for MFA setup information.
     */
    public static class MFASetupData {
        private final String secret;
        private final String qrCodeUrl;
        private final String issuer;
        
        public MFASetupData(String secret, String qrCodeUrl, String issuer) {
            this.secret = secret;
            this.qrCodeUrl = qrCodeUrl;
            this.issuer = issuer;
        }
        
        public String getSecret() {
            return secret;
        }
        
        public String getQrCodeUrl() {
            return qrCodeUrl;
        }
        
        public String getIssuer() {
            return issuer;
        }
        
        /**
         * Gets the secret formatted for manual entry.
         * Adds spaces every 4 characters for readability.
         */
        public String getFormattedSecret() {
            if (secret == null) {
                return "";
            }
            
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < secret.length(); i++) {
                if (i > 0 && i % 4 == 0) {
                    formatted.append(" ");
                }
                formatted.append(secret.charAt(i));
            }
            return formatted.toString();
        }
    }
}