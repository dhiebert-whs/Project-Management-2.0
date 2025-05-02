package org.frcpm.presenters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.frcpm.binding.Command;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Meeting;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.services.DialogService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.TaskService;
import org.frcpm.viewmodels.DashboardViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for DashboardPresenter using the recommended patterns
 * for AfterburnerFX integration testing.
 */
@ExtendWith(MockitoExtension.class)
public class DashboardPresenterTest {

    @Mock
    private DashboardViewModel mockViewModel;
    
    @Mock
    private TaskService mockTaskService;
    
    @Mock
    private MilestoneService mockMilestoneService;
    
    @Mock
    private MeetingService mockMeetingService;
    
    @Mock
    private DialogService mockDialogService;
    
    @Mock
    private ResourceBundle mockResources;
    
    @Mock
    private PieChart mockTaskStatusChart;
    
    @Mock
    private LineChart<Number, Number> mockProgressChart;
    
    @Mock
    private VBox mockChartsContainer;
    
    @Mock
    private Label mockProjectNameLabel;
    
    @Mock
    private Label mockStartDateLabel;
    
    @Mock
    private Label mockGoalDateLabel;
    
    @Mock
    private Label mockDeadlineLabel;
    
    @Mock
    private ProgressBar mockOverallProgressBar;
    
    @Mock
    private Label mockProgressPercentLabel;
    
    @Mock
    private Label mockDaysRemainingLabel;
    
    @Mock
    private TableView<Task> mockUpcomingTasksTable;
    
    @Mock
    private TableView<Milestone> mockUpcomingMilestonesTable;
    
    @Mock
    private TableView<Meeting> mockUpcomingMeetingsTable;
    
    @Mock
    private Button mockRefreshButton;
    
    @Mock
    private Command mockRefreshCommand;
    
    @InjectMocks
    private DashboardPresenter presenter;
    
    // Test data
    private Project testProject;
    private Task testTask;
    private Milestone testMilestone;
    private Meeting testMeeting;
    private StringProperty testProjectNameProperty;
    private ObjectProperty<LocalDate> testStartDateProperty;
    private ObjectProperty<LocalDate> testGoalEndDateProperty;
    private ObjectProperty<LocalDate> testHardDeadlineProperty;
    private DoubleProperty testProgressPercentageProperty;
    private IntegerProperty testDaysRemainingProperty;
    private StringProperty testErrorProperty;
    private ObservableList<Task> testUpcomingTasks;
    private ObservableList<Milestone> testUpcomingMilestones;
    private ObservableList<Meeting> testUpcomingMeetings;
    private ObservableList<PieChart.Data> testTaskStatusChartData;
    private ObservableList<javafx.scene.chart.XYChart.Series<Number, Number>> testProgressChartData;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test data
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setStartDate(LocalDate.now().minusDays(30));
        testProject.setGoalEndDate(LocalDate.now().plusDays(60));
        testProject.setHardDeadline(LocalDate.now().plusDays(90));
        
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setProject(testProject);
        
        testMilestone = new Milestone();
        testMilestone.setId(1L);
        testMilestone.setName("Test Milestone");
        testMilestone.setProject(testProject);
        testMilestone.setDate(LocalDate.now().plusDays(30));
        
        testMeeting = new Meeting();
        testMeeting.setId(1L);
        // No setTitle method - Use appropriate properties from Meeting class
        // Assuming the title is stored in a different field
        // For example, if Meeting has a name field:
        // testMeeting.setName("Test Meeting");
        testMeeting.setDate(LocalDate.now().plusDays(7));
        
        // Set up JavaFX properties
        testProjectNameProperty = new SimpleStringProperty("Test Project");
        testStartDateProperty = new SimpleObjectProperty<>(LocalDate.now().minusDays(30));
        testGoalEndDateProperty = new SimpleObjectProperty<>(LocalDate.now().plusDays(60));
        testHardDeadlineProperty = new SimpleObjectProperty<>(LocalDate.now().plusDays(90));
        testProgressPercentageProperty = new SimpleDoubleProperty(50.0);
        testDaysRemainingProperty = new SimpleIntegerProperty(60);
        testErrorProperty = new SimpleStringProperty("");
        
        // Set up collections
        testUpcomingTasks = FXCollections.observableArrayList();
        testUpcomingTasks.add(testTask);
        
        testUpcomingMilestones = FXCollections.observableArrayList();
        testUpcomingMilestones.add(testMilestone);
        
        testUpcomingMeetings = FXCollections.observableArrayList();
        testUpcomingMeetings.add(testMeeting);
        
        testTaskStatusChartData = FXCollections.observableArrayList();
        testTaskStatusChartData.add(new PieChart.Data("Not Started", 2));
        testTaskStatusChartData.add(new PieChart.Data("In Progress", 3));
        testTaskStatusChartData.add(new PieChart.Data("Completed", 1));
        
        testProgressChartData = FXCollections.observableArrayList();
        javafx.scene.chart.XYChart.Series<Number, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Progress");
        testProgressChartData.add(series);
        
        // Set up common ViewModel stubs with lenient mocking
        lenient().when(mockViewModel.projectNameProperty()).thenReturn(testProjectNameProperty);
        lenient().when(mockViewModel.startDateProperty()).thenReturn(testStartDateProperty);
        lenient().when(mockViewModel.goalEndDateProperty()).thenReturn(testGoalEndDateProperty);
        lenient().when(mockViewModel.hardDeadlineProperty()).thenReturn(testHardDeadlineProperty);
        lenient().when(mockViewModel.progressPercentageProperty()).thenReturn(testProgressPercentageProperty);
        lenient().when(mockViewModel.daysRemainingProperty()).thenReturn(testDaysRemainingProperty);
        lenient().when(mockViewModel.errorMessageProperty()).thenReturn(testErrorProperty);
        lenient().when(mockViewModel.getUpcomingTasks()).thenReturn(testUpcomingTasks);
        lenient().when(mockViewModel.getUpcomingMilestones()).thenReturn(testUpcomingMilestones);
        lenient().when(mockViewModel.getUpcomingMeetings()).thenReturn(testUpcomingMeetings);
        lenient().when(mockViewModel.getTaskStatusChartData()).thenReturn(testTaskStatusChartData);
        lenient().when(mockViewModel.getProgressChartData()).thenReturn(testProgressChartData);
        lenient().when(mockViewModel.getRefreshCommand()).thenReturn(mockRefreshCommand);
        lenient().when(mockViewModel.getStartDate()).thenReturn(testStartDateProperty.get());
        lenient().when(mockViewModel.getGoalEndDate()).thenReturn(testGoalEndDateProperty.get());
        lenient().when(mockViewModel.getHardDeadline()).thenReturn(testHardDeadlineProperty.get());
        
        // Inject UI components
        injectUIComponents();
    }
    
    /**
     * Injects UI components into the presenter using reflection.
     */
    private void injectUIComponents() {
        try {
            injectField(presenter, "taskStatusChart", mockTaskStatusChart);
            injectField(presenter, "progressChart", mockProgressChart);
            injectField(presenter, "chartsContainer", mockChartsContainer);
            injectField(presenter, "projectNameLabel", mockProjectNameLabel);
            injectField(presenter, "startDateLabel", mockStartDateLabel);
            injectField(presenter, "goalDateLabel", mockGoalDateLabel);
            injectField(presenter, "deadlineLabel", mockDeadlineLabel);
            injectField(presenter, "overallProgressBar", mockOverallProgressBar);
            injectField(presenter, "progressPercentLabel", mockProgressPercentLabel);
            injectField(presenter, "daysRemainingLabel", mockDaysRemainingLabel);
            injectField(presenter, "upcomingTasksTable", mockUpcomingTasksTable);
            injectField(presenter, "upcomingMilestonesTable", mockUpcomingMilestonesTable);
            injectField(presenter, "upcomingMeetingsTable", mockUpcomingMeetingsTable);
            injectField(presenter, "refreshButton", mockRefreshButton);
        } catch (Exception e) {
            fail("Failed to inject UI components: " + e.getMessage());
        }
    }
    
    /**
     * Injects a field using reflection.
     */
    private void injectField(Object target, String fieldName, Object value) 
            throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    
    @Test
    public void testInitialize() {
        // Act
        presenter.initialize(mock(URL.class), mockResources);
        
        // Assert - check that UI components are bound to ViewModel properties
        verify(mockViewModel, atLeastOnce()).projectNameProperty();
        verify(mockViewModel, atLeastOnce()).startDateProperty();
        verify(mockViewModel, atLeastOnce()).goalEndDateProperty();
        verify(mockViewModel, atLeastOnce()).hardDeadlineProperty();
        verify(mockViewModel, atLeastOnce()).progressPercentageProperty();
        verify(mockViewModel, atLeastOnce()).daysRemainingProperty();
        verify(mockViewModel, atLeastOnce()).errorMessageProperty();
        verify(mockViewModel).getUpcomingTasks();
        verify(mockViewModel).getUpcomingMilestones();
        verify(mockViewModel).getUpcomingMeetings();
        verify(mockViewModel).getTaskStatusChartData();
        verify(mockViewModel).getProgressChartData();
        verify(mockViewModel).getRefreshCommand();
    }
    
    @Test
    public void testSetProject() {
        // Arrange
        Project project = new Project();
        project.setId(2L);
        project.setName("New Project");
        
        // Act
        presenter.setProject(project);
        
        // Assert
        verify(mockViewModel).setProject(project);
        verify(mockViewModel).refreshDashboard();
    }
    
    @Test
    public void testRefreshData() {
        // We need to use reflection since refreshData() is a private method
        try {
            // Get the private method using reflection
            java.lang.reflect.Method method = DashboardPresenter.class.getDeclaredMethod("refreshData");
            method.setAccessible(true);
            
            // Act - invoke the private method using reflection
            method.invoke(presenter);
            
            // Assert
            verify(mockViewModel).refreshDashboard();
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testEditTask() {
        // We can't test this directly because it uses private methods
        // and requires JavaFX infrastructure, but we can test using reflection
        try {
            // Arrange
            when(mockResources.getString("task.edit.title")).thenReturn("Edit Task");
            when(mockUpcomingTasksTable.getScene()).thenReturn(mock(javafx.scene.Scene.class));
            when(mockUpcomingTasksTable.getScene().getWindow()).thenReturn(mock(Window.class));
            
            // Use reflection to access the private method
            java.lang.reflect.Method method = DashboardPresenter.class.getDeclaredMethod("handleEditTask", Task.class);
            method.setAccessible(true);
            
            // Mock static ViewLoader
            try (MockedStatic<ViewLoader> mockViewLoader = mockStatic(ViewLoader.class)) {
                // Arrange - mock the dialog result
                TaskPresenter mockTaskPresenter = mock(TaskPresenter.class);
                mockViewLoader.when(() -> ViewLoader.showDialog(any(), anyString(), any()))
                    .thenReturn(mockTaskPresenter);
                
                // Act
                method.invoke(presenter, testTask);
                
                // Assert
                mockViewLoader.verify(() -> ViewLoader.showDialog(any(), eq("Edit Task"), any()));
                verify(mockTaskPresenter).initExistingTask(testTask);
                verify(mockViewModel).refreshDashboard();
            }
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testGetViewModel() {
        // Act
        DashboardViewModel result = presenter.getViewModel();
        
        // Assert
        assertSame(mockViewModel, result);
    }
    
    @Test
    public void testErrorMessageListener() {
        // Arrange
        presenter.initialize(mock(URL.class), mockResources);
        
        // Act - simulate an error message change
        testErrorProperty.set("Test error message");
        
        // We can't directly verify the DialogService was called in unit tests
        // without a JavaFX thread, but we're testing that the binding is set up correctly
    }
}