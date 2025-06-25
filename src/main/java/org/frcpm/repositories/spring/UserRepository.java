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
 * Spring Data JPA repository for User entities.
 * Provides comprehensive user management and COPPA compliance queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Authentication queries
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    // Role-based queries
    List<User> findByRole(UserRole role);
    List<User> findByEnabledTrue();
    List<User> findByEnabledFalse();
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = true")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);
    
    // COPPA compliance queries
    @Query("SELECT u FROM User u WHERE u.age < 13 AND u.parentalConsentDate IS NULL AND u.requiresParentalConsent = true")
    List<User> findUsersRequiringParentalConsent();
    
    @Query("SELECT u FROM User u WHERE u.parentalConsentToken = :token")
    Optional<User> findByParentalConsentToken(@Param("token") String token);
    
    @Query("SELECT u FROM User u WHERE u.age < 13")
    List<User> findMinorsUnder13();
    
    @Query("SELECT u FROM User u WHERE u.age < 18")
    List<User> findAllMinors();
    
    // MFA queries
    List<User> findByMfaEnabledTrue();
    List<User> findByMfaEnabledFalse();
    
    @Query("SELECT u FROM User u WHERE u.role IN :roles AND u.mfaEnabled = false")
    List<User> findUsersRequiringMFA(@Param("roles") List<UserRole> roles);
    
    // Activity queries
    @Query("SELECT u FROM User u WHERE u.lastLogin > :since")
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);
    
    @Query("SELECT u FROM User u WHERE u.createdAt > :since")
    List<User> findRecentlyCreatedUsers(@Param("since") LocalDateTime since);
    
    // Utility queries
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.age < 13")
    long countMinorsUnder13();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countEnabledUsers();
}