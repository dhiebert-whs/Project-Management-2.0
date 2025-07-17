package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing FRC robot subsystems (drivetrain, elevator, collector, etc).
 * Each subsystem is owned by a subteam and can have multiple tasks.
 */
@Entity
@Table(name = "subsystems")
public class Subsystem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false)
    private String name;
    
    @Size(max = 500)
    @Column(name = "description")
    private String description;
    
    @Size(max = 7)
    @Column(name = "color")
    private String color; // Hex color for Gantt chart display
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_subteam_id", nullable = false)
    private Subteam ownerSubteam;
    
    @OneToMany(mappedBy = "subsystem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();
    
    // Constructors
    
    public Subsystem() {
        // Default constructor required by JPA
    }
    
    public Subsystem(String name, Project project, Subteam ownerSubteam) {
        this.name = name;
        this.project = project;
        this.ownerSubteam = ownerSubteam;
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
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public Subteam getOwnerSubteam() {
        return ownerSubteam;
    }
    
    public void setOwnerSubteam(Subteam ownerSubteam) {
        this.ownerSubteam = ownerSubteam;
    }
    
    public Set<Task> getTasks() {
        return tasks;
    }
    
    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }
    
    // Helper methods
    
    public void addTask(Task task) {
        tasks.add(task);
        task.setSubsystem(this);
    }
    
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setSubsystem(null);
    }
    
    /**
     * Get the completion percentage of this subsystem based on its tasks
     */
    public double getCompletionPercentage() {
        if (tasks.isEmpty()) {
            return 0.0;
        }
        
        double totalProgress = tasks.stream()
                .mapToDouble(Task::getProgress)
                .sum();
        
        return totalProgress / tasks.size();
    }
    
    /**
     * Get the number of completed tasks in this subsystem
     */
    public long getCompletedTaskCount() {
        return tasks.stream()
                .filter(Task::isCompleted)
                .count();
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subsystem subsystem = (Subsystem) o;
        return id != null && id.equals(subsystem.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}