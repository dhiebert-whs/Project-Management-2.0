// src/test/java/org/frcpm/services/impl/TestableTeamMemberServiceAsyncImpl.java
package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.repositories.specific.TeamMemberRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Testable implementation that provides async functionality similar to TeamMemberServiceAsyncImpl
 * but allows constructor injection for testing.
 */
public class TestableTeamMemberServiceAsyncImpl extends TestableTeamMemberServiceImpl {
    
    /**
     * Constructor for testing with injected repositories.
     * 
     * @param teamMemberRepository the team member repository
     * @param subteamRepository the subteam repository
     */
    public TestableTeamMemberServiceAsyncImpl(
            TeamMemberRepository teamMemberRepository,
            SubteamRepository subteamRepository) {
        super(teamMemberRepository, subteamRepository);
    }
    
    /**
     * Default constructor that uses ServiceLocator (for compatibility).
     */
    public TestableTeamMemberServiceAsyncImpl() {
        super();
    }
    
    // Async method implementations that mimic TeamMemberServiceAsyncImpl
    
    /**
     * Finds all team members asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of team members
     */
    public CompletableFuture<List<TeamMember>> findAllAsync(Consumer<List<TeamMember>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<TeamMember>> future = new CompletableFuture<>();
        
        try {
            List<TeamMember> result = findAll();
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
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
    public CompletableFuture<TeamMember> findByIdAsync(Long id, Consumer<TeamMember> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<TeamMember> future = new CompletableFuture<>();
        
        try {
            TeamMember result = findById(id);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
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
    public CompletableFuture<TeamMember> saveAsync(TeamMember entity, Consumer<TeamMember> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<TeamMember> future = new CompletableFuture<>();
        
        try {
            TeamMember result = save(entity);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
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
    public CompletableFuture<Boolean> deleteByIdAsync(Long id, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            boolean result = deleteById(id);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
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
    public CompletableFuture<Void> deleteAsync(TeamMember entity, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            delete(entity);
            if (onSuccess != null) {
                onSuccess.accept(null);
            }
            future.complete(null);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
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
    public CompletableFuture<Long> countAsync(Consumer<Long> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        
        try {
            long result = count();
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
}