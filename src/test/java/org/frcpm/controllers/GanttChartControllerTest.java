package org.frcpm.controllers;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.WebViewBridgeService;
import org.frcpm.viewmodels.GanttChartViewModel;
import org.frcpm.viewmodels.GanttChartViewModel.ViewMode;
import org.frcpm.viewmodels.GanttChartViewModel.FilterOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

class GanttChartControllerTest {

    private GanttChartController controller;
    
    @Mock
    private GanttChartViewModel mockViewModel;
    
    @Mock
    private WebViewBridgeService mockBridgeService;
    
    @Mock
    private DialogService mockDialogService;
    
    @Mock
    private Project mockProject;
    
    @Mock
    private Command mockRefreshCommand;
    
    @Mock
    private Command mockZoomInCommand;
    
    @Mock
    private Command mockZoomOutCommand;
    
    @Mock
    private Command mockExportCommand;
    
    @Mock
    private Command mockTodayCommand;
    
    // Mock properties for the view model
    private final StringProperty mockStatusProperty = new SimpleStringProperty();
    private final BooleanProperty mockShowMilestonesProperty = new SimpleBooleanProperty();
    private final BooleanProperty mockShowDependenciesProperty = new SimpleBooleanProperty();
    private final ObjectProperty<ViewMode> mockViewModeProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<FilterOption> mockFilterOptionProperty = new SimpleObjectProperty<>();
    private final StringProperty mockErrorProperty = new SimpleStringProperty();
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create controller 
        controller = new GanttChartController();
        
        // Set up command mocks
        when(mockViewModel.getRefreshCommand()).thenReturn(mockRefreshCommand);
        when(mockViewModel.getZoomInCommand()).thenReturn(mockZoomInCommand);
        when(mockViewModel.getZoomOutCommand()).thenReturn(mockZoomOutCommand);
        when(mockViewModel.getExportCommand()).thenReturn(mockExportCommand);
        when(mockViewModel.getTodayCommand()).thenReturn(mockTodayCommand);
        
        // Set up property mocks
        when(mockViewModel.statusMessageProperty()).thenReturn(mockStatusProperty);
        when(mockViewModel.showMilestonesProperty()).thenReturn(mockShowMilestonesProperty);
        when(mockViewModel.showDependenciesProperty()).thenReturn(mockShowDependenciesProperty);
        when(mockViewModel.viewModeProperty()).thenReturn(mockViewModeProperty);
        when(mockViewModel.filterOptionProperty()).thenReturn(mockFilterOptionProperty);
        when(mockViewModel.errorMessageProperty()).thenReturn(mockErrorProperty);
        
        // Inject dependencies using reflection since the controller doesn't have public setters for all fields
        setPrivateField(controller, "viewModel", mockViewModel);
        setPrivateField(controller, "bridgeService", mockBridgeService);
        setPrivateField(controller, "dialogService", mockDialogService);
        
        // Initialize without WebView
        controller.initializeForTesting();
        controller.setBridgeInitializedForTesting(true);
    }
    
    /**
     * Helper method to set private fields using reflection
     */
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = GanttChartController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    
    @Test
    void testHandleZoomOut() {
        // Act
        controller.handleZoomOut();
        
        // Assert
        verify(mockZoomOutCommand).execute();
    }
    
    @Test
    void testHandleRefresh() {
        // Act
        controller.handleRefresh();
        
        // Assert
        verify(mockRefreshCommand).execute();
    }
    
    @Test
    void testHandleExport() {
        // Act
        controller.handleExport();
        
        // Assert
        verify(mockExportCommand).execute();
    }
    
    @Test
    void testHandleZoomIn() {
        // Act
        controller.handleZoomIn();
        
        // Assert
        verify(mockZoomInCommand).execute();
    }
    
    @Test
    void testSetupErrorListener() {
        try {
            // First, we need to directly access the setupErrorListener method using reflection
            java.lang.reflect.Method setupErrorListenerMethod = 
                GanttChartController.class.getDeclaredMethod("setupErrorListener");
            setupErrorListenerMethod.setAccessible(true);
            
            // Now we can execute it directly
            setupErrorListenerMethod.invoke(controller);
            
            // Now we need to simulate an error condition to trigger the handler
            // To do this, we'll need access to the listener that was added to errorMessageProperty
            
            // Create a real JavaFX property that we can use to trigger the listener
            javafx.beans.property.StringProperty realErrorProperty = new javafx.beans.property.SimpleStringProperty("");
            
            // Replace the mock with our real property temporarily
            when(mockViewModel.errorMessageProperty()).thenReturn(realErrorProperty);
            
            // Re-execute the setupErrorListener method
            setupErrorListenerMethod.invoke(controller);
            
            // Now we can set the error message
            realErrorProperty.set("Test error");
            
            // Verify that the error handling occurred
            verify(mockDialogService).showErrorAlert("Error", "Test error");
            verify(mockViewModel).clearErrorMessage();
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage(), e);
        }
    }
    
    @Test
    void testShowErrorAlert() {
        // Act
        controller.showErrorAlert("Test Title", "Test Message");
        
        // Assert
        verify(mockDialogService).showErrorAlert("Test Title", "Test Message");
    }
    
    @Test
    void testSetProject() {
        // Act
        controller.setProject(mockProject);
        
        // Assert
        verify(mockViewModel).setProject(mockProject);
        verify(mockRefreshCommand).execute();
    }
}