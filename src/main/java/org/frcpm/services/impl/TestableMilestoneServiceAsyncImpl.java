// src/main/java/org/frcpm/services/impl/TestableMilestoneServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.services.MilestoneService;
import org.frcpm.di.ServiceLocator;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable async implementation of MilestoneService that accepts injected repositories
 * for better unit testing. This implementation provides async methods that can be stubbed
 * in tests without requiring spy patterns.
 */
public class TestableMilestoneServiceAsyncImpl extends TestableMilestoneServiceImpl {
    
    private static final Logger LOGGER = Logger.getLogger(TestableMilestoneServiceAsyncImpl.class.getName());
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableMilestoneServiceAsyncImpl() {
        super();
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param milestoneRepository the milestone repository
     * @param projectRepository the project repository
     */
    public TestableMilestoneServiceAsyncImpl(
            MilestoneRepository milestoneRepository,
            ProjectRepository projectRepository) {
        super(milestoneRepository, projectRepository);
    }
    
    // ASYNC METHODS - These can be stubbed directly in tests without spies
    
    /**
     * Finds all milestones asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of milestones
     */
    public CompletableFuture<List<Milestone>> findAllAsync(Consumer<List<Milestone>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Milestone>> future = new CompletableFuture<>();
        
        try {
            List<Milestone> result = findAll();
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
     * Finds milestones by project asynchronously.
     * 
     * @param project the project
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of milestones
     */
    public CompletableFuture<List<Milestone>> findByProjectAsync(Project project, Consumer<List<Milestone>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Milestone>> future = new CompletableFuture<>();
        
        try {
            List<Milestone> result = findByProject(project);
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
     * Finds a milestone by ID asynchronously.
     * 
     * @param id the milestone ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the milestone
     */
    public CompletableFuture<Milestone> findByIdAsync(Long id, Consumer<Milestone> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Milestone> future = new CompletableFuture<>();
        
        try {
            Milestone result = findById(id);
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
     * Saves a milestone asynchronously.
     * 
     * @param entity the milestone to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved milestone
     */
    public CompletableFuture<Milestone> saveAsync(Milestone entity, Consumer<Milestone> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Milestone> future = new CompletableFuture<>();
        
        try {
            Milestone result = save(entity);
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
     * Deletes a milestone by ID asynchronously.
     * 
     * @param id the ID of the milestone to delete
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
     * Creates a milestone asynchronously.
     * 
     * @param name the milestone name
     * @param date the milestone date
     * @param projectId the project ID
     * @param description the description
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created milestone
     */
    public CompletableFuture<Milestone> createMilestoneAsync(String name, LocalDate date, Long projectId, String description, Consumer<Milestone> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Milestone> future = new CompletableFuture<>();
        
        try {
            Milestone result = createMilestone(name, date, projectId, description);
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
     * Gets upcoming milestones asynchronously.
     * 
     * @param projectId the project ID
     * @param days the number of days ahead
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of upcoming milestones
     */
    public CompletableFuture<List<Milestone>> getUpcomingMilestonesAsync(Long projectId, int days, Consumer<List<Milestone>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Milestone>> future = new CompletableFuture<>();
        
        try {
            List<Milestone> result = getUpcomingMilestones(projectId, days);
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