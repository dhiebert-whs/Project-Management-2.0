package org.frcpm.services.impl;

import org.frcpm.models.Subsystem;
import org.frcpm.models.Subteam;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.repositories.spring.SubsystemRepository;
import org.frcpm.services.SubsystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of SubsystemService.
 */
@Service
@Transactional
public class SubsystemServiceImpl extends AbstractSpringService<Subsystem, Long> implements SubsystemService {
    
    private final SubsystemRepository subsystemRepository;
    
    @Autowired
    public SubsystemServiceImpl(SubsystemRepository subsystemRepository) {
        super(subsystemRepository);
        this.subsystemRepository = subsystemRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Subsystem> findByProject(Project project) {
        return subsystemRepository.findByProject(project);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Subsystem> findByProjectId(Long projectId) {
        return subsystemRepository.findByProjectId(projectId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Subsystem> findByOwnerSubteam(Subteam subteam) {
        return subsystemRepository.findByOwnerSubteam(subteam);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Subsystem> findByOwnerSubteamId(Long subteamId) {
        return subsystemRepository.findByOwnerSubteamId(subteamId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Subsystem> findByNameAndProject(String name, Project project) {
        return subsystemRepository.findByNameAndProject(name, project);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Subsystem> findByNameIgnoreCaseAndProject(String name, Project project) {
        return subsystemRepository.findByNameIgnoreCaseAndProject(name, project);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Subsystem> findByProjectOrderedByName(Project project) {
        return subsystemRepository.findByProjectOrderByName(project);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Subsystem> findByProjectAndOwnerSubteam(Project project, Subteam subteam) {
        return subsystemRepository.findByProjectAndOwnerSubteam(project, subteam);
    }
    
    @Override
    public Subsystem addTask(Subsystem subsystem, Task task) {
        subsystem.addTask(task);
        return subsystemRepository.save(subsystem);
    }
    
    @Override
    public Subsystem removeTask(Subsystem subsystem, Task task) {
        subsystem.removeTask(task);
        return subsystemRepository.save(subsystem);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countTasks(Long subsystemId) {
        return subsystemRepository.countTasksBySubsystemId(subsystemId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countCompletedTasks(Long subsystemId) {
        return subsystemRepository.countCompletedTasksBySubsystemId(subsystemId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public double calculateAverageProgress(Long subsystemId) {
        Double avgProgress = subsystemRepository.calculateAverageProgressBySubsystemId(subsystemId);
        return avgProgress != null ? avgProgress : 0.0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Subsystem> findSubsystemsWithTasks(Long projectId) {
        return subsystemRepository.findSubsystemsWithTasks(projectId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameInProject(String name, Long projectId) {
        return subsystemRepository.existsByNameIgnoreCaseAndProjectId(name, projectId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameInProjectExcluding(String name, Long projectId, Long excludeId) {
        Optional<Subsystem> existing = subsystemRepository.findByNameIgnoreCaseAndProject(name, 
            new Project() {{ setId(projectId); }});
        return existing.isPresent() && !existing.get().getId().equals(excludeId);
    }
}