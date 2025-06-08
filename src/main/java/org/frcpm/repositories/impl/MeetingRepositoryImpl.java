package org.frcpm.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.MeetingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of MeetingRepository using JPA.
 */
public class MeetingRepositoryImpl extends JpaRepositoryImpl<Meeting, Long> implements MeetingRepository {
    
    private static final Logger LOGGER = Logger.getLogger(MeetingRepositoryImpl.class.getName());
    
    @Override
    public List<Meeting> findByProject(Project project) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Meeting> query = em.createQuery(
                    "SELECT m FROM Meeting m WHERE m.project = :project", Meeting.class);
            query.setParameter("project", project);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding meetings by project", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Meeting> findByDate(LocalDate date) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Meeting> query = em.createQuery(
                    "SELECT m FROM Meeting m WHERE m.date = :date", Meeting.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding meetings by date", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Meeting> findByDateAfter(LocalDate date) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Meeting> query = em.createQuery(
                    "SELECT m FROM Meeting m WHERE m.date > :date", Meeting.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding meetings after date", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Meeting> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Meeting> query = em.createQuery(
                    "SELECT m FROM Meeting m WHERE m.date >= :startDate AND m.date <= :endDate", 
                    Meeting.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding meetings between dates", e);
            return List.of();
        } finally {
            em.close();
        }
    }
}
