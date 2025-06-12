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
 * CORRECTED: Uses dual repository pattern - typed repository for custom methods,
 * inherited methods for basic CRUD operations.
 */
@Service("teamMemberServiceImpl")
@Transactional
public class TeamMemberServiceImpl extends AbstractSpringService<TeamMember, Long> 
        implements TeamMemberService {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberServiceImpl.class.getName());
    
    // SOLUTION: Keep typed repository references for custom methods
    private final TeamMemberRepository teamMemberRepository;
    private final SubteamRepository subteamRepository;
    
    public TeamMemberServiceImpl(
            TeamMemberRepository teamMemberRepository,
            SubteamRepository subteamRepository) {
        super(teamMemberRepository);  // Pass to AbstractSpringService for basic CRUD
        this.teamMemberRepository = teamMemberRepository;  // Keep for custom methods
        this.subteamRepository = subteamRepository;
    }

    @Override
    protected String getEntityName() {
        return "team member";
    }
    
    // TeamMember-specific business methods - USE TYPED REPOSITORY
    
    @Override
    public Optional<TeamMember> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        // FIXED: Use typed repository for custom method
        return teamMemberRepository.findByUsername(username);
    }
    
    @Override
    public List<TeamMember> findBySubteam(Subteam subteam) {
        if (subteam == null) {
            throw new IllegalArgumentException("Subteam cannot be null");
        }
        // FIXED: Use typed repository for custom method
        return teamMemberRepository.findBySubteam(subteam);
    }
    
    @Override
    public List<TeamMember> findBySkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill cannot be empty");
        }
        // FIXED: Use typed repository for custom method
        return teamMemberRepository.findBySkillsContainingIgnoreCase(skill);
    }
    
    @Override
    public List<TeamMember> findLeaders() {
        // FIXED: Use typed repository for custom method
        return teamMemberRepository.findByIsLeaderTrue();
    }
    
    @Override
    public TeamMember createTeamMember(String username, String firstName, String lastName, 
                                      String email, String phone, boolean isLeader) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        // Check if username already exists using typed repository
        Optional<TeamMember> existing = teamMemberRepository.findByUsername(username);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Create new team member
        TeamMember member = new TeamMember(username, firstName, lastName, email);
        member.setPhone(phone);
        member.setLeader(isLeader);
        
        // Use inherited save() method from AbstractSpringService
        return save(member);
    }
    
    @Override
    public TeamMember assignToSubteam(Long memberId, Long subteamId) {
        if (memberId == null) {
            throw new IllegalArgumentException("Team member ID cannot be null");
        }
        
        // Use inherited findById() method from AbstractSpringService
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
        
        // Use inherited save() method from AbstractSpringService
        return save(member);
    }
    
    @Override
    public TeamMember updateSkills(Long memberId, String skills) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        
        // Use inherited findById() method from AbstractSpringService
        TeamMember member = findById(memberId);
        if (member == null) {
            LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
            return null;
        }
        
        member.setSkills(skills);
        
        // Use inherited save() method from AbstractSpringService
        return save(member);
    }
    
    @Override
    public TeamMember updateContactInfo(Long memberId, String email, String phone) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        
        // Use inherited findById() method from AbstractSpringService
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
        
        // Use inherited save() method from AbstractSpringService
        return save(member);
    }

    // Spring Boot Async Methods - Use appropriate method sources
    
    @Async
    public CompletableFuture<List<TeamMember>> findAllAsync() {
        try {
            // Use inherited findAll() method from AbstractSpringService
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
            // Use inherited findById() method from AbstractSpringService
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
            // Use inherited save() method from AbstractSpringService
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
            // Use inherited deleteById() method from AbstractSpringService
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
    
    @Async
    public CompletableFuture<TeamMember> updateSkillsAsync(Long memberId, String skills) {
        try {
            TeamMember result = updateSkills(memberId, skills);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<TeamMember> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<TeamMember> updateContactInfoAsync(Long memberId, String email, String phone) {
        try {
            TeamMember result = updateContactInfo(memberId, email, phone);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<TeamMember> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}