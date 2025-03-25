package org.frcpm.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.MilestoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MilestoneControllerTest {

    @Mock
    private MilestoneService milestoneService;
    
    @InjectMocks
    private MilestoneController milestoneController;
    
    @Mock
    private TextField nameField;
    
    @Mock
    private DatePicker datePicker;
    
    @Mock
    private TextArea descriptionArea;
    
    @Mock
    private Button saveButton;
    
    @Mock
    private Button cancelButton;
    
    @Mock
    private Stage mockStage;
    
    @Mock
    private ActionEvent mockEvent;
    
    private Project testProject;
    private Milestone testMilestone;

    @BeforeEach
    public void setUp() {
        // Create test project
        testProject = new Project(
                "Test Project",
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8)
        );
        testProject.setId(1L);
        
        // Create test milestone
        testMilestone = new Milestone("Test Milestone", LocalDate.now().plusWeeks(2), testProject);
        testMilestone.setId(1L);
        testMilestone.setDescription("Test milestone description");
        
        // Initialize controller by setting the mock fields
        when(milestoneController.getNameField()).thenReturn(nameField);
        when(milestoneController.getDatePicker()).thenReturn(datePicker);
        when(milestoneController.getDescriptionArea()).thenReturn(descriptionArea);
        when(milestoneController.getSaveButton()).thenReturn(saveButton);
        when(milestoneController.getCancelButton()).thenReturn(cancelButton);
        
        // Mock service behavior
        when(milestoneService.createMilestone(anyString(), any(), anyLong(), anyString()))
            .thenReturn(testMilestone);
        when(milestoneService.updateMilestoneDate(anyLong(), any()))
            .thenReturn(testMilestone);
        when(milestoneService.updateDescription(anyLong(), anyString()))
            .thenReturn(testMilestone);
        
        // Mock UI component behavior
        when(nameField.getText()).thenReturn("Test Milestone");
        when(datePicker.getValue()).thenReturn(LocalDate.now().plusWeeks(2));
        when(descriptionArea.getText()).thenReturn("Test milestone description");
        when(saveButton.getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(saveButton.getScene().getWindow()).thenReturn(mockStage);
    }

    @Test
    public void testInitialize() {
        // Call initialize via reflection (since it's private)
        try {
            milestoneController.testInitialize();
            
            // Verify that default date is set
            verify(datePicker).setValue(any(LocalDate.class));
            
            // Verify that button actions are set
            verify(saveButton).setOnAction(any());
            verify(cancelButton).setOnAction(any());
            
        } catch (Exception e) {
            fail("Exception during initialize: " + e.getMessage());
        }
    }

    @Test
    public void testSetNewMilestone() {
        // Test setting up for a new milestone
        milestoneController.setNewMilestone(testProject);
        
        // Verify the fields are initialized correctly
        assertEquals(testProject, milestoneController.getProject());
        assertNull(milestoneController.getMilestone());
        assertTrue(milestoneController.isNewMilestone());
        verify(nameField).setText("");
        verify(datePicker).setValue(any(LocalDate.class));
        verify(descriptionArea).setText("");
    }
    
    @Test
    public void testSetMilestone() {
        // Test setting up for editing an existing milestone
        milestoneController.setMilestone(testMilestone);
        
        // Verify the fields are initialized correctly
        assertEquals(testMilestone, milestoneController.getMilestone());
        assertEquals(testProject, milestoneController.getProject());
        assertFalse(milestoneController.isNewMilestone());
        verify(nameField).setText(testMilestone.getName());
        verify(datePicker).setValue(testMilestone.getDate());
        verify(descriptionArea).setText(testMilestone.getDescription());
    }
    
    @Test
    public void testHandleSaveForNewMilestone() {
        // Set up for a new milestone
        milestoneController.setNewMilestone(testProject);
        
        // Test saving
        milestoneController.testHandleSave(mockEvent);
        
        // Verify service was called to create a new milestone
        verify(milestoneService).createMilestone(
            nameField.getText(),
            datePicker.getValue(),
            testProject.getId(),
            descriptionArea.getText()
        );
        
        // Verify dialog was closed
        verify(mockStage).close();
    }
    
    @Test
    public void testHandleSaveForExistingMilestone() {
        // Set up for editing an existing milestone
        milestoneController.setMilestone(testMilestone);
        
        // Test saving
        milestoneController.testHandleSave(mockEvent);
        
        // Verify service was called to update the milestone
        verify(milestoneService).updateMilestoneDate(
            testMilestone.getId(),
            datePicker.getValue()
        );
        verify(milestoneService).updateDescription(
            testMilestone.getId(),
            descriptionArea.getText()
        );
        
        // Verify dialog was closed
        verify(mockStage).close();
    }
    
    @Test
    public void testHandleSaveWithValidationErrors() {
        // Set up for a new milestone with invalid values
        milestoneController.setNewMilestone(testProject);
        
        // Mock empty name
        when(nameField.getText()).thenReturn("");
        
        // Test saving
        milestoneController.testHandleSave(mockEvent);
        
        // Verify service was NOT called to create a new milestone
        verify(milestoneService, never()).createMilestone(anyString(), any(), anyLong(), anyString());
        
        // Verify dialog was NOT closed
        verify(mockStage, never()).close();
    }
    
    @Test
    public void testHandleCancel() {
        // Test canceling
        milestoneController.testHandleCancel(mockEvent);
        
        // Verify dialog was closed
        verify(mockStage).close();
    }
    
    @Test
    public void testGetMilestone() {
        // Set up a milestone
        milestoneController.setMilestone(testMilestone);
        
        // Test getting the milestone
        Milestone result = milestoneController.getMilestone();
        
        // Verify the correct milestone is returned
        assertEquals(testMilestone, result);
    }
}
