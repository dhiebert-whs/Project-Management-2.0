// src/test/java/org/frcpm/mvvm/viewmodels/SubteamListMvvmViewModelTest.java
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
import org.frcpm.services.impl.SubteamServiceAsyncImpl;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the SubteamListMvvmViewModel class.
 * FIXED: Uses proper mock pattern instead of casting to concrete implementations.
 */
public class SubteamListMvvmViewModelTest {
    
    private SubteamService subteamService;
    
    private Project testProject;
    private List<Subteam> testSubteams;
    private SubteamListMvvmViewModel viewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Initialize TestModule
        TestModule.initialize();
        
        // Create test data
        setupTestData();
        
        // Create mock of the ASYNC implementation directly
        SubteamServiceAsyncImpl mockSubteamService = mock(SubteamServiceAsyncImpl.class);
        
        // Configure findAllAsync to return test data
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<List<Subteam>> successCallback = 
                invocation.getArgument(0);
            successCallback.accept(testSubteams);
            return null;
        }).when(mockSubteamService).findAllAsync(any(), any());
        
        // Configure deleteByIdAsync
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Boolean> successCallback = 
                invocation.getArgument(1);
            
            // Check if subteam has members or subsystems
            Subteam subteamToDelete = testSubteams.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
            
            if (subteamToDelete != null) {
                // Check if has members
                if (subteamToDelete.getMembers() != null && !subteamToDelete.getMembers().isEmpty()) {
                    // This should trigger error handling in the ViewModel
                    successCallback.accept(false);
                } else {
                    // Simulate successful deletion
                    testSubteams.removeIf(s -> s.getId().equals(id));
                    successCallback.accept(true);
                }
            } else {
                successCallback.accept(false);
            }
            return null;
        }).when(mockSubteamService).deleteByIdAsync(anyLong(), any(), any());
        
        // Register mock with TestModule
        TestModule.setService(SubteamService.class, mockSubteamService);
        
        // Get service from TestModule
        subteamService = TestModule.getService(SubteamService.class);
        
        // Initialize JavaFX toolkit if needed
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
        
        // Create the view model on the JavaFX thread
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = new SubteamListMvvmViewModel(subteamService);
        });
    }
    
    private void setupTestData() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        
        // Create test subteams
        testSubteams = new ArrayList<>();
        
        Subteam mechanicalSubteam = new Subteam();
        mechanicalSubteam.setId(1L);
        mechanicalSubteam.setName("Mechanical");
        mechanicalSubteam.setColorCode("#FF5733");
        mechanicalSubteam.setSpecialties("Building, CAD");
        testSubteams.add(mechanicalSubteam);
        
        Subteam electricalSubteam = new Subteam();
        electricalSubteam.setId(2L);
        electricalSubteam.setName("Electrical");
        electricalSubteam.setColorCode("#33FF57");
        electricalSubteam.setSpecialties("Wiring, Control Systems");
        testSubteams.add(electricalSubteam);
        
        Subteam programmingSubteam = new Subteam();
        programmingSubteam.setId(3L);
        programmingSubteam.setName("Programming");
        programmingSubteam.setColorCode("#3357FF");
        programmingSubteam.setSpecialties("Java, Vision Processing");
        testSubteams.add(programmingSubteam);
        
        Subteam businessSubteam = new Subteam();
        businessSubteam.setId(4L);
        businessSubteam.setName("Business");
        businessSubteam.setColorCode("#F3FF33");
        businessSubteam.setSpecialties("Fundraising, Outreach");
        testSubteams.add(businessSubteam);
        
        // Add members to the mechanical subteam to test deletion with members
        TeamMember member1 = new TeamMember();
        member1.setId(1L);
        member1.setFirstName("John");
        member1.setLastName("Doe");
        member1.setSubteam(mechanicalSubteam);
        mechanicalSubteam.getMembers().add(member1);
        
        TeamMember member2 = new TeamMember();
        member2.setId(2L);
        member2.setFirstName("Jane");
        member2.setLastName("Smith");
        member2.setSubteam(mechanicalSubteam);
        mechanicalSubteam.getMembers().add(member2);
    }
    
    @Test
    public void testInitialState() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify initial state
            assertTrue(viewModel.getSubteams().isEmpty());
            assertNull(viewModel.getSelectedSubteam());
            assertNull(viewModel.getCurrentProject());
            assertFalse(viewModel.isLoading());
            
            // Verify commands exist
            assertNotNull(viewModel.getLoadSubteamsCommand());
            assertNotNull(viewModel.getNewSubteamCommand());
            assertNotNull(viewModel.getEditSubteamCommand());
            assertNotNull(viewModel.getDeleteSubteamCommand());
            assertNotNull(viewModel.getRefreshSubteamsCommand());
            
            // Check command executability - use !isExecutable instead of isNotExecutable
            assertTrue(viewModel.getLoadSubteamsCommand().isExecutable());
            assertTrue(viewModel.getNewSubteamCommand().isExecutable());
            assertTrue(viewModel.getRefreshSubteamsCommand().isExecutable());
            assertFalse(viewModel.getDeleteSubteamCommand().isExecutable()); // No selection
            assertFalse(viewModel.getEditSubteamCommand().isExecutable()); // No selection
        });
    }
    
    @Test
    public void testInitWithProject() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initialize with project - this triggers loadSubteamsAsync
            viewModel.initWithProject(testProject);
            
            // Verify project was set
            assertEquals(testProject, viewModel.getCurrentProject());
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify subteams were loaded - this was the main issue
            assertEquals(4, viewModel.getSubteams().size());
            assertEquals("Mechanical", viewModel.getSubteams().get(0).getName());
            assertEquals("Electrical", viewModel.getSubteams().get(1).getName());
            assertEquals("Programming", viewModel.getSubteams().get(2).getName());
            assertEquals("Business", viewModel.getSubteams().get(3).getName());
        });
    }
    
    @Test
    public void testLoadSubteamsCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute load command
            viewModel.getLoadSubteamsCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify subteams were loaded
            assertEquals(4, viewModel.getSubteams().size());
            
            // Verify specific subteams
            assertTrue(viewModel.getSubteams().stream().anyMatch(s -> s.getName().equals("Mechanical")));
            assertTrue(viewModel.getSubteams().stream().anyMatch(s -> s.getName().equals("Electrical")));
            assertTrue(viewModel.getSubteams().stream().anyMatch(s -> s.getName().equals("Programming")));
            assertTrue(viewModel.getSubteams().stream().anyMatch(s -> s.getName().equals("Business")));
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testRefreshSubteamsCommand() {
        TestUtils.runOnFxThreadAndWait(() -> {
            // Execute refresh command
            viewModel.getRefreshSubteamsCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify subteams were loaded
            assertEquals(4, viewModel.getSubteams().size());
        });
    }
    
    @Test
    public void testSubteamSelection() {
        // Load subteams first
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel.getLoadSubteamsCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Initially no selection
            assertNull(viewModel.getSelectedSubteam());
            
            // Commands requiring selection should not be executable
            assertFalse(viewModel.getEditSubteamCommand().isExecutable());
            assertFalse(viewModel.getDeleteSubteamCommand().isExecutable());
            
            // Select a subteam
            Subteam selectedSubteam = viewModel.getSubteams().get(0);
            viewModel.setSelectedSubteam(selectedSubteam);
            
            // Verify selection
            assertEquals(selectedSubteam, viewModel.getSelectedSubteam());
            
            // Commands requiring selection should now be executable
            assertTrue(viewModel.getEditSubteamCommand().isExecutable());
            assertTrue(viewModel.getDeleteSubteamCommand().isExecutable());
        });
    }
    
    @Test
    public void testDeleteSubteamCommand() {
        // Load subteams first
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel.getLoadSubteamsCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Get a subteam without members to delete (Programming subteam)
            Subteam subteamToDelete = viewModel.getSubteams().stream()
                .filter(s -> s.getName().equals("Programming"))
                .findFirst()
                .orElse(viewModel.getSubteams().get(2));
            
            // Select the subteam
            viewModel.setSelectedSubteam(subteamToDelete);
            
            // Execute delete command
            viewModel.getDeleteSubteamCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify subteam was removed
            assertEquals(3, viewModel.getSubteams().size());
            assertFalse(viewModel.getSubteams().stream().anyMatch(s -> s.getName().equals("Programming")));
            
            // Verify selection was cleared
            assertNull(viewModel.getSelectedSubteam());
            
            // Error message should be cleared
            assertNull(viewModel.getErrorMessage());
        });
    }
    
    @Test
    public void testDeleteSubteamWithMembersFailure() {
        // Load subteams first
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel.getLoadSubteamsCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Find a subteam with members (Mechanical)
            Subteam subteamWithMembers = viewModel.getSubteams().stream()
                .filter(s -> s.getName().equals("Mechanical"))
                .findFirst()
                .orElse(viewModel.getSubteams().get(0));
            
            // Make sure it has members
            assertTrue(subteamWithMembers.getMembers().size() > 0);
            
            // Select the subteam
            viewModel.setSelectedSubteam(subteamWithMembers);
            
            // Execute delete command
            viewModel.getDeleteSubteamCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify error message exists - the viewModel should have set an error message
            assertNotNull(viewModel.getErrorMessage());
            assertTrue(viewModel.getErrorMessage().contains("member"));
            
            // Verify subteam was not removed
            assertEquals(4, viewModel.getSubteams().size());
            assertTrue(viewModel.getSubteams().stream().anyMatch(s -> s.getName().equals("Mechanical")));
        });
    }
    
    @Test
    public void testErrorHandlingDuringLoad() {
        // Create error mock
        SubteamServiceAsyncImpl errorMockService = mock(SubteamServiceAsyncImpl.class);
        
        // Configure mock to throw error
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Throwable> errorCallback = 
                invocation.getArgument(1);
            errorCallback.accept(new RuntimeException("Test error"));
            return null;
        }).when(errorMockService).findAllAsync(any(), any());
        
        // Register error mock with TestModule
        TestModule.setService(SubteamService.class, errorMockService);
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Create new view model with error mock
            SubteamListMvvmViewModel errorViewModel = new SubteamListMvvmViewModel(
                TestModule.getService(SubteamService.class)
            );
            
            // Execute load command
            errorViewModel.getLoadSubteamsCommand().execute();
            
            // Let async operations complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Verify error message
            assertNotNull(errorViewModel.getErrorMessage());
            assertTrue(errorViewModel.getErrorMessage().contains("Failed to load"));
            
            // Verify subteams are empty
            assertTrue(errorViewModel.getSubteams().isEmpty());
        });
    }
    
    @Test
    public void testDispose() {
        // Load subteams first
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel.getLoadSubteamsCommand().execute();
        });
        
        // Let async operations complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TestUtils.runOnFxThreadAndWait(() -> {
            // Verify subteams are not empty
            assertFalse(viewModel.getSubteams().isEmpty());
            
            // Call dispose
            viewModel.dispose();
            
            // Verify subteams are now empty
            assertTrue(viewModel.getSubteams().isEmpty());
        });
    }
}