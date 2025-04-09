// src/test/java/org/frcpm/controllers/SubsystemControllerTest.java
package org.frcpm.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import org.frcpm.binding.Command;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.SubsystemViewModel;
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
 * Tests for SubsystemController that avoid JavaFX toolkit initialization.
 * Follows the standardized MVVM pattern for testability.
 */
public class SubsystemControllerTest {

    @Spy
    private SubsystemController controller;

    @Mock
    private SubsystemViewModel mockViewModel;

    @Mock
    private DialogService mockDialogService;

    @Mock
    private Command mockSaveCommand;

    @Mock
    private Command mockAddTaskCommand;

    @Mock
    private Command mockViewTaskCommand;

    @Mock
    private Command mockLoadTasksCommand;

    @Mock
    private Subsystem mockSubsystem;

    @Mock
    private Task mockTask;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up command mocks
        when(mockViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
        when(mockViewModel.getAddTaskCommand()).thenReturn(mockAddTaskCommand);
        when(mockViewModel.getViewTaskCommand()).thenReturn(mockViewTaskCommand);
        when(mockViewModel.getLoadTasksCommand()).thenReturn(mockLoadTasksCommand);

        // Set up property mocks
        when(mockViewModel.subsystemNameProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.subsystemDescriptionProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.statusProperty()).thenReturn(new SimpleObjectProperty<>());
        when(mockViewModel.responsibleSubteamProperty()).thenReturn(new SimpleObjectProperty<>());
        when(mockViewModel.totalTasksProperty()).thenReturn(new SimpleIntegerProperty(0));
        when(mockViewModel.completedTasksProperty()).thenReturn(new SimpleIntegerProperty(0));
        when(mockViewModel.completionPercentageProperty()).thenReturn(new SimpleDoubleProperty(0));
        when(mockViewModel.errorMessageProperty()).thenReturn(new SimpleStringProperty());

        // Set up collection mocks
        ObservableList<Task> mockTasks = FXCollections.observableArrayList();
        when(mockViewModel.getTasks()).thenReturn(mockTasks);

        // Set up mock methods
        doReturn(mockTask).when(controller).getSelectedTask();
        doNothing().when(controller).closeDialog();
        doReturn(Optional.of(javafx.scene.control.ButtonType.OK)).when(controller).showAndWaitDialog(any(Stage.class));

        // Mock the dialog opening methods instead of the individual FXMLLoader calls
        doNothing().when(controller).openTaskDialogDirectly(any(Task.class));

        // Inject dependencies
        controller.setViewModel(mockViewModel);
        controller.setDialogService(mockDialogService);
    }

    @Test
    public void testInitNewSubsystem() {
        // Act
        controller.initNewSubsystem();

        // Assert
        verify(mockViewModel).initNewSubsystem();
    }

    @Test
    public void testInitExistingSubsystem() {
        // Act
        controller.initExistingSubsystem(mockSubsystem);

        // Assert
        verify(mockViewModel).initExistingSubsystem(mockSubsystem);
    }

    @Test
    public void testInitExistingSubsystem_NullSubsystem() {
        // Act
        controller.initExistingSubsystem(null);

        // Assert
        verify(mockViewModel, never()).initExistingSubsystem(any());
    }

    @Test
    public void testHandleViewTask() throws Exception {
        // Act
        controller.handleViewTask(mockTask);

        // Assert
        verify(mockLoadTasksCommand).execute();
    }

    @Test
    public void testHandleViewTask_NullTask() {
        // Act
        controller.handleViewTask(null);

        // Assert
        verify(mockLoadTasksCommand, never()).execute();
    }

    @Test
    public void testShowErrorAlert() {
        // Act
        controller.showErrorAlert("Test Title", "Test Message");

        // Assert
        verify(mockDialogService).showErrorAlert("Test Title", "Test Message");
    }

    @Test
    public void testGetSubsystem() {
        // Arrange
        when(mockViewModel.getSelectedSubsystem()).thenReturn(mockSubsystem);

        // Act
        Subsystem result = controller.getSubsystem();

        // Assert
        assertEquals(mockSubsystem, result);
    }

    @Test
    public void testGetViewModel() {
        // Act
        SubsystemViewModel result = controller.getViewModel();

        // Assert
        assertEquals(mockViewModel, result);
    }

    @Test
    public void testSetViewModel() {
        // Arrange
        SubsystemViewModel newMockViewModel = mock(SubsystemViewModel.class);

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

    @Test
    public void testErrorMessageListener() {
        // Arrange
        SimpleStringProperty errorProperty = new SimpleStringProperty();
        when(mockViewModel.errorMessageProperty()).thenReturn(errorProperty);

        // Manually set up the error listener
        controller.setupErrorListener();

        // Act - simulate error message change
        errorProperty.set("Test Error");

        // Assert
        verify(mockDialogService).showErrorAlert(eq("Error"), eq("Test Error"));
    }
}