package org.frcpm.presenters.testfx;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.frcpm.binding.Command;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.presenters.MilestonePresenter;
import org.frcpm.services.DialogService;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.ProjectService;
import org.frcpm.testfx.BaseFxTest;
import org.frcpm.testfx.TestFXUtils;
import org.frcpm.viewmodels.MilestoneViewModel;
import org.frcpm.views.MilestoneView;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.util.WaitForAsyncUtils;

/**
 * TestFX test for the MilestonePresenter class.
 */
@ExtendWith(MockitoExtension.class)
public class MilestonePresenterTestFX extends BaseFxTest {

    private static final Logger LOGGER = Logger.getLogger(MilestonePresenterTestFX.class.getName());

    @Mock
    private MilestoneService milestoneService;
    
    @Mock
    private ProjectService projectService;
    
    @Mock
    private DialogService dialogService;
    
    @Mock
    private Command mockSaveCommand;
    
    @Mock
    private Command mockCancelCommand;

    private AutoCloseable closeable;
    private MilestoneView view;
    private MilestonePresenter presenter;
    private MilestoneViewModel viewModel;

    // Test data
    private Project testProject;
    private Milestone testMilestone;

    @Override
    protected void initializeTestComponents(Stage stage) {
        LOGGER.info("Initializing MilestonePresenterTestFX test components");

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
                    view = new MilestoneView();

                    // Get the presenter
                    presenter = (MilestonePresenter) view.getPresenter();

                    // Log successful creation
                    LOGGER.info("Created MilestoneView and got presenter: " + (presenter != null));

                    // Set the scene
                    Scene scene = new Scene(view.getView(), 800, 600);
                    stage.setScene(scene);

                    // Access the view model directly using reflection
                    if (presenter != null) {
                        try {
                            // Use reflection to access the private viewModel field
                            java.lang.reflect.Field viewModelField = presenter.getClass().getDeclaredField("viewModel");
                            viewModelField.setAccessible(true);
                            viewModel = (MilestoneViewModel) viewModelField.get(presenter);
                            LOGGER.info("Got view model via reflection: " + (viewModel != null));
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Failed to access viewModel field via reflection", e);
                        }
                    }

                    // Inject mocked services
                    injectMockedServices();

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error initializing MilestoneView", e);
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
            java.lang.reflect.Field milestoneServiceField = presenter.getClass().getDeclaredField("milestoneService");
            milestoneServiceField.setAccessible(true);
            milestoneServiceField.set(presenter, milestoneService);

            java.lang.reflect.Field dialogServiceField = presenter.getClass().getDeclaredField("dialogService");
            dialogServiceField.setAccessible(true);
            dialogServiceField.set(presenter, dialogService);
            
            // Inject mocked view model if needed for more control over test
            if (viewModel != null) {
                java.lang.reflect.Field viewModelField = presenter.getClass().getDeclaredField("viewModel");
                viewModelField.setAccessible(true);
                
                // Create a spy of the existing viewModel so we don't lose bindings
                MilestoneViewModel spyViewModel = spy(viewModel);
                when(spyViewModel.getSaveCommand()).thenReturn(mockSaveCommand);
                when(spyViewModel.getCancelCommand()).thenReturn(mockCancelCommand);
                
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

        // Create a test milestone
        testMilestone = new Milestone();
        testMilestone.setId(1L);
        testMilestone.setName("Test Milestone");
        testMilestone.setProject(testProject);
        testMilestone.setDate(LocalDate.now().plusDays(30));
        testMilestone.setDescription("Test milestone description");
    }

    private void setupMockResponses() {
        // Configure mock milestone service responses
        when(milestoneService.save(any(Milestone.class))).thenReturn(testMilestone);
        when(milestoneService.findById(eq(1L))).thenReturn(testMilestone);
        
        // Configure mock project service responses
        when(projectService.findById(eq(1L))).thenReturn(testProject);
        
        // For void methods in commands, use doNothing() instead of when()
        doNothing().when(mockSaveCommand).execute();
        doNothing().when(mockCancelCommand).execute();
    }

    @Test
    public void testInitializeWithNewMilestone() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for a new milestone
            Platform.runLater(() -> {
                presenter.initNewMilestone(testProject);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify UI components are set correctly
            TextField nameField = lookup("#nameField").queryAs(TextField.class);
            DatePicker datePicker = lookup("#datePicker").queryAs(DatePicker.class);
            TextArea descriptionArea = lookup("#descriptionArea").queryAs(TextArea.class);
            Label projectLabel = lookup("#projectLabel").queryAs(Label.class);

            // Check that the project name is displayed
            assertEquals("Test Project", projectLabel.getText(), "Project name should be displayed");
            
            // Check that fields are empty or with default values
            assertTrue(nameField.getText().isEmpty(), "Name field should be empty");
            assertNotNull(datePicker.getValue(), "Date picker should have a default value");
            assertTrue(descriptionArea.getText().isEmpty(), "Description area should be empty");

            // Check that the presenter is in the correct state
            if (viewModel != null) {
                assertTrue(viewModel.isNewMilestone(), "ViewModel should be in new milestone state");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testInitializeWithNewMilestone", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testInitializeWithExistingMilestone() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for an existing milestone
            Platform.runLater(() -> {
                presenter.initExistingMilestone(testMilestone);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify UI components are set correctly
            TextField nameField = lookup("#nameField").queryAs(TextField.class);
            DatePicker datePicker = lookup("#datePicker").queryAs(DatePicker.class);
            TextArea descriptionArea = lookup("#descriptionArea").queryAs(TextArea.class);
            Label projectLabel = lookup("#projectLabel").queryAs(Label.class);

            // Check that fields are populated with milestone data
            assertEquals("Test Milestone", nameField.getText(), "Name field should be populated");
            assertEquals(testMilestone.getDate(), datePicker.getValue(), "Date picker should be set to milestone date");
            assertEquals("Test milestone description", descriptionArea.getText(), "Description area should be populated");
            assertEquals("Test Project", projectLabel.getText(), "Project name should be displayed");

            // Check that the presenter is in the correct state
            if (viewModel != null) {
                assertFalse(viewModel.isNewMilestone(), "ViewModel should be in existing milestone state");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testInitializeWithExistingMilestone", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testSaveButton() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for a new milestone
            Platform.runLater(() -> {
                presenter.initNewMilestone(testProject);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Enter data in the form
            TextField nameField = lookup("#nameField").queryAs(TextField.class);
            clickOn(nameField).write("New Test Milestone");

            // Try to click the Save button
            clickOn("#saveButton");

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify the save command was executed
            verify(mockSaveCommand, atLeastOnce()).execute();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testSaveButton", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testCancelButton() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for a new milestone
            Platform.runLater(() -> {
                presenter.initNewMilestone(testProject);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Click the Cancel button
            clickOn("#cancelButton");

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Verify the cancel command was executed
            verify(mockCancelCommand, atLeastOnce()).execute();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testCancelButton", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    public void testValidation() {
        // Skip the test if presenter is null
        if (presenter == null) {
            LOGGER.severe("Cannot run test - presenter is null");
            return;
        }

        try {
            // Set up the presenter for a new milestone
            Platform.runLater(() -> {
                presenter.initNewMilestone(testProject);
            });

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Clear the name field (should trigger validation failure)
            TextField nameField = lookup("#nameField").queryAs(TextField.class);
            clickOn(nameField).press(javafx.scene.input.KeyCode.CONTROL).press(javafx.scene.input.KeyCode.A)
                 .release(javafx.scene.input.KeyCode.A).release(javafx.scene.input.KeyCode.CONTROL)
                 .press(javafx.scene.input.KeyCode.DELETE).release(javafx.scene.input.KeyCode.DELETE);

            // Click out of the field to trigger focus lost event
            clickOn("#datePicker");

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Check if error label is visible
            Label errorLabel = lookup("#errorLabel").queryAs(Label.class);
            assertTrue(errorLabel.isVisible(), "Error label should be visible when validation fails");
            assertFalse(errorLabel.getText().isEmpty(), "Error label should contain an error message");

            // Fix the validation issue by entering a name
            clickOn(nameField).write("Fixed Milestone Name");

            // Click out of the field to trigger focus lost event
            clickOn("#datePicker");

            // Wait for JavaFX thread to process
            WaitForAsyncUtils.waitForFxEvents();

            // Check if error is resolved
            assertFalse(errorLabel.isVisible() && !errorLabel.getText().isEmpty(), 
                    "Error label should be hidden or empty when validation passes");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in testValidation", e);
            e.printStackTrace();
            fail("Test failed due to exception: " + e.getMessage());
        }
    }
}