package org.frcpm.services;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service interface for Task entity.
 * FIXED: Added async method signatures to support async operations in ViewModels.
 */
public interface TaskService extends Service<Task, Long> {
    
    /**
     * Finds tasks by project.
     * 
     * @param project the project to find tasks for
     * @return a list of tasks for the project
     */
    List<Task> findByProject(Project project);
    
    /**
     * Finds tasks by subsystem.
     * 
     * @param subsystem the subsystem to find tasks for
     * @return a list of tasks for the subsystem
     */
    List<Task> findBySubsystem(Subsystem subsystem);
    
    /**
     * Finds tasks assigned to a team member.
     * 
     * @param member the team member
     * @return a list of tasks assigned to the member
     */
    List<Task> findByAssignedMember(TeamMember member);
    
    /**
     * Finds tasks by completion status.
     * 
     * @param completed whether to find completed or incomplete tasks
     * @return a list of tasks with the given completion status
     */
    List<Task> findByCompleted(boolean completed);
    
    /**
     * Creates a new task.
     * 
     * @param title the task title
     * @param project the project the task belongs to
     * @param subsystem the subsystem the task is for
     * @param estimatedHours the estimated hours to complete the task
     * @param priority the task priority
     * @param startDate the planned start date (optional)
     * @param endDate the planned end date (optional)
     * @return the created task
     */
    Task createTask(String title, Project project, Subsystem subsystem, 
                    double estimatedHours, Task.Priority priority,
                    LocalDate startDate, LocalDate endDate);
    
    /**
     * Updates a task's progress.
     * 
     * @param taskId the task ID
     * @param progress the new progress percentage (0-100)
     * @param completed whether the task is completed
     * @return the updated task, or null if not found
     */
    Task updateTaskProgress(Long taskId, int progress, boolean completed);
    
    /**
     * Assigns members to a task.
     * 
     * @param taskId the task ID
     * @param members the members to assign
     * @return the updated task, or null if not found
     */
    Task assignMembers(Long taskId, Set<TeamMember> members);
    
    /**
     * Adds a dependency between tasks.
     * 
     * @param taskId the task ID
     * @param dependencyId the dependency task ID
     * @return true if the dependency was added, false otherwise
     */
    boolean addDependency(Long taskId, Long dependencyId);
    
    /**
     * Removes a dependency between tasks.
     * 
     * @param taskId the task ID
     * @param dependencyId the dependency task ID
     * @return true if the dependency was removed, false otherwise
     */
    boolean removeDependency(Long taskId, Long dependencyId);
    
    /**
     * Gets tasks due soon for a project.
     * 
     * @param projectId the project ID
     * @param days the number of days to look ahead
     * @return a list of tasks due within the specified days
     */
    List<Task> getTasksDueSoon(Long projectId, int days);

    /**
     * Updates the required components for a task.
     * 
     * @param taskId the task ID
     * @param componentIds the component IDs to set as required
     * @return the updated task, or null if not found
     */
    Task updateRequiredComponents(Long taskId, Set<Long> componentIds);
    
    // ASYNC METHODS - Added to support async operations in ViewModels
    
    /**
     * Associates components with a task asynchronously.
     * This method is used by ComponentDetailMvvmViewModel.
     * 
     * @param taskId the task ID
     * @param componentIds the component IDs to associate
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated task
     */
    default CompletableFuture<Task> associateComponentsWithTaskAsync(Long taskId, Set<Long> componentIds,
                                                        Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
        CompletableFuture<Task> future = new CompletableFuture<>();
        try {
            Task result = updateRequiredComponents(taskId, componentIds);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Finds tasks by project asynchronously.
     * 
     * @param project the project to find tasks for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of tasks
     */
    default CompletableFuture<List<Task>> findByProjectAsync(Project project,
                                                       Consumer<List<Task>> onSuccess,
                                                       Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
        CompletableFuture<List<Task>> future = new CompletableFuture<>();
        try {
            List<Task> result = findByProject(project);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        return future;
    }
    
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
    default CompletableFuture<Task> createTaskAsync(String title, Project project, Subsystem subsystem,
                                              double estimatedHours, Task.Priority priority,
                                              LocalDate startDate, LocalDate endDate,
                                              Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
        CompletableFuture<Task> future = new CompletableFuture<>();
        try {
            Task result = createTask(title, project, subsystem, estimatedHours, priority, startDate, endDate);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * Deletes a task by ID asynchronously.
     * 
     * @param id the ID of the task to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a boolean indicating success
     */
    default CompletableFuture<Boolean> deleteByIdAsync(Long id, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            boolean result = deleteById(id);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        return future;
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
    default CompletableFuture<Task> updateTaskProgressAsync(Long taskId, int progress, boolean completed,
                                                         Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
        CompletableFuture<Task> future = new CompletableFuture<>();
        try {
            Task result = updateTaskProgress(taskId, progress, completed);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        return future;
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
    default CompletableFuture<Task> assignMembersAsync(Long taskId, Set<TeamMember> members,
                                                    Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
        CompletableFuture<Task> future = new CompletableFuture<>();
        try {
            Task result = assignMembers(taskId, members);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        return future;
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
    default CompletableFuture<List<Task>> getTasksDueSoonAsync(Long projectId, int days,
                                                            Consumer<List<Task>> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
        CompletableFuture<List<Task>> future = new CompletableFuture<>();
        try {
            List<Task> result = getTasksDueSoon(projectId, days);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        return future;
    }
}