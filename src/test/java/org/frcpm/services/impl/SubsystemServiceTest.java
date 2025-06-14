// src/test/java/org/frcpm/services/impl/SubsystemServiceTest.java

package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.spring.SubsystemRepository;
import org.frcpm.repositories.spring.SubteamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for SubsystemService implementation using Spring Boot testing patterns.
 * ✅ PROVEN PATTERN APPLIED: Following AttendanceServiceTest template for 100% success rate.
 */
@ExtendWith(MockitoExtension.class)
class SubsystemServiceTest {
    
    @Mock
    private SubsystemRepository subsystemRepository;
    
    @Mock
    private SubteamRepository subteamRepository;
    
    private SubsystemServiceImpl subsystemService; // ✅ Use implementation class, not interface
    
    private Subsystem testSubsystem;
    private Subteam testSubteam;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - NO mock stubbing here
        testSubteam = createTestSubteam();
        testSubsystem = createTestSubsystem();
        
        // Create service with injected mocks
        subsystemService = new SubsystemServiceImpl(subsystemRepository, subteamRepository);
        
        // ✅ NO mock stubbing in setUp() - move to individual test methods
    }
    
    /**
     * Creates a test subteam for use in tests.
     */
    private Subteam createTestSubteam() {
        Subteam subteam = new Subteam();
        subteam.setId(1L);
        subteam.setName("Test Subteam");
        subteam.setColorCode("#FF5733");
        return subteam;
    }
    
    /**
     * Creates a test subsystem for use in tests.
     */
    private Subsystem createTestSubsystem() {
        Subsystem subsystem = new Subsystem("Test Subsystem");
        subsystem.setId(1L);
        subsystem.setDescription("Test Description");
        subsystem.setStatus(Subsystem.Status.NOT_STARTED);
        subsystem.setResponsibleSubteam(testSubteam);
        return subsystem;
    }
    
    @Test
    void testFindById() {
        // Setup - Only stub what THIS test needs
        when(subsystemRepository.findById(1L)).thenReturn(Optional.of(testSubsystem));
        
        // Execute
        Subsystem result = subsystemService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Subsystem ID should match");
        assertEquals("Test Subsystem", result.getName(), "Subsystem name should match");
        
        // Verify repository interaction
        verify(subsystemRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindById_NotFound() {
        // Setup
        when(subsystemRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Subsystem result = subsystemService.findById(999L);
        
        // Verify
        assertNull(result, "Result should be null for non-existent ID");
        
        // Verify repository interaction
        verify(subsystemRepository).findById(999L);
    }
    
    @Test
    void testFindById_NullParameter() {
        // Execute
        Subsystem result = subsystemService.findById(null);
        
        // Verify
        assertNull(result, "Result should be null for null ID");
        
        // Verify repository was NOT called for null parameter
        verify(subsystemRepository, never()).findById(any());
    }
    
    @Test
    void testFindAll() {
        // Setup
        when(subsystemRepository.findAll()).thenReturn(List.of(testSubsystem));
        
        // Execute
        List<Subsystem> results = subsystemService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testSubsystem, results.get(0));
        
        // Verify repository interaction
        verify(subsystemRepository).findAll();
    }
    
    @Test
    void testSave() {
        // Setup
        Subsystem newSubsystem = new Subsystem("New Subsystem");
        when(subsystemRepository.save(newSubsystem)).thenReturn(newSubsystem);
        
        // Execute
        Subsystem result = subsystemService.save(newSubsystem);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Subsystem", result.getName());
        
        // Verify repository interaction
        verify(subsystemRepository).save(newSubsystem);
    }
    
    @Test
    void testSave_NullParameter() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subsystemService.save(null);
        });
        
        // Verify exception message
        assertEquals("Subsystem cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(subsystemRepository, never()).save(any());
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(subsystemRepository).delete(testSubsystem);
        
        // Execute
        subsystemService.delete(testSubsystem);
        
        // Verify repository interaction
        verify(subsystemRepository).delete(testSubsystem);
    }
    
    @Test
    void testDelete_NullParameter() {
        // Execute (should not throw exception)
        subsystemService.delete(null);
        
        // Verify repository was NOT called for null parameter
        verify(subsystemRepository, never()).delete(any());
    }
    
    @Test
    void testDeleteById() {
        // Setup - Configure existsById and deleteById behavior
        when(subsystemRepository.existsById(1L)).thenReturn(true);
        doNothing().when(subsystemRepository).deleteById(1L);
        
        // Execute
        boolean result = subsystemService.deleteById(1L);
        
        // Verify
        assertTrue(result, "Delete should return true for existing entity");
        
        // Verify repository interactions in correct order
        verify(subsystemRepository).existsById(1L);
        verify(subsystemRepository).deleteById(1L);
    }
    
    @Test
    void testDeleteById_NotFound() {
        // Setup
        when(subsystemRepository.existsById(999L)).thenReturn(false);
        
        // Execute
        boolean result = subsystemService.deleteById(999L);
        
        // Verify
        assertFalse(result, "Delete should return false for non-existent entity");
        
        // Verify repository interactions
        verify(subsystemRepository).existsById(999L);
        verify(subsystemRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testDeleteById_NullParameter() {
        // Execute
        boolean result = subsystemService.deleteById(null);
        
        // Verify
        assertFalse(result, "Delete should return false for null ID");
        
        // Verify repository was NOT called for null parameter
        verify(subsystemRepository, never()).existsById(any());
        verify(subsystemRepository, never()).deleteById(any());
    }
    
    @Test
    void testCount() {
        // Setup
        when(subsystemRepository.count()).thenReturn(5L);
        
        // Execute
        long result = subsystemService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository interaction
        verify(subsystemRepository).count();
    }
    
    @Test
    void testFindByName() {
        // Setup
        when(subsystemRepository.findByName("Test Subsystem")).thenReturn(Optional.of(testSubsystem));
        
        // Execute
        Optional<Subsystem> result = subsystemService.findByName("Test Subsystem");
        
        // Verify
        assertTrue(result.isPresent());
        assertEquals("Test Subsystem", result.get().getName());
        
        // Verify repository interaction
        verify(subsystemRepository).findByName("Test Subsystem");
    }
    
    @Test
    void testFindByName_NotFound() {
        // Setup
        when(subsystemRepository.findByName("Nonexistent")).thenReturn(Optional.empty());
        
        // Execute
        Optional<Subsystem> result = subsystemService.findByName("Nonexistent");
        
        // Verify
        assertFalse(result.isPresent());
        
        // Verify repository interaction
        verify(subsystemRepository).findByName("Nonexistent");
    }
    
    @Test
    void testFindByName_NullParameter() {
        // Execute
        Optional<Subsystem> result = subsystemService.findByName(null);
        
        // Verify
        assertFalse(result.isPresent(), "Should return empty Optional for null name");
        
        // Verify repository was NOT called for null parameter
        verify(subsystemRepository, never()).findByName(any());
    }
    
    @Test
    void testFindByName_EmptyParameter() {
        // Execute
        Optional<Subsystem> result = subsystemService.findByName("  ");
        
        // Verify
        assertFalse(result.isPresent(), "Should return empty Optional for empty name");
        
        // Verify repository was NOT called for empty parameter
        verify(subsystemRepository, never()).findByName(any());
    }
    
    @Test
    void testFindByStatus() {
        // Setup
        when(subsystemRepository.findByStatus(Subsystem.Status.NOT_STARTED)).thenReturn(List.of(testSubsystem));
        
        // Execute
        List<Subsystem> results = subsystemService.findByStatus(Subsystem.Status.NOT_STARTED);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testSubsystem, results.get(0));
        
        // Verify repository interaction
        verify(subsystemRepository).findByStatus(Subsystem.Status.NOT_STARTED);
    }
    
    @Test
    void testFindByStatus_NullParameter() {
        // Execute
        List<Subsystem> results = subsystemService.findByStatus(null);
        
        // Verify
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list for null status");
        
        // Verify repository was NOT called for null parameter
        verify(subsystemRepository, never()).findByStatus(any());
    }
    
    @Test
    void testFindByResponsibleSubteam() {
        // Setup
        when(subsystemRepository.findByResponsibleSubteam(testSubteam)).thenReturn(List.of(testSubsystem));
        
        // Execute
        List<Subsystem> results = subsystemService.findByResponsibleSubteam(testSubteam);
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testSubsystem, results.get(0));
        
        // Verify repository interaction
        verify(subsystemRepository).findByResponsibleSubteam(testSubteam);
    }
    
    @Test
    void testFindByResponsibleSubteam_NullParameter() {
        // Execute
        List<Subsystem> results = subsystemService.findByResponsibleSubteam(null);
        
        // Verify
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list for null subteam");
        
        // Verify repository was NOT called for null parameter
        verify(subsystemRepository, never()).findByResponsibleSubteam(any());
    }
    
    @Test
    void testCreateSubsystem() {
        // Setup
        when(subsystemRepository.existsByNameIgnoreCase("New Subsystem")).thenReturn(false);
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        when(subsystemRepository.save(any(Subsystem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Subsystem result = subsystemService.createSubsystem(
            "New Subsystem",
            "New Description",
            Subsystem.Status.IN_PROGRESS,
            1L
        );
        
        // Verify
        assertNotNull(result);
        assertEquals("New Subsystem", result.getName());
        assertEquals("New Description", result.getDescription());
        assertEquals(Subsystem.Status.IN_PROGRESS, result.getStatus());
        assertNotNull(result.getResponsibleSubteam());
        assertEquals(1L, result.getResponsibleSubteam().getId());
        
        // Verify repository interactions
        verify(subsystemRepository).existsByNameIgnoreCase("New Subsystem");
        verify(subteamRepository).findById(1L);
        verify(subsystemRepository).save(any(Subsystem.class));
    }
    
    @Test
    void testCreateSubsystem_NameExists() {
        // Setup
        when(subsystemRepository.existsByNameIgnoreCase("Test Subsystem")).thenReturn(true);
        
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subsystemService.createSubsystem(
                "Test Subsystem",
                "Updated Description",
                Subsystem.Status.IN_PROGRESS,
                1L
            );
        });
        
        // Verify exception message
        assertEquals("Subsystem with name 'Test Subsystem' already exists", exception.getMessage());
        
        // Verify repository interactions
        verify(subsystemRepository).existsByNameIgnoreCase("Test Subsystem");
        verify(subsystemRepository, never()).save(any(Subsystem.class));
    }
    
    @Test
    void testCreateSubsystem_NullName() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subsystemService.createSubsystem(null, "Description", Subsystem.Status.NOT_STARTED, null);
        });
        
        // Verify exception message
        assertEquals("Subsystem name cannot be null or empty", exception.getMessage());
        
        // Verify repository was NOT called
        verify(subsystemRepository, never()).existsByNameIgnoreCase(any());
        verify(subsystemRepository, never()).save(any());
    }
    
    @Test
    void testCreateSubsystem_NullStatus() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subsystemService.createSubsystem("Test Name", "Description", null, null);
        });
        
        // Verify exception message
        assertEquals("Subsystem status cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(subsystemRepository, never()).existsByNameIgnoreCase(any());
        verify(subsystemRepository, never()).save(any());
    }
    
    @Test
    void testCreateSubsystem_SubteamNotFound() {
        // Setup
        when(subsystemRepository.existsByNameIgnoreCase("New Subsystem")).thenReturn(false);
        when(subteamRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subsystemService.createSubsystem(
                "New Subsystem",
                "Description",
                Subsystem.Status.NOT_STARTED,
                999L
            );
        });
        
        // Verify exception message
        assertEquals("Subteam with ID 999 not found", exception.getMessage());
        
        // Verify repository interactions
        verify(subsystemRepository).existsByNameIgnoreCase("New Subsystem");
        verify(subteamRepository).findById(999L);
        verify(subsystemRepository, never()).save(any());
    }
    
    @Test
    void testUpdateStatus() {
        // Setup
        when(subsystemRepository.findById(1L)).thenReturn(Optional.of(testSubsystem));
        when(subsystemRepository.save(any(Subsystem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Subsystem result = subsystemService.updateStatus(1L, Subsystem.Status.COMPLETED);
        
        // Verify
        assertNotNull(result);
        assertEquals(Subsystem.Status.COMPLETED, result.getStatus());
        
        // Verify repository interactions
        verify(subsystemRepository).findById(1L);
        verify(subsystemRepository).save(testSubsystem);
    }
    
    @Test
    void testUpdateStatus_SubsystemNotFound() {
        // Setup
        when(subsystemRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Subsystem result = subsystemService.updateStatus(999L, Subsystem.Status.COMPLETED);
        
        // Verify
        assertNull(result);
        
        // Verify repository interactions
        verify(subsystemRepository).findById(999L);
        verify(subsystemRepository, never()).save(any());
    }
    
    @Test
    void testUpdateStatus_NullParameters() {
        // Execute
        Subsystem result = subsystemService.updateStatus(null, Subsystem.Status.COMPLETED);
        
        // Verify
        assertNull(result);
        
        // Verify repository was NOT called
        verify(subsystemRepository, never()).findById(any());
        verify(subsystemRepository, never()).save(any());
    }
    
    @Test
    void testAssignResponsibleSubteam() {
        // Setup
        when(subsystemRepository.findById(1L)).thenReturn(Optional.of(testSubsystem));
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        when(subsystemRepository.save(any(Subsystem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Subsystem result = subsystemService.assignResponsibleSubteam(1L, 1L);
        
        // Verify
        assertNotNull(result);
        assertNotNull(result.getResponsibleSubteam());
        assertEquals(testSubteam, result.getResponsibleSubteam());
        
        // Verify repository interactions
        verify(subsystemRepository).findById(1L);
        verify(subteamRepository).findById(1L);
        verify(subsystemRepository).save(testSubsystem);
    }
    
    @Test
    void testAssignResponsibleSubteam_SubsystemNotFound() {
        // Setup
        when(subsystemRepository.findById(999L)).thenReturn(Optional.empty());
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        
        // Execute
        Subsystem result = subsystemService.assignResponsibleSubteam(999L, 1L);
        
        // Verify
        assertNull(result);
        
        // Verify repository interactions
        verify(subsystemRepository).findById(999L);
        verify(subteamRepository).findById(1L);
        verify(subsystemRepository, never()).save(any());
    }
    
    @Test
    void testAssignResponsibleSubteam_SubteamNotFound() {
        // Setup
        when(subsystemRepository.findById(1L)).thenReturn(Optional.of(testSubsystem));
        when(subteamRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Subsystem result = subsystemService.assignResponsibleSubteam(1L, 999L);
        
        // Verify
        assertNull(result);
        
        // Verify repository interactions
        verify(subsystemRepository).findById(1L);
        verify(subteamRepository).findById(999L);
        verify(subsystemRepository, never()).save(any());
    }
    
    @Test
    void testAssignResponsibleSubteam_NullParameters() {
        // Execute
        Subsystem result = subsystemService.assignResponsibleSubteam(null, 1L);
        
        // Verify
        assertNull(result);
        
        // Verify repository was NOT called
        verify(subsystemRepository, never()).findById(any());
        verify(subteamRepository, never()).findById(any());
        verify(subsystemRepository, never()).save(any());
    }
}