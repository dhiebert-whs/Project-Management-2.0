// src/main/java/org/frcpm/services/COPPAComplianceService.java

package org.frcpm.services;

import org.frcpm.models.User;

import java.util.List;

/**
 * Service for COPPA compliance management and student data protection.
 */
public interface COPPAComplianceService {
    
    // Compliance checking
    boolean requiresParentalConsent(User user);
    boolean hasValidParentalConsent(User user);
    void enforceDataMinimization(User user);
    
    // Consent management
    boolean initiateParentalConsentProcess(User user);
    boolean processParentalConsent(String consentToken, boolean granted);
    void sendConsentReminder(User user);
    
    // Access control
    boolean canAccessUserData(User accessor, User subject);
    void logDataAccess(User accessor, User subject, String operation);
    
    // Monitoring and compliance
    List<User> getUsersRequiringConsentReview();
    void performScheduledCOPPAReview();
    void generateComplianceReport();
    
    // Data retention and deletion
    void scheduleDataRetentionReview(User user);
    boolean canDeleteUserData(User user);
}