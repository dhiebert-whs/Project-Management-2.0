package org.frcpm.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import org.frcpm.binding.Command;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.viewmodels.MeetingViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the MeetingController class with MVVM pattern.
 */
public class MeetingControllerTest {

    @Spy
    private MeetingController controller;

    @Mock
    private MeetingViewModel mockViewModel;

    @Mock
    private Meeting mockMeeting;

    @Mock
    private Project mockProject;

    @Mock
    private Command mockSaveCommand;
    
    @Mock
    private Alert mockAlert;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up mocks
        when(mockMeeting.getDate()).thenReturn(LocalDate.now());
        when(mockMeeting.getStartTime()).thenReturn(LocalTime.of(16, 0));
        when(mockMeeting.getEndTime()).thenReturn(LocalTime.of(18, 0));
        when(mockMeeting.getProject()).thenReturn(mockProject);

        // Set up ViewModel mocks
        when(mockViewModel.dateProperty()).thenReturn(new SimpleObjectProperty<>(LocalDate.now()));
        when(mockViewModel.startTimeStringProperty()).thenReturn(new SimpleStringProperty("16:00"));
        when(mockViewModel.endTimeStringProperty()).thenReturn(new SimpleStringProperty("18:00"));
        when(mockViewModel.notesProperty()).thenReturn(new SimpleStringProperty(""));
        when(mockViewModel.errorMessageProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
        when(mockViewModel.getMeeting()).thenReturn(mockMeeting);

        // Set the mock ViewModel
        controller.setViewModel(mockViewModel);
        
        // Mock alert creation
        doReturn(mockAlert).when(controller).createAlert(any());
    }

    @Test
    public void testSetNewMeeting() {
        // Act
        controller.setNewMeeting(mockProject);

        // Assert
        verify(mockViewModel).initNewMeeting(mockProject);
    }

    @Test
    public void testSetMeeting() {
        // Act
        controller.setMeeting(mockMeeting);

        // Assert
        verify(mockViewModel).initExistingMeeting(mockMeeting);
    }

    @Test
    public void testGetViewModel() {
        // Act
        MeetingViewModel result = controller.getViewModel();

        // Assert
        assertEquals(mockViewModel, result);
    }

    @Test
    public void testGetMeeting() {
        // Act
        Meeting result = controller.getMeeting();

        // Assert
        assertEquals(mockMeeting, result);
        verify(mockViewModel).getMeeting();
    }

    @Test
    public void testShowErrorAlert() {
        // Act
        controller.showErrorAlert("Test Title", "Test Message");

        // Assert
        verify(mockAlert).setTitle("Error");
        verify(mockAlert).setHeaderText("Test Title");
        verify(mockAlert).setContentText("Test Message");
        verify(mockAlert).showAndWait();
    }
    
    @Test
    public void testShowInfoAlert() {
        // Act
        controller.showInfoAlert("Test Title", "Test Message");

        // Assert
        verify(mockAlert).setTitle("Information");
        verify(mockAlert).setHeaderText("Test Title");
        verify(mockAlert).setContentText("Test Message");
        verify(mockAlert).showAndWait();
    }
    
    @Test
    public void testCloseDialog() {
        // This is a void method with UI interactions
        // Just call it to ensure no uncaught exceptions
        controller.closeDialog();
    }
}