package org.frcpm.controllers;

import javafx.scene.control.*;
import org.frcpm.binding.Command;
import org.frcpm.models.*;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.TaskViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDate;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskControllerTest {

    @Spy
    private TaskController controller;

    @Mock
    private TaskViewModel mockViewModel;

    @Mock
    private DialogService mockDialogService;

    @Mock
    private Project mockProject;

    @Mock
    private Subsystem mockSubsystem;

    @Mock
    private Task mockTask;

    @Mock
    private Command mockSaveCommand;
    
    @Mock
    private Command mockCancelCommand;
    
    @Mock
    private Command mockAddMemberCommand;
    
    @Mock
    private Command mockRemoveMemberCommand;
    
    @Mock
    private Command mockAddDependencyCommand;
    
    @Mock
    private Command mockRemoveDependencyCommand;
    
    @Mock
    private Command mockAddComponentCommand;
    
    @Mock
    private Command mockRemoveComponentCommand;

    @Mock
    private Alert mockAlert;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up mock task
        when(mockTask.getTitle()).thenReturn("Test Task");
        when(mockTask.getDescription()).thenReturn("Test Description");
        when(mockTask.getProject()).thenReturn(mockProject);
        when(mockTask.getSubsystem()).thenReturn(mockSubsystem);
        when(mockTask.getId()).thenReturn(1L);

        // Set up ViewModel mocks
        when(mockViewModel.titleProperty()).thenReturn(new SimpleStringProperty("Test Task"));
        when(mockViewModel.descriptionProperty()).thenReturn(new SimpleStringProperty("Test Description"));
        when(mockViewModel.estimatedHoursProperty()).thenReturn(new SimpleDoubleProperty(2.0));
        when(mockViewModel.actualHoursProperty()).thenReturn(new SimpleDoubleProperty(1.0));
        when(mockViewModel.priorityProperty()).thenReturn(new SimpleObjectProperty<>(Task.Priority.HIGH));
        when(mockViewModel.progressProperty()).thenReturn(new SimpleIntegerProperty(50));
        when(mockViewModel.startDateProperty()).thenReturn(new SimpleObjectProperty<>(LocalDate.now()));
        when(mockViewModel.endDateProperty()).thenReturn(new SimpleObjectProperty<>(LocalDate.now().plusDays(3)));
        when(mockViewModel.completedProperty()).thenReturn(new SimpleBooleanProperty(false));
        when(mockViewModel.projectProperty()).thenReturn(new SimpleObjectProperty<>(mockProject));
        when(mockViewModel.subsystemProperty()).thenReturn(new SimpleObjectProperty<>(mockSubsystem));
        when(mockViewModel.errorMessageProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.getTask()).thenReturn(mockTask);
        when(mockViewModel.isValid()).thenReturn(true);
        when(mockViewModel.getAssignedMembers()).thenReturn(FXCollections.observableArrayList());
        when(mockViewModel.getRequiredComponents()).thenReturn(FXCollections.observableArrayList());
        when(mockViewModel.getPreDependencies()).thenReturn(FXCollections.observableArrayList());

        // Set up command mocks
        when(mockViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
        when(mockViewModel.getCancelCommand()).thenReturn(mockCancelCommand);
        when(mockViewModel.getAddMemberCommand()).thenReturn(mockAddMemberCommand);
        when(mockViewModel.getRemoveMemberCommand()).thenReturn(mockRemoveMemberCommand);
        when(mockViewModel.getAddComponentCommand()).thenReturn(mockAddComponentCommand);
        when(mockViewModel.getRemoveComponentCommand()).thenReturn(mockRemoveComponentCommand);
        when(mockViewModel.getAddDependencyCommand()).thenReturn(mockAddDependencyCommand);
        when(mockViewModel.getRemoveDependencyCommand()).thenReturn(mockRemoveDependencyCommand);

        // Set the mock ViewModel and DialogService
        controller.setViewModel(mockViewModel);
        controller.setDialogService(mockDialogService);
        
        // Mock alert creation
        doReturn(mockAlert).when(controller).createAlert(any());
    }

    @Test
    public void testInitNewTask() {
        // Act
        controller.initNewTask(mockTask);

        // Assert
        verify(mockViewModel).initNewTask(mockProject, mockSubsystem);
        verify(mockViewModel).titleProperty();
    }

    @Test
    public void testInitExistingTask() {
        // Act
        controller.initExistingTask(mockTask);

        // Assert
        verify(mockViewModel).initExistingTask(mockTask);
    }

    @Test
    public void testGetViewModel() {
        // Act
        TaskViewModel result = controller.getViewModel();

        // Assert
        assertEquals(mockViewModel, result);
    }

    @Test
    public void testGetTask() {
        // Act
        Task result = controller.getTask();

        // Assert
        assertEquals(mockTask, result);
        verify(mockViewModel).getTask();
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
    
    @Test
    public void testErrorMessageListener() {
        // Arrange
        StringProperty errorProperty = new SimpleStringProperty();
        when(mockViewModel.errorMessageProperty()).thenReturn(errorProperty);
        
        // Act - simulate error message change
        errorProperty.set("Test Error");
        
        // Assert - test the listener would be triggered
        verify(controller).showErrorAlert(anyString(), eq("Test Error"));
    }

    @Test
    public void testSetTask() {
        // Act
        controller.setTask(mockTask);

        // Assert
        verify(mockViewModel).initExistingTask(mockTask);
    }

    @Test
    public void testSetNewTask() {
        // Arrange
        when(mockSubsystem.getName()).thenReturn("Test Subsystem");
        doNothing().when(controller).initNewTask(any(Task.class));

        // Act
        controller.setNewTask(mockProject, mockSubsystem);

        // Assert
        verify(controller).initNewTask(any(Task.class));
    }
    
    @Test
    public void testSetViewModel() {
        // Arrange
        TaskViewModel newViewModel = mock(TaskViewModel.class);
        
        // Act
        controller.setViewModel(newViewModel);
        
        // Assert
        assertEquals(newViewModel, controller.getViewModel());
    }
    
    @Test
    public void testSetDialogService() {
        // Arrange
        DialogService newDialogService = mock(DialogService.class);
        
        // Act
        controller.setDialogService(newDialogService);
        
        // Assert - would need reflection to verify this, but we're just testing no exceptions
        assertNotNull(controller);
    }
}