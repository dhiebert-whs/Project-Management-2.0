// src/main/java/org/frcpm/repositories/spring/TeamMemberRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for TeamMember entities.
 * This interface extends JpaRepository to provide automatic CRUD operations
 * and includes custom query methods for team member-specific operations.
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    
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
     * Finds team members by subteam ID.
     * 
     * @param subteamId the subteam ID
     * @return a list of team members in the subteam
     */
    List<TeamMember> findBySubteamId(Long subteamId);
    
    /**
     * Finds team members with a specific skill.
     * 
     * @param skill the skill to search for
     * @return a list of team members with the skill
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.skills LIKE %:skill%")
    List<TeamMember> findBySkill(@Param("skill") String skill);
    
    /**
     * Finds team members who are leaders.
     * 
     * @return a list of team members who are leaders
     */
    List<TeamMember> findByLeaderTrue();
    
    /**
     * Finds team members by name (first name or last name).
     * 
     * @param name the name to search for
     * @return a list of team members matching the name
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.firstName LIKE %:name% OR tm.lastName LIKE %:name%")
    List<TeamMember> findByName(@Param("name") String name);
    
    /**
     * Finds team members by first name containing the specified text.
     * 
     * @param firstName the first name to search for
     * @return a list of matching team members
     */
    List<TeamMember> findByFirstNameContainingIgnoreCase(String firstName);
    
    /**
     * Finds team members by last name containing the specified text.
     * 
     * @param lastName the last name to search for
     * @return a list of matching team members
     */
    List<TeamMember> findByLastNameContainingIgnoreCase(String lastName);
    
    /**
     * Finds team members by email.
     * 
     * @param email the email to search for
     * @return an Optional containing the found team member, or empty if not found
     */
    Optional<TeamMember> findByEmail(String email);
    
    /**
     * Finds team members without a subteam assignment.
     * 
     * @return a list of unassigned team members
     */
    List<TeamMember> findBySubteamIsNull();
    
    /**
     * Finds team members with specific skills.
     * 
     * @param skills the list of skills to search for
     * @return a list of team members with any of the specified skills
     */
    @Query("SELECT DISTINCT tm FROM TeamMember tm WHERE " +
           "EXISTS (SELECT 1 FROM unnest(string_to_array(tm.skills, ',')) AS skill " +
           "WHERE trim(skill) IN :skills)")
    List<TeamMember> findBySkillsIn(@Param("skills") List<String> skills);
    
    /**
     * Finds team members who are not leaders.
     * 
     * @return a list of non-leader team members
     */
    List<TeamMember> findByLeaderFalse();
    
    /**
     * Counts team members in a specific subteam.
     * 
     * @param subteam the subteam
     * @return the count of members in the subteam
     */
    long countBySubteam(Subteam subteam);
    
    /**
     * Counts team members who are leaders.
     * 
     * @return the count of leader team members
     */
    long countByLeaderTrue();
    
    /**
     * Finds team members assigned to tasks in a specific project.
     * 
     * @param projectId the project ID
     * @return a list of team members working on the project
     */
    @Query("SELECT DISTINCT tm FROM TeamMember tm " +
           "JOIN tm.assignedTasks t " +
           "WHERE t.project.id = :projectId")
    List<TeamMember> findMembersWorkingOnProject(@Param("projectId") Long projectId);
    
    /**
     * Finds the most active team members based on task assignments.
     * 
     * @param limit the maximum number of results
     * @return a list of the most active team members
     */
    @Query("SELECT tm FROM TeamMember tm " +
           "JOIN tm.assignedTasks t " +
           "GROUP BY tm " +
           "ORDER BY COUNT(t) DESC")
    List<TeamMember> findMostActiveMembers(@Param("limit") int limit);
    
    /**
     * Finds team members with the specified username or email.
     * Useful for login/authentication scenarios.
     * 
     * @param username the username
     * @param email the email
     * @return an Optional containing the found team member
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.username = :username OR tm.email = :email")
    Optional<TeamMember> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
    
    /**
     * Provides compatibility with the existing findLeaders method.
     * 
     * @return a list of team members who are leaders
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.leader = true")
    List<TeamMember> findLeaders();
}