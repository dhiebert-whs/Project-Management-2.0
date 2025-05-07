// src/main/java/org/frcpm/services/impl/AttendanceServiceAsyncImpl.java

package org.frcpm.services.impl;

import javafx.application.Platform;
import org.frcpm.async.TaskExecutor;
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

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Asynchronous implementation of AttendanceService using the task-based threading model.
 */
public class AttendanceServiceAsyncImpl extends AbstractAsyncService<Attendance, Long, AttendanceRepository>
        implements AttendanceService {

    private static final Logger LOGGER = Logger.getLogger(AttendanceServiceAsyncImpl.class.getName());
    private final MeetingRepository meetingRepository;
    private final TeamMemberRepository teamMemberRepository;

    public AttendanceServiceAsyncImpl() {
        super(RepositoryFactory.getAttendanceRepository());
        this.meetingRepository = RepositoryFactory.getMeetingRepository();
        this.teamMemberRepository = RepositoryFactory.getTeamMemberRepository();
    }
    
    // Synchronous interface methods (implementing AttendanceService interface)

    @Override
    public List<Attendance> findByMeeting(Meeting meeting) {
        return repository.findByMeeting(meeting);
    }

    @Override
    public List<Attendance> findByTeamMember(TeamMember teamMember) {
        return repository.findByTeamMember(teamMember);
    }

    @Override
    public List<Attendance> findByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    @Override
    public Attendance recordAttendance(Long meetingId, Long teamMemberId, boolean present, String notes) {
        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting ID cannot be null");
        }
        
        if (teamMemberId == null) {
            throw new IllegalArgumentException("Team member ID cannot be null");
        }
        
        return executeSync(em -> {
            // Check if meeting exists
            Meeting meeting = em.find(Meeting.class, meetingId);
            if (meeting == null) {
                LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
                throw new IllegalArgumentException("Meeting not found with ID: " + meetingId);
            }
            
            // Check if team member exists
            TeamMember teamMember = em.find(TeamMember.class, teamMemberId);
            if (teamMember == null) {
                LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", teamMemberId);
                throw new IllegalArgumentException("Team member not found with ID: " + teamMemberId);
            }
            
            // Check if attendance record already exists
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a WHERE a.meeting.id = :meetingId AND a.teamMember.id = :teamMemberId", 
                    Attendance.class);
            query.setParameter("meetingId", meetingId);
            query.setParameter("teamMemberId", teamMemberId);
            List<Attendance> existingAttendance = query.getResultList();
            
            Attendance attendance;
            if (!existingAttendance.isEmpty()) {
                // Update existing record
                attendance = existingAttendance.get(0);
                attendance.setPresent(present);
                attendance.setNotes(notes);
            } else {
                // Create new record
                attendance = new Attendance(meeting, teamMember, present);
                attendance.setNotes(notes);
            }
            
            em.merge(attendance);
            return attendance;
        });
    }

    @Override
    public List<Attendance> getAttendanceReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        return executeSync(em -> {
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a JOIN a.meeting m WHERE m.date BETWEEN :startDate AND :endDate " +
                    "ORDER BY m.date", Attendance.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        });
    }

    @Override
    public List<Attendance> getTeamMemberAttendance(Long teamMemberId, LocalDate startDate, LocalDate endDate) {
        if (teamMemberId == null) {
            throw new IllegalArgumentException("Team member ID cannot be null");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        return executeSync(em -> {
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a JOIN a.meeting m " +
                    "WHERE a.teamMember.id = :teamMemberId AND m.date BETWEEN :startDate AND :endDate " +
                    "ORDER BY m.date", Attendance.class);
            query.setParameter("teamMemberId", teamMemberId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        });
    }

    // Asynchronous methods

    /**
     * Asynchronously finds attendance records by meeting.
     * 
     * @param meeting the meeting to find attendance for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of attendance records
     */
    public CompletableFuture<List<Attendance>> findByMeetingAsync(Meeting meeting,
                                                           Consumer<List<Attendance>> onSuccess,
                                                           Consumer<Throwable> onFailure) {
        if (meeting == null) {
            CompletableFuture<List<Attendance>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Meeting cannot be null"));
            return future;
        }

        return executeAsync("Find Attendance By Meeting: " + meeting.getId(), em -> {
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a WHERE a.meeting.id = :meetingId", Attendance.class);
            query.setParameter("meetingId", meeting.getId());
            return query.getResultList();
        }, onSuccess, onFailure);
    }

    /**
     * Asynchronously finds attendance records by team member.
     * 
     * @param teamMember the team member to find attendance for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of attendance records
     */
    public CompletableFuture<List<Attendance>> findByTeamMemberAsync(TeamMember teamMember,
                                                              Consumer<List<Attendance>> onSuccess,
                                                              Consumer<Throwable> onFailure) {
        if (teamMember == null) {
            CompletableFuture<List<Attendance>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Team member cannot be null"));
            return future;
        }

        return executeAsync("Find Attendance By Team Member: " + teamMember.getId(), em -> {
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a WHERE a.teamMember.id = :teamMemberId", Attendance.class);
            query.setParameter("teamMemberId", teamMember.getId());
            return query.getResultList();
        }, onSuccess, onFailure);
    }

    /**
     * Asynchronously finds attendance records by date.
     * 
     * @param date the date to find attendance for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of attendance records
     */
    public CompletableFuture<List<Attendance>> findByDateAsync(LocalDate date,
                                                       Consumer<List<Attendance>> onSuccess,
                                                       Consumer<Throwable> onFailure) {
        if (date == null) {
            CompletableFuture<List<Attendance>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Date cannot be null"));
            return future;
        }

        return executeAsync("Find Attendance By Date: " + date, em -> {
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a JOIN a.meeting m WHERE m.date = :date", Attendance.class);
            query.setParameter("date", date);
            return query.getResultList();
        }, onSuccess, onFailure);
    }

    /**
     * Asynchronously records attendance.
     * 
     * @param meetingId the meeting ID
     * @param teamMemberId the team member ID
     * @param present whether the team member was present
     * @param notes any attendance notes (optional)
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created or updated attendance record
     */
    public CompletableFuture<Attendance> recordAttendanceAsync(Long meetingId, Long teamMemberId,
                                                      boolean present, String notes,
                                                      Consumer<Attendance> onSuccess,
                                                      Consumer<Throwable> onFailure) {
        try {
            if (meetingId == null) {
                throw new IllegalArgumentException("Meeting ID cannot be null");
            }
            
            if (teamMemberId == null) {
                throw new IllegalArgumentException("Team member ID cannot be null");
            }

            return executeAsync("Record Attendance: " + meetingId + " / " + teamMemberId, em -> {
                // Check if meeting exists
                Meeting meeting = em.find(Meeting.class, meetingId);
                if (meeting == null) {
                    LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
                    throw new IllegalArgumentException("Meeting not found with ID: " + meetingId);
                }
                
                // Check if team member exists
                TeamMember teamMember = em.find(TeamMember.class, teamMemberId);
                if (teamMember == null) {
                    LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", teamMemberId);
                    throw new IllegalArgumentException("Team member not found with ID: " + teamMemberId);
                }
                
                // Check if attendance record already exists
                TypedQuery<Attendance> query = em.createQuery(
                        "SELECT a FROM Attendance a WHERE a.meeting.id = :meetingId AND a.teamMember.id = :teamMemberId", 
                        Attendance.class);
                query.setParameter("meetingId", meetingId);
                query.setParameter("teamMemberId", teamMemberId);
                List<Attendance> existingAttendance = query.getResultList();
                
                Attendance attendance;
                if (!existingAttendance.isEmpty()) {
                    // Update existing record
                    attendance = existingAttendance.get(0);
                    attendance.setPresent(present);
                    attendance.setNotes(notes);
                } else {
                    // Create new record
                    attendance = new Attendance(meeting, teamMember, present);
                    attendance.setNotes(notes);
                }
                
                em.merge(attendance);
                return attendance;
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Attendance> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Asynchronously gets an attendance report for a date range.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of attendance records
     */
    public CompletableFuture<List<Attendance>> getAttendanceReportAsync(LocalDate startDate, LocalDate endDate,
                                                                Consumer<List<Attendance>> onSuccess,
                                                                Consumer<Throwable> onFailure) {
        try {
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Start date and end date cannot be null");
            }
            
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }

            return executeAsync("Get Attendance Report: " + startDate + " to " + endDate, em -> {
                TypedQuery<Attendance> query = em.createQuery(
                        "SELECT a FROM Attendance a JOIN a.meeting m WHERE m.date BETWEEN :startDate AND :endDate " +
                        "ORDER BY m.date", Attendance.class);
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
                return query.getResultList();
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<List<Attendance>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Asynchronously gets attendance records for a team member in a date range.
     * 
     * @param teamMemberId the team member ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of attendance records
     */
    public CompletableFuture<List<Attendance>> getTeamMemberAttendanceAsync(Long teamMemberId,
                                                                    LocalDate startDate, LocalDate endDate,
                                                                    Consumer<List<Attendance>> onSuccess,
                                                                    Consumer<Throwable> onFailure) {
        try {
            if (teamMemberId == null) {
                throw new IllegalArgumentException("Team member ID cannot be null");
            }
            
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Start date and end date cannot be null");
            }
            
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }

            return executeAsync("Get Team Member Attendance: " + teamMemberId + " / " + startDate + " to " + endDate, em -> {
                TypedQuery<Attendance> query = em.createQuery(
                        "SELECT a FROM Attendance a JOIN a.meeting m " +
                        "WHERE a.teamMember.id = :teamMemberId AND m.date BETWEEN :startDate AND :endDate " +
                        "ORDER BY m.date", Attendance.class);
                query.setParameter("teamMemberId", teamMemberId);
                query.setParameter("startDate", startDate);
                query.setParameter("endDate", endDate);
                return query.getResultList();
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<List<Attendance>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}