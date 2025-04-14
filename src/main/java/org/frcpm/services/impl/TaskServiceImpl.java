package org.frcpm.services.impl;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.TaskService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
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
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();

            // Use JPQL with fetch joins to eagerly load collections
            TypedQuery<Task> query = em.createQuery(
                    "SELECT DISTINCT t FROM Task t " +
                            "LEFT JOIN FETCH t.assignedTo " +
                            "LEFT JOIN FETCH t.preDependencies " +
                            "LEFT JOIN FETCH t.postDependencies " +
                            "LEFT JOIN FETCH t.requiredComponents " +
                            "JOIN t.assignedTo m WHERE m.id = :memberId",
                    Task.class);
            query.setParameter("memberId", member.getId());

            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding tasks by assigned member", e);
            return new ArrayList<>();
        } finally {
            if (em != null) {
                em.close();
            }
        }
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

        // First, load the task with its existing assignments
        Task task = findById(taskId);
        if (task == null) {
            LOGGER.log(Level.WARNING, "Task not found with ID: {0}", taskId);
            return null;
        }

        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();
            em.getTransaction().begin();

            // Get a managed instance of the task
            Task managedTask = em.find(Task.class, taskId);

            // Get managed instances of all current team members and clear connections
            for (TeamMember member : new HashSet<>(managedTask.getAssignedTo())) {
                TeamMember managedMember = em.find(TeamMember.class, member.getId());
                managedMember.getAssignedTasks().remove(managedTask);
                managedTask.getAssignedTo().remove(managedMember);
            }

            // Now add the new assignments
            if (members != null) {
                for (TeamMember member : members) {
                    TeamMember managedMember = em.find(TeamMember.class, member.getId());
                    managedTask.getAssignedTo().add(managedMember);
                    managedMember.getAssignedTasks().add(managedTask);
                }
            }

            // Commit everything
            em.flush();
            em.getTransaction().commit();

            // Return the updated task
            return findById(taskId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error assigning members to task", e);
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to assign members to task: " + e.getMessage(), e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
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

    @Override
    public boolean addDependency(Long taskId, Long dependencyId) {
        if (taskId == null || dependencyId == null) {
            throw new IllegalArgumentException("Task IDs cannot be null");
        }

        if (taskId.equals(dependencyId)) {
            throw new IllegalArgumentException("A task cannot depend on itself");
        }

        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();
            em.getTransaction().begin();

            // Get managed entities
            Task task = em.find(Task.class, taskId);
            Task dependency = em.find(Task.class, dependencyId);

            if (task == null || dependency == null) {
                LOGGER.log(Level.WARNING, "Task not found with ID: {0} or {1}", new Object[] { taskId, dependencyId });
                em.getTransaction().rollback();
                return false;
            }

            // Check for circular dependencies
            if (wouldCreateCircularDependency(dependency, task, em)) {
                LOGGER.log(Level.WARNING, "Adding dependency would create a circular dependency");
                em.getTransaction().rollback();
                throw new IllegalArgumentException("Adding this dependency would create a circular dependency");
            }

            // Directly manage the bidirectional relationship
            task.getPreDependencies().add(dependency);
            dependency.getPostDependencies().add(task);

            // Commit the changes
            em.flush();
            em.getTransaction().commit();

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding dependency", e);
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("Failed to add dependency: " + e.getMessage(), e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public boolean removeDependency(Long taskId, Long dependencyId) {
        if (taskId == null || dependencyId == null) {
            throw new IllegalArgumentException("Task IDs cannot be null");
        }

        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();
            em.getTransaction().begin();

            // Get managed task and dependency
            Task task = em.find(Task.class, taskId);
            Task dependency = em.find(Task.class, dependencyId);

            if (task == null || dependency == null) {
                LOGGER.log(Level.WARNING, "Task not found with ID: {0} or {1}", new Object[] { taskId, dependencyId });
                em.getTransaction().rollback();
                return false;
            }

            // Check if dependency exists
            if (!task.getPreDependencies().contains(dependency)) {
                LOGGER.log(Level.WARNING, "Dependency does not exist between tasks {0} and {1}",
                        new Object[] { taskId, dependencyId });
                em.getTransaction().rollback();
                return false;
            }

            // Clear persistence context to avoid stale data
            em.clear();

            // Reload the entities
            task = em.find(Task.class, taskId);
            dependency = em.find(Task.class, dependencyId);

            // Remove the dependency - update both sides of the relationship
            task.getPreDependencies().remove(dependency);
            dependency.getPostDependencies().remove(task);

            em.flush();
            em.getTransaction().commit();

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error removing dependency", e);
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to remove dependency: " + e.getMessage(), e);
        } finally {
            if (em != null) {
                em.close();
            }
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

    // Helper method to check for circular dependencies
    private boolean hasCircularDependency(Task start, Task target, EntityManager em) {
        // Initialize a set to track visited tasks
        Set<Long> visited = new HashSet<>();
        return hasCircularDependencyRecursive(start, target, visited, em);
    }

    private boolean hasCircularDependencyRecursive(Task current, Task target, Set<Long> visited, EntityManager em) {
        if (current.getId().equals(target.getId())) {
            return true;
        }

        visited.add(current.getId());

        // Get the post dependencies (tasks that depend on the current task)
        TypedQuery<Task> query = em.createQuery(
                "SELECT t FROM Task t JOIN t.preDependencies pd WHERE pd.id = :currentId",
                Task.class);
        query.setParameter("currentId", current.getId());
        List<Task> postDependencies = query.getResultList();

        for (Task postDep : postDependencies) {
            // Skip tasks we've already visited to prevent infinite recursion
            if (visited.contains(postDep.getId())) {
                continue;
            }

            if (hasCircularDependencyRecursive(postDep, target, visited, em)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Helper method to get a task with all its collections initialized.
     */
    private Task getTaskWithInitializedCollections(Long taskId) {
        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();

            // Use JPQL to eagerly fetch the task with all collections
            TypedQuery<Task> query = em.createQuery(
                    "SELECT DISTINCT t FROM Task t " +
                            "LEFT JOIN FETCH t.assignedTo " +
                            "LEFT JOIN FETCH t.preDependencies " +
                            "LEFT JOIN FETCH t.postDependencies " +
                            "LEFT JOIN FETCH t.requiredComponents " +
                            "WHERE t.id = :id",
                    Task.class);
            query.setParameter("id", taskId);

            try {
                Task task = query.getSingleResult();
                // Access collections to ensure they're initialized
                if (task != null) {
                    // Force initialization of all collections
                    task.getAssignedTo().size();
                    task.getPreDependencies().size();
                    task.getPostDependencies().size();
                    task.getRequiredComponents().size();
                }
                return task;
            } catch (jakarta.persistence.NoResultException e) {
                LOGGER.log(Level.WARNING, "Task not found with ID: {0}", taskId);
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching task with collections", e);
            return null;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public Task findById(Long id) {
        if (id == null) {
            return null;
        }

        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();

            // Use JPQL to eagerly fetch the task with all collections
            TypedQuery<Task> query = em.createQuery(
                    "SELECT DISTINCT t FROM Task t " +
                            "LEFT JOIN FETCH t.assignedTo " +
                            "LEFT JOIN FETCH t.preDependencies " +
                            "LEFT JOIN FETCH t.postDependencies " +
                            "LEFT JOIN FETCH t.requiredComponents " +
                            "WHERE t.id = :id",
                    Task.class);
            query.setParameter("id", id);

            try {
                return query.getSingleResult();
            } catch (jakarta.persistence.NoResultException e) {
                LOGGER.log(Level.WARNING, "Task not found with ID: {0}", id);
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching task with collections", e);
            return null;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    // Helper method to ensure task is properly initialized in cache
    private void updateTaskCache(Long taskId) {
        // This is a no-op, but in a real implementation, you might update
        // any cache or refresh the entity in the persistence context
        try {
            Task refreshed = getTaskWithInitializedCollections(taskId);
            LOGGER.log(Level.FINE, "Task refreshed: {0}", taskId);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to refresh task", e);
        }
    }

    private boolean hasDependencyPath(Task start, Task target, EntityManager em) {
        // Create a new query for each check to avoid stale data
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(t) FROM Task t JOIN t.preDependencies d " +
                        "WHERE t.id = :startId AND d.id = :targetId",
                Long.class);
        query.setParameter("startId", start.getId());
        query.setParameter("targetId", target.getId());

        return query.getSingleResult() > 0;
    }

    private boolean wouldCreateCircularDependency(Task start, Task target, EntityManager em) {
        // Create a query to check for direct circular dependencies
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(t) FROM Task t JOIN t.preDependencies d " +
                        "WHERE t.id = :startId AND d.id = :targetId",
                Long.class);
        query.setParameter("startId", start.getId());
        query.setParameter("targetId", target.getId());

        return query.getSingleResult() > 0;
    }
}
