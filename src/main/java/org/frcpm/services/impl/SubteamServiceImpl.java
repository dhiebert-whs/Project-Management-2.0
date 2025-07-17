package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.spring.SubteamRepository;
import org.frcpm.services.SubteamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of SubteamService.
 */
@Service
@Transactional
public class SubteamServiceImpl extends AbstractSpringService<Subteam, Long> implements SubteamService {
    
    private final SubteamRepository subteamRepository;
    
    @Autowired
    public SubteamServiceImpl(SubteamRepository subteamRepository) {
        super(subteamRepository);
        this.subteamRepository = subteamRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Subteam> findByName(String name) {
        return subteamRepository.findByName(name);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Subteam> findByNameIgnoreCase(String name) {
        return subteamRepository.findByNameIgnoreCase(name);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Subteam> findAllOrderedByName() {
        return subteamRepository.findAllByOrderByName();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Subteam> findSubteamsWithMembers() {
        return subteamRepository.findSubteamsWithMembers();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Subteam> findSubteamsWithSubsystems() {
        return subteamRepository.findSubteamsWithSubsystems();
    }
    
    @Override
    public Subteam addMember(Subteam subteam, TeamMember member) {
        subteam.addMember(member);
        return subteamRepository.save(subteam);
    }
    
    @Override
    public Subteam removeMember(Subteam subteam, TeamMember member) {
        subteam.removeMember(member);
        return subteamRepository.save(subteam);
    }
    
    @Override
    public Subteam addSubsystem(Subteam subteam, Subsystem subsystem) {
        subteam.addSubsystem(subsystem);
        return subteamRepository.save(subteam);
    }
    
    @Override
    public Subteam removeSubsystem(Subteam subteam, Subsystem subsystem) {
        subteam.removeSubsystem(subsystem);
        return subteamRepository.save(subteam);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countMembers(Long subteamId) {
        return subteamRepository.countMembersBySubteamId(subteamId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countSubsystems(Long subteamId) {
        return subteamRepository.countSubsystemsBySubteamId(subteamId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return subteamRepository.existsByNameIgnoreCase(name);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameExcluding(String name, Long excludeId) {
        Optional<Subteam> existing = subteamRepository.findByNameIgnoreCase(name);
        return existing.isPresent() && !existing.get().getId().equals(excludeId);
    }
    
    @Override
    public List<Subteam> createDefaultSubteams() {
        List<Subteam> defaultSubteams = new ArrayList<>();
        
        // Create default FRC subteams
        defaultSubteams.add(new Subteam("Mechanical", "Mechanical design and fabrication"));
        defaultSubteams.add(new Subteam("Programming", "Robot programming and controls"));
        defaultSubteams.add(new Subteam("Electrical", "Electrical systems and wiring"));
        defaultSubteams.add(new Subteam("CAD", "Computer-aided design"));
        defaultSubteams.add(new Subteam("Business", "Business plan and outreach"));
        defaultSubteams.add(new Subteam("Media", "Marketing and media relations"));
        
        // Set default colors
        defaultSubteams.get(0).setColor("#FF6B6B"); // Red for Mechanical
        defaultSubteams.get(1).setColor("#4ECDC4"); // Teal for Programming
        defaultSubteams.get(2).setColor("#45B7D1"); // Blue for Electrical
        defaultSubteams.get(3).setColor("#96CEB4"); // Green for CAD
        defaultSubteams.get(4).setColor("#FFEAA7"); // Yellow for Business
        defaultSubteams.get(5).setColor("#DDA0DD"); // Purple for Media
        
        // Save all default subteams
        return subteamRepository.saveAll(defaultSubteams);
    }
    
    @Override
    public Subteam createSubteam(String name, String color, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subteam name cannot be null or empty");
        }
        
        // Check if subteam with this name already exists
        if (existsByName(name)) {
            throw new IllegalArgumentException("Subteam with name '" + name + "' already exists");
        }
        
        Subteam subteam = new Subteam(name, description);
        
        if (color != null && !color.trim().isEmpty()) {
            subteam.setColor(color);
        } else {
            // Set default color if none provided
            subteam.setColor("#6C757D"); // Bootstrap secondary color
        }
        
        return subteamRepository.save(subteam);
    }
}