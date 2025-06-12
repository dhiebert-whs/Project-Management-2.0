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

/**
 * Spring Boot implementation of AttendanceService using composition-based pattern.
 * NO INHERITANCE - Direct implementation of all interface methods.
 */
@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {
    
    private final AttendanceRepository attendanceRepository;
    private final MeetingRepository meetingRepository;
    private final TeamMemberRepository teamMemberRepository;
    
    /**
     * Constructor injection - no inheritance, pure composition
     */
    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                MeetingRepository meetingRepository,
                                TeamMemberRepository teamMemberRepository) {
        this.attendanceRepository = attendanceRepository;
        this.meetingRepository = meetingRepository;
        this.teamMemberRepository = teamMemberRepository;
    }
    
    // ========================================
    // Basic CRUD Operations (from Service<T, ID> interface)
    // ========================================
    
    @Override
    public Attendance findById(Long id) {
        return attendanceRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Attendance> findAll() {
        return attendanceRepository.findAll();
    }
    
    @Override
    public Attendance save(Attendance entity) {
        return attendanceRepository.save(entity);
    }
    
    @Override
    public void delete(Attendance entity) {
        attendanceRepository.delete(entity);
    }
    
    @Override
    public boolean deleteById(Long id) {
        if (attendanceRepository.existsById(id)) {
            attendanceRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Override
    public long count() {
        return attendanceRepository.count();
    }
    
    // ========================================
    // Business Operations (AttendanceService specific)
    // ========================================
    
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
        
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("Meeting not found with ID: " + meetingId));
        
        TeamMember member = teamMemberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Team member not found with ID: " + memberId));
        
        // Check if attendance already exists
        Optional<Attendance> existing = attendanceRepository.findByMeetingAndMember(meeting, member);
        if (existing.isPresent()) {
            Attendance attendance = existing.get();
            attendance.setPresent(present);
            if (present) {
                attendance.setArrivalTime(meeting.getStartTime());
                attendance.setDepartureTime(meeting.getEndTime());
            } else {
                attendance.setArrivalTime(null);
                attendance.setDepartureTime(null);
            }
            return attendanceRepository.save(attendance);
        }
        
        // Create new attendance
        Attendance attendance = new Attendance(meeting, member, present);
        if (present) {
            attendance.setArrivalTime(meeting.getStartTime());
            attendance.setDepartureTime(meeting.getEndTime());
        }
        
        return attendanceRepository.save(attendance);
    }
    
    @Override
    public Attendance updateAttendance(Long attendanceId, boolean present, 
                                      LocalTime arrivalTime, LocalTime departureTime) {
        if (attendanceId == null) {
            throw new IllegalArgumentException("Attendance ID cannot be null");
        }
        
        Attendance attendance = attendanceRepository.findById(attendanceId).orElse(null);
        if (attendance == null) {
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
            attendance.setArrivalTime(null);
            attendance.setDepartureTime(null);
        }
        
        return attendanceRepository.save(attendance);
    }
    
    @Override
    public int recordAttendanceForMeeting(Long meetingId, List<Long> presentMemberIds) {
        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting ID cannot be null");
        }
        
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("Meeting not found with ID: " + meetingId));
        
        List<TeamMember> allMembers = teamMemberRepository.findAll();
        int count = 0;
        
        for (TeamMember member : allMembers) {
            boolean present = presentMemberIds != null && presentMemberIds.contains(member.getId());
            
            Optional<Attendance> existing = attendanceRepository.findByMeetingAndMember(meeting, member);
            Attendance attendance;
            
            if (existing.isPresent()) {
                attendance = existing.get();
                attendance.setPresent(present);
            } else {
                attendance = new Attendance(meeting, member, present);
            }
            
            if (present) {
                attendance.setArrivalTime(meeting.getStartTime());
                attendance.setDepartureTime(meeting.getEndTime());
            } else {
                attendance.setArrivalTime(null);
                attendance.setDepartureTime(null);
            }
            
            attendanceRepository.save(attendance);
            count++;
        }
        
        return count;
    }
    
    @Override
    public Map<String, Object> getAttendanceStatistics(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        
        TeamMember member = teamMemberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Team member not found with ID: " + memberId));
        
        List<Attendance> attendanceRecords = attendanceRepository.findByMember(member);
        
        int totalMeetings = attendanceRecords.size();
        long presentCount = attendanceRecords.stream().filter(Attendance::isPresent).count();
        double attendanceRate = totalMeetings > 0 ? 
            Math.round(((double) presentCount / totalMeetings * 100) * 100) / 100.0 : 0.0;
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("memberId", memberId);
        statistics.put("memberName", member.getFullName());
        statistics.put("totalMeetings", totalMeetings);
        statistics.put("presentCount", presentCount);
        statistics.put("absentCount", totalMeetings - (int) presentCount);
        statistics.put("attendanceRate", attendanceRate);
        
        return statistics;
    }
    
    // ========================================
    // Async Operations (Spring Boot style)
    // ========================================
    
    @Async
    public CompletableFuture<List<Attendance>> findAllAsync() {
        return CompletableFuture.completedFuture(findAll());
    }
    
    @Async
    public CompletableFuture<Attendance> findByIdAsync(Long id) {
        return CompletableFuture.completedFuture(findById(id));
    }
    
    @Async
    public CompletableFuture<Attendance> saveAsync(Attendance entity) {
        return CompletableFuture.completedFuture(save(entity));
    }
    
    @Async
    public CompletableFuture<Boolean> deleteByIdAsync(Long id) {
        return CompletableFuture.completedFuture(deleteById(id));
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> getAttendanceStatisticsAsync(Long memberId) {
        return CompletableFuture.completedFuture(getAttendanceStatistics(memberId));
    }
}