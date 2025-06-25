// src/main/java/org/frcpm/models/UserRole.java

package org.frcpm.models;

import org.springframework.security.core.GrantedAuthority;

/**
 * User role enumeration implementing Spring Security GrantedAuthority.
 * 
 * Defines the four primary roles in the FRC Project Management System:
 * - STUDENT: Team members with limited access
 * - MENTOR: Adult supervisors with full project access
 * - PARENT: Limited access for COPPA compliance
 * - ADMIN: System administrators with all privileges
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
public enum UserRole implements GrantedAuthority {
    STUDENT("ROLE_STUDENT", "Student team member", 15 * 60), // 15 minutes
    MENTOR("ROLE_MENTOR", "Adult mentor or coach", 30 * 60), // 30 minutes
    PARENT("ROLE_PARENT", "Parent with limited access", 30 * 60), // 30 minutes
    ADMIN("ROLE_ADMIN", "System administrator", 30 * 60); // 30 minutes
    
    private final String authority;
    private final String description;
    private final int sessionTimeoutSeconds;
    
    UserRole(String authority, String description, int sessionTimeoutSeconds) {
        this.authority = authority;
        this.description = description;
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }
    
    @Override
    public String getAuthority() {
        return authority;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }
    
    /**
     * Determines if this role is a mentor-level role.
     * 
     * @return true for MENTOR and ADMIN roles
     */
    public boolean isMentor() {
        return this == MENTOR || this == ADMIN;
    }
    
    /**
     * Determines if this role is a student.
     * 
     * @return true for STUDENT role only
     */
    public boolean isStudent() {
        return this == STUDENT;
    }
    
    /**
     * Determines if this role requires multi-factor authentication.
     * 
     * @return true for MENTOR and ADMIN roles
     */
    public boolean requiresMFA() {
        return this == MENTOR || this == ADMIN;
    }
    
    /**
     * Determines if this role can access admin functions.
     * 
     * @return true for ADMIN role only
     */
    public boolean canAdminister() {
        return this == ADMIN;
    }

    public boolean canManageUsers() {
        return this == MENTOR || this == ADMIN;
    }
    
    public boolean canAccessReports() {
        return this == MENTOR || this == ADMIN;
    }
    
    /**
     * Determines if this role can manage team members.
     * 
     * @return true for MENTOR and ADMIN roles
     */
    public boolean canManageTeam() {
        return this == MENTOR || this == ADMIN;
    }
    
    /**
     * Determines if this role can create/edit projects.
     * 
     * @return true for MENTOR and ADMIN roles
     */
    public boolean canManageProjects() {
        return this == MENTOR || this == ADMIN;
    }
    
    /**
     * Determines if this role can assign tasks to others.
     * 
     * @return true for MENTOR and ADMIN roles
     */
    public boolean canAssignTasks() {
        return this == MENTOR || this == ADMIN;
    }
    
    /**
     * Determines if this role has view-only access (for COPPA compliance).
     * 
     * @return true for PARENT role
     */
    public boolean isViewOnly() {
        return this == PARENT;
    }
}