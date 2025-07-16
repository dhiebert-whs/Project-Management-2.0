package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "safety_incidents")
@EntityListeners(AuditingEntityListener.class)
public class SafetyIncident {
    
    public enum IncidentType {
        MINOR_INJURY("Minor Injury"),
        MAJOR_INJURY("Major Injury"),
        NEAR_MISS("Near Miss"),
        EQUIPMENT_DAMAGE("Equipment Damage"),
        UNSAFE_CONDITION("Unsafe Condition"),
        SAFETY_VIOLATION("Safety Violation");
        
        private final String displayName;
        
        IncidentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
        
        public boolean requiresFirstAid() {
            return this == MINOR_INJURY || this == MAJOR_INJURY;
        }
        
        public boolean requiresImmediateAttention() {
            return this == MAJOR_INJURY || this == UNSAFE_CONDITION;
        }
    }
    
    public enum IncidentSeverity {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        CRITICAL("Critical");
        
        private final String displayName;
        
        IncidentSeverity(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "session_id")
    private WorkshopSession session;
    
    @ManyToOne
    @JoinColumn(name = "reported_by_id")
    private TeamMember reportedBy;
    
    @ManyToOne
    @JoinColumn(name = "involved_member_id")
    private TeamMember involvedMember;
    
    @NotNull
    private LocalDateTime incidentTime;
    
    @Enumerated(EnumType.STRING)
    private IncidentType type;
    
    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;
    
    @NotBlank
    private String description;
    
    private String location;
    private String equipmentInvolved;
    private String injuriesReported;
    private String immediateActions;
    private String preventiveMeasures;
    
    private boolean firstAidProvided = false;
    private boolean emergencyServicesContacted = false;
    private boolean parentsNotified = false;
    private boolean reportFiled = false;
    
    @ManyToOne
    @JoinColumn(name = "resolved_by_id")
    private TeamMember resolvedBy;
    
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public SafetyIncident() {}
    
    public SafetyIncident(WorkshopSession session, TeamMember reportedBy, IncidentType type, String description) {
        this.session = session;
        this.reportedBy = reportedBy;
        this.type = type;
        this.description = description;
        this.incidentTime = LocalDateTime.now();
        this.severity = IncidentSeverity.MEDIUM;
    }
    
    // Business methods
    public boolean isResolved() {
        return resolvedAt != null;
    }
    
    public boolean isHighPriority() {
        return severity == IncidentSeverity.HIGH || severity == IncidentSeverity.CRITICAL;
    }
    
    public boolean requiresFollowUp() {
        return type.requiresFirstAid() || !reportFiled || !isResolved();
    }
    
    public long getHoursSinceIncident() {
        return java.time.Duration.between(incidentTime, LocalDateTime.now()).toHours();
    }
    
    public String getSeverityClass() {
        return switch (severity) {
            case LOW -> "text-success";
            case MEDIUM -> "text-warning";
            case HIGH -> "text-danger";
            case CRITICAL -> "text-danger fw-bold";
        };
    }
    
    public String getTypeClass() {
        return switch (type) {
            case MINOR_INJURY -> "badge-warning";
            case MAJOR_INJURY -> "badge-danger";
            case NEAR_MISS -> "badge-info";
            case EQUIPMENT_DAMAGE -> "badge-secondary";
            case UNSAFE_CONDITION -> "badge-warning";
            case SAFETY_VIOLATION -> "badge-danger";
        };
    }
    
    public boolean isOverdue() {
        return !isResolved() && getHoursSinceIncident() > 24;
    }
    
    public void resolve(TeamMember resolver, String notes) {
        this.resolvedBy = resolver;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = notes;
    }
    
    // Standard getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public WorkshopSession getSession() { return session; }
    public void setSession(WorkshopSession session) { this.session = session; }
    
    public TeamMember getReportedBy() { return reportedBy; }
    public void setReportedBy(TeamMember reportedBy) { this.reportedBy = reportedBy; }
    
    public TeamMember getInvolvedMember() { return involvedMember; }
    public void setInvolvedMember(TeamMember involvedMember) { this.involvedMember = involvedMember; }
    
    public LocalDateTime getIncidentTime() { return incidentTime; }
    public void setIncidentTime(LocalDateTime incidentTime) { this.incidentTime = incidentTime; }
    
    public IncidentType getType() { return type; }
    public void setType(IncidentType type) { this.type = type; }
    
    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getEquipmentInvolved() { return equipmentInvolved; }
    public void setEquipmentInvolved(String equipmentInvolved) { this.equipmentInvolved = equipmentInvolved; }
    
    public String getInjuriesReported() { return injuriesReported; }
    public void setInjuriesReported(String injuriesReported) { this.injuriesReported = injuriesReported; }
    
    public String getImmediateActions() { return immediateActions; }
    public void setImmediateActions(String immediateActions) { this.immediateActions = immediateActions; }
    
    public String getPreventiveMeasures() { return preventiveMeasures; }
    public void setPreventiveMeasures(String preventiveMeasures) { this.preventiveMeasures = preventiveMeasures; }
    
    public boolean isFirstAidProvided() { return firstAidProvided; }
    public void setFirstAidProvided(boolean firstAidProvided) { this.firstAidProvided = firstAidProvided; }
    
    public boolean isEmergencyServicesContacted() { return emergencyServicesContacted; }
    public void setEmergencyServicesContacted(boolean emergencyServicesContacted) { this.emergencyServicesContacted = emergencyServicesContacted; }
    
    public boolean isParentsNotified() { return parentsNotified; }
    public void setParentsNotified(boolean parentsNotified) { this.parentsNotified = parentsNotified; }
    
    public boolean isReportFiled() { return reportFiled; }
    public void setReportFiled(boolean reportFiled) { this.reportFiled = reportFiled; }
    
    public TeamMember getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(TeamMember resolvedBy) { this.resolvedBy = resolvedBy; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}