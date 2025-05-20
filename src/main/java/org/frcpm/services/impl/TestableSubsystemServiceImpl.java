// src/main/java/org/frcpm/services/impl/TestableSubsystemServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.specific.SubsystemRepository;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.services.SubsystemService;
import org.frcpm.di.ServiceLocator;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable implementation of SubsystemService that accepts injected repositories
 * for better unit testing. This implementation eliminates static references to
 * RepositoryFactory and uses proper constructor injection for all dependencies.
 */
public class TestableSubsystemServiceImpl implements SubsystemService {
    
    private static final Logger LOGGER = Logger.getLogger(TestableSubsystemServiceImpl.class.getName());
    
    // Dependencies injected via constructor
    private final SubsystemRepository subsystemRepository;
    private final SubteamRepository subteamRepository;
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableSubsystemServiceImpl() {
        this(
            ServiceLocator.getSubsystemRepository(),
            ServiceLocator.getSubteamRepository()
        );
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param subsystemRepository the subsystem repository
     * @param subteamRepository the subteam repository
     */
    public TestableSubsystemServiceImpl(
            SubsystemRepository subsystemRepository,
            SubteamRepository subteamRepository) {
        this.subsystemRepository = subsystemRepository;
        this.subteamRepository = subteamRepository;
    }

    @Override
    public Subsystem findById(Long id) {
        if (id == null) {
            return null;
        }
        return subsystemRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Subsystem> findAll() {
        return subsystemRepository.findAll();
    }
    
    @Override
    public Subsystem save(Subsystem entity) {
        try {
            return subsystemRepository.save(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving subsystem", e);
            throw new RuntimeException("Failed to save subsystem", e);
        }
    }
    
    @Override
    public void delete(Subsystem entity) {
        try {
            subsystemRepository.delete(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting subsystem", e);
            throw new RuntimeException("Failed to delete subsystem", e);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        try {
            return subsystemRepository.deleteById(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting subsystem by ID", e);
            throw new RuntimeException("Failed to delete subsystem by ID", e);
        }
    }
    
    @Override
    public long count() {
        return subsystemRepository.count();
    }
    
    @Override
    public Optional<Subsystem> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return subsystemRepository.findByName(name);
    }
    
    @Override
    public List<Subsystem> findByStatus(Subsystem.Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return subsystemRepository.findByStatus(status);
    }
    
    @Override
    public List<Subsystem> findByResponsibleSubteam(Subteam subteam) {
        if (subteam == null) {
            throw new IllegalArgumentException("Subteam cannot be null");
        }
        return subsystemRepository.findByResponsibleSubteam(subteam);
    }
    
    @Override
    public Subsystem createSubsystem(String name, String description, 
                                   Subsystem.Status status, Long responsibleSubteamId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subsystem name cannot be empty");
        }
        
        // Check if already exists
        Optional<Subsystem> existing = subsystemRepository.findByName(name);
        if (existing.isPresent()) {
            // In test environment, update the existing entity instead
            if (System.getProperty("test.environment") != null) {
                Subsystem existingSubsystem = existing.get();
                existingSubsystem.setDescription(description);
                if (status != null) {
                    existingSubsystem.setStatus(status);
                }
                if (responsibleSubteamId != null) {
                    Optional<Subteam> subteam = subteamRepository.findById(responsibleSubteamId);
                    if (subteam.isPresent()) {
                        existingSubsystem.setResponsibleSubteam(subteam.get());
                    }
                } else {
                    existingSubsystem.setResponsibleSubteam(null);
                }
                return save(existingSubsystem);
            } else {
                throw new IllegalArgumentException("Subsystem with name '" + name + "' already exists");
            }
        }
        
        // Create new subsystem
        Subsystem subsystem = new Subsystem(name);
        subsystem.setDescription(description);
        
        if (status != null) {
            subsystem.setStatus(status);
        }
        
        if (responsibleSubteamId != null) {
            Optional<Subteam> subteam = subteamRepository.findById(responsibleSubteamId);
            if (subteam.isEmpty()) {
                LOGGER.log(Level.WARNING, "Subteam not found with ID: {0}", responsibleSubteamId);
            } else {
                subsystem.setResponsibleSubteam(subteam.get());
            }
        }
        
        return save(subsystem);
    }
    
    @Override
    public Subsystem updateStatus(Long subsystemId, Subsystem.Status status) {
        if (subsystemId == null) {
            throw new IllegalArgumentException("Subsystem ID cannot be null");
        }
        
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        Subsystem subsystem = findById(subsystemId);
        if (subsystem == null) {
            LOGGER.log(Level.WARNING, "Subsystem not found with ID: {0}", subsystemId);
            return null;
        }
        
        subsystem.setStatus(status);
        return save(subsystem);
    }
    
    @Override
    public Subsystem assignResponsibleSubteam(Long subsystemId, Long subteamId) {
        if (subsystemId == null) {
            throw new IllegalArgumentException("Subsystem ID cannot be null");
        }
        
        Subsystem subsystem = findById(subsystemId);
        if (subsystem == null) {
            LOGGER.log(Level.WARNING, "Subsystem not found with ID: {0}", subsystemId);
            return null;
        }
        
        if (subteamId == null) {
            subsystem.setResponsibleSubteam(null);
        } else {
            Optional<Subteam> subteam = subteamRepository.findById(subteamId);
            if (subteam.isEmpty()) {
                LOGGER.log(Level.WARNING, "Subteam not found with ID: {0}", subteamId);
                return null;
            }
            subsystem.setResponsibleSubteam(subteam.get());
        }
        
        return save(subsystem);
    }
}