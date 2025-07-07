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

import org.frcpm.events.WebSocketEventPublisher;
import org.frcpm.models.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.frcpm.security.UserPrincipal;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Spring Boot implementation of ProjectService using composition pattern.
 * Converted from inheritance to composition for architectural consistency.
 */
@Service("projectServiceImpl")
@Transactional
public class ProjectServiceImpl implements ProjectService {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectServiceImpl.class.getName());
    
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    
    private final WebSocketEventPublisher webSocketEventPublisher;

    // UPDATE CONSTRUCTOR (add WebSocketEventPublisher parameter):
    public ProjectServiceImpl(ProjectRepository projectRepository, 
                             TaskRepository taskRepository,
                             WebSocketEventPublisher webSocketEventPublisher) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.webSocketEventPublisher = webSocketEventPublisher;
    }
    
    // =========================================================================
    // BASIC CRUD OPERATIONS - Implementing Service<Project, Long> interface
    // =========================================================================
    
    @Override
    public Project findById(Long id) {
        if (id == null) {
            return null;
        }
        return projectRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
    }
    
    @Override
    public Project save(Project entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        try {
            return projectRepository.save(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving project", e);
            throw new RuntimeException("Failed to save project", e);
        }
    }
    
    @Override
    public void delete(Project entity) {
        if (entity != null) {
            try {
                projectRepository.delete(entity);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting project", e);
                throw new RuntimeException("Failed to delete project", e);
            }
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id != null && projectRepository.existsById(id)) {
            try {
                projectRepository.deleteById(id);
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting project by ID", e);
                throw new RuntimeException("Failed to delete project by ID", e);
            }
        }
        return false;
    }
    
    @Override
    public long count() {
        return projectRepository.count();
    }
    
    // =========================================================================
    // BUSINESS LOGIC METHODS - ProjectService specific methods
    // =========================================================================
    
    @Override
    public List<Project> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        return projectRepository.findByNameContainingIgnoreCase(name);
    }
    
    @Override
    public List<Project> findByDeadlineBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return projectRepository.findByHardDeadlineBefore(date);
    }
    
    @Override
    public List<Project> findByStartDateAfter(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return projectRepository.findByStartDateAfter(date);
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
            
            // Get task statistics using TaskRepository
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

    // =========================================================================
    // ASYNC METHODS - Using @Async annotation with CompletableFuture
    // =========================================================================
    
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

    /**
     * Monitor project deadlines and send alerts.
     * Runs every hour during business hours.
     */
    @Scheduled(cron = "0 0 8-18 * * MON-FRI") // Every hour from 8 AM to 6 PM, Mon-Fri
    public void monitorProjectDeadlines() {
        try {
            List<Project> allProjects = findAll();
            LocalDate today = LocalDate.now();
            
            for (Project project : allProjects) {
                if (project.getHardDeadline() != null) {
                    long daysUntilDeadline = java.time.temporal.ChronoUnit.DAYS.between(today, project.getHardDeadline());
                    
                    // Send alerts for deadlines in 1, 3, or 7 days
                    if (daysUntilDeadline == 1 || daysUntilDeadline == 3 || daysUntilDeadline == 7 || daysUntilDeadline == 0) {
                        webSocketEventPublisher.publishDeadlineAlert(project, daysUntilDeadline);
                        LOGGER.info(String.format("Sent deadline alert for project %s: %d days remaining", 
                                                project.getName(), daysUntilDeadline));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error monitoring project deadlines", e);
        }
    }

    /**
     * Gets the current user from Spring Security context.
     * @return current user or null if not authenticated
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                authentication.getPrincipal() instanceof UserPrincipal) {
                return ((UserPrincipal) authentication.getPrincipal()).getUser();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not get current user from security context", e);
        }
        return null;
    }
}