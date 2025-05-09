// src/main/java/org/frcpm/mvvm/viewmodels/ComponentListMvvmViewModel.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.utils.commands.Command;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.mvvm.async.MvvmAsyncHelper;
import org.frcpm.services.ComponentService;
import org.frcpm.services.impl.ComponentServiceAsyncImpl;

/**
 * ViewModel for the ComponentList view using MVVMFx.
 */
public class ComponentListMvvmViewModel extends BaseMvvmViewModel {
    
    private static final Logger LOGGER = Logger.getLogger(ComponentListMvvmViewModel.class.getName());
    
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
    
    // Service dependencies
    private final ComponentService componentService;
    private final ComponentServiceAsyncImpl componentServiceAsync;
    
    // Observable collections and properties
    private final ObservableList<Component> allComponents = FXCollections.observableArrayList();
    private final FilteredList<Component> filteredComponents = new FilteredList<>(allComponents);
    private final ObjectProperty<Component> selectedComponent = new SimpleObjectProperty<>();
    private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    
    // Current filter
    private ComponentFilter currentFilter = ComponentFilter.ALL;
    
    // Commands
    private Command loadComponentsCommand;
    private Command newComponentCommand;
    private Command editComponentCommand;
    private Command deleteComponentCommand;
    private Command refreshComponentsCommand;
    
    /**
     * Creates a new ComponentListMvvmViewModel.
     * 
     * @param componentService the component service
     */
    @Inject
    public ComponentListMvvmViewModel(ComponentService componentService) {
        this.componentService = componentService;
        
        // Cast to the async implementation
        this.componentServiceAsync = (ComponentServiceAsyncImpl) componentService;
        
        initializeCommands();
        updateFilterPredicate();
    }
    
    /**
     * Initializes the commands for this view model.
     */
    private void initializeCommands() {
        // Load components command
        loadComponentsCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadComponentsAsync);
        
        // New component command
        newComponentCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("New component command executed");
            },
            () -> true // Always enabled
        );
        
        // Edit component command
        editComponentCommand = createValidOnlyCommand(
            () -> {
                // This will be handled by the view/controller
                LOGGER.info("Edit component command executed for: " + 
                    (selectedComponent.get() != null ? selectedComponent.get().getName() : "null"));
            },
            () -> selectedComponent.get() != null
        );
        
        // Delete component command
        deleteComponentCommand = createValidOnlyCommand(
            this::deleteComponentAsync,
            () -> selectedComponent.get() != null
        );
        
        // Refresh components command
        refreshComponentsCommand = MvvmAsyncHelper.createSimpleAsyncCommand(this::loadComponentsAsync);
    }
    
    /**
     * Initializes the view model with a project.
     * 
     * @param project the project
     */
    public void initWithProject(Project project) {
        currentProject.set(project);
        loadComponentsAsync();
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
        filteredComponents.setPredicate(buildFilterPredicate());
    }
    
    /**
     * Builds a predicate based on the current filter.
     * 
     * @return the predicate
     */
    private Predicate<Component> buildFilterPredicate() {
        return component -> {
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
        };
    }
    
    /**
     * Loads components asynchronously.
     */
    private void loadComponentsAsync() {
        loading.set(true);
        
        // Find all components asynchronously
        componentServiceAsync.findAllAsync(
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    allComponents.clear();
                    allComponents.addAll(result);
                    clearErrorMessage();
                    loading.set(false);
                    LOGGER.info("Loaded " + result.size() + " components asynchronously");
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error loading components asynchronously", error);
                    setErrorMessage("Failed to load components: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Deletes a component asynchronously.
     */
    private void deleteComponentAsync() {
        Component component = selectedComponent.get();
        if (component == null || component.getId() == null) {
            return;
        }
        
        // Check if the component is used by any tasks
        if (component.getRequiredForTasks() != null && !component.getRequiredForTasks().isEmpty()) {
            int taskCount = component.getRequiredForTasks().size();
            setErrorMessage("Cannot delete component because it is required by " + taskCount + 
                    " task(s). Please remove these dependencies first.");
            return;
        }
        
        loading.set(true);
        
        // Using the async service method
        componentServiceAsync.deleteByIdAsync(
            component.getId(),
            // Success callback
            result -> {
                Platform.runLater(() -> {
                    if (result) {
                        allComponents.remove(component);
                        selectedComponent.set(null);
                        clearErrorMessage();
                        LOGGER.info("Deleted component: " + component.getName() + " asynchronously");
                    } else {
                        LOGGER.warning("Failed to delete component: " + component.getName() + " asynchronously");
                        setErrorMessage("Failed to delete component: Database operation unsuccessful");
                    }
                    loading.set(false);
                });
            },
            // Error callback
            error -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.SEVERE, "Error deleting component asynchronously", error);
                    setErrorMessage("Failed to delete component: " + error.getMessage());
                    loading.set(false);
                });
            }
        );
    }
    
    /**
     * Gets the components list.
     * 
     * @return the components list
     */
    public ObservableList<Component> getComponents() {
        return filteredComponents;
    }
    
    /**
     * Gets the selected component property.
     * 
     * @return the selected component property
     */
    public ObjectProperty<Component> selectedComponentProperty() {
        return selectedComponent;
    }
    
    /**
     * Gets the selected component.
     * 
     * @return the selected component
     */
    public Component getSelectedComponent() {
        return selectedComponent.get();
    }
    
    /**
     * Sets the selected component.
     * 
     * @param component the selected component
     */
    public void setSelectedComponent(Component component) {
        selectedComponent.set(component);
    }
    
    /**
     * Gets the current project property.
     * 
     * @return the current project property
     */
    public ObjectProperty<Project> currentProjectProperty() {
        return currentProject;
    }
    
    /**
     * Gets the current project.
     * 
     * @return the current project
     */
    public Project getCurrentProject() {
        return currentProject.get();
    }
    
    /**
     * Sets the current project.
     * 
     * @param project the project
     */
    public void setCurrentProject(Project project) {
        currentProject.set(project);
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
     * Gets the loading property.
     * 
     * @return the loading property
     */
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    /**
     * Checks if the view model is loading data.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return loading.get();
    }
    
    /**
     * Gets the load components command.
     * 
     * @return the load components command
     */
    public Command getLoadComponentsCommand() {
        return loadComponentsCommand;
    }
    
    /**
     * Gets the new component command.
     * 
     * @return the new component command
     */
    public Command getNewComponentCommand() {
        return newComponentCommand;
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
     * Gets the refresh components command.
     * 
     * @return the refresh components command
     */
    public Command getRefreshComponentsCommand() {
        return refreshComponentsCommand;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        allComponents.clear();
    }
}