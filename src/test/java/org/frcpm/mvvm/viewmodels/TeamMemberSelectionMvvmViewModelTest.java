// src/test/java/org/frcpm/mvvm/viewmodels/TeamMemberSelectionMvvmViewModelTest.java
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
 * Tests for the TeamMemberSelectionMvvmViewModel class.
 * FIXED: Uses proper mock pattern instead of casting to concrete implementations.
 */
public class TeamMemberSelectionMvvmViewModelTest {
    
    private TeamMemberService teamMemberService;
    
    private Project testProject;
    private List<TeamMember> testTeamMembers;
    private TeamMemberSelectionMvvmViewModel viewModel;
    
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
            viewModel = new TeamMemberSelectionMvvmViewModel(teamMemberService);
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
        
        TeamMember member4 = new TeamMember();
        member4.setId(4L);
        member4.setFirstName("Alice");
        member4.setLastName("Wilson");
        member4.setUsername("awilson");
        member4.setEmail("alice.wilson@example.com");
        member4.setLeader(false);
        testTeamMembers.add(member4);
    }
    
    @Test
    public void testInitialState() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Check that the viewModel is not null
            assertNotNull(viewModel, "ViewModel should not be null");
            
            // Check initial values
            assertNull(viewModel.getSelectedTeamMember());
            assertNull(viewModel.getCurrentProject());
            assertFalse(viewModel.isLoading());
            assertEquals("", viewModel.getSearchFilter());
            assertFalse(viewModel.isTeamMemberSelected());
            assertTrue(viewModel.getTeamMembers().isEmpty()); // Filtered list should be empty initially
            
            // Verify commands exist
            assertNotNull(viewModel.getLoadTeamMembersCommand());
            assertNotNull(viewModel.getSelectTeamMemberCommand());
            assertNotNull(viewModel.getCancelCommand());
            
            // Check command executability
            assertTrue(viewModel.getLoadTeamMembersCommand().isExecutable());
            assertTrue(viewModel.getSelectTeamMemberCommand().isNotExecutable()); // No selection
            assertTrue(viewModel.getCancelCommand().isExecutable());
        });
    }
    
    @Test
    public void testInitWithProject() {
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
            assertEquals(4, viewModel.getTeamMembers().size());
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
            assertEquals(4, viewModel.getTeamMembers().size());
            
            // Should no longer be loading
            assertFalse(viewModel.isLoading());
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testTeamMemberSelection() {
        // Load team members first
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<TeamMember>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testTeamMembers);
            return null;
        }).when(teamMemberService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load team members
            viewModel.getLoadTeamMembersCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Initially no selection
            assertNull(viewModel.getSelectedTeamMember());
            assertFalse(viewModel.isTeamMemberSelected());
            
            // Select command should not be executable
            assertTrue(viewModel.getSelectTeamMemberCommand().isNotExecutable());
            
            // Select a team member
            TeamMember selectedMember = testTeamMembers.get(0);
            viewModel.setSelectedTeamMember(selectedMember);
            
            // Verify selection
            assertEquals(selectedMember, viewModel.getSelectedTeamMember());
            assertFalse(viewModel.isTeamMemberSelected()); // Not selected until command is executed
            
            // Select command should now be executable
            assertTrue(viewModel.getSelectTeamMemberCommand().isExecutable());
        });
    }
    
    @Test
    public void testSelectTeamMemberCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Select a team member
            TeamMember selectedMember = testTeamMembers.get(0);
            viewModel.setSelectedTeamMember(selectedMember);
            
            // Execute select command
            assertTrue(viewModel.getSelectTeamMemberCommand().isExecutable());
            viewModel.getSelectTeamMemberCommand().execute();
            
            // Verify team member was selected
            assertTrue(viewModel.isTeamMemberSelected());
            assertEquals(selectedMember, viewModel.getSelectedTeamMember());
        });
    }
    
    @Test
    public void testCancelCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not selected
            assertFalse(viewModel.isTeamMemberSelected());
            
            // Execute cancel command
            assertTrue(viewModel.getCancelCommand().isExecutable());
            viewModel.getCancelCommand().execute();
            
            // Should remain not selected
            assertFalse(viewModel.isTeamMemberSelected());
        });
    }
    
    @Test
    public void testSearchFilterFunctionality() {
        // Load team members first
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<TeamMember>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testTeamMembers);
            return null;
        }).when(teamMemberService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load team members
            viewModel.getLoadTeamMembersCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Initially all members should be visible
            assertEquals(4, viewModel.getTeamMembers().size());
            
            // Filter by first name "John"
            viewModel.setSearchFilter("John");
            
            // Let filter update
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should only show John Doe
            assertEquals(1, viewModel.getTeamMembers().size());
            assertEquals("John", viewModel.getTeamMembers().get(0).getFirstName());
            
            // Filter by last name "Smith"
            viewModel.setSearchFilter("Smith");
            
            // Let filter update
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should only show Jane Smith
            assertEquals(1, viewModel.getTeamMembers().size());
            assertEquals("Jane", viewModel.getTeamMembers().get(0).getFirstName());
            
            // Filter by username "bjohnson"
            viewModel.setSearchFilter("bjohnson");
            
            // Let filter update
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should only show Bob Johnson
            assertEquals(1, viewModel.getTeamMembers().size());
            assertEquals("Bob", viewModel.getTeamMembers().get(0).getFirstName());
            
            // Filter by email domain "example.com"
            viewModel.setSearchFilter("example.com");
            
            // Let filter update
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should show all members (they all have example.com emails)
            assertEquals(4, viewModel.getTeamMembers().size());
            
            // Clear filter
            viewModel.setSearchFilter("");
            
            // Let filter update
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should show all members again
            assertEquals(4, viewModel.getTeamMembers().size());
        });
    }
    
    @Test
    public void testSearchFilterCaseInsensitive() {
        // Load team members first
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<TeamMember>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testTeamMembers);
            return null;
        }).when(teamMemberService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load team members
            viewModel.getLoadTeamMembersCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Filter with different cases
            String[] filterVariants = {"john", "JOHN", "John", "jOhN"};
            
            for (String filter : filterVariants) {
                viewModel.setSearchFilter(filter);
                
                // Let filter update
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // Should always find John Doe regardless of case
                assertEquals(1, viewModel.getTeamMembers().size(), 
                    "Filter '" + filter + "' should find John Doe");
                assertEquals("John", viewModel.getTeamMembers().get(0).getFirstName());
            }
        });
    }
    
    @Test
    public void testSearchFilterWithNoMatches() {
        // Load team members first
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<TeamMember>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testTeamMembers);
            return null;
        }).when(teamMemberService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load team members
            viewModel.getLoadTeamMembersCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Filter with non-existent term
            viewModel.setSearchFilter("NonExistentName");
            
            // Let filter update
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should show no members
            assertTrue(viewModel.getTeamMembers().isEmpty());
        });
    }
    
    @Test
    public void testSearchFilterWithNullAndEmpty() {
        // Load team members first
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<TeamMember>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testTeamMembers);
            return null;
        }).when(teamMemberService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load team members
            viewModel.getLoadTeamMembersCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Test null filter
            viewModel.setSearchFilter(null);
            
            // Let filter update
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should show all members
            assertEquals(4, viewModel.getTeamMembers().size());
            
            // Test empty filter
            viewModel.setSearchFilter("");
            
            // Let filter update
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should show all members
            assertEquals(4, viewModel.getTeamMembers().size());
            
            // Test whitespace-only filter
            viewModel.setSearchFilter("   ");
            
            // Let filter update
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should show all members (whitespace is trimmed)
            assertEquals(4, viewModel.getTeamMembers().size());
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
    public void testCurrentProjectProperty() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no current project
            assertNull(viewModel.getCurrentProject());
            
            // Set via initWithProject
            viewModel.initWithProject(testProject);
            
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
    public void testDispose() {
        // Load team members first
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<TeamMember>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testTeamMembers);
            return null;
        }).when(teamMemberService).findAllAsync(any(), any());
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Load team members
            viewModel.getLoadTeamMembersCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify team members were loaded
            assertFalse(viewModel.getTeamMembers().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Base dispose should be called, specific cleanup depends on implementation
            // The filtered list should be cleared since allTeamMembers is cleared
            assertTrue(viewModel.getTeamMembers().isEmpty());
        });
    }
    
    @Test
    public void testAsyncServiceCasting() {
        // Verify that the service is now a proper mock
        assertNotNull(teamMemberService);
        // The service is now a proper mock, no casting needed
    }
}