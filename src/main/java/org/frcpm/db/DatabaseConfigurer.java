// src/main/java/org/frcpm/db/DatabaseConfigurer.java
package org.frcpm.db;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.async.TaskExecutor;
import org.frcpm.utils.ErrorHandler;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configures the database with optimal settings for reliable operation.
 * Handles database connection properties, backup scheduling, and diagnostics.
 * Works with the H2 database to ensure proper persistence and performance.
 */
public class DatabaseConfigurer {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfigurer.class.getName());
    
    // Default H2 database connection parameters
    private static final String DEFAULT_URL_PREFIX = "jdbc:h2:";
    private static final String FILE_URL_PATTERN = "jdbc:h2:file:";
    private static final String MEM_URL_PATTERN = "jdbc:h2:mem:";
    
    // H2 database specific settings
    private static final String CLOSE_ON_EXIT_SETTING = "DB_CLOSE_ON_EXIT";
    private static final String CLOSE_DELAY_SETTING = "DB_CLOSE_DELAY";
    private static final String CACHE_SIZE_SETTING = "CACHE_SIZE";
    private static final String LOCK_TIMEOUT_SETTING = "LOCK_TIMEOUT";
    private static final String IGNORE_CASE_SETTING = "IGNORECASE";
    private static final String AUTO_SERVER_SETTING = "AUTO_SERVER";
    
    // Default values
    private static final int DEFAULT_CACHE_SIZE = 8192;  // KB
    private static final int DEFAULT_LOCK_TIMEOUT = 10000;  // ms
    
    // Scheduled backup task
    private static ScheduledFuture<?> backupSchedule;
    
    /**
     * Initializes the database with optimal settings.
     * 
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initializeWithOptimalSettings() {
        LOGGER.info("Initializing database with optimal settings");
        
        try {
            // Get database file path
            String dbPath = getDatabaseFilePath();
            LOGGER.info("Database path: " + dbPath);
            
            if (dbPath == null || dbPath.isEmpty()) {
                // For in-memory databases, just return true
                LOGGER.info("Using in-memory database, no file path configuration needed");
                return true;
            }
            
            // Ensure the database directory exists
            File dbDir = new File(dbPath).getParentFile();
            if (dbDir != null && !dbDir.exists()) {
                boolean dirCreated = dbDir.mkdirs();
                if (!dirCreated) {
                    LOGGER.severe("Failed to create database directory: " + dbDir.getAbsolutePath());
                    return false;
                }
            }
            
            // Apply optimal settings
            applyOptimalSettings();
            
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing database with optimal settings", e);
            return false;
        }
    }
    
    /**
     * Applies optimal settings to the H2 database.
     * This includes:
     * - Disabling automatic close on VM exit
     * - Setting close delay to -1 (never close until shutdown)
     * - Optimizing cache size
     * - Setting lock timeout
     * - Enabling auto server mode for multiple connections
     */
    private static void applyOptimalSettings() {
        try {
            // Get the JDBC URL from persistence properties (using current config as reference)
            Map<String, Object> props = getJdbcPropertiesFromConfig();
            String jdbcUrl = (String) props.get("jakarta.persistence.jdbc.url");
            LOGGER.info("Current JDBC URL: " + jdbcUrl);
            
            if (jdbcUrl == null) {
                LOGGER.warning("Could not determine JDBC URL");
                return;
            }
            
            // Parse and enhance URL with optimal settings
            String enhancedUrl = enhanceJdbcUrl(jdbcUrl);
            LOGGER.info("Enhanced JDBC URL: " + enhancedUrl);
            
            // Update properties and reinitialize if needed
            if (!jdbcUrl.equals(enhancedUrl)) {
                // The URL needs to be updated, but we need to reinitialize the config
                // to apply it properly. We'll store this for the next restart.
                
                // Store the enhanced URL in a system property for future initialization
                System.setProperty("app.db.url", enhancedUrl);
            }
            
            // Apply settings to existing connections via SQL
            EntityManager em = DatabaseConfig.getEntityManager();
            try {
                // Set optimal settings via SQL statements
                em.getTransaction().begin();
                
                em.createNativeQuery("SET " + CLOSE_ON_EXIT_SETTING + " FALSE").executeUpdate();
                em.createNativeQuery("SET " + CLOSE_DELAY_SETTING + " -1").executeUpdate();
                em.createNativeQuery("SET " + CACHE_SIZE_SETTING + " " + DEFAULT_CACHE_SIZE).executeUpdate();
                em.createNativeQuery("SET " + LOCK_TIMEOUT_SETTING + " " + DEFAULT_LOCK_TIMEOUT).executeUpdate();
                
                em.getTransaction().commit();
                
                LOGGER.info("Applied optimal settings to database");
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                LOGGER.log(Level.WARNING, "Error applying optimal settings via SQL", e);
                // Continue execution - this is not a critical failure
            } finally {
                em.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error applying optimal database settings", e);
            // Continue execution - application can still function without optimal settings
        }
    }
    
    /**
     * Gets JDBC properties from the current configuration.
     * 
     * @return a map of JDBC properties
     */
    private static Map<String, Object> getJdbcPropertiesFromConfig() {
        Map<String, Object> props = new HashMap<>();
        
        // Try to get a connection and extract properties
        try {
            // Try to get configuration from DatabaseConfig directly
            if (DatabaseConfig.isDevelopmentMode()) {
                // Development mode uses in-memory database
                props.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:" + DatabaseConfig.getDatabaseName());
            } else {
                String userHome = System.getProperty("user.home");
                File dbDir = new File(userHome, ".frcpm");
                String dbPath = "file:" + new File(dbDir, DatabaseConfig.getDatabaseName()).getAbsolutePath();
                props.put("jakarta.persistence.jdbc.url", "jdbc:h2:" + dbPath);
            }
            
            // Add other standard properties
            props.put("jakarta.persistence.jdbc.user", "sa");
            props.put("jakarta.persistence.jdbc.password", "");
            props.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting JDBC properties from config", e);
        }
        
        return props;
    }
    
    /**
     * Enhances the JDBC URL with optimal settings.
     * 
     * @param jdbcUrl the original JDBC URL
     * @return the enhanced JDBC URL
     */
    private static String enhanceJdbcUrl(String jdbcUrl) {
        // Skip if null
        if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
            return jdbcUrl;
        }
        
        // Skip if not H2
        if (!jdbcUrl.startsWith(DEFAULT_URL_PREFIX)) {
            return jdbcUrl;
        }
        
        // Parse URL and add parameters
        StringBuilder enhancedUrl = new StringBuilder(jdbcUrl);
        
        // Check if URL already has parameters
        if (jdbcUrl.contains(";")) {
            // URL already has parameters, just add our parameters
            enhancedUrl.append(";").append(CLOSE_ON_EXIT_SETTING).append("=FALSE");
            enhancedUrl.append(";").append(CLOSE_DELAY_SETTING).append("=-1");
            enhancedUrl.append(";").append(CACHE_SIZE_SETTING).append("=").append(DEFAULT_CACHE_SIZE);
            enhancedUrl.append(";").append(LOCK_TIMEOUT_SETTING).append("=").append(DEFAULT_LOCK_TIMEOUT);
            
            // Add AUTO_SERVER=TRUE if file-based database
            if (jdbcUrl.startsWith(FILE_URL_PATTERN)) {
                enhancedUrl.append(";").append(AUTO_SERVER_SETTING).append("=TRUE");
            }
        } else {
            // URL has no parameters yet, add parameters with initial semicolon
            enhancedUrl.append(";").append(CLOSE_ON_EXIT_SETTING).append("=FALSE");
            enhancedUrl.append(";").append(CLOSE_DELAY_SETTING).append("=-1");
            enhancedUrl.append(";").append(CACHE_SIZE_SETTING).append("=").append(DEFAULT_CACHE_SIZE);
            enhancedUrl.append(";").append(LOCK_TIMEOUT_SETTING).append("=").append(DEFAULT_LOCK_TIMEOUT);
            
            // Add AUTO_SERVER=TRUE if file-based database
            if (jdbcUrl.startsWith(FILE_URL_PATTERN)) {
                enhancedUrl.append(";").append(AUTO_SERVER_SETTING).append("=TRUE");
            }
        }
        
        return enhancedUrl.toString();
    }
    
    /**
     * Schedules automatic backups at regular intervals.
     * 
     * @param intervalHours the interval in hours between backups
     * @return true if the schedule was set up successfully, false otherwise
     */
    public static boolean scheduleAutomaticBackups(int intervalHours) {
        LOGGER.info("Scheduling automatic backups every " + intervalHours + " hours");
        
        try {
            // Cancel existing schedule if any
            stopScheduledBackups();
            
            // Schedule new backup task with a single-thread scheduler
            java.util.concurrent.ScheduledExecutorService scheduler = 
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
            
            backupSchedule = scheduler.scheduleAtFixedRate(
                () -> {
                    LOGGER.info("Running scheduled backup");
                    try {
                        BackupManager.Backup backup = BackupManager.createBackup();
                        if (backup != null) {
                            LOGGER.info("Scheduled backup created: " + backup.getFilePath());
                        } else {
                            LOGGER.warning("Scheduled backup failed");
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Scheduled backup failed", e);
                    }
                },
                intervalHours, intervalHours, TimeUnit.HOURS
            );
            
            LOGGER.info("Automatic backup schedule established");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error scheduling automatic backups", e);
            return false;
        }
    }
    
    /**
     * Stops scheduled automatic backups.
     * 
     * @return true if successful, false otherwise
     */
    public static boolean stopScheduledBackups() {
        LOGGER.info("Stopping scheduled backups");
        
        try {
            if (backupSchedule != null && !backupSchedule.isCancelled()) {
                backupSchedule.cancel(false);
                backupSchedule = null;
                LOGGER.info("Backup schedule cancelled");
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error stopping backup schedule", e);
            return false;
        }
    }
    
    /**
     * Verifies that the database is properly configured.
     * 
     * @return true if the database is properly configured, false otherwise
     */
    public static boolean verifyDatabase() {
        LOGGER.info("Verifying database configuration");
        
        try {
            // Get the DB URL from current configuration
            Map<String, Object> props = getJdbcPropertiesFromConfig();
            String url = (String) props.get("jakarta.persistence.jdbc.url");
            
            // Check if the database file exists if file-based
            if (url != null && url.startsWith("jdbc:h2:file:")) {
                String filePath = getDatabaseFilePath();
                if (filePath != null) {
                    File dbFile = new File(filePath);
                    if (!dbFile.exists() || !dbFile.isFile()) {
                        LOGGER.warning("Database file does not exist: " + filePath);
                        return false;
                    }
                } else {
                    LOGGER.warning("Could not determine database file path");
                    return false;
                }
            }
            
            // Test a connection
            EntityManager em = null;
            try {
                em = DatabaseConfig.getEntityManager();
                
                // Try to execute a simple query
                Boolean result = (Boolean) em.createNativeQuery("SELECT TRUE FROM DUAL")
                                           .getSingleResult();
                
                // Check the database settings using more compatible approach
                Object closeOnExit = em.createNativeQuery("SELECT SETTING_VALUE('" + CLOSE_ON_EXIT_SETTING + "')")
                                     .getSingleResult();
                Object closeDelay = em.createNativeQuery("SELECT SETTING_VALUE('" + CLOSE_DELAY_SETTING + "')")
                                    .getSingleResult();
                
                LOGGER.info("Database verification successful");
                LOGGER.info("Settings: " + CLOSE_ON_EXIT_SETTING + "=" + closeOnExit + 
                          ", " + CLOSE_DELAY_SETTING + "=" + closeDelay);
                
                return result != null && result;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during database settings check: " + e.getMessage());
                
                // Try a simpler query to verify basic connectivity
                try {
                    Object result = em.createNativeQuery("SELECT 1").getSingleResult();
                    LOGGER.info("Database basic connectivity check successful");
                    return result != null;
                } catch (Exception e2) {
                    LOGGER.log(Level.SEVERE, "Database verification failed", e2);
                    return false;
                }
            } finally {
                if (em != null) {
                    em.close();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database verification failed", e);
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
        LOGGER.info("Compacting database");
        
        // Skip for in-memory databases
        if (isInMemoryDatabase()) {
            LOGGER.info("Skipping compact for in-memory database");
            return true;
        }
        
        Connection conn = null;
        Statement stmt = null;
        
        try {
            // Get connection details from properties
            Map<String, Object> props = getJdbcPropertiesFromConfig();
            String url = (String) props.get("jakarta.persistence.jdbc.url");
            String username = (String) props.get("jakarta.persistence.jdbc.user");
            String password = (String) props.get("jakarta.persistence.jdbc.password");
            
            if (url == null) {
                LOGGER.warning("Could not determine JDBC URL for compaction");
                return false;
            }
            
            // Default H2 credentials if not specified
            if (username == null) username = "sa";
            if (password == null) password = "";
            
            // Temporarily shut down the EntityManagerFactory to free connections
            EntityManagerFactory emf = DatabaseConfig.getEntityManagerFactory();
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
            
            // Connect directly to run the SHUTDOWN COMPACT command
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();
            
            // Execute the compact command
            stmt.execute("SHUTDOWN COMPACT");
            
            LOGGER.info("Database compacted successfully");
            
            // Reinitialize the database configuration
            DatabaseConfig.initialize();
            
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error compacting database", e);
            
            // Try to reinitialize in case of failure
            try {
                DatabaseConfig.initialize();
            } catch (Exception re) {
                LOGGER.log(Level.SEVERE, "Failed to reinitialize database after compact error", re);
            }
            
            return false;
        } finally {
            // Close resources
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection after compact", e);
            }
        }
    }
    
    /**
     * Gets the database file path for file-based databases.
     * 
     * @return the database file path, or null if not a file-based database
     */
    private static String getDatabaseFilePath() {
        try {
            // Try to get configuration from DatabaseConfig directly
            if (!DatabaseConfig.isDevelopmentMode()) {
                String dbName = DatabaseConfig.getDatabaseName();
                String userHome = System.getProperty("user.home");
                File dbDir = new File(userHome, ".frcpm");
                File dbFile = new File(dbDir, dbName + ".mv.db");
                
                // Check if the file exists
                if (dbFile.exists()) {
                    return dbFile.getAbsolutePath();
                }
            }
            
            // If direct method doesn't work, try to extract from JDBC URL
            Map<String, Object> props = getJdbcPropertiesFromConfig();
            String url = (String) props.get("jakarta.persistence.jdbc.url");
            
            // Check if this is a file-based database
            if (url != null && url.startsWith("jdbc:h2:file:")) {
                // Extract the file path from the URL
                String path = url.substring("jdbc:h2:file:".length());
                
                // Remove any parameters
                if (path.contains(";")) {
                    path = path.substring(0, path.indexOf(";"));
                }
                
                // Resolve the path if it contains user.home
                if (path.contains("${user.home}")) {
                    String userHome = System.getProperty("user.home");
                    path = path.replace("${user.home}", userHome);
                }
                
                // Add .mv.db extension if missing
                if (!path.endsWith(".mv.db")) {
                    path += ".mv.db";
                }
                
                return path;
            } else if (url == null) {
                // Try to construct the database path from default location
                String dbName = DatabaseConfig.getDatabaseName();
                if (!DatabaseConfig.isDevelopmentMode()) {
                    String userHome = System.getProperty("user.home");
                    File dbDir = new File(userHome, ".frcpm");
                    return new File(dbDir, dbName + ".mv.db").getAbsolutePath();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting database file path", e);
        }
        
        return null; // Not a file-based database or couldn't determine path
    }
    
    /**
     * Checks if the database is in-memory.
     * 
     * @return true if the database is in-memory, false otherwise
     */
    private static boolean isInMemoryDatabase() {
        try {
            // Try to get configuration from DatabaseConfig first
            return DatabaseConfig.isDevelopmentMode(); // Development mode uses in-memory database
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking if database is in-memory", e);
            return false;
        }
    }
    
/**
     * Gets diagnostic information about the database configuration.
     * 
     * @return a string containing diagnostic information
     */
    public static String getDiagnosticInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Database Diagnostic Information\n");
        info.append("==============================\n\n");
        
        try {
            // Get JDBC URL from properties
            Map<String, Object> props = getJdbcPropertiesFromConfig();
            String url = (String) props.get("jakarta.persistence.jdbc.url");
            info.append("JDBC URL: ").append(url).append("\n");
            
            // Database type
            if (url != null) {
                if (url.startsWith("jdbc:h2:file:")) {
                    info.append("Database Type: File-based\n");
                    info.append("Database Path: ").append(getDatabaseFilePath()).append("\n");
                } else if (url.startsWith("jdbc:h2:mem:")) {
                    info.append("Database Type: In-Memory\n");
                } else {
                    info.append("Database Type: Unknown\n");
                }
            } else {
                // Get information from DatabaseConfig directly
                info.append("Database Name: ").append(DatabaseConfig.getDatabaseName()).append("\n");
                info.append("Development Mode: ").append(DatabaseConfig.isDevelopmentMode()).append("\n");
                if (DatabaseConfig.isDevelopmentMode()) {
                    info.append("Database Type: In-Memory\n");
                } else {
                    info.append("Database Type: File-based\n");
                    
                    // Try to construct the database path
                    String userHome = System.getProperty("user.home");
                    File dbDir = new File(userHome, ".frcpm");
                    File dbFile = new File(dbDir, DatabaseConfig.getDatabaseName() + ".mv.db");
                    info.append("Database Path: ").append(dbFile.getAbsolutePath()).append("\n");
                    info.append("Database Exists: ").append(dbFile.exists()).append("\n");
                }
            }
            
            // Connection pool info
            try {
                EntityManagerFactory emf = DatabaseConfig.getEntityManagerFactory();
                if (emf != null && emf.isOpen()) {
                    info.append("EntityManagerFactory: Open\n");
                } else {
                    info.append("EntityManagerFactory: Closed\n");
                }
            } catch (Exception e) {
                info.append("EntityManagerFactory: Error - ").append(e.getMessage()).append("\n");
            }
            
            // Database settings
            try {
                EntityManager em = DatabaseConfig.getEntityManager();
                try {
                    Object closeOnExit = em.createNativeQuery("SELECT SETTING_VALUE('" + CLOSE_ON_EXIT_SETTING + "')")
                                         .getSingleResult();
                    Object closeDelay = em.createNativeQuery("SELECT SETTING_VALUE('" + CLOSE_DELAY_SETTING + "')")
                                        .getSingleResult();
                    Object cacheSize = em.createNativeQuery("SELECT SETTING_VALUE('" + CACHE_SIZE_SETTING + "')")
                                       .getSingleResult();
                    Object lockTimeout = em.createNativeQuery("SELECT SETTING_VALUE('" + LOCK_TIMEOUT_SETTING + "')")
                                         .getSingleResult();
                    
                    info.append("\nDatabase Settings:\n");
                    info.append("- ").append(CLOSE_ON_EXIT_SETTING).append(": ").append(closeOnExit).append("\n");
                    info.append("- ").append(CLOSE_DELAY_SETTING).append(": ").append(closeDelay).append("\n");
                    info.append("- ").append(CACHE_SIZE_SETTING).append(": ").append(cacheSize).append("\n");
                    info.append("- ").append(LOCK_TIMEOUT_SETTING).append(": ").append(lockTimeout).append("\n");
                } catch (Exception e) {
                    // Try a simpler approach
                    info.append("\nCould not retrieve detailed settings: ").append(e.getMessage()).append("\n");
                    info.append("Checking basic connectivity instead...\n");
                    try {
                        Object result = em.createNativeQuery("SELECT 1").getSingleResult();
                        info.append("Basic database connectivity: OK\n");
                    } catch (Exception e2) {
                        info.append("Basic database connectivity: FAILED - ").append(e2.getMessage()).append("\n");
                    }
                } finally {
                    em.close();
                }
            } catch (Exception e) {
                info.append("\nCould not retrieve database settings: ").append(e.getMessage()).append("\n");
            }
            
            // Backup info
            try {
                java.util.List<BackupManager.Backup> backups = BackupManager.getBackups();
                info.append("\nBackups: ").append(backups.size()).append(" available\n");
                if (!backups.isEmpty()) {
                    BackupManager.Backup latestBackup = backups.get(0);
                    info.append("Latest Backup: ").append(latestBackup.getTimestamp())
                        .append(" (").append(latestBackup.getFilePath()).append(")\n");
                }
                
                info.append("Backup Schedule: ");
                if (backupSchedule != null && !backupSchedule.isCancelled()) {
                    info.append("Active\n");
                } else {
                    info.append("Not active\n");
                }
            } catch (Exception e) {
                info.append("\nCould not retrieve backup information: ").append(e.getMessage()).append("\n");
            }
            
        } catch (Exception e) {
            info.append("Error gathering diagnostic information: ").append(e.getMessage()).append("\n");
        }
        
        return info.toString();
    }
}