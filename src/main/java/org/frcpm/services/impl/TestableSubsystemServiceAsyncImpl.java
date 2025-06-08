// src/main/java/org/frcpm/services/impl/TestableSubsystemServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.specific.SubsystemRepository;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.services.SubsystemService;
import org.frcpm.di.ServiceLocator;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable async implementation of SubsystemService that accepts injected repositories
 * for better unit testing. This implementation provides async methods that can be stubbed
 * in tests without requiring spy patterns.
 */
public class TestableSubsystemServiceAsyncImpl extends TestableSubsystemServiceImpl {
    
    private static final Logger LOGGER = Logger.getLogger(TestableSubsystemServiceAsyncImpl.class.getName());
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableSubsystemServiceAsyncImpl() {
        super();
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param subsystemRepository the subsystem repository
     * @param subteamRepository the subteam repository
     */
    public TestableSubsystemServiceAsyncImpl(
            SubsystemRepository subsystemRepository,
            SubteamRepository subteamRepository) {
        super(subsystemRepository, subteamRepository);
    }
    
    // ASYNC METHODS - These can be stubbed directly in tests without spies
    
    /**
     * Finds all subsystems asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of subsystems
     */
    public CompletableFuture<List<Subsystem>> findAllAsync(Consumer<List<Subsystem>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Subsystem>> future = new CompletableFuture<>();
        
        try {
            List<Subsystem> result = findAll();
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
     * Finds a subsystem by ID asynchronously.
     * 
     * @param id the subsystem ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the subsystem
     */
    public CompletableFuture<Subsystem> findByIdAsync(Long id, Consumer<Subsystem> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subsystem> future = new CompletableFuture<>();
        
        try {
            Subsystem result = findById(id);
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
     * Saves a subsystem asynchronously.
     * 
     * @param entity the subsystem to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved subsystem
     */
    public CompletableFuture<Subsystem> saveAsync(Subsystem entity, Consumer<Subsystem> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subsystem> future = new CompletableFuture<>();
        
        try {
            Subsystem result = save(entity);
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
     * Deletes a subsystem asynchronously.
     * 
     * @param entity the subsystem to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed when the subsystem is deleted
     */
    public CompletableFuture<Void> deleteAsync(Subsystem entity, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
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
     * Deletes a subsystem by ID asynchronously.
     * 
     * @param id the ID of the subsystem to delete
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
     * Counts all subsystems asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the count of subsystems
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
    
    /**
     * Finds a subsystem by name asynchronously.
     * 
     * @param name the name to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the subsystem
     */
    public CompletableFuture<Optional<Subsystem>> findByNameAsync(String name, Consumer<Optional<Subsystem>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Optional<Subsystem>> future = new CompletableFuture<>();
        
        try {
            Optional<Subsystem> result = findByName(name);
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
     * Finds subsystems by status asynchronously.
     * 
     * @param status the status to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of subsystems
     */
    public CompletableFuture<List<Subsystem>> findByStatusAsync(Subsystem.Status status, Consumer<List<Subsystem>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Subsystem>> future = new CompletableFuture<>();
        
        try {
            List<Subsystem> result = findByStatus(status);
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
     * Finds subsystems by responsible subteam asynchronously.
     * 
     * @param subteam the responsible subteam
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of subsystems
     */
    public CompletableFuture<List<Subsystem>> findByResponsibleSubteamAsync(Subteam subteam, Consumer<List<Subsystem>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Subsystem>> future = new CompletableFuture<>();
        
        try {
            List<Subsystem> result = findByResponsibleSubteam(subteam);
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
     * Creates a subsystem asynchronously.
     * 
     * @param name the subsystem name
     * @param description the subsystem description
     * @param status the initial status
     * @param responsibleSubteamId the responsible subteam ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created subsystem
     */
    public CompletableFuture<Subsystem> createSubsystemAsync(String name, String description, Subsystem.Status status, Long responsibleSubteamId, Consumer<Subsystem> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subsystem> future = new CompletableFuture<>();
        
        try {
            Subsystem result = createSubsystem(name, description, status, responsibleSubteamId);
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
     * Updates a subsystem's status asynchronously.
     * 
     * @param subsystemId the subsystem ID
     * @param status the new status
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated subsystem
     */
    public CompletableFuture<Subsystem> updateStatusAsync(Long subsystemId, Subsystem.Status status, Consumer<Subsystem> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subsystem> future = new CompletableFuture<>();
        
        try {
            Subsystem result = updateStatus(subsystemId, status);
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
     * Assigns a responsible subteam asynchronously.
     * 
     * @param subsystemId the subsystem ID
     * @param subteamId the subteam ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated subsystem
     */
    public CompletableFuture<Subsystem> assignResponsibleSubteamAsync(Long subsystemId, Long subteamId, Consumer<Subsystem> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subsystem> future = new CompletableFuture<>();
        
        try {
            Subsystem result = assignResponsibleSubteam(subsystemId, subteamId);
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