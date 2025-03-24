package org.frcpm.services.impl;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;
import org.frcpm.services.Service;
import org.frcpm.services.ServiceFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AbstractServiceTestImpl {
    
    private Service<Project, Long> service;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        // Use ProjectServiceImpl as a concrete implementation of AbstractService
        service = ServiceFactory.getProjectService();
        
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
        ProjectService projectService = (ProjectService) service;
        
        projectService.createProject(
            "Test Abstract Service Project 1", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(6), 
            LocalDate.now().plusWeeks(8)
        );
        
        projectService.createProject(
            "Test Abstract Service Project 2", 
            LocalDate.now().plusDays(7), 
            LocalDate.now().plusWeeks(8), 
            LocalDate.now().plusWeeks(10)
        );
    }
    
    private void cleanupTestEntities() {
        List<Project> entities = service.findAll();
        for (Project entity : entities) {
            if (entity.getName().startsWith("Test Abstract Service Project")) {
                service.delete(entity);
            }
        }
    }
    
    @Test
    public void testFindAll() {
        List<Project> entities = service.findAll();
        assertNotNull(entities);
        assertTrue(entities.size() >= 2);
    }
    
    @Test
    public void testFindById() {
        // First, get an entity ID from the DB
        List<Project> entities = service.findAll();
        Project firstEntity = entities.stream()
            .filter(e -> e.getName().startsWith("Test Abstract Service Project"))
            .findFirst().orElseThrow();
        
        // Now test findById
        Project found = service.findById(firstEntity.getId());
        assertNotNull(found);
        assertEquals(firstEntity.getName(), found.getName());
    }
    
    @Test
    public void testSave() {
        ProjectService projectService = (ProjectService) service;
        Project created = projectService.createProject(
            "Test Abstract Save Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        
        // Now save an update
        created.setDescription("Updated description");
        Project saved = service.save(created);
        
        assertNotNull(saved);
        assertEquals("Updated description", saved.getDescription());
        
        // Verify it was saved
        Project found = service.findById(saved.getId());
        assertNotNull(found);
        assertEquals("Updated description", found.getDescription());
    }
    
    @Test
    public void testDelete() {
        // First, create an entity
        ProjectService projectService = (ProjectService) service;
        Project created = projectService.createProject(
            "Test Abstract Delete Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Long id = created.getId();
        
        // Now delete it
        service.delete(created);
        
        // Verify the deletion
        Project found = service.findById(id);
        assertNull(found);
    }
    
    @Test
    public void testDeleteById() {
        // First, create an entity
        ProjectService projectService = (ProjectService) service;
        Project created = projectService.createProject(
            "Test Abstract DeleteById Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Long id = created.getId();
        
        // Now delete it by ID
        boolean result = service.deleteById(id);
        assertTrue(result);
        
        // Verify the deletion
        Project found = service.findById(id);
        assertNull(found);
    }
    
    @Test
    public void testCount() {
        long initialCount = service.count();
        
        // Add a new entity
        ProjectService projectService = (ProjectService) service;
        projectService.createProject(
            "Test Abstract Count Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        
        // Verify count increased
        long newCount = service.count();
        assertEquals(initialCount + 1, newCount);
    }
}