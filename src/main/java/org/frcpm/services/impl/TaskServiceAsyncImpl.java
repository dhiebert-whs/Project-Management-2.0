// src/main/java/org/frcpm/services/impl/TaskServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.TaskService;
import org.frcpm.config.DatabaseConfig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

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
 * Implementation of TaskService using the task-based threading model.
 */
public class TaskServiceAsyncImpl extends AbstractAsyncService<Task, Long, TaskRepository>
        implements TaskService {

    private static final Logger LOGGER = Logger.getLogger(TaskServiceAsyncImpl.class.getName());

    public TaskServiceAsyncImpl() {
        super(RepositoryFactory.getTaskRepository());
    }

    // Implementing interface methods with synchronized versions
    
    @Override
    public List<Task> findByProject(Project project) {
        return repository.findByProject(project);
    }

    @Override
    public List<Task> findBySubsystem(Subsystem subsystem) {
        return repository.findBySubsystem(subsystem);
    }

    @Override
    public List<Task> findByAssignedMember(TeamMember member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        return repository.findByAssignedMember(member);
    }

    @Override
    public List<Task> findByCompleted(boolean completed) {
        return repository.findByCompleted(completed);
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

        return executeSync(em -> {
            // Get a fully managed Task entity
            Task task = em.find(Task.class, taskId);
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
                        // We need to make sure we're using the exact same member instance
                        // from the test to ensure proper identity comparison
                        task.getAssignedTo().add(member);
                    }
                }
            }

            // Flush changes
            em.flush();
            return task;
        });
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

        return executeSync(em -> {
            // Get managed instances
            Task task = em.find(Task.class, taskId);
            Task dependency = em.find(Task.class, dependencyId);

            if (task == null || dependency == null) {
                LOGGER.log(Level.WARNING, "Task not found with ID: {0} or {1}",
                        new Object[] { taskId, dependencyId });
                return false;
            }

            // Check if dependency exists
            boolean exists = false;
            for (Task dep : task.getPreDependencies()) {
                if (dep.getId().equals(dependencyId)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                LOGGER.log(Level.WARNING, "Dependency does not exist between tasks {0} and {1}",
                        new Object[] { taskId, dependencyId });
                return false;
            }

            // Manually maintain both sides of the relationship
            task.getPreDependencies().remove(dependency);
            dependency.getPostDependencies().remove(task);

            // Ensure changes are persisted
            em.flush();
            return true;
        });
    }

    @Override
    public List<Task> getTasksDueSoon(Long projectId, int days) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }

        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }

        Project project = RepositoryFactory.getProjectRepository().findById(projectId).orElse(null);
        if (project == null) {
            LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
            return new ArrayList<>();
        }

        LocalDate today = LocalDate.now();
        LocalDate dueBefore = today.plusDays(days);

        List<Task> allTasks = repository.findByProject(project);
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
            // Get component repository to find components by their IDs
            var componentRepository = RepositoryFactory.getComponentRepository();

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

    // Async methods specific to TaskService
    
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

        return executeAsync("Find Tasks By Project: " + project.getId(), em -> {
            return repository.findByProject(project, em);
        }, onSuccess, onFailure);
    }

// src/main/java/org/frcpm/services/impl/TaskServiceAsyncImpl.java (continued)

    /**
     * Creates a new task asynchronously.
     * 
     * @param title the task title
     * @param project the project the task belongs to
     * @param subsystem the subsystem the task is for
     * @param estimatedHours the estimated hours to complete the task
     * @param priority the task priority
     * @param startDate the planned start date (optional)
     * @param endDate the planned end date (optional)
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created task
     */
    public CompletableFuture<Task> createTaskAsync(String title, Project project, Subsystem subsystem,
                                                 double estimatedHours, Task.Priority priority,
                                                 LocalDate startDate, LocalDate endDate,
                                                 Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        try {
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

            return executeAsync("Create Task: " + title, em -> {
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

                return repository.save(task, em);
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Task> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Updates a task's progress asynchronously.
     * 
     * @param taskId the task ID
     * @param progress the new progress percentage (0-100)
     * @param completed whether the task is completed
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated task
     */
    public CompletableFuture<Task> updateTaskProgressAsync(Long taskId, int progress, boolean completed,
                                                         Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        if (taskId == null) {
            CompletableFuture<Task> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Task ID cannot be null"));
            return future;
        }

        return executeAsync("Update Task Progress: " + taskId, em -> {
            Task task = em.find(Task.class, taskId);
            if (task == null) {
                LOGGER.log(Level.WARNING, "Task not found with ID: {0}", taskId);
                return null;
            }

            // Progress must be between 0 and 100
            int boundedProgress = Math.max(0, Math.min(100, progress));
            task.setProgress(boundedProgress);

            // If completed is true, set progress to 100%
            if (completed && boundedProgress < 100) {
                task.setProgress(100);
            }

            task.setCompleted(completed || boundedProgress == 100);

            em.merge(task);
            return task;
        }, onSuccess, onFailure);
    }
    
    /**
     * Assigns members to a task asynchronously.
     * 
     * @param taskId the task ID
     * @param members the members to assign
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated task
     */
    public CompletableFuture<Task> assignMembersAsync(Long taskId, Set<TeamMember> members,
                                                    Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        if (taskId == null) {
            CompletableFuture<Task> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Task ID cannot be null"));
            return future;
        }

        return executeAsync("Assign Members to Task: " + taskId, em -> {
            // Get a fully managed Task entity
            Task task = em.find(Task.class, taskId);
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
                        TeamMember managedMember = em.find(TeamMember.class, member.getId());
                        if (managedMember != null) {
                            task.getAssignedTo().add(managedMember);
                        }
                    }
                }
            }

            // Flush changes
            em.flush();
            return task;
        }, onSuccess, onFailure);
    }
    
    /**
     * Gets tasks due soon for a project asynchronously.
     * 
     * @param projectId the project ID
     * @param days the number of days to look ahead
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of tasks
     */
    public CompletableFuture<List<Task>> getTasksDueSoonAsync(Long projectId, int days,
                                                            Consumer<List<Task>> onSuccess, Consumer<Throwable> onFailure) {
        try {
            if (projectId == null) {
                throw new IllegalArgumentException("Project ID cannot be null");
            }

            if (days <= 0) {
                throw new IllegalArgumentException("Days must be positive");
            }

            return executeAsync("Get Tasks Due Soon for Project: " + projectId, em -> {
                Project project = em.find(Project.class, projectId);
                if (project == null) {
                    LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
                    return new ArrayList<>();
                }

                LocalDate today = LocalDate.now();
                LocalDate dueBefore = today.plusDays(days);

                // Use a JPQL query to efficiently retrieve tasks due soon
                TypedQuery<Task> query = em.createQuery(
                        "SELECT t FROM Task t " +
                        "WHERE t.project.id = :projectId " +
                        "AND t.completed = false " +
                        "AND t.endDate IS NOT NULL " +
                        "AND t.endDate >= :today " +
                        "AND t.endDate <= :dueBefore", Task.class);
                
                query.setParameter("projectId", projectId);
                query.setParameter("today", today);
                query.setParameter("dueBefore", dueBefore);
                
                return query.getResultList();
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<List<Task>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}