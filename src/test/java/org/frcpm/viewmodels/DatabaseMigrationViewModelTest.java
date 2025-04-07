package org.frcpm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.frcpm.utils.DatabaseMigrationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for the DatabaseMigrationViewModel class.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DatabaseMigrationViewModelTest {
    
    @Mock
    private DatabaseMigrationUtil migrationUtil;
    
    private DatabaseMigrationViewModel viewModel;
    
    @BeforeEach
    public void setUp() {
        viewModel = new DatabaseMigrationViewModel(migrationUtil);
    }
    
    @Test
    public void testInitialState() {
        // Initial properties should have default values
        assertEquals("", viewModel.getSourceDbPath());
        assertEquals("Ready", viewModel.getProgressText());
        assertEquals("", viewModel.getLogText());
        assertTrue(viewModel.isMigrateButtonDisabled());
        assertTrue(viewModel.isCloseButtonDisabled());
        assertFalse(viewModel.isMigrationInProgress());
        assertFalse(viewModel.isErrorBoxVisible());
        assertTrue(viewModel.getErrorList().isEmpty());
        assertEquals(0.0, viewModel.getProgressValue());
    }
    
    @Test
    public void testSetSourceDbPath() {
        // When: Setting a valid source path
        viewModel.setSourceDbPath("test/path/database.db");
        
        // Then: The path should be updated and migrate button enabled
        assertEquals("test/path/database.db", viewModel.getSourceDbPath());
        assertFalse(viewModel.isMigrateButtonDisabled());
    }
    
    @Test
    public void testSetEmptySourceDbPath() {
        // When: Setting an empty source path
        viewModel.setSourceDbPath("");
        
        // Then: The path should be updated but migrate button should remain disabled
        assertEquals("", viewModel.getSourceDbPath());
        assertTrue(viewModel.isMigrateButtonDisabled());
    }
    
    @Test
    public void testCanMigrate() {
        // When: Source path is empty
        viewModel.setSourceDbPath("");
        
        // Then: Cannot migrate
        assertFalse(viewModel.canMigrate());
        
        // When: Source path is set and migration is not in progress
        viewModel.setSourceDbPath("test/path/database.db");
        viewModel.setMigrationInProgress(false);
        
        // Then: Can migrate
        assertTrue(viewModel.canMigrate());
        
        // When: Migration is in progress
        viewModel.setMigrationInProgress(true);
        
        // Then: Cannot migrate
        assertFalse(viewModel.canMigrate());
    }
    
    @Test
    public void testLogMessage() {
        // When: Logging a message
        viewModel.logMessage("Test message");
        
        // Then: The message should be added to the log text
        assertEquals("Test message\n", viewModel.getLogText());
        
        // When: Logging another message
        viewModel.logMessage("Another test message");
        
        // Then: The message should be appended to the log text
        assertEquals("Test message\nAnother test message\n", viewModel.getLogText());
    }
    
    @Test
    public void testMigrateWithEmptyPath() {
        // When: Trying to migrate with an empty path
        viewModel.setSourceDbPath("");
        viewModel.migrate();
        
        // Then: An error message should be set
        assertEquals("Source database path cannot be empty", viewModel.getErrorMessage());
    }
    
    @Test
    public void testMigrateWithSuccess() throws Exception {
        // Setup mock
        when(migrationUtil.migrateFromSqlite(anyString())).thenReturn(true);
        
        // When: Setting a valid source path
        viewModel.setSourceDbPath("test/path/database.db");
        
        // Save initial state for comparison
        boolean initialMigrationInProgress = viewModel.isMigrationInProgress();
        boolean initialErrorBoxVisible = viewModel.isErrorBoxVisible();
        int initialErrorListSize = viewModel.getErrorList().size();
        
        // When: Triggering migration
        viewModel.migrate();
        
        // Then: Migration should be in progress and UI state updated
        assertTrue(viewModel.isMigrationInProgress());
        assertFalse(viewModel.isErrorBoxVisible());
        assertEquals(0, viewModel.getErrorList().size());
        assertEquals(0.0, viewModel.getProgressValue());
        assertEquals("0.0%", viewModel.getProgressText());
        assertEquals("", viewModel.getLogText());
        assertEquals("", viewModel.getErrorMessage());
        
        // Note: We can't easily test the background thread completion in a unit test
        // A more comprehensive test would require integration testing or special thread handling
    }
    
    @Test
    public void testMigrateWithError() throws Exception {
        // Setup mock
        when(migrationUtil.migrateFromSqlite(anyString())).thenThrow(new RuntimeException("Test exception"));
        
        // When: Setting a valid source path
        viewModel.setSourceDbPath("test/path/database.db");
        
        // When: Triggering migration
        viewModel.migrate();
        
        // Then: Migration should be in progress initially
        assertTrue(viewModel.isMigrationInProgress());
        
        // Note: We can't easily test the background thread failure in a unit test
        // A more comprehensive test would require integration testing or special thread handling
    }
    
    @Test
    public void testPropertyBindings() {
        // Test that property bindings work correctly
        
        // Source path property
        viewModel.sourceDbPathProperty().set("test/path/database.db");
        assertEquals("test/path/database.db", viewModel.getSourceDbPath());
        
        // Progress text property
        viewModel.progressTextProperty().set("50.0%");
        assertEquals("50.0%", viewModel.getProgressText());
        
        // Log text property
        viewModel.logTextProperty().set("Log message");
        assertEquals("Log message", viewModel.getLogText());
        
        // Migrate button disabled property
        viewModel.migrateButtonDisabledProperty().set(false);
        assertFalse(viewModel.isMigrateButtonDisabled());
        
        // Close button disabled property
        viewModel.closeButtonDisabledProperty().set(false);
        assertFalse(viewModel.isCloseButtonDisabled());
        
        // Migration in progress property
        viewModel.migrationInProgressProperty().set(true);
        assertTrue(viewModel.isMigrationInProgress());
        
        // Error box visible property
        viewModel.errorBoxVisibleProperty().set(true);
        assertTrue(viewModel.isErrorBoxVisible());
        
        // Progress value property
        viewModel.progressValueProperty().set(0.5);
        assertEquals(0.5, viewModel.getProgressValue());
    }
    
    @Test
    public void testCommandsNotNull() {
        // All commands should be initialized
        assertNotNull(viewModel.getBrowseSourceDbCommand());
        assertNotNull(viewModel.getMigrateCommand());
        assertNotNull(viewModel.getCloseCommand());
    }
}