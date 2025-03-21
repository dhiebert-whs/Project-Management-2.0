package org.frcpm.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing a subteam in the FRC Project Management System.
 * This corresponds to the Subteam model in the Django application.
 */
@Entity
@Table(name = "subteams")
public class Subteam {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "color_code", length = 7, nullable = false)
    private String colorCode;
    
    @Column(name = "specialties", columnDefinition = "TEXT")
    private String specialties;
    
    @OneToMany(mappedBy = "subteam", cascade = CascadeType.ALL)
    private List<TeamMember> members = new ArrayList<>();
    
    @OneToMany(mappedBy = "responsibleSubteam")
    private List<Subsystem> subsystems = new ArrayList<>();
    
    // Constructors
    
    public Subteam() {
        // Default constructor required by JPA
    }
    
    public Subteam(String name, String colorCode) {
        this.name = name;
        this.colorCode = colorCode;
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

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getSpecialties() {
        return specialties;
    }

    public void setSpecialties(String specialties) {
        this.specialties = specialties;
    }

    public List<TeamMember> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMember> members) {
        this.members = members;
    }

    public List<Subsystem> getSubsystems() {
        return subsystems;
    }

    public void setSubsystems(List<Subsystem> subsystems) {
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
        subsystem.setResponsibleSubteam(this);
    }
    
    public void removeSubsystem(Subsystem subsystem) {
        subsystems.remove(subsystem);
        subsystem.setResponsibleSubteam(null);
    }
    
    @Override
    public String toString() {
        return name;
    }
}