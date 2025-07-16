package org.frcpm.web.controllers;

import org.frcpm.models.*;
import org.frcpm.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {
    
    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);
    
    private final WorkshopSessionService workshopSessionService;
    private final TeamMemberService teamMemberService;
    private final QRCodeService qrCodeService;
    private final ProjectService projectService;
    
    public AttendanceController(WorkshopSessionService workshopSessionService,
                               TeamMemberService teamMemberService,
                               QRCodeService qrCodeService,
                               ProjectService projectService) {
        this.workshopSessionService = workshopSessionService;
        this.teamMemberService = teamMemberService;
        this.qrCodeService = qrCodeService;
        this.projectService = projectService;
    }
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String attendanceHome(Model model) {
        List<WorkshopSession> activeSessions = workshopSessionService.getActiveSessions();
        List<WorkshopSession> todaysSessions = workshopSessionService.getTodaysSessions();
        List<Project> projects = projectService.findAll();
        
        model.addAttribute("activeSessions", activeSessions);
        model.addAttribute("todaysSessions", todaysSessions);
        model.addAttribute("projects", projects);
        
        return "attendance/home";
    }
    
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public String sessionAttendance(@PathVariable Long sessionId, Model model) {
        WorkshopSession session = workshopSessionService.findById(sessionId);
        
        if (session == null) {
            return "redirect:/attendance";
        }
        
        // Generate QR codes for check-in and check-out
        String checkInQR = qrCodeService.generateSessionCheckInQR(sessionId.toString(), 60); // 1 hour validity
        String checkOutQR = qrCodeService.generateSessionCheckOutQR(sessionId.toString(), 60);
        
        model.addAttribute("session", session);
        model.addAttribute("checkInQR", checkInQR);
        model.addAttribute("checkOutQR", checkOutQR);
        model.addAttribute("attendances", session.getAttendances());
        model.addAttribute("attendanceCount", session.getAttendanceCount());
        
        return "attendance/session";
    }
    
    @GetMapping("/scanner")
    @PreAuthorize("isAuthenticated()")
    public String qrScanner(@RequestParam(required = false) Long sessionId, Model model) {
        if (sessionId != null) {
            WorkshopSession session = workshopSessionService.findById(sessionId);
            if (session != null) {
                model.addAttribute("session", session);
            }
        }
        
        List<WorkshopSession> activeSessions = workshopSessionService.getActiveSessions();
        model.addAttribute("activeSessions", activeSessions);
        
        return "attendance/scanner";
    }
    
    @GetMapping("/manual")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public String manualAttendance(@RequestParam(required = false) Long sessionId, Model model) {
        if (sessionId != null) {
            WorkshopSession session = workshopSessionService.findById(sessionId);
            if (session != null) {
                model.addAttribute("session", session);
                model.addAttribute("attendances", session.getAttendances());
            }
        }
        
        List<WorkshopSession> activeSessions = workshopSessionService.getActiveSessions();
        List<TeamMember> teamMembers = teamMemberService.findAll();
        
        model.addAttribute("activeSessions", activeSessions);
        model.addAttribute("teamMembers", teamMembers);
        
        return "attendance/manual";
    }
    
    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public String attendanceReports(@RequestParam(required = false) Long projectId,
                                   @RequestParam(required = false) Long memberId,
                                   Model model) {
        List<Project> projects = projectService.findAll();
        List<TeamMember> teamMembers = teamMemberService.findAll();
        
        model.addAttribute("projects", projects);
        model.addAttribute("teamMembers", teamMembers);
        
        if (projectId != null) {
            // Get project-specific attendance data
            List<WorkshopSession> projectSessions = workshopSessionService.findByProject(
                projectService.findById(projectId)
            );
            model.addAttribute("projectSessions", projectSessions);
            model.addAttribute("selectedProject", projectService.findById(projectId));
        }
        
        if (memberId != null) {
            // Get member-specific attendance data
            TeamMember member = teamMemberService.findById(memberId);
            if (member != null) {
                // TODO: Implement attendance history for team member
                model.addAttribute("selectedMember", member);
            }
        }
        
        return "attendance/reports";
    }
    
    // API Endpoints for mobile and AJAX interactions
    
    @PostMapping("/api/checkin")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> checkIn(@RequestBody Map<String, Object> checkInData) {
        try {
            String sessionId = (String) checkInData.get("sessionId");
            String memberId = (String) checkInData.get("memberId");
            String method = (String) checkInData.get("method"); // "QR", "MANUAL", "NFC"
            
            if (sessionId == null || memberId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Session ID and Member ID are required"));
            }
            
            WorkshopSession session = workshopSessionService.findById(Long.parseLong(sessionId));
            TeamMember member = teamMemberService.findById(Long.parseLong(memberId));
            
            if (session == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Session not found"));
            }
            
            if (member == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Team member not found"));
            }
            
            // Create attendance record
            SessionAttendance attendance = new SessionAttendance(session, member);
            attendance.checkIn(method != null ? method : "MANUAL");
            
            // Add to session
            session.getAttendances().add(attendance);
            workshopSessionService.save(session);
            
            logger.info("Check-in recorded: Member {} for Session {} via {}", 
                       member.getFullName(), session.getId(), method);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Check-in recorded successfully",
                "memberName", member.getFullName(),
                "sessionTitle", getSessionDisplayName(session),
                "checkInTime", attendance.getCheckInTime(),
                "attendanceCount", session.getAttendanceCount()
            ));
            
        } catch (Exception e) {
            logger.error("Error processing check-in: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process check-in"));
        }
    }
    
    @PostMapping("/api/checkout")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> checkOut(@RequestBody Map<String, Object> checkOutData) {
        try {
            String sessionId = (String) checkOutData.get("sessionId");
            String memberId = (String) checkOutData.get("memberId");
            String method = (String) checkOutData.get("method");
            
            if (sessionId == null || memberId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Session ID and Member ID are required"));
            }
            
            WorkshopSession session = workshopSessionService.findById(Long.parseLong(sessionId));
            TeamMember member = teamMemberService.findById(Long.parseLong(memberId));
            
            if (session == null || member == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Session or member not found"));
            }
            
            // Find existing attendance record
            SessionAttendance attendance = session.getAttendances().stream()
                .filter(a -> a.getMember().equals(member) && a.getCheckOutTime() == null)
                .findFirst()
                .orElse(null);
            
            if (attendance == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No active check-in found for this member"));
            }
            
            // Update attendance with check-out
            attendance.checkOut();
            
            workshopSessionService.save(session);
            
            logger.info("Check-out recorded: Member {} from Session {} via {}", 
                       member.getFullName(), session.getId(), method);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Check-out recorded successfully",
                "memberName", member.getFullName(),
                "sessionTitle", getSessionDisplayName(session),
                "checkOutTime", attendance.getCheckOutTime(),
                "duration", attendance.getAttendanceDurationMinutes() + " minutes"
            ));
            
        } catch (Exception e) {
            logger.error("Error processing check-out: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process check-out"));
        }
    }
    
    @PostMapping("/api/qr/process")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> processQRCode(@RequestBody Map<String, Object> qrData) {
        try {
            String qrContent = (String) qrData.get("qrContent");
            String memberId = (String) qrData.get("memberId");
            
            if (qrContent == null || memberId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "QR content and member ID are required"));
            }
            
            // Parse QR code content (should be JSON with action, session, token, timestamp)
            // For now, we'll assume a simple format - in production, use a JSON parser
            if (qrContent.contains("CHECK_IN")) {
                return processQRCheckIn(qrContent, memberId);
            } else if (qrContent.contains("CHECK_OUT")) {
                return processQRCheckOut(qrContent, memberId);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid QR code format"));
            }
            
        } catch (Exception e) {
            logger.error("Error processing QR code: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process QR code"));
        }
    }
    
    @GetMapping("/api/session/{sessionId}/attendances")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> getSessionAttendances(@PathVariable Long sessionId) {
        try {
            WorkshopSession session = workshopSessionService.findById(sessionId);
            
            if (session == null) {
                return ResponseEntity.notFound().build();
            }
            
            var attendanceData = session.getAttendances().stream()
                .map(attendance -> Map.<String, Object>of(
                    "memberId", attendance.getMember().getId(),
                    "memberName", attendance.getMember().getFullName(),
                    "checkInTime", attendance.getCheckInTime(),
                    "checkOutTime", attendance.getCheckOutTime() != null ? attendance.getCheckOutTime() : "Still here",
                    "duration", attendance.getCheckOutTime() != null ? attendance.getAttendanceDurationMinutes() + " min" : "Active",
                    "checkInMethod", attendance.getCheckInMethod()
                ))
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "sessionTitle", getSessionDisplayName(session),
                "attendances", attendanceData,
                "totalAttendees", session.getAttendanceCount()
            ));
            
        } catch (Exception e) {
            logger.error("Error getting session attendances: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get attendances"));
        }
    }
    
    @PostMapping("/api/session/{sessionId}/bulk-checkin")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> bulkCheckIn(@PathVariable Long sessionId, 
                                        @RequestBody Map<String, Object> bulkData) {
        try {
            @SuppressWarnings("unchecked")
            List<String> memberIds = (List<String>) bulkData.get("memberIds");
            
            if (memberIds == null || memberIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Member IDs are required"));
            }
            
            WorkshopSession session = workshopSessionService.findById(sessionId);
            if (session == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Session not found"));
            }
            
            int successCount = 0;
            int errorCount = 0;
            
            for (String memberIdStr : memberIds) {
                try {
                    Long memberId = Long.parseLong(memberIdStr);
                    TeamMember member = teamMemberService.findById(memberId);
                    
                    if (member != null) {
                        // Check if already checked in
                        boolean alreadyCheckedIn = session.getAttendances().stream()
                            .anyMatch(a -> a.getMember().equals(member) && a.getCheckOutTime() == null);
                        
                        if (!alreadyCheckedIn) {
                            SessionAttendance attendance = new SessionAttendance(session, member);
                            attendance.checkIn("BULK");
                            
                            session.getAttendances().add(attendance);
                            successCount++;
                        }
                    } else {
                        errorCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                    logger.warn("Error processing member ID {}: {}", memberIdStr, e.getMessage());
                }
            }
            
            workshopSessionService.save(session);
            
            logger.info("Bulk check-in completed: {} successful, {} errors for session {}", 
                       successCount, errorCount, sessionId);
            
            return ResponseEntity.ok(Map.of(
                "status", "completed",
                "successCount", successCount,
                "errorCount", errorCount,
                "totalAttendees", session.getAttendanceCount()
            ));
            
        } catch (Exception e) {
            logger.error("Error processing bulk check-in: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process bulk check-in"));
        }
    }
    
    private ResponseEntity<?> processQRCheckIn(String qrContent, String memberId) {
        // Extract session ID and validate token from QR content
        // This is a simplified implementation - in production, parse JSON properly
        try {
            // For now, extract session ID from QR content (placeholder implementation)
            String sessionId = extractSessionIdFromQR(qrContent);
            String token = extractTokenFromQR(qrContent);
            
            // Validate token
            if (!qrCodeService.validateToken(token)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired QR code"));
            }
            
            // Process check-in
            Map<String, Object> checkInData = Map.of(
                "sessionId", sessionId,
                "memberId", memberId,
                "method", "QR"
            );
            
            return checkIn(checkInData);
            
        } catch (Exception e) {
            logger.error("Error processing QR check-in: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid QR code format"));
        }
    }
    
    private ResponseEntity<?> processQRCheckOut(String qrContent, String memberId) {
        // Similar to processQRCheckIn but for check-out
        try {
            String sessionId = extractSessionIdFromQR(qrContent);
            String token = extractTokenFromQR(qrContent);
            
            if (!qrCodeService.validateToken(token)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired QR code"));
            }
            
            Map<String, Object> checkOutData = Map.of(
                "sessionId", sessionId,
                "memberId", memberId,
                "method", "QR"
            );
            
            return checkOut(checkOutData);
            
        } catch (Exception e) {
            logger.error("Error processing QR check-out: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid QR code format"));
        }
    }
    
    private String extractSessionIdFromQR(String qrContent) {
        // Placeholder implementation - in production, use proper JSON parsing
        // QR content format: {"action":"CHECK_IN","session":"123","token":"abc123","timestamp":"..."}
        try {
            int sessionStart = qrContent.indexOf("\"session\":\"") + 11;
            int sessionEnd = qrContent.indexOf("\"", sessionStart);
            return qrContent.substring(sessionStart, sessionEnd);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid QR code format");
        }
    }
    
    private String extractTokenFromQR(String qrContent) {
        // Placeholder implementation - in production, use proper JSON parsing
        try {
            int tokenStart = qrContent.indexOf("\"token\":\"") + 9;
            int tokenEnd = qrContent.indexOf("\"", tokenStart);
            return qrContent.substring(tokenStart, tokenEnd);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid QR code format");
        }
    }
    
    /**
     * Gets a display name for a workshop session.
     * Uses objectives if available, otherwise creates a name from type and project.
     */
    private String getSessionDisplayName(WorkshopSession session) {
        if (session.getObjectives() != null && !session.getObjectives().trim().isEmpty()) {
            return session.getObjectives();
        }
        
        String projectName = session.getProject() != null ? session.getProject().getName() : "Unknown Project";
        String sessionType = session.getType() != null ? session.getType().name() : "Unknown";
        
        return String.format("%s - %s Session", projectName, sessionType);
    }
}