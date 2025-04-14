package org.frcpm.services;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Service interface for Task entity.
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
}