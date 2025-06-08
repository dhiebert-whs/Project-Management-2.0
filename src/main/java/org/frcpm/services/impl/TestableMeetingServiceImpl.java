// src/main/java/org/frcpm/services/impl/TestableMeetingServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.services.MeetingService;
import org.frcpm.di.ServiceLocator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable implementation of MeetingService that accepts injected repositories
 * for better unit testing. This implementation eliminates static references to
 * RepositoryFactory and uses proper constructor injection for all dependencies.
 */
public class TestableMeetingServiceImpl implements MeetingService {
    
    private static final Logger LOGGER = Logger.getLogger(TestableMeetingServiceImpl.class.getName());
    
    // Dependencies injected via constructor
    private final MeetingRepository meetingRepository;
    private final ProjectRepository projectRepository;
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableMeetingServiceImpl() {
        this(
            ServiceLocator.getMeetingRepository(),
            ServiceLocator.getProjectRepository()
        );
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param meetingRepository the meeting repository
     * @param projectRepository the project repository
     */
    public TestableMeetingServiceImpl(
            MeetingRepository meetingRepository,
            ProjectRepository projectRepository) {
        this.meetingRepository = meetingRepository;
        this.projectRepository = projectRepository;
    }

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
        try {
            return meetingRepository.save(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving meeting", e);
            throw new RuntimeException("Failed to save meeting", e);
        }
    }
    
    @Override
    public void delete(Meeting entity) {
        try {
            meetingRepository.delete(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting meeting", e);
            throw new RuntimeException("Failed to delete meeting", e);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        try {
            return meetingRepository.deleteById(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting meeting by ID", e);
            throw new RuntimeException("Failed to delete meeting by ID", e);
        }
    }
    
    @Override
    public long count() {
        return meetingRepository.count();
    }
    
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
        
        return meetingRepository.findByDateBetween(today, endDate);
    }
}