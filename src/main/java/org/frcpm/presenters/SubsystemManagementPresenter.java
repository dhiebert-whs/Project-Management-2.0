package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Window;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Subsystem;
import org.frcpm.services.DialogService;
import org.frcpm.services.SubsystemService;
import org.frcpm.viewmodels.SubsystemManagementViewModel;
import org.frcpm.views.SubsystemView;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for subsystem management view using AfterburnerFX pattern.
 */
public class SubsystemManagementPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(SubsystemManagementPresenter.class.getName());

    // FXML controls
    @FXML
    private TableView<Subsystem> subsystemsTable;

    @FXML
    private TableColumn<Subsystem, String> nameColumn;

    @FXML
    private TableColumn<Subsystem, Subsystem.Status> statusColumn;

    @FXML
    private TableColumn<Subsystem, String> subteamColumn;

    @FXML
    private TableColumn<Subsystem, Integer> tasksColumn;

    @FXML
    private TableColumn<Subsystem, Double> completionColumn;

    @FXML
    private Button addSubsystemButton;

    @FXML
    private Button editSubsystemButton;

    @FXML
    private Button deleteSubsystemButton;

    @FXML
    private Button closeButton;

    // Injected services
    @Inject
    private SubsystemService subsystemService;
    
    @Inject
    private DialogService dialogService;

    // ViewModel and resources
    private SubsystemManagementViewModel viewModel;
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing SubsystemManagementPresenter with resource bundle");
        
        this.resources = resources;
        
        // Create ViewModel with injected service
        viewModel = new SubsystemManagementViewModel(subsystemService);

        // Set up table columns
        setupTableColumns();
        
        // Set up bindings
        setupBindings();
        
        // Set up row double-click handler
        setupRowHandler();
        
        // Load subsystems
        loadSubsystems();
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        if (subsystemsTable == null || nameColumn == null || statusColumn == null ||
                subteamColumn == null || tasksColumn == null || completionColumn == null) {
            
            LOGGER.warning("Table components not initialized - likely in test environment");
            return;
        }

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        statusColumn.setCellFactory(column -> new TableCell<Subsystem, Subsystem.Status>() {
            @Override
            protected void updateItem(Subsystem.Status status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                } else {
                    setText(status.getDisplayName());
                }
            }
        });
        
        // Set up subteam column
        subteamColumn.setCellValueFactory(cellData -> {
            Subsystem subsystem = cellData.getValue();
            if (subsystem.getResponsibleSubteam() != null) {
                return new javafx.beans.property.SimpleStringProperty(subsystem.getResponsibleSubteam().getName());
            } else {
                return new javafx.beans.property.SimpleStringProperty("None");
            }
        });
        
        // Set up tasks column
        tasksColumn.setCellValueFactory(cellData -> {
            Subsystem subsystem = cellData.getValue();
            int taskCount = viewModel.getTaskCount(subsystem);
            return new javafx.beans.property.SimpleObjectProperty<>(taskCount);
        });
        
        // Set up completion column
        completionColumn.setCellValueFactory(cellData -> {
            Subsystem subsystem = cellData.getValue();
            double percentage = viewModel.getCompletionPercentage(subsystem);
            return new javafx.beans.property.SimpleObjectProperty<>(percentage);
        });
        
        completionColumn.setCellFactory(column -> new TableCell<Subsystem, Double>() {
            @Override
            protected void updateItem(Double percentage, boolean empty) {
                super.updateItem(percentage, empty);
                if (empty || percentage == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ProgressBar progressBar = new ProgressBar(percentage / 100.0);
                    progressBar.setPrefWidth(80);
                    setText(String.format("%.1f%%", percentage));
                    setGraphic(progressBar);
                }
            }
        });
    }
    
    /**
     * Sets up the bindings between the view model and UI controls.
     */
    private void setupBindings() {
        if (subsystemsTable == null || addSubsystemButton == null || 
            editSubsystemButton == null || deleteSubsystemButton == null || closeButton == null) {
            
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        // Bind table to view model subsystems list
        subsystemsTable.setItems(viewModel.getSubsystems());

        // Bind buttons to commands
        ViewModelBinding.bindCommandButton(addSubsystemButton, viewModel.getAddSubsystemCommand());
        ViewModelBinding.bindCommandButton(editSubsystemButton, viewModel.getEditSubsystemCommand());
        ViewModelBinding.bindCommandButton(deleteSubsystemButton, viewModel.getDeleteSubsystemCommand());

        // Set up explicit handlers
        addSubsystemButton.setOnAction(event -> handleAddSubsystem());
        editSubsystemButton.setOnAction(event -> handleEditSubsystem());
        deleteSubsystemButton.setOnAction(event -> handleDeleteSubsystem());
        closeButton.setOnAction(event -> handleClose());
        
        // Bind selection changes
        if (subsystemsTable.getSelectionModel() != null) {
            subsystemsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedSubsystem(newVal));
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
     */
    private void setupRowHandler() {
        if (subsystemsTable == null) {
            LOGGER.warning("Table not initialized - likely in test environment");
            return;
        }

        subsystemsTable.setRowFactory(tv -> {
            TableRow<Subsystem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Subsystem subsystem = row.getItem();
                    handleEditSubsystem(subsystem);
                }
            });
            return row;
        });
    }
    
    /**
     * Loads subsystems from the database.
     */
    private void loadSubsystems() {
        viewModel.loadSubsystems();
    }
    
    /**
     * Handles adding a new subsystem.
     */
    @FXML
    private void handleAddSubsystem() {
        try {
            // Use ViewLoader for AfterburnerFX integration
            SubsystemPresenter presenter = ViewLoader.showDialog(SubsystemView.class, 
                    resources.getString("subsystem.new.title"), 
                    getWindow());
            
            if (presenter != null) {
                presenter.initNewSubsystem();
                
                // Refresh the subsystems list after dialog closes
                viewModel.loadSubsystems();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystem dialog", e);
            showErrorAlert("Error", "Failed to open subsystem dialog: " + e.getMessage());
        }
    }

    /**
     * Handles editing the selected subsystem.
     */
    @FXML
    private void handleEditSubsystem() {
        Subsystem selectedSubsystem = viewModel.getSelectedSubsystem();
        if (selectedSubsystem != null) {
            handleEditSubsystem(selectedSubsystem);
        } else {
            showInfoAlert("No Selection", "Please select a subsystem to edit");
        }
    }
    
    /**
     * Handles editing a specific subsystem.
     * 
     * @param subsystem the subsystem to edit
     */
    private void handleEditSubsystem(Subsystem subsystem) {
        if (subsystem == null) {
            return;
        }

        try {
            // Use ViewLoader for AfterburnerFX integration
            SubsystemPresenter presenter = ViewLoader.showDialog(SubsystemView.class, 
                    resources.getString("subsystem.edit.title"), 
                    getWindow());
            
            if (presenter != null) {
                presenter.initExistingSubsystem(subsystem);
                
                // Refresh the subsystems list after dialog closes
                viewModel.loadSubsystems();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystem dialog", e);
            showErrorAlert("Error", "Failed to open subsystem dialog: " + e.getMessage());
        }
    }
    
    /**
     * Handles deleting the selected subsystem.
     */
    @FXML
    private void handleDeleteSubsystem() {
        Subsystem selectedSubsystem = viewModel.getSelectedSubsystem();
        if (selectedSubsystem == null) {
            showInfoAlert("No Selection", "Please select a subsystem to delete");
            return;
        }
        
        boolean confirmed = showConfirmationAlert(
                "Delete Subsystem", 
                "Are you sure you want to delete the subsystem '" + selectedSubsystem.getName() + "'?");
        
        if (confirmed) {
            boolean success = viewModel.deleteSubsystem(selectedSubsystem);
            if (success) {
                showInfoAlert("Subsystem Deleted", "Subsystem was successfully deleted");
                viewModel.loadSubsystems();
            }
        }
    }
    
    /**
     * Handles closing the window.
     */
    @FXML
    private void handleClose() {
        try {
            if (closeButton != null && closeButton.getScene() != null && 
                closeButton.getScene().getWindow() != null) {
                
                closeButton.getScene().getWindow().hide();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing window", e);
        }
    }
    
    /**
     * Gets the window for this presenter.
     * 
     * @return the window, or null if not available
     */
    private Window getWindow() {
        if (subsystemsTable != null && subsystemsTable.getScene() != null) {
            return subsystemsTable.getScene().getWindow();
        }
        return null;
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
     * Shows a confirmation alert dialog.
     * 
     * @param title the title
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    private boolean showConfirmationAlert(String title, String message) {
        try {
            return dialogService.showConfirmationAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Confirmation would show: {0} - {1}", new Object[] { title, message });
            return false;
        }
    }
    
    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public SubsystemManagementViewModel getViewModel() {
        return viewModel;
    }
}