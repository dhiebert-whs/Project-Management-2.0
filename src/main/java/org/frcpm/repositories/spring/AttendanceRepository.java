// src/main/java/org/frcpm/repositories/spring/AttendanceRepository.java

package org.frcpm.repositories.spring;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Attendance entity.
 * Provides both auto-implemented Spring Data JPA methods and custom query methods.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
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
    
    /**
     * Finds attendance records for a specific meeting by meeting ID.
     * 
     * @param meetingId the meeting ID
     * @return a list of attendance records for the meeting
     */
    @Query("SELECT a FROM Attendance a WHERE a.meeting.id = :meetingId")
    List<Attendance> findByMeetingId(@Param("meetingId") Long meetingId);
    
    /**
     * Finds attendance records for a specific team member by member ID.
     * 
     * @param memberId the team member ID
     * @return a list of attendance records for the team member
     */
    @Query("SELECT a FROM Attendance a WHERE a.member.id = :memberId")
    List<Attendance> findByMemberId(@Param("memberId") Long memberId);
    
    /**
     * Finds attendance records for a specific meeting and team member by IDs.
     * 
     * @param meetingId the meeting ID
     * @param memberId the team member ID
     * @return an Optional containing the attendance record, or empty if not found
     */
    @Query("SELECT a FROM Attendance a WHERE a.meeting.id = :meetingId AND a.member.id = :memberId")
    Optional<Attendance> findByMeetingIdAndMemberId(@Param("meetingId") Long meetingId, @Param("memberId") Long memberId);
    
    /**
     * Finds all attendance records for meetings within a date range.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return a list of attendance records for meetings in the date range
     */
    @Query("SELECT a FROM Attendance a JOIN a.meeting m WHERE m.date BETWEEN :startDate AND :endDate ORDER BY m.date")
    List<Attendance> findByMeetingDateBetween(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
    
    /**
     * Finds attendance records for a specific project by project ID.
     * 
     * @param projectId the project ID
     * @return a list of attendance records for meetings in the project
     */
    @Query("SELECT a FROM Attendance a JOIN a.meeting m WHERE m.project.id = :projectId")
    List<Attendance> findByProjectId(@Param("projectId") Long projectId);
    
    /**
     * Counts attendance records for a specific member by ID.
     * 
     * @param memberId the team member ID
     * @return the count of attendance records
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.member.id = :memberId")
    long countByMemberId(@Param("memberId") Long memberId);
    
    /**
     * Counts attendance records where the member was present by ID.
     * 
     * @param memberId the team member ID
     * @param present the presence status
     * @return the count of attendance records with the given presence status
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.member.id = :memberId AND a.present = :present")
    long countByMemberIdAndPresent(@Param("memberId") Long memberId, @Param("present") boolean present);
}