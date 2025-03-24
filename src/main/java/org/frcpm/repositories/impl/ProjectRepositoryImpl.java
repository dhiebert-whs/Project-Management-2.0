package org.frcpm.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.ProjectRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ProjectRepository using JPA.
 */
public class ProjectRepositoryImpl extends JpaRepositoryImpl<Project, Long> implements ProjectRepository {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectRepositoryImpl.class.getName());
    
    @Override
    public List<Project> findByName(String name) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Project> query = em.createQuery(
                    "SELECT p FROM Project p WHERE p.name LIKE :name", Project.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding projects by name", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Project> findByDeadlineBefore(LocalDate date) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Project> query = em.createQuery(
                    "SELECT p FROM Project p WHERE p.hardDeadline < :date", Project.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding projects by deadline", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Project> findByStartDateAfter(LocalDate date) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Project> query = em.createQuery(
                    "SELECT p FROM Project p WHERE p.startDate > :date", Project.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding projects by start date", e);
            return List.of();
        } finally {
            em.close();
        }
    }
}