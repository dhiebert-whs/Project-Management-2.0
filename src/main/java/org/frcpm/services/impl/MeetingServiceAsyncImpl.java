// src/main/java/org/frcpm/services/impl/MeetingServiceAsyncImpl.java

package org.frcpm.services.impl;

import javafx.application.Platform;
import org.frcpm.async.TaskExecutor;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.services.MeetingService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Asynchronous implementation of MeetingService using the task-based threading model.
 */
public class MeetingServiceAsyncImpl extends AbstractAsyncService<Meeting, Long, MeetingRepository>
        implements MeetingService {

    private static final Logger LOGGER = Logger.getLogger(MeetingServiceAsyncImpl.class.getName());
    private final ProjectRepository projectRepository;

    public MeetingServiceAsyncImpl() {
        super(RepositoryFactory.getMeetingRepository());
        this.projectRepository = RepositoryFactory.getProjectRepository();
    }

    // Synchronous interface methods (implementing MeetingService interface)

    @Override
    public List<Meeting> findByProject(Project project) {
        return repository.findByProject(project);
    }

    @Override
    public List<Meeting> findByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    @Override
    public List<Meeting> findByDateAfter(LocalDate date) {
        return repository.findByDateAfter(date);
    }

    @Override
    public List<Meeting> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        return repository.findByDateBetween(startDate, endDate);
    }

    @Override
    public Meeting createMeeting(LocalDate date, LocalTime startTime, LocalTime endTime,
                              Long projectId, String notes) {
        if (date == null) {
            throw new IllegalArgumentException("Meeting date cannot be null");
        }
        
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Meeting start and end times cannot be null");
        }
        
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Meeting end time cannot be before start time");
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
            
            Meeting meeting = new Meeting(date, startTime, endTime, project);
            meeting.setNotes(notes);
            
            em.persist(meeting);
            return meeting;
        });
    }

    @Override
    public Meeting updateMeetingDateTime(Long meetingId, LocalDate date,
                                      LocalTime startTime, LocalTime endTime) {
        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting ID cannot be null");
        }
        
        return executeSync(em -> {
            Meeting meeting = em.find(Meeting.class, meetingId);
            if (meeting == null) {
                LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
                return null;
            }
            
            if (date != null) {
                meeting.setDate(date);
            }
            
            if (startTime != null) {
                meeting.setStartTime(startTime);
            }
            
            if (endTime != null) {
                if (endTime.isBefore(meeting.getStartTime())) {
                    throw new IllegalArgumentException("Meeting end time cannot be before start time");
                }
                meeting.setEndTime(endTime);
            }
            
            em.merge(meeting);
            return meeting;
        });
    }

    @Override
    public Meeting updateNotes(Long meetingId, String notes) {
        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting ID cannot be null");
        }
        
        return executeSync(em -> {
            Meeting meeting = em.find(Meeting.class, meetingId);
            if (meeting == null) {
                LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
                return null;
            }
            
            meeting.setNotes(notes);
            
            em.merge(meeting);
            return meeting;
        });
    }

    @Override
    public List<Meeting> getUpcomingMeetings(Long projectId, int days) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
        
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
            return new ArrayList<>();
        }
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        return repository.findByDateBetween(today, endDate);
    }

    // Asynchronous methods

    /**
     * Asynchronously finds meetings by project.
     * 
     * @param project the project to find meetings for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of meetings
     */
    public CompletableFuture<List<Meeting>> findByProjectAsync(Project project,
                                                        Consumer<List<Meeting>> onSuccess,
                                                        Consumer<Throwable> onFailure) {
        if (project == null) {
            CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Project cannot be null"));
            return future;
        }

        return executeAsync("Find Meetings By Project: " + project.getId(), em -> {
            TypedQuery<Meeting> query = em.createQuery(
                    "SELECT m FROM Meeting m WHERE m.project.id = :projectId", Meeting.class);
            query.setParameter("projectId", project.getId());
            return query.getResultList();
        }, onSuccess, onFailure);
    }

    /**
     * Asynchronously finds meetings by date.
     * 
     * @param date the date to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of meetings
     */
    public CompletableFuture<List<Meeting>> findByDateAsync(LocalDate date,
                                                     Consumer<List<Meeting>> onSuccess,
                                                     Consumer<Throwable> onFailure) {
        if (date == null) {
            CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Date cannot be null"));
            return future;
        }

        return executeAsync("Find Meetings By Date: " + date, em -> {
            TypedQuery<Meeting> query = em.createQuery(
                    "SELECT m FROM Meeting m WHERE m.date = :date", Meeting.class);
            query.setParameter("date", date);
            return query.getResultList();
        }, onSuccess, onFailure);
    }

    /**
     * Asynchronously finds meetings after a specific date.
     * 
     * @param date the date to compare against
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of meetings
     */
    public CompletableFuture<List<Meeting>> findByDateAfterAsync(LocalDate date,
                                                          Consumer<List<Meeting>> onSuccess,
                                                          Consumer<Throwable> onFailure) {
        if (date == null) {
            CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Date cannot be null"));
            return future;
        }

        return executeAsync("Find Meetings By Date After: " + date, em -> {
            TypedQuery<Meeting> query = em.createQuery(
                    "SELECT m FROM Meeting m WHERE m.date >= :date", Meeting.class);
            query.setParameter("date", date);
            return query.getResultList();
        }, onSuccess, onFailure);
    }

    /**
     * Asynchronously finds meetings in a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of meetings
     */
    public CompletableFuture<List<Meeting>> findByDateBetweenAsync(LocalDate startDate, LocalDate endDate,
                                                            Consumer<List<Meeting>> onSuccess,
                                                            Consumer<Throwable> onFailure) {
        if (startDate == null || endDate == null) {
            CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Start date and end date cannot be null"));
            return future;
        }

        return executeAsync("Find Meetings By Date Between: " + startDate + " and " + endDate, em -> {
            TypedQuery<Meeting> query = em.createQuery(
                    "SELECT m FROM Meeting m WHERE m.date BETWEEN :startDate AND :endDate", Meeting.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        }, onSuccess, onFailure);
    }

    /**
     * Asynchronously creates a new meeting.
     * 
     * @param date the meeting date
     * @param startTime the start time
     * @param endTime the end time
     * @param projectId the ID of the project the meeting is for
     * @param notes any meeting notes (optional)
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created meeting
     */
    public CompletableFuture<Meeting> createMeetingAsync(LocalDate date, LocalTime startTime, LocalTime endTime,
                                                 Long projectId, String notes,
                                                 Consumer<Meeting> onSuccess,
                                                 Consumer<Throwable> onFailure) {
        try {
            if (date == null) {
                throw new IllegalArgumentException("Meeting date cannot be null");
            }
            
            if (startTime == null || endTime == null) {
                throw new IllegalArgumentException("Meeting start and end times cannot be null");
            }
            
            if (endTime.isBefore(startTime)) {
                throw new IllegalArgumentException("Meeting end time cannot be before start time");
            }
            
            if (projectId == null) {
                throw new IllegalArgumentException("Project ID cannot be null");
            }

            return executeAsync("Create Meeting: " + date, em -> {
                Project project = em.find(Project.class, projectId);
                if (project == null) {
                    LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
                    throw new IllegalArgumentException("Project not found with ID: " + projectId);
                }
                
                Meeting meeting = new Meeting(date, startTime, endTime, project);
                meeting.setNotes(notes);
                
                em.persist(meeting);
                return meeting;
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Meeting> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Asynchronously updates a meeting's date and time.
     * 
     * @param meetingId the meeting ID
     * @param date the new date (optional)
     * @param startTime the new start time (optional)
     * @param endTime the new end time (optional)
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated meeting
     */
    public CompletableFuture<Meeting> updateMeetingDateTimeAsync(Long meetingId, LocalDate date,
                                                        LocalTime startTime, LocalTime endTime,
                                                        Consumer<Meeting> onSuccess,
                                                        Consumer<Throwable> onFailure) {
        try {
            if (meetingId == null) {
                throw new IllegalArgumentException("Meeting ID cannot be null");
            }

            return executeAsync("Update Meeting Date/Time: " + meetingId, em -> {
                Meeting meeting = em.find(Meeting.class, meetingId);
                if (meeting == null) {
                    LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
                    return null;
                }
                
                if (date != null) {
                    meeting.setDate(date);
                }
                
                if (startTime != null) {
                    meeting.setStartTime(startTime);
                }
                
                if (endTime != null) {
                    if (endTime.isBefore(meeting.getStartTime())) {
                        throw new IllegalArgumentException("Meeting end time cannot be before start time");
                    }
                    meeting.setEndTime(endTime);
                }
                
                em.merge(meeting);
                return meeting;
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Meeting> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Asynchronously updates meeting notes.
     * 
     * @param meetingId the meeting ID
     * @param notes the new notes
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated meeting
     */
    public CompletableFuture<Meeting> updateNotesAsync(Long meetingId, String notes,
                                                Consumer<Meeting> onSuccess,
                                                Consumer<Throwable> onFailure) {
        try {
            if (meetingId == null) {
                throw new IllegalArgumentException("Meeting ID cannot be null");
            }

            return executeAsync("Update Meeting Notes: " + meetingId, em -> {
                Meeting meeting = em.find(Meeting.class, meetingId);
                if (meeting == null) {
                    LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
                    return null;
                }
                
                meeting.setNotes(notes);
                
                em.merge(meeting);
                return meeting;
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Meeting> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Asynchronously gets upcoming meetings for a project.
     * 
     * @param projectId the project ID
     * @param days the number of days to look ahead
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of upcoming meetings
     */
    public CompletableFuture<List<Meeting>> getUpcomingMeetingsAsync(Long projectId, int days,
                                                              Consumer<List<Meeting>> onSuccess,
                                                              Consumer<Throwable> onFailure) {
        try {
            if (projectId == null) {
                throw new IllegalArgumentException("Project ID cannot be null");
            }
            
            if (days <= 0) {
                throw new IllegalArgumentException("Days must be positive");
            }

            return executeAsync("Get Upcoming Meetings: " + projectId, em -> {
                Project project = em.find(Project.class, projectId);
                if (project == null) {
                    LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
                    return new ArrayList<>();
                }
                
                LocalDate today = LocalDate.now();
                LocalDate endDate = today.plusDays(days);
                
                TypedQuery<Meeting> query = em.createQuery(
                        "SELECT m FROM Meeting m WHERE m.project.id = :projectId AND " +
                        "m.date BETWEEN :today AND :endDate ORDER BY m.date, m.startTime", Meeting.class);
                query.setParameter("projectId", projectId);
                query.setParameter("today", today);
                query.setParameter("endDate", endDate);
                
                return query.getResultList();
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}