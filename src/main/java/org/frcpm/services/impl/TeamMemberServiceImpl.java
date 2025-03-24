package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.repositories.specific.TeamMemberRepository;
import org.frcpm.services.TeamMemberService;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of TeamMemberService using repository layer.
 */
public class TeamMemberServiceImpl extends AbstractService<TeamMember, Long, TeamMemberRepository> 
        implements TeamMemberService {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberServiceImpl.class.getName());
    private final SubteamRepository subteamRepository;
    
    public TeamMemberServiceImpl() {
        super(RepositoryFactory.getTeamMemberRepository());
        this.subteamRepository = RepositoryFactory.getSubteamRepository();
    }
    
    @Override
    public Optional<TeamMember> findByUsername(String username) {
        return repository.findByUsername(username);
    }
    
    @Override
    public List<TeamMember> findBySubteam(Subteam subteam) {
        return repository.findBySubteam(subteam);
    }
    
    @Override
    public List<TeamMember> findBySkill(String skill) {
        return repository.findBySkill(skill);
    }
    
    @Override
    public List<TeamMember> findLeaders() {
        return repository.findLeaders();
    }
    
    @Override
    public TeamMember createTeamMember(String username, String firstName, String lastName, 
                                       String email, String phone, boolean isLeader) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        if (repository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        TeamMember member = new TeamMember(username, firstName, lastName, email);
        member.setPhone(phone);
        member.setLeader(isLeader);
        
        return save(member);
    }
    
    @Override
    public TeamMember assignToSubteam(Long memberId, Long subteamId) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        
        TeamMember member = findById(memberId);
        if (member == null) {
            LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
            return null;
        }
        
        Subteam subteam = null;
        if (subteamId != null) {
            subteam = subteamRepository.findById(subteamId).orElse(null);
            if (subteam == null) {
                LOGGER.log(Level.WARNING, "Subteam not found with ID: {0}", subteamId);
                return null;
            }
        }
        
        // If member already has a subteam, remove them from it
        if (member.getSubteam() != null) {
            member.getSubteam().removeMember(member);
        }
        
        // Assign to new subteam
        if (subteam != null) {
            subteam.addMember(member);
        } else {
            member.setSubteam(null);
        }
        
        return save(member);
    }
    
    @Override
    public TeamMember updateSkills(Long memberId, String skills) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        
        TeamMember member = findById(memberId);
        if (member == null) {
            LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
            return null;
        }
        
        member.setSkills(skills);
        return save(member);
    }
    
    @Override
    public TeamMember updateContactInfo(Long memberId, String email, String phone) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        
        TeamMember member = findById(memberId);
        if (member == null) {
            LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
            return null;
        }
        
        if (email != null) {
            member.setEmail(email);
        }
        
        if (phone != null) {
            member.setPhone(phone);
        }
        
        return save(member);
    }
}
