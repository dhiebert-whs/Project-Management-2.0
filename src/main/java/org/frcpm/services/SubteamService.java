package org.frcpm.services;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Subsystem;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Subteam entities.
 */
public interface SubteamService extends Service<Subteam, Long> {
    
    /**
     * Find subteam by name.
     *
     * @param name the name of the subteam
     * @return Optional containing the subteam if found
     */
    Optional<Subteam> findByName(String name);
    
    /**
     * Find subteam by name (case insensitive).
     *
     * @param name the name of the subteam
     * @return Optional containing the subteam if found
     */
    Optional<Subteam> findByNameIgnoreCase(String name);
    
    /**
     * Find all subteams ordered by name.
     *
     * @return List of subteams ordered by name
     */
    List<Subteam> findAllOrderedByName();
    
    /**
     * Find subteams that have members.
     *
     * @return List of subteams with members
     */
    List<Subteam> findSubteamsWithMembers();
    
    /**
     * Find subteams that have subsystems.
     *
     * @return List of subteams with subsystems
     */
    List<Subteam> findSubteamsWithSubsystems();
    
    /**
     * Add a member to a subteam.
     *
     * @param subteam the subteam
     * @param member the team member to add
     * @return the updated subteam
     */
    Subteam addMember(Subteam subteam, TeamMember member);
    
    /**
     * Remove a member from a subteam.
     *
     * @param subteam the subteam
     * @param member the team member to remove
     * @return the updated subteam
     */
    Subteam removeMember(Subteam subteam, TeamMember member);
    
    /**
     * Add a subsystem to a subteam.
     *
     * @param subteam the subteam
     * @param subsystem the subsystem to add
     * @return the updated subteam
     */
    Subteam addSubsystem(Subteam subteam, Subsystem subsystem);
    
    /**
     * Remove a subsystem from a subteam.
     *
     * @param subteam the subteam
     * @param subsystem the subsystem to remove
     * @return the updated subteam
     */
    Subteam removeSubsystem(Subteam subteam, Subsystem subsystem);
    
    /**
     * Count members in a subteam.
     *
     * @param subteamId the ID of the subteam
     * @return number of members in the subteam
     */
    long countMembers(Long subteamId);
    
    /**
     * Count subsystems owned by a subteam.
     *
     * @param subteamId the ID of the subteam
     * @return number of subsystems owned by the subteam
     */
    long countSubsystems(Long subteamId);
    
    /**
     * Check if a subteam name exists (case insensitive).
     *
     * @param name the name to check
     * @return true if the name exists
     */
    boolean existsByName(String name);
    
    /**
     * Check if a subteam name exists (case insensitive) excluding a specific subteam.
     *
     * @param name the name to check
     * @param excludeId the ID of the subteam to exclude from the check
     * @return true if the name exists
     */
    boolean existsByNameExcluding(String name, Long excludeId);
    
    /**
     * Create default subteams for a new FRC team.
     *
     * @return List of default subteams (mechanical, programming, electrical, etc.)
     */
    List<Subteam> createDefaultSubteams();
    
    /**
     * Create a new subteam with the specified properties.
     *
     * @param name the name of the subteam
     * @param color the color code for the subteam
     * @param description the description of the subteam
     * @return the created subteam
     */
    Subteam createSubteam(String name, String color, String description);
}