// src/test/java/org/frcpm/services/impl/ComponentServiceTest.java

package org.frcpm.services.impl;

import de.saxsys.mvvmfx.MvvmFX;
import org.frcpm.models.Component;
import org.frcpm.models.Task;
import org.frcpm.repositories.specific.ComponentRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.BaseServiceTest;
import org.frcpm.services.ComponentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for ComponentService implementation using TestableComponentServiceImpl.
 */
public class ComponentServiceTest extends BaseServiceTest {
    
    @Mock
    private ComponentRepository componentRepository;
    
    @Mock
    private TaskRepository taskRepository;
    
    private ComponentService componentService;
    
    private Component testComponent;
    private Task testTask;
    
    @Override
    protected void setupTestData() {
        // Initialize test objects
        testComponent = createTestComponent();
        testTask = createTestTask();
    }
    
    @Override
    @BeforeEach
    public void setUp() {
        // Execute parent setup first (initializes Mockito annotations)
        super.setUp();
        
        // Configure mock repository responses
        when(componentRepository.findById(1L)).thenReturn(Optional.of(testComponent));
        when(componentRepository.findAll()).thenReturn(List.of(testComponent));
        when(componentRepository.save(any(Component.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Configure task repository
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        
        // Create service with injected mocks
        componentService = new TestableComponentServiceImpl(
            componentRepository,
            taskRepository
        );
        
        // Configure MVVMFx dependency injector for comprehensive testing
        MvvmFX.setCustomDependencyInjector(type -> {
            if (type == ComponentRepository.class) return componentRepository;
            if (type == TaskRepository.class) return taskRepository;
            if (type == ComponentService.class) return componentService;
            return null;
        });
    }
    
    @AfterEach
    @Override
    public void tearDown() throws Exception {
        // Clear MVVMFx dependency injector
        MvvmFX.setCustomDependencyInjector(null);
        
        // Call parent tearDown
        super.tearDown();
    }
    
    /**
     * Creates a test component for use in tests.
     * 
     * @return a test component
     */
    private Component createTestComponent() {
        Component component = new Component("Test Component", "TC-123");
        component.setId(1L);
        component.setDescription("Component for testing");
        component.setExpectedDelivery(LocalDate.now().plusDays(7));
        component.setDelivered(false);
        return component;
    }
    
    /**
     * Creates a test task for use in tests.
     * 
     * @return a test task
     */
    private Task createTestTask() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        return task;
    }
    
    @Test
    public void testFindById() {
        // Execute
        Component result = componentService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Component ID should match");
        assertEquals("Test Component", result.getName(), "Component name should match");
        
        // Verify repository was called exactly once with the correct ID
        verify(componentRepository, times(1)).findById(1L);
    }
    
// src/test/java/org/frcpm/services/impl/ComponentServiceTest.java (continued)

    @Test
    public void testFindAll() {
        // Execute
        List<Component> results = componentService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
        
        // Verify repository was called
        verify(componentRepository).findAll();
    }
    
    @Test
    public void testSave() {
        // Setup
        Component newComponent = new Component("New Component", "NC-456");
        newComponent.setDescription("New component for testing");
        
        // Execute
        Component result = componentService.save(newComponent);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Component", result.getName());
        assertEquals("NC-456", result.getPartNumber());
        
        // Verify repository was called
        verify(componentRepository).save(newComponent);
    }
    
    @Test
    public void testDelete() {
        // Setup
        doNothing().when(componentRepository).delete(any(Component.class));
        
        // Execute
        componentService.delete(testComponent);
        
        // Verify repository was called
        verify(componentRepository).delete(testComponent);
    }
    
    @Test
    public void testDeleteById() {
        // Setup
        when(componentRepository.deleteById(anyLong())).thenReturn(true);
        
        // Execute
        boolean result = componentService.deleteById(1L);
        
        // Verify
        assertTrue(result);
        
        // Verify repository was called
        verify(componentRepository).deleteById(1L);
    }
    
    @Test
    public void testCount() {
        // Setup
        when(componentRepository.count()).thenReturn(5L);
        
        // Execute
        long result = componentService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository was called
        verify(componentRepository).count();
    }
    
    @Test
    public void testFindByPartNumber() {
        // Setup
        when(componentRepository.findByPartNumber("TC-123")).thenReturn(Optional.of(testComponent));
        
        // Execute
        Optional<Component> result = componentService.findByPartNumber("TC-123");
        
        // Verify
        assertTrue(result.isPresent());
        assertEquals("Test Component", result.get().getName());
        
        // Verify repository was called
        verify(componentRepository).findByPartNumber("TC-123");
    }
    
    @Test
    public void testFindByName() {
        // Setup
        when(componentRepository.findByName("Test")).thenReturn(List.of(testComponent));
        
        // Execute
        List<Component> results = componentService.findByName("Test");
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testComponent, results.get(0));
        
        // Verify repository was called
        verify(componentRepository).findByName("Test");
    }
    
    @Test
    public void testFindByDelivered() {
        // Setup
        when(componentRepository.findByDelivered(false)).thenReturn(List.of(testComponent));
        
        // Execute
        List<Component> results = componentService.findByDelivered(false);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testComponent, results.get(0));
        
        // Verify repository was called
        verify(componentRepository).findByDelivered(false);
    }
    
    @Test
    public void testCreateComponent() {
        // Setup
        when(componentRepository.findByPartNumber("NC-456")).thenReturn(Optional.empty());
        
        // Execute
        Component result = componentService.createComponent(
            "New Component",
            "NC-456",
            "New component description",
            LocalDate.now().plusDays(14)
        );
        
        // Verify
        assertNotNull(result);
        assertEquals("New Component", result.getName());
        assertEquals("NC-456", result.getPartNumber());
        assertEquals("New component description", result.getDescription());
        assertEquals(LocalDate.now().plusDays(14), result.getExpectedDelivery());
        assertFalse(result.isDelivered());
        
        // Verify repository calls
        verify(componentRepository).findByPartNumber("NC-456");
        verify(componentRepository).save(any(Component.class));
    }
    
    @Test
    public void testCreateComponent_PartNumberExists() {
        // Setup
        when(componentRepository.findByPartNumber("TC-123")).thenReturn(Optional.of(testComponent));
        
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            componentService.createComponent(
                "Duplicate Component",
                "TC-123",
                "Component with duplicate part number",
                LocalDate.now().plusDays(14)
            );
        });
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("already exists"));
        
        // Verify repository calls
        verify(componentRepository).findByPartNumber("TC-123");
        verify(componentRepository, never()).save(any(Component.class));
    }
    
    @Test
    public void testCreateComponent_TestEnvironment() {
        // Setup - enable test environment
        System.setProperty("test.environment", "true");
        when(componentRepository.findByPartNumber("TC-123")).thenReturn(Optional.of(testComponent));
        
        try {
            // Execute
            Component result = componentService.createComponent(
                "Updated Component",
                "TC-123",
                "Updated component description",
                LocalDate.now().plusDays(10)
            );
            
            // Verify
            assertNotNull(result);
            assertEquals("Updated Component", result.getName());
            assertEquals("Updated component description", result.getDescription());
            assertEquals(LocalDate.now().plusDays(10), result.getExpectedDelivery());
            
            // Verify repository calls
            verify(componentRepository).findByPartNumber("TC-123");
            verify(componentRepository).save(any(Component.class));
        } finally {
            // Clean up
            System.clearProperty("test.environment");
        }
    }
    
    @Test
    public void testCreateComponent_NoName() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            componentService.createComponent(
                null,
                "TC-456",
                "Component with no name",
                LocalDate.now().plusDays(14)
            );
        });
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("name"));
        
        // Verify repository calls
        verify(componentRepository, never()).findByPartNumber(anyString());
        verify(componentRepository, never()).save(any(Component.class));
    }
    
    @Test
    public void testMarkAsDelivered() {
        // Execute
        Component result = componentService.markAsDelivered(1L, LocalDate.now());
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isDelivered());
        assertEquals(LocalDate.now(), result.getActualDelivery());
        
        // Verify repository calls
        verify(componentRepository).findById(1L);
        verify(componentRepository).save(any(Component.class));
    }
    
    @Test
    public void testMarkAsDelivered_NullDate() {
        // Execute
        Component result = componentService.markAsDelivered(1L, null);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isDelivered());
        assertEquals(LocalDate.now(), result.getActualDelivery());
        
        // Verify repository calls
        verify(componentRepository).findById(1L);
        verify(componentRepository).save(any(Component.class));
    }
    
    @Test
    public void testMarkAsDelivered_ComponentNotFound() {
        // Setup
        when(componentRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Component result = componentService.markAsDelivered(999L, LocalDate.now());
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(componentRepository).findById(999L);
        verify(componentRepository, never()).save(any(Component.class));
    }
    
    @Test
    public void testUpdateExpectedDelivery() {
        // Execute
        Component result = componentService.updateExpectedDelivery(1L, LocalDate.now().plusDays(20));
        
        // Verify
        assertNotNull(result);
        assertEquals(LocalDate.now().plusDays(20), result.getExpectedDelivery());
        
        // Verify repository calls
        verify(componentRepository).findById(1L);
        verify(componentRepository).save(any(Component.class));
    }
    
    @Test
    public void testUpdateExpectedDelivery_ComponentNotFound() {
        // Setup
        when(componentRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Component result = componentService.updateExpectedDelivery(999L, LocalDate.now().plusDays(20));
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(componentRepository).findById(999L);
        verify(componentRepository, never()).save(any(Component.class));
    }
    
    @Test
    public void testAssociateComponentsWithTask() {
        // Setup
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(componentRepository.findById(1L)).thenReturn(Optional.of(testComponent));
        
        // Execute
        Task result = componentService.associateComponentsWithTask(1L, Set.of(1L));
        
        // Verify
        assertNotNull(result);
        
        // Verify repository calls
        verify(taskRepository).findById(1L);
        verify(componentRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    public void testAssociateComponentsWithTask_TaskNotFound() {
        // Setup
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Task result = componentService.associateComponentsWithTask(999L, Set.of(1L));
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(taskRepository).findById(999L);
        verify(componentRepository, never()).findById(anyLong());
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    public void testAssociateComponentsWithTask_ComponentNotFound() {
        // Setup
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(componentRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Task result = componentService.associateComponentsWithTask(1L, Set.of(999L));
        
        // Verify
        assertNotNull(result);
        
        // Verify repository calls
        verify(taskRepository).findById(1L);
        verify(componentRepository).findById(999L);
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    public void testAssociateComponentsWithTask_NullComponentIds() {
        // Execute
        Task result = componentService.associateComponentsWithTask(1L, null);
        
        // Verify
        assertNotNull(result);
        
        // Verify repository calls
        verify(taskRepository).findById(1L);
        verify(componentRepository, never()).findById(anyLong());
        verify(taskRepository).save(any(Task.class));
    }
    
    @Test
    public void testAssociateComponentsWithTask_EmptyComponentIds() {
        // Execute
        Task result = componentService.associateComponentsWithTask(1L, new HashSet<>());
        
        // Verify
        assertNotNull(result);
        
        // Verify repository calls
        verify(taskRepository).findById(1L);
        verify(componentRepository, never()).findById(anyLong());
        verify(taskRepository).save(any(Task.class));
    }
}