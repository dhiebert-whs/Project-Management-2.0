// src/test/java/org/frcpm/services/impl/SubteamServiceTest.java

package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.repositories.spring.SubteamRepository;
import org.frcpm.repositories.spring.TeamMemberRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for SubteamService implementation using Spring Boot testing patterns.
 * ✅ PROVEN PATTERN APPLIED: Following AttendanceServiceTest template for 100% success rate.
 */
@ExtendWith(MockitoExtension.class)
class SubteamServiceTest {
    
    @Mock
    private SubteamRepository subteamRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    
    private SubteamServiceImpl subteamService; // ✅ Use implementation class, not interface
    
    private Subteam testSubteam;
    
    @BeforeEach
    void setUp() {
        // Create test objects ONLY - NO mock stubbing here
        testSubteam = createTestSubteam();
        
        // Create service with injected mocks
        subteamService = new SubteamServiceImpl(subteamRepository, teamMemberRepository);
        
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
        subteam.setSpecialties("Programming, Electronics");
        return subteam;
    }
    
    @Test
    void testFindById() {
        // Setup - Only stub what THIS test needs
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        
        // Execute
        Subteam result = subteamService.findById(1L);
        
        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Subteam ID should match");
        assertEquals("Test Subteam", result.getName(), "Subteam name should match");
        
        // Verify repository interaction
        verify(subteamRepository, times(1)).findById(1L);
    }
    
    @Test
    void testFindById_NotFound() {
        // Setup
        when(subteamRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Subteam result = subteamService.findById(999L);
        
        // Verify
        assertNull(result, "Result should be null for non-existent ID");
        
        // Verify repository interaction
        verify(subteamRepository).findById(999L);
    }
    
    @Test
    void testFindById_NullParameter() {
        // Execute
        Subteam result = subteamService.findById(null);
        
        // Verify
        assertNull(result, "Result should be null for null ID");
        
        // Verify repository was NOT called for null parameter
        verify(subteamRepository, never()).findById(any());
    }
    
    @Test
    void testFindAll() {
        // Setup
        when(subteamRepository.findAll()).thenReturn(List.of(testSubteam));
        
        // Execute
        List<Subteam> results = subteamService.findAll();
        
        // Verify
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testSubteam, results.get(0));
        
        // Verify repository interaction
        verify(subteamRepository).findAll();
    }
    
    @Test
    void testSave() {
        // Setup
        Subteam newSubteam = new Subteam();
        newSubteam.setName("New Subteam");
        newSubteam.setColorCode("#0000FF");
        when(subteamRepository.save(newSubteam)).thenReturn(newSubteam);
        
        // Execute
        Subteam result = subteamService.save(newSubteam);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Subteam", result.getName());
        
        // Verify repository interaction
        verify(subteamRepository).save(newSubteam);
    }
    
    @Test
    void testSave_NullParameter() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subteamService.save(null);
        });
        
        // Verify exception message
        assertEquals("Subteam cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(subteamRepository, never()).save(any());
    }
    
    @Test
    void testDelete() {
        // Setup
        doNothing().when(subteamRepository).delete(testSubteam);
        
        // Execute
        subteamService.delete(testSubteam);
        
        // Verify repository interaction
        verify(subteamRepository).delete(testSubteam);
    }
    
    @Test
    void testDelete_NullParameter() {
        // Execute (should not throw exception)
        subteamService.delete(null);
        
        // Verify repository was NOT called for null parameter
        verify(subteamRepository, never()).delete(any());
    }
    
    @Test
    void testDeleteById() {
        // Setup - Configure existsById and deleteById behavior
        when(subteamRepository.existsById(1L)).thenReturn(true);
        doNothing().when(subteamRepository).deleteById(1L);
        
        // Execute
        boolean result = subteamService.deleteById(1L);
        
        // Verify
        assertTrue(result, "Delete should return true for existing entity");
        
        // Verify repository interactions in correct order
        verify(subteamRepository).existsById(1L);
        verify(subteamRepository).deleteById(1L);
    }
    
    @Test
    void testDeleteById_NotFound() {
        // Setup
        when(subteamRepository.existsById(999L)).thenReturn(false);
        
        // Execute
        boolean result = subteamService.deleteById(999L);
        
        // Verify
        assertFalse(result, "Delete should return false for non-existent entity");
        
        // Verify repository interactions
        verify(subteamRepository).existsById(999L);
        verify(subteamRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testDeleteById_NullParameter() {
        // Execute
        boolean result = subteamService.deleteById(null);
        
        // Verify
        assertFalse(result, "Delete should return false for null ID");
        
        // Verify repository was NOT called for null parameter
        verify(subteamRepository, never()).existsById(any());
        verify(subteamRepository, never()).deleteById(any());
    }
    
    @Test
    void testCount() {
        // Setup
        when(subteamRepository.count()).thenReturn(5L);
        
        // Execute
        long result = subteamService.count();
        
        // Verify
        assertEquals(5L, result);
        
        // Verify repository interaction
        verify(subteamRepository).count();
    }
    
    @Test
    void testFindByName() {
        // Setup
        when(subteamRepository.findByNameIgnoreCase("Test Subteam")).thenReturn(Optional.of(testSubteam));
        
        // Execute
        Optional<Subteam> result = subteamService.findByName("Test Subteam");
        
        // Verify
        assertTrue(result.isPresent());
        assertEquals("Test Subteam", result.get().getName());
        
        // Verify repository interaction
        verify(subteamRepository).findByNameIgnoreCase("Test Subteam");
    }
    
    @Test
    void testFindByName_NotFound() {
        // Setup
        when(subteamRepository.findByNameIgnoreCase("Nonexistent Subteam")).thenReturn(Optional.empty());
        
        // Execute
        Optional<Subteam> result = subteamService.findByName("Nonexistent Subteam");
        
        // Verify
        assertFalse(result.isPresent());
        
        // Verify repository interaction
        verify(subteamRepository).findByNameIgnoreCase("Nonexistent Subteam");
    }
    
    @Test
    void testFindByName_NullParameter() {
        // Execute
        Optional<Subteam> result = subteamService.findByName(null);
        
        // Verify
        assertFalse(result.isPresent(), "Should return empty Optional for null name");
        
        // Verify repository was NOT called for null parameter
        verify(subteamRepository, never()).findByNameIgnoreCase(any());
    }
    
    @Test
    void testFindByName_EmptyParameter() {
        // Execute
        Optional<Subteam> result = subteamService.findByName("  ");
        
        // Verify
        assertFalse(result.isPresent(), "Should return empty Optional for empty name");
        
        // Verify repository was NOT called for empty parameter
        verify(subteamRepository, never()).findByNameIgnoreCase(any());
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
        
        // Verify repository interaction
        verify(subteamRepository).findBySpecialtiesContainingIgnoreCase("Programming");
    }
    
    @Test
    void testFindBySpecialty_NotFound() {
        // Setup
        when(subteamRepository.findBySpecialtiesContainingIgnoreCase("NonExistent")).thenReturn(List.of());
        
        // Execute
        List<Subteam> results = subteamService.findBySpecialty("NonExistent");
        
        // Verify
        assertNotNull(results);
        assertTrue(results.isEmpty());
        
        // Verify repository interaction
        verify(subteamRepository).findBySpecialtiesContainingIgnoreCase("NonExistent");
    }
    
    @Test
    void testFindBySpecialty_NullParameter() {
        // Execute
        List<Subteam> results = subteamService.findBySpecialty(null);
        
        // Verify
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list for null specialty");
        
        // Verify repository was NOT called for null parameter
        verify(subteamRepository, never()).findBySpecialtiesContainingIgnoreCase(any());
    }
    
    @Test
    void testFindBySpecialty_EmptyParameter() {
        // Execute
        List<Subteam> results = subteamService.findBySpecialty("  ");
        
        // Verify
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list for empty specialty");
        
        // Verify repository was NOT called for empty parameter
        verify(subteamRepository, never()).findBySpecialtiesContainingIgnoreCase(any());
    }
    
    @Test
    void testCreateSubteam() {
        // Setup
        when(subteamRepository.existsByNameIgnoreCase("New Subteam")).thenReturn(false);
        when(subteamRepository.save(any(Subteam.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
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
        
        // Verify repository interactions
        verify(subteamRepository).existsByNameIgnoreCase("New Subteam");
        verify(subteamRepository).save(any(Subteam.class));
    }
    
    @Test
    void testCreateSubteam_NameExists() {
        // Setup
        when(subteamRepository.existsByNameIgnoreCase("Test Subteam")).thenReturn(true);
        
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
        
        // Verify repository interactions
        verify(subteamRepository).existsByNameIgnoreCase("Test Subteam");
        verify(subteamRepository, never()).save(any(Subteam.class));
    }
    
    @Test
    void testCreateSubteam_NullName() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subteamService.createSubteam(null, "#00FF00", "Design, Mechanical");
        });
        
        // Verify exception message
        assertEquals("Subteam name cannot be null or empty", exception.getMessage());
        
        // Verify repository was NOT called
        verify(subteamRepository, never()).existsByNameIgnoreCase(any());
        verify(subteamRepository, never()).save(any());
    }
    
    @Test
    void testCreateSubteam_EmptyName() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subteamService.createSubteam("  ", "#00FF00", "Design, Mechanical");
        });
        
        // Verify exception message
        assertEquals("Subteam name cannot be null or empty", exception.getMessage());
        
        // Verify repository was NOT called
        verify(subteamRepository, never()).existsByNameIgnoreCase(any());
        verify(subteamRepository, never()).save(any());
    }
    
    @Test
    void testCreateSubteam_NullColorCode() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subteamService.createSubteam("New Subteam", null, "Design, Mechanical");
        });
        
        // Verify exception message
        assertEquals("Color code cannot be null or empty", exception.getMessage());
        
        // Verify repository was NOT called
        verify(subteamRepository, never()).existsByNameIgnoreCase(any());
        verify(subteamRepository, never()).save(any());
    }
    
    @Test
    void testCreateSubteam_EmptyColorCode() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subteamService.createSubteam("New Subteam", "  ", "Design, Mechanical");
        });
        
        // Verify exception message
        assertEquals("Color code cannot be null or empty", exception.getMessage());
        
        // Verify repository was NOT called
        verify(subteamRepository, never()).existsByNameIgnoreCase(any());
        verify(subteamRepository, never()).save(any());
    }
    
    @Test
    void testCreateSubteam_NullSpecialties() {
        // Setup
        when(subteamRepository.existsByNameIgnoreCase("New Subteam")).thenReturn(false);
        when(subteamRepository.save(any(Subteam.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Subteam result = subteamService.createSubteam("New Subteam", "#00FF00", null);
        
        // Verify
        assertNotNull(result);
        assertEquals("New Subteam", result.getName());
        assertEquals("#00FF00", result.getColorCode());
        assertNull(result.getSpecialties());
        
        // Verify repository interactions
        verify(subteamRepository).existsByNameIgnoreCase("New Subteam");
        verify(subteamRepository).save(any(Subteam.class));
    }
    
    @Test
    void testUpdateSpecialties() {
        // Setup
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        when(subteamRepository.save(any(Subteam.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Subteam result = subteamService.updateSpecialties(1L, "Updated Skills");
        
        // Verify
        assertNotNull(result);
        assertEquals("Updated Skills", result.getSpecialties());
        
        // Verify repository interactions
        verify(subteamRepository).findById(1L);
        verify(subteamRepository).save(testSubteam);
    }
    
    @Test
    void testUpdateSpecialties_SubteamNotFound() {
        // Setup
        when(subteamRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Subteam result = subteamService.updateSpecialties(999L, "Updated Skills");
        
        // Verify
        assertNull(result);
        
        // Verify repository interactions
        verify(subteamRepository).findById(999L);
        verify(subteamRepository, never()).save(any());
    }
    
    @Test
    void testUpdateSpecialties_NullId() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subteamService.updateSpecialties(null, "Updated Skills");
        });
        
        // Verify exception message
        assertEquals("Subteam ID cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(subteamRepository, never()).findById(any());
        verify(subteamRepository, never()).save(any());
    }
    
    @Test
    void testUpdateSpecialties_NullSpecialties() {
        // Setup
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        when(subteamRepository.save(any(Subteam.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Subteam result = subteamService.updateSpecialties(1L, null);
        
        // Verify
        assertNotNull(result);
        assertNull(result.getSpecialties());
        
        // Verify repository interactions
        verify(subteamRepository).findById(1L);
        verify(subteamRepository).save(testSubteam);
    }
    
    @Test
    void testUpdateColorCode() {
        // Setup
        when(subteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        when(subteamRepository.save(any(Subteam.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Execute
        Subteam result = subteamService.updateColorCode(1L, "#00FF00");
        
        // Verify
        assertNotNull(result);
        assertEquals("#00FF00", result.getColorCode());
        
        // Verify repository interactions
        verify(subteamRepository).findById(1L);
        verify(subteamRepository).save(testSubteam);
    }
    
    @Test
    void testUpdateColorCode_SubteamNotFound() {
        // Setup
        when(subteamRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute
        Subteam result = subteamService.updateColorCode(999L, "#00FF00");
        
        // Verify
        assertNull(result);
        
        // Verify repository interactions
        verify(subteamRepository).findById(999L);
        verify(subteamRepository, never()).save(any());
    }
    
    @Test
    void testUpdateColorCode_NullId() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subteamService.updateColorCode(null, "#00FF00");
        });
        
        // Verify exception message
        assertEquals("Subteam ID cannot be null", exception.getMessage());
        
        // Verify repository was NOT called
        verify(subteamRepository, never()).findById(any());
        verify(subteamRepository, never()).save(any());
    }
    
    @Test
    void testUpdateColorCode_NullColorCode() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subteamService.updateColorCode(1L, null);
        });
        
        // Verify exception message
        assertEquals("Color code cannot be null or empty", exception.getMessage());
        
        // Verify repository was NOT called
        verify(subteamRepository, never()).findById(any());
        verify(subteamRepository, never()).save(any());
    }
    
    @Test
    void testUpdateColorCode_EmptyColorCode() {
        // Execute and verify exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subteamService.updateColorCode(1L, "  ");
        });
        
        // Verify exception message
        assertEquals("Color code cannot be null or empty", exception.getMessage());
        
        // Verify repository was NOT called
        verify(subteamRepository, never()).findById(any());
        verify(subteamRepository, never()).save(any());
    }
}