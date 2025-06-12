// src/main/java/org/frcpm/services/impl/AbstractSpringService.java

package org.frcpm.services.impl;

import org.frcpm.services.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base class for Spring Boot service implementations.
 * Provides common CRUD operations using Spring Data JPA repositories.
 * 
 * FIXED: Removed generic R parameter that was incompatible with Spring DI.
 * Now uses JpaRepository<T, ID> directly which Spring can inject properly.
 * 
 * @param <T> the entity type
 * @param <ID> the entity's ID type
 */
public abstract class AbstractSpringService<T, ID> implements Service<T, ID> {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractSpringService.class.getName());
    
    // FIXED: Direct interface type instead of generic R parameter
    protected final JpaRepository<T, ID> repository;
    
    // FIXED: Constructor accepts JpaRepository<T, ID> which Spring can inject
    public AbstractSpringService(JpaRepository<T, ID> repository) {
        this.repository = repository;
    }
    
    @Override
    public T findById(ID id) {
        if (id == null) {
            return null;
        }
        return repository.findById(id).orElse(null);
    }
    
    @Override
    public List<T> findAll() {
        return repository.findAll();
    }
    
    @Override
    public T save(T entity) {
        try {
            return repository.save(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving " + getEntityName(), e);
            throw new RuntimeException("Failed to save " + getEntityName(), e);
        }
    }
    
    @Override
    public void delete(T entity) {
        try {
            repository.delete(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting " + getEntityName(), e);
            throw new RuntimeException("Failed to delete " + getEntityName(), e);
        }
    }
    
    @Override
    public boolean deleteById(ID id) {
        try {
            if (id == null || !repository.existsById(id)) {
                return false;
            }
            repository.deleteById(id);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting " + getEntityName() + " by ID", e);
            throw new RuntimeException("Failed to delete " + getEntityName() + " by ID", e);
        }
    }
    
    @Override
    public long count() {
        return repository.count();
    }
    
    /**
     * Gets the entity name for logging purposes.
     * Subclasses can override this to provide specific entity names.
     * 
     * @return the entity name
     */
    protected String getEntityName() {
        return "entity";
    }
}