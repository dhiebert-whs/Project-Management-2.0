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
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the MilestoneService interface.
 */
public class MilestoneServiceImpl extends AbstractService<Milestone, Long, MilestoneRepository>
        implements MilestoneService {

    private static final Logger LOGGER = Logger.getLogger(MilestoneServiceImpl.class.getName());
    
    private final ProjectRepository projectRepository;

    /**
     * Constructor for MilestoneServiceImpl.
     */
    public MilestoneServiceImpl() {
        super(RepositoryFactory.getMilestoneRepository());
        this.projectRepository = RepositoryFactory.getProjectRepository();
    }

    @Override
    public List<Milestone> findByProject(Project project) {
        LOGGER.info("Finding milestones for project: " + project.getId());
        return repository.findByProject(project);
    }

    @Override
    public List<Milestone> findByDeadlineBefore(LocalDate date) {
        LOGGER.info("Finding milestones with deadline before: " + date);
        return repository.findByDeadlineBefore(date);
    }

    @Override
    public List<Milestone> findByDeadlineAfter(LocalDate date) {
        LOGGER.info("Finding milestones with deadline after: " + date);
        return repository.findByDeadlineAfter(date);
    }

    @Override
    public Milestone createMilestone(Project project, String name, String description, LocalDate deadline) {
        LOGGER.info("Creating milestone: " + name + " for project: " + project.getId());
        
        Milestone milestone = new Milestone();
        milestone.setProject(project);
        milestone.setName(name);
        milestone.setDescription(description);
        milestone.setDeadline(deadline);
        milestone.setCompleted(false);
        milestone.setDependencies(new ArrayList<>());
        
        return repository.save(milestone);
    }

    @Override
    public Milestone updateCompletionStatus(Milestone milestone, boolean completed) {
        LOGGER.info("Updating completion status for milestone: " + milestone.getId() + " to: " + completed);
        
        milestone.setCompleted(completed);
        
        // If completing, set completion date to now
        if (completed && milestone.getCompletionDate() == null) {
            milestone.setCompletionDate(LocalDate.now());
        }
        
        // If marking as incomplete, clear completion date
        if (!completed) {
            milestone.setCompletionDate(null);
        }
        
        return repository.update(milestone);
    }

    @Override
    public Milestone updateCompletionDate(Milestone milestone, LocalDate completionDate) {
        LOGGER.info("Updating completion date for milestone: " + milestone.getId() + " to: " + completionDate);
        
        milestone.setCompletionDate(completionDate);
        
        // If setting completion date, mark as completed
        if (completionDate != null) {
            milestone.setCompleted(true);
        }
        
        return repository.update(milestone);
    }

    @Override
    public Milestone setDependency(Milestone milestone, Milestone dependency) {
        LOGGER.info("Setting dependency for milestone: " + milestone.getId() + 
                " on milestone: " + dependency.getId());
        
        // Prevent circular dependencies
        if (isDependentOn(dependency, milestone)) {
            LOGGER.warning("Circular dependency detected. Cannot set dependency.");
            throw new IllegalArgumentException("Setting this dependency would create a circular reference");
        }
        
        List<Milestone> dependencies = milestone.getDependencies();
        if (dependencies == null) {
            dependencies = new ArrayList<>();
            milestone.setDependencies(dependencies);
        }
        
        if (!dependencies.contains(dependency)) {
            dependencies.add(dependency);
        }
        
        return repository.update(milestone);
    }

    @Override
    public Milestone removeDependency(Milestone milestone, Milestone dependency) {
        LOGGER.info("Removing dependency for milestone: " + milestone.getId() + 
                " on milestone: " + dependency.getId());
        
        List<Milestone> dependencies = milestone.getDependencies();
        if (dependencies != null) {
            dependencies.remove(dependency);
        }
        
        return repository.update(milestone);
    }

    @Override
    public List<Milestone> getUpcomingMilestones(Project project) {
        LOGGER.info("Getting upcoming milestones for project: " + project.getId());
        
        LocalDate now = LocalDate.now();
        
        return repository.findByProject(project).stream()
                .filter(m -> !m.isCompleted() && m.getDeadline().isAfter(now))
                .collect(Collectors.toList());
    }

    @Override
    public List<Milestone> getCompletedMilestones(Project project) {
        LOGGER.info("Getting completed milestones for project: " + project.getId());
        
        return repository.findByProject(project).stream()
                .filter(Milestone::isCompleted)
                .collect(Collectors.toList());
    }

    @Override
    public List<Milestone> getOverdueMilestones(Project project) {
        LOGGER.info("Getting overdue milestones for project: " + project.getId());
        
        LocalDate now = LocalDate.now();
        
        return repository.findByProject(project).stream()
                .filter(m -> !m.isCompleted() && m.getDeadline().isBefore(now))
                .collect(Collectors.toList());
    }
    
    /**
     * Checks if one milestone is dependent on another, directly or indirectly.
     * Used to prevent circular dependencies.
     *
     * @param milestone The milestone to check
     * @param potentialDependency The potential dependency
     * @return True if milestone depends on potentialDependency, false otherwise
     */
    private boolean isDependentOn(Milestone milestone, Milestone potentialDependency) {
        // Direct dependency check
        if (milestone.getDependencies() != null && 
                milestone.getDependencies().contains(potentialDependency)) {
            return true;
        }
        
        // Recursive check for indirect dependencies
        if (milestone.getDependencies() != null) {
            for (Milestone dependency : milestone.getDependencies()) {
                if (isDependentOn(dependency, potentialDependency)) {
                    return true;
                }
            }
        }
        
        return false;
    }
}