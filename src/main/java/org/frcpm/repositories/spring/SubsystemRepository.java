package org.frcpm.repositories.spring;

import org.frcpm.models.Subsystem;
import org.frcpm.models.Subteam;
import org.frcpm.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Subsystem entities.
 */
@Repository
public interface SubsystemRepository extends JpaRepository<Subsystem, Long> {
    
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
    List<Subsystem> findByProjectOrderByName(Project project);
    
    /**
     * Find subsystems by project and owner subteam.
     *
     * @param project the project
     * @param subteam the owner subteam
     * @return List of subsystems owned by the subteam in the project
     */
    List<Subsystem> findByProjectAndOwnerSubteam(Project project, Subteam subteam);
    
    /**
     * Count tasks in a subsystem.
     *
     * @param subsystemId the ID of the subsystem
     * @return number of tasks in the subsystem
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.subsystem.id = :subsystemId")
    long countTasksBySubsystemId(@Param("subsystemId") Long subsystemId);
    
    /**
     * Count completed tasks in a subsystem.
     *
     * @param subsystemId the ID of the subsystem
     * @return number of completed tasks in the subsystem
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.subsystem.id = :subsystemId AND t.completed = true")
    long countCompletedTasksBySubsystemId(@Param("subsystemId") Long subsystemId);
    
    /**
     * Calculate average progress for a subsystem.
     *
     * @param subsystemId the ID of the subsystem
     * @return average progress percentage
     */
    @Query("SELECT AVG(t.progress) FROM Task t WHERE t.subsystem.id = :subsystemId")
    Double calculateAverageProgressBySubsystemId(@Param("subsystemId") Long subsystemId);
    
    /**
     * Find subsystems with tasks.
     *
     * @param projectId the project ID
     * @return List of subsystems that have tasks
     */
    @Query("SELECT DISTINCT s FROM Subsystem s WHERE s.project.id = :projectId AND s.tasks IS NOT EMPTY")
    List<Subsystem> findSubsystemsWithTasks(@Param("projectId") Long projectId);
    
    /**
     * Check if a subsystem name exists within a project (case insensitive).
     *
     * @param name the name to check
     * @param projectId the project ID
     * @return true if the name exists
     */
    boolean existsByNameIgnoreCaseAndProjectId(String name, Long projectId);
}