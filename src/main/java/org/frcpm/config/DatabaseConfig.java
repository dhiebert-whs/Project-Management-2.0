package org.frcpm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database configuration for the FRC Project Management System.
 * Handles connection to the H2 database using HikariCP and JPA.
 */
public class DatabaseConfig {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static final String PERSISTENCE_UNIT_NAME = "frcpm";
    private static final String DB_DIR = "db";
    private static final String DB_NAME = "frcpm";
    private static EntityManagerFactory emf;
    private static HikariDataSource dataSource;
    
    /**
     * Initializes the database connection pool and JPA.
     */
    public static void initialize() {
        try {
            // Ensure database directory exists
            File dbDir = new File(DB_DIR);
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            
            // Configure connection pool
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:file:./" + DB_DIR + "/" + DB_NAME);
            config.setUsername("sa");
            config.setPassword("");
            config.setDriverClassName("org.h2.Driver");
            config.setPoolName("FRC-PM-Pool");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(10000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            // Create data source
            dataSource = new HikariDataSource(config);
            
            // Configure JPA
            Map<String, Object> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
            props.put("jakarta.persistence.jdbc.url", "jdbc:h2:file:./" + DB_DIR + "/" + DB_NAME);
            props.put("jakarta.persistence.jdbc.user", "sa");
            props.put("jakarta.persistence.jdbc.password", "");
            props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            props.put("hibernate.hbm2ddl.auto", "create");
            //props.put("hibernate.hbm2ddl.auto", "update");
            props.put("hibernate.show_sql", "false");
            props.put("hibernate.format_sql", "true");
            
            // Create EntityManagerFactory
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, props);
            
            LOGGER.info("Database connection initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    /**
     * Gets a new EntityManager from the factory.
     * 
     * @return a new EntityManager instance
     */
    public static EntityManager getEntityManager() {
        if (emf == null) {
            initialize();
        }
        return emf.createEntityManager();
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            initialize();
        }
        return emf;
    }
    
    /**
     * Closes all database resources.
     * This should be called when the application is shutting down.
     */
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        
        LOGGER.info("Database resources closed");
    }
}
