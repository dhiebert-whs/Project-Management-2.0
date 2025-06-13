// src/main/java/org/frcpm/services/impl/MilestoneServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.spring.MilestoneRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.services.MilestoneService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Boot implementation of MilestoneService using composition pattern.
 * Eliminates AbstractSpringService inheritance to resolve compilation errors.
 */
@Service
@Transactional
public class MilestoneServiceImpl implements MilestoneService {
    
    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    
    /**
     * Constructor injection for repositories.
     * No @Autowired needed with single constructor.
     */
    public MilestoneServiceImpl(MilestoneRepository milestoneRepository,
                               ProjectRepository projectRepository) {
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
    }
    
    // =========================================================================
    // BASIC CRUD OPERATIONS - Implementing Service<Milestone, Long> interface
    // =========================================================================
    
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
        if (entity == null) {
            throw new IllegalArgumentException("Milestone cannot be null");
        }
        return milestoneRepository.save(entity);
    }
    
    @Override
    public void delete(Milestone entity) {
        if (entity != null) {
            milestoneRepository.delete(entity);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id != null && milestoneRepository.existsById(id)) {
            milestoneRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Override
    public long count() {
        return milestoneRepository.count();
    }
    
    // =========================================================================
    // BUSINESS LOGIC METHODS - MilestoneService specific methods
    // =========================================================================
    
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
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        return milestoneRepository.findByDateBetween(startDate, endDate);
    }
    
    @Override
    public Milestone createMilestone(String name, LocalDate date, Long projectId, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Milestone name cannot be null or empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Milestone date cannot be null");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
        // Find the project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        
        // Create the milestone
        Milestone milestone = new Milestone();
        milestone.setName(name.trim());
        milestone.setDate(date);
        milestone.setProject(project);
        milestone.setDescription(description != null ? description.trim() : null);
        
        return milestoneRepository.save(milestone);
    }
    
    @Override
    public Milestone updateMilestoneDate(Long milestoneId, LocalDate date) {
        if (milestoneId == null) {
            throw new IllegalArgumentException("Milestone ID cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        Milestone milestone = milestoneRepository.findById(milestoneId).orElse(null);
        if (milestone == null) {
            return null;
        }
        
        milestone.setDate(date);
        return milestoneRepository.save(milestone);
    }
    
    @Override
    public Milestone updateDescription(Long milestoneId, String description) {
        if (milestoneId == null) {
            throw new IllegalArgumentException("Milestone ID cannot be null");
        }
        
        Milestone milestone = milestoneRepository.findById(milestoneId).orElse(null);
        if (milestone == null) {
            return null;
        }
        
        milestone.setDescription(description != null ? description.trim() : null);
        return milestoneRepository.save(milestone);
    }
    
    @Override
    public List<Milestone> getUpcomingMilestones(Long projectId, int days) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (days < 0) {
            throw new IllegalArgumentException("Days cannot be negative");
        }
        
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            throw new IllegalArgumentException("Project not found with ID: " + projectId);
        }
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        return milestoneRepository.findUpcomingMilestones(project, today, endDate);
    }
}