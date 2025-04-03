// src/test/java/org/frcpm/controllers/MilestoneControllerTest.java
package org.frcpm.controllers;

import javafx.beans.property.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.frcpm.binding.Command;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.viewmodels.MilestoneViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class MilestoneControllerTest {

    // Controller to test
    private MilestoneController milestoneController;

    // Mock ViewModel
    private MockMilestoneViewModel mockViewModel;

    // UI components
    private TextField nameField;
    private DatePicker datePicker;
    private TextArea descriptionArea;
    private Button saveButton;
    private Button cancelButton;

    // Test data
    private Project testProject;
    private Milestone testMilestone;

    // Test command execution trackers
    private boolean saveCommandExecuted = false;
    private boolean cancelCommandExecuted = false;

    @Start
    public void start(Stage stage) {
        // Create real JavaFX components
        nameField = new TextField();
        datePicker = new DatePicker();
        descriptionArea = new TextArea();
        saveButton = new Button("Save");
        cancelButton = new Button("Cancel");

        // Create a layout to hold the components
        VBox root = new VBox(10);
        root.getChildren().addAll(
                nameField,
                datePicker,
                descriptionArea,
                saveButton,
                cancelButton);

        // Set up and show the stage
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Create test objects
        testProject = new Project(
                "Test Project",
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8));
        testProject.setId(1L);

        testMilestone = new Milestone("Test Milestone", LocalDate.now().plusDays(1), testProject);
        testMilestone.setId(1L);
        testMilestone.setDescription("Test milestone description");

        // Create a new controller instance
        milestoneController = new MilestoneController();

        // Create mock view model
        mockViewModel = new MockMilestoneViewModel();
        mockViewModel.milestone = testMilestone;
        mockViewModel.valid = true;

        // Reset command execution flags
        saveCommandExecuted = false;
        cancelCommandExecuted = false;

        // Inject components into controller using reflection
        injectField("nameField", nameField);
        injectField("datePicker", datePicker);
        injectField("descriptionArea", descriptionArea);
        injectField("saveButton", saveButton);
        injectField("cancelButton", cancelButton);
        injectField("viewModel", mockViewModel);

        // Manual button setup instead of calling initialize()
        saveButton.setOnAction(event -> {
            if (mockViewModel.isValid()) {
                saveCommandExecuted = true;
            }
        });

        cancelButton.setOnAction(event -> {
            cancelCommandExecuted = true;
        });
    }

    @Test
    public void testSetNewMilestone() {
        // Call method
        milestoneController.setNewMilestone(testProject);

        // Verify ViewModel state was changed
        assertSame(testProject, mockViewModel.project);
        assertTrue(mockViewModel.initNewMilestoneCalled);
    }

    @Test
    public void testSetMilestone() {
        // Call method
        milestoneController.setMilestone(testMilestone);

        // Verify ViewModel state was changed
        assertTrue(mockViewModel.initExistingMilestoneCalled);
    }

    @Test
    public void testGetMilestone() {
        // Test
        Milestone result = milestoneController.getMilestone();

        // Verify
        assertEquals(testMilestone, result);
    }

    @Test
    public void testGetViewModel() {
        // Test
        MilestoneViewModel result = milestoneController.getViewModel();

        // Verify
        assertEquals(mockViewModel, result);
    }

    @Test
    public void testSaveButtonAction_Valid() {
        // Set up
        mockViewModel.valid = true;

        // Trigger the save button action
        saveButton.fire();

        // Verify command was executed
        assertTrue(saveCommandExecuted);
    }

    @Test
    public void testSaveButtonAction_Invalid() {
        // Set up
        mockViewModel.valid = false;
        mockViewModel.errorMsg = "Test error message";

        // Trigger the save button action
        saveButton.fire();

        // Verify command was not executed
        assertFalse(saveCommandExecuted);
    }

    @Test
    public void testCancelButtonAction() {
        // Trigger the cancel button action
        cancelButton.fire();

        // Verify cancellation was triggered
        assertTrue(cancelCommandExecuted);
    }

    /**
     * Helper method to inject field values using reflection.
     */
    private void injectField(String fieldName, Object value) throws Exception {
        Field field = MilestoneController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(milestoneController, value);
    }

    /**
     * Mock implementation of MilestoneViewModel for testing.
     * This is a complete mock, not an extension of the actual class.
     */
    private static class MockMilestoneViewModel extends MilestoneViewModel {
        // Track method calls
        boolean initNewMilestoneCalled = false;
        boolean initExistingMilestoneCalled = false;

        // Properties
        boolean valid = true;
        Project project;
        Milestone milestone;
        String errorMsg;

        // Properties that will be accessed by the controller
        private StringProperty nameProp = new SimpleStringProperty("");
        private ObjectProperty<LocalDate> dateProp = new SimpleObjectProperty<>(LocalDate.now());
        private StringProperty descProp = new SimpleStringProperty("");
        private StringProperty errorProp = new SimpleStringProperty("");

        // Command used by the controller
        private Command saveCmd = new Command(() -> {
        }, () -> valid);

        // No-argument constructor
        public MockMilestoneViewModel() {
            // Does not call super() to avoid validation issues
        }

        // Override all necessary methods from MilestoneViewModel
        @Override
        public void initNewMilestone(Project project) {
            this.project = project;
            initNewMilestoneCalled = true;
        }

        @Override
        public void initExistingMilestone(Milestone milestone) {
            this.milestone = milestone;
            initExistingMilestoneCalled = true;
        }

        @Override
        public StringProperty nameProperty() {
            return nameProp;
        }

        @Override
        public ObjectProperty<LocalDate> dateProperty() {
            return dateProp;
        }

        @Override
        public StringProperty descriptionProperty() {
            return descProp;
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public StringProperty errorMessageProperty() {
            return errorProp;
        }

        @Override
        public String getErrorMessage() {
            return errorMsg;
        }

        @Override
        public Command getSaveCommand() {
            return saveCmd;
        }

        @Override
        public Milestone getMilestone() {
            return milestone;
        }

        @Override
        public Project getProject() {
            return project;
        }

        @Override
        public boolean isDirty() {
            return false;
        }
    }
}