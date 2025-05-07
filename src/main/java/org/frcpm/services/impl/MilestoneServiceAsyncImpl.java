// src/main/java/org/frcpm/services/impl/MilestoneServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.services.MilestoneService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of MilestoneService using the task-based threading model.
 */
public class MilestoneServiceAsyncImpl extends AbstractAsyncService<Milestone, Long, MilestoneRepository>
        implements MilestoneService {

    private static final Logger LOGGER = Logger.getLogger(MilestoneServiceAsyncImpl.class.getName());

    public MilestoneServiceAsyncImpl() {
        super(RepositoryFactory.getMilestoneRepository());
    }

    // Synchronous interface methods
    
    @Override
    public List<Milestone> findByProject(Project project) {
        return repository.findByProject(project);
    }
    
    @Override
    public List<Milestone> findByDateBefore(LocalDate date) {
        return repository.findByDateBefore(date);
    }
    
    @Override
    public List<Milestone> findByDateAfter(LocalDate date) {
        return repository.findByDateAfter(date);
    }
    
    @Override
    public List<Milestone> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        return repository.findByDateBetween(startDate, endDate);
    }
    
    @Override
    public Milestone createMilestone(String name, LocalDate date, Long projectId, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Milestone name cannot be empty");
        }

        if (date == null) {
            throw new IllegalArgumentException("Milestone date cannot be null");
        }

        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }

        return executeSync(em -> {
            Project project = em.find(Project.class, projectId);
            if (project == null) {
                LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
                throw new IllegalArgumentException("Project not found with ID: " + projectId);
            }
            
            Milestone milestone = new Milestone(name, date, project);
            
            if (description != null && !description.trim().isEmpty()) {
                milestone.setDescription(description);
            }
            
            em.persist(milestone);
            return milestone;
        });
    }
    
    @Override
    public Milestone updateMilestoneDate(Long milestoneId, LocalDate date) {
        if (milestoneId == null) {
            throw new IllegalArgumentException("Milestone ID cannot be null");
        }

        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        return executeSync(em -> {
            Milestone milestone = em.find(Milestone.class, milestoneId);
            if (milestone == null) {
                LOGGER.log(Level.WARNING, "Milestone not found with ID: {0}", milestoneId);
                return null;
            }
            
            milestone.setDate(date);
            em.merge(milestone);
            return milestone;
        });
    }
    
    @Override
    public Milestone updateDescription(Long milestoneId, String description) {
        if (milestoneId == null) {
            throw new IllegalArgumentException("Milestone ID cannot be null");
        }

        return executeSync(em -> {
            Milestone milestone = em.find(Milestone.class, milestoneId);
            if (milestone == null) {
                LOGGER.log(Level.WARNING, "Milestone not found with ID: {0}", milestoneId);
                return null;
            }
            
            milestone.setDescription(description);
            em.merge(milestone);
            return milestone;
        });
    }
    
    @Override
    public List<Milestone> getUpcomingMilestones(Long projectId, int days) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }

        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }

        return executeSync(em -> {
            Project project = em.find(Project.class, projectId);
            if (project == null) {
                LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
                return new ArrayList<>();
            }

            LocalDate today = LocalDate.now();
            LocalDate dueBefore = today.plusDays(days);

            TypedQuery<Milestone> query = em.createQuery(
                    "SELECT m FROM Milestone m " +
                    "WHERE m.project.id = :projectId " +
                    "AND m.date >= :today " +
                    "AND m.date <= :dueBefore", Milestone.class);
            
            query.setParameter("projectId", projectId);
            query.setParameter("today", today);
            query.setParameter("dueBefore", dueBefore);
            
            return query.getResultList();
        });
    }

    // Async methods
    
    /**
     * Finds milestones by project asynchronously.
     * 
     * @param project the project to find milestones for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of milestones
     */
    public CompletableFuture<List<Milestone>> findByProjectAsync(Project project,
                                                               Consumer<List<Milestone>> onSuccess,
                                                               Consumer<Throwable> onFailure) {
        if (project == null) {
            CompletableFuture<List<Milestone>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Project cannot be null"));
            return future;
        }

        return executeAsync("Find Milestones By Project: " + project.getId(), em -> {
            TypedQuery<Milestone> query = em.createQuery(
                    "SELECT m FROM Milestone m WHERE m.project.id = :projectId", Milestone.class);
            query.setParameter("projectId", project.getId());
            return query.getResultList();
        }, onSuccess, onFailure);
    }
    
    /**
     * Finds milestones with a date before the specified date asynchronously.
     * 
     * @param date the date to compare against
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of milestones
     */
    public CompletableFuture<List<Milestone>> findByDateBeforeAsync(LocalDate date,
                                                                  Consumer<List<Milestone>> onSuccess,
                                                                  Consumer<Throwable> onFailure) {
        if (date == null) {
            CompletableFuture<List<Milestone>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Date cannot be null"));
            return future;
        }

        return executeAsync("Find Milestones Before Date: " + date, em -> {
            TypedQuery<Milestone> query = em.createQuery(
                    "SELECT m FROM Milestone m WHERE m.date < :date", Milestone.class);
            query.setParameter("date", date);
            return query.getResultList();
        }, onSuccess, onFailure);
    }
    
    /**
     * Finds milestones with a date after the specified date asynchronously.
     * 
     * @param date the date to compare against
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of milestones
     */
    public CompletableFuture<List<Milestone>> findByDateAfterAsync(LocalDate date,
                                                                 Consumer<List<Milestone>> onSuccess,
                                                                 Consumer<Throwable> onFailure) {
        if (date == null) {
            CompletableFuture<List<Milestone>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Date cannot be null"));
            return future;
        }

        return executeAsync("Find Milestones After Date: " + date, em -> {
            TypedQuery<Milestone> query = em.createQuery(
                    "SELECT m FROM Milestone m WHERE m.date > :date", Milestone.class);
            query.setParameter("date", date);
            return query.getResultList();
        }, onSuccess, onFailure);
    }
    
    /**
     * Finds milestones in a date range asynchronously.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of milestones
     */
    public CompletableFuture<List<Milestone>> findByDateBetweenAsync(LocalDate startDate, LocalDate endDate,
                                                                   Consumer<List<Milestone>> onSuccess,
                                                                   Consumer<Throwable> onFailure) {
        if (startDate == null || endDate == null) {
            CompletableFuture<List<Milestone>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Start date and end date cannot be null"));
            return future;
        }

        return executeAsync("Find Milestones Between Dates: " + startDate + " and " + endDate, em -> {
            TypedQuery<Milestone> query = em.createQuery(
                    "SELECT m FROM Milestone m WHERE m.date >= :startDate AND m.date <= :endDate", 
                    Milestone.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        }, onSuccess, onFailure);
    }
    
    /**
     * Creates a new milestone asynchronously.
     * 
     * @param name the milestone name
     * @param date the milestone date
     * @param projectId the ID of the project the milestone is for
     * @param description the milestone description (optional)
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created milestone
     */
    public CompletableFuture<Milestone> createMilestoneAsync(String name, LocalDate date, Long projectId, 
                                                          String description, Consumer<Milestone> onSuccess, 
                                                          Consumer<Throwable> onFailure) {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Milestone name cannot be empty");
            }

            if (date == null) {
                throw new IllegalArgumentException("Milestone date cannot be null");
            }

            if (projectId == null) {
                throw new IllegalArgumentException("Project ID cannot be null");
            }

            return executeAsync("Create Milestone: " + name, em -> {
                Project project = em.find(Project.class, projectId);
                if (project == null) {
                    LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
                    throw new IllegalArgumentException("Project not found with ID: " + projectId);
                }
                
                Milestone milestone = new Milestone(name, date, project);
                
                if (description != null && !description.trim().isEmpty()) {
                    milestone.setDescription(description);
                }
                
                em.persist(milestone);
                return milestone;
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Milestone> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Updates a milestone's date asynchronously.
     * 
     * @param milestoneId the milestone ID
     * @param date the new date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated milestone
     */
    public CompletableFuture<Milestone> updateMilestoneDateAsync(Long milestoneId, LocalDate date,
                                                              Consumer<Milestone> onSuccess,
                                                              Consumer<Throwable> onFailure) {
        try {
            if (milestoneId == null) {
                throw new IllegalArgumentException("Milestone ID cannot be null");
            }

            if (date == null) {
                throw new IllegalArgumentException("Date cannot be null");
            }

            return executeAsync("Update Milestone Date: " + milestoneId, em -> {
                Milestone milestone = em.find(Milestone.class, milestoneId);
                if (milestone == null) {
                    LOGGER.log(Level.WARNING, "Milestone not found with ID: {0}", milestoneId);
                    return null;
                }
                
                milestone.setDate(date);
                em.merge(milestone);
                return milestone;
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Milestone> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Updates a milestone's description asynchronously.
     * 
     * @param milestoneId the milestone ID
     * @param description the new description
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated milestone
     */
    public CompletableFuture<Milestone> updateDescriptionAsync(Long milestoneId, String description,
                                                            Consumer<Milestone> onSuccess,
                                                            Consumer<Throwable> onFailure) {
        try {
            if (milestoneId == null) {
                throw new IllegalArgumentException("Milestone ID cannot be null");
            }

            return executeAsync("Update Milestone Description: " + milestoneId, em -> {
                Milestone milestone = em.find(Milestone.class, milestoneId);
                if (milestone == null) {
                    LOGGER.log(Level.WARNING, "Milestone not found with ID: {0}", milestoneId);
                    return null;
                }
                
                milestone.setDescription(description);
                em.merge(milestone);
                return milestone;
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Milestone> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Gets upcoming milestones for a project asynchronously.
     * 
     * @param projectId the project ID
     * @param days the number of days to look ahead
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of milestones
     */
    public CompletableFuture<List<Milestone>> getUpcomingMilestonesAsync(Long projectId, int days,
                                                                       Consumer<List<Milestone>> onSuccess,
                                                                       Consumer<Throwable> onFailure) {
        try {
            if (projectId == null) {
                throw new IllegalArgumentException("Project ID cannot be null");
            }

            if (days <= 0) {
                throw new IllegalArgumentException("Days must be positive");
            }

            return executeAsync("Get Upcoming Milestones: " + projectId, em -> {
                Project project = em.find(Project.class, projectId);
                if (project == null) {
                    LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
                    return new ArrayList<>();
                }

                LocalDate today = LocalDate.now();
                LocalDate dueBefore = today.plusDays(days);

                TypedQuery<Milestone> query = em.createQuery(
                        "SELECT m FROM Milestone m " +
                        "WHERE m.project.id = :projectId " +
                        "AND m.date >= :today " +
                        "AND m.date <= :dueBefore", Milestone.class);
                
                query.setParameter("projectId", projectId);
                query.setParameter("today", today);
                query.setParameter("dueBefore", dueBefore);
                
                return query.getResultList();
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<List<Milestone>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}