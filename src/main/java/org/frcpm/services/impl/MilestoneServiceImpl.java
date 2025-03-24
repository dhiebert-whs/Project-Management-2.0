package org.frcpm.services.impl;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.services.MilestoneService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of MilestoneService using repository layer.
 */
public class MilestoneServiceImpl extends AbstractService<Milestone, Long, MilestoneRepository>
        implements MilestoneService {
    
    private static final Logger LOGGER = Logger.getLogger(MilestoneServiceImpl.class.getName());
    private final ProjectRepository projectRepository;
    
    public MilestoneServiceImpl() {
        super(RepositoryFactory.getMilestoneRepository());
        this.projectRepository = RepositoryFactory.getProjectRepository();
    }
    
    @Override
    public List<Milestone> findByProject(Project project) {
        return repository.findByProject(project);
    }
    
    @Override
    public List<Milestone> findByDateBefore(LocalDate date) {
        return repository.findByDateBefore(date);
    }
    
    @Override
    public List<Milestone> findByDateAfter(LocalDate date) {
        return repository.findByDateAfter(date);
    }
    
    @Override
    public List<Milestone> findByDateBetween(LocalDate startDate, LocalDate endDate) {
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
        
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
            throw new IllegalArgumentException("Project not found with ID: " + projectId);
        }
        
        // Validate that the milestone date is within the project timeline
        if (date.isBefore(project.getStartDate()) || date.isAfter(project.getHardDeadline())) {
            LOGGER.log(Level.WARNING, "Milestone date is outside the project timeline");
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
        
        Project project = milestone.getProject();
        
        // Validate that the milestone date is within the project timeline
        if (date.isBefore(project.getStartDate()) || date.isAfter(project.getHardDeadline())) {
            LOGGER.log(Level.WARNING, "Milestone date is outside the project timeline");
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
        
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
            return new ArrayList<>();
        }
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        List<Milestone> milestones = repository.findByProject(project);
        List<Milestone> upcomingMilestones = new ArrayList<>();
        
        for (Milestone milestone : milestones) {
            if (!milestone.getDate().isBefore(today) && !milestone.getDate().isAfter(endDate)) {
                upcomingMilestones.add(milestone);
            }
        }
        
        return upcomingMilestones;
    }
}