package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.frcpm.controllers.AttendanceController.TeamMemberAttendanceRecord;
import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.TeamMember;
import org.frcpm.services.AttendanceService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.TeamMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AttendanceControllerTest {

    @Mock
    private AttendanceService attendanceService;
    
    @Mock
    private MeetingService meetingService;
    
    @Mock
    private TeamMemberService teamMemberService;
    
    @InjectMocks
    private AttendanceController attendanceController;
    
    @Mock
    private Label meetingTitleLabel;
    
    @Mock
    private Label dateLabel;
    
    @Mock
    private Label timeLabel;
    
    @Mock
    private TableView<TeamMemberAttendanceRecord> attendanceTable;
    
    @Mock
    private TableColumn<TeamMemberAttendanceRecord, String> nameColumn;
    
    @Mock
    private TableColumn<TeamMemberAttendanceRecord, String> subteamColumn;
    
    @Mock
    private TableColumn<TeamMemberAttendanceRecord, Boolean> presentColumn;
    
    @Mock
    private TableColumn<TeamMemberAttendanceRecord, LocalTime> arrivalColumn;
    
    @Mock
    private TableColumn<TeamMemberAttendanceRecord, LocalTime> departureColumn;
    
    @Mock
    private TextField arrivalTimeField;
    
    @Mock
    private TextField departureTimeField;
    
    @Mock
    private Button saveButton;
    
    @Mock
    private Button cancelButton;
    
    @Mock
    private Stage mockStage;
    
    @Mock
    private ActionEvent mockEvent;
    
    private Project testProject;
    private Meeting testMeeting;
    private List<TeamMember> testMembers;
    private List<Attendance> testAttendances;
    private ObservableList<TeamMemberAttendanceRecord> testRecords;

    @BeforeEach
    public void setUp() {
        // Create test project
        testProject = new Project(
                "Test Project",
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8)
        );
        testProject.setId(1L);
        
        // Create test meeting
        testMeeting = new Meeting(
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                testProject
        );
        testMeeting.setId(1L);
        
        // Create test members
        TeamMember member1 = new TeamMember("testuser1", "Test", "User1", "test1@example.com");
        member1.setId(1L);
        
        TeamMember member2 = new TeamMember("testuser2", "Test", "User2", "test2@example.com");
        member2.setId(2L);
        
        testMembers = Arrays.asList(member1, member2);
        
        // Create test attendances
        Attendance attendance1 = new Attendance(testMeeting, member1, true);
        attendance1.setId(1L);
        attendance1.setArrivalTime(LocalTime.of(18, 0));
        attendance1.setDepartureTime(LocalTime.of(20, 0));
        
        testAttendances = Arrays.asList(attendance1);
        
        // Create test records
        testRecords = FXCollections.observableArrayList();
        testRecords.add(new TeamMemberAttendanceRecord(member1, attendance1));
        testRecords.add(new TeamMemberAttendanceRecord(member2, null));
        
        // Initialize controller by setting the mock fields
        //attendanceController.meetingTitleLabel = meetingTitleLabel;
        // Use reflection to set private fields
        //java.lang.reflect.Field 

    try {
        // Use reflection to set private fields
        java.lang.reflect.Field field;

        field = AttendanceController.class.getDeclaredField("meetingTitleLabel");
        field.setAccessible(true);
        field.set(attendanceController, meetingTitleLabel);
        
        field = AttendanceController.class.getDeclaredField("dateLabel");
        field.setAccessible(true);
        field.set(attendanceController, dateLabel);
        
        field = AttendanceController.class.getDeclaredField("timeLabel");
        field.setAccessible(true);
        field.set(attendanceController, timeLabel);
        
        field = AttendanceController.class.getDeclaredField("attendanceTable");
        field.setAccessible(true);
        field.set(attendanceController, attendanceTable);
        
        field = AttendanceController.class.getDeclaredField("nameColumn");
        field.setAccessible(true);
        field.set(attendanceController, nameColumn);
        
        field = AttendanceController.class.getDeclaredField("subteamColumn");
        field.setAccessible(true);
        field.set(attendanceController, subteamColumn);
        
        field = AttendanceController.class.getDeclaredField("presentColumn");
        field.setAccessible(true);
        field.set(attendanceController, presentColumn);
        
        field = AttendanceController.class.getDeclaredField("arrivalColumn");
        field.setAccessible(true);
        field.set(attendanceController, arrivalColumn);
        
        field = AttendanceController.class.getDeclaredField("departureColumn");
        field.setAccessible(true);
        field.set(attendanceController, departureColumn);
        
        field = AttendanceController.class.getDeclaredField("arrivalTimeField");
        field.setAccessible(true);
        field.set(attendanceController, arrivalTimeField);
        
        field = AttendanceController.class.getDeclaredField("departureTimeField");
        field.setAccessible(true);
        field.set(attendanceController, departureTimeField);
        
        field = AttendanceController.class.getDeclaredField("saveButton");
        field.setAccessible(true);
        field.set(attendanceController, saveButton);
        
        field = AttendanceController.class.getDeclaredField("cancelButton");
        field.setAccessible(true);
        field.set(attendanceController, cancelButton);
    } catch (Exception e) {
        fail("Failed to set fields using reflection: " + e.getMessage());
    }
        
        // Set attendanceRecords field using reflection
        try {
            java.lang.reflect.Field recordsField = AttendanceController.class.getDeclaredField("attendanceRecords");
            recordsField.setAccessible(true);
            recordsField.set(attendanceController, testRecords);
        } catch (Exception e) {
            fail("Failed to set attendanceRecords field: " + e.getMessage());
        }
        
        // Mock service behavior
        when(teamMemberService.findAll()).thenReturn(testMembers);
        when(attendanceService.findByMeeting(testMeeting)).thenReturn(testAttendances);
        when(attendanceService.createAttendance(anyLong(), anyLong(), anyBoolean())).thenReturn(attendance1);
        when(attendanceService.updateAttendance(anyLong(), anyBoolean(), any(), any())).thenReturn(attendance1);
        
        // Mock UI component behavior
        when(attendanceController.getAttendanceTable().getItems()).thenReturn(testRecords);
        when(attendanceController.getSaveButton().getScene()).thenReturn(mock(javafx.scene.Scene.class));
        when(attendanceController.getSaveButton().getScene().getWindow()).thenReturn(mockStage);
    }

    @Test
    public void testInitialize() {
        // Call initialize via reflection (since it's private)
        try {
            java.lang.reflect.Method initMethod = AttendanceController.class.getDeclaredMethod("initialize");
            initMethod.setAccessible(true);
            initMethod.invoke(attendanceController);
            
            // Verify that the table columns are set up
            verify(attendanceController.getNameColumn()).setCellValueFactory(any());
            verify(attendanceController.getSubteamColumn()).setCellValueFactory(any());
            verify(attendanceController.getPresentColumn()).setCellValueFactory(any());
            verify(attendanceController.getArrivalColumn()).setCellValueFactory(any());
            verify(attendanceController.getDepartureColumn()).setCellValueFactory(any());
            
            // Verify that button actions are set
            verify(attendanceController.getSaveButton()).setOnAction(any());
            verify(attendanceController.getCancelButton()).setOnAction(any());
            
            // Verify that table items are set
            verify(attendanceController.getAttendanceTable()).setItems(testRecords);
            
        } catch (Exception e) {
            fail("Exception during initialize: " + e.getMessage());
        }
    }

    @Test
    public void testSetMeeting() {
        // Test setting the meeting
        attendanceController.setMeeting(testMeeting);
        
        // Verify the meeting is set
        assertEquals(testMeeting, attendanceController.getMeeting());
        
        // Verify that attendance data is loaded
        verify(attendanceController.getMeetingTitleLabel()).setText("Meeting Attendance");
        verify(attendanceController.getDateLabel()).setText(testMeeting.getDate().toString());
        verify(attendanceController.getTimeLabel()).setText(testMeeting.getStartTime() + " - " + testMeeting.getEndTime());
        
        // Verify that team members and attendance records are loaded
        verify(teamMemberService).findAll();
        verify(attendanceService).findByMeeting(testMeeting);
    }
    
    @Test
    public void testLoadAttendanceData() {
        // Set up a meeting first
        attendanceController.setMeeting(testMeeting);
        
        // Call the method to test
        try {
            java.lang.reflect.Method loadDataMethod = AttendanceController.class.getDeclaredMethod("loadAttendanceData");
            loadDataMethod.setAccessible(true);
            loadDataMethod.invoke(attendanceController);
            
            // Verify that team members and attendance records are loaded
            verify(teamMemberService, times(2)).findAll();
            verify(attendanceService, times(2)).findByMeeting(testMeeting);
            
        } catch (Exception e) {
            fail("Exception during loadAttendanceData: " + e.getMessage());
        }
    }
    
    @Test
    public void testHandleSave() {
        // Set up a meeting
        attendanceController.setMeeting(testMeeting);
        
        // Test saving
        attendanceController.testHandleSave(mockEvent);
        
        // Since we have 2 members, one present and one absent,
        // we should have one create/update call for each
        verify(attendanceService, atLeastOnce()).updateAttendance(
            anyLong(),
            anyBoolean(),
            any(),
            any()
        );
        
        // Verify dialog was closed
        verify(mockStage).close();
    }
    
    @Test
    public void testHandleCancel() {
        // Test canceling
        attendanceController.testHandleCancel(mockEvent);
        
        // Verify dialog was closed
        verify(mockStage).close();
    }
    
    @Test
    public void testTeamMemberAttendanceRecord() {
        // Test the inner class
        TeamMember member = testMembers.get(0);
        Attendance attendance = testAttendances.get(0);
        
        TeamMemberAttendanceRecord record = new TeamMemberAttendanceRecord(member, attendance);
        
        assertEquals(member, record.getTeamMember());
        assertEquals(attendance, record.getAttendance());
        assertTrue(record.isPresent());
        assertEquals(attendance.getArrivalTime(), record.getArrivalTime());
        assertEquals(attendance.getDepartureTime(), record.getDepartureTime());
        assertEquals(member.getFullName(), record.getName());
    }

    
}
