package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entity class representing a project milestone in the FRC Project Management System.
 * This corresponds to the Milestone model in the Django application.
 */
@Entity
@Table(name = "milestones")
public class Milestone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", length = 255, nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
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