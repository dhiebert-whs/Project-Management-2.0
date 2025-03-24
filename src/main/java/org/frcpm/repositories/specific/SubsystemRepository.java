package org.frcpm.repositories.specific;

import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Subsystem entity.
 */
public interface SubsystemRepository extends Repository<Subsystem, Long> {
    
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
}