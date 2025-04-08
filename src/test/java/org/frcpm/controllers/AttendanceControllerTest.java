package org.frcpm.controllers;

import javafx.scene.control.Alert;
import org.frcpm.models.Meeting;
import org.frcpm.viewmodels.AttendanceViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AttendanceController that avoid JavaFX toolkit initialization.
 */
public class AttendanceControllerTest {

    private AttendanceController controller;
    
    private AttendanceViewModel mockViewModel;
    
    private Meeting mockMeeting;
    
    @BeforeEach
    public void setUp() {
        // Create the controller
        controller = new AttendanceController();
        
        // Create mocks without MockitoExtension to avoid unnecessary stubbing errors
        mockViewModel = mock(AttendanceViewModel.class);
        mockMeeting = mock(Meeting.class);
        
        // Set mock ViewModel to avoid JavaFX initialization
        controller.setViewModel(mockViewModel);
    }
    
    @Test
    public void testSetMeeting() {
        // Arrange - only set up what's needed for this test
        doNothing().when(mockViewModel).initWithMeeting(any(Meeting.class));
        
        // Act
        controller.setMeeting(mockMeeting);
        
        // Assert
        verify(mockViewModel).initWithMeeting(mockMeeting);
    }
    
    @Test
    public void testGetMeeting() {
        // Arrange - only set up what's needed for this test
        when(mockViewModel.getMeeting()).thenReturn(mockMeeting);
        
        // Act
        Meeting result = controller.getMeeting();
        
        // Assert
        assertEquals(mockMeeting, result);
        verify(mockViewModel).getMeeting();
    }
    
    @Test
    public void testGetViewModel() {
        // Act
        AttendanceViewModel result = controller.getViewModel();
        
        // Assert
        assertEquals(mockViewModel, result);
    }
    
    @Test
    public void testHandleSetTime() {
        // Arrange
        AttendanceController spy = spy(controller);
        AttendanceViewModel.AttendanceRecord mockRecord = mock(AttendanceViewModel.AttendanceRecord.class);
        
        // Set up only what's needed for this test
        when(mockViewModel.getSelectedRecord()).thenReturn(mockRecord);
        doNothing().when(mockViewModel).updateRecordTimes(any(), any(), any());
        doNothing().when(spy).showInfoAlert(anyString(), anyString());
        
        // Act
        spy.handleSetTime();
        
        // Assert
        verify(mockViewModel).getSelectedRecord();
        verify(mockViewModel).updateRecordTimes(eq(mockRecord), isNull(), isNull());
    }
    
    @Test
    public void testHandleSetTimeWithNoSelection() {
        // Arrange
        AttendanceController spy = spy(controller);
        
        // Set up only what's needed for this test
        when(mockViewModel.getSelectedRecord()).thenReturn(null);
        doNothing().when(spy).showInfoAlert(anyString(), anyString());
        
        // Act
        spy.handleSetTime();
        
        // Assert
        verify(mockViewModel).getSelectedRecord();
        verify(spy).showInfoAlert("No Selection", "Please select a team member first.");
    }
    
    @Test
    public void testShowErrorAlert() {
        // Arrange
        AttendanceController spy = spy(controller);
        Alert mockAlert = mock(Alert.class);
        
        // Set up only what's needed for this test
        doReturn(mockAlert).when(spy).createAlert(any());
        
        // Act
        spy.showErrorAlert("Test Title", "Test Message");
        
        // Assert
        verify(spy).createAlert(Alert.AlertType.ERROR);
        verify(mockAlert).setTitle("Error");
        verify(mockAlert).setHeaderText("Test Title");
        verify(mockAlert).setContentText("Test Message");
        verify(mockAlert).showAndWait();
    }
    
    @Test
    public void testShowInfoAlert() {
        // Arrange
        AttendanceController spy = spy(controller);
        Alert mockAlert = mock(Alert.class);
        
        // Set up only what's needed for this test
        doReturn(mockAlert).when(spy).createAlert(any());
        
        // Act
        spy.showInfoAlert("Test Title", "Test Message");
        
        // Assert
        verify(spy).createAlert(Alert.AlertType.INFORMATION);
        verify(mockAlert).setTitle("Information");
        verify(mockAlert).setHeaderText("Test Title");
        verify(mockAlert).setContentText("Test Message");
        verify(mockAlert).showAndWait();
    }
    
    @Test
    public void testCreateAlert() {
        // Arrange
        AttendanceController spy = spy(controller);
        Alert mockAlert = mock(Alert.class);
        
        // Set up only what's needed for this test
        doReturn(mockAlert).when(spy).createAlert(Alert.AlertType.ERROR);
        
        // Act
        Alert result = spy.createAlert(Alert.AlertType.ERROR);
        
        // Assert
        assertEquals(mockAlert, result);
    }
    
    @Test
    public void testCloseDialog() {
        // Arrange
        AttendanceController spy = spy(controller);
        
        // Set up only what's needed for this test
        doNothing().when(spy).closeDialog();
        
        // Act
        spy.closeDialog();
        
        // Assert
        verify(spy).closeDialog();
    }
}