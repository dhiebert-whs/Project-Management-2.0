// src/test/java/org/frcpm/controllers/GanttChartControllerTest.java
package org.frcpm.controllers;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.WebViewBridgeService;
import org.frcpm.viewmodels.GanttChartViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GanttChartControllerTest {
    
    @Spy
    private GanttChartController controller;
    
    @Mock
    private GanttChartViewModel mockViewModel;
    
    @Mock
    private WebViewBridgeService mockBridgeService;
    
    @Mock
    private DialogService mockDialogService;
    
    @Mock
    private WebView mockWebView;
    
    @Mock
    private WebEngine mockWebEngine;
    
    @Mock
    private Button mockRefreshButton;
    
    @Mock
    private ComboBox<GanttChartViewModel.ViewMode> mockViewModeComboBox;
    
    @Mock
    private ComboBox<GanttChartViewModel.FilterOption> mockFilterComboBox;
    
    @Mock
    private Button mockZoomInButton;
    
    @Mock
    private Button mockZoomOutButton;
    
    @Mock
    private Button mockExportButton;
    
    @Mock
    private Button mockTodayButton;
    
    @Mock
    private ToggleButton mockMilestonesToggle;
    
    @Mock
    private ToggleButton mockDependenciesToggle;
    
    @Mock
    private Label mockStatusLabel;
    
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
    
    @Mock
    private Project mockProject;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup view model command mocks
        when(mockViewModel.getRefreshCommand()).thenReturn(mockRefreshCommand);
        when(mockViewModel.getZoomInCommand()).thenReturn(mockZoomInCommand);
        when(mockViewModel.getZoomOutCommand()).thenReturn(mockZoomOutCommand);
        when(mockViewModel.getExportCommand()).thenReturn(mockExportCommand);
        when(mockViewModel.getTodayCommand()).thenReturn(mockTodayCommand);
        
        // Setup webview mock
        doReturn(mockWebView).when(controller).getWebView();
        when(mockWebView.getEngine()).thenReturn(mockWebEngine);
        
        // Inject dependencies
        controller.setViewModel(mockViewModel);
        controller.setBridgeService(mockBridgeService);
        controller.setDialogService(mockDialogService);
        
        // Mock controller method to avoid NullPointerException
        doNothing().when(controller).setupBindings();
    }
    
    @Test
    void testSetProject() {
        // Act
        controller.setProject(mockProject);
        
        // Assert
        verify(mockViewModel).setProject(mockProject);
    }
    
    @Test
    void testHandleRefresh() {
        // Act
        controller.handleRefresh();
        
        // Assert
        verify(mockRefreshCommand).execute();
    }
    
    @Test
    void testHandleZoomIn() {
        // Act
        controller.handleZoomIn();
        
        // Assert
        verify(mockZoomInCommand).execute();
    }
    
    @Test
    void testHandleZoomOut() {
        // Act
        controller.handleZoomOut();
        
        // Assert
        verify(mockZoomOutCommand).execute();
    }
    
    @Test
    void testHandleExport() {
        // Act
        controller.handleExport();
        
        // Assert
        verify(mockExportCommand).execute();
    }
    
    @Test
    void testShowErrorAlert() {
        // Act
        controller.showErrorAlert("Test Title", "Test Message");
        
        // Assert
        verify(mockDialogService).showErrorAlert("Test Title", "Test Message");
    }
    
    @Test
    void testSetupErrorListener() {
        // Call the method
        controller.setupErrorListener();
        
        // Verify that the error listener is set up
        verify(mockViewModel).errorMessageProperty();
    }
}