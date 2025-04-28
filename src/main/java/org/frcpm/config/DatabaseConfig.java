package org.frcpm.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration class for database settings and connection management.
 */
public class DatabaseConfig {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static EntityManagerFactory emf;
    private static final String PERSISTENCE_UNIT_NAME = "frcpm";
    private static boolean initialized = false;
    private static boolean developmentMode = false;
    private static String databaseName = System.getProperty("app.db.name", "frcpm");
    
    /**
     * Initializes the database configuration with default settings.
     */
    public static synchronized void initialize() {
        initialize(false);
    }
    
    /**
     * Initializes the database configuration.
     * 
     * @param forceDevMode whether to force development mode (create-drop)
     */
    public static synchronized void initialize(boolean forceDevMode) {
        if (initialized) {
            return;
        }
        
        try {
            LOGGER.info("Initializing database configuration...");
            developmentMode = forceDevMode || Boolean.getBoolean("app.db.dev");
            
            // Get the database name from system property
            databaseName = System.getProperty("app.db.name", "frcpm");
            
            // Determine database path - use persistent location for production
            String dbPath;
            if (developmentMode) {
                // Use in-memory database for development
                dbPath = "mem:" + databaseName + ";DB_CLOSE_DELAY=-1";
                LOGGER.info("Using IN-MEMORY database (development mode)");
            } else {
                // Use file-based database for production in user's home directory
                String userHome = System.getProperty("user.home");
                File dbDir = new File(userHome, ".frcpm");
                if (!dbDir.exists()) {
                    dbDir.mkdirs();
                }
                dbPath = "file:" + new File(dbDir, databaseName).getAbsolutePath() + ";DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1";
                LOGGER.info("Using FILE-BASED database at: " + dbPath);
            }
            
            String jdbcUrl = "jdbc:h2:" + dbPath;
            LOGGER.info("Using JDBC URL: " + jdbcUrl);
            
            // Set up JPA properties
            Map<String, Object> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
            props.put("jakarta.persistence.jdbc.url", jdbcUrl);
            props.put("jakarta.persistence.jdbc.user", "sa");
            props.put("jakarta.persistence.jdbc.password", "");
            props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            
            // Set appropriate schema mode - "update" for production, "create-drop" for development
            String schemaMode = developmentMode ? "create-drop" : "update";
            props.put("hibernate.hbm2ddl.auto", schemaMode);
            LOGGER.info("Using schema mode: " + schemaMode);
            
            // SQL logging - more verbose in development mode
            props.put("hibernate.show_sql", developmentMode);
            props.put("hibernate.format_sql", developmentMode);
            props.put("hibernate.use_sql_comments", developmentMode);
            
            // Add standard connection pool settings with improved reliability
            props.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
            props.put("hibernate.hikari.minimumIdle", "2");
            props.put("hibernate.hikari.maximumPoolSize", "10");
            props.put("hibernate.hikari.idleTimeout", "30000");
            props.put("hibernate.hikari.connectionTimeout", "10000");
            props.put("hibernate.hikari.maxLifetime", "1800000"); // 30 minutes
            props.put("hibernate.hikari.poolName", "FRC-PM-HikariCP");
            props.put("hibernate.hikari.autoCommit", "false");
            props.put("hibernate.hikari.initializationFailTimeout", "30000");
            
            // Connection testing
            props.put("hibernate.hikari.connectionTestQuery", "SELECT 1");
            props.put("hibernate.hikari.validationTimeout", "5000");
            
            // IMPORTANT: Disable second-level cache for all test runs to avoid cache provider issues
            props.put("hibernate.cache.use_second_level_cache", false);
            props.put("hibernate.cache.use_query_cache", false);
            
            // Connection handling
            props.put("hibernate.connection.handling_mode", "DELAYED_ACQUISITION_AND_RELEASE_AFTER_STATEMENT");
            
            // Batch processing - optimize for production mode
            props.put("hibernate.jdbc.batch_size", "30");
            props.put("hibernate.order_inserts", true);
            props.put("hibernate.order_updates", true);
            props.put("hibernate.jdbc.batch_versioned_data", true);
            
            // Transaction management
            props.put("hibernate.current_session_context_class", "thread");
            
            // Logging and statistics
            props.put("hibernate.generate_statistics", developmentMode);
            
            // Create the entity manager factory
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, props);
            
            initialized = true;
            LOGGER.info("Database configuration initialized successfully in " + 
                       (developmentMode ? "DEVELOPMENT" : "PRODUCTION") + " mode");
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
     * Checks if the database is running in development mode.
     * 
     * @return true if in development mode, false if in production mode
     */
    public static boolean isDevelopmentMode() {
        return developmentMode;
    }
    
    /**
     * Explicitly sets development mode.
     * Only has effect before initialization.
     * 
     * @param devMode whether to use development mode
     */
    public static void setDevelopmentMode(boolean devMode) {
        if (!initialized) {
            developmentMode = devMode;
        } else {
            LOGGER.warning("Cannot change development mode after initialization");
        }
    }
    
    /**
     * Sets the database name for file-based storage.
     * Only has effect before initialization.
     * 
     * @param name the database name
     */
    public static void setDatabaseName(String name) {
        if (!initialized) {
            databaseName = name;
        } else {
            LOGGER.warning("Cannot change database name after initialization");
        }
    }
    
    /**
     * Gets the current database name.
     * 
     * @return the database name
     */
    public static String getDatabaseName() {
        return databaseName;
    }
    
    /**
     * Shuts down the database configuration.
     */
    public static synchronized void shutdown() {
        if (emf != null && emf.isOpen()) {
            try {
                LOGGER.info("Shutting down database...");
                emf.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing EntityManagerFactory", e);
            }
            emf = null;
        }
        
        initialized = false;
        LOGGER.info("Database configuration shut down");
    }
    
    /**
     * Reinitializes the database configuration.
     * Useful for testing or when changing modes.
     * 
     * @param forceDevMode whether to force development mode
     */
    public static synchronized void reinitialize(boolean forceDevMode) {
        // Ensure we fully close and clear any existing connections
        shutdown();
        
        // Reset the initialized flag
        initialized = false;
        
        // Set development mode
        developmentMode = forceDevMode;
        
        // Reinitialize with clean state
        initialize();
    }
    
    /**
     * Creates a backup of the database.
     * 
     * @param backupPath the path to save the backup to
     * @return true if the backup was successful, false otherwise
     */
    public static boolean createBackup(String backupPath) {
        if (developmentMode) {
            LOGGER.warning("Cannot create backup in development mode (in-memory database)");
            return false;
        }
        
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            
            // Execute BACKUP SQL command
            String backupScript = "BACKUP TO '" + backupPath + "'";
            em.createNativeQuery(backupScript).executeUpdate();
            
            em.getTransaction().commit();
            LOGGER.info("Database backup created at: " + backupPath);
            return true;
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error creating database backup", e);
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    /**
     * Restores the database from a backup.
     * 
     * @param backupPath the path to the backup file
     * @return true if the restore was successful, false otherwise
     */
    public static boolean restoreFromBackup(String backupPath) {
        // First shut down the current database
        shutdown();
        
        // Now restore from backup
        EntityManager em = null;
        try {
            // Reinitialize with minimal configuration
            initialize();
            
            em = getEntityManager();
            em.getTransaction().begin();
            
            // Execute RESTORE SQL command
            String restoreScript = "RESTORE FROM '" + backupPath + "'";
            em.createNativeQuery(restoreScript).executeUpdate();
            
            em.getTransaction().commit();
            LOGGER.info("Database restored from: " + backupPath);
            
            // Reinitialize to ensure clean state
            reinitialize(developmentMode);
            
            return true;
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error restoring database from backup", e);
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}