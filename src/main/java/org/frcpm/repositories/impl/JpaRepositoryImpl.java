// src/main/java/org/frcpm/repositories/impl/JpaRepositoryImpl.java (Enhancement)
package org.frcpm.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.frcpm.config.DatabaseConfig;
import org.frcpm.db.DatabaseConfigurer;
import org.frcpm.db.DatabaseManager;
import org.frcpm.repositories.Repository;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced base implementation of the Repository interface using JPA.
 * Includes improved transaction management, error handling, and connection pooling.
 * 
 * @param <T> the entity type
 * @param <ID> the entity's ID type
 */
public abstract class JpaRepositoryImpl<T, ID> implements Repository<T, ID> {
    
    private static final Logger LOGGER = Logger.getLogger(JpaRepositoryImpl.class.getName());
    
    // Maximum number of retries for transaction errors
    private static final int MAX_TRANSACTION_RETRIES = 3;
    
    // Delay between transaction retries in milliseconds
    private static final long RETRY_DELAY_MS = 200;
    
    protected final Class<T> entityClass;
    
    @SuppressWarnings("unchecked")
    public JpaRepositoryImpl() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }
    
    /**
     * Gets an EntityManager from the configured source.
     * 
     * @return the entity manager
     */
    protected EntityManager getEntityManager() {
        return DatabaseConfig.getEntityManager();
    }
    
    @Override
    public Optional<T> findById(ID id) {
        EntityManager em = getEntityManager();
        try {
            T entity = em.find(entityClass, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding entity by ID", e);
            return Optional.empty();
        } finally {
            closeEntityManager(em);
        }
    }
    
    @Override
    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);
            TypedQuery<T> query = em.createQuery(cq);
            List<T> resultList = query.getResultList();
            // Ensure we're not returning null
            return resultList != null ? resultList : List.of();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding all entities", e);
            return List.of();
        } finally {
            closeEntityManager(em);
        }
    }
    
    @Override
    public T save(T entity) {
        // Use transaction retry logic for persistence operations
        return executeWithRetry(() -> {
            EntityManager em = getEntityManager();
            try {
                em.getTransaction().begin();
                
                T managedEntity;
                if (isNew(entity)) {
                    em.persist(entity);
                    managedEntity = entity;
                } else {
                    managedEntity = em.merge(entity);
                }
                
                em.flush(); // Force SQL execution to detect any errors
                em.getTransaction().commit();
                return managedEntity;
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e; // Re-throw for retry mechanism
            } finally {
                closeEntityManager(em);
            }
        }, "save entity");
    }
    
    /**
     * Determines if an entity is new (not yet persisted).
     * 
     * @param entity the entity to check
     * @return true if the entity is new, false otherwise
     */
    protected boolean isNew(T entity) {
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                    field.setAccessible(true);
                    Object id = field.get(entity);
                    return id == null || (id instanceof Number && ((Number) id).longValue() == 0);
                }
            }
            
            // Check superclass if no ID field found in declared fields
            Class<?> superClass = entity.getClass().getSuperclass();
            while (superClass != null && !superClass.equals(Object.class)) {
                for (Field field : superClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                        field.setAccessible(true);
                        Object id = field.get(entity);
                        return id == null || (id instanceof Number && ((Number) id).longValue() == 0);
                    }
                }
                superClass = superClass.getSuperclass();
            }
            
            // If we can't determine, assume it's new
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not determine if entity is new", e);
            return true;
        }
    }

    // Helper method to find the ID field
    private Field getIdField(Class<?> clazz) {
        // Check current class
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                return field;
            }
        }
        
        // Check superclass if exists
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            return getIdField(clazz.getSuperclass());
        }
        
        throw new IllegalArgumentException("No ID field found in class " + clazz.getName());
    }
    
    @Override
    public void delete(T entity) {
        // Use transaction retry logic for delete operations
        executeWithRetry(() -> {
            EntityManager em = getEntityManager();
            try {
                em.getTransaction().begin();
                
                // Enhanced cascading deletion with pre-deletion hooks for specific entity types
                if (entityClass.equals(org.frcpm.models.Project.class)) {
                    handleProjectDeletion(entity, em);
                } else if (entityClass.equals(org.frcpm.models.Task.class)) {
                    handleTaskDeletion(entity, em);
                } else if (entityClass.equals(org.frcpm.models.Component.class)) {
                    handleComponentDeletion(entity, em);
                } else if (entityClass.equals(org.frcpm.models.TeamMember.class)) {
                    handleTeamMemberDeletion(entity, em);
                } else {
                    // Default deletion behavior
                    T managedEntity = em.contains(entity) ? entity : em.merge(entity);
                    em.remove(managedEntity);
                }
                
                em.flush(); // Force SQL execution to detect any errors
                em.getTransaction().commit();
                return null; // No return value needed for delete
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e; // Re-throw for retry mechanism
            } finally {
                closeEntityManager(em);
            }
        }, "delete entity");
    }

    /**
     * Handles the deletion of a Project entity with proper cascade operations.
     * 
     * @param entity the Project entity to delete
     * @param em the EntityManager
     */
    private void handleProjectDeletion(T entity, EntityManager em) {
        Long entityId = getEntityId(entity);
        if (entityId != null) {
            // Delete related tasks first
            em.createQuery("SELECT t FROM Task t WHERE t.project.id = :projectId")
              .setParameter("projectId", entityId)
              .getResultList()
              .forEach(task -> handleTaskDeletion((T) task, em));
            
            // Delete related milestones
            em.createQuery("DELETE FROM Milestone m WHERE m.project.id = :projectId")
              .setParameter("projectId", entityId)
              .executeUpdate();
            
            // Delete related meetings
            em.createQuery("DELETE FROM Meeting m WHERE m.project.id = :projectId")
              .setParameter("projectId", entityId)
              .executeUpdate();
        }
        
        // Now delete the project
        T managedEntity = em.contains(entity) ? entity : em.merge(entity);
        em.remove(managedEntity);
    }

    /**
     * Handles the deletion of a Task entity with proper relationship cleaning.
     * 
     * @param entity the Task entity to delete
     * @param em the EntityManager
     */
    private void handleTaskDeletion(T entity, EntityManager em) {
        Long entityId = getEntityId(entity);
        if (entityId != null) {
            // Remove dependencies
            em.createNativeQuery(
                "DELETE FROM task_dependencies WHERE task_id = :taskId OR dependency_id = :taskId")
            .setParameter("taskId", entityId)
            .executeUpdate();
            
            // Remove component relationships
            em.createNativeQuery(
                "DELETE FROM task_components WHERE task_id = :taskId")
            .setParameter("taskId", entityId)
            .executeUpdate();
            
            // Remove assignments
            em.createNativeQuery(
                "DELETE FROM task_assignments WHERE task_id = :taskId")
            .setParameter("taskId", entityId)
            .executeUpdate();
        }
        
        // Now delete the task
        T managedEntity = em.contains(entity) ? entity : em.merge(entity);
        em.remove(managedEntity);
    }

    /**
     * Handles the deletion of a Component entity with proper relationship cleaning.
     * 
     * @param entity the Component entity to delete
     * @param em the EntityManager
     */
    private void handleComponentDeletion(T entity, EntityManager em) {
        Long entityId = getEntityId(entity);
        if (entityId != null) {
            // Remove component references from tasks
            em.createNativeQuery(
                "DELETE FROM task_components WHERE component_id = :componentId")
            .setParameter("componentId", entityId)
            .executeUpdate();
        }
        
        // Now delete the component
        T managedEntity = em.contains(entity) ? entity : em.merge(entity);
        em.remove(managedEntity);
    }

    /**
     * Handles the deletion of a TeamMember entity with proper relationship cleaning.
     * 
     * @param entity the TeamMember entity to delete
     * @param em the EntityManager
     */
    private void handleTeamMemberDeletion(T entity, EntityManager em) {
        Long entityId = getEntityId(entity);
        if (entityId != null) {
            // Remove task assignments
            em.createNativeQuery(
                "DELETE FROM task_assignments WHERE team_member_id = :memberId")
            .setParameter("memberId", entityId)
            .executeUpdate();
            
            // Remove attendance records
            em.createQuery("DELETE FROM Attendance a WHERE a.member.id = :memberId")
            .setParameter("memberId", entityId)
            .executeUpdate();
        }
        
        // Now delete the team member
        T managedEntity = em.contains(entity) ? entity : em.merge(entity);
        em.remove(managedEntity);
    }
    
    @Override
    public boolean deleteById(ID id) {
        // Use transaction retry logic for delete operations
        return executeWithRetry(() -> {
            EntityManager em = getEntityManager();
            try {
                em.getTransaction().begin();
                T entity = em.find(entityClass, id);
                if (entity != null) {
                    // Use specialized deletion logic if applicable
                    if (entityClass.equals(org.frcpm.models.Project.class)) {
                        handleProjectDeletion(entity, em);
                    } else if (entityClass.equals(org.frcpm.models.Task.class)) {
                        handleTaskDeletion(entity, em);
                    } else if (entityClass.equals(org.frcpm.models.Component.class)) {
                        handleComponentDeletion(entity, em);
                    } else if (entityClass.equals(org.frcpm.models.TeamMember.class)) {
                        handleTeamMemberDeletion(entity, em);
                    } else {
                        // Default deletion behavior
                        em.remove(entity);
                    }
                    em.getTransaction().commit();
                    return true;
                } else {
                    em.getTransaction().rollback();
                    return false;
                }
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e; // Re-throw for retry mechanism
            } finally {
                closeEntityManager(em);
            }
        }, "delete entity by ID");
    }
    
    @Override
    public long count() {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<T> root = cq.from(entityClass);
            cq.select(cb.count(root));
            TypedQuery<Long> query = em.createQuery(cq);
            Long result = query.getSingleResult();
            return result != null ? result : 0L;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting entities", e);
            return 0;
        } finally {
            closeEntityManager(em);
        }
    }

    /**
     * Helper method to get entity ID.
     * 
     * @param entity the entity
     * @return the ID, or null if it cannot be determined
     */
    private Long getEntityId(T entity) {
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                    field.setAccessible(true);
                    Object id = field.get(entity);
                    if (id instanceof Long) {
                        return (Long) id;
                    }
                }
            }
            
            // Check superclasses if ID not found
            Class<?> superClass = entity.getClass().getSuperclass();
            while (superClass != null && !superClass.equals(Object.class)) {
                for (Field field : superClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                        field.setAccessible(true);
                        Object id = field.get(entity);
                        if (id instanceof Long) {
                            return (Long) id;
                        }
                    }
                }
                superClass = superClass.getSuperclass();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not get entity ID", e);
        }
        return null;
    }
    
    /**
     * Executes a database operation with retry logic.
     * 
     * @param <R> the return type
     * @param operation the operation to execute
     * @param operationName the name of the operation for logging
     * @return the result of the operation
     */
    private <R> R executeWithRetry(DatabaseOperation<R> operation, String operationName) {
        int attempts = 0;
        
        while (attempts < MAX_TRANSACTION_RETRIES) {
            attempts++;
            
            try {
                // Execute the operation
                return operation.execute();
                
            } catch (Exception e) {
                // Check if this is a retryable error
                if (isRetryableError(e) && attempts < MAX_TRANSACTION_RETRIES) {
                    // Log the retry
                    LOGGER.log(Level.WARNING, 
                              "Retrying " + operationName + " after error (attempt " + attempts + 
                              " of " + MAX_TRANSACTION_RETRIES + "): " + e.getMessage());
                    
                    // Wait before retrying
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempts); // Progressive backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrupted during retry delay", ie);
                    }
                } else {
                    // Not retryable or max retries reached
                    LOGGER.log(Level.SEVERE, "Error in " + operationName + ": " + e.getMessage(), e);
                    throw new RuntimeException("Failed to " + operationName, e);
                }
            }
        }
        
        // This should not be reached, but just in case
        throw new RuntimeException("Failed to " + operationName + " after " + 
                                 MAX_TRANSACTION_RETRIES + " attempts");
    }
    
    /**
     * Functional interface for database operations.
     * 
     * @param <R> the return type
     */
    @FunctionalInterface
    private interface DatabaseOperation<R> {
        R execute() throws Exception;
    }
    
    /**
     * Determines if an error is retryable.
     * 
     * @param e the exception
     * @return true if the error is retryable, false otherwise
     */
    private boolean isRetryableError(Exception e) {
        // Common transient errors that can be retried:
        // - Deadlocks
        // - Lock timeouts
        // - Connection issues
        
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        
        // Check for common H2 retryable errors
        return message.contains("deadlock") || 
               message.contains("lock timeout") || 
               message.contains("connection") || 
               message.contains("timeout") || 
               message.contains("temporarily unavailable");
    }
    
    /**
     * Safely closes an EntityManager if it is open.
     * 
     * @param em the EntityManager to close
     */
    private void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            try {
                em.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing EntityManager", e);
            }
        }
    }
    
    /**
     * Performs a batch operation on multiple entities.
     * More efficient than processing entities one by one.
     * 
     * @param entities the entities to process
     * @param batchSize the batch size
     * @param batchOperation the operation to perform on each batch
     * @param <E> the entity type
     */
    protected <E> void performBatchOperation(List<E> entities, int batchSize, 
                                           BatchOperation<E> batchOperation) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        
        int totalSize = entities.size();
        int totalBatches = (int) Math.ceil((double) totalSize / batchSize);
        
        for (int i = 0; i < totalBatches; i++) {
            int startIndex = i * batchSize;
            int endIndex = Math.min(startIndex + batchSize, totalSize);
            List<E> batch = entities.subList(startIndex, endIndex);
            
            executeWithRetry(() -> {
                EntityManager em = getEntityManager();
                try {
                    em.getTransaction().begin();
                    batchOperation.process(batch, em);
                    em.getTransaction().commit();
                    return null;
                } catch (Exception e) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    throw e;
                } finally {
                    closeEntityManager(em);
                }
            }, "batch operation");
        }
    }
    
    /**
     * Functional interface for batch operations.
     * 
     * @param <E> the entity type
     */
    @FunctionalInterface
    protected interface BatchOperation<E> {
        void process(List<E> batch, EntityManager em) throws Exception;
    }
}