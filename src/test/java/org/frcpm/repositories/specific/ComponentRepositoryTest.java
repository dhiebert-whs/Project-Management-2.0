// src/test/java/org/frcpm/repositories/specific/ComponentRepositoryTest.java
package org.frcpm.repositories.specific;

import org.frcpm.models.Component;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.repositories.BaseRepositoryTest;
import org.frcpm.repositories.impl.ComponentRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ComponentRepository implementation.
 */
public class ComponentRepositoryTest extends BaseRepositoryTest {

    private ComponentRepository componentRepository;
    private Project testProject;
    
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        componentRepository = new ComponentRepositoryImpl();
    }

    @Override
    protected void setupTestData() {
        // Create test project
        beginTransaction();
        testProject = new Project("Test Project", 
                LocalDate.now().minusDays(10), 
                LocalDate.now().plusDays(80), 
                LocalDate.now().plusDays(90));
        em.persist(testProject);
        commitTransaction();
    }

    @Test
    public void testSave() {
        // Create a new component
        Component component = new Component("Motor Controller", "MC-2023-001");
        component.setDescription("Motor controller for the drive system");
        component.setExpectedDelivery(LocalDate.now().plusDays(7));
        
        // Save the component
        Component savedComponent = componentRepository.save(component);
        
        // Verify saved component
        assertNotNull(savedComponent);
        assertNotNull(savedComponent.getId());
        assertEquals("Motor Controller", savedComponent.getName());
        assertEquals("MC-2023-001", savedComponent.getPartNumber());
        assertEquals("Motor controller for the drive system", savedComponent.getDescription());
        assertEquals(LocalDate.now().plusDays(7), savedComponent.getExpectedDelivery());
        assertFalse(savedComponent.isDelivered());
        assertNull(savedComponent.getActualDelivery());
    }

    @Test
    public void testFindById() {
        // Create and save a component
        beginTransaction();
        Component component = new Component("Test Component", "TC-123");
        component.setDescription("Component for testing findById");
        em.persist(component);
        Long componentId = component.getId();
        commitTransaction();
        
        // Find by ID
        Optional<Component> found = componentRepository.findById(componentId);
        
        // Verify the result
        assertTrue(found.isPresent());
        assertEquals("Test Component", found.get().getName());
        assertEquals("TC-123", found.get().getPartNumber());
        assertEquals("Component for testing findById", found.get().getDescription());
    }

    @Test
    public void testFindAll() {
        // Create multiple components
        beginTransaction();
        Component component1 = new Component("Component 1", "C1-001");
        Component component2 = new Component("Component 2", "C2-002");
        Component component3 = new Component("Component 3", "C3-003");
        em.persist(component1);
        em.persist(component2);
        em.persist(component3);
        commitTransaction();
        
        // Find all components
        List<Component> components = componentRepository.findAll();
        
        // Verify results
        assertNotNull(components);
        assertEquals(3, components.size());
    }

    @Test
    public void testUpdate() {
        // Create and save a component
        beginTransaction();
        Component component = new Component("Original Name", "ON-123");
        component.setDescription("Original description");
        component.setExpectedDelivery(LocalDate.now().plusDays(10));
        em.persist(component);
        Long componentId = component.getId();
        commitTransaction();
        
        // Update the component
        Optional<Component> found = componentRepository.findById(componentId);
        assertTrue(found.isPresent());
        
        Component toUpdate = found.get();
        toUpdate.setName("Updated Name");
        toUpdate.setPartNumber("UN-456");
        toUpdate.setDescription("Updated description");
        toUpdate.setExpectedDelivery(LocalDate.now().plusDays(5));
        
        componentRepository.save(toUpdate);
        
        // Verify the update
        Optional<Component> updated = componentRepository.findById(componentId);
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getName());
        assertEquals("UN-456", updated.get().getPartNumber());
        assertEquals("Updated description", updated.get().getDescription());
        assertEquals(LocalDate.now().plusDays(5), updated.get().getExpectedDelivery());
    }

    @Test
    public void testDelete() {
        // Create and save a component
        beginTransaction();
        Component component = new Component("To Delete", "DEL-123");
        em.persist(component);
        Long componentId = component.getId();
        commitTransaction();
        
        // Verify it exists
        Optional<Component> found = componentRepository.findById(componentId);
        assertTrue(found.isPresent());
        
        // Delete the component
        componentRepository.delete(found.get());
        
        // Verify it was deleted
        Optional<Component> deleted = componentRepository.findById(componentId);
        assertFalse(deleted.isPresent());
    }

    @Test
    public void testDeleteById() {
        // Create and save a component
        beginTransaction();
        Component component = new Component("To Delete By ID", "DEL-456");
        em.persist(component);
        Long componentId = component.getId();
        commitTransaction();
        
        // Verify it exists
        Optional<Component> found = componentRepository.findById(componentId);
        assertTrue(found.isPresent());
        
        // Delete by ID
        boolean result = componentRepository.deleteById(componentId);
        assertTrue(result);
        
        // Verify it was deleted
        Optional<Component> deleted = componentRepository.findById(componentId);
        assertFalse(deleted.isPresent());
    }

    @Test
    public void testCount() {
        // Create multiple components
        beginTransaction();
        Component component1 = new Component("Component A", "CA-001");
        Component component2 = new Component("Component B", "CB-002");
        em.persist(component1);
        em.persist(component2);
        commitTransaction();
        
        // Count components
        long count = componentRepository.count();
        
        // Verify count
        assertEquals(2, count);
    }

    @Test
    public void testFindByPartNumber() {
        // Create components with unique part numbers
        beginTransaction();
        Component component1 = new Component("Specific Component", "SPEC-123");
        Component component2 = new Component("Other Component", "OTH-456");
        em.persist(component1);
        em.persist(component2);
        commitTransaction();
        
        // Find by part number
        Optional<Component> found = componentRepository.findByPartNumber("SPEC-123");
        
        // Verify result
        assertTrue(found.isPresent());
        assertEquals("Specific Component", found.get().getName());
        assertEquals("SPEC-123", found.get().getPartNumber());
        
        // Test finding non-existent part number
        Optional<Component> notFound = componentRepository.findByPartNumber("NONEXISTENT");
        assertFalse(notFound.isPresent());
    }

    @Test
    public void testFindByName() {
        // Create components with different names
        beginTransaction();
        Component component1 = new Component("Motor", "MTR-001");
        Component component2 = new Component("Motor Controller", "MC-001");
        Component component3 = new Component("Sensor", "SNS-001");
        em.persist(component1);
        em.persist(component2);
        em.persist(component3);
        commitTransaction();
        
        // Find by partial name
        List<Component> motorComponents = componentRepository.findByName("Motor");
        
        // Verify results
        assertNotNull(motorComponents);
        assertEquals(2, motorComponents.size());
        assertTrue(motorComponents.stream().allMatch(c -> c.getName().contains("Motor")));
        
        // Test case sensitivity and exact matching
        List<Component> exactNameComponents = componentRepository.findByName("Sensor");
        assertEquals(1, exactNameComponents.size());
        assertEquals("Sensor", exactNameComponents.get(0).getName());
    }

    @Test
    public void testFindByDelivered() {
        // Create components with different delivery status
        beginTransaction();
        Component component1 = new Component("Delivered Component", "DC-001");
        component1.setDelivered(true);
        
        Component component2 = new Component("Pending Component 1", "PC-001");
        component2.setDelivered(false);
        
        Component component3 = new Component("Pending Component 2", "PC-002");
        component3.setDelivered(false);
        
        em.persist(component1);
        em.persist(component2);
        em.persist(component3);
        commitTransaction();
        
        // Find delivered components
        List<Component> deliveredComponents = componentRepository.findByDelivered(true);
        
        // Verify results
        assertNotNull(deliveredComponents);
        assertEquals(1, deliveredComponents.size());
        assertTrue(deliveredComponents.get(0).isDelivered());
        assertEquals("Delivered Component", deliveredComponents.get(0).getName());
        
        // Find pending components
        List<Component> pendingComponents = componentRepository.findByDelivered(false);
        
        // Verify results
        assertNotNull(pendingComponents);
        assertEquals(2, pendingComponents.size());
        assertTrue(pendingComponents.stream().noneMatch(Component::isDelivered));
    }

    @Test
    public void testFindByExpectedDeliveryAfter() {
        // Create components with different expected delivery dates
        LocalDate today = LocalDate.now();
        LocalDate cutoffDate = today.plusDays(10);
        
        beginTransaction();
        Component component1 = new Component("Soon Component", "SC-001");
        component1.setExpectedDelivery(today.plusDays(5));
        
        Component component2 = new Component("Later Component", "LC-001");
        component2.setExpectedDelivery(today.plusDays(15));
        
        Component component3 = new Component("No Delivery Date", "ND-001");
        // No expected delivery date set
        
        em.persist(component1);
        em.persist(component2);
        em.persist(component3);
        commitTransaction();
        
        // Find components expected after cutoff date
        List<Component> laterComponents = componentRepository.findByExpectedDeliveryAfter(cutoffDate);
        
        // Verify results
        assertNotNull(laterComponents);
        assertEquals(1, laterComponents.size());
        assertEquals("Later Component", laterComponents.get(0).getName());
    }

    @Test
    public void testComponentTaskRelationship() {
        // Create components and tasks with relationships
        beginTransaction();
        
        // Create components
        Component motor = new Component("Motor", "MTR-001");
        Component controller = new Component("Controller", "CTRL-001");
        Component sensor = new Component("Sensor", "SNS-001");
        em.persist(motor);
        em.persist(controller);
        em.persist(sensor);
        
        // Create a subsystem without assuming any specific setter methods
        Subsystem subsystem = new Subsystem("Test Subsystem");
        // We won't assume any specific required fields or setters
        em.persist(subsystem);
        
        // Create tasks with the required subsystem
        Task task1 = new Task("Assemble drivetrain", testProject, subsystem);
        Task task2 = new Task("Setup sensors", testProject, subsystem);
        
        // Set up the many-to-many relationship manually
        // First, persist the tasks
        em.persist(task1);
        em.persist(task2);
        
        // Now create the relationship using raw SQL (to avoid needing to know the exact entity structure)
        // This approach will work regardless of how the many-to-many relationship is defined in the entities
        em.createNativeQuery("INSERT INTO task_components (task_id, component_id) VALUES (?, ?)")
            .setParameter(1, task1.getId())
            .setParameter(2, motor.getId())
            .executeUpdate();
        
        em.createNativeQuery("INSERT INTO task_components (task_id, component_id) VALUES (?, ?)")
            .setParameter(1, task1.getId())
            .setParameter(2, controller.getId())
            .executeUpdate();
        
        em.createNativeQuery("INSERT INTO task_components (task_id, component_id) VALUES (?, ?)")
            .setParameter(1, task2.getId())
            .setParameter(2, sensor.getId())
            .executeUpdate();
        
        // Capture IDs for later verification
        Long motorId = motor.getId();
        Long task1Id = task1.getId();
        
        // Ensure changes are synchronized
        em.flush();
        commitTransaction();
        
        // Use a direct query to verify the relationships
        beginTransaction();
        
        // Query to count how many tasks use the motor component
        Long taskCount = (Long) em.createQuery(
                "SELECT COUNT(t) FROM Task t JOIN t.requiredComponents c WHERE c.id = :componentId")
                .setParameter("componentId", motorId)
                .getSingleResult();
        
        assertEquals(1L, taskCount);
        
        // Query to count components used by task1
        Long componentCount = (Long) em.createQuery(
                "SELECT COUNT(c) FROM Component c JOIN c.requiredForTasks t WHERE t.id = :taskId")
                .setParameter("taskId", task1Id)
                .getSingleResult();
        
        assertEquals(2L, componentCount);
        
        commitTransaction();
    }

    @Test
    public void testComponentDeliveryStatusUpdate() {
        // Create a component
        Component component = new Component("Automatic Delivery Test", "ADT-001");
        component.setExpectedDelivery(LocalDate.now().plusDays(5));
        
        // Save the component
        Component savedComponent = componentRepository.save(component);
        
        // Verify initial state
        assertFalse(savedComponent.isDelivered());
        assertNull(savedComponent.getActualDelivery());
        
        // Mark as delivered
        savedComponent.setDelivered(true);
        Component updatedComponent = componentRepository.save(savedComponent);
        
        // Verify delivered state
        assertTrue(updatedComponent.isDelivered());
        assertNotNull(updatedComponent.getActualDelivery());
        assertEquals(LocalDate.now(), updatedComponent.getActualDelivery());
        
        // Test explicit delivery date
        Component anotherComponent = new Component("Explicit Delivery", "EXP-001");
        LocalDate specificDate = LocalDate.now().minusDays(2);
        
        anotherComponent.setActualDelivery(specificDate);
        anotherComponent.setDelivered(true);
        
        Component savedWithExplicitDate = componentRepository.save(anotherComponent);
        
        // Verify delivery date wasn't overwritten
        assertEquals(specificDate, savedWithExplicitDate.getActualDelivery());
    }

    @Test
    public void testComponentToString() {
        // Test toString with part number
        Component withPartNumber = new Component("Test Component", "TEST-123");
        String withPartNumberString = withPartNumber.toString();
        assertEquals("Test Component (TEST-123)", withPartNumberString);
        
        // Test toString without part number
        Component withoutPartNumber = new Component("No Part Number");
        String withoutPartNumberString = withoutPartNumber.toString();
        assertEquals("No Part Number", withoutPartNumberString);
        
        // Test toString with empty part number
        Component withEmptyPartNumber = new Component("Empty Part", "");
        String withEmptyPartNumberString = withEmptyPartNumber.toString();
        assertEquals("Empty Part", withEmptyPartNumberString);
    }
}