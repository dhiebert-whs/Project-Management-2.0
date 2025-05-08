// src/main/java/org/frcpm/services/impl/SubteamServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.services.SubteamService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Asynchronous implementation of SubteamService.
 * Provides async methods for subteam operations.
 */
public class SubteamServiceAsyncImpl extends AbstractAsyncService<Subteam, Long, SubteamRepository> 
        implements SubteamService {
    
    private static final Logger LOGGER = Logger.getLogger(SubteamServiceAsyncImpl.class.getName());
    
    public SubteamServiceAsyncImpl() {
        super(RepositoryFactory.getSubteamRepository());
    }
    
    @Override
    public Optional<Subteam> findByName(String name) {
        return repository.findByName(name);
    }
    
    @Override
    public List<Subteam> findBySpecialty(String specialty) {
        return repository.findBySpecialty(specialty);
    }
    
    @Override
    public Subteam createSubteam(String name, String colorCode, String specialties) {
        validateSubteamParams(name, colorCode);
        
        if (repository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Subteam with name '" + name + "' already exists");
        }
        
        Subteam subteam = new Subteam(name, colorCode);
        subteam.setSpecialties(specialties);
        
        return save(subteam);
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
    public CompletableFuture<Subteam> createSubteamAsync(String name, String colorCode, String specialties,
                                                   Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        try {
            validateSubteamParams(name, colorCode);
            
            return executeAsync("Create Subteam", em -> {
                // Check if subteam with name already exists
                Optional<Subteam> existing = repository.findByName(name);
                if (existing.isPresent()) {
                    throw new IllegalArgumentException("Subteam with name '" + name + "' already exists");
                }
                
                Subteam subteam = new Subteam(name, colorCode);
                subteam.setSpecialties(specialties);
                
                return repository.save(subteam);
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Subteam> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Override
    public Subteam updateSpecialties(Long subteamId, String specialties) {
        if (subteamId == null) {
            throw new IllegalArgumentException("Subteam ID cannot be null");
        }
        
        Subteam subteam = findById(subteamId);
        if (subteam == null) {
            LOGGER.log(Level.WARNING, "Subteam not found with ID: {0}", subteamId);
            return null;
        }
        
        subteam.setSpecialties(specialties);
        return save(subteam);
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
    public CompletableFuture<Subteam> updateSpecialtiesAsync(Long subteamId, String specialties,
                                                      Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        if (subteamId == null) {
            CompletableFuture<Subteam> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Subteam ID cannot be null"));
            return future;
        }
        
        return executeAsync("Update Subteam Specialties: " + subteamId, em -> {
            Subteam subteam = em.find(Subteam.class, subteamId);
            if (subteam == null) {
                LOGGER.log(Level.WARNING, "Subteam not found with ID: {0}", subteamId);
                throw new IllegalArgumentException("Subteam not found with ID: " + subteamId);
            }
            
            subteam.setSpecialties(specialties);
            return em.merge(subteam);
        }, onSuccess, onFailure);
    }
    
    @Override
    public Subteam updateColorCode(Long subteamId, String colorCode) {
        if (subteamId == null) {
            throw new IllegalArgumentException("Subteam ID cannot be null");
        }
        
        validateColorCode(colorCode);
        
        Subteam subteam = findById(subteamId);
        if (subteam == null) {
            LOGGER.log(Level.WARNING, "Subteam not found with ID: {0}", subteamId);
            return null;
        }
        
        subteam.setColorCode(colorCode);
        return save(subteam);
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
    public CompletableFuture<Subteam> updateColorCodeAsync(Long subteamId, String colorCode,
                                                    Consumer<Subteam> onSuccess, Consumer<Throwable> onFailure) {
        try {
            if (subteamId == null) {
                throw new IllegalArgumentException("Subteam ID cannot be null");
            }
            
            validateColorCode(colorCode);
            
            return executeAsync("Update Subteam Color Code: " + subteamId, em -> {
                Subteam subteam = em.find(Subteam.class, subteamId);
                if (subteam == null) {
                    LOGGER.log(Level.WARNING, "Subteam not found with ID: {0}", subteamId);
                    throw new IllegalArgumentException("Subteam not found with ID: " + subteamId);
                }
                
                subteam.setColorCode(colorCode);
                return em.merge(subteam);
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Subteam> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Validates subteam parameters.
     * 
     * @param name the subteam name
     * @param colorCode the color code
     */
    private void validateSubteamParams(String name, String colorCode) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subteam name cannot be empty");
        }
        
        validateColorCode(colorCode);
    }
    
    /**
     * Validates a color code.
     * 
     * @param colorCode the color code to validate
     */
    private void validateColorCode(String colorCode) {
        if (colorCode == null || !colorCode.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("Color code must be a valid hex color code");
        }
    }
}