// src/test/java/org/frcpm/services/impl/SubteamServiceTest.java

package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
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
 * Test class for SubteamService implementation using Spring Boot testing patterns.
 */
@ExtendWith(MockitoExtension.class)
class SubteamServiceTest {
    
    @Mock
    private SubteamRepository subteamRepository;
    
    private SubteamServiceImpl subteamService;
    
    private Subteam testSubteam;
    
    @BeforeEach
    void setUp() {
        // Initialize test objects
        testSubteam = createTestSubteam();
        
        // Configure mock repository responses
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        when(subteamRepository.findAll()).thenReturn(List.of(testSubteam));
        when(subteamRepository.save(any(Subteam.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Create service with injected mocks
        subteamService = new SubteamServiceImpl(subteamRepository);
    }
    
    /**
     * Creates a test subteam for use in tests.
     */
    private Subteam createTestSubteam() {
        Subteam subteam = new Subteam();
        subteam.setId(1L);
        subteam.setName("Test Subteam");
        subteam.setColorCode("#FF5733");
        subteam.setSpecialties("Programming, Electronics");
        return subteam;
    }
    
    @Test
    void testFindById() {
        // Reset mocks to ensure clean test state
        reset(subteamRepository);
        
        // Setup - create a special subteam for this test
        Subteam uniqueSubteam = new Subteam();
        uniqueSubteam.setId(1L);
        uniqueSubteam.setName("Unique Subteam");
        uniqueSubteam.setColorCode("#00FF00");
        
        // Configure fresh mock behavior for this test
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(uniqueSubteam));
        
        // Execute
        Subteam result = subteamService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Subteam ID should match");
        assertEquals("Unique Subteam", result.getName(), "Subteam name should match exactly");
        
        // Verify repository was called exactly once with the correct ID
        verify(subteamRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindAll() {
        // Execute
        List<Subteam> results = subteamService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
        
        // Verify repository was called
        verify(subteamRepository).findAll();
    }
    
    @Test
    void testSave() {
        // Setup
        Subteam newSubteam = new Subteam();
        newSubteam.setName("New Subteam");
        newSubteam.setColorCode("#0000FF");
        
        // Execute
        Subteam result = subteamService.save(newSubteam);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Subteam", result.getName());
        
        // Verify repository was called
        verify(subteamRepository).save(newSubteam);
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(subteamRepository).delete(any(Subteam.class));
        
        // Execute
        subteamService.delete(testSubteam);
        
        // Verify repository was called
        verify(subteamRepository).delete(testSubteam);
    }
    
    @Test
    void testDeleteById() {
        // Setup
        when(subteamRepository.deleteById(anyLong())).thenReturn(true);
        
        // Execute
        boolean result = subteamService.deleteById(1L);
        
        // Verify
        assertTrue(result);
        
        // Verify repository was called
        verify(subteamRepository).deleteById(1L);
    }
    
    @Test
    void testCount() {
        // Setup
        when(subteamRepository.count()).thenReturn(5L);
        
        // Execute
        long result = subteamService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository was called
        verify(subteamRepository).count();
    }
    
    @Test
    void testFindByName() {
        // Setup
        when(subteamRepository.findByName("Test Subteam")).thenReturn(Optional.of(testSubteam));
        
        // Execute
        Optional<Subteam> result = subteamService.findByName("Test Subteam");
        
        // Verify
        assertTrue(result.isPresent());
        assertEquals("Test Subteam", result.get().getName());
        
        // Verify repository was called
        verify(subteamRepository).findByName("Test Subteam");
    }
    
    @Test
    void testFindByName_NotFound() {
        // Setup
        when(subteamRepository.findByName("Nonexistent Subteam")).thenReturn(Optional.empty());
        
        // Execute
        Optional<Subteam> result = subteamService.findByName("Nonexistent Subteam");
        
        // Verify
        assertFalse(result.isPresent());
        
        // Verify repository was called
        verify(subteamRepository).findByName("Nonexistent Subteam");
    }
    
    @Test
    void testFindBySpecialty() {
        // Setup
        when(subteamRepository.findBySpecialtiesContainingIgnoreCase("Programming")).thenReturn(List.of(testSubteam));
        
        // Execute
        List<Subteam> results = subteamService.findBySpecialty("Programming");
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testSubteam, results.get(0));
        
        // Verify repository was called
        verify(subteamRepository).findBySpecialtiesContainingIgnoreCase("Programming");
    }
    
    @Test
    void testCreateSubteam() {
        // Setup
        when(subteamRepository.findByName("New Subteam")).thenReturn(Optional.empty());
        
        // Execute
        Subteam result = subteamService.createSubteam(
            "New Subteam",
            "#00FF00",
            "Design, Mechanical"
        );
        
        // Verify
        assertNotNull(result);
        assertEquals("New Subteam", result.getName());
        assertEquals("#00FF00", result.getColorCode());
        assertEquals("Design, Mechanical", result.getSpecialties());
        
        // Verify repository calls
        verify(subteamRepository).findByName("New Subteam");
        verify(subteamRepository).save(any(Subteam.class));
    }
    
    @Test
    void testCreateSubteam_NameExists() {
        // Setup
        when(subteamRepository.findByName("Test Subteam")).thenReturn(Optional.of(testSubteam));
        
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subteamService.createSubteam(
                "Test Subteam",
                "#00FF00",
                "Design, Mechanical"
            );
        });
        
        // Verify exception message
        assertEquals("Subteam with name 'Test Subteam' already exists", exception.getMessage());
        
        // Verify repository calls
        verify(subteamRepository).findByName("Test Subteam");
        verify(subteamRepository, never()).save(any(Subteam.class));
    }
    
    @Test
    void testCreateSubteam_InvalidColorCode() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subteamService.createSubteam(
                "New Subteam",
                "invalid-color", // Invalid color code
                "Design, Mechanical"
            );
        });
        
        // Verify exception message
        assertEquals("Color code must be a valid hex color code", exception.getMessage());
        
        // Verify repository calls
        verify(subteamRepository, never()).findByName(anyString());
        verify(subteamRepository, never()).save(any(Subteam.class));
    }
    
    @Test
    void testUpdateSpecialties() {
        // Execute
        Subteam result = subteamService.updateSpecialties(1L, "Updated Skills");
        
        // Verify
        assertNotNull(result);
        assertEquals("Updated Skills", result.getSpecialties());
        
        // Verify repository calls
        verify(subteamRepository).findById(1L);
        verify(subteamRepository).save(any(Subteam.class));
    }
    
    @Test
    void testUpdateSpecialties_SubteamNotFound() {
        // Setup
        when(subteamRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Subteam result = subteamService.updateSpecialties(999L, "Updated Skills");
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(subteamRepository).findById(999L);
        verify(subteamRepository, never()).save(any(Subteam.class));
    }
    
    @Test
    void testUpdateColorCode() {
        // Execute
        Subteam result = subteamService.updateColorCode(1L, "#00FF00");
        
        // Verify
        assertNotNull(result);
        assertEquals("#00FF00", result.getColorCode());
        
        // Verify repository calls
        verify(subteamRepository).findById(1L);
        verify(subteamRepository).save(any(Subteam.class));
    }
    
    @Test
    void testUpdateColorCode_SubteamNotFound() {
        // Setup
        when(subteamRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Subteam result = subteamService.updateColorCode(999L, "#00FF00");
        
        // Verify
        assertNull(result);
        
        // Verify repository calls
        verify(subteamRepository).findById(999L);
        verify(subteamRepository, never()).save(any(Subteam.class));
    }
    
    @Test
    void testUpdateColorCode_InvalidColorCode() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subteamService.updateColorCode(1L, "invalid-color");
        });
        
        // Verify exception message
        assertEquals("Color code must be a valid hex color code", exception.getMessage());
        
        // Verify repository calls
        verify(subteamRepository, never()).findById(anyLong());
        verify(subteamRepository, never()).save(any(Subteam.class));
    }
}