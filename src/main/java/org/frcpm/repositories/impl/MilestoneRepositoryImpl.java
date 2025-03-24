package org.frcpm.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.MilestoneRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of MilestoneRepository using JPA.
 */
public class MilestoneRepositoryImpl extends JpaRepositoryImpl<Milestone, Long> implements MilestoneRepository {
    
    private static final Logger LOGGER = Logger.getLogger(MilestoneRepositoryImpl.class.getName());
    
    @Override
    public List<Milestone> findByProject(Project project) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Milestone> query = em.createQuery(
                    "SELECT m FROM Milestone m WHERE m.project = :project", Milestone.class);
            query.setParameter("project", project);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding milestones by project", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Milestone> findByDateBefore(LocalDate date) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Milestone> query = em.createQuery(
                    "SELECT m FROM Milestone m WHERE m.date < :date", Milestone.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding milestones before date", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Milestone> findByDateAfter(LocalDate date) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Milestone> query = em.createQuery(
                    "SELECT m FROM Milestone m WHERE m.date > :date", Milestone.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding milestones after date", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Milestone> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Milestone> query = em.createQuery(
                    "SELECT m FROM Milestone m WHERE m.date >= :startDate AND m.date <= :endDate", 
                    Milestone.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding milestones between dates", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Milestone> findByName(String name) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Milestone> query = em.createQuery(
                    "SELECT m FROM Milestone m WHERE m.name LIKE :name", Milestone.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding milestones by name", e);
            return List.of();
        } finally {
            em.close();
        }
    }
}
