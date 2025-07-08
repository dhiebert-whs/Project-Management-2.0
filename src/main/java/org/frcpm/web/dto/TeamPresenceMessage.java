package org.frcpm.web.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

/**
 * WebSocket message for real-time workshop presence summary.
 * Provides "who's here now" visibility for team coordination.
 */
public class TeamPresenceMessage {
    
    private Long meetingId;
    private String sessionTitle;
    private LocalDateTime lastUpdated;
    
    // Overall presence statistics
    private Integer totalPresent;
    private Integer totalExpected;
    private Double attendancePercentage;
    
    // Subteam breakdown
    private List<SubteamPresence> subteamPresence;
    
    // Recent activity (last 15 minutes)
    private List<RecentActivity> recentActivity;
    
    // Workshop status
    private String workshopStatus; // "ACTIVE", "BREAK", "CLOSED"
    private LocalTime sessionStartTime;
    private LocalTime sessionEndTime;
    
    // Constructors
    public TeamPresenceMessage() {
        this.lastUpdated = LocalDateTime.now();
        this.subteamPresence = new ArrayList<>();
        this.recentActivity = new ArrayList<>();
    }
    
    public TeamPresenceMessage(Long meetingId, String sessionTitle) {
        this();
        this.meetingId = meetingId;
        this.sessionTitle = sessionTitle;
    }
    
    // Helper methods
    public void calculateAttendancePercentage() {
        if (totalExpected != null && totalExpected > 0) {
            this.attendancePercentage = (totalPresent.doubleValue() / totalExpected.doubleValue()) * 100.0;
        } else {
            this.attendancePercentage = 0.0;
        }
    }
    
    public void addSubteamPresence(String subteamName, String colorCode, int present, int total) {
        SubteamPresence presence = new SubteamPresence(subteamName, colorCode, present, total);
        this.subteamPresence.add(presence);
    }
    
    public void addRecentActivity(String memberName, String eventType, LocalTime eventTime) {
        RecentActivity activity = new RecentActivity(memberName, eventType, eventTime);
        this.recentActivity.add(activity);
        
        // Keep only last 10 activities
        if (this.recentActivity.size() > 10) {
            this.recentActivity.remove(0);
        }
    }
    
    // Getters and setters
    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
    
    public String getSessionTitle() { return sessionTitle; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public Integer getTotalPresent() { return totalPresent; }
    public void setTotalPresent(Integer totalPresent) { this.totalPresent = totalPresent; }
    
    public Integer getTotalExpected() { return totalExpected; }
    public void setTotalExpected(Integer totalExpected) { this.totalExpected = totalExpected; }
    
    public Double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(Double attendancePercentage) { this.attendancePercentage = attendancePercentage; }
    
    public List<SubteamPresence> getSubteamPresence() { return subteamPresence; }
    public void setSubteamPresence(List<SubteamPresence> subteamPresence) { this.subteamPresence = subteamPresence; }
    
    public List<RecentActivity> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<RecentActivity> recentActivity) { this.recentActivity = recentActivity; }
    
    public String getWorkshopStatus() { return workshopStatus; }
    public void setWorkshopStatus(String workshopStatus) { this.workshopStatus = workshopStatus; }
    
    public LocalTime getSessionStartTime() { return sessionStartTime; }
    public void setSessionStartTime(LocalTime sessionStartTime) { this.sessionStartTime = sessionStartTime; }
    
    public LocalTime getSessionEndTime() { return sessionEndTime; }
    public void setSessionEndTime(LocalTime sessionEndTime) { this.sessionEndTime = sessionEndTime; }
    
    // Inner classes for structured data
    public static class SubteamPresence {
        private String subteamName;
        private String colorCode;
        private Integer present;
        private Integer total;
        private Double percentage;
        
        public SubteamPresence() {}
        
        public SubteamPresence(String subteamName, String colorCode, Integer present, Integer total) {
            this.subteamName = subteamName;
            this.colorCode = colorCode;
            this.present = present;
            this.total = total;
            this.percentage = total > 0 ? (present.doubleValue() / total.doubleValue()) * 100.0 : 0.0;
        }
        
        // Getters and setters
        public String getSubteamName() { return subteamName; }
        public void setSubteamName(String subteamName) { this.subteamName = subteamName; }
        
        public String getColorCode() { return colorCode; }
        public void setColorCode(String colorCode) { this.colorCode = colorCode; }
        
        public Integer getPresent() { return present; }
        public void setPresent(Integer present) { this.present = present; }
        
        public Integer getTotal() { return total; }
        public void setTotal(Integer total) { this.total = total; }
        
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }
    
    public static class RecentActivity {
        private String memberName;
        private String eventType; // "ARRIVED", "DEPARTED", "LATE"
        private LocalTime eventTime;
        private String timeAgo; // "2 minutes ago"
        
        public RecentActivity() {}
        
        public RecentActivity(String memberName, String eventType, LocalTime eventTime) {
            this.memberName = memberName;
            this.eventType = eventType;
            this.eventTime = eventTime;
            this.timeAgo = calculateTimeAgo(eventTime);
        }
        
        private String calculateTimeAgo(LocalTime eventTime) {
            LocalTime now = LocalTime.now();
            long minutesAgo = java.time.Duration.between(eventTime, now).toMinutes();
            
            if (minutesAgo < 1) {
                return "just now";
            } else if (minutesAgo == 1) {
                return "1 minute ago";
            } else {
                return minutesAgo + " minutes ago";
            }
        }
        
        // Getters and setters
        public String getMemberName() { return memberName; }
        public void setMemberName(String memberName) { this.memberName = memberName; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public LocalTime getEventTime() { return eventTime; }
        public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }
        
        public String getTimeAgo() { return timeAgo; }
        public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
    }
    
    @Override
    public String toString() {
        return "TeamPresenceMessage{" +
                "meetingId=" + meetingId +
                ", sessionTitle='" + sessionTitle + '\'' +
                ", totalPresent=" + totalPresent +
                ", totalExpected=" + totalExpected +
                ", attendancePercentage=" + attendancePercentage +
                ", workshopStatus='" + workshopStatus + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}