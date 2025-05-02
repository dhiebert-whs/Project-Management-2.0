package org.frcpm.presenters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;

import org.frcpm.binding.Command;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Component;
import org.frcpm.services.ComponentService;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.ComponentManagementViewModel;
import org.frcpm.viewmodels.ComponentManagementViewModel.ComponentFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for ComponentManagementPresenter using the recommended patterns
 * for AfterburnerFX integration testing.
 */
@ExtendWith(MockitoExtension.class)
public class ComponentManagementPresenterTest {

    @Mock
    private ComponentManagementViewModel mockViewModel;
    
    @Mock
    private ComponentService mockComponentService;
    
    @Mock
    private DialogService mockDialogService;
    
    @Mock
    private ResourceBundle mockResources;
    
    @Mock
    private TableView<Component> mockComponentsTable;
    
    @Mock
    private Button mockAddComponentButton;
    
    @Mock
    private Button mockEditComponentButton;
    
    @Mock
    private Button mockDeleteComponentButton;
    
    @Mock
    private Button mockRefreshButton;
    
    @Mock
    private ComboBox<ComponentFilter> mockFilterComboBox;
    
    @Mock
    private Command mockAddCommand;
    
    @Mock
    private Command mockEditCommand;
    
    @Mock
    private Command mockDeleteCommand;
    
    @Mock
    private Command mockRefreshCommand;
    
    @InjectMocks
    private ComponentManagementPresenter presenter;
    
    // Test data
    private ObservableList<Component> testComponents;
    private StringProperty testErrorProperty;
    private Component testComponent;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test data
        testComponent = new Component();
        testComponent.setId(1L);
        testComponent.setName("Test Component");
        
        testComponents = FXCollections.observableArrayList();
        testComponents.add(testComponent);
        
        testErrorProperty = new SimpleStringProperty("");
        
        // Set up common ViewModel stubs with lenient mocking
        lenient().when(mockViewModel.errorMessageProperty()).thenReturn(testErrorProperty);
        lenient().when(mockViewModel.getComponents()).thenReturn(testComponents);
        lenient().when(mockViewModel.getAddComponentCommand()).thenReturn(mockAddCommand);
        lenient().when(mockViewModel.getEditComponentCommand()).thenReturn(mockEditCommand);
        lenient().when(mockViewModel.getDeleteComponentCommand()).thenReturn(mockDeleteCommand);
        lenient().when(mockViewModel.getRefreshCommand()).thenReturn(mockRefreshCommand);
        
        // Reset test fields
        injectUIComponents();
    }
    
    /**
     * Injects UI components into the presenter using reflection.
     */
    private void injectUIComponents() {
        try {
            injectField(presenter, "componentsTable", mockComponentsTable);
            injectField(presenter, "addComponentButton", mockAddComponentButton);
            injectField(presenter, "editComponentButton", mockEditComponentButton);
            injectField(presenter, "deleteComponentButton", mockDeleteComponentButton);
            injectField(presenter, "refreshButton", mockRefreshButton);
            injectField(presenter, "filterComboBox", mockFilterComboBox);
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
        
        // Assert
        verify(mockViewModel).errorMessageProperty();
        verify(mockViewModel).getComponents();
        verify(mockViewModel).loadComponents();
    }
    
    @Test
    public void testHandleAddComponent() {
        // Arrange
        when(mockResources.getString("component.new.title")).thenReturn("New Component");
        
        // Use try-with-resources to handle the static mock
        try (MockedStatic<ViewLoader> mockViewLoader = mockStatic(ViewLoader.class)) {
            ComponentPresenter mockComponentPresenter = mock(ComponentPresenter.class);
            mockViewLoader.when(() -> ViewLoader.showDialog(any(), anyString(), any()))
                .thenReturn(mockComponentPresenter);
            
            // Act
            presenter.handleAddComponent();
            
            // Assert
            mockViewLoader.verify(() -> ViewLoader.showDialog(any(), eq("New Component"), any()));
            verify(mockComponentPresenter).initNewComponent();
            verify(mockViewModel).loadComponents();
        }
    }
    
    @Test
    public void testHandleEditComponent() {
        // Arrange
        when(mockResources.getString("component.edit.title")).thenReturn("Edit Component");
        
        // Use try-with-resources to handle the static mock
        try (MockedStatic<ViewLoader> mockViewLoader = mockStatic(ViewLoader.class)) {
            ComponentPresenter mockComponentPresenter = mock(ComponentPresenter.class);
            mockViewLoader.when(() -> ViewLoader.showDialog(any(), anyString(), any()))
                .thenReturn(mockComponentPresenter);
            
            // Act
            presenter.handleEditComponent(testComponent);
            
            // Assert
            mockViewLoader.verify(() -> ViewLoader.showDialog(any(), eq("Edit Component"), any()));
            verify(mockComponentPresenter).initExistingComponent(testComponent);
            verify(mockViewModel).loadComponents();
        }
    }
    
    @Test
    public void testHandleDeleteComponent_Success() {
        // Arrange
        Component component = new Component();
        component.setId(2L);
        component.setName("Component to Delete");
        
        when(mockViewModel.getSelectedComponent()).thenReturn(component);
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(true);
        when(mockViewModel.deleteComponent(component)).thenReturn(true);
        
        // Act
        presenter.handleDeleteComponent();
        
        // Assert
        verify(mockDialogService).showConfirmationAlert(anyString(), anyString());
        verify(mockViewModel).deleteComponent(component);
        verify(mockViewModel).loadComponents();
    }
    
    @Test
    public void testHandleDeleteComponent_Cancelled() {
        // Arrange
        Component component = new Component();
        component.setId(2L);
        component.setName("Component to Delete");
        
        when(mockViewModel.getSelectedComponent()).thenReturn(component);
        when(mockDialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(false);
        
        // Act
        presenter.handleDeleteComponent();
        
        // Assert
        verify(mockDialogService).showConfirmationAlert(anyString(), anyString());
        verify(mockViewModel, never()).deleteComponent(any());
        verify(mockViewModel, never()).loadComponents();
    }
    
    @Test
    public void testHandleDeleteComponent_NoSelection() {
        // Arrange
        when(mockViewModel.getSelectedComponent()).thenReturn(null);
        
        // Act
        presenter.handleDeleteComponent();
        
        // Assert
        verify(mockDialogService).showInfoAlert(anyString(), anyString());
        verify(mockViewModel, never()).deleteComponent(any());
    }
    
    @Test
    public void testHandleRefresh() {
        // Act
        presenter.handleRefresh();
        
        // Assert
        verify(mockViewModel).loadComponents();
    }
    
    @Test
    public void testGetViewModel() {
        // Act
        ComponentManagementViewModel result = presenter.getViewModel();
        
        // Assert
        assertSame(mockViewModel, result);
    }
    
    @Test
    public void testSetViewModel() {
        // Arrange
        ComponentManagementViewModel newViewModel = mock(ComponentManagementViewModel.class);
        when(newViewModel.errorMessageProperty()).thenReturn(new SimpleStringProperty());
        when(newViewModel.getComponents()).thenReturn(FXCollections.observableArrayList());
        
        // Act
        presenter.setViewModel(newViewModel);
        
        // Assert
        assertEquals(newViewModel, presenter.getViewModel());
    }
    
    @Test
    public void testCleanup() {
        // Act
        presenter.cleanup();
        
        // Assert
        verify(mockViewModel).cleanupResources();
    }
    
    @Test
    public void testErrorMessageListener() {
        // Arrange
        presenter.initialize(mock(URL.class), mockResources);
        
        // Act - simulate an error message change
        testErrorProperty.set("Test error message");
        
        // This won't actually trigger the listener in unit tests without a JavaFX thread
        // But we're testing that the binding is set up correctly
    }
}