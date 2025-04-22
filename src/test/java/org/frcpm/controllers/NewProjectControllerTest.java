package org.frcpm.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;

import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.viewmodels.NewProjectViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the NewProjectController class.
 */
public class NewProjectControllerTest {

    // Controller to test
    private TestableNewProjectController controller;
    
    // Mock ViewModel
    @Mock
    private NewProjectViewModel viewModel;
    
    // Mock Command objects
    @Mock
    private Command createProjectCommand;
    
    @Mock
    private Command cancelCommand;
    
    // Mock Stage for dialog
    @Mock
    private Stage dialogStage;
    
    // Test data
    private Project testProject;
    
    // Property for error message testing
    private StringProperty errorProperty;
    
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create controller
        controller = spy(new TestableNewProjectController());
        
        // Create test project
        testProject = new Project(
            "Test Project",
            LocalDate.now(),
            LocalDate.now().plusWeeks(6),
            LocalDate.now().plusWeeks(8)
        );
        testProject.setId(1L);
        testProject.setDescription("Test project description");
        
        // Setup error property
        errorProperty = new SimpleStringProperty("");
        
        // Configure mock ViewModel
        when(viewModel.getCreateProjectCommand()).thenReturn(createProjectCommand);
        when(viewModel.getCancelCommand()).thenReturn(cancelCommand);
        when(viewModel.getCreatedProject()).thenReturn(null);
        when(viewModel.errorMessageProperty()).thenReturn(errorProperty);
        
        // Inject the mock ViewModel
        injectField(controller, "viewModel", viewModel);
        
        // Reset tracking before each test
        controller.resetTracking();
    }
    
    /**
     * Helper method to inject field values using reflection.
     */
    private void injectField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = NewProjectController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void testInitialize() {
        // Setup a mock viewModel that's not null
        NewProjectViewModel realViewModel = new NewProjectViewModel();
        TestableNewProjectController controllerWithRealVM = spy(new TestableNewProjectController());
        
        try {
            // Inject real ViewModel
            injectField(controllerWithRealVM, "viewModel", realViewModel);
            
            // Call initialize (our override that doesn't access UI components)
            controllerWithRealVM.testInitialize();
            
            // Success is just not throwing an exception
            // No need for assertions here as we're testing the method doesn't throw
        } catch (Exception e) {
            fail("Initialize should not throw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testSetDialogStage() {
        // Act
        controller.setDialogStage(dialogStage);
        
        // Verify command execution
        verify(viewModel).getCreateProjectCommand();
        verify(createProjectCommand).execute();
    }

    @Test
    public void testSetDialogStageWithProjectCreated() {
        // Setup
        when(viewModel.getCreatedProject()).thenReturn(testProject);
        
        // Act
        controller.setDialogStage(dialogStage);
        
        // Verify dialog was marked as closed
        assertTrue(controller.wasDialogStageClosed());
    }
    
    @Test
    public void testGetCreatedProject() {
        // Setup
        when(viewModel.getCreatedProject()).thenReturn(testProject);
        
        // Act
        Project result = controller.getCreatedProject();
        
        // Assert
        assertEquals(testProject, result);
        verify(viewModel).getCreatedProject();
    }
    
    @Test
    public void testShowErrorAlert() {
        // Act
        controller.testShowErrorAlert("Test Title", "Test Message");
        
        // Assert
        assertTrue(controller.wasErrorMessageHandled());
        assertEquals("Test Title", controller.getLastErrorTitle());
        assertEquals("Test Message", controller.getLastErrorMessage());
    }
    
    @Test
    public void testErrorMessageListener() {
        // Setup error handling
        controller.testSetupErrorHandling();
        
        // Act - trigger error message
        errorProperty.set("Test Error");
        
        // Verify error was handled
        assertTrue(controller.wasErrorMessageHandled(), "Error message should be handled");
        assertEquals("Error Creating Project", controller.getLastErrorTitle());
        assertEquals("Test Error", controller.getLastErrorMessage());
        
        // Verify error property was cleared
        assertEquals("", errorProperty.get());
    }
    
    @Test
    public void testGetViewModel() {
        // Act
        NewProjectViewModel result = controller.getViewModel();
        
        // Assert
        assertEquals(viewModel, result);
    }
}