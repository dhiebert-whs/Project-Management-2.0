package org.frcpm.repositories.spring;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for Milestone entity.
 * Provides Spring Data JPA auto-implemented methods plus custom query methods.
 */
@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    
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
     * Finds milestones by name containing the specified text (case insensitive).
     * 
     * @param name the name to search for
     * @return a list of milestones with matching names
     */
    List<Milestone> findByNameContainingIgnoreCase(String name);
    
    /**
     * Finds upcoming milestones for a project within the specified number of days.
     * 
     * @param project the project
     * @param today today's date
     * @param endDate the end date for the search
     * @return a list of upcoming milestones
     */
    @Query("SELECT m FROM Milestone m WHERE m.project = :project AND m.date >= :today AND m.date <= :endDate ORDER BY m.date")
    List<Milestone> findUpcomingMilestones(@Param("project") Project project, 
                                          @Param("today") LocalDate today, 
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * Finds overdue milestones for a project.
     * 
     * @param project the project
     * @param today today's date
     * @return a list of overdue milestones
     */
    @Query("SELECT m FROM Milestone m WHERE m.project = :project AND m.date < :today ORDER BY m.date")
    List<Milestone> findOverdueMilestones(@Param("project") Project project, @Param("today") LocalDate today);
}