// src/main/java/org/frcpm/services/impl/TestableProjectServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.ProjectService;
import org.frcpm.di.ServiceLocator;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable implementation of ProjectService that accepts injected repositories
 * for better unit testing. This implementation eliminates static references to
 * RepositoryFactory and uses proper constructor injection for all dependencies.
 */
public class TestableProjectServiceImpl implements ProjectService {
    
    private static final Logger LOGGER = Logger.getLogger(TestableProjectServiceImpl.class.getName());
    
    // Dependencies injected via constructor
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableProjectServiceImpl() {
        this(
            ServiceLocator.getProjectRepository(),
            ServiceLocator.getTaskRepository()
        );
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param projectRepository the project repository
     * @param taskRepository the task repository
     */
    public TestableProjectServiceImpl(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

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
        try {
            return projectRepository.save(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving project", e);
            throw new RuntimeException("Failed to save project", e);
        }
    }
    
    @Override
    public void delete(Project entity) {
        try {
            projectRepository.delete(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting project", e);
            throw new RuntimeException("Failed to delete project", e);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        try {
            return projectRepository.deleteById(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting project by ID", e);
            throw new RuntimeException("Failed to delete project by ID", e);
        }
    }
    
    @Override
    public long count() {
        return projectRepository.count();
    }
    
    @Override
    public List<Project> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        return projectRepository.findByName(name);
    }
    
    @Override
    public List<Project> findByDeadlineBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return projectRepository.findByDeadlineBefore(date);
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
}