package org.frcpm.services.impl;

import org.frcpm.models.Meeting;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.services.MeetingService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the MeetingService interface.
 */
public class MeetingServiceImpl extends AbstractService<Meeting, Long, MeetingRepository>
        implements MeetingService {

    private static final Logger LOGGER = Logger.getLogger(MeetingServiceImpl.class.getName());
    
    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;

    /**
     * Constructor for MeetingServiceImpl.
     */
    public MeetingServiceImpl() {
        super(RepositoryFactory.getMeetingRepository());
        this.projectRepository = RepositoryFactory.getProjectRepository();
        this.milestoneRepository = RepositoryFactory.getMilestoneRepository();
    }

    @Override
    public List<Meeting> findByProject(Project project) {
        LOGGER.info("Finding meetings for project: " + project.getId());
        return repository.findByProject(project);
    }

    @Override
    public List<Meeting> findByDate(LocalDate date) {
        LOGGER.info("Finding meetings for date: " + date);
        return repository.findByDate(date);
    }

    @Override
    public List<Meeting> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        LOGGER.info("Finding meetings between dates: " + startDate + " and " + endDate);
        return repository.findByDateBetween(startDate, endDate);
    }

    @Override
    public Meeting createOneTimeMeeting(Project project, String title, String description,
                                       LocalDate date, LocalTime startTime, LocalTime endTime,
                                       String location) {
        LOGGER.info("Creating one-time meeting: " + title + " for project: " + project.getId());
        
        Meeting meeting = new Meeting();
        meeting.setProject(project);
        meeting.setTitle(title);
        meeting.setDescription(description);
        meeting.setDate(date);
        meeting.setStartTime(startTime);
        meeting.setEndTime(endTime);
        meeting.setLocation(location);
        meeting.setRecurring(false);
        meeting.setCancelled(false);
        meeting.setMilestones(new ArrayList<>());
        
        return repository.save(meeting);
    }

    @Override
    public List<Meeting> createRecurringMeetings(Project project, String title, String description,
                                                LocalDate startDate, LocalDate endDate, DayOfWeek dayOfWeek,
                                                LocalTime startTime, LocalTime endTime, String location) {
        LOGGER.info("Creating recurring meetings: " + title + " for project: " + project.getId());
        
        List<Meeting> meetings = new ArrayList<>();
        
        // Find the first occurrence of the day of week from the start date
        LocalDate currentDate = startDate;
        if (currentDate.getDayOfWeek() != dayOfWeek) {
            currentDate = startDate.with(TemporalAdjusters.nextOrSame(dayOfWeek));
        }
        
        // Create meetings for each occurrence of the day of week until the end date
        while (!currentDate.isAfter(endDate)) {
            Meeting meeting = new Meeting();
            meeting.setProject(project);
            meeting.setTitle(title);
            meeting.setDescription(description);
            meeting.setDate(currentDate);
            meeting.setStartTime(startTime);
            meeting.setEndTime(endTime);
            meeting.setLocation(location);
            meeting.setRecurring(true);
            meeting.setCancelled(false);
            meeting.setMilestones(new ArrayList<>());
            
            meetings.add(repository.save(meeting));
            
            // Move to the next occurrence
            currentDate = currentDate.plusWeeks(1);
        }
        
        return meetings;
    }

    @Override
    public Meeting associateWithMilestone(Meeting meeting, Milestone milestone) {
        LOGGER.info("Associating meeting: " + meeting.getId() + " with milestone: " + milestone.getId());
        
        List<Milestone> milestones = meeting.getMilestones();
        if (milestones == null) {
            milestones = new ArrayList<>();
            meeting.setMilestones(milestones);
        }
        
        if (!milestones.contains(milestone)) {
            milestones.add(milestone);
        }
        
        return repository.update(meeting);
    }

    @Override
    public Meeting removeMilestoneAssociation(Meeting meeting, Milestone milestone) {
        LOGGER.info("Removing milestone association for meeting: " + meeting.getId() + 
                " from milestone: " + milestone.getId());
        
        List<Milestone> milestones = meeting.getMilestones();
        if (milestones != null) {
            milestones.remove(milestone);
        }
        
        return repository.update(meeting);
    }

    @Override
    public Meeting updateNotes(Meeting meeting, String notes) {
        LOGGER.info("Updating notes for meeting: " + meeting.getId());
        
        meeting.setNotes(notes);
        
        return repository.update(meeting);
    }

    @Override
    public Meeting cancelMeeting(Meeting meeting) {
        LOGGER.info("Canceling meeting: " + meeting.getId());
        
        meeting.setCancelled(true);
        
        return repository.update(meeting);
    }

    @Override
    public List<Meeting> getUpcomingMeetings(Project project) {
        LOGGER.info("Getting upcoming meetings for project: " + project.getId());
        
        LocalDate now = LocalDate.now();
        
        return repository.findByProject(project).stream()
                .filter(m -> !m.isCancelled() && !m.getDate().isBefore(now))
                .sorted(Comparator.comparing(Meeting::getDate))
                .collect(Collectors.toList());
    }

    @Override
    public Meeting getNextMeeting(Project project) {
        LOGGER.info("Getting next meeting for project: " + project.getId());
        
        LocalDate now = LocalDate.now();
        
        return repository.findByProject(project).stream()
                .filter(m -> !m.isCancelled() && !m.getDate().isBefore(now))
                .min(Comparator.comparing(Meeting::getDate)
                        .thenComparing(Meeting::getStartTime))
                .orElse(null);
    }

    @Override
    public List<Meeting> findConflictingMeetings(LocalDate date, LocalTime startTime, LocalTime endTime) {
        LOGGER.info("Finding conflicting meetings for date: " + date + 
                    ", time: " + startTime + " - " + endTime);
        
        List<Meeting> meetingsOnDate = repository.findByDate(date);
        
        return meetingsOnDate.stream()
                .filter(m -> !m.isCancelled())
                .filter(m -> 
                    // Meeting starts during the time slot
                    (m.getStartTime().compareTo(startTime) >= 0 && 
                     m.getStartTime().compareTo(endTime) < 0) ||
                    // Meeting ends during the time slot
                    (m.getEndTime().compareTo(startTime) > 0 && 
                     m.getEndTime().compareTo(endTime) <= 0) ||
                    // Meeting completely encloses the time slot
                    (m.getStartTime().compareTo(startTime) <= 0 && 
                     m.getEndTime().compareTo(endTime) >= 0))
                .collect(Collectors.toList());
    }
}