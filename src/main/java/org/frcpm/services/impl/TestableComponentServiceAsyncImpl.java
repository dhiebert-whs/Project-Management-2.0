// src/main/java/org/frcpm/services/impl/TestableComponentServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.repositories.specific.ComponentRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.ComponentService;
import org.frcpm.di.ServiceLocator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable async implementation of ComponentService that accepts injected repositories
 * for better unit testing. This implementation provides async methods that can be stubbed
 * in tests without requiring spy patterns.
 */
public class TestableComponentServiceAsyncImpl extends TestableComponentServiceImpl {
    
    private static final Logger LOGGER = Logger.getLogger(TestableComponentServiceAsyncImpl.class.getName());
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableComponentServiceAsyncImpl() {
        super();
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param componentRepository the component repository
     * @param taskRepository the task repository
     */
    public TestableComponentServiceAsyncImpl(
            ComponentRepository componentRepository,
            TaskRepository taskRepository) {
        super(componentRepository, taskRepository);
    }
    
    // ASYNC METHODS - These can be stubbed directly in tests without spies
    
    /**
     * Finds all components asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of components
     */
    public CompletableFuture<List<Component>> findAllAsync(Consumer<List<Component>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Component>> future = new CompletableFuture<>();
        
        try {
            List<Component> result = findAll();
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Finds a component by ID asynchronously.
     * 
     * @param id the component ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the component
     */
    public CompletableFuture<Component> findByIdAsync(Long id, Consumer<Component> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Component> future = new CompletableFuture<>();
        
        try {
            Component result = findById(id);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Saves a component asynchronously.
     * 
     * @param entity the component to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved component
     */
    public CompletableFuture<Component> saveAsync(Component entity, Consumer<Component> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Component> future = new CompletableFuture<>();
        
        try {
            Component result = save(entity);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Deletes a component by ID asynchronously.
     * 
     * @param id the ID of the component to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a boolean indicating success
     */
    public CompletableFuture<Boolean> deleteByIdAsync(Long id, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            boolean result = deleteById(id);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Associates components with a task asynchronously.
     * 
     * @param taskId the task ID
     * @param componentIds the component IDs to associate
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated task
     */
    public CompletableFuture<Task> associateComponentsWithTaskAsync(Long taskId, Set<Long> componentIds, Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Task> future = new CompletableFuture<>();
        
        try {
            Task result = associateComponentsWithTask(taskId, componentIds);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Creates a component asynchronously.
     * 
     * @param name the component name
     * @param partNumber the part number
     * @param description the description
     * @param expectedDelivery the expected delivery date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created component
     */
    public CompletableFuture<Component> createComponentAsync(String name, String partNumber, String description, LocalDate expectedDelivery, Consumer<Component> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Component> future = new CompletableFuture<>();
        
        try {
            Component result = createComponent(name, partNumber, description, expectedDelivery);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Marks a component as delivered asynchronously.
     * 
     * @param componentId the component ID
     * @param deliveryDate the delivery date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated component
     */
    public CompletableFuture<Component> markAsDeliveredAsync(Long componentId, LocalDate deliveryDate, Consumer<Component> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Component> future = new CompletableFuture<>();
        
        try {
            Component result = markAsDelivered(componentId, deliveryDate);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
}