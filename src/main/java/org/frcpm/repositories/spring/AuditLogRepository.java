// src/main/java/org/frcpm/repositories/spring/AuditLogRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.AuditLog;
import org.frcpm.models.AuditLevel;
import org.frcpm.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ✅ UPDATED: Repository for audit log entries with enhanced COPPA compliance support.
 * 
 * Enhanced for Phase 2B with:
 * - COPPA-specific audit queries
 * - Advanced security monitoring
 * - Compliance reporting capabilities
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // =========================================================================
    // BASIC AUDIT QUERIES (EXISTING - KEEP THESE)
    // =========================================================================
    
    List<AuditLog> findByUser(User user);
    List<AuditLog> findBySubjectUser(User subjectUser);
    List<AuditLog> findByLevel(AuditLevel level);
    List<AuditLog> findByCoppaRelevantTrue();
    
    // =========================================================================
    // TIME-BASED QUERIES (EXISTING - KEEP THESE)
    // =========================================================================
    
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentLogs(@Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.coppaRelevant = true AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentCOPPALogs(@Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action AND a.timestamp >= :since")
    List<AuditLog> findByActionSince(@Param("action") String action, @Param("since") LocalDateTime since);
    
    // =========================================================================
    // SECURITY MONITORING (EXISTING - KEEP THESE)
    // =========================================================================
    
    @Query("SELECT a FROM AuditLog a WHERE a.level IN :levels AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findSecurityEventsSince(@Param("levels") List<AuditLevel> levels, @Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findByIpAddressSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    // =========================================================================
    // STATISTICS (EXISTING + NEW)
    // =========================================================================
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.coppaRelevant = true")
    long countCOPPARelevantLogs();
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.level = :level AND a.timestamp >= :since")
    long countByLevelSince(@Param("level") AuditLevel level, @Param("since") LocalDateTime since);
    
    // ✅ NEW: Additional statistics methods needed by AuditServiceImpl
    
    /**
     * Count all COPPA-relevant logs (used by getCOPPALogCount method).
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.coppaRelevant = true")
    long getCOPPALogCount();
    
    /**
     * Count security events by multiple levels.
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.level IN :levels AND a.timestamp >= :since")
    long countSecurityEventsSince(@Param("levels") List<AuditLevel> levels, @Param("since") LocalDateTime since);
    
    // =========================================================================
    // ✅ NEW: ENHANCED COPPA COMPLIANCE QUERIES
    // =========================================================================
    
    /**
     * Find all audit logs related to a specific minor user.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.subjectUser = :user AND a.coppaRelevant = true ORDER BY a.timestamp DESC")
    List<AuditLog> findCOPPALogsBySubjectUser(@Param("user") User user);
    
    /**
     * Find users who have accessed data of minors (for compliance review).
     */
    @Query("SELECT DISTINCT a.user FROM AuditLog a WHERE a.coppaRelevant = true AND a.timestamp >= :since")
    List<User> findUsersAccessingMinorData(@Param("since") LocalDateTime since);
    
    // Note: findDataAccessEvents query removed - LIKE validation issues in H2
    
    /**
     * Count COPPA violations or alerts.
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.coppaRelevant = true AND a.level IN ('SECURITY_ALERT', 'VIOLATION') AND a.timestamp >= :since")
    long countCOPPAViolationsSince(@Param("since") LocalDateTime since);
    
    // =========================================================================
    // ✅ NEW: RESOURCE-SPECIFIC AUDIT QUERIES
    // =========================================================================
    
    /**
     * Find audit logs for a specific resource.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.resourceType = :resourceType AND a.resourceId = :resourceId ORDER BY a.timestamp DESC")
    List<AuditLog> findByResource(@Param("resourceType") String resourceType, @Param("resourceId") Long resourceId);
    
    /**
     * Find audit logs by resource type.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.resourceType = :resourceType AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findByResourceType(@Param("resourceType") String resourceType, @Param("since") LocalDateTime since);
    
    // =========================================================================
    // ✅ NEW: ADVANCED SECURITY MONITORING
    // =========================================================================
    
    /**
     * Find failed login attempts from a specific IP.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = 'LOGIN_FAILED' AND a.ipAddress = :ipAddress AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedLoginsByIp(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    /**
     * Find suspicious activity patterns (multiple failed attempts).
     */
    @Query("SELECT a.ipAddress, COUNT(a) as attemptCount FROM AuditLog a WHERE a.action = 'LOGIN_FAILED' AND a.timestamp >= :since GROUP BY a.ipAddress HAVING COUNT(a) >= :threshold")
    List<Object[]> findSuspiciousLoginActivity(@Param("since") LocalDateTime since, @Param("threshold") long threshold);
    
    /**
     * Find all actions performed by a user in a time period.
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user = :user AND a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    List<AuditLog> findUserActivityInPeriod(@Param("user") User user, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // =========================================================================
    // ✅ NEW: COMPLIANCE REPORTING QUERIES
    // =========================================================================
    
    /**
     * Generate daily statistics for compliance reporting.
     */
    @Query("SELECT DATE(a.timestamp) as logDate, COUNT(a) as totalEvents, " +
           "SUM(CASE WHEN a.coppaRelevant = true THEN 1 ELSE 0 END) as coppaEvents, " +
           "SUM(CASE WHEN a.level IN ('SECURITY_ALERT', 'VIOLATION') THEN 1 ELSE 0 END) as securityEvents " +
           "FROM AuditLog a WHERE a.timestamp >= :since GROUP BY DATE(a.timestamp) ORDER BY logDate DESC")
    List<Object[]> getDailyAuditStatistics(@Param("since") LocalDateTime since);
    
    /**
     * Find the most accessed resources for compliance review.
     */
    @Query("SELECT a.resourceType, a.resourceId, COUNT(a) as accessCount " +
           "FROM AuditLog a WHERE a.resourceType IS NOT NULL AND a.timestamp >= :since " +
           "GROUP BY a.resourceType, a.resourceId ORDER BY accessCount DESC")
    List<Object[]> getMostAccessedResources(@Param("since") LocalDateTime since);
}