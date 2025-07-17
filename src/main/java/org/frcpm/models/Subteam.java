package org.frcpm.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing FRC team subteams (mechanical, programming, electrical, etc).
 * Each team member belongs to one or more subteams.
 */
@Entity
@Table(name = "subteams")
public class Subteam {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Size(max = 500)
    @Column(name = "description")
    private String description;
    
    @Size(max = 7)
    @Column(name = "color")
    private String color; // Hex color for UI display
    
    @OneToMany(mappedBy = "subteam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TeamMember> members = new HashSet<>();
    
    @OneToMany(mappedBy = "ownerSubteam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Subsystem> subsystems = new HashSet<>();
    
    // Constructors
    
    public Subteam() {
        // Default constructor required by JPA
    }
    
    public Subteam(String name) {
        this.name = name;
    }
    
    public Subteam(String name, String description) {
        this.name = name;
        this.description = description;
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
    
    public Set<TeamMember> getMembers() {
        return members;
    }
    
    public void setMembers(Set<TeamMember> members) {
        this.members = members;
    }
    
    public Set<Subsystem> getSubsystems() {
        return subsystems;
    }
    
    public void setSubsystems(Set<Subsystem> subsystems) {
        this.subsystems = subsystems;
    }
    
    // Helper methods
    
    public void addMember(TeamMember member) {
        members.add(member);
        member.setSubteam(this);
    }
    
    public void removeMember(TeamMember member) {
        members.remove(member);
        member.setSubteam(null);
    }
    
    public void addSubsystem(Subsystem subsystem) {
        subsystems.add(subsystem);
        subsystem.setOwnerSubteam(this);
    }
    
    public void removeSubsystem(Subsystem subsystem) {
        subsystems.remove(subsystem);
        subsystem.setOwnerSubteam(null);
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subteam subteam = (Subteam) o;
        return id != null && id.equals(subteam.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}