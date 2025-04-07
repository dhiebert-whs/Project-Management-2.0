package org.frcpm.controllers;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.MilestoneService;
import org.frcpm.viewmodels.MilestoneViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the MilestoneController class.
 * This uses a combination of real and mocked components to test the controller
 * without requiring JavaFX initialization.
 */
@ExtendWith(MockitoExtension.class)
public class MilestoneControllerTest {

    @Mock
    private MilestoneService milestoneService;

    private MilestoneController controller;
    private MilestoneViewModel viewModel;

    @BeforeEach
    public void setUp() {
        // Create a real controller
        controller = new MilestoneController();

        // Create a real ViewModel with mocked service
        viewModel = new MilestoneViewModel(milestoneService);

        // Set the ViewModel in the controller via reflection
        try {
            java.lang.reflect.Field viewModelField = MilestoneController.class.getDeclaredField("viewModel");
            viewModelField.setAccessible(true);
            viewModelField.set(controller, viewModel);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }

    /**
     * Tests initializing a new milestone.
     */
    @Test
    public void testSetNewMilestone() {
        // Create a test project
        Project project = new Project("Test Project", LocalDate.now(),
                LocalDate.now().plusWeeks(5), LocalDate.now().plusWeeks(6));
        project.setId(1L);

        // Call the method to test
        controller.setNewMilestone(project);

        // Verify properties were set correctly on the ViewModel
        assertEquals(project, viewModel.getProject());
        assertTrue(viewModel.isNewMilestone());
        assertNull(viewModel.getMilestone());
        assertFalse(viewModel.isDirty());
    }

    /**
     * Tests initializing with an existing milestone.
     */
    @Test
    public void testSetMilestone() {
        // Create a test project
        Project project = new Project("Test Project", LocalDate.now(),
                LocalDate.now().plusWeeks(5), LocalDate.now().plusWeeks(6));
        project.setId(1L);

        // Create a test milestone
        Milestone milestone = new Milestone("Test Milestone", LocalDate.now(), project);
        milestone.setId(1L);
        milestone.setDescription("Test Description");

        // Call the method to test
        controller.setMilestone(milestone);

        // Verify properties were set correctly on the ViewModel
        assertEquals(project, viewModel.getProject());
        assertFalse(viewModel.isNewMilestone());
        assertEquals(milestone, viewModel.getMilestone());
        assertEquals("Test Milestone", viewModel.getName());
        assertEquals("Test Description", viewModel.getDescription());
        assertEquals(milestone.getDate(), viewModel.getDate());
        assertFalse(viewModel.isDirty());
    }

    /**
     * Tests that validation errors in the ViewModel work as expected.
     */
    @Test
    public void testValidationErrors() {
        // Create a test project
        Project project = new Project("Test Project", LocalDate.now(),
                LocalDate.now().plusWeeks(5), LocalDate.now().plusWeeks(6));
        project.setId(1L);

        // Set up the ViewModel for a new milestone
        viewModel.initNewMilestone(project);

        // Set invalid values
        viewModel.setName(""); // Empty name should trigger validation error

        // Validate
        viewModel.validate();

        // Verify validation error occurs
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("name cannot be empty"));
    }

    /**
     * Tests that date validation works correctly.
     */
    @Test
    public void testDateValidation() {
        // Create a test project with specific dates
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 3, 1);
        LocalDate hardDeadline = LocalDate.of(2025, 4, 1);

        Project project = new Project("Test Project", startDate, endDate, hardDeadline);
        project.setId(1L);

        // Set up the ViewModel for a new milestone
        viewModel.initNewMilestone(project);
        viewModel.setName("Test Milestone"); // Set a valid name

        // Test date before project start date
        viewModel.setDate(LocalDate.of(2024, 12, 15));
        viewModel.validate();

        // Verify validation error occurs
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("before project start date"));

        // Test date after project hard deadline
        viewModel.setDate(LocalDate.of(2025, 5, 1));
        viewModel.validate();

        // Verify validation error occurs
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("after project hard deadline"));

        // Test valid date
        viewModel.setDate(LocalDate.of(2025, 2, 15));
        viewModel.validate();

        // Verify validation passes
        assertTrue(viewModel.isValid());
        assertEquals("", viewModel.getErrorMessage());
    }

    /**
     * Tests saving a new milestone.
     */
    @Test
    public void testSaveNewMilestone() {
        // Create a test project
        Project project = new Project("Test Project", LocalDate.now(),
                LocalDate.now().plusWeeks(5), LocalDate.now().plusWeeks(6));
        project.setId(1L);

        // Set up the ViewModel for a new milestone
        viewModel.initNewMilestone(project);
        viewModel.setName("Test Milestone");
        viewModel.setDescription("Test Description");
        LocalDate testDate = LocalDate.now();
        viewModel.setDate(testDate);

        // Mock the service response
        Milestone createdMilestone = new Milestone("Test Milestone", testDate, project);
        createdMilestone.setId(1L);
        createdMilestone.setDescription("Test Description");

        when(milestoneService.createMilestone(anyString(), any(LocalDate.class), anyLong(), anyString()))
                .thenReturn(createdMilestone);

        // Execute the save command
        viewModel.getSaveCommand().execute();

        // Verify createMilestone was called with the correct arguments
        verify(milestoneService).createMilestone(
                eq("Test Milestone"),
                eq(testDate),
                eq(1L),
                eq("Test Description"));

        // Verify the milestone property was updated
        assertEquals(createdMilestone, viewModel.getMilestone());

        // Verify the dirty flag was cleared
        assertFalse(viewModel.isDirty());
    }

    /**
     * Tests updating an existing milestone.
     */
    @Test
    public void testUpdateExistingMilestone() {
        // Create a test project
        Project project = new Project("Test Project", LocalDate.now(),
                LocalDate.now().plusWeeks(5), LocalDate.now().plusWeeks(6));
        project.setId(1L);

        // Create a test milestone
        Milestone milestone = new Milestone("Original Name", LocalDate.now().minusDays(1), project);
        milestone.setId(1L);
        milestone.setDescription("Original Description");

        // Set up the ViewModel for an existing milestone
        viewModel.initExistingMilestone(milestone);

        // Change properties
        LocalDate newDate = LocalDate.now();
        String newDescription = "Updated Description";

        viewModel.setName("Updated Name");
        viewModel.setDate(newDate);
        viewModel.setDescription(newDescription);

        // Mock the service responses
        Milestone updatedMilestone = new Milestone("Updated Name", LocalDate.now().minusDays(1), project);
        updatedMilestone.setId(1L);
        updatedMilestone.setDescription("Original Description");

        Milestone updatedDateMilestone = new Milestone("Updated Name", newDate, project);
        updatedDateMilestone.setId(1L);
        updatedDateMilestone.setDescription("Original Description");

        Milestone finalMilestone = new Milestone("Updated Name", newDate, project);
        finalMilestone.setId(1L);
        finalMilestone.setDescription(newDescription);

        when(milestoneService.save(any(Milestone.class))).thenReturn(updatedMilestone);
        when(milestoneService.updateMilestoneDate(anyLong(), any(LocalDate.class))).thenReturn(updatedDateMilestone);
        when(milestoneService.updateDescription(anyLong(), anyString())).thenReturn(finalMilestone);

        // Execute the save command
        viewModel.getSaveCommand().execute();

        // Verify service methods were called correctly
        verify(milestoneService).save(any(Milestone.class));
        verify(milestoneService).updateMilestoneDate(eq(1L), eq(newDate));
        verify(milestoneService).updateDescription(eq(1L), eq(newDescription));

        // Verify the milestone property was updated
        assertEquals(finalMilestone, viewModel.getMilestone());

        // Verify the dirty flag was cleared
        assertFalse(viewModel.isDirty());
    }

    /**
     * Tests command canExecute conditions.
     */
    @Test
    public void testCommandCanExecuteConditions() {
        // Create a test project
        Project project = new Project("Test Project", LocalDate.now(),
                LocalDate.now().plusWeeks(5), LocalDate.now().plusWeeks(6));
        project.setId(1L);

        // Set up the ViewModel for a new milestone
        viewModel.initNewMilestone(project);

        // Invalid state - name is empty
        viewModel.setName("");
        assertFalse(viewModel.getSaveCommand().canExecute());

        // Valid state
        viewModel.setName("Test Milestone");
        assertTrue(viewModel.getSaveCommand().canExecute());

        // Cancel command should always be executable
        assertTrue(viewModel.getCancelCommand().canExecute());
    }

    /**
     * Tests that the command gets the appropriate validation state.
     */
    @Test
    public void testCommandValidationState() {
        // Create a test project
        Project project = new Project("Test Project", LocalDate.now(),
                LocalDate.now().plusWeeks(5), LocalDate.now().plusWeeks(6));
        project.setId(1L);

        // Set up the ViewModel for a new milestone
        viewModel.initNewMilestone(project);

        // Test initial state (should be invalid with empty name)
        viewModel.setName("");
        assertFalse(viewModel.isValid());
        assertFalse(viewModel.getSaveCommand().canExecute());

        // Test valid state
        viewModel.setName("Test Name");
        assertTrue(viewModel.isValid());
        assertTrue(viewModel.getSaveCommand().canExecute());

        // Test another invalid state
        viewModel.setDate(project.getHardDeadline().plusDays(1));
        viewModel.validate();
        assertFalse(viewModel.isValid());
        assertFalse(viewModel.getSaveCommand().canExecute());
    }
}