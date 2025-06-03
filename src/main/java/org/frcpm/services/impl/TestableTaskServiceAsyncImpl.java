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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable async implementation of TaskService that accepts injected repositories
 * for better unit testing. This implementation provides async methods that can be stubbed
 * in tests without requiring spy patterns.
 */
public class TestableTaskServiceAsyncImpl extends TestableTaskServiceImpl {
    
    private static final Logger LOGGER = Logger.getLogger(TestableTaskServiceAsyncImpl.class.getName());
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableTaskServiceAsyncImpl() {
        super();
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
        super(taskRepository, projectRepository, componentRepository);
    }
    
    // ASYNC METHODS - These can be stubbed directly in tests without spies
    
    /**
     * Finds all tasks asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of tasks
     */
    public CompletableFuture<List<Task>> findAllAsync(Consumer<List<Task>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Task>> future = new CompletableFuture<>();
        
        try {
            List<Task> result = findAll();
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
     * Finds a task by ID asynchronously.
     * 
     * @param id the task ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the task
     */
    public CompletableFuture<Task> findByIdAsync(Long id, Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Task> future = new CompletableFuture<>();
        
        try {
            Task result = findById(id);
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
     * Saves a task asynchronously.
     * 
     * @param entity the task to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved task
     */
    public CompletableFuture<Task> saveAsync(Task entity, Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Task> future = new CompletableFuture<>();
        
        try {
            Task result = save(entity);
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
     * Deletes a task asynchronously.
     * 
     * @param entity the task to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed when the task is deleted
     */
    public CompletableFuture<Void> deleteAsync(Task entity, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            delete(entity);
            if (onSuccess != null) {
                onSuccess.accept(null);
            }
            future.complete(null);
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
    public CompletableFuture<Boolean> deleteByIdAsync(Long id, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
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
     * Counts all tasks asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the count of tasks
     */
    public CompletableFuture<Long> countAsync(Consumer<Long> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        
        try {
            long result = count();
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
    public CompletableFuture<List<Task>> findByProjectAsync(Project project, Consumer<List<Task>> onSuccess, Consumer<Throwable> onFailure) {
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
     * Finds tasks by subsystem asynchronously.
     * 
     * @param subsystem the subsystem to find tasks for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of tasks
     */
    public CompletableFuture<List<Task>> findBySubsystemAsync(Subsystem subsystem, Consumer<List<Task>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Task>> future = new CompletableFuture<>();
        
        try {
            List<Task> result = findBySubsystem(subsystem);
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
     * Finds tasks by assigned member asynchronously.
     * 
     * @param member the team member
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of tasks
     */
    public CompletableFuture<List<Task>> findByAssignedMemberAsync(TeamMember member, Consumer<List<Task>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Task>> future = new CompletableFuture<>();
        
        try {
            List<Task> result = findByAssignedMember(member);
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
     * Finds tasks by completed status asynchronously.
     * 
     * @param completed whether to find completed or incomplete tasks
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of tasks
     */
    public CompletableFuture<List<Task>> findByCompletedAsync(boolean completed, Consumer<List<Task>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Task>> future = new CompletableFuture<>();
        
        try {
            List<Task> result = findByCompleted(completed);
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
    public CompletableFuture<Task> createTaskAsync(String title, Project project, Subsystem subsystem,
                                              double estimatedHours, Task.Priority priority,
                                              LocalDate startDate, LocalDate endDate,
                                              Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
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
    public CompletableFuture<Task> assignMembersAsync(Long taskId, Set<TeamMember> members,
                                                    Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
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
     * Adds a dependency between tasks asynchronously.
     * 
     * @param taskId the task ID
     * @param dependencyId the dependency task ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a boolean indicating success
     */
    public CompletableFuture<Boolean> addDependencyAsync(Long taskId, Long dependencyId,
                                                       Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            boolean result = addDependency(taskId, dependencyId);
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
     * Removes a dependency between tasks asynchronously.
     * 
     * @param taskId the task ID
     * @param dependencyId the dependency task ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a boolean indicating success
     */
    public CompletableFuture<Boolean> removeDependencyAsync(Long taskId, Long dependencyId,
                                                          Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            boolean result = removeDependency(taskId, dependencyId);
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
    public CompletableFuture<List<Task>> getTasksDueSoonAsync(Long projectId, int days,
                                                            Consumer<List<Task>> onSuccess, Consumer<Throwable> onFailure) {
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
    public CompletableFuture<Task> associateComponentsWithTaskAsync(Long taskId, Set<Long> componentIds,
                                                        Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
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
}