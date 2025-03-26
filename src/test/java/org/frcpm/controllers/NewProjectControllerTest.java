package org.frcpm.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NewProjectControllerTest {

    @Mock
    private ProjectService projectService;
    
    @InjectMocks
    private NewProjectController newProjectController;
    
    @Mock
    private TextField nameField;
    
    @Mock
    private DatePicker startDatePicker;
    
    @Mock
    private DatePicker goalEndDatePicker;
    
    @Mock
    private DatePicker hardDeadlinePicker;
    
    @Mock
    private TextArea descriptionArea;
    
    @Mock
    private Button createButton;
    
    @Mock
    private Button cancelButton;
    
    @Mock
    private Stage dialogStage;
    
    @Mock
    private ActionEvent mockEvent;
    
    private Project testProject;

    @BeforeEach
    public void setUp() {
        // Create test project
        testProject = new Project(
                "Test Project",
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8)
        );
        testProject.setId(1L);
        testProject.setDescription("Test project description");
        
        // Initialize controller by setting the mock fields
        when(newProjectController.getNameField()).thenReturn(nameField);
        when(newProjectController.getStartDatePicker()).thenReturn(startDatePicker);
        when(newProjectController.getGoalEndDatePicker()).thenReturn(goalEndDatePicker);
        when(newProjectController.getHardDeadlinePicker()).thenReturn(hardDeadlinePicker);
        when(newProjectController.getDescriptionArea()).thenReturn(descriptionArea);
        when(newProjectController.getCreateButton()).thenReturn(createButton);
        when(newProjectController.getCancelButton()).thenReturn(cancelButton);
        when(newProjectController.getDialogStage()).thenReturn(dialogStage);
        
        // Mock service behavior
        when(projectService.createProject(anyString(), any(), any(), any())).thenReturn(testProject);
        when(projectService.updateProject(anyLong(), anyString(), any(), any(), any(), anyString())).thenReturn(testProject);
        
        // Mock UI component behavior
        when(nameField.getText()).thenReturn("Test Project");
        when(startDatePicker.getValue()).thenReturn(LocalDate.now());
        when(goalEndDatePicker.getValue()).thenReturn(LocalDate.now().plusWeeks(6));
        when(hardDeadlinePicker.getValue()).thenReturn(LocalDate.now().plusWeeks(8));
        when(descriptionArea.getText()).thenReturn("Test project description");
    }

    @Test
    public void testInitialize() {
        // Call initialize using the test access method
        newProjectController.testInitialize();
        
        // Verify that default dates are set
        verify(startDatePicker).setValue(any(LocalDate.class));
        verify(goalEndDatePicker).setValue(any(LocalDate.class));
        verify(hardDeadlinePicker).setValue(any(LocalDate.class));
        
        // Verify that listeners are added
        verify(nameField).textProperty();
        verify(startDatePicker).valueProperty();
        verify(goalEndDatePicker).valueProperty();
        verify(hardDeadlinePicker).valueProperty();
        
        // Verify that button actions are set
        verify(createButton).setOnAction(any());
        verify(cancelButton).setOnAction(any());
    }

    @Test
    public void testSetDialogStage() {
        // Create a new stage
        Stage stage = mock(Stage.class);
        
        // Call the method to test
        newProjectController.setDialogStage(stage);
        
        // Verify the stage is set
        assertEquals(stage, newProjectController.getDialogStage());
    }

    @Test
    public void testGetCreatedProject() {
        // Set the created project using reflection
        try {
            java.lang.reflect.Field field = NewProjectController.class.getDeclaredField("createdProject");
            field.setAccessible(true);
            field.set(newProjectController, testProject);
        } catch (Exception e) {
            fail("Failed to set createdProject field: " + e.getMessage());
        }
        
        // Test getting the created project
        Project result = newProjectController.getCreatedProject();
        
        // Verify the result
        assertEquals(testProject, result);
    }

    @Test
    public void testHandleCreate() {
        // Test creating a new project
        newProjectController.testHandleCreate();
        
        // Verify service was called to create a project
        verify(projectService).createProject(
            nameField.getText(),
            startDatePicker.getValue(),
            goalEndDatePicker.getValue(),
            hardDeadlinePicker.getValue()
        );
        
        // Verify description update
        verify(projectService).updateProject(
            testProject.getId(),
            nameField.getText(),
            startDatePicker.getValue(),
            goalEndDatePicker.getValue(),
            hardDeadlinePicker.getValue(),
            descriptionArea.getText()
        );
        
        // Verify dialog was closed
        verify(dialogStage).close();
    }

    @Test
    public void testHandleCreateWithNoDescription() {
        // Mock empty description
        when(descriptionArea.getText()).thenReturn("");
        
        // Test creating a new project
        newProjectController.testHandleCreate();
        
        // Verify service was called to create a project
        verify(projectService).createProject(
            nameField.getText(),
            startDatePicker.getValue(),
            goalEndDatePicker.getValue(),
            hardDeadlinePicker.getValue()
        );
        
        // Verify update was NOT called (no description to update)
        verify(projectService, never()).updateProject(
            anyLong(),
            anyString(),
            any(),
            any(),
            any(),
            anyString()
        );
        
        // Verify dialog was closed
        verify(dialogStage).close();
    }

    @Test
    public void testValidateInput() {
        // Test with valid inputs
        boolean result = newProjectController.testValidateInput();
        
        // Verify result
        assertTrue(result);
        verify(createButton).setDisable(false);
    }

    @Test
    public void testValidateInputWithEmptyName() {
        // Mock empty name
        when(nameField.getText()).thenReturn("");
        
        // Test validation
        boolean result = newProjectController.testValidateInput();
        
        // Verify result
        assertFalse(result);
        verify(createButton).setDisable(true);
    }

    @Test
    public void testValidateInputWithNullDates() {
        // Mock null dates
        when(startDatePicker.getValue()).thenReturn(null);
        
        // Test validation
        boolean result = newProjectController.testValidateInput();
        
        // Verify result
        assertFalse(result);
        verify(createButton).setDisable(true);
    }

    @Test
    public void testValidateInputWithInvalidDateRelations() {
        // Mock invalid date relations (goal before start)
        when(goalEndDatePicker.getValue()).thenReturn(LocalDate.now().minusDays(1));
        
        // Test validation
        boolean result = newProjectController.testValidateInput();
        
        // Verify result
        assertFalse(result);
        verify(createButton).setDisable(true);
    }

    @Test
    public void testShowErrorAlert() {
        // Create a mock alert
        Alert mockAlert = mock(Alert.class);
        
        // Create a special test controller that returns our mock alert
        NewProjectController spyController = spy(newProjectController);
        doReturn(mockAlert).when(spyController).createErrorAlert();
        
        // Test showing an error alert
        spyController.testShowErrorAlert("Test Title", "Test Message");
        
        // Verify alert was configured and shown
        verify(mockAlert).setTitle("Error");
        verify(mockAlert).setHeaderText("Test Title");
        verify(mockAlert).setContentText("Test Message");
        verify(mockAlert).showAndWait();
    }
    
    // Helper method to mock Alert creation for the controller
    private Alert createErrorAlert() {
        return mock(Alert.class);
    }
}