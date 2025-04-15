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
import jakarta.persistence.Query;
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
    
        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();
            em.getTransaction().begin();
            
            // Get a fully managed Task entity
            Task task = em.find(Task.class, taskId);
            if (task == null) {
                LOGGER.log(Level.WARNING, "Task not found with ID: {0}", taskId);
                em.getTransaction().rollback();
                return null;
            }
            
            // Create a copy of current assignments to avoid concurrent modification issues
            Set<TeamMember> currentAssignments = new HashSet<>(task.getAssignedTo());
            
            // Clear existing assignments by directly manipulating both sides
            for (TeamMember member : currentAssignments) {
                TeamMember managedMember = em.find(TeamMember.class, member.getId());
                task.getAssignedTo().remove(managedMember);
                managedMember.getAssignedTasks().remove(task);
            }
            
            // Add new assignments with direct manipulation
            if (members != null) {
                for (TeamMember member : members) {
                    if (member != null && member.getId() != null) {
                        TeamMember managedMember = em.find(TeamMember.class, member.getId());
                        if (managedMember != null) {
                            task.getAssignedTo().add(managedMember);
                            managedMember.getAssignedTasks().add(task);
                        }
                    }
                }
            }
            
            // Ensure changes are persisted
            em.flush();
            task = em.merge(task);
            em.getTransaction().commit();
            
            return task;
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
            
            // Get managed instances directly
            Task task = em.find(Task.class, taskId);
            Task dependency = em.find(Task.class, dependencyId);
            
            if (task == null || dependency == null) {
                LOGGER.log(Level.WARNING, "Task not found with ID: {0} or {1}", 
                        new Object[]{taskId, dependencyId});
                em.getTransaction().rollback();
                return false;
            }
            
            // Check for circular dependency
            if (checkCircularDependency(dependency, task, em)) {
                LOGGER.log(Level.WARNING, "Adding dependency {0} to task {1} would create a circular reference", 
                        new Object[]{dependencyId, taskId});
                em.getTransaction().rollback();
                return false;
            }
            
            // Manually establish the bidirectional relationship
            task.getPreDependencies().add(dependency);
            dependency.getPostDependencies().add(task);
            
            // Ensure changes are persisted
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

            // Get managed instances
            Task task = em.find(Task.class, taskId);
            Task dependency = em.find(Task.class, dependencyId);

            if (task == null || dependency == null) {
                LOGGER.log(Level.WARNING, "Task not found with ID: {0} or {1}", 
                        new Object[]{taskId, dependencyId});
                em.getTransaction().rollback();
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
                        new Object[]{taskId, dependencyId});
                em.getTransaction().rollback();
                return false;
            }

            // Manually maintain both sides of the relationship
            task.getPreDependencies().remove(dependency);
            dependency.getPostDependencies().remove(task);

            // Ensure changes are persisted
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

    /**
     * Helper method to check for circular dependencies
     */
    private boolean checkCircularDependency(Task dependency, Task task, EntityManager em) {
        // Simple SQL-based check for immediate circular dependency
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(t) FROM Task t JOIN t.preDependencies d " +
                        "WHERE t.id = :depId AND d.id = :taskId",
                Long.class);
        query.setParameter("depId", dependency.getId());
        query.setParameter("taskId", task.getId());

        return query.getSingleResult() > 0;
    }

    /**
     * Override findById to eagerly fetch collections
     */
    @Override
    public Task findById(Long id) {
        if (id == null) {
            return null;
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

    /**
     * Checks if adding dependency as a pre-dependency of task would create a
     * circular dependency.
     * 
     * @param dependency the dependency task
     * @param task       the task that would depend on the dependency
     * @param em         the entity manager
     * @return true if adding the dependency would create a circular dependency,
     *         false otherwise
     */
    private boolean wouldCreateCircularDependency(Task dependency, Task task, EntityManager em) {
        // If dependency depends on task, there would be a direct cycle
        if (hasDependencyPath(dependency, task, em)) {
            return true;
        }

        // Check for indirect cycles through other tasks
        return hasIndirectCircularDependency(dependency, task, new HashSet<>(), em);
    }

    /**
     * Checks if there is a direct dependency path from the source task to the
     * target task.
     * 
     * @param source the source task
     * @param target the target task
     * @param em     the entity manager
     * @return true if the source task depends on the target task, false otherwise
     */
    private boolean hasDependencyPath(Task source, Task target, EntityManager em) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(t) FROM Task t JOIN t.preDependencies d " +
                        "WHERE t.id = :sourceId AND d.id = :targetId",
                Long.class);
        query.setParameter("sourceId", source.getId());
        query.setParameter("targetId", target.getId());

        return query.getSingleResult() > 0;
    }

    /**
     * Recursively checks for indirect circular dependencies.
     * 
     * @param current the current task in the dependency chain
     * @param target  the original task we're checking for cycles with
     * @param visited set of already visited task IDs to avoid infinite recursion
     * @param em      the entity manager
     * @return true if an indirect circular dependency is detected, false otherwise
     */
    private boolean hasIndirectCircularDependency(Task current, Task target, Set<Long> visited, EntityManager em) {
        // Skip already visited tasks to prevent infinite recursion
        if (visited.contains(current.getId())) {
            return false;
        }

        // Mark current task as visited
        visited.add(current.getId());

        // Get all tasks that depend on the current task
        TypedQuery<Task> query = em.createQuery(
                "SELECT t FROM Task t JOIN t.preDependencies d WHERE d.id = :currentId",
                Task.class);
        query.setParameter("currentId", current.getId());
        List<Task> postDependencies = query.getResultList();

        for (Task postDep : postDependencies) {
            // Check if we found our target (which would create a cycle)
            if (postDep.getId().equals(target.getId())) {
                return true;
            }

            // Recursively check for cycles through this task's dependencies
            if (hasIndirectCircularDependency(postDep, target, visited, em)) {
                return true;
            }
        }

        return false;
    }
}