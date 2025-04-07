// src/main/java/org/frcpm/controllers/ComponentManagementController.java
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
import org.frcpm.models.Component;
import org.frcpm.services.DialogService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.viewmodels.ComponentManagementViewModel;

import java.io.IOException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Component Management view.
 * Handles listing, adding, editing, and deleting components.
 */
public class ComponentManagementController {

    private static final Logger LOGGER = Logger.getLogger(ComponentManagementController.class.getName());
    
    // FXML controls
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
    private Button addComponentButton;
    
    @FXML
    private Button editComponentButton;
    
    @FXML
    private Button deleteComponentButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private ComboBox<ComponentManagementViewModel.ComponentFilter> filterComboBox;
    
    // ViewModel and services
    private final ComponentManagementViewModel viewModel = new ComponentManagementViewModel();
    private DialogService dialogService = ServiceFactory.getDialogService();
    
    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing ComponentManagementController");
        
        // Set up table columns
        setupTableColumns();
        
        // Set up bindings
        setupBindings();
        
        // Set up table row double-click handler
        setupRowHandler();
        
        // Set up filter combo box
        setupFilterComboBox();
        
        // Load components data
        viewModel.loadComponents();
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        partNumberColumn.setCellValueFactory(new PropertyValueFactory<>("partNumber"));
        expectedDeliveryColumn.setCellValueFactory(new PropertyValueFactory<>("expectedDelivery"));
        
        // Set up the delivered column with a checkbox
        deliveredColumn.setCellValueFactory(new PropertyValueFactory<>("delivered"));
        deliveredColumn.setCellFactory(column -> new TableCell<Component, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            
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
        
        // Set up date formatter for expected delivery column
        expectedDeliveryColumn.setCellFactory(column -> new TableCell<Component, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                }
            }
        });
    }
    
    /**
     * Sets up the bindings between the view model and UI controls.
     */
    private void setupBindings() {
        // Bind table to view model components list
        componentsTable.setItems(viewModel.getComponents());
        
        // Bind buttons to commands
        ViewModelBinding.bindCommandButton(addComponentButton, viewModel.getAddComponentCommand());
        ViewModelBinding.bindCommandButton(editComponentButton, viewModel.getEditComponentCommand());
        ViewModelBinding.bindCommandButton(deleteComponentButton, viewModel.getDeleteComponentCommand());
        ViewModelBinding.bindCommandButton(refreshButton, viewModel.getRefreshCommand());
        
        // Bind selection changes
        componentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedComponent(newVal));
        
        // Set up error message binding
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorAlert("Error", newVal);
                viewModel.errorMessageProperty().set("");
            }
        });
    }
    
    /**
     * Sets up the double-click handler for table rows.
     */
    private void setupRowHandler() {
        componentsTable.setRowFactory(tv -> {
            TableRow<Component> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Component component = row.getItem();
                    handleEditComponent(component);
                }
            });
            return row;
        });
    }
    
    /**
     * Sets up the filter combo box.
     */
    private void setupFilterComboBox() {
        // Set up filter options
        filterComboBox.getItems().addAll(ComponentManagementViewModel.ComponentFilter.values());
        filterComboBox.setValue(ComponentManagementViewModel.ComponentFilter.ALL);
        
        // Set up filter change listener
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.setFilter(newVal);
            }
        });
    }
    
    /**
     * Handles showing the add component dialog.
     */
    @FXML
    private void handleAddComponent() {
        try {
            // Load the component dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ComponentView.fxml"));
            Parent dialogView = loader.load();
            
            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Component");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(componentsTable.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));
            
            // Get the controller
            ComponentController controller = loader.getController();
            controller.initNewComponent();
            
            // Show the dialog
            dialogStage.showAndWait();
            
            // Refresh the components list
            viewModel.loadComponents();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading component dialog", e);
            showErrorAlert("Error", "Failed to open component dialog: " + e.getMessage());
        }
    }
    
    /**
     * Handles showing the edit component dialog.
     * 
     * @param component the component to edit
     */
    private void handleEditComponent(Component component) {
        if (component == null) {
            return;
        }
        
        try {
            // Load the component dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ComponentView.fxml"));
            Parent dialogView = loader.load();
            
            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Component");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(componentsTable.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));
            
            // Get the controller
            ComponentController controller = loader.getController();
            controller.initExistingComponent(component);
            
            // Show the dialog
            dialogStage.showAndWait();
            
            // Refresh the components list
            viewModel.loadComponents();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading component dialog", e);
            showErrorAlert("Error", "Failed to open component dialog: " + e.getMessage());
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
     * Sets the DialogService for this controller.
     * This method is primarily used for testing to inject mock services.
     * 
     * @param dialogService the dialog service to use
     */
    public void setDialogService(DialogService dialogService) {
        this.dialogService = dialogService;
    }
    
    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public ComponentManagementViewModel getViewModel() {
        return viewModel;
    }
}