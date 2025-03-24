package org.frcpm.repositories;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Component;
import org.frcpm.repositories.specific.ComponentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ComponentRepositoryTest {
    
    private ComponentRepository repository;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        repository = RepositoryFactory.getComponentRepository();
        
        // Add test data
        createTestComponents();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestComponents();
        DatabaseConfig.shutdown();
    }
    
    private void createTestComponents() {
        Component component1 = new Component("Test Component 1", "TC-001");
        component1.setDescription("First test component description");
        component1.setExpectedDelivery(LocalDate.now().plusDays(5));
        
        Component component2 = new Component("Test Component 2", "TC-002");
        component2.setDescription("Second test component description");
        component2.setExpectedDelivery(LocalDate.now().plusDays(7));
        
        Component component3 = new Component("Test Component 3", "TC-003");
        component3.setDescription("Third test component description");
        component3.setExpectedDelivery(LocalDate.now().plusDays(10));
        component3.setDelivered(true);
        component3.setActualDelivery(LocalDate.now().plusDays(9));
        
        repository.save(component1);
        repository.save(component2);
        repository.save(component3);
    }
    
    private void cleanupTestComponents() {
        List<Component> components = repository.findAll();
        for (Component component : components) {
            if (component.getName().startsWith("Test Component")) {
                repository.delete(component);
            }
        }
    }
    
    @Test
    public void testFindAll() {
        List<Component> components = repository.findAll();
        assertNotNull(components);
        assertTrue(components.size() >= 3);
    }
    
    @Test
    public void testFindById() {
        // First, get a component ID from the DB
        List<Component> components = repository.findAll();
        Component firstComponent = components.stream()
            .filter(c -> c.getName().startsWith("Test Component"))
            .findFirst().orElseThrow();
        
        // Now test findById
        Optional<Component> found = repository.findById(firstComponent.getId());
        assertTrue(found.isPresent());
        assertEquals(firstComponent.getName(), found.get().getName());
    }
    
    @Test
    public void testFindByPartNumber() {
        Optional<Component> component = repository.findByPartNumber("TC-001");
        assertTrue(component.isPresent());
        assertEquals("Test Component 1", component.get().getName());
    }
    
    @Test
    public void testFindByName() {
        List<Component> components = repository.findByName("Component 2");
        assertFalse(components.isEmpty());
        assertTrue(components.stream().anyMatch(c -> c.getName().equals("Test Component 2")));
    }
    
    @Test
    public void testFindByDelivered() {
        List<Component> deliveredComponents = repository.findByDelivered(true);
        assertFalse(deliveredComponents.isEmpty());
        assertTrue(deliveredComponents.stream().allMatch(Component::isDelivered));
        assertTrue(deliveredComponents.stream().anyMatch(c -> c.getName().equals("Test Component 3")));
        
        List<Component> undeliveredComponents = repository.findByDelivered(false);
        assertFalse(undeliveredComponents.isEmpty());
        assertTrue(undeliveredComponents.stream().noneMatch(Component::isDelivered));
        assertTrue(undeliveredComponents.stream().anyMatch(c -> c.getName().equals("Test Component 1")));
    }
    
    @Test
    public void testFindByExpectedDeliveryAfter() {
        LocalDate cutoffDate = LocalDate.now().plusDays(6);
        List<Component> components = repository.findByExpectedDeliveryAfter(cutoffDate);
        assertFalse(components.isEmpty());
        
        for (Component component : components) {
            assertTrue(component.getExpectedDelivery().isAfter(cutoffDate));
        }
        
        assertTrue(components.stream().anyMatch(c -> c.getName().equals("Test Component 2")));
        assertTrue(components.stream().anyMatch(c -> c.getName().equals("Test Component 3")));
    }
    
    @Test
    public void testSave() {
        Component newComponent = new Component("Test Save Component", "TSC-001");
        newComponent.setDescription("Save component description");
        newComponent.setExpectedDelivery(LocalDate.now().plusDays(15));
        
        Component saved = repository.save(newComponent);
        assertNotNull(saved.getId());
        
        // Verify it was saved
        Optional<Component> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Save Component", found.get().getName());
    }
    
    @Test
    public void testUpdate() {
        // First, create a component
        Component component = new Component("Test Update Component", "TUC-001");
        Component saved = repository.save(component);
        
        // Now update it
        saved.setName("Updated Component Name");
        saved.setDescription("Updated description");
        saved.setDelivered(true);
        Component updated = repository.save(saved);
        
        // Verify the update
        assertEquals("Updated Component Name", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertTrue(updated.isDelivered());
        
        // Check in DB
        Optional<Component> found = repository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Component Name", found.get().getName());
        assertEquals("Updated description", found.get().getDescription());
        assertTrue(found.get().isDelivered());
    }
    
    @Test
    public void testDelete() {
        // First, create a component
        Component component = new Component("Test Delete Component", "TDC-001");
        Component saved = repository.save(component);
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<Component> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        // First, create a component
        Component component = new Component("Test DeleteById Component", "TDBI-001");
        Component saved = repository.save(component);
        Long id = saved.getId();
        
        // Now delete it by ID
        boolean result = repository.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Optional<Component> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testCount() {
        long initialCount = repository.count();
        
        // Add a new component
        Component component = new Component("Test Count Component", "TCC-001");
        repository.save(component);
        
        // Verify count increased
        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
    }
}