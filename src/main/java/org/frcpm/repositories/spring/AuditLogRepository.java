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
 * Repository for audit log entries.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Basic queries
    List<AuditLog> findByUser(User user);
    List<AuditLog> findBySubjectUser(User subjectUser);
    List<AuditLog> findByLevel(AuditLevel level);
    List<AuditLog> findByCoppaRelevantTrue();
    
    // Time-based queries
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentLogs(@Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.coppaRelevant = true AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentCOPPALogs(@Param("since") LocalDateTime since);
    
    // Action-specific queries
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action AND a.timestamp >= :since")
    List<AuditLog> findByActionSince(@Param("action") String action, @Param("since") LocalDateTime since);
    
    // Security monitoring
    @Query("SELECT a FROM AuditLog a WHERE a.level IN :levels AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findSecurityEventsSince(@Param("levels") List<AuditLevel> levels, @Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findByIpAddressSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    // Statistics
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.coppaRelevant = true")
    long countCOPPARelevantLogs();
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.level = :level AND a.timestamp >= :since")
    long countByLevelSince(@Param("level") AuditLevel level, @Param("since") LocalDateTime since);
}