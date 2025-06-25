// src/main/java/org/frcpm/models/User.java

package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Enhanced User entity for authentication and COPPA compliance.
 * 
 * This entity extends beyond TeamMember to provide comprehensive
 * user management including authentication, role-based access control,
 * and COPPA compliance for users under 13.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2B - COPPA Compliance & Security
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false)
    private String username;
    
    @NotBlank
    @Size(min = 8, max = 255)
    @Column(nullable = false)
    private String password;
    
    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank
    @Size(min = 2, max = 50)
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank
    @Size(min = 2, max = 50)
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    // Account status flags
    @Column(nullable = false)
    private boolean enabled = true;
    
    @Column(name = "account_non_expired", nullable = false)
    private boolean accountNonExpired = true;
    
    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;
    
    @Column(name = "credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;
    
    // COPPA Compliance Fields
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "requires_parental_consent", nullable = false)
    private boolean requiresParentalConsent = false;
    
    @Column(name = "parental_consent_date")
    private LocalDateTime parentalConsentDate;
    
    @Column(name = "parental_consent_token")
    private String parentalConsentToken;
    
    @Column(name = "parent_email")
    private String parentEmail;
    
    // MFA Fields
    @Column(name = "totp_secret")
    private String totpSecret;
    
    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;
    
    // Audit fields
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    // Relationship to TeamMember (one-to-one)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TeamMember teamMember;
    
    // Constructors
    public User() {}
    
    public User(String username, String password, String email, String firstName, String lastName, UserRole role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
    
    // Business logic methods
    
    /**
     * Gets the user's full display name.
     * 
     * @return formatted full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Determines if this user is a minor (under 18).
     * 
     * @return true if user is under 18
     */
    public boolean isMinor() {
        return age != null && age < 18;
    }
    
    /**
     * Determines if this user requires COPPA compliance (under 13).
     * 
     * @return true if user is under 13 and requires COPPA protections
     */
    public boolean requiresCOPPACompliance() {
        return age != null && age < 13;
    }
    
    /**
     * Checks if user has parental consent (for COPPA compliance).
     * 
     * @return true if consent has been granted or not required
     */
    public boolean hasParentalConsent() {
        return !requiresCOPPACompliance() || 
               (parentalConsentDate != null && !requiresParentalConsent);
    }
    
    /**
     * Determines if user account should be accessible.
     * Combines standard Spring Security flags with COPPA compliance.
     * 
     * @return true if account is fully accessible
     */
    public boolean isAccountAccessible() {
        return enabled && 
               accountNonExpired && 
               accountNonLocked && 
               credentialsNonExpired &&
               hasParentalConsent();
    }
    
    /**
     * Gets the appropriate session timeout for this user's role.
     * 
     * @return session timeout in seconds
     */
    public int getSessionTimeoutSeconds() {
        if (role == null) {
            return 900; // 15 minutes default
        }
        
        switch (role) {
            case STUDENT:
                return 900;  // 15 minutes for students
            case MENTOR:
            case ADMIN:
                return 1800; // 30 minutes for mentors/admins
            case PARENT:
                return 1800; // 30 minutes for parents
            default:
                return 900;
        }
    }
    
    // Standard getters and setters
    
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
        // Automatically set COPPA requirement flag
        if (age != null && age < 13) {
            this.requiresParentalConsent = true;
        }
    }

    public boolean isRequiresParentalConsent() {
        return requiresParentalConsent;
    }

    public void setRequiresParentalConsent(boolean requiresParentalConsent) {
        this.requiresParentalConsent = requiresParentalConsent;
    }

    public LocalDateTime getParentalConsentDate() {
        return parentalConsentDate;
    }

    public void setParentalConsentDate(LocalDateTime parentalConsentDate) {
        this.parentalConsentDate = parentalConsentDate;
    }

    public String getParentalConsentToken() {
        return parentalConsentToken;
    }

    public void setParentalConsentToken(String parentalConsentToken) {
        this.parentalConsentToken = parentalConsentToken;
    }

    public String getParentEmail() {
        return parentEmail;
    }

    public void setParentEmail(String parentEmail) {
        this.parentEmail = parentEmail;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public TeamMember getTeamMember() {
        return teamMember;
    }

    public void setTeamMember(TeamMember teamMember) {
        this.teamMember = teamMember;
        if (teamMember != null) {
            teamMember.setUser(this);
        }
    }
    
    @Override
    public String toString() {
        return getFullName() + " (" + username + ")";
    }
}

