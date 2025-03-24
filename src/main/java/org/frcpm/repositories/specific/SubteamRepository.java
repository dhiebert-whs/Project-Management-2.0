package org.frcpm.repositories.specific;

import org.frcpm.models.Subteam;
import org.frcpm.repositories.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Subteam entity.
 */
public interface SubteamRepository extends Repository<Subteam, Long> {
    
    /**
     * Finds a subteam by name.
     * 
     * @param name the name to search for
     * @return an Optional containing the found subteam, or empty if not found
     */
    Optional<Subteam> findByName(String name);
    
    /**
     * Finds subteams by color code.
     * 
     * @param colorCode the color code to search for
     * @return a list of subteams with the given color code
     */
    List<Subteam> findByColorCode(String colorCode);
    
    /**
     * Finds subteams with specialties containing the given text.
     * 
     * @param specialty the specialty to search for
     * @return a list of subteams with matching specialties
     */
    List<Subteam> findBySpecialty(String specialty);
}