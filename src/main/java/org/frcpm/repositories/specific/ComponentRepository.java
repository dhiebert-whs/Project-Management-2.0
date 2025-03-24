package org.frcpm.repositories.specific;

import org.frcpm.models.Component;
import org.frcpm.repositories.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Component entity.
 */
public interface ComponentRepository extends Repository<Component, Long> {
    
    /**
     * Finds a component by part number.
     * 
     * @param partNumber the part number to search for
     * @return an Optional containing the found component, or empty if not found
     */
    Optional<Component> findByPartNumber(String partNumber);
    
    /**
     * Finds components by name.
     * 
     * @param name the name to search for
     * @return a list of components with matching names
     */
    List<Component> findByName(String name);
    
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
}