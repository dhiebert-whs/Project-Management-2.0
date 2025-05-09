// src/main/java/org/frcpm/mvvm/views/ComponentDetailMvvmView.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.ComponentDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.TaskSelectionMvvmViewModel;

/**
 * View for the component detail using MVVMFx.
 */
public class ComponentDetailMvvmView implements FxmlView<ComponentDetailMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(ComponentDetailMvvmView.class.getName());
    
    @FXML private TextField nameTextField;
    @FXML private TextField partNumberTextField;
    @FXML private TextArea descriptionTextArea;
    @FXML private DatePicker expectedDeliveryDatePicker;
    @FXML private DatePicker actualDeliveryDatePicker;
    @FXML private CheckBox deliveredCheckBox;
    @FXML private TableView<Task> requiredForTasksTable;
    @FXML private TableColumn<Task, String> taskTitleColumn;
    @FXML private TableColumn<Task, String> taskSubsystemColumn;
    @FXML private TableColumn<Task, Integer> taskProgressColumn;
    @FXML private Button addTaskButton;
    @FXML private Button removeTaskButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    
    @InjectViewModel
    private ComponentDetailMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing ComponentDetailMvvmView");
        this.resources = resources;
        
        // Set up table columns
        setupTableColumns();
        
        // Set up bindings
        setupBindings();
        
        // Set up loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
        }
        
        // Set up error label
        if (errorLabel != null) {
            errorLabel.textProperty().bind(viewModel.errorMessageProperty());
            errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
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
    
            // Set up the progress column with progress bars
            taskProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
            taskProgressColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Task, Integer>() {
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
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up table columns", e);
            throw new RuntimeException("Failed to set up table columns", e);
        }
    }
    
    /**
     * Sets up data bindings.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (nameTextField == null || partNumberTextField == null || descriptionTextArea == null || 
            expectedDeliveryDatePicker == null || actualDeliveryDatePicker == null || 
            deliveredCheckBox == null || requiredForTasksTable == null || 
            saveButton == null || cancelButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind text fields
            nameTextField.textProperty().bindBidirectional(viewModel.nameProperty());
            partNumberTextField.textProperty().bindBidirectional(viewModel.partNumberProperty());
            descriptionTextArea.textProperty().bindBidirectional(viewModel.descriptionProperty());
            
            // Bind date pickers
            expectedDeliveryDatePicker.valueProperty().bindBidirectional(viewModel.expectedDeliveryProperty());
            actualDeliveryDatePicker.valueProperty().bindBidirectional(viewModel.actualDeliveryProperty());
            
            // Bind checkbox
            deliveredCheckBox.selectedProperty().bindBidirectional(viewModel.deliveredProperty());
            
            // When delivered is checked, enable/disable actual delivery date
            actualDeliveryDatePicker.disableProperty().bind(viewModel.deliveredProperty().not());
            
            // Bind table to view model collection
            requiredForTasksTable.setItems(viewModel.getRequiredForTasks());
            
            // Bind buttons to commands
            CommandAdapter.bindCommandButton(saveButton, viewModel.getSaveCommand());
            CommandAdapter.bindCommandButton(cancelButton, viewModel.getCancelCommand());
            CommandAdapter.bindCommandButton(addTaskButton, viewModel.getAddTaskCommand());
            CommandAdapter.bindCommandButton(removeTaskButton, viewModel.getRemoveTaskCommand());
            
            // Setup selection changes
            requiredForTasksTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedTask(newVal));
            
            // Set up cancel button to close dialog
            cancelButton.setOnAction(event -> closeDialog());
            
            // Set up delivered checkbox action
            deliveredCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal && actualDeliveryDatePicker.getValue() == null) {
                    actualDeliveryDatePicker.setValue(java.time.LocalDate.now());
                }
            });
            
            // Set up add task button to show task selection dialog
            addTaskButton.setOnAction(event -> handleAddTask());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            throw new RuntimeException("Failed to set up bindings", e);
        }
    }
    
    /**
     * Initializes the view with a new component.
     */
    public void initNewComponent() {
        try {
            viewModel.initNewComponent();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing new component", e);
            // Show error alert
            showErrorAlert(resources.getString("error.title"), 
                          "Failed to initialize new component: " + e.getMessage());
        }
    }
    
    /**
     * Initializes the view with an existing component.
     * 
     * @param component the component to edit
     */
    public void initExistingComponent(Component component) {
        try {
            viewModel.initExistingComponent(component);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing existing component", e);
            // Show error alert
            showErrorAlert(resources.getString("error.title"), 
                          "Failed to initialize component: " + e.getMessage());
        }
    }
    
    /**
     * Handles adding a task to the component.
     * Shows a task selection dialog.
     */
    private void handleAddTask() {
        try {
            // Load the task selection view
            ViewTuple<TaskSelectionMvvmView, TaskSelectionMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(TaskSelectionMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            TaskSelectionMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize with the current project
            viewController.initWithProject(viewModel.getCurrentProject());
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("task.selection.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(saveButton.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Check if a task was selected
            if (viewController.wasTaskSelected()) {
                Task selectedTask = viewController.getSelectedTask();
                if (selectedTask != null) {
                    // Add the task to the component
                    boolean success = viewModel.addTask(selectedTask);
                    if (!success) {
                        // Show error if unsuccessful
                        showErrorAlert(resources.getString("error.title"), 
                                    viewModel.getErrorMessage());
                        viewModel.clearErrorMessage();
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing task selection dialog", e);
            showErrorAlert(resources.getString("error.title"), 
                        "Failed to show task selection dialog: " + e.getMessage());
    }
}
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        // This would use an AlertHelper or DialogService in a full implementation
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        // This would use an AlertHelper or DialogService in a full implementation
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            if (saveButton != null && saveButton.getScene() != null && 
                saveButton.getScene().getWindow() != null) {
                
                // Clean up resources
                viewModel.dispose();
                
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }
    
    /**
     * Gets the view model.
     * 
     * @return the view model
     */
    public ComponentDetailMvvmViewModel getViewModel() {
        return viewModel;
    }
}