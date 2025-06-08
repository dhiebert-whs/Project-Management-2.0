// src/main/java/org/frcpm/services/impl/ProjectServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.repositories.spring.TaskRepository;
import org.frcpm.services.ProjectService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spring Boot implementation of ProjectService.
 * Updated to extend AbstractSpringService for consistent CRUD operations.
 */
@Service("projectServiceImpl")
@Transactional
public class ProjectServiceImpl extends AbstractSpringService<Project, Long, ProjectRepository> 
        implements ProjectService {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectServiceImpl.class.getName());
    
    // Additional dependencies injected via constructor
    private final TaskRepository taskRepository;
    
    public ProjectServiceImpl(ProjectRepository projectRepository, TaskRepository taskRepository) {
        super(projectRepository);
        this.taskRepository = taskRepository;
    }

    @Override
    protected String getEntityName() {
        return "project";
    }
    
    // Project-specific business methods
    
    @Override
    public List<Project> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        return repository.findByNameContainingIgnoreCase(name);
    }
    
    @Override
    public List<Project> findByDeadlineBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return repository.findByHardDeadlineBefore(date);
    }
    
    @Override
    public List<Project> findByStartDateAfter(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return repository.findByStartDateAfter(date);
    }
    
    @Override
    public Project createProject(String name, LocalDate startDate, LocalDate goalEndDate, LocalDate hardDeadline) {
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
            
            // Get task statistics using Spring Data JPA
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

    // Spring Boot Async Methods
    
    @Async
    public CompletableFuture<List<Project>> findAllAsync() {
        try {
            List<Project> result = findAll();
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<Project>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Project> findByIdAsync(Long id) {
        try {
            Project result = findById(id);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Project> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Project> saveAsync(Project entity) {
        try {
            Project result = save(entity);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Project> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Boolean> deleteByIdAsync(Long id) {
        try {
            boolean result = deleteById(id);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> getProjectSummaryAsync(Long projectId) {
        try {
            Map<String, Object> result = getProjectSummary(projectId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<List<Project>> findByNameAsync(String name) {
        try {
            List<Project> result = findByName(name);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<Project>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Project> createProjectAsync(String name, LocalDate startDate, 
                                                        LocalDate goalEndDate, LocalDate hardDeadline) {
        try {
            Project result = createProject(name, startDate, goalEndDate, hardDeadline);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Project> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}