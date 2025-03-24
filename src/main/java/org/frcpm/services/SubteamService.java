package org.frcpm.services;

import org.frcpm.models.Subteam;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Subteam entity.
 */
public interface SubteamService extends Service<Subteam, Long> {
    
    /**
     * Finds a subteam by name.
     * 
     * @param name the name to search for
     * @return an Optional containing the found subteam, or empty if not found
     */
    Optional<Subteam> findByName(String name);
    
    /**
     * Finds subteams by specialty.
     * 
     * @param specialty the specialty to search for
     * @return a list of subteams with matching specialties
     */
    List<Subteam> findBySpecialty(String specialty);
    
    /**
     * Creates a new subteam.
     * 
     * @param name the subteam name
     * @param colorCode the color code for UI display
     * @param specialties the subteam specialties (optional)
     * @return the created subteam
     */
    Subteam createSubteam(String name, String colorCode, String specialties);
    
    /**
     * Updates a subteam's specialties.
     * 
     * @param subteamId the subteam ID
     * @param specialties the new specialties
     * @return the updated subteam, or null if not found
     */
    Subteam updateSpecialties(Long subteamId, String specialties);
    
    /**
     * Updates a subteam's color code.
     * 
     * @param subteamId the subteam ID
     * @param colorCode the new color code
     * @return the updated subteam, or null if not found
     */
    Subteam updateColorCode(Long subteamId, String colorCode);
}
