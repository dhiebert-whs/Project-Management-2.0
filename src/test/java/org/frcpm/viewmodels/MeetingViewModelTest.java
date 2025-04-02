// src/test/java/org/frcpm/viewmodels/MeetingViewModelTest.java

package org.frcpm.viewmodels;

import org.frcpm.binding.Command;
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
        
        // Set up mock ID
        when(project.getId()).thenReturn(1L);
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
        assertTrue(viewModel.isValid());
    }
    
    @Test
    void testInitExistingMeeting() {
        // Arrange
        Meeting meeting = new Meeting(
            LocalDate.now().minusDays(1),
            LocalTime.of(14, 0),
            LocalTime.of(15, 30),
            project
        );
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
        assertTrue(viewModel.isValid());
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
    void testSaveCommand_NewMeeting() {
        // Arrange
        viewModel.initNewMeeting(project);
        viewModel.setDate(LocalDate.of(2025, 4, 10));
        viewModel.setStartTimeString("09:00");
        viewModel.setEndTimeString("10:30");
        viewModel.setNotes("Important meeting");
        
        Meeting savedMeeting = new Meeting(
            LocalDate.of(2025, 4, 10),
            LocalTime.of(9, 0),
            LocalTime.of(10, 30),
            project
        );
        savedMeeting.setId(1L);
        savedMeeting.setNotes("Important meeting");
        
        when(meetingService.createMeeting(
            eq(LocalDate.of(2025, 4, 10)),
            eq(LocalTime.of(9, 0)),
            eq(LocalTime.of(10, 30)),
            eq(1L),
            eq("Important meeting")
        )).thenReturn(savedMeeting);
        
        // Act
        viewModel.getSaveCommand().execute();
        
        // Assert
        verify(meetingService).createMeeting(
            eq(LocalDate.of(2025, 4, 10)),
            eq(LocalTime.of(9, 0)),
            eq(LocalTime.of(10, 30)),
            eq(1L),
            eq("Important meeting")
        );
        
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
            project
        );
        existingMeeting.setId(1L);
        existingMeeting.setNotes("Original notes");
        
        viewModel.initExistingMeeting(existingMeeting);
        viewModel.setDate(LocalDate.of(2025, 4, 11));
        viewModel.setStartTimeString("10:00");
        viewModel.setEndTimeString("11:30");
        viewModel.setNotes("Updated notes");
        
        Meeting updatedMeeting = new Meeting(
            LocalDate.of(2025, 4, 11),
            LocalTime.of(10, 0),
            LocalTime.of(11, 30),
            project
        );
        updatedMeeting.setId(1L);
        updatedMeeting.setNotes("Updated notes");
        
        when(meetingService.updateMeetingDateTime(
            eq(1L),
            eq(LocalDate.of(2025, 4, 11)),
            eq(LocalTime.of(10, 0)),
            eq(LocalTime.of(11, 30))
        )).thenReturn(updatedMeeting);
        
        when(meetingService.updateNotes(eq(1L), eq("Updated notes"))).thenReturn(updatedMeeting);
        
        // Act
        viewModel.getSaveCommand().execute();
        
        // Assert
        verify(meetingService).updateMeetingDateTime(
            eq(1L),
            eq(LocalDate.of(2025, 4, 11)),
            eq(LocalTime.of(10, 0)),
            eq(LocalTime.of(11, 30))
        );
        
        verify(meetingService).updateNotes(eq(1L), eq("Updated notes"));
        
        assertEquals(updatedMeeting, viewModel.getMeeting());
        assertFalse(viewModel.isDirty());
    }
    
    @Test
    void testDirtyFlag_AfterNotesChange() {
        // Arrange
        viewModel.initNewMeeting(project);
        assertFalse(viewModel.isDirty());
        
        // Act - Change notes
        viewModel.setNotes("New notes");
        
        // Assert
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    void testDirtyFlag_AfterDateChange() {
        // Arrange
        viewModel.initNewMeeting(project);
        assertFalse(viewModel.isDirty());
        
        // Act - Change date
        viewModel.setDate(LocalDate.now().plusDays(1));
        
        // Assert - Date affects validation but also should mark as dirty
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    void testGetCommands() {
        // Arrange
        viewModel.initNewMeeting(project);
        
        // Assert
        assertNotNull(viewModel.getSaveCommand());
        assertNotNull(viewModel.getCancelCommand());
        
        // Verify command types
        assertTrue(viewModel.getSaveCommand() instanceof Command);
        assertTrue(viewModel.getCancelCommand() instanceof Command);
    }
    
    @Test
    void testSaveCommand_CanExecuteWithValidMeeting() {
        // Arrange
        viewModel.initNewMeeting(project);
        
        // Assert - Default values should be valid
        assertTrue(viewModel.getSaveCommand().canExecute());
    }
    
    @Test
    void testSaveCommand_CannotExecuteWithInvalidMeeting() {
        // Arrange
        viewModel.initNewMeeting(project);
        viewModel.setStartTimeString("invalid"); // Invalid start time
        
        // Assert
        assertFalse(viewModel.getSaveCommand().canExecute());
    }
    
    @Test
    void testExceptionHandlingInSave() {
        // Arrange
        viewModel.initNewMeeting(project);
        
        // Set up to throw exception on save
        when(meetingService.createMeeting(
            any(), any(), any(), anyLong(), any())
        ).thenThrow(new RuntimeException("Test exception"));
        
        // Act
        viewModel.getSaveCommand().execute();
        
        // Assert
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Failed to save meeting"));
        assertTrue(viewModel.getErrorMessage().contains("Test exception"));
        assertFalse(viewModel.isValid());
    }
    
    @Test
    void testSettersAndGetters() {
        // Test all getters and setters for coverage
        viewModel.setDate(LocalDate.of(2025, 5, 1));
        assertEquals(LocalDate.of(2025, 5, 1), viewModel.getDate());
        
        viewModel.setStartTimeString("14:30");
        assertEquals("14:30", viewModel.getStartTimeString());
        
        viewModel.setEndTimeString("16:45");
        assertEquals("16:45", viewModel.getEndTimeString());
        
        viewModel.setNotes("Meeting notes");
        assertEquals("Meeting notes", viewModel.getNotes());
        
        Meeting testMeeting = new Meeting(LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1), project);
        viewModel.setMeeting(testMeeting);
        assertEquals(testMeeting, viewModel.getMeeting());
        
        viewModel.setProject(project);
        assertEquals(project, viewModel.getProject());
        
        viewModel.setIsNewMeeting(true);
        assertTrue(viewModel.isNewMeeting());
        
        viewModel.setValid(false);
        assertFalse(viewModel.isValid());
    }
    
    @Test
    void testProperties() {
        // Test all JavaFX properties
        viewModel.initNewMeeting(project);
        
        // Test date property
        assertEquals(LocalDate.now(), viewModel.dateProperty().get());
        LocalDate newDate = LocalDate.now().plusDays(1);
        viewModel.dateProperty().set(newDate);
        assertEquals(newDate, viewModel.getDate());
        
        // Test start time string property
        assertEquals("16:00", viewModel.startTimeStringProperty().get());
        viewModel.startTimeStringProperty().set("17:00");
        assertEquals("17:00", viewModel.getStartTimeString());
        
        // Test end time string property
        assertEquals("18:00", viewModel.endTimeStringProperty().get());
        viewModel.endTimeStringProperty().set("19:00");
        assertEquals("19:00", viewModel.getEndTimeString());
        
        // Test notes property
        assertEquals("", viewModel.notesProperty().get());
        viewModel.notesProperty().set("Test notes");
        assertEquals("Test notes", viewModel.getNotes());
        
        // Test meeting property
        assertNull(viewModel.meetingProperty().get());
        Meeting testMeeting = new Meeting(LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1), project);
        viewModel.meetingProperty().set(testMeeting);
        assertEquals(testMeeting, viewModel.getMeeting());
        
        // Test project property
        assertEquals(project, viewModel.projectProperty().get());
        Project newProject = mock(Project.class);
        viewModel.projectProperty().set(newProject);
        assertEquals(newProject, viewModel.getProject());
        
        // Test newMeeting property
        assertTrue(viewModel.isNewMeetingProperty().get());
        viewModel.isNewMeetingProperty().set(false);
        assertFalse(viewModel.isNewMeeting());
        
        // Test valid property
        assertTrue(viewModel.validProperty().get());
        viewModel.validProperty().set(false);
        assertFalse(viewModel.isValid());
        
        // Test error message property
        assertNull(viewModel.errorMessageProperty().get());
        viewModel.errorMessageProperty().set("Test error");
        assertEquals("Test error", viewModel.getErrorMessage());
        
        // Test dirty property
        assertTrue(viewModel.dirtyProperty().get()); // Should be dirty after all the changes
        viewModel.dirtyProperty().set(false);
        assertFalse(viewModel.isDirty());
    }
}