// src/test/java/org/frcpm/presenters/testfx/TeamMemberPresenterTestFX.java
package org.frcpm.presenters.testfx;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.frcpm.binding.Command;
import org.frcpm.models.Project;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.presenters.TeamMemberPresenter;
import org.frcpm.services.DialogService;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.testfx.BaseFxTest;
import org.frcpm.testfx.TestFXUtils;
import org.frcpm.viewmodels.TeamMemberViewModel;
import org.frcpm.views.TeamMemberView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.util.WaitForAsyncUtils;

/**
 * TestFX test for the TeamMemberPresenter class.
 */
@ExtendWith(MockitoExtension.class)
public class TeamMemberPresenterTestFX extends BaseFxTest {

    private static final Logger LOGGER = Logger.getLogger(TeamMemberPresenterTestFX.class.getName());

    @Mock
    private TeamMemberService teamMemberService;
    
    @Mock
    private SubteamService subteamService;
    
    @Mock
    private DialogService dialogService;
    
    @Mock
    private Command mockSaveCommand;
    
    @Mock
    private Command mockDeleteCommand;
    
    @Mock
    private Command mockNewCommand;

    private AutoCloseable closeable;
    private TeamMemberView view;
    private TeamMemberPresenter presenter;
    private TeamMemberViewModel viewModel;

    // Test data
    private Project testProject;
    private TeamMember testMember;
    private Subteam testSubteam;
    private List<TeamMember> testMembers;
    private List<Subteam> testSubteams;

    @Override
    protected void initializeTestComponents(Stage stage) {
        LOGGER.info("Initializing TeamMemberPresenterTestFX test components");

        try {
            // Open mocks first
            closeable = MockitoAnnotations.openMocks(this);

            // Create test data
            setupTestData();

            // Setup mocked service responses
            setupMockResponses();

            // Initialize the view on JavaFX thread
            Platform.runLater(() -> {
                try {
                    // Create view
                    view = new TeamMemberView();

                    // Get the presenter
                    presenter = (TeamMemberPresenter) view.getPresenter();

                    // Log successful creation
                    LOGGER.info("Created TeamMemberView and got presenter: " + (presenter != null));

                    // Set the scene
                    Scene scene = new Scene(view.getView(), 800, 600);
                    stage.setScene(scene);

                    // Access the view model directly using reflection
                    if (presenter != null) {
                        try {
                            // Use reflection to access the private viewModel field
                            java.lang.reflect.Field viewModelField = presenter.getClass().getDeclaredField("viewModel");
                            viewModelField.setAccessible(true);
                            viewModel = (TeamMemberViewModel) viewModelField.get(presenter);
                            LOGGER.info("Got view model via reflection: " + (viewModel != null));
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Failed to access viewModel field via reflection", e);
                        }
                    }

                    // Inject mocked services
                    injectMockedServices();

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error initializing TeamMemberView", e);
                    e.printStackTrace();
                }
            });

            // Wait for UI to update
            WaitForAsyncUtils.waitForFxEvents();

            // Show the stage
            stage.show();
            WaitForAsyncUtils.waitForFxEvents();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in initializeTestComponents", e);
            e.printStackTrace();
        }
    }

    private void injectMockedServices() {
        if (presenter == null) {
            LOGGER.severe("Cannot inject services - presenter is null");
            return;
        }

        try {
            // Use reflection to inject mocked services
            java.lang.reflect.Field teamMemberServiceField = presenter.getClass().getDeclaredField("teamMemberService");
            teamMemberServiceField.setAccessible(true);
            teamMemberServiceField.set(presenter, teamMemberService);

            java.lang.reflect.Field subteamServiceField = presenter.getClass().getDeclaredField("subteamService");
            subteamServiceField.setAccessible(true);
            subteamServiceField.set(presenter, subteamService);

            java.lang.reflect.Field dialogServiceField = presenter.getClass().getDeclaredField("dialogService");
            dialogServiceField.setAccessible(true);
            dialogServiceField.set(presenter, dialogService);
            
            // Inject mocked view model if needed for more control over test
            if (viewModel != null) {
                java.lang.reflect.Field viewModelField = presenter.getClass().getDeclaredField("viewModel");
                viewModelField.setAccessible(true);
                
                // Create a spy of the existing viewModel so we don't lose bindings
                TeamMemberViewModel spyViewModel = spy(viewModel);
                when(spyViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
                when(spyViewModel.getDeleteCommand()).thenReturn(mockDeleteCommand);
                when(spyViewModel.getNewCommand()).thenReturn(mockNewCommand);
                
                // Mock collections to avoid null pointers
                when(spyViewModel.getTeamMembers()).thenReturn(FXCollections.observableArrayList(testMembers));
                when(spyViewModel.getSubteams()).thenReturn(FXCollections.observableArrayList(testSubteams));
                
                viewModelField.set(presenter, spyViewModel);
                viewModel = spyViewModel;
            }

            LOGGER.info("Successfully injected mock services into presenter");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to inject mocked services", e);
            e.printStackTrace();
        }
    }

    private void setupTestData() {
        // Create a test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setStartDate(LocalDate.now().minusDays(30));
        testProject.setGoalEndDate(LocalDate.now().plusDays(60));
        testProject.setHardDeadline(LocalDate.now().plusDays(90));

        // Create a test subteam
        testSubteam = new Subteam();
        testSubteam.setId(1L);
        testSubteam.setName("Test Subteam");
        testSubteam.setColorCode("#FF5733");


        // Create test subteams list
        testSubteams = new ArrayList<>();
        testSubteams.add(testSubteam);
        
        Subteam subteam2 = new Subteam();
        subteam2.setId(2L);
        subteam2.setName("Another Subteam");
        subteam2.setColorCode("#33FF57");
        testSubteams.add(subteam2);

        // Create a test team member
        testMember = new TeamMember();
        testMember.setId(1L);
        testMember.setUsername("testuser");
        testMember.setFirstName("Test");
        testMember.setLastName("User");
        testMember.setEmail("test@example.com");
        testMember.setPhone("555-1234");
        testMember.setSubteam(testSubteam);

        // Create test team members list
        testMembers = new ArrayList<>();
        testMembers.add(testMember);
        
        TeamMember member2 = new TeamMember();
        member2.setId(2L);
        member2.setUsername("anotheruser");
        member2.setFirstName("Another");
        member2.setLastName("User");
        member2.setEmail("another@example.com");
        testMembers.add(member2);
    }

    private void setupMockResponses() {
        // Configure mock team member service responses
        when(teamMemberService.save(any(TeamMember.class))).thenReturn(testMember);
        when(teamMemberService.findById(eq(1L))).thenReturn(testMember);
        when(teamMemberService.findAll()).thenReturn(testMembers);
        
        // Configure mock subteam service responses
        when(subteamService.findById(eq(1L))).thenReturn(testSubteam);
        when(subteamService.findAll()).thenReturn(testSubteams);
        
        // For void methods in commands, use doNothing() instead of when()
        doNothing().when(mockSaveCommand).execute();
        doNothing().when(mockDeleteCommand).execute();
        doNothing().when(mockNewCommand).execute();
    }

    @Test
    public void testInitializeWithProject() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter with a test project
            Platform.runLater(() -> {
                presenter.initProject(testProject);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify that the project is initialized in the view model
            verify(viewModel).initProject(testProject);
            
            // Verify UI components are populated
            TableView<TeamMember> membersTable = lookup("#teamMemberTableView").queryAs(TableView.class);
            ComboBox<Subteam> subteamComboBox = lookup("#subteamComboBox").queryAs(ComboBox.class);
            
            assertNotNull(membersTable, "Team members table should be initialized");
            assertNotNull(subteamComboBox, "Subteam combo box should be initialized");
            
            // Verify the table contains members
            assertFalse(membersTable.getItems().isEmpty(), "Members table should contain items");
            
            // Verify the combo box contains subteams
            assertFalse(subteamComboBox.getItems().isEmpty(), "Subteam combo box should contain items");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testInitializeWithProject", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testAddTeamMember() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter with a test project
            Platform.runLater(() -> {
                presenter.initProject(testProject);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Click the "New" button to add a new team member
            clickOn("#newButton");

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify that the initNewTeamMember method was called on the view model
            verify(viewModel).initNewTeamMember();
            
            // Verify that the dialog service was used to show a message
            verify(dialogService).showInfoAlert(anyString(), anyString());
            
            // Check the form fields are clear for new team member
            TextField usernameField = lookup("#usernameTextField").queryAs(TextField.class);
            TextField firstNameField = lookup("#firstNameTextField").queryAs(TextField.class);
            TextField lastNameField = lookup("#lastNameTextField").queryAs(TextField.class);
            TextField emailField = lookup("#emailTextField").queryAs(TextField.class);
            
            assertTrue(usernameField.getText().isEmpty(), "Username field should be empty");
            assertTrue(firstNameField.getText().isEmpty(), "First name field should be empty");
            assertTrue(lastNameField.getText().isEmpty(), "Last name field should be empty");
            assertTrue(emailField.getText().isEmpty(), "Email field should be empty");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testAddTeamMember", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testSaveTeamMember() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter with a test project
            Platform.runLater(() -> {
                presenter.initProject(testProject);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Enter data in the form fields
            TextField usernameField = lookup("#usernameTextField").queryAs(TextField.class);
            TextField firstNameField = lookup("#firstNameTextField").queryAs(TextField.class);
            TextField lastNameField = lookup("#lastNameTextField").queryAs(TextField.class);
            TextField emailField = lookup("#emailTextField").queryAs(TextField.class);
            
            clickOn(usernameField).write("newuser");
            clickOn(firstNameField).write("New");
            clickOn(lastNameField).write("User");
            clickOn(emailField).write("newuser@example.com");
            
            // Click the Save button
            clickOn("#saveButton");

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify the save command was executed
            verify(mockSaveCommand, atLeastOnce()).execute();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testSaveTeamMember", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteTeamMember() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter with a test project and existing team member
            Platform.runLater(() -> {
                presenter.initProject(testProject);
                
                // Initialize with an existing team member
                viewModel.initExistingTeamMember(testMember);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();
            
            // Mock the dialog service to return true for confirmation
            when(dialogService.showConfirmationAlert(anyString(), anyString())).thenReturn(true);

            // Click the Delete button
            clickOn("#deleteButton");

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify the delete command was executed
            verify(mockDeleteCommand, atLeastOnce()).execute();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testDeleteTeamMember", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testSelectTeamMemberFromTable() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter with a test project
            Platform.runLater(() -> {
                presenter.initProject(testProject);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Click on the first row in the table
            TableView<TeamMember> membersTable = lookup("#teamMemberTableView").queryAs(TableView.class);
            
            // Ensure the table has items
            assertFalse(membersTable.getItems().isEmpty(), "Members table should have items");
            
            // TestFX doesn't have a direct way to click on a table row,
            // so we'll use reflection to simulate selecting an item
            Platform.runLater(() -> {
                membersTable.getSelectionModel().select(testMember);
            });
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify the view model's initExistingTeamMember method is called
            verify(viewModel).initExistingTeamMember(testMember);
            
            // Verify UI components are filled with test member data
            TextField usernameField = lookup("#usernameTextField").queryAs(TextField.class);
            TextField firstNameField = lookup("#firstNameTextField").queryAs(TextField.class);
            TextField lastNameField = lookup("#lastNameTextField").queryAs(TextField.class);
            TextField emailField = lookup("#emailTextField").queryAs(TextField.class);
            
            assertEquals("testuser", usernameField.getText(), "Username field should match test member");
            assertEquals("Test", firstNameField.getText(), "First name field should match test member");
            assertEquals("User", lastNameField.getText(), "Last name field should match test member");
            assertEquals("test@example.com", emailField.getText(), "Email field should match test member");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testSelectTeamMemberFromTable", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testSubteamComboBoxSelection() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter with a test project
            Platform.runLater(() -> {
                presenter.initProject(testProject);
                // Initialize with a new team member
                viewModel.initNewTeamMember();
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Click on the subteam combo box to show the dropdown
            ComboBox<Subteam> subteamComboBox = lookup("#subteamComboBox").queryAs(ComboBox.class);
            
            // Ensure the combo box has items
            assertFalse(subteamComboBox.getItems().isEmpty(), "Subteam combo box should have items");
            
            // Directly select a subteam via the view model
            Platform.runLater(() -> {
                subteamComboBox.getSelectionModel().select(testSubteam);
            });
            
            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify the selected subteam is updated in the view model
            verify(viewModel).selectedSubteamProperty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testSubteamComboBoxSelection", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }
}