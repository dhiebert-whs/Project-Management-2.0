// src/test/java/org/frcpm/viewmodels/MeetingViewModelTest.java
package org.frcpm.viewmodels;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.services.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MeetingViewModelTest {

    @Mock
    private MeetingService meetingService;
    
    private MeetingViewModel viewModel;
    private Project testProject;
    private Meeting testMeeting;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Create test data
        testProject = new Project(
            "Test Project",
            LocalDate.now(),
            LocalDate.now().plusWeeks(6),
            LocalDate.now().plusWeeks(8)
        );
        testProject.setId(1L);
        
        testMeeting = new Meeting(
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0),
            testProject
        );
        testMeeting.setId(1L);
        testMeeting.setNotes("Test meeting notes");
        
        // Set up mock service
        when(meetingService.createMeeting(any(), any(), any(), anyLong(), anyString()))
            .thenReturn(testMeeting);
        when(meetingService.updateMeetingDateTime(anyLong(), any(), any(), any()))
            .thenReturn(testMeeting);
        when(meetingService.updateNotes(anyLong(), anyString()))
            .thenReturn(testMeeting);
        
        // Create ViewModel with mocked service
        viewModel = new MeetingViewModel(meetingService);
    }
    
    @Test
    public void testInitNewMeeting() {
        // Init for new meeting
        viewModel.initNewMeeting(testProject);
        
        // Verify state
        assertEquals(testProject, viewModel.getProject());
        assertNull(viewModel.getMeeting());
        assertTrue(viewModel.isNewMeeting());
        
        // Verify default values
        assertEquals(LocalDate.now(), viewModel.dateProperty().get());
        assertEquals("16:00", viewModel.startTimeStringProperty().get());
        assertEquals("18:00", viewModel.endTimeStringProperty().get());
        assertEquals("", viewModel.notesProperty().get());
        
        // Verify validation state
        assertTrue(viewModel.isValid());
        assertNull(viewModel.getErrorMessage());
    }
    
    @Test
    public void testInitExistingMeeting() {
        // Init for existing meeting
        viewModel.initExistingMeeting(testMeeting);
        
        // Verify state
        assertEquals(testMeeting, viewModel.meetingProperty().get());
        assertEquals(testProject, viewModel.getProject());
        assertFalse(viewModel.isNewMeeting());
        
        // Verify field values
        assertEquals(testMeeting.getDate(), viewModel.dateProperty().get());
        assertEquals(testMeeting.getStartTime().toString(), viewModel.startTimeStringProperty().get());
        assertEquals(testMeeting.getEndTime().toString(), viewModel.endTimeStringProperty().get());
        assertEquals(testMeeting.getNotes(), viewModel.notesProperty().get());
        
        // Verify validation state
        assertTrue(viewModel.isValid());
        assertNull(viewModel.getErrorMessage());
    }
    
    @Test
    public void testValidation_EmptyDate() {
        // Set up
        viewModel.initNewMeeting(testProject);
        
        // Set invalid value
        viewModel.dateProperty().set(null);
        
        // Verify validation state
        assertFalse(viewModel.isValid());
        assertEquals("Meeting date cannot be empty", viewModel.getErrorMessage());
    }
    
    @Test
    public void testValidation_InvalidStartTime() {
        // Set up
        viewModel.initNewMeeting(testProject);
        
        // Set invalid value
        viewModel.startTimeStringProperty().set("invalid");
        
        // Verify validation state
        assertFalse(viewModel.isValid());
        assertEquals("Start time format should be HH:MM", viewModel.getErrorMessage());
    }
    
    @Test
    public void testValidation_InvalidEndTime() {
        // Set up
        viewModel.initNewMeeting(testProject);
        
        // Set invalid value
        viewModel.endTimeStringProperty().set("invalid");
        
        // Verify validation state
        assertFalse(viewModel.isValid());
        assertEquals("End time format should be HH:MM", viewModel.getErrorMessage());
    }
    
    @Test
    public void testValidation_EndTimeBeforeStartTime() {
        // Set up
        viewModel.initNewMeeting(testProject);
        
        // Set invalid values
        viewModel.startTimeStringProperty().set("18:00");
        viewModel.endTimeStringProperty().set("17:00");
        
        // Verify validation state
        assertFalse(viewModel.isValid());
        assertEquals("End time must be after start time", viewModel.getErrorMessage());
    }
    
    @Test
    public void testSave_NewMeeting() {
        // Set up
        viewModel.initNewMeeting(testProject);
        
        // Set values
        LocalDate date = LocalDate.now().plusDays(1);
        viewModel.dateProperty().set(date);
        viewModel.startTimeStringProperty().set("18:00");
        viewModel.endTimeStringProperty().set("20:00");
        viewModel.notesProperty().set("Test notes");
        
        // Execute save command
        viewModel.getSaveCommand().execute();
        
        // Verify service call
        verify(meetingService).createMeeting(
            eq(date),
            eq(LocalTime.of(18, 0)),
            eq(LocalTime.of(20, 0)),
            eq(testProject.getId()),
            eq("Test notes")
        );
        
        // Verify meeting is updated
        assertEquals(testMeeting, viewModel.getMeeting());
    }
    
    @Test
    public void testSave_ExistingMeeting() {
        // Set up
        viewModel.initExistingMeeting(testMeeting);
        
        // Set updated values
        LocalDate date = LocalDate.now().plusDays(2);
        viewModel.dateProperty().set(date);
        viewModel.startTimeStringProperty().set("19:00");
        viewModel.endTimeStringProperty().set("21:00");
        viewModel.notesProperty().set("Updated notes");
        
        // Execute save command
        viewModel.getSaveCommand().execute();
        
        // Verify service calls
        verify(meetingService).updateMeetingDateTime(
            eq(testMeeting.getId()),
            eq(date),
            eq(LocalTime.of(19, 0)),
            eq(LocalTime.of(21, 0))
        );
        verify(meetingService).updateNotes(
            eq(testMeeting.getId()),
            eq("Updated notes")
        );
        
        // Verify meeting is updated
        assertEquals(testMeeting, viewModel.getMeeting());
    }
}
