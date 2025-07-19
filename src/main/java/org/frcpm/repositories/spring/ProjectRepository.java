// src/main/java/org/frcpm/repositories/spring/ProjectRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for Project entities.
 * This interface extends JpaRepository to provide automatic CRUD operations
 * and includes custom query methods for project-specific operations.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * Finds projects by name using case-insensitive partial matching.
     * 
     * @param name the project name to search for
     * @return a list of matching projects
     */
    List<Project> findByNameContainingIgnoreCase(String name);
    
    /**
     * Finds projects with hard deadlines before the specified date.
     * 
     * @param date the date to compare against
     * @return a list of projects with deadlines before the date
     */
    List<Project> findByHardDeadlineBefore(LocalDate date);
    
    /**
     * Finds projects starting after the specified date.
     * 
     * @param date the date to compare against
     * @return a list of projects starting after the date
     */
    List<Project> findByStartDateAfter(LocalDate date);
    
    /**
     * Finds projects by exact name match.
     * This method provides compatibility with the existing findByName method.
     * 
     * @param name the exact project name
     * @return a list of projects with the exact name
     */
    List<Project> findByName(String name);
    
    /**
     * Finds projects with deadlines before the specified date.
     * This method provides compatibility with the existing findByDeadlineBefore method.
     * 
     * @param date the date to compare against
     * @return a list of projects with deadlines before the date
     */
    @Query("SELECT p FROM Project p WHERE p.hardDeadline < :date")
    List<Project> findByDeadlineBefore(@Param("date") LocalDate date);
    
    /**
     * Finds projects that are currently active (between start date and goal end date).
     * 
     * @return a list of active projects
     */
    // @Query("SELECT p FROM Project p WHERE p.startDate <= CURRENT_DATE AND p.goalEndDate >= CURRENT_DATE")
    // List<Project> findActiveProjects();
    
    /**
     * Finds projects that are overdue (past their goal end date but not completed).
     * 
     * @return a list of overdue projects
     */
    // @Query("SELECT p FROM Project p WHERE p.goalEndDate < CURRENT_DATE")
    // List<Project> findOverdueProjects();
    
    /**
     * Finds projects with goal end dates within the specified number of days.
     * 
     * @param days the number of days to look ahead
     * @return a list of projects due soon
     */
    // @Query("SELECT p FROM Project p WHERE p.goalEndDate BETWEEN CURRENT_DATE AND :endDate")
    // List<Project> findProjectsDueSoon(@Param("endDate") LocalDate endDate);
    
    /**
     * Counts the number of active projects.
     * 
     * @return the count of active projects
     */
    // @Query("SELECT COUNT(p) FROM Project p WHERE p.startDate <= CURRENT_DATE AND p.goalEndDate >= CURRENT_DATE")
    // long countActiveProjects();
}