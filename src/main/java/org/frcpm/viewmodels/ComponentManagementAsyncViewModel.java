// src/main/java/org/frcpm/viewmodels/ComponentManagementAsyncViewModel.java

package org.frcpm.viewmodels;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import org.frcpm.binding.Command;
import org.frcpm.models.Component;
import org.frcpm.services.impl.ComponentServiceAsyncImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Asynchronous ViewModel for component list management.
 */
public class ComponentManagementAsyncViewModel extends ComponentManagementViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(ComponentManagementAsyncViewModel.class.getName());
    
    // Services
    private final ComponentServiceAsyncImpl componentServiceAsync;
    
    // UI state
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Async commands
    private Command asyncRefreshCommand;
    private Command asyncDeleteComponentCommand;
    
    /**
     * Creates a new ComponentManagementAsyncViewModel with the specified service.
     * 
     * @param componentServiceAsync the async component service
     */
    public ComponentManagementAsyncViewModel(ComponentServiceAsyncImpl componentServiceAsync) {
        super(null); // Initialize parent with null service
        
        this.componentServiceAsync = componentServiceAsync;
        
        // Initialize async commands
        initAsyncCommands();
    }
    
    /**
     * Initializes async commands.
     */
    private void initAsyncCommands() {
        asyncRefreshCommand = new Command(
            this::loadComponentsAsync,
            () -> !loading.get()
        );
        
        asyncDeleteComponentCommand = new Command(
            this::deleteComponentAsync,
            () -> {
                try {
                    return getSelectedComponent() != null && !loading.get();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error checking selected component", e);
                    return false;
                }
            }
        );
    }

    /**
     * Asynchronously loads all components.
     */
    public void loadComponentsAsync() {
        try {
            loading.set(true);
            
            componentServiceAsync.findAllAsync(
                // Success callback
                components -> {
                    Platform.runLater(() -> {
                        try {
                            // Get the allComponents field via reflection
                            Field allComponentsField = ComponentManagementViewModel.class.getDeclaredField("allComponents");
                            allComponentsField.setAccessible(true);
                            
                            // Set the components in the ViewModel
                            allComponentsField.set(this, FXCollections.observableArrayList(components));
                            
                            // Update the filter
                            updateFilterPredicate();
                            
                            loading.set(false);
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Error updating components in ViewModel", e);
                            setErrorMessage("Error updating components: " + e.getMessage());
                            loading.set(false);
                        }
                    });
                },
                // Error callback
                error -> {
                    Platform.runLater(() -> {
                        loading.set(false);
                        LOGGER.log(Level.SEVERE, "Error loading components", error);
                        setErrorMessage("Failed to load components: " + error.getMessage());
                    });
                }
            );
            
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error loading components", e);
            setErrorMessage("Failed to load components: " + e.getMessage());
        }
    }
    
    /**
     * Updates the filter predicate based on the current filter.
     * Uses reflection to call the parent method.
     */
    private void updateFilterPredicate() {
        try {
            Method updateFilterPredicateMethod = ComponentManagementViewModel.class.getDeclaredMethod("updateFilterPredicate");
            updateFilterPredicateMethod.setAccessible(true);
            updateFilterPredicateMethod.invoke(this);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating filter predicate", e);
        }
    }
    
    /**
     * Asynchronously deletes the selected component.
     */
    public void deleteComponentAsync() {
        Component component = getSelectedComponent();
        if (component == null) {
            setErrorMessage("No component selected");
            return;
        }
        
        try {
            // Check if the component is used by any tasks
            if (component.getRequiredForTasks() != null && !component.getRequiredForTasks().isEmpty()) {
                int taskCount = component.getRequiredForTasks().size();
                setErrorMessage("Cannot delete component because it is required by " + taskCount + 
                        " task(s). Please remove these dependencies first.");
                return;
            }
            
            loading.set(true);
            
            componentServiceAsync.deleteByIdAsync(
                component.getId(),
                // Success callback
                result -> {
                    Platform.runLater(() -> {
                        try {
                            if (result) {
                                // Get the allComponents field via reflection
                                Field allComponentsField = ComponentManagementViewModel.class.getDeclaredField("allComponents");
                                allComponentsField.setAccessible(true);
                                List<Component> allComponents = (List<Component>) allComponentsField.get(this);
                                
                                // Remove the component from the list
                                allComponents.remove(component);
                                
                                // Reset selected component if needed
                                Field selectedComponentField = ComponentManagementViewModel.class.getDeclaredField("selectedComponent");
                                selectedComponentField.setAccessible(true);
                                Component selectedComponent = (Component) selectedComponentField.get(this);
                                
                                if (component.equals(selectedComponent)) {
                                    selectedComponentField.set(this, null);
                                }
                                
                                clearErrorMessage();
                            } else {
                                setErrorMessage("Failed to delete component");
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Error updating components after deletion", e);
                            setErrorMessage("Error updating components: " + e.getMessage());
                        } finally {
                            loading.set(false);
                        }
                    });
                },
                // Error callback
                error -> {
                    Platform.runLater(() -> {
                        loading.set(false);
                        LOGGER.log(Level.SEVERE, "Error deleting component", error);
                        setErrorMessage("Failed to delete component: " + error.getMessage());
                    });
                }
            );
            
        } catch (Exception e) {
            loading.set(false);
            LOGGER.log(Level.SEVERE, "Error deleting component", e);
            setErrorMessage("Failed to delete component: " + e.getMessage());
        }
    }
    
    /**
     * Gets whether the ViewModel is currently loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the async refresh command.
     * 
     * @return the async refresh command
     */
    public Command getAsyncRefreshCommand() {
        return asyncRefreshCommand;
    }
    
    /**
     * Gets the async delete component command.
     * 
     * @return the async delete component command
     */
    public Command getAsyncDeleteComponentCommand() {
        return asyncDeleteComponentCommand;
    }
    
    // Override parent methods to use async implementations
    
    @Override
    public void loadComponents() {
        loadComponentsAsync();
    }
    
    @Override
    public boolean deleteComponent(Component component) {
        if (component == null) {
            setErrorMessage("No component selected");
            return false;
        }
        
        try {
            // Set the selected component first
            setSelectedComponent(component);
            
            // Then delete it
            deleteComponentAsync();
            
            // Assume success for now
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting component", e);
            setErrorMessage("Failed to delete component: " + e.getMessage());
            return false;
        }
    }
}