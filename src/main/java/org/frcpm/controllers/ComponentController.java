// src/main/java/org/frcpm/controllers/ComponentController.java
package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.services.DialogService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.viewmodels.ComponentViewModel;

import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Component view.
 * Follows the standardized MVVM pattern by delegating all business logic to the
 * ComponentViewModel.
 */
public class ComponentController {

    private static final Logger LOGGER = Logger.getLogger(ComponentController.class.getName());

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

    // ViewModel and services
    private ComponentViewModel viewModel = new ComponentViewModel();
    private DialogService dialogService = ServiceFactory.getDialogService();

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing ComponentController");

        if (nameTextField == null || partNumberTextField == null || descriptionTextArea == null ||
                expectedDeliveryDatePicker == null || actualDeliveryDatePicker == null ||
                deliveredCheckBox == null || requiredForTasksTable == null ||
                taskTitleColumn == null || taskSubsystemColumn == null || taskProgressColumn == null ||
                saveButton == null || cancelButton == null || addTaskButton == null || removeTaskButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        // Set up table columns
        setupTableColumns();

        // Set up bindings
        setupBindings();
    }

    /**
     * Sets up the table columns.
     * Protected for testability.
     */
    protected void setupTableColumns() {
        if (requiredForTasksTable == null || taskTitleColumn == null ||
                taskSubsystemColumn == null || taskProgressColumn == null) {
            LOGGER.warning("Table components not initialized - likely in test environment");
            return;
        }

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
    }

    /**
     * Creates a factory for progress cells.
     * Protected for testability.
     * 
     * @return the cell factory
     */
    protected Callback<TableColumn<Task, Integer>, TableCell<Task, Integer>> createProgressCellFactory() {
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
     * Protected for testability.
     */
    protected void setupBindings() {
        if (nameTextField == null || partNumberTextField == null || descriptionTextArea == null ||
                expectedDeliveryDatePicker == null || actualDeliveryDatePicker == null ||
                deliveredCheckBox == null || requiredForTasksTable == null ||
                saveButton == null || cancelButton == null || addTaskButton == null || removeTaskButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

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

        // Set up error message binding
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorAlert("Validation Error", newVal);
                viewModel.clearErrorMessage();
            }
        });

        // Set up cancel button override to close dialog
        cancelButton.setOnAction(event -> closeDialog());

        // Set up delivered checkbox action
        deliveredCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && actualDeliveryDatePicker.getValue() == null) {
                actualDeliveryDatePicker.setValue(LocalDate.now());
            }
        });
    }

    /**
     * Initializes the controller for a new component.
     */
    public void initNewComponent() {
        viewModel.initNewComponent();
    }

    /**
     * Initializes the controller for editing an existing component.
     * 
     * @param component the component to edit
     */
    public void initExistingComponent(Component component) {
        viewModel.initExistingComponent(component);
    }

    /**
     * Shows an error alert with the given message.
     * Protected for testability.
     * 
     * @param title   the title of the alert
     * @param message the error message
     */
    protected void showErrorAlert(String title, String message) {
        try {
            dialogService.showErrorAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }

    /**
     * Shows an information alert with the given message.
     * Protected for testability.
     * 
     * @param title   the title of the alert
     * @param message the message
     */
    protected void showInfoAlert(String title, String message) {
        try {
            dialogService.showInfoAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }

    /**
     * Shows a confirmation alert dialog.
     * Protected for testability.
     * 
     * @param title   the title
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    protected boolean showConfirmationAlert(String title, String message) {
        try {
            return dialogService.showConfirmationAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Confirmation would show: {0} - {1}", new Object[] { title, message });
            return false;
        }
    }

    /**
     * Shows a dialog and waits for it to be closed.
     * Protected for testability.
     * 
     * @param <T>    the type of the dialog result
     * @param dialog the dialog to show
     * @return an optional containing the dialog result
     */
    protected <T> Optional<T> showAndWaitDialog(Dialog<T> dialog) {
        try {
            if (dialog != null) {
                return dialog.showAndWait();
            }
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing dialog", e);
            return Optional.empty();
        }
    }

    /**
     * Gets the selected task from the table.
     * Protected for testability.
     * 
     * @return the selected task
     */
    protected Task getSelectedTask() {
        if (requiredForTasksTable != null && requiredForTasksTable.getSelectionModel() != null) {
            return requiredForTasksTable.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    /**
     * Closes the dialog.
     * Protected for testability.
     */
    protected void closeDialog() {
        try {
            if (saveButton != null && saveButton.getScene() != null && saveButton.getScene().getWindow() != null) {
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
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
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
     * Sets the ViewModel.
     * This method is primarily used for testing to inject mock viewmodels.
     * 
     * @param viewModel the viewModel to use
     */
    public void setViewModel(ComponentViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Sets the DialogService for this controller.
     * This method is primarily used for testing to inject mock services.
     * 
     * @param dialogService the dialog service to use
     */
    public void setDialogService(DialogService dialogService) {
        this.dialogService = dialogService;
    }
}