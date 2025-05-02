// src/test/java/org/frcpm/presenters/DailyPresenterAfterburnerTest.java
package org.frcpm.presenters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.frcpm.di.FrcpmModule;
import org.frcpm.di.TestModule;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.services.DialogService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.TaskService;
import org.frcpm.viewmodels.DailyViewModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airhacks.afterburner.injection.Injector;

@ExtendWith(MockitoExtension.class)
public class DailyPresenterAfterburnerTest {
    
    @Mock
    private TaskService taskService;
    
    @Mock
    private MeetingService meetingService;
    
    @Mock
    private DialogService dialogService;
    
    @Mock
    private ResourceBundle resources;
    
    @Spy
    private DailyViewModel viewModel;
    
    @InjectMocks
    private DailyPresenter presenter;
    
    private AutoCloseable closeable;
    
    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        
        // Setup resource bundle mock
        when(resources.getString(anyString())).thenReturn("MOCKED_STRING");
        
        // Setup viewModel mock
        ObservableList<Task> mockTasks = FXCollections.observableArrayList();
        ObservableList<Meeting> mockMeetings = FXCollections.observableArrayList();
        when(viewModel.getTasks()).thenReturn(mockTasks);
        when(viewModel.getMeetings()).thenReturn(mockMeetings);
        when(viewModel.selectedDateProperty()).thenReturn(new SimpleObjectProperty<>(LocalDate.now()));
        
        // Use TestModule to register services
        TestModule.initialize();
        TestModule.registerMock(TaskService.class, taskService);
        TestModule.registerMock(MeetingService.class, meetingService);
        TestModule.registerMock(DialogService.class, dialogService);
        TestModule.registerMock(DailyViewModel.class, viewModel);
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        TestModule.shutdown();
        closeable.close();
    }
    
    @Test
    public void testInjectionWorksCorrectly() {
        // Arrange - in a real JavaFX environment, initialize would be called by FXMLLoader
        presenter.initialize(mock(URL.class), resources);
        
        // Assert - Access the view model through the presenter's getter
        assertSame(viewModel, presenter.getViewModel(), 
                  "ViewModel should be injected correctly");
        
        // This verifies that the presenter correctly interacts with the injected ViewModel
        verify(viewModel).selectedDateProperty();
    }
    
    @Test
    public void testSetProject() {
        // Arrange
        Project mockProject = new Project();
        mockProject.setName("Test Project");
        presenter.initialize(mock(URL.class), resources);
        
        // Act
        presenter.setProject(mockProject);
        
        // Assert
        verify(viewModel).setProject(mockProject);
    }
    
    @Test
    public void testResourceBundleAccess() {
        // Arrange - use a real resource bundle
        ResourceBundle realBundle = ResourceBundle.getBundle("org.frcpm.views.dailyview");
        
        // Act - initialize with real resources
        presenter.initialize(mock(URL.class), realBundle);
        
        // Since we can't test UI elements directly in a unit test, we can verify
        // that the resource bundle has the expected keys
        assertNotNull(realBundle.getString("daily.date"), 
                     "Resource bundle should have the expected key");
        assertEquals("Date:", realBundle.getString("daily.date"), 
                    "Resource should have the expected value");
    }
    
    @Test
    public void testServiceIntegration() {
        // Setup TestModule with real services to test integration
        Injector.forgetAll();
        FrcpmModule.initialize(); // Use real services
        
        try {
            // Create a new presenter that will use the real services
            DailyPresenter realPresenter = new DailyPresenter();
            
            // This would throw an exception if the services weren't properly registered
            // We're just verifying that the initialization doesn't throw
            realPresenter.initialize(mock(URL.class), ResourceBundle.getBundle("org.frcpm.views.dailyview"));
            
            // Get the view model that should have been injected
            DailyViewModel realViewModel = realPresenter.getViewModel();
            
            // Verify that a view model was created
            assertNotNull(realViewModel, "ViewModel should be created when using real services");
            
        } finally {
            // Clean up
            Injector.forgetAll();
            TestModule.initialize();
        }
    }
}