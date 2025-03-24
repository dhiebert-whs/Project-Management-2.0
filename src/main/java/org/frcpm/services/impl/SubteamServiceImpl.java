package org.frcpm.services.impl;

import org.frcpm.models.Subteam;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.SubteamRepository;
import org.frcpm.services.SubteamService;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of SubteamService using repository layer.
 */
public class SubteamServiceImpl extends AbstractService<Subteam, Long, SubteamRepository> 
        implements SubteamService {
    
    private static final Logger LOGGER = Logger.getLogger(SubteamServiceImpl.class.getName());
    
    public SubteamServiceImpl() {
        super(RepositoryFactory.getSubteamRepository());
    }
    
    @Override
    public Optional<Subteam> findByName(String name) {
        return repository.findByName(name);
    }
    
    @Override
    public List<Subteam> findBySpecialty(String specialty) {
        return repository.findBySpecialty(specialty);
    }
    
    @Override
    public Subteam createSubteam(String name, String colorCode, String specialties) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subteam name cannot be empty");
        }
        
        if (colorCode == null || !colorCode.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("Color code must be a valid hex color code");
        }
        
        if (repository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Subteam with name '" + name + "' already exists");
        }
        
        Subteam subteam = new Subteam(name, colorCode);
        subteam.setSpecialties(specialties);
        
        return save(subteam);
    }
    
    @Override
    public Subteam updateSpecialties(Long subteamId, String specialties) {
        if (subteamId == null) {
            throw new IllegalArgumentException("Subteam ID cannot be null");
        }
        
        Subteam subteam = findById(subteamId);
        if (subteam == null) {
            LOGGER.log(Level.WARNING, "Subteam not found with ID: {0}", subteamId);
            return null;
        }
        
        subteam.setSpecialties(specialties);
        return save(subteam);
    }
    
    @Override
    public Subteam updateColorCode(Long subteamId, String colorCode) {
        if (subteamId == null) {
            throw new IllegalArgumentException("Subteam ID cannot be null");
        }
        
        if (colorCode == null || !colorCode.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("Color code must be a valid hex color code");
        }
        
        Subteam subteam = findById(subteamId);
        if (subteam == null) {
            LOGGER.log(Level.WARNING, "Subteam not found with ID: {0}", subteamId);
            return null;
        }
        
        subteam.setColorCode(colorCode);
        return save(subteam);
    }
}