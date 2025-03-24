package org.frcpm.services.impl;

import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.ComponentRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.ComponentService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ComponentService using repository layer.
 */
public class ComponentServiceImpl extends AbstractService<Component, Long, ComponentRepository> 
        implements ComponentService {
    
    private static final Logger LOGGER = Logger.getLogger(ComponentServiceImpl.class.getName());
    private final TaskRepository taskRepository;
    
    public ComponentServiceImpl() {
        super(RepositoryFactory.getComponentRepository());
        this.taskRepository = RepositoryFactory.getTaskRepository();
    }
    
    @Override
    public Optional<Component> findByPartNumber(String partNumber) {
        return repository.findByPartNumber(partNumber);
    }
    
    @Override
    public List<Component> findByName(String name) {
        return repository.findByName(name);
    }
    
    @Override
    public List<Component> findByDelivered(boolean delivered) {
        return repository.findByDelivered(delivered);
    }
    
    @Override
    public Component createComponent(String name, String partNumber, 
                                    String description, LocalDate expectedDelivery) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Component name cannot be empty");
        }
        
        if (partNumber != null && !partNumber.trim().isEmpty()) {
            if (repository.findByPartNumber(partNumber).isPresent()) {
                throw new IllegalArgumentException("Component with part number '" + partNumber + "' already exists");
            }
        }
        
        Component component;
        if (partNumber != null && !partNumber.trim().isEmpty()) {
            component = new Component(name, partNumber);
        } else {
            component = new Component(name);
        }
        
        component.setDescription(description);
        component.setExpectedDelivery(expectedDelivery);
        
        return save(component);
    }
    
    @Override
    public Component markAsDelivered(Long componentId, LocalDate deliveryDate) {
        if (componentId == null) {
            throw new IllegalArgumentException("Component ID cannot be null");
        }
        
        Component component = findById(componentId);
        if (component == null) {
            LOGGER.log(Level.WARNING, "Component not found with ID: {0}", componentId);
            return null;
        }
        
        component.setDelivered(true);
        
        if (deliveryDate != null) {
            component.setActualDelivery(deliveryDate);
        } else {
            component.setActualDelivery(LocalDate.now());
        }
        
        return save(component);
    }
    
    @Override
    public Component updateExpectedDelivery(Long componentId, LocalDate expectedDelivery) {
        if (componentId == null) {
            throw new IllegalArgumentException("Component ID cannot be null");
        }
        
        Component component = findById(componentId);
        if (component == null) {
            LOGGER.log(Level.WARNING, "Component not found with ID: {0}", componentId);
            return null;
        }
        
        component.setExpectedDelivery(expectedDelivery);
        return save(component);
    }
    
    @Override
    public Task associateComponentsWithTask(Long taskId, Set<Long> componentIds) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            LOGGER.log(Level.WARNING, "Task not found with ID: {0}", taskId);
            return null;
        }
        
        // Clear existing components
        task.getRequiredComponents().clear();
        
        if (componentIds != null && !componentIds.isEmpty()) {
            Set<Component> components = new HashSet<>();
            
            for (Long componentId : componentIds) {
                Component component = findById(componentId);
                if (component != null) {
                    components.add(component);
                    task.addRequiredComponent(component);
                } else {
                    LOGGER.log(Level.WARNING, "Component not found with ID: {0}", componentId);
                }
            }
        }
        
        return taskRepository.save(task);
    }
}
