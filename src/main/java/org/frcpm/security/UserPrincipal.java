// src/main/java/org/frcpm/security/UserPrincipal.java

package org.frcpm.security;

import org.frcpm.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security UserDetails implementation for FRC Project Management users.
 * 
 * Provides integration between our User entity and Spring Security's
 * authentication framework, including support for COPPA compliance
 * and multi-factor authentication.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
public class UserPrincipal implements UserDetails {
    
    private final User user;
    
    public UserPrincipal(User user) {
        this.user = user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(user.getRole());
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return user.isAccountNonExpired();
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return user.isCredentialsNonExpired();
    }
    
    @Override
    public boolean isEnabled() {
        // Account is enabled if user is enabled AND has proper consent
        return user.isEnabled() && user.hasParentalConsent();
    }
    
    // Additional methods for FRC-specific functionality
    
    public User getUser() {
        return user;
    }
    
    public String getFullName() {
        return user.getFullName();
    }
    
    public boolean requiresMFA() {
        return user.getRole().requiresMFA();
    }
    
    public boolean isMFAEnabled() {
        return user.isMfaEnabled();
    }
    
    public boolean requiresCOPPACompliance() {
        return user.requiresCOPPACompliance();
    }
    
    public boolean hasParentalConsent() {
        return user.hasParentalConsent();
    }
    
    public boolean canManageTeam() {
        return user.getRole().canManageTeam();
    }
    
    public boolean canManageProjects() {
        return user.getRole().canManageProjects();
    }
    
    public boolean isAdmin() {
        return user.getRole().canAdminister();
    }
    
    public boolean isMentor() {
        return user.getRole().isMentor();
    }
    
    public boolean isStudent() {
        return user.getRole().isStudent();
    }
    
    public boolean isParent() {
        return user.getRole().isViewOnly();
    }
    
    /**
     * Gets the appropriate session timeout for this user.
     * 
     * @return session timeout in seconds
     */
    public int getSessionTimeout() {
        return user.getSessionTimeoutSeconds();
    }
    
    /**
     * Determines if this user can access data of another user.
     * 
     * @param targetUser the user whose data is being accessed
     * @return true if access is allowed
     */
    public boolean canAccessUserData(User targetUser) {
        if (targetUser == null) {
            return false;
        }
        
        // Users can access their own data
        if (user.getId().equals(targetUser.getId())) {
            return true;
        }
        
        // Admins can access all data
        if (user.getRole().canAdminister()) {
            return true;
        }
        
        // Mentors can access student data (with COPPA compliance)
        if (user.getRole().isMentor()) {
            // For COPPA-protected users, ensure consent is valid
            if (targetUser.requiresCOPPACompliance()) {
                return targetUser.hasParentalConsent();
            }
            return true;
        }
        
        // Parents can only access their own child's data
        if (user.getRole().isViewOnly()) {
            // TODO: Implement parent-child relationship checking
            return false; // Simplified for now
        }
        
        // Students cannot access other users' data
        return false;
    }
    
    @Override
    public String toString() {
        return user.getFullName() + " (" + user.getUsername() + ", " + user.getRole() + ")";
    }
}