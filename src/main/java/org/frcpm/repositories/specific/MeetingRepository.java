package org.frcpm.repositories.specific;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.repositories.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Meeting entity.
 */
public interface MeetingRepository extends Repository<Meeting, Long> {
    
    /**
     * Finds meetings for a specific project.
     * 
     * @param project the project to find meetings for
     * @return a list of meetings for the project
     */
    List<Meeting> findByProject(Project project);
    
    /**
     * Finds meetings on a specific date.
     * 
     * @param date the date to search for
     * @return a list of meetings on the given date
     */
    List<Meeting> findByDate(LocalDate date);
    
    /**
     * Finds meetings after a specific date.
     * 
     * @param date the date to compare against
     * @return a list of meetings after the date
     */
    List<Meeting> findByDateAfter(LocalDate date);
    
    /**
     * Finds meetings in a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return a list of meetings within the date range
     */
    List<Meeting> findByDateBetween(LocalDate startDate, LocalDate endDate);
}