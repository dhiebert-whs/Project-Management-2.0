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
        List<Project> entities = repository.findAll();
        assertNotNull(entities);
        assertTrue(entities.size() >= 2);
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
        long initialCount = repository.count();
        
        // Add a new entity
        Project entity = new Project(
            "Test Base Count Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        repository.save(entity);
        
        // Verify count increased
        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
    }
}