package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "robot_tests")
@EntityListeners(AuditingEntityListener.class)
public class RobotTest {
    
    public enum TestType {
        UNIT_TEST("Unit Test"),
        INTEGRATION_TEST("Integration Test"),
        SYSTEM_TEST("System Test"),
        PERFORMANCE_TEST("Performance Test"),
        STRESS_TEST("Stress Test"),
        FIELD_TEST("Field Test");
        
        private final String displayName;
        
        TestType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public enum TestStatus {
        PLANNED("Planned"),
        IN_PROGRESS("In Progress"),
        PASSED("Passed"),
        FAILED("Failed"),
        BLOCKED("Blocked");
        
        private final String displayName;
        
        TestStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
        
        public boolean isComplete() {
            return this == PASSED || this == FAILED;
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "robot_id")
    private Robot robot;
    
    @ManyToOne
    @JoinColumn(name = "subsystem_id")
    private Subsystem subsystem;
    
    @Enumerated(EnumType.STRING)
    private TestType type;
    
    @Enumerated(EnumType.STRING)
    private TestStatus status;
    
    private String description;
    private String expectedResults;
    private String actualResults;
    
    @ManyToOne
    @JoinColumn(name = "conducted_by_id")
    private TeamMember conductedBy;
    
    private LocalDateTime plannedDate;
    private LocalDateTime conductedDate;
    private LocalDateTime completedDate;
    
    private String notes;
    private String issuesFound;
    private String recommendations;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Constructors
    public RobotTest() {}
    
    public RobotTest(String name, Robot robot, TestType type) {
        this.name = name;
        this.robot = robot;
        this.type = type;
        this.status = TestStatus.PLANNED;
    }
    
    // Business methods
    public boolean isPassed() {
        return status == TestStatus.PASSED;
    }
    
    public boolean isFailed() {
        return status == TestStatus.FAILED;
    }
    
    public boolean isComplete() {
        return status.isComplete();
    }
    
    public boolean isOverdue() {
        return plannedDate != null && 
               LocalDateTime.now().isAfter(plannedDate) && 
               !isComplete();
    }
    
    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return plannedDate.toLocalDate().until(LocalDateTime.now().toLocalDate()).getDays();
    }
    
    public String getStatusClass() {
        return switch (status) {
            case PLANNED -> "text-secondary";
            case IN_PROGRESS -> "text-primary";
            case PASSED -> "text-success";
            case FAILED -> "text-danger";
            case BLOCKED -> "text-warning";
        };
    }
    
    public String getTypeClass() {
        return switch (type) {
            case UNIT_TEST -> "badge-info";
            case INTEGRATION_TEST -> "badge-primary";
            case SYSTEM_TEST -> "badge-success";
            case PERFORMANCE_TEST -> "badge-warning";
            case STRESS_TEST -> "badge-danger";
            case FIELD_TEST -> "badge-dark";
        };
    }
    
    // Standard getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Robot getRobot() { return robot; }
    public void setRobot(Robot robot) { this.robot = robot; }
    
    public Subsystem getSubsystem() { return subsystem; }
    public void setSubsystem(Subsystem subsystem) { this.subsystem = subsystem; }
    
    public TestType getType() { return type; }
    public void setType(TestType type) { this.type = type; }
    
    public TestStatus getStatus() { return status; }
    public void setStatus(TestStatus status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getExpectedResults() { return expectedResults; }
    public void setExpectedResults(String expectedResults) { this.expectedResults = expectedResults; }
    
    public String getActualResults() { return actualResults; }
    public void setActualResults(String actualResults) { this.actualResults = actualResults; }
    
    public TeamMember getConductedBy() { return conductedBy; }
    public void setConductedBy(TeamMember conductedBy) { this.conductedBy = conductedBy; }
    
    public LocalDateTime getPlannedDate() { return plannedDate; }
    public void setPlannedDate(LocalDateTime plannedDate) { this.plannedDate = plannedDate; }
    
    public LocalDateTime getConductedDate() { return conductedDate; }
    public void setConductedDate(LocalDateTime conductedDate) { this.conductedDate = conductedDate; }
    
    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getIssuesFound() { return issuesFound; }
    public void setIssuesFound(String issuesFound) { this.issuesFound = issuesFound; }
    
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}