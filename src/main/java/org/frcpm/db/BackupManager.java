// src/main/java/org/frcpm/db/BackupManager.java
package org.frcpm.db;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.utils.ErrorHandler;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Manages database backups and restoration.
 * Provides functionality for creating, restoring, and managing database backups.
 */
public class BackupManager {
    
    private static final Logger LOGGER = Logger.getLogger(BackupManager.class.getName());
    
    // Date format for backup file timestamps
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    // Default backup directory
    private static final String DEFAULT_BACKUP_DIR = "backups";
    
    // Backup file extension
    private static final String BACKUP_EXTENSION = ".zip";
    
    // Database file extensions to backup
    private static final List<String> DB_EXTENSIONS = Arrays.asList(".mv.db", ".trace.db", ".lock.db");
    
    /**
     * Backup object representing a database backup.
     */
    public static class Backup {
        private final String filePath;
        private final LocalDateTime timestamp;
        private final long fileSize;
        
        /**
         * Creates a new Backup.
         * 
         * @param filePath the path to the backup file
         * @param timestamp the timestamp when the backup was created
         * @param fileSize the size of the backup file in bytes
         */
        public Backup(String filePath, LocalDateTime timestamp, long fileSize) {
            this.filePath = filePath;
            this.timestamp = timestamp;
            this.fileSize = fileSize;
        }
        
        /**
         * Gets the path to the backup file.
         * 
         * @return the file path
         */
        public String getFilePath() {
            return filePath;
        }
        
        /**
         * Gets the timestamp when the backup was created.
         * 
         * @return the timestamp
         */
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        /**
         * Gets the size of the backup file in bytes.
         * 
         * @return the file size
         */
        public long getFileSize() {
            return fileSize;
        }
        
        /**
         * Gets a formatted string of the file size.
         * 
         * @return the formatted file size
         */
        public String getFormattedSize() {
            if (fileSize < 1024) {
                return fileSize + " B";
            } else if (fileSize < 1024 * 1024) {
                return String.format("%.2f KB", fileSize / 1024.0);
            } else if (fileSize < 1024 * 1024 * 1024) {
                return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
            } else {
                return String.format("%.2f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
            }
        }
    }
    
    /**
     * Ensures the backup directory exists.
     * 
     * @return true if the directory exists or was created, false otherwise
     */
    public static boolean ensureBackupDirExists() {
        LOGGER.info("Ensuring backup directory exists");
        
        try {
            // Get backup directory
            File backupDir = getBackupDirectory();
            if (!backupDir.exists()) {
                LOGGER.info("Creating backup directory: " + backupDir.getAbsolutePath());
                boolean created = backupDir.mkdirs();
                if (!created) {
                    LOGGER.severe("Failed to create backup directory: " + backupDir.getAbsolutePath());
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error ensuring backup directory exists", e);
            return false;
        }
    }
    
    /**
     * Creates a backup of the database.
     * 
     * @return the backup, or null if the backup failed
     */
    public static Backup createBackup() {
        LOGGER.info("Creating database backup");
        
        // Skip for in-memory databases
        if (isInMemoryDatabase()) {
            LOGGER.info("Skipping backup for in-memory database");
            return null;
        }
        
        // Get database file path
        String dbFilePath = getDatabaseFilePath();
        if (dbFilePath == null) {
            LOGGER.warning("Cannot create backup: database file path is null");
            return null;
        }
        
        // Ensure backup directory exists
        if (!ensureBackupDirExists()) {
            LOGGER.warning("Cannot create backup: backup directory could not be created");
            return null;
        }
        
        // Generate timestamp for the backup file
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        
        // Generate backup file name
        File dbFile = new File(dbFilePath);
        String dbName = dbFile.getName();
        if (dbName.endsWith(".mv.db")) {
            dbName = dbName.substring(0, dbName.length() - 6); // Remove .mv.db extension
        }
        
        String backupFileName = dbName + "_" + timestamp + BACKUP_EXTENSION;
        File backupDir = getBackupDirectory();
        File backupFile = new File(backupDir, backupFileName);
        
        try {
            // Create a backup using the SCRIPT command or file copy
            boolean backupSuccess;
            
            // First try H2's BACKUP command (more reliable)
            backupSuccess = createBackupUsingH2Backup(dbName, backupFile);
            
            // If that fails, try file copy
            if (!backupSuccess) {
                backupSuccess = createBackupUsingFileCopy(dbFilePath, backupFile);
            }
            
            if (backupSuccess) {
                LOGGER.info("Backup created successfully: " + backupFile.getAbsolutePath());
                return new Backup(
                    backupFile.getAbsolutePath(),
                    LocalDateTime.parse(timestamp, TIMESTAMP_FORMATTER),
                    backupFile.length()
                );
            } else {
                LOGGER.warning("Failed to create backup");
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating backup", e);
            // Delete incomplete backup file if it exists
            if (backupFile.exists()) {
                backupFile.delete();
            }
            return null;
        }
    }
    
    /**
     * Creates a backup using H2's BACKUP command.
     * 
     * @param dbName the database name
     * @param backupFile the backup file
     * @return true if successful, false otherwise
     */
    private static boolean createBackupUsingH2Backup(String dbName, File backupFile) {
        LOGGER.info("Attempting to create backup using H2 BACKUP command");
        
        EntityManager em = null;
        
        try {
            em = DatabaseConfig.getEntityManager();
            em.getTransaction().begin();
            
            // Generate the backup command
            // Format: BACKUP TO 'path/to/backup.zip'
            String backupCommand = "BACKUP TO '" + backupFile.getAbsolutePath().replace('\\', '/') + "'";
            
            // Execute the backup command
            em.createNativeQuery(backupCommand).executeUpdate();
            
            em.getTransaction().commit();
            
            // Verify the backup file was created
            if (backupFile.exists() && backupFile.length() > 0) {
                LOGGER.info("H2 BACKUP command executed successfully");
                return true;
            } else {
                LOGGER.warning("H2 BACKUP command executed, but backup file not found or empty");
                return false;
            }
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.log(Level.WARNING, "Error creating backup using H2 BACKUP command", e);
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    /**
     * Creates a backup by copying the database files.
     * 
     * @param dbFilePath the database file path
     * @param backupFile the backup file
     * @return true if successful, false otherwise
     */
    private static boolean createBackupUsingFileCopy(String dbFilePath, File backupFile) {
        LOGGER.info("Creating backup using file copy");
        
        try {
            // Get the database file
            File dbFile = new File(dbFilePath);
            String basePath = dbFile.getParent();
            String dbName = dbFile.getName();
            if (dbName.endsWith(".mv.db")) {
                dbName = dbName.substring(0, dbName.length() - 6); // Remove .mv.db extension
            }
            
            // Create a temporary zip file
            File tempZipFile = File.createTempFile("backup_", BACKUP_EXTENSION);
            
            // Create a ZIP file with all database files
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
                // Add all database-related files
                for (String ext : DB_EXTENSIONS) {
                    File file = new File(basePath, dbName + ext);
                    if (file.exists()) {
                        LOGGER.fine("Adding to backup: " + file.getName());
                        
                        // Add file to ZIP
                        ZipEntry entry = new ZipEntry(file.getName());
                        zos.putNextEntry(entry);
                        
                        // Copy file data to ZIP
                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, length);
                            }
                        }
                        
                        zos.closeEntry();
                    }
                }
            }
            
            // Move the temporary zip file to the final location
            Files.move(tempZipFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // Verify the backup file
            if (backupFile.exists() && backupFile.length() > 0) {
                LOGGER.info("Backup file created: " + backupFile.getAbsolutePath());
                return true;
            } else {
                LOGGER.warning("Backup file not found or empty");
                return false;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error creating backup using file copy", e);
            return false;
        }
    }
    
    /**
     * Restores the database from a backup.
     * 
     * @param backup the backup to restore
     * @return true if the restore was successful, false otherwise
     */
    public static boolean restoreBackup(Backup backup) {
        LOGGER.info("Restoring database from backup: " + backup.getFilePath());
        
        // Skip for in-memory databases
        if (isInMemoryDatabase()) {
            LOGGER.warning("Cannot restore in-memory database");
            return false;
        }
        
        // Get database file path
        String dbFilePath = getDatabaseFilePath();
        if (dbFilePath == null) {
            LOGGER.warning("Cannot restore backup: database file path is null");
            return false;
        }
        
        // Check if the backup file exists
        File backupFile = new File(backup.getFilePath());
        if (!backupFile.exists()) {
            LOGGER.warning("Backup file does not exist: " + backup.getFilePath());
            return false;
        }
        
        try {
            // Shut down the database
            shutdown();
            
            // Get the database file details
            File dbFile = new File(dbFilePath);
            String basePath = dbFile.getParent();
            String dbName = dbFile.getName();
            if (dbName.endsWith(".mv.db")) {
                dbName = dbName.substring(0, dbName.length() - 6); // Remove .mv.db extension
            }
            
            // Create backups of current database files
            File tempBackupDir = createTempBackup(basePath, dbName);
            
            try {
                // Delete current database files
                deleteCurrentDatabaseFiles(basePath, dbName);
                
                // Extract the backup
                extractBackup(backupFile, basePath);
                
                // Reinitialize the database
                DatabaseConfig.initialize();
                
                // Verify the database is working
                if (verifyDatabaseConnection()) {
                    LOGGER.info("Database restored successfully from backup");
                    return true;
                } else {
                    LOGGER.severe("Database verification failed after restore");
                    // Attempt to restore from temporary backup
                    restoreFromTempBackup(tempBackupDir, basePath);
                    return false;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error restoring database", e);
                // Attempt to restore from temporary backup
                restoreFromTempBackup(tempBackupDir, basePath);
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during backup restoration", e);
            return false;
        }
    }
    
    /**
     * Creates a temporary backup of the current database files.
     * 
     * @param basePath the base path of the database files
     * @param dbName the database name
     * @return the temporary backup directory
     * @throws IOException if an I/O error occurs
     */
    private static File createTempBackup(String basePath, String dbName) throws IOException {
        LOGGER.info("Creating temporary backup of current database files");
        
        // Create a temporary directory
        File tempBackupDir = Files.createTempDirectory("db_restore_backup_").toFile();
        
        // Copy all database files to the temporary directory
        for (String ext : DB_EXTENSIONS) {
            File srcFile = new File(basePath, dbName + ext);
            if (srcFile.exists()) {
                File destFile = new File(tempBackupDir, dbName + ext);
                Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        
        LOGGER.info("Temporary backup created: " + tempBackupDir.getAbsolutePath());
        return tempBackupDir;
    }
    
    /**
     * Deletes the current database files.
     * 
     * @param basePath the base path of the database files
     * @param dbName the database name
     * @throws IOException if an I/O error occurs
     */
    private static void deleteCurrentDatabaseFiles(String basePath, String dbName) throws IOException {
        LOGGER.info("Deleting current database files");
        
        for (String ext : DB_EXTENSIONS) {
            File file = new File(basePath, dbName + ext);
            if (file.exists()) {
                if (!file.delete()) {
                    LOGGER.warning("Could not delete file: " + file.getAbsolutePath());
                }
            }
        }
    }
    
    /**
     * Extracts the backup file to the specified path.
     * 
     * @param backupFile the backup file
     * @param destPath the destination path
     * @throws IOException if an I/O error occurs
     */
    private static void extractBackup(File backupFile, String destPath) throws IOException {
        LOGGER.info("Extracting backup to: " + destPath);
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backupFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];
            
            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName();
                File destFile = new File(destPath, fileName);
                
                // Create output directory if it doesn't exist
                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }
                
                // Extract the file
                try (FileOutputStream fos = new FileOutputStream(destFile)) {
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
                
                zis.closeEntry();
            }
        }
        
        LOGGER.info("Backup extracted successfully");
    }
    
    /**
     * Restores the database from a temporary backup.
     * 
     * @param tempBackupDir the temporary backup directory
     * @param destPath the destination path
     */
    private static void restoreFromTempBackup(File tempBackupDir, String destPath) {
        LOGGER.info("Restoring from temporary backup");
        
        try {
            for (File file : tempBackupDir.listFiles()) {
                File destFile = new File(destPath, file.getName());
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Reinitialize the database
            DatabaseConfig.initialize();
            
            LOGGER.info("Restored from temporary backup successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error restoring from temporary backup", e);
        }
    }
    
    /**
     * Verifies the database connection.
     * 
     * @return true if the connection is valid, false otherwise
     */
    private static boolean verifyDatabaseConnection() {
        LOGGER.info("Verifying database connection");
        
        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();
            
            // Try a simple query
            Object result = em.createNativeQuery("SELECT 1").getSingleResult();
            
            return result != null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database verification failed", e);
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    /**
     * Gets all available backups.
     * 
     * @return a list of backups, sorted by timestamp (newest first)
     */
    public static List<Backup> getBackups() {
        LOGGER.info("Getting all available backups");
        
        List<Backup> backups = new ArrayList<>();
        
        try {
            // Ensure backup directory exists
            if (!ensureBackupDirExists()) {
                LOGGER.warning("Backup directory does not exist");
                return backups;
            }
            
            // Get backup directory
            File backupDir = getBackupDirectory();
            
            // List all backup files
            File[] files = backupDir.listFiles((dir, name) -> name.endsWith(BACKUP_EXTENSION));
            if (files == null || files.length == 0) {
                LOGGER.info("No backups found");
                return backups;
            }
            
            // Parse backup files
            for (File file : files) {
                try {
                    // Extract timestamp from filename
                    String fileName = file.getName();
                    
                    // Format: dbname_yyyyMMdd_HHmmss.zip
                    int timestampStartIndex = fileName.lastIndexOf('_') - 8; // 8 is the length of yyyyMMdd
                    if (timestampStartIndex > 0) {
                        String timestampStr = fileName.substring(
                                timestampStartIndex, fileName.length() - BACKUP_EXTENSION.length());
                        
                        LocalDateTime timestamp = LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
                        
                        backups.add(new Backup(file.getAbsolutePath(), timestamp, file.length()));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error parsing backup file: " + file.getName(), e);
                    // Skip invalid backup files
                }
            }
            
            // Sort backups by timestamp, newest first
            backups.sort(Comparator.comparing(Backup::getTimestamp).reversed());
            
            LOGGER.info("Found " + backups.size() + " backups");
            return backups;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting backups", e);
            return backups;
        }
    }
    
    /**
     * Deletes a backup.
     * 
     * @param backup the backup to delete
     * @return true if the backup was deleted, false otherwise
     */
    public static boolean deleteBackup(Backup backup) {
        LOGGER.info("Deleting backup: " + backup.getFilePath());
        
        try {
            File backupFile = new File(backup.getFilePath());
            if (!backupFile.exists()) {
                LOGGER.warning("Backup file does not exist: " + backup.getFilePath());
                return false;
            }
            
            boolean deleted = backupFile.delete();
            if (deleted) {
                LOGGER.info("Backup deleted successfully");
            } else {
                LOGGER.warning("Failed to delete backup");
            }
            
            return deleted;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting backup", e);
            return false;
        }
    }
    
    /**
     * Cleans up old backups, keeping only the specified number of newest backups.
     * 
     * @param maxBackups the maximum number of backups to keep
     * @return the number of backups deleted
     */
    public static int cleanupOldBackups(int maxBackups) {
        LOGGER.info("Cleaning up old backups, keeping " + maxBackups + " newest backups");
        
        try {
            // Get all backups
            List<Backup> backups = getBackups();
            
            // If we have fewer backups than the maximum, no cleanup needed
            if (backups.size() <= maxBackups) {
                LOGGER.info("No cleanup needed, have " + backups.size() + 
                          " backups, maximum is " + maxBackups);
                return 0;
            }
            
            // Delete oldest backups
            int toDelete = backups.size() - maxBackups;
            int deleted = 0;
            
            for (int i = maxBackups; i < backups.size(); i++) {
                Backup backup = backups.get(i);
                if (deleteBackup(backup)) {
                    deleted++;
                }
            }
            
            LOGGER.info("Deleted " + deleted + " old backups");
            return deleted;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cleaning up old backups", e);
            return 0;
        }
    }
    
    /**
     * Gets the backup directory.
     * 
     * @return the backup directory
     */
    private static File getBackupDirectory() {
        // Get user home directory
        String userHome = System.getProperty("user.home");
        
        // Get database directory (parent of database file)
        String dbPath = getDatabaseFilePath();
        String parentPath;
        
        if (dbPath != null) {
            File dbFile = new File(dbPath);
            parentPath = dbFile.getParent();
        } else {
            // Default to user home directory/.frcpm
            parentPath = userHome + File.separator + ".frcpm";
        }
        
        // Create backup directory path
        return new File(parentPath, DEFAULT_BACKUP_DIR);
    }
    
    /**
     * Gets the database file path.
     * 
     * @return the database file path, or null if it cannot be determined
     */
    private static String getDatabaseFilePath() {
        try {
            // Try to get the database file path from the database configuration
            String url = DatabaseConfig.getConnectionUrl();
            
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
                
                // Add .mv.db extension if not present
                if (!path.endsWith(".mv.db")) {
                    path += ".mv.db";
                }
                
                return path;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting database file path", e);
        }
        
        // If we can't determine the path from the URL, try a default location
        String userHome = System.getProperty("user.home");
        String defaultPath = userHome + File.separator + ".frcpm" + File.separator + 
                           "frcpm" + ".mv.db";
        
        File defaultFile = new File(defaultPath);
        if (defaultFile.exists()) {
            return defaultPath;
        }
        
        return null;
    }
    
    /**
     * Checks if the database is in-memory.
     * 
     * @return true if the database is in-memory, false otherwise
     */
    private static boolean isInMemoryDatabase() {
        try {
            String url = DatabaseConfig.getConnectionUrl();
            return url != null && url.startsWith("jdbc:h2:mem:");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking if database is in-memory", e);
            return false;
        }
    }
    
    /**
     * Shuts down the database connection.
     */
    private static void shutdown() {
        LOGGER.info("Shutting down database");
        
        try {
            // Shutdown EntityManagerFactory
            EntityManagerFactory emf = DatabaseConfig.getEntityManagerFactory();
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
            
            // Execute SHUTDOWN command
            Connection conn = null;
            Statement stmt = null;
            
            try {
                String url = DatabaseConfig.getConnectionUrl();
                String username = DatabaseConfig.getUsername();
                String password = DatabaseConfig.getPassword();
                
                conn = DriverManager.getConnection(url, username, password);
                stmt = conn.createStatement();
                stmt.execute("SHUTDOWN");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error executing SHUTDOWN command", e);
            } finally {
                if (stmt != null) {
                    try { stmt.close(); } catch (SQLException e) { /* Ignore */ }
                }
                if (conn != null) {
                    try { conn.close(); } catch (SQLException e) { /* Ignore */ }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error shutting down database", e);
        }
    }
}