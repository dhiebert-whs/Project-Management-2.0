package org.frcpm.db;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.config.DatabaseConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Manager class for database operations and lifecycle.
 * Integrates with DatabaseConfigurer and BackupManager.
 */
public class DatabaseManager {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static DatabaseManager instance;
    private boolean initialized = false;
    private final Properties properties = new Properties();
    
    // Private constructor for singleton pattern
    private DatabaseManager() {
        // Initialize default properties
        properties.setProperty("backup.auto", "true");
        properties.setProperty("backup.interval", "24"); // Hours
        properties.setProperty("backup.count", "10"); // Number of backups to keep
    }
    
    /**
     * Gets the singleton instance.
     * 
     * @return the singleton instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Initializes the database manager.
     * 
     * @return true if initialization was successful, false otherwise
     */
    public boolean initialize() {
        if (initialized) {
            LOGGER.info("Database manager already initialized");
            return true;
        }
        
        try {
            LOGGER.info("Initializing database manager...");
            
            // Configure the database with optimal settings
            boolean configSuccess = DatabaseConfigurer.initializeWithOptimalSettings();
            if (!configSuccess) {
                LOGGER.severe("Failed to configure database");
                return false;
            }
            
            // Ensure backup directory exists
            BackupManager.ensureBackupDirExists();
            
            // Schedule automatic backups if enabled
            if (Boolean.parseBoolean(properties.getProperty("backup.auto", "true"))) {
                int backupInterval = Integer.parseInt(properties.getProperty("backup.interval", "24"));
                DatabaseConfigurer.scheduleAutomaticBackups(backupInterval);
            }
            
            initialized = true;
            LOGGER.info("Database manager initialized successfully");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing database manager", e);
            return false;
        }
    }
    
    /**
     * Shuts down the database manager.
     * 
     * @return true if shutdown was successful, false otherwise
     */
    public boolean shutdown() {
        try {
            LOGGER.info("Shutting down database manager...");
            
            // Stop scheduled backups
            DatabaseConfigurer.stopScheduledBackups();
            
            // Shutdown database configuration
            DatabaseConfig.shutdown();
            
            initialized = false;
            LOGGER.info("Database manager shut down successfully");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error shutting down database manager", e);
            return false;
        }
    }
    
    /**
     * Creates a backup of the database.
     * 
     * @return the backup, or null if the backup failed
     */
    public BackupManager.Backup createBackup() {
        return BackupManager.createBackup();
    }
    
    /**
     * Restores the database from a backup.
     * 
     * @param backup the backup to restore
     * @return true if the restore was successful, false otherwise
     */
    public boolean restoreBackup(BackupManager.Backup backup) {
        return BackupManager.restoreBackup(backup);
    }
    
    /**
     * Gets all available backups.
     * 
     * @return a list of backups
     */
    public java.util.List<BackupManager.Backup> getBackups() {
        return BackupManager.getBackups();
    }
    
    /**
     * Verifies the database connection and schema.
     * 
     * @return true if the database is properly configured, false otherwise
     */
    public boolean verifyDatabase() {
        return DatabaseConfigurer.verifyDatabase();
    }
    
    /**
     * Compacts the database to reduce file size.
     * 
     * @return true if the compact operation was successful, false otherwise
     */
    public boolean compactDatabase() {
        return DatabaseConfigurer.compactDatabase();
    }
    
    /**
     * Sets a configuration property.
     * 
     * @param key the property key
     * @param value the property value
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        
        // Apply changes immediately for certain properties
        if (key.equals("backup.auto")) {
            boolean autoBackup = Boolean.parseBoolean(value);
            if (autoBackup) {
                int backupInterval = Integer.parseInt(properties.getProperty("backup.interval", "24"));
                DatabaseConfigurer.scheduleAutomaticBackups(backupInterval);
            } else {
                DatabaseConfigurer.stopScheduledBackups();
            }
        } else if (key.equals("backup.interval")) {
            boolean autoBackup = Boolean.parseBoolean(properties.getProperty("backup.auto", "true"));
            if (autoBackup) {
                int backupInterval = Integer.parseInt(value);
                DatabaseConfigurer.scheduleAutomaticBackups(backupInterval);
            }
        } else if (key.equals("backup.count")) {
            int maxBackups = Integer.parseInt(value);
            BackupManager.cleanupOldBackups(maxBackups);
        }
    }
    
    /**
     * Gets a configuration property.
     * 
     * @param key the property key
     * @return the property value, or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Gets an entity manager for database operations.
     * 
     * @return a new entity manager
     */
    public EntityManager getEntityManager() {
        return DatabaseConfig.getEntityManager();
    }
    
    /**
     * Gets the entity manager factory.
     * 
     * @return the entity manager factory
     */
    public EntityManagerFactory getEntityManagerFactory() {
        return DatabaseConfig.getEntityManagerFactory();
    }
    
    /**
     * Gets the database file path.
     * 
     * @return the database file path, or null if using in-memory database
     */
    public String getDatabaseFilePath() {
        if (DatabaseConfig.isDevelopmentMode()) {
            return null; // In-memory database in development mode
        }
        
        String userHome = System.getProperty("user.home");
        File dbDir = new File(userHome, ".frcpm");
        return new File(dbDir, DatabaseConfig.getDatabaseName() + ".mv.db").getAbsolutePath();
    }
    
    /**
     * Gets the current database name.
     * 
     * @return the database name
     */
    public String getDatabaseName() {
        return DatabaseConfig.getDatabaseName();
    }
    
    /**
     * Checks if the database is in development mode.
     * 
     * @return true if in development mode, false if in production mode
     */
    public boolean isDevelopmentMode() {
        return DatabaseConfig.isDevelopmentMode();
    }
}