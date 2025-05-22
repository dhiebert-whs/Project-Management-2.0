// src/main/java/org/frcpm/services/impl/TestableTaskServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.ComponentRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.TaskService;
import org.frcpm.di.ServiceLocator;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable async implementation of TaskService that accepts injected repositories
 * for better unit testing. This implementation eliminates static references to
 * RepositoryFactory and uses proper constructor injection for all dependencies.
 * 
 * This class extends TaskServiceAsyncImpl to provide async capabilities while
 * allowing for proper dependency injection in tests.
 */
public class TestableTaskServiceAsyncImpl extends TaskServiceAsyncImpl implements TaskService {
    
    private static final Logger LOGGER = Logger.getLogger(TestableTaskServiceAsyncImpl.class.getName());
    
    // Dependencies injected via constructor
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ComponentRepository componentRepository;
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableTaskServiceAsyncImpl() {
        this(
            ServiceLocator.getTaskRepository(),
            ServiceLocator.getProjectRepository(),
            ServiceLocator.getComponentRepository()
        );
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param taskRepository the task repository
     * @param projectRepository the project repository
     * @param componentRepository the component repository
     */
    public TestableTaskServiceAsyncImpl(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            ComponentRepository componentRepository) {
        super(); // Call parent constructor
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.componentRepository = componentRepository;
        
        // Inject repositories into the parent class using reflection
        try {
            injectRepositoryIntoParent("repository", taskRepository);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to inject task repository into parent", e);
        }
    }
    
    /**
     * Helper method to inject repository into parent class using reflection.
     */
    private void injectRepositoryIntoParent(String fieldName, Object repository) throws Exception {
        java.lang.reflect.Field field = findFieldInHierarchy(this.getClass(), fieldName);
        if (field != null) {
            field.setAccessible(true);
            field.set(this, repository);
        }
    }
    
    /**
     * Find field in class hierarchy.
     */
    private java.lang.reflect.Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return findFieldInHierarchy(superClass, fieldName);
            }
            return null;
        }
    }

    // Override synchronous interface methods to use injected repositories
    
    @Override
    public Task findById(Long id) {
        if (id == null) {
            return null;
        }
        return taskRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Task> findAll() {
        return taskRepository.findAll();
    }
    
    @Override
    public Task save(Task entity) {
        try {
            return taskRepository.save(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving task", e);
            throw new RuntimeException("Failed to save task", e);
        }
    }
    
    @Override
    public void delete(Task entity) {
        try {
            taskRepository.delete(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting task", e);
            throw new RuntimeException("Failed to delete task", e);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        try {
            return taskRepository.deleteById(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting task by ID", e);
            throw new RuntimeException("Failed to delete task by ID", e);
        }
    }
    
    @Override
    public long count() {
        return taskRepository.count();
    }
    
    @Override
    public List<Task> findByProject(Project project) {
        return taskRepository.findByProject(project);
    }
    
    @Override
    public List<Task> findBySubsystem(Subsystem subsystem) {
        return taskRepository.findBySubsystem(subsystem);
    }
    
    @Override
    public List<Task> findByAssignedMember(TeamMember member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        return taskRepository.findByAssignedMember(member);
    }
    
    @Override
    public List<Task> findByCompleted(boolean completed) {
        return taskRepository.findByCompleted(completed);
    }
    
    @Override
    public Task createTask(String title, Project project, Subsystem subsystem,
            double estimatedHours, Task.Priority priority, 
            LocalDate startDate, LocalDate endDate) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }

        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        if (subsystem == null) {
            throw new IllegalArgumentException("Subsystem cannot be null");
        }

        if (estimatedHours <= 0) {
            throw new IllegalArgumentException("Estimated hours must be positive");
        }

        Task task = new Task(title, project, subsystem);
        task.setEstimatedDuration(Duration.ofMinutes((long) (estimatedHours * 60)));

        if (priority != null) {
            task.setPriority(priority);
        }

        if (startDate != null) {
            task.setStartDate(startDate);
        }

        if (endDate != null) {
            if (startDate != null && endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }
            task.setEndDate(endDate);
        }

        return save(task);
    }
    
    @Override
    public Task updateTaskProgress(Long taskId, int progress, boolean completed) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }

        Task task = findById(taskId);
        if (task == null) {
            LOGGER.log(Level.WARNING, "Task not found with ID: {0}", taskId);
            return null;
        }

        // Progress must be between 0 and 100
        progress = Math.max(0, Math.min(100, progress));
        task.setProgress(progress);

        // If completed is true, set progress to 100%
        if (completed && progress < 100) {
            task.setProgress(100);
        }

        task.setCompleted(completed || progress == 100);

        return save(task);
    }
    
    @Override
    public Task assignMembers(Long taskId, Set<TeamMember> members) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }

        Task task = findById(taskId);
        if (task == null) {
            LOGGER.log(Level.WARNING, "Task not found with ID: {0}", taskId);
            return null;
        }

        // Clear existing assignments
        task.getAssignedTo().clear();

        // Add new assignments
        if (members != null) {
            for (TeamMember member : members) {
                if (member != null && member.getId() != null) {
                    task.assignMember(member);
                }
            }
        }

        return save(task);
    }
    
    @Override
    public boolean addDependency(Long taskId, Long dependencyId) {
        if (taskId == null || dependencyId == null) {
            throw new IllegalArgumentException("Task IDs cannot be null");
        }

        if (taskId.equals(dependencyId)) {
            throw new IllegalArgumentException("A task cannot depend on itself");
        }

        try {
            // Get task and dependency objects
            Task task = findById(taskId);
            Task dependency = findById(dependencyId);

            if (task == null || dependency == null) {
                return false;
            }

            // Add the dependency relationship
            task.addPreDependency(dependency);

            // Save the changes
            save(task);
            save(dependency);

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding dependency", e);
            return false;
        }
    }
    
    @Override
    public boolean removeDependency(Long taskId, Long dependencyId) {
        if (taskId == null || dependencyId == null) {
            throw new IllegalArgumentException("Task IDs cannot be null");
        }

        try {
            // Get task and dependency objects
            Task task = findById(taskId);
            Task dependency = findById(dependencyId);

            if (task == null || dependency == null) {
                return false;
            }

            // Remove the dependency relationship
            task.removePreDependency(dependency);

            // Save the changes
            save(task);
            save(dependency);

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error removing dependency", e);
            return false;
        }
    }
    
    @Override
    public List<Task> getTasksDueSoon(Long projectId, int days) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }

        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }

        // Use project repository instead of static RepositoryFactory access
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
            return new ArrayList<>();
        }

        LocalDate today = LocalDate.now();
        LocalDate dueBefore = today.plusDays(days);

        List<Task> allTasks = taskRepository.findByProject(project);
        List<Task> dueSoonTasks = new ArrayList<>();

        for (Task task : allTasks) {
            if (!task.isCompleted() && task.getEndDate() != null &&
                    !task.getEndDate().isBefore(today) && !task.getEndDate().isAfter(dueBefore)) {
                dueSoonTasks.add(task);
            }
        }

        return dueSoonTasks;
    }
    
    @Override
    public Task updateRequiredComponents(Long taskId, Set<Long> componentIds) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }

        Task task = findById(taskId);
        if (task == null) {
            LOGGER.log(Level.WARNING, "Task not found with ID: {0}", taskId);
            return null;
        }

        // Clear existing component relationships
        task.getRequiredComponents().clear();

        // Add new component relationships if componentIds is not null or empty
        if (componentIds != null && !componentIds.isEmpty()) {
            for (Long componentId : componentIds) {
                var componentOpt = componentRepository.findById(componentId);
                if (componentOpt.isPresent()) {
                    task.addRequiredComponent(componentOpt.get());
                } else {
                    LOGGER.log(Level.WARNING, "Component not found with ID: {0}", componentId);
                }
            }
        }

        // Save the updated task
        return save(task);
    }
    
    // Async methods - these will delegate to the parent class implementation
    // but the parent class will use our injected repositories
    
    /**
     * Finds tasks by project asynchronously.
     * 
     * @param project the project to find tasks for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of tasks
     */
    public CompletableFuture<List<Task>> findByProjectAsync(Project project,
                                                       Consumer<List<Task>> onSuccess,
                                                       Consumer<Throwable> onFailure) {
        if (project == null) {
            CompletableFuture<List<Task>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Project cannot be null"));
            return future;
        }

        return executeAsync("Find Tasks By Project: " + project.getId(), () -> {
            return findByProject(project);
        }, onSuccess, onFailure);
    }
}