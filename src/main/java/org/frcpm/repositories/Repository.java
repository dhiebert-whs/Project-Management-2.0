package org.frcpm.repositories;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface for data access operations.
 * 
 * @param <T> the entity type
 * @param <ID> the entity's ID type
 */
public interface Repository<T, ID> {
    
    /**
     * Finds an entity by its ID.
     * 
     * @param id the entity ID
     * @return an Optional containing the found entity, or empty if not found
     */
    Optional<T> findById(ID id);
    
    /**
     * Finds all entities of the type.
     * 
     * @return a list of all entities
     */
    List<T> findAll();
    
    /**
     * Saves an entity (creates new or updates existing).
     * 
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);
    
    /**
     * Deletes an entity.
     * 
     * @param entity the entity to delete
     */
    void delete(T entity);
    
    /**
     * Deletes an entity by its ID.
     * 
     * @param id the ID of the entity to delete
     * @return true if an entity was deleted, false otherwise
     */
    boolean deleteById(ID id);
    
    /**
     * Counts all entities of the type.
     * 
     * @return the count of entities
     */
    long count();
}