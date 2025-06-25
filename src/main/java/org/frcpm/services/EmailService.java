// src/main/java/org/frcpm/services/EmailService.java

package org.frcpm.services;

import org.frcpm.models.User;

/**
 * Service for email operations including COPPA compliance notifications.
 */
public interface EmailService {
    
    /**
     * Sends a parental consent request email.
     * 
     * @param user the user requiring consent
     * @param consentToken the consent token for the URL
     * @return true if email was sent successfully
     */
    boolean sendParentalConsentRequest(User user, String consentToken);
    
    /**
     * Sends a consent reminder email.
     * 
     * @param user the user requiring consent
     * @return true if email was sent successfully
     */
    boolean sendConsentReminder(User user);
    
    /**
     * Sends a general notification email.
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param body email body
     * @return true if email was sent successfully
     */
    boolean sendEmail(String to, String subject, String body);
}