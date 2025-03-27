// Path: src/main/java/org/frcpm/controllers/TaskController.java

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
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.viewmodels.TaskViewModel;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

/**
 * Controller for the Task view.
 */
public class TaskController {
    
    // FXML controls
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
    
    // Table views and buttons
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
    
    private TaskViewModel viewModel;
    
    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        // Create view model
        viewModel = new TaskViewModel();
        
        // Set up priority combo box
        priorityComboBox.getItems().addAll(Arrays.asList(Task.Priority.values()));
        
        // Set up table columns
        memberNameColumn.setCellValueFactory(cellData -> 
            Bindings.createStringBinding(() -> cellData.getValue().getFullName()));
        memberSubteamColumn.setCellValueFactory(cellData -> 
            Bindings.createStringBinding(() -> 
                cellData.getValue().getSubteam() != null 
                    ? cellData.getValue().getSubteam().getName() 
                    : ""));
        
        componentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        componentPartNumberColumn.setCellValueFactory(new PropertyValueFactory<>("partNumber"));
        componentDeliveredColumn.setCellValueFactory(new PropertyValueFactory<>("delivered"));
        
        dependencyTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dependencyProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        
        // Set up progress slider and label binding
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int progress = newVal.intValue();
            progressLabel.setText(progress + "%");
            viewModel.setProgress(progress);
        });
        
        // Set up bindings
        bindViewModel();
    }
    
    /**
     * Binds the view model to the UI controls.
     */
    private void bindViewModel() {
        // Bind text fields
        taskTitleLabel.textProperty().bind(viewModel.titleProperty());
        projectLabel.textProperty().bind(
            Bindings.createStringBinding(() -> 
                viewModel.getProject() != null ? viewModel.getProject().getName() : "", 
                viewModel.projectProperty())
        );
        subsystemLabel.textProperty().bind(
            Bindings.createStringBinding(() -> 
                viewModel.getSubsystem() != null ? viewModel.getSubsystem().getName() : "", 
                viewModel.subsystemProperty())
        );


        StringProperty estimatedHoursStringProp = new SimpleStringProperty();
        estimatedHoursStringProp.bind(Bindings.createStringBinding(
            () -> String.valueOf(viewModel.getEstimatedHours()),
            viewModel.estimatedHoursProperty()
        ));
        ViewModelBinding.bindTextField(estimatedHoursField, estimatedHoursStringProp);


        StringProperty actualHoursStringProp = new SimpleStringProperty();
        actualHoursStringProp.bind(Bindings.createStringBinding(
            () -> String.valueOf(viewModel.getActualHours()),
            viewModel.actualHoursProperty()
        ));
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
        
        // Set up error message binding
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorAlert(newVal);
            }
        });
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Validation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows a dialog to select a team member.
     * 
     * @param members the available team members
     * @return the selected team member, or empty if cancelled
     */
    private Optional<TeamMember> showMemberSelectionDialog(ObservableList<TeamMember> members) {
        // In a real implementation, this would show a dialog to select a team member
        // For now, we'll just return an empty Optional
        return Optional.empty();
    }
    
    /**
     * Shows a dialog to select a component.
     * 
     * @param components the available components
     * @return the selected component, or empty if cancelled
     */
    private Optional<Component> showComponentSelectionDialog(ObservableList<Component> components) {
        // In a real implementation, this would show a dialog to select a component
        // For now, we'll just return an empty Optional
        return Optional.empty();
    }
    
    /**
     * Shows a dialog to select a task dependency.
     * 
     * @param tasks the available tasks
     * @return the selected task, or empty if cancelled
     */
    private Optional<Task> showTaskSelectionDialog(ObservableList<Task> tasks) {
        // In a real implementation, this would show a dialog to select a task
        // For now, we'll just return an empty Optional
        return Optional.empty();
    }
    
    /**
     * Handles the add member button action.
     */
    @FXML
    private void handleAddMember() {
        // In a real implementation, this would show a dialog to select a team member
        // and then add the selected member to the view model
    }
    
    /**
     * Handles the remove member button action.
     */
    @FXML
    private void handleRemoveMember() {
        TeamMember selectedMember = assignedMembersTable.getSelectionModel().getSelectedItem();
        if (selectedMember != null) {
            viewModel.removeMember(selectedMember);
        }
    }
    
    /**
     * Handles the add component button action.
     */
    @FXML
    private void handleAddComponent() {
        // In a real implementation, this would show a dialog to select a component
        // and then add the selected component to the view model
    }
    
    /**
     * Handles the remove component button action.
     */
    @FXML
    private void handleRemoveComponent() {
        Component selectedComponent = requiredComponentsTable.getSelectionModel().getSelectedItem();
        if (selectedComponent != null) {
            viewModel.removeComponent(selectedComponent);
        }
    }
    
    /**
     * Handles the add dependency button action.
     */
    @FXML
    private void handleAddDependency() {
        // In a real implementation, this would show a dialog to select a task
        // and then add the selected task as a dependency to the view model
    }
    
    /**
     * Handles the remove dependency button action.
     */
    @FXML
    private void handleRemoveDependency() {
        Task selectedDependency = dependenciesTable.getSelectionModel().getSelectedItem();
        if (selectedDependency != null) {
            viewModel.removeDependency(selectedDependency);
        }
    }
    
    /**
     * Handles the save button action.
     */
    @FXML
    private void handleSave() {
        // The save action is handled by the view model's save command
        // When save is complete, close the dialog
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Handles the cancel button action.
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}