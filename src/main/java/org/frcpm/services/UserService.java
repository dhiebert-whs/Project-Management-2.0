// src/main/java/org/frcpm/services/UserService.java

package org.frcpm.services;

import org.frcpm.models.User;
import org.frcpm.models.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for User management and authentication.
 */
public interface UserService extends Service<User, Long> {
    
    // Authentication methods
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    // User creation and management
    User createUser(String username, String password, String email, 
                   String firstName, String lastName, UserRole role, Integer age);
    User createUser(String username, String password, String email, 
                   String firstName, String lastName, UserRole role, Integer age, String parentEmail);
    
    // Role management
    List<User> findByRole(UserRole role);
    List<User> findActiveUsersByRole(UserRole role);
    User updateUserRole(Long userId, UserRole newRole);
    
    // Account management
    User enableUser(Long userId);
    User disableUser(Long userId);
    User updatePassword(Long userId, String newPassword);
    User updateContactInfo(Long userId, String email, String firstName, String lastName);
    
    // COPPA compliance
    List<User> findUsersRequiringParentalConsent();
    boolean initiateParentalConsent(Long userId, String parentEmail);
    boolean processParentalConsent(String consentToken, boolean granted);
    
    // MFA management
    User enableMFA(Long userId, String totpSecret);
    User disableMFA(Long userId);
    List<User> findUsersRequiringMFA();
    
    // Statistics and reporting
    long countByRole(UserRole role);
    long countMinorsUnder13();
    long countEnabledUsers();
    List<User> findRecentlyActiveUsers(int days);
}