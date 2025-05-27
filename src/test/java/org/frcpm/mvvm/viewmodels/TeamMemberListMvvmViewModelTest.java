// src/test/java/org/frcpm/mvvm/viewmodels/TeamMemberListMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.frcpm.di.TestModule;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.services.TeamMemberService;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the TeamMemberListMvvmViewModel class.
 * FIXED: Uses proper mock pattern instead of casting to concrete implementations.
 */
public class TeamMemberListMvvmViewModelTest {
    
    private TeamMemberService teamMemberService;
    
    private Project testProject;
    private List<TeamMember> testTeamMembers;
    private TeamMemberListMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create mock service
        TeamMemberService mockService = mock(TeamMemberService.class);
        
        // Register mock with TestModule
        TestModule.setService(TeamMemberService.class, mockService);
        
        // Get service from TestModule (now returns mock)
        teamMemberService = TestModule.getService(TeamMemberService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new TeamMemberListMvvmViewModel(teamMemberService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        // Create test team members
        testTeamMembers = new ArrayList<>();
        
        TeamMember member1 = new TeamMember();
        member1.setId(1L);
        member1.setFirstName("John");
        member1.setLastName("Doe");
        member1.setUsername("jdoe");
        member1.setEmail("john.doe@example.com");
        member1.setLeader(false);
        testTeamMembers.add(member1);
        
        TeamMember member2 = new TeamMember();
        member2.setId(2L);
        member2.setFirstName("Jane");
        member2.setLastName("Smith");
        member2.setUsername("jsmith");
        member2.setEmail("jane.smith@example.com");
        member2.setLeader(true);
        testTeamMembers.add(member2);
        
        TeamMember member3 = new TeamMember();
        member3.setId(3L);
        member3.setFirstName("Bob");
        member3.setLastName("Johnson");
        member3.setUsername("bjohnson");
        member3.setEmail("bob.johnson@example.com");
        member3.setLeader(false);
        testTeamMembers.add(member3);
    }
    
    @Test
    public void testInitialState() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Check that the viewModel is not null
            assertNotNull(viewModel, "ViewModel should not be null");
            
            // Check initial values
            assertTrue(viewModel.getTeamMembers().isEmpty());
            assertNull(viewModel.getSelectedTeamMember());
            assertNull(viewModel.getCurrentProject());
            assertFalse(viewModel.isLoading());
            
            // Verify commands exist
            assertNotNull(viewModel.getLoadTeamMembersCommand());
            assertNotNull(viewModel.getNewTeamMemberCommand());
            assertNotNull(viewModel.getEditTeamMemberCommand());
            assertNotNull(viewModel.getDeleteTeamMemberCommand());
            assertNotNull(viewModel.getRefreshTeamMembersCommand());
            
            // Check command executability
            assertTrue(viewModel.getNewTeamMemberCommand().isExecutable());
            assertTrue(viewModel.getDeleteTeamMemberCommand().isNotExecutable()); // No selection
            assertTrue(viewModel.getEditTeamMemberCommand().isNotExecutable()); // No selection
        });
    }
    
    @Test
    public void testInitWithProject() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            // Get the success callback and call it with test data
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<TeamMember>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testTeamMembers);
            return null;
        }).when(teamMemberService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project
            viewModel.initWithProject(testProject);
            
            // Verify project was set
            assertEquals(testProject, viewModel.getCurrentProject());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify team members were loaded
            assertFalse(viewModel.getTeamMembers().isEmpty());
            assertEquals(3, viewModel.getTeamMembers().size());
            assertEquals("John", viewModel.getTeamMembers().get(0).getFirstName());
            assertEquals("Jane", viewModel.getTeamMembers().get(1).getFirstName());
            assertEquals("Bob", viewModel.getTeamMembers().get(2).getFirstName());
        });
    }
    
    @Test
    public void testLoadTeamMembersCommand() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<TeamMember>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testTeamMembers);
            return null;
        }).when(teamMemberService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute load command
            viewModel.getLoadTeamMembersCommand().execute();
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify team members were loaded
            assertFalse(viewModel.getTeamMembers().isEmpty());
            assertEquals(3, viewModel.getTeamMembers().size());
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testRefreshTeamMembersCommand() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<TeamMember>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testTeamMembers);
            return null;
        }).when(teamMemberService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute refresh command
            viewModel.getRefreshTeamMembersCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify team members were loaded
            assertFalse(viewModel.getTeamMembers().isEmpty());
            assertEquals(3, viewModel.getTeamMembers().size());
        });
    }
    
    @Test
    public void testTeamMemberSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no selection
            assertNull(viewModel.getSelectedTeamMember());
            
            // Commands requiring selection should not be executable
            assertTrue(viewModel.getEditTeamMemberCommand().isNotExecutable());
            assertTrue(viewModel.getDeleteTeamMemberCommand().isNotExecutable());
            
            // Select a team member
            TeamMember selectedMember = testTeamMembers.get(0);
            viewModel.setSelectedTeamMember(selectedMember);
            
            // Verify selection
            assertEquals(selectedMember, viewModel.getSelectedTeamMember());
            
            // Commands requiring selection should now be executable
            assertTrue(viewModel.getEditTeamMemberCommand().isExecutable());
            assertTrue(viewModel.getDeleteTeamMemberCommand().isExecutable());
        });
    }
    
    @Test
    public void testNewTeamMemberCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Command should always be executable
            assertTrue(viewModel.getNewTeamMemberCommand().isExecutable());
            
            // Execute command
            viewModel.getNewTeamMemberCommand().execute();
            
            // This command is mainly for the view to handle
            // Just verify it executed without error
        });
    }
    
    @Test
    public void testEditTeamMemberCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not executable (no selection)
            assertTrue(viewModel.getEditTeamMemberCommand().isNotExecutable());
            
            // Select a team member
            viewModel.setSelectedTeamMember(testTeamMembers.get(0));
            
            // Should now be executable
            assertTrue(viewModel.getEditTeamMemberCommand().isExecutable());
            
            // Execute command
            viewModel.getEditTeamMemberCommand().execute();
            
            // This command is mainly for the view to handle
            // Just verify it executed without error
        });
    }
    
    @Test
    public void testDeleteTeamMemberCommand() {
        // Configure the mock service for successful deletion
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Boolean> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(true);
            return null;
        }).when(teamMemberService).deleteByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add team members to the list
            viewModel.getTeamMembers().addAll(testTeamMembers);
            
            // Select a team member
            TeamMember memberToDelete = testTeamMembers.get(0);
            viewModel.setSelectedTeamMember(memberToDelete);
            
            // Should be executable
            assertTrue(viewModel.getDeleteTeamMemberCommand().isExecutable());
            
            // Execute delete command
            viewModel.getDeleteTeamMemberCommand().execute();
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify team member was removed from list
            assertFalse(viewModel.getTeamMembers().contains(memberToDelete));
            assertEquals(2, viewModel.getTeamMembers().size());
            
            // Selection should be cleared
            assertNull(viewModel.getSelectedTeamMember());
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testDeleteTeamMemberCommandFailure() {
        // Configure the mock service for failed deletion
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Boolean> successCallback = 
                invocation.getArgument(1);
            successCallback.accept(false);
            return null;
        }).when(teamMemberService).deleteByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add team members to the list
            viewModel.getTeamMembers().addAll(testTeamMembers);
            
            // Select a team member
            TeamMember memberToDelete = testTeamMembers.get(0);
            viewModel.setSelectedTeamMember(memberToDelete);
            
            // Execute delete command
            viewModel.getDeleteTeamMemberCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Team member should still be in the list (deletion failed)
            assertTrue(viewModel.getTeamMembers().contains(memberToDelete));
            assertEquals(3, viewModel.getTeamMembers().size());
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to delete"));
        });
    }
    
    @Test
    public void testDeleteTeamMemberWithNullSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Ensure no selection
            viewModel.setSelectedTeamMember(null);
            
            // Command should not be executable
            assertTrue(viewModel.getDeleteTeamMemberCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testDeleteTeamMemberWithNullId() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create team member without ID
            TeamMember memberWithoutId = new TeamMember();
            memberWithoutId.setFirstName("No ID");
            memberWithoutId.setLastName("Member");
            
            // Add to list and select
            viewModel.getTeamMembers().add(memberWithoutId);
            viewModel.setSelectedTeamMember(memberWithoutId);
            
            // Execute delete command
            viewModel.getDeleteTeamMemberCommand().execute();
            
            // Should handle gracefully and not call service
            verify(teamMemberService, never()).deleteByIdAsync(any(), any(), any());
        });
    }
    
    @Test
    public void testErrorHandlingDuringLoad() {
        // Configure the mock service to return an error
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = 
                invocation.getArgument(1);
            errorCallback.accept(new RuntimeException("Service error"));
            return null;
        }).when(teamMemberService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute load command
            viewModel.getLoadTeamMembersCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to load"));
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Team members list should remain empty
            assertTrue(viewModel.getTeamMembers().isEmpty());
        });
    }
    
    
    @Test
    public void testErrorHandlingDuringDelete() {
        // Configure the mock service to return an error
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = 
                invocation.getArgument(2);
            errorCallback.accept(new RuntimeException("Delete error"));
            return null;
        }).when(teamMemberService).deleteByIdAsync(anyLong(), any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Add team members to the list
            viewModel.getTeamMembers().addAll(testTeamMembers);
            
            // Select a team member
            TeamMember memberToDelete = testTeamMembers.get(0);
            viewModel.setSelectedTeamMember(memberToDelete);
            
            // Execute delete command
            viewModel.getDeleteTeamMemberCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should have error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Failed to delete"));
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Team member should still be in the list
            assertTrue(viewModel.getTeamMembers().contains(memberToDelete));
        });
    }
    
    @Test
    public void testCurrentProjectProperty() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no current project
            assertNull(viewModel.getCurrentProject());
            
            // Set current project
            viewModel.setCurrentProject(testProject);
            
            // Verify project was set
            assertEquals(testProject, viewModel.getCurrentProject());
        });
    }
    
    @Test
    public void testLoadingProperty() {
        // Configure the mock service with a delay to test loading state
        doAnswer(invocation -> {
            // Simulate async delay
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    @SuppressWarnings("unchecked")
                    java.util.function.Consumer<List<TeamMember>> successCallback = 
                        invocation.getArgument(0);
                    successCallback.accept(testTeamMembers);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return null;
        }).when(teamMemberService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not loading
            assertFalse(viewModel.isLoading());
            
            // Execute load command
            viewModel.getLoadTeamMembersCommand().execute();
            
            // Should be loading immediately
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testTeamMembersListManipulation() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially empty
            assertTrue(viewModel.getTeamMembers().isEmpty());
            
            // Add team members
            viewModel.getTeamMembers().addAll(testTeamMembers);
            
            // Verify size
            assertEquals(3, viewModel.getTeamMembers().size());
            
            // Verify specific members
            assertTrue(viewModel.getTeamMembers().contains(testTeamMembers.get(0)));
            assertTrue(viewModel.getTeamMembers().contains(testTeamMembers.get(1)));
            assertTrue(viewModel.getTeamMembers().contains(testTeamMembers.get(2)));
            
            // Clear list
            viewModel.getTeamMembers().clear();
            
            // Should be empty again
            assertTrue(viewModel.getTeamMembers().isEmpty());
        });
    }
    
    @Test
    public void testCommandExecutabilityWithoutSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // No team member selected
            assertNull(viewModel.getSelectedTeamMember());
            
            // Commands that don't require selection should be executable
            assertTrue(viewModel.getLoadTeamMembersCommand().isExecutable());
            assertTrue(viewModel.getNewTeamMemberCommand().isExecutable());
            assertTrue(viewModel.getRefreshTeamMembersCommand().isExecutable());
            
            // Commands that require selection should not be executable
            assertTrue(viewModel.getEditTeamMemberCommand().isNotExecutable());
            assertTrue(viewModel.getDeleteTeamMemberCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testCommandExecutabilityWithSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Select a team member
            viewModel.setSelectedTeamMember(testTeamMembers.get(0));
            
            // All commands should be executable
            assertTrue(viewModel.getLoadTeamMembersCommand().isExecutable());
            assertTrue(viewModel.getNewTeamMemberCommand().isExecutable());
            assertTrue(viewModel.getRefreshTeamMembersCommand().isExecutable());
            assertTrue(viewModel.getEditTeamMemberCommand().isExecutable());
            assertTrue(viewModel.getDeleteTeamMemberCommand().isExecutable());
        });
    }
    
    @Test
    public void testMultipleLoadOperations() {
        // Configure the mock service to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<TeamMember>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testTeamMembers);
            return null;
        }).when(teamMemberService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute load command multiple times
            viewModel.getLoadTeamMembersCommand().execute();
            viewModel.getRefreshTeamMembersCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should still have correct data
            assertEquals(3, viewModel.getTeamMembers().size());
            
            // Verify service was called multiple times
            verify(teamMemberService, atLeast(2)).findAllAsync(any(), any());
        });
    }
    
    @Test
    public void testTeamMemberSelectionClearing() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Select a team member
            viewModel.setSelectedTeamMember(testTeamMembers.get(0));
            assertNotNull(viewModel.getSelectedTeamMember());
            
            // Clear selection
            viewModel.setSelectedTeamMember(null);
            assertNull(viewModel.getSelectedTeamMember());
            
            // Commands requiring selection should not be executable
            assertTrue(viewModel.getEditTeamMemberCommand().isNotExecutable());
            assertTrue(viewModel.getDeleteTeamMemberCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testAsyncServiceCasting() {
        // Verify that the service is now a proper mock
        assertNotNull(teamMemberService);
        // The service is now a proper mock, no casting needed
    }
}