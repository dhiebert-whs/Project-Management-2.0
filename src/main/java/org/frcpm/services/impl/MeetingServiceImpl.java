// src/main/java/org/frcpm/services/impl/MeetingServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.repositories.spring.MeetingRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.services.MeetingService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Spring Boot implementation of MeetingService using composition pattern.
 * Eliminates AbstractSpringService inheritance to resolve compilation errors.
 */
@Service
@Transactional
public class MeetingServiceImpl implements MeetingService {
    
    private final MeetingRepository meetingRepository;
    private final ProjectRepository projectRepository;
    
    /**
     * Constructor injection for repositories.
     * No @Autowired needed with single constructor.
     */
    public MeetingServiceImpl(MeetingRepository meetingRepository,
                             ProjectRepository projectRepository) {
        this.meetingRepository = meetingRepository;
        this.projectRepository = projectRepository;
    }
    
    // =========================================================================
    // BASIC CRUD OPERATIONS - Implementing Service<Meeting, Long> interface
    // =========================================================================
    
    @Override
    public Meeting findById(Long id) {
        if (id == null) {
            return null;
        }
        return meetingRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Meeting> findAll() {
        return meetingRepository.findAll();
    }
    
    @Override
    public Meeting save(Meeting entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Meeting cannot be null");
        }
        return meetingRepository.save(entity);
    }
    
    @Override
    public void delete(Meeting entity) {
        if (entity != null) {
            meetingRepository.delete(entity);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (id != null && meetingRepository.existsById(id)) {
            meetingRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Override
    public long count() {
        return meetingRepository.count();
    }
    
    // =========================================================================
    // BUSINESS LOGIC METHODS - MeetingService specific methods
    // =========================================================================
    
    @Override
    public List<Meeting> findByProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        return meetingRepository.findByProject(project);
    }
    
    @Override
    public List<Meeting> findByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return meetingRepository.findByDate(date);
    }
    
    @Override
    public List<Meeting> findByDateAfter(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return meetingRepository.findByDateAfter(date);
    }
    
    @Override
    public List<Meeting> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        return meetingRepository.findByDateBetween(startDate, endDate);
    }
    
    @Override
    public Meeting createMeeting(LocalDate date, LocalTime startTime, LocalTime endTime, 
                                Long projectId, String notes) {
        if (date == null) {
            throw new IllegalArgumentException("Meeting date cannot be null");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        
        // Find the project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
        
        // Create the meeting
        Meeting meeting = new Meeting();
        meeting.setDate(date);
        meeting.setStartTime(startTime);
        meeting.setEndTime(endTime);
        meeting.setProject(project);
        meeting.setNotes(notes);
        
        return meetingRepository.save(meeting);
    }
    
    @Override
    public Meeting updateMeetingDateTime(Long meetingId, LocalDate date, 
                                        LocalTime startTime, LocalTime endTime) {
        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting ID cannot be null");
        }
        
        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) {
            return null;
        }
        
        // Update fields if provided
        if (date != null) {
            meeting.setDate(date);
        }
        if (startTime != null) {
            meeting.setStartTime(startTime);
        }
        if (endTime != null) {
            meeting.setEndTime(endTime);
        }
        
        // Validate times if both are present
        if (meeting.getStartTime() != null && meeting.getEndTime() != null 
            && meeting.getStartTime().isAfter(meeting.getEndTime())) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        
        return meetingRepository.save(meeting);
    }
    
    @Override
    public Meeting updateNotes(Long meetingId, String notes) {
        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting ID cannot be null");
        }
        
        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) {
            return null;
        }
        
        meeting.setNotes(notes);
        return meetingRepository.save(meeting);
    }
    
    @Override
    public List<Meeting> getUpcomingMeetings(Long projectId, int days) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (days < 0) {
            throw new IllegalArgumentException("Days cannot be negative");
        }
        
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            throw new IllegalArgumentException("Project not found with ID: " + projectId);
        }
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        
        // Use the findByProjectAndDateBetween method that takes start and end dates
        return meetingRepository.findByProjectAndDateBetween(project, today, endDate);
    }
    
    // =========================================================================
    // ASYNC METHODS - Using @Async annotation with CompletableFuture
    // =========================================================================
    
    @Async
    @Override
    public CompletableFuture<List<Meeting>> findAllAsync(
            Consumer<List<Meeting>> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Meeting> result = findAll();
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Meeting> saveAsync(
            Meeting entity,
            Consumer<Meeting> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Meeting result = save(entity);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(
            Long id,
            Consumer<Boolean> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Boolean result = deleteById(id);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<List<Meeting>> findByProjectAsync(
            Project project,
            Consumer<List<Meeting>> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Meeting> result = findByProject(project);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<List<Meeting>> getUpcomingMeetingsAsync(
            Long projectId, 
            int days,
            Consumer<List<Meeting>> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Meeting> result = getUpcomingMeetings(projectId, days);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Meeting> createMeetingAsync(
            LocalDate date, 
            LocalTime startTime, 
            LocalTime endTime, 
            Long projectId, 
            String notes,
            Consumer<Meeting> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Meeting result = createMeeting(date, startTime, endTime, projectId, notes);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Meeting> updateMeetingDateTimeAsync(
            Long meetingId, 
            LocalDate date, 
            LocalTime startTime, 
            LocalTime endTime,
            Consumer<Meeting> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Meeting result = updateMeetingDateTime(meetingId, date, startTime, endTime);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
    
    @Async
    @Override
    public CompletableFuture<Meeting> updateNotesAsync(
            Long meetingId, 
            String notes,
            Consumer<Meeting> onSuccess, 
            Consumer<Throwable> onFailure) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Meeting result = updateNotes(meetingId, notes);
                if (onSuccess != null) onSuccess.accept(result);
                return result;
            } catch (Exception e) {
                if (onFailure != null) onFailure.accept(e);
                throw new RuntimeException(e);
            }
        });
    }
}