// src/main/java/org/frcpm/services/impl/TestableProjectServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Project;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.ProjectService;
import org.frcpm.di.ServiceLocator;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Testable implementation of ProjectService that provides async functionality
 * but allows constructor injection for testing.
 */
public class TestableProjectServiceAsyncImpl extends TestableProjectServiceImpl {
    
    private static final Logger LOGGER = Logger.getLogger(TestableProjectServiceAsyncImpl.class.getName());
    
    /**
     * Constructor for testing with injected repositories.
     * 
     * @param projectRepository the project repository
     * @param taskRepository the task repository
     */
    public TestableProjectServiceAsyncImpl(ProjectRepository projectRepository, TaskRepository taskRepository) {
        super(projectRepository, taskRepository);
    }
    
    /**
     * Default constructor that uses ServiceLocator (for compatibility).
     */
    public TestableProjectServiceAsyncImpl() {
        super();
    }
    
    // Async method implementations
    
    /**
     * Finds all projects asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of projects
     */
    public CompletableFuture<List<Project>> findAllAsync(Consumer<List<Project>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Project>> future = new CompletableFuture<>();
        
        try {
            List<Project> result = findAll();
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
     * Finds a project by ID asynchronously.
     * 
     * @param id the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the project
     */
    public CompletableFuture<Project> findByIdAsync(Long id, Consumer<Project> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Project> future = new CompletableFuture<>();
        
        try {
            Project result = findById(id);
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
     * Saves a project asynchronously.
     * 
     * @param entity the project to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved project
     */
    public CompletableFuture<Project> saveAsync(Project entity, Consumer<Project> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Project> future = new CompletableFuture<>();
        
        try {
            Project result = save(entity);
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
     * Deletes a project by ID asynchronously.
     * 
     * @param id the ID of the project to delete
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
     * Deletes a project asynchronously.
     * 
     * @param entity the project to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed when the project is deleted
     */
    public CompletableFuture<Void> deleteAsync(Project entity, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
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
     * Counts all projects asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the count of projects
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