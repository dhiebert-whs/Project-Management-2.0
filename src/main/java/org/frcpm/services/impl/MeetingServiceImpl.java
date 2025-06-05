// src/main/java/org/frcpm/services/impl/MeetingServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.repositories.spring.MeetingRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.services.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spring Boot implementation of MeetingService.
 * Uses AbstractSpringService base class for consistent CRUD operations.
 */
@Service("meetingServiceImpl")
@Transactional
public class MeetingServiceImpl extends AbstractSpringService<Meeting, Long, MeetingRepository> 
        implements MeetingService {
    
    private static final Logger LOGGER = Logger.getLogger(MeetingServiceImpl.class.getName());
    
    // Additional dependencies injected via constructor
    private final ProjectRepository projectRepository;
    
    public MeetingServiceImpl(
            MeetingRepository meetingRepository,
            ProjectRepository projectRepository) {
        super(meetingRepository);
        this.projectRepository = projectRepository;
    }

    @Override
    protected String getEntityName() {
        return "meeting";
    }
    
    // Meeting-specific business methods
    
    @Override
    public List<Meeting> findByProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        return repository.findByProject(project);
    }
    
    @Override
    public List<Meeting> findByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return repository.findByDate(date);
    }
    
    @Override
    public List<Meeting> findByDateAfter(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return repository.findByDateAfter(date);
    }
    
    @Override
    public List<Meeting> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
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
        
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            LOGGER.log(Level.WARNING, "Project not found with ID: {0}", projectId);
            throw new IllegalArgumentException("Project not found with ID: " + projectId);
        }
        
        Meeting meeting = new Meeting(date, startTime, endTime, project);
        meeting.setNotes(notes);
        
        return save(meeting);
    }
    
    @Override
    public Meeting updateMeetingDateTime(Long meetingId, LocalDate date, 
                                        LocalTime startTime, LocalTime endTime) {
        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting ID cannot be null");
        }
        
        Meeting meeting = findById(meetingId);
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
            LocalTime effectiveStartTime = startTime != null ? startTime : meeting.getStartTime();
            if (endTime.isBefore(effectiveStartTime)) {
                throw new IllegalArgumentException("Meeting end time cannot be before start time");
            }
            meeting.setEndTime(endTime);
        }
        
        return save(meeting);
    }
    
    @Override
    public Meeting updateNotes(Long meetingId, String notes) {
        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting ID cannot be null");
        }
        
        Meeting meeting = findById(meetingId);
        if (meeting == null) {
            LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
            return null;
        }
        
        meeting.setNotes(notes);
        return save(meeting);
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

    // Spring Boot Async Methods
    
    @Async
    public CompletableFuture<List<Meeting>> findAllAsync() {
        try {
            List<Meeting> result = findAll();
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Meeting> findByIdAsync(Long id) {
        try {
            Meeting result = findById(id);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Meeting> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Meeting> saveAsync(Meeting entity) {
        try {
            Meeting result = save(entity);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Meeting> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Boolean> deleteByIdAsync(Long id) {
        try {
            boolean result = deleteById(id);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<List<Meeting>> findByProjectAsync(Project project) {
        try {
            List<Meeting> result = findByProject(project);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<List<Meeting>> findByDateAsync(LocalDate date) {
        try {
            List<Meeting> result = findByDate(date);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Meeting> createMeetingAsync(LocalDate date, LocalTime startTime, LocalTime endTime, 
                                                        Long projectId, String notes) {
        try {
            Meeting result = createMeeting(date, startTime, endTime, projectId, notes);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Meeting> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<List<Meeting>> getUpcomingMeetingsAsync(Long projectId, int days) {
        try {
            List<Meeting> result = getUpcomingMeetings(projectId, days);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<Meeting>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    // Interface async method implementations with callbacks
    
    @Override
    public CompletableFuture<List<Meeting>> findAllAsync(Consumer<List<Meeting>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Meeting>> future = findAllAsync();
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<Meeting> saveAsync(Meeting entity, Consumer<Meeting> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = saveAsync(entity);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(Long id, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Boolean> future = deleteByIdAsync(id);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<List<Meeting>> findByProjectAsync(Project project, Consumer<List<Meeting>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Meeting>> future = findByProjectAsync(project);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<List<Meeting>> getUpcomingMeetingsAsync(Long projectId, int days, Consumer<List<Meeting>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Meeting>> future = getUpcomingMeetingsAsync(projectId, days);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<Meeting> createMeetingAsync(LocalDate date, LocalTime startTime, LocalTime endTime, 
                                                        Long projectId, String notes, Consumer<Meeting> onSuccess, 
                                                        Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = createMeetingAsync(date, startTime, endTime, projectId, notes);
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<Meeting> updateMeetingDateTimeAsync(Long meetingId, LocalDate date, LocalTime startTime, 
                                                               LocalTime endTime, Consumer<Meeting> onSuccess, 
                                                               Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = CompletableFuture.supplyAsync(() -> updateMeetingDateTime(meetingId, date, startTime, endTime));
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
    
    @Override
    public CompletableFuture<Meeting> updateNotesAsync(Long meetingId, String notes, Consumer<Meeting> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Meeting> future = CompletableFuture.supplyAsync(() -> updateNotes(meetingId, notes));
        if (onSuccess != null) {
            future.thenAccept(onSuccess);
        }
        if (onFailure != null) {
            future.exceptionally(throwable -> {
                onFailure.accept(throwable);
                return null;
            });
        }
        return future;
    }
}