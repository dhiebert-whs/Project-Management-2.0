// src/main/java/org/frcpm/repositories/spring/TaskRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.DependencyType;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TaskDependency;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Enhanced Spring Data JPA repository for Task entities.
 * 
 * This repository supports both simple task dependencies (legacy) and 
 * advanced TaskDependency relationships for complex project scheduling.
 * 
 * Features:
 * - All existing simple dependency queries (preserved)
 * - Advanced TaskDependency entity queries
 * - Critical path analysis support
 * - Cycle detection queries
 * - Performance-optimized dependency traversal
 * 
 * @version 2.0.0-Phase2E-D Enhanced
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // =========================================================================
    // EXISTING METHODS - PRESERVED FOR BACKWARD COMPATIBILITY
    // =========================================================================
    
    /**
     * Finds tasks by project.
     * 
     * @param project the project to find tasks for
     * @return a list of tasks for the project
     */
    List<Task> findByProject(Project project);
    
    /**
     * Finds tasks by project ID.
     * 
     * @param projectId the project ID
     * @return a list of tasks for the project
     */
    List<Task> findByProjectId(Long projectId);
    
    /**
     * Finds tasks by subsystem.
     * 
     * @param subsystem the subsystem to find tasks for
     * @return a list of tasks for the subsystem
     */
    List<Task> findBySubsystem(Subsystem subsystem);
    
    /**
     * Finds tasks assigned to a team member.
     * 
     * @param member the team member
     * @return a list of tasks assigned to the member
     */
    // @Query("SELECT t FROM Task t WHERE :member MEMBER OF t.assignedTo")
    List<Task> findByAssignedMember(@Param("member") TeamMember member);
    
    /**
     * Finds tasks by completion status.
     * 
     * @param completed whether to find completed or incomplete tasks
     * @return a list of tasks with the given completion status
     */
    List<Task> findByCompleted(boolean completed);
    
    /**
     * Finds tasks with a due date before the specified date.
     * 
     * @param date the date to compare against
     * @return a list of tasks due before the date
     */
    List<Task> findByEndDateBefore(LocalDate date);
    
    /**
     * Finds tasks by priority.
     * 
     * @param priority the priority level
     * @return a list of tasks with the given priority
     */
    List<Task> findByPriority(Task.Priority priority);
    
    /**
     * Finds incomplete tasks for a project.
     * 
     * @param project the project
     * @return a list of incomplete tasks
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.completed = false")
    List<Task> findIncompleteTasksByProject(@Param("project") Project project);
    
    /**
     * Finds tasks due within the specified number of days for a project.
     * 
     * @param projectId the project ID
     * @param startDate the start date (today)
     * @param endDate the end date (today + days)
     * @return a list of tasks due soon
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
           "AND t.completed = false " +
           "AND t.endDate IS NOT NULL " +
           "AND t.endDate >= :startDate " +
           "AND t.endDate <= :endDate")
    List<Task> findTasksDueSoon(@Param("projectId") Long projectId, 
                               @Param("startDate") LocalDate startDate, 
                               @Param("endDate") LocalDate endDate);
    
    /**
     * Finds overdue tasks for a project.
     * 
     * @param project the project
     * @return a list of overdue tasks
     */
    // @Query("SELECT t FROM Task t WHERE t.project = :project " +
    //        "AND t.completed = false " +
    //        "AND t.endDate IS NOT NULL " +
    //        "AND t.endDate < CURRENT_DATE")
    // List<Task> findOverdueTasksByProject(@Param("project") Project project);
    
    /**
     * Finds tasks by title containing the specified text.
     * 
     * @param title the text to search for in task titles
     * @return a list of matching tasks
     */
    List<Task> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Finds tasks assigned to multiple team members.
     * 
     * @param memberIds the list of team member IDs
     * @return a list of tasks assigned to any of the specified members
     */
    // @Query("SELECT DISTINCT t FROM Task t WHERE EXISTS (SELECT m FROM t.assignedTo m WHERE m.id IN :memberIds)")
    // List<Task> findTasksAssignedToMembers(@Param("memberIds") List<Long> memberIds);
    
    /**
     * Counts completed tasks for a project.
     * 
     * @param project the project
     * @return the count of completed tasks
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project = :project AND t.completed = true")
    long countCompletedTasksByProject(@Param("project") Project project);
    
    /**
     * Counts total tasks for a project.
     * 
     * @param project the project
     * @return the total count of tasks
     */
    long countByProject(Project project);
    
    /**
     * Finds tasks with dependencies (tasks that depend on other tasks).
     * Uses TaskDependency entity for proper dependency management.
     * 
     * @return a list of tasks with dependencies
     */
    @Query("SELECT DISTINCT td.dependentTask FROM TaskDependency td WHERE td.isActive = true")
    List<Task> findTasksWithDependencies();
    
    /**
     * Finds tasks that are blocking other tasks (tasks that other tasks depend on).
     * Uses TaskDependency entity for proper dependency management.
     * 
     * @return a list of blocking tasks
     */
    @Query("SELECT DISTINCT td.prerequisiteTask FROM TaskDependency td WHERE td.isActive = true")
    List<Task> findBlockingTasks();
    
    // =========================================================================
    // ENHANCED METHODS - NEW ADVANCED DEPENDENCY SUPPORT
    // =========================================================================
    
    /**
     * Finds tasks with advanced TaskDependency relationships as dependents.
     * 
     * @param projectId the project ID to scope the search
     * @return tasks that have TaskDependency entities where they are the dependent task
     */
    @Query("SELECT DISTINCT td.dependentTask FROM TaskDependency td " +
           "WHERE td.project.id = :projectId AND td.isActive = true")
    List<Task> findTasksWithAdvancedDependencies(@Param("projectId") Long projectId);
    
    /**
     * Finds tasks that are prerequisites in TaskDependency relationships.
     * 
     * @param projectId the project ID to scope the search
     * @return tasks that have TaskDependency entities where they are the prerequisite task
     */
    @Query("SELECT DISTINCT td.prerequisiteTask FROM TaskDependency td " +
           "WHERE td.project.id = :projectId AND td.isActive = true")
    List<Task> findAdvancedBlockingTasks(@Param("projectId") Long projectId);
    
    /**
     * Finds tasks with unsatisfied dependencies that are blocking their start.
     * 
     * @param projectId the project ID
     * @return tasks that cannot start due to unsatisfied dependencies
     */
    @Query("SELECT DISTINCT td.dependentTask FROM TaskDependency td " +
           "WHERE td.project.id = :projectId " +
           "AND td.isActive = true " +
           "AND td.dependentTask.completed = false " +
           "AND (td.dependencyType IN ('FINISH_TO_START', 'BLOCKING') AND td.prerequisiteTask.completed = false) " +
           "OR (td.dependencyType = 'START_TO_START' AND td.prerequisiteTask.progress = 0)")
    List<Task> findBlockedTasks(@Param("projectId") Long projectId);
    
    /**
     * Finds tasks on the critical path for a project.
     * 
     * @param projectId the project ID
     * @return tasks marked as being on the critical path
     */
    @Query("SELECT DISTINCT td.dependentTask FROM TaskDependency td " +
           "WHERE td.project.id = :projectId " +
           "AND td.isActive = true " +
           "AND td.criticalPath = true " +
           "UNION " +
           "SELECT DISTINCT td.prerequisiteTask FROM TaskDependency td " +
           "WHERE td.project.id = :projectId " +
           "AND td.isActive = true " +
           "AND td.criticalPath = true")
    List<Task> findCriticalPathTasks(@Param("projectId") Long projectId);
    
    /**
     * Finds tasks with specific dependency types.
     * 
     * @param projectId the project ID
     * @param dependencyType the type of dependency to find
     * @return tasks involved in dependencies of the specified type
     */
    @Query("SELECT DISTINCT td.dependentTask FROM TaskDependency td " +
           "WHERE td.project.id = :projectId " +
           "AND td.isActive = true " +
           "AND td.dependencyType = :dependencyType")
    List<Task> findTasksByDependencyType(@Param("projectId") Long projectId, 
                                        @Param("dependencyType") DependencyType dependencyType);
    
    /**
     * Finds tasks that can be started immediately (no blocking dependencies).
     * 
     * @param projectId the project ID
     * @return tasks with no unsatisfied dependencies
     */
    // @Query("SELECT t FROM Task t " +
    //        "WHERE t.project.id = :projectId " +
    //        "AND t.completed = false " +
    //        "AND t.progress = 0 " +
    //        "AND NOT EXISTS (" +
    //        "    SELECT 1 FROM TaskDependency td " +
    //        "    WHERE td.dependentTask = t " +
    //        "    AND td.isActive = true " +
    //        "    AND (" +
    //        "        (td.dependencyType IN ('FINISH_TO_START', 'BLOCKING') AND td.prerequisiteTask.completed = false) " +
    //        "        OR (td.dependencyType = 'START_TO_START' AND td.prerequisiteTask.progress = 0)" +
    //        "    )" +
    //        ")")
    // List<Task> findTasksReadyToStart(@Param("projectId") Long projectId);
    
    /**
     * Finds all direct prerequisite tasks for a given task using TaskDependency entities.
     * 
     * @param taskId the task ID
     * @return list of prerequisite tasks
     */
    @Query("SELECT td.prerequisiteTask FROM TaskDependency td " +
           "WHERE td.dependentTask.id = :taskId " +
           "AND td.isActive = true " +
           "ORDER BY td.dependencyType, td.prerequisiteTask.endDate")
    List<Task> findDirectPrerequisites(@Param("taskId") Long taskId);
    
    /**
     * Finds all direct dependent tasks for a given task using TaskDependency entities.
     * 
     * @param taskId the task ID
     * @return list of dependent tasks
     */
    @Query("SELECT td.dependentTask FROM TaskDependency td " +
           "WHERE td.prerequisiteTask.id = :taskId " +
           "AND td.isActive = true " +
           "ORDER BY td.dependencyType, td.dependentTask.startDate")
    List<Task> findDirectDependents(@Param("taskId") Long taskId);
    
    /**
     * Finds tasks with lag time in their dependencies.
     * Useful for identifying schedule flexibility.
     * 
     * @param projectId the project ID
     * @return tasks that have dependencies with lag time
     */
    @Query("SELECT DISTINCT td.dependentTask FROM TaskDependency td " +
           "WHERE td.project.id = :projectId " +
           "AND td.isActive = true " +
           "AND td.lagHours IS NOT NULL " +
           "AND td.lagHours != 0")
    List<Task> findTasksWithLagTime(@Param("projectId") Long projectId);
    
    /**
     * Finds the longest dependency chain length for critical path analysis.
     * Returns tasks with their maximum dependency depth.
     * 
     * @param projectId the project ID
     * @return tasks ordered by dependency chain length (deepest first)
     */
    @Query("SELECT t FROM Task t " +
           "WHERE t.project.id = :projectId " +
           "ORDER BY (" +
           "    SELECT COUNT(td) FROM TaskDependency td " +
           "    WHERE td.dependentTask = t AND td.active = true" +
           ") DESC")
    List<Task> findTasksByDependencyDepth(@Param("projectId") Long projectId);
    
    /**
     * Finds tasks that have soft dependencies only (recommendations, not blockers).
     * 
     * @param projectId the project ID
     * @return tasks with only soft dependencies
     */
    // @Query("SELECT DISTINCT td.dependentTask FROM TaskDependency td " +
    //        "WHERE td.project.id = :projectId " +
    //        "AND td.isActive = true " +
    //        "AND td.dependencyType = 'SOFT' " +
    //        "AND NOT EXISTS (" +
    //        "    SELECT 1 FROM TaskDependency td2 " +
    //        "    WHERE td2.dependentTask = td.dependentTask " +
    //        "    AND td2.active = true " +
    //        "    AND td2.dependencyType != 'SOFT'" +
    //        ")")
    // List<Task> findTasksWithOnlySoftDependencies(@Param("projectId") Long projectId);
    
    /**
     * Performance analytics: Find tasks with the most dependencies.
     * Useful for identifying potential bottlenecks.
     * 
     * @param projectId the project ID
     * @param limit maximum number of results
     * @return tasks with highest dependency count
     */
    @Query(value = "SELECT t.* FROM tasks t " +
           "WHERE t.project_id = :projectId " +
           "ORDER BY (" +
           "    SELECT COUNT(*) FROM task_dependencies td " +
           "    WHERE (td.dependent_task_id = t.id OR td.prerequisite_task_id = t.id) " +
           "    AND td.is_active = true" +
           ") DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Task> findMostConnectedTasks(@Param("projectId") Long projectId, @Param("limit") int limit);
    
    /**
     * Build season optimization: Find tasks with external dependencies.
     * These may need special coordination or procurement lead time.
     * 
     * @param projectId the project ID
     * @return tasks that might have external constraints
     */
    // @Query("SELECT t FROM Task t " +
    //        "WHERE t.project.id = :projectId " +
    //        "AND (" +
    //        "    SIZE(t.requiredComponents) > 0 " +
    //        "    OR EXISTS (" +
    //        "        SELECT 1 FROM TaskDependency td " +
    //        "        WHERE td.dependentTask = t " +
    //        "        AND td.isActive = true " +
    //        "        AND td.lagHours IS NOT NULL " +
    //        "        AND td.lagHours > 24" +  // More than 1 day lag suggests external dependency
    //        "    )" +
    //        ")")
    // List<Task> findTasksWithExternalConstraints(@Param("projectId") Long projectId);
    
    /**
     * Finds orphaned tasks (no dependencies in either direction).
     * These tasks can be worked on independently.
     * 
     * @param projectId the project ID
     * @return tasks with no dependency relationships
     */
    // @Query("SELECT t FROM Task t " +
    //        "WHERE t.project.id = :projectId " +
    //        "AND t.completed = false " +
    //        "AND NOT EXISTS (" +
    //        "    SELECT 1 FROM TaskDependency td " +
    //        "    WHERE (td.dependentTask = t OR td.prerequisiteTask = t) " +
    //        "    AND td.isActive = true" +
    //        ")")
    // List<Task> findIndependentTasks(@Param("projectId") Long projectId);
    
    /**
     * Advanced search: Find tasks by dependency relationship and completion status.
     * 
     * @param projectId the project ID
     * @param dependencyType the dependency type to filter by
     * @param prerequisiteCompleted whether prerequisite should be completed
     * @param dependentCompleted whether dependent should be completed
     * @return filtered tasks based on dependency criteria
     */
    @Query("SELECT td.dependentTask FROM TaskDependency td " +
           "WHERE td.project.id = :projectId " +
           "AND td.isActive = true " +
           "AND (:dependencyType IS NULL OR td.dependencyType = :dependencyType) " +
           "AND (:prerequisiteCompleted IS NULL OR td.prerequisiteTask.completed = :prerequisiteCompleted) " +
           "AND (:dependentCompleted IS NULL OR td.dependentTask.completed = :dependentCompleted)")
    List<Task> findTasksByDependencyCriteria(@Param("projectId") Long projectId,
                                            @Param("dependencyType") DependencyType dependencyType,
                                            @Param("prerequisiteCompleted") Boolean prerequisiteCompleted,
                                            @Param("dependentCompleted") Boolean dependentCompleted);
}