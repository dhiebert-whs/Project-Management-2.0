package org.frcpm.services;

import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Subsystem entity.
 */
public interface SubsystemService extends Service<Subsystem, Long> {
    
    /**
     * Finds a subsystem by name.
     * 
     * @param name the name to search for
     * @return an Optional containing the found subsystem, or empty if not found
     */
    Optional<Subsystem> findByName(String name);
    
    /**
     * Finds subsystems by status.
     * 
     * @param status the status to search for
     * @return a list of subsystems with the given status
     */
    List<Subsystem> findByStatus(Subsystem.Status status);
    
    /**
     * Finds subsystems managed by a specific subteam.
     * 
     * @param subteam the responsible subteam
     * @return a list of subsystems managed by the subteam
     */
    List<Subsystem> findByResponsibleSubteam(Subteam subteam);
    
    /**
     * Creates a new subsystem.
     * 
     * @param name the subsystem name
     * @param description the subsystem description (optional)
     * @param status the initial status
     * @param responsibleSubteamId the ID of the responsible subteam (optional)
     * @return the created subsystem
     */
    Subsystem createSubsystem(String name, String description, 
                             Subsystem.Status status, Long responsibleSubteamId);
    
    /**
     * Updates a subsystem's status.
     * 
     * @param subsystemId the subsystem ID
     * @param status the new status
     * @return the updated subsystem, or null if not found
     */
    Subsystem updateStatus(Long subsystemId, Subsystem.Status status);
    
    /**
     * Assigns a responsible subteam to a subsystem.
     * 
     * @param subsystemId the subsystem ID
     * @param subteamId the subteam ID
     * @return the updated subsystem, or null if not found
     */
    Subsystem assignResponsibleSubteam(Long subsystemId, Long subteamId);
}