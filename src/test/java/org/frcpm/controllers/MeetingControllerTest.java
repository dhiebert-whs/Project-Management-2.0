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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testfx.framework.junit5.ApplicationExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the MeetingController class with MVVM pattern.
 */
@ExtendWith(ApplicationExtension.class)
public class MeetingControllerTest extends BaseJavaFXTest {

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

        // Inject fields into controller using reflection
        try {
            java.lang.reflect.Field viewModelField = MeetingController.class.getDeclaredField("viewModel");
            viewModelField.setAccessible(true);
            viewModelField.set(controller, mockViewModel);

            // Inject necessary JavaFX components
            java.lang.reflect.Field datePickerField = MeetingController.class.getDeclaredField("datePicker");
            datePickerField.setAccessible(true);
            datePickerField.set(controller, new DatePicker());

            java.lang.reflect.Field startTimeField = MeetingController.class.getDeclaredField("startTimeField");
            startTimeField.setAccessible(true);
            startTimeField.set(controller, new TextField());

            java.lang.reflect.Field endTimeField = MeetingController.class.getDeclaredField("endTimeField");
            endTimeField.setAccessible(true);
            endTimeField.set(controller, new TextField());

            java.lang.reflect.Field notesAreaField = MeetingController.class.getDeclaredField("notesArea");
            notesAreaField.setAccessible(true);
            notesAreaField.set(controller, new TextArea());

            java.lang.reflect.Field saveButtonField = MeetingController.class.getDeclaredField("saveButton");
            saveButtonField.setAccessible(true);
            saveButtonField.set(controller, new Button());

            java.lang.reflect.Field cancelButtonField = MeetingController.class.getDeclaredField("cancelButton");
            cancelButtonField.setAccessible(true);
            cancelButtonField.set(controller, new Button());

        } catch (Exception e) {
            fail("Failed to set up controller: " + e.getMessage());
        }
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
        // Arrange
        Alert mockAlert = mock(Alert.class);
        doReturn(mockAlert).when(controller).createAlert(any());

        // Act
        controller.showErrorAlert("Test Title", "Test Message");

        // Assert
        verify(mockAlert).setTitle("Error");
        verify(mockAlert).setHeaderText("Test Title");
        verify(mockAlert).setContentText("Test Message");
        verify(mockAlert).showAndWait();
    }
}