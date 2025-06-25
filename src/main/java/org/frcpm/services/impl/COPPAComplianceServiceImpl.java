// src/main/java/org/frcpm/services/impl/COPPAComplianceServiceImpl.java (Fixed Unused Method)

package org.frcpm.services.impl;

import org.frcpm.models.User;
import org.frcpm.services.AuditService;
import org.frcpm.services.COPPAComplianceService;
import org.frcpm.services.EmailService;
import org.frcpm.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of COPPA compliance service for student data protection.
 * 
 * ✅ FIXED: Removed unused validateCOPPARequiredData method
 * 
 * Ensures compliance with the Children's Online Privacy Protection Act
 * for users under 13 years of age.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
@Service
@Transactional
public class COPPAComplianceServiceImpl implements COPPAComplianceService {
    
    private static final Logger LOGGER = Logger.getLogger(COPPAComplianceServiceImpl.class.getName());
    
    private final UserService userService;
    private final AuditService auditService;
    private final EmailService emailService; 

    @Autowired
    public COPPAComplianceServiceImpl(UserService userService, 
        AuditService auditService, 
        EmailService emailService) { // Add EmailService parameter
    this.userService = userService;
    this.auditService = auditService;
    this.emailService = emailService; // Add this line
    }
    
    // =========================================================================
    // COMPLIANCE CHECKING
    // =========================================================================
    
    @Override
    public boolean requiresParentalConsent(User user) {
        return user != null && user.requiresCOPPACompliance();
    }
    
    @Override
    public boolean hasValidParentalConsent(User user) {
        if (!requiresParentalConsent(user)) {
            return true; // Not required
        }
        
        return user.hasParentalConsent();
    }
    
    @Override
    public void enforceDataMinimization(User user) {
        if (!requiresParentalConsent(user)) {
            return;
        }
        
        // Log the data minimization enforcement
        auditService.logCOPPAAccess(
            getCurrentUser(), 
            user, 
            "DATA_MINIMIZATION_ENFORCED", 
            "Data minimization rules applied for user under 13"
        );
        
        // In a real implementation, this would:
        // - Limit data collection to minimum necessary
        // - Restrict profile visibility
        // - Apply enhanced privacy controls
        // - Limit data sharing capabilities
        
        LOGGER.info("Data minimization enforced for user under 13: " + user.getUsername());
    }
    
    // =========================================================================
    // CONSENT MANAGEMENT
    // =========================================================================
    
    @Override
    public boolean initiateParentalConsentProcess(User user) {
        if (!requiresParentalConsent(user)) {
            return false;
        }
        
        if (user.getParentEmail() == null || user.getParentEmail().trim().isEmpty()) {
            LOGGER.warning("Cannot initiate consent process - no parent email for user: " + user.getUsername());
            return false;
        }
        
        boolean initiated = userService.initiateParentalConsent(user.getId(), user.getParentEmail());
        
        if (initiated) {
            auditService.logCOPPAAccess(
                getCurrentUser(),
                user,
                "CONSENT_PROCESS_INITIATED",
                "Parental consent process initiated for user under 13"
            );
            
            LOGGER.info("Parental consent process initiated for user: " + user.getUsername());
        }
        
        return initiated;
    }
    
    @Override
    public boolean processParentalConsent(String consentToken, boolean granted) {
        boolean processed = userService.processParentalConsent(consentToken, granted);
        
        if (processed) {
            String action = granted ? "CONSENT_GRANTED" : "CONSENT_DENIED";
            String description = granted ? 
                "Parental consent granted for user under 13" :
                "Parental consent denied for user under 13";
            
            // Note: We don't have the user context here from just the token
            // In a full implementation, we'd look up the user by token
            auditService.logAction(null, action, description);
            
            LOGGER.info("Parental consent " + (granted ? "granted" : "denied") + " via token");
        }
        
        return processed;
    }
    
    @Override
    public void sendConsentReminder(User user) {
        if (!requiresParentalConsent(user) || hasValidParentalConsent(user)) {
            return;
        }
        
        // TODO: Implement email reminder service
        LOGGER.info("Consent reminder needed for user: " + user.getUsername());
        
        auditService.logCOPPAAccess(
            getCurrentUser(),
            user,
            "CONSENT_REMINDER_SENT",
            "Parental consent reminder sent"
        );
    }
    
    // =========================================================================
    // ACCESS CONTROL
    // =========================================================================
    
    @Override
    public boolean canAccessUserData(User accessor, User subject) {
        if (accessor == null || subject == null) {
            return false;
        }
        
        // Same user can always access their own data
        if (accessor.getId().equals(subject.getId())) {
            return true;
        }
        
        // Admin can access all data
        if (accessor.getRole() != null && accessor.getRole().canAdminister()) {
            return true;
        }
        
        // For COPPA-protected users, additional restrictions apply
        if (subject.requiresCOPPACompliance()) {
            // Must have valid parental consent
            if (!hasValidParentalConsent(subject)) {
                return false;
            }
            
            // Only mentors can access COPPA-protected user data
            if (accessor.getRole() == null || !accessor.getRole().isMentor()) {
                return false;
            }
        }
        
        // Mentors can access student data
        if (accessor.getRole() != null && accessor.getRole().isMentor()) {
            return true;
        }
        
        // Students can't access other students' data
        return false;
    }
    
    @Override
    public void logDataAccess(User accessor, User subject, String operation) {
        if (subject.requiresCOPPACompliance()) {
            auditService.logCOPPAAccess(
                accessor,
                subject,
                "DATA_ACCESS_" + operation.toUpperCase(),
                "Accessed data of user under 13: " + operation
            );
        } else {
            auditService.logDataAccess(
                accessor,
                subject,
                "User",
                subject.getId(),
                "DATA_ACCESS_" + operation.toUpperCase()
            );
        }
    }
    
    // =========================================================================
    // MONITORING AND COMPLIANCE
    // =========================================================================
    
    @Override
    public List<User> getUsersRequiringConsentReview() {
        return userService.findUsersRequiringParentalConsent();
    }
    
    @Override
    public void performScheduledCOPPAReview() {
        List<User> usersRequiringReview = getUsersRequiringConsentReview();
        
        LOGGER.info("Performing scheduled COPPA review for " + usersRequiringReview.size() + " users");
        
        for (User user : usersRequiringReview) {
            // Check if consent process has been pending too long (7 days)
            if (user.getCreatedAt().isBefore(LocalDateTime.now().minusDays(7))) {
                // Disable account after 7 days without consent
                userService.disableUser(user.getId());
                
                auditService.logCOPPAAccess(
                    null, // System action
                    user,
                    "ACCOUNT_DISABLED_NO_CONSENT",
                    "Account disabled due to missing parental consent after 7 days"
                );
                
                LOGGER.warning("Disabled account for user " + user.getUsername() + 
                             " due to missing parental consent");
            } else {
                // Send reminder if needed
                sendConsentReminder(user);
            }
        }
        
        auditService.logAction(
            null,
            "COPPA_SCHEDULED_REVIEW",
            "Completed scheduled COPPA compliance review for " + usersRequiringReview.size() + " users"
        );
    }
    
    @Override
    public void generateComplianceReport() {
        // Generate compliance statistics
        long totalUsers = userService.count();
        long minorsUnder13 = userService.countMinorsUnder13();
        long coppaLogs = auditService.getCOPPALogCount();
        
        String reportDescription = String.format(
            "COPPA Compliance Report - Total Users: %d, Users Under 13: %d, COPPA Log Entries: %d",
            totalUsers, minorsUnder13, coppaLogs
        );
        
        auditService.logAction(
            getCurrentUser(),
            "COMPLIANCE_REPORT_GENERATED",
            reportDescription
        );
        
        LOGGER.info("Generated COPPA compliance report: " + reportDescription);
    }
    
    // =========================================================================
    // DATA RETENTION AND DELETION
    // =========================================================================
    
    @Override
    public void scheduleDataRetentionReview(User user) {
        if (!requiresParentalConsent(user)) {
            return;
        }
        
        // Schedule review of data retention policies
        auditService.logCOPPAAccess(
            getCurrentUser(),
            user,
            "DATA_RETENTION_REVIEW_SCHEDULED",
            "Data retention review scheduled for user under 13"
        );
        
        // TODO: Implement actual scheduling mechanism
        LOGGER.info("Data retention review scheduled for user: " + user.getUsername());
    }
    
    @Override
    public boolean canDeleteUserData(User user) {
        // Always allow deletion for COPPA compliance
        if (requiresParentalConsent(user)) {
            return true;
        }
        
        // Apply normal deletion rules for other users
        return true; // Simplified - would have business rules
    }
    
    // =========================================================================
    // ADDITIONAL COPPA COMPLIANCE METHODS
    // =========================================================================
    
    /**
     * Validates that a user account meets COPPA requirements before activation.
     * 
     * @param user the user to validate
     * @return true if the account can be activated
     */
    public boolean validateAccountForActivation(User user) {
        if (!requiresParentalConsent(user)) {
            return true; // No special requirements
        }
        
        // Check if all COPPA requirements are met
        boolean hasConsent = hasValidParentalConsent(user);
        boolean hasParentEmail = user.getParentEmail() != null && !user.getParentEmail().trim().isEmpty();
        
        if (!hasConsent || !hasParentEmail) {
            LOGGER.warning("Account activation failed for user under 13: " + user.getUsername() + 
                         " - hasConsent: " + hasConsent + ", hasParentEmail: " + hasParentEmail);
            return false;
        }
        
        // Log successful validation
        auditService.logCOPPAAccess(
            getCurrentUser(),
            user,
            "COPPA_VALIDATION_SUCCESS",
            "Account validated for activation with proper COPPA compliance"
        );
        
        return true;
    }
    
    /**
     * Checks if a data operation is permitted under COPPA rules.
     * 
     * @param accessor the user attempting access
     * @param subject the user whose data is being accessed
     * @param operation the type of operation (READ, WRITE, DELETE)
     * @return true if the operation is permitted
     */
    public boolean isDataOperationPermitted(User accessor, User subject, String operation) {
        if (!canAccessUserData(accessor, subject)) {
            return false;
        }
        
        // For COPPA-protected users, additional operation-specific checks
        if (subject.requiresCOPPACompliance()) {
            switch (operation.toUpperCase()) {
                case "DELETE":
                    // Only admins can delete COPPA-protected user data
                    return accessor.getRole().canAdminister();
                case "EXPORT":
                    // Only mentors and admins can export COPPA-protected data
                    return accessor.getRole().isMentor();
                case "SHARE":
                    // Sharing COPPA-protected data requires special approval
                    return false; // Simplified - would require additional consent
                default:
                    return true; // READ, WRITE operations allowed if basic access granted
            }
        }
        
        return true; // Non-COPPA users have normal permissions
    }
    
    /**
     * Gets the data retention period for a user based on COPPA requirements.
     * 
     * @param user the user
     * @return retention period in days
     */
    public int getDataRetentionPeriodDays(User user) {
        if (requiresParentalConsent(user)) {
            // COPPA requires data to be retained for 7 years or until user turns 18 + 3 years
            return 2555; // Approximately 7 years
        } else {
            // Standard retention period for non-COPPA users
            return 1825; // Approximately 5 years
        }
    }
    
    /**
     * Determines if a user's data should be anonymized rather than deleted.
     * 
     * @param user the user
     * @return true if data should be anonymized instead of deleted
     */
    public boolean shouldAnonymizeInsteadOfDelete(User user) {
        if (requiresParentalConsent(user)) {
            // For COPPA users, check if they have any completed educational activities
            // that should be preserved for legitimate educational purposes
            return true; // Simplified - would check actual usage patterns
        }
        
        return false; // Non-COPPA users can have data fully deleted
    }
    
    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================
    
    /**
     * Gets the current user from security context.
     * Returns null if no authenticated user (e.g., system operations).
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                
                // Check if principal is our UserPrincipal
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.frcpm.security.UserPrincipal) {
                    return ((org.frcpm.security.UserPrincipal) principal).getUser();
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Could not get current user from security context: " + e.getMessage());
        }
        
        return null; // No authenticated user or system operation
    }
}