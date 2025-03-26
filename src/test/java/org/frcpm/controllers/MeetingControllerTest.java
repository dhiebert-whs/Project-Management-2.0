package org.frcpm.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.services.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class MeetingControllerTest extends BaseJavaFXTest {

    // Controller to test
    private MeetingController meetingController;
    
    // Mock service
    private MeetingService meetingService;
    
    // Test data
    private Project testProject;
    private Meeting testMeeting;
    
    // UI components - real JavaFX components, not mocks
    private DatePicker datePicker;
    private TextField startTimeField;
    private TextField endTimeField;
    private TextArea notesArea;
    private Button saveButton;
    private Button cancelButton;
    
    // Track the dialog close status
    private boolean dialogClosed = false;
    private List<Alert> shownAlerts = new ArrayList<>();
    
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
        
        // Create mock service
        meetingService = mock(MeetingService.class);
        
        // Inject components into controller using reflection
        injectField("datePicker", datePicker);
        injectField("startTimeField", startTimeField);
        injectField("endTimeField", endTimeField);
        injectField("notesArea", notesArea);
        injectField("saveButton", saveButton);
        injectField("cancelButton", cancelButton);
        injectField("meetingService", meetingService);
        
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
        
        // Reset tracking variables
        dialogClosed = false;
        shownAlerts.clear();
        
        // Mock service behavior
        when(meetingService.createMeeting(any(), any(), any(), anyLong(), anyString()))
            .thenReturn(testMeeting);
        when(meetingService.updateMeetingDateTime(anyLong(), any(), any(), any()))
            .thenReturn(testMeeting);
        when(meetingService.updateNotes(anyLong(), anyString()))
            .thenReturn(testMeeting);
        
        // Override dialog closing and alert showing methods for testing
        overrideMethod("closeDialog", () -> dialogClosed = true);
        
        // Override alert showing
        overrideMethod("showErrorAlert", (title, message) -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(title);
            alert.setContentText(message);
            shownAlerts.add(alert);
        });
        
        // Initialize the controller
        meetingController.testInitialize();
    }
    
    /**
     * Test the initialization of the controller.
     */
    @Test
    public void testInitialize() {
        // Verify that the fields have been initialized with default values
        assertEquals(LocalDate.now(), datePicker.getValue());
        assertEquals("16:00", startTimeField.getText());
        assertEquals("18:00", endTimeField.getText());
        
        // Verify that button actions have been set
        assertNotNull(saveButton.getOnAction());
        assertNotNull(cancelButton.getOnAction());
    }
    
    /**
     * Test setting up controller for a new meeting.
     */
    @Test
    public void testSetNewMeeting() {
        // Set up for new meeting
        meetingController.setNewMeeting(testProject);
        
        // Verify controller state
        assertEquals(testProject, meetingController.getProject());
        assertNull(meetingController.getMeeting());
        assertTrue(meetingController.isNewMeeting());
        
        // Verify UI state
        assertEquals(LocalDate.now(), datePicker.getValue());
        assertEquals("16:00", startTimeField.getText());
        assertEquals("18:00", endTimeField.getText());
        assertEquals("", notesArea.getText());
    }
    
    /**
     * Test setting up controller for editing an existing meeting.
     */
    @Test
    public void testSetMeeting() {
        // Set up for editing existing meeting
        meetingController.setMeeting(testMeeting);
        
        // Verify controller state
        assertEquals(testMeeting, meetingController.getMeeting());
        assertEquals(testProject, meetingController.getProject());
        assertFalse(meetingController.isNewMeeting());
        
        // Verify UI state
        assertEquals(testMeeting.getDate(), datePicker.getValue());
        assertEquals(testMeeting.getStartTime().toString(), startTimeField.getText());
        assertEquals(testMeeting.getEndTime().toString(), endTimeField.getText());
        assertEquals(testMeeting.getNotes(), notesArea.getText());
    }
    
    /**
     * Test saving a new meeting.
     */
    @Test
    public void testHandleSaveForNewMeeting() {
        // Set up for new meeting
        meetingController.setNewMeeting(testProject);
        
        // Set field values
        LocalDate date = LocalDate.now().plusDays(1);
        datePicker.setValue(date);
        startTimeField.setText("18:00");
        endTimeField.setText("20:00");
        notesArea.setText("Test meeting notes");
        
        // Call save method
        meetingController.testHandleSave(null);
        
        // Verify service was called with correct parameters
        verify(meetingService).createMeeting(
            date,
            LocalTime.parse("18:00"),
            LocalTime.parse("20:00"),
            testProject.getId(),
            "Test meeting notes"
        );
        
        // Verify dialog was closed
        assertTrue(dialogClosed);
    }
    
    /**
     * Test saving an existing meeting.
     */
    @Test
    public void testHandleSaveForExistingMeeting() {
        // Set up for editing meeting
        meetingController.setMeeting(testMeeting);
        
        // Set field values
        LocalDate date = LocalDate.now().plusDays(2);
        datePicker.setValue(date);
        startTimeField.setText("19:00");
        endTimeField.setText("21:00");
        notesArea.setText("Updated meeting notes");
        
        // Call save method
        meetingController.testHandleSave(null);
        
        // Verify service was called with correct parameters
        verify(meetingService).updateMeetingDateTime(
            testMeeting.getId(),
            date,
            LocalTime.parse("19:00"),
            LocalTime.parse("21:00")
        );
        verify(meetingService).updateNotes(
            testMeeting.getId(),
            "Updated meeting notes"
        );
        
        // Verify dialog was closed
        assertTrue(dialogClosed);
    }
    
    /**
     * Test validation when saving with invalid data.
     */
    @Test
    public void testHandleSaveWithValidationErrors() {
        // Set up for new meeting
        meetingController.setNewMeeting(testProject);
        
        // Set invalid values (missing date)
        when(datePicker.getValue()).thenReturn(null);
        
        // Create a spy of the controller to intercept the showErrorAlert method
        MeetingController controllerSpy = spy(meetingController);
        doNothing().when(controllerSpy).testShowErrorAlert(anyString(), anyString());
        
        // Call save method with null event - we don't need the mock event
        controllerSpy.testHandleSave(null);
        
        // Verify error alert was shown with correct messages
        verify(controllerSpy).testShowErrorAlert("Invalid Input", "Meeting date cannot be empty");
        
        // Verify service was NOT called
        verify(meetingService, never()).createMeeting(any(), any(), any(), anyLong(), anyString());
        
        // Verify dialog was NOT closed - check this through the original method
        // This avoids needing the mockStage variable
        verify(controllerSpy, never()).testCloseDialog();
    }
    
    /**
     * Test canceling the meeting dialog.
     */
    @Test
    public void testHandleCancel() {
        // Call cancel method
        meetingController.testHandleCancel(null);
        
        // Verify dialog was closed
        assertTrue(dialogClosed);
    }
    
    /**
     * Helper method to inject field values using reflection.
     */
    private void injectField(String fieldName, Object value) throws Exception {
        Field field = MeetingController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(meetingController, value);
    }
    
    /**
     * Helper method to override methods for testing purposes.
     */
    private void overrideMethod(String methodName, Runnable implementation) {
        try {
            // This is a simplified version - in real code you'd need to handle different method signatures
            Field field = meetingController.getClass().getDeclaredField(methodName + "Override");
            field.setAccessible(true);
            field.set(meetingController, implementation);
        } catch (Exception e) {
            // In a real implementation, you'd need to handle this better
            e.printStackTrace();
        }
    }
    
    /**
     * Override method for showErrorAlert with parameters.
     */
    private void overrideMethod(String methodName, BiConsumer<String, String> implementation) {
        try {
            Field field = meetingController.getClass().getDeclaredField(methodName + "Override");
            field.setAccessible(true);
            field.set(meetingController, implementation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Simple functional interface for consuming two String parameters.
     */
    @FunctionalInterface
    private interface BiConsumer<T, U> {
        void accept(T t, U u);
    }
}