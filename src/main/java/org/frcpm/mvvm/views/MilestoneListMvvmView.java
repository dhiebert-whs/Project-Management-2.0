// src/main/java/org/frcpm/mvvm/views/MilestoneListMvvmView.java

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
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.MilestoneDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MilestoneListMvvmViewModel;

/**
 * View for the milestone list using MVVMFx.
 */
public class MilestoneListMvvmView implements FxmlView<MilestoneListMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(MilestoneListMvvmView.class.getName());
    
    @FXML
    private BorderPane mainPane;
    
    @FXML
    private Label projectNameLabel;
    
    @FXML
    private ComboBox<MilestoneListMvvmViewModel.MilestoneFilter> filterComboBox;
    
    @FXML
    private TableView<Milestone> milestoneTableView;
    
    @FXML
    private TableColumn<Milestone, String> nameColumn;
    
    @FXML
    private TableColumn<Milestone, LocalDate> dateColumn;
    
    @FXML
    private TableColumn<Milestone, String> statusColumn;
    
    @FXML
    private TableColumn<Milestone, String> descriptionColumn;
    
    @FXML
    private Button newMilestoneButton;
    
    @FXML
    private Button editMilestoneButton;
    
    @FXML
    private Button deleteMilestoneButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @InjectViewModel
    private MilestoneListMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing MilestoneListMvvmView");
        this.resources = resources;
        
        // Initialize table columns
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
        
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Set up milestone table view
        milestoneTableView.setItems(viewModel.getFilteredMilestones());
        
        // Set up filter dropdown
        filterComboBox.getItems().addAll(MilestoneListMvvmViewModel.MilestoneFilter.values());
        filterComboBox.setValue(MilestoneListMvvmViewModel.MilestoneFilter.ALL);
        
        // Bind filter to view model
        filterComboBox.valueProperty().bindBidirectional(viewModel.currentFilterProperty());
        
        // Bind selected milestone
        milestoneTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedMilestone(newVal);
        });
        
        // Bind project name label
        viewModel.currentProjectProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                projectNameLabel.setText(newVal.getName());
            } else {
                projectNameLabel.setText("");
            }
        });
        
        // Bind command buttons using CommandAdapter
        CommandAdapter.bindCommandButton(newMilestoneButton, viewModel.getNewMilestoneCommand());
        CommandAdapter.bindCommandButton(editMilestoneButton, viewModel.getEditMilestoneCommand());
        CommandAdapter.bindCommandButton(deleteMilestoneButton, viewModel.getDeleteMilestoneCommand());
        CommandAdapter.bindCommandButton(refreshButton, viewModel.getRefreshMilestonesCommand());
        
        // Override button actions to handle dialogs
        newMilestoneButton.setOnAction(e -> handleNewMilestone());
        editMilestoneButton.setOnAction(e -> handleEditMilestone());
        
        // Bind error message
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        
        // Bind loading indicator
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
        
        // Setup double-click for editing
        milestoneTableView.setRowFactory(tv -> {
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
     * Sets the project for the milestone list.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        viewModel.setCurrentProject(project);
        viewModel.getLoadMilestonesCommand().execute();
    }
    
    /**
     * Handle new milestone button click.
     */
    private void handleNewMilestone() {
        Project project = viewModel.getCurrentProject();
        if (project == null) {
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.project.required"));
            return;
        }
        
        try {
            // Create a new milestone and show dialog
            openMilestoneDetailDialog(project, null, true);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating new milestone", e);
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.milestone.create.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle edit milestone button click.
     */
    private void handleEditMilestone() {
        Milestone selectedMilestone = viewModel.getSelectedMilestone();
        if (selectedMilestone == null) {
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.milestone.select"));
            return;
        }
        
        handleEditMilestone(selectedMilestone);
    }
    
    /**
     * Handle edit milestone.
     * 
     * @param milestone the milestone to edit
     */
    private void handleEditMilestone(Milestone milestone) {
        try {
            // Show milestone dialog for editing
            openMilestoneDetailDialog(viewModel.getCurrentProject(), milestone, false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing milestone", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.milestone.edit.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Opens a milestone detail dialog.
     * 
     * @param project the project for the milestone
     * @param milestone the milestone to edit (null for new milestone)
     * @param isNew true if creating a new milestone, false if editing
     */
    private void openMilestoneDetailDialog(Project project, Milestone milestone, boolean isNew) {
        try {
            // Load the milestone detail view
            ViewTuple<MilestoneDetailMvvmView, MilestoneDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(MilestoneDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller (code behind) and view model
            MilestoneDetailMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize the controller with the milestone or project
            if (isNew) {
                viewController.initNewMilestone(project);
            } else {
                viewController.initExistingMilestone(milestone);
            }
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(isNew ? resources.getString("milestone.new.title") : 
                                        resources.getString("milestone.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainPane.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh milestones after dialog closes
            viewModel.getRefreshMilestonesCommand().execute();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening milestone dialog", e);
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.milestone.dialog.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle delete milestone button click.
     */
    @FXML
    private void onDeleteMilestoneAction() {
        if (viewModel.getSelectedMilestone() == null) {
            // Show alert about no selection
            showErrorAlert(resources.getString("error.title"),
                           resources.getString("info.no.selection.milestone"));
            return;
        }
        
        // Confirm deletion
        String confirmMessage = resources.getString("milestone.delete.confirm") + 
            " '" + viewModel.getSelectedMilestone().getName() + "'?";
        
        if (showConfirmationAlert(resources.getString("confirm.title"), confirmMessage)) {
            // Execute delete command
            viewModel.getDeleteMilestoneCommand().execute();
        }
    }
    
    /**
     * Handle refresh button click.
     */
    @FXML
    private void onRefreshAction() {
        viewModel.getRefreshMilestonesCommand().execute();
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
     * Shows a confirmation alert dialog.
     * 
     * @param title the title
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    private boolean showConfirmationAlert(String title, String message) {
        // This would use an AlertHelper or DialogService in a full implementation
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        return alert.showAndWait()
            .filter(response -> response == javafx.scene.control.ButtonType.OK)
            .isPresent();
    }
}