package org.frcpm.services;

import org.frcpm.models.WorkshopSession;
import org.frcpm.models.SessionStatus;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkshopSessionService extends Service<WorkshopSession, Long> {
    
    /**
     * Find workshop sessions by project
     */
    List<WorkshopSession> findByProject(Project project);
    
    /**
     * Find workshop sessions by status
     */
    List<WorkshopSession> findByStatus(SessionStatus status);
    
    /**
     * Find workshop sessions by supervising mentor
     */
    List<WorkshopSession> findBySupervisingMentor(TeamMember mentor);
    
    /**
     * Find workshop sessions by date range
     */
    List<WorkshopSession> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get active workshop sessions
     */
    List<WorkshopSession> getActiveSessions();
    
    /**
     * Get today's workshop sessions
     */
    List<WorkshopSession> getTodaysSessions();
    
    /**
     * Get upcoming workshop sessions
     */
    List<WorkshopSession> getUpcomingSessions();
    
    /**
     * Get completed workshop sessions
     */
    List<WorkshopSession> getCompletedSessions();
    
    /**
     * Get sessions requiring safety checklist
     */
    List<WorkshopSession> getSessionsRequiringSafetyChecklist();
    
    /**
     * Get sessions with safety incidents
     */
    List<WorkshopSession> getSessionsWithSafetyIncidents();
    
    /**
     * Get overdue sessions
     */
    List<WorkshopSession> getOverdueSessions();
    
    /**
     * Create a new workshop session
     */
    WorkshopSession createSession(Project project, LocalDateTime startTime, 
                                 org.frcpm.models.SessionType type, TeamMember mentor);
    
    /**
     * Start a workshop session
     */
    WorkshopSession startSession(Long sessionId);
    
    /**
     * End a workshop session
     */
    WorkshopSession endSession(Long sessionId);
    
    /**
     * Cancel a workshop session
     */
    WorkshopSession cancelSession(Long sessionId);
    
    /**
     * Get session statistics
     */
    WorkshopSessionStats getSessionStats(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get session statistics for a specific project
     */
    WorkshopSessionStats getSessionStatsForProject(Long projectId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Inner class for session statistics
     */
    class WorkshopSessionStats {
        private int totalSessions;
        private int completedSessions;
        private int cancelledSessions;
        private long totalHours;
        private int totalAttendees;
        private int safetyIncidents;
        
        public WorkshopSessionStats() {}
        
        public WorkshopSessionStats(int totalSessions, int completedSessions, int cancelledSessions,
                                   long totalHours, int totalAttendees, int safetyIncidents) {
            this.totalSessions = totalSessions;
            this.completedSessions = completedSessions;
            this.cancelledSessions = cancelledSessions;
            this.totalHours = totalHours;
            this.totalAttendees = totalAttendees;
            this.safetyIncidents = safetyIncidents;
        }
        
        // Getters and setters
        public int getTotalSessions() { return totalSessions; }
        public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
        
        public int getCompletedSessions() { return completedSessions; }
        public void setCompletedSessions(int completedSessions) { this.completedSessions = completedSessions; }
        
        public int getCancelledSessions() { return cancelledSessions; }
        public void setCancelledSessions(int cancelledSessions) { this.cancelledSessions = cancelledSessions; }
        
        public long getTotalHours() { return totalHours; }
        public void setTotalHours(long totalHours) { this.totalHours = totalHours; }
        
        public int getTotalAttendees() { return totalAttendees; }
        public void setTotalAttendees(int totalAttendees) { this.totalAttendees = totalAttendees; }
        
        public int getSafetyIncidents() { return safetyIncidents; }
        public void setSafetyIncidents(int safetyIncidents) { this.safetyIncidents = safetyIncidents; }
        
        public double getAverageAttendeesPerSession() {
            return totalSessions > 0 ? (double) totalAttendees / totalSessions : 0.0;
        }
        
        public double getCompletionRate() {
            return totalSessions > 0 ? (double) completedSessions / totalSessions * 100.0 : 0.0;
        }
    }
}