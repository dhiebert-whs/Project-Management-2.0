package org.frcpm.services;

/**
 * Base service interface that defines common service operations.
 * 
 * @param <T> the entity type
 * @param <ID> the entity's ID type
 */
public interface Service<T, ID> {
    
    /**
     * Finds an entity by its ID.
     * 
     * @param id the entity ID
     * @return the entity, or null if not found
     */
    T findById(ID id);
    
    /**
     * Finds all entities.
     * 
     * @return a list of all entities
     */
    java.util.List<T> findAll();
    
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
     * Counts all entities.
     * 
     * @return the count of entities
     */
    long count();
}