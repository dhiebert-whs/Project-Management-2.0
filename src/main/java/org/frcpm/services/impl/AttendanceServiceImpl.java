package org.frcpm.services.impl;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.AttendanceRepository;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.TeamMemberRepository;
import org.frcpm.services.AttendanceService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of AttendanceService using repository layer.
 */
public class AttendanceServiceImpl extends AbstractService<Attendance, Long, AttendanceRepository>
        implements AttendanceService {

    private static final Logger LOGGER = Logger.getLogger(AttendanceServiceImpl.class.getName());
    private final MeetingRepository meetingRepository;
    private final TeamMemberRepository teamMemberRepository;

    public AttendanceServiceImpl() {
        super(RepositoryFactory.getAttendanceRepository());
        this.meetingRepository = RepositoryFactory.getMeetingRepository();
        this.teamMemberRepository = RepositoryFactory.getTeamMemberRepository();
    }

    @Override
    public List<Attendance> findByMeeting(Meeting meeting) {
        return repository.findByMeeting(meeting);
    }

    @Override
    public List<Attendance> findByMember(TeamMember member) {
        return repository.findByMember(member);
    }

    @Override
    public Optional<Attendance> findByMeetingAndMember(Meeting meeting, TeamMember member) {
        return repository.findByMeetingAndMember(meeting, member);
    }

    @Override
    public Attendance createAttendance(Long meetingId, Long memberId, boolean present) {
        if (meetingId == null || memberId == null) {
            throw new IllegalArgumentException("Meeting ID and Member ID cannot be null");
        }

        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) {
            LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
            throw new IllegalArgumentException("Meeting not found with ID: " + meetingId);
        }

        TeamMember member = teamMemberRepository.findById(memberId).orElse(null);
        if (member == null) {
            LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
            throw new IllegalArgumentException("Team member not found with ID: " + memberId);
        }

        // Check if an attendance record already exists
        Optional<Attendance> existingAttendance = repository.findByMeetingAndMember(meeting, member);
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

        Attendance attendance = findById(attendanceId);
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

        return save(attendance);
    }

    @Override
    public int recordAttendanceForMeeting(Long meetingId, List<Long> presentMemberIds) {
        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting ID cannot be null");
        }

        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) {
            LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
            throw new IllegalArgumentException("Meeting not found with ID: " + meetingId);
        }

        // Get all team members
        List<TeamMember> allMembers = teamMemberRepository.findAll();

        int count = 0;
        for (TeamMember member : allMembers) {
            boolean present = presentMemberIds != null && presentMemberIds.contains(member.getId());

            // Check if an attendance record already exists
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

        TeamMember member = teamMemberRepository.findById(memberId).orElse(null);
        if (member == null) {
            LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
            return statistics;
        }

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
        // Convert to Integer explicitly to match test expectations
        statistics.put("absentCount", Integer.valueOf(totalMeetings - (int) presentCount));
        statistics.put("attendanceRate", attendanceRate);

        return statistics;
    }
}