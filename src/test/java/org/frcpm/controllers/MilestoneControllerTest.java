// src/test/java/org/frcpm/controllers/MilestoneControllerTest.java
package org.frcpm.controllers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import org.frcpm.binding.Command;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.MilestoneViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for MilestoneController that avoid JavaFX toolkit initialization.
 * Follows the standardized MVVM pattern for testability.
 */
public class MilestoneControllerTest {

    @Spy
    private MilestoneController controller;

    @Mock
    private MilestoneViewModel mockViewModel;

    @Mock
    private DialogService mockDialogService;

    @Mock
    private Project mockProject;

    @Mock
    private Milestone mockMilestone;

    @Mock
    private Command mockSaveCommand;

    @Mock
    private Command mockCancelCommand;

    @Mock
    private Runnable mockCloseAction;
    
    @Mock
    private Label mockErrorLabel;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up command mocks
        when(mockViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
        when(mockViewModel.getCancelCommand()).thenReturn(mockCancelCommand);

        // Set up property mocks
        StringProperty nameProperty = new SimpleStringProperty();
        StringProperty descriptionProperty = new SimpleStringProperty();
        ObjectProperty<LocalDate> dateProperty = new SimpleObjectProperty<>(LocalDate.now());
        StringProperty errorProperty = new SimpleStringProperty();
        ObjectProperty<Project> projectProperty = new SimpleObjectProperty<>(mockProject);

        when(mockViewModel.nameProperty()).thenReturn(nameProperty);
        when(mockViewModel.descriptionProperty()).thenReturn(descriptionProperty);
        when(mockViewModel.dateProperty()).thenReturn(dateProperty);
        when(mockViewModel.errorMessageProperty()).thenReturn(errorProperty);
        when(mockViewModel.projectProperty()).thenReturn(projectProperty);
        when(mockViewModel.getProject()).thenReturn(mockProject);

        // Set project and milestone properties
        when(mockProject.getId()).thenReturn(1L);
        when(mockProject.getName()).thenReturn("Test Project");
        when(mockMilestone.getId()).thenReturn(1L);
        when(mockMilestone.getProject()).thenReturn(mockProject);
        
        // Set up mock methods
        doNothing().when(controller).closeDialog();
        
        // Inject dependencies
        controller.setViewModel(mockViewModel);
        controller.setDialogService(mockDialogService);
    }

    @Test
    public void testInitNewMilestone() {
        // Act
        controller.initNewMilestone(mockProject);

        // Assert
        verify(mockViewModel).initNewMilestone(mockProject);
    }

    @Test
    public void testInitExistingMilestone() {
        // Act
        controller.initExistingMilestone(mockMilestone);

        // Assert
        verify(mockViewModel).initExistingMilestone(mockMilestone);
    }

    @Test
    public void testInitNewMilestoneWithNullProject() {
        // Act
        controller.initNewMilestone(null);

        // Assert
        verify(mockViewModel, never()).initNewMilestone(any());
    }

    @Test
    public void testInitExistingMilestoneWithNullMilestone() {
        // Act
        controller.initExistingMilestone(null);

        // Assert
        verify(mockViewModel, never()).initExistingMilestone(any());
    }

    @Test
    public void testErrorMessageListener() {
        // Arrange
        StringProperty errorProperty = new SimpleStringProperty();
        when(mockViewModel.errorMessageProperty()).thenReturn(errorProperty);

        // Manually set up the error listener
        controller.setupErrorListener();

        // Act - simulate error message change
        errorProperty.set("Test Error");

        // Assert
        verify(controller).showErrorAlert(eq("Validation Error"), eq("Test Error"));
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
    public void testGetMilestone() {
        // Arrange
        when(mockViewModel.getMilestone()).thenReturn(mockMilestone);

        // Act
        Milestone result = controller.getMilestone();

        // Assert
        assertEquals(mockMilestone, result);
    }

    @Test
    public void testGetProject() {
        // Arrange
        when(mockViewModel.getProject()).thenReturn(mockProject);

        // Act
        Project result = controller.getProject();

        // Assert
        assertEquals(mockProject, result);
    }

    @Test
    public void testIsNewMilestone() {
        // Arrange
        when(mockViewModel.isNewMilestone()).thenReturn(true);

        // Act
        boolean result = controller.isNewMilestone();

        // Assert
        assertTrue(result);
    }

    @Test
    public void testGetViewModel() {
        // Act
        MilestoneViewModel result = controller.getViewModel();

        // Assert
        assertEquals(mockViewModel, result);
    }

    @Test
    public void testSetViewModel() {
        // Arrange
        MilestoneViewModel newMockViewModel = mock(MilestoneViewModel.class);
        when(newMockViewModel.errorMessageProperty()).thenReturn(new SimpleStringProperty());

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
    public void testCloseDialog() {
        // Set up the close action on the view model
        doAnswer(invocation -> {
            Runnable closeAction = invocation.getArgument(0);
            closeAction.run();
            return null;
        }).when(mockViewModel).setCloseDialogAction(any());
        
        // Call the setupBindings method to set the close action
        controller.setupBindings();
        
        // Assert that closeDialog was called
        verify(controller).closeDialog();
    }
    
    @Test
    public void testExceptionHandlingInSetupBindings() {
        // Set up a runtime exception when binding
        doThrow(new RuntimeException("Test exception")).when(controller).setupBindings();
        
        // Act - this should not throw an exception due to try-catch
        assertDoesNotThrow(() -> controller.setViewModel(mockViewModel));
        
        // Unfortunately we can't verify the showErrorAlert call here due to our spy setup
    }
}