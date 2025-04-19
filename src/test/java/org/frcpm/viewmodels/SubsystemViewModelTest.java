package org.frcpm.viewmodels;

import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the SubsystemViewModel.
 */
public class SubsystemViewModelTest {

    @Mock
    private SubsystemService subsystemService;

    @Mock
    private SubteamService subteamService;

    @Mock
    private TaskService taskService;

    private SubsystemViewModel viewModel;
    private Subsystem testSubsystem;
    private Subteam testSubteam;
    private List<Subsystem> testSubsystems;
    private List<Subteam> testSubteams;
    private List<Task> testTasks;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test data
        testSubteam = new Subteam("Test Subteam", "#FF0000");
        testSubteam.setId(1L);
        testSubteam.setSpecialties("Testing");

        testSubsystem = new Subsystem("Test Subsystem");
        testSubsystem.setId(1L);
        testSubsystem.setDescription("Test Description");
        testSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
        testSubsystem.setResponsibleSubteam(testSubteam);

        testSubsystems = new ArrayList<>();
        testSubsystems.add(testSubsystem);

        testSubteams = new ArrayList<>();
        testSubteams.add(testSubteam);

        // Create test tasks
        testTasks = new ArrayList<>();
        Task task1 = new Task("Test Task 1", null, testSubsystem);
        task1.setId(1L);
        task1.setProgress(0);
        task1.setCompleted(false);
        task1.setEndDate(LocalDate.now().plusDays(7));

        Task task2 = new Task("Test Task 2", null, testSubsystem);
        task2.setId(2L);
        task2.setProgress(50);
        task2.setCompleted(false);
        task2.setEndDate(LocalDate.now().plusDays(14));

        Task task3 = new Task("Test Task 3", null, testSubsystem);
        task3.setId(3L);
        task3.setProgress(100);
        task3.setCompleted(true);
        task3.setEndDate(LocalDate.now().minusDays(3));

        testTasks.add(task1);
        testTasks.add(task2);
        testTasks.add(task3);

        // Configure mocks BEFORE creating the ViewModel
        when(subsystemService.findAll()).thenReturn(testSubsystems);
        when(subteamService.findAll()).thenReturn(testSubteams);
        when(taskService.findBySubsystem(testSubsystem)).thenReturn(testTasks);

        // Create ViewModel
        viewModel = new SubsystemViewModel(subsystemService, subteamService, taskService);
    }

    @Test
    public void testInitialState() {
        // Re-create viewModel without calling loadSubsystems/loadSubteams in constructor
        // for clean initial state test
        SubsystemViewModel cleanViewModel = spy(new SubsystemViewModel(subsystemService, subteamService, taskService));
        doNothing().when(cleanViewModel).loadSubsystems();
        doNothing().when(cleanViewModel).loadSubteams();
        
        // Verify initial state
        assertEquals("", cleanViewModel.getSubsystemName());
        assertEquals("", cleanViewModel.getSubsystemDescription());
        assertEquals(Subsystem.Status.NOT_STARTED, cleanViewModel.getStatus());
        assertNull(cleanViewModel.getResponsibleSubteam());
        assertFalse(cleanViewModel.isValid());
    }

    @Test
    public void testLoadSubsystems() {
        // Clear and reload to verify the method works correctly
        viewModel.getSubsystems().clear();
        assertEquals(0, viewModel.getSubsystems().size());
        
        // Execute command
        viewModel.loadSubsystems();

        // Verify subsystems are loaded
        assertEquals(1, viewModel.getSubsystems().size());
        assertEquals("Test Subsystem", viewModel.getSubsystems().get(0).getName());
        
        // Verify service was called
        verify(subsystemService, atLeast(1)).findAll();
    }

    @Test
    public void testLoadSubteams() {
        // Clear and reload to verify the method works correctly
        viewModel.getAvailableSubteams().clear();
        assertEquals(0, viewModel.getAvailableSubteams().size());
        
        // Execute command
        viewModel.loadSubteams();

        // Verify subteams are loaded
        assertEquals(1, viewModel.getAvailableSubteams().size());
        assertEquals("Test Subteam", viewModel.getAvailableSubteams().get(0).getName());
        
        // Verify service was called
        verify(subteamService, atLeast(1)).findAll();
    }

    @Test
    public void testInitNewSubsystem() {
        // First select an existing subsystem
        viewModel.setSelectedSubsystem(testSubsystem);
        
        // Then initialize new subsystem
        viewModel.initNewSubsystem();

        // Verify state
        assertTrue(viewModel.isNewSubsystem());
        assertEquals("", viewModel.getSubsystemName());
        assertEquals("", viewModel.getSubsystemDescription());
        assertEquals(Subsystem.Status.NOT_STARTED, viewModel.getStatus());
        assertNull(viewModel.getResponsibleSubteam());
        assertFalse(viewModel.isValid());
        assertFalse(viewModel.isDirty());
        
        // When initializing a new subsystem, error message should be cleared
        assertNull(viewModel.getErrorMessage());
        
        // Verify task data is cleared
        assertEquals(0, viewModel.getTasks().size());
        assertEquals(0, viewModel.getTotalTasks());
        assertEquals(0, viewModel.getCompletedTasks());
        assertEquals(0.0, viewModel.getCompletionPercentage());
    }

    @Test
    public void testInitExistingSubsystem() {
        // Mock task service for this specific test
        when(taskService.findBySubsystem(testSubsystem)).thenReturn(testTasks);
        
        // Initialize existing subsystem
        viewModel.initExistingSubsystem(testSubsystem);

        // Verify state
        assertFalse(viewModel.isNewSubsystem());
        assertEquals("Test Subsystem", viewModel.getSubsystemName());
        assertEquals("Test Description", viewModel.getSubsystemDescription());
        assertEquals(Subsystem.Status.IN_PROGRESS, viewModel.getStatus());
        assertEquals(testSubteam, viewModel.getResponsibleSubteam());
        assertFalse(viewModel.isDirty());
        assertNull(viewModel.getErrorMessage());
        
        // Verify tasks are loaded
        assertEquals(3, viewModel.getTasks().size());
        assertEquals(3, viewModel.getTotalTasks());
        assertEquals(1, viewModel.getCompletedTasks());
        // Note: 1/3 = 33.33...% - Using delta for floating point comparison
        assertEquals(33.33, viewModel.getCompletionPercentage(), 0.01);
        
        // Verify services were called
        verify(taskService, atLeastOnce()).findBySubsystem(testSubsystem);
    }

    @Test
    public void testValidation_ValidSubsystem() {
        // Set valid values
        viewModel.setSubsystemName("Test Subsystem");

        // Check validation
        assertTrue(viewModel.isValid());
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    public void testValidation_MissingName() {
        // Set empty name - using setter that marks dirty flag
        viewModel.setSubsystemName("");
        
        // Trigger validation - validation is not automatically triggered
        // when setting values in some test environments
        viewModel.validate();
        
        // Check validation
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Subsystem name is required"));
    }

    @Test
    public void testSaveCommand_NewSubsystem() {
        // Configure mock
        when(subsystemService.createSubsystem(anyString(), anyString(), any(Subsystem.Status.class), any()))
                .thenReturn(testSubsystem);

        // Set valid values for new subsystem
        viewModel.initNewSubsystem();
        viewModel.setSubsystemName("New Subsystem");
        viewModel.setSubsystemDescription("New Description");
        viewModel.setStatus(Subsystem.Status.NOT_STARTED);
        viewModel.setResponsibleSubteam(testSubteam);

        // Save subsystem
        viewModel.getSaveCommand().execute();

        // Verify service calls
        verify(subsystemService).createSubsystem(
                eq("New Subsystem"),
                eq("New Description"),
                eq(Subsystem.Status.NOT_STARTED),
                eq(1L)
        );
        
        // Verify dirty flag is cleared
        assertFalse(viewModel.isDirty());
        
        // Verify error message is cleared
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    public void testSaveCommand_ExistingSubsystem() {
        // Configure mocks
        when(subsystemService.updateStatus(anyLong(), any(Subsystem.Status.class)))
                .thenReturn(testSubsystem);
        when(subsystemService.assignResponsibleSubteam(anyLong(), any()))
                .thenReturn(testSubsystem);
        when(subsystemService.save(any(Subsystem.class)))
                .thenReturn(testSubsystem);
        when(taskService.findBySubsystem(any(Subsystem.class)))
                .thenReturn(testTasks);

        // Set up existing subsystem
        viewModel.initExistingSubsystem(testSubsystem);

        // Modify subsystem - setting name marks it dirty
        viewModel.setSubsystemName("Updated Subsystem");
        viewModel.setSubsystemDescription("Updated Description");
        viewModel.setStatus(Subsystem.Status.COMPLETED);

        // Save subsystem
        viewModel.getSaveCommand().execute();

        // Verify service calls
        verify(subsystemService).updateStatus(eq(1L), eq(Subsystem.Status.COMPLETED));
        verify(subsystemService).assignResponsibleSubteam(eq(1L), eq(1L));
        verify(subsystemService).save(any(Subsystem.class));
        
        // Verify dirty flag is cleared
        assertFalse(viewModel.isDirty());
        
        // Verify error message is cleared
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    public void testDeleteCommand() {
        // Configure mocks
        when(taskService.findBySubsystem(testSubsystem)).thenReturn(new ArrayList<>());
        when(subsystemService.deleteById(anyLong())).thenReturn(true);

        // Set up existing subsystem
        viewModel.initExistingSubsystem(testSubsystem);

        // Initially the delete command should be executable
        assertTrue(viewModel.getDeleteCommand().canExecute());

        // Delete subsystem
        viewModel.getDeleteCommand().execute();

        // Verify service calls
        verify(subsystemService).deleteById(eq(1L));

        // After deletion, the subsystem should no longer be selected
        assertNull(viewModel.getSelectedSubsystem());
        
        // Verify error message is cleared
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    public void testDeleteCommand_WithTasks() {
        // Set up existing subsystem
        viewModel.initExistingSubsystem(testSubsystem);

        // Initially the delete command should be executable
        assertTrue(viewModel.getDeleteCommand().canExecute());

        // Delete subsystem
        viewModel.getDeleteCommand().execute();

        // Verify service calls - deleteById should not be called
        verify(subsystemService, never()).deleteById(anyLong());
        
        // Error message should be set
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Cannot delete subsystem that has tasks"));
    }

    @Test
    public void testUpdateStatusCommand() {
        // Configure mock
        when(subsystemService.updateStatus(anyLong(), any(Subsystem.Status.class)))
                .thenReturn(testSubsystem);
        when(taskService.findBySubsystem(any(Subsystem.class)))
                .thenReturn(testTasks);

        // Set up existing subsystem
        viewModel.initExistingSubsystem(testSubsystem);

        // Initially the update status command should be executable
        assertTrue(viewModel.getUpdateStatusCommand().canExecute());

        // Change status
        viewModel.setStatus(Subsystem.Status.COMPLETED);

        // Update status
        viewModel.getUpdateStatusCommand().execute();

        // Verify service calls
        verify(subsystemService).updateStatus(eq(1L), eq(Subsystem.Status.COMPLETED));
        
        // Verify dirty flag is cleared
        assertFalse(viewModel.isDirty());
        
        // Verify error message is cleared
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    public void testFindByName() {
        // Configure mock
        when(subsystemService.findByName(anyString()))
                .thenReturn(Optional.of(testSubsystem));

        // Find subsystem by name
        Subsystem found = viewModel.findByName("Test Subsystem");

        // Verify result
        assertNotNull(found);
        assertEquals("Test Subsystem", found.getName());
        
        // Verify service was called
        verify(subsystemService).findByName(eq("Test Subsystem"));
        
        // Verify error message is cleared
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    public void testFindByName_NotFound() {
        // Configure mock
        when(subsystemService.findByName(anyString()))
                .thenReturn(Optional.empty());

        // Find subsystem by name
        Subsystem found = viewModel.findByName("Nonexistent Subsystem");

        // Verify result
        assertNull(found);
        
        // Verify service was called
        verify(subsystemService).findByName(eq("Nonexistent Subsystem"));
        
        // Verify error message is cleared
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    public void testLoadTasks() {
        // Set up existing subsystem
        viewModel.initExistingSubsystem(testSubsystem);

        // Clear tasks
        viewModel.getTasks().clear();
        assertEquals(0, viewModel.getTasks().size());

        // Load tasks
        viewModel.loadTasks();

        // Verify tasks are loaded
        assertEquals(3, viewModel.getTasks().size());
        assertEquals(3, viewModel.getTotalTasks());
        assertEquals(1, viewModel.getCompletedTasks());
        assertEquals(33.33, viewModel.getCompletionPercentage(), 0.01);
        
        // Verify service was called
        verify(taskService, atLeastOnce()).findBySubsystem(testSubsystem);
        
        // Verify error message is cleared
        assertNull(viewModel.getErrorMessage());
    }
}