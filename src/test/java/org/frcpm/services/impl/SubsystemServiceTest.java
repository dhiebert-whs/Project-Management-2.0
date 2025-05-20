// src/test/java/org/frcpm/services/impl/SubsystemServiceTest.java

package org.frcpm.services.impl;

import de.saxsys.mvvmfx.MvvmFX;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.specific.SubsystemRepository;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.services.BaseServiceTest;
import org.frcpm.services.SubsystemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for SubsystemService implementation using TestableSubsystemServiceImpl.
 */
public class SubsystemServiceTest extends BaseServiceTest {
    
    @Mock
    private SubsystemRepository subsystemRepository;
    
    @Mock
    private SubteamRepository subteamRepository;
    
    private SubsystemService subsystemService;
    
    private Subsystem testSubsystem;
    private Subteam testSubteam;
    
    @Override
    protected void setupTestData() {
        // Initialize test objects
        testSubteam = createTestSubteam();
        testSubsystem = createTestSubsystem();
    }
    
    @Override
    @BeforeEach
    public void setUp() {
        // Execute parent setup first (initializes Mockito annotations)
        super.setUp();
        
        // Configure mock repository responses
        when(subsystemRepository.findById(1L)).thenReturn(Optional.of(testSubsystem));
        when(subsystemRepository.findAll()).thenReturn(List.of(testSubsystem));
        when(subsystemRepository.save(any(Subsystem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Configure subteam repository
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        
        // Create service with injected mocks
        subsystemService = new TestableSubsystemServiceImpl(
            subsystemRepository,
            subteamRepository
        );
        
        // Configure MVVMFx dependency injector for comprehensive testing
        MvvmFX.setCustomDependencyInjector(type -> {
            if (type == SubsystemRepository.class) return subsystemRepository;
            if (type == SubteamRepository.class) return subteamRepository;
            if (type == SubsystemService.class) return subsystemService;
            return null;
        });
    }
    
    @AfterEach
    @Override
    public void tearDown() throws Exception {
        // Clear MVVMFx dependency injector
        MvvmFX.setCustomDependencyInjector(null);
        
        // Call parent tearDown
        super.tearDown();
    }
    
    /**
     * Creates a test subteam for use in tests.
     * 
     * @return a test subteam
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
     * 
     * @return a test subsystem
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
    public void testFindById() {
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
    public void testFindAll() {
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
    public void testSave() {
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
    public void testDelete() {
        // Setup
        doNothing().when(subsystemRepository).delete(any(Subsystem.class));
        
        // Execute
        subsystemService.delete(testSubsystem);
        
        // Verify repository was called
        verify(subsystemRepository).delete(testSubsystem);
    }
    
    @Test
    public void testDeleteById() {
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
    public void testCount() {
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
    public void testFindByName() {
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
    public void testFindByName_NotFound() {
        // Setup
        when(subsystemRepository.findByName("Nonexistent")).thenReturn(Optional.empty());
        
        // Execute
        Optional<Subsystem> result = subsystemService.findByName("Nonexistent");
        
        // Verify
        assertFalse(result.isPresent());
        
        // Verify repository was called
        verify(subsystemRepository).findByName("Nonexistent");
    }
    
    @Test
    public void testFindByStatus() {
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
    public void testFindByResponsibleSubteam() {
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
    public void testCreateSubsystem() {
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
    public void testCreateSubsystem_NoSubteam() {
        // Setup
        when(subsystemRepository.findByName("New Subsystem")).thenReturn(Optional.empty());
        
        // Execute
        Subsystem result = subsystemService.createSubsystem(
            "New Subsystem",
            "New Description",
            Subsystem.Status.IN_PROGRESS,
            null
        );
        
        // Verify
        assertNotNull(result);
        assertEquals("New Subsystem", result.getName());
        assertEquals("New Description", result.getDescription());
        assertEquals(Subsystem.Status.IN_PROGRESS, result.getStatus());
        assertNull(result.getResponsibleSubteam());
        
        // Verify repository calls
        verify(subsystemRepository).findByName("New Subsystem");
        verify(subteamRepository, never()).findById(anyLong());
        verify(subsystemRepository).save(any(Subsystem.class));
    }
    
    @Test
    public void testCreateSubsystem_NameExists() {
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
    public void testCreateSubsystem_NameExists_TestEnvironment() {
        // Setup
        System.setProperty("test.environment", "true");
        when(subsystemRepository.findByName("Test Subsystem")).thenReturn(Optional.of(testSubsystem));
        
        try {
            // Execute
            Subsystem result = subsystemService.createSubsystem(
                "Test Subsystem",
                "Updated Description",
                Subsystem.Status.IN_PROGRESS,
                1L
            );
            
            // Verify
            assertNotNull(result);
            assertEquals("Test Subsystem", result.getName());
            assertEquals("Updated Description", result.getDescription());
            assertEquals(Subsystem.Status.IN_PROGRESS, result.getStatus());
            assertNotNull(result.getResponsibleSubteam());
            
            // Verify repository calls
            verify(subsystemRepository).findByName("Test Subsystem");
            verify(subsystemRepository).save(any(Subsystem.class));
        } finally {
            // Clean up
            System.clearProperty("test.environment");
        }
    }
    
    @Test
    public void testCreateSubsystem_SubteamNotFound() {
        // Setup
        when(subsystemRepository.findByName("New Subsystem")).thenReturn(Optional.empty());
        when(subteamRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Subsystem result = subsystemService.createSubsystem(
            "New Subsystem",
            "New Description",
            Subsystem.Status.IN_PROGRESS,
            999L
        );
        
        // Verify
        assertNotNull(result);
        assertEquals("New Subsystem", result.getName());
        assertNull(result.getResponsibleSubteam());
        
        // Verify repository calls
        verify(subsystemRepository).findByName("New Subsystem");
        verify(subteamRepository).findById(999L);
        verify(subsystemRepository).save(any(Subsystem.class));
    }
    
    @Test
    public void testUpdateStatus() {
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
    public void testUpdateStatus_SubsystemNotFound() {
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
    public void testAssignResponsibleSubteam() {
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
    public void testAssignResponsibleSubteam_RemoveSubteam() {
        // Execute
        Subsystem result = subsystemService.assignResponsibleSubteam(1L, null);
        
        // Verify
        assertNotNull(result);
        assertNull(result.getResponsibleSubteam());
        
        // Verify repository calls
        verify(subsystemRepository).findById(1L);
        verify(subteamRepository, never()).findById(anyLong());
        verify(subsystemRepository).save(any(Subsystem.class));
    }
    
    @Test
    public void testAssignResponsibleSubteam_SubsystemNotFound() {
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
    public void testAssignResponsibleSubteam_SubteamNotFound() {
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