// src/main/java/org/frcpm/services/impl/TaskServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.ComponentRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.repositories.spring.TaskRepository;
import org.frcpm.services.TaskService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spring Boot implementation of TaskService using composition pattern.
 * Converted from inheritance to composition for architectural consistency.
 * This is the most complex service with task management, dependencies, and component associations.
 */
@Service("taskServiceImpl")
@Transactional
public class TaskServiceImpl implements TaskService {
    
    private static final Logger LOGGER = Logger.getLogger(TaskServiceImpl.class.getName());
    
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ComponentRepository componentRepository;
    
    /**
     * Constructor injection for repositories.
     * No @Autowired needed with single constructor.
     */
    public TaskServiceImpl(TaskRepository taskRepository,
                          ProjectRepository projectRepository,
                          ComponentRepository componentRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.componentRepository = componentRepository;
    }
    
    // =========================================================================
    // BASIC CRUD OPERATIONS - Implementing Service<Task, Long> interface
    // =========================================================================
    
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
        if (entity == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        try {
            return taskRepository.save(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving task", e);
            throw new RuntimeException("Failed to save task", e);
        }
    }
    
    @Override
    public void delete(Task entity) {
        if (entity != null) {
            try {
                taskRepository.delete(entity);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting task", e);
                throw new RuntimeException("Failed to delete task", e);
            }
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id != null && taskRepository.existsById(id)) {
            try {
                taskRepository.deleteById(id);
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting task by ID", e);
                throw new RuntimeException("Failed to delete task by ID", e);
            }
        }
        return false;
    }
    
    @Override
    public long count() {
        return taskRepository.count();
    }
    
    // =========================================================================
    // BUSINESS LOGIC METHODS - TaskService specific methods
    // =========================================================================
    
    @Override
    public List<Task> findByProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        return taskRepository.findByProject(project);
    }
    
    @Override
    public List<Task> findBySubsystem(Subsystem subsystem) {
        if (subsystem == null) {
            throw new IllegalArgumentException("Subsystem cannot be null");
        }
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
            Task task = findById(taskId);
            Task dependency = findById(dependencyId);

            if (task == null || dependency == null) {
                return false;
            }

            // Add the dependency relationship
            task.addPreDependency(dependency);

            // Save both tasks to persist the bidirectional relationship
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
            Task task = findById(taskId);
            Task dependency = findById(dependencyId);

            if (task == null || dependency == null) {
                return false;
            }

            // Remove the dependency relationship
            task.removePreDependency(dependency);

            // Save both tasks to persist the bidirectional relationship
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

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
            return List.of();
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

        return save(task);
    }

    // =========================================================================
    // ASYNC METHODS - Using @Async annotation with CompletableFuture
    // Following the exact pattern from TaskService interface
    // =========================================================================
    
    @Async
    @Override
    public CompletableFuture<Task> associateComponentsWithTaskAsync(
            Long taskId, Set<Long> componentIds,
            Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Task result = updateRequiredComponents(taskId, componentIds);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<List<Task>> findByProjectAsync(
            Project project,
            Consumer<List<Task>> onSuccess,
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Task> result = findByProject(project);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Task> createTaskAsync(
            String title, Project project, Subsystem subsystem,
            double estimatedHours, Task.Priority priority,
            LocalDate startDate, LocalDate endDate,
            Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Task result = createTask(title, project, subsystem, estimatedHours, priority, startDate, endDate);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(
            Long id, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Boolean result = deleteById(id);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }

    @Async
    @Override
    public CompletableFuture<Task> updateTaskProgressAsync(
            Long taskId, int progress, boolean completed,
            Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Task result = updateTaskProgress(taskId, progress, completed);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Task> assignMembersAsync(
            Long taskId, Set<TeamMember> members,
            Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Task result = assignMembers(taskId, members);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<List<Task>> getTasksDueSoonAsync(
            Long projectId, int days,
            Consumer<List<Task>> onSuccess, Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Task> result = getTasksDueSoon(projectId, days);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    // Additional async methods for complete functionality
    
    @Async
    public CompletableFuture<List<Task>> findAllAsync() {
        try {
            List<Task> result = findAll();
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<Task>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Task> findByIdAsync(Long id) {
        try {
            Task result = findById(id);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Task> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Task> saveAsync(Task entity) {
        try {
            Task result = save(entity);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Task> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<List<Task>> findBySubsystemAsync(Subsystem subsystem) {
        try {
            List<Task> result = findBySubsystem(subsystem);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<Task>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<List<Task>> findByAssignedMemberAsync(TeamMember member) {
        try {
            List<Task> result = findByAssignedMember(member);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<Task>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Boolean> addDependencyAsync(Long taskId, Long dependencyId) {
        try {
            boolean result = addDependency(taskId, dependencyId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Boolean> removeDependencyAsync(Long taskId, Long dependencyId) {
        try {
            boolean result = removeDependency(taskId, dependencyId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}