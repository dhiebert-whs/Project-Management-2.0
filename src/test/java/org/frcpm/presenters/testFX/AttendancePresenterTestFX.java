package org.frcpm.presenters.testfx;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.presenters.AttendancePresenter;
import org.frcpm.services.AttendanceService;
import org.frcpm.services.DialogService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.testfx.BaseFxTest;
import org.frcpm.testfx.TestFXHeadlessConfig;
import org.frcpm.viewmodels.AttendanceViewModel;
import org.frcpm.views.AttendanceView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.util.WaitForAsyncUtils;

/**
 * TestFX test for the AttendancePresenter class.
 */
@ExtendWith(MockitoExtension.class)
public class AttendancePresenterTestFX extends BaseFxTest {

    @Mock
    private AttendanceService attendanceService;
    
    @Mock
    private MeetingService meetingService;
    
    @Mock
    private TeamMemberService teamMemberService;
    
    @Mock
    private DialogService dialogService;
    
    private AutoCloseable closeable;
    private AttendanceView view;
    private AttendancePresenter presenter;
    private AttendanceViewModel viewModel;
    
    // Test data
    private Meeting testMeeting;
    private List<TeamMember> testMembers;
    private List<Attendance> testAttendances;

    @Override
    protected void initializeTestComponents(Stage stage) {
        // Create view and presenter
        view = new AttendanceView();
        presenter = (AttendancePresenter) view.getPresenter();
        
        // Initialize the scene with our view
        Scene scene = new Scene(view.getView(), 800, 600);
        stage.setScene(scene);
        
        // Setup the presenter with mocked services
        injectMockedServices();
        
        // Create test data
        setupTestData();
        
        // Setup mocked service responses
        setupMockResponses();
    }
    
    @BeforeEach
    public void initMocks() {
        closeable = MockitoAnnotations.openMocks(this);
        
        // Allow access to the ViewModel for test verification
        if (presenter != null) {
            viewModel = presenter.getViewModel();
        }
    }
    
    private void injectMockedServices() {
        try {
            // Use reflection to inject mocked services
            java.lang.reflect.Field attendanceServiceField = presenter.getClass().getDeclaredField("attendanceService");
            attendanceServiceField.setAccessible(true);
            attendanceServiceField.set(presenter, attendanceService);
            
            java.lang.reflect.Field meetingServiceField = presenter.getClass().getDeclaredField("meetingService");
            meetingServiceField.setAccessible(true);
            meetingServiceField.set(presenter, meetingService);
            
            java.lang.reflect.Field teamMemberServiceField = presenter.getClass().getDeclaredField("teamMemberService");
            teamMemberServiceField.setAccessible(true);
            teamMemberServiceField.set(presenter, teamMemberService);
            
            java.lang.reflect.Field dialogServiceField = presenter.getClass().getDeclaredField("dialogService");
            dialogServiceField.setAccessible(true);
            dialogServiceField.set(presenter, dialogService);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to inject mocked services: " + e.getMessage());
        }
    }
    
    private void setupTestData() {
        // Create a test meeting
        Project testProject = new Project("Test Project", LocalDate.now(), LocalDate.now().plusWeeks(6), LocalDate.now().plusWeeks(8));
        testProject.setId(1L);
        
        testMeeting = new Meeting(LocalDate.now(), LocalTime.of(18, 0), LocalTime.of(20, 0), testProject);
        testMeeting.setId(1L);
        
        // Create test team members
        testMembers = new ArrayList<>();
        
        TeamMember member1 = new TeamMember("testuser1", "Test", "User1", "test1@example.com");
        member1.setId(1L);
        
        TeamMember member2 = new TeamMember("testuser2", "Test", "User2", "test2@example.com");
        member2.setId(2L);
        
        testMembers.add(member1);
        testMembers.add(member2);
        
        // Create test attendance records
        testAttendances = new ArrayList<>();
        
        Attendance attendance1 = new Attendance(testMeeting, member1, true);
        attendance1.setId(1L);
        attendance1.setArrivalTime(LocalTime.of(18, 0));
        attendance1.setDepartureTime(LocalTime.of(20, 0));
        
        Attendance attendance2 = new Attendance(testMeeting, member2, false);
        attendance2.setId(2L);
        
        testAttendances.add(attendance1);
        testAttendances.add(attendance2);
    }
    
    private void setupMockResponses() {
        // Configure the mock services
        when(meetingService.findById(anyLong())).thenReturn(testMeeting);
        when(teamMemberService.findAll()).thenReturn(testMembers);
        when(attendanceService.findByMeeting(any(Meeting.class))).thenReturn(testAttendances);
    }
    
    @Test
    public void testAttendanceTableInitialization() {
        // Set the meeting for the presenter
        presenter.setMeeting(testMeeting);
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Get the table view and verify it has the correct number of rows
        TableView<?> attendanceTable = lookup("#attendanceTable").queryTableView();
        assertEquals(2, attendanceTable.getItems().size(), "Table should have 2 attendance records");
    }
    
    @Test
    public void testToggleAttendance() {
        // Set the meeting for the presenter
        presenter.setMeeting(testMeeting);
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Get the table view
        TableView<?> attendanceTable = lookup("#attendanceTable").queryTableView();
        
        // Select the second row (absent member)
        attendanceTable.getSelectionModel().select(1);
        
        // Get the present column and click on the checkbox
        TableColumn<?, ?> presentColumn = attendanceTable.getColumns().get(2); // Index 2 should be the "Present" column
        
        // Find checkbox in selected row of present column and click it
        Node checkbox = lookup(".check-box").nth(2).query();
        clickOn(checkbox);
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Verify that the attendance service was called to update the attendance
        verify(attendanceService).updateAttendance(eq(2L), eq(true), any(), any());
    }
    
    @Test
    public void testSetTimeButtonWhenMemberSelected() {
        // Set the meeting for the presenter
        presenter.setMeeting(testMeeting);
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Get the table view
        TableView<?> attendanceTable = lookup("#attendanceTable").queryAs(TableView.class);
        
        // Select the first row (present member)
        attendanceTable.getSelectionModel().select(0);
        
        // Set arrival and departure times
        TextField arrivalTimeField = lookup("#arrivalTimeField").queryTextInputControl();
        TextField departureTimeField = lookup("#departureTimeField").queryTextInputControl();
        
        // Clear fields and enter new times
        arrivalTimeField.clear();
        clickOn(arrivalTimeField).write("18:15");
        
        departureTimeField.clear();
        clickOn(departureTimeField).write("19:45");
        
        // Click set time button
        clickOn("Set");
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Verify that the attendance service was called to update the attendance
        verify(attendanceService).updateAttendance(
            eq(1L), 
            eq(true), 
            eq(LocalTime.of(18, 15)), 
            eq(LocalTime.of(19, 45))
        );
    }
    
    @Test
    public void testSaveButton() {
        // Set the meeting for the presenter
        presenter.setMeeting(testMeeting);
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Click the save button
        clickOn("#saveButton");
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Verify that the dialog service was called to show confirmation
        verify(dialogService).showInformation(anyString(), anyString());
    }
}