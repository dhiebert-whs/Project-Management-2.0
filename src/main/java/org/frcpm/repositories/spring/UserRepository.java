// src/main/java/org/frcpm/repositories/spring/UserRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.User;
import org.frcpm.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ✅ UPDATED: Repository for User entity with comprehensive COPPA compliance support.
 * 
 * Added Methods for Phase 2B:
 * - COPPA compliance queries
 * - Parental consent token management
 * - Age-based user statistics
 * - Role-based queries for security
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // =========================================================================
    // BASIC USER QUERIES (EXISTING - KEEP THESE)
    // =========================================================================
    
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByEnabledTrue();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // =========================================================================
    // ✅ NEW: COPPA COMPLIANCE QUERIES
    // =========================================================================
    
    /**
     * Find user by parental consent token for COPPA compliance.
     */
    Optional<User> findByParentalConsentToken(String token);
    
    /**
     * Find all users requiring parental consent (under 13 without consent).
     */
    List<User> findByRequiresParentalConsentTrue();
    
    /**
     * Count users under a specific age for COPPA statistics.
     */
    long countByAgeLessThan(Integer age);
    
    /**
     * Find users requiring consent that were created before a certain date.
     * Used for identifying accounts that need to be disabled after 7 days.
     */
    @Query("SELECT u FROM User u WHERE u.requiresParentalConsent = true AND u.createdAt < :cutoffDate")
    List<User> findUsersRequiringConsentCreatedBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // =========================================================================
    // ✅ NEW: ROLE-BASED SECURITY QUERIES
    // =========================================================================
    
    /**
     * Find active users by role (enabled users only).
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = true")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);
    
    /**
     * Find users by multiple roles (for MFA requirements).
     */
    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    List<User> findByRoleIn(@Param("roles") List<UserRole> roles);
    
    /**
     * Find users requiring MFA setup (mentors/admins without MFA enabled).
     */
    @Query("SELECT u FROM User u WHERE u.role IN :roles AND u.mfaEnabled = false")
    List<User> findUsersRequiringMFA(@Param("roles") List<UserRole> roles);
    
    // =========================================================================
    // ✅ NEW: AUDIT AND MONITORING QUERIES
    // =========================================================================
    
    /**
     * Find recently active users based on last login.
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :since AND u.enabled = true ORDER BY u.lastLogin DESC")
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);
    
    /**
     * Count enabled users for system statistics.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countEnabledUsers();
    
    /**
     * Count users by role for reporting.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);
    
    /**
     * Find users with expired credentials requiring password reset.
     */
    @Query("SELECT u FROM User u WHERE u.credentialsNonExpired = false AND u.enabled = true")
    List<User> findUsersWithExpiredCredentials();
    
    /**
     * Find locked accounts that may need admin intervention.
     */
    @Query("SELECT u FROM User u WHERE u.accountNonLocked = false")
    List<User> findLockedAccounts();
    
    // =========================================================================
    // ✅ NEW: COPPA-SPECIFIC COMPLIANCE QUERIES
    // =========================================================================
    
    /**
     * Find all users under 13 for COPPA monitoring.
     */
    @Query("SELECT u FROM User u WHERE u.age < 13")
    List<User> findMinorUsers();
    
    /**
     * Find users under 13 with valid parental consent.
     */
    @Query("SELECT u FROM User u WHERE u.age < 13 AND u.parentalConsentDate IS NOT NULL AND u.requiresParentalConsent = false")
    List<User> findMinorsWithValidConsent();
    
    /**
     * Find users under 13 without parental consent (for compliance review).
     */
    @Query("SELECT u FROM User u WHERE u.age < 13 AND (u.parentalConsentDate IS NULL OR u.requiresParentalConsent = true)")
    List<User> findMinorsWithoutConsent();
    
    /**
     * Find users with pending consent tokens that may have expired.
     */
    @Query("SELECT u FROM User u WHERE u.parentalConsentToken IS NOT NULL AND u.createdAt < :expiryDate")
    List<User> findUsersWithExpiredConsentTokens(@Param("expiryDate") LocalDateTime expiryDate);
    
    // =========================================================================
    // ✅ NEW: TEAM MANAGEMENT QUERIES
    // =========================================================================
    
    /**
     * Find users by role and enabled status for team management.
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = :enabled ORDER BY u.lastName, u.firstName")
    List<User> findByRoleAndEnabled(@Param("role") UserRole role, @Param("enabled") boolean enabled);
    
    /**
     * Find users created within a specific time period.
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findUsersCreatedSince(@Param("since") LocalDateTime since);
    
    /**
     * Search users by name for autocomplete features.
     */
    @Query("SELECT u FROM User u WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND u.enabled = true")
    List<User> searchUsersByName(@Param("searchTerm") String searchTerm);
}