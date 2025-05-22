// src/main/java/org/frcpm/services/impl/TestableMeetingServiceAsyncImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.services.MeetingService;
import org.frcpm.di.ServiceLocator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable async implementation of MeetingService that accepts injected repositories
 * for better unit testing. This implementation provides async methods that can be stubbed
 * in tests without requiring spy patterns.
 */
public class TestableMeetingServiceAsyncImpl extends TestableMeetingServiceImpl {
    
    private static final Logger LOGGER = Logger.getLogger(TestableMeetingServiceAsyncImpl.class.getName());
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableMeetingServiceAsyncImpl() {
        super();
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param meetingRepository the meeting repository
     * @param projectRepository the project repository
     */
    public TestableMeetingServiceAsyncImpl(
            MeetingRepository meetingRepository,
            ProjectRepository projectRepository) {
        super(meetingRepository, projectRepository);
    }
    
    // ASYNC METHODS - These can be stubbed directly in tests without spies
    
    /**
     * Finds all meetings asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of meetings
     */
    public CompletableFuture<List<Meeting>> findAllAsync(Consumer<List<Meeting>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
        
        try {
            List<Meeting> result = findAll();
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Updates meeting date and time asynchronously.
     * 
     * @param meetingId the meeting ID
     * @param date the new date
     * @param startTime the new start time
     * @param endTime the new end time
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated meeting
     */
    public CompletableFuture<Meeting> updateMeetingDateTimeAsync(Long meetingId, LocalDate date, LocalTime startTime, LocalTime endTime, Consumer<Meeting> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = new CompletableFuture<>();
        
        try {
            Meeting result = updateMeetingDateTime(meetingId, date, startTime, endTime);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Updates meeting notes asynchronously.
     * 
     * @param meetingId the meeting ID
     * @param notes the new notes
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated meeting
     */
    public CompletableFuture<Meeting> updateNotesAsync(Long meetingId, String notes, Consumer<Meeting> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = new CompletableFuture<>();
        
        try {
            Meeting result = updateNotes(meetingId, notes);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Finds meetings by date asynchronously.
     * 
     * @param date the date to search for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of meetings
     */
    public CompletableFuture<List<Meeting>> findByDateAsync(LocalDate date, Consumer<List<Meeting>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
        
        try {
            List<Meeting> result = findByDate(date);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Finds meetings by date range asynchronously.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of meetings
     */
    public CompletableFuture<List<Meeting>> findByDateBetweenAsync(LocalDate startDate, LocalDate endDate, Consumer<List<Meeting>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
        
        try {
            List<Meeting> result = findByDateBetween(startDate, endDate);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Deletes a meeting asynchronously.
     * 
     * @param entity the meeting to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed when the meeting is deleted
     */
    public CompletableFuture<Void> deleteAsync(Meeting entity, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            delete(entity);
            if (onSuccess != null) {
                onSuccess.accept(null);
            }
            future.complete(null);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Counts all meetings asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the count of meetings
     */
    public CompletableFuture<Long> countAsync(Consumer<Long> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        
        try {
            long result = count();
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
//}
    
    /**
     * Finds meetings by project asynchronously.
     * 
     * @param project the project
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of meetings
     */
    public CompletableFuture<List<Meeting>> findByProjectAsync(Project project, Consumer<List<Meeting>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
        
        try {
            List<Meeting> result = findByProject(project);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Gets upcoming meetings asynchronously.
     * 
     * @param projectId the project ID
     * @param days the number of days ahead
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of upcoming meetings
     */
    public CompletableFuture<List<Meeting>> getUpcomingMeetingsAsync(Long projectId, int days, Consumer<List<Meeting>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
        
        try {
            List<Meeting> result = getUpcomingMeetings(projectId, days);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Finds a meeting by ID asynchronously.
     * 
     * @param id the meeting ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the meeting
     */
    public CompletableFuture<Meeting> findByIdAsync(Long id, Consumer<Meeting> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = new CompletableFuture<>();
        
        try {
            Meeting result = findById(id);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Saves a meeting asynchronously.
     * 
     * @param entity the meeting to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved meeting
     */
    public CompletableFuture<Meeting> saveAsync(Meeting entity, Consumer<Meeting> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = new CompletableFuture<>();
        
        try {
            Meeting result = save(entity);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Deletes a meeting by ID asynchronously.
     * 
     * @param id the ID of the meeting to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a boolean indicating success
     */
    public CompletableFuture<Boolean> deleteByIdAsync(Long id, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            boolean result = deleteById(id);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Creates a meeting asynchronously.
     * 
     * @param date the meeting date
     * @param startTime the start time
     * @param endTime the end time
     * @param projectId the project ID
     * @param notes the meeting notes
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created meeting
     */
    public CompletableFuture<Meeting> createMeetingAsync(LocalDate date, LocalTime startTime, LocalTime endTime, Long projectId, String notes, Consumer<Meeting> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = new CompletableFuture<>();
        
        try {
            Meeting result = createMeeting(date, startTime, endTime, projectId, notes);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
}