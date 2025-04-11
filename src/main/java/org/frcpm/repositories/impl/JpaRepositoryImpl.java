package org.frcpm.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.frcpm.config.DatabaseConfig;
import org.frcpm.repositories.Repository;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base implementation of the Repository interface using JPA.
 * 
 * @param <T> the entity type
 * @param <ID> the entity's ID type
 */
public abstract class JpaRepositoryImpl<T, ID> implements Repository<T, ID> {
    
    private static final Logger LOGGER = Logger.getLogger(JpaRepositoryImpl.class.getName());
    
    protected final Class<T> entityClass;
    
    @SuppressWarnings("unchecked")
    public JpaRepositoryImpl() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }
    
    protected EntityManager getEntityManager() {
        return DatabaseConfig.getEntityManager();
    }
    
    @Override
    public Optional<T> findById(ID id) {
        EntityManager em = getEntityManager();
        try {
            T entity = em.find(entityClass, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding entity by ID", e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    @Override
    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);
            TypedQuery<T> query = em.createQuery(cq);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding all entities", e);
            return List.of();
        } finally {
            em.close();
        }
    }
    
    @Override
    public T save(T entity) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            
            T managedEntity;
            if (isNew(entity)) {
                em.persist(entity);
                managedEntity = entity;
            } else {
                managedEntity = em.merge(entity);
            }
            
            em.flush(); // Force SQL execution to detect any errors
            em.getTransaction().commit();
            return managedEntity;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error saving entity", e);
            throw new RuntimeException("Failed to save entity", e);
        } finally {
            em.close();
        }
    }
    
    /**
     * Determines if an entity is new (not yet persisted).
     * 
     * @param entity the entity to check
     * @return true if the entity is new, false otherwise
     */
    protected boolean isNew(T entity) {
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                    field.setAccessible(true);
                    Object id = field.get(entity);
                    return id == null || (id instanceof Number && ((Number) id).longValue() == 0);
                }
            }
            
            // Check superclass if no ID field found in declared fields
            Class<?> superClass = entity.getClass().getSuperclass();
            while (superClass != null && !superClass.equals(Object.class)) {
                for (Field field : superClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                        field.setAccessible(true);
                        Object id = field.get(entity);
                        return id == null || (id instanceof Number && ((Number) id).longValue() == 0);
                    }
                }
                superClass = superClass.getSuperclass();
            }
            
            // If we can't determine, assume it's new
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not determine if entity is new", e);
            return true;
        }
    }
    
    // Helper method to find the ID field
    private Field getIdField(Class<?> clazz) {
        // Check current class
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                return field;
            }
        }
        
        // Check superclass if exists
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            return getIdField(clazz.getSuperclass());
        }
        
        throw new IllegalArgumentException("No ID field found in class " + clazz.getName());
    }
    
    @Override
    public void delete(T entity) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            if (!em.contains(entity)) {
                entity = em.merge(entity);
            }
            em.remove(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting entity", e);
            throw new RuntimeException("Failed to delete entity", e);
        } finally {
            em.close();
        }
    }
    
    @Override
    public boolean deleteById(ID id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
                em.getTransaction().commit();
                return true;
            } else {
                em.getTransaction().rollback();
                return false;
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting entity by ID", e);
            throw new RuntimeException("Failed to delete entity by ID", e);
        } finally {
            em.close();
        }
    }
    
    @Override
    public long count() {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<T> root = cq.from(entityClass);
            cq.select(cb.count(root));
            return em.createQuery(cq).getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting entities", e);
            return 0;
        } finally {
            em.close();
        }
    }
}