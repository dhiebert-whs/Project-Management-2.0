// src/main/java/org/frcpm/repositories/spring/TaskRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for Task entities.
 * This interface extends JpaRepository to provide automatic CRUD operations
 * and includes custom query methods for task-specific operations.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Finds tasks by project.
     * 
     * @param project the project to find tasks for
     * @return a list of tasks for the project
     */
    List<Task> findByProject(Project project);
    
    /**
     * Finds tasks by project ID.
     * 
     * @param projectId the project ID
     * @return a list of tasks for the project
     */
    List<Task> findByProjectId(Long projectId);
    
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
    @Query("SELECT t FROM Task t JOIN t.assignedTo m WHERE m = :member")
    List<Task> findByAssignedMember(@Param("member") TeamMember member);
    
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
    
    /**
     * Finds incomplete tasks for a project.
     * 
     * @param project the project
     * @return a list of incomplete tasks
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.completed = false")
    List<Task> findIncompleteTasksByProject(@Param("project") Project project);
    
    /**
     * Finds tasks due within the specified number of days for a project.
     * 
     * @param projectId the project ID
     * @param startDate the start date (today)
     * @param endDate the end date (today + days)
     * @return a list of tasks due soon
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
           "AND t.completed = false " +
           "AND t.endDate IS NOT NULL " +
           "AND t.endDate >= :startDate " +
           "AND t.endDate <= :endDate")
    List<Task> findTasksDueSoon(@Param("projectId") Long projectId, 
                               @Param("startDate") LocalDate startDate, 
                               @Param("endDate") LocalDate endDate);
    
    /**
     * Finds overdue tasks for a project.
     * 
     * @param project the project
     * @return a list of overdue tasks
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project " +
           "AND t.completed = false " +
           "AND t.endDate IS NOT NULL " +
           "AND t.endDate < CURRENT_DATE")
    List<Task> findOverdueTasksByProject(@Param("project") Project project);
    
    /**
     * Finds tasks by title containing the specified text.
     * 
     * @param title the text to search for in task titles
     * @return a list of matching tasks
     */
    List<Task> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Finds tasks assigned to multiple team members.
     * 
     * @param memberIds the list of team member IDs
     * @return a list of tasks assigned to any of the specified members
     */
    @Query("SELECT DISTINCT t FROM Task t JOIN t.assignedTo m WHERE m.id IN :memberIds")
    List<Task> findTasksAssignedToMembers(@Param("memberIds") List<Long> memberIds);
    
    /**
     * Counts completed tasks for a project.
     * 
     * @param project the project
     * @return the count of completed tasks
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project = :project AND t.completed = true")
    long countCompletedTasksByProject(@Param("project") Project project);
    
    /**
     * Counts total tasks for a project.
     * 
     * @param project the project
     * @return the total count of tasks
     */
    long countByProject(Project project);
    
    /**
     * Finds tasks with dependencies (tasks that depend on other tasks).
     * 
     * @return a list of tasks with dependencies
     */
    @Query("SELECT t FROM Task t WHERE SIZE(t.preDependencies) > 0")
    List<Task> findTasksWithDependencies();
    
    /**
     * Finds tasks that are blocking other tasks (tasks that other tasks depend on).
     * 
     * @return a list of blocking tasks
     */
    @Query("SELECT t FROM Task t WHERE SIZE(t.postDependencies) > 0")
    List<Task> findBlockingTasks();
}