// src/main/java/org/frcpm/services/impl/AttendanceServiceImpl.java (Enhanced with WebSocket)

package org.frcpm.services.impl;

import org.frcpm.web.websocket.AttendanceController;

import org.frcpm.models.Attendance;
import org.frcpm.models.Meeting;
import org.frcpm.models.TeamMember;
import org.frcpm.models.User;
import org.frcpm.repositories.spring.AttendanceRepository;
import org.frcpm.repositories.spring.MeetingRepository;
import org.frcpm.repositories.spring.TeamMemberRepository;
import org.frcpm.services.AttendanceService;
import org.frcpm.events.WebSocketEventPublisher;
import org.frcpm.web.dto.AttendanceUpdateMessage;
import org.frcpm.web.dto.TeamPresenceMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Enhanced AttendanceService with real-time WebSocket event publishing.
 * 
 * 🚀 PHASE 2D: AttendanceService WebSocket Integration
 * 
 * Preserves all existing functionality while adding:
 * - Real-time check-in/check-out notifications
 * - Live workshop presence summaries
 * - Attendance event broadcasting
 * - Team coordination features
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2D - Real-time Attendance Features
 */
@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {
    
    private static final Logger LOGGER = Logger.getLogger(AttendanceServiceImpl.class.getName());
    
    private final AttendanceRepository attendanceRepository;
    private final MeetingRepository meetingRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final WebSocketEventPublisher webSocketEventPublisher;
    private final AttendanceController attendanceController;
    
    /**
     * Constructor injection with @Lazy for WebSocketEventPublisher to avoid circular dependencies.
     */
    @Autowired
    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                MeetingRepository meetingRepository,
                                TeamMemberRepository teamMemberRepository,
                                @Lazy WebSocketEventPublisher webSocketEventPublisher,
                                @Lazy AttendanceController attendanceController) {
        this.attendanceRepository = attendanceRepository;
        this.meetingRepository = meetingRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.webSocketEventPublisher = webSocketEventPublisher;
        this.attendanceController = attendanceController;
    }
    
    // ========================================
    // Basic CRUD Operations (PRESERVED - No Changes)
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
        boolean isNewAttendance = entity.getId() == null;
        boolean wasPresent = false;
        
        // Track previous state for change detection
        if (!isNewAttendance) {
            Optional<Attendance> existing = attendanceRepository.findById(entity.getId());
            wasPresent = existing.map(Attendance::isPresent).orElse(false);
        }
        
        Attendance savedAttendance = attendanceRepository.save(entity);
        
        // Publish real-time events based on changes
        User currentUser = getCurrentUser();
        if (isNewAttendance) {
            publishAttendanceCreated(savedAttendance, currentUser);
        } else if (wasPresent != entity.isPresent()) {
            publishAttendanceUpdated(savedAttendance, currentUser);
        }
        
        // Always publish updated workshop presence summary
        publishWorkshopPresenceUpdate(savedAttendance.getMeeting());
        
        return savedAttendance;
    }
    
    @Override
    public void delete(Attendance entity) {
        if (entity != null) {
            Meeting meeting = entity.getMeeting();
            attendanceRepository.delete(entity);
            
            // Publish presence update after deletion
            publishWorkshopPresenceUpdate(meeting);
        }
    }
    
    @Override
    public boolean deleteById(Long id) {
        Optional<Attendance> attendance = attendanceRepository.findById(id);
        if (attendance.isPresent()) {
            Meeting meeting = attendance.get().getMeeting();
            attendanceRepository.deleteById(id);
            
            // Publish presence update after deletion
            publishWorkshopPresenceUpdate(meeting);
            return true;
        }
        return false;
    }
    
    @Override
    public long count() {
        return attendanceRepository.count();
    }
    
    // ========================================
    // Business Operations (PRESERVED with Real-time Enhancements)
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
            return updateExistingAttendance(existing.get(), present, null, null);
        }
        
        // Create new attendance
        Attendance attendance = new Attendance(meeting, member, present);
        if (present) {
            attendance.setArrivalTime(LocalTime.now()); // Use current time for real-time check-in
        }
        
        return save(attendance); // This will trigger WebSocket events
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
        
        return updateExistingAttendance(attendance, present, arrivalTime, departureTime);
    }
    
    /**
     * Enhanced workshop check-in method with real-time events.
     * 
     * @param meetingId Meeting ID
     * @param memberId Member ID
     * @param arrivalTime Arrival time (null = current time)
     * @return Updated attendance record
     */
    public Attendance checkInMember(Long meetingId, Long memberId, LocalTime arrivalTime) {
        LocalTime actualArrivalTime = arrivalTime != null ? arrivalTime : LocalTime.now();
        
        Attendance attendance = createAttendance(meetingId, memberId, true);
        attendance.setArrivalTime(actualArrivalTime);
        
        Attendance savedAttendance = attendanceRepository.save(attendance);
        
        // Publish real-time check-in event
        User currentUser = getCurrentUser();
        publishCheckInEvent(savedAttendance, currentUser);
        
        return savedAttendance;
    }
    
    /**
     * Enhanced workshop check-out method with real-time events.
     * 
     * @param meetingId Meeting ID
     * @param memberId Member ID
     * @param departureTime Departure time (null = current time)
     * @return Updated attendance record
     */
    public Attendance checkOutMember(Long meetingId, Long memberId, LocalTime departureTime) {
        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("Meeting not found"));
        
        TeamMember member = teamMemberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        
        Optional<Attendance> existingOpt = attendanceRepository.findByMeetingAndMember(meeting, member);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("No attendance record found for check-out");
        }
        
        Attendance attendance = existingOpt.get();
        LocalTime actualDepartureTime = departureTime != null ? departureTime : LocalTime.now();
        
        attendance.setDepartureTime(actualDepartureTime);
        // Note: Keep present=true but add departure time for duration tracking
        
        Attendance savedAttendance = attendanceRepository.save(attendance);
        
        // Publish real-time check-out event
        User currentUser = getCurrentUser();
        publishCheckOutEvent(savedAttendance, currentUser);
        
        // FIX: Also publish workshop presence update after check-out
        publishWorkshopPresenceUpdate(savedAttendance.getMeeting());
        
        return savedAttendance;
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
                boolean wasPresent = attendance.isPresent();
                attendance.setPresent(present);
                
                // Only update times if status changed
                if (present && !wasPresent) {
                    attendance.setArrivalTime(LocalTime.now());
                } else if (!present && wasPresent) {
                    attendance.setDepartureTime(LocalTime.now());
                }
            } else {
                attendance = new Attendance(meeting, member, present);
                if (present) {
                    attendance.setArrivalTime(LocalTime.now());
                }
            }
            
            attendanceRepository.save(attendance);
            count++;
        }
        
        // Publish bulk attendance update
        publishWorkshopPresenceUpdate(meeting);
        
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
    // NEW: Real-time WebSocket Event Publishing
    // ========================================
    
    private void publishAttendanceCreated(Attendance attendance, User updatedByUser) {
        try {
            String eventType = attendance.isPresent() ? "CHECK_IN" : "MARKED_ABSENT";
            String updatedBy = updatedByUser != null ? updatedByUser.getFullName() : "System";
            
            AttendanceUpdateMessage message = AttendanceUpdateMessage.checkIn(
                attendance.getId(),
                attendance.getMeeting().getId(),
                attendance.getMember().getId(),
                attendance.getMember().getDisplayName(),
                attendance.getMember().getUsername(),
                attendance.getArrivalTime(),
                updatedBy
            );
            
            // Add subteam context
            if (attendance.getMember().getSubteam() != null) {
                message.setSubteamName(attendance.getMember().getSubteam().getName());
                message.setSubteamColorCode(attendance.getMember().getSubteam().getColorCode());
            }
            
            message.setEventType(eventType);
            message.setSessionInfo("Workshop Session");
            
            // Broadcast to meeting/project subscribers
            broadcastAttendanceUpdate(message);
            
            LOGGER.info(String.format("Published attendance creation: %s - %s", 
                                    attendance.getMember().getDisplayName(), eventType));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing attendance creation", e);
        }
    }
    
    private void publishAttendanceUpdated(Attendance attendance, User updatedByUser) {
        try {
            String eventType = attendance.isPresent() ? "CHECK_IN" : "CHECK_OUT";
            String updatedBy = updatedByUser != null ? updatedByUser.getFullName() : "System";
            
            AttendanceUpdateMessage message = new AttendanceUpdateMessage(
                attendance.getId(),
                attendance.getMeeting().getId(),
                attendance.getMember().getId(),
                attendance.getMember().getDisplayName(),
                eventType,
                attendance.isPresent(),
                updatedBy
            );
            
            message.setMemberUsername(attendance.getMember().getUsername());
            message.setArrivalTime(attendance.getArrivalTime());
            message.setDepartureTime(attendance.getDepartureTime());
            
            // Add subteam context
            if (attendance.getMember().getSubteam() != null) {
                message.setSubteamName(attendance.getMember().getSubteam().getName());
                message.setSubteamColorCode(attendance.getMember().getSubteam().getColorCode());
            }
            
            message.setSessionInfo("Workshop Session");
            
            // Broadcast to meeting/project subscribers
            broadcastAttendanceUpdate(message);
            
            LOGGER.info(String.format("Published attendance update: %s - %s", 
                                    attendance.getMember().getDisplayName(), eventType));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing attendance update", e);
        }
    }
    
    private void publishCheckInEvent(Attendance attendance, User updatedByUser) {
        try {
            String updatedBy = updatedByUser != null ? updatedByUser.getFullName() : "System";
            
            // Check if this is a late arrival
            LocalTime scheduledStart = attendance.getMeeting().getStartTime();
            LocalTime actualArrival = attendance.getArrivalTime();
            boolean isLate = actualArrival != null && scheduledStart != null && 
                           actualArrival.isAfter(scheduledStart.plusMinutes(15)); // 15 min grace period
            
            AttendanceUpdateMessage message;
            if (isLate) {
                message = AttendanceUpdateMessage.lateArrival(
                    attendance.getId(),
                    attendance.getMeeting().getId(),
                    attendance.getMember().getId(),
                    attendance.getMember().getDisplayName(),
                    actualArrival,
                    updatedBy
                );
            } else {
                message = AttendanceUpdateMessage.checkIn(
                    attendance.getId(),
                    attendance.getMeeting().getId(),
                    attendance.getMember().getId(),
                    attendance.getMember().getDisplayName(),
                    attendance.getMember().getUsername(),
                    actualArrival,
                    updatedBy
                );
            }
            
            // Add context
            if (attendance.getMember().getSubteam() != null) {
                message.setSubteamName(attendance.getMember().getSubteam().getName());
                message.setSubteamColorCode(attendance.getMember().getSubteam().getColorCode());
            }
            
            message.setSessionInfo("Workshop Session");
            message.setCurrentLocation("Main Workshop");
            
            // Broadcast to meeting/project subscribers
            broadcastAttendanceUpdate(message);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing check-in event", e);
        }
    }
    
    private void publishCheckOutEvent(Attendance attendance, User updatedByUser) {
        try {
            String updatedBy = updatedByUser != null ? updatedByUser.getFullName() : "System";
            
            AttendanceUpdateMessage message = AttendanceUpdateMessage.checkOut(
                attendance.getId(),
                attendance.getMeeting().getId(),
                attendance.getMember().getId(),
                attendance.getMember().getDisplayName(),
                attendance.getMember().getUsername(),
                attendance.getDepartureTime(),
                updatedBy
            );
            
            // Add context
            if (attendance.getMember().getSubteam() != null) {
                message.setSubteamName(attendance.getMember().getSubteam().getName());
                message.setSubteamColorCode(attendance.getMember().getSubteam().getColorCode());
            }
            
            message.setSessionInfo("Workshop Session");
            
            // Broadcast to meeting/project subscribers
            broadcastAttendanceUpdate(message);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing check-out event", e);
        }
    }
    
    private void publishWorkshopPresenceUpdate(Meeting meeting) {
        try {
            List<Attendance> allAttendance = attendanceRepository.findByMeeting(meeting);
            
            // Calculate overall statistics
            int totalPresent = (int) allAttendance.stream().filter(Attendance::isPresent).count();
            int totalExpected = allAttendance.size();
            
            // Create presence message
            TeamPresenceMessage presenceMessage = new TeamPresenceMessage(
                meeting.getId(),
                "Workshop Session"
            );
            
            presenceMessage.setTotalPresent(totalPresent);
            presenceMessage.setTotalExpected(totalExpected);
            presenceMessage.calculateAttendancePercentage();
            presenceMessage.setWorkshopStatus("ACTIVE");
            presenceMessage.setSessionStartTime(meeting.getStartTime());
            presenceMessage.setSessionEndTime(meeting.getEndTime());
            
            // Calculate subteam breakdown
            Map<String, List<Attendance>> bySubteam = allAttendance.stream()
                .filter(a -> a.getMember().getSubteam() != null)
                .collect(Collectors.groupingBy(a -> a.getMember().getSubteam().getName()));
            
            bySubteam.forEach((subteamName, attendances) -> {
                int present = (int) attendances.stream().filter(Attendance::isPresent).count();
                int total = attendances.size();
                String colorCode = attendances.get(0).getMember().getSubteam().getColorCode();
                
                presenceMessage.addSubteamPresence(subteamName, colorCode, present, total);
            });
            
            // Broadcast to meeting/project subscribers
            broadcastPresenceUpdate(presenceMessage);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing workshop presence update", e);
        }
    }
    
    private void broadcastAttendanceUpdate(AttendanceUpdateMessage message) {
        try {
            // Use the dedicated AttendanceController for broadcasting
            attendanceController.broadcastAttendanceUpdate(message);
            
            // Also publish to general activity stream via WebSocketEventPublisher
            webSocketEventPublisher.publishSystemAlert(
                String.format("%s: %s", message.getEventType(), message.getMemberName()),
                "INFO"
            );
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting attendance update", e);
        }
    }
    
    private void broadcastPresenceUpdate(TeamPresenceMessage message) {
        try {
            // Use the dedicated AttendanceController for broadcasting
            attendanceController.broadcastPresenceUpdate(message);
            
            // Also publish summary to activity stream
            webSocketEventPublisher.publishSystemAlert(
                String.format("Workshop Update: %d/%d present (%d%%)", 
                             message.getTotalPresent(), 
                             message.getTotalExpected(),
                             message.getAttendancePercentage().intValue()),
                "INFO"
            );
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting presence update", e);
        }
    }
    
    // ========================================
    // Helper Methods
    // ========================================
    
    private Attendance updateExistingAttendance(Attendance attendance, boolean present, 
                                               LocalTime arrivalTime, LocalTime departureTime) {
        attendance.setPresent(present);
        
        if (present) {
            if (arrivalTime != null) {
                attendance.setArrivalTime(arrivalTime);
            } else if (attendance.getArrivalTime() == null) {
                attendance.setArrivalTime(LocalTime.now());
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
        
        return save(attendance); // This will trigger WebSocket events
    }
    
    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof org.frcpm.security.UserPrincipal) {
                org.frcpm.security.UserPrincipal userPrincipal = 
                    (org.frcpm.security.UserPrincipal) auth.getPrincipal();
                return userPrincipal.getUser();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not get current user", e);
        }
        return null;
    }
    
    // ========================================
    // Async Operations (PRESERVED)
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

    private void publishCheckInEventEnhanced(Attendance attendance, User updatedByUser) {
        try {
            String updatedBy = updatedByUser != null ? updatedByUser.getFullName() : "System";
            
            // Check if this is a late arrival
            LocalTime scheduledStart = attendance.getMeeting().getStartTime();
            LocalTime actualArrival = attendance.getArrivalTime();
            boolean isLate = actualArrival != null && scheduledStart != null && 
                           actualArrival.isAfter(scheduledStart.plusMinutes(15)); // 15 min grace period
            
            AttendanceUpdateMessage message;
            if (isLate) {
                message = AttendanceUpdateMessage.lateArrival(
                    attendance.getId(),
                    attendance.getMeeting().getId(),
                    attendance.getMember().getId(),
                    attendance.getMember().getDisplayName(),
                    actualArrival,
                    updatedBy
                );
                
                // Use special late arrival broadcasting
                attendanceController.broadcastLateArrival(message);
            } else {
                message = AttendanceUpdateMessage.checkIn(
                    attendance.getId(),
                    attendance.getMeeting().getId(),
                    attendance.getMember().getId(),
                    attendance.getMember().getDisplayName(),
                    attendance.getMember().getUsername(),
                    actualArrival,
                    updatedBy
                );
                
                // Standard check-in broadcasting
                broadcastAttendanceUpdate(message);
            }
            
            // Add context information
            if (attendance.getMember().getSubteam() != null) {
                message.setSubteamName(attendance.getMember().getSubteam().getName());
                message.setSubteamColorCode(attendance.getMember().getSubteam().getColorCode());
            }
            
            message.setSessionInfo("Workshop Session");
            message.setCurrentLocation("Main Workshop");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error publishing enhanced check-in event", e);
        }
    }
}