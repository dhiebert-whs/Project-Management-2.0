// src/main/java/org/frcpm/repositories/spring/SubteamRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.Subteam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Subteam entity.
 * Provides Spring Data JPA auto-implemented methods plus custom query methods.
 */
@Repository
public interface SubteamRepository extends JpaRepository<Subteam, Long> {
    
    /**
     * Finds a subteam by name.
     * 
     * @param name the name to search for
     * @return an Optional containing the found subteam, or empty if not found
     */
    Optional<Subteam> findByName(String name);
    
    /**
     * Finds a subteam by name (case insensitive).
     * 
     * @param name the name to search for
     * @return an Optional containing the found subteam, or empty if not found
     */
    Optional<Subteam> findByNameIgnoreCase(String name);
    
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
    @Query("SELECT s FROM Subteam s WHERE s.specialties LIKE %:specialty%")
    List<Subteam> findBySpecialty(@Param("specialty") String specialty);
    
    /**
     * Finds subteams with specialties containing the given text (case insensitive).
     * 
     * @param specialty the specialty to search for
     * @return a list of subteams with matching specialties
     */
    @Query("SELECT s FROM Subteam s WHERE LOWER(s.specialties) LIKE LOWER(CONCAT('%', :specialty, '%'))")
    List<Subteam> findBySpecialtyIgnoreCase(@Param("specialty") String specialty);
    
    /**
     * Finds subteams with specialties containing the given text (case insensitive).
     * This method is used by the service layer for specialty searches.
     * 
     * @param specialty the specialty to search for
     * @return a list of subteams with matching specialties
     */
    @Query("SELECT s FROM Subteam s WHERE LOWER(s.specialties) LIKE LOWER(CONCAT('%', :specialty, '%'))")
    List<Subteam> findBySpecialtiesContainingIgnoreCase(@Param("specialty") String specialty);
    
    /**
     * Finds all subteams ordered by name.
     * 
     * @return a list of all subteams ordered by name
     */
    List<Subteam> findAllByOrderByName();
    
    /**
     * Checks if a subteam with the given name exists.
     * 
     * @param name the name to check
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Checks if a subteam with the given name exists (case insensitive).
     * 
     * @param name the name to check
     * @return true if exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);
}