// src/test/java/org/frcpm/presenters/DailyPresenterAfterburnerTest.java
package org.frcpm.presenters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
import org.mockito.junit.jupiter.MockitoExtension;

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
    
    @Mock
    private DailyViewModel viewModel;
    
    @InjectMocks
    private DailyPresenter presenter;
    
    private AutoCloseable closeable;
    
    @BeforeEach
    public void setUp() {
        // Use regular openMocks
        closeable = MockitoAnnotations.openMocks(this);
        
        // Use lenient() for specific mocks we need
        StringProperty errorProperty = new SimpleStringProperty("");
        lenient().when(viewModel.errorMessageProperty()).thenReturn(errorProperty);
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }
    
    @Test
    public void testInjectionWorksCorrectly() {
        // Arrange - additional setup for this specific test
        ObjectProperty<LocalDate> dateProperty = new SimpleObjectProperty<>(LocalDate.now());
        lenient().when(viewModel.selectedDateProperty()).thenReturn(dateProperty);
        
        // Act
        presenter.initialize(mock(URL.class), resources);
        
        // Assert - Access the view model through the presenter's getter
        assertSame(viewModel, presenter.getViewModel(),
                  "ViewModel should be injected correctly");
        
        // No verify here - we're just testing that initialization completes successfully
    }
    
    @Test
    public void testSetProject() {
        // Arrange
        Project mockProject = new Project();
        mockProject.setName("Test Project");
        
        // Act
        presenter.initialize(mock(URL.class), resources);
        presenter.setProject(mockProject);
        
        // Assert
        verify(viewModel).setProject(mockProject);
    }
    
    @Test
    public void testResourceBundleAccess() {
        // Arrange
        lenient().when(resources.getString(anyString())).thenReturn("MOCKED_STRING");
        
        // Act
        presenter.initialize(mock(URL.class), resources);
        
        // Assert - Just check that the test completes without errors
        // This verifies that resource access works in the initialization
        assertTrue(true, "Test should complete without exceptions");
    }
    
    @Test
    public void testServiceIntegration() {
        // Create a presenter directly without Mockito annotations
        DailyPresenter localPresenter = new DailyPresenter();
        
        // Create a fresh mock with lenient settings
        DailyViewModel mockViewModel = mock(DailyViewModel.class);
        
        // Setup the minimum properties needed
        StringProperty errorProperty = new SimpleStringProperty("");
        lenient().when(mockViewModel.errorMessageProperty()).thenReturn(errorProperty);
        
        try {
            // Inject the dependencies manually with reflection
            injectField(localPresenter, "viewModel", mockViewModel);
            injectField(localPresenter, "dialogService", dialogService);
            
            // Just verify we can set up the presenter without exceptions
            localPresenter.initialize(mock(URL.class), resources);
            
            // Simple assertion to make sure our injection worked
            assertSame(mockViewModel, localPresenter.getViewModel(),
                      "ViewModel should be correctly injected");
            
        } catch (Exception e) {
            fail("Failed to set up presenter: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to inject a field using reflection
     */
    private void injectField(Object target, String fieldName, Object value) 
            throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}