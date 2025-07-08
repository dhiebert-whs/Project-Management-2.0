// src/main/java/org/frcpm/models/TeamMember.java (Updated for User Integration)

package org.frcpm.models;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Enhanced TeamMember entity with User authentication integration.
 * 
 * This entity represents the FRC team member profile information,
 * now linked to the User authentication system for comprehensive
 * user management and COPPA compliance.
 * 
 * Key Changes in Phase 2B:
 * - Added one-to-one relationship with User entity
 * - Preserved all existing functionality for backward compatibility
 * - Enhanced with authentication-aware methods
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - Enhanced with User Integration
 */
@Entity
@Table(name = "team_members")
public class TeamMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Authentication relationship (new in Phase 2B)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true) User user;
    
    // Core profile information (preserved from Phase 1/2A)
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
    
    // Relationships (preserved from Phase 1/2A)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subteam_id")
    private Subteam subteam;
    
    @ManyToMany(mappedBy = "assignedTo", fetch = FetchType.LAZY)
    private Set<Task> assignedTasks = new HashSet<>();
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
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
    
    // Enhanced business logic methods (Phase 2B additions)
    
    /**
     * Determines if this team member has authentication enabled.
     * 
     * @return true if linked to a User account
     */
    public boolean hasAuthentication() {
        return user != null;
    }
    
    /**
     * Gets the authentication role if available.
     * 
     * @return UserRole or null if no authentication
     */
    public UserRole getAuthenticationRole() {
        return user != null ? user.getRole() : null;
    }
    
    /**
     * Determines if this member can perform mentor-level actions.
     * 
     * @return true if has mentor privileges
     */
    public boolean canMentor() {
        return leader || (user != null && user.getRole().isMentor());
    }
    
    /**
     * Determines if this member requires COPPA compliance.
     * 
     * @return true if user is under 13 and requires protection
     */
    public boolean requiresCOPPACompliance() {
        return user != null && user.requiresCOPPACompliance();
    }
    
    /**
     * Gets display name prioritizing authentication data.
     * 
     * @return best available full name
     */
    public String getDisplayName() {
        if (user != null) {
            return user.getFullName();
        }
        return getFullName();
    }
    
    /**
     * Gets primary email prioritizing authentication data.
     * 
     * @return best available email address
     */
    public String getPrimaryEmail() {
        if (user != null) {
            return user.getEmail();
        }
        return email;
    }
    
    // Preserved business logic methods (Phase 1/2A compatibility)
    
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
    
    // Data synchronization methods (Phase 2B helpers)
    
    /**
     * Synchronizes profile data from linked User account.
     * This ensures consistency between authentication and profile data.
     */
    public void syncFromUser() {
        if (user != null) {
            this.username = user.getUsername();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.email = user.getEmail();
            
            // Determine leadership based on role
            if (user.getRole() != null) {
                this.leader = user.getRole().isMentor();
            }
        }
    }
    
    /**
     * Synchronizes data to linked User account.
     * Updates authentication data from profile changes.
     */
    public void syncToUser() {
        if (user != null) {
            user.setUsername(this.username);
            user.setFirstName(this.firstName);
            user.setLastName(this.lastName);
            user.setEmail(this.email);
        }
    }
    
    // Standard getters and setters (preserved for compatibility)
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        // Prevent circular reference
        if (this.user != user) {
            // Clear old relationship if exists
            if (this.user != null && this.user.getTeamMember() == this) {
                this.user.teamMember = null; // Direct field access to avoid recursion
            }
            
            this.user = user;
            
            // Set reverse relationship only if not already set
            if (user != null && user.getTeamMember() != this) {
                user.teamMember = this; // Direct field access to avoid recursion
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        // Sync to user if linked
        if (user != null) {
            user.setUsername(username);
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        // Sync to user if linked
        if (user != null) {
            user.setFirstName(firstName);
        }
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        // Sync to user if linked
        if (user != null) {
            user.setLastName(lastName);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        // Sync to user if linked
        if (user != null) {
            user.setEmail(email);
        }
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
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}