package org.frcpm.repositories;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.ProjectRepository;
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
public class ProjectRepositoryTest {
    
    private ProjectRepository repository;
    
    @BeforeEach
    public void setUp() {
        DatabaseConfig.initialize();
        repository = RepositoryFactory.getProjectRepository();
        
        // Add test data
        createTestProjects();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test data
        cleanupTestProjects();
        DatabaseConfig.shutdown();
    }
    
    private void createTestProjects() {
        Project project1 = new Project(
            "Test Project 1", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(6), 
            LocalDate.now().plusWeeks(8)
        );
        project1.setDescription("Test project 1 description");
        
        Project project2 = new Project(
            "Test Project 2", 
            LocalDate.now().plusDays(7), 
            LocalDate.now().plusWeeks(8), 
            LocalDate.now().plusWeeks(10)
        );
        project2.setDescription("Test project 2 description");
        
        repository.save(project1);
        repository.save(project2);
    }
    
    private void cleanupTestProjects() {
        List<Project> projects = repository.findAll();
        for (Project project : projects) {
            if (project.getName().startsWith("Test Project")) {
                repository.delete(project);
            }
        }
    }
    
    @Test
    public void testFindAll() {
        List<Project> projects = repository.findAll();
        assertNotNull(projects);
        assertTrue(projects.size() >= 2);
    }
    
    @Test
    public void testFindById() {
        // First, get a project ID from the DB
        List<Project> projects = repository.findAll();
        Project firstProject = projects.stream()
            .filter(p -> p.getName().startsWith("Test Project"))
            .findFirst().orElseThrow();
        
        // Now test findById
        Optional<Project> found = repository.findById(firstProject.getId());
        assertTrue(found.isPresent());
        assertEquals(firstProject.getName(), found.get().getName());
    }
    
    @Test
    public void testFindByName() {
        List<Project> projects = repository.findByName("Test Project");
        assertFalse(projects.isEmpty());
        assertTrue(projects.stream().allMatch(p -> p.getName().contains("Test Project")));
    }
    
    @Test
    public void testSave() {
        Project newProject = new Project(
            "Test Save Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        newProject.setDescription("Test save description");
        
        Project saved = repository.save(newProject);
        assertNotNull(saved.getId());
        
        // Verify it was saved
        Optional<Project> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Save Project", found.get().getName());
    }
    
    @Test
    public void testUpdate() {
        // First, create a project
        Project project = new Project(
            "Test Update Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project saved = repository.save(project);
        
        // Now update it
        saved.setName("Updated Project Name");
        Project updated = repository.save(saved);
        
        // Verify the update
        assertEquals("Updated Project Name", updated.getName());
        
        // Check in DB
        Optional<Project> found = repository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Project Name", found.get().getName());
    }
    
    @Test
    public void testDelete() {
        // First, create a project
        Project project = new Project(
            "Test Delete Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project saved = repository.save(project);
        Long id = saved.getId();
        
        // Now delete it
        repository.delete(saved);
        
        // Verify the deletion
        Optional<Project> found = repository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        // First, create a project
        Project project = new Project(
            "Test DeleteById Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        Project saved = repository.save(project);
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
        
        // Add a new project
        Project project = new Project(
            "Test Count Project", 
            LocalDate.now(), 
            LocalDate.now().plusWeeks(4), 
            LocalDate.now().plusWeeks(6)
        );
        repository.save(project);
        
        // Verify count increased
        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
    }
    
    @Test
    public void testFindByDeadlineBefore() {
        LocalDate futureDate = LocalDate.now().plusWeeks(9);
        List<Project> projects = repository.findByDeadlineBefore(futureDate);
        assertFalse(projects.isEmpty());
        
        for (Project project : projects) {
            assertTrue(project.getHardDeadline().isBefore(futureDate));
        }
    }
    
    @Test
    public void testFindByStartDateAfter() {
        LocalDate pastDate = LocalDate.now().minusWeeks(1);
        List<Project> projects = repository.findByStartDateAfter(pastDate);
        assertFalse(projects.isEmpty());
        
        for (Project project : projects) {
            assertTrue(project.getStartDate().isAfter(pastDate));
        }
    }
}