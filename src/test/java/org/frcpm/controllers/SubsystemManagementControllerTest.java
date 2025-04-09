// src/test/java/org/frcpm/controllers/SubsystemManagementControllerTest.java
package org.frcpm.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.frcpm.binding.Command;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.SubsystemManagementViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SubsystemManagementController that avoid JavaFX toolkit initialization.
 * Follows the standardized MVVM pattern for testability.
 */
public class SubsystemManagementControllerTest {

    @Spy
    private SubsystemManagementController controller;

    @Mock
    private SubsystemManagementViewModel mockViewModel;

    @Mock
    private DialogService mockDialogService;

    @Mock
    private Subsystem mockSubsystem;

    @Mock
    private Command mockAddCommand;

    @Mock
    private Command mockEditCommand;

    @Mock
    private Command mockDeleteCommand;

    @Mock
    private Command mockLoadCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up command mocks
        when(mockViewModel.getAddSubsystemCommand()).thenReturn(mockAddCommand);
        when(mockViewModel.getEditSubsystemCommand()).thenReturn(mockEditCommand);
        when(mockViewModel.getDeleteSubsystemCommand()).thenReturn(mockDeleteCommand);
        when(mockViewModel.getLoadSubsystemsCommand()).thenReturn(mockLoadCommand);

        // Set up observable list mock
        ObservableList<Subsystem> mockSubsystems = FXCollections.observableArrayList();
        mockSubsystems.add(mockSubsystem);
        when(mockViewModel.getSubsystems()).thenReturn(mockSubsystems);

        // Set up property mocks
        when(mockViewModel.errorMessageProperty()).thenReturn(new SimpleStringProperty());
        
        // Set up selected subsystem
        when(mockViewModel.getSelectedSubsystem()).thenReturn(mockSubsystem);

        // Mock protected methods to avoid JavaFX operations
        doReturn(mockSubsystem).when(controller).getSelectedSubsystem();
        doReturn(Optional.of(ButtonType.OK)).when(controller).showAndWaitDialog(any(Stage.class));
        
        // Inject dependencies
        controller.setViewModel(mockViewModel);
        controller.setDialogService(mockDialogService);
    }

    @Test
    public void testHandleAddSubsystem() {
        // Add more mocking to simulate MainController
        MainController mockMainController = mock(MainController.class);
        
        try {
            // Use reflection to set the static instance field
            java.lang.reflect.Field instanceField = MainController.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            Object oldValue = instanceField.get(null);
            
            try {
                // Set mock instance
                instanceField.set(null, mockMainController);
                
                // Act
                controller.handleAddSubsystem();
                
                // Assert
                verify(mockMainController).showSubsystemDialog(null);
                verify(mockLoadCommand).execute();
                
            } finally {
                // Restore original value
                instanceField.set(null, oldValue);
            }
        } catch (Exception e) {
            // If reflection fails, test using direct method call
            try {
                // Mock openSubsystemDialog method
                doNothing().when(controller).openSubsystemDialog(any());
                
                // Act
                controller.handleAddSubsystem();
                
                // Verify fallback behavior
                verify(controller).openSubsystemDialog(null);
                verify(mockLoadCommand).execute();
            } catch (Exception ex) {
                fail("Test failed: " + ex.getMessage());
            }
        }
    }

    @Test
    public void testHandleEditSubsystem() {
        // Add more mocking to simulate MainController
        MainController mockMainController = mock(MainController.class);
        
        try {
            // Use reflection to set the static instance field
            java.lang.reflect.Field instanceField = MainController.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            Object oldValue = instanceField.get(null);
            
            try {
                // Set mock instance
                instanceField.set(null, mockMainController);
                
                // Act
                controller.handleEditSubsystem();
                
                // Assert
                verify(mockMainController).showSubsystemDialog(mockSubsystem);
                verify(mockLoadCommand).execute();
                
            } finally {
                // Restore original value
                instanceField.set(null, oldValue);
            }
        } catch (Exception e) {
            // If reflection fails, test using direct method call
            try {
                // Mock openSubsystemDialog method
                doNothing().when(controller).openSubsystemDialog(any());
                
                // Act
                controller.handleEditSubsystem();
                
                // Verify fallback behavior
                verify(controller).openSubsystemDialog(mockSubsystem);
                verify(mockLoadCommand).execute();
            } catch (Exception ex) {
                fail("Test failed: " + ex.getMessage());
            }
        }
    }

    @Test
    public void testShowErrorAlert() {
        // Act
        controller.showErrorAlert("Test Title", "Test Message");

        // Assert
        verify(mockDialogService).showErrorAlert("Test Title", "Test Message");
    }

    @Test
    public void testShowInfoAlert() {
        // Act
        controller.showInfoAlert("Test Title", "Test Message");

        // Assert
        verify(mockDialogService).showInfoAlert("Test Title", "Test Message");
    }

    @Test
    public void testShowConfirmationAlert() {
        // Arrange
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(true);

        // Act
        boolean result = controller.showConfirmationAlert("Test Title", "Test Message");

        // Assert
        assertTrue(result);
        verify(mockDialogService).showConfirmationAlert("Test Title", "Test Message");
    }

    @Test
    public void testGetViewModel() {
        // Act
        SubsystemManagementViewModel result = controller.getViewModel();

        // Assert
        assertEquals(mockViewModel, result);
    }
}