// src/main/java/org/frcpm/services/SubsystemService.java

package org.frcpm.services;

import org.frcpm.models.TeamMember;
import org.frcpm.models.Subsystem;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service interface for Subsystem entity.
 */
public interface SubsystemService extends Service<Subsystem, Long> {
    
    /**
     * Finds a subsystem by name.
     * 
     * @param name the name to search for
     * @return an Optional containing the found subsystem, or empty if not found
     */
    Optional<Subsystem> findByName(String name);
    
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
     * Creates a new subsystem.
     * 
     * @param name the subsystem name
     * @param description the subsystem description (optional)
     * @param status the initial status
     * @param responsibleMemberId the ID of the responsible team member (optional)
     * @return the created subsystem
     */
    Subsystem createSubsystem(String name, String description, 
                             Subsystem.SubsystemStatus status, Long responsibleMemberId);
    
    /**
     * Updates a subsystem's status.
     * 
     * @param subsystemId the subsystem ID
     * @param status the new status
     * @return the updated subsystem, or null if not found
     */
    Subsystem updateStatus(Long subsystemId, Subsystem.SubsystemStatus status);
    
    /**
     * Assigns a responsible team member to a subsystem.
     * 
     * @param subsystemId the subsystem ID
     * @param memberId the team member ID
     * @return the updated subsystem, or null if not found
     */
    Subsystem assignResponsibleMember(Long subsystemId, Long memberId);
    
    // Async methods
    
    /**
     * Finds a subsystem by name asynchronously.
     * 
     * @param name the name to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the found subsystem or empty
     */
    default CompletableFuture<Optional<Subsystem>> findByNameAsync(String name,
                                                              Consumer<Optional<Subsystem>> onSuccess,
                                                              Consumer<Throwable> onFailure) {
        CompletableFuture<Optional<Subsystem>> future = new CompletableFuture<>();
        try {
            Optional<Subsystem> result = findByName(name);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Finds subsystems by status asynchronously.
     * 
     * @param status the status to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of subsystems
     */
    default CompletableFuture<List<Subsystem>> findByStatusAsync(Subsystem.SubsystemStatus status,
                                                            Consumer<List<Subsystem>> onSuccess,
                                                            Consumer<Throwable> onFailure) {
        CompletableFuture<List<Subsystem>> future = new CompletableFuture<>();
        try {
            List<Subsystem> result = findByStatus(status);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Finds subsystems managed by a specific team member asynchronously.
     * 
     * @param member the responsible team member
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of subsystems
     */
    default CompletableFuture<List<Subsystem>> findByResponsibleMemberAsync(TeamMember member,
                                                                        Consumer<List<Subsystem>> onSuccess,
                                                                        Consumer<Throwable> onFailure) {
        CompletableFuture<List<Subsystem>> future = new CompletableFuture<>();
        try {
            List<Subsystem> result = findByResponsibleMember(member);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Creates a new subsystem asynchronously.
     * 
     * @param name the subsystem name
     * @param description the subsystem description (optional)
     * @param status the initial status
     * @param responsibleMemberId the ID of the responsible team member (optional)
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created subsystem
     */
    default CompletableFuture<Subsystem> createSubsystemAsync(String name, String description,
                                                         Subsystem.SubsystemStatus status, Long responsibleMemberId,
                                                         Consumer<Subsystem> onSuccess,
                                                         Consumer<Throwable> onFailure) {
        CompletableFuture<Subsystem> future = new CompletableFuture<>();
        try {
            Subsystem result = createSubsystem(name, description, status, responsibleMemberId);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Updates a subsystem's status asynchronously.
     * 
     * @param subsystemId the subsystem ID
     * @param status the new status
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated subsystem
     */
    default CompletableFuture<Subsystem> updateStatusAsync(Long subsystemId, Subsystem.SubsystemStatus status,
                                                      Consumer<Subsystem> onSuccess,
                                                      Consumer<Throwable> onFailure) {
        CompletableFuture<Subsystem> future = new CompletableFuture<>();
        try {
            Subsystem result = updateStatus(subsystemId, status);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Assigns a responsible team member to a subsystem asynchronously.
     * 
     * @param subsystemId the subsystem ID
     * @param memberId the team member ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated subsystem
     */
    default CompletableFuture<Subsystem> assignResponsibleMemberAsync(Long subsystemId, Long memberId,
                                                                  Consumer<Subsystem> onSuccess,
                                                                  Consumer<Throwable> onFailure) {
        CompletableFuture<Subsystem> future = new CompletableFuture<>();
        try {
            Subsystem result = assignResponsibleMember(subsystemId, memberId);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Finds a subsystem by ID asynchronously.
     *
     * @param id the ID to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the found subsystem or null
     */
    default CompletableFuture<Subsystem> findByIdAsync(Long id,
                                                  Consumer<Subsystem> onSuccess,
                                                  Consumer<Throwable> onFailure) {
        CompletableFuture<Subsystem> future = new CompletableFuture<>();
        try {
            Subsystem result = findById(id);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Finds all subsystems asynchronously.
     *
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of subsystems
     */
    default CompletableFuture<List<Subsystem>> findAllAsync(
        Consumer<List<Subsystem>> onSuccess, 
        Consumer<Throwable> onFailure) {
        CompletableFuture<List<Subsystem>> future = new CompletableFuture<>();
        try {
            List<Subsystem> result = findAll();
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Saves a subsystem asynchronously.
     *
     * @param subsystem the subsystem to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved subsystem
     */
    default CompletableFuture<Subsystem> saveAsync(Subsystem subsystem,
                                              Consumer<Subsystem> onSuccess,
                                              Consumer<Throwable> onFailure) {
        CompletableFuture<Subsystem> future = new CompletableFuture<>();
        try {
            Subsystem result = save(subsystem);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Deletes a subsystem asynchronously.
     *
     * @param subsystem the subsystem to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed when deletion is complete
     */
    default CompletableFuture<Void> deleteAsync(Subsystem subsystem,
                                          Consumer<Void> onSuccess,
                                          Consumer<Throwable> onFailure) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            delete(subsystem);
            if (onSuccess != null) onSuccess.accept(null);
            future.complete(null);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Deletes a subsystem by ID asynchronously.
     *
     * @param id the ID of the subsystem to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a boolean indicating success
     */
    default CompletableFuture<Boolean> deleteByIdAsync(Long id,
                                                  Consumer<Boolean> onSuccess,
                                                  Consumer<Throwable> onFailure) {
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
}