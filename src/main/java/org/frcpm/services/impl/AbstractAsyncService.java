// src/main/java/org/frcpm/services/impl/AbstractAsyncService.java

package org.frcpm.services.impl;

import org.frcpm.async.DatabaseTask;
import org.frcpm.async.TaskFactory;
import org.frcpm.config.DatabaseConfig;
import org.frcpm.repositories.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base class that extends AbstractService and adds asynchronous execution capabilities.
 * 
 * @param <T> the entity type
 * @param <ID> the entity's ID type
 * @param <R> the repository type
 */
public abstract class AbstractAsyncService<T, ID, R extends Repository<T, ID>> extends AbstractService<T, ID, R> {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractAsyncService.class.getName());
    
    public AbstractAsyncService(R repository) {
        super(repository);
    }
    
    /**
     * Finds an entity by its ID asynchronously.
     * 
     * @param id the entity ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the entity
     */
    public CompletableFuture<T> findByIdAsync(ID id, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        return executeAsync("Find Entity By ID: " + id, em -> {
            Optional<T> result = repository.findById(id);
            return result.orElse(null);
        }, onSuccess, onFailure);
    }
    
    /**
     * Finds all entities asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of entities
     */
    public CompletableFuture<List<T>> findAllAsync(Consumer<List<T>> onSuccess, Consumer<Throwable> onFailure) {
        return executeAsync("Find All Entities", em -> {
            return repository.findAll();
        }, onSuccess, onFailure);
    }
    
    /**
     * Saves an entity asynchronously.
     * 
     * @param entity the entity to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved entity
     */
    public CompletableFuture<T> saveAsync(T entity, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        if (entity == null) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Entity cannot be null"));
            return future;
        }
        
        return executeAsync("Save Entity", em -> {
            return repository.save(entity);
        }, onSuccess, onFailure);
    }
    
    /**
     * Deletes an entity asynchronously.
     * 
     * @param entity the entity to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed when the entity is deleted
     */
    public CompletableFuture<Void> deleteAsync(T entity, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
        if (entity == null) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Entity cannot be null"));
            return future;
        }
        
        return executeAsync("Delete Entity", em -> {
            repository.delete(entity);
            return null;
        }, onSuccess, onFailure);
    }
    
    /**
     * Deletes an entity by its ID asynchronously.
     * 
     * @param id the ID of the entity to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a boolean indicating success
     */
    public CompletableFuture<Boolean> deleteByIdAsync(ID id, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        if (id == null) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("ID cannot be null"));
            return future;
        }
        
        return executeAsync("Delete Entity By ID: " + id, em -> {
            return repository.deleteById(id);
        }, onSuccess, onFailure);
    }
    
    /**
     * Counts all entities asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the count of entities
     */
    public CompletableFuture<Long> countAsync(Consumer<Long> onSuccess, Consumer<Throwable> onFailure) {
        return executeAsync("Count Entities", em -> {
            return repository.count();
        }, onSuccess, onFailure);
    }
    
    /**
     * Executes a database operation asynchronously with error handling.
     * This method allows providing a database operation that uses an EntityManager.
     * 
     * @param <R> the result type
     * @param taskName the name of the task
     * @param databaseOperation the database operation to execute
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the result
     */
    protected <R> CompletableFuture<R> executeAsync(String taskName, 
                                                  Function<EntityManager, R> databaseOperation,
                                                  Consumer<R> onSuccess, 
                                                  Consumer<Throwable> onFailure) {
        return executeAsync(taskName, () -> {
            EntityManager em = DatabaseConfig.getEntityManager();
            try {
                return databaseOperation.apply(em);
            } finally {
                if (em != null && em.isOpen()) {
                    em.close();
                }
            }
        }, onSuccess, onFailure);
    }
    
    /**
     * Executes a callable task asynchronously with error handling.
     * 
     * @param <R> the result type
     * @param taskName the name of the task
     * @param callable the callable to execute
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the result
     */
    protected <R> CompletableFuture<R> executeAsync(String taskName, 
                                                  Callable<R> callable,
                                                  Consumer<R> onSuccess, 
                                                  Consumer<Throwable> onFailure) {
        // Create a CompletableFuture for the result
        CompletableFuture<R> future = new CompletableFuture<>();
        
        // Use TaskFactory to create and execute the task
        TaskFactory.createDataLoadTask(
            callable,
            result -> {
                if (onSuccess != null) {
                    onSuccess.accept(result);
                }
                future.complete(result);
            },
            error -> {
                if (onFailure != null) {
                    onFailure.accept(error);
                }
                future.completeExceptionally(error);
            }
        );
        
        return future;
    }
    
    /**
     * Executes a database operation synchronously with transaction management.
     * This is a utility method for operations that cannot be easily refactored for async.
     * 
     * @param <R> the result type
     * @param operation the operation to execute
     * @return the result of the operation
     */
    protected <R> R executeSync(Function<EntityManager, R> operation) {
        EntityManager em = null;
        EntityTransaction tx = null;
        
        try {
            // Get entity manager
            em = DatabaseConfig.getEntityManager();
            tx = em.getTransaction();
            
            // Begin transaction
            tx.begin();
            
            // Execute operation
            R result = operation.apply(em);
            
            // Commit transaction
            tx.commit();
            
            return result;
        } catch (Exception e) {
            // Rollback transaction if active
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackException) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", rollbackException);
                }
            }
            
            LOGGER.log(Level.SEVERE, "Error executing database operation", e);
            throw e;
        } finally {
            // Close entity manager
            if (em != null) {
                em.close();
            }
        }
    }
}