package org.frcpm.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.frcpm.binding.Command;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.viewmodels.MeetingViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MeetingControllerTest extends BaseJavaFXTest {

    // Controller to test
    private MeetingController meetingController;
    
    // Mock ViewModel
    private MeetingViewModel mockViewModel;
    
    // Mock Commands
    private Command mockSaveCommand;
    private Command mockCancelCommand;
    
    // UI components - real JavaFX components, not mocks
    private DatePicker datePicker;
    private TextField startTimeField;
    private TextField endTimeField;
    private TextArea notesArea;
    private Button saveButton;
    private Button cancelButton;
    
    // Test data
    private Project testProject;
    private Meeting testMeeting;
    
    /**
     * Set up the JavaFX environment before each test.
     * This is invoked by TestFX before each test method.
     */
    @Start
    public void start(Stage stage) {
        // Create real JavaFX components
        datePicker = new DatePicker();
        startTimeField = new TextField();
        endTimeField = new TextField();
        notesArea = new TextArea();
        saveButton = new Button("Save");
        cancelButton = new Button("Cancel");
        
        // Create a layout to hold the components
        VBox root = new VBox(10);
        root.getChildren().addAll(
            datePicker, 
            startTimeField, 
            endTimeField, 
            notesArea, 
            saveButton, 
            cancelButton
        );
        
        // Set up and show the stage
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * Set up the test data and mock objects before each test.
     */
    @BeforeEach
    public void setUp() throws Exception {
        // Create a new controller instance
        meetingController = new MeetingController();
        
        // Create mock Command objects
        mockSaveCommand = mock(Command.class);
        mockCancelCommand = mock(Command.class);
        
        // Create mock ViewModel
        mockViewModel = mock(MeetingViewModel.class);
        
        // Set up basic mock behavior
        when(mockViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
        when(mockViewModel.getCancelCommand()).thenReturn(mockCancelCommand);
        when(mockViewModel.isValid()).thenReturn(true);
        
        // Inject components into controller using reflection
        injectField("datePicker", datePicker);
        injectField("startTimeField", startTimeField);
        injectField("endTimeField", endTimeField);
        injectField("notesArea", notesArea);
        injectField("saveButton", saveButton);
        injectField("cancelButton", cancelButton);
        injectField("viewModel", mockViewModel);
        
        // Create test project
        testProject = new Project(
                "Test Project",
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8)
        );
        testProject.setId(1L);
        
        // Create test meeting
        testMeeting = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                testProject
        );
        testMeeting.setId(1L);
        testMeeting.setNotes("Test meeting notes");
        
        // Set up mock ViewModel behavior
        when(mockViewModel.getMeeting()).thenReturn(testMeeting);
        
        // Initialize the controller
        meetingController.testInitialize();
    }
    
    /**
     * Test the initialization of the controller.
     */
    @Test
    public void testInitialize() {
        // Verify that bindings were set up
        verify(mockViewModel).dateProperty();
        verify(mockViewModel).startTimeStringProperty();
        verify(mockViewModel).endTimeStringProperty();
        verify(mockViewModel).notesProperty();
        
        // Verify that the buttons have actions set
        assertNotNull(saveButton.getOnAction());
        assertNotNull(cancelButton.getOnAction());
    }
    
    /**
     * Test setting up controller for a new meeting.
     */
    @Test
    public void testSetNewMeeting() {
        // Call method
        meetingController.setNewMeeting(testProject);
        
        // Verify ViewModel method was called
        verify(mockViewModel).initNewMeeting(testProject);
    }
    
    /**
     * Test setting up controller for editing an existing meeting.
     */
    @Test
    public void testSetMeeting() {
        // Call method
        meetingController.setMeeting(testMeeting);
        
        // Verify ViewModel method was called
        verify(mockViewModel).initExistingMeeting(testMeeting);
    }
    
    /**
     * Test getting the meeting from the ViewModel.
     */
    @Test
    public void testGetMeeting() {
        // Test
        Meeting result = meetingController.getMeeting();
        
        // Verify
        assertEquals(testMeeting, result);
        verify(mockViewModel).getMeeting();
    }
    
    /**
     * Test getting the ViewModel.
     */
    @Test
    public void testGetViewModel() {
        // Test
        MeetingViewModel result = meetingController.getViewModel();
        
        // Verify
        assertEquals(mockViewModel, result);
    }
    
    /**
     * Test the save button action when validation succeeds.
     */
    @Test
    public void testSaveButtonAction_Valid() {
        // Set up
        when(mockViewModel.isValid()).thenReturn(true);
        
        // Trigger the save button action
        saveButton.fire();
        
        // Verify command was executed
        verify(mockSaveCommand).execute();
    }
    
    /**
     * Test the save button action when validation fails.
     */
    @Test
    public void testSaveButtonAction_Invalid() {
        // Set up
        when(mockViewModel.isValid()).thenReturn(false);
        when(mockViewModel.getErrorMessage()).thenReturn("Test error message");
        
        // Trigger the save button action
        saveButton.fire();
        
        // Verify command was not executed
        verify(mockSaveCommand, never()).execute();
    }
    
    /**
     * Test the cancel button action.
     */
    @Test
    public void testCancelButtonAction() {
        // Trigger the cancel button action
        cancelButton.fire();
    }
    
    /**
     * Helper method to inject field values using reflection.
     */
    private void injectField(String fieldName, Object value) throws Exception {
        Field field = MeetingController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(meetingController, value);
    }
}