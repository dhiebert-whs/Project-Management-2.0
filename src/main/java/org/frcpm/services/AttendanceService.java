package org.frcpm.services;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Attendance entity.
 */
public interface AttendanceService extends Service<Attendance, Long> {
    
    /**
     * Finds attendance records for a specific meeting.
     * 
     * @param meeting the meeting to find attendance for
     * @return a list of attendance records for the meeting
     */
    List<Attendance> findByMeeting(Meeting meeting);
    
    /**
     * Finds attendance records for a specific team member.
     * 
     * @param member the team member to find attendance for
     * @return a list of attendance records for the team member
     */
    List<Attendance> findByMember(TeamMember member);
    
    /**
     * Finds attendance records for a specific meeting and team member.
     * 
     * @param meeting the meeting
     * @param member the team member
     * @return an Optional containing the attendance record, or empty if not found
     */
    Optional<Attendance> findByMeetingAndMember(Meeting meeting, TeamMember member);
    
    /**
     * Creates a new attendance record.
     * 
     * @param meetingId the meeting ID
     * @param memberId the team member ID
     * @param present whether the member is present
     * @return the created attendance record
     */
    Attendance createAttendance(Long meetingId, Long memberId, boolean present);
    
    /**
     * Updates an attendance record.
     * 
     * @param attendanceId the attendance ID
     * @param present whether the member is present
     * @param arrivalTime the arrival time (optional, only used if present is true)
     * @param departureTime the departure time (optional, only used if present is true)
     * @return the updated attendance record, or null if not found
     */
    Attendance updateAttendance(Long attendanceId, boolean present, 
                               LocalTime arrivalTime, LocalTime departureTime);
    
    /**
     * Records attendance for all team members in a meeting.
     * 
     * @param meetingId the meeting ID
     * @param presentMemberIds the IDs of present team members
     * @return the number of attendance records created or updated
     */
    int recordAttendanceForMeeting(Long meetingId, List<Long> presentMemberIds);
    
    /**
     * Gets attendance statistics for a team member.
     * 
     * @param memberId the team member ID
     * @return a map containing attendance statistics
     */
    java.util.Map<String, Object> getAttendanceStatistics(Long memberId);
}