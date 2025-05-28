// src/test/java/org/frcpm/mvvm/viewmodels/SubteamDetailMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.frcpm.di.TestModule;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.SubteamServiceAsyncImpl;
import org.frcpm.services.impl.TeamMemberServiceAsyncImpl;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the SubteamDetailMvvmViewModel class.
 * FIXED: Properly handles service casting and creates ViewModel correctly.
 */
public class SubteamDetailMvvmViewModelTest {
    
    private SubteamService subteamService;
    private TeamMemberService teamMemberService;
    private SubteamDetailMvvmViewModel viewModel;
    
    private Subteam testSubteam;
    private List<TeamMember> testTeamMembers;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create mock services - CRITICAL: Mock the actual async implementations
        SubteamServiceAsyncImpl mockSubteamService = mock(SubteamServiceAsyncImpl.class);
        TeamMemberServiceAsyncImpl mockTeamMemberService = mock(TeamMemberServiceAsyncImpl.class);
        
        // Configure mock behavior for SubteamService
        when(mockSubteamService.findById(anyLong())).thenReturn(testSubteam);
        when(mockSubteamService.save(any(Subteam.class))).thenAnswer(inv -> inv.getArgument(0));
        
        // Mock async methods for SubteamService
        doAnswer(invocation -> {
            Subteam subteam = invocation.getArgument(0);
            Consumer<Subteam> callback = invocation.getArgument(1);
            callback.accept(subteam);
            return null;
        }).when(mockSubteamService).saveAsync(any(Subteam.class), any(), any());
        
        doAnswer(invocation -> {
            String name = invocation.getArgument(0);
            String colorCode = invocation.getArgument(1);
            String specialties = invocation.getArgument(2);
            Consumer<Subteam> callback = invocation.getArgument(3);
            
            Subteam newSubteam = new Subteam();
            newSubteam.setId(100L);
            newSubteam.setName(name);
            newSubteam.setColorCode(colorCode);
            newSubteam.setSpecialties(specialties);
            callback.accept(newSubteam);
            return null;
        }).when(mockSubteamService).createSubteamAsync(anyString(), anyString(), anyString(), any(), any());
        
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            String colorCode = invocation.getArgument(1);
            Consumer<Subteam> callback = invocation.getArgument(2);
            
            testSubteam.setColorCode(colorCode);
            callback.accept(testSubteam);
            return null;
        }).when(mockSubteamService).updateColorCodeAsync(anyLong(), anyString(), any(), any());
        
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            String specialties = invocation.getArgument(1);
            Consumer<Subteam> callback = invocation.getArgument(2);
            
            testSubteam.setSpecialties(specialties);
            callback.accept(testSubteam);
            return null;
        }).when(mockSubteamService).updateSpecialtiesAsync(anyLong(), anyString(), any(), any());
        
        // Configure mock behavior for TeamMemberService
        when(mockTeamMemberService.findBySubteam(any(Subteam.class))).thenReturn(testTeamMembers.subList(0, 2));
        
        // Mock async methods for TeamMemberService
        doAnswer(invocation -> {
            Subteam subteam = invocation.getArgument(0);
            Consumer<List<TeamMember>> callback = invocation.getArgument(1);
            callback.accept(testTeamMembers.subList(0, 2));
            return null;
        }).when(mockTeamMemberService).findBySubteamAsync(any(Subteam.class), any(), any());
        
        doAnswer(invocation -> {
            Long memberId = invocation.getArgument(0);
            Long subteamId = invocation.getArgument(1);
            Consumer<TeamMember> callback = invocation.getArgument(2);
            
            TeamMember member = testTeamMembers.stream()
                .filter(m -> m.getId().equals(memberId))
                .findFirst()
                .orElse(null);
            
            if (member != null) {
                if (subteamId == null) {
                    member.setSubteam(null);
                } else {
                    member.setSubteam(testSubteam);
                }
                callback.accept(member);
            }
            return null;
        }).when(mockTeamMemberService).assignToSubteamAsync(anyLong(), any(), any(), any());
        
        // Register mocks with TestModule
        TestModule.setService(SubteamService.class, mockSubteamService);
        TestModule.setService(TeamMemberService.class, mockTeamMemberService);
        
        // Get services from TestModule
        subteamService = TestModule.getService(SubteamService.class);
        teamMemberService = TestModule.getService(TeamMemberService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new SubteamDetailMvvmViewModel(subteamService, teamMemberService);
        });
    }
    
    private void setupTestData() {
        // Create test subteam
        testSubteam = new Subteam();
        testSubteam.setId(1L);
        testSubteam.setName("Mechanical");
        testSubteam.setColorCode("#FF5733");
        testSubteam.setSpecialties("Building, CAD, Machining");
        
        // Create test team members
        testTeamMembers = new ArrayList<>();
        
        TeamMember member1 = new TeamMember();
        member1.setId(1L);
        member1.setFirstName("John");
        member1.setLastName("Doe");
        member1.setEmail("john.doe@example.com");
        member1.setSubteam(testSubteam);
        testTeamMembers.add(member1);
        
        TeamMember member2 = new TeamMember();
        member2.setId(2L);
        member2.setFirstName("Jane");
        member2.setLastName("Smith");
        member2.setEmail("jane.smith@example.com");
        member2.setSubteam(testSubteam);
        testTeamMembers.add(member2);
        
        TeamMember member3 = new TeamMember();
        member3.setId(3L);
        member3.setFirstName("Bob");
        member3.setLastName("Johnson");
        member3.setEmail("bob.johnson@example.com");
        member3.setSubteam(null); // Not assigned to a subteam yet
        testTeamMembers.add(member3);
        
        // Add members to subteam
        testSubteam.getMembers().addAll(testTeamMembers.subList(0, 2));
    }
    
    @Test
    public void testInitialStateForNewSubteam() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new subteam
            viewModel.initNewSubteam();
            
            // Verify initial state
            assertTrue(viewModel.isNewSubteam());
            assertEquals("", viewModel.getName());
            assertEquals("#007BFF", viewModel.getColorCode()); // Default blue color
            assertEquals("", viewModel.getSpecialties());
            assertFalse(viewModel.isDirty());
            assertFalse(viewModel.isValid()); // Should be invalid due to empty name
            
            // Verify team members list is empty
            assertTrue(viewModel.getTeamMembers().isEmpty());
            
            // Verify commands
            assertNotNull(viewModel.getSaveCommand());
            assertNotNull(viewModel.getCancelCommand());
            assertNotNull(viewModel.getManageMembersCommand());
            assertNotNull(viewModel.getRemoveTeamMemberCommand());
            
            // Use !isExecutable() instead of isNotExecutable()
            assertFalse(viewModel.getSaveCommand().isExecutable());
            assertFalse(viewModel.getManageMembersCommand().isExecutable());
            assertFalse(viewModel.getRemoveTeamMemberCommand().isExecutable());
        });
    }
    
    @Test
    public void testInitExistingSubteam() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for an existing subteam
            viewModel.initExistingSubteam(testSubteam);
            
            // Verify state
            assertFalse(viewModel.isNewSubteam());
            assertEquals("Mechanical", viewModel.getName());
            assertEquals("#FF5733", viewModel.getColorCode());
            assertEquals("Building, CAD, Machining", viewModel.getSpecialties());
            assertFalse(viewModel.isDirty());
            assertTrue(viewModel.isValid()); // Should be valid with existing data
            
            // Verify subteam reference
            assertEquals(testSubteam, viewModel.getSubteam());
            
            // Manage members command should be executable for existing subteam
            assertTrue(viewModel.getManageMembersCommand().isExecutable());
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify team members were loaded
            assertFalse(viewModel.getTeamMembers().isEmpty());
            assertEquals(2, viewModel.getTeamMembers().size());
        });
    }
    
    @Test
    public void testValidation() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new subteam
            viewModel.initNewSubteam();
            
            // Initially not valid (name is empty)
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("name"));
            
            // Set name only
            viewModel.setName("Test Subteam");
            
            // Should now be valid
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
            
            // Set invalid color code
            viewModel.setColorCode("invalid");
            
            // Should be invalid due to invalid color code
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Color code"));
            
            // Set valid color code
            viewModel.setColorCode("#FF5733");
            
            // Should be valid again
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testSaveNewSubteam() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new subteam
            viewModel.initNewSubteam();
            
            // Set required properties
            viewModel.setName("New Subteam");
            viewModel.setColorCode("#FF5733");
            viewModel.setSpecialties("Testing, Quality Assurance");
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Should no longer be loading after save completes
            assertFalse(viewModel.isLoading());
            
            // Should no longer be a new subteam
            assertFalse(viewModel.isNewSubteam());
        });
    }
    
    @Test
    public void testUpdateExistingSubteam() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for an existing subteam
            viewModel.initExistingSubteam(testSubteam);
            
            // Change some properties
            viewModel.setColorCode("#33FF57"); // Change from #FF5733
            viewModel.setSpecialties("Building, CAD, Machining, Design"); // Add Design
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Should no longer be loading after save completes
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testDirtyFlag() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new subteam
            viewModel.initNewSubteam();
            
            // Initially not dirty
            assertFalse(viewModel.isDirty());
            
            // Change name
            viewModel.setName("Test Name");
            
            // Should be dirty now
            assertTrue(viewModel.isDirty());
            
            // Change other properties
            viewModel.setColorCode("#33FF57");
            viewModel.setSpecialties("Test Specialties");
            
            // Should still be dirty
            assertTrue(viewModel.isDirty());
        });
    }
    
    @Test
    public void testAddTeamMember() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing subteam
            viewModel.initExistingSubteam(testSubteam);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add a new team member
            TeamMember newMember = testTeamMembers.get(2); // The unassigned member
            
            // Add the team member
            boolean result = viewModel.addTeamMember(newMember);
            
            // Verify result
            assertTrue(result);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testRemoveTeamMember() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing subteam
            viewModel.initExistingSubteam(testSubteam);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify initial team members count
            assertEquals(2, viewModel.getTeamMembers().size());
            
            // Select a team member
            TeamMember selectedMember = viewModel.getTeamMembers().get(0);
            viewModel.setSelectedTeamMember(selectedMember);
            
            // Execute remove team member command
            viewModel.getRemoveTeamMemberCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testCanAddTeamMember() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // New subteam (not saved yet)
            viewModel.initNewSubteam();
            
            // Attempt to add team member to new subteam
            boolean result = viewModel.addTeamMember(testTeamMembers.get(0));
            
            // Should fail (new subteam not saved yet)
            assertFalse(result);
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("must be saved"));
        });
    }
    
    @Test
    public void testPropertyBindings() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new subteam
            viewModel.initNewSubteam();
            
            // Test name property
            viewModel.setName("Test Name");
            assertEquals("Test Name", viewModel.getName());
            assertEquals("Test Name", viewModel.nameProperty().get());
            
            // Test color code property
            viewModel.setColorCode("#33FF57");
            assertEquals("#33FF57", viewModel.getColorCode());
            assertEquals("#33FF57", viewModel.colorCodeProperty().get());
            
            // Test specialties property
            viewModel.setSpecialties("Test Specialties");
            assertEquals("Test Specialties", viewModel.getSpecialties());
            assertEquals("Test Specialties", viewModel.specialtiesProperty().get());
        });
    }
    
    @Test
    public void testTeamMemberSelection() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing subteam
            viewModel.initExistingSubteam(testSubteam);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no team member selected
            assertNull(viewModel.getSelectedTeamMember());
            
            // Remove team member command should not be executable (no selection)
            assertFalse(viewModel.getRemoveTeamMemberCommand().isExecutable());
            
            // Select a team member
            TeamMember selectedMember = viewModel.getTeamMembers().get(0);
            viewModel.setSelectedTeamMember(selectedMember);
            
            // Verify selection
            assertEquals(selectedMember, viewModel.getSelectedTeamMember());
            
            // Remove team member command should now be executable
            assertTrue(viewModel.getRemoveTeamMemberCommand().isExecutable());
        });
    }
    
    @Test
    public void testDispose() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing subteam
            viewModel.initExistingSubteam(testSubteam);
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Call dispose
            viewModel.dispose();
            
            // Verify collections were cleared
            assertTrue(viewModel.getTeamMembers().isEmpty());
        });
    }
    
    @Test
    public void testErrorHandlingDuringSave() {
        // Create error mock
        SubteamServiceAsyncImpl errorMockService = mock(SubteamServiceAsyncImpl.class);
        
        // Configure mock to throw error during save
        doAnswer(invocation -> {
            Consumer<Throwable> errorCallback = invocation.getArgument(4);
            errorCallback.accept(new RuntimeException("Test error"));
            return null;
        }).when(errorMockService).createSubteamAsync(anyString(), anyString(), anyString(), any(), any());
        
        // Register error mock
        TestModule.setService(SubteamService.class, errorMockService);
        TestModule.setService(TeamMemberService.class, teamMemberService);
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new view model with error mock
            SubteamDetailMvvmViewModel errorViewModel = new SubteamDetailMvvmViewModel(
                TestModule.getService(SubteamService.class), 
                TestModule.getService(TeamMemberService.class)
            );
            
            // Initialize for a new subteam
            errorViewModel.initNewSubteam();
            
            // Set required properties
            errorViewModel.setName("New Subteam");
            errorViewModel.setColorCode("#FF5733");
            
            // Execute save command
            errorViewModel.getSaveCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(errorViewModel.getErrorMessage());
            assertTrue(errorViewModel.getErrorMessage().contains("Failed to"));
            
            // Should no longer be loading
            assertFalse(errorViewModel.isLoading());
        });
    }
}