package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.SubsystemRepository;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.services.SubsystemService;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of SubsystemService using repository layer.
 */
public class SubsystemServiceImpl extends AbstractService<Subsystem, Long, SubsystemRepository> 
        implements SubsystemService {
    
    private static final Logger LOGGER = Logger.getLogger(SubsystemServiceImpl.class.getName());
    private final SubteamRepository subteamRepository;
    
    public SubsystemServiceImpl() {
        super(RepositoryFactory.getSubsystemRepository());
        this.subteamRepository = RepositoryFactory.getSubteamRepository();
    }
    
    @Override
    public Optional<Subsystem> findByName(String name) {
        return repository.findByName(name);
    }
    
    @Override
    public List<Subsystem> findByStatus(Subsystem.Status status) {
        return repository.findByStatus(status);
    }
    
    @Override
    public List<Subsystem> findByResponsibleSubteam(Subteam subteam) {
        return repository.findByResponsibleSubteam(subteam);
    }
    
    @Override
    public Subsystem createSubsystem(String name, String description, 
                                    Subsystem.Status status, Long responsibleSubteamId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subsystem name cannot be empty");
        }
        
        if (repository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Subsystem with name '" + name + "' already exists");
        }
        
        Subsystem subsystem = new Subsystem(name);
        subsystem.setDescription(description);
        
        if (status != null) {
            subsystem.setStatus(status);
        }
        
        if (responsibleSubteamId != null) {
            Subteam subteam = subteamRepository.findById(responsibleSubteamId).orElse(null);
            if (subteam == null) {
                LOGGER.log(Level.WARNING, "Subteam not found with ID: {0}", responsibleSubteamId);
            } else {
                subsystem.setResponsibleSubteam(subteam);
            }
        }
        
        return save(subsystem);
    }
    
    @Override
    public Subsystem updateStatus(Long subsystemId, Subsystem.Status status) {
        if (subsystemId == null) {
            throw new IllegalArgumentException("Subsystem ID cannot be null");
        }
        
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        Subsystem subsystem = findById(subsystemId);
        if (subsystem == null) {
            LOGGER.log(Level.WARNING, "Subsystem not found with ID: {0}", subsystemId);
            return null;
        }
        
        subsystem.setStatus(status);
        return save(subsystem);
    }
    
    @Override
    public Subsystem assignResponsibleSubteam(Long subsystemId, Long subteamId) {
        if (subsystemId == null) {
            throw new IllegalArgumentException("Subsystem ID cannot be null");
        }
        
        Subsystem subsystem = findById(subsystemId);
        if (subsystem == null) {
            LOGGER.log(Level.WARNING, "Subsystem not found with ID: {0}", subsystemId);
            return null;
        }
        
        if (subteamId == null) {
            subsystem.setResponsibleSubteam(null);
        } else {
            Subteam subteam = subteamRepository.findById(subteamId).orElse(null);
            if (subteam == null) {
                LOGGER.log(Level.WARNING, "Subteam not found with ID: {0}", subteamId);
                return null;
            }
            subsystem.setResponsibleSubteam(subteam);
        }
        
        return save(subsystem);
    }
}