// src/main/java/org/frcpm/repositories/spring/TaskDependencyRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.DependencyType;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for TaskDependency entities.
 * 
 * Provides comprehensive CRUD operations and specialized queries for 
 * advanced task dependency management, critical path analysis, and 
 * build season project scheduling.
 * 
 * Features:
 * - Basic CRUD operations with validation
 * - Cycle detection and prevention queries
 * - Critical path analysis support
 * - Bulk operations for efficiency
 * - Project-scoped dependency management
 * 
 * @author FRC Project Management Team
 * @version 2.0.0-Phase2E-D
 * @since Phase 2E-D - Advanced Task Management
 */
@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    
    // =========================================================================
    // BASIC RELATIONSHIP QUERIES
    // =========================================================================
    
    /**
     * Finds all dependencies where the specified task is the dependent (successor).
     * 
     * @param dependentTask the task that depends on others
     * @return list of dependencies where this task is dependent
     */
    List<TaskDependency> findByDependentTask(Task dependentTask);
    
    /**
     * Finds all dependencies where the specified task is the prerequisite (predecessor).
     * 
     * @param prerequisiteTask the task that others depend on
     * @return list of dependencies where this task is prerequisite
     */
    List<TaskDependency> findByPrerequisiteTask(Task prerequisiteTask);
    
    /**
     * Finds all active dependencies for a specific dependent task.
     * 
     * @param dependentTask the dependent task
     * @param active whether to find active or inactive dependencies
     * @return list of active dependencies for the task
     */
    List<TaskDependency> findByDependentTaskAndActive(Task dependentTask, boolean active);
    
    /**
     * Finds all active dependencies for a specific prerequisite task.
     * 
     * @param prerequisiteTask the prerequisite task
     * @param active whether to find active or inactive dependencies
     * @return list of active dependencies for the task
     */
    List<TaskDependency> findByPrerequisiteTaskAndActive(Task prerequisiteTask, boolean active);
    
    /**
     * Finds a specific dependency between two tasks.
     * 
     * @param dependentTask the dependent task
     * @param prerequisiteTask the prerequisite task
     * @return optional dependency if it exists
     */
    Optional<TaskDependency> findByDependentTaskAndPrerequisiteTask(Task dependentTask, Task prerequisiteTask);
    
    /**
     * Checks if a dependency exists between two tasks.
     * 
     * @param dependentTask the dependent task
     * @param prerequisiteTask the prerequisite task
     * @return true if dependency exists
     */
    boolean existsByDependentTaskAndPrerequisiteTask(Task dependentTask, Task prerequisiteTask);
    
    // =========================================================================
    // PROJECT-SCOPED QUERIES
    // =========================================================================
    
    /**
     * Finds all dependencies within a specific project.
     * 
     * @param project the project
     * @return list of all dependencies in the project
     */
    List<TaskDependency> findByProject(Project project);
    
    /**
     * Finds all active dependencies within a specific project.
     * 
     * @param project the project
     * @param active whether to find active dependencies
     * @return list of active dependencies in the project
     */
    List<TaskDependency> findByProjectAndActive(Project project, boolean active);
    
    /**
     * Counts total dependencies in a project.
     * 
     * @param project the project
     * @return count of dependencies
     */
    long countByProject(Project project);
    
    /**
     * Counts active dependencies in a project.
     * 
     * @param project the project
     * @param active whether to count active dependencies
     * @return count of active dependencies
     */
    long countByProjectAndActive(Project project, boolean active);
    
    // =========================================================================
    // DEPENDENCY TYPE QUERIES
    // =========================================================================
    
    /**
     * Finds all dependencies of a specific type.
     * 
     * @param dependencyType the type of dependency
     * @return list of dependencies of the specified type
     */
    List<TaskDependency> findByDependencyType(DependencyType dependencyType);
    
    /**
     * Finds dependencies by type within a project.
     * 
     * @param project the project
     * @param dependencyType the dependency type
     * @return list of dependencies of the specified type in the project
     */
    List<TaskDependency> findByProjectAndDependencyType(Project project, DependencyType dependencyType);
    
    /**
     * Finds active dependencies by type within a project.
     * 
     * @param project the project
     * @param dependencyType the dependency type
     * @param active whether to find active dependencies
     * @return list of active dependencies of the specified type
     */
    List<TaskDependency> findByProjectAndDependencyTypeAndActive(Project project, DependencyType dependencyType, boolean active);
    
    /**
     * Counts dependencies by type within a project.
     * 
     * @param project the project
     * @param dependencyType the dependency type
     * @return count of dependencies of the specified type
     */
    long countByProjectAndDependencyType(Project project, DependencyType dependencyType);
    
    // =========================================================================
    // CRITICAL PATH ANALYSIS QUERIES
    // =========================================================================
    
    /**
     * Finds all dependencies marked as being on the critical path.
     * 
     * @param project the project
     * @return list of critical path dependencies
     */
    List<TaskDependency> findByProjectAndCriticalPath(Project project, boolean criticalPath);
    
    /**
     * Finds critical path dependencies of a specific type.
     * 
     * @param project the project
     * @param dependencyType the dependency type
     * @param criticalPath whether to find critical path dependencies
     * @return list of critical path dependencies of the specified type
     */
    List<TaskDependency> findByProjectAndDependencyTypeAndCriticalPath(Project project, DependencyType dependencyType, boolean criticalPath);
    
    /**
     * Counts critical path dependencies in a project.
     * 
     * @param project the project
     * @param criticalPath whether to count critical path dependencies
     * @return count of critical path dependencies
     */
    long countByProjectAndCriticalPath(Project project, boolean criticalPath);
    
    /**
     * Updates critical path status for dependencies in a project.
     * Used by critical path analysis algorithms.
     * 
     * @param project the project
     * @param criticalPath the new critical path status
     * @return number of dependencies updated
     */
    @Modifying
    @Query("UPDATE TaskDependency td SET td.criticalPath = :criticalPath WHERE td.project = :project")
    int updateCriticalPathForProject(@Param("project") Project project, @Param("criticalPath") boolean criticalPath);
    
    /**
     * Marks specific dependencies as critical path.
     * 
     * @param dependencyIds list of dependency IDs to mark as critical
     * @return number of dependencies updated
     */
    @Modifying
    @Query("UPDATE TaskDependency td SET td.criticalPath = true WHERE td.id IN :dependencyIds")
    int markDependenciesAsCriticalPath(@Param("dependencyIds") List<Long> dependencyIds);
    
    // =========================================================================
    // LAG TIME AND SCHEDULING QUERIES
    // =========================================================================
    
    /**
     * Finds dependencies with lag time (positive or negative).
     * 
     * @param project the project
     * @return list of dependencies with lag time
     */
    @Query("SELECT td FROM TaskDependency td WHERE td.project = :project AND td.lagHours IS NOT NULL AND td.lagHours != 0 AND td.active = true")
    List<TaskDependency> findDependenciesWithLagTime(@Param("project") Project project);
    
    /**
     * Finds dependencies with positive lag time (delays).
     * 
     * @param project the project
     * @return list of dependencies with delays
     */
    @Query("SELECT td FROM TaskDependency td WHERE td.project = :project AND td.lagHours > 0 AND td.active = true")
    List<TaskDependency> findDependenciesWithDelays(@Param("project") Project project);
    
    /**
     * Finds dependencies with negative lag time (lead time).
     * 
     * @param project the project
     * @return list of dependencies with lead time
     */
    @Query("SELECT td FROM TaskDependency td WHERE td.project = :project AND td.lagHours < 0 AND td.active = true")
    List<TaskDependency> findDependenciesWithLeadTime(@Param("project") Project project);
    
    /**
     * Finds the maximum lag time in a project.
     * 
     * @param project the project
     * @return maximum lag time in hours
     */
    @Query("SELECT MAX(td.lagHours) FROM TaskDependency td WHERE td.project = :project AND td.active = true")
    Integer findMaxLagTimeInProject(@Param("project") Project project);
    
    // =========================================================================
    // CYCLE DETECTION QUERIES
    // =========================================================================
    
    /**
     * Finds all dependencies involving a specific task (as either dependent or prerequisite).
     * Used for cycle detection algorithms.
     * 
     * @param task the task to check
     * @return list of all dependencies involving the task
     */
    @Query("SELECT td FROM TaskDependency td WHERE (td.dependentTask = :task OR td.prerequisiteTask = :task) AND td.active = true")
    List<TaskDependency> findAllDependenciesInvolvingTask(@Param("task") Task task);
    
    /**
     * Finds potential cycle dependencies.
     * Returns dependencies where the prerequisite task depends on the dependent task.
     * 
     * @param project the project to check
     * @return list of potentially circular dependencies
     */
    @Query("SELECT td1 FROM TaskDependency td1 WHERE td1.project = :project AND td1.active = true " +
           "AND EXISTS (SELECT td2 FROM TaskDependency td2 WHERE td2.active = true " +
           "AND td2.dependentTask = td1.prerequisiteTask AND td2.prerequisiteTask = td1.dependentTask)")
    List<TaskDependency> findPotentialCircularDependencies(@Param("project") Project project);
    
    /**
     * Checks if adding a dependency would create a cycle.
     * 
     * @param dependentTaskId the ID of the dependent task
     * @param prerequisiteTaskId the ID of the prerequisite task
     * @return true if adding this dependency would create a cycle
     */
    @Query("SELECT COUNT(td) > 0 FROM TaskDependency td WHERE td.active = true " +
           "AND td.dependentTask.id = :prerequisiteTaskId AND td.prerequisiteTask.id = :dependentTaskId")
    boolean wouldCreateDirectCycle(@Param("dependentTaskId") Long dependentTaskId, @Param("prerequisiteTaskId") Long prerequisiteTaskId);
    
    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================
    
    /**
     * Deactivates all dependencies for a specific task.
     * Used when a task is deleted or needs to be isolated.
     * 
     * @param task the task
     * @return number of dependencies deactivated
     */
    @Modifying
    @Query("UPDATE TaskDependency td SET td.active = false WHERE td.dependentTask = :task OR td.prerequisiteTask = :task")
    int deactivateDependenciesForTask(@Param("task") Task task);
    
    /**
     * Reactivates all dependencies for a specific task.
     * 
     * @param task the task
     * @return number of dependencies reactivated
     */
    @Modifying
    @Query("UPDATE TaskDependency td SET td.active = true WHERE td.dependentTask = :task OR td.prerequisiteTask = :task")
    int reactivateDependenciesForTask(@Param("task") Task task);
    
    /**
     * Deletes all dependencies for a specific task.
     * Used when permanently removing a task.
     * 
     * @param task the task
     * @return number of dependencies deleted
     */
    @Modifying
    @Query("DELETE FROM TaskDependency td WHERE td.dependentTask = :task OR td.prerequisiteTask = :task")
    int deleteDependenciesForTask(@Param("task") Task task);
    
    /**
     * Updates all dependencies in a project to not be on critical path.
     * Used to reset critical path analysis.
     * 
     * @param project the project
     * @return number of dependencies updated
     */
    @Modifying
    @Query("UPDATE TaskDependency td SET td.criticalPath = false WHERE td.project = :project")
    int clearCriticalPathForProject(@Param("project") Project project);
    
    // =========================================================================
    // ANALYTICS AND REPORTING QUERIES
    // =========================================================================
    
    /**
     * Gets dependency statistics for a project.
     * 
     * @param project the project
     * @return list of dependency type counts
     */
    @Query("SELECT td.dependencyType, COUNT(td) FROM TaskDependency td WHERE td.project = :project AND td.active = true GROUP BY td.dependencyType")
    List<Object[]> getDependencyStatsByProject(@Param("project") Project project);
    
    /**
     * Finds the most connected tasks (tasks with the most dependencies).
     * 
     * @param project the project
     * @param limit maximum number of results
     * @return list of tasks ordered by dependency count
     */
    @Query(value = "SELECT td.dependent_task, COUNT(td) as depCount FROM task_dependency td " +
           "WHERE td.project = :project AND td.active = true " +
           "GROUP BY td.dependent_task " +
           "ORDER BY depCount DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findMostDependentTasks(@Param("project") Project project, @Param("limit") int limit);
    
    /**
     * Finds tasks that others depend on most (blocking tasks).
     * 
     * @param project the project
     * @param limit maximum number of results
     * @return list of tasks ordered by how many depend on them
     */
    @Query(value = "SELECT td.prerequisite_task, COUNT(td) as blockCount FROM task_dependency td " +
           "WHERE td.project = :project AND td.active = true " +
           "GROUP BY td.prerequisite_task " +
           "ORDER BY blockCount DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findMostBlockingTasks(@Param("project") Project project, @Param("limit") int limit);
    
    /**
     * Calculates average lag time for dependencies in a project.
     * 
     * @param project the project
     * @return average lag time in hours
     */
    @Query("SELECT AVG(td.lagHours) FROM TaskDependency td WHERE td.project = :project AND td.active = true AND td.lagHours IS NOT NULL")
    Double getAverageLagTimeForProject(@Param("project") Project project);
    
    // =========================================================================
    // BUILD SEASON SPECIFIC QUERIES
    // =========================================================================
    
    /**
     * Finds dependencies that are likely external constraints.
     * These have significant lag times suggesting supplier/vendor dependencies.
     * 
     * @param project the project
     * @param minLagHours minimum lag time to consider external
     * @return list of external constraint dependencies
     */
    @Query("SELECT td FROM TaskDependency td WHERE td.project = :project AND td.active = true " +
           "AND td.lagHours >= :minLagHours " +
           "ORDER BY td.lagHours DESC")
    List<TaskDependency> findExternalConstraints(@Param("project") Project project, @Param("minLagHours") int minLagHours);
    
    /**
     * Finds blocking dependencies (dependencies that prevent work from starting).
     * 
     * @param project the project
     * @return list of blocking dependencies
     */
    @Query("SELECT td FROM TaskDependency td WHERE td.project = :project AND td.active = true " +
           "AND td.dependencyType IN ('BLOCKING', 'FINISH_TO_START') " +
           "AND td.prerequisiteTask.completed = false " +
           "ORDER BY td.prerequisiteTask.endDate ASC")
    List<TaskDependency> findCurrentlyBlockingDependencies(@Param("project") Project project);
}