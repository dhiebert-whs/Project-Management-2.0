package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing a project in the FRC Project Management System.
 * This corresponds to the Project model in the Django application.
 */
@Entity
@Table(name = "projects")
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "goal_end_date", nullable = false)
    private LocalDate goalEndDate;
    
    @Column(name = "hard_deadline", nullable = false)
    private LocalDate hardDeadline;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Milestone> milestones = new ArrayList<>();
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meeting> meetings = new ArrayList<>();
    
    // Constructors
    
    public Project() {
        // Default constructor required by JPA
    }
    
    public Project(String name, LocalDate startDate, LocalDate goalEndDate, LocalDate hardDeadline) {
        this.name = name;
        this.startDate = startDate;
        this.goalEndDate = goalEndDate;
        this.hardDeadline = hardDeadline;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getGoalEndDate() {
        return goalEndDate;
    }

    public void setGoalEndDate(LocalDate goalEndDate) {
        this.goalEndDate = goalEndDate;
    }

    public LocalDate getHardDeadline() {
        return hardDeadline;
    }

    public void setHardDeadline(LocalDate hardDeadline) {
        this.hardDeadline = hardDeadline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }

    public List<Meeting> getMeetings() {
        return meetings;
    }

    public void setMeetings(List<Meeting> meetings) {
        this.meetings = meetings;
    }
    
    // Helper methods
    
    public void addTask(Task task) {
        tasks.add(task);
        task.setProject(this);
    }
    
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setProject(null);
    }
    
    public void addMilestone(Milestone milestone) {
        milestones.add(milestone);
        milestone.setProject(this);
    }
    
    public void removeMilestone(Milestone milestone) {
        milestones.remove(milestone);
        milestone.setProject(null);
    }
    
    public void addMeeting(Meeting meeting) {
        meetings.add(meeting);
        meeting.setProject(this);
    }
    
    public void removeMeeting(Meeting meeting) {
        meetings.remove(meeting);
        meeting.setProject(null);
    }
    
    @Override
    public String toString() {
        return name;
    }
}
