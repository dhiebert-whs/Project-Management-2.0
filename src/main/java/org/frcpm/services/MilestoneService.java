package org.frcpm.services;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Milestone entity.
 */
public interface MilestoneService extends Service<Milestone, Long> {
    
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
     * Creates a new milestone.
     * 
     * @param name the milestone name
     * @param date the milestone date
     * @param projectId the ID of the project the milestone is for
     * @param description the milestone description (optional)
     * @return the created milestone
     */
    Milestone createMilestone(String name, LocalDate date, Long projectId, String description);
    
    /**
     * Updates a milestone's date.
     * 
     * @param milestoneId the milestone ID
     * @param date the new date
     * @return the updated milestone, or null if not found
     */
    Milestone updateMilestoneDate(Long milestoneId, LocalDate date);
    
    /**
     * Updates a milestone's description.
     * 
     * @param milestoneId the milestone ID
     * @param description the new description
     * @return the updated milestone, or null if not found
     */
    Milestone updateDescription(Long milestoneId, String description);
    
    /**
     * Gets upcoming milestones for a project.
     * 
     * @param projectId the project ID
     * @param days the number of days to look ahead
     * @return a list of upcoming milestones within the specified days
     */
    List<Milestone> getUpcomingMilestones(Long projectId, int days);
}