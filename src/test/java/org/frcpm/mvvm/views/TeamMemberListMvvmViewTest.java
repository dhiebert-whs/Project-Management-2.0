// src/test/java/org/frcpm/mvvm/views/TeamMemberListMvvmViewTest.java

package org.frcpm.mvvm.views;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import org.frcpm.di.TestModule;
import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.mvvm.viewmodels.TeamMemberListMvvmViewModel;
import org.frcpm.repositories.specific.TeamMemberRepository;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.services.TeamMemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * TestFX test for TeamMemberListMvvmView.
 * Tests the UI components, data binding, and user interactions using the real MVVMFx view loading.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TeamMemberListMvvmViewTest extends BaseViewTest<TeamMemberListMvvmView, TeamMemberListMvvmViewModel> {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberListMvvmViewTest.class.getName());
    
    // Test data
    private List<TeamMember> testTeamMembers;
    private TeamMember testMember1;
    private TeamMember testMember2;
    private TeamMember testMember3;
    private Project testProject;
    private Subteam testSubteam;
    
    // Mock repositories
    private TeamMemberRepository mockTeamMemberRepository;
    private SubteamRepository mockSubteamRepository;
    
    @Override
    protected Class<TeamMemberListMvvmView> getViewClass() {
        return TeamMemberListMvvmView.class;
    }
    
    @Override
    protected void setupTestData() {
        LOGGER.info("Setting up test data for TeamMemberListMvvmViewTest");
        
        // Create test project
        testProject = new Project("Test Project", 
            LocalDate.now().minusDays(30), 
            LocalDate.now().plusDays(30), 
            LocalDate.now().plusDays(45));
        testProject.setId(1L);
        
        // Create test subteam
        testSubteam = new Subteam();
        testSubteam.setId(1L);
        testSubteam.setName("Programming");
        //testSubteam.setDescription("Software development team");
        
        // Create test team members
        testMember1 = new TeamMember("alice123", "Alice", "Johnson", "alice@example.com");
        testMember1.setId(1L);
        testMember1.setPhone("555-0101");
        testMember1.setSkills("Java, Python");
        testMember1.setLeader(true);
        testMember1.setSubteam(testSubteam);
        
        testMember2 = new TeamMember("bob456", "Bob", "Smith", "bob@example.com");
        testMember2.setId(2L);
        testMember2.setPhone("555-0102");
        testMember2.setSkills("C++, Electronics");
        testMember2.setLeader(false);
        testMember2.setSubteam(testSubteam);
        
        testMember3 = new TeamMember("carol789", "Carol", "Davis", "carol@example.com");
        testMember3.setId(3L);
        testMember3.setPhone("555-0103");
        testMember3.setSkills("CAD, Mechanical");
        testMember3.setLeader(false);
        testMember3.setSubteam(null); // No subteam assigned
        
        testTeamMembers = new ArrayList<>();
        testTeamMembers.add(testMember1);
        testTeamMembers.add(testMember2);
        testTeamMembers.add(testMember3);
        
        // Mock the team member repository
        mockTeamMemberRepository = mock(TeamMemberRepository.class);
        when(mockTeamMemberRepository.findAll()).thenReturn(testTeamMembers);
        when(mockTeamMemberRepository.findById(1L)).thenReturn(Optional.of(testMember1));
        when(mockTeamMemberRepository.findById(2L)).thenReturn(Optional.of(testMember2));
        when(mockTeamMemberRepository.findById(3L)).thenReturn(Optional.of(testMember3));
        when(mockTeamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mockTeamMemberRepository.deleteById(any(Long.class))).thenReturn(true);
        
        // Mock the subteam repository
        mockSubteamRepository = mock(SubteamRepository.class);
        when(mockSubteamRepository.findById(1L)).thenReturn(Optional.of(testSubteam));
        
        // Register the mock repositories with TestModule
        TestModule.setRepository(TeamMemberRepository.class, mockTeamMemberRepository);
        TestModule.setRepository(SubteamRepository.class, mockSubteamRepository);
    }
    
    @Override
    protected void createAndShowView(Stage stage) {
        LOGGER.info("Creating and showing TeamMemberListMvvmView");
        super.createAndShowView(stage);
        
        // Set the current project after view is created
        Platform.runLater(() -> {
            viewModel.setCurrentProject(testProject);
        });
        
        waitForFxEvents();
        sleep(100);
    }
    
    @Test
    @Order(1)
    public void testViewInitialization() {
        LOGGER.info("Testing view initialization");
        
        // Verify the view and ViewModel were created properly
        assertNotNull(view, "View should be created");
        assertNotNull(viewModel, "ViewModel should be created");
        
        // Verify basic UI components exist
        assertNotNull(lookup("#teamMemberTableView").query(), "Team member table view should exist");
        assertNotNull(lookup("#newButton").query(), "New button should exist");
        assertNotNull(lookup("#editButton").query(), "Edit button should exist");
        assertNotNull(lookup("#deleteButton").query(), "Delete button should exist");
        assertNotNull(lookup("#refreshButton").query(), "Refresh button should exist");
        assertNotNull(lookup("#projectLabel").query(), "Project label should exist");
    }
    
    @Test
    @Order(2)
    public void testProjectLabelBinding() {
        LOGGER.info("Testing project label binding");
        
        Label projectLabel = lookup("#projectLabel").query();
        assertNotNull(projectLabel, "Project label should exist");
        
        // Verify project label shows current project
        org.frcpm.utils.TestUtils.waitUntil(() -> projectLabel.getText().contains("Test Project"), 2000);
        assertTrue(projectLabel.getText().contains("Test Project"), "Project label should show current project name");
    }
    
    @Test
    @Order(3)
    public void testInitialButtonStates() {
        LOGGER.info("Testing initial button states");
        
        Button newButton = lookup("#newButton").query();
        Button editButton = lookup("#editButton").query();
        Button deleteButton = lookup("#deleteButton").query();
        Button refreshButton = lookup("#refreshButton").query();
        
        // New and refresh buttons should be enabled
        assertFalse(newButton.isDisabled(), "New button should be enabled initially");
        assertFalse(refreshButton.isDisabled(), "Refresh button should be enabled initially");
        
        // Edit and delete buttons should be disabled when no selection
        assertTrue(editButton.isDisabled(), "Edit button should be disabled when no selection");
        assertTrue(deleteButton.isDisabled(), "Delete button should be disabled when no selection");
    }
    
    @Test
    @Order(4)
    public void testTeamMemberTableBinding() {
        LOGGER.info("Testing team member table binding");
        
        TableView<TeamMember> tableView = lookup("#teamMemberTableView").query();
        assertNotNull(tableView, "Team member TableView should exist");
        
        // Verify the TableView is bound to the ViewModel's team members
        ObservableList<TeamMember> viewMembers = tableView.getItems();
        ObservableList<TeamMember> viewModelMembers = viewModel.getTeamMembers();
        
        assertSame(viewModelMembers, viewMembers, "TableView should be bound to ViewModel team members");
    }
    
    @Test
    @Order(5)
    public void testLoadTeamMembersCommand() {
        LOGGER.info("Testing load team members command");
        
        // Execute the load team members command
        Platform.runLater(() -> {
            viewModel.getLoadTeamMembersCommand().execute();
        });
        
        // Wait for async operation
        waitForFxEvents();
        sleep(300);
        
        // Verify team members were loaded
        TableView<TeamMember> tableView = lookup("#teamMemberTableView").query();
        org.frcpm.utils.TestUtils.waitUntil(() -> tableView.getItems().size() > 0, 2000);
        
        assertEquals(3, tableView.getItems().size(), "Should load 3 test team members");
        
        // Verify team member names
        ObservableList<TeamMember> items = tableView.getItems();
        assertTrue(items.stream().anyMatch(m -> "Alice Johnson".equals(m.getFullName())));
        assertTrue(items.stream().anyMatch(m -> "Bob Smith".equals(m.getFullName())));
        assertTrue(items.stream().anyMatch(m -> "Carol Davis".equals(m.getFullName())));
    }
    
    @Test
    @Order(6)
    public void testTeamMemberSelection() {
        LOGGER.info("Testing team member selection");
        
        // Make sure team members are loaded first
        Platform.runLater(() -> {
            viewModel.getLoadTeamMembersCommand().execute();
        });
        
        waitForFxEvents();
        sleep(300);
        
        TableView<TeamMember> tableView = lookup("#teamMemberTableView").query();
        org.frcpm.utils.TestUtils.waitUntil(() -> tableView.getItems().size() > 0, 2000);
        
        // Select the first team member
        Platform.runLater(() -> {
            tableView.getSelectionModel().select(0);
        });
        
        waitForFxEvents();
        sleep(200);
        
        // Verify selection affected button states
        Button editButton = lookup("#editButton").query();
        Button deleteButton = lookup("#deleteButton").query();
        
        org.frcpm.utils.TestUtils.waitUntil(() -> !editButton.isDisabled() && !deleteButton.isDisabled(), 2000);
        
        assertFalse(editButton.isDisabled(), "Edit button should be enabled after selection");
        assertFalse(deleteButton.isDisabled(), "Delete button should be enabled after selection");
        
        // Verify ViewModel has the selection
        assertNotNull(viewModel.getSelectedTeamMember(), "ViewModel should have selected team member");
        assertEquals("Alice Johnson", viewModel.getSelectedTeamMember().getFullName(), 
                    "ViewModel should have correct selected team member");
    }
    
    @Test
    @Order(7)
    public void testButtonClicks() {
        LOGGER.info("Testing button clicks");
        
        // Test new team member button
        Button newButton = lookup("#newButton").query();
        assertDoesNotThrow(() -> {
            clickOn(newButton);
        }, "Clicking new team member button should not throw exception");
        
        // Test refresh button
        Button refreshButton = lookup("#refreshButton").query();
        assertDoesNotThrow(() -> {
            clickOn(refreshButton);
        }, "Clicking refresh button should not throw exception");
        
        // Load team members and test edit/delete buttons
        Platform.runLater(() -> {
            viewModel.getLoadTeamMembersCommand().execute();
        });
        
        waitForFxEvents();
        sleep(300);
        
        // Select a team member
        TableView<TeamMember> tableView = lookup("#teamMemberTableView").query();
        org.frcpm.utils.TestUtils.waitUntil(() -> tableView.getItems().size() > 0, 2000);
        
        Platform.runLater(() -> {
            tableView.getSelectionModel().select(0);
        });
        
        waitForFxEvents();
        sleep(200);
        
        // Test edit button
        Button editButton = lookup("#editButton").query();
        org.frcpm.utils.TestUtils.waitUntil(() -> !editButton.isDisabled(), 2000);
        assertDoesNotThrow(() -> {
            clickOn(editButton);
        }, "Clicking edit team member button should not throw exception");
        
        // Test delete button
        Button deleteButton = lookup("#deleteButton").query();
        assertDoesNotThrow(() -> {
            clickOn(deleteButton);
        }, "Clicking delete team member button should not throw exception");
    }
    
    @Test
    @Order(8)
    public void testViewModelCommands() {
        LOGGER.info("Testing ViewModel commands");
        
        // Verify commands exist and are not null
        assertNotNull(viewModel.getLoadTeamMembersCommand(), "Load team members command should exist");
        assertNotNull(viewModel.getNewTeamMemberCommand(), "New team member command should exist");
        assertNotNull(viewModel.getEditTeamMemberCommand(), "Edit team member command should exist");
        assertNotNull(viewModel.getDeleteTeamMemberCommand(), "Delete team member command should exist");
        assertNotNull(viewModel.getRefreshTeamMembersCommand(), "Refresh team members command should exist");
        
        // Test command execution doesn't throw exceptions
        assertDoesNotThrow(() -> {
            viewModel.getLoadTeamMembersCommand().execute();
        }, "Load team members command should execute without exception");
        
        assertDoesNotThrow(() -> {
            viewModel.getNewTeamMemberCommand().execute();
        }, "New team member command should execute without exception");
        
        assertDoesNotThrow(() -> {
            viewModel.getRefreshTeamMembersCommand().execute();
        }, "Refresh team members command should execute without exception");
    }
    
    @Test
    @Order(9)
    public void testErrorMessageHandling() {
        LOGGER.info("Testing error message handling");
        
        // Set an error message in the ViewModel
        Platform.runLater(() -> {
            viewModel.setErrorMessage("Test team member error");
        });
        
        waitForFxEvents();
        sleep(100);
        
        // Verify error label is visible and shows the message
        Label errorLabel = lookup("#errorLabel").query();
        assertNotNull(errorLabel, "Error label should exist");
        
        org.frcpm.utils.TestUtils.waitUntil(() -> errorLabel.isVisible(), 2000);
        assertTrue(errorLabel.isVisible(), "Error label should be visible when there's an error");
        assertEquals("Test team member error", errorLabel.getText(), 
                    "Error label should show the error message");
        
        // Clear the error message
        Platform.runLater(() -> {
            viewModel.clearErrorMessage();
        });
        
        waitForFxEvents();
        sleep(100);
        
        // Verify error label is hidden
        org.frcpm.utils.TestUtils.waitUntil(() -> !errorLabel.isVisible(), 2000);
        assertFalse(errorLabel.isVisible(), "Error label should be hidden when error is cleared");
    }
    
    @Test
    @Order(10)
    public void testCurrentProjectProperty() {
        LOGGER.info("Testing current project property");
        
        // Verify current project is set
        assertEquals(testProject, viewModel.getCurrentProject(), "ViewModel should have correct current project");
        
        // Change the current project
        Platform.runLater(() -> {
            Project newProject = new Project("New Project", 
                LocalDate.now(), 
                LocalDate.now().plusDays(60), 
                LocalDate.now().plusDays(75));
            newProject.setId(2L);
            viewModel.setCurrentProject(newProject);
        });
        
        waitForFxEvents();
        sleep(100);
        
        // Verify the project label updated
        Label projectLabel = lookup("#projectLabel").query();
        org.frcpm.utils.TestUtils.waitUntil(() -> projectLabel.getText().contains("New Project"), 2000);
        assertTrue(projectLabel.getText().contains("New Project"), "Project label should update when current project changes");
    }
}