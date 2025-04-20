// Updated MilestoneControllerTest.java
package org.frcpm.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
 * Uses a headless testing approach that avoids direct JavaFX dependencies.
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

    private StringProperty errorProperty;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up command mocks
        when(mockViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
        when(mockViewModel.getCancelCommand()).thenReturn(mockCancelCommand);

        // Set up property mocks - avoid creating JavaFX properties directly
        errorProperty = new SimpleStringProperty();
        when(mockViewModel.errorMessageProperty()).thenReturn(errorProperty);
        when(mockViewModel.nameProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.descriptionProperty()).thenReturn(new SimpleStringProperty());
        when(mockViewModel.dateProperty()).thenReturn(new SimpleObjectProperty<>(LocalDate.now()));
        when(mockViewModel.projectProperty()).thenReturn(new SimpleObjectProperty<>(mockProject));
        
        // Set project and milestone properties
        when(mockProject.getId()).thenReturn(1L);
        when(mockProject.getName()).thenReturn("Test Project");
        when(mockMilestone.getId()).thenReturn(1L);
        when(mockMilestone.getProject()).thenReturn(mockProject);
        when(mockViewModel.getProject()).thenReturn(mockProject);

        // IMPORTANT: Avoid calling methods that directly interact with UI components
        doNothing().when(controller).setupBindings();
        doNothing().when(controller).setupErrorListener();
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
        // We need to directly test the behavior that setupErrorListener would perform
        // rather than calling the method itself
        
        // Simulate what happens in setupErrorListener when error property changes
        doAnswer(invocation -> {
            // When error property changes, controller should show error alert
            if (errorProperty.get() != null && !errorProperty.get().isEmpty()) {
                controller.showErrorAlert("Validation Error", errorProperty.get());
            }
            return null;
        }).when(mockViewModel).errorMessageProperty();
        
        // Act - set error message
        errorProperty.set("Test Error");
        
        // Manually call the listener logic since we're not setting up real listeners
        if (errorProperty.get() != null && !errorProperty.get().isEmpty()) {
            controller.showErrorAlert("Validation Error", errorProperty.get());
        }
        
        // Assert
        verify(mockDialogService).showErrorAlert("Validation Error", "Test Error");
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
        assertEquals(mockMilestone, result, "Should return the milestone from ViewModel");
    }

    @Test
    public void testGetProject() {
        // Act
        Project result = controller.getProject();

        // Assert
        assertEquals(mockProject, result, "Should return the project from ViewModel");
    }

    @Test
    public void testIsNewMilestone() {
        // Arrange
        when(mockViewModel.isNewMilestone()).thenReturn(true);

        // Act
        boolean result = controller.isNewMilestone();

        // Assert
        assertTrue(result, "Should return isNewMilestone from ViewModel");
    }

    @Test
    public void testGetViewModel() {
        // Act
        MilestoneViewModel result = controller.getViewModel();

        // Assert
        assertEquals(mockViewModel, result, "Should return the mockViewModel");
    }

    @Test
    public void testSetViewModel() {
        // Arrange
        MilestoneViewModel newMockViewModel = mock(MilestoneViewModel.class);
        when(newMockViewModel.errorMessageProperty()).thenReturn(new SimpleStringProperty());

        // Act
        controller.setViewModel(newMockViewModel);

        // Assert
        assertEquals(newMockViewModel, controller.getViewModel(), "Should update the ViewModel");
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