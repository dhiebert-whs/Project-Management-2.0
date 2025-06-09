// src/test/java/org/frcpm/services/impl/MilestoneServiceTest.java

package org.frcpm.services.impl;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.spring.MilestoneRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for MilestoneService implementation using Spring Boot testing patterns.
 */
@ExtendWith(MockitoExtension.class)
class MilestoneServiceTest {
    
    @Mock
    private MilestoneRepository milestoneRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    private MilestoneServiceImpl milestoneService;
    
    private Milestone testMilestone;
    private Project testProject;
    private LocalDate now;
    
    @BeforeEach
    void setUp() {
        // Initialize dates and objects first to avoid NullPointerException
        now = LocalDate.now();
        testProject = createTestProject();
        testMilestone = createTestMilestone();
        
        // Configure mock repository responses
        when(milestoneRepository.findById(1L)).thenReturn(Optional.of(testMilestone));
        when(milestoneRepository.findAll()).thenReturn(List.of(testMilestone));
        when(milestoneRepository.save(any(Milestone.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Configure project repository
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // Create service with injected mocks
        milestoneService = new MilestoneServiceImpl(
            milestoneRepository,
            projectRepository
        );
    }
    
    /**
     * Creates a test project for use in tests.
     */
    private Project createTestProject() {
        Project project = new Project("Test Project", now.minusDays(10), now.plusDays(80), now.plusDays(90));
        project.setId(1L);
        project.setDescription("Test Description");
        return project;
    }
    
    /**
     * Creates a test milestone for use in tests.
     */
    private Milestone createTestMilestone() {
        Milestone milestone = new Milestone("Test Milestone", now.plusDays(30), testProject);
        milestone.setId(1L);
        milestone.setDescription("Test Description");
        return milestone;
    }
    
    @Test
    void testFindById() {
        // Reset mocks to ensure clean test state
        reset(milestoneRepository);
        
        // Setup - create a special milestone for this test
        Milestone uniqueMilestone = new Milestone("Unique Milestone", now.plusDays(25), testProject);
        uniqueMilestone.setId(1L);
        
        // Configure fresh mock behavior for this test
        when(milestoneRepository.findById(1L)).thenReturn(Optional.of(uniqueMilestone));
        
        // Execute
        Milestone result = milestoneService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Milestone ID should match");
        assertEquals("Unique Milestone", result.getName(), "Milestone name should match exactly");
        
        // Verify repository was called exactly once with the correct ID
        verify(milestoneRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindAll() {
        // Execute
        List<Milestone> results = milestoneService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
        
        // Verify repository was called
        verify(milestoneRepository).findAll();
    }
    
    @Test
    void testSave() {
        // Setup
        Milestone newMilestone = new Milestone("New Milestone", now.plusDays(40), testProject);
        
        // Execute
        Milestone result = milestoneService.save(newMilestone);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Milestone", result.getName());
        
        // Verify repository was called
        verify(milestoneRepository).save(newMilestone);
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(milestoneRepository).delete(any(Milestone.class));
        
        // Execute
        milestoneService.delete(testMilestone);
        
        // Verify repository was called
        verify(milestoneRepository).delete(testMilestone);
    }
    
    @Test
    void testDeleteById() {
        // Setup - Use doNothing() for void methods
        doNothing().when(milestoneRepository).deleteById(anyLong());
        
        // Execute
        milestoneService.deleteById(1L);
        
        // Verify repository was called
        verify(milestoneRepository).deleteById(1L);
    }
    
    @Test
    void testCount() {
        // Setup
        when(milestoneRepository.count()).thenReturn(5L);
        
        // Execute
        long result = milestoneService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository was called
        verify(milestoneRepository).count();
    }
    
    @Test
    void testFindByProject() {
        // Setup
        when(milestoneRepository.findByProject(testProject)).thenReturn(List.of(testMilestone));
        
        // Execute
        List<Milestone> results = milestoneService.findByProject(testProject);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testMilestone, results.get(0));
        
        // Verify repository was called
        verify(milestoneRepository).findByProject(testProject);
    }
    
    @Test
    void testFindByDateBefore() {
        // Setup
        LocalDate cutoffDate = now.plusDays(50);
        when(milestoneRepository.findByDateBefore(cutoffDate)).thenReturn(List.of(testMilestone));
        
        // Execute
        List<Milestone> results = milestoneService.findByDateBefore(cutoffDate);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        
        // Verify repository was called
        verify(milestoneRepository).findByDateBefore(cutoffDate);
    }
    
    @Test
    void testFindByDateAfter() {
        // Setup
        LocalDate cutoffDate = now.plusDays(10);
        when(milestoneRepository.findByDateAfter(cutoffDate)).thenReturn(List.of(testMilestone));
        
        // Execute
        List<Milestone> results = milestoneService.findByDateAfter(cutoffDate);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        
        // Verify repository was called
        verify(milestoneRepository).findByDateAfter(cutoffDate);
    }
    
    @Test
    void testFindByDateBetween() {
        // Setup
        LocalDate startDate = now.plusDays(20);
        LocalDate endDate = now.plusDays(40);
        when(milestoneRepository.findByDateBetween(startDate, endDate)).thenReturn(List.of(testMilestone));
        
        // Execute
        List<Milestone> results = milestoneService.findByDateBetween(startDate, endDate);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        
        // Verify repository was called
        verify(milestoneRepository).findByDateBetween(startDate, endDate);
    }
    
    @Test
    void testCreateMilestone() {
        // Execute
        Milestone result = milestoneService.createMilestone(
            "New Milestone", 
            now.plusDays(45), 
            1L, 
            "New Description"
        );
        
        // Verify
        assertNotNull(result);
        assertEquals("New Milestone", result.getName());
        assertEquals(now.plusDays(45), result.getDate());
        assertEquals("New Description", result.getDescription());
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(milestoneRepository).save(any(Milestone.class));
    }
    
    @Test
    void testCreateMilestone_ProjectNotFound() {
        // Setup
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.createMilestone(
                "New Milestone", 
                now.plusDays(45), 
                999L, 
                "New Description"
            );
        });
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Project not found"));
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(milestoneRepository, never()).save(any(Milestone.class));
    }
    
    @Test
    void testUpdateMilestoneDate() {
        // Setup
        LocalDate newDate = now.plusDays(50);
        
        // Execute
        Milestone result = milestoneService.updateMilestoneDate(1L, newDate);
        
        // Verify
        assertNotNull(result);
        assertEquals(newDate, result.getDate());
        
        // Verify repository calls
        verify(milestoneRepository).findById(1L);
        verify(milestoneRepository).save(any(Milestone.class));
    }
    
    @Test
    void testUpdateMilestoneDate_MilestoneNotFound() {
        // Setup
        when(milestoneRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Milestone result = milestoneService.updateMilestoneDate(999L, now.plusDays(50));
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(milestoneRepository).findById(999L);
        verify(milestoneRepository, never()).save(any(Milestone.class));
    }
    
    @Test
    void testUpdateDescription() {
        // Execute
        Milestone result = milestoneService.updateDescription(1L, "Updated Description");
        
        // Verify
        assertNotNull(result);
        assertEquals("Updated Description", result.getDescription());
        
        // Verify repository calls
        verify(milestoneRepository).findById(1L);
        verify(milestoneRepository).save(any(Milestone.class));
    }
    
    @Test
    void testGetUpcomingMilestones() {
        // Setup
        LocalDate today = LocalDate.now();
        Milestone upcomingMilestone = new Milestone("Upcoming Milestone", today.plusDays(5), testProject);
        upcomingMilestone.setId(2L);
        
        List<Milestone> projectMilestones = List.of(testMilestone, upcomingMilestone);
        when(milestoneRepository.findByProject(testProject)).thenReturn(projectMilestones);
        
        // Execute
        List<Milestone> results = milestoneService.getUpcomingMilestones(1L, 10);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Upcoming Milestone", results.get(0).getName());
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(milestoneRepository).findByProject(testProject);
    }
    
    @Test
    void testGetUpcomingMilestones_ProjectNotFound() {
        // Setup
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        List<Milestone> results = milestoneService.getUpcomingMilestones(999L, 10);
        
        // Verify
        assertNotNull(results);
        assertTrue(results.isEmpty());
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(milestoneRepository, never()).findByProject(any(Project.class));
    }
}