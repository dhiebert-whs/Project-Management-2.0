// src/main/java/org/frcpm/services/SubteamService.java
package org.frcpm.services;

import org.frcpm.models.Subteam;
//import org.frcpm.models.TeamMember;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service interface for Subteam entity.
 */
public interface SubteamService extends Service<Subteam, Long> {
    
    /**
     * Finds a subteam by name.
     * 
     * @param name the name to search for
     * @return an Optional containing the found subteam, or empty if not found
     */
    Optional<Subteam> findByName(String name);
    
    /**
     * Finds subteams by specialty.
     * 
     * @param specialty the specialty to search for
     * @return a list of subteams with matching specialties
     */
    List<Subteam> findBySpecialty(String specialty);
    
    /**
     * Creates a new subteam.
     * 
     * @param name the subteam name
     * @param colorCode the color code for UI display
     * @param specialties the subteam specialties (optional)
     * @return the created subteam
     */
    Subteam createSubteam(String name, String colorCode, String specialties);
    
    /**
     * Updates a subteam's specialties.
     * 
     * @param subteamId the subteam ID
     * @param specialties the new specialties
     * @return the updated subteam, or null if not found
     */
    Subteam updateSpecialties(Long subteamId, String specialties);
    
    /**
     * Updates a subteam's color code.
     * 
     * @param subteamId the subteam ID
     * @param colorCode the new color code
     * @return the updated subteam, or null if not found
     */
    Subteam updateColorCode(Long subteamId, String colorCode);
    
    /**
     * Finds an entity by its ID asynchronously.
     * 
     * @param id the entity ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the entity
     */
    default CompletableFuture<Subteam> findByIdAsync(Long id, Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subteam> future = new CompletableFuture<>();
        try {
            Subteam result = findById(id);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Finds all entities asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of entities
     */
    default CompletableFuture<List<Subteam>> findAllAsync(Consumer<List<Subteam>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Subteam>> future = new CompletableFuture<>();
        try {
            List<Subteam> result = findAll();
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Saves an entity asynchronously.
     * 
     * @param entity the entity to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved entity
     */
    default CompletableFuture<Subteam> saveAsync(Subteam entity, Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subteam> future = new CompletableFuture<>();
        try {
            Subteam result = save(entity);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Deletes an entity by its ID asynchronously.
     * 
     * @param id the ID of the entity to delete
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
     * Creates a subteam asynchronously.
     * 
     * @param name the subteam name
     * @param colorCode the color code for UI display
     * @param specialties the subteam specialties
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created subteam
     */
    default CompletableFuture<Subteam> createSubteamAsync(String name, String colorCode, String specialties, 
                                                         Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subteam> future = new CompletableFuture<>();
        try {
            Subteam result = createSubteam(name, colorCode, specialties);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
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
    default CompletableFuture<Subteam> updateSpecialtiesAsync(Long subteamId, String specialties, 
                                                            Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subteam> future = new CompletableFuture<>();
        try {
            Subteam result = updateSpecialties(subteamId, specialties);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
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
    default CompletableFuture<Subteam> updateColorCodeAsync(Long subteamId, String colorCode, 
                                                          Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Subteam> future = new CompletableFuture<>();
        try {
            Subteam result = updateColorCode(subteamId, colorCode);
            if (onSuccess != null) onSuccess.accept(result);
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) onFailure.accept(e);
            future.completeExceptionally(e);
        }
        return future;
    }
}