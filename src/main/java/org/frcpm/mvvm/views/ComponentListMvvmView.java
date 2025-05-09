// src/main/java/org/frcpm/mvvm/views/ComponentListMvvmView.java
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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.ComponentDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.ComponentListMvvmViewModel;
import org.frcpm.mvvm.viewmodels.ComponentListMvvmViewModel.ComponentFilter;

/**
 * View for the component list using MVVMFx.
 */
public class ComponentListMvvmView implements FxmlView<ComponentListMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(ComponentListMvvmView.class.getName());
    
    @FXML
    private BorderPane mainPane;
    
    @FXML
    private Label projectLabel;
    
    @FXML
    private TableView<Component> componentsTable;
    
    @FXML
    private TableColumn<Component, String> nameColumn;
    
    @FXML
    private TableColumn<Component, String> partNumberColumn;
    
    @FXML
    private TableColumn<Component, LocalDate> expectedDeliveryColumn;
    
    @FXML
    private TableColumn<Component, Boolean> deliveredColumn;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private ComboBox<ComponentFilter> filterComboBox;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @InjectViewModel
    private ComponentListMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing ComponentListMvvmView");
        this.resources = resources;
        
        // Initialize table columns
        setupTableColumns();
        
        // Set up filtered list
        setupFilterComboBox();
        
        // Set up table view
        componentsTable.setItems(viewModel.getComponents());
        
        // Bind selected component
        componentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedComponent(newVal);
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
        componentsTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Component> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Component component = row.getItem();
                    handleEditComponent(component);
                }
            });
            return row;
        });
        
        // Bind command buttons using CommandAdapter
        CommandAdapter.bindCommandButton(addButton, viewModel.getNewComponentCommand());
        CommandAdapter.bindCommandButton(editButton, viewModel.getEditComponentCommand());
        CommandAdapter.bindCommandButton(deleteButton, viewModel.getDeleteComponentCommand());
        CommandAdapter.bindCommandButton(refreshButton, viewModel.getRefreshComponentsCommand());
        
        // Override button actions to handle dialogs
        addButton.setOnAction(e -> handleAddComponent());
        editButton.setOnAction(e -> handleEditComponent(viewModel.getSelectedComponent()));
        
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
        partNumberColumn.setCellValueFactory(new PropertyValueFactory<>("partNumber"));
        
        // Set up the date formatter
        expectedDeliveryColumn.setCellValueFactory(new PropertyValueFactory<>("expectedDelivery"));
        expectedDeliveryColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Component, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });
        
        // Set up the delivered column with checkboxes
        deliveredColumn.setCellValueFactory(new PropertyValueFactory<>("delivered"));
        deliveredColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Component, Boolean>() {
            private final javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox();
            
            {
                // Disable editing in the table view
                checkBox.setDisable(true);
            }
            
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
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
        filterComboBox.getItems().addAll(ComponentFilter.values());
        filterComboBox.setValue(ComponentFilter.ALL);
        
        // Set up listener
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.setFilter(newVal);
            }
        });
    }
    
    /**
     * Sets the project for the component list.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        viewModel.setCurrentProject(project);
        viewModel.getLoadComponentsCommand().execute();
    }
    
    /**
     * Handle add component button click.
     */
    private void handleAddComponent() {
        try {
            // Show component dialog
            openComponentDetailDialog(new Component(), true);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating new component", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.component.create.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle edit component button click.
     */
    private void handleEditComponent(Component component) {
        if (component == null) {
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.component.select"));
            return;
        }
        
        try {
            // Show component dialog
            openComponentDetailDialog(component, false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing component", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.component.edit.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Opens a component detail dialog.
     * 
     * @param component the component to edit or create
     * @param isNew true if creating a new component, false if editing
     */
    private void openComponentDetailDialog(Component component, boolean isNew) {
        try {
            // Load the component detail view
            ViewTuple<ComponentDetailMvvmView, ComponentDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(ComponentDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            ComponentDetailMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize the controller with the component
            if (isNew) {
                viewController.initNewComponent();
            } else {
                viewController.initExistingComponent(component);
            }
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(isNew ? resources.getString("component.new.title") : 
                                        resources.getString("component.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainPane.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh components after dialog closes
            viewModel.getRefreshComponentsCommand().execute();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening component dialog", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.component.dialog.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle delete component button click.
     */
    @FXML
    private void onDeleteComponentAction() {
        if (viewModel.getSelectedComponent() == null) {
            // Show alert about no selection
            showErrorAlert(resources.getString("error.title"),
                           resources.getString("info.no.selection.component"));
            return;
        }
        
        // Confirm deletion
        String confirmMessage = resources.getString("component.delete.confirm") + 
            " '" + viewModel.getSelectedComponent().getName() + "'?";
        
        if (showConfirmationAlert(resources.getString("confirm.title"), confirmMessage)) {
            // Execute delete command
            viewModel.getDeleteComponentCommand().execute();
        }
    }
    
    /**
     * Handle refresh button click.
     */
    @FXML
    private void onRefreshAction() {
        viewModel.getRefreshComponentsCommand().execute();
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