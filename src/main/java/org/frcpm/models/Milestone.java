package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entity representing project milestones in the FRC Project Management System.
 * Enhanced for build season management with completion tracking and priorities.
 */
@Entity
@Table(name = "milestones")
@EntityListeners(AuditingEntityListener.class)
public class Milestone {
    
    public enum MilestoneType {
        DESIGN("Design Milestone"),
        PROTOTYPE("Prototype Milestone"),
        BUILD("Build Milestone"),
        TESTING("Testing Milestone"),
        COMPETITION("Competition Milestone"),
        DOCUMENTATION("Documentation Milestone"),
        SAFETY("Safety Milestone"),
        CUSTOM("Custom Milestone");
        
        private final String displayName;
        
        MilestoneType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public enum Priority {
        LOW(1, "Low"),
        MEDIUM(2, "Medium"),
        HIGH(3, "High"),
        CRITICAL(4, "Critical");
        
        private final int value;
        private final String displayName;
        
        Priority(int value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public int getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "title", length = 255, nullable = false)
    private String title;
    
    // Keep backward compatibility with existing 'name' field
    @Column(name = "name", length = 255)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @NotNull
    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;
    
    // Keep backward compatibility with existing 'date' field
    @Column(name = "date")
    private LocalDate date;
    
    @Column(name = "completion_date")
    private LocalDate completionDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private MilestoneType type = MilestoneType.CUSTOM;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority = Priority.MEDIUM;
    
    @Column(name = "completed", nullable = false)
    private boolean completed = false;
    
    @Column(name = "completion_percentage", nullable = false)
    private int completionPercentage = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "robot_id")
    private Robot robot;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private CompetitionSeason season;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private TeamMember assignedTo;
    
    @Column(name = "is_critical_path", nullable = false)
    private boolean isCriticalPath = false;
    
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    
    public Milestone() {
        // Default constructor required by JPA
    }
    
    public Milestone(String name, LocalDate date, Project project) {
        this.name = name;
        this.date = date;
        this.project = project;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
    
    // Helper methods
    
    /**
     * Checks if the milestone date has passed.
     * 
     * @return true if the milestone date is in the past, false otherwise
     */
    public boolean isPassed() {
        return date.isBefore(LocalDate.now());
    }
    
    /**
     * Gets the number of days until or since the milestone date.
     * 
     * @return positive number for days in the future, negative for days in the past
     */
    public long getDaysUntil() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
    
    @Override
    public String toString() {
        return name;
    }
}