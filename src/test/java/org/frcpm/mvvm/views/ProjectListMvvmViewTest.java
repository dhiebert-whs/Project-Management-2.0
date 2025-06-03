// src/test/java/org/frcpm/mvvm/views/ProjectListMvvmViewTest.java

package org.frcpm.mvvm.views;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

import org.frcpm.di.TestModule;
import org.frcpm.models.Project;
import org.frcpm.mvvm.viewmodels.ProjectListMvvmViewModel;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * TestFX test for ProjectListMvvmView.
 * Tests the UI components, data binding, and user interactions using the real MVVMFx view loading.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectListMvvmViewTest extends BaseViewTest<ProjectListMvvmView, ProjectListMvvmViewModel> {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectListMvvmViewTest.class.getName());
    
    // Test data
    private List<Project> testProjects;
    private Project testProject1;
    private Project testProject2;
    private Project testProject3;
    
    // Mock repository
    private ProjectRepository mockProjectRepository;
    
    @Override
    protected Class<ProjectListMvvmView> getViewClass() {
        return ProjectListMvvmView.class;
    }
    
    @Override
    protected void setupTestData() {
        LOGGER.info("Setting up test data for ProjectListMvvmViewTest");
        
        // Create test projects
        testProject1 = new Project("Test Project 1", 
            LocalDate.now().minusDays(30), 
            LocalDate.now().plusDays(30), 
            LocalDate.now().plusDays(45));
        testProject1.setId(1L);
        testProject1.setDescription("First test project");
        
        testProject2 = new Project("Test Project 2", 
            LocalDate.now().minusDays(20), 
            LocalDate.now().plusDays(40), 
            LocalDate.now().plusDays(50));
        testProject2.setId(2L);
        testProject2.setDescription("Second test project");
        
        testProject3 = new Project("Test Project 3", 
            LocalDate.now().minusDays(10), 
            LocalDate.now().plusDays(35), 
            LocalDate.now().plusDays(60));
        testProject3.setId(3L);
        testProject3.setDescription("Third test project");
        
        testProjects = new ArrayList<>();
        testProjects.add(testProject1);
        testProjects.add(testProject2);
        testProjects.add(testProject3);
        
        // Mock the project repository
        mockProjectRepository = mock(ProjectRepository.class);
        when(mockProjectRepository.findAll()).thenReturn(testProjects);
        when(mockProjectRepository.findById(1L)).thenReturn(Optional.of(testProject1));
        when(mockProjectRepository.findById(2L)).thenReturn(Optional.of(testProject2));
        when(mockProjectRepository.findById(3L)).thenReturn(Optional.of(testProject3));
        when(mockProjectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mockProjectRepository.deleteById(any(Long.class))).thenReturn(true);
        
        // Register the mock repository with TestModule
        TestModule.setRepository(ProjectRepository.class, mockProjectRepository);
    }
    
    @Test
    @Order(1)
    public void testViewInitialization() {
        LOGGER.info("Testing view initialization");
        
        // Verify the view and ViewModel were created properly
        assertNotNull(view, "View should be created");
        assertNotNull(viewModel, "ViewModel should be created");
        
        // Verify basic UI components exist
        assertNotNull(lookup("#projectListView").query(), "Project list view should exist");
        assertNotNull(lookup("#newProjectButton").query(), "New project button should exist");
        assertNotNull(lookup("#openProjectButton").query(), "Open project button should exist");
        assertNotNull(lookup("#deleteProjectButton").query(), "Delete project button should exist");
        assertNotNull(lookup("#importProjectButton").query(), "Import project button should exist");
    }
    
    @Test
    @Order(2)
    public void testInitialButtonStates() {
        LOGGER.info("Testing initial button states");
        
        Button newButton = lookup("#newProjectButton").query();
        Button openButton = lookup("#openProjectButton").query();
        Button deleteButton = lookup("#deleteProjectButton").query();
        Button importButton = lookup("#importProjectButton").query();
        
        // New and import buttons should be enabled
        assertFalse(newButton.isDisabled(), "New project button should be enabled initially");
        assertFalse(importButton.isDisabled(), "Import project button should be enabled initially");
        
        // Open and delete buttons should be disabled when no selection
        assertTrue(openButton.isDisabled(), "Open project button should be disabled when no selection");
        assertTrue(deleteButton.isDisabled(), "Delete project button should be disabled when no selection");
    }
    
    @Test
    @Order(3)
    public void testProjectListBinding() {
        LOGGER.info("Testing project list binding");
        
        ListView<Project> projectListView = lookup("#projectListView").query();
        assertNotNull(projectListView, "Project ListView should exist");
        
        // Verify the ListView is bound to the ViewModel's projects
        ObservableList<Project> viewProjects = projectListView.getItems();
        ObservableList<Project> viewModelProjects = viewModel.getProjects();
        
        assertSame(viewModelProjects, viewProjects, "ListView should be bound to ViewModel projects");
    }
    
    @Test
    @Order(4)
    public void testLoadProjectsCommand() {
        LOGGER.info("Testing load projects command");
        
        // Execute the load projects command
        Platform.runLater(() -> {
            viewModel.getLoadProjectsCommand().execute();
        });
        
        // Wait for async operation
        waitForFxEvents();
        sleep(300);
        
        // Verify projects were loaded
        ListView<Project> projectListView = lookup("#projectListView").query();
        org.frcpm.utils.TestUtils.waitUntil(() -> projectListView.getItems().size() > 0, 2000);
        
        assertEquals(3, projectListView.getItems().size(), "Should load 3 test projects");
        
        // Verify project names
        ObservableList<Project> items = projectListView.getItems();
        assertTrue(items.stream().anyMatch(p -> "Test Project 1".equals(p.getName())));
        assertTrue(items.stream().anyMatch(p -> "Test Project 2".equals(p.getName())));
        assertTrue(items.stream().anyMatch(p -> "Test Project 3".equals(p.getName())));
    }
    
    @Test
    @Order(5)
    public void testProjectSelection() {
        LOGGER.info("Testing project selection");
        
        // Make sure projects are loaded first
        Platform.runLater(() -> {
            viewModel.getLoadProjectsCommand().execute();
        });
        
        waitForFxEvents();
        sleep(300);
        
        ListView<Project> projectListView = lookup("#projectListView").query();
        org.frcpm.utils.TestUtils.waitUntil(() -> projectListView.getItems().size() > 0, 2000);
        
        // Select the first project
        Platform.runLater(() -> {
            projectListView.getSelectionModel().select(0);
        });
        
        waitForFxEvents();
        sleep(200);
        
        // Verify selection affected button states
        Button openButton = lookup("#openProjectButton").query();
        Button deleteButton = lookup("#deleteProjectButton").query();
        
        org.frcpm.utils.TestUtils.waitUntil(() -> !openButton.isDisabled() && !deleteButton.isDisabled(), 2000);
        
        assertFalse(openButton.isDisabled(), "Open button should be enabled after selection");
        assertFalse(deleteButton.isDisabled(), "Delete button should be enabled after selection");
        
        // Verify ViewModel has the selection
        assertNotNull(viewModel.getSelectedProject(), "ViewModel should have selected project");
        assertEquals("Test Project 1", viewModel.getSelectedProject().getName(), 
                    "ViewModel should have correct selected project");
    }
    
    @Test
    @Order(6)
    public void testButtonClicks() {
        LOGGER.info("Testing button clicks");
        
        // Test new project button
        Button newButton = lookup("#newProjectButton").query();
        assertDoesNotThrow(() -> {
            clickOn(newButton);
        }, "Clicking new project button should not throw exception");
        
        // Test import project button
        Button importButton = lookup("#importProjectButton").query();
        assertDoesNotThrow(() -> {
            clickOn(importButton);
        }, "Clicking import project button should not throw exception");
        
        // Load projects and test open/delete buttons
        Platform.runLater(() -> {
            viewModel.getLoadProjectsCommand().execute();
        });
        
        waitForFxEvents();
        sleep(300);
        
        // Select a project
        ListView<Project> projectListView = lookup("#projectListView").query();
        org.frcpm.utils.TestUtils.waitUntil(() -> projectListView.getItems().size() > 0, 2000);
        
        Platform.runLater(() -> {
            projectListView.getSelectionModel().select(0);
        });
        
        waitForFxEvents();
        sleep(200);
        
        // Test open button
        Button openButton = lookup("#openProjectButton").query();
        org.frcpm.utils.TestUtils.waitUntil(() -> !openButton.isDisabled(), 2000);
        assertDoesNotThrow(() -> {
            clickOn(openButton);
        }, "Clicking open project button should not throw exception");
        
        // Test delete button
        Button deleteButton = lookup("#deleteProjectButton").query();
        assertDoesNotThrow(() -> {
            clickOn(deleteButton);
        }, "Clicking delete project button should not throw exception");
    }
    
    @Test
    @Order(7)
    public void testViewModelCommands() {
        LOGGER.info("Testing ViewModel commands");
        
        // Verify commands exist and are not null
        assertNotNull(viewModel.getLoadProjectsCommand(), "Load projects command should exist");
        assertNotNull(viewModel.getNewProjectCommand(), "New project command should exist");
        assertNotNull(viewModel.getOpenProjectCommand(), "Open project command should exist");
        assertNotNull(viewModel.getImportProjectCommand(), "Import project command should exist");
        assertNotNull(viewModel.getDeleteProjectCommand(), "Delete project command should exist");
        
        // Test command execution doesn't throw exceptions
        assertDoesNotThrow(() -> {
            viewModel.getLoadProjectsCommand().execute();
        }, "Load projects command should execute without exception");
        
        assertDoesNotThrow(() -> {
            viewModel.getNewProjectCommand().execute();
        }, "New project command should execute without exception");
        
        assertDoesNotThrow(() -> {
            viewModel.getImportProjectCommand().execute();
        }, "Import project command should execute without exception");
    }
    
    @Test
    @Order(8)
    public void testErrorMessageHandling() {
        LOGGER.info("Testing error message handling");
        
        // Set an error message in the ViewModel
        Platform.runLater(() -> {
            viewModel.setErrorMessage("Test error message");
        });
        
        waitForFxEvents();
        sleep(100);
        
        // Verify error label is visible and shows the message
        Label errorLabel = lookup("#errorLabel").query();
        assertNotNull(errorLabel, "Error label should exist");
        
        org.frcpm.utils.TestUtils.waitUntil(() -> errorLabel.isVisible(), 2000);
        assertTrue(errorLabel.isVisible(), "Error label should be visible when there's an error");
        assertEquals("Test error message", errorLabel.getText(), 
                    "Error label should show the error message");
        
        // Clear the error message
        Platform.runLater(() -> {
            viewModel.clearErrorMessage();
        });
        
        waitForFxEvents();
        sleep(100);
        
        // Verify error label is hidden
        org.frcpm.utils.TestUtils.waitUntil(() -> !errorLabel.isVisible(), 2000);
        assertFalse(errorLabel.isVisible(), "Error label should be hidden when error is cleared");
    }
}