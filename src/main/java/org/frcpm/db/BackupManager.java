package org.frcpm.db;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for managing database backups.
 */
public class BackupManager {
    
    private static final Logger LOGGER = Logger.getLogger(BackupManager.class.getName());
    private static final Pattern BACKUP_PATTERN = Pattern.compile("(.+)_(\\d{8}_\\d{6})\\.zip");
    
    /**
     * Represents a database backup file.
     */
    public static class Backup {
        private final String fileName;
        private final String databaseName;
        private final Date timestamp;
        private final File file;
        
        /**
         * Creates a new backup.
         * 
         * @param fileName the name of the backup file
         * @param databaseName the name of the database
         * @param timestamp the timestamp of the backup
         * @param file the backup file
         */
        public Backup(String fileName, String databaseName, Date timestamp, File file) {
            this.fileName = fileName;
            this.databaseName = databaseName;
            this.timestamp = timestamp;
            this.file = file;
        }
        
        /**
         * Gets the file name.
         * 
         * @return the file name
         */
        public String getFileName() {
            return fileName;
        }
        
        /**
         * Gets the database name.
         * 
         * @return the database name
         */
        public String getDatabaseName() {
            return databaseName;
        }
        
        /**
         * Gets the timestamp.
         * 
         * @return the timestamp
         */
        public Date getTimestamp() {
            return timestamp;
        }
        
        /**
         * Gets the file.
         * 
         * @return the file
         */
        public File getFile() {
            return file;
        }
        
        /**
         * Gets a formatted string representation of the timestamp.
         * 
         * @return the formatted timestamp
         */
        public String getFormattedTimestamp() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.format(timestamp);
        }
        
        @Override
        public String toString() {
            return databaseName + " - " + getFormattedTimestamp();
        }
    }
    
    /**
     * Gets all available backups.
     * 
     * @return a list of backups
     */
    public static List<Backup> getBackups() {
        List<Backup> backups = new ArrayList<>();
        
        // Get backup directory
        String userHome = System.getProperty("user.home");
        File backupDir = new File(userHome, ".frcpm/backups");
        
        // Check if directory exists
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return backups;
        }
        
        // Parse backup files
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        
        for (File file : backupDir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".zip")) {
                Matcher matcher = BACKUP_PATTERN.matcher(file.getName());
                if (matcher.matches()) {
                    String databaseName = matcher.group(1);
                    String timestampStr = matcher.group(2);
                    
                    try {
                        Date timestamp = dateFormat.parse(timestampStr);
                        backups.add(new Backup(file.getName(), databaseName, timestamp, file));
                    } catch (ParseException e) {
                        LOGGER.log(Level.WARNING, "Error parsing backup timestamp: " + timestampStr, e);
                    }
                }
            }
        }
        
        return backups;
    }
    
    /**
     * Creates a backup of the current database.
     * 
     * @return the created backup, or null if the backup failed
     */
    public static Backup createBackup() {
        String backupPath = DatabaseConfigurer.createBackup();
        if (backupPath == null) {
            return null;
        }
        
        File backupFile = new File(backupPath);
        
        // Parse backup file name
        Matcher matcher = BACKUP_PATTERN.matcher(backupFile.getName());
        if (matcher.matches()) {
            String databaseName = matcher.group(1);
            String timestampStr = matcher.group(2);
            
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                Date timestamp = dateFormat.parse(timestampStr);
                return new Backup(backupFile.getName(), databaseName, timestamp, backupFile);
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing backup timestamp: " + timestampStr, e);
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Restores a backup.
     * 
     * @param backup the backup to restore
     * @return true if the restore was successful, false otherwise
     */
    public static boolean restoreBackup(Backup backup) {
        if (backup == null || !backup.getFile().exists()) {
            return false;
        }
        
        return DatabaseConfigurer.restoreFromBackup(backup.getFile().getAbsolutePath());
    }
    
    /**
     * Deletes a backup.
     * 
     * @param backup the backup to delete
     * @return true if the delete was successful, false otherwise
     */
    public static boolean deleteBackup(Backup backup) {
        if (backup == null || !backup.getFile().exists()) {
            return false;
        }
        
        return backup.getFile().delete();
    }
    
    /**
     * Exports a backup to a specified location.
     * 
     * @param backup the backup to export
     * @param destination the destination file
     * @return true if the export was successful, false otherwise
     */
    public static boolean exportBackup(Backup backup, File destination) {
        if (backup == null || !backup.getFile().exists()) {
            return false;
        }
        
        try {
            Files.copy(backup.getFile().toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting backup", e);
            return false;
        }
    }
    
    /**
     * Imports a backup from a specified location.
     * 
     * @param source the source file
     * @return the imported backup, or null if the import failed
     */
    public static Backup importBackup(File source) {
        if (source == null || !source.exists() || !source.isFile()) {
            return null;
        }
        
        // Get backup directory
        String userHome = System.getProperty("user.home");
        File backupDir = new File(userHome, ".frcpm/backups");
        
        // Create backup directory if it doesn't exist
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        
        // Create destination file
        File destination = new File(backupDir, source.getName());
        
        try {
            // Copy backup file
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // Parse backup file name
            Matcher matcher = BACKUP_PATTERN.matcher(destination.getName());
            if (matcher.matches()) {
                String databaseName = matcher.group(1);
                String timestampStr = matcher.group(2);
                
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    Date timestamp = dateFormat.parse(timestampStr);
                    return new Backup(destination.getName(), databaseName, timestamp, destination);
                } catch (ParseException e) {
                    LOGGER.log(Level.WARNING, "Error parsing backup timestamp: " + timestampStr, e);
                    return null;
                }
            }
            
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error importing backup", e);
            return null;
        }
    }
    
    /**
     * Cleans up old backups, keeping only the specified number of most recent backups.
     * 
     * @param maxBackups the maximum number of backups to keep
     * @return the number of backups deleted
     */
    public static int cleanupOldBackups(int maxBackups) {
        List<Backup> backups = getBackups();
        
        // If we have fewer backups than the maximum, no cleanup needed
        if (backups.size() <= maxBackups) {
            return 0;
        }
        
        // Sort backups by timestamp (newest first)
        backups.sort((b1, b2) -> b2.getTimestamp().compareTo(b1.getTimestamp()));
        
        // Delete oldest backups
        int deleted = 0;
        for (int i = maxBackups; i < backups.size(); i++) {
            if (deleteBackup(backups.get(i))) {
                deleted++;
            }
        }
        
        return deleted;
    }
}