package org.frcpm.services;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for TeamMember entity.
 */
public interface TeamMemberService extends Service<TeamMember, Long> {
    
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
     * Creates a new team member.
     * 
     * @param username the username
     * @param firstName the first name
     * @param lastName the last name
     * @param email the email address
     * @param phone the phone number (optional)
     * @param isLeader whether the member is a leader
     * @return the created team member
     */
    TeamMember createTeamMember(String username, String firstName, String lastName, 
                               String email, String phone, boolean isLeader);
    
    /**
     * Assigns a team member to a subteam.
     * 
     * @param memberId the team member ID
     * @param subteamId the subteam ID
     * @return the updated team member, or null if not found
     */
    TeamMember assignToSubteam(Long memberId, Long subteamId);
    
    /**
     * Updates a team member's skills.
     * 
     * @param memberId the team member ID
     * @param skills the new skills
     * @return the updated team member, or null if not found
     */
    TeamMember updateSkills(Long memberId, String skills);
    
    /**
     * Updates a team member's contact information.
     * 
     * @param memberId the team member ID
     * @param email the new email (optional)
     * @param phone the new phone (optional)
     * @return the updated team member, or null if not found
     */
    TeamMember updateContactInfo(Long memberId, String email, String phone);
}
