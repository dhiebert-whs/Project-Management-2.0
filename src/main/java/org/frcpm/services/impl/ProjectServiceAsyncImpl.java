// src/main/java/org/frcpm/services/impl/ProjectServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.ProjectService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ProjectService using the task-based threading model.
 */
public class ProjectServiceAsyncImpl extends AbstractAsyncService<Project, Long, ProjectRepository>
        implements ProjectService {

    private static final Logger LOGGER = Logger.getLogger(ProjectServiceAsyncImpl.class.getName());
    private final TaskRepository taskRepository;

    public ProjectServiceAsyncImpl() {
        super(RepositoryFactory.getProjectRepository());
        this.taskRepository = RepositoryFactory.getTaskRepository();
    }

    // Existing synchronous methods from ProjectServiceImpl are inherited from AbstractService
    // We only need to implement the ProjectService interface methods

    @Override
    public List<Project> findByName(String name) {
        try {
            if (name == null) {
                throw new IllegalArgumentException("Name cannot be null");
            }
            return repository.findByName(name);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding projects by name", e);
            return List.of();
        }
    }

    @Override
    public List<Project> findByDeadlineBefore(LocalDate date) {
        try {
            if (date == null) {
                throw new IllegalArgumentException("Date cannot be null");
            }
            return repository.findByDeadlineBefore(date);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding projects by deadline", e);
            return List.of();
        }
    }

    @Override
    public List<Project> findByStartDateAfter(LocalDate date) {
        try {
            if (date == null) {
                throw new IllegalArgumentException("Date cannot be null");
            }
            return repository.findByStartDateAfter(date);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding projects by start date", e);
            return List.of();
        }
    }

    @Override
    public Project createProject(String name, LocalDate startDate, LocalDate goalEndDate,
                                LocalDate hardDeadline) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }

        if (startDate == null || goalEndDate == null || hardDeadline == null) {
            throw new IllegalArgumentException("Project dates cannot be null");
        }

        if (goalEndDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Goal end date cannot be before start date");
        }

        if (hardDeadline.isBefore(startDate)) {
            throw new IllegalArgumentException("Hard deadline cannot be before start date");
        }

        Project project = new Project(name, startDate, goalEndDate, hardDeadline);
        return save(project);
    }

    @Override
    public Project updateProject(Long id, String name, LocalDate startDate, LocalDate goalEndDate,
                                LocalDate hardDeadline, String description) {
        if (id == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }

        Project project = findById(id);
        if (project == null) {
            LOGGER.log(Level.WARNING, "Project not found with ID: {0}", id);
            return null;
        }

        if (name != null && !name.trim().isEmpty()) {
            project.setName(name);
        }

        if (startDate != null) {
            project.setStartDate(startDate);
        }

        if (goalEndDate != null) {
            if (goalEndDate.isBefore(project.getStartDate())) {
                throw new IllegalArgumentException("Goal end date cannot be before start date");
            }
            project.setGoalEndDate(goalEndDate);
        }

        if (hardDeadline != null) {
            if (hardDeadline.isBefore(project.getStartDate())) {
                throw new IllegalArgumentException("Hard deadline cannot be before start date");
            }
            project.setHardDeadline(hardDeadline);
        }

        if (description != null) {
            project.setDescription(description);
        }

        return save(project);
    }

    @Override
    public Map<String, Object> getProjectSummary(Long projectId) {
        Map<String, Object> summary = new HashMap<>();

        if (projectId == null) {
            LOGGER.log(Level.WARNING, "Cannot get summary for null project ID");
            return summary;
        }

        Project project = findById(projectId);
        if (project == null) {
            LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
            return summary;
        }

        try {
            // Get project details
            summary.put("id", project.getId());
            summary.put("name", project.getName());
            summary.put("startDate", project.getStartDate());
            summary.put("goalEndDate", project.getGoalEndDate());
            summary.put("hardDeadline", project.getHardDeadline());

            // Get task statistics
            List<Task> tasks = taskRepository.findByProject(project);
            int totalTasks = tasks != null ? tasks.size() : 0;
            long completedTasks = tasks != null ?
                    tasks.stream().filter(t -> t != null && t.isCompleted()).count() : 0;
            double completionPercentage = totalTasks > 0 ?
                    (double) completedTasks / totalTasks * 100 : 0;

            summary.put("totalTasks", totalTasks);
            summary.put("completedTasks", (int) completedTasks);
            summary.put("completionPercentage", completionPercentage);

            // Calculate days remaining
            LocalDate today = LocalDate.now();
            long daysUntilGoal = project.getGoalEndDate() != null ?
                    java.time.temporal.ChronoUnit.DAYS.between(today, project.getGoalEndDate()) : 0;
            long daysUntilDeadline = project.getHardDeadline() != null ?
                    java.time.temporal.ChronoUnit.DAYS.between(today, project.getHardDeadline()) : 0;

            summary.put("daysUntilGoal", daysUntilGoal);
            summary.put("daysUntilDeadline", daysUntilDeadline);

            // Count milestones
            int totalMilestones = project.getMilestones() != null ? project.getMilestones().size() : 0;
            summary.put("totalMilestones", totalMilestones);

            return summary;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating project summary", e);

            // Ensure all required fields are in the summary even in case of error
            if (!summary.containsKey("totalTasks")) summary.put("totalTasks", 0);
            if (!summary.containsKey("completedTasks")) summary.put("completedTasks", 0);
            if (!summary.containsKey("completionPercentage")) summary.put("completionPercentage", 0.0);
            if (!summary.containsKey("daysUntilGoal")) summary.put("daysUntilGoal", 0L);
            if (!summary.containsKey("daysUntilDeadline")) summary.put("daysUntilDeadline", 0L);
            if (!summary.containsKey("totalMilestones")) summary.put("totalMilestones", 0);

            return summary;
        }
    }

    // New asynchronous methods - FIXED TO REMOVE ENTITYMANAGER PARAMETER

    /**
     * Finds projects by name asynchronously.
     *
     * @param name the project name to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of matching projects
     */
    public CompletableFuture<List<Project>> findByNameAsync(String name,
                                                          Consumer<List<Project>> onSuccess,
                                                          Consumer<Throwable> onFailure) {
        if (name == null) {
            CompletableFuture<List<Project>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Name cannot be null"));
            return future;
        }

        return executeAsync("Find Projects By Name: " + name, em -> {
            return repository.findByName(name);
        }, onSuccess, onFailure);
    }

    /**
     * Finds projects with deadlines before the specified date asynchronously.
     *
     * @param date the date to compare against
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of projects
     */
    public CompletableFuture<List<Project>> findByDeadlineBeforeAsync(LocalDate date,
                                                                    Consumer<List<Project>> onSuccess,
                                                                    Consumer<Throwable> onFailure) {
        if (date == null) {
            CompletableFuture<List<Project>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Date cannot be null"));
            return future;
        }

        return executeAsync("Find Projects By Deadline Before: " + date, em -> {
            return repository.findByDeadlineBefore(date);
        }, onSuccess, onFailure);
    }

    /**
     * Finds projects starting after the specified date asynchronously.
     *
     * @param date the date to compare against
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of projects
     */
    public CompletableFuture<List<Project>> findByStartDateAfterAsync(LocalDate date,
                                                                    Consumer<List<Project>> onSuccess,
                                                                    Consumer<Throwable> onFailure) {
        if (date == null) {
            CompletableFuture<List<Project>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Date cannot be null"));
            return future;
        }

        return executeAsync("Find Projects By Start Date After: " + date, em -> {
            return repository.findByStartDateAfter(date);
        }, onSuccess, onFailure);
    }

    /**
     * Creates a new project with the specified details asynchronously.
     *
     * @param name the project name
     * @param startDate the start date
     * @param goalEndDate the goal end date
     * @param hardDeadline the hard deadline
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created project
     */
    public CompletableFuture<Project> createProjectAsync(String name, LocalDate startDate,
                                                      LocalDate goalEndDate, LocalDate hardDeadline,
                                                      Consumer<Project> onSuccess,
                                                      Consumer<Throwable> onFailure) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Project name cannot be empty");
            }

            if (startDate == null || goalEndDate == null || hardDeadline == null) {
                throw new IllegalArgumentException("Project dates cannot be null");
            }

            if (goalEndDate.isBefore(startDate)) {
                throw new IllegalArgumentException("Goal end date cannot be before start date");
            }

            if (hardDeadline.isBefore(startDate)) {
                throw new IllegalArgumentException("Hard deadline cannot be before start date");
            }

            return executeAsync("Create Project: " + name, em -> {
                Project project = new Project(name, startDate, goalEndDate, hardDeadline);
                return repository.save(project);
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Project> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Updates an existing project with the specified details asynchronously.
     *
     * @param id the project ID
     * @param name the project name
     * @param startDate the start date
     * @param goalEndDate the goal end date
     * @param hardDeadline the hard deadline
     * @param description the project description
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated project
     */
    public CompletableFuture<Project> updateProjectAsync(Long id, String name,
                                                      LocalDate startDate, LocalDate goalEndDate,
                                                      LocalDate hardDeadline, String description,
                                                      Consumer<Project> onSuccess,
                                                      Consumer<Throwable> onFailure) {
        if (id == null) {
            CompletableFuture<Project> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Project ID cannot be null"));
            return future;
        }

        return executeAsync("Update Project: " + id, em -> {
            // First, find the existing project
            Project project = em.find(Project.class, id);
            if (project == null) {
                LOGGER.log(Level.WARNING, "Project not found with ID: {0}", id);
                return null;
            }

            if (name != null && !name.trim().isEmpty()) {
                project.setName(name);
            }

            if (startDate != null) {
                project.setStartDate(startDate);
            }

            if (goalEndDate != null) {
                if (goalEndDate.isBefore(project.getStartDate())) {
                    throw new IllegalArgumentException("Goal end date cannot be before start date");
                }
                project.setGoalEndDate(goalEndDate);
            }

            if (hardDeadline != null) {
                if (hardDeadline.isBefore(project.getStartDate())) {
                    throw new IllegalArgumentException("Hard deadline cannot be before start date");
                }
                project.setHardDeadline(hardDeadline);
            }

            if (description != null) {
                project.setDescription(description);
            }

            // Merge the updated project
            em.merge(project);
            return project;
        }, onSuccess, onFailure);
    }

    /**
     * Gets a summary of the project including task counts, completion percentage, etc. asynchronously.
     *
     * @param projectId the project ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the summary data
     */
    public CompletableFuture<Map<String, Object>> getProjectSummaryAsync(Long projectId,
                                                                      Consumer<Map<String, Object>> onSuccess,
                                                                      Consumer<Throwable> onFailure) {
        if (projectId == null) {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Project ID cannot be null"));
            return future;
        }

        return executeAsync("Get Project Summary: " + projectId, em -> {
            Map<String, Object> summary = new HashMap<>();

            Project project = em.find(Project.class, projectId);
            if (project == null) {
                LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
                return summary;
            }

            try {
                // Get project details
                summary.put("id", project.getId());
                summary.put("name", project.getName());
                summary.put("startDate", project.getStartDate());
                summary.put("goalEndDate", project.getGoalEndDate());
                summary.put("hardDeadline", project.getHardDeadline());

                // Get task statistics using entityManager for the query
                List<Task> tasks = taskRepository.findByProject(project);
                int totalTasks = tasks != null ? tasks.size() : 0;
                long completedTasks = tasks != null ?
                        tasks.stream().filter(t -> t != null && t.isCompleted()).count() : 0;
                double completionPercentage = totalTasks > 0 ?
                        (double) completedTasks / totalTasks * 100 : 0;

                summary.put("totalTasks", totalTasks);
                summary.put("completedTasks", (int) completedTasks);
                summary.put("completionPercentage", completionPercentage);

                // Calculate days remaining
                LocalDate today = LocalDate.now();
                long daysUntilGoal = project.getGoalEndDate() != null ?
                        java.time.temporal.ChronoUnit.DAYS.between(today, project.getGoalEndDate()) : 0;
                long daysUntilDeadline = project.getHardDeadline() != null ?
                        java.time.temporal.ChronoUnit.DAYS.between(today, project.getHardDeadline()) : 0;

                summary.put("daysUntilGoal", daysUntilGoal);
                summary.put("daysUntilDeadline", daysUntilDeadline);

                // Count milestones
                int totalMilestones = project.getMilestones() != null ? project.getMilestones().size() : 0;
                summary.put("totalMilestones", totalMilestones);

                return summary;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error generating project summary", e);

                // Ensure all required fields are in the summary even in case of error
                if (!summary.containsKey("totalTasks")) summary.put("totalTasks", 0);
                if (!summary.containsKey("completedTasks")) summary.put("completedTasks", 0);
                if (!summary.containsKey("completionPercentage")) summary.put("completionPercentage", 0.0);
                if (!summary.containsKey("daysUntilGoal")) summary.put("daysUntilGoal", 0L);
                if (!summary.containsKey("daysUntilDeadline")) summary.put("daysUntilDeadline", 0L);
                if (!summary.containsKey("totalMilestones")) summary.put("totalMilestones", 0);

                return summary;
            }
        }, onSuccess, onFailure);
    }
}