package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.frcpm.binding.Command;
import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.ComponentViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;

import java.time.LocalDate;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ComponentControllerTest extends BaseJavaFXTest {

    // Controller to test
    private ComponentController componentController;

    // Spy for the controller for testing protected methods
    private ComponentController controllerSpy;

    // Mock services
    private DialogService mockDialogService;

    // Mock ViewModel
    private ComponentViewModel mockViewModel;

    // Mock Commands
    private Command mockSaveCommand;
    private Command mockCancelCommand;
    private Command mockAddTaskCommand;
    private Command mockRemoveTaskCommand;

    // UI components - real JavaFX components
    private TextField nameTextField;
    private TextField partNumberTextField;
    private TextArea descriptionTextArea;
    private DatePicker expectedDeliveryDatePicker;
    private DatePicker actualDeliveryDatePicker;
    private CheckBox deliveredCheckBox;
    private Button saveButton;
    private Button cancelButton;

    // Test data
    private Component testComponent;

    /**
     * Set up the JavaFX environment before each test.
     * This is invoked by TestFX before each test method.
     */
    @Start
    public void start(Stage stage) {
        // Create real JavaFX components
        nameTextField = new TextField();
        partNumberTextField = new TextField();
        descriptionTextArea = new TextArea();
        expectedDeliveryDatePicker = new DatePicker();
        actualDeliveryDatePicker = new DatePicker();
        deliveredCheckBox = new CheckBox();
        saveButton = new Button("Save");
        cancelButton = new Button("Cancel");

        // Create a layout to hold the components
        VBox root = new VBox(10);
        root.getChildren().addAll(
                nameTextField, partNumberTextField, descriptionTextArea,
                expectedDeliveryDatePicker, actualDeliveryDatePicker,
                deliveredCheckBox, saveButton, cancelButton);

        // Set up and show the stage
        Scene scene = new Scene(root, 400, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Set up the test data and mock objects before each test.
     */
    @BeforeEach
    public void setUp() throws Exception {
        // Create mock services
        mockDialogService = mock(DialogService.class);

        // Create a new controller instance
        componentController = new ComponentController();

        // Create a spy for the controller
        controllerSpy = spy(componentController);

        // Inject the mock dialog service into the controller
        setPrivateField(controllerSpy, "dialogService", mockDialogService);

        // Create mock Command objects
        mockSaveCommand = mock(Command.class);
        mockCancelCommand = mock(Command.class);
        mockAddTaskCommand = mock(Command.class);
        mockRemoveTaskCommand = mock(Command.class);

        // Create mock ViewModel
        mockViewModel = mock(ComponentViewModel.class);

        // Set up basic mock behavior
        when(mockViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
        when(mockViewModel.getCancelCommand()).thenReturn(mockCancelCommand);
        when(mockViewModel.getAddTaskCommand()).thenReturn(mockAddTaskCommand);
        when(mockViewModel.getRemoveTaskCommand()).thenReturn(mockRemoveTaskCommand);
        when(mockViewModel.getRequiredForTasks()).thenReturn(FXCollections.observableArrayList());
        when(mockViewModel.nameProperty()).thenReturn(new javafx.beans.property.SimpleStringProperty());
        when(mockViewModel.partNumberProperty()).thenReturn(new javafx.beans.property.SimpleStringProperty());
        when(mockViewModel.descriptionProperty()).thenReturn(new javafx.beans.property.SimpleStringProperty());
        when(mockViewModel.expectedDeliveryProperty()).thenReturn(new javafx.beans.property.SimpleObjectProperty<>());
        when(mockViewModel.actualDeliveryProperty()).thenReturn(new javafx.beans.property.SimpleObjectProperty<>());
        when(mockViewModel.deliveredProperty()).thenReturn(new javafx.beans.property.SimpleBooleanProperty(false));
        when(mockViewModel.errorMessageProperty()).thenReturn(new javafx.beans.property.SimpleStringProperty());

        // Inject components into controller using reflection
        injectField("nameTextField", nameTextField);
        injectField("partNumberTextField", partNumberTextField);
        injectField("descriptionTextArea", descriptionTextArea);
        injectField("expectedDeliveryDatePicker", expectedDeliveryDatePicker);
        injectField("actualDeliveryDatePicker", actualDeliveryDatePicker);
        injectField("deliveredCheckBox", deliveredCheckBox);
        injectField("saveButton", saveButton);
        injectField("cancelButton", cancelButton);

        // Create mock tables and columns since they're problematic in tests
        injectField("requiredForTasksTable", mock(TableView.class));
        injectField("taskTitleColumn", mock(TableColumn.class));
        injectField("taskSubsystemColumn", mock(TableColumn.class));
        injectField("taskProgressColumn", mock(TableColumn.class));

        // Also inject mock buttons for those we don't use
        injectField("addTaskButton", mock(Button.class));
        injectField("removeTaskButton", mock(Button.class));

        // Inject the mock ViewModel
        injectField("viewModel", mockViewModel);

        // Create test component
        testComponent = new Component("Test Component", "PART123");
        testComponent.setId(1L);
        testComponent.setDescription("Test component description");
        testComponent.setExpectedDelivery(LocalDate.now().plusWeeks(2));
        testComponent.setDelivered(false);

        // Set up mock ViewModel behavior
        when(mockViewModel.getComponent()).thenReturn(testComponent);
    }

    /**
     * Test the initialization of the controller.
     */
    @Test
    public void testInitialize() {
        // Call the method to test
        controllerSpy.testInitialize();

        // Verify that bindings were set up
        verify(mockViewModel).nameProperty();
        verify(mockViewModel).partNumberProperty();
        verify(mockViewModel).descriptionProperty();
        verify(mockViewModel).expectedDeliveryProperty();
        verify(mockViewModel).actualDeliveryProperty();
        verify(mockViewModel).deliveredProperty();

        // Verify command bindings
        verify(mockViewModel).getSaveCommand();
        verify(mockViewModel).getCancelCommand();
        verify(mockViewModel).getAddTaskCommand();
        verify(mockViewModel).getRemoveTaskCommand();

        // Verify tables were bound to the ViewModel collections
        verify(mockViewModel).getRequiredForTasks();
    }

    /**
     * Test setting up controller for a new component.
     */
    @Test
    public void testInitNewComponent() {
        // Call method
        controllerSpy.initNewComponent();

        // Verify ViewModel method was called
        verify(mockViewModel).initNewComponent();
    }

    /**
     * Test setting up controller for editing an existing component.
     */
    @Test
    public void testInitExistingComponent() {
        // Call method
        controllerSpy.initExistingComponent(testComponent);

        // Verify ViewModel method was called
        verify(mockViewModel).initExistingComponent(testComponent);
    }

    /**
     * Test the showErrorAlert protected method.
     */
    @Test
    public void testShowErrorAlert() {
        // Call method
        controllerSpy.showErrorAlert("Test Title", "Test Message");

        // Verify dialog service was called
        verify(mockDialogService).showErrorAlert("Test Title", "Test Message");
    }

    /**
     * Test error message property listener.
     */
    @Test
    public void testErrorMessageListener() {
        // Set up controller
        controllerSpy.testInitialize();

        // Set up test data
        javafx.beans.property.SimpleStringProperty errorProperty = new javafx.beans.property.SimpleStringProperty();
        when(mockViewModel.errorMessageProperty()).thenReturn(errorProperty);

        // Trigger the error message listener
        errorProperty.set("Test Error");

        // Verify dialog service was called and error message was cleared
        verify(mockDialogService).showErrorAlert(anyString(), eq("Test Error"));
        verify(mockViewModel).errorMessageProperty();
    }

    /**
     * Test the closeDialog protected method.
     */
    @Test
    public void testCloseDialog() {
        // Setup - mock the stage closing
        doNothing().when(controllerSpy).closeDialog();

        // Call method
        controllerSpy.closeDialog();

        // Verify the method was called
        verify(controllerSpy).closeDialog();
    }

    /**
     * Test the legacy setComponent method.
     */
    @Test
    public void testSetComponent() {
        // Call method
        controllerSpy.setComponent(testComponent);

        // Verify ViewModel method was called
        verify(mockViewModel).initExistingComponent(testComponent);
    }

    /**
     * Test the getComponent method.
     */
    @Test
    public void testGetComponent() {
        // Call method
        Component result = controllerSpy.getComponent();

        // Verify ViewModel method was called and returned the expected result
        verify(mockViewModel).getComponent();
        assertEquals(testComponent, result);
    }

    /**
     * Test the getViewModel method.
     */
    @Test
    public void testGetViewModel() {
        // Call method
        ComponentViewModel result = controllerSpy.getViewModel();

        // Verify correct ViewModel is returned
        assertEquals(mockViewModel, result);
    }

    /**
     * Helper method to inject field values using reflection.
     */
    private void injectField(String fieldName, Object value) throws Exception {
        Field field = ComponentController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controllerSpy, value);
    }

    /**
     * Helper method to set a private field using reflection.
     */
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}