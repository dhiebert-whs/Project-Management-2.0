package org.frcpm.services;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.utils.TestEnvironmentSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, TestEnvironmentSetup.class})
public class ComponentServiceTest {
    
    private static final Logger LOGGER = Logger.getLogger(ComponentServiceTest.class.getName());
    
    private ComponentService componentService;
    private ProjectService projectService;
    private SubsystemService subsystemService;
    private TaskService taskService;
    
    private Project testProject;
    private Subsystem testSubsystem;
    private Task testTask;
    
    @BeforeEach
    public void setUp() {
        // Force development mode for testing
        System.setProperty("app.db.dev", "true");
        
        // Initialize a clean database for each test
        DatabaseConfig.reinitialize(true);
        
        componentService = ServiceFactory.getComponentService();
        projectService = ServiceFactory.getProjectService();
        subsystemService = ServiceFactory.getSubsystemService();
        taskService = ServiceFactory.getTaskService();
        
        // Create required test entities in a single transaction
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // Create test project
            testProject = new Project(
                "Component Test Project", 
                LocalDate.now(), 
                LocalDate.now().plusWeeks(6), 
                LocalDate.now().plusWeeks(8)
            );
            
            em.persist(testProject);
            
            // Create test subsystem
            testSubsystem = new Subsystem(
                "Component Test Subsystem",
                Subsystem.Status.NOT_STARTED
            );
            testSubsystem.setDescription("Test subsystem for component tests");
            
            em.persist(testSubsystem);
            
            // Create test task
            testTask = new Task(
                "Component Test Task",
                testProject,
                testSubsystem
            );
            testTask.setEstimatedDuration(java.time.Duration.ofHours(5));
            testTask.setPriority(Task.Priority.MEDIUM);
            testTask.setStartDate(LocalDate.now());
            testTask.setEndDate(LocalDate.now().plusDays(7));
            
            em.persist(testTask);
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.severe("Error setting up test data: " + e.getMessage());
            e.printStackTrace();
            fail("Failed to set up test data: " + e.getMessage());
        } finally {
            em.close();
        }
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data in reverse order (respecting foreign key constraints)
        EntityManager em = DatabaseConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // First clean up components
            em.createQuery("DELETE FROM Component c WHERE c.name LIKE 'Test Component%'")
                .executeUpdate();
            
            // Delete test task
            Task task = em.find(Task.class, testTask.getId());
            if (task != null) {
                em.remove(task);
            }
            
            // Delete test subsystem
            Subsystem subsystem = em.find(Subsystem.class, testSubsystem.getId());
            if (subsystem != null) {
                em.remove(subsystem);
            }
            
            // Delete test project
            Project project = em.find(Project.class, testProject.getId());
            if (project != null) {
                em.remove(project);
            }
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            LOGGER.warning("Error during cleanup: " + e.getMessage());
        } finally {
            em.close();
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
        assertTrue(deliveredList.stream().anyMatch(c -> c.getId().equals(delivered.getId())));
        
        // Find undelivered components
        List<Component> undeliveredList = componentService.findByDelivered(false);
        assertFalse(undeliveredList.isEmpty());
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