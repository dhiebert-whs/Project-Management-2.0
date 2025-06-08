package org.frcpm.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.frcpm.models.Component;
import org.frcpm.repositories.specific.ComponentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ComponentRepository using JPA.
 */
public class ComponentRepositoryImpl extends JpaRepositoryImpl<Component, Long> implements ComponentRepository {
    
    private static final Logger LOGGER = Logger.getLogger(ComponentRepositoryImpl.class.getName());
    
    @Override
    public Optional<Component> findByPartNumber(String partNumber) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Component> query = em.createQuery(
                    "SELECT c FROM Component c WHERE c.partNumber = :partNumber", Component.class);
            query.setParameter("partNumber", partNumber);
            return query.getResultStream().findFirst();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding component by part number", e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Component> findByName(String name) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Component> query = em.createQuery(
                    "SELECT c FROM Component c WHERE c.name LIKE :name", Component.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding components by name", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Component> findByDelivered(boolean delivered) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Component> query = em.createQuery(
                    "SELECT c FROM Component c WHERE c.delivered = :delivered", Component.class);
            query.setParameter("delivered", delivered);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding components by delivery status", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<Component> findByExpectedDeliveryAfter(LocalDate date) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Component> query = em.createQuery(
                    "SELECT c FROM Component c WHERE c.expectedDelivery > :date", Component.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding components by expected delivery date", e);
            return List.of();
        } finally {
            em.close();
        }
    }
}
