package org.frcpm.services.impl;

import org.frcpm.repositories.Repository;
import org.frcpm.services.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base implementation of the Service interface.
 * Provides common functionality for all service implementations.
 * 
 * @param <T> the entity type
 * @param <ID> the entity's ID type
 * @param <R> the repository type
 */
public abstract class AbstractService<T, ID, R extends Repository<T, ID>> implements Service<T, ID> {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractService.class.getName());
    
    protected final R repository;
    
    public AbstractService(R repository) {
        this.repository = repository;
    }
    
    @Override
    public T findById(ID id) {
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
            LOGGER.log(Level.SEVERE, "Error saving entity", e);
            throw new RuntimeException("Failed to save entity", e);
        }
    }
    
    @Override
    public void delete(T entity) {
        try {
            repository.delete(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting entity", e);
            throw new RuntimeException("Failed to delete entity", e);
        }
    }
    
    @Override
    public boolean deleteById(ID id) {
        try {
            return repository.deleteById(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting entity by ID", e);
            throw new RuntimeException("Failed to delete entity by ID", e);
        }
    }
    
    @Override
    public long count() {
        return repository.count();
    }
}
