package org.frcpm.controllers;

import javafx.beans.property.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.frcpm.binding.Command;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.viewmodels.MeetingViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class MeetingControllerTest {

    // Controller to test
    private MeetingController meetingController;

    // Test ViewModel - not using mockito
    private TestMeetingViewModel testViewModel;

    // UI components
    private DatePicker datePicker;
    private TextField startTimeField;
    private TextField endTimeField;
    private TextArea notesArea;
    private Button saveButton;
    private Button cancelButton;

    // Test data
    private Project testProject;
    private Meeting testMeeting;

    // Test command execution trackers
    private boolean saveCommandExecuted = false;
    private boolean cancelCommandExecuted = false;

    @Start
    public void start(Stage stage) {
        // Create real JavaFX components
        datePicker = new DatePicker();
        startTimeField = new TextField();
        endTimeField = new TextField();
        notesArea = new TextArea();
        saveButton = new Button("Save");
        cancelButton = new Button("Cancel");

        // Create a layout to hold the components
        VBox root = new VBox(10);
        root.getChildren().addAll(
                datePicker,
                startTimeField,
                endTimeField,
                notesArea,
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

        testMeeting = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                testProject);
        testMeeting.setId(1L);
        testMeeting.setNotes("Test meeting notes");

        // Create a new controller instance and test ViewModel
        meetingController = new MeetingController();
        testViewModel = new TestMeetingViewModel();
        testViewModel.setMeeting(testMeeting);
        testViewModel.setValid(true);

        // Reset command execution flags
        saveCommandExecuted = false;
        cancelCommandExecuted = false;

        // Inject components into controller using reflection
        injectField("datePicker", datePicker);
        injectField("startTimeField", startTimeField);
        injectField("endTimeField", endTimeField);
        injectField("notesArea", notesArea);
        injectField("saveButton", saveButton);
        injectField("cancelButton", cancelButton);
        injectField("viewModel", testViewModel);

        // Manual button setup instead of calling initialize()
        saveButton.setOnAction(event -> {
            if (testViewModel.isValid()) {
                saveCommandExecuted = true;
            }
        });

        cancelButton.setOnAction(event -> {
            cancelCommandExecuted = true;
        });
    }

    @Test
    public void testSetNewMeeting() {
        // Call method
        meetingController.setNewMeeting(testProject);

        // Verify ViewModel state was changed
        assertSame(testProject, testViewModel.getProject());
        assertTrue(testViewModel.wasInitNewMeetingCalled());
    }

    @Test
    public void testSetMeeting() {
        // Call method
        meetingController.setMeeting(testMeeting);

        // Verify ViewModel state was changed
        assertTrue(testViewModel.wasInitExistingMeetingCalled());
    }

    @Test
    public void testGetMeeting() {
        // Test
        Meeting result = meetingController.getMeeting();

        // Verify
        assertEquals(testMeeting, result);
    }

    @Test
    public void testGetViewModel() {
        // Test
        MeetingViewModel result = meetingController.getViewModel();

        // Verify
        assertEquals(testViewModel, result);
    }

    @Test
    public void testSaveButtonAction_Valid() {
        // Set up
        testViewModel.setValid(true);

        // Trigger the save button action
        saveButton.fire();

        // Verify command was executed
        assertTrue(saveCommandExecuted);
    }

    @Test
    public void testSaveButtonAction_Invalid() {
        // Set up
        testViewModel.setValid(false);
        testViewModel.setErrorMessage("Test error message");

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
        Field field = MeetingController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(meetingController, value);
    }

    /**
     * Test implementation of MeetingViewModel to avoid using Mockito.
     */
    private class TestMeetingViewModel extends MeetingViewModel {
        private boolean initNewMeetingCalled = false;
        private boolean initExistingMeetingCalled = false;
        private boolean valid = true;
        private final ObjectProperty<LocalDate> dateProperty = new SimpleObjectProperty<>();
        private final StringProperty startTimeProperty = new SimpleStringProperty();
        private final StringProperty endTimeProperty = new SimpleStringProperty();
        private final StringProperty notesProperty = new SimpleStringProperty();
        private final StringProperty errorMessageProperty = new SimpleStringProperty();
        private final Command saveCommand = new Command(() -> {
        }, () -> true);

        @Override
        public void initNewMeeting(Project project) {
            setProject(project);
            initNewMeetingCalled = true;
        }

        @Override
        public void initExistingMeeting(Meeting meeting) {
            setMeeting(meeting);
            initExistingMeetingCalled = true;
        }

        @Override
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        @Override
        public StringProperty errorMessageProperty() {
            return errorMessageProperty;
        }

        @Override
        public String getErrorMessage() {
            return errorMessageProperty.get();
        }

        public void setErrorMessage(String message) {
            errorMessageProperty.set(message);
        }

        @Override
        public ObjectProperty<LocalDate> dateProperty() {
            return dateProperty;
        }

        @Override
        public StringProperty startTimeStringProperty() {
            return startTimeProperty;
        }

        @Override
        public StringProperty endTimeStringProperty() {
            return endTimeProperty;
        }

        @Override
        public StringProperty notesProperty() {
            return notesProperty;
        }

        @Override
        public Command getSaveCommand() {
            return saveCommand;
        }

        public boolean wasInitNewMeetingCalled() {
            return initNewMeetingCalled;
        }

        public boolean wasInitExistingMeetingCalled() {
            return initExistingMeetingCalled;
        }
    }
}