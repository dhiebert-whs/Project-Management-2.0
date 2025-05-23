package org.frcpm.services;

import org.frcpm.models.Component;
import org.frcpm.models.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service interface for Component entity.
 * FIXED: Added async method signatures to support async operations in ViewModels.
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
    
    // ASYNC METHODS - Added to support async operations in ViewModels
    
    /**
     * Finds a component by ID asynchronously.
     * This method is used by ComponentDetailMvvmViewModel.
     * 
     * @param id the component ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure  
     * @return a CompletableFuture that will be completed with the component
     */
    default CompletableFuture<Component> findByIdAsync(Long id, Consumer<Component> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
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
     * This method is used by ComponentDetailMvvmViewModel.
     * 
     * @param entity the component to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved component
     */
    default CompletableFuture<Component> saveAsync(Component entity, Consumer<Component> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
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
     * Finds all components asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of components
     */
    default CompletableFuture<List<Component>> findAllAsync(Consumer<List<Component>> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
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
     * Deletes a component by ID asynchronously.
     * 
     * @param id the ID of the component to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a boolean indicating success
     */
    default CompletableFuture<Boolean> deleteByIdAsync(Long id, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
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
     * Finds components by part number asynchronously.
     * 
     * @param partNumber the part number to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the component or empty
     */
    default CompletableFuture<Optional<Component>> findByPartNumberAsync(String partNumber,
                                                                  Consumer<Optional<Component>> onSuccess,
                                                                  Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
        CompletableFuture<Optional<Component>> future = new CompletableFuture<>();
        try {
            Optional<Component> result = findByPartNumber(partNumber);
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
     * Finds components by name asynchronously.
     * 
     * @param name the name to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of components
     */
    default CompletableFuture<List<Component>> findByNameAsync(String name,
                                                        Consumer<List<Component>> onSuccess,
                                                        Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
        CompletableFuture<List<Component>> future = new CompletableFuture<>();
        try {
            List<Component> result = findByName(name);
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
     * Finds components by delivery status asynchronously.
     * 
     * @param delivered whether to find delivered or undelivered components
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of components
     */
    default CompletableFuture<List<Component>> findByDeliveredAsync(boolean delivered,
                                                             Consumer<List<Component>> onSuccess,
                                                             Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
        CompletableFuture<List<Component>> future = new CompletableFuture<>();
        try {
            List<Component> result = findByDelivered(delivered);
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
    default CompletableFuture<Component> createComponentAsync(String name, String partNumber, String description, LocalDate expectedDelivery, Consumer<Component> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
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
    default CompletableFuture<Component> markAsDeliveredAsync(Long componentId, LocalDate deliveryDate, Consumer<Component> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
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
    
    /**
     * Updates a component's expected delivery date asynchronously.
     * 
     * @param componentId the component ID
     * @param expectedDelivery the new expected delivery date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated component
     */
    default CompletableFuture<Component> updateExpectedDeliveryAsync(Long componentId, LocalDate expectedDelivery,
                                                             Consumer<Component> onSuccess,
                                                             Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
        CompletableFuture<Component> future = new CompletableFuture<>();
        try {
            Component result = updateExpectedDelivery(componentId, expectedDelivery);
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
    default CompletableFuture<Task> associateComponentsWithTaskAsync(Long taskId, Set<Long> componentIds, Consumer<Task> onSuccess, Consumer<Throwable> onFailure) {
        // Default implementation for services that don't support async operations
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
}