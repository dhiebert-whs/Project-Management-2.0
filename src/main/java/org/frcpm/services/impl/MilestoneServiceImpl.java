package org.frcpm.services.impl;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.spring.MilestoneRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.services.MilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spring Boot implementation of MilestoneService.
 * Converted from JavaFX/MVVMFx to Spring Boot with dependency injection.
 */
@Service("milestoneServiceImpl")
@Transactional
public class MilestoneServiceImpl extends AbstractSpringService<Milestone, Long, MilestoneRepository> 
        implements MilestoneService {
    
    private static final Logger LOGGER = Logger.getLogger(MilestoneServiceImpl.class.getName());
    
    private final ProjectRepository projectRepository;
    
    @Autowired
    public MilestoneServiceImpl(MilestoneRepository milestoneRepository, 
                               ProjectRepository projectRepository) {
        super(milestoneRepository);
        this.projectRepository = projectRepository;
    }

    @Override
    protected String getEntityName() {
        return "milestone";
    }

    // Milestone-specific operations
    
    @Override
    public List<Milestone> findByProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        return repository.findByProject(project);
    }
    
    @Override
    public List<Milestone> findByDateBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return repository.findByDateBefore(date);
    }
    
    @Override
    public List<Milestone> findByDateAfter(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return repository.findByDateAfter(date);
    }
    
    @Override
    public List<Milestone> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        return repository.findByDateBetween(startDate, endDate);
    }
    
    @Override
    public Milestone createMilestone(String name, LocalDate date, Long projectId, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Milestone name cannot be empty");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Milestone date cannot be null");
        }
        
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
            throw new IllegalArgumentException("Project not found with ID: " + projectId);
        }
        
        Project project = projectOpt.get();
        
        // Validate that the milestone date is within the project timeline
        if (project.getStartDate() != null && date.isBefore(project.getStartDate())) {
            LOGGER.log(Level.WARNING, "Milestone date is before project start date");
        }
        
        if (project.getHardDeadline() != null && date.isAfter(project.getHardDeadline())) {
            LOGGER.log(Level.WARNING, "Milestone date is after project deadline");
        }
        
        Milestone milestone = new Milestone(name, date, project);
        milestone.setDescription(description);
        
        return save(milestone);
    }
    
    @Override
    public Milestone updateMilestoneDate(Long milestoneId, LocalDate date) {
        if (milestoneId == null) {
            throw new IllegalArgumentException("Milestone ID cannot be null");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Milestone date cannot be null");
        }
        
        Milestone milestone = findById(milestoneId);
        if (milestone == null) {
            LOGGER.log(Level.WARNING, "Milestone not found with ID: {0}", milestoneId);
            return null;
        }
        
        // Optional validation (just log warnings)
        Project project = milestone.getProject();
        if (project != null) {
            if (project.getStartDate() != null && date.isBefore(project.getStartDate())) {
                LOGGER.log(Level.WARNING, "Milestone date {0} is before project start date {1}",
                        new Object[]{date, project.getStartDate()});
            }
            
            if (project.getHardDeadline() != null && date.isAfter(project.getHardDeadline())) {
                LOGGER.log(Level.WARNING, "Milestone date {0} is after project deadline {1}",
                        new Object[]{date, project.getHardDeadline()});
            }
        }
        
        milestone.setDate(date);
        return save(milestone);
    }
    
    @Override
    public Milestone updateDescription(Long milestoneId, String description) {
        if (milestoneId == null) {
            throw new IllegalArgumentException("Milestone ID cannot be null");
        }
        
        Milestone milestone = findById(milestoneId);
        if (milestone == null) {
            LOGGER.log(Level.WARNING, "Milestone not found with ID: {0}", milestoneId);
            return null;
        }
        
        milestone.setDescription(description);
        return save(milestone);
    }
    
    @Override
    public List<Milestone> getUpcomingMilestones(Long projectId, int days) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
        
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
            return new ArrayList<>();
        }
        
        Project project = projectOpt.get();
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        // Get all milestones for the project
        List<Milestone> allMilestones = repository.findByProject(project);
        List<Milestone> upcomingMilestones = new ArrayList<>();
        
        // Filter milestones by date range
        for (Milestone milestone : allMilestones) {
            if (!milestone.getDate().isBefore(today) && !milestone.getDate().isAfter(endDate)) {
                upcomingMilestones.add(milestone);
            }
        }
        
        return upcomingMilestones;
    }

    // Spring @Async methods for background processing

    @Async
    public CompletableFuture<List<Milestone>> findAllAsync() {
        return CompletableFuture.completedFuture(findAll());
    }

    @Async
    public CompletableFuture<Milestone> findByIdAsync(Long id) {
        return CompletableFuture.completedFuture(findById(id));
    }

    @Async
    public CompletableFuture<List<Milestone>> findByProjectAsync(Project project) {
        return CompletableFuture.completedFuture(findByProject(project));
    }

    @Async
    public CompletableFuture<Milestone> saveAsync(Milestone milestone) {
        return CompletableFuture.completedFuture(save(milestone));
    }

    @Async
    public CompletableFuture<List<Milestone>> getUpcomingMilestonesAsync(Long projectId, int days) {
        return CompletableFuture.completedFuture(getUpcomingMilestones(projectId, days));
    }

    @Async
    public CompletableFuture<Milestone> createMilestoneAsync(String name, LocalDate date, Long projectId, String description) {
        return CompletableFuture.completedFuture(createMilestone(name, date, projectId, description));
    }
}