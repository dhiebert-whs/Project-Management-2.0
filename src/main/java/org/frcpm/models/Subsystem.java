package org.frcpm.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing a robot subsystem in the FRC Project Management System.
 * This corresponds to the Subsystem model in the Django application.
 */
@Entity
@Table(name = "subsystems")
public class Subsystem {
    
    public enum Status {
        NOT_STARTED("Not Started"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        TESTING("Testing"),
        ISSUES("Issues");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.NOT_STARTED;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_subteam_id")
    private Subteam responsibleSubteam;
    
    @OneToMany(mappedBy = "subsystem", cascade = CascadeType.ALL)
    private List<Task> tasks = new ArrayList<>();
    
    // Constructors
    
    public Subsystem() {
        // Default constructor required by JPA
    }
    
    public Subsystem(String name) {
        this.name = name;
    }
    
    public Subsystem(String name, Status status) {
        this.name = name;
        this.status = status;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Subteam getResponsibleSubteam() {
        return responsibleSubteam;
    }

    public void setResponsibleSubteam(Subteam responsibleSubteam) {
        this.responsibleSubteam = responsibleSubteam;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
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
    
    @Override
    public String toString() {
        return name;
    }
}