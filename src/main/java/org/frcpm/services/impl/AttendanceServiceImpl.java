// src/main/java/org/frcpm/services/impl/AttendanceServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.spring.AttendanceRepository;
import org.frcpm.repositories.spring.MeetingRepository;
import org.frcpm.repositories.spring.TeamMemberRepository;
import org.frcpm.services.AttendanceService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spring Boot implementation of AttendanceService.
 * CORRECTED: Uses the actual AbstractSpringService signature from ProjectServiceImpl.
 * 
 * ARCHITECTURE PATTERN (CORRECTED):
 * - Extends AbstractSpringService<Attendance, Long, AttendanceRepository> (MATCHES ProjectServiceImpl)
 * - Constructor passes AttendanceRepository to super() for basic CRUD
 * - Additional repositories injected via constructor for business logic
 */
@Service("attendanceServiceImpl")
@Transactional
public class AttendanceServiceImpl extends AbstractSpringService<Attendance, Long, AttendanceRepository> 
        implements AttendanceService {
    
    private static final Logger LOGGER = Logger.getLogger(AttendanceServiceImpl.class.getName());
    
    // Additional dependencies injected via constructor
    private final MeetingRepository meetingRepository;
    private final TeamMemberRepository teamMemberRepository;
    
    /**
     * Constructor using CORRECTED pattern matching ProjectServiceImpl exactly.
     * 
     * @param attendanceRepository the attendance repository (passed to super())
     * @param meetingRepository the meeting repository for business logic
     * @param teamMemberRepository the team member repository for business logic
     */
    public AttendanceServiceImpl(
            AttendanceRepository attendanceRepository,
            MeetingRepository meetingRepository,
            TeamMemberRepository teamMemberRepository) {
        // CORRECTED: Match ProjectServiceImpl constructor pattern exactly
        super(attendanceRepository);
        this.meetingRepository = meetingRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    protected String getEntityName() {
        return "attendance";
    }
    
    // Attendance-specific business methods using repository from AbstractSpringService
    
    @Override
    public List<Attendance> findByMeeting(Meeting meeting) {
        if (meeting == null) {
            throw new IllegalArgumentException("Meeting cannot be null");
        }
        // Use repository from AbstractSpringService (attendanceRepository is accessible via 'repository')
        return repository.findByMeeting(meeting);
    }
    
    @Override
    public List<Attendance> findByMember(TeamMember member) {
        if (member == null) {
            throw new IllegalArgumentException("Team member cannot be null");
        }
        return repository.findByMember(member);
    }
    
    @Override
    public Optional<Attendance> findByMeetingAndMember(Meeting meeting, TeamMember member) {
        if (meeting == null || member == null) {
            throw new IllegalArgumentException("Meeting and member cannot be null");
        }
        return repository.findByMeetingAndMember(meeting, member);
    }
    
    @Override
    public Attendance createAttendance(Long meetingId, Long memberId, boolean present) {
        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting ID cannot be null");
        }
        
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        
        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);
        if (meetingOpt.isEmpty()) {
            LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
            throw new IllegalArgumentException("Meeting not found with ID: " + meetingId);
        }
        
        Optional<TeamMember> memberOpt = teamMemberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
            throw new IllegalArgumentException("Team member not found with ID: " + memberId);
        }
        
        Meeting meeting = meetingOpt.get();
        TeamMember member = memberOpt.get();
        
        // Check if attendance record already exists
        Optional<Attendance> existingAttendance = repository.findByMeetingAndMember(meeting, member);
        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();
            attendance.setPresent(present);
            return save(attendance); // Uses AbstractSpringService.save()
        }
        
        // Create new attendance record
        Attendance attendance = new Attendance(meeting, member, present);
        
        // If present, set default arrival time to meeting start time
        if (present) {
            attendance.setArrivalTime(meeting.getStartTime());
            attendance.setDepartureTime(meeting.getEndTime());
        }
        
        return save(attendance); // Uses AbstractSpringService.save()
    }
    
    @Override
    public Attendance updateAttendance(Long attendanceId, boolean present, 
                                      LocalTime arrivalTime, LocalTime departureTime) {
        if (attendanceId == null) {
            throw new IllegalArgumentException("Attendance ID cannot be null");
        }
        
        Attendance attendance = findById(attendanceId); // Uses AbstractSpringService.findById()
        if (attendance == null) {
            LOGGER.log(Level.WARNING, "Attendance not found with ID: {0}", attendanceId);
            return null;
        }
        
        attendance.setPresent(present);
        
        if (present) {
            if (arrivalTime != null) {
                attendance.setArrivalTime(arrivalTime);
            }
            
            if (departureTime != null) {
                if (attendance.getArrivalTime() != null && 
                    departureTime.isBefore(attendance.getArrivalTime())) {
                    throw new IllegalArgumentException("Departure time cannot be before arrival time");
                }
                attendance.setDepartureTime(departureTime);
            }
        } else {
            // If not present, clear times
            attendance.setArrivalTime(null);
            attendance.setDepartureTime(null);
        }
        
        return save(attendance); // Uses AbstractSpringService.save()
    }
    
    @Override
    public int recordAttendanceForMeeting(Long meetingId, List<Long> presentMemberIds) {
        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting ID cannot be null");
        }
        
        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);
        if (meetingOpt.isEmpty()) {
            LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
            throw new IllegalArgumentException("Meeting not found with ID: " + meetingId);
        }
        
        Meeting meeting = meetingOpt.get();
        
        // Get all team members
        List<TeamMember> allMembers = teamMemberRepository.findAll();
        
        int count = 0;
        for (TeamMember member : allMembers) {
            boolean present = presentMemberIds != null && presentMemberIds.contains(member.getId());
            
            // Check if attendance record already exists
            Optional<Attendance> existingAttendance = repository.findByMeetingAndMember(meeting, member);
            Attendance attendance;
            
            if (existingAttendance.isPresent()) {
                attendance = existingAttendance.get();
                attendance.setPresent(present);
            } else {
                attendance = new Attendance(meeting, member, present);
                
                // If present, set default arrival time to meeting start time
                if (present) {
                    attendance.setArrivalTime(meeting.getStartTime());
                    attendance.setDepartureTime(meeting.getEndTime());
                }
            }
            
            save(attendance); // Uses AbstractSpringService.save()
            count++;
        }
        
        return count;
    }
    
    @Override
    public Map<String, Object> getAttendanceStatistics(Long memberId) {
        Map<String, Object> statistics = new HashMap<>();
        
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        
        Optional<TeamMember> memberOpt = teamMemberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
            return statistics;
        }
        
        TeamMember member = memberOpt.get();
        List<Attendance> attendanceRecords = repository.findByMember(member);
        
        int totalMeetings = attendanceRecords.size();
        long presentCount = attendanceRecords.stream().filter(Attendance::isPresent).count();
        
        // Calculate attendance rate and round to 2 decimal places
        double attendanceRate = 0.0;
        if (totalMeetings > 0) {
            attendanceRate = Math.round(((double) presentCount / totalMeetings * 100) * 100) / 100.0;
        }
        
        statistics.put("memberId", memberId);
        statistics.put("memberName", member.getFullName());
        statistics.put("totalMeetings", totalMeetings);
        statistics.put("presentCount", presentCount);
        statistics.put("absentCount", Integer.valueOf(totalMeetings - (int) presentCount));
        statistics.put("attendanceRate", attendanceRate);
        
        return statistics;
    }

    // Spring Boot Async Methods
    
    @Async
    public CompletableFuture<List<Attendance>> findAllAsync() {
        try {
            List<Attendance> result = findAll(); // Uses AbstractSpringService.findAll()
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<List<Attendance>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Attendance> saveAsync(Attendance entity) {
        try {
            Attendance result = save(entity); // Uses AbstractSpringService.save()
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Attendance> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> getAttendanceStatisticsAsync(Long memberId) {
        try {
            Map<String, Object> result = getAttendanceStatistics(memberId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public Attendance findById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public List<Attendance> findAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public Attendance save(Attendance entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public void delete(Attendance entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public boolean deleteById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }

    @Override
    public long count() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'count'");
    }
}