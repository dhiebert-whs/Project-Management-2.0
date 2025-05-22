// src/test/java/org/frcpm/mvvm/viewmodels/TeamMemberDetailMvvmViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.frcpm.di.TestModule;
import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.services.impl.TestableTeamMemberServiceAsyncImpl;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the TeamMemberDetailMvvmViewModel class.
 */
public class TeamMemberDetailMvvmViewModelTest {
    
    private TeamMemberService teamMemberService;
    private SubteamService subteamService;
    private TestableTeamMemberServiceAsyncImpl teamMemberServiceAsync;
    
    private Project testProject;
    private Subteam testSubteam;
    private TeamMember testTeamMember;
    private List<Subteam> testSubteams;
    private TeamMemberDetailMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Get service references from TestModule (they're already testable implementations)
        teamMemberService = TestModule.getService(TeamMemberService.class);
        subteamService = TestModule.getService(SubteamService.class);
        teamMemberServiceAsync = (TestableTeamMemberServiceAsyncImpl) teamMemberService;
        
        // Configure service mocks
        when(subteamService.findAll()).thenReturn(testSubteams);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new TeamMemberDetailMvvmViewModel(teamMemberService, subteamService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        // Create test subteam
        testSubteam = new Subteam();
        testSubteam.setId(1L);
        testSubteam.setName("Test Subteam");
        
        // Create test subteams list
        testSubteams = new ArrayList<>();
        testSubteams.add(testSubteam);
        
        Subteam subteam2 = new Subteam();
        subteam2.setId(2L);
        subteam2.setName("Second Subteam");
        testSubteams.add(subteam2);
        
        // Create test team member
        testTeamMember = new TeamMember();
        testTeamMember.setId(1L);
        testTeamMember.setFirstName("John");
        testTeamMember.setLastName("Doe");
        testTeamMember.setUsername("jdoe");
        testTeamMember.setEmail("john.doe@example.com");
        testTeamMember.setPhone("555-1234");
        testTeamMember.setSkills("Programming, Testing");
        testTeamMember.setLeader(false);
        testTeamMember.setSubteam(testSubteam);
    }
    
    @Test
    public void testInitialStateForNewTeamMember() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new team member
            viewModel.initNewTeamMember(new TeamMember());
            
            // Verify initial state
            assertTrue(viewModel.isNewTeamMember());
            assertEquals("", viewModel.getFirstName());
            assertEquals("", viewModel.getLastName());
            assertEquals("", viewModel.getUsername());
            assertEquals("", viewModel.getEmail());
            assertEquals("", viewModel.getPhone());
            assertEquals("", viewModel.getSkills());
            assertFalse(viewModel.isLeader());
            assertNull(viewModel.getSelectedSubteam());
            assertFalse(viewModel.isDirty());
            assertFalse(viewModel.isValid()); // Should be invalid due to empty username
            
            // Verify subteams were loaded
            assertFalse(viewModel.getSubteams().isEmpty());
            assertEquals(2, viewModel.getSubteams().size());
            
            // Verify commands
            assertNotNull(viewModel.getSaveCommand());
            assertNotNull(viewModel.getCancelCommand());
            assertTrue(viewModel.getSaveCommand().isNotExecutable()); // Not valid
        });
    }
    
    @Test
    public void testInitExistingTeamMember() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for an existing team member
            viewModel.initExistingTeamMember(testTeamMember);
            
            // Verify state
            assertFalse(viewModel.isNewTeamMember());
            assertEquals("John", viewModel.getFirstName());
            assertEquals("Doe", viewModel.getLastName());
            assertEquals("jdoe", viewModel.getUsername());
            assertEquals("john.doe@example.com", viewModel.getEmail());
            assertEquals("555-1234", viewModel.getPhone());
            assertEquals("Programming, Testing", viewModel.getSkills());
            assertFalse(viewModel.isLeader());
            assertEquals(testSubteam, viewModel.getSelectedSubteam());
            assertFalse(viewModel.isDirty());
            assertTrue(viewModel.isValid()); // Should be valid with existing data
            
            // Verify team member reference
            assertEquals(testTeamMember, viewModel.getTeamMember());
        });
    }
    
    @Test
    public void testValidation() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new team member
            viewModel.initNewTeamMember(new TeamMember());
            
            // Initially not valid (username is empty)
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("Username"));
            
            // Set username only
            viewModel.setUsername("testuser");
            
            // Should now be valid
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
            
            // Set invalid email
            viewModel.setEmail("invalid-email");
            
            // Should be invalid due to email format
            assertFalse(viewModel.isValid());
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("email"));
            
            // Fix email
            viewModel.setEmail("valid@example.com");
            
            // Should be valid again
            assertTrue(viewModel.isValid());
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testPropertyChangesSetDirtyFlag() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing team member
            viewModel.initExistingTeamMember(testTeamMember);
            
            // Initially should not be dirty
            assertFalse(viewModel.isDirty());
            
            // Change first name
            viewModel.setFirstName("Jane");
            assertTrue(viewModel.isDirty());
            
            // Reset dirty flag
            viewModel.setDirty(false);
            
            // Change last name
            viewModel.setLastName("Smith");
            assertTrue(viewModel.isDirty());
            
            // Reset dirty flag
            viewModel.setDirty(false);
            
            // Change username
            viewModel.setUsername("jsmith");
            assertTrue(viewModel.isDirty());
            
            // Reset dirty flag
            viewModel.setDirty(false);
            
            // Change email
            viewModel.setEmail("jane.smith@example.com");
            assertTrue(viewModel.isDirty());
            
            // Reset dirty flag
            viewModel.setDirty(false);
            
            // Change phone
            viewModel.setPhone("555-5678");
            assertTrue(viewModel.isDirty());
            
            // Reset dirty flag
            viewModel.setDirty(false);
            
            // Change skills
            viewModel.setSkills("Leadership, Management");
            assertTrue(viewModel.isDirty());
            
            // Reset dirty flag
            viewModel.setDirty(false);
            
            // Change leader status
            viewModel.setIsLeader(true);
            assertTrue(viewModel.isDirty());
            
            // Reset dirty flag
            viewModel.setDirty(false);
            
            // Change subteam
            viewModel.setSelectedSubteam(testSubteams.get(1));
            assertTrue(viewModel.isDirty());
        });
    }
    
    @Test
    public void testSaveNewTeamMember() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new team member
            viewModel.initNewTeamMember(new TeamMember());
            
            // Set required properties
            viewModel.setFirstName("Alice");
            viewModel.setLastName("Johnson");
            viewModel.setUsername("ajohnson");
            viewModel.setEmail("alice.johnson@example.com");
            viewModel.setPhone("555-9876");
            viewModel.setSkills("Design, CAD");
            viewModel.setIsLeader(true);
            viewModel.setSelectedSubteam(testSubteam);
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading after save completes
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testSaveExistingTeamMember() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with existing team member
            viewModel.initExistingTeamMember(testTeamMember);
            
            // Update some properties
            viewModel.setFirstName("Johnny");
            viewModel.setSkills("Programming, Testing, Leadership");
            viewModel.setIsLeader(true);
            
            // Should be valid and dirty
            assertTrue(viewModel.isValid());
            assertTrue(viewModel.isDirty());
            
            // Save command should be executable
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Execute save command
            viewModel.getSaveCommand().execute();
            
            // Verify loading state
            assertTrue(viewModel.isLoading());
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Should no longer be loading after save completes
            assertFalse(viewModel.isLoading());
        });
    }
    
    @Test
    public void testCancelCommand() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new team member
            viewModel.initNewTeamMember(new TeamMember());
            
            // Verify cancel command is executable
            assertTrue(viewModel.getCancelCommand().isExecutable());
            
            // Execute cancel command
            viewModel.getCancelCommand().execute();
            
            // This is mainly for the view to handle dialog closing
            // Just verify the command executed without error
        });
    }
    
    @Test
    public void testSubteamLoading() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify subteams were loaded during initialization
            verify(subteamService).findAll();
            
            // Verify subteams are available
            assertFalse(viewModel.getSubteams().isEmpty());
            assertEquals(2, viewModel.getSubteams().size());
            assertEquals("Test Subteam", viewModel.getSubteams().get(0).getName());
            assertEquals("Second Subteam", viewModel.getSubteams().get(1).getName());
        });
    }
    
    @Test
    public void testSubteamSelection() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new team member
            viewModel.initNewTeamMember(new TeamMember());
            
            // Initially no subteam selected
            assertNull(viewModel.getSelectedSubteam());
            
            // Select a subteam
            viewModel.setSelectedSubteam(testSubteam);
            
            // Verify selection
            assertEquals(testSubteam, viewModel.getSelectedSubteam());
            
            // Verify dirty flag was set
            assertTrue(viewModel.isDirty());
        });
    }
    
    @Test
    public void testEmailValidation() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new team member
            viewModel.initNewTeamMember(new TeamMember());
            
            // Set username to make basic validation pass
            viewModel.setUsername("testuser");
            
            // Test valid email formats
            String[] validEmails = {
                "user@example.com",
                "test.email@domain.org",
                "user+tag@company.co.uk",
                ""  // Empty email should be valid (optional field)
            };
            
            for (String email : validEmails) {
                viewModel.setEmail(email);
                assertTrue(viewModel.isValid(), "Email should be valid: " + email);
            }
            
            // Test invalid email formats
            String[] invalidEmails = {
                "invalid-email",
                "@domain.com",
                "user@",
                "user..double.dot@domain.com",
                "user@domain",
                "user name@domain.com"
            };
            
            for (String email : invalidEmails) {
                viewModel.setEmail(email);
                assertFalse(viewModel.isValid(), "Email should be invalid: " + email);
            }
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
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially not loading
            assertFalse(viewModel.isLoading());
            
            // Initialize with existing team member and trigger save
            viewModel.initExistingTeamMember(testTeamMember);
            viewModel.setFirstName("Updated Name");
            
            // Execute save to trigger loading
            viewModel.getSaveCommand().execute();
            
            // Should be loading
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
    public void testErrorHandlingDuringSubteamLoading() {
        // Create a new TestModule instance with failing subteam service
        SubteamService failingSubteamService = mock(SubteamService.class);
        when(failingSubteamService.findAll()).thenThrow(new RuntimeException("Service error"));
        
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new ViewModel with failing service
            TeamMemberDetailMvvmViewModel errorViewModel = 
                new TeamMemberDetailMvvmViewModel(teamMemberService, failingSubteamService);
            
            // Let any async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify error was handled
            assertNotNull(errorViewModel.getErrorMessage());
            assertTrue(errorViewModel.getErrorMessage().contains("subteams"));
        });
    }
    
    @Test
    public void testSaveCommandExecutability() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize for a new team member
            viewModel.initNewTeamMember(new TeamMember());
            
            // Initially not executable (not valid and not dirty)
            assertTrue(viewModel.getSaveCommand().isNotExecutable());
            
            // Set username to make it valid
            viewModel.setUsername("testuser");
            
            // Now should be executable (valid and dirty)
            assertTrue(viewModel.getSaveCommand().isExecutable());
            
            // Clear dirty flag
            viewModel.setDirty(false);
            
            // Should not be executable (valid but not dirty)
            assertTrue(viewModel.getSaveCommand().isNotExecutable());
            
            // Make invalid again
            viewModel.setUsername("");
            
            // Should not be executable (not valid even though dirty)
            assertTrue(viewModel.getSaveCommand().isNotExecutable());
        });
    }
    
    @Test
    public void testNullTeamMemberHandling() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Should throw exception for null team member
            assertThrows(IllegalArgumentException.class, () -> {
                viewModel.initExistingTeamMember(null);
            });
        });
    }
    
    @Test
    public void testDispose() {
        // Run on JavaFX thread to avoid threading issues
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize the view model
            viewModel.initNewTeamMember(new TeamMember());
            
            // Verify subteams were loaded
            assertFalse(viewModel.getSubteams().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify subteams were cleared
            assertTrue(viewModel.getSubteams().isEmpty());
        });
    }
}