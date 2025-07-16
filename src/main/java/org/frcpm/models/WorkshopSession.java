package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "workshop_sessions")
@EntityListeners(AuditingEntityListener.class)
public class WorkshopSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    
    @NotNull
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @Enumerated(EnumType.STRING)
    private SessionType type;
    
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
    
    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private TeamMember supervisingMentor;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<SessionAttendance> attendances;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<ToolUsage> toolUsages;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<SafetyIncident> incidents;
    
    private String objectives;
    private String accomplishments;
    private String notes;
    
    // Workshop safety checklist
    private boolean safetyBriefingCompleted;
    private boolean emergencyExitsIdentified;
    private boolean firstAidKitLocation;
    private boolean fireExtinguisherLocation;
    private boolean eyeWashStationChecked;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public WorkshopSession() {}
    
    public WorkshopSession(Project project, LocalDateTime startTime, SessionType type, TeamMember mentor) {
        this.project = project;
        this.startTime = startTime;
        this.type = type;
        this.supervisingMentor = mentor;
        this.status = SessionStatus.PLANNED;
    }
    
    // Business methods
    public boolean isActive() {
        return status == SessionStatus.IN_PROGRESS;
    }
    
    public boolean requiresMentorSupervision() {
        return type == SessionType.BUILD || type == SessionType.MACHINING;
    }
    
    public long getDurationMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }
    
    public boolean hasSafetyIncidents() {
        return incidents != null && !incidents.isEmpty();
    }
    
    public void startSession() {
        this.status = SessionStatus.IN_PROGRESS;
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
        }
    }
    
    public void endSession() {
        this.status = SessionStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
    }
    
    public boolean isReadyToStart() {
        if (type.requiresSafetyChecklist()) {
            return safetyBriefingCompleted && 
                   emergencyExitsIdentified && 
                   firstAidKitLocation && 
                   fireExtinguisherLocation && 
                   eyeWashStationChecked;
        }
        return true;
    }
    
    public int getAttendanceCount() {
        return attendances != null ? attendances.size() : 0;
    }
    
    public boolean isOverdue() {
        return startTime != null && 
               LocalDateTime.now().isAfter(startTime.plusHours(4)) && 
               status == SessionStatus.IN_PROGRESS;
    }
    
    public String getStatusClass() {
        return switch (status) {
            case PLANNED -> "text-primary";
            case IN_PROGRESS -> "text-success";
            case COMPLETED -> "text-secondary";
            case CANCELLED -> "text-danger";
            case POSTPONED -> "text-warning";
        };
    }
    
    // Standard getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public SessionType getType() { return type; }
    public void setType(SessionType type) { this.type = type; }
    
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    
    public TeamMember getSupervisingMentor() { return supervisingMentor; }
    public void setSupervisingMentor(TeamMember supervisingMentor) { this.supervisingMentor = supervisingMentor; }
    
    public List<SessionAttendance> getAttendances() { return attendances; }
    public void setAttendances(List<SessionAttendance> attendances) { this.attendances = attendances; }
    
    public List<ToolUsage> getToolUsages() { return toolUsages; }
    public void setToolUsages(List<ToolUsage> toolUsages) { this.toolUsages = toolUsages; }
    
    public List<SafetyIncident> getIncidents() { return incidents; }
    public void setIncidents(List<SafetyIncident> incidents) { this.incidents = incidents; }
    
    public String getObjectives() { return objectives; }
    public void setObjectives(String objectives) { this.objectives = objectives; }
    
    public String getAccomplishments() { return accomplishments; }
    public void setAccomplishments(String accomplishments) { this.accomplishments = accomplishments; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public boolean isSafetyBriefingCompleted() { return safetyBriefingCompleted; }
    public void setSafetyBriefingCompleted(boolean safetyBriefingCompleted) { this.safetyBriefingCompleted = safetyBriefingCompleted; }
    
    public boolean isEmergencyExitsIdentified() { return emergencyExitsIdentified; }
    public void setEmergencyExitsIdentified(boolean emergencyExitsIdentified) { this.emergencyExitsIdentified = emergencyExitsIdentified; }
    
    public boolean isFirstAidKitLocation() { return firstAidKitLocation; }
    public void setFirstAidKitLocation(boolean firstAidKitLocation) { this.firstAidKitLocation = firstAidKitLocation; }
    
    public boolean isFireExtinguisherLocation() { return fireExtinguisherLocation; }
    public void setFireExtinguisherLocation(boolean fireExtinguisherLocation) { this.fireExtinguisherLocation = fireExtinguisherLocation; }
    
    public boolean isEyeWashStationChecked() { return eyeWashStationChecked; }
    public void setEyeWashStationChecked(boolean eyeWashStationChecked) { this.eyeWashStationChecked = eyeWashStationChecked; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}