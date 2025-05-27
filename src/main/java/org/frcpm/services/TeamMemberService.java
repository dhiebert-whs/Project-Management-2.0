// src/main/java/org/frcpm/services/TeamMemberService.java
package org.frcpm.services;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
    
    // Async method signatures with default implementations for backwards compatibility
    
    /**
     * Finds all team members asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of team members
     */
    default CompletableFuture<List<TeamMember>> findAllAsync(Consumer<List<TeamMember>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<TeamMember>> future = new CompletableFuture<>();
        try {
            List<TeamMember> result = findAll();
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Finds a team member by ID asynchronously.
     * 
     * @param id the team member ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the team member
     */
    default CompletableFuture<TeamMember> findByIdAsync(Long id, Consumer<TeamMember> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<TeamMember> future = new CompletableFuture<>();
        try {
            TeamMember result = findById(id);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Saves a team member asynchronously.
     * 
     * @param entity the team member to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved team member
     */
    default CompletableFuture<TeamMember> saveAsync(TeamMember entity, Consumer<TeamMember> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<TeamMember> future = new CompletableFuture<>();
        try {
            TeamMember result = save(entity);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Deletes a team member by ID asynchronously.
     * 
     * @param id the ID of the team member to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a boolean indicating success
     */
    default CompletableFuture<Boolean> deleteByIdAsync(Long id, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            boolean result = deleteById(id);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Deletes a team member asynchronously.
     * 
     * @param entity the team member to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed when the team member is deleted
     */
    default CompletableFuture<Void> deleteAsync(TeamMember entity, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            delete(entity);
            if (onSuccess != null) onSuccess.accept(null);
            future.complete(null);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Counts all team members asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the count of team members
     */
    default CompletableFuture<Long> countAsync(Consumer<Long> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        try {
            long result = count();
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Finds a team member by username asynchronously.
     * 
     * @param username the username to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the found team member or empty
     */
    default CompletableFuture<Optional<TeamMember>> findByUsernameAsync(String username, Consumer<Optional<TeamMember>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Optional<TeamMember>> future = new CompletableFuture<>();
        try {
            Optional<TeamMember> result = findByUsername(username);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Finds team members by subteam asynchronously.
     * 
     * @param subteam the subteam to find members for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of team members
     */
    default CompletableFuture<List<TeamMember>> findBySubteamAsync(Subteam subteam, Consumer<List<TeamMember>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<TeamMember>> future = new CompletableFuture<>();
        try {
            List<TeamMember> result = findBySubteam(subteam);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Finds team members with a specific skill asynchronously.
     * 
     * @param skill the skill to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of team members
     */
    default CompletableFuture<List<TeamMember>> findBySkillAsync(String skill, Consumer<List<TeamMember>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<TeamMember>> future = new CompletableFuture<>();
        try {
            List<TeamMember> result = findBySkill(skill);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Finds team members who are leaders asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of team members
     */
    default CompletableFuture<List<TeamMember>> findLeadersAsync(Consumer<List<TeamMember>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<TeamMember>> future = new CompletableFuture<>();
        try {
            List<TeamMember> result = findLeaders();
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
}