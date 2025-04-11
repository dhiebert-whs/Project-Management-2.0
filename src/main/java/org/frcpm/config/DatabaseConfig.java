// src/main/java/org/frcpm/config/DatabaseConfig.java
package org.frcpm.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConfig {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static EntityManagerFactory emf;
    private static final String PERSISTENCE_UNIT_NAME = "frcpm";
    private static boolean initialized = false;
    
    /**
     * Initializes the database configuration.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            LOGGER.info("Initializing database configuration...");
            
            // Use in-memory database for tests to avoid file path issues
            String jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
            
            LOGGER.info("Using JDBC URL: " + jdbcUrl);
            
            // Set up JPA properties
            Map<String, Object> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
            props.put("jakarta.persistence.jdbc.url", jdbcUrl);
            props.put("jakarta.persistence.jdbc.user", "sa");
            props.put("jakarta.persistence.jdbc.password", "");
            props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            props.put("hibernate.hbm2ddl.auto", "create-drop"); // Use create-drop for tests
            props.put("hibernate.show_sql", "true");
            props.put("hibernate.format_sql", "true");
            
            // Add standard connection pool settings
            props.put("hibernate.connection.pool_size", "5");
            
            // Create the entity manager factory
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, props);
            
            initialized = true;
            LOGGER.info("Database configuration initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    /**
     * Gets the entity manager factory.
     * 
     * @return the entity manager factory
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (!initialized) {
            initialize();
        }
        return emf;
    }
    
    /**
     * Creates a new entity manager.
     * 
     * @return a new entity manager
     */
    public static EntityManager getEntityManager() {
        if (!initialized) {
            initialize();
        }
        return emf.createEntityManager();
    }
    
    /**
     * Shuts down the database configuration.
     */
    public static synchronized void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        
        initialized = false;
        LOGGER.info("Database configuration shut down");
    }
}