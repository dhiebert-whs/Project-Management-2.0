package org.frcpm.repositories.specific;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Task entity.
 */
public interface TaskRepository extends Repository<Task, Long> {
    
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
     * Finds tasks with a due date before the specified date.
     * 
     * @param date the date to compare against
     * @return a list of tasks due before the date
     */
    List<Task> findByEndDateBefore(LocalDate date);
    
    /**
     * Finds tasks by priority.
     * 
     * @param priority the priority level
     * @return a list of tasks with the given priority
     */
    List<Task> findByPriority(Task.Priority priority);
}