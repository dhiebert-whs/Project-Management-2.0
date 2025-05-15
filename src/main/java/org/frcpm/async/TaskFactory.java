package org.frcpm.async;

import javafx.concurrent.Task;
import org.frcpm.models.Project;
import org.frcpm.models.Task.Priority;
import org.frcpm.services.*;
import org.frcpm.di.ServiceLocator; // Changed from ServiceProvider

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Factory class for creating common task types.
 * Provides methods for creating tasks for common operations.
 */
public class TaskFactory {

    private static final Logger LOGGER = Logger.getLogger(TaskFactory.class.getName());

    /**
     * Creates a task for loading all projects.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of projects
     */
    public static CompletableFuture<List<Project>> loadProjects(
            Consumer<List<Project>> onSuccess, Consumer<Throwable> onFailure) {

        ProjectService projectService = ServiceLocator.getProjectService(); // Changed

        return TaskExecutor.executeAsync(
                "Load Projects",
                projectService::findAll,
                onSuccess,
                onFailure);
    }

    /**
     * Creates a task for loading a project by ID.
     * 
     * @param projectId the ID of the project
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the project
     */
    public static CompletableFuture<Project> loadProject(
            Long projectId, Consumer<Project> onSuccess, Consumer<Throwable> onFailure) {

        ProjectService projectService = ServiceLocator.getProjectService(); // Changed

        return TaskExecutor.executeAsync(
                "Load Project " + projectId,
                () -> projectService.findById(projectId),
                onSuccess,
                onFailure);
    }

    /**
     * Creates a task for saving a project.
     * 
     * @param project   the project to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved project
     */
    public static CompletableFuture<Project> saveProject(
            Project project, Consumer<Project> onSuccess, Consumer<Throwable> onFailure) {

        ProjectService projectService = ServiceLocator.getProjectService(); // Changed

        return TaskExecutor.executeAsync(
                "Save Project " + (project.getId() == null ? "New" : project.getId()),
                () -> projectService.save(project),
                onSuccess,
                onFailure);
    }

    /**
     * Creates a task for creating a new project.
     * 
     * @param name         the name of the project
     * @param startDate    the start date
     * @param goalEndDate  the goal end date
     * @param hardDeadline the hard deadline
     * @param description  the description
     * @param onSuccess    the callback to run on success
     * @param onFailure    the callback to run on failure
     * @return a CompletableFuture that will be completed with the created project
     */
    public static CompletableFuture<Project> createProject(
            String name, LocalDate startDate, LocalDate goalEndDate, LocalDate hardDeadline,
            String description, Consumer<Project> onSuccess, Consumer<Throwable> onFailure) {

        ProjectService projectService = ServiceLocator.getProjectService(); // Changed

        return TaskExecutor.executeAsync(
                "Create Project " + name,
                () -> {
                    Project project = projectService.createProject(name, startDate, goalEndDate, hardDeadline);
                    project.setDescription(description);
                    return projectService.save(project);
                },
                onSuccess,
                onFailure);
    }

    /**
     * Creates a task for deleting a project.
     * 
     * @param projectId the ID of the project to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a boolean indicating
     *         success
     */
    public static CompletableFuture<Boolean> deleteProject(
            Long projectId, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {

        ProjectService projectService = ServiceLocator.getProjectService(); // Changed

        return TaskExecutor.executeAsync(
                "Delete Project " + projectId,
                () -> projectService.deleteById(projectId),
                onSuccess,
                onFailure);
    }

    /**
     * Creates a task for creating a new task in a project.
     * 
     * @param title          the title of the task
     * @param project        the project
     * @param subsystem      the subsystem
     * @param estimatedHours the estimated hours
     * @param priority       the priority
     * @param startDate      the start date
     * @param endDate        the end date
     * @param onSuccess      the callback to run on success
     * @param onFailure      the callback to run on failure
     * @return a CompletableFuture that will be completed with the created task
     */
    public static CompletableFuture<org.frcpm.models.Task> createTask(
            String title, Project project, org.frcpm.models.Subsystem subsystem,
            double estimatedHours, Priority priority, LocalDate startDate, LocalDate endDate,
            Consumer<org.frcpm.models.Task> onSuccess, Consumer<Throwable> onFailure) {

        TaskService taskService = ServiceLocator.getTaskService(); // Changed

        return TaskExecutor.executeAsync(
                "Create Task " + title,
                () -> taskService.createTask(title, project, subsystem, estimatedHours, priority, startDate, endDate),
                onSuccess,
                onFailure);
    }

    /**
     * Creates a database task with progress reporting.
     * 
     * @param <T>               the result type
     * @param taskName          the name of the task
     * @param databaseOperation the database operation
     * @return the task
     */
    public static <T> Task<T> createDatabaseTask(String taskName,
            java.util.function.Function<jakarta.persistence.EntityManager, T> databaseOperation) {
        return new DatabaseTask<>(taskName, databaseOperation);
    }
}