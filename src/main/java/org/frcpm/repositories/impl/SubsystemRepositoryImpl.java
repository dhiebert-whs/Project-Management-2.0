package org.frcpm.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.frcpm.models.Subteam;
import org.frcpm.models.Subsystem;
import org.frcpm.repositories.specific.SubsystemRepository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of SubsystemRepository using JPA.
 */
public class SubsystemRepositoryImpl extends JpaRepositoryImpl<Subsystem, Long> implements SubsystemRepository {
    
    private static final Logger LOGGER = Logger.getLogger(SubsystemRepositoryImpl.class.getName());
    
    @Override
    public Optional<Subsystem> findByName(String name) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Subsystem> query = em.createQuery(
                    "SELECT s FROM Subsystem s WHERE s.name = :name", Subsystem.class);
            query.setParameter("name", name);
            return query.getResultStream().findFirst();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding subsystem by name", e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Subsystem> findByStatus(Subsystem.Status status) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Subsystem> query = em.createQuery(
                    "SELECT s FROM Subsystem s WHERE s.status = :status", Subsystem.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding subsystems by status", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Subsystem> findByResponsibleSubteam(Subteam subteam) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Subsystem> query = em.createQuery(
                    "SELECT s FROM Subsystem s WHERE s.responsibleSubteam = :subteam", Subsystem.class);
            query.setParameter("subteam", subteam);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding subsystems by responsible subteam", e);
            return List.of();
        } finally {
            em.close();
        }
    }
}