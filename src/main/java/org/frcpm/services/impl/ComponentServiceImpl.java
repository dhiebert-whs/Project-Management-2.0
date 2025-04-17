package org.frcpm.services.impl;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.ComponentRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.ComponentService;

import jakarta.persistence.EntityManager;

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
        
        // Use entity manager for proper transaction handling
        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();
            em.getTransaction().begin();
            
            // Get a managed instance of the task
            Task task = em.find(Task.class, taskId);
            if (task == null) {
                LOGGER.log(Level.WARNING, "Task not found with ID: {0}", taskId);
                em.getTransaction().rollback();
                return null;
            }
            
            // Clear existing components but maintain managed references
            // Get a copy first to avoid concurrent modification
            Set<Component> currentComponents = new HashSet<>(task.getRequiredComponents());
            for (Component component : currentComponents) {
                task.getRequiredComponents().remove(component);
                component.getRequiredForTasks().remove(task);
            }
            
            // Add new components
            if (componentIds != null && !componentIds.isEmpty()) {
                for (Long componentId : componentIds) {
                    Component component = em.find(Component.class, componentId);
                    if (component != null) {
                        // Use the entity helper method to maintain both sides of the relationship
                        task.addRequiredComponent(component);
                    } else {
                        LOGGER.log(Level.WARNING, "Component not found with ID: {0}", componentId);
                    }
                }
            }
            
            // Flush changes to detect any errors before committing
            em.flush();
            em.getTransaction().commit();
            
            return task;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error associating components with task", e);
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to associate components with task: " + e.getMessage(), e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
