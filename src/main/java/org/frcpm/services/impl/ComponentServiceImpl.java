// src/main/java/org/frcpm/services/impl/ComponentServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.repositories.spring.ComponentRepository;
import org.frcpm.repositories.spring.TaskRepository;
import org.frcpm.services.ComponentService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Spring Boot implementation of ComponentService using composition-based pattern.
 * NO INHERITANCE - Direct implementation of all interface methods.
 */
@Service
@Transactional
public class ComponentServiceImpl implements ComponentService {
    
    private final ComponentRepository componentRepository;
    private final TaskRepository taskRepository;
    
    /**
     * Constructor injection - no inheritance, pure composition
     */
    public ComponentServiceImpl(ComponentRepository componentRepository,
                               TaskRepository taskRepository) {
        this.componentRepository = componentRepository;
        this.taskRepository = taskRepository;
    }
    
    // ========================================
    // Basic CRUD Operations (from Service<T, ID> interface)
    // ========================================
    
    @Override
    public Component findById(Long id) {
        return componentRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Component> findAll() {
        return componentRepository.findAll();
    }
    
    @Override
    public Component save(Component entity) {
        return componentRepository.save(entity);
    }
    
    @Override
    public void delete(Component entity) {
        componentRepository.delete(entity);
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (componentRepository.existsById(id)) {
            componentRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Override
    public long count() {
        return componentRepository.count();
    }
    
    // ========================================
    // Business Operations (ComponentService specific)
    // ========================================
    
    @Override
    public Optional<Component> findByPartNumber(String partNumber) {
        if (partNumber == null || partNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        return componentRepository.findByPartNumber(partNumber);
    }
    
    @Override
    public List<Component> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        return componentRepository.findByNameContainingIgnoreCase(name);
    }
    
    @Override
    public List<Component> findByDelivered(boolean delivered) {
        return componentRepository.findByDelivered(delivered);
    }
    
    @Override
    public Component createComponent(String name, String partNumber, 
                                   String description, LocalDate expectedDelivery) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Component name cannot be empty");
        }
        
        // Check if part number already exists
        if (partNumber != null && !partNumber.trim().isEmpty()) {
            Optional<Component> existing = componentRepository.findByPartNumber(partNumber);
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Component with part number '" + partNumber + "' already exists");
            }
        }
        
        // Create new component using proper constructor
        Component component;
        if (partNumber != null && !partNumber.trim().isEmpty()) {
            component = new Component(name, partNumber);
        } else {
            component = new Component(name);
        }
        
        component.setDescription(description);
        component.setExpectedDelivery(expectedDelivery);
        
        return componentRepository.save(component);
    }
    
    @Override
    public Component markAsDelivered(Long componentId, LocalDate deliveryDate) {
        if (componentId == null) {
            throw new IllegalArgumentException("Component ID cannot be null");
        }
        
        Component component = componentRepository.findById(componentId).orElse(null);
        if (component == null) {
            return null;
        }
        
        component.setDelivered(true);
        component.setActualDelivery(deliveryDate != null ? deliveryDate : LocalDate.now());
        
        return componentRepository.save(component);
    }
    
    @Override
    public Component updateExpectedDelivery(Long componentId, LocalDate expectedDelivery) {
        if (componentId == null) {
            throw new IllegalArgumentException("Component ID cannot be null");
        }
        
        Component component = componentRepository.findById(componentId).orElse(null);
        if (component == null) {
            return null;
        }
        
        component.setExpectedDelivery(expectedDelivery);
        
        return componentRepository.save(component);
    }
    
    @Override
    public Task associateComponentsWithTask(Long taskId, Set<Long> componentIds) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return null;
        }
        
        // Clear existing components
        task.getRequiredComponents().clear();
        
        // Add new components
        if (componentIds != null && !componentIds.isEmpty()) {
            for (Long componentId : componentIds) {
                Component component = componentRepository.findById(componentId).orElse(null);
                if (component != null) {
                    task.addRequiredComponent(component);
                }
            }
        }
        
        return taskRepository.save(task);
    }
    
    // ========================================
    // Async Operations (Spring Boot style)
    // ========================================
    
    @Async
    public CompletableFuture<List<Component>> findAllAsync() {
        return CompletableFuture.completedFuture(findAll());
    }
    
    @Async
    public CompletableFuture<Component> findByIdAsync(Long id) {
        return CompletableFuture.completedFuture(findById(id));
    }
    
    @Async
    public CompletableFuture<Component> saveAsync(Component entity) {
        return CompletableFuture.completedFuture(save(entity));
    }
    
    @Async
    public CompletableFuture<Boolean> deleteByIdAsync(Long id) {
        return CompletableFuture.completedFuture(deleteById(id));
    }
    
    @Async
    public CompletableFuture<Optional<Component>> findByPartNumberAsync(String partNumber) {
        return CompletableFuture.completedFuture(findByPartNumber(partNumber));
    }
    
    @Async
    public CompletableFuture<List<Component>> findByNameAsync(String name) {
        return CompletableFuture.completedFuture(findByName(name));
    }
    
    @Async
    public CompletableFuture<List<Component>> findByDeliveredAsync(boolean delivered) {
        return CompletableFuture.completedFuture(findByDelivered(delivered));
    }
    
    @Async
    public CompletableFuture<Component> createComponentAsync(String name, String partNumber, 
                                                           String description, LocalDate expectedDelivery) {
        return CompletableFuture.completedFuture(createComponent(name, partNumber, description, expectedDelivery));
    }
    
    @Async
    public CompletableFuture<Component> markAsDeliveredAsync(Long componentId, LocalDate deliveryDate) {
        return CompletableFuture.completedFuture(markAsDelivered(componentId, deliveryDate));
    }
    
    @Async
    public CompletableFuture<Component> updateExpectedDeliveryAsync(Long componentId, LocalDate expectedDelivery) {
        return CompletableFuture.completedFuture(updateExpectedDelivery(componentId, expectedDelivery));
    }
    
    @Async
    public CompletableFuture<Task> associateComponentsWithTaskAsync(Long taskId, Set<Long> componentIds) {
        return CompletableFuture.completedFuture(associateComponentsWithTask(taskId, componentIds));
    }
    
    // ========================================
    // Interface Async Methods with Callbacks (ComponentService interface compatibility)
    // ========================================
    
    @Override
    public CompletableFuture<Component> findByIdAsync(Long id, Consumer<Component> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Component> future = findByIdAsync(id);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<Component> saveAsync(Component entity, Consumer<Component> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Component> future = saveAsync(entity);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<List<Component>> findAllAsync(Consumer<List<Component>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Component>> future = findAllAsync();
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(Long id, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Boolean> future = deleteByIdAsync(id);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<Optional<Component>> findByPartNumberAsync(String partNumber,
                                                                      Consumer<Optional<Component>> onSuccess,
                                                                      Consumer<Throwable> onFailure) {
        CompletableFuture<Optional<Component>> future = findByPartNumberAsync(partNumber);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<List<Component>> findByNameAsync(String name,
                                                            Consumer<List<Component>> onSuccess,
                                                            Consumer<Throwable> onFailure) {
        CompletableFuture<List<Component>> future = findByNameAsync(name);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<List<Component>> findByDeliveredAsync(boolean delivered,
                                                                 Consumer<List<Component>> onSuccess,
                                                                 Consumer<Throwable> onFailure) {
        CompletableFuture<List<Component>> future = findByDeliveredAsync(delivered);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<Component> createComponentAsync(String name, String partNumber, String description, 
                                                           LocalDate expectedDelivery, Consumer<Component> onSuccess, 
                                                           Consumer<Throwable> onFailure) {
        CompletableFuture<Component> future = createComponentAsync(name, partNumber, description, expectedDelivery);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<Component> markAsDeliveredAsync(Long componentId, LocalDate deliveryDate, 
                                                           Consumer<Component> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Component> future = markAsDeliveredAsync(componentId, deliveryDate);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<Component> updateExpectedDeliveryAsync(Long componentId, LocalDate expectedDelivery,
                                                                  Consumer<Component> onSuccess,
                                                                  Consumer<Throwable> onFailure) {
        CompletableFuture<Component> future = updateExpectedDeliveryAsync(componentId, expectedDelivery);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<Task> associateComponentsWithTaskAsync(Long taskId, Set<Long> componentIds, 
                                                                  Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Task> future = associateComponentsWithTaskAsync(taskId, componentIds);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
}