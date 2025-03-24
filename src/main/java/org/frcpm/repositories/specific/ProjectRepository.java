package org.frcpm.repositories.specific;

import org.frcpm.models.Project;
import org.frcpm.repositories.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Project entity.
 */
public interface ProjectRepository extends Repository<Project, Long> {
    
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
}