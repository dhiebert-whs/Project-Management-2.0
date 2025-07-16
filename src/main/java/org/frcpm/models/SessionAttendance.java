package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_attendance")
@EntityListeners(AuditingEntityListener.class)
public class SessionAttendance {
    
    public enum AttendanceStatus {
        PRESENT("Present"),
        ABSENT("Absent"),
        LATE("Late"),
        LEFT_EARLY("Left Early");
        
        private final String displayName;
        
        AttendanceStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
        
        public boolean isPresent() {
            return this == PRESENT || this == LATE;
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "session_id")
    private WorkshopSession session;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "member_id")
    private TeamMember member;
    
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;
    
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    
    private String checkInMethod; // "QR_CODE", "MANUAL", "VOICE"
    private String notes;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public SessionAttendance() {}
    
    public SessionAttendance(WorkshopSession session, TeamMember member) {
        this.session = session;
        this.member = member;
        this.status = AttendanceStatus.PRESENT;
        this.checkInTime = LocalDateTime.now();
        this.checkInMethod = "MANUAL";
    }
    
    // Business methods
    public boolean isPresent() {
        return status.isPresent();
    }
    
    public boolean isCheckedIn() {
        return checkInTime != null;
    }
    
    public boolean isCheckedOut() {
        return checkOutTime != null;
    }
    
    public boolean isLate() {
        return status == AttendanceStatus.LATE;
    }
    
    public long getAttendanceDurationMinutes() {
        if (checkInTime == null) return 0;
        
        LocalDateTime endTime = checkOutTime != null ? checkOutTime : LocalDateTime.now();
        return java.time.Duration.between(checkInTime, endTime).toMinutes();
    }
    
    public boolean isActiveAttendance() {
        return isCheckedIn() && !isCheckedOut();
    }
    
    public String getStatusClass() {
        return switch (status) {
            case PRESENT -> "text-success";
            case ABSENT -> "text-danger";
            case LATE -> "text-warning";
            case LEFT_EARLY -> "text-info";
        };
    }
    
    public String getAttendanceSummary() {
        if (!isPresent()) {
            return status.getDisplayName();
        }
        
        long minutes = getAttendanceDurationMinutes();
        if (minutes < 60) {
            return String.format("%s (%d min)", status.getDisplayName(), minutes);
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return String.format("%s (%dh %dm)", status.getDisplayName(), hours, remainingMinutes);
        }
    }
    
    public void checkIn(String method) {
        this.checkInTime = LocalDateTime.now();
        this.checkInMethod = method;
        
        // Determine if late based on session start time
        if (session.getStartTime() != null && checkInTime.isAfter(session.getStartTime().plusMinutes(15))) {
            this.status = AttendanceStatus.LATE;
        } else {
            this.status = AttendanceStatus.PRESENT;
        }
    }
    
    public void checkOut() {
        this.checkOutTime = LocalDateTime.now();
        
        // Update status if leaving early
        if (session.getEndTime() != null && checkOutTime.isBefore(session.getEndTime().minusMinutes(15))) {
            this.status = AttendanceStatus.LEFT_EARLY;
        }
    }
    
    public void markAbsent() {
        this.status = AttendanceStatus.ABSENT;
        this.checkInTime = null;
        this.checkOutTime = null;
    }
    
    // Standard getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public WorkshopSession getSession() { return session; }
    public void setSession(WorkshopSession session) { this.session = session; }
    
    public TeamMember getMember() { return member; }
    public void setMember(TeamMember member) { this.member = member; }
    
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }
    
    public String getCheckInMethod() { return checkInMethod; }
    public void setCheckInMethod(String checkInMethod) { this.checkInMethod = checkInMethod; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}