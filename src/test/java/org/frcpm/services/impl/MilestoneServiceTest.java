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
 * FIXED: Applied proven pattern from AttendanceServiceTest success template.
 */
@ExtendWith(MockitoExtension.class)
class MilestoneServiceTest {
    
    @Mock
    private MilestoneRepository milestoneRepository;
    
    @Mock
    private ProjectRepository projectRepository;
    
    private MilestoneServiceImpl milestoneService; // ✅ FIXED: Use implementation class
    
    private Milestone testMilestone;
    private Project testProject;
    private LocalDate now;
    
    @BeforeEach
    void setUp() {
        // ✅ FIXED: Create test objects ONLY - NO MOCK STUBBING HERE
        now = LocalDate.now();
        testProject = createTestProject();
        testMilestone = createTestMilestone();
        
        // Create service with injected mocks
        milestoneService = new MilestoneServiceImpl(milestoneRepository, projectRepository);
        
        // ✅ FIXED: NO mock stubbing in setUp() - move to individual test methods
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
        // Setup - Only stub what THIS test needs
        when(milestoneRepository.findById(1L)).thenReturn(Optional.of(testMilestone));
        
        // Execute
        Milestone result = milestoneService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Milestone ID should match");
        assertEquals("Test Milestone", result.getName(), "Milestone name should match");
        
        // Verify repository was called exactly once with the correct ID
        verify(milestoneRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindById_NotFound() {
        // Setup - Entity doesn't exist
        when(milestoneRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Milestone result = milestoneService.findById(999L);
        
        // Verify
        assertNull(result);
        
        // Verify repository was called
        verify(milestoneRepository).findById(999L);
    }
    
    @Test
    void testFindById_NullId() {
        // Execute
        Milestone result = milestoneService.findById(null);
        
        // Verify
        assertNull(result);
        
        // Verify repository was never called with null
        verify(milestoneRepository, never()).findById(any());
    }
    
    @Test
    void testFindAll() {
        // Setup
        when(milestoneRepository.findAll()).thenReturn(List.of(testMilestone));
        
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
        when(milestoneRepository.save(newMilestone)).thenReturn(newMilestone);
        
        // Execute
        Milestone result = milestoneService.save(newMilestone);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Milestone", result.getName());
        
        // Verify repository was called
        verify(milestoneRepository).save(newMilestone);
    }
    
    @Test
    void testSave_NullEntity() {
        // Execute and verify exception
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.save(null);
        });
        
        // Verify repository was never called
        verify(milestoneRepository, never()).save(any());
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(milestoneRepository).delete(testMilestone);
        
        // Execute
        milestoneService.delete(testMilestone);
        
        // Verify repository was called
        verify(milestoneRepository).delete(testMilestone);
    }
    
    @Test
    void testDelete_NullEntity() {
        // Execute - should not throw exception
        milestoneService.delete(null);
        
        // Verify repository was never called
        verify(milestoneRepository, never()).delete(any());
    }
    
    @Test
    void testDeleteById() {
        // ✅ FIXED: Setup - Mock both existsById and deleteById as service calls both
        when(milestoneRepository.existsById(1L)).thenReturn(true);
        doNothing().when(milestoneRepository).deleteById(anyLong());
        
        // Execute
        boolean result = milestoneService.deleteById(1L);
        
        // Verify result
        assertTrue(result);
        
        // Verify repository calls in correct order
        verify(milestoneRepository).existsById(1L);
        verify(milestoneRepository).deleteById(1L);
    }
    
    @Test
    void testDeleteById_NotExists() {
        // ✅ FIXED: Setup - Entity doesn't exist
        when(milestoneRepository.existsById(999L)).thenReturn(false);
        
        // Execute
        boolean result = milestoneService.deleteById(999L);
        
        // Verify result
        assertFalse(result);
        
        // Verify repository calls
        verify(milestoneRepository).existsById(999L);
        verify(milestoneRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testDeleteById_NullId() {
        // Execute
        boolean result = milestoneService.deleteById(null);
        
        // Verify result
        assertFalse(result);
        
        // Verify repository was never called
        verify(milestoneRepository, never()).existsById(any());
        verify(milestoneRepository, never()).deleteById(any());
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
    void testFindByProject_NullProject() {
        // Execute and verify exception
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.findByProject(null);
        });
        
        // Verify repository was never called
        verify(milestoneRepository, never()).findByProject(any());
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
    void testFindByDateBefore_NullDate() {
        // Execute and verify exception
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.findByDateBefore(null);
        });
        
        // Verify repository was never called
        verify(milestoneRepository, never()).findByDateBefore(any());
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
    void testFindByDateAfter_NullDate() {
        // Execute and verify exception
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.findByDateAfter(null);
        });
        
        // Verify repository was never called
        verify(milestoneRepository, never()).findByDateAfter(any());
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
    void testFindByDateBetween_NullDates() {
        // Execute and verify exception
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.findByDateBetween(null, now.plusDays(10));
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.findByDateBetween(now, null);
        });
        
        // Verify repository was never called
        verify(milestoneRepository, never()).findByDateBetween(any(), any());
    }
    
    @Test
    void testFindByDateBetween_InvalidDateOrder() {
        // Execute and verify exception
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.findByDateBetween(now.plusDays(10), now.plusDays(5));
        });
        
        // Verify repository was never called
        verify(milestoneRepository, never()).findByDateBetween(any(), any());
    }
    
    @Test
    void testCreateMilestone() {
        // Setup
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(milestoneRepository.save(any(Milestone.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
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
        assertEquals(testProject, result.getProject());
        
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
    void testCreateMilestone_InvalidInputs() {
        // Test null name
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.createMilestone(null, now.plusDays(45), 1L, "Description");
        });
        
        // Test empty name
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.createMilestone("", now.plusDays(45), 1L, "Description");
        });
        
        // Test null date
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.createMilestone("Name", null, 1L, "Description");
        });
        
        // Test null project ID
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.createMilestone("Name", now.plusDays(45), null, "Description");
        });
        
        // Verify repository was never called
        verify(projectRepository, never()).findById(any());
        verify(milestoneRepository, never()).save(any());
    }
    
    @Test
    void testUpdateMilestoneDate() {
        // Setup
        LocalDate newDate = now.plusDays(50);
        when(milestoneRepository.findById(1L)).thenReturn(Optional.of(testMilestone));
        when(milestoneRepository.save(any(Milestone.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Milestone result = milestoneService.updateMilestoneDate(1L, newDate);
        
        // Verify
        assertNotNull(result);
        assertEquals(newDate, result.getDate());
        
        // Verify repository calls
        verify(milestoneRepository).findById(1L);
        verify(milestoneRepository).save(testMilestone);
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
    void testUpdateMilestoneDate_InvalidInputs() {
        // Test null milestone ID
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.updateMilestoneDate(null, now.plusDays(50));
        });
        
        // Test null date
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.updateMilestoneDate(1L, null);
        });
        
        // Verify repository was never called
        verify(milestoneRepository, never()).findById(any());
        verify(milestoneRepository, never()).save(any());
    }
    
    @Test
    void testUpdateDescription() {
        // Setup
        when(milestoneRepository.findById(1L)).thenReturn(Optional.of(testMilestone));
        when(milestoneRepository.save(any(Milestone.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Milestone result = milestoneService.updateDescription(1L, "Updated Description");
        
        // Verify
        assertNotNull(result);
        assertEquals("Updated Description", result.getDescription());
        
        // Verify repository calls
        verify(milestoneRepository).findById(1L);
        verify(milestoneRepository).save(testMilestone);
    }
    
    @Test
    void testUpdateDescription_NullDescription() {
        // Setup
        when(milestoneRepository.findById(1L)).thenReturn(Optional.of(testMilestone));
        when(milestoneRepository.save(any(Milestone.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Milestone result = milestoneService.updateDescription(1L, null);
        
        // Verify
        assertNotNull(result);
        assertNull(result.getDescription());
        
        // Verify repository calls
        verify(milestoneRepository).findById(1L);
        verify(milestoneRepository).save(testMilestone);
    }
    
    @Test
    void testUpdateDescription_InvalidMilestoneId() {
        // Test null milestone ID
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.updateDescription(null, "Description");
        });
        
        // Verify repository was never called
        verify(milestoneRepository, never()).findById(any());
        verify(milestoneRepository, never()).save(any());
    }
    
    @Test
    void testGetUpcomingMilestones() {
        // Setup
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(10);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(milestoneRepository.findUpcomingMilestones(testProject, today, endDate))
            .thenReturn(List.of(testMilestone));
        
        // Execute
        List<Milestone> results = milestoneService.getUpcomingMilestones(1L, 10);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testMilestone, results.get(0));
        
        // Verify repository calls
        verify(projectRepository).findById(1L);
        verify(milestoneRepository).findUpcomingMilestones(eq(testProject), any(LocalDate.class), any(LocalDate.class));
    }
    
    @Test
    void testGetUpcomingMilestones_ProjectNotFound() {
        // Setup
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.getUpcomingMilestones(999L, 10);
        });
        
        // Verify exception message
        assertTrue(exception.getMessage().contains("Project not found"));
        
        // Verify repository calls
        verify(projectRepository).findById(999L);
        verify(milestoneRepository, never()).findUpcomingMilestones(any(), any(), any());
    }
    
    @Test
    void testGetUpcomingMilestones_InvalidInputs() {
        // Test null project ID
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.getUpcomingMilestones(null, 10);
        });
        
        // Test negative days
        assertThrows(IllegalArgumentException.class, () -> {
            milestoneService.getUpcomingMilestones(1L, -5);
        });
        
        // Verify repository was never called
        verify(projectRepository, never()).findById(any());
        verify(milestoneRepository, never()).findUpcomingMilestones(any(), any(), any());
    }
}