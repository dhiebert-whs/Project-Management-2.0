package org.frcpm.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.TaskRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of TaskRepository using JPA.
 */
public class TaskRepositoryImpl extends JpaRepositoryImpl<Task, Long> implements TaskRepository {
    
    private static final Logger LOGGER = Logger.getLogger(TaskRepositoryImpl.class.getName());
    
    @Override
    public List<Task> findByProject(Project project) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t WHERE t.project = :project", Task.class);
            query.setParameter("project", project);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding tasks by project", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Task> findBySubsystem(Subsystem subsystem) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t WHERE t.subsystem = :subsystem", Task.class);
            query.setParameter("subsystem", subsystem);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding tasks by subsystem", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Task> findByAssignedMember(TeamMember member) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t JOIN t.assignedTo m WHERE m = :member", Task.class);
            query.setParameter("member", member);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding tasks by assigned member", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Task> findByCompleted(boolean completed) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t WHERE t.completed = :completed", Task.class);
            query.setParameter("completed", completed);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding tasks by completion status", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Task> findByEndDateBefore(LocalDate date) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t WHERE t.endDate < :date", Task.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding tasks by end date", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Task> findByPriority(Task.Priority priority) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t WHERE t.priority = :priority", Task.class);
            query.setParameter("priority", priority);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding tasks by priority", e);
            return List.of();
        } finally {
            em.close();
        }
    }
}