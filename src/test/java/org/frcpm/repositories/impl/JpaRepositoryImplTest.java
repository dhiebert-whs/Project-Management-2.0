package org.frcpm.repositories.impl;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Project;
import org.frcpm.repositories.Repository;
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
public class JpaRepositoryImplTest {
    
    private Repository<Project, Long> repository;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        // Use ProjectRepositoryImpl as a concrete implementation of JpaRepositoryImpl
        repository = new ProjectRepositoryImpl();
        
        // Add test data
        createTestEntities();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestEntities();
        DatabaseConfig.shutdown();
    }
    
    private void createTestEntities() {
        Project project1 = new Project(
            "Test Base Project 1", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(6), 
            LocalDate.now().plusWeeks(8)
        );
        
        Project project2 = new Project(
            "Test Base Project 2", 
            LocalDate.now().plusDays(7), 
            LocalDate.now().plusWeeks(8), 
            LocalDate.now().plusWeeks(10)
        );
        
        repository.save(project1);
        repository.save(project2);
    }
    
    private void cleanupTestEntities() {
        List<Project> entities = repository.findAll();
        for (Project entity : entities) {
            if (entity.getName().startsWith("Test Base Project")) {
                repository.delete(entity);
            }
        }
    }
    
    @Test
    public void testFindAll() {
        // First let's count how many test entities we have
        List<Project> allEntities = repository.findAll();
        int initialCount = allEntities.size();
        
        // Now add two specific test entities that we can verify
        Project testProject1 = new Project(
            "Test FindAll Project 1", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project testProject2 = new Project(
            "Test FindAll Project 2", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        
        repository.save(testProject1);
        repository.save(testProject2);
        
        // Get the list again and verify
        List<Project> entities = repository.findAll();
        assertNotNull(entities);
        assertTrue(entities.size() >= initialCount + 2);
        
        // Verify our test entities are in the results
        boolean foundProject1 = false;
        boolean foundProject2 = false;
        
        for (Project project : entities) {
            if (project.getName().equals("Test FindAll Project 1")) {
                foundProject1 = true;
            }
            if (project.getName().equals("Test FindAll Project 2")) {
                foundProject2 = true;
            }
        }
        
        assertTrue(foundProject1, "Test project 1 was not found in results");
        assertTrue(foundProject2, "Test project 2 was not found in results");
    }
    
    @Test
    public void testFindById() {
        // First, get an entity ID from the DB
        List<Project> entities = repository.findAll();
        Project firstEntity = entities.stream()
            .filter(e -> e.getName().startsWith("Test Base Project"))
            .findFirst().orElseThrow();
        
        // Now test findById
        Optional<Project> found = repository.findById(firstEntity.getId());
        assertTrue(found.isPresent());
        assertEquals(firstEntity.getName(), found.get().getName());
    }
    
    @Test
    public void testSave() {
        Project newEntity = new Project(
            "Test Base Save Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        
        Project saved = repository.save(newEntity);
        assertNotNull(saved.getId());
        
        // Verify it was saved
        Optional<Project> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Base Save Project", found.get().getName());
    }
    
    @Test
    public void testUpdate() {
        // First, create an entity
        Project entity = new Project(
            "Test Base Update Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project saved = repository.save(entity);
        
        // Now update it
        saved.setName("Updated Base Project Name");
        Project updated = repository.save(saved);
        
        // Verify the update
        assertEquals("Updated Base Project Name", updated.getName());
        
        // Check in DB
        Optional<Project> found = repository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Base Project Name", found.get().getName());
    }
    
    @Test
    public void testDelete() {
        // First, create an entity
        Project entity = new Project(
            "Test Base Delete Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project saved = repository.save(entity);
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<Project> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        // First, create an entity
        Project entity = new Project(
            "Test Base DeleteById Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project saved = repository.save(entity);
        Long id = saved.getId();
        
        // Now delete it by ID
        boolean result = repository.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Optional<Project> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testCount() {
        // Get initial count
        long initialCount = repository.count();
        
        // Add a specific number of test entities
        Project entity1 = new Project(
            "Test Count Project 1", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project entity2 = new Project(
            "Test Count Project 2", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        
        repository.save(entity1);
        repository.save(entity2);
        
        // Verify count increased by exactly the number we added
        long newCount = repository.count();
        assertEquals(initialCount + 2, newCount, 
                    "Count should increase by exactly 2 after adding 2 projects");
    }
}