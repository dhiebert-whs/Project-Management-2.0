package org.frcpm.db;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.config.DatabaseConfig;

/**
 * Utility class for database configuration and management.
 * Provides methods for configuring, backing up, and restoring the database.
 */
public class DatabaseConfigurer {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfigurer.class.getName());
    
    /**
     * Configures the database with the given settings.
     * 
     * @param developmentMode whether to use development mode
     * @param databaseName the name of the database
     * @return true if configuration was successful, false otherwise
     */
    public static boolean configureDatabase(boolean developmentMode, String databaseName) {
        try {
            // Set configuration options
            DatabaseConfig.setDevelopmentMode(developmentMode);
            DatabaseConfig.setDatabaseName(databaseName);
            
            // Initialize the database
            DatabaseConfig.initialize();
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error configuring database", e);
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
        
        // Create backup
        boolean success = DatabaseConfig.createBackup(backupPath);
        
        return success ? backupPath : null;
    }
    
    /**
     * Restores the database from a backup file.
     * 
     * @param backupPath the path to the backup file
     * @return true if the restore was successful, false otherwise
     */
    public static boolean restoreFromBackup(String backupPath) {
        // Verify that the backup file exists
        File backupFile = new File(backupPath);
        if (!backupFile.exists() || !backupFile.isFile()) {
            LOGGER.severe("Backup file does not exist: " + backupPath);
            return false;
        }
        
        // Restore from backup
        boolean success = DatabaseConfig.restoreFromBackup(backupPath);
        
        return success;
    }
    
    /**
     * Lists all available backup files.
     * 
     * @return an array of backup file names
     */
    public static String[] listBackups() {
        // Get backup directory
        String userHome = System.getProperty("user.home");
        File backupDir = new File(userHome, ".frcpm/backups");
        
        // Check if directory exists
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return new String[0];
        }
        
        // List backup files
        return backupDir.list((dir, name) -> name.endsWith(".zip"));
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
                return true;
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
        jakarta.persistence.EntityManager em = null;
        try {
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