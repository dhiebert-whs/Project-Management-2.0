package org.frcpm.services;

import org.frcpm.models.Subsystem;
import org.frcpm.models.Subteam;
import org.frcpm.models.Project;
import org.frcpm.models.Task;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Subsystem entities.
 */
public interface SubsystemService extends Service<Subsystem, Long> {
    
    /**
     * Find subsystems by project.
     *
     * @param project the project
     * @return List of subsystems in the project
     */
    List<Subsystem> findByProject(Project project);
    
    /**
     * Find subsystems by project ID.
     *
     * @param projectId the project ID
     * @return List of subsystems in the project
     */
    List<Subsystem> findByProjectId(Long projectId);
    
    /**
     * Find subsystems by owner subteam.
     *
     * @param subteam the owner subteam
     * @return List of subsystems owned by the subteam
     */
    List<Subsystem> findByOwnerSubteam(Subteam subteam);
    
    /**
     * Find subsystems by owner subteam ID.
     *
     * @param subteamId the owner subteam ID
     * @return List of subsystems owned by the subteam
     */
    List<Subsystem> findByOwnerSubteamId(Long subteamId);
    
    /**
     * Find subsystem by name within a project.
     *
     * @param name the name of the subsystem
     * @param project the project
     * @return Optional containing the subsystem if found
     */
    Optional<Subsystem> findByNameAndProject(String name, Project project);
    
    /**
     * Find subsystem by name within a project (case insensitive).
     *
     * @param name the name of the subsystem
     * @param project the project
     * @return Optional containing the subsystem if found
     */
    Optional<Subsystem> findByNameIgnoreCaseAndProject(String name, Project project);
    
    /**
     * Find subsystems by project ordered by name.
     *
     * @param project the project
     * @return List of subsystems ordered by name
     */
    List<Subsystem> findByProjectOrderedByName(Project project);
    
    /**
     * Find subsystems by project and owner subteam.
     *
     * @param project the project
     * @param subteam the owner subteam
     * @return List of subsystems owned by the subteam in the project
     */
    List<Subsystem> findByProjectAndOwnerSubteam(Project project, Subteam subteam);
    
    /**
     * Add a task to a subsystem.
     *
     * @param subsystem the subsystem
     * @param task the task to add
     * @return the updated subsystem
     */
    Subsystem addTask(Subsystem subsystem, Task task);
    
    /**
     * Remove a task from a subsystem.
     *
     * @param subsystem the subsystem
     * @param task the task to remove
     * @return the updated subsystem
     */
    Subsystem removeTask(Subsystem subsystem, Task task);
    
    /**
     * Count tasks in a subsystem.
     *
     * @param subsystemId the ID of the subsystem
     * @return number of tasks in the subsystem
     */
    long countTasks(Long subsystemId);
    
    /**
     * Count completed tasks in a subsystem.
     *
     * @param subsystemId the ID of the subsystem
     * @return number of completed tasks in the subsystem
     */
    long countCompletedTasks(Long subsystemId);
    
    /**
     * Calculate average progress for a subsystem.
     *
     * @param subsystemId the ID of the subsystem
     * @return average progress percentage
     */
    double calculateAverageProgress(Long subsystemId);
    
    /**
     * Find subsystems with tasks.
     *
     * @param projectId the project ID
     * @return List of subsystems that have tasks
     */
    List<Subsystem> findSubsystemsWithTasks(Long projectId);
    
    /**
     * Check if a subsystem name exists within a project (case insensitive).
     *
     * @param name the name to check
     * @param projectId the project ID
     * @return true if the name exists
     */
    boolean existsByNameInProject(String name, Long projectId);
    
    /**
     * Check if a subsystem name exists within a project (case insensitive) excluding a specific subsystem.
     *
     * @param name the name to check
     * @param projectId the project ID
     * @param excludeId the ID of the subsystem to exclude from the check
     * @return true if the name exists
     */
    boolean existsByNameInProjectExcluding(String name, Long projectId, Long excludeId);
}