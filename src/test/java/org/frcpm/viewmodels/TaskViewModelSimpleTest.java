// src/test/java/org/frcpm/viewmodels/TaskViewModelSimpleTest.java
package org.frcpm.viewmodels;

import org.frcpm.models.*;
import org.frcpm.services.ComponentService;
import org.frcpm.services.TaskService;
import org.frcpm.services.TeamMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TaskViewModelSimpleTest {

    @Mock
    private TaskService taskService;

    @Mock
    private TeamMemberService teamMemberService;

    @Mock
    private ComponentService componentService;

    private TaskViewModel viewModel;
    private Project testProject;
    private Subsystem testSubsystem;
    private Subteam testSubteam;
    private Task testTask;
    private TeamMember testMember;
    private Component testComponent;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test data
        testProject = new Project(
                "Test Project",
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8));
        testProject.setId(1L);

        testSubteam = new Subteam("Test Subteam", "#FF0000");
        testSubteam.setId(1L);

        testSubsystem = new Subsystem("Test Subsystem");
        testSubsystem.setId(1L);
        testSubsystem.setResponsibleSubteam(testSubteam);

        testTask = new Task("Test Task", testProject, testSubsystem);
        testTask.setId(1L);
        testTask.setDescription("Test task description");
        testTask.setEstimatedDuration(Duration.ofHours(2));
        testTask.setPriority(Task.Priority.HIGH);
        testTask.setStartDate(LocalDate.now());
        testTask.setEndDate(LocalDate.now().plusDays(3));

        testMember = new TeamMember("testuser", "Test", "User", "test@example.com");
        testMember.setId(1L);
        testMember.setSubteam(testSubteam);

        testComponent = new Component("Test Component", "TC-001");
        testComponent.setId(1L);

        // Set up mock service
        when(taskService.createTask(anyString(), any(), any(), anyDouble(), any(), any(), any()))
                .thenReturn(testTask);
        when(taskService.save(any(Task.class))).thenReturn(testTask);
        when(taskService.updateTaskProgress(anyLong(), anyInt(), anyBoolean()))
                .thenReturn(testTask);
        when(taskService.assignMembers(anyLong(), anySet()))
                .thenReturn(testTask);
        when(taskService.addDependency(anyLong(), anyLong()))
                .thenReturn(true);

        // Create ViewModel with mocked services
        viewModel = new TaskViewModel(taskService, teamMemberService, componentService);
    }

    @Test
    public void testInitNewTask() {
        // Init for new task
        viewModel.initNewTask(testProject, testSubsystem);

        // Verify state
        assertEquals(testProject, viewModel.getProject());
        assertEquals(testSubsystem, viewModel.getSubsystem());
        assertNull(viewModel.getTask());
        assertTrue(viewModel.isNewTask());

        // Verify default values - title should be empty
        assertEquals("", viewModel.getTitle());
        assertEquals("", viewModel.getDescription());
        assertEquals(1.0, viewModel.getEstimatedHours());
        assertEquals(0.0, viewModel.getActualHours());
        assertEquals(Task.Priority.MEDIUM, viewModel.getPriority());
        assertEquals(0, viewModel.getProgress());
        assertEquals(LocalDate.now(), viewModel.getStartDate());
        assertNull(viewModel.getEndDate());
        assertFalse(viewModel.isCompleted());

        // Verify collections are empty
        assertTrue(viewModel.getAssignedMembers().isEmpty());
        assertTrue(viewModel.getPreDependencies().isEmpty());
        assertTrue(viewModel.getRequiredComponents().isEmpty());

        // Verify validation state - should be invalid due to empty title
        assertFalse(viewModel.isValid());
        assertTrue(viewModel.getErrorMessage().contains("Task title cannot be empty"));
    }

    @Test
    public void testInitExistingTask() {
        // Add test data to task
        testTask.assignMember(testMember);
        testTask.addRequiredComponent(testComponent);

        // Init for existing task
        viewModel.initExistingTask(testTask);

        // Verify state
        assertEquals(testTask, viewModel.getTask());
        assertEquals(testProject, viewModel.getProject());
        assertEquals(testSubsystem, viewModel.getSubsystem());
        assertFalse(viewModel.isNewTask());

        // Verify field values
        assertEquals(testTask.getTitle(), viewModel.getTitle());
        assertEquals(testTask.getDescription(), viewModel.getDescription());
        assertEquals(2.0, viewModel.getEstimatedHours()); // 2 hours from Duration
        assertEquals(Task.Priority.HIGH, viewModel.getPriority());
        assertEquals(testTask.getProgress(), viewModel.getProgress());
        assertEquals(testTask.getStartDate(), viewModel.getStartDate());
        assertEquals(testTask.getEndDate(), viewModel.getEndDate());
        assertEquals(testTask.isCompleted(), viewModel.isCompleted());

        // Verify collections
        assertEquals(1, viewModel.getAssignedMembers().size());
        assertTrue(viewModel.getAssignedMembers().contains(testMember));
        assertEquals(1, viewModel.getRequiredComponents().size());
        assertTrue(viewModel.getRequiredComponents().contains(testComponent));

        // Verify validation state
        assertTrue(viewModel.isValid());
    }

    @Test
    public void testAddingTitle_ValidationPasses() {
        // Set up with invalid state
        viewModel.initNewTask(testProject, testSubsystem);
        assertFalse(viewModel.isValid());

        // Add title
        viewModel.setTitle("New Task Title");

        // Verify validation passes
        assertTrue(viewModel.isValid());
    }

    @Test
    public void testMissingStartDate_ValidationFails() {
        // Set up with valid task
        viewModel.initNewTask(testProject, testSubsystem);
        viewModel.setTitle("New Task Title");
        assertTrue(viewModel.isValid());

        // Set missing start date
        viewModel.setStartDate(null);

        // Verify validation fails
        assertFalse(viewModel.isValid());
        assertTrue(viewModel.getErrorMessage().contains("Start date cannot be empty"));
    }

    @Test
    public void testEndDateBeforeStartDate_ValidationFails() {
        // Set up with valid task
        viewModel.initNewTask(testProject, testSubsystem);
        viewModel.setTitle("New Task Title");

        // Set invalid dates
        viewModel.setStartDate(LocalDate.now());
        viewModel.setEndDate(LocalDate.now().minusDays(1));

        // Verify validation fails
        assertFalse(viewModel.isValid());
        assertTrue(viewModel.getErrorMessage().contains("End date cannot be before start date"));
    }

    @Test
    public void testAddMemberToTask() {
        // Set up
        viewModel.initNewTask(testProject, testSubsystem);

        // Add member
        viewModel.addMember(testMember);

        // Verify member was added
        assertEquals(1, viewModel.getAssignedMembers().size());
        assertTrue(viewModel.getAssignedMembers().contains(testMember));
    }

    @Test
    public void testRemoveMemberFromTask() {
        // Set up
        viewModel.initNewTask(testProject, testSubsystem);
        viewModel.addMember(testMember);
        assertEquals(1, viewModel.getAssignedMembers().size());

        // Remove member
        viewModel.removeMember(testMember);

        // Verify member was removed
        assertTrue(viewModel.getAssignedMembers().isEmpty());
    }

    @Test
    public void testAddComponentToTask() {
        // Set up
        viewModel.initNewTask(testProject, testSubsystem);

        // Add component
        viewModel.addComponent(testComponent);

        // Verify component was added
        assertEquals(1, viewModel.getRequiredComponents().size());
        assertTrue(viewModel.getRequiredComponents().contains(testComponent));
    }

    @Test
    public void testProgressAndCompletionSync() {
        // Set up
        viewModel.initNewTask(testProject, testSubsystem);
        viewModel.setTitle("Test Task");

        // Set progress to 100%
        viewModel.setProgress(100);

        // Verify completion is also set to true
        assertTrue(viewModel.isCompleted());
        assertEquals(100, viewModel.getProgress());

        // Set completion to false and progress to 50%
        viewModel.setCompleted(false);
        viewModel.setProgress(50);

        // Verify values
        assertFalse(viewModel.isCompleted());
        assertEquals(50, viewModel.getProgress());

        // Set completion to true
        viewModel.setCompleted(true);

        // Verify progress is set to 100%
        assertTrue(viewModel.isCompleted());
        assertEquals(100, viewModel.getProgress());
    }

    @Test
    public void testNegativeEstimatedHours_ValidationFails() {
        // Set up with valid task
        viewModel.initNewTask(testProject, testSubsystem);
        viewModel.setTitle("New Task Title");

        // Set negative estimated hours
        viewModel.setEstimatedHours(-1.0);

        // Verify validation fails
        assertFalse(viewModel.isValid());
        assertTrue(viewModel.getErrorMessage().contains("Estimated hours must be positive"));
    }
}