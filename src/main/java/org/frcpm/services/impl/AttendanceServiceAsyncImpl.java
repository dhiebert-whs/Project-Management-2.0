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
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public List<Attendance> findByMember(TeamMember member) {
        return repository.findByMember(member);
    }

    @Override
    public Optional<Attendance> findByMeetingAndMember(Meeting meeting, TeamMember member) {
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
        
        return executeSync(em -> {
            Meeting meeting = em.find(Meeting.class, meetingId);
            if (meeting == null) {
                LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
                throw new IllegalArgumentException("Meeting not found with ID: " + meetingId);
            }
            
            TeamMember member = em.find(TeamMember.class, memberId);
            if (member == null) {
                LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
                throw new IllegalArgumentException("Team member not found with ID: " + memberId);
            }
            
            // Check if attendance record already exists
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a WHERE a.meeting.id = :meetingId AND a.member.id = :memberId", 
                    Attendance.class);
            query.setParameter("meetingId", meetingId);
            query.setParameter("memberId", memberId);
            List<Attendance> existingAttendance = query.getResultList();
            
            Attendance attendance;
            if (!existingAttendance.isEmpty()) {
                // Update existing record
                attendance = existingAttendance.get(0);
                attendance.setPresent(present);
            } else {
                // Create new record
                attendance = new Attendance(meeting, member, present);
            }
            
            em.merge(attendance);
            return attendance;
        });
    }

    @Override
    public Attendance updateAttendance(Long attendanceId, boolean present, LocalTime arrivalTime, LocalTime departureTime) {
        if (attendanceId == null) {
            throw new IllegalArgumentException("Attendance ID cannot be null");
        }
        
        return executeSync(em -> {
            Attendance attendance = em.find(Attendance.class, attendanceId);
            if (attendance == null) {
                LOGGER.log(Level.WARNING, "Attendance not found with ID: {0}", attendanceId);
                throw new IllegalArgumentException("Attendance not found with ID: " + attendanceId);
            }
            
            attendance.setPresent(present);
            
            if (present) {
                if (arrivalTime != null) {
                    attendance.setArrivalTime(arrivalTime);
                }
                
                if (departureTime != null) {
                    if (attendance.getArrivalTime() != null && departureTime.isBefore(attendance.getArrivalTime())) {
                        throw new IllegalArgumentException("Departure time cannot be before arrival time");
                    }
                    attendance.setDepartureTime(departureTime);
                }
            } else {
                // If not present, clear times
                attendance.setArrivalTime(null);
                attendance.setDepartureTime(null);
            }
            
            em.merge(attendance);
            return attendance;
        });
    }

    @Override
    public int recordAttendanceForMeeting(Long meetingId, List<Long> presentMemberIds) {
        if (meetingId == null) {
            throw new IllegalArgumentException("Meeting ID cannot be null");
        }
        
        return executeSync(em -> {
            Meeting meeting = em.find(Meeting.class, meetingId);
            if (meeting == null) {
                LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
                throw new IllegalArgumentException("Meeting not found with ID: " + meetingId);
            }
            
            // Get all team members
            TypedQuery<TeamMember> membersQuery = em.createQuery(
                    "SELECT tm FROM TeamMember tm", TeamMember.class);
            List<TeamMember> allMembers = membersQuery.getResultList();
            
            int count = 0;
            for (TeamMember member : allMembers) {
                boolean present = presentMemberIds != null && presentMemberIds.contains(member.getId());
                
                // Check if attendance record already exists
                TypedQuery<Attendance> query = em.createQuery(
                        "SELECT a FROM Attendance a WHERE a.meeting.id = :meetingId AND a.member.id = :memberId", 
                        Attendance.class);
                query.setParameter("meetingId", meetingId);
                query.setParameter("memberId", member.getId());
                List<Attendance> existingAttendance = query.getResultList();
                
                Attendance attendance;
                if (!existingAttendance.isEmpty()) {
                    // Update existing record
                    attendance = existingAttendance.get(0);
                    attendance.setPresent(present);
                } else {
                    // Create new record
                    attendance = new Attendance(meeting, member, present);
                    
                    // If present, set default arrival time to meeting start time
                    if (present) {
                        attendance.setArrivalTime(meeting.getStartTime());
                        attendance.setDepartureTime(meeting.getEndTime());
                    }
                }
                
                em.merge(attendance);
                count++;
            }
            
            return count;
        });
    }

    @Override
    public Map<String, Object> getAttendanceStatistics(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        
        return executeSync(em -> {
            Map<String, Object> statistics = new HashMap<>();
            
            TeamMember member = em.find(TeamMember.class, memberId);
            if (member == null) {
                LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
                return statistics;
            }
            
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a WHERE a.member.id = :memberId", 
                    Attendance.class);
            query.setParameter("memberId", memberId);
            List<Attendance> attendanceRecords = query.getResultList();
            
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
     * @param member the team member to find attendance for
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of attendance records
     */
    public CompletableFuture<List<Attendance>> findByMemberAsync(TeamMember member,
                                                              Consumer<List<Attendance>> onSuccess,
                                                              Consumer<Throwable> onFailure) {
        if (member == null) {
            CompletableFuture<List<Attendance>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Team member cannot be null"));
            return future;
        }

        return executeAsync("Find Attendance By Member: " + member.getId(), em -> {
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a WHERE a.member.id = :memberId", Attendance.class);
            query.setParameter("memberId", member.getId());
            return query.getResultList();
        }, onSuccess, onFailure);
    }

    /**
     * Asynchronously finds attendance records by meeting and member.
     * 
     * @param meeting the meeting
     * @param member the team member
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the optional attendance record
     */
    public CompletableFuture<Optional<Attendance>> findByMeetingAndMemberAsync(Meeting meeting, TeamMember member,
                                                                       Consumer<Optional<Attendance>> onSuccess,
                                                                       Consumer<Throwable> onFailure) {
        if (meeting == null || member == null) {
            CompletableFuture<Optional<Attendance>> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Meeting and member cannot be null"));
            return future;
        }

        return executeAsync("Find Attendance By Meeting And Member: " + meeting.getId() + " / " + member.getId(), em -> {
            TypedQuery<Attendance> query = em.createQuery(
                    "SELECT a FROM Attendance a WHERE a.meeting.id = :meetingId AND a.member.id = :memberId", 
                    Attendance.class);
            query.setParameter("meetingId", meeting.getId());
            query.setParameter("memberId", member.getId());
            return query.getResultStream().findFirst();
        }, onSuccess, onFailure);
    }

    /**
     * Asynchronously creates a new attendance record.
     * 
     * @param meetingId the meeting ID
     * @param memberId the team member ID
     * @param present whether the member is present
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created attendance record
     */
    public CompletableFuture<Attendance> createAttendanceAsync(Long meetingId, Long memberId, boolean present,
                                                      Consumer<Attendance> onSuccess,
                                                      Consumer<Throwable> onFailure) {
        try {
            if (meetingId == null) {
                throw new IllegalArgumentException("Meeting ID cannot be null");
            }
            
            if (memberId == null) {
                throw new IllegalArgumentException("Member ID cannot be null");
            }

            return executeAsync("Create Attendance: " + meetingId + " / " + memberId, em -> {
                Meeting meeting = em.find(Meeting.class, meetingId);
                if (meeting == null) {
                    LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
                    throw new IllegalArgumentException("Meeting not found with ID: " + meetingId);
                }
                
                TeamMember member = em.find(TeamMember.class, memberId);
                if (member == null) {
                    LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
                    throw new IllegalArgumentException("Team member not found with ID: " + memberId);
                }
                
                // Check if attendance record already exists
                TypedQuery<Attendance> query = em.createQuery(
                        "SELECT a FROM Attendance a WHERE a.meeting.id = :meetingId AND a.member.id = :memberId", 
                        Attendance.class);
                query.setParameter("meetingId", meetingId);
                query.setParameter("memberId", memberId);
                List<Attendance> existingAttendance = query.getResultList();
                
                Attendance attendance;
                if (!existingAttendance.isEmpty()) {
                    // Update existing record
                    attendance = existingAttendance.get(0);
                    attendance.setPresent(present);
                } else {
                    // Create new record
                    attendance = new Attendance(meeting, member, present);
                    
                    // If present, set default arrival time to meeting start time
                    if (present) {
                        attendance.setArrivalTime(meeting.getStartTime());
                        attendance.setDepartureTime(meeting.getEndTime());
                    }
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
     * Asynchronously updates an attendance record.
     * 
     * @param attendanceId the attendance ID
     * @param present whether the member is present
     * @param arrivalTime the arrival time
     * @param departureTime the departure time
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated attendance record
     */
    public CompletableFuture<Attendance> updateAttendanceAsync(Long attendanceId, boolean present,
                                                      LocalTime arrivalTime, LocalTime departureTime,
                                                      Consumer<Attendance> onSuccess,
                                                      Consumer<Throwable> onFailure) {
        try {
            if (attendanceId == null) {
                throw new IllegalArgumentException("Attendance ID cannot be null");
            }

            return executeAsync("Update Attendance: " + attendanceId, em -> {
                Attendance attendance = em.find(Attendance.class, attendanceId);
                if (attendance == null) {
                    LOGGER.log(Level.WARNING, "Attendance not found with ID: {0}", attendanceId);
                    throw new IllegalArgumentException("Attendance not found with ID: " + attendanceId);
                }
                
                attendance.setPresent(present);
                
                if (present) {
                    if (arrivalTime != null) {
                        attendance.setArrivalTime(arrivalTime);
                    }
                    
                    if (departureTime != null) {
                        if (attendance.getArrivalTime() != null && departureTime.isBefore(attendance.getArrivalTime())) {
                            throw new IllegalArgumentException("Departure time cannot be before arrival time");
                        }
                        attendance.setDepartureTime(departureTime);
                    }
                } else {
                    // If not present, clear times
                    attendance.setArrivalTime(null);
                    attendance.setDepartureTime(null);
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
     * Asynchronously records attendance for all team members in a meeting.
     * 
     * @param meetingId the meeting ID
     * @param presentMemberIds the IDs of present team members
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the number of attendance records created or updated
     */
    public CompletableFuture<Integer> recordAttendanceForMeetingAsync(Long meetingId, List<Long> presentMemberIds,
                                                              Consumer<Integer> onSuccess,
                                                              Consumer<Throwable> onFailure) {
        try {
            if (meetingId == null) {
                throw new IllegalArgumentException("Meeting ID cannot be null");
            }

            return executeAsync("Record Attendance For Meeting: " + meetingId, em -> {
                Meeting meeting = em.find(Meeting.class, meetingId);
                if (meeting == null) {
                    LOGGER.log(Level.WARNING, "Meeting not found with ID: {0}", meetingId);
                    throw new IllegalArgumentException("Meeting not found with ID: " + meetingId);
                }
                
                // Get all team members
                TypedQuery<TeamMember> membersQuery = em.createQuery(
                        "SELECT tm FROM TeamMember tm", TeamMember.class);
                List<TeamMember> allMembers = membersQuery.getResultList();
                
                int count = 0;
                for (TeamMember member : allMembers) {
                    boolean present = presentMemberIds != null && presentMemberIds.contains(member.getId());
                    
                    // Check if attendance record already exists
                    TypedQuery<Attendance> query = em.createQuery(
                            "SELECT a FROM Attendance a WHERE a.meeting.id = :meetingId AND a.member.id = :memberId", 
                            Attendance.class);
                    query.setParameter("meetingId", meetingId);
                    query.setParameter("memberId", member.getId());
                    List<Attendance> existingAttendance = query.getResultList();
                    
                    Attendance attendance;
                    if (!existingAttendance.isEmpty()) {
                        // Update existing record
                        attendance = existingAttendance.get(0);
                        attendance.setPresent(present);
                    } else {
                        // Create new record
                        attendance = new Attendance(meeting, member, present);
                        
                        // If present, set default arrival time to meeting start time
                        if (present) {
                            attendance.setArrivalTime(meeting.getStartTime());
                            attendance.setDepartureTime(meeting.getEndTime());
                        }
                    }
                    
                    em.merge(attendance);
                    count++;
                }
                
                return count;
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Integer> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Asynchronously gets attendance statistics for a team member.
     * 
     * @param memberId the team member ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a map containing attendance statistics
     */
    public CompletableFuture<Map<String, Object>> getAttendanceStatisticsAsync(Long memberId,
                                                                     Consumer<Map<String, Object>> onSuccess,
                                                                     Consumer<Throwable> onFailure) {
        try {
            if (memberId == null) {
                throw new IllegalArgumentException("Member ID cannot be null");
            }

            return executeAsync("Get Attendance Statistics: " + memberId, em -> {
                Map<String, Object> statistics = new HashMap<>();
                
                TeamMember member = em.find(TeamMember.class, memberId);
                if (member == null) {
                    LOGGER.log(Level.WARNING, "Team member not found with ID: {0}", memberId);
                    return statistics;
                }
                
                TypedQuery<Attendance> query = em.createQuery(
                        "SELECT a FROM Attendance a WHERE a.member.id = :memberId", 
                        Attendance.class);
                query.setParameter("memberId", memberId);
                List<Attendance> attendanceRecords = query.getResultList();
                
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
            }, onSuccess, onFailure);
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    // Additional methods for AttendanceServiceAsyncImpl.java

    /**
     * Asynchronously gets an attendance report for the specified date range.
     * 
     * @param startDate the start date
     * @param endDate the end date
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
}