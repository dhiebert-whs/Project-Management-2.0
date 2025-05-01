package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.di.DialogFactory;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.MilestoneService;
import org.frcpm.viewmodels.MilestoneManagementViewModel;
import org.frcpm.views.MilestoneView;

import javax.inject.Inject;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for milestone management using AfterburnerFX pattern.
 */
public class MilestoneManagementPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(MilestoneManagementPresenter.class.getName());

    // FXML UI components - Table
    @FXML
    private TableView<Milestone> milestonesTable;

    @FXML
    private TableColumn<Milestone, String> nameColumn;

    @FXML
    private TableColumn<Milestone, LocalDate> dateColumn;

    @FXML
    private TableColumn<Milestone, String> statusColumn;

    @FXML
    private TableColumn<Milestone, String> descriptionColumn;

    // FXML UI components - Controls
    @FXML
    private Button addMilestoneButton;

    @FXML
    private Button editMilestoneButton;

    @FXML
    private Button deleteMilestoneButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button closeButton;

    @FXML
    private ComboBox<MilestoneManagementViewModel.MilestoneFilter> filterComboBox;

    @FXML
    private Label projectNameLabel;

    @FXML
    private VBox detailsPane;

    @FXML
    private Label selectedMilestoneNameLabel;

    @FXML
    private Label selectedMilestoneDateLabel;

    @FXML
    private TextArea selectedMilestoneDescriptionArea;

    // Injected services
    @Inject
    private MilestoneService milestoneService;
    
    @Inject
    private DialogService dialogService;

    // ViewModel and resources
    private MilestoneManagementViewModel viewModel;
    private ResourceBundle resources;
    private Project currentProject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing MilestoneManagementPresenter with resource bundle");
        
        this.resources = resources;
        
        // Create view model with injected services
        viewModel = new MilestoneManagementViewModel(milestoneService);

        // Setup table columns
        setupTableColumns();
        
        // Setup filters
        setupFilters();
        
        // Setup bindings
        setupBindings();
        
        // Setup error handling
        setupErrorHandling();
    }

    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        // Check for null UI components for testability
        if (milestonesTable == null || nameColumn == null || dateColumn == null || 
            statusColumn == null || descriptionColumn == null) {
            LOGGER.warning("Table components not initialized - likely in test environment");
            return;
        }

        // Setup name column
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // Setup date column with formatter
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        dateColumn.setCellFactory(column -> new TableCell<Milestone, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });
        
        // Setup status column
        statusColumn.setCellValueFactory(cellData -> {
            Milestone milestone = cellData.getValue();
            LocalDate milestoneDate = milestone.getDate();
            LocalDate today = LocalDate.now();
            
            if (milestoneDate.isBefore(today)) {
                return new javafx.beans.property.SimpleStringProperty("Passed");
            } else if (milestoneDate.isEqual(today)) {
                return new javafx.beans.property.SimpleStringProperty("Today");
            } else {
                return new javafx.beans.property.SimpleStringProperty("Upcoming");
            }
        });
        
        // Setup description column
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Setup row double-click handler for editing
        milestonesTable.setRowFactory(tv -> {
            TableRow<Milestone> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditMilestone(row.getItem());
                }
            });
            return row;
        });
    }
    
    /**
     * Sets up the filter combo box.
     */
    private void setupFilters() {
        if (filterComboBox == null) {
            LOGGER.warning("Filter combo box not initialized - likely in test environment");
            return;
        }

        // Setup filter options
        filterComboBox.getItems().clear();
        filterComboBox.getItems().addAll(MilestoneManagementViewModel.MilestoneFilter.values());
        filterComboBox.setValue(MilestoneManagementViewModel.MilestoneFilter.ALL);
        
        // Set up filter change listener
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.setFilter(newVal);
                viewModel.applyFilter();
            }
        });
    }
    
    /**
     * Sets up bindings between UI controls and ViewModel properties.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (milestonesTable == null || addMilestoneButton == null || editMilestoneButton == null || 
            deleteMilestoneButton == null || refreshButton == null || closeButton == null || 
            projectNameLabel == null || detailsPane == null || selectedMilestoneNameLabel == null || 
            selectedMilestoneDateLabel == null || selectedMilestoneDescriptionArea == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind project name
            projectNameLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                    () -> viewModel.getProject() != null ? viewModel.getProject().getName() : "",
                    viewModel.projectProperty()
                )
            );
            
            // Bind milestones table
            milestonesTable.setItems(viewModel.getFilteredMilestones());
            
            // Bind buttons to commands
            ViewModelBinding.bindCommandButton(addMilestoneButton, viewModel.getAddMilestoneCommand());
            ViewModelBinding.bindCommandButton(editMilestoneButton, viewModel.getEditMilestoneCommand());
            ViewModelBinding.bindCommandButton(deleteMilestoneButton, viewModel.getDeleteMilestoneCommand());
            ViewModelBinding.bindCommandButton(refreshButton, viewModel.getRefreshCommand());
            
            // Manual button actions
            addMilestoneButton.setOnAction(event -> handleAddMilestone());
            editMilestoneButton.setOnAction(event -> {
                Milestone selectedMilestone = milestonesTable.getSelectionModel().getSelectedItem();
                if (selectedMilestone != null) {
                    handleEditMilestone(selectedMilestone);
                } else {
                    showInfoAlert("No Selection", "Please select a milestone to edit");
                }
            });
            deleteMilestoneButton.setOnAction(event -> handleDeleteMilestone());
            refreshButton.setOnAction(event -> refreshMilestones());
            closeButton.setOnAction(event -> handleClose());
            
            // Bind selected milestone to selection
            milestonesTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> {
                        viewModel.setSelectedMilestone(newVal);
                        updateDetailsPane();
                    });
            
            // Initial state of details pane
            detailsPane.setVisible(false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            showErrorAlert("Setup Error", "Failed to initialize bindings: " + e.getMessage());
        }
    }
    
    /**
     * Sets up error message handling.
     */
    private void setupErrorHandling() {
        if (viewModel == null) {
            LOGGER.warning("ViewModel not initialized - likely in test environment");
            return;
        }
        
        // Show an alert when error message changes
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorAlert("Error", newVal);
                viewModel.clearErrorMessage();
            }
        });
    }
    
    /**
     * Updates the details pane with selected milestone info.
     */
    private void updateDetailsPane() {
        if (detailsPane == null || selectedMilestoneNameLabel == null || 
            selectedMilestoneDateLabel == null || selectedMilestoneDescriptionArea == null) {
            return;
        }

        Milestone milestone = viewModel.getSelectedMilestone();
        if (milestone != null) {
            selectedMilestoneNameLabel.setText(milestone.getName());
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            selectedMilestoneDateLabel.setText(milestone.getDate().format(dateFormatter));
            
            selectedMilestoneDescriptionArea.setText(milestone.getDescription());
            
            detailsPane.setVisible(true);
        } else {
            detailsPane.setVisible(false);
        }
    }
    
    /**
     * Sets the current project and loads milestones.
     * 
     * @param project the current project
     */
    public void setProject(Project project) {
        if (project == null) {
            LOGGER.warning("Cannot set null project");
            return;
        }
        
        this.currentProject = project;
        viewModel.setProject(project);
        refreshMilestones();
    }
    
    /**
     * Refreshes the milestones data.
     */
    private void refreshMilestones() {
        viewModel.loadMilestones();
        viewModel.applyFilter();
    }
    
    /**
     * Handles adding a new milestone.
     */
    private void handleAddMilestone() {
        if (currentProject == null) {
            showErrorAlert("Error", "No project selected");
            return;
        }
        
        try {
            // Use ViewLoader for dialog creation (consistent with other parts of the app)
            MilestonePresenter presenter = ViewLoader.showDialog(
                    MilestoneView.class, 
                    resources.getString("milestone.new.title"), 
                    getWindow());
            
            // Initialize presenter with new milestone
            if (presenter != null) {
                presenter.initNewMilestone(currentProject);
                
                // Refresh milestones after dialog closes
                refreshMilestones();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding milestone", e);
            showErrorAlert("Error", "Failed to open milestone dialog: " + e.getMessage());
        }
    }
    
    /**
     * Handles editing an existing milestone.
     * 
     * @param milestone the milestone to edit
     */
    private void handleEditMilestone(Milestone milestone) {
        if (milestone == null) {
            return;
        }
        
        try {
            // Use ViewLoader for dialog creation
            MilestonePresenter presenter = ViewLoader.showDialog(
                    MilestoneView.class, 
                    resources.getString("milestone.edit.title"), 
                    getWindow());
            
            // Initialize presenter with existing milestone
            if (presenter != null) {
                presenter.initExistingMilestone(milestone);
                
                // Refresh milestones after dialog closes
                refreshMilestones();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing milestone", e);
            showErrorAlert("Error", "Failed to open milestone dialog: " + e.getMessage());
        }
    }

    /**
     * Handles the Edit button click event.
     */
    @FXML
    private void handleEditMilestone() {
        Milestone selectedMilestone = milestonesTable.getSelectionModel().getSelectedItem();
        if (selectedMilestone != null) {
            handleEditMilestone(selectedMilestone);
        } else {
            showInfoAlert("No Selection", "Please select a milestone to edit");
        }
    }
    
    /**
     * Handles deleting the selected milestone.
     */
    private void handleDeleteMilestone() {
        Milestone selectedMilestone = milestonesTable.getSelectionModel().getSelectedItem();
        if (selectedMilestone == null) {
            showInfoAlert("No Selection", "Please select a milestone to delete");
            return;
        }
        
        boolean confirmed = showConfirmationAlert(
                "Delete Milestone", 
                "Are you sure you want to delete the milestone '" + selectedMilestone.getName() + "'?");
        
        if (confirmed) {
            boolean success = viewModel.deleteMilestone(selectedMilestone);
            if (success) {
                showInfoAlert("Milestone Deleted", "Milestone was successfully deleted");
                refreshMilestones();
            }
        }
    }
    
    /**
     * Handles closing the window.
     */
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
        if (milestonesTable != null && milestonesTable.getScene() != null) {
            return milestonesTable.getScene().getWindow();
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
    public MilestoneManagementViewModel getViewModel() {
        return viewModel;
    }
}