package org.frcpm.viewmodels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.frcpm.binding.Command;
import org.frcpm.di.ServiceProvider;
import org.frcpm.models.Component;
import org.frcpm.services.ComponentService;


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * ViewModel for the Component Management view.
 * Handles the business logic for listing, filtering, and managing components.
 */
public class ComponentManagementViewModel extends BaseViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(ComponentManagementViewModel.class.getName());
    
    /**
     * Enum for filtering components.
     */
    public enum ComponentFilter {
        ALL("All Components"),
        DELIVERED("Delivered"),
        PENDING("Pending Delivery");
        
        private final String displayName;
        
        ComponentFilter(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Services
    private final ComponentService componentService;
    
    // Data
    private final ObservableList<Component> allComponents = FXCollections.observableArrayList();
    private final FilteredList<Component> filteredComponents = new FilteredList<>(allComponents);
    
    // Selected component
    private Component selectedComponent;
    
    // Current filter
    private ComponentFilter currentFilter = ComponentFilter.ALL;
    
    // Commands
    private final Command addComponentCommand;
    private final Command editComponentCommand;
    private final Command deleteComponentCommand;
    private final Command refreshCommand;
    
    /**
     * Creates a new ComponentManagementViewModel with default services.
     */
    public ComponentManagementViewModel() {
        this(ServiceProvider.getService(ComponentService.class));
    }
    
    /**
     * Creates a new ComponentManagementViewModel with specified services.
     * This constructor is primarily used for testing to inject mock services.
     * 
     * @param componentService the component service
     */
    public ComponentManagementViewModel(ComponentService componentService) {
        this.componentService = componentService;
        
        // Initialize commands using BaseViewModel utility methods
        addComponentCommand = new Command(this::addComponent);
        editComponentCommand = createValidOnlyCommand(this::editComponent, this::canEditOrDeleteComponent);
        deleteComponentCommand = createValidOnlyCommand(this::deleteComponent, this::canEditOrDeleteComponent);
        refreshCommand = new Command(this::loadComponents);
        
        // Set up filtered list predicate
        updateFilterPredicate();
    }
    
    /**
     * Loads all components from the service.
     */
    public void loadComponents() {
        try {
            List<Component> components = componentService.findAll();
            allComponents.setAll(components);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading components", e);
            setErrorMessage("Failed to load components: " + e.getMessage());
        }
    }
    
    /**
     * Sets the filter and updates the filtered list.
     * 
     * @param filter the filter to apply
     */
    public void setFilter(ComponentFilter filter) {
        if (filter == null) {
            LOGGER.warning("Attempting to set null filter, using ALL instead");
            filter = ComponentFilter.ALL;
        }
        
        this.currentFilter = filter;
        updateFilterPredicate();
    }
    
    /**
     * Updates the filter predicate based on the current filter.
     */
    private void updateFilterPredicate() {
        filteredComponents.setPredicate(component -> {
            if (component == null) {
                return false;
            }
            
            switch (currentFilter) {
                case ALL:
                    return true;
                case DELIVERED:
                    return component.isDelivered();
                case PENDING:
                    return !component.isDelivered();
                default:
                    return true;
            }
        });
    }
    
    /**
     * Command action to add a new component.
     * This will be handled by the controller to show the dialog.
     */
    private void addComponent() {
        // This is intentionally empty because the controller will handle dialog creation
        LOGGER.info("Add component action triggered");
    }
    
    /**
     * Command action to edit the selected component.
     * This will be handled by the controller to show the dialog.
     */
    private void editComponent() {
        // This is intentionally empty because the controller will handle dialog creation
        LOGGER.info("Edit component action triggered");
    }
    
    /**
     * Command action to delete the selected component.
     * Checks if the component is used by any tasks before deletion.
     * 
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteComponent(Component component) {
        if (component == null) {
            setErrorMessage("No component selected");
            return false;
        }
        
        try {
            // Check if the component is used by any tasks
            if (component.getRequiredForTasks() != null && !component.getRequiredForTasks().isEmpty()) {
                int taskCount = component.getRequiredForTasks().size();
                setErrorMessage("Cannot delete component because it is required by " + taskCount + 
                        " task(s). Please remove these dependencies first.");
                return false;
            }
            
            // Delete the component
            componentService.deleteById(component.getId());
            
            // Remove it from the list
            allComponents.remove(component);
            if (component.equals(selectedComponent)) {
                selectedComponent = null;
            }
            
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting component", e);
            setErrorMessage("Failed to delete component: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Command action to delete the selected component.
     * Implementation for the command binding.
     */
    private void deleteComponent() {
        deleteComponent(selectedComponent);
    }
    
    /**
     * Command condition to check if a component is selected for edit/delete.
     * 
     * @return true if a component is selected, false otherwise
     */
    public boolean canEditOrDeleteComponent() {
        return selectedComponent != null;
    }
    
    /**
     * Gets the filtered components list.
     * 
     * @return the filtered components list
     */
    public ObservableList<Component> getComponents() {
        return filteredComponents;
    }
    
    /**
     * Sets the selected component.
     * 
     * @param component the selected component
     */
    public void setSelectedComponent(Component component) {
        this.selectedComponent = component;
    }
    
    /**
     * Gets the selected component.
     * 
     * @return the selected component
     */
    public Component getSelectedComponent() {
        return selectedComponent;
    }
    
    /**
     * Gets the add component command.
     * 
     * @return the add component command
     */
    public Command getAddComponentCommand() {
        return addComponentCommand;
    }
    
    /**
     * Gets the edit component command.
     * 
     * @return the edit component command
     */
    public Command getEditComponentCommand() {
        return editComponentCommand;
    }
    
    /**
     * Gets the delete component command.
     * 
     * @return the delete component command
     */
    public Command getDeleteComponentCommand() {
        return deleteComponentCommand;
    }
    
    /**
     * Gets the refresh command.
     * 
     * @return the refresh command
     */
    public Command getRefreshCommand() {
        return refreshCommand;
    }
    
    /**
     * Gets the current filter.
     * 
     * @return the current filter
     */
    public ComponentFilter getCurrentFilter() {
        return currentFilter;
    }
    
    /**
     * Clears the error message.
     * This overrides the protected method in BaseViewModel to make it public.
     */
    @Override
    public void clearErrorMessage() {
        super.clearErrorMessage();
    }
    
    /**
     * Cleans up resources when the ViewModel is no longer needed.
     */
    @Override
    public void cleanupResources() {
        super.cleanupResources();
        allComponents.clear();
        selectedComponent = null;
    }
}