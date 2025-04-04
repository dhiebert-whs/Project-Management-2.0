package org.frcpm.viewmodels;

import org.frcpm.binding.Command;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubsystemManagementViewModel.
 */
public class SubsystemManagementViewModelTest {
    
    @Mock
    private SubsystemService subsystemService;
    
    @Mock
    private TaskService taskService;
    
    private SubsystemManagementViewModel viewModel;
    
    // Test data
    private Subsystem testSubsystem1;
    private Subsystem testSubsystem2;
    private Subteam testSubteam;
    private List<Subsystem> testSubsystems;
    private List<Task> testTasks;
    private List<Task> emptyTasks;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test data
        testSubteam = new Subteam("Test Subteam", "#FF0000");
        testSubteam.setId(1L);
        
        testSubsystem1 = new Subsystem("Test Subsystem 1");
        testSubsystem1.setId(1L);
        testSubsystem1.setDescription("First test subsystem");
        testSubsystem1.setStatus(Subsystem.Status.IN_PROGRESS);
        testSubsystem1.setResponsibleSubteam(testSubteam);
        
        testSubsystem2 = new Subsystem("Test Subsystem 2");
        testSubsystem2.setId(2L);
        testSubsystem2.setDescription("Second test subsystem");
        testSubsystem2.setStatus(Subsystem.Status.NOT_STARTED);
        
        testSubsystems = new ArrayList<>();
        testSubsystems.add(testSubsystem1);
        testSubsystems.add(testSubsystem2);
        
        // Create test tasks
        testTasks = new ArrayList<>();
        Task task1 = new Task("Task 1", null, testSubsystem1);
        task1.setId(1L);
        task1.setProgress(0);
        task1.setCompleted(false);
        
        Task task2 = new Task("Task 2", null, testSubsystem1);
        task2.setId(2L);
        task2.setProgress(100);
        task2.setCompleted(true);
        
        testTasks.add(task1);
        testTasks.add(task2);
        
        emptyTasks = new ArrayList<>();
        
        // Configure mocks
        when(subsystemService.findAll()).thenReturn(testSubsystems);
        when(taskService.findBySubsystem(testSubsystem1)).thenReturn(testTasks);
        when(taskService.findBySubsystem(testSubsystem2)).thenReturn(emptyTasks);
        
        // Create ViewModel
        viewModel = new SubsystemManagementViewModel(subsystemService, taskService);
    }
    
    @Test
    public void testLoadSubsystems() {
        // Verify initial state
        assertEquals(2, viewModel.getSubsystems().size());
        assertEquals("Test Subsystem 1", viewModel.getSubsystems().get(0).getName());
        assertEquals("Test Subsystem 2", viewModel.getSubsystems().get(1).getName());
        
        // Clear list and reload
        viewModel.getSubsystems().clear();
        assertEquals(0, viewModel.getSubsystems().size());
        
        viewModel.getLoadSubsystemsCommand().execute();
        
        // Verify reload
        assertEquals(2, viewModel.getSubsystems().size());
        verify(subsystemService, times(2)).findAll();
    }
    
    @Test
    public void testSelectSubsystem() {
        // Initially no selection
        assertNull(viewModel.getSelectedSubsystem());
        
        // Set selection
        viewModel.setSelectedSubsystem(testSubsystem1);
        
        // Verify selection
        assertEquals(testSubsystem1, viewModel.getSelectedSubsystem());
    }
    
    @Test
    public void testDeleteSubsystem_Success() {
        // Setup
        when(subsystemService.deleteById(anyLong())).thenReturn(true);
        
        // Select subsystem with no tasks
        viewModel.setSelectedSubsystem(testSubsystem2);
        when(taskService.findBySubsystem(testSubsystem2)).thenReturn(emptyTasks);
        
        // Execute command
        Command deleteCommand = viewModel.getDeleteSubsystemCommand();
        assertTrue(deleteCommand.canExecute());
        deleteCommand.execute();
        
        // Verify
        verify(subsystemService).deleteById(2L);
        assertEquals(1, viewModel.getSubsystems().size());
        assertNull(viewModel.getSelectedSubsystem());
        assertNull(viewModel.getErrorMessage());
    }
    
    @Test
    public void testDeleteSubsystem_WithTasks() {
        // Setup
        when(subsystemService.deleteById(anyLong())).thenReturn(true);
        
        // Select subsystem with tasks
        viewModel.setSelectedSubsystem(testSubsystem1);
        
        // Execute command
        Command deleteCommand = viewModel.getDeleteSubsystemCommand();
        assertTrue(deleteCommand.canExecute());
        deleteCommand.execute();
        
        // Verify
        verify(subsystemService, never()).deleteById(anyLong());
        assertEquals(2, viewModel.getSubsystems().size());
        assertEquals(testSubsystem1, viewModel.getSelectedSubsystem());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Cannot delete a subsystem that has tasks"));
    }
    
    @Test
    public void testDeleteSubsystem_Failure() {
        // Setup
        when(subsystemService.deleteById(anyLong())).thenReturn(false);
        
        // Select subsystem with no tasks
        viewModel.setSelectedSubsystem(testSubsystem2);
        when(taskService.findBySubsystem(testSubsystem2)).thenReturn(emptyTasks);
        
        // Execute command
        Command deleteCommand = viewModel.getDeleteSubsystemCommand();
        assertTrue(deleteCommand.canExecute());
        deleteCommand.execute();
        
        // Verify
        verify(subsystemService).deleteById(2L);
        assertEquals(2, viewModel.getSubsystems().size());
        assertEquals(testSubsystem2, viewModel.getSelectedSubsystem());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Failed to delete subsystem"));
    }
    
    @Test
    public void testComputationMethods() {
        // Test getTaskCount
        int taskCount1 = viewModel.getTaskCount(testSubsystem1);
        assertEquals(2, taskCount1);
        
        int taskCount2 = viewModel.getTaskCount(testSubsystem2);
        assertEquals(0, taskCount2);
        
        // Test getCompletionPercentage
        double completion1 = viewModel.getCompletionPercentage(testSubsystem1);
        assertEquals(50.0, completion1);
        
        double completion2 = viewModel.getCompletionPercentage(testSubsystem2);
        assertEquals(0.0, completion2);
    }
    
    @Test
    public void testCommandAvailability() {
        // Initially no selection
        assertNull(viewModel.getSelectedSubsystem());
        assertFalse(viewModel.getEditSubsystemCommand().canExecute());
        assertFalse(viewModel.getDeleteSubsystemCommand().canExecute());
        
        // Set selection
        viewModel.setSelectedSubsystem(testSubsystem1);
        
        // Commands should be available
        assertTrue(viewModel.getEditSubsystemCommand().canExecute());
        assertTrue(viewModel.getDeleteSubsystemCommand().canExecute());
        
        // Clear selection
        viewModel.setSelectedSubsystem(null);
        
        // Commands should be unavailable
        assertFalse(viewModel.getEditSubsystemCommand().canExecute());
        assertFalse(viewModel.getDeleteSubsystemCommand().canExecute());
    }
    
    @Test
    public void testLoadSubsystems_HandleError() {
        // Setup error condition
        when(subsystemService.findAll()).thenThrow(new RuntimeException("Test error"));
        
        // Clear and reload
        viewModel.getSubsystems().clear();
        viewModel.getLoadSubsystemsCommand().execute();
        
        // Verify error handling
        assertEquals(0, viewModel.getSubsystems().size());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Failed to load subsystems"));
    }
}