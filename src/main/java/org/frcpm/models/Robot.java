package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "robots")
@EntityListeners(AuditingEntityListener.class)
public class Robot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    
    @ManyToOne
    @JoinColumn(name = "season_id")
    private CompetitionSeason season;
    
    @Enumerated(EnumType.STRING)
    private RobotStatus status;
    
    @Enumerated(EnumType.STRING)
    private RobotType type; // COMPETITION, PRACTICE, PROTOTYPE
    
    // Physical specifications
    private Double weightKg;
    private Double lengthCm;
    private Double widthCm;
    private Double heightCm;
    
    // Build tracking
    private LocalDate mechanicalCompleteDate;
    private LocalDate electricalCompleteDate;
    private LocalDate programmingCompleteDate;
    private LocalDate inspectionReadyDate;
    
    // Competition tracking
    private boolean passedInspection = false;
    private LocalDate inspectionDate;
    private String inspectionNotes;
    
    @OneToMany(mappedBy = "robot", cascade = CascadeType.ALL)
    private List<Subsystem> subsystems;
    
    @OneToMany(mappedBy = "robot", cascade = CascadeType.ALL)
    private List<RobotTest> tests;
    
    private String notes;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public Robot() {}
    
    public Robot(String name, Project project, RobotType type) {
        this.name = name;
        this.project = project;
        this.type = type;
        this.status = RobotStatus.DESIGN;
    }
    
    // Business methods
    public boolean isCompetitionReady() {
        return status == RobotStatus.COMPETITION_READY || status == RobotStatus.COMPETING;
    }
    
    public boolean requiresInspection() {
        return type == RobotType.COMPETITION && !passedInspection;
    }
    
    public double getCompletionPercentage() {
        int completedMilestones = 0;
        int totalMilestones = 4; // mechanical, electrical, programming, inspection
        
        if (mechanicalCompleteDate != null) completedMilestones++;
        if (electricalCompleteDate != null) completedMilestones++;
        if (programmingCompleteDate != null) completedMilestones++;
        if (passedInspection) completedMilestones++;
        
        return (completedMilestones * 100.0) / totalMilestones;
    }
    
    public boolean isWithinWeightLimit(double maxWeightKg) {
        return weightKg != null && weightKg <= maxWeightKg;
    }
    
    public boolean isWithinDimensionLimits(double maxLengthCm, double maxWidthCm, double maxHeightCm) {
        return (lengthCm == null || lengthCm <= maxLengthCm) &&
               (widthCm == null || widthCm <= maxWidthCm) &&
               (heightCm == null || heightCm <= maxHeightCm);
    }
    
    public String getDimensionsString() {
        if (lengthCm != null && widthCm != null && heightCm != null) {
            return String.format("%.1f × %.1f × %.1f cm", lengthCm, widthCm, heightCm);
        }
        return "Dimensions not specified";
    }
    
    public String getWeightString() {
        if (weightKg != null) {
            return String.format("%.1f kg", weightKg);
        }
        return "Weight not specified";
    }
    
    public boolean allMilestonesComplete() {
        return mechanicalCompleteDate != null && 
               electricalCompleteDate != null && 
               programmingCompleteDate != null && 
               (type != RobotType.COMPETITION || passedInspection);
    }
    
    public long getDaysToInspection() {
        if (inspectionReadyDate != null) {
            return LocalDate.now().until(inspectionReadyDate).getDays();
        }
        return -1;
    }
    
    public boolean isOverdue() {
        return inspectionReadyDate != null && 
               LocalDate.now().isAfter(inspectionReadyDate) && 
               !passedInspection;
    }
    
    // Standard getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    
    public CompetitionSeason getSeason() { return season; }
    public void setSeason(CompetitionSeason season) { this.season = season; }
    
    public RobotStatus getStatus() { return status; }
    public void setStatus(RobotStatus status) { this.status = status; }
    
    public RobotType getType() { return type; }
    public void setType(RobotType type) { this.type = type; }
    
    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    
    public Double getLengthCm() { return lengthCm; }
    public void setLengthCm(Double lengthCm) { this.lengthCm = lengthCm; }
    
    public Double getWidthCm() { return widthCm; }
    public void setWidthCm(Double widthCm) { this.widthCm = widthCm; }
    
    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
    
    public LocalDate getMechanicalCompleteDate() { return mechanicalCompleteDate; }
    public void setMechanicalCompleteDate(LocalDate mechanicalCompleteDate) { this.mechanicalCompleteDate = mechanicalCompleteDate; }
    
    public LocalDate getElectricalCompleteDate() { return electricalCompleteDate; }
    public void setElectricalCompleteDate(LocalDate electricalCompleteDate) { this.electricalCompleteDate = electricalCompleteDate; }
    
    public LocalDate getProgrammingCompleteDate() { return programmingCompleteDate; }
    public void setProgrammingCompleteDate(LocalDate programmingCompleteDate) { this.programmingCompleteDate = programmingCompleteDate; }
    
    public LocalDate getInspectionReadyDate() { return inspectionReadyDate; }
    public void setInspectionReadyDate(LocalDate inspectionReadyDate) { this.inspectionReadyDate = inspectionReadyDate; }
    
    public boolean isPassedInspection() { return passedInspection; }
    public void setPassedInspection(boolean passedInspection) { this.passedInspection = passedInspection; }
    
    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }
    
    public String getInspectionNotes() { return inspectionNotes; }
    public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }
    
    public List<Subsystem> getSubsystems() { return subsystems; }
    public void setSubsystems(List<Subsystem> subsystems) { this.subsystems = subsystems; }
    
    public List<RobotTest> getTests() { return tests; }
    public void setTests(List<RobotTest> tests) { this.tests = tests; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}