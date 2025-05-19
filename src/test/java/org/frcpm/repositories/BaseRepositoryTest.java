// src/test/java/org/frcpm/repositories/BaseRepositoryTest.java
package org.frcpm.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.frcpm.config.DatabaseConfig;
import org.frcpm.utils.TestDatabaseCleaner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.logging.Logger;

/**
 * Base class for repository tests.
 * Provides common setup and teardown functionality for database testing.
 */
public abstract class BaseRepositoryTest {
    
    protected static final Logger LOGGER = Logger.getLogger(BaseRepositoryTest.class.getName());
    
    protected EntityManagerFactory emf;
    protected EntityManager em;
    
    /**
     * Sets up the test environment before each test.
     * Initializes the database in development mode and creates an EntityManager.
     */
    @BeforeEach
    public void setUp() {
        LOGGER.info("Setting up repository test environment");
        
        // Configure database for testing
        DatabaseConfig.setDevelopmentMode(true);
        DatabaseConfig.setDatabaseName("test_db");
        DatabaseConfig.initialize(true);
        
        // Get EntityManagerFactory and create EntityManager
        emf = DatabaseConfig.getEntityManagerFactory();
        em = emf.createEntityManager();
        
        // Ensure database is clean before test
        TestDatabaseCleaner.clearTestDatabase();
        
        // Additional setup
        setupTestData();
    }
    
    /**
     * Tears down the test environment after each test.
     * Closes the EntityManager and cleans up database connections.
     */
    @AfterEach
    public void tearDown() {
        LOGGER.info("Tearing down repository test environment");
        
        // Close EntityManager
        if (em != null && em.isOpen()) {
            em.close();
        }
        
        // Clean database for next test
        TestDatabaseCleaner.clearTestDatabase();
    }
    
    /**
     * Sets up test data for the test.
     * Override this method to create specific test data for each test class.
     */
    protected abstract void setupTestData();
    
    /**
     * Begins a transaction if one is not already active.
     */
    protected void beginTransaction() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }
    
    /**
     * Commits the current transaction if one is active.
     */
    protected void commitTransaction() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }
    
    /**
     * Rolls back the current transaction if one is active.
     */
    protected void rollbackTransaction() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
    
    /**
     * Flushes and clears the EntityManager to ensure all changes are synchronized with the database.
     */
    protected void flushAndClear() {
        em.flush();
        em.clear();
    }
}