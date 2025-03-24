package org.frcpm.repositories.specific;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.repositories.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Attendance entity.
 */
public interface AttendanceRepository extends Repository<Attendance, Long> {
    
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
     * Finds attendance records by presence status.
     * 
     * @param present whether to find records for present or absent members
     * @return a list of attendance records with the given presence status
     */
    List<Attendance> findByPresent(boolean present);
}
