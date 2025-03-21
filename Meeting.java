package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing a team meeting in the FRC Project Management System.
 * This corresponds to the Meeting model in the Django application.
 */
@Entity
@Table(name = "meetings")
public class Meeting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances = new ArrayList<>();
    
    // Constructors
    
    public Meeting() {
        // Default constructor required by JPA
    }
    
    public Meeting(LocalDate date, LocalTime startTime, LocalTime endTime, Project project) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.project = project;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Attendance> getAttendances() {
        return attendances;
    }

    public void setAttendances(List<Attendance> attendances) {
        this.attendances = attendances;
    }
    
    // Helper methods
    
    public void addAttendance(Attendance attendance) {
        attendances.add(attendance);
        attendance.setMeeting(this);
    }
    
    public void removeAttendance(Attendance attendance) {
        attendances.remove(attendance);
        attendance.setMeeting(null);
    }
    
    /**
     * Gets the number of present team members.
     * 
     * @return the count of attendances where present is true
     */
    public long getPresentCount() {
        return attendances.stream().filter(Attendance::isPresent).count();
    }
    
    /**
     * Gets the attendance percentage.
     * 
     * @return the percentage of team members who were present, or 0 if no attendances
     */
    public double getAttendancePercentage() {
        if (attendances.isEmpty()) {
            return 0;
        }
        return (double) getPresentCount() / attendances.size() * 100;
    }
    
    @Override
    public String toString() {
        return "Meeting on " + date + " (" + startTime + "-" + endTime + ")";
    }
}