package org.frcpm.db;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.utils.UpdatedErrorHandler;

/**
 * Utility class for database configuration and management.
 * Provides methods for configuring, backing up, and restoring the database.
 */
public class DatabaseConfigurer {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfigurer.class.getName());
    private static ScheduledExecutorService scheduledBackupService;
    private static final Object backupLock = new Object();
    
    /**
     * Configures the database with optimal settings for reliability.
     * 
     * @param developmentMode whether to use development mode
     * @param databaseName the name of the database
     * @return true if configuration was successful, false otherwise
     */
    public static boolean configureDatabase(boolean developmentMode, String databaseName) {
        try {
            LOGGER.info("Configuring database with name: " + databaseName + 
                      ", developmentMode: " + developmentMode);
            
            // Set configuration options
            DatabaseConfig.setDevelopmentMode(developmentMode);
            DatabaseConfig.setDatabaseName(databaseName);
            
            // Initialize the database with proper settings
            DatabaseConfig.initialize();
            
            // Verify that the database is properly configured
            return verifyDatabase();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error configuring database", e);
            UpdatedErrorHandler.showError("Database Configuration Error", 
                    "Failed to configure the database: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Creates a backup of the database.
     * The backup file will be saved in the backup directory with a timestamp.
     * 
     * @return the path to the backup file, or null if the backup failed
     */
    public static String createBackup() {
        synchronized (backupLock) {
            // Get user home directory
            String userHome = System.getProperty("user.home");
            File backupDir = new File(userHome, ".frcpm/backups");
            
            // Create backup directory if it doesn't exist
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            // Create backup file name with timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = dateFormat.format(new Date());
            String dbName = DatabaseConfig.getDatabaseName();
            String backupFileName = dbName + "_" + timestamp + ".zip";
            
            String backupPath = new File(backupDir, backupFileName).getAbsolutePath();
            
            try {
                LOGGER.info("Creating database backup at: " + backupPath);
                
                // Create backup using DatabaseConfig
                boolean success = DatabaseConfig.createBackup(backupPath);
                
                if (success) {
                    LOGGER.info("Database backup created successfully");
                    // Clean up old backups - keep only the 10 most recent
                    BackupManager.cleanupOldBackups(10);
                    return backupPath;
                } else {
                    LOGGER.severe("Database backup failed");
                    return null;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error creating database backup", e);
                return null;
            }
        }
    }
    
    /**
     * Restores the database from a backup file.
     * 
     * @param backupPath the path to the backup file
     * @return true if the restore was successful, false otherwise
     */
    public static boolean restoreFromBackup(String backupPath) {
        synchronized (backupLock) {
            // Verify that the backup file exists
            File backupFile = new File(backupPath);
            if (!backupFile.exists() || !backupFile.isFile()) {
                LOGGER.severe("Backup file does not exist: " + backupPath);
                return false;
            }
            
            try {
                LOGGER.info("Restoring database from backup: " + backupPath);
                
                // Create a pre-restore backup just in case
                String preRestoreBackup = createBackup();
                LOGGER.info("Created pre-restore backup at: " + preRestoreBackup);
                
                // Restore from backup
                boolean success = DatabaseConfig.restoreFromBackup(backupPath);
                
                if (success) {
                    LOGGER.info("Database restored successfully from backup");
                } else {
                    LOGGER.severe("Database restore failed");
                }
                
                return success;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error restoring database from backup", e);
                return false;
            }
        }
    }
    
    /**
     * Schedules automatic backups at specified intervals.
     * 
     * @param intervalHours the interval between backups in hours
     * @return true if the schedule was set up, false otherwise
     */
    public static boolean scheduleAutomaticBackups(int intervalHours) {
        try {
            // Stop any existing scheduled backups
            stopScheduledBackups();
            
            // Create a new scheduled executor service
            scheduledBackupService = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "Database-Backup-Thread");
                thread.setDaemon(true);
                return thread;
            });
            
            // Schedule backups at the specified interval
            scheduledBackupService.scheduleAtFixedRate(() -> {
                try {
                    LOGGER.info("Performing scheduled database backup");
                    createBackup();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in scheduled backup", e);
                }
            }, 1, intervalHours, TimeUnit.HOURS); // First backup after 1 hour
            
            LOGGER.info("Automatic backups scheduled every " + intervalHours + " hours");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error scheduling automatic backups", e);
            return false;
        }
    }
    
    /**
     * Stops scheduled automatic backups.
     */
    public static void stopScheduledBackups() {
        if (scheduledBackupService != null && !scheduledBackupService.isShutdown()) {
            scheduledBackupService.shutdown();
            try {
                scheduledBackupService.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Interrupted while waiting for backup service shutdown", e);
            }
            LOGGER.info("Automatic backups stopped");
        }
    }
    
    /**
     * Verifies database connectivity and schema.
     * 
     * @return true if the database is properly configured, false otherwise
     */
    public static boolean verifyDatabase() {
        try {
            // Get entity manager and check if it can be created
            jakarta.persistence.EntityManager em = DatabaseConfig.getEntityManager();
            
            try {
                // Try a simple query
                em.createQuery("SELECT COUNT(p) FROM Project p").getSingleResult();
                
                // Test transaction support
                em.getTransaction().begin();
                em.getTransaction().rollback();
                
                LOGGER.info("Database verification successful");
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Database verification failed: " + e.getMessage(), e);
                return false;
            } finally {
                em.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying database", e);
            return false;
        }
    }
    
    /**
     * Compacts the database to reduce file size.
     * This is useful after deleting large amounts of data.
     * 
     * @return true if the compact operation was successful, false otherwise
     */
    public static boolean compactDatabase() {
        synchronized (backupLock) {
            jakarta.persistence.EntityManager em = null;
            try {
                // Create a backup before compacting
                String backupPath = createBackup();
                LOGGER.info("Created pre-compact backup at: " + backupPath);
                
                em = DatabaseConfig.getEntityManager();
                em.getTransaction().begin();
                
                // Execute SHUTDOWN COMPACT command
                em.createNativeQuery("SHUTDOWN COMPACT").executeUpdate();
                
                em.getTransaction().commit();
                
                // Reinitialize the database
                DatabaseConfig.reinitialize(DatabaseConfig.isDevelopmentMode());
                
                LOGGER.info("Database compacted successfully");
                return true;
            } catch (Exception e) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                LOGGER.log(Level.SEVERE, "Error compacting database", e);
                return false;
            } finally {
                if (em != null) {
                    em.close();
                }
            }
        }
    }
    
    /**
     * Initializes the database with optimal settings for reliability.
     * 
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initializeWithOptimalSettings() {
        String dbName = System.getProperty("app.db.name", "frcpm");
        boolean devMode = Boolean.getBoolean("app.db.dev");
        
        return configureDatabase(devMode, dbName);
    }
}