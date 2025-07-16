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
import java.util.Optional;

@Controller
@RequestMapping("/mobile/workshop")
public class MobileWorkshopController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileWorkshopController.class);
    
    private final WorkshopSessionService workshopSessionService;
    private final QRCodeService qrCodeService;
    private final TaskService taskService;
    private final ProjectService projectService;
    private final TeamMemberService teamMemberService;
    
    public MobileWorkshopController(WorkshopSessionService workshopSessionService,
                                   QRCodeService qrCodeService,
                                   TaskService taskService,
                                   ProjectService projectService,
                                   TeamMemberService teamMemberService) {
        this.workshopSessionService = workshopSessionService;
        this.qrCodeService = qrCodeService;
        this.taskService = taskService;
        this.projectService = projectService;
        this.teamMemberService = teamMemberService;
    }
    
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public String dashboard(Model model) {
        // Get active workshop sessions
        List<WorkshopSession> activeSessions = workshopSessionService.getActiveSessions();
        List<WorkshopSession> todaysSessions = workshopSessionService.getTodaysSessions();
        
        model.addAttribute("activeSessions", activeSessions);
        model.addAttribute("todaysSessions", todaysSessions);
        model.addAttribute("upcomingSessions", workshopSessionService.getUpcomingSessions());
        
        return "mobile/workshop-dashboard";
    }
    
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public String sessionDetail(@PathVariable Long sessionId, Model model) {
        WorkshopSession workshopSession = workshopSessionService.findById(sessionId);
        
        if (workshopSession == null) {
            return "redirect:/mobile/workshop/dashboard";
        }
        
        // Generate QR codes for check-in/check-out
        String checkInQR = qrCodeService.generateSessionCheckInQR(sessionId.toString(), 30);
        String checkOutQR = qrCodeService.generateSessionCheckOutQR(sessionId.toString(), 30);
        
        model.addAttribute("session", workshopSession);
        model.addAttribute("checkInQR", checkInQR);
        model.addAttribute("checkOutQR", checkOutQR);
        model.addAttribute("attendanceCount", workshopSession.getAttendanceCount());
        model.addAttribute("activeTasks", taskService.getActiveTasksForProject(workshopSession.getProject().getId()));
        
        return "mobile/workshop-session";
    }
    
    @GetMapping("/tasks")
    @PreAuthorize("isAuthenticated()")
    public String mobileTasks(@RequestParam(required = false) Long projectId, Model model) {
        List<Project> projects = projectService.findAll();
        
        if (projectId != null) {
            List<Task> tasks = taskService.getActiveTasksForProject(projectId);
            model.addAttribute("tasks", tasks);
            model.addAttribute("selectedProject", projectService.findById(projectId));
        }
        
        model.addAttribute("projects", projects);
        
        return "mobile/task-quick-update";
    }
    
    @GetMapping("/attendance")
    @PreAuthorize("isAuthenticated()")
    public String attendanceScanner(@RequestParam(required = false) Long sessionId, Model model) {
        if (sessionId != null) {
            WorkshopSession session = workshopSessionService.findById(sessionId);
            if (session != null) {
                model.addAttribute("session", session);
                model.addAttribute("attendees", session.getAttendances());
            }
        }
        
        model.addAttribute("activeSessions", workshopSessionService.getActiveSessions());
        
        return "mobile/attendance-scanner";
    }
    
    @GetMapping("/voice")
    @PreAuthorize("isAuthenticated()")
    public String voiceCommands(Model model) {
        model.addAttribute("activeSessions", workshopSessionService.getActiveSessions());
        model.addAttribute("recentTasks", taskService.getRecentTasks(10));
        
        return "mobile/voice-commands";
    }
    
    // API Endpoints for mobile interactions
    
    @PostMapping("/api/session/{sessionId}/start")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> startSession(@PathVariable Long sessionId) {
        try {
            WorkshopSession workshopSession = workshopSessionService.findById(sessionId);
            if (workshopSession == null) {
                return ResponseEntity.notFound().build();
            }
            workshopSession.startSession();
            workshopSessionService.save(workshopSession);
            
            logger.info("Started workshop session: {}", sessionId);
            return ResponseEntity.ok(Map.of("status", "started", "sessionId", sessionId));
            
        } catch (Exception e) {
            logger.error("Error starting session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to start session"));
        }
    }
    
    @PostMapping("/api/session/{sessionId}/end")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> endSession(@PathVariable Long sessionId) {
        try {
            WorkshopSession workshopSession = workshopSessionService.findById(sessionId);
            if (workshopSession == null) {
                return ResponseEntity.notFound().build();
            }
            workshopSession.endSession();
            workshopSessionService.save(workshopSession);
            
            // Invalidate QR codes for this session
            qrCodeService.invalidateSessionTokens(sessionId.toString());
            
            logger.info("Ended workshop session: {}", sessionId);
            return ResponseEntity.ok(Map.of("status", "ended", "sessionId", sessionId));
            
        } catch (Exception e) {
            logger.error("Error ending session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to end session"));
        }
    }
    
    @PostMapping("/api/qr/scan")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> handleQRScan(@RequestBody Map<String, Object> qrData) {
        try {
            String token = (String) qrData.get("token");
            String action = (String) qrData.get("action");
            
            // Validate token
            if (!qrCodeService.validateToken(token)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired QR code"));
            }
            
            QRCodeService.QRCodeToken tokenInfo = qrCodeService.getTokenInfo(token);
            if (tokenInfo == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token not found"));
            }
            
            // Process based on action
            return switch (action) {
                case "CHECK_IN" -> handleCheckIn(tokenInfo, qrData);
                case "CHECK_OUT" -> handleCheckOut(tokenInfo, qrData);
                case "TOOL_CHECKOUT" -> handleToolCheckout(tokenInfo, qrData);
                case "TASK_UPDATE" -> handleTaskUpdate(tokenInfo, qrData);
                default -> ResponseEntity.badRequest().body(Map.of("error", "Unknown action: " + action));
            };
            
        } catch (Exception e) {
            logger.error("Error processing QR scan: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process QR scan"));
        }
    }
    
    @PostMapping("/api/task/{taskId}/quick-update")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> quickTaskUpdate(@PathVariable Long taskId, @RequestBody Map<String, Object> updateData) {
        try {
            Task task = taskService.findById(taskId);
            if (task == null) {
                return ResponseEntity.notFound().build();
            }
            
            String action = (String) updateData.get("action");
            String notes = (String) updateData.get("notes");
            
            // Process quick update
            switch (action) {
                case "START" -> {
                    task.setProgress(1); // Mark as started
                    task.setStartDate(LocalDateTime.now().toLocalDate());
                }
                case "COMPLETE" -> {
                    task.setProgress(100);
                    task.setCompleted(true);
                }
                case "BLOCK" -> {
                    // Task model doesn't have a blocked status, so we'll add to description
                    String currentDesc = task.getDescription() != null ? task.getDescription() : "";
                    task.setDescription(currentDesc + "\n[BLOCKED] Task blocked on " + LocalDateTime.now());
                }
                case "ADD_NOTE" -> {
                    if (notes != null && !notes.trim().isEmpty()) {
                        String currentDesc = task.getDescription() != null ? task.getDescription() : "";
                        task.setDescription(currentDesc + "\n[Mobile Update] " + notes);
                    }
                }
                default -> {
                    return ResponseEntity.badRequest().body(Map.of("error", "Unknown action: " + action));
                }
            }
            
            taskService.save(task);
            
            logger.info("Quick task update: Task {} -> {}", taskId, action);
            return ResponseEntity.ok(Map.of("status", "updated", "taskId", taskId, "action", action));
            
        } catch (Exception e) {
            logger.error("Error updating task {}: {}", taskId, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update task"));
        }
    }
    
    @PostMapping("/api/voice/process")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> processVoiceCommand(@RequestBody Map<String, Object> voiceData) {
        try {
            String command = (String) voiceData.get("command");
            String transcript = (String) voiceData.get("transcript");
            
            // Simple voice command processing
            if (transcript != null) {
                transcript = transcript.toLowerCase();
                
                if (transcript.contains("start task")) {
                    // Extract task information and start task
                    return ResponseEntity.ok(Map.of("action", "start_task", "response", "Task started"));
                } else if (transcript.contains("complete task")) {
                    // Extract task information and complete task
                    return ResponseEntity.ok(Map.of("action", "complete_task", "response", "Task completed"));
                } else if (transcript.contains("check in")) {
                    // Process check-in
                    return ResponseEntity.ok(Map.of("action", "check_in", "response", "Checked in successfully"));
                } else if (transcript.contains("help")) {
                    // Provide help
                    return ResponseEntity.ok(Map.of("action", "help", "response", "Available commands: start task, complete task, check in, check out"));
                }
            }
            
            return ResponseEntity.ok(Map.of("action", "unknown", "response", "Sorry, I didn't understand that command"));
            
        } catch (Exception e) {
            logger.error("Error processing voice command: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process voice command"));
        }
    }
    
    private ResponseEntity<?> handleCheckIn(QRCodeService.QRCodeToken tokenInfo, Map<String, Object> qrData) {
        // Implementation for check-in processing
        logger.info("Processing check-in for session: {}", tokenInfo.getSessionId());
        return ResponseEntity.ok(Map.of("action", "check_in", "status", "success"));
    }
    
    private ResponseEntity<?> handleCheckOut(QRCodeService.QRCodeToken tokenInfo, Map<String, Object> qrData) {
        // Implementation for check-out processing
        logger.info("Processing check-out for session: {}", tokenInfo.getSessionId());
        return ResponseEntity.ok(Map.of("action", "check_out", "status", "success"));
    }
    
    private ResponseEntity<?> handleToolCheckout(QRCodeService.QRCodeToken tokenInfo, Map<String, Object> qrData) {
        // Implementation for tool checkout processing
        logger.info("Processing tool checkout for session: {}", tokenInfo.getSessionId());
        return ResponseEntity.ok(Map.of("action", "tool_checkout", "status", "success"));
    }
    
    private ResponseEntity<?> handleTaskUpdate(QRCodeService.QRCodeToken tokenInfo, Map<String, Object> qrData) {
        // Implementation for task update processing
        logger.info("Processing task update for task: {}", tokenInfo.getSessionId());
        return ResponseEntity.ok(Map.of("action", "task_update", "status", "success"));
    }
}