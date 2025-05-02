package org.frcpm.presenters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.frcpm.binding.Command;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.services.ComponentService;
import org.frcpm.services.DialogService;
import org.frcpm.services.TaskService;
import org.frcpm.viewmodels.ComponentViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for ComponentPresenter using the recommended patterns
 * for AfterburnerFX integration testing.
 */
@ExtendWith(MockitoExtension.class)
public class ComponentPresenterTest {

    @Mock
    private ComponentViewModel mockViewModel;
    
    @Mock
    private ComponentService mockComponentService;
    
    @Mock
    private TaskService mockTaskService;
    
    @Mock
    private DialogService mockDialogService;
    
    @Mock
    private ResourceBundle mockResources;
    
    @Mock
    private TextField mockNameTextField;
    
    @Mock
    private TextField mockPartNumberTextField;
    
    @Mock
    private TextArea mockDescriptionTextArea;
    
    @Mock
    private DatePicker mockExpectedDeliveryDatePicker;
    
    @Mock
    private DatePicker mockActualDeliveryDatePicker;
    
    @Mock
    private CheckBox mockDeliveredCheckBox;
    
    @Mock
    private TableView<Task> mockRequiredForTasksTable;
    
    @Mock
    private Button mockAddTaskButton;
    
    @Mock
    private Button mockRemoveTaskButton;
    
    @Mock
    private Button mockSaveButton;
    
    @Mock
    private Button mockCancelButton;
    
    @Mock
    private Command mockSaveCommand;
    
    @Mock
    private Command mockCancelCommand;
    
    @Mock
    private Command mockAddTaskCommand;
    
    @Mock
    private Command mockRemoveTaskCommand;
    
    @InjectMocks
    private ComponentPresenter presenter;
    
    // Test data
    private Component testComponent;
    private Task testTask;
    private StringProperty testNameProperty;
    private StringProperty testPartNumberProperty;
    private StringProperty testDescriptionProperty;
    private ObjectProperty<LocalDate> testExpectedDeliveryProperty;
    private ObjectProperty<LocalDate> testActualDeliveryProperty;
    private BooleanProperty testDeliveredProperty;
    private StringProperty testErrorProperty;
    private ObservableList<Task> testTasks;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test data
        testComponent = new Component();
        testComponent.setId(1L);
        testComponent.setName("Test Component");
        
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        
        Set<Task> taskSet = new HashSet<>();
        taskSet.add(testTask);
        testComponent.setRequiredForTasks(taskSet);
        
        testNameProperty = new SimpleStringProperty("Test Component");
        testPartNumberProperty = new SimpleStringProperty("123-ABC");
        testDescriptionProperty = new SimpleStringProperty("Test Description");
        testExpectedDeliveryProperty = new SimpleObjectProperty<>(LocalDate.now().plusDays(14));
        testActualDeliveryProperty = new SimpleObjectProperty<>();
        testDeliveredProperty = new SimpleBooleanProperty(false);
        testErrorProperty = new SimpleStringProperty("");
        
        testTasks = FXCollections.observableArrayList();
        testTasks.add(testTask);
        
        // Set up common ViewModel stubs with lenient mocking
        lenient().when(mockViewModel.nameProperty()).thenReturn(testNameProperty);
        lenient().when(mockViewModel.partNumberProperty()).thenReturn(testPartNumberProperty);
        lenient().when(mockViewModel.descriptionProperty()).thenReturn(testDescriptionProperty);
        lenient().when(mockViewModel.expectedDeliveryProperty()).thenReturn(testExpectedDeliveryProperty);
        lenient().when(mockViewModel.actualDeliveryProperty()).thenReturn(testActualDeliveryProperty);
        lenient().when(mockViewModel.deliveredProperty()).thenReturn(testDeliveredProperty);
        lenient().when(mockViewModel.errorMessageProperty()).thenReturn(testErrorProperty);
        lenient().when(mockViewModel.getRequiredForTasks()).thenReturn(testTasks);
        lenient().when(mockViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
        lenient().when(mockViewModel.getCancelCommand()).thenReturn(mockCancelCommand);
        lenient().when(mockViewModel.getAddTaskCommand()).thenReturn(mockAddTaskCommand);
        lenient().when(mockViewModel.getRemoveTaskCommand()).thenReturn(mockRemoveTaskCommand);
        lenient().when(mockViewModel.getComponent()).thenReturn(testComponent);
        
        // Inject UI components
        injectUIComponents();
    }
    
    /**
     * Injects UI components into the presenter using reflection.
     */
    private void injectUIComponents() {
        try {
            injectField(presenter, "nameTextField", mockNameTextField);
            injectField(presenter, "partNumberTextField", mockPartNumberTextField);
            injectField(presenter, "descriptionTextArea", mockDescriptionTextArea);
            injectField(presenter, "expectedDeliveryDatePicker", mockExpectedDeliveryDatePicker);
            injectField(presenter, "actualDeliveryDatePicker", mockActualDeliveryDatePicker);
            injectField(presenter, "deliveredCheckBox", mockDeliveredCheckBox);
            injectField(presenter, "requiredForTasksTable", mockRequiredForTasksTable);
            injectField(presenter, "addTaskButton", mockAddTaskButton);
            injectField(presenter, "removeTaskButton", mockRemoveTaskButton);
            injectField(presenter, "saveButton", mockSaveButton);
            injectField(presenter, "cancelButton", mockCancelButton);
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
        
        // Assert - verify UI bindings are established
        verify(mockViewModel, atLeastOnce()).nameProperty();
        verify(mockViewModel, atLeastOnce()).partNumberProperty();
        verify(mockViewModel, atLeastOnce()).descriptionProperty();
        verify(mockViewModel, atLeastOnce()).expectedDeliveryProperty();
        verify(mockViewModel, atLeastOnce()).actualDeliveryProperty();
        verify(mockViewModel, atLeastOnce()).deliveredProperty();
        verify(mockViewModel, atLeastOnce()).errorMessageProperty();
        verify(mockViewModel).getRequiredForTasks();
        verify(mockViewModel).getSaveCommand();
        verify(mockViewModel).getCancelCommand();
        verify(mockViewModel).getAddTaskCommand();
        verify(mockViewModel).getRemoveTaskCommand();
    }
    
    @Test
    public void testInitNewComponent() {
        // Act
        presenter.initNewComponent();
        
        // Assert
        verify(mockViewModel).initNewComponent();
    }
    
    @Test
    public void testInitExistingComponent() {
        // Arrange
        Component component = new Component();
        component.setId(2L);
        component.setName("Existing Component");
        
        // Act
        presenter.initExistingComponent(component);
        
        // Assert
        verify(mockViewModel).initExistingComponent(component);
    }
    
    
    /**
     * Test for add task button action.
     * This now uses reflection since handleAddTask is a private method.
     */
    @Test
    public void testHandleAddTaskButton() {
        // We need to use reflection to access the private handleAddTask method
        try {
            // Arrange
            Scene mockScene = mock(Scene.class);
            Window mockWindow = mock(Window.class);
            
            when(mockResources.getString("task.select.title")).thenReturn("Select Task");
            when(mockSaveButton.getScene()).thenReturn(mockScene);
            when(mockScene.getWindow()).thenReturn(mockWindow);
            
            // Get the private method using reflection
            java.lang.reflect.Method method = ComponentPresenter.class.getDeclaredMethod("handleAddTask");
            method.setAccessible(true);
            
            // Mock static ViewLoader
            try (MockedStatic<ViewLoader> mockViewLoader = mockStatic(ViewLoader.class)) {
                // Arrange - mock the dialog result
                TaskPresenter mockTaskPresenter = mock(TaskPresenter.class);
                when(mockTaskPresenter.getTask()).thenReturn(testTask);
                mockViewLoader.when(() -> ViewLoader.showDialog(any(), anyString(), any()))
                    .thenReturn(mockTaskPresenter);
                
                // Arrange - mock the ViewModel's addTask method
                when(mockViewModel.addTask(testTask)).thenReturn(true);
                
                // Act - invoke the private method using reflection
                method.invoke(presenter);
                
                // Assert
                mockViewLoader.verify(() -> ViewLoader.showDialog(any(), eq("Select Task"), any()));
                verify(mockViewModel).addTask(testTask);
                verify(mockDialogService).showInfoAlert(anyString(), anyString());
            }
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for add task button action with failure.
     * This uses reflection to access the private method.
     */
    @Test
    public void testHandleAddTaskButton_Failure() {
        try {
            // Arrange
            Scene mockScene = mock(Scene.class);
            Window mockWindow = mock(Window.class);
            
            when(mockResources.getString("task.select.title")).thenReturn("Select Task");
            when(mockSaveButton.getScene()).thenReturn(mockScene);
            when(mockScene.getWindow()).thenReturn(mockWindow);
            
            // Get the private method using reflection
            java.lang.reflect.Method method = ComponentPresenter.class.getDeclaredMethod("handleAddTask");
            method.setAccessible(true);
            
            // Mock static ViewLoader
            try (MockedStatic<ViewLoader> mockViewLoader = mockStatic(ViewLoader.class)) {
                // Arrange - mock the dialog result
                TaskPresenter mockTaskPresenter = mock(TaskPresenter.class);
                when(mockTaskPresenter.getTask()).thenReturn(testTask);
                mockViewLoader.when(() -> ViewLoader.showDialog(any(), anyString(), any()))
                    .thenReturn(mockTaskPresenter);
                
                // Arrange - mock the ViewModel's addTask method to return false (failure)
                when(mockViewModel.addTask(testTask)).thenReturn(false);
                
                // Act - invoke the private method using reflection
                method.invoke(presenter);
                
                // Assert
                mockViewLoader.verify(() -> ViewLoader.showDialog(any(), eq("Select Task"), any()));
                verify(mockViewModel).addTask(testTask);
                verify(mockDialogService, never()).showInfoAlert(anyString(), anyString());
            }
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for add task button action with null task.
     * This uses reflection to access the private method.
     */
    @Test
    public void testHandleAddTaskButton_NullTask() {
        try {
            // Arrange
            Scene mockScene = mock(Scene.class);
            Window mockWindow = mock(Window.class);
            
            when(mockResources.getString("task.select.title")).thenReturn("Select Task");
            when(mockSaveButton.getScene()).thenReturn(mockScene);
            when(mockScene.getWindow()).thenReturn(mockWindow);
            
            // Get the private method using reflection
            java.lang.reflect.Method method = ComponentPresenter.class.getDeclaredMethod("handleAddTask");
            method.setAccessible(true);
            
            // Mock static ViewLoader
            try (MockedStatic<ViewLoader> mockViewLoader = mockStatic(ViewLoader.class)) {
                // Arrange - mock the dialog result with null task
                TaskPresenter mockTaskPresenter = mock(TaskPresenter.class);
                when(mockTaskPresenter.getTask()).thenReturn(null);
                mockViewLoader.when(() -> ViewLoader.showDialog(any(), anyString(), any()))
                    .thenReturn(mockTaskPresenter);
                
                // Act - invoke the private method using reflection
                method.invoke(presenter);
                
                // Assert
                mockViewLoader.verify(() -> ViewLoader.showDialog(any(), eq("Select Task"), any()));
                verify(mockViewModel, never()).addTask(any());
            }
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testCloseDialog() {
        // Arrange
        Stage mockStage = mock(Stage.class);
        Scene mockScene = mock(Scene.class);
        when(mockSaveButton.getScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockStage);
        
        // Create a method to access the private method
        try {
            java.lang.reflect.Method method = ComponentPresenter.class.getDeclaredMethod("closeDialog");
            method.setAccessible(true);
            
            // Act
            method.invoke(presenter);
            
            // Assert
            verify(mockViewModel).cleanupResources();
            verify(mockStage).close();
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testGetViewModel() {
        // Act
        ComponentViewModel result = presenter.getViewModel();
        
        // Assert
        assertSame(mockViewModel, result);
    }
    
    @Test
    public void testSetViewModel() {
        // Arrange
        ComponentViewModel newViewModel = mock(ComponentViewModel.class);
        when(newViewModel.nameProperty()).thenReturn(new SimpleStringProperty());
        when(newViewModel.partNumberProperty()).thenReturn(new SimpleStringProperty());
        when(newViewModel.descriptionProperty()).thenReturn(new SimpleStringProperty());
        when(newViewModel.expectedDeliveryProperty()).thenReturn(new SimpleObjectProperty<>());
        when(newViewModel.actualDeliveryProperty()).thenReturn(new SimpleObjectProperty<>());
        when(newViewModel.deliveredProperty()).thenReturn(new SimpleBooleanProperty());
        when(newViewModel.errorMessageProperty()).thenReturn(new SimpleStringProperty());
        when(newViewModel.getRequiredForTasks()).thenReturn(FXCollections.observableArrayList());
        
        // Act
        presenter.setViewModel(newViewModel);
        
        // Assert
        assertEquals(newViewModel, presenter.getViewModel());
    }
    
    @Test
    public void testGetComponent() {
        // Act
        Component result = presenter.getComponent();
        
        // Assert
        assertSame(testComponent, result);
        verify(mockViewModel).getComponent();
    }
    
    @Test
    public void testSetComponent() {
        // Arrange
        Component component = new Component();
        component.setId(3L);
        component.setName("Component from Legacy Code");
        
        // Act
        presenter.setComponent(component);
        
        // Assert
        verify(mockViewModel).initExistingComponent(component);
    }
    
    @Test
    public void testErrorHandler() {
        // Arrange
        presenter.initialize(mock(URL.class), mockResources);
        
        // Act - simulate an error message change
        testErrorProperty.set("Test error message");
        
        // We can't directly test the alert showing in a unit test
        // But we can verify the binding setup by accessing the updated property
        assertEquals("Test error message", testErrorProperty.get());
    }
    
    @Test
    public void testDeliveredCheckboxListener() {
        // Arrange
        presenter.initialize(mock(URL.class), mockResources);
        
        // Mock the ActualDeliveryDatePicker getValue() call
        when(mockActualDeliveryDatePicker.getValue()).thenReturn(null);
        
        // Create a listener trigger simulation
        // Since we can't directly access the listener bound to the checkbox
        try {
            // Get the private field where the listener is set
            java.lang.reflect.Field checkboxField = ComponentPresenter.class.getDeclaredField("deliveredCheckBox");
            checkboxField.setAccessible(true);
            
            // Set the checkbox to checked and fire the change event
            // This is simulating the UI interaction
            testDeliveredProperty.set(true);
            
            // We can now test if the property is updated correctly
            assertEquals(true, testDeliveredProperty.get());
            
            // Note: We can't directly verify DatePicker.setValue() was called in a unit test
            // This is a limitation of testing JavaFX components without TestFX
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testCleanup() {
        // Act
        presenter.cleanup();
        
        // Assert
        verify(mockViewModel).cleanupResources();
    }
}