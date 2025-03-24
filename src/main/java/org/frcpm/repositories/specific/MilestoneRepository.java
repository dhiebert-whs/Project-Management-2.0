package org.frcpm.repositories.specific;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Milestone entity.
 */
public interface MilestoneRepository extends Repository<Milestone, Long> {
    
    /**
     * Finds milestones for a specific project.
     * 
     * @param project the project to find milestones for
     * @return a list of milestones for the project
     */
    List<Milestone> findByProject(Project project);
    
    /**
     * Finds milestones with a date before the specified date.
     * 
     * @param date the date to compare against
     * @return a list of milestones before the date
     */
    List<Milestone> findByDateBefore(LocalDate date);
    
    /**
     * Finds milestones with a date after the specified date.
     * 
     * @param date the date to compare against
     * @return a list of milestones after the date
     */
    List<Milestone> findByDateAfter(LocalDate date);
    
    /**
     * Finds milestones in a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return a list of milestones within the date range
     */
    List<Milestone> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Finds milestones by name.
     * 
     * @param name the name to search for
     * @return a list of milestones with matching names
     */
    List<Milestone> findByName(String name);
}