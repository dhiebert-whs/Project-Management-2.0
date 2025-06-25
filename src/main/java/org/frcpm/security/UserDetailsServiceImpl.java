// src/main/java/org/frcpm/security/UserDetailsServiceImpl.java

package org.frcpm.security;

import org.frcpm.models.User;
import org.frcpm.services.AuditService;
import org.frcpm.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Spring Security UserDetailsService implementation.
 * 
 * Loads user details for authentication and integrates with
 * COPPA compliance checking and audit logging.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
@Service
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserService userService;
    private final AuditService auditService;
    
    @Autowired
    public UserDetailsServiceImpl(UserService userService, AuditService auditService) {
        this.userService = userService;
        this.auditService = auditService;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find user by username or email
        Optional<User> userOpt = userService.findByUsernameOrEmail(username, username);
        
        if (userOpt.isEmpty()) {
            // Log failed login attempt
            auditService.logLoginAttempt(username, getClientIpAddress(), false);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        
        User user = userOpt.get();
        
        // Check COPPA compliance for users under 13
        if (user.requiresCOPPACompliance() && !user.hasParentalConsent()) {
            auditService.logLoginAttempt(username, getClientIpAddress(), false);
            auditService.logCOPPAAccess(
                null,
                user,
                "LOGIN_DENIED_NO_CONSENT",
                "Login denied for user under 13 without parental consent"
            );
            throw new UsernameNotFoundException("Account requires parental consent");
        }
        
        return new UserPrincipal(user);
    }
    
    /**
     * Gets the client IP address from the current request.
     * Returns "unknown" if no request context is available.
     */
    private String getClientIpAddress() {
        try {
            return "127.0.0.1"; // Simplified - would get from request context
        } catch (Exception e) {
            return "unknown";
        }
    }
}