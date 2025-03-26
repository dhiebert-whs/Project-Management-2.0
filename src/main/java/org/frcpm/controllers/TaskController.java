package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.frcpm.models.*;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.TaskService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.ComponentService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for task view and management.
 */
public class TaskController {
    
    private static final Logger LOGGER = Logger.getLogger(TaskController.class.getName());
    
    // FXML Controls
    @FXML
    private Label taskTitleLabel;
    
    @FXML
    private Label projectLabel;
    
    @FXML
    private Label subsystemLabel;
    
    @FXML
    private TextArea descriptionArea;
    
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
    private TableView<TeamMember> assignedMembersTable;
    
    @FXML
    private TableColumn<TeamMember, String> memberNameColumn;
    
    @FXML
    private TableColumn<TeamMember, String> memberSubteamColumn;
    
    @FXML
    private TableView<Component> requiredComponentsTable;
    
    @FXML
    private TableColumn<Component, String> componentNameColumn;
    
    @FXML
    private TableColumn<Component, String> componentPartNumberColumn;
    
    @FXML
    private TableColumn<Component, Boolean> componentDeliveredColumn;
    
    @FXML
    private TableView<Task> dependenciesTable;
    
    @FXML
    private TableColumn<Task, String> dependencyTitleColumn;
    
    @FXML
    private TableColumn<Task, Integer> dependencyProgressColumn;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button addMemberButton;
    
    @FXML
    private Button removeMemberButton;
    
    @FXML
    private Button addComponentButton;
    
    @FXML
    private Button removeComponentButton;
    
    @FXML
    private Button addDependencyButton;
    
    @FXML
    private Button removeDependencyButton;
    
    // Services
    private final TaskService taskService = ServiceFactory.getTaskService();
    private final TeamMemberService teamMemberService = ServiceFactory.getTeamMemberService();
    private final ComponentService componentService = ServiceFactory.getComponentService();
    
    // Data
    protected Task task;
    protected boolean isNewTask;
    ObservableList<TeamMember> assignedMembers = FXCollections.observableArrayList();
    ObservableList<Component> requiredComponents = FXCollections.observableArrayList();
    ObservableList<Task> dependencies = FXCollections.observableArrayList();
    
    /**
     * Helper method to create a date cell factory that works with any entity type.
     * 
     * @param <T> the entity type for the table row
     * @return a callback that creates properly formatted date cells
     */
    private <T> Callback<TableColumn<T, LocalDate>, TableCell<T, LocalDate>> createDateCellFactory() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return column -> new TableCell<T, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        };
    }
    
    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing TaskController");
        
        // Set up progress slider
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int progress = newVal.intValue();
            progressLabel.setText(progress + "%");
            
            // Auto-check completed if progress is 100%
            if (progress == 100) {
                completedCheckBox.setSelected(true);
            }
        });
        
        // Set up completed checkbox
        completedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                progressSlider.setValue(100);
            }
        });
        
        // Set up priority combo box
        priorityComboBox.setItems(FXCollections.observableArrayList(Task.Priority.values()));
        
        // Set up team members table
        memberNameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));
        
        memberSubteamColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getSubteam() != null ? 
                cellData.getValue().getSubteam().getName() : ""));
        
        assignedMembersTable.setItems(assignedMembers);
        
        // Set up components table
        componentNameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        
        componentPartNumberColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPartNumber()));
        
        componentDeliveredColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isDelivered()).asObject());
        
        // Cell factory for boolean delivered column
        componentDeliveredColumn.setCellFactory(column -> new TableCell<Component, Boolean>() {
            @Override
            protected void updateItem(Boolean delivered, boolean empty) {
                super.updateItem(delivered, empty);
                if (empty || delivered == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    CheckBox checkBox = new CheckBox();
                    checkBox.setSelected(delivered);
                    checkBox.setDisable(true); // Read-only
                    setGraphic(checkBox);
                }
            }
        });
        
        requiredComponentsTable.setItems(requiredComponents);
        
        // Set up dependencies table
        dependencyTitleColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        
        dependencyProgressColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getProgress()).asObject());
        
        // Cell factory for progress column
        dependencyProgressColumn.setCellFactory(column -> new TableCell<Task, Integer>() {
            @Override
            protected void updateItem(Integer progress, boolean empty) {
                super.updateItem(progress, empty);
                if (empty || progress == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(progress + "%");
                    ProgressBar progressBar = new ProgressBar(progress / 100.0);
                    progressBar.setPrefWidth(80);
                    setGraphic(progressBar);
                }
            }
        });
        
        dependenciesTable.setItems(dependencies);
        
        // Set up button actions
        saveButton.setOnAction(this::handleSave);
        cancelButton.setOnAction(this::handleCancel);
        addMemberButton.setOnAction(this::handleAddMember);
        removeMemberButton.setOnAction(this::handleRemoveMember);
        addComponentButton.setOnAction(this::handleAddComponent);
        removeComponentButton.setOnAction(this::handleRemoveComponent);
        addDependencyButton.setOnAction(this::handleAddDependency);
        removeDependencyButton.setOnAction(this::handleRemoveDependency);
    }
    
    /**
     * Sets an existing task for editing.
     * 
     * @param task the task to edit
     */
    public void setTask(Task task) {
        this.task = task;
        this.isNewTask = false;
        loadTaskData();
    }
    
    /**
     * Sets up the controller for creating a new task.
     * 
     * @param project the project for the new task
     * @param subsystem the subsystem for the new task
     */
    public void setNewTask(Project project, Subsystem subsystem) {
        this.task = new Task("New Task", project, subsystem);
        this.isNewTask = true;
        
        // Set default values
        task.setEstimatedDuration(Duration.ofHours(1));
        task.setPriority(Task.Priority.MEDIUM);
        task.setStartDate(LocalDate.now());
        task.setEndDate(LocalDate.now().plusWeeks(1));
        
        loadTaskData();
    }
    
    /**
     * Loads task data into the UI.
     */
    private void loadTaskData() {
        if (task == null) {
            return;
        }
        
        taskTitleLabel.setText(isNewTask ? "New Task" : task.getTitle());
        projectLabel.setText(task.getProject().getName());
        subsystemLabel.setText(task.getSubsystem().getName());
        descriptionArea.setText(task.getDescription());
        
        startDatePicker.setValue(task.getStartDate());
        endDatePicker.setValue(task.getEndDate());
        
        priorityComboBox.setValue(task.getPriority());
        
        progressSlider.setValue(task.getProgress());
        progressLabel.setText(task.getProgress() + "%");
        
        completedCheckBox.setSelected(task.isCompleted());
        
        Duration estimatedDuration = task.getEstimatedDuration();
        estimatedHoursField.setText(String.format("%.1f", estimatedDuration.toMinutes() / 60.0));
        
        Duration actualDuration = task.getActualDuration();
        if (actualDuration != null) {
            actualHoursField.setText(String.format("%.1f", actualDuration.toMinutes() / 60.0));
        } else {
            actualHoursField.setText("");
        }
        
        // Load assigned members
        assignedMembers.clear();
        assignedMembers.addAll(task.getAssignedTo());
        
        // Load required components
        requiredComponents.clear();
        requiredComponents.addAll(task.getRequiredComponents());
        
        // Load dependencies
        dependencies.clear();
        dependencies.addAll(task.getPreDependencies());
    }
    
    /**
     * Handles saving the task.
     * 
     * @param event the action event
     */
    public void handleSave(ActionEvent event) {
        if (task == null) {
            return;
        }
        
        try {
            // Update task data from UI
            String title = taskTitleLabel.getText();
            if (title == null || title.trim().isEmpty()) {
                showErrorAlert("Invalid Title", "Task title cannot be empty");
                return;
            }
            task.setTitle(title);
            
            task.setDescription(descriptionArea.getText());
            
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                showErrorAlert("Invalid Dates", "End date cannot be before start date");
                return;
            }
            
            task.setStartDate(startDate);
            task.setEndDate(endDate);
            
            Task.Priority priority = priorityComboBox.getValue();
            if (priority != null) {
                task.setPriority(priority);
            }
            
            int progress = (int) progressSlider.getValue();
            boolean completed = completedCheckBox.isSelected();
            
            String estimatedHoursStr = estimatedHoursField.getText();
            if (estimatedHoursStr != null && !estimatedHoursStr.isEmpty()) {
                try {
                    double estimatedHours = Double.parseDouble(estimatedHoursStr);
                    if (estimatedHours <= 0) {
                        showErrorAlert("Invalid Input", "Estimated hours must be positive");
                        return;
                    }
                    
                    task.setEstimatedDuration(Duration.ofMinutes((long) (estimatedHours * 60)));
                } catch (NumberFormatException e) {
                    showErrorAlert("Invalid Input", "Estimated hours must be a number");
                    return;
                }
            }
            
            String actualHoursStr = actualHoursField.getText();
            if (actualHoursStr != null && !actualHoursStr.isEmpty()) {
                try {
                    double actualHours = Double.parseDouble(actualHoursStr);
                    if (actualHours <= 0) {
                        showErrorAlert("Invalid Input", "Actual hours must be positive");
                        return;
                    }
                    
                    task.setActualDuration(Duration.ofMinutes((long) (actualHours * 60)));
                } catch (NumberFormatException e) {
                    showErrorAlert("Invalid Input", "Actual hours must be a number");
                    return;
                }
            }
            
            // Save the task
            if (isNewTask) {
                task = taskService.createTask(
                    task.getTitle(),
                    task.getProject(),
                    task.getSubsystem(),
                    task.getEstimatedDuration().toMinutes() / 60.0,
                    task.getPriority(),
                    task.getStartDate(),
                    task.getEndDate()
                );
                
                // Update task with additional information
                task.setDescription(descriptionArea.getText());
                task = taskService.save(task);
            }
            
            // Update progress and completion status
            task = taskService.updateTaskProgress(task.getId(), progress, completed);
            
            // Update assignments, components, and dependencies
            taskService.assignMembers(task.getId(), new HashSet<>(assignedMembers));
            
            // Close the dialog
            closeDialog();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving task", e);
            showErrorAlert("Error Saving Task", "Failed to save the task: " + e.getMessage());
        }
    }
    
    /**
     * Handles canceling task editing.
     * 
     * @param event the action event
     */
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }
    
    /**
     * Handles adding a team member to the task.
     * 
     * @param event the action event
     */
    private void handleAddMember(ActionEvent event) {
        try {
            // Create and show team member selection dialog
            List<TeamMember> allMembers = teamMemberService.findAll();
            
            // Remove already assigned members
            allMembers.removeAll(assignedMembers);
            
            if (allMembers.isEmpty()) {
                showInfoAlert("No Available Members", "All team members are already assigned to this task.");
                return;
            }
            
            // For now, just show a simple selection dialog
            // In Phase 3, this should be replaced with a more sophisticated dialog
            ChoiceDialog<TeamMember> dialog = new ChoiceDialog<>(allMembers.get(0), allMembers);
            dialog.setTitle("Add Team Member");
            dialog.setHeaderText("Select a team member to add");
            dialog.setContentText("Team member:");
            
            dialog.showAndWait().ifPresent(member -> {
                assignedMembers.add(member);
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding team member", e);
            showErrorAlert("Error", "Failed to add team member: " + e.getMessage());
        }
    }
    
    /**
     * Handles removing a team member from the task.
     * 
     * @param event the action event
     */
    private void handleRemoveMember(ActionEvent event) {
        TeamMember selectedMember = assignedMembersTable.getSelectionModel().getSelectedItem();
        if (selectedMember == null) {
            showErrorAlert("No Selection", "Please select a team member to remove");
            return;
        }
        
        assignedMembers.remove(selectedMember);
    }
    
    /**
     * Handles adding a component to the task.
     * 
     * @param event the action event
     */
    private void handleAddComponent(ActionEvent event) {
        try {
            // Create and show component selection dialog
            List<Component> allComponents = componentService.findAll();
            
            // Remove already required components
            allComponents.removeAll(requiredComponents);
            
            if (allComponents.isEmpty()) {
                showInfoAlert("No Available Components", "All components are already required for this task.");
                return;
            }
            
            // For now, just show a simple selection dialog
            // In Phase 3, this should be replaced with a more sophisticated dialog
            ChoiceDialog<Component> dialog = new ChoiceDialog<>(allComponents.get(0), allComponents);
            dialog.setTitle("Add Component");
            dialog.setHeaderText("Select a component to add");
            dialog.setContentText("Component:");
            
            dialog.showAndWait().ifPresent(component -> {
                requiredComponents.add(component);
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding component", e);
            showErrorAlert("Error", "Failed to add component: " + e.getMessage());
        }
    }
    
    /**
     * Handles removing a component from the task.
     * 
     * @param event the action event
     */
    private void handleRemoveComponent(ActionEvent event) {
        Component selectedComponent = requiredComponentsTable.getSelectionModel().getSelectedItem();
        if (selectedComponent == null) {
            showErrorAlert("No Selection", "Please select a component to remove");
            return;
        }
        
        requiredComponents.remove(selectedComponent);
    }
    
    /**
     * Handles adding a dependency to the task.
     * 
     * @param event the action event
     */
    private void handleAddDependency(ActionEvent event) {
        try {
            // Create and show task selection dialog
            List<Task> allTasks = taskService.findByProject(task.getProject());
            
            // Remove current task and already added dependencies
            allTasks.remove(task);
            allTasks.removeAll(dependencies);
            
            // Remove tasks that depend on this task (to avoid circular dependencies)
            allTasks.removeAll(task.getPostDependencies());
            
            if (allTasks.isEmpty()) {
                showInfoAlert("No Available Tasks", "No tasks are available to add as dependencies.");
                return;
            }
            
            // For now, just show a simple selection dialog
            // In Phase 3, this should be replaced with a more sophisticated dialog
            ChoiceDialog<Task> dialog = new ChoiceDialog<>(allTasks.get(0), allTasks);
            dialog.setTitle("Add Dependency");
            dialog.setHeaderText("Select a task that must be completed before this task can start");
            dialog.setContentText("Task:");
            
            dialog.showAndWait().ifPresent(dependency -> {
                dependencies.add(dependency);
                taskService.addDependency(task.getId(), dependency.getId());
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding dependency", e);
            showErrorAlert("Error", "Failed to add dependency: " + e.getMessage());
        }
    }
    
    /**
     * Handles removing a dependency from the task.
     * 
     * @param event the action event
     */
    private void handleRemoveDependency(ActionEvent event) {
        Task selectedDependency = dependenciesTable.getSelectionModel().getSelectedItem();
        if (selectedDependency == null) {
            showErrorAlert("No Selection", "Please select a dependency to remove");
            return;
        }
        
        try {
            if (taskService.removeDependency(task.getId(), selectedDependency.getId())) {
                dependencies.remove(selectedDependency);
            } else {
                showErrorAlert("Error", "Failed to remove dependency");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error removing dependency", e);
            showErrorAlert("Error", "Failed to remove dependency: " + e.getMessage());
        }
    }
    
    /**
     * Closes the task dialog.
     */
    private void closeDialog() {
        // Get the current stage
        javafx.stage.Stage stage = (javafx.stage.Stage) saveButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Helper method to create a member selection dialog for testing.
     * This is a placeholder for mocking in tests.
     * 
     * @param availableMembers the list of available members
     * @return the dialog
     */
    public ChoiceDialog<TeamMember> createMemberDialog(List<TeamMember> availableMembers) {
        return new ChoiceDialog<>(
            availableMembers.isEmpty() ? null : availableMembers.get(0), 
            availableMembers);
    }
    
    /**
     * Helper method to create a component selection dialog for testing.
     * This is a placeholder for mocking in tests.
     * 
     * @param availableComponents the list of available components
     * @return the dialog
     */
    public ChoiceDialog<Component> createComponentDialog(List<Component> availableComponents) {
        return new ChoiceDialog<>(
            availableComponents.isEmpty() ? null : availableComponents.get(0), 
            availableComponents);
    }
    
    /**
     * Helper method to create a dependency selection dialog for testing.
     * This is a placeholder for mocking in tests.
     * 
     * @param availableTasks the list of available tasks
     * @return the dialog
     */
    public ChoiceDialog<Task> createDependencyDialog(List<Task> availableTasks) {
        return new ChoiceDialog<>(
            availableTasks.isEmpty() ? null : availableTasks.get(0), 
            availableTasks);
    }
    
    // Getters for testing
    
    /**
     * Gets the task title label.
     * 
     * @return the task title label
     */
    public Label getTaskTitleLabel() {
        return taskTitleLabel;
    }
    
    /**
     * Gets the project label.
     * 
     * @return the project label
     */
    public Label getProjectLabel() {
        return projectLabel;
    }
    
    /**
     * Gets the subsystem label.
     * 
     * @return the subsystem label
     */
    public Label getSubsystemLabel() {
        return subsystemLabel;
    }
    
    /**
     * Gets the description area.
     * 
     * @return the description area
     */
    public TextArea getDescriptionArea() {
        return descriptionArea;
    }
    
    /**
     * Gets the start date picker.
     * 
     * @return the start date picker
     */
    public DatePicker getStartDatePicker() {
        return startDatePicker;
    }
    
    /**
     * Gets the end date picker.
     * 
     * @return the end date picker
     */
    public DatePicker getEndDatePicker() {
        return endDatePicker;
    }
    
    /**
     * Gets the priority combo box.
     * 
     * @return the priority combo box
     */
    public ComboBox<Task.Priority> getPriorityComboBox() {
        return priorityComboBox;
    }
    
    /**
     * Gets the progress slider.
     * 
     * @return the progress slider
     */
    public Slider getProgressSlider() {
        return progressSlider;
    }
    
    /**
     * Gets the progress label.
     * 
     * @return the progress label
     */
    public Label getProgressLabel() {
        return progressLabel;
    }
    
    /**
     * Gets the completed check box.
     * 
     * @return the completed check box
     */
    public CheckBox getCompletedCheckBox() {
        return completedCheckBox;
    }
    
    /**
     * Gets the estimated hours field.
     * 
     * @return the estimated hours field
     */
    public TextField getEstimatedHoursField() {
        return estimatedHoursField;
    }
    
    /**
     * Gets the actual hours field.
     * 
     * @return the actual hours field
     */
    public TextField getActualHoursField() {
        return actualHoursField;
    }
    
    /**
     * Gets the assigned members table.
     * 
     * @return the assigned members table
     */
    public TableView<TeamMember> getAssignedMembersTable() {
        return assignedMembersTable;
    }
    
    /**
     * Gets the member name column.
     * 
     * @return the member name column
     */
    public TableColumn<TeamMember, String> getMemberNameColumn() {
        return memberNameColumn;
    }
    
    /**
     * Gets the member subteam column.
     * 
     * @return the member subteam column
     */
    public TableColumn<TeamMember, String> getMemberSubteamColumn() {
        return memberSubteamColumn;
    }
    
    /**
     * Gets the required components table.
     * 
     * @return the required components table
     */
    public TableView<Component> getRequiredComponentsTable() {
        return requiredComponentsTable;
    }
    
    /**
     * Gets the component name column.
     * 
     * @return the component name column
     */
    public TableColumn<Component, String> getComponentNameColumn() {
        return componentNameColumn;
    }
    
    /**
     * Gets the component part number column.
     * 
     * @return the component part number column
     */
    public TableColumn<Component, String> getComponentPartNumberColumn() {
        return componentPartNumberColumn;
    }
    
    /**
     * Gets the component delivered column.
     * 
     * @return the component delivered column
     */
    public TableColumn<Component, Boolean> getComponentDeliveredColumn() {
        return componentDeliveredColumn;
    }
    
    /**
     * Gets the dependencies table.
     * 
     * @return the dependencies table
     */
    public TableView<Task> getDependenciesTable() {
        return dependenciesTable;
    }
    
    /**
     * Gets the dependency title column.
     * 
     * @return the dependency title column
     */
    public TableColumn<Task, String> getDependencyTitleColumn() {
        return dependencyTitleColumn;
    }
    
    /**
     * Gets the dependency progress column.
     * 
     * @return the dependency progress column
     */
    public TableColumn<Task, Integer> getDependencyProgressColumn() {
        return dependencyProgressColumn;
    }
    
    /**
     * Gets the save button.
     * 
     * @return the save button
     */
    public Button getSaveButton() {
        return saveButton;
    }
    
    /**
     * Gets the cancel button.
     * 
     * @return the cancel button
     */
    public Button getCancelButton() {
        return cancelButton;
    }
    
    /**
     * Gets the add member button.
     * 
     * @return the add member button
     */
    public Button getAddMemberButton() {
        return addMemberButton;
    }
    
    /**
     * Gets the remove member button.
     * 
     * @return the remove member button
     */
    public Button getRemoveMemberButton() {
        return removeMemberButton;
    }
    
    /**
     * Gets the add component button.
     * 
     * @return the add component button
     */
    public Button getAddComponentButton() {
        return addComponentButton;
    }
    
    /**
     * Gets the remove component button.
     * 
     * @return the remove component button
     */
    public Button getRemoveComponentButton() {
        return removeComponentButton;
    }
    
    /**
     * Gets the add dependency button.
     * 
     * @return the add dependency button
     */
    public Button getAddDependencyButton() {
        return addDependencyButton;
    }
    /**
     * Gets the remove dependency button.
     * 
     * @return the remove dependency button
     */
    public Button getRemoveDependencyButton() {
        return removeDependencyButton;
    }

    /**
     * Public method to access initialize for testing.
     */
    public void testInitialize() {
        initialize();
    }

    /**
     * Public method to access loadTaskData for testing.
     */
    public void testLoadTaskData() {
        loadTaskData();
    }

    /**
     * Public method to access handleSave for testing.
     * 
     * @param event the action event
     */
    public void testHandleSave(ActionEvent event) {
        handleSave(event);
    }

    /**
     * Public method to access handleCancel for testing.
     * 
     * @param event the action event
     */
    public void testHandleCancel(ActionEvent event) {
        handleCancel(event);
    }

    /**
     * Public method to access closeDialog for testing.
     */
    public void testCloseDialog() {
        closeDialog();
    }

    /**
     * Public method to access showErrorAlert for testing.
     * 
     * @param title the title
     * @param message the message
     */
    public void testShowErrorAlert(String title, String message) {
        showErrorAlert(title, message);
    }

    /**
     * Public method to access showInfoAlert for testing.
     * 
     * @param title the title
     * @param message the message
     */
    public void testShowInfoAlert(String title, String message) {
        showInfoAlert(title, message);
    }

    /**
     * Public method to access handleAddMember for testing.
     * 
     * @param event the action event
     */
    public void testHandleAddMember(ActionEvent event) {
        handleAddMember(event);
    }

    /**
     * Public method to access handleRemoveMember for testing.
     * 
     * @param event the action event
     */
    public void testHandleRemoveMember(ActionEvent event) {
        handleRemoveMember(event);
    }

    /**
     * Public method to access handleAddComponent for testing.
     * 
     * @param event the action event
     */
    public void testHandleAddComponent(ActionEvent event) {
        handleAddComponent(event);
    }

    /**
     * Public method to access handleRemoveComponent for testing.
     * 
     * @param event the action event
     */
    public void testHandleRemoveComponent(ActionEvent event) {
        handleRemoveComponent(event);
    }

    /**
     * Public method to access handleAddDependency for testing.
     * 
     * @param event the action event
     */
    public void testHandleAddDependency(ActionEvent event) {
        handleAddDependency(event);
    }

    /**
     * Public method to access handleRemoveDependency for testing.
     * 
     * @param event the action event
     */
    public void testHandleRemoveDependency(ActionEvent event) {
        handleRemoveDependency(event);
    }
}