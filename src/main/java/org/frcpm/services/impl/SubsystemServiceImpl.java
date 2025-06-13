// src/main/java/org/frcpm/services/impl/SubsystemServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.spring.SubsystemRepository;
import org.frcpm.repositories.spring.SubteamRepository;
import org.frcpm.services.SubsystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Spring Boot implementation of SubsystemService using composition pattern.
 * 
 * MIGRATION COMPLETE: Removed AbstractSpringService inheritance, implemented Service interface directly.
 * SUCCESS PATTERN: Following the same composition pattern proven successful in 5/6 services.
 */
@Service
@Transactional
public class SubsystemServiceImpl implements SubsystemService {
    
    private final SubsystemRepository subsystemRepository;
    private final SubteamRepository subteamRepository;
    
    @Autowired
    public SubsystemServiceImpl(SubsystemRepository subsystemRepository,
                               SubteamRepository subteamRepository) {
        this.subsystemRepository = subsystemRepository;
        this.subteamRepository = subteamRepository;
    }
    
    // ===== BASIC CRUD OPERATIONS (Service<Subsystem, Long> interface) =====
    
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
        if (entity == null) {
            throw new IllegalArgumentException("Subsystem cannot be null");
        }
        return subsystemRepository.save(entity);
    }
    
    @Override
    public void delete(Subsystem entity) {
        if (entity != null) {
            subsystemRepository.delete(entity);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id != null && subsystemRepository.existsById(id)) {
            subsystemRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Override
    public long count() {
        return subsystemRepository.count();
    }
    
    // ===== BUSINESS LOGIC METHODS (SubsystemService specific) =====
    
    @Override
    public Optional<Subsystem> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return subsystemRepository.findByName(name.trim());
    }
    
    @Override
    public List<Subsystem> findByStatus(Subsystem.Status status) {
        if (status == null) {
            return List.of();
        }
        return subsystemRepository.findByStatus(status);
    }
    
    @Override
    public List<Subsystem> findByResponsibleSubteam(Subteam subteam) {
        if (subteam == null) {
            return List.of();
        }
        return subsystemRepository.findByResponsibleSubteam(subteam);
    }
    
    @Override
    public Subsystem createSubsystem(String name, String description, 
                                   Subsystem.Status status, Long responsibleSubteamId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subsystem name cannot be null or empty");
        }
        
        if (status == null) {
            throw new IllegalArgumentException("Subsystem status cannot be null");
        }
        
        // Check if subsystem with name already exists
        if (subsystemRepository.existsByNameIgnoreCase(name.trim())) {
            throw new IllegalArgumentException("Subsystem with name '" + name + "' already exists");
        }
        
        Subsystem subsystem = new Subsystem();
        subsystem.setName(name.trim());
        subsystem.setDescription(description != null ? description.trim() : null);
        subsystem.setStatus(status);
        
        // Set responsible subteam if provided
        if (responsibleSubteamId != null) {
            Optional<Subteam> subteam = subteamRepository.findById(responsibleSubteamId);
            if (subteam.isPresent()) {
                subsystem.setResponsibleSubteam(subteam.get());
            } else {
                throw new IllegalArgumentException("Subteam with ID " + responsibleSubteamId + " not found");
            }
        }
        
        return subsystemRepository.save(subsystem);
    }
    
    @Override
    public Subsystem updateStatus(Long subsystemId, Subsystem.Status status) {
        if (subsystemId == null || status == null) {
            return null;
        }
        
        Optional<Subsystem> subsystemOpt = subsystemRepository.findById(subsystemId);
        if (subsystemOpt.isPresent()) {
            Subsystem subsystem = subsystemOpt.get();
            subsystem.setStatus(status);
            return subsystemRepository.save(subsystem);
        }
        
        return null;
    }
    
    @Override
    public Subsystem assignResponsibleSubteam(Long subsystemId, Long subteamId) {
        if (subsystemId == null || subteamId == null) {
            return null;
        }
        
        Optional<Subsystem> subsystemOpt = subsystemRepository.findById(subsystemId);
        Optional<Subteam> subteamOpt = subteamRepository.findById(subteamId);
        
        if (subsystemOpt.isPresent() && subteamOpt.isPresent()) {
            Subsystem subsystem = subsystemOpt.get();
            subsystem.setResponsibleSubteam(subteamOpt.get());
            return subsystemRepository.save(subsystem);
        }
        
        return null;
    }
    
    // ===== ASYNC METHODS (Spring Boot @Async support) =====
    
    @Async
    @Override
    public CompletableFuture<Optional<Subsystem>> findByNameAsync(String name,
                                                              Consumer<Optional<Subsystem>> onSuccess,
                                                              Consumer<Throwable> onFailure) {
        try {
            Optional<Subsystem> result = findByName(name);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Override
    public CompletableFuture<List<Subsystem>> findByStatusAsync(Subsystem.Status status,
                                                            Consumer<List<Subsystem>> onSuccess,
                                                            Consumer<Throwable> onFailure) {
        try {
            List<Subsystem> result = findByStatus(status);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Override
    public CompletableFuture<List<Subsystem>> findByResponsibleSubteamAsync(Subteam subteam,
                                                                        Consumer<List<Subsystem>> onSuccess,
                                                                        Consumer<Throwable> onFailure) {
        try {
            List<Subsystem> result = findByResponsibleSubteam(subteam);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Override
    public CompletableFuture<Subsystem> createSubsystemAsync(String name, String description,
                                                         Subsystem.Status status, Long responsibleSubteamId,
                                                         Consumer<Subsystem> onSuccess,
                                                         Consumer<Throwable> onFailure) {
        try {
            Subsystem result = createSubsystem(name, description, status, responsibleSubteamId);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Override
    public CompletableFuture<Subsystem> updateStatusAsync(Long subsystemId, Subsystem.Status status,
                                                      Consumer<Subsystem> onSuccess,
                                                      Consumer<Throwable> onFailure) {
        try {
            Subsystem result = updateStatus(subsystemId, status);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Override
    public CompletableFuture<Subsystem> assignResponsibleSubteamAsync(Long subsystemId, Long subteamId,
                                                                  Consumer<Subsystem> onSuccess,
                                                                  Consumer<Throwable> onFailure) {
        try {
            Subsystem result = assignResponsibleSubteam(subsystemId, subteamId);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Override
    public CompletableFuture<Subsystem> findByIdAsync(Long id,
                                                  Consumer<Subsystem> onSuccess,
                                                  Consumer<Throwable> onFailure) {
        try {
            Subsystem result = findById(id);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Override
    public CompletableFuture<List<Subsystem>> findAllAsync(
        Consumer<List<Subsystem>> onSuccess, 
        Consumer<Throwable> onFailure) {
        try {
            List<Subsystem> result = findAll();
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Override
    public CompletableFuture<Subsystem> saveAsync(Subsystem subsystem,
                                              Consumer<Subsystem> onSuccess,
                                              Consumer<Throwable> onFailure) {
        try {
            Subsystem result = save(subsystem);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Override
    public CompletableFuture<Void> deleteAsync(Subsystem subsystem,
                                          Consumer<Void> onSuccess,
                                          Consumer<Throwable> onFailure) {
        try {
            delete(subsystem);
            if (onSuccess != null) {
                onSuccess.accept(null);
            }
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(Long id,
                                                  Consumer<Boolean> onSuccess,
                                                  Consumer<Throwable> onFailure) {
        try {
            boolean result = deleteById(id);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
}