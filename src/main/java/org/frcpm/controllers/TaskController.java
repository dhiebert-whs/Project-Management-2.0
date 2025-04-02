package org.frcpm.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.viewmodels.TaskViewModel;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Task view.
 */
public class TaskController {

    private static final Logger LOGGER = Logger.getLogger(TaskController.class.getName());

    // FXML controls
    @FXML
    private Label taskTitleLabel;
    @FXML
    private Label projectLabel;
    @FXML
    private Label subsystemLabel;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<Task.Priority> priorityComboBox;
    @FXML
    private Slider progressSlider;
    @FXML
    private Label progressLabel;
    @FXML
    private CheckBox completedCheckBox;
    @FXML
    private TextField estimatedHoursField;
    @FXML
    private TextField actualHoursField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    // Table views and buttons
    @FXML
    private TableView<TeamMember> assignedMembersTable;
    @FXML
    private TableColumn<TeamMember, String> memberNameColumn;
    @FXML
    private TableColumn<TeamMember, String> memberSubteamColumn;
    @FXML
    private Button addMemberButton;
    @FXML
    private Button removeMemberButton;

    @FXML
    private TableView<Component> requiredComponentsTable;
    @FXML
    private TableColumn<Component, String> componentNameColumn;
    @FXML
    private TableColumn<Component, String> componentPartNumberColumn;
    @FXML
    private TableColumn<Component, Boolean> componentDeliveredColumn;
    @FXML
    private Button addComponentButton;
    @FXML
    private Button removeComponentButton;

    @FXML
    private TableView<Task> dependenciesTable;
    @FXML
    private TableColumn<Task, String> dependencyTitleColumn;
    @FXML
    private TableColumn<Task, Integer> dependencyProgressColumn;
    @FXML
    private Button addDependencyButton;
    @FXML
    private Button removeDependencyButton;

    private TaskViewModel viewModel;

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing TaskController");

        // Create view model
        viewModel = new TaskViewModel();

        // Set up priority combo box
        priorityComboBox.getItems().addAll(Arrays.asList(Task.Priority.values()));

        // Set up table columns
        setupTableColumns();

        // Set up progress slider and label binding
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int progress = newVal.intValue();
            progressLabel.setText(progress + "%");
        });

        // Set up bindings
        setupBindings();
    }

    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        memberNameColumn
                .setCellValueFactory(cellData -> Bindings.createStringBinding(() -> cellData.getValue().getFullName()));
        memberSubteamColumn.setCellValueFactory(
                cellData -> Bindings.createStringBinding(() -> cellData.getValue().getSubteam() != null
                        ? cellData.getValue().getSubteam().getName()
                        : ""));

        componentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        componentPartNumberColumn.setCellValueFactory(new PropertyValueFactory<>("partNumber"));
        componentDeliveredColumn.setCellValueFactory(new PropertyValueFactory<>("delivered"));

        dependencyTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dependencyProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
    }

    /**
     * Binds the view model to the UI controls.
     */
    private void setupBindings() {
        // Bind text fields
        taskTitleLabel.textProperty().bind(viewModel.titleProperty());
        projectLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> viewModel.getProject() != null ? viewModel.getProject().getName() : "",
                        viewModel.projectProperty()));
        subsystemLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> viewModel.getSubsystem() != null ? viewModel.getSubsystem().getName() : "",
                        viewModel.subsystemProperty()));

        StringProperty estimatedHoursStringProp = new SimpleStringProperty();
        estimatedHoursStringProp.bind(Bindings.createStringBinding(
                () -> String.valueOf(viewModel.getEstimatedHours()),
                viewModel.estimatedHoursProperty()));
        ViewModelBinding.bindTextField(estimatedHoursField, estimatedHoursStringProp);

        StringProperty actualHoursStringProp = new SimpleStringProperty();
        actualHoursStringProp.bind(Bindings.createStringBinding(
                () -> String.valueOf(viewModel.getActualHours()),
                viewModel.actualHoursProperty()));
        ViewModelBinding.bindTextField(actualHoursField, actualHoursStringProp);

        ViewModelBinding.bindTextArea(descriptionArea, viewModel.descriptionProperty());

        // Bind date pickers
        ViewModelBinding.bindDatePicker(startDatePicker, viewModel.startDateProperty());
        ViewModelBinding.bindDatePicker(endDatePicker, viewModel.endDateProperty());

        // Bind combo box
        ViewModelBinding.bindComboBox(priorityComboBox, viewModel.priorityProperty());

        // Bind progress and completion
        progressSlider.valueProperty().bindBidirectional(viewModel.progressProperty());
        completedCheckBox.selectedProperty().bindBidirectional(viewModel.completedProperty());

        // Bind tables to view model collections
        assignedMembersTable.setItems(viewModel.getAssignedMembers());
        requiredComponentsTable.setItems(viewModel.getRequiredComponents());
        dependenciesTable.setItems(viewModel.getPreDependencies());

        // Bind buttons to commands
        ViewModelBinding.bindCommandButton(saveButton, viewModel.getSaveCommand());
        ViewModelBinding.bindCommandButton(cancelButton, viewModel.getCancelCommand());
        ViewModelBinding.bindCommandButton(addMemberButton, viewModel.getAddMemberCommand());
        ViewModelBinding.bindCommandButton(removeMemberButton, viewModel.getRemoveMemberCommand());
        ViewModelBinding.bindCommandButton(addComponentButton, viewModel.getAddComponentCommand());
        ViewModelBinding.bindCommandButton(removeComponentButton, viewModel.getRemoveComponentCommand());
        ViewModelBinding.bindCommandButton(addDependencyButton, viewModel.getAddDependencyCommand());
        ViewModelBinding.bindCommandButton(removeDependencyButton, viewModel.getRemoveDependencyCommand());

        // Setup selection changes with view model
        assignedMembersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> removeMemberButton.setDisable(newVal == null));

        requiredComponentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> removeComponentButton.setDisable(newVal == null));

        dependenciesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> removeDependencyButton.setDisable(newVal == null));

        // Set up error message binding
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorAlert(newVal);
            }
        });

        // Set up save and cancel actions
        saveButton.setOnAction(event -> {
            if (viewModel.isValid()) {
                viewModel.getSaveCommand().execute();
                closeDialog();
            } else {
                showErrorAlert(viewModel.getErrorMessage());
            }
        });

        cancelButton.setOnAction(event -> closeDialog());
    }

    /**
     * Initializes the controller for a new task.
     * 
     * @param task the new task to create
     */
    public void initNewTask(Task task) {
        viewModel.initNewTask(task.getProject(), task.getSubsystem());
        viewModel.titleProperty().set(task.getTitle());
    }

    /**
     * Initializes the controller for editing an existing task.
     * 
     * @param task the task to edit
     */
    public void initExistingTask(Task task) {
        viewModel.initExistingTask(task);
    }

    /**
     * Shows an error alert with the given message.
     * 
     * @param message the error message
     */
    private void showErrorAlert(String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Validation Error");
            alert.setContentText(message);
            alert.showAndWait();
        } catch (IllegalStateException e) {
            // This can happen in tests when not on FX thread
            // Just log the error for testing purposes
            LOGGER.log(Level.INFO, "Alert would show: Validation Error - {0}", message);
        }
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }

    // Table selection handlers

    /**
     * Handles the remove member action when a member is selected.
     */
    @FXML
    private void handleRemoveMember() {
        TeamMember selectedMember = assignedMembersTable.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            viewModel.removeMember(selectedMember);
        }
    }

    /**
     * Handles the remove component action when a component is selected.
     */
    @FXML
    private void handleRemoveComponent() {
        Component selectedComponent = requiredComponentsTable.getSelectionModel().getSelectedItem();
        if (selectedComponent != null) {
            viewModel.removeComponent(selectedComponent);
        }
    }

    /**
     * Handles the remove dependency action when a dependency is selected.
     */
    @FXML
    private void handleRemoveDependency() {
        Task selectedDependency = dependenciesTable.getSelectionModel().getSelectedItem();
        if (selectedDependency != null) {
            viewModel.removeDependency(selectedDependency);
        }
    }

    /**
     * Compatibility method for legacy code calling setTask
     * 
     * @param task the task to edit
     */
    public void setTask(Task task) {
        initExistingTask(task);
    }

    /**
     * Compatibility method for legacy code calling setNewTask
     * 
     * @param project   the project
     * @param subsystem the subsystem
     */
    public void setNewTask(Project project, Subsystem subsystem) {
        Task newTask = new Task("New Task", project, subsystem);
        initNewTask(newTask);
    }

    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }

    /**
     * Gets the task from the ViewModel.
     * 
     * @return the task
     */
    public Task getTask() {
        return viewModel.getTask();
    }

    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public TaskViewModel getViewModel() {
        return viewModel;
    }
}