package org.frcpm.repositories.spring;

import org.frcpm.models.TeamMember;
import org.frcpm.models.Subsystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Subsystem entity.
 * Provides Spring Data JPA auto-implemented methods plus custom query methods.
 */
@Repository
public interface SubsystemRepository extends JpaRepository<Subsystem, Long> {
    
    /**
     * Finds a subsystem by name.
     * 
     * @param name the name to search for
     * @return an Optional containing the found subsystem, or empty if not found
     */
    Optional<Subsystem> findByName(String name);
    
    /**
     * Finds a subsystem by name (case insensitive).
     * 
     * @param name the name to search for
     * @return an Optional containing the found subsystem, or empty if not found
     */
    Optional<Subsystem> findByNameIgnoreCase(String name);
    
    /**
     * Finds subsystems by status.
     * 
     * @param status the status to search for
     * @return a list of subsystems with the given status
     */
    List<Subsystem> findByStatus(Subsystem.SubsystemStatus status);
    
    /**
     * Finds subsystems managed by a specific team member.
     * 
     * @param member the responsible team member
     * @return a list of subsystems managed by the team member
     */
    List<Subsystem> findByResponsibleMember(TeamMember member);
    
    /**
     * Finds subsystems with no responsible team member assigned.
     * 
     * @return a list of subsystems with no responsible team member
     */
    List<Subsystem> findByResponsibleMemberIsNull();
    
    /**
     * Finds subsystems with a responsible team member assigned.
     * 
     * @return a list of subsystems with a responsible team member
     */
    List<Subsystem> findByResponsibleMemberIsNotNull();
    
    /**
     * Finds subsystems by description containing the given text.
     * 
     * @param description the description text to search for
     * @return a list of subsystems with matching descriptions
     */
    List<Subsystem> findByDescriptionContainingIgnoreCase(String description);
    
    /**
     * Finds all subsystems ordered by name.
     * 
     * @return a list of all subsystems ordered by name
     */
    List<Subsystem> findAllByOrderByName();
    
    /**
     * Finds subsystems by status ordered by name.
     * 
     * @param status the status to search for
     * @return a list of subsystems with the given status ordered by name
     */
    List<Subsystem> findByStatusOrderByName(Subsystem.SubsystemStatus status);
    
    /**
     * Counts subsystems by status.
     * 
     * @param status the status to count
     * @return the number of subsystems with the given status
     */
    long countByStatus(Subsystem.SubsystemStatus status);
    
    /**
     * Counts subsystems managed by a specific team member.
     * 
     * @param member the responsible team member
     * @return the number of subsystems managed by the team member
     */
    long countByResponsibleMember(TeamMember member);
    
    /**
     * Checks if a subsystem with the given name exists.
     * 
     * @param name the name to check
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Checks if a subsystem with the given name exists (case insensitive).
     * 
     * @param name the name to check
     * @return true if exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Finds subsystems with a specific status managed by a specific team member.
     * 
     * @param status the status to search for
     * @param member the responsible team member
     * @return a list of subsystems matching both criteria
     */
    @Query("SELECT s FROM Subsystem s WHERE s.status = :status AND s.responsibleMember = :member ORDER BY s.name")
    List<Subsystem> findByStatusAndResponsibleMember(@Param("status") Subsystem.SubsystemStatus status, 
                                                     @Param("member") TeamMember member);
}