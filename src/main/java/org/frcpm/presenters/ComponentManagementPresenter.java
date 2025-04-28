package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Window;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.Component;
import org.frcpm.services.ComponentService;
import org.frcpm.services.DialogService;
import org.frcpm.viewmodels.ComponentManagementViewModel;
import org.frcpm.views.ComponentView;

import javax.inject.Inject;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for the Component Management view using AfterburnerFX pattern.
 */
public class ComponentManagementPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ComponentManagementPresenter.class.getName());

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

    // Injected services
    @Inject
    private ComponentService componentService;
    
    @Inject
    private DialogService dialogService;

    // ViewModel and resources
    private ComponentManagementViewModel viewModel;
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing ComponentManagementPresenter with resource bundle");
        
        this.resources = resources;
        
        // Create view model with injected service
        viewModel = new ComponentManagementViewModel(componentService);

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
        if (componentsTable == null || nameColumn == null || partNumberColumn == null ||
                expectedDeliveryColumn == null || deliveredColumn == null) {
            
            LOGGER.warning("Table components not initialized - likely in test environment");
            return;
        }

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
                    setText(date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                }
            }
        });
    }
    
    /**
     * Sets up the bindings between the view model and UI controls.
     */
    private void setupBindings() {
        if (componentsTable == null || addComponentButton == null || editComponentButton == null ||
                deleteComponentButton == null || refreshButton == null) {
            
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        // Bind table to view model components list
        componentsTable.setItems(viewModel.getComponents());

        // Bind buttons to commands
        ViewModelBinding.bindCommandButton(addComponentButton, viewModel.getAddComponentCommand());
        ViewModelBinding.bindCommandButton(editComponentButton, viewModel.getEditComponentCommand());
        ViewModelBinding.bindCommandButton(deleteComponentButton, viewModel.getDeleteComponentCommand());
        ViewModelBinding.bindCommandButton(refreshButton, viewModel.getRefreshCommand());

        // Bind selection changes
        if (componentsTable.getSelectionModel() != null) {
            componentsTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> viewModel.setSelectedComponent(newVal));
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
        if (componentsTable == null) {
            LOGGER.warning("Table not initialized - likely in test environment");
            return;
        }

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
        if (filterComboBox == null) {
            LOGGER.warning("Filter combo box not initialized - likely in test environment");
            return;
        }

        // Set up filter options
        filterComboBox.getItems().clear();
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
     * This is called by the Add Component button.
     */
    @FXML
    public void handleAddComponent() {
        try {
            // Use ViewLoader for AfterburnerFX integration
            ComponentPresenter presenter = ViewLoader.showDialog(ComponentView.class, 
                    resources.getString("component.new.title"), 
                    getWindow());
            
            if (presenter != null) {
                presenter.initNewComponent();
                
                // Refresh the components list after dialog closes
                viewModel.loadComponents();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading component dialog", e);
            showErrorAlert("Error", "Failed to open component dialog: " + e.getMessage());
        }
    }

    /**
     * Handles showing the edit component dialog.
     * This is called by the Edit Component button and table row double-click.
     * 
     * @param component the component to edit
     */
    public void handleEditComponent(Component component) {
        if (component == null) {
            return;
        }

        try {
            // Use ViewLoader for AfterburnerFX integration
            ComponentPresenter presenter = ViewLoader.showDialog(ComponentView.class, 
                    resources.getString("component.edit.title"), 
                    getWindow());
            
            if (presenter != null) {
                presenter.initExistingComponent(component);
                
                // Refresh the components list after dialog closes
                viewModel.loadComponents();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading component dialog", e);
            showErrorAlert("Error", "Failed to open component dialog: " + e.getMessage());
        }
    }
    
    /**
     * Handles the delete component action.
     */
    @FXML
    public void handleDeleteComponent() {
        Component selectedComponent = viewModel.getSelectedComponent();
        if (selectedComponent == null) {
            showInfoAlert("No Selection", "Please select a component to delete.");
            return;
        }
        
        boolean confirmed = showConfirmationAlert(
                "Delete Component", 
                "Are you sure you want to delete the component '" + selectedComponent.getName() + "'?");
        
        if (confirmed) {
            boolean success = viewModel.deleteComponent(selectedComponent);
            if (success) {
                showInfoAlert("Component Deleted", "Component was successfully deleted.");
                viewModel.loadComponents();
            }
        }
    }
    
    /**
     * Handles the refresh action.
     */
    @FXML
    public void handleRefresh() {
        viewModel.loadComponents();
    }
    
    /**
     * Gets the window for this presenter.
     * 
     * @return the window, or null if not available
     */
    private Window getWindow() {
        if (componentsTable != null && componentsTable.getScene() != null) {
            return componentsTable.getScene().getWindow();
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
    public ComponentManagementViewModel getViewModel() {
        return viewModel;
    }
}