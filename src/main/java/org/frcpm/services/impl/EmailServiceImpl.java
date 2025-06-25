// src/main/java/org/frcpm/services/impl/EmailServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.User;
import org.frcpm.services.EmailService;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/**
 * Implementation of email service for COPPA compliance notifications.
 * 
 * This is a basic implementation that logs email operations.
 * In production, this would integrate with an actual email service.
 */
@Service
public class EmailServiceImpl implements EmailService {
    
    private static final Logger LOGGER = Logger.getLogger(EmailServiceImpl.class.getName());
    
    @Override
    public boolean sendParentalConsentRequest(User user, String consentToken) {
        // TODO: In production, implement actual email sending
        LOGGER.info("Sending parental consent request email to: " + user.getParentEmail() + 
                   " for user: " + user.getUsername() + " with token: " + consentToken);
        
        // For now, just log and return success
        return true;
    }
    
    @Override
    public boolean sendConsentReminder(User user) {
        // TODO: In production, implement actual email sending
        LOGGER.info("Sending consent reminder email to: " + user.getParentEmail() + 
                   " for user: " + user.getUsername());
        
        // For now, just log and return success
        return true;
    }
    
    @Override
    public boolean sendEmail(String to, String subject, String body) {
        // TODO: In production, implement actual email sending
        LOGGER.info("Sending email to: " + to + " with subject: " + subject);
        
        // For now, just log and return success
        return true;
    }
}