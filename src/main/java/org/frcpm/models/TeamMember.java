package org.frcpm.models;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity class representing a team member in the FRC Project Management System.
 * This corresponds to the TeamMember model in the Django application.
 */
@Entity
@Table(name = "team_members")
public class TeamMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", length = 150, nullable = false, unique = true)
    private String username;
    
    @Column(name = "first_name", length = 150)
    private String firstName;
    
    @Column(name = "last_name", length = 150)
    private String lastName;
    
    @Column(name = "email", length = 254)
    private String email;
    
    @Column(name = "phone", length = 15)
    private String phone;
    
    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;
    
    @Column(name = "is_leader")
    private boolean leader;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subteam_id")
    private Subteam subteam;
    
    @ManyToMany(mappedBy = "assignedTo")
    private Set<Task> assignedTasks = new HashSet<>();
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Attendance> attendances = new HashSet<>();
    
    // Constructors
    
    public TeamMember() {
        // Default constructor required by JPA
    }
    
    public TeamMember(String username, String firstName, String lastName, String email) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public Subteam getSubteam() {
        return subteam;
    }

    public void setSubteam(Subteam subteam) {
        this.subteam = subteam;
    }

    public Set<Task> getAssignedTasks() {
        return assignedTasks;
    }

    public void setAssignedTasks(Set<Task> assignedTasks) {
        this.assignedTasks = assignedTasks;
    }

    public Set<Attendance> getAttendances() {
        return attendances;
    }

    public void setAttendances(Set<Attendance> attendances) {
        this.attendances = attendances;
    }
    
    // Helper methods
    
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username;
        }
    }
    
    public void addAttendance(Attendance attendance) {
        attendances.add(attendance);
        attendance.setMember(this);
    }
    
    public void removeAttendance(Attendance attendance) {
        attendances.remove(attendance);
        attendance.setMember(null);
    }
    
    @Override
    public String toString() {
        return getFullName();
    }
}