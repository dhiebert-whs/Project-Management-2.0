package org.frcpm.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.viewmodels.NewProjectViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the NewProjectController class.
 * Follows the same pattern as other controller tests.
 */
@ExtendWith(ApplicationExtension.class)
public class NewProjectControllerTest extends BaseJavaFXTest {

    // Controller to test
    private NewProjectController controller;
    
    // Mock ViewModel
    @Mock
    private NewProjectViewModel viewModel;
    
    // Mock Command objects
    @Mock
    private Command createProjectCommand;
    
    @Mock
    private Command cancelCommand;

    // UI components
    private TextField nameField;
    private DatePicker startDatePicker;
    private DatePicker goalEndDatePicker;
    private DatePicker hardDeadlinePicker;
    private TextArea descriptionArea;
    private Button createButton;
    private Button cancelButton;
    
    // Mock Stage for dialog
    @Mock
    private Stage dialogStage;
    
    // Test data
    private Project testProject;
    
    /**
     * Set up the JavaFX environment before each test.
     * This is invoked by TestFX before each test method.
     */
    @Start
    public void start(Stage stage) {
        // Create real JavaFX components
        nameField = new TextField();
        startDatePicker = new DatePicker();
        goalEndDatePicker = new DatePicker();
        hardDeadlinePicker = new DatePicker();
        descriptionArea = new TextArea();
        createButton = new Button("Create");
        cancelButton = new Button("Cancel");
        
        // Create a layout to hold the components
        VBox root = new VBox(10);
        root.getChildren().addAll(
            nameField, startDatePicker, goalEndDatePicker, 
            hardDeadlinePicker, descriptionArea, createButton, cancelButton);
        
        // Set up and show the stage
        Scene scene = new Scene(root, 400, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create controller
        controller = new NewProjectController();
        
        // Create test project
        testProject = new Project(
            "Test Project",
            LocalDate.now(),
            LocalDate.now().plusWeeks(6),
            LocalDate.now().plusWeeks(8)
        );
        testProject.setId(1L);
        testProject.setDescription("Test project description");
        
        // Configure mock ViewModel
        when(viewModel.getCreateProjectCommand()).thenReturn(createProjectCommand);
        when(viewModel.getCancelCommand()).thenReturn(cancelCommand);
        when(viewModel.getCreatedProject()).thenReturn(null);
        
        // Set up properties for binding
        javafx.beans.property.StringProperty errorProperty = new javafx.beans.property.SimpleStringProperty("");
        when(viewModel.errorMessageProperty()).thenReturn(errorProperty);
        
        // Inject components into controller using reflection
        injectField("nameField", nameField);
        injectField("startDatePicker", startDatePicker);
        injectField("goalEndDatePicker", goalEndDatePicker);
        injectField("hardDeadlinePicker", hardDeadlinePicker);
        injectField("descriptionArea", descriptionArea);
        injectField("createButton", createButton);
        injectField("cancelButton", cancelButton);
        injectField("dialogStage", dialogStage);
        
        // Inject the mock ViewModel
        injectField("viewModel", viewModel);
    }
    
    /**
     * Helper method to inject field values using reflection.
     */
    private void injectField(String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = NewProjectController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    public void testInitialize() {
        // Call initialize
        controller.testInitialize();
        
        // Verify ViewModel interactions
        verify(viewModel).projectNameProperty();
        verify(viewModel).startDateProperty();
        verify(viewModel).goalEndDateProperty();
        verify(viewModel).hardDeadlineProperty();
        verify(viewModel).descriptionProperty();
        verify(viewModel).getCreateProjectCommand();
        verify(viewModel).errorMessageProperty();
    }

    @Test
    public void testSetDialogStage() {
        // Act
        controller.setDialogStage(dialogStage);
        
        // Verify command was executed
        verify(createProjectCommand).execute();
    }

    @Test
    public void testSetDialogStageWithProjectCreated() {
        // Setup - change return value to a project
        when(viewModel.getCreatedProject()).thenReturn(testProject);
        
        // Act
        controller.setDialogStage(dialogStage);
        
        // Verify dialog was closed
        verify(dialogStage).close();
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
        // Create a special test controller that returns our mock alert
        NewProjectController spyController = spy(controller);
        Alert mockAlert = mock(Alert.class);
        doReturn(mockAlert).when(spyController).createErrorAlert();
        
        // Act
        spyController.testShowErrorAlert("Test Title", "Test Message");
        
        // Assert
        verify(mockAlert).setTitle("Error");
        verify(mockAlert).setHeaderText("Test Title");
        verify(mockAlert).setContentText("Test Message");
        verify(mockAlert).showAndWait();
    }
    
    @Test
    public void testErrorMessageListener() throws Exception {
        // Setup
        NewProjectController spyController = spy(controller);
        Alert mockAlert = mock(Alert.class);
        doReturn(mockAlert).when(spyController).createErrorAlert();
        
        // Access the setupErrorHandling method using reflection
        java.lang.reflect.Method setupErrorHandlingMethod = 
            NewProjectController.class.getDeclaredMethod("setupErrorHandling");
        setupErrorHandlingMethod.setAccessible(true);
        setupErrorHandlingMethod.invoke(spyController);
        
        // Create a property for testing
        javafx.beans.property.StringProperty errorProperty = 
            new javafx.beans.property.SimpleStringProperty("");
        when(viewModel.errorMessageProperty()).thenReturn(errorProperty);
        
        // Act - trigger error message listener
        errorProperty.set("Test Error");
        
        // Verify error handler was called
        verify(spyController).testShowErrorAlert("Error Creating Project", "Test Error");
        verify(viewModel.errorMessageProperty()).set("");
    }
    
    @Test
    public void testGetViewModel() {
        // Act
        NewProjectViewModel result = controller.getViewModel();
        
        // Assert
        assertEquals(viewModel, result);
    }
}