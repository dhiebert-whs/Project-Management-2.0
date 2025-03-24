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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ProjectService using repository layer.
 */
public class ProjectServiceImpl extends AbstractService<Project, Long, ProjectRepository> 
        implements ProjectService {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectServiceImpl.class.getName());
    private final TaskRepository taskRepository;
    
    public ProjectServiceImpl() {
        super(RepositoryFactory.getProjectRepository());
        this.taskRepository = RepositoryFactory.getTaskRepository();
    }
    
    @Override
    public List<Project> findByName(String name) {
        return repository.findByName(name);
    }
    
    @Override
    public List<Project> findByDeadlineBefore(LocalDate date) {
        return repository.findByDeadlineBefore(date);
    }
    
    @Override
    public List<Project> findByStartDateAfter(LocalDate date) {
        return repository.findByStartDateAfter(date);
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
        
        Project project = findById(projectId);
        if (project == null) {
            LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
            return summary;
        }
        
        // Get project details
        summary.put("id", project.getId());
        summary.put("name", project.getName());
        summary.put("startDate", project.getStartDate());
        summary.put("goalEndDate", project.getGoalEndDate());
        summary.put("hardDeadline", project.getHardDeadline());
        
        // Get task statistics
        List<Task> tasks = taskRepository.findByProject(project);
        int totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(Task::isCompleted).count();
        double completionPercentage = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        
        summary.put("totalTasks", totalTasks);
        summary.put("completedTasks", completedTasks);
        summary.put("completionPercentage", completionPercentage);
        
        // Calculate days remaining
        LocalDate today = LocalDate.now();
        long daysUntilGoal = java.time.temporal.ChronoUnit.DAYS.between(today, project.getGoalEndDate());
        long daysUntilDeadline = java.time.temporal.ChronoUnit.DAYS.between(today, project.getHardDeadline());
        
        summary.put("daysUntilGoal", daysUntilGoal);
        summary.put("daysUntilDeadline", daysUntilDeadline);
        
        // Count milestones
        int totalMilestones = project.getMilestones().size();
        summary.put("totalMilestones", totalMilestones);
        
        return summary;
    }
}
