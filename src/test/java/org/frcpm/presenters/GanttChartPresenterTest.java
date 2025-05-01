package org.frcpm.presenters;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.web.WebView;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.GanttDataService;
import org.frcpm.services.WebViewBridgeService;
import org.frcpm.viewmodels.GanttChartViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ResourceBundle;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GanttChartPresenterTest {

    @Mock
    private GanttDataService ganttDataService;
    
    @Mock
    private WebViewBridgeService bridgeService;
    
    @Mock
    private DialogService dialogService;
    
    @Mock
    private WebView webView;
    
    @Mock
    private Button refreshButton;
    
    @Mock
    private ComboBox<GanttChartViewModel.ViewMode> viewModeComboBox;
    
    @Mock
    private ComboBox<GanttChartViewModel.FilterOption> filterComboBox;
    
    @Mock
    private Button zoomInButton;
    
    @Mock
    private Button zoomOutButton;
    
    @Mock
    private Button exportButton;
    
    @Mock
    private Button todayButton;
    
    @Mock
    private ToggleButton milestonesToggle;
    
    @Mock
    private ToggleButton dependenciesToggle;
    
    @Mock
    private Label statusLabel;
    
    @Mock
    private ResourceBundle resources;
    
    @Mock
    private GanttChartViewModel viewModel;
    
    @Mock
    private Project project;
    
    @Spy
    @InjectMocks
    private GanttChartPresenter presenter;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Set up resource bundle
        when(resources.getString(anyString())).thenReturn("test");
        
        // Set up UI fields via reflection
        injectMocks();
        
        // Set up view model
        doReturn(viewModel).when(presenter).getViewModel();
        
        // Set up commands
        when(viewModel.getRefreshCommand()).thenReturn(mock(org.frcpm.binding.Command.class));
        when(viewModel.getZoomInCommand()).thenReturn(mock(org.frcpm.binding.Command.class));
        when(viewModel.getZoomOutCommand()).thenReturn(mock(org.frcpm.binding.Command.class));
        when(viewModel.getExportCommand()).thenReturn(mock(org.frcpm.binding.Command.class));
        when(viewModel.getTodayCommand()).thenReturn(mock(org.frcpm.binding.Command.class));
        
        // Set up properties
        when(viewModel.viewModeProperty()).thenReturn(mock(javafx.beans.property.ObjectProperty.class));
        when(viewModel.filterOptionProperty()).thenReturn(mock(javafx.beans.property.ObjectProperty.class));
        when(viewModel.statusMessageProperty()).thenReturn(mock(javafx.beans.property.StringProperty.class));
        when(viewModel.showMilestonesProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(viewModel.showDependenciesProperty()).thenReturn(mock(javafx.beans.property.BooleanProperty.class));
        when(viewModel.errorMessageProperty()).thenReturn(mock(javafx.beans.property.StringProperty.class));
    }
    
    /**
     * Injects mocked fields into the presenter
     */
    private void injectMocks() throws Exception {
        setField(presenter, "webView", webView);
        setField(presenter, "refreshButton", refreshButton);
        setField(presenter, "viewModeComboBox", viewModeComboBox);
        setField(presenter, "filterComboBox", filterComboBox);
        setField(presenter, "zoomInButton", zoomInButton);
        setField(presenter, "zoomOutButton", zoomOutButton);
        setField(presenter, "exportButton", exportButton);
        setField(presenter, "todayButton", todayButton);
        setField(presenter, "milestonesToggle", milestonesToggle);
        setField(presenter, "dependenciesToggle", dependenciesToggle);
        setField(presenter, "statusLabel", statusLabel);
    }
    
    /**
     * Sets a field value via reflection
     */
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    
    @Test
    public void testInitialize() {
        // Call initialize
        presenter.initialize(mock(URL.class), resources);
        
        // Initialize for testing to avoid WebView issues
        presenter.initializeForTesting();
        
        // Verify viewModel is created
        verify(presenter).getViewModel();
    }
    
    @Test
    public void testHandleRefresh() {
        // Set up
        presenter.initializeForTesting();
        
        // Execute
        presenter.handleRefresh();
        
        // Verify
        verify(viewModel.getRefreshCommand()).execute();
    }
    
    @Test
    public void testHandleZoomIn() {
        // Set up
        presenter.initializeForTesting();
        
        // Execute
        presenter.handleZoomIn();
        
        // Verify
        verify(viewModel.getZoomInCommand()).execute();
    }
    
    @Test
    public void testHandleZoomOut() {
        // Set up
        presenter.initializeForTesting();
        
        // Execute
        presenter.handleZoomOut();
        
        // Verify
        verify(viewModel.getZoomOutCommand()).execute();
    }
    
    @Test
    public void testHandleToday() {
        // Set up
        presenter.initializeForTesting();
        
        // Execute
        presenter.handleToday();
        
        // Verify
        verify(viewModel.getTodayCommand()).execute();
    }
    
    @Test
    public void testHandleExport() {
        // Set up
        presenter.initializeForTesting();
        
        // Execute
        presenter.handleExport();
        
        // Verify
        verify(viewModel.getExportCommand()).execute();
    }
    
    @Test
    public void testSetProject() {
        // Set up
        presenter.initializeForTesting();
        
        // Execute
        presenter.setProject(project);
        
        // Verify
        verify(viewModel).setProject(project);
        
        // Test with bridge initialized
        presenter.setBridgeInitializedForTesting(true);
        presenter.setProject(project);
        verify(viewModel, times(2)).setProject(project);
        verify(viewModel.getRefreshCommand()).execute();
    }
    
    @Test
    public void testSetProjectWithNull() {
        // Set up
        presenter.initializeForTesting();
        
        // Execute
        presenter.setProject(null);
        
        // Verify
        verify(viewModel, never()).setProject(any());
    }
    
    @Test
    public void testCleanup() {
        // Set up
        presenter.initializeForTesting();
        
        // Execute
        presenter.cleanup();
        
        // Verify
        verify(viewModel).cleanupResources();
    }
}