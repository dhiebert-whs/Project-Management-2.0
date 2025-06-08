package org.frcpm.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.TeamMemberRepository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of TeamMemberRepository using JPA.
 */
public class TeamMemberRepositoryImpl extends JpaRepositoryImpl<TeamMember, Long> implements TeamMemberRepository {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberRepositoryImpl.class.getName());
    
    @Override
    public Optional<TeamMember> findByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<TeamMember> query = em.createQuery(
                    "SELECT tm FROM TeamMember tm WHERE tm.username = :username", TeamMember.class);
            query.setParameter("username", username);
            return query.getResultStream().findFirst();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding team member by username", e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<TeamMember> findBySubteam(Subteam subteam) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<TeamMember> query = em.createQuery(
                    "SELECT tm FROM TeamMember tm WHERE tm.subteam = :subteam", TeamMember.class);
            query.setParameter("subteam", subteam);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding team members by subteam", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<TeamMember> findBySkill(String skill) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<TeamMember> query = em.createQuery(
                    "SELECT tm FROM TeamMember tm WHERE tm.skills LIKE :skill", TeamMember.class);
            query.setParameter("skill", "%" + skill + "%");
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding team members by skill", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<TeamMember> findLeaders() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<TeamMember> query = em.createQuery(
                    "SELECT tm FROM TeamMember tm WHERE tm.leader = true", TeamMember.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding team leaders", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<TeamMember> findByName(String name) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<TeamMember> query = em.createQuery(
                    "SELECT tm FROM TeamMember tm WHERE tm.firstName LIKE :name OR tm.lastName LIKE :name", 
                    TeamMember.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding team members by name", e);
            return List.of();
        } finally {
            em.close();
        }
    }
}
