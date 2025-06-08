// src/main/java/org/frcpm/services/impl/TestableAttendanceServiceImpl.java

package org.frcpm.services.impl;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.specific.AttendanceRepository;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.TeamMemberRepository;
import org.frcpm.services.AttendanceService;
import org.frcpm.di.ServiceLocator;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable implementation of AttendanceService that accepts injected repositories
 * for better unit testing. This implementation eliminates static references to
 * RepositoryFactory and uses proper constructor injection for all dependencies.
 */
public class TestableAttendanceServiceImpl implements AttendanceService {
    
    private static final Logger LOGGER = Logger.getLogger(TestableAttendanceServiceImpl.class.getName());
    
    // Dependencies injected via constructor
    private final AttendanceRepository attendanceRepository;
    private final MeetingRepository meetingRepository;
    private final TeamMemberRepository teamMemberRepository;
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableAttendanceServiceImpl() {
        this(
            ServiceLocator.getAttendanceRepository(),
            ServiceLocator.getMeetingRepository(),
            ServiceLocator.getTeamMemberRepository()
        );
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param attendanceRepository the attendance repository
     * @param meetingRepository the meeting repository
     * @param teamMemberRepository the team member repository
     */
    public TestableAttendanceServiceImpl(
            AttendanceRepository attendanceRepository,
            MeetingRepository meetingRepository,
            TeamMemberRepository teamMemberRepository) {
        this.attendanceRepository = attendanceRepository;
        this.meetingRepository = meetingRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    public Attendance findById(Long id) {
        if (id == null) {
            return null;
        }
        return attendanceRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Attendance> findAll() {
        return attendanceRepository.findAll();
    }
    
    @Override
    public Attendance save(Attendance entity) {
        try {
            return attendanceRepository.save(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving attendance", e);
            throw new RuntimeException("Failed to save attendance", e);
        }
    }
    
    @Override
    public void delete(Attendance entity) {
        try {
            attendanceRepository.delete(entity);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting attendance", e);
            throw new RuntimeException("Failed to delete attendance", e);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        try {
            return attendanceRepository.deleteById(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting attendance by ID", e);
            throw new RuntimeException("Failed to delete attendance by ID", e);
        }
    }
    
    @Override
    public long count() {
        return attendanceRepository.count();
    }
    
    @Override
    public List<Attendance> findByMeeting(Meeting meeting) {
        if (meeting == null) {
            throw new IllegalArgumentException("Meeting cannot be null");
        }
        return attendanceRepository.findByMeeting(meeting);
    }
    
    @Override
    public List<Attendance> findByMember(TeamMember member) {
        if (member == null) {
            throw new IllegalArgumentException("Team member cannot be null");
        }
        return attendanceRepository.findByMember(member);
    }
    
    @Override
    public Optional<Attendance> findByMeetingAndMember(Meeting meeting, TeamMember member) {
        if (meeting == null || member == null) {
            throw new IllegalArgumentException("Meeting and member cannot be null");
        }
        return attendanceRepository.findByMeetingAndMember(meeting, member);
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
        Optional<Attendance> existingAttendance = attendanceRepository.findByMeetingAndMember(meeting, member);
        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();
            attendance.setPresent(present);
            return save(attendance);
        }
        
        // Create new attendance record
        Attendance attendance = new Attendance(meeting, member, present);
        
        // If present, set default arrival time to meeting start time
        if (present) {
            attendance.setArrivalTime(meeting.getStartTime());
            attendance.setDepartureTime(meeting.getEndTime());
        }
        
        return save(attendance);
    }
    
    @Override
    public Attendance updateAttendance(Long attendanceId, boolean present, 
                                      LocalTime arrivalTime, LocalTime departureTime) {
        if (attendanceId == null) {
            throw new IllegalArgumentException("Attendance ID cannot be null");
        }
        
        Optional<Attendance> attendanceOpt = attendanceRepository.findById(attendanceId);
        if (attendanceOpt.isEmpty()) {
            LOGGER.log(Level.WARNING, "Attendance not found with ID: {0}", attendanceId);
            return null;
        }
        
        Attendance attendance = attendanceOpt.get();
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
        
        return save(attendance);
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
            Optional<Attendance> existingAttendance = attendanceRepository.findByMeetingAndMember(meeting, member);
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
            
            save(attendance);
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
        List<Attendance> attendanceRecords = attendanceRepository.findByMember(member);
        
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
}