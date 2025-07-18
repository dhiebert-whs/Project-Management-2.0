package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced Meeting entity for Phase 3A - Meeting Management System.
 * 
 * Features added for Phase 3A:
 * - Meeting types (TEAM_MEETING, DESIGN_REVIEW, STRATEGY_SESSION, etc.)
 * - Meeting status tracking (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)
 * - Location and virtual meeting support
 * - Agenda and action items
 * - Meeting reminder settings
 * - Recurring meeting support
 * - Meeting priorities
 * 
 * @author FRC Project Management Team
 * @version 3.0.0-3A
 * @since Phase 3A - Meeting Management System
 */
@Entity
@Table(name = "meetings")
public class Meeting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Size(min = 3, max = 200)
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances = new ArrayList<>();
    
    // =========================================================================
    // PHASE 3A: ENHANCED MEETING MANAGEMENT FIELDS
    // =========================================================================
    
    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_type", nullable = false)
    private MeetingType meetingType = MeetingType.TEAM_MEETING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MeetingStatus status = MeetingStatus.SCHEDULED;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private MeetingPriority priority = MeetingPriority.MEDIUM;
    
    @Size(max = 200)
    @Column(name = "location")
    private String location;
    
    @Column(name = "virtual_meeting_url")
    private String virtualMeetingUrl;
    
    @Column(name = "agenda", columnDefinition = "TEXT")
    private String agenda;
    
    @Column(name = "action_items", columnDefinition = "TEXT")
    private String actionItems;
    
    @Column(name = "is_recurring", nullable = false)
    private boolean isRecurring = false;
    
    @Column(name = "recurrence_pattern")
    private String recurrencePattern; // WEEKLY, BIWEEKLY, MONTHLY, etc.
    
    @Column(name = "reminder_enabled", nullable = false)
    private boolean reminderEnabled = true;
    
    @Column(name = "reminder_minutes_before")
    private Integer reminderMinutesBefore = 60; // Default 1 hour before
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "max_attendees")
    private Integer maxAttendees;
    
    @Column(name = "requires_preparation", nullable = false)
    private boolean requiresPreparation = false;
    
    @Column(name = "preparation_notes", columnDefinition = "TEXT")
    private String preparationNotes;
    
    // Constructors
    
    public Meeting() {
        // Default constructor required by JPA
    }
    
    public Meeting(LocalDate date, LocalTime startTime, LocalTime endTime, Project project) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.project = project;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Attendance> getAttendances() {
        return attendances;
    }

    public void setAttendances(List<Attendance> attendances) {
        this.attendances = attendances;
    }
    
    // =========================================================================
    // PHASE 3A: ENHANCED MEETING MANAGEMENT GETTERS/SETTERS
    // =========================================================================
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MeetingType getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(MeetingType meetingType) {
        this.meetingType = meetingType;
    }

    public MeetingStatus getStatus() {
        return status;
    }

    public void setStatus(MeetingStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now(); // Update timestamp when status changes
    }

    public MeetingPriority getPriority() {
        return priority;
    }

    public void setPriority(MeetingPriority priority) {
        this.priority = priority;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVirtualMeetingUrl() {
        return virtualMeetingUrl;
    }

    public void setVirtualMeetingUrl(String virtualMeetingUrl) {
        this.virtualMeetingUrl = virtualMeetingUrl;
    }

    public String getAgenda() {
        return agenda;
    }

    public void setAgenda(String agenda) {
        this.agenda = agenda;
    }

    public String getActionItems() {
        return actionItems;
    }

    public void setActionItems(String actionItems) {
        this.actionItems = actionItems;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getRecurrencePattern() {
        return recurrencePattern;
    }

    public void setRecurrencePattern(String recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public Integer getReminderMinutesBefore() {
        return reminderMinutesBefore;
    }

    public void setReminderMinutesBefore(Integer reminderMinutesBefore) {
        this.reminderMinutesBefore = reminderMinutesBefore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(Integer maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public boolean isRequiresPreparation() {
        return requiresPreparation;
    }

    public void setRequiresPreparation(boolean requiresPreparation) {
        this.requiresPreparation = requiresPreparation;
    }

    public String getPreparationNotes() {
        return preparationNotes;
    }

    public void setPreparationNotes(String preparationNotes) {
        this.preparationNotes = preparationNotes;
    }
    
    // Helper methods
    
    public void addAttendance(Attendance attendance) {
        attendances.add(attendance);
        attendance.setMeeting(this);
    }
    
    public void removeAttendance(Attendance attendance) {
        attendances.remove(attendance);
        attendance.setMeeting(null);
    }
    
    /**
     * Gets the number of present team members.
     * 
     * @return the count of attendances where present is true
     */
    public long getPresentCount() {
        return attendances.stream().filter(Attendance::isPresent).count();
    }
    
    /**
     * Gets the attendance percentage.
     * 
     * @return the percentage of team members who were present, or 0 if no attendances
     */
    public double getAttendancePercentage() {
        if (attendances.isEmpty()) {
            return 0;
        }
        return (double) getPresentCount() / attendances.size() * 100;
    }
    
    // =========================================================================
    // PHASE 3A: ENHANCED HELPER METHODS
    // =========================================================================
    
    /**
     * Gets the duration of the meeting in minutes.
     * 
     * @return the duration in minutes
     */
    public long getDurationMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }
    
    /**
     * Gets the duration of the meeting in hours.
     * 
     * @return the duration in hours (formatted to 1 decimal place)
     */
    public String getDurationHours() {
        long minutes = getDurationMinutes();
        double hours = minutes / 60.0;
        return String.format("%.1f", hours);
    }
    
    /**
     * Check if the meeting is happening today.
     * 
     * @return true if the meeting is scheduled for today
     */
    public boolean isToday() {
        return date != null && date.equals(LocalDate.now());
    }
    
    /**
     * Check if the meeting is in the future.
     * 
     * @return true if the meeting is scheduled for a future date
     */
    public boolean isFuture() {
        if (date == null) return false;
        LocalDate today = LocalDate.now();
        return date.isAfter(today) || (date.equals(today) && startTime != null && startTime.isAfter(LocalTime.now()));
    }
    
    /**
     * Check if the meeting is in the past.
     * 
     * @return true if the meeting is scheduled for a past date
     */
    public boolean isPast() {
        if (date == null) return false;
        LocalDate today = LocalDate.now();
        return date.isBefore(today) || (date.equals(today) && endTime != null && endTime.isBefore(LocalTime.now()));
    }
    
    /**
     * Check if the meeting is currently in progress.
     * 
     * @return true if the meeting is happening right now
     */
    public boolean isInProgress() {
        if (!isToday() || startTime == null || endTime == null) {
            return false;
        }
        LocalTime now = LocalTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }
    
    /**
     * Check if the meeting is virtual (has a virtual meeting URL).
     * 
     * @return true if the meeting has a virtual meeting URL
     */
    public boolean isVirtual() {
        return virtualMeetingUrl != null && !virtualMeetingUrl.trim().isEmpty();
    }
    
    /**
     * Check if the meeting is hybrid (has both location and virtual URL).
     * 
     * @return true if the meeting has both physical location and virtual URL
     */
    public boolean isHybrid() {
        return (location != null && !location.trim().isEmpty()) && isVirtual();
    }
    
    /**
     * Get the meeting format as a string.
     * 
     * @return "Virtual", "Hybrid", or "In-Person"
     */
    public String getMeetingFormat() {
        if (isHybrid()) {
            return "Hybrid";
        } else if (isVirtual()) {
            return "Virtual";
        } else {
            return "In-Person";
        }
    }
    
    /**
     * Get a formatted string representation of the meeting for display.
     * 
     * @return formatted meeting string
     */
    public String getFormattedTitle() {
        if (title != null && !title.trim().isEmpty()) {
            return title;
        } else {
            return meetingType.getDisplayName() + " - " + project.getName();
        }
    }
    
    /**
     * Check if the meeting can be edited based on its status.
     * 
     * @return true if the meeting can be edited
     */
    public boolean canBeEdited() {
        return status != null && status.allowsEditing();
    }
    
    /**
     * Check if attendance can be recorded for this meeting.
     * 
     * @return true if attendance can be recorded
     */
    public boolean canRecordAttendance() {
        return status != null && status.allowsAttendanceRecording();
    }
    
    /**
     * Get the number of days until the meeting.
     * 
     * @return number of days until meeting (negative if in the past)
     */
    public long getDaysUntilMeeting() {
        if (date == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
    
    /**
     * Get the number of hours until the meeting starts.
     * 
     * @return number of hours until meeting starts (negative if in the past)
     */
    public long getHoursUntilMeeting() {
        if (date == null || startTime == null) return 0;
        LocalDateTime meetingStart = LocalDateTime.of(date, startTime);
        return java.time.temporal.ChronoUnit.HOURS.between(LocalDateTime.now(), meetingStart);
    }
    
    /**
     * Check if the meeting is overbooked (more attendees than max allowed).
     * 
     * @return true if the meeting is overbooked
     */
    public boolean isOverbooked() {
        if (maxAttendees == null) return false;
        return attendances.size() > maxAttendees;
    }
    
    /**
     * Get the number of available seats remaining.
     * 
     * @return number of available seats, or -1 if no limit
     */
    public int getAvailableSeats() {
        if (maxAttendees == null) return -1;
        return Math.max(0, maxAttendees - attendances.size());
    }
    
    /**
     * Enhanced toString method with meeting type and status.
     */
    @Override
    public String toString() {
        String titleStr = title != null ? title : meetingType.getDisplayName();
        return String.format("%s - %s (%s %s-%s)", 
            titleStr, 
            project.getName(), 
            date, 
            startTime, 
            endTime
        );
    }
}