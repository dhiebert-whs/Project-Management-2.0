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
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Component Management view.
 * Follows the standardized MVVM pattern by delegating all business logic to the
 * ComponentManagementViewModel.
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
    private ComponentManagementViewModel viewModel = new ComponentManagementViewModel();
    private DialogService dialogService = ServiceFactory.getDialogService();

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing ComponentManagementController");

        if (componentsTable == null || nameColumn == null || partNumberColumn == null ||
                expectedDeliveryColumn == null || deliveredColumn == null || addComponentButton == null ||
                editComponentButton == null || deleteComponentButton == null || refreshButton == null ||
                filterComboBox == null) {

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

        // Load components data
        viewModel.loadComponents();
    }

    /**
     * Sets up the table columns.
     * Protected for testability.
     */
    protected void setupTableColumns() {
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
     * Protected for testability.
     */
    protected void setupBindings() {
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
     * Protected for testability.
     */
    protected void setupRowHandler() {
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
     * Protected for testability.
     */
    protected void setupFilterComboBox() {
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
            showComponentDialog("New Component", null);
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
    protected void handleEditComponent(Component component) {
        if (component == null) {
            return;
        }

        try {
            showComponentDialog("Edit Component", component);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading component dialog", e);
            showErrorAlert("Error", "Failed to open component dialog: " + e.getMessage());
        }
    }

    /**
     * Shows the component dialog for adding or editing a component.
     * Protected for testability.
     * 
     * @param title     the dialog title
     * @param component the component to edit, or null for a new component
     * @throws IOException if the FXML file cannot be loaded
     */
    protected void showComponentDialog(String title, Component component) throws IOException {
        // Load the component dialog
        FXMLLoader loader = createFXMLLoader("/fxml/ComponentView.fxml");
        Parent dialogView = loader.load();

        // Create the dialog
        Stage dialogStage = createDialogStage(title, dialogView);

        // Get the controller
        ComponentController controller = loader.getController();

        // Initialize the controller
        if (component == null) {
            controller.initNewComponent();
        } else {
            controller.initExistingComponent(component);
        }

        // Show the dialog
        Optional<ButtonType> result = showAndWaitDialog(dialogStage);

        // Refresh the components list
        viewModel.loadComponents();
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

        if (componentsTable != null && componentsTable.getScene() != null &&
                componentsTable.getScene().getWindow() != null) {
            dialogStage.initOwner(componentsTable.getScene().getWindow());
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
     * Gets the selected component from the table.
     * Protected for testability.
     * 
     * @return the selected component
     */
    protected Component getSelectedComponent() {
        if (componentsTable != null && componentsTable.getSelectionModel() != null) {
            return componentsTable.getSelectionModel().getSelectedItem();
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
    public ComponentManagementViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Sets the ViewModel.
     * This method is primarily used for testing to inject mock viewmodels.
     * 
     * @param viewModel the viewModel to use
     */
    public void setViewModel(ComponentManagementViewModel viewModel) {
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