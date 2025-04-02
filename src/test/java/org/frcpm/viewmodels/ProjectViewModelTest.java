package org.frcpm.viewmodels;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ProjectViewModel.
 */
public class ProjectViewModelTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private MilestoneService milestoneService;

    private ProjectViewModel viewModel;
    private Project testProject;
    private List<Project> testProjects;
    private List<Milestone> testMilestones;
    private Map<String, Object> testSummary;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test data
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");
        testProject.setStartDate(LocalDate.now());
        testProject.setGoalEndDate(LocalDate.now().plusWeeks(5));
        testProject.setHardDeadline(LocalDate.now().plusWeeks(6));

        testProjects = new ArrayList<>();
        testProjects.add(testProject);

        testMilestones = new ArrayList<>();
        Milestone milestone = new Milestone();
        milestone.setId(1L);
        milestone.setName("Test Milestone");
        milestone.setDate(LocalDate.now().plusWeeks(2));
        milestone.setProject(testProject);
        testMilestones.add(milestone);

        testSummary = new HashMap<>();
        testSummary.put("totalTasks", 10);
        testSummary.put("completedTasks", 5);
        testSummary.put("completionPercentage", 50.0);
        testSummary.put("daysUntilGoal", 35L);
        testSummary.put("daysUntilDeadline", 42L);

        // Create ViewModel
        viewModel = new ProjectViewModel(projectService, milestoneService);
    }

    @Test
    public void testInitialState() {
        // Verify initial state
        assertEquals("", viewModel.getProjectName());
        assertEquals("", viewModel.getProjectDescription());
        assertNotNull(viewModel.getStartDate());
        assertFalse(viewModel.isValid());
    }

    @Test
    public void testLoadProjects() {
        // Configure mock
        when(projectService.findAll()).thenReturn(testProjects);

        // Load projects
        viewModel.getLoadProjectsCommand().execute();

        // Verify projects are loaded
        assertEquals(1, viewModel.getProjects().size());
        assertEquals("Test Project", viewModel.getProjects().get(0).getName());
    }

    @Test
    public void testInitNewProject() {
        // Initialize new project
        viewModel.initNewProject();

        // Verify state
        assertTrue(viewModel.isNewProject());
        assertEquals("", viewModel.getProjectName());
        assertNotNull(viewModel.getStartDate());
        assertNotNull(viewModel.getGoalEndDate());
        assertNotNull(viewModel.getHardDeadline());
        assertFalse(viewModel.isValid());
    }

    @Test
    public void testInitExistingProject() {
        // Configure mocks
        when(projectService.getProjectSummary(anyLong())).thenReturn(testSummary);
        when(milestoneService.findByProject(any(Project.class))).thenReturn(testMilestones);

        // Initialize existing project
        viewModel.initExistingProject(testProject);

        // Verify state
        assertFalse(viewModel.isNewProject());
        assertEquals("Test Project", viewModel.getProjectName());
        assertEquals("Test Description", viewModel.getProjectDescription());
        assertEquals(testProject.getStartDate(), viewModel.getStartDate());
        assertEquals(testProject.getGoalEndDate(), viewModel.getGoalEndDate());
        assertEquals(testProject.getHardDeadline(), viewModel.getHardDeadline());

        // Verify summary data
        assertEquals(10, viewModel.getTotalTasks());
        assertEquals(5, viewModel.getCompletedTasks());
        assertEquals(50.0, viewModel.getCompletionPercentage());
        assertEquals(35L, viewModel.getDaysUntilGoal());
        assertEquals(42L, viewModel.getDaysUntilDeadline());

        // Verify milestones loaded
        assertEquals(1, viewModel.getMilestones().size());
        assertEquals("Test Milestone", viewModel.getMilestones().get(0).getName());
    }

    @Test
    public void testValidation_ValidProject() {
        // Set valid values
        viewModel.setProjectName("Test Project");
        viewModel.setStartDate(LocalDate.now());
        viewModel.setGoalEndDate(LocalDate.now().plusWeeks(5));
        viewModel.setHardDeadline(LocalDate.now().plusWeeks(6));

        // Check validation
        assertTrue(viewModel.isValid());
        assertNull(viewModel.getErrorMessage());
    }

    @Test
    public void testValidation_MissingName() {
        // Set values with missing name
        viewModel.setProjectName("");
        viewModel.setStartDate(LocalDate.now());
        viewModel.setGoalEndDate(LocalDate.now().plusWeeks(5));
        viewModel.setHardDeadline(LocalDate.now().plusWeeks(6));

        // Check validation
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Project name is required"));
    }

    @Test
    public void testValidation_InvalidDateOrder() {
        // Set values with goal date after deadline
        viewModel.setProjectName("Test Project");
        viewModel.setStartDate(LocalDate.now());
        viewModel.setGoalEndDate(LocalDate.now().plusWeeks(7));
        viewModel.setHardDeadline(LocalDate.now().plusWeeks(6));

        // Check validation
        assertFalse(viewModel.isValid());
        assertNotNull(viewModel.getErrorMessage());
        assertTrue(viewModel.getErrorMessage().contains("Competition date cannot be before goal end date"));
    }

    @Test
    public void testSaveCommand_NewProject() {
        // Configure mocks
        when(projectService.createProject(anyString(), any(LocalDate.class), any(LocalDate.class),
                any(LocalDate.class)))
                .thenReturn(testProject);
        when(projectService.updateProject(anyLong(), anyString(), any(LocalDate.class), any(LocalDate.class),
                any(LocalDate.class), anyString()))
                .thenReturn(testProject);

        // Set valid values for new project
        viewModel.initNewProject();
        viewModel.setProjectName("New Project");
        viewModel.setStartDate(LocalDate.now());
        viewModel.setGoalEndDate(LocalDate.now().plusWeeks(5));
        viewModel.setHardDeadline(LocalDate.now().plusWeeks(6));
        viewModel.setProjectDescription("New Description");

        // Save project
        viewModel.getSaveCommand().execute();

        // Verify service calls
        verify(projectService).createProject(eq("New Project"), any(LocalDate.class),
                any(LocalDate.class), any(LocalDate.class));
        verify(projectService).updateProject(anyLong(), anyString(), any(LocalDate.class),
                any(LocalDate.class), any(LocalDate.class), eq("New Description"));
    }

    @Test
    public void testSaveCommand_ExistingProject() {
        // Configure mocks
        when(projectService.updateProject(anyLong(), anyString(), any(LocalDate.class), any(LocalDate.class),
                any(LocalDate.class), anyString()))
                .thenReturn(testProject);
        when(projectService.getProjectSummary(anyLong())).thenReturn(testSummary);
        when(milestoneService.findByProject(any(Project.class))).thenReturn(testMilestones);

        // Set up existing project
        viewModel.initExistingProject(testProject);

        // Modify project
        viewModel.setProjectName("Updated Project");
        viewModel.setProjectDescription("Updated Description");

        // Save project
        viewModel.getSaveCommand().execute();

        // Verify service calls
        verify(projectService).updateProject(eq(1L), eq("Updated Project"), any(LocalDate.class),
                any(LocalDate.class), any(LocalDate.class), eq("Updated Description"));
    }

    @Test
    public void testDeleteCommand() {
        // Configure mocks
        doNothing().when(projectService).deleteById(anyLong());
        when(projectService.getProjectSummary(anyLong())).thenReturn(testSummary);
        when(milestoneService.findByProject(any(Project.class))).thenReturn(testMilestones);

        // Load projects
        when(projectService.findAll()).thenReturn(testProjects);
        viewModel.getLoadProjectsCommand().execute();

        // Set up existing project
        viewModel.initExistingProject(testProject);

        // Initially the delete command should be executable
        assertTrue(viewModel.getDeleteCommand().canExecute());

        // Delete project
        viewModel.getDeleteCommand().execute();

        // Verify service calls
        verify(projectService).deleteById(eq(1L));

        // After deletion, the project list should be empty
        assertEquals(0, viewModel.getProjects().size());

        // And the delete command should no longer be executable
        assertFalse(viewModel.getDeleteCommand().canExecute());
    }

    @Test
    public void testLoadMilestonesCommand() {
        // Configure mocks
        when(projectService.getProjectSummary(anyLong())).thenReturn(testSummary);
        when(milestoneService.findByProject(any(Project.class))).thenReturn(testMilestones);

        // Initially the load milestones command should not be executable
        assertFalse(viewModel.getLoadMilestonesCommand().canExecute());

        // Set up existing project
        viewModel.initExistingProject(testProject);

        // Now the load milestones command should be executable
        assertTrue(viewModel.getLoadMilestonesCommand().canExecute());

        // Clear milestones
        viewModel.getMilestones().clear();
        assertEquals(0, viewModel.getMilestones().size());

        // Load milestones
        viewModel.getLoadMilestonesCommand().execute();

        // Verify milestones are loaded
        assertEquals(1, viewModel.getMilestones().size());
        assertEquals("Test Milestone", viewModel.getMilestones().get(0).getName());

        // Verify service calls
        verify(milestoneService, times(2)).findByProject(eq(testProject));
    }
}