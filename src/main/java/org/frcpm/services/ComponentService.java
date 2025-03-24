package org.frcpm.services;

import org.frcpm.models.Component;
import org.frcpm.models.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for Component entity.
 */
public interface ComponentService extends Service<Component, Long> {
    
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
     * Creates a new component.
     * 
     * @param name the component name
     * @param partNumber the part number (optional)
     * @param description the component description (optional)
     * @param expectedDelivery the expected delivery date (optional)
     * @return the created component
     */
    Component createComponent(String name, String partNumber, 
                             String description, LocalDate expectedDelivery);
    
    /**
     * Marks a component as delivered.
     * 
     * @param componentId the component ID
     * @param deliveryDate the actual delivery date
     * @return the updated component, or null if not found
     */
    Component markAsDelivered(Long componentId, LocalDate deliveryDate);
    
    /**
     * Updates a component's expected delivery date.
     * 
     * @param componentId the component ID
     * @param expectedDelivery the new expected delivery date
     * @return the updated component, or null if not found
     */
    Component updateExpectedDelivery(Long componentId, LocalDate expectedDelivery);
    
    /**
     * Associates components with a task.
     * 
     * @param taskId the task ID
     * @param componentIds the component IDs to associate
     * @return the updated task, or null if not found
     */
    Task associateComponentsWithTask(Long taskId, Set<Long> componentIds);
}