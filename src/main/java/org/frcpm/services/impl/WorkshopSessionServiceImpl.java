package org.frcpm.services.impl;

import org.frcpm.models.*;
import org.frcpm.services.WorkshopSessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WorkshopSessionServiceImpl implements WorkshopSessionService {
    
    // Temporary in-memory storage - replace with actual repository
    private final List<WorkshopSession> sessions = new ArrayList<>();
    private Long nextId = 1L;
    
    @Override
    public List<WorkshopSession> findAll() {
        return new ArrayList<>(sessions);
    }
    
    @Override
    public long count() {
        return sessions.size();
    }
    
    @Override
    public WorkshopSession findById(Long id) {
        return sessions.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }
    
    @Override
    public WorkshopSession save(WorkshopSession session) {
        if (session.getId() == null) {
            session.setId(nextId++);
        }
        
        sessions.removeIf(s -> s.getId().equals(session.getId()));
        sessions.add(session);
        return session;
    }
    
    @Override
    public void delete(WorkshopSession session) {
        sessions.remove(session);
    }
    
    @Override
    public boolean deleteById(Long id) {
        return sessions.removeIf(s -> s.getId().equals(id));
    }
    
    @Override
    public List<WorkshopSession> findByProject(Project project) {
        return sessions.stream()
            .filter(s -> s.getProject().equals(project))
            .toList();
    }
    
    @Override
    public List<WorkshopSession> findByStatus(SessionStatus status) {
        return sessions.stream()
            .filter(s -> s.getStatus() == status)
            .toList();
    }
    
    @Override
    public List<WorkshopSession> findBySupervisingMentor(TeamMember mentor) {
        return sessions.stream()
            .filter(s -> mentor.equals(s.getSupervisingMentor()))
            .toList();
    }
    
    @Override
    public List<WorkshopSession> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return sessions.stream()
            .filter(s -> s.getStartTime().isAfter(startDate) && s.getStartTime().isBefore(endDate))
            .toList();
    }
    
    @Override
    public List<WorkshopSession> getActiveSessions() {
        return sessions.stream()
            .filter(WorkshopSession::isActive)
            .toList();
    }
    
    @Override
    public List<WorkshopSession> getTodaysSessions() {
        LocalDate today = LocalDate.now();
        return sessions.stream()
            .filter(s -> s.getStartTime().toLocalDate().equals(today))
            .toList();
    }
    
    @Override
    public List<WorkshopSession> getUpcomingSessions() {
        LocalDateTime now = LocalDateTime.now();
        return sessions.stream()
            .filter(s -> s.getStartTime().isAfter(now) && s.getStatus() == SessionStatus.PLANNED)
            .toList();
    }
    
    @Override
    public List<WorkshopSession> getCompletedSessions() {
        return sessions.stream()
            .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
            .toList();
    }
    
    @Override
    public List<WorkshopSession> getSessionsRequiringSafetyChecklist() {
        return sessions.stream()
            .filter(s -> s.getType().requiresSafetyChecklist())
            .toList();
    }
    
    @Override
    public List<WorkshopSession> getSessionsWithSafetyIncidents() {
        return sessions.stream()
            .filter(WorkshopSession::hasSafetyIncidents)
            .toList();
    }
    
    @Override
    public List<WorkshopSession> getOverdueSessions() {
        return sessions.stream()
            .filter(WorkshopSession::isOverdue)
            .toList();
    }
    
    @Override
    public WorkshopSession createSession(Project project, LocalDateTime startTime, 
                                       SessionType type, TeamMember mentor) {
        WorkshopSession session = new WorkshopSession(project, startTime, type, mentor);
        return save(session);
    }
    
    @Override
    public WorkshopSession startSession(Long sessionId) {
        WorkshopSession session = findById(sessionId);
        if (session != null) {
            session.startSession();
            return save(session);
        }
        return null;
    }
    
    @Override
    public WorkshopSession endSession(Long sessionId) {
        WorkshopSession session = findById(sessionId);
        if (session != null) {
            session.endSession();
            return save(session);
        }
        return null;
    }
    
    @Override
    public WorkshopSession cancelSession(Long sessionId) {
        WorkshopSession session = findById(sessionId);
        if (session != null) {
            session.setStatus(SessionStatus.CANCELLED);
            return save(session);
        }
        return null;
    }
    
    @Override
    public WorkshopSessionStats getSessionStats(LocalDate startDate, LocalDate endDate) {
        List<WorkshopSession> relevantSessions = sessions.stream()
            .filter(s -> !s.getStartTime().toLocalDate().isBefore(startDate) && 
                        !s.getStartTime().toLocalDate().isAfter(endDate))
            .toList();
        
        return calculateStats(relevantSessions);
    }
    
    @Override
    public WorkshopSessionStats getSessionStatsForProject(Long projectId, LocalDate startDate, LocalDate endDate) {
        List<WorkshopSession> relevantSessions = sessions.stream()
            .filter(s -> s.getProject().getId().equals(projectId) &&
                        !s.getStartTime().toLocalDate().isBefore(startDate) && 
                        !s.getStartTime().toLocalDate().isAfter(endDate))
            .toList();
        
        return calculateStats(relevantSessions);
    }
    
    private WorkshopSessionStats calculateStats(List<WorkshopSession> sessions) {
        int totalSessions = sessions.size();
        int completedSessions = (int) sessions.stream()
            .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
            .count();
        int cancelledSessions = (int) sessions.stream()
            .filter(s -> s.getStatus() == SessionStatus.CANCELLED)
            .count();
        
        long totalHours = sessions.stream()
            .mapToLong(WorkshopSession::getDurationMinutes)
            .sum() / 60;
        
        int totalAttendees = sessions.stream()
            .mapToInt(WorkshopSession::getAttendanceCount)
            .sum();
        
        int safetyIncidents = (int) sessions.stream()
            .filter(WorkshopSession::hasSafetyIncidents)
            .count();
        
        return new WorkshopSessionStats(totalSessions, completedSessions, cancelledSessions,
                                       totalHours, totalAttendees, safetyIncidents);
    }
}