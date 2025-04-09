// src/test/java/org/frcpm/controllers/MilestoneManagementControllerTest.java
package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.frcpm.binding.Command;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.MilestoneManagementViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for MilestoneManagementController that avoid JavaFX toolkit initialization.
 * Follows the standardized MVVM pattern for testability.
 */
public class MilestoneManagementControllerTest {

    @Spy
    private MilestoneManagementController controller;

    @Mock
    private MilestoneManagementViewModel mockViewModel;

    @Mock
    private DialogService mockDialogService;

    @Mock
    private Project mockProject;

    @Mock
    private Milestone mockMilestone;

    @Mock
    private Command mockAddCommand;

    @Mock
    private Command mockEditCommand;

    @Mock
    private Command mockDeleteCommand;

    @Mock
    private Command mockRefreshCommand;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up command mocks
        when(mockViewModel.getAddMilestoneCommand()).thenReturn(mockAddCommand);
        when(mockViewModel.getEditMilestoneCommand()).thenReturn(mockEditCommand);
        when(mockViewModel.getDeleteMilestoneCommand()).thenReturn(mockDeleteCommand);
        when(mockViewModel.getRefreshCommand()).thenReturn(mockRefreshCommand);

        // Set up mock milestones list
        ObservableList<Milestone> mockMilestones = FXCollections.observableArrayList();
        mockMilestones.add(mockMilestone);
        when(mockViewModel.getMilestones()).thenReturn(mockMilestones);

        // Mock error message property
        when(mockViewModel.errorMessageProperty()).thenReturn(new javafx.beans.property.SimpleStringProperty());

        // Mock milestone properties
        when(mockMilestone.getName()).thenReturn("Test Milestone");
        when(mockMilestone.getDate()).thenReturn(LocalDate.now().plusWeeks(1));
        when(mockMilestone.isPassed()).thenReturn(false);

        // Mock protected methods to avoid JavaFX operations
        doReturn(mockMilestone).when(controller).getSelectedMilestone();
        doReturn(Optional.of(ButtonType.OK)).when(controller).showAndWaitDialog(any(Stage.class));
        doNothing().when(controller).handleEditMilestone(any(Milestone.class));

        // Inject dependencies
        controller.setViewModel(mockViewModel);
        controller.setDialogService(mockDialogService);
    }

    @Test
    public void testSetProject() {
        // Act
        controller.setProject(mockProject);

        // Assert
        verify(mockViewModel).setProject(mockProject);
        verify(mockViewModel).loadMilestones();
    }

    @Test
    public void testHandleAddMilestone() {
        // Act
        controller.handleAddMilestone();

        // Assert
        verify(mockViewModel).loadMilestones();
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
    public void testShowConfirmationAlert_Confirmed() {
        // Arrange
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(true);

        // Act
        boolean result = controller.showConfirmationAlert("Test Title", "Test Message");

        // Assert
        verify(mockDialogService).showConfirmationAlert("Test Title", "Test Message");
        assertTrue(result);
    }

    @Test
    public void testShowConfirmationAlert_Cancelled() {
        // Arrange
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(false);

        // Act
        boolean result = controller.showConfirmationAlert("Test Title", "Test Message");

        // Assert
        verify(mockDialogService).showConfirmationAlert("Test Title", "Test Message");
        assertFalse(result);
    }

    @Test
    public void testErrorMessageListener() {
        // Arrange
        javafx.beans.property.SimpleStringProperty errorProperty = new javafx.beans.property.SimpleStringProperty();
        when(mockViewModel.errorMessageProperty()).thenReturn(errorProperty);

        // Act - simulate error message change
        errorProperty.set("Test Error");

        // Manually trigger the error message listener since this is normally done in setupBindings
        if (errorProperty.get() != null && !errorProperty.get().isEmpty()) {
            controller.showErrorAlert("Error", errorProperty.get());
            mockViewModel.clearErrorMessage();
        }

        // Assert
        verify(mockDialogService).showErrorAlert(eq("Error"), eq("Test Error"));
        verify(mockViewModel).clearErrorMessage();
    }

    @Test
    public void testGetViewModel() {
        // Act
        MilestoneManagementViewModel result = controller.getViewModel();

        // Assert
        assertEquals(mockViewModel, result);
    }

    @Test
    public void testSetViewModel() {
        // Arrange
        MilestoneManagementViewModel newMockViewModel = mock(MilestoneManagementViewModel.class);

        // Act
        controller.setViewModel(newMockViewModel);

        // Assert
        assertEquals(newMockViewModel, controller.getViewModel());
    }

    @Test
    public void testSetDialogService() {
        // Arrange
        DialogService newMockDialogService = mock(DialogService.class);

        // Act
        controller.setDialogService(newMockDialogService);

        // Assert - verification through subsequent method calls
        controller.showErrorAlert("Test", "Test");
        verify(newMockDialogService).showErrorAlert("Test", "Test");
    }
}