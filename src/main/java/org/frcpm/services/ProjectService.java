package org.frcpm.services;

import org.frcpm.models.Project;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Project entity.
 */
public interface ProjectService extends Service<Project, Long> {
    
    /**
     * Finds projects by name.
     * 
     * @param name the project name to search for
     * @return a list of matching projects
     */
    List<Project> findByName(String name);
    
    /**
     * Finds projects with deadlines before the specified date.
     * 
     * @param date the date to compare against
     * @return a list of projects with deadlines before the date
     */
    List<Project> findByDeadlineBefore(LocalDate date);
    
    /**
     * Finds projects starting after the specified date.
     * 
     * @param date the date to compare against
     * @return a list of projects starting after the date
     */
    List<Project> findByStartDateAfter(LocalDate date);
    
    /**
     * Creates a new project with the specified details.
     * 
     * @param name the project name
     * @param startDate the start date
     * @param goalEndDate the goal end date
     * @param hardDeadline the hard deadline
     * @return the created project
     */
    Project createProject(String name, LocalDate startDate, LocalDate goalEndDate, LocalDate hardDeadline);
    
    /**
     * Updates an existing project with the specified details.
     * 
     * @param id the project ID
     * @param name the project name
     * @param startDate the start date
     * @param goalEndDate the goal end date
     * @param hardDeadline the hard deadline
     * @param description the project description
     * @return the updated project, or null if not found
     */
    Project updateProject(Long id, String name, LocalDate startDate, LocalDate goalEndDate, 
                          LocalDate hardDeadline, String description);
    
    /**
     * Gets a summary of the project including task counts, completion percentage, etc.
     * 
     * @param projectId the project ID
     * @return a map containing the summary data
     */
    java.util.Map<String, Object> getProjectSummary(Long projectId);
}