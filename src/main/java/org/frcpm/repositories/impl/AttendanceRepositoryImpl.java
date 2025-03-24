package org.frcpm.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.AttendanceRepository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of AttendanceRepository using JPA.
 */
public class AttendanceRepositoryImpl extends JpaRepositoryImpl<Attendance, Long> implements AttendanceRepository {
    
    private static final Logger LOGGER = Logger.getLogger(AttendanceRepositoryImpl.class.getName());
    
    @Override
    public List<Attendance> findByMeeting(Meeting meeting) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a WHERE a.meeting = :meeting", Attendance.class);
            query.setParameter("meeting", meeting);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by meeting", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Attendance> findByMember(TeamMember member) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a WHERE a.member = :member", Attendance.class);
            query.setParameter("member", member);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by member", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public Optional<Attendance> findByMeetingAndMember(Meeting meeting, TeamMember member) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a WHERE a.meeting = :meeting AND a.member = :member", 
                    Attendance.class);
            query.setParameter("meeting", meeting);
            query.setParameter("member", member);
            return query.getResultStream().findFirst();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by meeting and member", e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Attendance> findByPresent(boolean present) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a WHERE a.present = :present", Attendance.class);
            query.setParameter("present", present);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding attendance by presence status", e);
            return List.of();
        } finally {
            em.close();
        }
    }
}