// src/main/java/org/frcpm/mvvm/views/SubteamListMvvmView.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

import org.frcpm.models.Subteam;
import org.frcpm.models.Project;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.SubteamDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.SubteamListMvvmViewModel;

/**
 * View for the subteam list using MVVMFx.
 */
public class SubteamListMvvmView implements FxmlView<SubteamListMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(SubteamListMvvmView.class.getName());
    
    @FXML
    private BorderPane mainPane;
    
    @FXML
    private Label projectLabel;
    
    @FXML
    private TableView<Subteam> subteamsTable;
    
    @FXML
    private TableColumn<Subteam, String> nameColumn;
    
    @FXML
    private TableColumn<Subteam, String> colorCodeColumn;
    
    @FXML
    private TableColumn<Subteam, String> specialtiesColumn;
    
    @FXML
    private TableColumn<Subteam, Integer> membersColumn;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @InjectViewModel
    private SubteamListMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing SubteamListMvvmView");
        this.resources = resources;
        
        // Initialize table columns
        setupTableColumns();
        
        // Set up table view
        subteamsTable.setItems(viewModel.getSubteams());
        
        // Bind selected subteam
        subteamsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedSubteam(newVal);
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
        subteamsTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Subteam> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Subteam subteam = row.getItem();
                    handleEditSubteam(subteam);
                }
            });
            return row;
        });
        
        // Bind command buttons using CommandAdapter
        CommandAdapter.bindCommandButton(addButton, viewModel.getNewSubteamCommand());
        CommandAdapter.bindCommandButton(editButton, viewModel.getEditSubteamCommand());
        CommandAdapter.bindCommandButton(deleteButton, viewModel.getDeleteSubteamCommand());
        CommandAdapter.bindCommandButton(refreshButton, viewModel.getRefreshSubteamsCommand());
        
        // Override button actions to handle dialogs
        addButton.setOnAction(e -> handleAddSubteam());
        editButton.setOnAction(e -> handleEditSubteam(viewModel.getSelectedSubteam()));
        
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
        colorCodeColumn.setCellValueFactory(new PropertyValueFactory<>("colorCode"));
        specialtiesColumn.setCellValueFactory(new PropertyValueFactory<>("specialties"));
        
        // Set up the members column with a count of team members
        membersColumn.setCellValueFactory(cellData -> {
            Subteam subteam = cellData.getValue();
            return javafx.beans.binding.Bindings.createObjectBinding(() -> {
                // Get the count inside the lambda, not outside
                int memberCount = 0;
                if (subteam != null && subteam.getMembers() != null) {
                    memberCount = subteam.getMembers().size();
                }
                return memberCount;
            });
        });
        
        // Set up custom cell factory for color code to display a colored box
        colorCodeColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Subteam, String>() {
            @Override
            protected void updateItem(String colorCode, boolean empty) {
                super.updateItem(colorCode, empty);
                
                if (empty || colorCode == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(colorCode);
                    
                    // Create a colored rectangle to display the color
                    javafx.scene.shape.Rectangle colorRect = new javafx.scene.shape.Rectangle(16, 16);
                    colorRect.setFill(javafx.scene.paint.Color.web(colorCode));
                    colorRect.setStroke(javafx.scene.paint.Color.BLACK);
                    
                    // Wrap in an HBox for layout
                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5);
                    hbox.getChildren().addAll(colorRect, new Label(colorCode));
                    setGraphic(hbox);
                }
            }
        });
    }
    
    /**
     * Sets the project for the subteam list.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        viewModel.setCurrentProject(project);
        viewModel.getLoadSubteamsCommand().execute();
    }
    
    /**
     * Handle add subteam button click.
     */
    private void handleAddSubteam() {
        try {
            // Show subteam dialog
            openSubteamDetailDialog(new Subteam(), true);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating new subteam", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.subteam.create.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle edit subteam button click.
     */
    private void handleEditSubteam(Subteam subteam) {
        if (subteam == null) {
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.subteam.select"));
            return;
        }
        
        try {
            // Show subteam dialog
            openSubteamDetailDialog(subteam, false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing subteam", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.subteam.edit.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Opens a subteam detail dialog.
     * 
     * @param subteam the subteam to edit or create
     * @param isNew true if creating a new subteam, false if editing
     */
    private void openSubteamDetailDialog(Subteam subteam, boolean isNew) {
        try {
            // Load the subteam detail view
            ViewTuple<SubteamDetailMvvmView, SubteamDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(SubteamDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            SubteamDetailMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize the controller with the subteam
            if (isNew) {
                viewController.initNewSubteam();
            } else {
                viewController.initExistingSubteam(subteam);
            }
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(isNew ? resources.getString("subteam.new.title") : 
                                        resources.getString("subteam.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainPane.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh subteams after dialog closes
            viewModel.getRefreshSubteamsCommand().execute();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening subteam dialog", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.subteam.dialog.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle delete subteam button click.
     */
    @FXML
    private void onDeleteSubteamAction() {
        if (viewModel.getSelectedSubteam() == null) {
            // Show alert about no selection
            showErrorAlert(resources.getString("error.title"),
                           resources.getString("info.no.selection.subteam"));
            return;
        }
        
        // Confirm deletion
        String confirmMessage = resources.getString("subteam.delete.confirm") + 
            " '" + viewModel.getSelectedSubteam().getName() + "'?";
        
        if (showConfirmationAlert(resources.getString("confirm.title"), confirmMessage)) {
            // Execute delete command
            viewModel.getDeleteSubteamCommand().execute();
        }
    }
    
    /**
     * Handle refresh button click.
     */
    @FXML
    private void onRefreshAction() {
        viewModel.getRefreshSubteamsCommand().execute();
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