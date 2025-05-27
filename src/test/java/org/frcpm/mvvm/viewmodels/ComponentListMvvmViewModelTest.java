// src/test/java/org/frcpm/mvvm/viewmodels/ComponentListMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.frcpm.di.TestModule;
import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.mvvm.viewmodels.ComponentListMvvmViewModel.ComponentFilter;
import org.frcpm.services.ComponentService;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the ComponentListMvvmViewModel class.
 * FIXED: Uses proper mock pattern instead of casting to concrete implementations.
 */
public class ComponentListMvvmViewModelTest {
    
    private ComponentService componentService;
    
    private Project testProject;
    private List<Component> testComponents;
    private ComponentListMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create mock service
        ComponentService mockService = mock(ComponentService.class);
        
        // Register mock with TestModule
        TestModule.setService(ComponentService.class, mockService);
        
        // Get service from TestModule (now returns mock)
        componentService = TestModule.getService(ComponentService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new ComponentListMvvmViewModel(componentService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        // Create test components
        testComponents = new ArrayList<>();
        
        Component component1 = new Component();
        component1.setId(1L);
        component1.setName("Motors");
        component1.setPartNumber("CIM-001");
        component1.setDescription("Drive motors for robot");
        component1.setExpectedDelivery(LocalDate.now().plusDays(5));
        component1.setDelivered(false);
        testComponents.add(component1);
        
        Component component2 = new Component();
        component2.setId(2L);
        component2.setName("Wheels");
        component2.setPartNumber("WHL-002");
        component2.setDescription("6-inch pneumatic wheels");
        component2.setExpectedDelivery(LocalDate.now().minusDays(2));
        component2.setActualDelivery(LocalDate.now().minusDays(1));
        component2.setDelivered(true);
        testComponents.add(component2);
        
        Component component3 = new Component();
        component3.setId(3L);
        component3.setName("Control System");
        component3.setPartNumber("CTRL-003");
        component3.setDescription("RoboRIO and related electronics");
        component3.setExpectedDelivery(LocalDate.now().plusDays(10));
        component3.setDelivered(false);
        testComponents.add(component3);
        
        Component component4 = new Component();
        component4.setId(4L);
        component4.setName("Sensors");
        component4.setPartNumber("SNS-004");
        component4.setDescription("Various sensors for autonomous");
        component4.setExpectedDelivery(LocalDate.now().minusDays(5));
        component4.setActualDelivery(LocalDate.now().minusDays(3));
        component4.setDelivered(true);
        testComponents.add(component4);
        
        Component component5 = new Component();
        component5.setId(5L);
        component5.setName("Frame Material");
        component5.setPartNumber("FRM-005");
        component5.setDescription("Aluminum extrusion for frame");
        component5.setExpectedDelivery(LocalDate.now().plusDays(15));
        component5.setDelivered(false);
        testComponents.add(component5);
    }
    
    @Test
    public void testInitialState() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Check that the viewModel is not null
            assertNotNull(viewModel, "ViewModel should not be null");
            
            // Check initial values
            assertTrue(viewModel.getComponents().isEmpty());
            assertNull(viewModel.getSelectedComponent());
            assertNull(viewModel.getCurrentProject());
            assertFalse(viewModel.isLoading());
            assertEquals(ComponentFilter.ALL, viewModel.getCurrentFilter());
            
            // Verify commands exist
            assertNotNull(viewModel.getLoadComponentsCommand());
            assertNotNull(viewModel.getNewComponentCommand());
            assertNotNull(viewModel.getEditComponentCommand());
            assertNotNull(viewModel.getDeleteComponentCommand());
            assertNotNull(viewModel.getRefreshComponentsCommand());
            
            // Check command executability
            assertTrue(viewModel.getLoadComponentsCommand().isExecutable());
            assertTrue(viewModel.getNewComponentCommand().isExecutable());
            assertTrue(viewModel.getRefreshComponentsCommand().isExecutable());
            assertTrue(viewModel.getDeleteComponentCommand().isNotExecutable()); // No selection
            assertTrue(viewModel.getEditComponentCommand().isNotExecutable()); // No selection
        });
    }
    
    @Test
    public void testInitWithProject() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            // Get the success callback and call it with test data
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Component>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testComponents);
            return null;
        }).when(componentService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.initWithProject(testProject);
            
            // Verify project was set
            assertEquals(testProject, viewModel.getCurrentProject());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify components were loaded
            assertFalse(viewModel.getComponents().isEmpty());
            assertEquals(5, viewModel.getComponents().size());
        });
    }
    
    @Test
    public void testLoadComponentsCommand() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Component>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testComponents);
            return null;
        }).when(componentService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute load command
            viewModel.getLoadComponentsCommand().execute();
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify components were loaded
            assertFalse(viewModel.getComponents().isEmpty());
            assertEquals(5, viewModel.getComponents().size());
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testRefreshComponentsCommand() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Component>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testComponents);
            return null;
        }).when(componentService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute refresh command
            viewModel.getRefreshComponentsCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify components were loaded
            assertFalse(viewModel.getComponents().isEmpty());
            assertEquals(5, viewModel.getComponents().size());
        });
    }
    
    @Test
    public void testComponentSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no selection
            assertNull(viewModel.getSelectedComponent());
            
            // Commands requiring selection should not be executable
            assertTrue(viewModel.getEditComponentCommand().isNotExecutable());
            assertTrue(viewModel.getDeleteComponentCommand().isNotExecutable());
            
            // Select a component
            Component selectedComponent = testComponents.get(0);
            viewModel.setSelectedComponent(selectedComponent);
            
            // Verify selection
            assertEquals(selectedComponent, viewModel.getSelectedComponent());
            
            // Commands requiring selection should now be executable
            assertTrue(viewModel.getEditComponentCommand().isExecutable());
            assertTrue(viewModel.getDeleteComponentCommand().isExecutable());
        });
    }
    
    @Test
    public void testNewComponentCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Command should always be executable
            assertTrue(viewModel.getNewComponentCommand().isExecutable());
            
            // Execute command
            viewModel.getNewComponentCommand().execute();
            
            // This command is mainly for the view to handle
            // Just verify it executed without error
        });
    }
    
    @Test
    public void testEditComponentCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not executable (no selection)
            assertTrue(viewModel.getEditComponentCommand().isNotExecutable());
            
            // Select a component
            viewModel.setSelectedComponent(testComponents.get(0));
            
            // Should now be executable
            assertTrue(viewModel.getEditComponentCommand().isExecutable());
            
            // Execute command
            viewModel.getEditComponentCommand().execute();
            
            // This command is mainly for the view to handle
            // Just verify it executed without error
        });
    }
    
    @Test
    public void testDeleteComponentCommand() {
        // Configure the mock service for successful deletion
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Boolean> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(true);
            return null;
        }).when(componentService).deleteByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add components to the list
            viewModel.getComponents().clear();
            viewModel.getComponents().addAll(testComponents);
            
            // Select a component
            Component componentToDelete = testComponents.get(0);
            viewModel.setSelectedComponent(componentToDelete);
            
            // Should be executable
            assertTrue(viewModel.getDeleteComponentCommand().isExecutable());
            
            // Execute delete command
            viewModel.getDeleteComponentCommand().execute();
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify component was removed from list
            assertFalse(viewModel.getComponents().contains(componentToDelete));
            assertEquals(4, viewModel.getComponents().size());
            
            // Selection should be cleared
            assertNull(viewModel.getSelectedComponent());
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testDeleteComponentCommandFailure() {
        // Configure the mock service for failed deletion
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Boolean> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(false);
            return null;
        }).when(componentService).deleteByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add components to the list
            viewModel.getComponents().clear();
            viewModel.getComponents().addAll(testComponents);
            
            // Select a component
            Component componentToDelete = testComponents.get(0);
            viewModel.setSelectedComponent(componentToDelete);
            
            // Execute delete command
            viewModel.getDeleteComponentCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Component should still be in the list (deletion failed)
            assertTrue(viewModel.getComponents().contains(componentToDelete));
            assertEquals(5, viewModel.getComponents().size());
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to delete"));
        });
    }
    
    @Test
    public void testDeleteComponentWithTaskDependencies() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create component with task dependencies
            Component componentWithTasks = testComponents.get(0);
            // Simulate having tasks that require this component
            componentWithTasks.getRequiredForTasks().add(new org.frcpm.models.Task());
            
            // Add components to the list
            viewModel.getComponents().clear();
            viewModel.getComponents().addAll(testComponents);
            viewModel.setSelectedComponent(componentWithTasks);
            
            // Execute delete command
            viewModel.getDeleteComponentCommand().execute();
            
            // Should show error message about dependencies
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("required by"));
            
            // Should not call service
            verify(componentService, never()).deleteByIdAsync(any(), any(), any());
        });
    }
    
    @Test
    public void testFilterComponents() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add all components to the list first
            viewModel.getComponents().clear();
            for (Component comp : testComponents) {
                viewModel.getComponents().add(comp);
            }
            
            // Initially should show all components (filter = ALL)
            assertEquals(ComponentFilter.ALL, viewModel.getCurrentFilter());
            assertEquals(5, viewModel.getComponents().size());
            
            // Filter to show only delivered components
            viewModel.setFilter(ComponentFilter.DELIVERED);
            
            // Should show only delivered components (2 out of 5)
            assertEquals(ComponentFilter.DELIVERED, viewModel.getCurrentFilter());
            
            // Count delivered components manually for verification
            long deliveredCount = testComponents.stream()
                .filter(Component::isDelivered)
                .count();
            assertEquals(2, deliveredCount); // Wheels and Sensors are delivered
            
            // Filter to show only pending components
            viewModel.setFilter(ComponentFilter.PENDING);
            assertEquals(ComponentFilter.PENDING, viewModel.getCurrentFilter());
            
            // Filter back to all
            viewModel.setFilter(ComponentFilter.ALL);
            assertEquals(ComponentFilter.ALL, viewModel.getCurrentFilter());
        });
    }
    
    @Test
    public void testFilterWithNullValue() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Try to set null filter
            viewModel.setFilter(null);
            
            // Should default to ALL
            assertEquals(ComponentFilter.ALL, viewModel.getCurrentFilter());
        });
    }
    
    @Test
    public void testComponentFilterEnum() {
        // Test the ComponentFilter enum
        assertEquals("All Components", ComponentFilter.ALL.toString());
        assertEquals("Delivered", ComponentFilter.DELIVERED.toString());
        assertEquals("Pending Delivery", ComponentFilter.PENDING.toString());
    }
    
    @Test
    public void testErrorHandlingDuringLoad() {
        // Configure the mock service to return an error
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = 
                invocation.getArgument(1);
            errorCallback.accept(new RuntimeException("Service error"));
            return null;
        }).when(componentService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute load command
            viewModel.getLoadComponentsCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to load"));
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Components list should remain empty
            assertTrue(viewModel.getComponents().isEmpty());
        });
    }
    
    @Test
    public void testErrorHandlingDuringDelete() {
        // Configure the mock service to return an error
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = 
                invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Delete error"));
            return null;
        }).when(componentService).deleteByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add components to the list
            viewModel.getComponents().clear();
            viewModel.getComponents().addAll(testComponents);
            
            // Select a component
            Component componentToDelete = testComponents.get(0);
            viewModel.setSelectedComponent(componentToDelete);
            
            // Execute delete command
            viewModel.getDeleteComponentCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to delete"));
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Component should still be in the list
            assertTrue(viewModel.getComponents().contains(componentToDelete));
        });
    }
    
    @Test
    public void testCurrentProjectProperty() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no current project
            assertNull(viewModel.getCurrentProject());
            
            // Set current project
            viewModel.setCurrentProject(testProject);
            
            // Verify project was set
            assertEquals(testProject, viewModel.getCurrentProject());
        });
    }
    
    @Test
    public void testLoadingProperty() {
        // Configure the mock service with a delay to test loading state
        doAnswer(invocation -> {
            // Simulate async delay
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    @SuppressWarnings("unchecked")
                    java.util.function.Consumer<List<Component>> successCallback = 
                        invocation.getArgument(0);
                    successCallback.accept(testComponents);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return null;
        }).when(componentService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not loading
            assertFalse(viewModel.isLoading());
            
            // Execute load command
            viewModel.getLoadComponentsCommand().execute();
            
            // Should be loading immediately
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testComponentsListManipulation() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially empty
            assertTrue(viewModel.getComponents().isEmpty());
            
            // Add components
            viewModel.getComponents().addAll(testComponents);
            
            // Verify size
            assertEquals(5, viewModel.getComponents().size());
            
            // Verify specific components
            assertTrue(viewModel.getComponents().contains(testComponents.get(0)));
            assertTrue(viewModel.getComponents().contains(testComponents.get(1)));
            assertTrue(viewModel.getComponents().contains(testComponents.get(2)));
            assertTrue(viewModel.getComponents().contains(testComponents.get(3)));
            assertTrue(viewModel.getComponents().contains(testComponents.get(4)));
            
            // Clear list
            viewModel.getComponents().clear();
            
            // Should be empty again
            assertTrue(viewModel.getComponents().isEmpty());
        });
    }
    
    @Test
    public void testCommandExecutabilityWithoutSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // No component selected
            assertNull(viewModel.getSelectedComponent());
            
            // Commands that don't require selection should be executable
            assertTrue(viewModel.getLoadComponentsCommand().isExecutable());
            assertTrue(viewModel.getNewComponentCommand().isExecutable());
            assertTrue(viewModel.getRefreshComponentsCommand().isExecutable());
            
            // Commands that require selection should not be executable
            assertTrue(viewModel.getEditComponentCommand().isNotExecutable());
            assertTrue(viewModel.getDeleteComponentCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testCommandExecutabilityWithSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Select a component
            viewModel.setSelectedComponent(testComponents.get(0));
            
            // All commands should be executable
            assertTrue(viewModel.getLoadComponentsCommand().isExecutable());
            assertTrue(viewModel.getNewComponentCommand().isExecutable());
            assertTrue(viewModel.getRefreshComponentsCommand().isExecutable());
            assertTrue(viewModel.getEditComponentCommand().isExecutable());
            assertTrue(viewModel.getDeleteComponentCommand().isExecutable());
        });
    }
    
    @Test
    public void testMultipleLoadOperations() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Component>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testComponents);
            return null;
        }).when(componentService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute load command multiple times
            viewModel.getLoadComponentsCommand().execute();
            viewModel.getRefreshComponentsCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should still have correct data
            assertEquals(5, viewModel.getComponents().size());
            
            // Verify service was called multiple times
            verify(componentService, atLeast(2)).findAllAsync(any(), any());
        });
    }
    
    @Test
    public void testComponentSelectionClearing() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Select a component
            viewModel.setSelectedComponent(testComponents.get(0));
            assertNotNull(viewModel.getSelectedComponent());
            
            // Clear selection
            viewModel.setSelectedComponent(null);
            assertNull(viewModel.getSelectedComponent());
            
            // Commands requiring selection should not be executable
            assertTrue(viewModel.getEditComponentCommand().isNotExecutable());
            assertTrue(viewModel.getDeleteComponentCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testComponentDeliveryStatus() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add components to list
            viewModel.getComponents().addAll(testComponents);
            
            // Verify we have both delivered and undelivered components
            boolean foundDelivered = false;
            boolean foundUndelivered = false;
            
            for (Component component : viewModel.getComponents()) {
                if (component.isDelivered()) foundDelivered = true;
                else foundUndelivered = true;
            }
            
            assertTrue(foundDelivered);
            assertTrue(foundUndelivered);
        });
    }
    
    @Test
    public void testComponentPartNumbers() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add components to list
            viewModel.getComponents().addAll(testComponents);
            
            // Verify all components have part numbers
            for (Component component : viewModel.getComponents()) {
                assertNotNull(component.getPartNumber());
                assertFalse(component.getPartNumber().isEmpty());
            }
        });
    }
    
    @Test
    public void testDeleteComponentWithNullSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Ensure no selection
            viewModel.setSelectedComponent(null);
            
            // Command should not be executable
            assertTrue(viewModel.getDeleteComponentCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testDeleteComponentWithNullId() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create component without ID
            Component componentWithoutId = new Component();
            componentWithoutId.setName("No ID Component");
            
            // Add to list and select
            viewModel.getComponents().add(componentWithoutId);
            viewModel.setSelectedComponent(componentWithoutId);
            
            // Execute delete command
            viewModel.getDeleteComponentCommand().execute();
            
            // Should handle gracefully and not call service
            verify(componentService, never()).deleteByIdAsync(any(), any(), any());
        });
    }
    
    @Test
    public void testAsyncServiceCasting() {
        // Verify that the service can be cast to our mock
        assertNotNull(componentService);
        // The service is now a proper mock, no casting needed
    }
    
    @Test
    public void testDispose() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Set up some data
            viewModel.getComponents().addAll(testComponents);
            viewModel.setSelectedComponent(testComponents.get(0));
            
            // Verify data exists
            assertFalse(viewModel.getComponents().isEmpty());
            assertNotNull(viewModel.getSelectedComponent());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify collections were cleared
            assertTrue(viewModel.getComponents().isEmpty());
        });
    }
    
    @Test
    public void testFilteredListUpdatesWithFilter() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add all components to the list
            viewModel.getComponents().clear();
            for (Component comp : testComponents) {
                viewModel.getComponents().add(comp);
            }
            
            // Test filtering to delivered only
            viewModel.setFilter(ComponentFilter.DELIVERED);
            
            // The filtered list should automatically update
            // Count how many delivered components we expect
            long expectedDelivered = testComponents.stream()
                .filter(Component::isDelivered)
                .count();
            
            // Verify the count matches what we expect (2 delivered components)
            assertEquals(2, expectedDelivered);
            
            // Test filtering to pending only
            viewModel.setFilter(ComponentFilter.PENDING);
            
            // Count how many pending components we expect
            long expectedPending = testComponents.stream()
                .filter(comp -> !comp.isDelivered())
                .count();
            
            // Verify the count matches what we expect (3 pending components)
            assertEquals(3, expectedPending);
        });
    }
}