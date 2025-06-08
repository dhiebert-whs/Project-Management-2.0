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
 */
@ExtendWith(MockitoExtension.class)
class SubsystemServiceTest {
    
    @Mock
    private SubsystemRepository subsystemRepository;
    
    @Mock
    private SubteamRepository subteamRepository;
    
    private SubsystemServiceImpl subsystemService;
    
    private Subsystem testSubsystem;
    private Subteam testSubteam;
    
    @BeforeEach
    void setUp() {
        // Initialize test objects
        testSubteam = createTestSubteam();
        testSubsystem = createTestSubsystem();
        
        // Configure mock repository responses
        when(subsystemRepository.findById(1L)).thenReturn(Optional.of(testSubsystem));
        when(subsystemRepository.findAll()).thenReturn(List.of(testSubsystem));
        when(subsystemRepository.save(any(Subsystem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Configure subteam repository
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        
        // Create service with injected mocks
        subsystemService = new SubsystemServiceImpl(
            subsystemRepository,
            subteamRepository
        );
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
        // Reset mocks to ensure clean test state
        reset(subsystemRepository);
        
        // Setup - create a special subsystem for this test
        Subsystem uniqueSubsystem = new Subsystem("Unique Subsystem");
        uniqueSubsystem.setId(1L);
        
        // Configure fresh mock behavior for this test
        when(subsystemRepository.findById(1L)).thenReturn(Optional.of(uniqueSubsystem));
        
        // Execute
        Subsystem result = subsystemService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Subsystem ID should match");
        assertEquals("Unique Subsystem", result.getName(), "Subsystem name should match exactly");
        
        // Verify repository was called exactly once with the correct ID
        verify(subsystemRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindAll() {
        // Execute
        List<Subsystem> results = subsystemService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
        
        // Verify repository was called
        verify(subsystemRepository).findAll();
    }
    
    @Test
    void testSave() {
        // Setup
        Subsystem newSubsystem = new Subsystem("New Subsystem");
        
        // Execute
        Subsystem result = subsystemService.save(newSubsystem);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Subsystem", result.getName());
        
        // Verify repository was called
        verify(subsystemRepository).save(newSubsystem);
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(subsystemRepository).delete(any(Subsystem.class));
        
        // Execute
        subsystemService.delete(testSubsystem);
        
        // Verify repository was called
        verify(subsystemRepository).delete(testSubsystem);
    }
    
    @Test
    void testDeleteById() {
        // Setup
        when(subsystemRepository.deleteById(anyLong())).thenReturn(true);
        
        // Execute
        boolean result = subsystemService.deleteById(1L);
        
        // Verify
        assertTrue(result);
        
        // Verify repository was called
        verify(subsystemRepository).deleteById(1L);
    }
    
    @Test
    void testCount() {
        // Setup
        when(subsystemRepository.count()).thenReturn(5L);
        
        // Execute
        long result = subsystemService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository was called
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
        
        // Verify repository was called
        verify(subsystemRepository).findByName("Test Subsystem");
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
        
        // Verify repository was called
        verify(subsystemRepository).findByStatus(Subsystem.Status.NOT_STARTED);
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
        
        // Verify repository was called
        verify(subsystemRepository).findByResponsibleSubteam(testSubteam);
    }
    
    @Test
    void testCreateSubsystem() {
        // Setup
        when(subsystemRepository.findByName("New Subsystem")).thenReturn(Optional.empty());
        
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
        
        // Verify repository calls
        verify(subsystemRepository).findByName("New Subsystem");
        verify(subteamRepository).findById(1L);
        verify(subsystemRepository).save(any(Subsystem.class));
    }
    
    @Test
    void testCreateSubsystem_NameExists() {
        // Setup
        when(subsystemRepository.findByName("Test Subsystem")).thenReturn(Optional.of(testSubsystem));
        
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
        
        // Verify repository calls
        verify(subsystemRepository).findByName("Test Subsystem");
        verify(subsystemRepository, never()).save(any(Subsystem.class));
    }
    
    @Test
    void testUpdateStatus() {
        // Execute
        Subsystem result = subsystemService.updateStatus(1L, Subsystem.Status.COMPLETED);
        
        // Verify
        assertNotNull(result);
        assertEquals(Subsystem.Status.COMPLETED, result.getStatus());
        
        // Verify repository calls
        verify(subsystemRepository).findById(1L);
        verify(subsystemRepository).save(any(Subsystem.class));
    }
    
    @Test
    void testUpdateStatus_SubsystemNotFound() {
        // Setup
        when(subsystemRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Subsystem result = subsystemService.updateStatus(999L, Subsystem.Status.COMPLETED);
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(subsystemRepository).findById(999L);
        verify(subsystemRepository, never()).save(any(Subsystem.class));
    }
    
    @Test
    void testAssignResponsibleSubteam() {
        // Execute
        Subsystem result = subsystemService.assignResponsibleSubteam(1L, 1L);
        
        // Verify
        assertNotNull(result);
        assertNotNull(result.getResponsibleSubteam());
        assertEquals(1L, result.getResponsibleSubteam().getId());
        
        // Verify repository calls
        verify(subsystemRepository).findById(1L);
        verify(subteamRepository).findById(1L);
        verify(subsystemRepository).save(any(Subsystem.class));
    }
    
    @Test
    void testAssignResponsibleSubteam_SubsystemNotFound() {
        // Setup
        when(subsystemRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Subsystem result = subsystemService.assignResponsibleSubteam(999L, 1L);
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(subsystemRepository).findById(999L);
        verify(subteamRepository, never()).findById(anyLong());
        verify(subsystemRepository, never()).save(any(Subsystem.class));
    }
    
    @Test
    void testAssignResponsibleSubteam_SubteamNotFound() {
        // Setup
        when(subteamRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Subsystem result = subsystemService.assignResponsibleSubteam(1L, 999L);
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(subsystemRepository).findById(1L);
        verify(subteamRepository).findById(999L);
        verify(subsystemRepository, never()).save(any(Subsystem.class));
    }
}