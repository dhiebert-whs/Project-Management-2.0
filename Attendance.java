package org.frcpm.models;

import jakarta.persistence.*;
import java.time.LocalTime;

/**
 * Entity class representing attendance at a meeting in the FRC Project Management System.
 * This corresponds to the Attendance model in the Django application.
 */
@Entity
@Table(
    name = "attendances",
    uniqueConstraints = @UniqueConstraint(columnNames = {"meeting_id", "member_id"})
)
public class Attendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private TeamMember member;
    
    @Column(name = "present", nullable = false)
    private boolean present = false;
    
    @Column(name = "arrival_time")
    private LocalTime arrivalTime;
    
    @Column(name = "departure_time")
    private LocalTime departureTime;
    
    // Constructors
    
    public Attendance() {
        // Default constructor required by JPA
    }
    
    public Attendance(Meeting meeting, TeamMember member, boolean present) {
        this.meeting = meeting;
        this.member = member;
        this.present = present;
        
        // If present, set arrival time to meeting start time by default
        if (present && meeting != null) {
            this.arrivalTime = meeting.getStartTime();
        }
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public TeamMember getMember() {
        return member;
    }

    public void setMember(TeamMember member) {
        this.member = member;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
        
        // Clear times if not present
        if (!present) {
            this.arrivalTime = null;
            this.departureTime = null;
        }
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }
    
    // Helper methods
    
    /**
     * Calculates the duration of attendance in minutes.
     * 
     * @return the duration in minutes, or 0 if arrival or departure time is missing
     */
    public long getDurationMinutes() {
        if (!present || arrivalTime == null || departureTime == null) {
            return 0;
        }
        
        return java.time.Duration.between(arrivalTime, departureTime).toMinutes();
    }
    
    @Override
    public String toString() {
        String status = present ? "Present" : "Absent";
        return member + " - " + status + " at " + meeting;
    }
}
