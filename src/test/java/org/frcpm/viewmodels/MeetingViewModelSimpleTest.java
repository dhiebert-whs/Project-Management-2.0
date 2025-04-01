// src/test/java/org/frcpm/viewmodels/MeetingViewModelSimpleTest.java
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

public class MeetingViewModelSimpleTest {

    @Mock
    private MeetingService meetingService;

    private MeetingViewModel viewModel;
    private Project testProject;
    private Meeting testMeeting;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test data
        testProject = new Project(
                "Test Project",
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8));
        testProject.setId(1L);

        testMeeting = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                testProject);
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
        assertEquals(LocalDate.now(), viewModel.getDate());
        assertEquals("16:00", viewModel.getStartTimeString());
        assertEquals("18:00", viewModel.getEndTimeString());
        assertEquals("", viewModel.getNotes());

        // Verify validation state
        assertTrue(viewModel.isValid());
    }

    @Test
    public void testInitExistingMeeting() {
        // Init for existing meeting
        viewModel.initExistingMeeting(testMeeting);

        // Verify state
        assertEquals(testMeeting, viewModel.getMeeting());
        assertEquals(testProject, viewModel.getProject());
        assertFalse(viewModel.isNewMeeting());

        // Verify field values
        assertEquals(testMeeting.getDate(), viewModel.getDate());
        assertEquals(testMeeting.getStartTime().toString(), viewModel.getStartTimeString());
        assertEquals(testMeeting.getEndTime().toString(), viewModel.getEndTimeString());
        assertEquals(testMeeting.getNotes(), viewModel.getNotes());

        // Verify validation state
        assertTrue(viewModel.isValid());
    }

    @Test
    public void testValidation_EmptyDate() {
        // Set up
        viewModel.initNewMeeting(testProject);

        // Set invalid value
        viewModel.setDate(null);

        // Verify validation state
        assertFalse(viewModel.isValid());
        assertTrue(viewModel.getErrorMessage().contains("date cannot be empty"));
    }

    @Test
    public void testValidation_InvalidStartTime() {
        // Set up
        viewModel.initNewMeeting(testProject);

        // Set invalid value
        viewModel.setStartTimeString("invalid");

        // Verify validation state
        assertFalse(viewModel.isValid());
        assertTrue(viewModel.getErrorMessage().contains("Start time format"));
    }

    @Test
    public void testValidation_EndTimeBeforeStartTime() {
        // Set up
        viewModel.initNewMeeting(testProject);

        // Set invalid values
        viewModel.setStartTimeString("18:00");
        viewModel.setEndTimeString("17:00");

        // Verify validation state
        assertFalse(viewModel.isValid());
        assertTrue(viewModel.getErrorMessage().contains("End time must be after start time"));
    }

    @Test
    public void testSaveCommand_ExecutesWhenValid() {
        // Set up
        viewModel.initNewMeeting(testProject);
        assertTrue(viewModel.isValid()); // Should be valid with default values

        // Verify command can execute
        assertTrue(viewModel.getSaveCommand().canExecute());
    }

    @Test
    public void testSaveCommand_BlockedWhenInvalid() {
        // Set up
        viewModel.initNewMeeting(testProject);
        viewModel.setDate(null); // Make invalid
        assertFalse(viewModel.isValid());

        // Verify command cannot execute
        assertFalse(viewModel.getSaveCommand().canExecute());
    }
}