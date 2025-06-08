package org.frcpm.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.frcpm.models.Subteam;
import org.frcpm.repositories.specific.SubteamRepository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of SubteamRepository using JPA.
 */
public class SubteamRepositoryImpl extends JpaRepositoryImpl<Subteam, Long> implements SubteamRepository {
    
    private static final Logger LOGGER = Logger.getLogger(SubteamRepositoryImpl.class.getName());
    
    @Override
    public Optional<Subteam> findByName(String name) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Subteam> query = em.createQuery(
                    "SELECT s FROM Subteam s WHERE s.name = :name", Subteam.class);
            query.setParameter("name", name);
            return query.getResultStream().findFirst();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding subteam by name", e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Subteam> findByColorCode(String colorCode) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Subteam> query = em.createQuery(
                    "SELECT s FROM Subteam s WHERE s.colorCode = :colorCode", Subteam.class);
            query.setParameter("colorCode", colorCode);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding subteams by color code", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Subteam> findBySpecialty(String specialty) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Subteam> query = em.createQuery(
                    "SELECT s FROM Subteam s WHERE s.specialties LIKE :specialty", Subteam.class);
            query.setParameter("specialty", "%" + specialty + "%");
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding subteams by specialty", e);
            return List.of();
        } finally {
            em.close();
        }
    }
}