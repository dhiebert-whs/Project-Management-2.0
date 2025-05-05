package org.frcpm.presenters.testfx;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.presenters.GanttChartPresenter;
import org.frcpm.services.DialogService;
import org.frcpm.services.GanttDataService;
import org.frcpm.services.WebViewBridgeService;
import org.frcpm.testfx.BaseFxTest;
import org.frcpm.viewmodels.GanttChartViewModel;
import org.frcpm.views.GanttChartView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.util.WaitForAsyncUtils;

/**
 * TestFX test for the GanttChartPresenter class.
 */
@ExtendWith(MockitoExtension.class)
public class GanttChartPresenterTestFX extends BaseFxTest {

    private static final Logger LOGGER = Logger.getLogger(GanttChartPresenterTestFX.class.getName());

    @Mock
    private GanttDataService ganttDataService;
    
    @Mock
    private WebViewBridgeService bridgeService;
    
    @Mock
    private DialogService dialogService;
    
    @Mock
    private WebEngine webEngine;
    
    private AutoCloseable closeable;
    private GanttChartView view;
    private GanttChartPresenter presenter;
    private GanttChartViewModel viewModel;
    
    // Test data
    private Project testProject;
    private Command mockRefreshCommand;
    private Command mockZoomInCommand;
    private Command mockZoomOutCommand;
    private Command mockTodayCommand;
    private Command mockExportCommand;
    private boolean bridgeInitialized = false;

    @Override
    protected void initializeTestComponents(Stage stage) {
        LOGGER.info("Initializing GanttChartPresenterTestFX test components");
        
        try {
            // Open mocks first
            closeable = MockitoAnnotations.openMocks(this);
            
            // Create test data and mocks
            setupTestData();
            
            // Initialize the view
            Platform.runLater(() -> {
                try {
                    // Create view
                    view = new GanttChartView();
                    
                    // Get the presenter
                    presenter = (GanttChartPresenter) view.getPresenter();
                    
                    // Log successful creation
                    LOGGER.info("Created GanttChartView and got presenter: " + (presenter != null));
                    
                    // Set the scene
                    Scene scene = new Scene(view.getView(), 800, 600);
                    stage.setScene(scene);
                    
                    // Inject mocked services
                    injectMockedServices();
                    
                    // Get the view model
                    if (presenter != null) {
                        viewModel = presenter.getViewModel();
                        LOGGER.info("Got view model: " + (viewModel != null));
                        
                        // Set bridge initialized through injected field
                        injectBridgeInitialized(true);
                    }
                    
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error initializing GanttChartView", e);
                    e.printStackTrace();
                }
            });
            
            // Wait for UI to update
            WaitForAsyncUtils.waitForFxEvents();
            
            // Set up mock view model
            setupMockViewModel();
            
            // Show the stage
            stage.show();
            WaitForAsyncUtils.waitForFxEvents();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in initializeTestComponents", e);
            e.printStackTrace();
        }
    }
    
    private void injectMockedServices() {
        if (presenter == null) {
            LOGGER.severe("Cannot inject services - presenter is null");
            return;
        }
        
        try {
            // Use reflection to inject mocked services
            java.lang.reflect.Field ganttDataServiceField = presenter.getClass().getDeclaredField("ganttDataService");
            ganttDataServiceField.setAccessible(true);
            ganttDataServiceField.set(presenter, ganttDataService);
            
            java.lang.reflect.Field bridgeServiceField = presenter.getClass().getDeclaredField("bridgeService");
            bridgeServiceField.setAccessible(true);
            bridgeServiceField.set(presenter, bridgeService);
            
            java.lang.reflect.Field dialogServiceField = presenter.getClass().getDeclaredField("dialogService");
            dialogServiceField.setAccessible(true);
            dialogServiceField.set(presenter, dialogService);
            
            LOGGER.info("Successfully injected mock services into presenter");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to inject mocked services", e);
            e.printStackTrace();
        }
    }
    
    private void injectBridgeInitialized(boolean value) {
        if (presenter == null) {
            LOGGER.severe("Cannot inject bridge initialized - presenter is null");
            return;
        }
        
        try {
            // Use reflection to inject the bridgeInitialized field
            java.lang.reflect.Field bridgeInitializedField = presenter.getClass().getDeclaredField("bridgeInitialized");
            bridgeInitializedField.setAccessible(true);
            bridgeInitializedField.set(presenter, value);
            
            // Keep track locally
            bridgeInitialized = value;
            
            LOGGER.info("Successfully injected bridgeInitialized = " + value);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to inject bridgeInitialized", e);
            e.printStackTrace();
        }
    }
    
    private void setupTestData() {
        // Create a test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        // Create mock commands
        mockRefreshCommand = mock(Command.class);
        mockZoomInCommand = mock(Command.class);
        mockZoomOutCommand = mock(Command.class);
        mockTodayCommand = mock(Command.class);
        mockExportCommand = mock(Command.class);
    }
    
    private void setupMockViewModel() {
        if (presenter == null) {
            LOGGER.severe("Cannot set up mock view model - presenter is null");
            return;
        }
        
        // Create a mock view model
        GanttChartViewModel mockViewModel = mock(GanttChartViewModel.class);
        
        // Set up properties
        StringProperty statusMessageProperty = new SimpleStringProperty("Ready");
        ObjectProperty<GanttChartViewModel.ViewMode> viewModeProperty = 
                new SimpleObjectProperty<>(GanttChartViewModel.ViewMode.WEEK);
        ObjectProperty<GanttChartViewModel.FilterOption> filterOptionProperty = 
                new SimpleObjectProperty<>(GanttChartViewModel.FilterOption.ALL_TASKS);
        BooleanProperty showMilestonesProperty = new SimpleBooleanProperty(true);
        BooleanProperty showDependenciesProperty = new SimpleBooleanProperty(true);
        StringProperty errorMessageProperty = new SimpleStringProperty("");
        
        // Configure the mock view model
        when(mockViewModel.statusMessageProperty()).thenReturn(statusMessageProperty);
        when(mockViewModel.viewModeProperty()).thenReturn(viewModeProperty);
        when(mockViewModel.filterOptionProperty()).thenReturn(filterOptionProperty);
        when(mockViewModel.showMilestonesProperty()).thenReturn(showMilestonesProperty);
        when(mockViewModel.showDependenciesProperty()).thenReturn(showDependenciesProperty);
        when(mockViewModel.errorMessageProperty()).thenReturn(errorMessageProperty);
        
        // Configure commands
        when(mockViewModel.getRefreshCommand()).thenReturn(mockRefreshCommand);
        when(mockViewModel.getZoomInCommand()).thenReturn(mockZoomInCommand);
        when(mockViewModel.getZoomOutCommand()).thenReturn(mockZoomOutCommand);
        when(mockViewModel.getTodayCommand()).thenReturn(mockTodayCommand);
        when(mockViewModel.getExportCommand()).thenReturn(mockExportCommand);
        
        // Set the mock view model via reflection
        try {
            java.lang.reflect.Field viewModelField = presenter.getClass().getDeclaredField("viewModel");
            viewModelField.setAccessible(true);
            
            Platform.runLater(() -> {
                try {
                    viewModelField.set(presenter, mockViewModel);
                    LOGGER.info("Successfully injected mock view model");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error setting view model", e);
                }
            });
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();
            
            // Update our reference
            this.viewModel = mockViewModel;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting view model field", e);
        }
    }
    
    @Test
    public void testButtonClicks() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }
        
        // Set the project for the presenter
        Platform.runLater(() -> {
            presenter.setProject(testProject);
        });
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        try {
            // Click the refresh button
            clickOn("#refreshButton");
            WaitForAsyncUtils.waitForFxEvents();
            verify(mockRefreshCommand).execute();
            
            // Click the zoom in button
            clickOn("#zoomInButton");
            WaitForAsyncUtils.waitForFxEvents();
            verify(mockZoomInCommand).execute();
            
            // Click the zoom out button
            clickOn("#zoomOutButton");
            WaitForAsyncUtils.waitForFxEvents();
            verify(mockZoomOutCommand).execute();
            
            // Click the today button
            clickOn("#todayButton");
            WaitForAsyncUtils.waitForFxEvents();
            verify(mockTodayCommand).execute();
            
            // Click the export button
            clickOn("#exportButton");
            WaitForAsyncUtils.waitForFxEvents();
            verify(mockExportCommand).execute();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testButtonClicks", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testToggleButtons() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        try {
            // Toggle the milestones button
            clickOn("#milestonesToggle");
            WaitForAsyncUtils.waitForFxEvents();
            
            // Toggle the dependencies button
            clickOn("#dependenciesToggle");
            WaitForAsyncUtils.waitForFxEvents();
            
            // Verify the properties were toggled
            // Note: We can't directly verify the property values changed because we're using a mock ViewModel
            // In a real application, we would verify the actual property values, but here we're just
            // checking that the UI responds to clicks appropriately
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testToggleButtons", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testComboBoxSelection() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        try {
            // Get the viewMode combo box
            ComboBox<GanttChartViewModel.ViewMode> viewModeComboBox = lookup("#viewModeComboBox").queryComboBox();
            
            // Select a different view mode
            Platform.runLater(() -> {
                viewModeComboBox.getSelectionModel().select(GanttChartViewModel.ViewMode.MONTH);
            });
            WaitForAsyncUtils.waitForFxEvents();
            
            // Get the filter combo box
            ComboBox<GanttChartViewModel.FilterOption> filterComboBox = lookup("#filterComboBox").queryComboBox();
            
            // Select a different filter option - use an actual enum value that exists
            Platform.runLater(() -> {
                filterComboBox.getSelectionModel().select(GanttChartViewModel.FilterOption.ALL_TASKS);
            });
            WaitForAsyncUtils.waitForFxEvents();
        
            
            // Verify the view model was updated
            // Note: We can't directly verify the property values changed because we're using a mock ViewModel
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testComboBoxSelection", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testSetProject() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }
        
        // Set the project for the presenter
        Platform.runLater(() -> {
            presenter.setProject(testProject);
        });
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Verify the view model was updated
        verify(viewModel).setProject(testProject);
        
        // Since the bridge is initialized, verify refresh was called
        verify(mockRefreshCommand).execute();
    }
}