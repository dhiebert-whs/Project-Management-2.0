// src/main/java/org/frcpm/mvvm/views/TaskDetailMvvmView.java

package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.TaskDetailMvvmViewModel;

/**
 * View for the task detail using MVVMFx.
 */
public class TaskDetailMvvmView implements FxmlView<TaskDetailMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(TaskDetailMvvmView.class.getName());
    
    @FXML private Label taskTitleLabel;
    @FXML private Label projectLabel;
    @FXML private Label subsystemLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<Task.Priority> priorityComboBox;
    @FXML private Slider progressSlider;
    @FXML private Label progressLabel;
    @FXML private CheckBox completedCheckBox;
    @FXML private TextField estimatedHoursField;
    @FXML private TextField actualHoursField;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    @FXML private TableView<TeamMember> assignedMembersTable;
    @FXML private TableColumn<TeamMember, String> memberNameColumn;
    @FXML private TableColumn<TeamMember, String> memberSubteamColumn;
    @FXML private Button addMemberButton;
    @FXML private Button removeMemberButton;
    
    @FXML private TableView<Component> requiredComponentsTable;
    @FXML private TableColumn<Component, String> componentNameColumn;
    @FXML private TableColumn<Component, String> componentPartNumberColumn;
    @FXML private TableColumn<Component, Boolean> componentDeliveredColumn;
    @FXML private Button addComponentButton;
    @FXML private Button removeComponentButton;
    
    @FXML private TableView<Task> dependenciesTable;
    @FXML private TableColumn<Task, String> dependencyTitleColumn;
    @FXML private TableColumn<Task, Integer> dependencyProgressColumn;
    @FXML private Button addDependencyButton;
    @FXML private Button removeDependencyButton;
    
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    
    @InjectViewModel
    private TaskDetailMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing TaskDetailMvvmView");
        this.resources = resources;
        
        // Set up priority combo box
        if (priorityComboBox != null) {
            priorityComboBox.getItems().addAll(Task.Priority.values());
        }
        
        // Set up table columns
        setupTableColumns();
        
        // Set up bindings
        setupBindings();
        
        // Set up loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
        }
        
        // Set up error label
        if (errorLabel != null) {
            errorLabel.textProperty().bind(viewModel.errorMessageProperty());
            errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        }
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        // Check for null UI components for testability
        if (memberNameColumn == null || memberSubteamColumn == null || 
            componentNameColumn == null || componentPartNumberColumn == null || 
            componentDeliveredColumn == null || dependencyTitleColumn == null || 
            dependencyProgressColumn == null) {
            LOGGER.warning("Table columns not initialized - likely in test environment");
            return;
        }

        try {
            // Team member columns
            memberNameColumn.setCellValueFactory(cellData -> {
                TeamMember member = cellData.getValue();
                if (member != null) {
                    return javafx.beans.binding.Bindings.createStringBinding(() -> member.getFullName());
                }
                return javafx.beans.binding.Bindings.createStringBinding(() -> "");
            });
            
            memberSubteamColumn.setCellValueFactory(cellData -> {
                TeamMember member = cellData.getValue();
                if (member != null && member.getSubteam() != null) {
                    return javafx.beans.binding.Bindings.createStringBinding(() -> member.getSubteam().getName());
                }
                return javafx.beans.binding.Bindings.createStringBinding(() -> "");
            });
            
            // Component columns
            componentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            componentPartNumberColumn.setCellValueFactory(new PropertyValueFactory<>("partNumber"));
            componentDeliveredColumn.setCellValueFactory(new PropertyValueFactory<>("delivered"));
            componentDeliveredColumn.setCellFactory(column -> new CheckBoxTableCell<>());
            
            // Dependency columns
            dependencyTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            dependencyProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
            dependencyProgressColumn.setCellFactory(column -> new ProgressBarTableCell<>());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up table columns", e);
            throw new RuntimeException("Failed to set up table columns", e);
        }
    }
    
    /**
     * Sets up data bindings.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (taskTitleLabel == null || projectLabel == null || subsystemLabel == null || 
            startDatePicker == null || endDatePicker == null || priorityComboBox == null || 
            progressSlider == null || progressLabel == null || completedCheckBox == null || 
            estimatedHoursField == null || actualHoursField == null || descriptionArea == null || 
            saveButton == null || cancelButton == null || assignedMembersTable == null || 
            requiredComponentsTable == null || dependenciesTable == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        try {
            // Bind text properties
            taskTitleLabel.textProperty().bind(viewModel.titleProperty());
            projectLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                    () -> viewModel.getProject() != null ? viewModel.getProject().getName() : "",
                    viewModel.projectProperty()));
            subsystemLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                    () -> viewModel.getSubsystem() != null ? viewModel.getSubsystem().getName() : "",
                    viewModel.subsystemProperty()));
            
            // Bind numeric fields with proper converters
            javafx.util.StringConverter<Number> numberConverter = new javafx.util.converter.NumberStringConverter();
            javafx.beans.binding.Bindings.bindBidirectional(estimatedHoursField.textProperty(), viewModel.estimatedHoursProperty(), numberConverter);
            javafx.beans.binding.Bindings.bindBidirectional(actualHoursField.textProperty(), viewModel.actualHoursProperty(), numberConverter);
            
            // Bind text area
            descriptionArea.textProperty().bindBidirectional(viewModel.descriptionProperty());
            
            // Bind date pickers
            startDatePicker.valueProperty().bindBidirectional(viewModel.startDateProperty());
            endDatePicker.valueProperty().bindBidirectional(viewModel.endDateProperty());
            
            // Bind combo box
            priorityComboBox.valueProperty().bindBidirectional(viewModel.priorityProperty());
            
            // Bind progress slider and completion
            progressSlider.valueProperty().bindBidirectional(viewModel.progressProperty());
            progressLabel.textProperty().bind(javafx.beans.binding.Bindings.format("%d%%", viewModel.progressProperty()));
            completedCheckBox.selectedProperty().bindBidirectional(viewModel.completedProperty());
            
            // Bind tables to view model collections
            assignedMembersTable.setItems(viewModel.getAssignedMembers());
            requiredComponentsTable.setItems(viewModel.getRequiredComponents());
            dependenciesTable.setItems(viewModel.getPreDependencies());
            
            // Bind buttons to commands
            CommandAdapter.bindCommandButton(saveButton, viewModel.getSaveCommand());
            CommandAdapter.bindCommandButton(cancelButton, viewModel.getCancelCommand());
            CommandAdapter.bindCommandButton(addMemberButton, viewModel.getAddMemberCommand());
            CommandAdapter.bindCommandButton(removeMemberButton, viewModel.getRemoveMemberCommand());
            CommandAdapter.bindCommandButton(addComponentButton, viewModel.getAddComponentCommand());
            CommandAdapter.bindCommandButton(removeComponentButton, viewModel.getRemoveComponentCommand());
            CommandAdapter.bindCommandButton(addDependencyButton, viewModel.getAddDependencyCommand());
            CommandAdapter.bindCommandButton(removeDependencyButton, viewModel.getRemoveDependencyCommand());
            
            // Setup selection changes
            assignedMembersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedMember(newVal));
            
            requiredComponentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedComponent(newVal));
            
            dependenciesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedDependency(newVal));
            
            // Set up cancel button to close dialog
            cancelButton.setOnAction(event -> closeDialog());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up bindings", e);
            throw new RuntimeException("Failed to set up bindings", e);
        }
    }
    
    /**
     * Initializes the view with a new task.
     * 
     * @param task the task to create
     */
    public void initNewTask(Task task) {
        try {
            viewModel.initNewTask(task.getProject(), task.getSubsystem());
            viewModel.setTitle(task.getTitle());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing new task", e);
            // Show error alert using AlertHelper or another utility
        }
    }
    
    /**
     * Initializes the view with an existing task.
     * 
     * @param task the task to edit
     */
    public void initExistingTask(Task task) {
        try {
            viewModel.initExistingTask(task);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing existing task", e);
            // Show error alert using AlertHelper or another utility
        }
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            if (saveButton != null && saveButton.getScene() != null && 
                saveButton.getScene().getWindow() != null) {
                
                // Clean up resources
                viewModel.dispose();
                
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }
    
    /**
     * Handler for add team member button click.
     */
    @FXML
    private void handleAddTeamMember() {
        // This would be implemented to show a dialog to select a team member
        LOGGER.info("Add team member button clicked");
    }
    
    /**
     * Handler for add component button click.
     */
    @FXML
    private void handleAddComponent() {
        // This would be implemented to show a dialog to select a component
        LOGGER.info("Add component button clicked");
    }
    
    /**
     * Handler for add dependency button click.
     */
    @FXML
    private void handleAddDependency() {
        // This would be implemented to show a dialog to select a dependency
        LOGGER.info("Add dependency button clicked");
    }
    
    /**
     * Gets the view model.
     * 
     * @return the view model
     */
    public TaskDetailMvvmViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * CheckBox cell for Boolean properties in a TableView.
     */
    private static class CheckBoxTableCell<S, T> extends TableCell<S, T> {
        private final CheckBox checkBox = new CheckBox();

        public CheckBoxTableCell() {
            setGraphic(checkBox);
            checkBox.setDisable(true);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                checkBox.setSelected((Boolean) item);
                setGraphic(checkBox);
            }
        }
    }

    /**
     * ProgressBar cell for numeric progress properties in a TableView.
     */
    private static class ProgressBarTableCell<S, T> extends TableCell<S, T> {
        private final ProgressBar progressBar = new ProgressBar();

        public ProgressBarTableCell() {
            progressBar.setMaxWidth(Double.MAX_VALUE);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                int progress = (Integer) item;
                progressBar.setProgress(progress / 100.0);
                setText(progress + "%");
                setGraphic(progressBar);
            }
        }
    }
}