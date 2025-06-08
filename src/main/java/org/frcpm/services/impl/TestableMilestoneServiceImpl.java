// src/main/java/org/frcpm/services/impl/TestableMilestoneServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.services.MilestoneService;
import org.frcpm.di.ServiceLocator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable implementation of MilestoneService that accepts injected repositories
 * for better unit testing. This implementation eliminates static references to
 * RepositoryFactory and uses proper constructor injection for all dependencies.
 */
public class TestableMilestoneServiceImpl implements MilestoneService {
    
    private static final Logger LOGGER = Logger.getLogger(TestableMilestoneServiceImpl.class.getName());
    
    // Dependencies injected via constructor
    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableMilestoneServiceImpl() {
        this(
            ServiceLocator.getMilestoneRepository(),
            ServiceLocator.getProjectRepository()
        );
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param milestoneRepository the milestone repository
     * @param projectRepository the project repository
     */
    public TestableMilestoneServiceImpl(
            MilestoneRepository milestoneRepository,
            ProjectRepository projectRepository) {
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public Milestone findById(Long id) {
        if (id == null) {
            return null;
        }
        return milestoneRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Milestone> findAll() {
        return milestoneRepository.findAll();
    }
    
    @Override
    public Milestone save(Milestone entity) {
        try {
            return milestoneRepository.save(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving milestone", e);
            throw new RuntimeException("Failed to save milestone", e);
        }
    }
    
    @Override
    public void delete(Milestone entity) {
        try {
            milestoneRepository.delete(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting milestone", e);
            throw new RuntimeException("Failed to delete milestone", e);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        try {
            return milestoneRepository.deleteById(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting milestone by ID", e);
            throw new RuntimeException("Failed to delete milestone by ID", e);
        }
    }
    
    @Override
    public long count() {
        return milestoneRepository.count();
    }
    
    @Override
    public List<Milestone> findByProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        return milestoneRepository.findByProject(project);
    }
    
    @Override
    public List<Milestone> findByDateBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return milestoneRepository.findByDateBefore(date);
    }
    
    @Override
    public List<Milestone> findByDateAfter(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return milestoneRepository.findByDateAfter(date);
    }
    
    @Override
    public List<Milestone> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        return milestoneRepository.findByDateBetween(startDate, endDate);
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
        List<Milestone> allMilestones = milestoneRepository.findByProject(project);
        List<Milestone> upcomingMilestones = new ArrayList<>();
        
        // Filter milestones by date range
        for (Milestone milestone : allMilestones) {
            if (!milestone.getDate().isBefore(today) && !milestone.getDate().isAfter(endDate)) {
                upcomingMilestones.add(milestone);
            }
        }
        
        return upcomingMilestones;
    }
}