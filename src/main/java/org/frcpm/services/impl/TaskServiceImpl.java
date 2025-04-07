package org.frcpm.services.impl;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.TaskService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of TaskService using repository layer.
 */
public class TaskServiceImpl extends AbstractService<Task, Long, TaskRepository> implements TaskService {

    private static final Logger LOGGER = Logger.getLogger(TaskServiceImpl.class.getName());

    public TaskServiceImpl() {
        super(RepositoryFactory.getTaskRepository());
    }

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
                task.assignMember(member);
            }
        }

        return save(task);
    }

    // TODO: need to implement Task updateRequiredComponents
    @Override
    public Task updateRequiredComponents(Long taskId, Set<Long> componentIds) {
        return null;
    };

    @Override
    public boolean addDependency(Long taskId, Long dependencyId) {
        if (taskId == null || dependencyId == null) {
            throw new IllegalArgumentException("Task IDs cannot be null");
        }

        if (taskId.equals(dependencyId)) {
            throw new IllegalArgumentException("A task cannot depend on itself");
        }

        Task task = findById(taskId);
        Task dependency = findById(dependencyId);

        if (task == null || dependency == null) {
            LOGGER.log(Level.WARNING, "Task not found with ID: {0} or {1}", new Object[] { taskId, dependencyId });
            return false;
        }

        // Check for circular dependencies
        if (hasDependencyPath(dependency, task)) {
            throw new IllegalArgumentException("Adding this dependency would create a circular dependency");
        }

        task.addPreDependency(dependency);
        save(task);

        return true;
    }

    @Override
    public boolean removeDependency(Long taskId, Long dependencyId) {
        if (taskId == null || dependencyId == null) {
            throw new IllegalArgumentException("Task IDs cannot be null");
        }

        Task task = findById(taskId);
        Task dependency = findById(dependencyId);

        if (task == null || dependency == null) {
            LOGGER.log(Level.WARNING, "Task not found with ID: {0} or {1}", new Object[] { taskId, dependencyId });
            return false;
        }

        if (!task.getPreDependencies().contains(dependency)) {
            return false;
        }

        task.removePreDependency(dependency);
        save(task);

        return true;
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

    /**
     * Checks if there is a dependency path from one task to another.
     * Used to detect circular dependencies.
     */
    private boolean hasDependencyPath(Task start, Task target) {
        if (start.getPostDependencies().contains(target)) {
            return true;
        }

        for (Task postDep : start.getPostDependencies()) {
            if (hasDependencyPath(postDep, target)) {
                return true;
            }
        }

        return false;
    }

}
