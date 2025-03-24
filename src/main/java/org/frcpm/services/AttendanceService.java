package org.frcpm.services;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for managing Attendance entities.
 */
public interface AttendanceService extends Service<Attendance, Long> {

    /**
     * Finds all attendance records for a specific meeting.
     *
     * @param meeting The meeting to find attendance for
     * @return List of attendance records for the meeting
     */
    List<Attendance> findByMeeting(Meeting meeting);

    /**
     * Finds all attendance records for a specific team member.
     *
     * @param teamMember The team member to find attendance for
     * @return List of attendance records for the team member
     */
    List<Attendance> findByTeamMember(TeamMember teamMember);

    /**
     * Finds all attendance records for a specific date.
     *
     * @param date The date to find attendance for
     * @return List of attendance records for the date
     */
    List<Attendance> findByDate(LocalDate date);

    /**
     * Records attendance for a team member at a meeting.
     *
     * @param meeting The meeting
     * @param teamMember The team member
     * @param present Whether the team member was present
     * @return The created attendance record
     */
    Attendance recordAttendance(Meeting meeting, TeamMember teamMember, boolean present);

    /**
     * Records arrival time for a team member at a meeting.
     *
     * @param attendance The attendance record
     * @param arrivalTime The arrival time
     * @return The updated attendance record
     */
    Attendance recordArrivalTime(Attendance attendance, LocalDate arrivalTime);

    /**
     * Records departure time for a team member at a meeting.
     *
     * @param attendance The attendance record
     * @param departureTime The departure time
     * @return The updated attendance record
     */
    Attendance recordDepartureTime(Attendance attendance, LocalDate departureTime);

    /**
     * Calculates the attendance rate for a team member.
     *
     * @param teamMember The team member
     * @return The attendance rate as a percentage
     */
    double calculateAttendanceRate(TeamMember teamMember);

    /**
     * Gets all team members present at a specific meeting.
     *
     * @param meeting The meeting
     * @return List of team members present at the meeting
     */
    List<TeamMember> getPresentTeamMembers(Meeting meeting);

    /**
     * Gets all team members absent from a specific meeting.
     *
     * @param meeting The meeting
     * @return List of team members absent from the meeting
     */
    List<TeamMember> getAbsentTeamMembers(Meeting meeting);
}