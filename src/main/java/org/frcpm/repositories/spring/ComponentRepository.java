// src/main/java/org/frcpm/repositories/spring/ComponentRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.Component;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Component entity.
 * Provides both auto-implemented Spring Data JPA methods and custom query methods.
 */
@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {
    
    /**
     * Finds a component by part number.
     * 
     * @param partNumber the part number to search for
     * @return an Optional containing the found component, or empty if not found
     */
    Optional<Component> findByPartNumber(String partNumber);
    
    /**
     * Finds a component by part number (case-insensitive).
     * 
     * @param partNumber the part number to search for
     * @return an Optional containing the found component, or empty if not found
     */
    Optional<Component> findByPartNumberIgnoreCase(String partNumber);
    
    /**
     * Finds components by name.
     * 
     * @param name the name to search for
     * @return a list of components with matching names
     */
    @Query("SELECT c FROM Component c WHERE c.name LIKE %:name%")
    List<Component> findByName(@Param("name") String name);
    
    /**
     * Finds components by name containing the search term (case-insensitive).
     * 
     * @param name the name to search for
     * @return a list of components with matching names
     */
    List<Component> findByNameContainingIgnoreCase(String name);
    
    /**
     * Finds components by delivery status.
     * 
     * @param delivered whether to find delivered or undelivered components
     * @return a list of components with the given delivery status
     */
    List<Component> findByDelivered(boolean delivered);
    
    /**
     * Finds components expected to be delivered after a certain date.
     * 
     * @param date the date to compare against
     * @return a list of components with expected delivery after the date
     */
    List<Component> findByExpectedDeliveryAfter(LocalDate date);
    
    /**
     * Finds components expected to be delivered before a certain date.
     * 
     * @param date the date to compare against
     * @return a list of components with expected delivery before the date
     */
    List<Component> findByExpectedDeliveryBefore(LocalDate date);
    
    /**
     * Finds components expected to be delivered between two dates.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return a list of components with expected delivery in the date range
     */
    List<Component> findByExpectedDeliveryBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Finds components that have been delivered after a certain date.
     * 
     * @param date the date to compare against
     * @return a list of components delivered after the date
     */
    List<Component> findByDeliveredTrueAndActualDeliveryAfter(LocalDate date);
    
    /**
     * Finds components that are overdue (not delivered and expected delivery date has passed).
     * 
     * @param currentDate the current date to compare against
     * @return a list of overdue components
     */
    @Query("SELECT c FROM Component c WHERE c.delivered = false AND c.expectedDelivery < :currentDate")
    List<Component> findOverdueComponents(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Finds components required for a specific task.
     * 
     * @param taskId the task ID
     * @return a list of components required for the task
     */
    @Query("SELECT c FROM Component c JOIN c.requiredForTasks t WHERE t.id = :taskId")
    List<Component> findByRequiredForTasksId(@Param("taskId") Long taskId);
    
    /**
     * Counts components by delivery status.
     * 
     * @param delivered the delivery status
     * @return the count of components with the given delivery status
     */
    long countByDelivered(boolean delivered);
    
    /**
     * Checks if a part number already exists (case-insensitive).
     * 
     * @param partNumber the part number to check
     * @return true if the part number exists, false otherwise
     */
    boolean existsByPartNumberIgnoreCase(String partNumber);
}