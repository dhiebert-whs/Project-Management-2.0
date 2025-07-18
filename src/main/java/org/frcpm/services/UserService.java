// src/main/java/org/frcpm/services/UserService.java

package org.frcpm.services;

import org.frcpm.models.User;
import org.frcpm.models.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * ✅ UPDATED: User service interface for authentication and user management with COPPA compliance.
 * 
 * Enhanced for Phase 2B with:
 * - COPPA compliance methods for users under 13
 * - Multi-factor authentication support
 * - Role-based user management
 * - Comprehensive audit and reporting capabilities
 * 
 * Note: This interface extends Service<User, Long> which defines findById(Long) returning User.
 * For Optional<User> return, use findByIdOptional() method.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
public interface UserService extends Service<User, Long> {
    
    // Note: findById(Long) is inherited from Service<User, Long> and returns User
    // Additional method for Optional<User> return is available as findByIdOptional(Long)
    
    // =========================================================================
    // AUTHENTICATION METHODS
    // =========================================================================
    
    /**
     * Find user by username.
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email address.
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by username or email (for login flexibility).
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    // =========================================================================
    // USER CREATION AND MANAGEMENT
    // =========================================================================
    
    /**
     * Create a new user with basic information.
     */
    User createUser(String username, String password, String email, 
                   String firstName, String lastName, UserRole role, Integer age);
    
    /**
     * Create a new user with COPPA compliance support (parent email for minors).
     */
    User createUser(String username, String password, String email, 
                   String firstName, String lastName, UserRole role, 
                   Integer age, String parentEmail);
    
    /**
     * Update user password with validation.
     */
    User updatePassword(Long userId, String newPassword);
    
    /**
     * Update user contact information.
     */
    User updateContactInfo(Long userId, String email, String firstName, String lastName);
    
    // =========================================================================
    // ROLE MANAGEMENT
    // =========================================================================
    
    /**
     * Find users by role.
     */
    List<User> findByRole(UserRole role);
    
    /**
     * Find active users by role (enabled users only).
     */
    List<User> findActiveUsersByRole(UserRole role);
    
    /**
     * Update user role with proper MFA handling.
     */
    User updateUserRole(Long userId, UserRole newRole);
    
    // =========================================================================
    // ACCOUNT MANAGEMENT
    // =========================================================================
    
    /**
     * Enable user account with COPPA compliance check.
     */
    User enableUser(Long userId);
    
    /**
     * Disable user account.
     */
    void disableUser(Long userId);
    
    // =========================================================================
    // ✅ NEW: COPPA COMPLIANCE METHODS
    // =========================================================================
    
    /**
     * Find all users requiring parental consent.
     */
    List<User> findUsersRequiringParentalConsent();
    
    /**
     * Initiate parental consent process for a user under 13.
     */
    boolean initiateParentalConsent(Long userId, String parentEmail);
    
    /**
     * Process parental consent response (grant or deny).
     */
    boolean processParentalConsent(String consentToken, boolean granted);
    
    // =========================================================================
    // ✅ NEW: MULTI-FACTOR AUTHENTICATION
    // =========================================================================
    
    /**
     * Enable MFA for a user with TOTP secret.
     */
    User enableMFA(Long userId, String totpSecret);
    
    /**
     * Disable MFA for a user (if role allows).
     */
    User disableMFA(Long userId);
    
    /**
     * Find users requiring MFA setup.
     */
    List<User> findUsersRequiringMFA();
    
    // =========================================================================
    // ✅ NEW: STATISTICS AND REPORTING
    // =========================================================================
    
    /**
     * Count users by role.
     */
    long countByRole(UserRole role);
    
    /**
     * Count users under 13 for COPPA compliance reporting.
     */
    long countMinorsUnder13();
    
    /**
     * Count enabled users.
     */
    long countEnabledUsers();
    
    /**
     * Find recently active users.
     */
    List<User> findRecentlyActiveUsers(int days);
    
    // =========================================================================
    // PHASE 2E-E: ENHANCED REAL-TIME FEATURES
    // =========================================================================
    
    /**
     * Find all active users (enabled users who have logged in recently).
     */
    List<User> findActiveUsers();
    
    /**
     * Find users by project (project members).
     */
    List<User> findByProject(org.frcpm.models.Project project);
}