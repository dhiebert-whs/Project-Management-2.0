// src/test/java/org/frcpm/repositories/specific/ProjectRepositoryTest.java
package org.frcpm.repositories.specific;

import org.frcpm.models.Project;
import org.frcpm.repositories.BaseRepositoryTest;
import org.frcpm.repositories.impl.ProjectRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ProjectRepository implementation.
 */
public class ProjectRepositoryTest extends BaseRepositoryTest {
    
    private ProjectRepository projectRepository;
    
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        projectRepository = new ProjectRepositoryImpl();
    }
    
    @Override
    protected void setupTestData() {
        // No data setup by default - tests will create their own data
    }
    
    @Test
    public void testSaveAndFindById() {
        // Create a test project
        Project project = new Project("Test Project", 
                                      LocalDate.now(), 
                                      LocalDate.now().plusMonths(1), 
                                      LocalDate.now().plusMonths(2));
        project.setDescription("Test Description");
        
        // Save the project
        Project savedProject = projectRepository.save(project);
        
        // Verify that ID was generated
        assertNotNull(savedProject.getId());
        
        // Find the project by ID
        Optional<Project> foundProject = projectRepository.findById(savedProject.getId());
        
        // Verify that the project was found
        assertTrue(foundProject.isPresent());
        assertEquals(savedProject.getId(), foundProject.get().getId());
        assertEquals("Test Project", foundProject.get().getName());
        assertEquals("Test Description", foundProject.get().getDescription());
    }
    
    @Test
    public void testFindAll() {
        // Create test projects
        Project project1 = new Project("Project 1", 
                                      LocalDate.now(), 
                                      LocalDate.now().plusMonths(1), 
                                      LocalDate.now().plusMonths(2));
        
        Project project2 = new Project("Project 2", 
                                      LocalDate.now().plusDays(5), 
                                      LocalDate.now().plusMonths(2), 
                                      LocalDate.now().plusMonths(3));
        
        // Save projects
        projectRepository.save(project1);
        projectRepository.save(project2);
        
        // Find all projects
        List<Project> projects = projectRepository.findAll();
        
        // Verify all projects were found
        assertEquals(2, projects.size());
    }
    
    @Test
    public void testUpdate() {
        // Create a test project
        Project project = new Project("Original Name", 
                                      LocalDate.now(), 
                                      LocalDate.now().plusMonths(1), 
                                      LocalDate.now().plusMonths(2));
        
        // Save the project
        Project savedProject = projectRepository.save(project);
        
        // Update the project
        savedProject.setName("Updated Name");
        savedProject.setDescription("Updated Description");
        
        // Save the updated project
        Project updatedProject = projectRepository.save(savedProject);
        
        // Find the project by ID
        Optional<Project> foundProject = projectRepository.findById(updatedProject.getId());
        
        // Verify that the project was updated
        assertTrue(foundProject.isPresent());
        assertEquals("Updated Name", foundProject.get().getName());
        assertEquals("Updated Description", foundProject.get().getDescription());
    }
    
    @Test
    public void testDelete() {
        // Create a test project
        Project project = new Project("Project to Delete", 
                                      LocalDate.now(), 
                                      LocalDate.now().plusMonths(1), 
                                      LocalDate.now().plusMonths(2));
        
        // Save the project
        Project savedProject = projectRepository.save(project);
        
        // Verify that the project exists
        Optional<Project> foundBeforeDelete = projectRepository.findById(savedProject.getId());
        assertTrue(foundBeforeDelete.isPresent());
        
        // Delete the project
        projectRepository.delete(savedProject);
        
        // Verify that the project was deleted
        Optional<Project> foundAfterDelete = projectRepository.findById(savedProject.getId());
        assertFalse(foundAfterDelete.isPresent());
    }
    
    @Test
    public void testDeleteById() {
        // Create a test project
        Project project = new Project("Project to Delete by ID", 
                                      LocalDate.now(), 
                                      LocalDate.now().plusMonths(1), 
                                      LocalDate.now().plusMonths(2));
        
        // Save the project
        Project savedProject = projectRepository.save(project);
        
        // Verify that the project exists
        Optional<Project> foundBeforeDelete = projectRepository.findById(savedProject.getId());
        assertTrue(foundBeforeDelete.isPresent());
        
        // Delete the project by ID
        boolean deleted = projectRepository.deleteById(savedProject.getId());
        
        // Verify that deletion was successful
        assertTrue(deleted);
        
        // Verify that the project was deleted
        Optional<Project> foundAfterDelete = projectRepository.findById(savedProject.getId());
        assertFalse(foundAfterDelete.isPresent());
    }
    
    @Test
    public void testCount() {
        // Create test projects
        Project project1 = new Project("Project 1", 
                                      LocalDate.now(), 
                                      LocalDate.now().plusMonths(1), 
                                      LocalDate.now().plusMonths(2));
        
        Project project2 = new Project("Project 2", 
                                      LocalDate.now().plusDays(5), 
                                      LocalDate.now().plusMonths(2), 
                                      LocalDate.now().plusMonths(3));
        
        // Verify initial count
        assertEquals(0, projectRepository.count());
        
        // Save projects
        projectRepository.save(project1);
        projectRepository.save(project2);
        
        // Verify updated count
        assertEquals(2, projectRepository.count());
        
        // Delete a project
        projectRepository.delete(project1);
        
        // Verify updated count after deletion
        assertEquals(1, projectRepository.count());
    }
    
    @Test
    public void testFindByName() {
        // Create test projects
        Project project1 = new Project("Test Project", 
                                      LocalDate.now(), 
                                      LocalDate.now().plusMonths(1), 
                                      LocalDate.now().plusMonths(2));
        
        Project project2 = new Project("Another Project", 
                                      LocalDate.now().plusDays(5), 
                                      LocalDate.now().plusMonths(2), 
                                      LocalDate.now().plusMonths(3));
        
        Project project3 = new Project("Test Something Else", 
                                      LocalDate.now().plusDays(10), 
                                      LocalDate.now().plusMonths(3), 
                                      LocalDate.now().plusMonths(4));
        
        // Save projects
        projectRepository.save(project1);
        projectRepository.save(project2);
        projectRepository.save(project3);
        
        // Find projects by name
        List<Project> testProjects = projectRepository.findByName("Test");
        
        // Verify that projects with "Test" in the name were found
        assertEquals(2, testProjects.size());
        assertTrue(testProjects.stream().anyMatch(p -> p.getName().equals("Test Project")));
        assertTrue(testProjects.stream().anyMatch(p -> p.getName().equals("Test Something Else")));
    }
    
    @Test
    public void testFindByDeadlineBefore() {
        LocalDate today = LocalDate.now();
        
        // Create test projects
        Project project1 = new Project("Early Deadline", 
                                      today, 
                                      today.plusMonths(1), 
                                      today.plusMonths(2));
        
        Project project2 = new Project("Late Deadline", 
                                      today, 
                                      today.plusMonths(3), 
                                      today.plusMonths(4));
        
        // Save projects
        projectRepository.save(project1);
        projectRepository.save(project2);
        
        // Find projects with deadline before a specific date
        LocalDate deadlineDate = today.plusMonths(3);
        List<Project> earlyProjects = projectRepository.findByDeadlineBefore(deadlineDate);
        
        // Verify that projects with deadline before the specified date were found
        assertEquals(1, earlyProjects.size());
        assertEquals("Early Deadline", earlyProjects.get(0).getName());
    }
    
    @Test
    public void testFindByStartDateAfter() {
        LocalDate today = LocalDate.now();
        
        // Create test projects
        Project project1 = new Project("Early Start", 
                                      today, 
                                      today.plusMonths(1), 
                                      today.plusMonths(2));
        
        Project project2 = new Project("Late Start", 
                                      today.plusDays(10), 
                                      today.plusMonths(2), 
                                      today.plusMonths(3));
        
        // Save projects
        projectRepository.save(project1);
        projectRepository.save(project2);
        
        // Find projects with start date after a specific date
        LocalDate startDate = today.plusDays(5);
        List<Project> lateProjects = projectRepository.findByStartDateAfter(startDate);
        
        // Verify that projects with start date after the specified date were found
        assertEquals(1, lateProjects.size());
        assertEquals("Late Start", lateProjects.get(0).getName());
    }
}