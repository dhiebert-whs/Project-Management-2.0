// src/main/java/org/frcpm/repositories/spring/ManufacturingStepRepository.java
// Phase 4A.3: Manufacturing Workflow Tracking

package org.frcpm.repositories.spring;

import org.frcpm.models.ManufacturingProcess;
import org.frcpm.models.ManufacturingStep;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ManufacturingStep entities.
 * Provides step-level workflow tracking within manufacturing processes.
 * 
 * @author FRC Project Management Team
 * @version 4.0.0-4A.3
 * @since Phase 4A.3 - Manufacturing Workflow Tracking
 */
@Repository
public interface ManufacturingStepRepository extends JpaRepository<ManufacturingStep, Long> {
    
    // Process-based Queries
    
    /**
     * Finds all steps for a manufacturing process in sequence order.
     */
    List<ManufacturingStep> findByManufacturingProcessAndIsActiveTrueOrderBySequenceNumberAsc(
            ManufacturingProcess manufacturingProcess);
    
    /**
     * Finds all steps for a manufacturing process including inactive ones.
     */
    List<ManufacturingStep> findByManufacturingProcessOrderBySequenceNumberAsc(
            ManufacturingProcess manufacturingProcess);
    
    /**
     * Finds a specific step by process and sequence number.
     */
    Optional<ManufacturingStep> findByManufacturingProcessAndSequenceNumberAndIsActiveTrue(
            ManufacturingProcess manufacturingProcess, Integer sequenceNumber);
    
    /**
     * Counts total steps for a manufacturing process.
     */
    long countByManufacturingProcessAndIsActiveTrue(ManufacturingProcess manufacturingProcess);
    
    // Status-based Queries
    
    /**
     * Finds steps by status.
     */
    List<ManufacturingStep> findByStatusAndIsActiveTrueOrderByStartedAtAsc(
            ManufacturingStep.StepStatus status);
    
    /**
     * Finds steps by multiple statuses.
     */
    List<ManufacturingStep> findByStatusInAndIsActiveTrueOrderByStartedAtAsc(
            List<ManufacturingStep.StepStatus> statuses);
    
    /**
     * Finds steps for a process by status.
     */
    List<ManufacturingStep> findByManufacturingProcessAndStatusAndIsActiveTrueOrderBySequenceNumberAsc(
            ManufacturingProcess manufacturingProcess, ManufacturingStep.StepStatus status);
    
    /**
     * Finds steps for a process by multiple statuses.
     */
    List<ManufacturingStep> findByManufacturingProcessAndStatusInAndIsActiveTrueOrderBySequenceNumberAsc(
            ManufacturingProcess manufacturingProcess, List<ManufacturingStep.StepStatus> statuses);
    
    // Team Member Assignment Queries
    
    /**
     * Finds steps performed by a team member.
     */
    List<ManufacturingStep> findByPerformedByAndIsActiveTrueOrderByStartedAtDesc(TeamMember performedBy);
    
    /**
     * Finds steps performed by a team member with specific status.
     */
    List<ManufacturingStep> findByPerformedByAndStatusAndIsActiveTrueOrderByStartedAtAsc(
            TeamMember performedBy, ManufacturingStep.StepStatus status);
    
    /**
     * Finds steps verified by a team member.
     */
    List<ManufacturingStep> findByVerifiedByAndIsActiveTrueOrderByVerifiedAtDesc(TeamMember verifiedBy);
    
    /**
     * Finds active steps assigned to a team member.
     */
    List<ManufacturingStep> findByPerformedByAndStatusInAndIsActiveTrueOrderByStartedAtAsc(
            TeamMember performedBy, List<ManufacturingStep.StepStatus> activeStatuses);
    
    // Sequence and Workflow Queries
    
    /**
     * Finds the next step in sequence for a process.
     */
    Optional<ManufacturingStep> findFirstByManufacturingProcessAndSequenceNumberGreaterThanAndIsActiveTrueOrderBySequenceNumberAsc(
            ManufacturingProcess manufacturingProcess, Integer currentSequence);
    
    /**
     * Finds the previous step in sequence for a process.
     */
    Optional<ManufacturingStep> findFirstByManufacturingProcessAndSequenceNumberLessThanAndIsActiveTrueOrderBySequenceNumberDesc(
            ManufacturingProcess manufacturingProcess, Integer currentSequence);
    
    /**
     * Finds all steps before a given sequence number.
     */
    List<ManufacturingStep> findByManufacturingProcessAndSequenceNumberLessThanAndIsActiveTrueOrderBySequenceNumberAsc(
            ManufacturingProcess manufacturingProcess, Integer sequenceNumber);
    
    /**
     * Finds all steps after a given sequence number.
     */
    List<ManufacturingStep> findByManufacturingProcessAndSequenceNumberGreaterThanAndIsActiveTrueOrderBySequenceNumberAsc(
            ManufacturingProcess manufacturingProcess, Integer sequenceNumber);
    
    // Time-based Queries
    
    /**
     * Finds steps started within a date range.
     */
    List<ManufacturingStep> findByStartedAtBetweenAndIsActiveTrueOrderByStartedAtAsc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds steps completed within a date range.
     */
    List<ManufacturingStep> findByCompletedAtBetweenAndIsActiveTrueOrderByCompletedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Finds steps verified within a date range.
     */
    List<ManufacturingStep> findByVerifiedAtBetweenAndIsActiveTrueOrderByVerifiedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    // Verification Queries
    
    /**
     * Finds steps requiring verification.
     */
    List<ManufacturingStep> findByRequiresVerificationTrueAndIsActiveTrueOrderByCompletedAtAsc();
    
    /**
     * Finds unverified steps that require verification.
     */
    List<ManufacturingStep> findByRequiresVerificationTrueAndIsVerifiedFalseAndStatusAndIsActiveTrueOrderByCompletedAtAsc(
            ManufacturingStep.StepStatus completedStatus);
    
    /**
     * Finds verified steps.
     */
    List<ManufacturingStep> findByIsVerifiedTrueAndIsActiveTrueOrderByVerifiedAtDesc();
    
    /**
     * Finds steps verified by a specific team member.
     */
    List<ManufacturingStep> findByVerifiedByAndIsVerifiedTrueAndIsActiveTrueOrderByVerifiedAtDesc(
            TeamMember verifiedBy);
    
    // Duration and Performance Queries
    
    /**
     * Finds steps with estimated duration.
     */
    List<ManufacturingStep> findByEstimatedMinutesIsNotNullAndIsActiveTrueOrderByEstimatedMinutesDesc();
    
    /**
     * Finds steps with actual duration.
     */
    List<ManufacturingStep> findByActualMinutesIsNotNullAndIsActiveTrueOrderByActualMinutesDesc();
    
    /**
     * Finds steps that took longer than estimated.
     */
    @Query("SELECT ms FROM ManufacturingStep ms WHERE ms.isActive = true " +
           "AND ms.estimatedMinutes IS NOT NULL AND ms.actualMinutes IS NOT NULL " +
           "AND ms.actualMinutes > ms.estimatedMinutes " +
           "ORDER BY (ms.actualMinutes - ms.estimatedMinutes) DESC")
    List<ManufacturingStep> findStepsOverEstimate();
    
    /**
     * Finds steps that were completed faster than estimated.
     */
    @Query("SELECT ms FROM ManufacturingStep ms WHERE ms.isActive = true " +
           "AND ms.estimatedMinutes IS NOT NULL AND ms.actualMinutes IS NOT NULL " +
           "AND ms.actualMinutes < ms.estimatedMinutes " +
           "ORDER BY (ms.estimatedMinutes - ms.actualMinutes) DESC")
    List<ManufacturingStep> findStepsUnderEstimate();
    
    // Search and Filter Queries
    
    /**
     * Finds steps by name containing search term.
     */
    List<ManufacturingStep> findByNameContainingIgnoreCaseAndIsActiveTrueOrderBySequenceNumberAsc(String searchTerm);
    
    /**
     * Finds steps by instructions containing search term.
     */
    List<ManufacturingStep> findByInstructionsContainingIgnoreCaseAndIsActiveTrueOrderBySequenceNumberAsc(String searchTerm);
    
    /**
     * Finds steps by notes containing search term.
     */
    List<ManufacturingStep> findByNotesContainingIgnoreCaseAndIsActiveTrueOrderByStartedAtDesc(String searchTerm);
    
    // Statistical Queries
    
    /**
     * Counts steps by status for a manufacturing process.
     */
    long countByManufacturingProcessAndStatusAndIsActiveTrue(
            ManufacturingProcess manufacturingProcess, ManufacturingStep.StepStatus status);
    
    /**
     * Counts verified steps for a manufacturing process.
     */
    long countByManufacturingProcessAndIsVerifiedTrueAndIsActiveTrue(ManufacturingProcess manufacturingProcess);
    
    /**
     * Counts steps requiring verification for a manufacturing process.
     */
    long countByManufacturingProcessAndRequiresVerificationTrueAndIsActiveTrue(
            ManufacturingProcess manufacturingProcess);
    
    // Workflow State Queries
    
    /**
     * Finds steps that can be started (pending status).
     */
    List<ManufacturingStep> findByStatusAndIsActiveTrueOrderBySequenceNumberAsc(
            ManufacturingStep.StepStatus pendingStatus);
    
    /**
     * Finds blocked steps.
     */
    List<ManufacturingStep> findByStatusAndIsActiveTrueOrderByStartedAtAscForBlocked(
            ManufacturingStep.StepStatus blockedStatus);
    
    /**
     * Finds failed steps.
     */
    List<ManufacturingStep> findByStatusAndIsActiveTrueOrderByStartedAtDesc(
            ManufacturingStep.StepStatus failedStatus);
    
    // Complex Workflow Queries
    
    /**
     * Finds the current step (in progress) for a manufacturing process.
     */
    Optional<ManufacturingStep> findByManufacturingProcessAndStatusAndIsActiveTrue(
            ManufacturingProcess manufacturingProcess, ManufacturingStep.StepStatus inProgressStatus);
    
    /**
     * Finds the next available step for a manufacturing process.
     */
    @Query("SELECT ms FROM ManufacturingStep ms WHERE ms.manufacturingProcess = :process " +
           "AND ms.status = 'PENDING' AND ms.isActive = true " +
           "AND NOT EXISTS (SELECT ms2 FROM ManufacturingStep ms2 " +
           "                WHERE ms2.manufacturingProcess = :process " +
           "                AND ms2.sequenceNumber < ms.sequenceNumber " +
           "                AND ms2.status NOT IN ('COMPLETED', 'SKIPPED') " +
           "                AND ms2.isActive = true) " +
           "ORDER BY ms.sequenceNumber ASC")
    Optional<ManufacturingStep> findNextAvailableStep(@Param("process") ManufacturingProcess process);
    
    /**
     * Finds steps blocking process completion.
     */
    @Query("SELECT ms FROM ManufacturingStep ms WHERE ms.manufacturingProcess = :process " +
           "AND ms.status IN ('PENDING', 'IN_PROGRESS', 'BLOCKED', 'FAILED') " +
           "AND ms.isActive = true " +
           "ORDER BY ms.sequenceNumber ASC")
    List<ManufacturingStep> findBlockingSteps(@Param("process") ManufacturingProcess process);
    
    /**
     * Finds manufacturing step metrics for a process.
     */
    @Query("SELECT " +
           "COUNT(ms) as totalSteps, " +
           "SUM(CASE WHEN ms.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedSteps, " +
           "SUM(CASE WHEN ms.status = 'FAILED' THEN 1 ELSE 0 END) as failedSteps, " +
           "SUM(CASE WHEN ms.requiresVerification = true THEN 1 ELSE 0 END) as stepsRequiringVerification, " +
           "SUM(CASE WHEN ms.isVerified = true THEN 1 ELSE 0 END) as verifiedSteps, " +
           "AVG(ms.estimatedMinutes) as avgEstimatedMinutes, " +
           "AVG(ms.actualMinutes) as avgActualMinutes " +
           "FROM ManufacturingStep ms " +
           "WHERE ms.manufacturingProcess = :process AND ms.isActive = true")
    Object[] findProcessStepMetrics(@Param("process") ManufacturingProcess process);
    
    /**
     * Finds performance analysis for a team member.
     */
    @Query("SELECT " +
           "COUNT(ms) as totalStepsPerformed, " +
           "SUM(CASE WHEN ms.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedSteps, " +
           "AVG(ms.actualMinutes) as avgCompletionTime, " +
           "SUM(CASE WHEN ms.actualMinutes > ms.estimatedMinutes THEN 1 ELSE 0 END) as stepsOverEstimate " +
           "FROM ManufacturingStep ms " +
           "WHERE ms.performedBy = :teamMember AND ms.isActive = true " +
           "AND ms.actualMinutes IS NOT NULL")
    Object[] findTeamMemberStepPerformance(@Param("teamMember") TeamMember teamMember);
    
    // Active Status Management
    
    /**
     * Finds all inactive steps.
     */
    List<ManufacturingStep> findByIsActiveFalseOrderBySequenceNumberAsc();
    
    /**
     * Checks if any steps exist for a manufacturing process.
     */
    boolean existsByManufacturingProcessAndIsActiveTrue(ManufacturingProcess manufacturingProcess);
    
    /**
     * Finds the maximum sequence number for a manufacturing process.
     */
    @Query("SELECT MAX(ms.sequenceNumber) FROM ManufacturingStep ms " +
           "WHERE ms.manufacturingProcess = :process AND ms.isActive = true")
    Optional<Integer> findMaxSequenceNumber(@Param("process") ManufacturingProcess process);
}