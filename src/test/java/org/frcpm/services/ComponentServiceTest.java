package org.frcpm.services;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ComponentServiceTest {
    
    private ComponentService componentService;
    private ProjectService projectService;
    private SubsystemService subsystemService;
    private TaskService taskService;
    
    private Project testProject;
    private Subsystem testSubsystem;
    private Task testTask;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        
        componentService = ServiceFactory.getComponentService();
        projectService = ServiceFactory.getProjectService();
        subsystemService = ServiceFactory.getSubsystemService();
        taskService = ServiceFactory.getTaskService();
        
        // Create required test entities
        testProject = projectService.createProject(
            "Component Test Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(6), 
            LocalDate.now().plusWeeks(8)
        );
        
        testSubsystem = subsystemService.createSubsystem(
            "Component Test Subsystem",
            "Test subsystem for component tests",
            Subsystem.Status.NOT_STARTED,
            null
        );
        
        testTask = taskService.createTask(
            "Component Test Task",
            testProject,
            testSubsystem,
            5.0, // 5 hours
            Task.Priority.MEDIUM,
            LocalDate.now(),
            LocalDate.now().plusDays(7)
        );
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        try {
            // First, clean up components
            List<Component> components = componentService.findAll();
            for (Component component : components) {
                if (component.getName().startsWith("Test Component")) {
                    componentService.deleteById(component.getId());
                }
            }
            
            // Delete test task
            taskService.deleteById(testTask.getId());
            
            // Delete test subsystem
            subsystemService.deleteById(testSubsystem.getId());
            
            // Delete test project
            projectService.deleteById(testProject.getId());
        } catch (Exception e) {
            // Log but continue with cleanup
            System.err.println("Error during cleanup: " + e.getMessage());
        }
        
        DatabaseConfig.shutdown();
    }
    
    @Test
    public void testCreateComponent() {
        Component component = componentService.createComponent(
            "Test Component 1",
            "TC-001",
            "Test component description",
            LocalDate.now().plusDays(5)
        );
        
        assertNotNull(component);
        assertNotNull(component.getId());
        assertEquals("Test Component 1", component.getName());
        assertEquals("TC-001", component.getPartNumber());
        assertEquals("Test component description", component.getDescription());
        assertEquals(LocalDate.now().plusDays(5), component.getExpectedDelivery());
        assertFalse(component.isDelivered());
    }
    
    @Test
    public void testCreateComponentWithoutPartNumber() {
        Component component = componentService.createComponent(
            "Test Component No PN",
            null,
            "Component with no part number",
            LocalDate.now().plusDays(3)
        );
        
        assertNotNull(component);
        assertNotNull(component.getId());
        assertEquals("Test Component No PN", component.getName());
        assertNull(component.getPartNumber());
    }
    
    @Test
    public void testFindByPartNumber() {
        // Create a component with a part number
        componentService.createComponent(
            "Test Component PN",
            "TC-FIND",
            "Component for part number test",
            LocalDate.now().plusDays(3)
        );
        
        // Find by part number
        Optional<Component> found = componentService.findByPartNumber("TC-FIND");
        assertTrue(found.isPresent());
        assertEquals("Test Component PN", found.get().getName());
        assertEquals("TC-FIND", found.get().getPartNumber());
    }
    
    @Test
    public void testFindByName() {
        // Create some components
        componentService.createComponent(
            "Test Component Name A",
            "TC-A",
            "Component A",
            LocalDate.now().plusDays(3)
        );
        
        componentService.createComponent(
            "Test Component Name B",
            "TC-B",
            "Component B",
            LocalDate.now().plusDays(5)
        );
        
        // Find by partial name
        List<Component> found = componentService.findByName("Name A");
        assertFalse(found.isEmpty());
        assertTrue(found.stream().anyMatch(c -> c.getName().equals("Test Component Name A")));
    }
    
    @Test
    public void testMarkAsDelivered() {
        // Create a component
        Component component = componentService.createComponent(
            "Test Component Delivery",
            "TC-DEL",
            "Component for delivery test",
            LocalDate.now().plusDays(10)
        );
        
        assertFalse(component.isDelivered());
        assertNull(component.getActualDelivery());
        
        // Mark as delivered
        LocalDate deliveryDate = LocalDate.now().minusDays(1);
        Component updated = componentService.markAsDelivered(component.getId(), deliveryDate);
        
        assertNotNull(updated);
        assertTrue(updated.isDelivered());
        assertEquals(deliveryDate, updated.getActualDelivery());
        
        // Check DB record
        Component found = componentService.findById(updated.getId());
        assertTrue(found.isDelivered());
        assertEquals(deliveryDate, found.getActualDelivery());
    }
    
    @Test
    public void testUpdateExpectedDelivery() {
        // Create a component
        Component component = componentService.createComponent(
            "Test Component Update",
            "TC-UPD",
            "Component for update test",
            LocalDate.now().plusDays(5)
        );
        
        // Update expected delivery
        LocalDate newDate = LocalDate.now().plusDays(15);
        Component updated = componentService.updateExpectedDelivery(component.getId(), newDate);
        
        assertNotNull(updated);
        assertEquals(newDate, updated.getExpectedDelivery());
        
        // Check DB record
        Component found = componentService.findById(updated.getId());
        assertEquals(newDate, found.getExpectedDelivery());
    }
    
    @Test
    public void testAssociateComponentsWithTask() {
        // Create some components
        Component component1 = componentService.createComponent(
            "Test Component Assoc 1",
            "TC-ASSOC1",
            "Component 1 for association test",
            LocalDate.now().plusDays(3)
        );
        
        Component component2 = componentService.createComponent(
            "Test Component Assoc 2",
            "TC-ASSOC2",
            "Component 2 for association test",
            LocalDate.now().plusDays(5)
        );
        
        // Associate components with task
        Set<Long> componentIds = new HashSet<>();
        componentIds.add(component1.getId());
        componentIds.add(component2.getId());
        
        Task updatedTask = componentService.associateComponentsWithTask(testTask.getId(), componentIds);
        
        assertNotNull(updatedTask);
        assertEquals(2, updatedTask.getRequiredComponents().size());
        assertTrue(updatedTask.getRequiredComponents().stream()
            .anyMatch(c -> c.getId().equals(component1.getId())));
        assertTrue(updatedTask.getRequiredComponents().stream()
            .anyMatch(c -> c.getId().equals(component2.getId())));
        
        // Check that the task has the components in the DB
        Task foundTask = taskService.findById(testTask.getId());
        assertEquals(2, foundTask.getRequiredComponents().size());
    }
    
    @Test
    public void testFindByDelivered() {
        // Create delivered and undelivered components
        Component delivered = componentService.createComponent(
            "Test Component Delivered",
            "TC-DEL-Y",
            "Delivered component",
            LocalDate.now().minusDays(5)
        );
        componentService.markAsDelivered(delivered.getId(), LocalDate.now().minusDays(1));
        
        Component undelivered = componentService.createComponent(
            "Test Component Undelivered",
            "TC-DEL-N",
            "Undelivered component",
            LocalDate.now().plusDays(5)
        );
        
        // Find delivered components
        List<Component> deliveredList = componentService.findByDelivered(true);
        assertFalse(deliveredList.isEmpty());
        assertTrue(deliveredList.stream().allMatch(Component::isDelivered));
        assertTrue(deliveredList.stream().anyMatch(c -> c.getId().equals(delivered.getId())));
        
        // Find undelivered components
        List<Component> undeliveredList = componentService.findByDelivered(false);
        assertFalse(undeliveredList.isEmpty());
        assertTrue(undeliveredList.stream().noneMatch(Component::isDelivered));
        assertTrue(undeliveredList.stream().anyMatch(c -> c.getId().equals(undelivered.getId())));
    }
    
    @Test
    public void testDeleteById() {
        // Create a component
        Component component = componentService.createComponent(
            "Test Component Delete",
            "TC-DEL",
            "Component for delete test",
            LocalDate.now().plusDays(5)
        );
        
        Long id = component.getId();
        
        // Delete the component
        boolean result = componentService.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Component found = componentService.findById(id);
        assertNull(found);
    }
    
    @Test
    public void testInvalidComponentCreation() {
        // Test null name
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            componentService.createComponent(
                null,
                "TC-INV",
                "Invalid component",
                LocalDate.now().plusDays(5)
            );
        });
        assertTrue(exception.getMessage().contains("name cannot be empty"));
        
        // Test duplicate part number
        componentService.createComponent(
            "Test Component Duplicate PN",
            "TC-DUP",
            "Component with duplicate part number",
            LocalDate.now().plusDays(3)
        );
        
        exception = assertThrows(IllegalArgumentException.class, () -> {
            componentService.createComponent(
                "Test Component Another with Duplicate PN",
                "TC-DUP", // Same part number
                "Another component with same part number",
                LocalDate.now().plusDays(5)
            );
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }
}