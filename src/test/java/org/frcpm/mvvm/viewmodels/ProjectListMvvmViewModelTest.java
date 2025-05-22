// src/test/java/org/frcpm/mvvm/viewmodels/ProjectListMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.frcpm.di.TestModule;
import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the ProjectListMvvmViewModel class.
 */
public class ProjectListMvvmViewModelTest {
    
    private ProjectService projectService;
    
    private List<Project> testProjects;
    private Project testProject1;
    private Project testProject2;
    private ProjectListMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data first
        setupTestData();
        
        // Create a clean mock for ProjectService
        ProjectService mockProjectService = mock(ProjectService.class);
        
        // Configure the mock service
        when(mockProjectService.findAll()).thenReturn(testProjects);
        when(mockProjectService.findById(1L)).thenReturn(testProject1);
        when(mockProjectService.findById(2L)).thenReturn(testProject2);
        when(mockProjectService.deleteById(anyLong())).thenReturn(true);
        
        // Register the mock with TestModule
        TestModule.setService(ProjectService.class, mockProjectService);
        
        // Get service reference from TestModule
        projectService = TestModule.getService(ProjectService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new ProjectListMvvmViewModel(projectService);
        });
    }
    
    private void setupTestData() {
        // Create test projects
        testProject1 = new Project();
        testProject1.setId(1L);
        testProject1.setName("Test Project 1");
        testProject1.setStartDate(LocalDate.now().minusMonths(1));
        testProject1.setGoalEndDate(LocalDate.now().plusMonths(1));
        testProject1.setHardDeadline(LocalDate.now().plusMonths(2));
        testProject1.setDescription("Test Description 1");
        
        testProject2 = new Project();
        testProject2.setId(2L);
        testProject2.setName("Test Project 2");
        testProject2.setStartDate(LocalDate.now().minusWeeks(2));
        testProject2.setGoalEndDate(LocalDate.now().plusWeeks(6));
        testProject2.setHardDeadline(LocalDate.now().plusMonths(3));
        testProject2.setDescription("Test Description 2");
        
        testProjects = new ArrayList<>();
        testProjects.add(testProject1);
        testProjects.add(testProject2);
    }
    
    @Test
    public void testInitialState() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Check that the viewModel is not null
            assertNotNull(viewModel, "ViewModel should not be null");
            
            // Check initial values
            assertTrue(viewModel.getProjects().isEmpty());
            assertNull(viewModel.getSelectedProject());
            assertFalse(viewModel.isLoading());
            
            // Check that commands exist
            assertNotNull(viewModel.getLoadProjectsCommand());
            assertNotNull(viewModel.getNewProjectCommand());
            assertNotNull(viewModel.getOpenProjectCommand());
            assertNotNull(viewModel.getImportProjectCommand());
            assertNotNull(viewModel.getDeleteProjectCommand());
            
            // Check command states
            assertTrue(viewModel.getLoadProjectsCommand().isExecutable());
            assertTrue(viewModel.getNewProjectCommand().isExecutable());
            assertFalse(viewModel.getOpenProjectCommand().isExecutable()); // No selection
            assertTrue(viewModel.getImportProjectCommand().isExecutable());
            assertFalse(viewModel.getDeleteProjectCommand().isExecutable()); // No selection
        });
    }
    
    @Test
    public void testLoadProjects() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no projects
            assertTrue(viewModel.getProjects().isEmpty());
            
            // Execute load projects command
            viewModel.getLoadProjectsCommand().execute();
            
            // Let any async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify projects were loaded
            assertEquals(2, viewModel.getProjects().size());
            assertEquals(testProject1, viewModel.getProjects().get(0));
            assertEquals(testProject2, viewModel.getProjects().get(1));
            
            // Verify service was called
            verify(projectService, atLeastOnce()).findAll();
            
            // Should not be loading after completion
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testProjectSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load projects first
            viewModel.getLoadProjectsCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Initially no selection
            assertNull(viewModel.getSelectedProject());
            assertFalse(viewModel.getOpenProjectCommand().isExecutable());
            assertFalse(viewModel.getDeleteProjectCommand().isExecutable());
            
            // Select a project
            viewModel.setSelectedProject(testProject1);
            
            // Verify selection
            assertEquals(testProject1, viewModel.getSelectedProject());
            
            // Commands should now be executable
            assertTrue(viewModel.getOpenProjectCommand().isExecutable());
            assertTrue(viewModel.getDeleteProjectCommand().isExecutable());
        });
    }
    
    @Test
    public void testOpenProjectCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load projects and select one
            viewModel.getLoadProjectsCommand().execute();
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            viewModel.setSelectedProject(testProject1);
            
            // Verify command is executable
            assertTrue(viewModel.getOpenProjectCommand().isExecutable());
            
            // Execute open project command
            viewModel.getOpenProjectCommand().execute();
            
            // Command execution is logged - in real application would navigate to project
            // For test purposes, we just verify the command executed without error
            assertNotNull(viewModel.getSelectedProject());
        });
    }
    
    @Test
    public void testDeleteProjectCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load projects and select one
            viewModel.getLoadProjectsCommand().execute();
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            viewModel.setSelectedProject(testProject1);
            
            // Verify initial state
            assertEquals(2, viewModel.getProjects().size());
            assertTrue(viewModel.getDeleteProjectCommand().isExecutable());
            
            // Execute delete command
            viewModel.getDeleteProjectCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify project was removed from list
            assertEquals(1, viewModel.getProjects().size());
            assertFalse(viewModel.getProjects().contains(testProject1));
            
            // Verify selection was cleared
            assertNull(viewModel.getSelectedProject());
            
            // Verify service was called
            verify(projectService).deleteById(1L);
        });
    }
    
    @Test
    public void testNewProjectCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Command should always be executable
            assertTrue(viewModel.getNewProjectCommand().isExecutable());
            
            // Execute new project command
            viewModel.getNewProjectCommand().execute();
            
            // Command execution is logged - in real application would open new project dialog
            // For test purposes, we just verify the command executed without error
            assertTrue(viewModel.getNewProjectCommand().isExecutable());
        });
    }
    
    @Test
    public void testImportProjectCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Command should always be executable
            assertTrue(viewModel.getImportProjectCommand().isExecutable());
            
            // Execute import project command
            viewModel.getImportProjectCommand().execute();
            
            // Command execution is logged - in real application would open import dialog
            // For test purposes, we just verify the command executed without error
            assertTrue(viewModel.getImportProjectCommand().isExecutable());
        });
    }
    
    @Test
    public void testLoadingState() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not loading
            assertFalse(viewModel.isLoading());
            
            // Execute load command
            viewModel.getLoadProjectsCommand().execute();
            
            // Should be loading immediately after command execution
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should not be loading after completion
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testErrorHandling() {
        // Configure mock to throw an exception
        when(projectService.findAll()).thenThrow(new RuntimeException("Database error"));
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute load command
            viewModel.getLoadProjectsCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to load projects"));
            
            // Should not be loading
            assertFalse(viewModel.isLoading());
            
            // Projects list should be empty
            assertTrue(viewModel.getProjects().isEmpty());
        });
    }
    
    @Test
    public void testDeleteProjectFailure() {
        // Configure mock to return false for delete
        when(projectService.deleteById(anyLong())).thenReturn(false);
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load projects and select one
            viewModel.getLoadProjectsCommand().execute();
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            viewModel.setSelectedProject(testProject1);
            
            // Execute delete command
            viewModel.getDeleteProjectCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Project should still be in list (delete failed)
            assertEquals(2, viewModel.getProjects().size());
            assertTrue(viewModel.getProjects().contains(testProject1));
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to delete project"));
        });
    }
    
    @Test
    public void testPropertyBindings() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Test that properties are properly bound
            assertNotNull(viewModel.selectedProjectProperty());
            assertNotNull(viewModel.loadingProperty());
            assertNotNull(viewModel.getProjects());
            
            // Test property changes
            viewModel.setSelectedProject(testProject1);
            assertEquals(testProject1, viewModel.selectedProjectProperty().get());
        });
    }
}