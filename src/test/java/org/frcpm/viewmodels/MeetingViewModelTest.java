// src/test/java/org/frcpm/viewmodels/MeetingViewModelTest.java

package org.frcpm.viewmodels;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.services.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingViewModelTest {

    @Mock
    private MeetingService meetingService;

    @Mock
    private Project project;

    private MeetingViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new MeetingViewModel(meetingService);
    }

    @Test
    void testInitNewMeeting() {
        // Act
        viewModel.initNewMeeting(project);

        // Assert
        assertTrue(viewModel.isNewMeeting());
        assertEquals(project, viewModel.getProject());
        assertEquals(LocalDate.now(), viewModel.getDate());
        assertEquals("16:00", viewModel.getStartTimeString());
        assertEquals("18:00", viewModel.getEndTimeString());
        assertEquals("", viewModel.getNotes());
        assertFalse(viewModel.isDirty());
    }

    @Test
    void testInitExistingMeeting() {
        // Arrange
        Meeting meeting = new Meeting(
                LocalDate.now().minusDays(1),
                LocalTime.of(14, 0),
                LocalTime.of(15, 30),
                project);
        meeting.setId(1L);
        meeting.setNotes("Test meeting notes");

        // Act
        viewModel.initExistingMeeting(meeting);

        // Assert
        assertFalse(viewModel.isNewMeeting());
        assertEquals(meeting, viewModel.getMeeting());
        assertEquals(project, viewModel.getProject());
        assertEquals(LocalDate.now().minusDays(1), viewModel.getDate());
        assertEquals("14:00", viewModel.getStartTimeString());
        assertEquals("15:30", viewModel.getEndTimeString());
        assertEquals("Test meeting notes", viewModel.getNotes());
        assertFalse(viewModel.isDirty());
    }

    @Test
    void testInitWithNullValues() {
        // Act & Assert
        viewModel.initNewMeeting(null);
        // Should not throw exception, but log a warning

        Meeting nullMeeting = null;
        viewModel.initExistingMeeting(nullMeeting);
        // Should not throw exception, but log a warning
    }

    @Test
    void testValidation_ValidMeeting() {
        // Arrange
        viewModel.initNewMeeting(project);

        // Assert - Default values should be valid
        assertTrue(viewModel.isValid());
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    void testValidation_EmptyDate() {
        // Arrange
        viewModel.initNewMeeting(project);
        viewModel.setDate(null);

        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("date"));
    }

    @Test
    void testValidation_EmptyStartTime() {
        // Arrange
        viewModel.initNewMeeting(project);
        viewModel.setStartTimeString("");

        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Start time"));
    }

    @Test
    void testValidation_EmptyEndTime() {
        // Arrange
        viewModel.initNewMeeting(project);
        viewModel.setEndTimeString("");

        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("End time"));
    }

    @Test
    void testValidation_InvalidStartTime() {
        // Arrange
        viewModel.initNewMeeting(project);
        viewModel.setStartTimeString("invalid");

        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Start time"));
    }

    @Test
    void testValidation_InvalidEndTime() {
        // Arrange
        viewModel.initNewMeeting(project);
        viewModel.setEndTimeString("invalid");

        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("End time"));
    }

    @Test
    void testValidation_EndTimeBeforeStartTime() {
        // Arrange
        viewModel.initNewMeeting(project);
        viewModel.setStartTimeString("17:00");
        viewModel.setEndTimeString("16:00");

        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("End time must be after start time"));
    }

    @Test
    void testValidation_EndTimeEqualsStartTime() {
        // Arrange
        viewModel.initNewMeeting(project);
        viewModel.setStartTimeString("16:00");
        viewModel.setEndTimeString("16:00");

        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("End time must be after start time"));
    }

    @Test
    void testValidation_MultipleErrors() {
        // Arrange
        viewModel.initNewMeeting(project);
        viewModel.setDate(null);
        viewModel.setStartTimeString("invalid");
        viewModel.setEndTimeString("");

        // Assert
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        String errorMsg = viewModel.getErrorMessage();
        assertTrue(errorMsg.contains("date"));
        assertTrue(errorMsg.contains("Start time"));
        assertTrue(errorMsg.contains("End time"));
    }

    @Test
    void testSaveCommand_NewMeeting() {
        // Arrange
        when(project.getId()).thenReturn(1L);

        viewModel.initNewMeeting(project);
        viewModel.setDate(LocalDate.of(2025, 4, 10));
        viewModel.setStartTimeString("09:00");
        viewModel.setEndTimeString("10:30");
        viewModel.setNotes("Important meeting");
        
        // Set dirty flag for command to execute
        viewModel.setDirty(true);

        Meeting savedMeeting = new Meeting(
                LocalDate.of(2025, 4, 10),
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                project);
        savedMeeting.setId(1L);
        savedMeeting.setNotes("Important meeting");

        when(meetingService.createMeeting(
                eq(LocalDate.of(2025, 4, 10)),
                eq(LocalTime.of(9, 0)),
                eq(LocalTime.of(10, 30)),
                eq(1L),
                eq("Important meeting"))).thenReturn(savedMeeting);

        // Act
        viewModel.getSaveCommand().execute();

        // Assert
        verify(meetingService).createMeeting(
                eq(LocalDate.of(2025, 4, 10)),
                eq(LocalTime.of(9, 0)),
                eq(LocalTime.of(10, 30)),
                eq(1L),
                eq("Important meeting"));

        assertEquals(savedMeeting, viewModel.getMeeting());
        assertFalse(viewModel.isDirty());
    }

    @Test
    void testSaveCommand_ExistingMeeting() {
        // Arrange
        Meeting existingMeeting = new Meeting(
                LocalDate.of(2025, 4, 10),
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                project);
        existingMeeting.setId(1L);
        existingMeeting.setNotes("Original notes");

        viewModel.initExistingMeeting(existingMeeting);
        viewModel.setDate(LocalDate.of(2025, 4, 11));
        viewModel.setStartTimeString("10:00");
        viewModel.setEndTimeString("11:30");
        viewModel.setNotes("Updated notes");
        
        // Set dirty flag for command to execute
        viewModel.setDirty(true);

        // Create the updated meeting object that will be returned by both service calls
        Meeting updatedMeeting = new Meeting(
                LocalDate.of(2025, 4, 11),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                project);
        updatedMeeting.setId(1L);
        updatedMeeting.setNotes("Updated notes");

        // First stub the updateMeetingDateTime call
        when(meetingService.updateMeetingDateTime(
                eq(1L),
                eq(LocalDate.of(2025, 4, 11)),
                eq(LocalTime.of(10, 0)),
                eq(LocalTime.of(11, 30)))).thenReturn(updatedMeeting);

        // Then stub the updateNotes call
        when(meetingService.updateNotes(eq(1L), eq("Updated notes"))).thenReturn(updatedMeeting);

        // Act
        viewModel.getSaveCommand().execute();

        // Assert
        verify(meetingService).updateMeetingDateTime(
                eq(1L),
                eq(LocalDate.of(2025, 4, 11)),
                eq(LocalTime.of(10, 0)),
                eq(LocalTime.of(11, 30)));

        verify(meetingService).updateNotes(eq(1L), eq("Updated notes"));

        assertEquals(updatedMeeting, viewModel.getMeeting());
        assertFalse(viewModel.isDirty());
    }

    @Test
    void testSaveCommand_CannotExecuteWhenInvalid() {
        // Arrange
        viewModel.initNewMeeting(project);
        viewModel.setDate(null); // Make invalid
        viewModel.setDirty(true);
        
        // Assert
        assertFalse(viewModel.getSaveCommand().canExecute());
    }
    
    @Test
    void testSaveCommand_CannotExecuteWhenNotDirty() {
        // Arrange
        viewModel.initNewMeeting(project);
        // Should be valid but not dirty
        
        // Assert
        assertFalse(viewModel.getSaveCommand().canExecute());
    }

    @Test
    void testExceptionHandlingInSave() {
        // Arrange
        when(project.getId()).thenReturn(1L);

        viewModel.initNewMeeting(project);
        viewModel.setDirty(true);

        // Set up to throw exception on save
        when(meetingService.createMeeting(
                any(), any(), any(), anyLong(), any())).thenThrow(new RuntimeException("Test exception"));

        // Act
        viewModel.getSaveCommand().execute();

        // Assert
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Failed to save meeting"));
        assertTrue(viewModel.getErrorMessage().contains("Test exception"));
    }
    
    @Test
    void testCleanupResources() {
        // Setup
        viewModel.initNewMeeting(project);
        
        // Act
        viewModel.cleanupResources();
        
        // Assert - Just verify it doesn't throw exceptions
        // This is primarily testing that the method exists and can be called
    }

    @Test
    void testTimeFormatting() {
        // Arrange & Act
        String formattedTime = viewModel.formatTime(LocalTime.of(13, 45));
        LocalTime parsedTime = viewModel.parseTime("13:45");
        
        // Assert
        assertEquals("13:45", formattedTime);
        assertEquals(LocalTime.of(13, 45), parsedTime);
    }
    
    @Test
    void testTimeParsingWithInvalidFormat() {
        // Arrange & Act
        LocalTime parsedTime = viewModel.parseTime("invalid");
        
        // Assert
        assertNull(parsedTime);
    }
}