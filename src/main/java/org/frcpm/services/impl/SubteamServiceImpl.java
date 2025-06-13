// src/main/java/org/frcpm/services/impl/SubteamServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.repositories.spring.SubteamRepository;
import org.frcpm.repositories.spring.TeamMemberRepository;
import org.frcpm.services.SubteamService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Spring Boot implementation of SubteamService using composition pattern.
 * Eliminates AbstractSpringService inheritance to resolve compilation errors.
 */
@Service
@Transactional
public class SubteamServiceImpl implements SubteamService {
    
    private final SubteamRepository subteamRepository;
    private final TeamMemberRepository teamMemberRepository;
    
    /**
     * Constructor injection for repositories.
     * No @Autowired needed with single constructor.
     */
    public SubteamServiceImpl(SubteamRepository subteamRepository,
                             TeamMemberRepository teamMemberRepository) {
        this.subteamRepository = subteamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }
    
    // =========================================================================
    // BASIC CRUD OPERATIONS - Implementing Service<Subteam, Long> interface
    // =========================================================================
    
    @Override
    public Subteam findById(Long id) {
        if (id == null) {
            return null;
        }
        return subteamRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Subteam> findAll() {
        return subteamRepository.findAll();
    }
    
    @Override
    public Subteam save(Subteam entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Subteam cannot be null");
        }
        return subteamRepository.save(entity);
    }
    
    @Override
    public void delete(Subteam entity) {
        if (entity != null) {
            subteamRepository.delete(entity);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id != null && subteamRepository.existsById(id)) {
            subteamRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Override
    public long count() {
        return subteamRepository.count();
    }
    
    // =========================================================================
    // BUSINESS LOGIC METHODS - SubteamService specific methods
    // =========================================================================
    
    @Override
    public Optional<Subteam> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return subteamRepository.findByNameIgnoreCase(name.trim());
    }
    
    @Override
    public List<Subteam> findBySpecialty(String specialty) {
        if (specialty == null || specialty.trim().isEmpty()) {
            return List.of();
        }
        return subteamRepository.findBySpecialtiesContainingIgnoreCase(specialty.trim());
    }
    
    @Override
    public Subteam createSubteam(String name, String colorCode, String specialties) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subteam name cannot be null or empty");
        }
        if (colorCode == null || colorCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Color code cannot be null or empty");
        }
        
        // Check if subteam with this name already exists
        if (subteamRepository.existsByNameIgnoreCase(name.trim())) {
            throw new IllegalArgumentException("Subteam with name '" + name.trim() + "' already exists");
        }
        
        // Create the subteam
        Subteam subteam = new Subteam();
        subteam.setName(name.trim());
        subteam.setColorCode(colorCode.trim());
        subteam.setSpecialties(specialties != null ? specialties.trim() : null);
        
        return subteamRepository.save(subteam);
    }
    
    @Override
    public Subteam updateSpecialties(Long subteamId, String specialties) {
        if (subteamId == null) {
            throw new IllegalArgumentException("Subteam ID cannot be null");
        }
        
        Subteam subteam = subteamRepository.findById(subteamId).orElse(null);
        if (subteam == null) {
            return null;
        }
        
        subteam.setSpecialties(specialties != null ? specialties.trim() : null);
        return subteamRepository.save(subteam);
    }
    
    @Override
    public Subteam updateColorCode(Long subteamId, String colorCode) {
        if (subteamId == null) {
            throw new IllegalArgumentException("Subteam ID cannot be null");
        }
        if (colorCode == null || colorCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Color code cannot be null or empty");
        }
        
        Subteam subteam = subteamRepository.findById(subteamId).orElse(null);
        if (subteam == null) {
            return null;
        }
        
        subteam.setColorCode(colorCode.trim());
        return subteamRepository.save(subteam);
    }
    
    // =========================================================================
    // ASYNC METHODS - Using @Async annotation with CompletableFuture
    // Following the exact pattern from SubteamService interface
    // =========================================================================
    
    @Async
    @Override
    public CompletableFuture<Subteam> findByIdAsync(
            Long id, 
            Consumer<Subteam> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Subteam result = findById(id);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<List<Subteam>> findAllAsync(
            Consumer<List<Subteam>> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Subteam> result = findAll();
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Subteam> saveAsync(
            Subteam entity, 
            Consumer<Subteam> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Subteam result = save(entity);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(
            Long id, 
            Consumer<Boolean> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Boolean result = deleteById(id);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Subteam> createSubteamAsync(
            String name, 
            String colorCode, 
            String specialties, 
            Consumer<Subteam> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Subteam result = createSubteam(name, colorCode, specialties);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Subteam> updateSpecialtiesAsync(
            Long subteamId, 
            String specialties, 
            Consumer<Subteam> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Subteam result = updateSpecialties(subteamId, specialties);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Subteam> updateColorCodeAsync(
            Long subteamId, 
            String colorCode, 
            Consumer<Subteam> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Subteam result = updateColorCode(subteamId, colorCode);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
}