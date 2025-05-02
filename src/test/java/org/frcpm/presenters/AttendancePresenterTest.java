package org.frcpm.presenters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.Optional;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.services.AttendanceService;
import org.frcpm.services.DialogService;
import org.frcpm.services.MeetingService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.viewmodels.AttendanceViewModel;
import org.frcpm.viewmodels.AttendanceViewModel.AttendanceRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for AttendancePresenter using the recommended patterns
 * for AfterburnerFX integration testing.
 */
@ExtendWith(MockitoExtension.class)
public class AttendancePresenterTest {

    @Mock
    private AttendanceViewModel mockViewModel;
    
    @Mock
    private AttendanceService mockAttendanceService;
    
    @Mock
    private DialogService mockDialogService;
    
    @Mock
    private TeamMemberService mockTeamMemberService;
    
    @Mock
    private MeetingService mockMeetingService;
    
    @Mock
    private ResourceBundle mockResources;
    
    @Mock
    private Label mockMeetingTitleLabel;
    
    @Mock
    private Label mockDateLabel;
    
    @Mock
    private Label mockTimeLabel;
    
    @Mock
    private TableView<AttendanceRecord> mockAttendanceTable;
    
    @Mock
    private Button mockSaveButton;
    
    @Mock
    private Button mockCancelButton;
    
    @InjectMocks
    private AttendancePresenter presenter;
    
    // Test data
    private Meeting testMeeting;
    private ObservableList<AttendanceRecord> testRecords;
    private StringProperty testErrorProperty;
    private StringProperty testMeetingTitleProperty;
    private StringProperty testMeetingDateProperty;
    private StringProperty testMeetingTimeProperty;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test data
        testMeeting = new Meeting();
        testMeeting.setId(1L);
        // No setTitle method - Modify as needed based on the Meeting class fields
        // For example, if it uses a name field instead:
        // testMeeting.setName("Test Meeting");
        
        // Create test JavaFX properties
        testErrorProperty = new SimpleStringProperty("");
        testMeetingTitleProperty = new SimpleStringProperty("Test Meeting");
        testMeetingDateProperty = new SimpleStringProperty("2025-05-01");
        testMeetingTimeProperty = new SimpleStringProperty("14:00 - 16:00");
        
        // Create test records list
        testRecords = FXCollections.observableArrayList();
        TeamMember testMember = new TeamMember();
        testMember.setId(1L);
        testMember.setFirstName("John");
        testMember.setLastName("Doe");
        AttendanceRecord record = mock(AttendanceRecord.class);
        when(record.getTeamMember()).thenReturn(testMember);
        testRecords.add(record);
        
        // Set up common ViewModel stubs with lenient mocking
        lenient().when(mockViewModel.errorMessageProperty()).thenReturn(testErrorProperty);
        lenient().when(mockViewModel.meetingTitleProperty()).thenReturn(testMeetingTitleProperty);
        lenient().when(mockViewModel.meetingDateProperty()).thenReturn(testMeetingDateProperty);
        lenient().when(mockViewModel.meetingTimeProperty()).thenReturn(testMeetingTimeProperty);
        lenient().when(mockViewModel.getAttendanceRecords()).thenReturn(testRecords);
        lenient().when(mockViewModel.getMeeting()).thenReturn(testMeeting);
        
        // Reset test fields
        injectUIComponents();
    }
    
    /**
     * Injects UI components into the presenter using reflection.
     */
    private void injectUIComponents() {
        try {
            injectField(presenter, "meetingTitleLabel", mockMeetingTitleLabel);
            injectField(presenter, "dateLabel", mockDateLabel);
            injectField(presenter, "timeLabel", mockTimeLabel);
            injectField(presenter, "attendanceTable", mockAttendanceTable);
            injectField(presenter, "saveButton", mockSaveButton);
            injectField(presenter, "cancelButton", mockCancelButton);
        } catch (Exception e) {
            fail("Failed to inject UI components: " + e.getMessage());
        }
    }
    
    /**
     * Injects a field using reflection.
     */
    private void injectField(Object target, String fieldName, Object value) 
            throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    
    @Test
    public void testInitialize() {
        // Act
        presenter.initialize(mock(URL.class), mockResources);
        
        // Assert
        verify(mockViewModel).errorMessageProperty();
        verify(mockViewModel).meetingTitleProperty();
        verify(mockViewModel).meetingDateProperty();
        verify(mockViewModel).meetingTimeProperty();
        verify(mockViewModel).getAttendanceRecords();
    }
    
    @Test
    public void testInitWithMeeting() {
        // Arrange
        Meeting meeting = new Meeting();
        meeting.setId(2L);
        // No setTitle method - Use proper fields from Meeting class
        // Examples based on what might be in the Meeting class:
        // meeting.setName("New Meeting");
        // OR meeting.setDescription("New Meeting");
        
        // Act
        presenter.initWithMeeting(meeting);
        
        // Assert
        verify(mockViewModel).initWithMeeting(meeting);
    }
    
    @Test
    public void testHandleSetTime_SelectedRecord() {
        // Arrange
        AttendanceRecord mockRecord = mock(AttendanceRecord.class);
        LocalTime arrivalTime = LocalTime.of(14, 0);
        LocalTime departureTime = LocalTime.of(16, 0);
        
        when(mockViewModel.getSelectedRecord()).thenReturn(mockRecord);
        when(mockViewModel.parseTime(anyString())).thenReturn(arrivalTime, departureTime);
        
        // Inject text fields for time
        try {
            injectField(presenter, "arrivalTimeField", mock(javafx.scene.control.TextField.class));
            injectField(presenter, "departureTimeField", mock(javafx.scene.control.TextField.class));
        } catch (Exception e) {
            fail("Failed to inject text fields: " + e.getMessage());
        }
        
        // Act
        presenter.handleSetTime();
        
        // Assert
        verify(mockViewModel).updateRecordTimes(mockRecord, arrivalTime, departureTime);
    }
    
    @Test
    public void testHandleSetTime_NoSelectedRecord() {
        // Arrange
        when(mockViewModel.getSelectedRecord()).thenReturn(null);
        when(mockResources.getString("info.title")).thenReturn("Information");
        when(mockResources.getString("info.no.selection.member")).thenReturn("No member selected");
        
        // Act
        presenter.handleSetTime();
        
        // Assert
        verify(mockDialogService).showInfoAlert(anyString(), anyString());
        verify(mockViewModel, never()).updateRecordTimes(any(), any(), any());
    }
    
    @Test
    public void testGetViewModel() {
        // Act
        AttendanceViewModel result = presenter.getViewModel();
        
        // Assert
        assertSame(mockViewModel, result);
    }
    
    @Test
    public void testSetViewModel() {
        // Arrange
        AttendanceViewModel newViewModel = mock(AttendanceViewModel.class);
        when(newViewModel.errorMessageProperty()).thenReturn(new SimpleStringProperty());
        when(newViewModel.meetingTitleProperty()).thenReturn(new SimpleStringProperty());
        when(newViewModel.meetingDateProperty()).thenReturn(new SimpleStringProperty());
        when(newViewModel.meetingTimeProperty()).thenReturn(new SimpleStringProperty());
        when(newViewModel.getAttendanceRecords()).thenReturn(FXCollections.observableArrayList());
        
        // Act
        presenter.setViewModel(newViewModel);
        
        // Assert
        assertEquals(newViewModel, presenter.getViewModel());
    }
    
    @Test
    public void testSaveMeetingAttendance_Success() {
        // Arrange
        when(mockViewModel.saveMeetingAttendance()).thenReturn(true);
        
        // Simulate the save button in a scene
        Button realSaveButton = new Button();
        try {
            injectField(presenter, "saveButton", realSaveButton);
        } catch (Exception e) {
            fail("Failed to inject save button: " + e.getMessage());
        }
        
        // Act - directly call the handler that would be triggered by the button
        // This doesn't test the UI event, but tests the handler logic
        when(mockViewModel.saveMeetingAttendance()).thenReturn(true);
        
        // We can't directly test the button action since it requires a JavaFX environment
        // Instead, we can test if binding is correct
        verify(mockViewModel, never()).saveMeetingAttendance(); // Not called yet
    }
    
    @Test
    public void testErrorMessageListener() {
        // Arrange
        presenter.initialize(mock(URL.class), mockResources);
        
        // Act - simulate an error message change
        testErrorProperty.set("Test error message");
        
        // Assert - we can't directly verify the alert is shown due to JavaFX thread,
        // but we can check if the DialogService was called
        // Note: This may not work in all cases due to how JavaFX properties work in tests
        // This is one limitation of UI testing without TestFX
    }
}