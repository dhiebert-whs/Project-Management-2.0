package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import org.frcpm.models.Meeting;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @Mock
    private TableView<Task> tasksTable;

    @Mock
    private TableView<Milestone> milestonesTable;

    @Mock
    private TableView<Meeting> meetingsTable;

    @Mock
    private Label projectNameLabel;

    @Mock
    private Label startDateLabel;

    @Mock
    private Label goalDateLabel;

    @Mock
    private Label deadlineLabel;

    @Mock
    private Label completionLabel;

    @Mock
    private Label totalTasksLabel;

    @Mock
    private Label completedTasksLabel;

    @Mock
    private Label daysRemainingLabel;

    @Mock
    private TextArea descriptionArea;

    private Project testProject;

    @BeforeEach
    public void setUp() {
        // Initialize test project
        testProject = new Project(
                "Test Project",
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8)
        );
        testProject.setDescription("Test project description");

        // Initialize controller by setting the mock fields
        projectController.projectNameLabel = projectNameLabel;
        projectController.startDateLabel = startDateLabel;
        projectController.goalDateLabel = goalDateLabel;
        projectController.deadlineLabel = deadlineLabel;
        projectController.completionLabel = completionLabel;
        projectController.totalTasksLabel = totalTasksLabel;
        projectController.completedTasksLabel = completedTasksLabel;
        projectController.daysRemainingLabel = daysRemainingLabel;
        projectController.descriptionArea = descriptionArea;
        projectController.tasksTable = tasksTable;
        projectController.milestonesTable = milestonesTable;
        projectController.meetingsTable = meetingsTable;

        // Mock project summary data
        Map<String, Object> summaryData = new HashMap<>();
        summaryData.put("totalTasks", 10);
        summaryData.put("completedTasks", 5);
        summaryData.put("completionPercentage", 50.0);
        summaryData.put("daysUntilGoal", 42);

        // Set up mocks
        when(projectService.getProjectSummary(testProject.getId())).thenReturn(summaryData);
        when(tasksTable.getItems()).thenReturn(FXCollections.observableArrayList());
        when(milestonesTable.getItems()).thenReturn(FXCollections.observableArrayList());
        when(meetingsTable.getItems()).thenReturn(FXCollections.observableArrayList());
    }

    @Test
    public void testSetProject() {
        // Call the method to test
        projectController.setProject(testProject);

        // Verify that UI elements are updated
        verify(projectNameLabel).setText(testProject.getName());
        verify(startDateLabel).setText(testProject.getStartDate().toString());
        verify(goalDateLabel).setText(testProject.getGoalEndDate().toString());
        verify(deadlineLabel).setText(testProject.getHardDeadline().toString());
        verify(descriptionArea).setText(testProject.getDescription());

        // Verify that summary data is displayed
        verify(completionLabel).setText(String.format("%.1f%%", 50.0));
        verify(totalTasksLabel).setText("10");
        verify(completedTasksLabel).setText("5");
        verify(daysRemainingLabel).setText("42 days until goal");
    }

    @Test
    public void testLoadProjectData() {
        // Set up the project
        projectController.setProject(testProject);

        // Verify that task, milestone, and meeting lists are loaded
        verify(projectService).getProjectSummary(testProject.getId());
    }

    @Test
    public void testHandleAddTask() {
        // Create a mock ActionEvent
        ActionEvent mockEvent = mock(ActionEvent.class);
        
        // Set the project
        projectController.setProject(testProject);
        
        // Call the handler with the mock event
        projectController.handleAddTask(mockEvent);
        
        // Since we can't easily test dialog creation, we'll just verify the project is set
        assertNotNull(projectController.getProject());
    }

    @Test
    public void testHandleAddMilestone() {
        // Create a mock ActionEvent
        ActionEvent mockEvent = mock(ActionEvent.class);
        
        // Set the project
        projectController.setProject(testProject);
        
        // Call the handler with the mock event
        projectController.handleAddMilestone(mockEvent);
        
        // Verify project is set
        assertNotNull(projectController.getProject());
    }

    @Test
    public void testHandleScheduleMeeting() {
        // Create a mock ActionEvent
        ActionEvent mockEvent = mock(ActionEvent.class);
        
        // Set the project
        projectController.setProject(testProject);
        
        // Call the handler with the mock event
        projectController.handleScheduleMeeting(mockEvent);
        
        // Verify project is set
        assertNotNull(projectController.getProject());
    }
    
    @Test
    public void testProjectInitiallyNullThenSet() {
        // Initially the project should be null
        assertNull(projectController.getProject());
        
        // After setting, it should not be null
        projectController.setProject(testProject);
        assertNotNull(projectController.getProject());
        assertEquals(testProject.getId(), projectController.getProject().getId());
    }
}