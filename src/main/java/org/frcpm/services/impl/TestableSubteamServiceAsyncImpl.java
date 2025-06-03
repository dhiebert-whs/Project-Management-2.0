// src/main/java/org/frcpm/services/impl/TestableSubteamServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.services.SubteamService;
import org.frcpm.di.ServiceLocator;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable async implementation of SubteamService that accepts injected repositories
 * for better unit testing. This implementation provides async methods that can be stubbed
 * in tests without requiring spy patterns.
 */
public class TestableSubteamServiceAsyncImpl extends TestableSubteamServiceImpl {
    
    private static final Logger LOGGER = Logger.getLogger(TestableSubteamServiceAsyncImpl.class.getName());
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableSubteamServiceAsyncImpl() {
        super();
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param subteamRepository the subteam repository
     */
    public TestableSubteamServiceAsyncImpl(SubteamRepository subteamRepository) {
        super(subteamRepository);
    }
    
    // ASYNC METHODS - These can be stubbed directly in tests without spies
    
    /**
     * Finds all subteams asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of subteams
     */
    public CompletableFuture<List<Subteam>> findAllAsync(Consumer<List<Subteam>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Subteam>> future = new CompletableFuture<>();
        
        try {
            List<Subteam> result = findAll();
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
     * Finds a subteam by ID asynchronously.
     * 
     * @param id the subteam ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the subteam
     */
    public CompletableFuture<Subteam> findByIdAsync(Long id, Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subteam> future = new CompletableFuture<>();
        
        try {
            Subteam result = findById(id);
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
     * Saves a subteam asynchronously.
     * 
     * @param entity the subteam to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved subteam
     */
    public CompletableFuture<Subteam> saveAsync(Subteam entity, Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subteam> future = new CompletableFuture<>();
        
        try {
            Subteam result = save(entity);
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
     * Deletes a subteam asynchronously.
     * 
     * @param entity the subteam to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed when the subteam is deleted
     */
    public CompletableFuture<Void> deleteAsync(Subteam entity, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
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
     * Deletes a subteam by ID asynchronously.
     * 
     * @param id the ID of the subteam to delete
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
     * Counts all subteams asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the count of subteams
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
     * Finds a subteam by name asynchronously.
     * 
     * @param name the name to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the subteam
     */
    public CompletableFuture<Optional<Subteam>> findByNameAsync(String name, Consumer<Optional<Subteam>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Optional<Subteam>> future = new CompletableFuture<>();
        
        try {
            Optional<Subteam> result = findByName(name);
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
     * Finds subteams by specialty asynchronously.
     * 
     * @param specialty the specialty to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of subteams
     */
    public CompletableFuture<List<Subteam>> findBySpecialtyAsync(String specialty, Consumer<List<Subteam>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Subteam>> future = new CompletableFuture<>();
        
        try {
            List<Subteam> result = findBySpecialty(specialty);
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
     * Creates a subteam asynchronously.
     * 
     * @param name the subteam name
     * @param colorCode the color code
     * @param specialties the specialties
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created subteam
     */
    public CompletableFuture<Subteam> createSubteamAsync(String name, String colorCode, String specialties, Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subteam> future = new CompletableFuture<>();
        
        try {
            Subteam result = createSubteam(name, colorCode, specialties);
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
     * Updates a subteam's specialties asynchronously.
     * 
     * @param subteamId the subteam ID
     * @param specialties the new specialties
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated subteam
     */
    public CompletableFuture<Subteam> updateSpecialtiesAsync(Long subteamId, String specialties, Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subteam> future = new CompletableFuture<>();
        
        try {
            Subteam result = updateSpecialties(subteamId, specialties);
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
     * Updates a subteam's color code asynchronously.
     * 
     * @param subteamId the subteam ID
     * @param colorCode the new color code
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated subteam
     */
    public CompletableFuture<Subteam> updateColorCodeAsync(Long subteamId, String colorCode, Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subteam> future = new CompletableFuture<>();
        
        try {
            Subteam result = updateColorCode(subteamId, colorCode);
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