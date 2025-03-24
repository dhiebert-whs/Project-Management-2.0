package org.frcpm.repositories.specific;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TeamMember entity.
 */
public interface TeamMemberRepository extends Repository<TeamMember, Long> {
    
    /**
     * Finds a team member by username.
     * 
     * @param username the username to search for
     * @return an Optional containing the found team member, or empty if not found
     */
    Optional<TeamMember> findByUsername(String username);
    
    /**
     * Finds team members by subteam.
     * 
     * @param subteam the subteam to find members for
     * @return a list of team members in the subteam
     */
    List<TeamMember> findBySubteam(Subteam subteam);
    
    /**
     * Finds team members with a specific skill.
     * 
     * @param skill the skill to search for
     * @return a list of team members with the skill
     */
    List<TeamMember> findBySkill(String skill);
    
    /**
     * Finds team members who are leaders.
     * 
     * @return a list of team members who are leaders
     */
    List<TeamMember> findLeaders();
    
    /**
     * Finds team members by name (first name or last name).
     * 
     * @param name the name to search for
     * @return a list of team members matching the name
     */
    List<TeamMember> findByName(String name);
}