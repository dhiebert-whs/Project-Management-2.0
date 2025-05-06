package org.frcpm.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for managing database backups.
 */
public class BackupManager {
    
    private static final Logger LOGGER = Logger.getLogger(BackupManager.class.getName());
    private static final Pattern BACKUP_PATTERN = Pattern.compile("(.+)_(\\d{8}_\\d{6})\\.zip");
    private static final String BACKUP_DIR_PATH = System.getProperty("user.home") + "/.frcpm/backups";
    private static final int BUFFER_SIZE = 4096;
    
    /**
     * Represents a database backup file.
     */
    public static class Backup implements Comparable<Backup> {
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
        
        /**
         * Gets the file size in bytes.
         * 
         * @return the file size
         */
        public long getFileSize() {
            return file.length();
        }
        
        /**
         * Gets a formatted representation of the file size.
         * 
         * @return the formatted file size
         */
        public String getFormattedFileSize() {
            long size = getFileSize();
            if (size < 1024) {
                return size + " B";
            } else if (size < 1024 * 1024) {
                return String.format("%.2f KB", size / 1024.0);
            } else {
                return String.format("%.2f MB", size / (1024.0 * 1024.0));
            }
        }
        
        @Override
        public String toString() {
            return databaseName + " - " + getFormattedTimestamp() + " (" + getFormattedFileSize() + ")";
        }
        
        @Override
        public int compareTo(Backup other) {
            // Sort from newest to oldest
            return other.timestamp.compareTo(this.timestamp);
        }
    }
    
    /**
     * Gets all available backups sorted by timestamp (newest first).
     * 
     * @return a list of backups
     */
    public static List<Backup> getBackups() {
        List<Backup> backups = new ArrayList<>();
        
        // Get backup directory
        File backupDir = new File(BACKUP_DIR_PATH);
        
        // Check if directory exists
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return backups;
        }
        
        // Parse backup files
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        
        File[] files = backupDir.listFiles();
        if (files != null) {
            for (File file : files) {
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
        }
        
        // Sort backups by timestamp (newest first)
        backups.sort(Comparator.naturalOrder());
        
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
            LOGGER.warning("Failed to create backup");
            return null;
        }
        
        File backupFile = new File(backupPath);
        if (!backupFile.exists()) {
            LOGGER.warning("Backup file not created at expected path: " + backupPath);
            return null;
        }
        
        // Parse backup file name
        Matcher matcher = BACKUP_PATTERN.matcher(backupFile.getName());
        if (matcher.matches()) {
            String databaseName = matcher.group(1);
            String timestampStr = matcher.group(2);
            
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                Date timestamp = dateFormat.parse(timestampStr);
                Backup backup = new Backup(backupFile.getName(), databaseName, timestamp, backupFile);
                LOGGER.info("Created backup: " + backup);
                return backup;
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing backup timestamp: " + timestampStr, e);
                return null;
            }
        } else {
            LOGGER.warning("Backup file name does not match expected pattern: " + backupFile.getName());
            return null;
        }
    }
    
    /**
     * Restores a backup.
     * 
     * @param backup the backup to restore
     * @return true if the restore was successful, false otherwise
     */
    public static boolean restoreBackup(Backup backup) {
        if (backup == null || !backup.getFile().exists()) {
            LOGGER.warning("Cannot restore non-existent backup");
            return false;
        }
        
        LOGGER.info("Restoring backup: " + backup);
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
            LOGGER.warning("Cannot delete non-existent backup");
            return false;
        }
        
        boolean success = backup.getFile().delete();
        if (success) {
            LOGGER.info("Deleted backup: " + backup);
        } else {
            LOGGER.warning("Failed to delete backup: " + backup);
        }
        
        return success;
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
            LOGGER.warning("Cannot export non-existent backup");
            return false;
        }
        
        try {
            Files.copy(backup.getFile().toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Exported backup to: " + destination.getAbsolutePath());
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
            LOGGER.warning("Cannot import non-existent backup file");
            return null;
        }
        
        // Get backup directory
        File backupDir = new File(BACKUP_DIR_PATH);
        
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
                    Backup backup = new Backup(destination.getName(), databaseName, timestamp, destination);
                    LOGGER.info("Imported backup: " + backup);
                    return backup;
                } catch (ParseException e) {
                    LOGGER.log(Level.WARNING, "Error parsing backup timestamp: " + timestampStr, e);
                    return null;
                }
            } else {
                LOGGER.warning("Imported file name does not match backup pattern: " + source.getName());
                return null;
            }
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
        
        // Delete oldest backups
        int deleted = 0;
        for (int i = maxBackups; i < backups.size(); i++) {
            if (deleteBackup(backups.get(i))) {
                deleted++;
            }
        }
        
        LOGGER.info("Cleaned up " + deleted + " old backups, keeping " + maxBackups + " recent backups");
        return deleted;
    }
    
    /**
     * Creates a compressed backup of the database.
     * 
     * @param databasePath the path to the database file
     * @param backupPath the path to save the compressed backup
     * @return true if the backup was successful, false otherwise
     */
    public static boolean createCompressedBackup(String databasePath, String backupPath) {
        try {
            // Create output ZIP file
            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(backupPath))) {
                File databaseFile = new File(databasePath);
                
                // Only backup the file if it exists
                if (databaseFile.exists()) {
                    try (FileInputStream fis = new FileInputStream(databaseFile)) {
                        ZipEntry zipEntry = new ZipEntry(databaseFile.getName());
                        zipOut.putNextEntry(zipEntry);
                        
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zipOut.write(buffer, 0, length);
                        }
                    }
                } else {
                    LOGGER.warning("Database file not found for backup: " + databasePath);
                    return false;
                }
            }
            
            LOGGER.info("Compressed backup created at: " + backupPath);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating compressed backup", e);
            return false;
        }
    }
    
    /**
     * Extracts a compressed backup.
     * 
     * @param backupPath the path to the compressed backup
     * @param extractPath the path to extract the backup to
     * @return true if the extraction was successful, false otherwise
     */
    public static boolean extractCompressedBackup(String backupPath, String extractPath) {
        try {
            File backupFile = new File(backupPath);
            
            // Verify backup file exists
            if (!backupFile.exists()) {
                LOGGER.warning("Backup file does not exist: " + backupPath);
                return false;
            }
            
            // Create extraction directory if it doesn't exist
            File extractDir = new File(extractPath);
            if (!extractDir.exists()) {
                extractDir.mkdirs();
            }
            
            // Extract files from ZIP
            try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(backupFile))) {
                ZipEntry entry = zipIn.getNextEntry();
                
                while (entry != null) {
                    File outputFile = new File(extractPath, entry.getName());
                    
                    // Create output directory if it doesn't exist
                    if (entry.isDirectory()) {
                        outputFile.mkdirs();
                    } else {
                        // Create parent directories if they don't exist
                        if (outputFile.getParentFile() != null) {
                            outputFile.getParentFile().mkdirs();
                        }
                        
                        // Extract file
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int length;
                            while ((length = zipIn.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                        }
                    }
                    
                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                }
            }
            
            LOGGER.info("Backup extracted to: " + extractPath);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error extracting compressed backup", e);
            return false;
        }
    }
    
    /**
     * Gets the backup directory path.
     * 
     * @return the backup directory path
     */
    public static String getBackupDirPath() {
        return BACKUP_DIR_PATH;
    }
    
    /**
     * Ensures the backup directory exists.
     * 
     * @return true if the directory exists or was created, false otherwise
     */
    public static boolean ensureBackupDirExists() {
        File backupDir = new File(BACKUP_DIR_PATH);
        if (!backupDir.exists()) {
            boolean created = backupDir.mkdirs();
            if (created) {
                LOGGER.info("Created backup directory: " + BACKUP_DIR_PATH);
            } else {
                LOGGER.warning("Failed to create backup directory: " + BACKUP_DIR_PATH);
            }
            return created;
        }
        return true;
    }
}