// src/main/java/org/frcpm/controllers/MilestoneManagementController.java
package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.viewmodels.MilestoneManagementViewModel;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;

/**
 * Controller for the Milestone Management view.
 * Follows the standardized MVVM pattern by delegating all business logic to the
 * MilestoneManagementViewModel.
 */
public class MilestoneManagementController {

    private static final Logger LOGGER = Logger.getLogger(MilestoneManagementController.class.getName());

    // FXML controls
    @FXML
    private TableView<Milestone> milestonesTable;

    @FXML
    private TableColumn<Milestone, String> nameColumn;

    @FXML
    private TableColumn<Milestone, LocalDate> dateColumn;

    @FXML
    private TableColumn<Milestone, String> statusColumn;

    @FXML
    private Button addMilestoneButton;

    @FXML
    private Button editMilestoneButton;

    @FXML
    private Button deleteMilestoneButton;

    @FXML
    private Button refreshButton;

    @FXML
    private ComboBox<MilestoneManagementViewModel.MilestoneFilter> filterComboBox;

    // ViewModel and services
    private MilestoneManagementViewModel viewModel = new MilestoneManagementViewModel();
    private DialogService dialogService = ServiceFactory.getDialogService();
    private Project currentProject;

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing MilestoneManagementController");

        if (milestonesTable == null || nameColumn == null || dateColumn == null ||
                statusColumn == null || addMilestoneButton == null ||
                editMilestoneButton == null || deleteMilestoneButton == null || 
                refreshButton == null || filterComboBox == null) {

            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        // Set up table columns
        setupTableColumns();

        // Set up bindings
        setupBindings();

        // Set up table row double-click handler
        setupRowHandler();

        // Set up filter combo box
        setupFilterComboBox();
    }

    /**
     * Sets up the table columns.
     * Protected for testability.
     */
    protected void setupTableColumns() {
        if (milestonesTable == null || nameColumn == null || dateColumn == null || statusColumn == null) {
            LOGGER.warning("Table components not initialized - likely in test environment");
            return;
        }

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Set up custom status column
        statusColumn.setCellValueFactory(param -> {
            Milestone milestone = param.getValue();
            if (milestone == null) {
                return new SimpleStringProperty("");
            }
            
            if (milestone.isPassed()) {
                return new SimpleStringProperty("Passed");
            } else {
                long daysUntil = milestone.getDaysUntil();
                return new SimpleStringProperty(daysUntil + " days remaining");
            }
        });

        // Set up date formatter for date column
        dateColumn.setCellFactory(column -> new TableCell<Milestone, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                }
            }
        });
    }

    /**
     * Sets up the bindings between the view model and UI controls.
     * Protected for testability.
     */
    protected void setupBindings() {
        if (milestonesTable == null || addMilestoneButton == null || 
            editMilestoneButton == null || deleteMilestoneButton == null || 
            refreshButton == null) {

            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        // Bind table to view model milestones list
        milestonesTable.setItems(viewModel.getMilestones());

        // Bind buttons to commands
        ViewModelBinding.bindCommandButton(addMilestoneButton, viewModel.getAddMilestoneCommand());
        ViewModelBinding.bindCommandButton(editMilestoneButton, viewModel.getEditMilestoneCommand());
        ViewModelBinding.bindCommandButton(deleteMilestoneButton, viewModel.getDeleteMilestoneCommand());
        ViewModelBinding.bindCommandButton(refreshButton, viewModel.getRefreshCommand());

        // Bind selection changes
        if (milestonesTable.getSelectionModel() != null) {
            milestonesTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedMilestone(newVal));
        }

        // Set up error message binding
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorAlert("Error", newVal);
                viewModel.clearErrorMessage();
            }
        });
    }

    /**
     * Sets up the double-click handler for table rows.
     * Protected for testability.
     */
    protected void setupRowHandler() {
        if (milestonesTable == null) {
            LOGGER.warning("Table not initialized - likely in test environment");
            return;
        }

        milestonesTable.setRowFactory(tv -> {
            TableRow<Milestone> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Milestone milestone = row.getItem();
                    handleEditMilestone(milestone);
                }
            });
            return row;
        });
    }

    /**
     * Sets up the filter combo box.
     * Protected for testability.
     */
    protected void setupFilterComboBox() {
        if (filterComboBox == null) {
            LOGGER.warning("Filter combo box not initialized - likely in test environment");
            return;
        }

        // Set up filter options
        filterComboBox.getItems().clear();
        filterComboBox.getItems().addAll(MilestoneManagementViewModel.MilestoneFilter.values());
        filterComboBox.setValue(MilestoneManagementViewModel.MilestoneFilter.ALL);

        // Set up filter change listener
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.setFilter(newVal);
            }
        });
    }

    /**
     * Sets the current project for this controller.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        this.currentProject = project;
        viewModel.setProject(project);
        viewModel.loadMilestones();
    }

    /**
     * Handles showing the add milestone dialog.
     * This is called by the Add Milestone button.
     */
    @FXML
    public void handleAddMilestone() {
        try {
            showMilestoneDialog("New Milestone", null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading milestone dialog", e);
            showErrorAlert("Error", "Failed to open milestone dialog: " + e.getMessage());
        }
    }

    /**
     * Handles showing the edit milestone dialog.
     * This is called by the Edit Milestone button and table row double-click.
     * 
     * @param milestone the milestone to edit
     */
    protected void handleEditMilestone(Milestone milestone) {
        if (milestone == null) {
            return;
        }

        try {
            showMilestoneDialog("Edit Milestone", milestone);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading milestone dialog", e);
            showErrorAlert("Error", "Failed to open milestone dialog: " + e.getMessage());
        }
    }

    /**
     * Shows the milestone dialog for adding or editing a milestone.
     * Protected for testability.
     * 
     * @param title     the dialog title
     * @param milestone the milestone to edit, or null for a new milestone
     * @throws IOException if the FXML file cannot be loaded
     */
    protected void showMilestoneDialog(String title, Milestone milestone) throws IOException {
        // Load the milestone dialog
        FXMLLoader loader = createFXMLLoader("/fxml/MilestoneView.fxml");
        Parent dialogView = loader.load();

        // Create the dialog
        Stage dialogStage = createDialogStage(title, dialogView);

        // Get the controller
        MilestoneController controller = loader.getController();

        // Initialize the controller
        if (milestone == null) {
            controller.initNewMilestone(currentProject);
        } else {
            controller.initExistingMilestone(milestone);
        }

        // Show the dialog
        Optional<ButtonType> result = showAndWaitDialog(dialogStage);

        // Refresh the milestones list
        viewModel.loadMilestones();
    }

    /**
     * Creates an FXML loader for the specified FXML file.
     * Protected for testability.
     * 
     * @param fxmlFile the FXML file to load
     * @return the FXML loader
     */
    protected FXMLLoader createFXMLLoader(String fxmlFile) {
        return new FXMLLoader(getClass().getResource(fxmlFile));
    }

    /**
     * Creates a dialog stage for the specified title and content.
     * Protected for testability.
     * 
     * @param title   the dialog title
     * @param content the dialog content
     * @return the dialog stage
     */
    protected Stage createDialogStage(String title, Parent content) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.WINDOW_MODAL);

        if (milestonesTable != null && milestonesTable.getScene() != null &&
                milestonesTable.getScene().getWindow() != null) {
            dialogStage.initOwner(milestonesTable.getScene().getWindow());
        }

        dialogStage.setScene(new Scene(content));
        return dialogStage;
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
     * Shows a stage and waits for it to be closed.
     * Protected for testability.
     * 
     * @param stage the stage to show
     * @return an optional containing ButtonType.OK
     */
    protected Optional<ButtonType> showAndWaitDialog(Stage stage) {
        try {
            if (stage != null) {
                stage.showAndWait();
                return Optional.of(ButtonType.OK);
            }
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing dialog", e);
            return Optional.empty();
        }
    }

    /**
     * Gets the selected milestone from the table.
     * Protected for testability.
     * 
     * @return the selected milestone
     */
    protected Milestone getSelectedMilestone() {
        if (milestonesTable != null && milestonesTable.getSelectionModel() != null) {
            return milestonesTable.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }

    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public MilestoneManagementViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Sets the ViewModel.
     * This method is primarily used for testing to inject mock viewmodels.
     * 
     * @param viewModel the viewModel to use
     */
    public void setViewModel(MilestoneManagementViewModel viewModel) {
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