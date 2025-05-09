// src/main/java/org/frcpm/mvvm/viewmodels/TaskDetailMvvmViewModel.java

package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.ComponentService;
import org.frcpm.services.TaskService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.TaskServiceAsyncImpl;

/**
 * ViewModel for the TaskDetail view using MVVMFx.
 */
public class TaskDetailMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(TaskDetailMvvmViewModel.class.getName());
    
    // Service dependencies
    private final TaskService taskService;
    private final TaskServiceAsyncImpl taskServiceAsync;
    private final TeamMemberService teamMemberService;
    private final ComponentService componentService;
    
    // Observable properties for task fields
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final DoubleProperty estimatedHours = new SimpleDoubleProperty(1.0);
    private final DoubleProperty actualHours = new SimpleDoubleProperty();
    private final ObjectProperty<Task.Priority> priority = new SimpleObjectProperty<>(Task.Priority.MEDIUM);
    private final IntegerProperty progress = new SimpleIntegerProperty(0);
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final BooleanProperty completed = new SimpleBooleanProperty(false);
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final ObjectProperty<Subsystem> subsystem = new SimpleObjectProperty<>();
    private final BooleanProperty isNewTask = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final ObjectProperty<Task> task = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Selection properties for tables
    private final ObjectProperty<TeamMember> selectedMember = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> selectedComponent = new SimpleObjectProperty<>();
    private final ObjectProperty<Task> selectedDependency = new SimpleObjectProperty<>();
    
    // Observable collections for tables
    private final ObservableList<TeamMember> assignedMembers = FXCollections.observableArrayList();
    private final ObservableList<Task> preDependencies = FXCollections.observableArrayList();
    private final ObservableList<Component> requiredComponents = FXCollections.observableArrayList();
    
    // Commands
    private Command saveCommand;
    private Command cancelCommand;
    private Command addMemberCommand;
    private Command removeMemberCommand;
    private Command addDependencyCommand;
    private Command removeDependencyCommand;
    private Command addComponentCommand;
    private Command removeComponentCommand;
    
    /**
     * Creates a new TaskDetailMvvmViewModel.
     * 
     * @param taskService the task service
     * @param teamMemberService the team member service
     * @param componentService the component service
     */
    @Inject
    public TaskDetailMvvmViewModel(TaskService taskService, TeamMemberService teamMemberService, 
                                 ComponentService componentService) {
        this.taskService = taskService;
        this.taskServiceAsync = (TaskServiceAsyncImpl) taskService;
        this.teamMemberService = teamMemberService;
        this.componentService = componentService;
        
        initializeCommands();
        setupValidation();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Save command
        saveCommand = createValidAndDirtyCommand(this::save, this::isValid);
        
        // Cancel command - implemented by the view to close dialog
        cancelCommand = MvvmAsyncHelper.createSimpleAsyncCommand(() -> {
            LOGGER.info("Cancel command executed");
        });
        
        // Member commands
        addMemberCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Add member command executed");
            },
            () -> true
        );
        
        removeMemberCommand = createValidOnlyCommand(
            this::removeMember,
            this::canRemoveMember
        );
        
        // Dependency commands
        addDependencyCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Add dependency command executed");
            },
            () -> true
        );
        
        removeDependencyCommand = createValidOnlyCommand(
            this::removeDependency,
            this::canRemoveDependency
        );
        
        // Component commands
        addComponentCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Add component command executed");
            },
            () -> true
        );
        
        removeComponentCommand = createValidOnlyCommand(
            this::removeComponent,
            this::canRemoveComponent
        );
    }
    
    /**
     * Sets up validation for this view model.
     */
    private void setupValidation() {
        // Validation listener for all required fields
        Runnable validateAndMarkDirty = createDirtyFlagHandler(this::validate);
        
        // Add listeners to properties that affect validation
        title.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        project.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        subsystem.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        estimatedHours.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        startDate.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        endDate.addListener((obs, oldVal, newVal) -> validateAndMarkDirty.run());
        
        // Add listeners to properties that don't affect validation but mark as dirty
        description.addListener((obs, oldVal, newVal) -> setDirty(true));
        actualHours.addListener((obs, oldVal, newVal) -> setDirty(true));
        priority.addListener((obs, oldVal, newVal) -> setDirty(true));
        progress.addListener((obs, oldVal, newVal) -> setDirty(true));
        completed.addListener((obs, oldVal, newVal) -> setDirty(true));
        
        // Add listeners to collections
        assignedMembers.addListener((javafx.collections.ListChangeListener<TeamMember>) c -> setDirty(true));
        preDependencies.addListener((javafx.collections.ListChangeListener<Task>) c -> setDirty(true));
        requiredComponents.addListener((javafx.collections.ListChangeListener<Component>) c -> setDirty(true));
        
        // Initial validation
        validate();
    }
    
    /**
     * Validates the task data.
     * Sets the valid property and error message accordingly.
     */
    private void validate() {
        StringBuilder errorMessages = new StringBuilder();
        
        // Check required fields
        if (title.get() == null || title.get().trim().isEmpty()) {
            errorMessages.append("Task title cannot be empty\n");
        }
        
        if (project.get() == null) {
            errorMessages.append("Project cannot be null\n");
        }
        
        if (subsystem.get() == null) {
            errorMessages.append("Subsystem cannot be null\n");
        }
        
        if (estimatedHours.get() <= 0) {
            errorMessages.append("Estimated hours must be positive\n");
        }
        
        // Check dates
        if (startDate.get() == null) {
            errorMessages.append("Start date cannot be empty\n");
        }
        
        if (startDate.get() != null && endDate.get() != null && 
                endDate.get().isBefore(startDate.get())) {
            errorMessages.append("End date cannot be before start date\n");
        }
        
        // Update valid property and error message
        boolean isValid = errorMessages.length() == 0;
        valid.set(isValid);
        if (!isValid) {
            setErrorMessage(errorMessages.toString().trim());
        } else {
            clearErrorMessage();
        }
    }
    
    /**
     * Initializes the view model for a new task.
     * 
     * @param project the project
     * @param subsystem the subsystem
     */
    public void initNewTask(Project project, Subsystem subsystem) {
        this.project.set(project);
        this.subsystem.set(subsystem);
        this.task.set(null);
        this.isNewTask.set(true);
        
        // Set default values
        title.set("");
        description.set("");
        estimatedHours.set(1.0);
        actualHours.set(0.0);
        priority.set(Task.Priority.MEDIUM);
        progress.set(0);
        startDate.set(LocalDate.now());
        endDate.set(null);
        completed.set(false);
        
        // Clear collections
        assignedMembers.clear();
        preDependencies.clear();
        requiredComponents.clear();
        
        // Clear dirty flag
        setDirty(false);
        validate();
    }
    
    /**
     * Initializes the view model for editing an existing task.
     * 
     * @param task the task to edit
     */
    public void initExistingTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        this.task.set(task);
        this.project.set(task.getProject());
        this.subsystem.set(task.getSubsystem());
        this.isNewTask.set(false);
        
        // Set field values from task
        title.set(task.getTitle());
        description.set(task.getDescription() != null ? task.getDescription() : "");
        
        Duration estimated = task.getEstimatedDuration();
        estimatedHours.set(estimated != null ? estimated.toMinutes() / 60.0 : 1.0);
        
        Duration actual = task.getActualDuration();
        actualHours.set(actual != null ? actual.toMinutes() / 60.0 : 0.0);
        
        priority.set(task.getPriority() != null ? task.getPriority() : Task.Priority.MEDIUM);
        progress.set(task.getProgress());
        startDate.set(task.getStartDate());
        endDate.set(task.getEndDate());
        completed.set(task.isCompleted());
        
        // Set collections
        assignedMembers.clear();
        if (task.getAssignedTo() != null) {
            assignedMembers.addAll(task.getAssignedTo());
        }
        
        preDependencies.clear();
        if (task.getPreDependencies() != null) {
            preDependencies.addAll(task.getPreDependencies());
        }
        
        requiredComponents.clear();
        if (task.getRequiredComponents() != null) {
            requiredComponents.addAll(task.getRequiredComponents());
        }
        
        // Clear dirty flag
        setDirty(false);
        validate();
    }
    
    /**
     * Saves the task.
     */
    private void save() {
        if (!valid.get()) {
            return;
        }
        
        loading.set(true);
        
        try {
            if (isNewTask.get()) {
                // Create new task asynchronously
                taskServiceAsync.createTaskAsync(
                    title.get(),
                    project.get(),
                    subsystem.get(),
                    estimatedHours.get(),
                    priority.get(),
                    startDate.get(),
                    endDate.get(),
                    // Success handler
                    savedTask -> {
                        Platform.runLater(() -> {
                            // Update description
                            savedTask.setDescription(description.get());
                            
                            // Handle assigned members
                            if (!assignedMembers.isEmpty()) {
                                Set<TeamMember> members = new HashSet<>(assignedMembers);
                                handleAssignMembersAsync(savedTask, members);
                            } else {
                                // If no members to assign, continue with dependencies
                                handleDependenciesAsync(savedTask);
                            }
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error creating task asynchronously", error);
                            loading.set(false);
                            setErrorMessage("Failed to create task: " + error.getMessage());
                        });
                    }
                );
            } else {
                // Update existing task asynchronously
                Task existingTask = task.get();
                existingTask.setTitle(title.get());
                existingTask.setDescription(description.get());
                existingTask.setEstimatedDuration(Duration.ofMinutes((long) (estimatedHours.get() * 60)));
                
                if (actualHours.get() > 0) {
                    existingTask.setActualDuration(Duration.ofMinutes((long) (actualHours.get() * 60)));
                }
                
                existingTask.setPriority(priority.get());
                existingTask.setStartDate(startDate.get());
                existingTask.setEndDate(endDate.get());
                
                // Update progress and completion status asynchronously
                taskServiceAsync.updateTaskProgressAsync(
                    existingTask.getId(),
                    progress.get(),
                    completed.get(),
                    // Success handler
                    updatedTask -> {
                        Platform.runLater(() -> {
                            // Handle assigned members
                            if (!assignedMembers.isEmpty()) {
                                Set<TeamMember> members = new HashSet<>(assignedMembers);
                                handleAssignMembersAsync(updatedTask, members);
                            } else {
                                // If no members to assign, continue with dependencies
                                handleDependenciesAsync(updatedTask);
                            }
                        });
                    },
                    // Error handler
                    error -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.SEVERE, "Error updating task progress asynchronously", error);
                            loading.set(false);
                            setErrorMessage("Failed to update task progress: " + error.getMessage());
                        });
                    }
                );
            }
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error in save method", e);
            setErrorMessage("Failed to save task: " + e.getMessage());
        }
    }
    
    /**
     * Handles assigning members to a task asynchronously.
     * 
     * @param task the task to update
     * @param members the members to assign
     */
    private void handleAssignMembersAsync(Task task, Set<TeamMember> members) {
        taskServiceAsync.assignMembersAsync(
            task.getId(),
            members,
            // Success handler
            updatedTask -> {
                // Continue with dependencies
                Platform.runLater(() -> {
                    handleDependenciesAsync(updatedTask);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error assigning members asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to assign members: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Handles dependencies for a task.
     * 
     * @param task the task to update
     */
    private void handleDependenciesAsync(Task task) {
        // We would need to add dependency handling to TaskServiceAsyncImpl
        // For now, just continue to the next step
        handleComponentsAsync(task);
    }
    
    /**
     * Handles components for a task.
     * 
     * @param task the task to update
     */
    private void handleComponentsAsync(Task task) {
        if (requiredComponents.isEmpty()) {
            // No components to add, finalize the task
            finalizeTaskSave(task);
            return;
        }
        
        // Convert components to IDs
        Set<Long> componentIds = new HashSet<>();
        for (Component component : requiredComponents) {
            if (component.getId() != null) {
                componentIds.add(component.getId());
            }
        }
        
        // Use the async service method
        taskServiceAsync.associateComponentsWithTaskAsync(
            task.getId(),
            componentIds,
            // Success handler
            updatedTask -> {
                Platform.runLater(() -> {
                    finalizeTaskSave(updatedTask);
                });
            },
            // Error handler
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error associating components asynchronously", error);
                    loading.set(false);
                    setErrorMessage("Failed to associate components: " + error.getMessage());
                });
            }
        );
    }
    
    /**
     * Finalizes the task save operation.
     * 
     * @param task the saved task
     */
    private void finalizeTaskSave(Task task) {
        // Update task property with saved task
        this.task.set(task);
        
        // Clear dirty flag and loading indicator
        setDirty(false);
        loading.set(false);
        
        LOGGER.info("Task saved successfully: " + task.getTitle());
    }
    
    /**
     * Adds a member to the task.
     * 
     * @param member the member to add
     */
    public void addMember(TeamMember member) {
        if (member != null && !assignedMembers.contains(member)) {
            assignedMembers.add(member);
            setDirty(true);
        }
    }
    
    /**
     * Removes the selected member from the task.
     */
    private void removeMember() {
        TeamMember member = selectedMember.get();
        if (member != null) {
            assignedMembers.remove(member);
            setDirty(true);
        }
    }
    
    /**
     * Checks if a member can be removed.
     * 
     * @return true if a member is selected, false otherwise
     */
    private boolean canRemoveMember() {
        return selectedMember.get() != null;
    }
    
    /**
     * Adds a dependency to the task.
     * 
     * @param dependency the dependency to add
     */
    public void addDependency(Task dependency) {
        if (dependency == null) {
            return;
        }
        
        // Prevent adding itself as a dependency
        if (task.get() != null && task.get().equals(dependency)) {
            setErrorMessage("Cannot add a task as a dependency to itself");
            return;
        }
        
        // Prevent adding a dependency that already exists
        if (preDependencies.contains(dependency)) {
            return;
        }
        
        preDependencies.add(dependency);
        setDirty(true);
    }
    
    /**
     * Removes the selected dependency from the task.
     */
    private void removeDependency() {
        Task dependency = selectedDependency.get();
        if (dependency != null) {
            preDependencies.remove(dependency);
            setDirty(true);
        }
    }
    
    /**
     * Checks if a dependency can be removed.
     * 
     * @return true if a dependency is selected, false otherwise
     */
    private boolean canRemoveDependency() {
        return selectedDependency.get() != null;
    }
    
    /**
     * Adds a component to the task.
     * 
     * @param component the component to add
     */
    public void addComponent(Component component) {
        if (component != null && !requiredComponents.contains(component)) {
            requiredComponents.add(component);
            setDirty(true);
        }
    }
    
    /**
     * Removes the selected component from the task.
     */
    private void removeComponent() {
        Component component = selectedComponent.get();
        if (component != null) {
            requiredComponents.remove(component);
            setDirty(true);
        }
    }
    
    /**
     * Checks if a component can be removed.
     * 
     * @return true if a component is selected, false otherwise
     */
    private boolean canRemoveComponent() {
        return selectedComponent.get() != null;
    }
    
    /**
     * Gets the value of the valid property.
     * 
     * @return true if the task is valid, false otherwise
     */
    public boolean isValid() {
        return valid.get();
    }
    
    /**
     * Gets the valid property.
     * 
     * @return the valid property
     */
    public BooleanProperty validProperty() {
        return valid;
    }
    
    /**
     * Gets the loading property.
     * 
     * @return the loading property
     */
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    /**
     * Gets whether the view model is loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }

    /**
     * Gets the task.
     * 
     * @return the task
     */
    public Task getTask() {
        return task.get();
    }
    
    /**
     * Gets the task property.
     * 
     * @return the task property
     */
    public ObjectProperty<Task> taskProperty() {
        return task;
    }
    
    /**
     * Sets the task.
     * 
     * @param task the task
     */
    public void setTask(Task task) {
        this.task.set(task);
    }
    
    /**
     * Gets whether this is a new task.
     * 
     * @return true if this is a new task, false if editing an existing task
     */
    public boolean isNewTask() {
        return isNewTask.get();
    }
    
    /**
     * Gets the new task property.
     * 
     * @return the new task property
     */
    public BooleanProperty isNewTaskProperty() {
        return isNewTask;
    }
    
    /**
     * Gets the save command.
     * 
     * @return the save command
     */
    public Command getSaveCommand() {
        return saveCommand;
    }
    
    /**
     * Gets the cancel command.
     * 
     * @return the cancel command
     */
    public Command getCancelCommand() {
        return cancelCommand;
    }
    
    /**
     * Gets the add member command.
     * 
     * @return the add member command
     */
    public Command getAddMemberCommand() {
        return addMemberCommand;
    }
    
    /**
     * Gets the remove member command.
     * 
     * @return the remove member command
     */
    public Command getRemoveMemberCommand() {
        return removeMemberCommand;
    }
    
    /**
     * Gets the add dependency command.
     * 
     * @return the add dependency command
     */
    public Command getAddDependencyCommand() {
        return addDependencyCommand;
    }
    
    /**
     * Gets the remove dependency command.
     * 
     * @return the remove dependency command
     */
    public Command getRemoveDependencyCommand() {
        return removeDependencyCommand;
    }
    
    /**
     * Gets the add component command.
     * 
     * @return the add component command
     */
    public Command getAddComponentCommand() {
        return addComponentCommand;
    }
    
    /**
     * Gets the remove component command.
     * 
     * @return the remove component command
     */
    public Command getRemoveComponentCommand() {
        return removeComponentCommand;
    }
    
    /**
     * Gets the assigned members.
     * 
     * @return the assigned members
     */
    public ObservableList<TeamMember> getAssignedMembers() {
        return assignedMembers;
    }
    
    /**
     * Gets the pre-dependencies.
     * 
     * @return the pre-dependencies
     */
    public ObservableList<Task> getPreDependencies() {
        return preDependencies;
    }
    
    /**
     * Gets the required components.
     * 
     * @return the required components
     */
    public ObservableList<Component> getRequiredComponents() {
        return requiredComponents;
    }
    
    /**
     * Gets the selected member property.
     * 
     * @return the selected member property
     */
    public ObjectProperty<TeamMember> selectedMemberProperty() {
        return selectedMember;
    }
    
    /**
     * Gets the selected member.
     * 
     * @return the selected member
     */
    public TeamMember getSelectedMember() {
        return selectedMember.get();
    }
    
    /**
     * Sets the selected member.
     * 
     * @param member the member to select
     */
    public void setSelectedMember(TeamMember member) {
        selectedMember.set(member);
    }
    
    /**
     * Gets the selected component property.
     * 
     * @return the selected component property
     */
    public ObjectProperty<Component> selectedComponentProperty() {
        return selectedComponent;
    }
    
    /**
     * Gets the selected component.
     * 
     * @return the selected component
     */
    public Component getSelectedComponent() {
        return selectedComponent.get();
    }
    
    /**
     * Sets the selected component.
     * 
     * @param component the component to select
     */
    public void setSelectedComponent(Component component) {
        selectedComponent.set(component);
    }
    
    /**
     * Gets the selected dependency property.
     * 
     * @return the selected dependency property
     */
    public ObjectProperty<Task> selectedDependencyProperty() {
        return selectedDependency;
    }
    
    /**
     * Gets the selected dependency.
     * 
     * @return the selected dependency
     */
    public Task getSelectedDependency() {
        return selectedDependency.get();
    }
    
    /**
     * Sets the selected dependency.
     * 
     * @param dependency the dependency to select
     */
    public void setSelectedDependency(Task dependency) {
        selectedDependency.set(dependency);
    }
    
    // Property getters and setters
    
    public StringProperty titleProperty() {
        return title;
    }
    
    public String getTitle() {
        return title.get();
    }
    
    public void setTitle(String value) {
        title.set(value);
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String value) {
        description.set(value);
    }
    
    public DoubleProperty estimatedHoursProperty() {
        return estimatedHours;
    }
    
    public double getEstimatedHours() {
        return estimatedHours.get();
    }
    
    public void setEstimatedHours(double value) {
        estimatedHours.set(value);
    }
    
    public DoubleProperty actualHoursProperty() {
        return actualHours;
    }
    
    public double getActualHours() {
        return actualHours.get();
    }
    
    public void setActualHours(double value) {
        actualHours.set(value);
    }
    
    public ObjectProperty<Task.Priority> priorityProperty() {
        return priority;
    }
    
    public Task.Priority getPriority() {
        return priority.get();
    }
    
    public void setPriority(Task.Priority value) {
        priority.set(value);
    }
    
    public IntegerProperty progressProperty() {
        return progress;
    }
    
    public int getProgress() {
        return progress.get();
    }
    
    public void setProgress(int value) {
        // Ensure progress is between 0 and 100
        int clampedValue = Math.max(0, Math.min(100, value));
        progress.set(clampedValue);
        
        // If progress is 100%, set completed to true
        if (clampedValue == 100) {
            completed.set(true);
        }
    }
    
    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }
    
    public LocalDate getStartDate() {
        return startDate.get();
    }
    
    public void setStartDate(LocalDate value) {
        startDate.set(value);
    }
    
    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }
    
    public LocalDate getEndDate() {
        return endDate.get();
    }
    
    public void setEndDate(LocalDate value) {
        endDate.set(value);
    }
    
    public BooleanProperty completedProperty() {
        return completed;
    }
    
    public boolean isCompleted() {
        return completed.get();
    }
    
    public void setCompleted(boolean value) {
        completed.set(value);
        
        // If marking as completed, set progress to 100%
        if (value) {
            progress.set(100);
        }
    }
    
    public ObjectProperty<Project> projectProperty() {
        return project;
    }
    
    public Project getProject() {
        return project.get();
    }
    
    public void setProject(Project value) {
        project.set(value);
    }
    
    public ObjectProperty<Subsystem> subsystemProperty() {
        return subsystem;
    }
    
    public Subsystem getSubsystem() {
        return subsystem.get();
    }
    
    public void setSubsystem(Subsystem value) {
        subsystem.set(value);
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void dispose() {
        super.dispose();
        assignedMembers.clear();
        preDependencies.clear();
        requiredComponents.clear();
    }
}