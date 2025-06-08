// src/main/java/org/frcpm/services/impl/TestableAttendanceServiceAsyncImpl.java

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A testable async implementation of AttendanceService that accepts injected repositories
 * for better unit testing. This implementation provides async methods that can be stubbed
 * in tests without requiring spy patterns.
 */
public class TestableAttendanceServiceAsyncImpl extends TestableAttendanceServiceImpl {
    
    private static final Logger LOGGER = Logger.getLogger(TestableAttendanceServiceAsyncImpl.class.getName());
    
    /**
     * Constructor for MVVMFx injection.
     * Uses ServiceLocator for default initialization.
     */
    public TestableAttendanceServiceAsyncImpl() {
        super();
    }
    
    /**
     * Constructor for manual/test injection.
     * 
     * @param attendanceRepository the attendance repository
     * @param meetingRepository the meeting repository
     * @param teamMemberRepository the team member repository
     */
    public TestableAttendanceServiceAsyncImpl(
            AttendanceRepository attendanceRepository,
            MeetingRepository meetingRepository,
            TeamMemberRepository teamMemberRepository) {
        super(attendanceRepository, meetingRepository, teamMemberRepository);
    }
    
    // ASYNC METHODS - These can be stubbed directly in tests without spies
    
    /**
     * Finds all attendance records asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of attendance records
     */
    public CompletableFuture<List<Attendance>> findAllAsync(Consumer<List<Attendance>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Attendance>> future = new CompletableFuture<>();
        
        try {
            List<Attendance> result = findAll();
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Finds an attendance record by ID asynchronously.
     * 
     * @param id the attendance ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the attendance record
     */
    public CompletableFuture<Attendance> findByIdAsync(Long id, Consumer<Attendance> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Attendance> future = new CompletableFuture<>();
        
        try {
            Attendance result = findById(id);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Saves an attendance record asynchronously.
     * 
     * @param entity the attendance record to save
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the saved attendance record
     */
    public CompletableFuture<Attendance> saveAsync(Attendance entity, Consumer<Attendance> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Attendance> future = new CompletableFuture<>();
        
        try {
            Attendance result = save(entity);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Deletes an attendance record asynchronously.
     * 
     * @param entity the attendance record to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed when the attendance record is deleted
     */
    public CompletableFuture<Void> deleteAsync(Attendance entity, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        try {
            delete(entity);
            if (onSuccess != null) {
                onSuccess.accept(null);
            }
            future.complete(null);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Deletes an attendance record by ID asynchronously.
     * 
     * @param id the ID of the attendance record to delete
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with a boolean indicating success
     */
    public CompletableFuture<Boolean> deleteByIdAsync(Long id, Consumer<Boolean> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        try {
            boolean result = deleteById(id);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Counts all attendance records asynchronously.
     * 
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the count of attendance records
     */
    public CompletableFuture<Long> countAsync(Consumer<Long> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        
        try {
            long result = count();
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Finds attendance records by meeting asynchronously.
     * 
     * @param meeting the meeting
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of attendance records
     */
    public CompletableFuture<List<Attendance>> findByMeetingAsync(Meeting meeting, Consumer<List<Attendance>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Attendance>> future = new CompletableFuture<>();
        
        try {
            List<Attendance> result = findByMeeting(meeting);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Finds attendance records by member asynchronously.
     * 
     * @param member the team member
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the list of attendance records
     */
    public CompletableFuture<List<Attendance>> findByMemberAsync(TeamMember member, Consumer<List<Attendance>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<List<Attendance>> future = new CompletableFuture<>();
        
        try {
            List<Attendance> result = findByMember(member);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Finds attendance record by meeting and member asynchronously.
     * 
     * @param meeting the meeting
     * @param member the team member
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the attendance record
     */
    public CompletableFuture<Optional<Attendance>> findByMeetingAndMemberAsync(Meeting meeting, TeamMember member, Consumer<Optional<Attendance>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Optional<Attendance>> future = new CompletableFuture<>();
        
        try {
            Optional<Attendance> result = findByMeetingAndMember(meeting, member);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Creates an attendance record asynchronously.
     * 
     * @param meetingId the meeting ID
     * @param memberId the member ID
     * @param present whether the member is present
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the created attendance record
     */
    public CompletableFuture<Attendance> createAttendanceAsync(Long meetingId, Long memberId, boolean present, Consumer<Attendance> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Attendance> future = new CompletableFuture<>();
        
        try {
            Attendance result = createAttendance(meetingId, memberId, present);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Updates an attendance record asynchronously.
     * 
     * @param attendanceId the attendance ID
     * @param present whether the member is present
     * @param arrivalTime the arrival time
     * @param departureTime the departure time
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the updated attendance record
     */
    public CompletableFuture<Attendance> updateAttendanceAsync(Long attendanceId, boolean present, LocalTime arrivalTime, LocalTime departureTime, Consumer<Attendance> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Attendance> future = new CompletableFuture<>();
        
        try {
            Attendance result = updateAttendance(attendanceId, present, arrivalTime, departureTime);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Records attendance for a meeting asynchronously.
     * 
     * @param meetingId the meeting ID
     * @param presentMemberIds the list of present member IDs
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the number of records created/updated
     */
    public CompletableFuture<Integer> recordAttendanceForMeetingAsync(Long meetingId, List<Long> presentMemberIds, Consumer<Integer> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        
        try {
            int result = recordAttendanceForMeeting(meetingId, presentMemberIds);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Gets attendance statistics asynchronously.
     * 
     * @param memberId the member ID
     * @param onSuccess the callback to run on success
     * @param onFailure the callback to run on failure
     * @return a CompletableFuture that will be completed with the attendance statistics
     */
    public CompletableFuture<Map<String, Object>> getAttendanceStatisticsAsync(Long memberId, Consumer<Map<String, Object>> onSuccess, Consumer<Throwable> onFailure) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        try {
            Map<String, Object> result = getAttendanceStatistics(memberId);
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            future.complete(result);
        } catch (Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
            future.completeExceptionally(e);
        }
        
        return future;
    }
}