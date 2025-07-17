package org.frcpm.repositories.spring;

import org.frcpm.models.Subteam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Subteam entities.
 */
@Repository
public interface SubteamRepository extends JpaRepository<Subteam, Long> {
    
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
    List<Subteam> findAllByOrderByName();
    
    /**
     * Find subteams that have members.
     *
     * @return List of subteams with members
     */
    @Query("SELECT DISTINCT s FROM Subteam s WHERE s.members IS NOT EMPTY")
    List<Subteam> findSubteamsWithMembers();
    
    /**
     * Find subteams that have subsystems.
     *
     * @return List of subteams with subsystems
     */
    @Query("SELECT DISTINCT s FROM Subteam s WHERE s.subsystems IS NOT EMPTY")
    List<Subteam> findSubteamsWithSubsystems();
    
    /**
     * Count members in a subteam.
     *
     * @param subteamId the ID of the subteam
     * @return number of members in the subteam
     */
    @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.subteam.id = :subteamId")
    long countMembersBySubteamId(@Param("subteamId") Long subteamId);
    
    /**
     * Count subsystems owned by a subteam.
     *
     * @param subteamId the ID of the subteam
     * @return number of subsystems owned by the subteam
     */
    @Query("SELECT COUNT(s) FROM Subsystem s WHERE s.ownerSubteam.id = :subteamId")
    long countSubsystemsBySubteamId(@Param("subteamId") Long subteamId);
    
    /**
     * Check if a subteam name exists (case insensitive).
     *
     * @param name the name to check
     * @return true if the name exists
     */
    boolean existsByNameIgnoreCase(String name);
}