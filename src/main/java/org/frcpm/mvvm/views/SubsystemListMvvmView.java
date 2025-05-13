// src/main/java/org/frcpm/mvvm/views/SubsystemListMvvmView.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Subsystem;
import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.SubsystemDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.SubsystemListMvvmViewModel;
import org.frcpm.mvvm.viewmodels.SubsystemListMvvmViewModel.SubsystemFilter;

/**
 * View for the subsystem list using MVVMFx.
 */
public class SubsystemListMvvmView implements FxmlView<SubsystemListMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(SubsystemListMvvmView.class.getName());
    
    @FXML
    private BorderPane mainPane;
    
    @FXML
    private Label projectLabel;
    
    @FXML
    private TableView<Subsystem> subsystemsTable;
    
    @FXML
    private TableColumn<Subsystem, String> nameColumn;
    
    @FXML
    private TableColumn<Subsystem, Subsystem.Status> statusColumn;
    
    @FXML
    private TableColumn<Subsystem, Subteam> subteamColumn;
    
    @FXML
    private TableColumn<Subsystem, Integer> tasksColumn;
    
    @FXML
    private TableColumn<Subsystem, Double> completionColumn;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private ComboBox<SubsystemFilter> filterComboBox;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @InjectViewModel
    private SubsystemListMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing SubsystemListMvvmView");
        this.resources = resources;
        
        // Initialize table columns
        setupTableColumns();
        
        // Set up filtered list
        setupFilterComboBox();
        
        // Set up table view
        subsystemsTable.setItems(viewModel.getSubsystems());
        
        // Bind selected subsystem
        subsystemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedSubsystem(newVal);
        });
        
        // Bind project label
        viewModel.currentProjectProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                projectLabel.setText(newVal.getName());
            } else {
                projectLabel.setText("");
            }
        });
        
        // Set up row double-click handler
        subsystemsTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Subsystem> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Subsystem subsystem = row.getItem();
                    handleEditSubsystem(subsystem);
                }
            });
            return row;
        });
        
        // Bind command buttons using CommandAdapter
        CommandAdapter.bindCommandButton(addButton, viewModel.getNewSubsystemCommand());
        CommandAdapter.bindCommandButton(editButton, viewModel.getEditSubsystemCommand());
        CommandAdapter.bindCommandButton(deleteButton, viewModel.getDeleteSubsystemCommand());
        CommandAdapter.bindCommandButton(refreshButton, viewModel.getRefreshSubsystemsCommand());
        
        // Override button actions to handle dialogs
        addButton.setOnAction(e -> handleAddSubsystem());
        editButton.setOnAction(e -> handleEditSubsystem(viewModel.getSelectedSubsystem()));
        
        // Bind error message
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        
        // Bind loading indicator
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // Set up the status column
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Subsystem, Subsystem.Status>() {
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
        
        // Set up the subteam column
        subteamColumn.setCellValueFactory(cellData -> {
            Subsystem subsystem = cellData.getValue();
            if (subsystem.getResponsibleSubteam() != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(subsystem.getResponsibleSubteam());
            } else {
                return new javafx.beans.property.SimpleObjectProperty<>(null);
            }
        });
        subteamColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Subsystem, Subteam>() {
            @Override
            protected void updateItem(Subteam subteam, boolean empty) {
                super.updateItem(subteam, empty);
                if (empty || subteam == null) {
                    setText("None");
                } else {
                    setText(subteam.getName());
                }
            }
        });
        
        // Set up the tasks column
        tasksColumn.setCellValueFactory(cellData -> {
            Subsystem subsystem = cellData.getValue();
            int taskCount = viewModel.getTaskCount(subsystem);
            return new javafx.beans.property.SimpleObjectProperty<>(taskCount);
        });
        
        // Set up the completion column
        completionColumn.setCellValueFactory(cellData -> {
            Subsystem subsystem = cellData.getValue();
            double percentage = viewModel.getCompletionPercentage(subsystem);
            return new javafx.beans.property.SimpleObjectProperty<>(percentage);
        });
        completionColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Subsystem, Double>() {
            private final ProgressBar progressBar = new ProgressBar();
            
            {
                progressBar.setPrefWidth(80);
            }
            
            @Override
            protected void updateItem(Double percentage, boolean empty) {
                super.updateItem(percentage, empty);
                if (empty || percentage == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    progressBar.setProgress(percentage / 100.0);
                    setText(String.format("%.1f%%", percentage));
                    setGraphic(progressBar);
                }
            }
        });
    }
    
    /**
     * Sets up the filter combo box.
     */
    private void setupFilterComboBox() {
        // Add filter options
        filterComboBox.getItems().clear();
        filterComboBox.getItems().addAll(SubsystemFilter.values());
        filterComboBox.setValue(SubsystemFilter.ALL);
        
        // Set up listener
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.setFilter(newVal);
            }
        });
    }
    
    /**
     * Sets the project for the subsystem list.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        viewModel.setCurrentProject(project);
        viewModel.getLoadSubsystemsCommand().execute();
    }
    
    /**
     * Handle add subsystem button click.
     */
    private void handleAddSubsystem() {
        try {
            // Show subsystem dialog
            openSubsystemDetailDialog(new Subsystem(), true);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating new subsystem", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.subsystem.create.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle edit subsystem button click.
     */
    private void handleEditSubsystem(Subsystem subsystem) {
        if (subsystem == null) {
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.subsystem.select"));
            return;
        }
        
        try {
            // Show subsystem dialog
            openSubsystemDetailDialog(subsystem, false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing subsystem", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.subsystem.edit.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Opens a subsystem detail dialog.
     * 
     * @param subsystem the subsystem to edit or create
     * @param isNew true if creating a new subsystem, false if editing
     */
    private void openSubsystemDetailDialog(Subsystem subsystem, boolean isNew) {
        try {
            // Load the subsystem detail view
            ViewTuple<SubsystemDetailMvvmView, SubsystemDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(SubsystemDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            SubsystemDetailMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize the controller with the subsystem
            if (isNew) {
                viewController.initNewSubsystem();
            } else {
                viewController.initExistingSubsystem(subsystem);
            }
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(isNew ? resources.getString("subsystem.new.title") : 
                                        resources.getString("subsystem.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainPane.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh subsystems after dialog closes
            viewModel.getRefreshSubsystemsCommand().execute();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening subsystem dialog", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.subsystem.dialog.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle delete subsystem button click.
     */
    @FXML
    private void onDeleteSubsystemAction() {
        if (viewModel.getSelectedSubsystem() == null) {
            // Show alert about no selection
            showErrorAlert(resources.getString("error.title"),
                           resources.getString("info.no.selection.subsystem"));
            return;
        }
        
        // Confirm deletion
        String confirmMessage = resources.getString("subsystem.delete.confirm") + 
            " '" + viewModel.getSelectedSubsystem().getName() + "'?";
        
        if (showConfirmationAlert(resources.getString("confirm.title"), confirmMessage)) {
            // Execute delete command
            viewModel.getDeleteSubsystemCommand().execute();
        }
    }
    
    /**
     * Handle refresh button click.
     */
    @FXML
    private void onRefreshAction() {
        viewModel.getRefreshSubsystemsCommand().execute();
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