// src/main/java/org/frcpm/services/impl/TeamMemberServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.SubteamRepository;
import org.frcpm.repositories.spring.TeamMemberRepository;
import org.frcpm.services.TeamMemberService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spring Boot implementation of TeamMemberService.
 * Updated to extend AbstractSpringService for consistent CRUD operations.
 */
@Service("teamMemberServiceImpl")
@Transactional
public class TeamMemberServiceImpl extends AbstractSpringService<TeamMember, Long, TeamMemberRepository> 
        implements TeamMemberService {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberServiceImpl.class.getName());
    
    // Additional dependencies injected via constructor
    private final SubteamRepository subteamRepository;
    
    
    public TeamMemberServiceImpl(
            TeamMemberRepository teamMemberRepository,
            SubteamRepository subteamRepository) {
        super(teamMemberRepository);
        this.subteamRepository = subteamRepository;
    }

    @Override
    protected String getEntityName() {
        return "team member";
    }
    
    // TeamMember-specific business methods
    
    @Override
    public Optional<TeamMember> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return repository.findByUsername(username);
    }
    
    @Override
    public List<TeamMember> findBySubteam(Subteam subteam) {
        if (subteam == null) {
            throw new IllegalArgumentException("Subteam cannot be null");
        }
        return repository.findBySubteam(subteam);
    }
    
    @Override
    public List<TeamMember> findBySkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill cannot be empty");
        }
        // Use the correct method name that matches the repository
        return repository.findBySkillsContainingIgnoreCase(skill);
    }
    
    @Override
    public List<TeamMember> findLeaders() {
        // Use the correct method name that matches the repository
        return repository.findByIsLeaderTrue();
    }
    
    @Override
    public TeamMember createTeamMember(String username, String firstName, String lastName, 
                                      String email, String phone, boolean isLeader) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        // Check if username already exists
        Optional<TeamMember> existing = repository.findByUsername(username);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Create new team member
        TeamMember member = new TeamMember(username, firstName, lastName, email);
        member.setPhone(phone);
        member.setLeader(isLeader);
        
        return save(member);
    }
    
    @Override
    public TeamMember assignToSubteam(Long memberId, Long subteamId) {
        if (memberId == null) {
            throw new IllegalArgumentException("Team member ID cannot be null");
        }
        
        TeamMember member = findById(memberId);
        if (member == null) {
            LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
            return null;
        }
        
        // Remove from current subteam if any
        Subteam currentSubteam = member.getSubteam();
        if (currentSubteam != null) {
            // Get the current subteam to avoid LazyInitializationException
            currentSubteam.getMembers().remove(member);
            member.setSubteam(null);
        }
        
        // Assign to new subteam if not null
        if (subteamId != null) {
            Optional<Subteam> subteamOpt = subteamRepository.findById(subteamId);
            if (subteamOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Subteam not found with ID: {0}", subteamId);
                return null;
            }
            
            Subteam subteam = subteamOpt.get();
            subteam.getMembers().add(member);
            member.setSubteam(subteam);
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

    // Spring Boot Async Methods
    
    @Async
    public CompletableFuture<List<TeamMember>> findAllAsync() {
        try {
            List<TeamMember> result = findAll();
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<TeamMember>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<TeamMember> findByIdAsync(Long id) {
        try {
            TeamMember result = findById(id);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<TeamMember> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<TeamMember> saveAsync(TeamMember entity) {
        try {
            TeamMember result = save(entity);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<TeamMember> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Boolean> deleteByIdAsync(Long id) {
        try {
            boolean result = deleteById(id);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Optional<TeamMember>> findByUsernameAsync(String username) {
        try {
            Optional<TeamMember> result = findByUsername(username);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Optional<TeamMember>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<List<TeamMember>> findBySubteamAsync(Subteam subteam) {
        try {
            List<TeamMember> result = findBySubteam(subteam);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<TeamMember>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<List<TeamMember>> findLeadersAsync() {
        try {
            List<TeamMember> result = findLeaders();
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<TeamMember>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<TeamMember> createTeamMemberAsync(String username, String firstName, String lastName, 
                                                              String email, String phone, boolean isLeader) {
        try {
            TeamMember result = createTeamMember(username, firstName, lastName, email, phone, isLeader);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<TeamMember> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<TeamMember> assignToSubteamAsync(Long memberId, Long subteamId) {
        try {
            TeamMember result = assignToSubteam(memberId, subteamId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<TeamMember> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}