package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.services.ComponentService;
import org.frcpm.services.DialogService;
import org.frcpm.services.TaskService;
import org.frcpm.viewmodels.ComponentViewModel;
import org.frcpm.views.TaskView;

import javax.inject.Inject;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for the Component view using AfterburnerFX pattern.
 */
public class ComponentPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ComponentPresenter.class.getName());

    // FXML controls
    @FXML
    private TextField nameTextField;

    @FXML
    private TextField partNumberTextField;

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private DatePicker expectedDeliveryDatePicker;

    @FXML
    private DatePicker actualDeliveryDatePicker;

    @FXML
    private CheckBox deliveredCheckBox;

    @FXML
    private TableView<Task> requiredForTasksTable;

    @FXML
    private TableColumn<Task, String> taskTitleColumn;

    @FXML
    private TableColumn<Task, String> taskSubsystemColumn;

    @FXML
    private TableColumn<Task, Integer> taskProgressColumn;

    @FXML
    private Button addTaskButton;

    @FXML
    private Button removeTaskButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    // Injected services
    @Inject
    private ComponentService componentService;
    
    @Inject
    private TaskService taskService;
    
    @Inject
    private DialogService dialogService;
    
    // Injected ViewModel
    @Inject
    private ComponentViewModel viewModel;

    // Resource bundle
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing ComponentPresenter with resource bundle");
        
        this.resources = resources;
        
        // Verify injection - create fallback if needed
        if (viewModel == null) {
            LOGGER.severe("ComponentViewModel not injected - creating manually as fallback");
            viewModel = new ComponentViewModel(componentService, taskService);
        }

        try {
            // Set up table columns
            setupTableColumns();
    
            // Set up bindings
            setupBindings();
            
            // Set up error handling
            setupErrorHandling();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing presenter", e);
            showErrorAlert("Initialization Error", "Failed to initialize component view: " + e.getMessage());
        }
    }

    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        if (requiredForTasksTable == null || taskTitleColumn == null ||
                taskSubsystemColumn == null || taskProgressColumn == null) {
            LOGGER.warning("Table components not initialized - likely in test environment");
            return;
        }

        try {
            taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
    
            // Set up the subsystem column that displays subsystem name
            taskSubsystemColumn.setCellValueFactory(cellData -> {
                Task task = cellData.getValue();
                return new javafx.beans.property.SimpleStringProperty(
                        task.getSubsystem() != null ? task.getSubsystem().getName() : "");
            });
    
            // Set up the progress column
            taskProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
            taskProgressColumn.setCellFactory(createProgressCellFactory());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up table columns", e);
            throw new RuntimeException("Failed to set up table columns", e);
        }
    }

    /**
     * Creates a factory for progress cells.
     * 
     * @return the cell factory
     */
    private Callback<TableColumn<Task, Integer>, TableCell<Task, Integer>> createProgressCellFactory() {
        return column -> new TableCell<Task, Integer>() {
            private final ProgressBar progressBar = new ProgressBar();

            @Override
            protected void updateItem(Integer progress, boolean empty) {
                super.updateItem(progress, empty);

                if (empty || progress == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    progressBar.setProgress(progress / 100.0);
                    progressBar.setPrefWidth(80);
                    setText(progress + "%");
                    setGraphic(progressBar);
                }
            }
        };
    }

    /**
     * Binds the view model to the UI controls.
     */
    private void setupBindings() {
        if (nameTextField == null || partNumberTextField == null || descriptionTextArea == null ||
                expectedDeliveryDatePicker == null || actualDeliveryDatePicker == null ||
                deliveredCheckBox == null || requiredForTasksTable == null ||
                saveButton == null || cancelButton == null || addTaskButton == null || removeTaskButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind text fields
            ViewModelBinding.bindTextField(nameTextField, viewModel.nameProperty());
            ViewModelBinding.bindTextField(partNumberTextField, viewModel.partNumberProperty());
            ViewModelBinding.bindTextArea(descriptionTextArea, viewModel.descriptionProperty());
    
            // Bind date pickers
            ViewModelBinding.bindDatePicker(expectedDeliveryDatePicker, viewModel.expectedDeliveryProperty());
            ViewModelBinding.bindDatePicker(actualDeliveryDatePicker, viewModel.actualDeliveryProperty());
    
            // Bind checkbox
            deliveredCheckBox.selectedProperty().bindBidirectional(viewModel.deliveredProperty());
    
            // When delivered is checked, enable/disable actual delivery date
            actualDeliveryDatePicker.disableProperty().bind(viewModel.deliveredProperty().not());
    
            // Bind table to view model collection
            requiredForTasksTable.setItems(viewModel.getRequiredForTasks());
    
            // Bind buttons to commands
            ViewModelBinding.bindCommandButton(saveButton, viewModel.getSaveCommand());
            ViewModelBinding.bindCommandButton(cancelButton, viewModel.getCancelCommand());
            ViewModelBinding.bindCommandButton(addTaskButton, viewModel.getAddTaskCommand());
            ViewModelBinding.bindCommandButton(removeTaskButton, viewModel.getRemoveTaskCommand());
    
            // Setup selection changes
            requiredForTasksTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedTask(newVal));
    
            // Set up cancel button override to close dialog
            cancelButton.setOnAction(event -> closeDialog());
    
            // Set up delivered checkbox action
            deliveredCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal && actualDeliveryDatePicker.getValue() == null) {
                    actualDeliveryDatePicker.setValue(LocalDate.now());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            throw new RuntimeException("Failed to set up bindings", e);
        }
    }
    
    /**
     * Sets up error handling for the ViewModel.
     */
    private void setupErrorHandling() {
        if (viewModel == null) {
            LOGGER.warning("ViewModel not initialized - cannot set up error handling");
            return;
        }
        
        try {
            // Set up error message binding
            viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    showErrorAlert("Validation Error", newVal);
                    viewModel.clearErrorMessage();
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up error handling", e);
            throw new RuntimeException("Failed to set up error handling", e);
        }
    }

    /**
     * Initializes the presenter for a new component.
     */
    public void initNewComponent() {
        try {
            viewModel.initNewComponent();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing new component", e);
            showErrorAlert("Initialization Error", "Failed to initialize new component: " + e.getMessage());
        }
    }

    /**
     * Initializes the presenter for editing an existing component.
     * 
     * @param component the component to edit
     */
    public void initExistingComponent(Component component) {
        try {
            viewModel.initExistingComponent(component);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing existing component", e);
            showErrorAlert("Initialization Error", "Failed to initialize component: " + e.getMessage());
        }
    }
    
    /**
     * Handles the add task action.
     */
    @FXML
    private void handleAddTask() {
        try {
            // Example of using ViewLoader with AfterburnerFX
            TaskPresenter presenter = ViewLoader.showDialog(TaskView.class, 
                    resources.getString("task.select.title"), 
                    saveButton.getScene().getWindow());
            
            if (presenter != null) {
                Task task = presenter.getTask();
                if (task != null) {
                    boolean success = viewModel.addTask(task);
                    if (success) {
                        showInfoAlert("Success", "Task added successfully");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding task", e);
            showErrorAlert("Error", "Failed to add task: " + e.getMessage());
        }
    }

    /**
     * Shows an error alert with the given message.
     * 
     * @param title the title of the alert
     * @param message the error message
     */
    private void showErrorAlert(String title, String message) {
        try {
            dialogService.showErrorAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }

    /**
     * Shows an information alert with the given message.
     * 
     * @param title the title of the alert
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        try {
            dialogService.showInfoAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            if (saveButton != null && saveButton.getScene() != null && 
                saveButton.getScene().getWindow() != null) {
                
                // Clean up resources
                if (viewModel != null) {
                    viewModel.cleanupResources();
                }
                
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }

    /**
     * Compatibility method for legacy code
     * 
     * @param component the component to edit
     */
    public void setComponent(Component component) {
        initExistingComponent(component);
    }

    /**
     * Gets the component from the ViewModel.
     * 
     * @return the component
     */
    public Component getComponent() {
        return viewModel.getComponent();
    }

    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public ComponentViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * Sets the ViewModel (for testing purposes).
     * 
     * @param viewModel the view model to set
     */
    public void setViewModel(ComponentViewModel viewModel) {
        this.viewModel = viewModel;
        setupBindings();
        setupErrorHandling();
    }
    
    /**
     * Clean up resources when the presenter is no longer needed.
     */
    public void cleanup() {
        if (viewModel != null) {
            viewModel.cleanupResources();
        }
    }
}