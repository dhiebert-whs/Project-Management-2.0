package org.frcpm.services.impl;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.services.MeetingService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of MeetingService using repository layer.
 */
public class MeetingServiceImpl extends AbstractService<Meeting, Long, MeetingRepository>
        implements MeetingService {
    
    private static final Logger LOGGER = Logger.getLogger(MeetingServiceImpl.class.getName());
    private final ProjectRepository projectRepository;
    
    public MeetingServiceImpl() {
        super(RepositoryFactory.getMeetingRepository());
        this.projectRepository = RepositoryFactory.getProjectRepository();
    }
    
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
            if (endTime.isBefore(meeting.getStartTime())) {
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
}