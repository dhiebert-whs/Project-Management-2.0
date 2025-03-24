package org.frcpm.services.impl;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.AttendanceRepository;
import org.frcpm.repositories.specific.MeetingRepository;
import org.frcpm.repositories.specific.TeamMemberRepository;
import org.frcpm.services.AttendanceService;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the AttendanceService interface.
 */
public class AttendanceServiceImpl extends AbstractService<Attendance, Long, AttendanceRepository>
        implements AttendanceService {

    private static final Logger LOGGER = Logger.getLogger(AttendanceServiceImpl.class.getName());
    
    private final MeetingRepository meetingRepository;
    private final TeamMemberRepository teamMemberRepository;

    /**
     * Constructor for AttendanceServiceImpl.
     */
    public AttendanceServiceImpl() {
        super(RepositoryFactory.getAttendanceRepository());
        this.meetingRepository = RepositoryFactory.getMeetingRepository();
        this.teamMemberRepository = RepositoryFactory.getTeamMemberRepository();
    }

    @Override
    public List<Attendance> findByMeeting(Meeting meeting) {
        LOGGER.info("Finding attendance records for meeting: " + meeting.getId());
        return repository.findByMeeting(meeting);
    }

    @Override
    public List<Attendance> findByTeamMember(TeamMember teamMember) {
        LOGGER.info("Finding attendance records for team member: " + teamMember.getId());
        return repository.findByTeamMember(teamMember);
    }

    @Override
    public List<Attendance> findByDate(LocalDate date) {
        LOGGER.info("Finding attendance records for date: " + date);
        return repository.findByDate(date);
    }

    @Override
    public Attendance recordAttendance(Meeting meeting, TeamMember teamMember, boolean present) {
        LOGGER.info("Recording attendance for team member: " + teamMember.getId() + 
                " at meeting: " + meeting.getId() + ", present: " + present);
        
        // Check if an attendance record already exists
        Attendance existingAttendance = repository.findByMeetingAndTeamMember(meeting, teamMember);
        
        if (existingAttendance != null) {
            existingAttendance.setPresent(present);
            return repository.update(existingAttendance);
        } else {
            Attendance attendance = new Attendance();
            attendance.setMeeting(meeting);
            attendance.setTeamMember(teamMember);
            attendance.setPresent(present);
            attendance.setDate(meeting.getDate());
            return repository.save(attendance);
        }
    }

    @Override
    public Attendance recordArrivalTime(Attendance attendance, LocalDate arrivalTime) {
        LOGGER.info("Recording arrival time for attendance: " + attendance.getId());
        attendance.setArrivalTime(arrivalTime);
        return repository.update(attendance);
    }

    @Override
    public Attendance recordDepartureTime(Attendance attendance, LocalDate departureTime) {
        LOGGER.info("Recording departure time for attendance: " + attendance.getId());
        attendance.setDepartureTime(departureTime);
        return repository.update(attendance);
    }

    @Override
    public double calculateAttendanceRate(TeamMember teamMember) {
        LOGGER.info("Calculating attendance rate for team member: " + teamMember.getId());
        
        List<Attendance> attendanceRecords = repository.findByTeamMember(teamMember);
        
        if (attendanceRecords.isEmpty()) {
            return 0.0;
        }
        
        long presentCount = attendanceRecords.stream()
                .filter(Attendance::isPresent)
                .count();
        
        return (double) presentCount / attendanceRecords.size() * 100.0;
    }

    @Override
    public List<TeamMember> getPresentTeamMembers(Meeting meeting) {
        LOGGER.info("Getting present team members for meeting: " + meeting.getId());
        
        List<Attendance> attendanceRecords = repository.findByMeeting(meeting);
        
        return attendanceRecords.stream()
                .filter(Attendance::isPresent)
                .map(Attendance::getTeamMember)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamMember> getAbsentTeamMembers(Meeting meeting) {
        LOGGER.info("Getting absent team members for meeting: " + meeting.getId());
        
        List<Attendance> attendanceRecords = repository.findByMeeting(meeting);
        List<TeamMember> presentMembers = getPresentTeamMembers(meeting);
        
        // Get all team members and filter out the present ones
        List<TeamMember> allMembers = teamMemberRepository.findAll();
        
        return allMembers.stream()
                .filter(member -> !presentMembers.contains(member))
                .collect(Collectors.toList());
    }
}