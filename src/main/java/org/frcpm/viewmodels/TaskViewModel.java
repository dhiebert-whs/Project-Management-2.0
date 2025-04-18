// src/main/java/org/frcpm/viewmodels/TaskViewModel.java
package org.frcpm.viewmodels;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.frcpm.binding.Command;
import org.frcpm.models.*;
import org.frcpm.services.ComponentService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.TaskService;
import org.frcpm.services.TeamMemberService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for the Task view.
 */
public class TaskViewModel extends BaseViewModel {

    private static final Logger LOGGER = Logger.getLogger(TaskViewModel.class.getName());

    // Services
    private final TaskService taskService;
    private final TeamMemberService teamMemberService;
    private final ComponentService componentService;

    // Observable properties
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final DoubleProperty estimatedHours = new SimpleDoubleProperty(1.0);
    private final DoubleProperty actualHours = new SimpleDoubleProperty();
    private final ObjectProperty<Task.Priority> priority = new SimpleObjectProperty<>(Task.Priority.MEDIUM);
    private final IntegerProperty progress = new SimpleIntegerProperty(0);
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final BooleanProperty completed = new SimpleBooleanProperty(false);
    private final ObjectProperty<Project> project = new SimpleObjectProperty<>();
    private final ObjectProperty<Subsystem> subsystem = new SimpleObjectProperty<>();
    private final ObjectProperty<Task> task = new SimpleObjectProperty<>();
    private final BooleanProperty isNewTask = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final ObjectProperty<TeamMember> selectedMember = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> selectedComponent = new SimpleObjectProperty<>();
    private final ObjectProperty<Task> selectedDependency = new SimpleObjectProperty<>();

    // Observable collections
    private final ObservableList<TeamMember> assignedMembers = FXCollections.observableArrayList();
    private final ObservableList<Task> preDependencies = FXCollections.observableArrayList();
    private final ObservableList<Component> requiredComponents = FXCollections.observableArrayList();

    // Commands
    private final Command saveCommand;
    private final Command cancelCommand;
    private final Command addMemberCommand;
    private final Command removeMemberCommand;
    private final Command addDependencyCommand;
    private final Command removeDependencyCommand;
    private final Command addComponentCommand;
    private final Command removeComponentCommand;

    /**
     * Creates a new TaskViewModel with default services.
     */
    public TaskViewModel() {
        this(
                ServiceFactory.getTaskService(),
                ServiceFactory.getTeamMemberService(),
                ServiceFactory.getComponentService());
    }

    /**
     * Creates a new TaskViewModel with the specified services.
     * This constructor is mainly used for testing.
     * 
     * @param taskService       the task service
     * @param teamMemberService the team member service
     * @param componentService  the component service
     */
    public TaskViewModel(TaskService taskService, TeamMemberService teamMemberService,
            ComponentService componentService) {
        this.taskService = taskService;
        this.teamMemberService = teamMemberService;
        this.componentService = componentService;

        // Create commands
        saveCommand = new Command(this::save, this::isValid);
        cancelCommand = new Command(() -> {
        });
        addMemberCommand = new Command(this::addMember);
        removeMemberCommand = new Command(this::removeMember, this::canRemoveMember);
        addDependencyCommand = new Command(this::addDependency);
        removeDependencyCommand = new Command(this::removeDependency, this::canRemoveDependency);
        addComponentCommand = new Command(this::addComponent);
        removeComponentCommand = new Command(this::removeComponent, this::canRemoveComponent);

        // Set up validation listeners
        title.addListener((observable, oldValue, newValue) -> validate());
        project.addListener((observable, oldValue, newValue) -> validate());
        subsystem.addListener((observable, oldValue, newValue) -> validate());
        estimatedHours.addListener((observable, oldValue, newValue) -> validate());
        startDate.addListener((observable, oldValue, newValue) -> validate());
        endDate.addListener((observable, oldValue, newValue) -> validate());

        // Property change listeners for dirty flag
        description.addListener((observable, oldValue, newValue) -> setDirty(true));
        priority.addListener((observable, oldValue, newValue) -> setDirty(true));
        progress.addListener((observable, oldValue, newValue) -> setDirty(true));
        completed.addListener((observable, oldValue, newValue) -> setDirty(true));
        actualHours.addListener((observable, oldValue, newValue) -> setDirty(true));

        // Collection change listeners
        assignedMembers
                .addListener((javafx.collections.ListChangeListener.Change<? extends TeamMember> c) -> setDirty(true));
        preDependencies.addListener((javafx.collections.ListChangeListener.Change<? extends Task> c) -> setDirty(true));
        requiredComponents
                .addListener((javafx.collections.ListChangeListener.Change<? extends Component> c) -> setDirty(true));

        // Set default date
        startDate.set(LocalDate.now());
        
        // Initial validation
        validate();
    }

    /**
     * Sets up the ViewModel for creating a new task.
     * 
     * @param project   the project for the task
     * @param subsystem the subsystem for the task
     */
    public void initNewTask(Project project, Subsystem subsystem) {
        this.project.set(project);
        this.subsystem.set(subsystem);
        task.set(null);
        isNewTask.set(true);

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

        // Clear dirty flag and validate
        setDirty(false);
        validate();
    }

    /**
     * Sets up the ViewModel for editing an existing task.
     * 
     * @param task the task to edit
     */
    public void initExistingTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        this.task.set(task);
        project.set(task.getProject());
        subsystem.set(task.getSubsystem());
        isNewTask.set(false);

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

        // Clear dirty flag and validate
        setDirty(false);
        validate();
    }

    /**
     * Validates the task data.
     * Sets the valid property and error message.
     */
    private void validate() {
        List<String> errors = new ArrayList<>();

        // Check required fields
        if (title.get() == null || title.get().trim().isEmpty()) {
            errors.add("Task title cannot be empty");
        }

        if (project.get() == null) {
            errors.add("Project cannot be null");
        }

        if (subsystem.get() == null) {
            errors.add("Subsystem cannot be null");
        }

        if (estimatedHours.get() <= 0) {
            errors.add("Estimated hours must be positive");
        }

        // Check dates
        if (startDate.get() == null) {
            errors.add("Start date cannot be empty");
        }

        if (startDate.get() != null && endDate.get() != null &&
                endDate.get().isBefore(startDate.get())) {
            errors.add("End date cannot be before start date");
        }

        // Update valid property and error message
        valid.set(errors.isEmpty());
        if (!errors.isEmpty()) {
            setErrorMessage(String.join("\n", errors));
        } else {
            clearErrorMessage();  // This should now correctly set to null via BaseViewModel
        }
    }
    /**
     * Saves the task.
     * Called when the save command is executed.
     */
    private void save() {
        if (!valid.get()) {
            return;
        }

        try {
            Task savedTask;
            if (isNewTask.get()) {
                // Create new task
                savedTask = taskService.createTask(
                        title.get(),
                        project.get(),
                        subsystem.get(),
                        estimatedHours.get(),
                        priority.get(),
                        startDate.get(),
                        endDate.get());
                savedTask.setDescription(description.get());

                // Save again to update description
                savedTask = taskService.save(savedTask);

                // Only handle members if there are any to assign
                if (!assignedMembers.isEmpty()) {
                    Set<TeamMember> members = new HashSet<>(assignedMembers);
                    savedTask = taskService.assignMembers(savedTask.getId(), members);
                }

                // Only handle dependencies if there are any
                if (!preDependencies.isEmpty()) {
                    for (Task dependency : preDependencies) {
                        if (dependency.getId() != null) {
                            taskService.addDependency(savedTask.getId(), dependency.getId());
                        }
                    }
                }

                // Only handle components if there are any
                if (!requiredComponents.isEmpty()) {
                    for (Component component : requiredComponents) {
                        savedTask.addRequiredComponent(component);
                    }
                }

                // Final save to ensure all changes are persisted
                savedTask = taskService.save(savedTask);

            } else {
                // Update existing task
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

                // Update progress and completion status
                savedTask = taskService.updateTaskProgress(
                        existingTask.getId(),
                        progress.get(),
                        completed.get());

                // Only handle members if there are any to assign
                if (!assignedMembers.isEmpty()) {
                    Set<TeamMember> members = new HashSet<>(assignedMembers);
                    savedTask = taskService.assignMembers(savedTask.getId(), members);
                }

                // Only handle dependencies if there are any to add
                if (!preDependencies.isEmpty()) {
                    for (Task dependency : preDependencies) {
                        if (dependency.getId() != null &&
                                !task.get().getPreDependencies().contains(dependency)) {
                            taskService.addDependency(savedTask.getId(), dependency.getId());
                        }
                    }
                }

                // Only handle components if there are any to add
                if (!requiredComponents.isEmpty()) {
                    for (Component component : requiredComponents) {
                        if (!task.get().getRequiredComponents().contains(component)) {
                            savedTask.addRequiredComponent(component);
                        }
                    }
                }

                // Final save to ensure all changes are persisted
                savedTask = taskService.save(savedTask);
            }

            // Update task property with saved task
            task.set(savedTask);

            // Clear dirty flag
            setDirty(false);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving task", e);
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "Unknown error occurred while saving task";
            }
            setErrorMessage("Failed to save task: " + errorMsg);
            valid.set(false);
        }
    }

    // Helper methods for commands

    private void addMember() {
        // This is just a placeholder - in the actual implementation,
        // this would open a dialog to select a team member
    }

    public void addMember(TeamMember member) {
        if (member != null && !assignedMembers.contains(member)) {
            assignedMembers.add(member);
        }
    }

    private void removeMember() {
        if (selectedMember.get() != null) {
            assignedMembers.remove(selectedMember.get());
        }
    }

    public void removeMember(TeamMember member) {
        assignedMembers.remove(member);
    }

    private boolean canRemoveMember() {
        return selectedMember.get() != null;
    }

    private void addDependency() {
        // Placeholder for adding a dependency
    }

    public void addDependency(Task dependency) {
        if (dependency == null) {
            return;
        }

        // Prevent adding itself as a dependency
        if (task.get() != null && task.get().equals(dependency)) {
            setErrorMessage("Cannot add task as a dependency to itself");
            return;
        }

        // Prevent adding a dependency that already exists
        if (preDependencies.contains(dependency)) {
            return;
        }

        // Prevent circular dependencies
        if (task.get() != null) {
            // Direct circular dependency - task depends on dependency, which already
            // depends on task
            if (dependency.getPostDependencies().contains(task.get())) {
                setErrorMessage("Adding this dependency would create a circular dependency");
                return;
            }

            // Check for potential indirect circular dependencies
            for (Task transitiveTask : dependency.getPreDependencies()) {
                if (transitiveTask.equals(task.get())) {
                    setErrorMessage("Adding this dependency would create a circular dependency");
                    return;
                }
            }
        }

        preDependencies.add(dependency);
    }

    private void removeDependency() {
        if (selectedDependency.get() != null) {
            preDependencies.remove(selectedDependency.get());
        }
    }

    public void removeDependency(Task dependency) {
        preDependencies.remove(dependency);
    }

    private boolean canRemoveDependency() {
        return selectedDependency.get() != null;
    }

    private void addComponent() {
        // Placeholder for adding a component
    }

    public void addComponent(Component component) {
        if (component != null && !requiredComponents.contains(component)) {
            requiredComponents.add(component);
        }
    }

    private void removeComponent() {
        if (selectedComponent.get() != null) {
            requiredComponents.remove(selectedComponent.get());
        }
    }

    public void removeComponent(Component component) {
        requiredComponents.remove(component);
    }

    private boolean canRemoveComponent() {
        return selectedComponent.get() != null;
    }

    // Property getters and accessors

    /**
     * Gets whether the input is valid.
     * 
     * @return true if the input is valid, false otherwise
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
     * Gets the new task flag.
     * 
     * @return true if this is a new task, false otherwise
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

    // Title property accessors
    public StringProperty titleProperty() {
        return title;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String value) {
        title.set(value);
    }

    // Description property accessors
    public StringProperty descriptionProperty() {
        return description;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String value) {
        description.set(value);
    }

    // Estimated hours property accessors
    public DoubleProperty estimatedHoursProperty() {
        return estimatedHours;
    }

    public double getEstimatedHours() {
        return estimatedHours.get();
    }

    public void setEstimatedHours(double value) {
        estimatedHours.set(value);
    }

    // Actual hours property accessors
    public DoubleProperty actualHoursProperty() {
        return actualHours;
    }

    public double getActualHours() {
        return actualHours.get();
    }

    public void setActualHours(double value) {
        actualHours.set(value);
    }

    // Priority property accessors
    public ObjectProperty<Task.Priority> priorityProperty() {
        return priority;
    }

    public Task.Priority getPriority() {
        return priority.get();
    }

    public void setPriority(Task.Priority value) {
        priority.set(value);
    }

    // Progress property accessors
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

    // Start date property accessors
    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    public LocalDate getStartDate() {
        return startDate.get();
    }

    public void setStartDate(LocalDate value) {
        startDate.set(value);
    }

    // End date property accessors
    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }

    public LocalDate getEndDate() {
        return endDate.get();
    }

    public void setEndDate(LocalDate value) {
        endDate.set(value);
    }

    // Completed property accessors
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

    // Project property accessors
    public ObjectProperty<Project> projectProperty() {
        return project;
    }

    public Project getProject() {
        return project.get();
    }

    public void setProject(Project value) {
        project.set(value);
    }

    // Subsystem property accessors
    public ObjectProperty<Subsystem> subsystemProperty() {
        return subsystem;
    }

    public Subsystem getSubsystem() {
        return subsystem.get();
    }

    public void setSubsystem(Subsystem value) {
        subsystem.set(value);
    }

    // Additional setters
    public void setTask(Task value) {
        task.set(value);
    }

    public void setIsNewTask(boolean value) {
        isNewTask.set(value);
    }

    public void setValid(boolean value) {
        valid.set(value);
    }

    /**
     * Gets the selected team member.
     * 
     * @return the selected team member
     */
    public TeamMember getSelectedMember() {
        return selectedMember.get();
    }

    /**
     * Sets the selected team member.
     * 
     * @param member the team member to select
     */
    public void setSelectedMember(TeamMember member) {
        selectedMember.set(member);
    }

    /**
     * Gets the selected team member property.
     * 
     * @return the selected team member property
     */
    public ObjectProperty<TeamMember> selectedMemberProperty() {
        return selectedMember;
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
     * Gets the selected component property.
     * 
     * @return the selected component property
     */
    public ObjectProperty<Component> selectedComponentProperty() {
        return selectedComponent;
    }

    /**
     * Gets the selected dependency task.
     * 
     * @return the selected dependency task
     */
    public Task getSelectedDependency() {
        return selectedDependency.get();
    }

    /**
     * Sets the selected dependency task.
     * 
     * @param dependency the dependency task to select
     */
    public void setSelectedDependency(Task dependency) {
        selectedDependency.set(dependency);
    }

    /**
     * Gets the selected dependency task property.
     * 
     * @return the selected dependency task property
     */
    public ObjectProperty<Task> selectedDependencyProperty() {
        return selectedDependency;
    }
}