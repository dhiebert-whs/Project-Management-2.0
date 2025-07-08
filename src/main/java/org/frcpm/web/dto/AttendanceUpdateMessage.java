package org.frcpm.web.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * WebSocket message for real-time attendance updates in workshop coordination.
 * Supports check-in/check-out events and current presence status.
 */
public class AttendanceUpdateMessage {
    
    private Long attendanceId;
    private Long meetingId;
    private Long memberId;
    private String memberName;
    private String memberUsername;
    private String subteamName;
    private String subteamColorCode;
    
    // Event details
    private String eventType; // CHECK_IN, CHECK_OUT, UPDATE, LATE_ARRIVAL
    private boolean present;
    private LocalTime arrivalTime;
    private LocalTime departureTime;
    
    // Context information
    private String updatedBy;
    private LocalDateTime timestamp;
    private String sessionInfo; // "Workshop Session", "Team Meeting", etc.
    
    // Workshop coordination fields
    private String currentLocation; // "Main Workshop", "Programming Room", etc.
    private Integer totalPresent; // Current workshop occupancy
    private String notes; // Optional notes about the attendance event
    
    // Constructors
    public AttendanceUpdateMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    public AttendanceUpdateMessage(Long attendanceId, Long meetingId, Long memberId, 
                                 String memberName, String eventType, boolean present, 
                                 String updatedBy) {
        this();
        this.attendanceId = attendanceId;
        this.meetingId = meetingId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.eventType = eventType;
        this.present = present;
        this.updatedBy = updatedBy;
    }
    
    // Workshop check-in constructor
    public static AttendanceUpdateMessage checkIn(Long attendanceId, Long meetingId, 
                                                Long memberId, String memberName, 
                                                String memberUsername, LocalTime arrivalTime, 
                                                String updatedBy) {
        AttendanceUpdateMessage message = new AttendanceUpdateMessage(
            attendanceId, meetingId, memberId, memberName, "CHECK_IN", true, updatedBy
        );
        message.setMemberUsername(memberUsername);
        message.setArrivalTime(arrivalTime);
        return message;
    }
    
    // Workshop check-out constructor
    public static AttendanceUpdateMessage checkOut(Long attendanceId, Long meetingId, 
                                                 Long memberId, String memberName, 
                                                 String memberUsername, LocalTime departureTime, 
                                                 String updatedBy) {
        AttendanceUpdateMessage message = new AttendanceUpdateMessage(
            attendanceId, meetingId, memberId, memberName, "CHECK_OUT", false, updatedBy
        );
        message.setMemberUsername(memberUsername);
        message.setDepartureTime(departureTime);
        return message;
    }
    
    // Late arrival constructor (for build season time tracking)
    public static AttendanceUpdateMessage lateArrival(Long attendanceId, Long meetingId, 
                                                    Long memberId, String memberName, 
                                                    LocalTime arrivalTime, String updatedBy) {
        AttendanceUpdateMessage message = new AttendanceUpdateMessage(
            attendanceId, meetingId, memberId, memberName, "LATE_ARRIVAL", true, updatedBy
        );
        message.setArrivalTime(arrivalTime);
        return message;
    }
    
    // Getters and setters
    public Long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }
    
    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
    
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    
    public String getMemberUsername() { return memberUsername; }
    public void setMemberUsername(String memberUsername) { this.memberUsername = memberUsername; }
    
    public String getSubteamName() { return subteamName; }
    public void setSubteamName(String subteamName) { this.subteamName = subteamName; }
    
    public String getSubteamColorCode() { return subteamColorCode; }
    public void setSubteamColorCode(String subteamColorCode) { this.subteamColorCode = subteamColorCode; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }
    
    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
    
    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getSessionInfo() { return sessionInfo; }
    public void setSessionInfo(String sessionInfo) { this.sessionInfo = sessionInfo; }
    
    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
    
    public Integer getTotalPresent() { return totalPresent; }
    public void setTotalPresent(Integer totalPresent) { this.totalPresent = totalPresent; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    @Override
    public String toString() {
        return "AttendanceUpdateMessage{" +
                "attendanceId=" + attendanceId +
                ", meetingId=" + meetingId +
                ", memberName='" + memberName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", present=" + present +
                ", arrivalTime=" + arrivalTime +
                ", departureTime=" + departureTime +
                ", timestamp=" + timestamp +
                '}';
    }
}