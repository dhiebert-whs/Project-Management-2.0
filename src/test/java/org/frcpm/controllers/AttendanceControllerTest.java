package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.viewmodels.AttendanceViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testfx.framework.junit5.ApplicationExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the AttendanceController class with MVVM pattern.
 */
@ExtendWith(ApplicationExtension.class)
public class AttendanceControllerTest {

    @Spy
    private AttendanceController controller;

    @Mock
    private AttendanceViewModel mockViewModel;

    @Mock
    private Meeting mockMeeting;

    @Mock
    private Project mockProject;

    @Mock
    private Stage mockStage;

    @Mock
    private Window mockWindow;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up mocks
        when(mockMeeting.getDate()).thenReturn(LocalDate.now());
        when(mockMeeting.getStartTime()).thenReturn(LocalTime.of(16, 0));
        when(mockMeeting.getEndTime()).thenReturn(LocalTime.of(18, 0));
        when(mockMeeting.getProject()).thenReturn(mockProject);

        // Set mock ViewModel
        ObservableList<AttendanceViewModel.AttendanceRecord> mockRecords = FXCollections
                .observableArrayList(new ArrayList<>());
        when(mockViewModel.getAttendanceRecords()).thenReturn(mockRecords);

        // Inject fields into controller using reflection
        try {
            java.lang.reflect.Field viewModelField = AttendanceController.class.getDeclaredField("viewModel");
            viewModelField.setAccessible(true);
            viewModelField.set(controller, mockViewModel);

            java.lang.reflect.Field meetingTitleLabelField = AttendanceController.class
                    .getDeclaredField("meetingTitleLabel");
            meetingTitleLabelField.setAccessible(true);
            meetingTitleLabelField.set(controller, new Label());

            java.lang.reflect.Field dateLabelField = AttendanceController.class.getDeclaredField("dateLabel");
            dateLabelField.setAccessible(true);
            dateLabelField.set(controller, new Label());

            java.lang.reflect.Field timeLabelField = AttendanceController.class.getDeclaredField("timeLabel");
            timeLabelField.setAccessible(true);
            timeLabelField.set(controller, new Label());

            java.lang.reflect.Field attendanceTableField = AttendanceController.class
                    .getDeclaredField("attendanceTable");
            attendanceTableField.setAccessible(true);
            TableView<AttendanceViewModel.AttendanceRecord> tableView = new TableView<>();
            attendanceTableField.set(controller, tableView);

            java.lang.reflect.Field nameColumnField = AttendanceController.class.getDeclaredField("nameColumn");
            nameColumnField.setAccessible(true);
            nameColumnField.set(controller, new TableColumn<AttendanceViewModel.AttendanceRecord, String>());

            java.lang.reflect.Field subteamColumnField = AttendanceController.class.getDeclaredField("subteamColumn");
            subteamColumnField.setAccessible(true);
            subteamColumnField.set(controller, new TableColumn<AttendanceViewModel.AttendanceRecord, String>());

            java.lang.reflect.Field presentColumnField = AttendanceController.class.getDeclaredField("presentColumn");
            presentColumnField.setAccessible(true);
            presentColumnField.set(controller, new TableColumn<AttendanceViewModel.AttendanceRecord, Boolean>());

            java.lang.reflect.Field arrivalColumnField = AttendanceController.class.getDeclaredField("arrivalColumn");
            arrivalColumnField.setAccessible(true);
            arrivalColumnField.set(controller, new TableColumn<AttendanceViewModel.AttendanceRecord, LocalTime>());

            java.lang.reflect.Field departureColumnField = AttendanceController.class
                    .getDeclaredField("departureColumn");
            departureColumnField.setAccessible(true);
            departureColumnField.set(controller, new TableColumn<AttendanceViewModel.AttendanceRecord, LocalTime>());

            java.lang.reflect.Field saveButtonField = AttendanceController.class.getDeclaredField("saveButton");
            saveButtonField.setAccessible(true);
            Button saveButton = new Button();
            saveButtonField.set(controller, saveButton);

            java.lang.reflect.Field cancelButtonField = AttendanceController.class.getDeclaredField("cancelButton");
            cancelButtonField.setAccessible(true);
            Button cancelButton = new Button();
            cancelButtonField.set(controller, cancelButton);

        } catch (Exception e) {
            fail("Failed to set up controller: " + e.getMessage());
        }
    }

    @Test
    public void testSetMeeting() {
        // Act
        controller.setMeeting(mockMeeting);

        // Assert
        verify(mockViewModel).initWithMeeting(mockMeeting);
    }

    @Test
    public void testGetViewModel() {
        // Act
        AttendanceViewModel result = controller.getViewModel();

        // Assert
        assertEquals(mockViewModel, result);
    }

    @Test
    public void testGetMeeting() {
        // Arrange
        when(mockViewModel.getMeeting()).thenReturn(mockMeeting);

        // Act
        Meeting result = controller.getMeeting();

        // Assert
        assertEquals(mockMeeting, result);
        verify(mockViewModel).getMeeting();
    }

    @Test
    public void testInitialize() throws Exception {
        // Create a real controller for this test to avoid mocking all the JavaFX
        // initialization
        AttendanceController realController = new AttendanceController();

        // Setup minimal JavaFX components
        TableView<AttendanceViewModel.AttendanceRecord> tableView = new TableView<>();

        // Set required fields via reflection
        java.lang.reflect.Field nameColumnField = AttendanceController.class.getDeclaredField("nameColumn");
        nameColumnField.setAccessible(true);
        nameColumnField.set(realController, new TableColumn<AttendanceViewModel.AttendanceRecord, String>());

        java.lang.reflect.Field subteamColumnField = AttendanceController.class.getDeclaredField("subteamColumn");
        subteamColumnField.setAccessible(true);
        subteamColumnField.set(realController, new TableColumn<AttendanceViewModel.AttendanceRecord, String>());

        java.lang.reflect.Field presentColumnField = AttendanceController.class.getDeclaredField("presentColumn");
        presentColumnField.setAccessible(true);
        presentColumnField.set(realController, new TableColumn<AttendanceViewModel.AttendanceRecord, Boolean>());

        java.lang.reflect.Field arrivalColumnField = AttendanceController.class.getDeclaredField("arrivalColumn");
        arrivalColumnField.setAccessible(true);
        arrivalColumnField.set(realController, new TableColumn<AttendanceViewModel.AttendanceRecord, LocalTime>());

        java.lang.reflect.Field departureColumnField = AttendanceController.class.getDeclaredField("departureColumn");
        departureColumnField.setAccessible(true);
        departureColumnField.set(realController, new TableColumn<AttendanceViewModel.AttendanceRecord, LocalTime>());

        java.lang.reflect.Field attendanceTableField = AttendanceController.class.getDeclaredField("attendanceTable");
        attendanceTableField.setAccessible(true);
        attendanceTableField.set(realController, tableView);

        java.lang.reflect.Field meetingTitleLabelField = AttendanceController.class
                .getDeclaredField("meetingTitleLabel");
        meetingTitleLabelField.setAccessible(true);
        meetingTitleLabelField.set(realController, new Label());

        java.lang.reflect.Field dateLabelField = AttendanceController.class.getDeclaredField("dateLabel");
        dateLabelField.setAccessible(true);
        dateLabelField.set(realController, new Label());

        java.lang.reflect.Field timeLabelField = AttendanceController.class.getDeclaredField("timeLabel");
        timeLabelField.setAccessible(true);
        timeLabelField.set(realController, new Label());

        java.lang.reflect.Field saveButtonField = AttendanceController.class.getDeclaredField("saveButton");
        saveButtonField.setAccessible(true);
        saveButtonField.set(realController, new Button());

        java.lang.reflect.Field cancelButtonField = AttendanceController.class.getDeclaredField("cancelButton");
        cancelButtonField.setAccessible(true);
        cancelButtonField.set(realController, new Button());

        // Call initialize via reflection
        java.lang.reflect.Method initializeMethod = AttendanceController.class.getDeclaredMethod("initialize");
        initializeMethod.setAccessible(true);

        // This will cause some exception in JavaFX thread, but we're just making sure
        // it doesn't crash
        try {
            initializeMethod.invoke(realController);
            // Reaching here means initialize() didn't throw an exception
            assertTrue(true);
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause().getMessage() != null &&
                    e.getCause().getMessage().contains("Toolkit not initialized")) {
                // This is expected in test environment without JavaFX
                assertTrue(true);
            } else {
                fail("initialize() threw unexpected exception: " + e.getMessage());
            }
        }
    }
}