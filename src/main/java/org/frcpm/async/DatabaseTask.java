package org.frcpm.async;

import javafx.concurrent.Task;
import org.frcpm.config.DatabaseConfig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for tasks that perform database operations.
 * Provides transaction management and progress reporting.
 * 
 * @param <T> the result type
 */
public class DatabaseTask<T> extends Task<T> {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseTask.class.getName());
    
    private final String taskName;
    private final Function<EntityManager, T> databaseOperation;
    
    /**
     * Creates a new database task.
     * 
     * @param taskName the name of the task
     * @param databaseOperation the database operation to perform
     */
    public DatabaseTask(String taskName, Function<EntityManager, T> databaseOperation) {
        this.taskName = taskName;
        this.databaseOperation = databaseOperation;
    }
    
    /**
     * Executes the database operation.
     * 
     * @return the result of the operation
     * @throws Exception if an error occurs
     */
    @Override
    protected T call() throws Exception {
        updateMessage("Starting " + taskName);
        updateProgress(0, 100);
        
        EntityManager em = null;
        EntityTransaction tx = null;
        
        try {
            // Get entity manager
            em = DatabaseConfig.getEntityManager();
            tx = em.getTransaction();
            
            updateProgress(10, 100);
            updateMessage("Connected to database");
            
            // Begin transaction
            tx.begin();
            
            updateProgress(20, 100);
            updateMessage("Executing " + taskName);
            
            // Execute operation
            T result = databaseOperation.apply(em);
            
            updateProgress(80, 100);
            updateMessage("Committing transaction");
            
            // Commit transaction
            tx.commit();
            
            updateProgress(100, 100);
            updateMessage(taskName + " completed");
            
            return result;
        } catch (Exception e) {
            updateMessage("Error: " + e.getMessage());
            
            // Rollback transaction if active
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackException) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", rollbackException);
                }
            }
            
            LOGGER.log(Level.SEVERE, "Error executing database task: " + taskName, e);
            throw e;
        } finally {
            // Close entity manager
            if (em != null) {
                em.close();
            }
        }
    }
}