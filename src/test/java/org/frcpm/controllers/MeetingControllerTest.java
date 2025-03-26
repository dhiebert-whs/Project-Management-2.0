package org.frcpm.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.services.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MeetingControllerTest {

    @Mock
    private MeetingService meetingService;
    
    @InjectMocks
    private MeetingController meetingController;
    
    @Mock
    private DatePicker datePicker;
    
    @Mock
    private TextField startTimeField;
    
    @Mock
    private TextField endTimeField;
    
    @Mock
    private TextArea notesArea;
    
    @Mock
    private Button saveButton;
    
    @Mock
    private Button cancelButton;
    
    @Mock
    private Stage mockStage;
    
    @Mock
    private ActionEvent mockEvent;
    
    private Project testProject;
    private Meeting testMeeting;

    /**
     * Utility method to set private fields using reflection
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            fail("Could not set field " + fieldName + ": " + e.getMessage());
        }
    }

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
        
        // Create test meeting
        testMeeting = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                testProject
        );
        testMeeting.setId(1L);
        testMeeting.setNotes("Test meeting notes");
        
        // Set UI components using reflection instead of mocking controller methods
        setField(meetingController, "datePicker", datePicker);
        setField(meetingController, "startTimeField", startTimeField);
        setField(meetingController, "endTimeField", endTimeField);
        setField(meetingController, "notesArea", notesArea);
        setField(meetingController, "saveButton", saveButton);
        setField(meetingController, "cancelButton", cancelButton);
        
        // Mock service behavior
        when(meetingService.createMeeting(any(), any(), any(), anyLong(), anyString()))
            .thenReturn(testMeeting);
        when(meetingService.updateMeetingDateTime(anyLong(), any(), any(), any()))
            .thenReturn(testMeeting);
        when(meetingService.updateNotes(anyLong(), anyString()))
            .thenReturn(testMeeting);
        
        // Mock UI component behavior
        when(datePicker.getValue()).thenReturn(LocalDate.now().plusDays(1));
        when(startTimeField.getText()).thenReturn("18:00");
        when(endTimeField.getText()).thenReturn("20:00");
        when(notesArea.getText()).thenReturn("Test meeting notes");
        
        // Mock JavaFX scene/stage for dialog closing
        javafx.scene.Scene mockScene = mock(javafx.scene.Scene.class);
        when(saveButton.getScene()).thenReturn(mockScene);
        when(cancelButton.getScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockStage);
    }

    @Test
    public void testInitialize() {
        // Call initialize
        meetingController.testInitialize();
        
        // Verify that default values are set
        verify(datePicker).setValue(any(LocalDate.class));
        verify(startTimeField).setText("16:00");
        verify(endTimeField).setText("18:00");
        
        // Verify that button actions are set
        verify(saveButton).setOnAction(any());
        verify(cancelButton).setOnAction(any());
    }

    @Test
    public void testSetNewMeeting() {
        // Test setting up for a new meeting
        meetingController.setNewMeeting(testProject);
        
        // Verify the fields are initialized correctly
        assertEquals(testProject, meetingController.getProject());
        assertNull(meetingController.getMeeting());
        assertTrue(meetingController.isNewMeeting());
        verify(datePicker).setValue(any(LocalDate.class));
        verify(startTimeField).setText("16:00");
        verify(endTimeField).setText("18:00");
        verify(notesArea).setText("");
    }
    
    @Test
    public void testSetMeeting() {
        // Test setting up for editing an existing meeting
        meetingController.setMeeting(testMeeting);
        
        // Verify the fields are initialized correctly
        assertEquals(testMeeting, meetingController.getMeeting());
        assertEquals(testProject, meetingController.getProject());
        assertFalse(meetingController.isNewMeeting());
        verify(datePicker).setValue(testMeeting.getDate());
        verify(startTimeField).setText(testMeeting.getStartTime().toString());
        verify(endTimeField).setText(testMeeting.getEndTime().toString());
        verify(notesArea).setText(testMeeting.getNotes());
    }
    
    @Test
    public void testHandleSaveForNewMeeting() {
        // Set up for a new meeting
        meetingController.setNewMeeting(testProject);
        
        // Test saving
        meetingController.testHandleSave(mockEvent);
        
        // Verify service was called to create a new meeting
        verify(meetingService).createMeeting(
            datePicker.getValue(),
            LocalTime.parse(startTimeField.getText()),
            LocalTime.parse(endTimeField.getText()),
            testProject.getId(),
            notesArea.getText()
        );
        
        // Verify dialog was closed
        verify(mockStage).close();
    }
    
    @Test
    public void testHandleSaveForExistingMeeting() {
        // Set up for editing an existing meeting
        meetingController.setMeeting(testMeeting);
        
        // Test saving
        meetingController.testHandleSave(mockEvent);
        
        // Verify service was called to update the meeting
        verify(meetingService).updateMeetingDateTime(
            testMeeting.getId(),
            datePicker.getValue(),
            LocalTime.parse(startTimeField.getText()),
            LocalTime.parse(endTimeField.getText())
        );
        verify(meetingService).updateNotes(
            testMeeting.getId(),
            notesArea.getText()
        );
        
        // Verify dialog was closed
        verify(mockStage).close();
    }
    
    @Test
    public void testHandleSaveWithValidationErrors() {
        // Set up for a new meeting with invalid values
        meetingController.setNewMeeting(testProject);
        
        // Mock missing date
        when(datePicker.getValue()).thenReturn(null);
        
        // Test saving
        meetingController.testHandleSave(mockEvent);
        
        // Verify service was NOT called to create a new meeting
        verify(meetingService, never()).createMeeting(any(), any(), any(), anyLong(), anyString());
        
        // Verify dialog was NOT closed
        verify(mockStage, never()).close();
    }
    
    @Test
    public void testHandleCancel() {
        // Test canceling
        meetingController.testHandleCancel(mockEvent);
        
        // Verify dialog was closed
        verify(mockStage).close();
    }
}