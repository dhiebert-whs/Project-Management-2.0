// src/main/java/org/frcpm/web/controllers/FrcEventController.java

package org.frcpm.web.controllers;

import org.frcpm.integration.frc.FrcApiService;
import org.frcpm.models.FrcEvent;
import org.frcpm.models.FrcTeamRanking;
import org.frcpm.models.Project;
import org.frcpm.repositories.spring.FrcEventRepository;
import org.frcpm.repositories.spring.FrcTeamRankingRepository;
import org.frcpm.services.ProjectService;
import org.frcpm.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Controller for FRC Event management and competition dashboard.
 * 
 * Provides endpoints for viewing FRC competitions, team rankings,
 * and integrating events with project timelines.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 3A - FRC API Integration
 */
@Controller
@RequestMapping("/frc")
public class FrcEventController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(FrcEventController.class.getName());
    
    private final FrcApiService frcApiService;
    private final FrcEventRepository frcEventRepository;
    private final FrcTeamRankingRepository frcTeamRankingRepository;
    private final ProjectService projectService;
    
    @Value("${app.team.default-number:0}")
    private Integer defaultTeamNumber;
    
    @Value("${app.frc.season.current-year:2025}")
    private Integer currentSeasonYear;
    
    @Autowired
    public FrcEventController(FrcApiService frcApiService,
                             FrcEventRepository frcEventRepository,
                             FrcTeamRankingRepository frcTeamRankingRepository,
                             ProjectService projectService) {
        this.frcApiService = frcApiService;
        this.frcEventRepository = frcEventRepository;
        this.frcTeamRankingRepository = frcTeamRankingRepository;
        this.projectService = projectService;
    }
    
    /**
     * Competition dashboard showing upcoming events and team status.
     */
    @GetMapping("/dashboard")
    public String competitionDashboard(Model model, @AuthenticationPrincipal UserPrincipal user) {
        try {
            // Get upcoming events
            List<FrcEvent> upcomingEvents = frcEventRepository.findUpcomingEvents(LocalDate.now());
            model.addAttribute("upcomingEvents", upcomingEvents);
            
            // Get events this week
            LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);
            List<FrcEvent> eventsThisWeek = frcEventRepository.findEventsThisWeek(weekStart, weekEnd);
            model.addAttribute("eventsThisWeek", eventsThisWeek);
            
            // Get team events if team number is configured
            if (defaultTeamNumber > 0) {
                List<FrcEvent> teamEvents = frcApiService.getTeamEvents(defaultTeamNumber, currentSeasonYear);
                model.addAttribute("teamEvents", teamEvents);
                model.addAttribute("teamNumber", defaultTeamNumber);
                
                // Get team rankings for current events
                Map<String, FrcTeamRanking> teamRankings = new HashMap<>();
                for (FrcEvent event : teamEvents) {
                    if (!event.isCompleted()) {
                        Optional<FrcTeamRanking> ranking = frcTeamRankingRepository
                            .findByFrcEventAndTeamNumber(event, defaultTeamNumber);
                        ranking.ifPresent(r -> teamRankings.put(event.getEventCode(), r));
                    }
                }
                model.addAttribute("teamRankings", teamRankings);
            }
            
            // Get events linked to projects
            List<FrcEvent> linkedEvents = frcEventRepository.findEventsLinkedToProjects();
            model.addAttribute("linkedEvents", linkedEvents);
            
            // Get events that could be linked to projects
            List<FrcEvent> unlinkableEvents = frcEventRepository.findUnlinkedUpcomingEvents(LocalDate.now());
            model.addAttribute("unlinkableEvents", unlinkableEvents);
            
            // Competition season info
            model.addAttribute("currentSeason", currentSeasonYear);
            model.addAttribute("apiConfigured", frcApiService.validateApiConnection());
            
            return "frc/dashboard";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading competition dashboard: " + e.getMessage());
            model.addAttribute("error", "Unable to load competition data");
            return "frc/dashboard";
        }
    }
    
    /**
     * List all events for the current season.
     */
    @GetMapping("/events")
    public String listEvents(@RequestParam(defaultValue = "0") Integer seasonYear,
                           @RequestParam(required = false) String eventType,
                           Model model,
                           @AuthenticationPrincipal UserPrincipal user) {
        
        Integer targetYear = seasonYear > 0 ? seasonYear : currentSeasonYear;
        
        try {
            List<FrcEvent> events;
            
            if (eventType != null && !eventType.isEmpty()) {
                FrcEvent.EventType type = FrcEvent.EventType.fromString(eventType);
                events = frcEventRepository.findByEventTypeAndSeasonYearOrderByStartDateAsc(type, targetYear);
            } else {
                events = frcEventRepository.findBySeasonYearOrderByStartDateAsc(targetYear);
            }
            
            model.addAttribute("events", events);
            model.addAttribute("seasonYear", targetYear);
            model.addAttribute("selectedEventType", eventType);
            model.addAttribute("eventTypes", FrcEvent.EventType.values());
            
            // Add statistics
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalEvents", events.size());
            stats.put("upcomingEvents", events.stream().filter(FrcEvent::isUpcoming).count());
            stats.put("activeEvents", events.stream().filter(FrcEvent::isActive).count());
            stats.put("completedEvents", events.stream().filter(FrcEvent::isCompleted).count());
            model.addAttribute("eventStats", stats);
            
            return "frc/events";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading events: " + e.getMessage());
            model.addAttribute("error", "Unable to load events");
            return "frc/events";
        }
    }
    
    /**
     * Show details for a specific event.
     */
    @GetMapping("/events/{eventId}")
    public String eventDetails(@PathVariable Long eventId, Model model, 
                              @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            Optional<FrcEvent> eventOpt = frcEventRepository.findById(eventId);
            if (eventOpt.isEmpty()) {
                model.addAttribute("error", "Event not found");
                return "error/not-found";
            }
            
            FrcEvent event = eventOpt.get();
            model.addAttribute("event", event);
            
            // Get rankings for this event
            List<FrcTeamRanking> rankings = frcTeamRankingRepository.findByFrcEventOrderByRankAsc(event);
            model.addAttribute("rankings", rankings);
            
            // Highlight team ranking if configured
            if (defaultTeamNumber > 0) {
                Optional<FrcTeamRanking> teamRanking = rankings.stream()
                    .filter(r -> r.getTeamNumber().equals(defaultTeamNumber))
                    .findFirst();
                model.addAttribute("teamRanking", teamRanking.orElse(null));
                model.addAttribute("teamNumber", defaultTeamNumber);
            }
            
            // Get linked project if any
            if (event.getLinkedProject() != null) {
                model.addAttribute("linkedProject", event.getLinkedProject());
            }
            
            // Get available projects for linking
            List<Project> availableProjects = projectService.findAll().stream()
                .filter(p -> p.getHardDeadline() != null && p.getHardDeadline().isAfter(LocalDate.now()))
                .toList();
            model.addAttribute("availableProjects", availableProjects);
            
            return "frc/event-details";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading event details: " + e.getMessage());
            model.addAttribute("error", "Unable to load event details");
            return "error/general";
        }
    }
    
    /**
     * Link an FRC event to a project for deadline management.
     */
    @PostMapping("/events/{eventId}/link-project")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> linkEventToProject(
            @PathVariable Long eventId,
            @RequestParam Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check permissions - only mentors and admins can link events
            if (!user.getUser().getRole().isMentor()) {
                response.put("success", false);
                response.put("message", "Insufficient permissions");
                return ResponseEntity.status(403).body(response);
            }
            
            Optional<FrcEvent> eventOpt = frcEventRepository.findById(eventId);
            Project project = projectService.findById(projectId); // Returns Project directly, not Optional
            
            if (eventOpt.isEmpty() || project == null) { // Check for null instead of isEmpty()
                response.put("success", false);
                response.put("message", "Event or project not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            FrcEvent event = eventOpt.get();
            // project is already the Project object, no need to call get()
            
            // Link the event to the project
            event.setLinkedProject(project);
            frcEventRepository.save(event);
            
            // Update project deadline to match event if earlier
            if (event.getStartDate() != null && 
                (project.getHardDeadline() == null || event.getStartDate().isBefore(project.getHardDeadline()))) {
                
                project.setHardDeadline(event.getStartDate().minusDays(1)); // Day before event
                projectService.save(project);
                
                response.put("deadlineUpdated", true);
                response.put("newDeadline", project.getHardDeadline().toString());
            }
            
            response.put("success", true);
            response.put("message", "Event linked to project successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.severe("Error linking event to project: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to link event: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Unlink an FRC event from a project.
     */
    @PostMapping("/events/{eventId}/unlink-project")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unlinkEventFromProject(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check permissions
            if (!user.getUser().getRole().isMentor()) {
                response.put("success", false);
                response.put("message", "Insufficient permissions");
                return ResponseEntity.status(403).body(response);
            }
            
            Optional<FrcEvent> eventOpt = frcEventRepository.findById(eventId);
            if (eventOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Event not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            FrcEvent event = eventOpt.get();
            event.setLinkedProject(null);
            frcEventRepository.save(event);
            
            response.put("success", true);
            response.put("message", "Event unlinked from project");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.severe("Error unlinking event from project: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to unlink event: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Trigger manual synchronization with FRC API.
     */
    @PostMapping("/sync")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> syncWithFrcApi(@AuthenticationPrincipal UserPrincipal user) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check permissions - only admins can trigger manual sync
            if (!user.getUser().getRole().equals(org.frcpm.models.UserRole.ADMIN)) {
                response.put("success", false);
                response.put("message", "Insufficient permissions");
                return ResponseEntity.status(403).body(response);
            }
            
            // Validate API connection first
            if (!frcApiService.validateApiConnection()) {
                response.put("success", false);
                response.put("message", "FRC API connection failed - check configuration");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Trigger async synchronization
            frcApiService.syncAllData();
            
            response.put("success", true);
            response.put("message", "FRC API synchronization started");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.severe("Error triggering FRC API sync: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Sync failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get FRC API status and configuration info.
     */
    @GetMapping("/api/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFrcApiStatus(@AuthenticationPrincipal UserPrincipal user) {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Basic status info available to all authenticated users
            status.put("apiConfigured", frcApiService.validateApiConnection());
            status.put("currentSeason", currentSeasonYear);
            status.put("teamNumber", defaultTeamNumber);
            
            // Event counts
            long totalEvents = frcEventRepository.count();
            long currentSeasonEvents = frcEventRepository.findBySeasonYearOrderByStartDateAsc(currentSeasonYear).size();
            long upcomingEvents = frcEventRepository.findUpcomingEvents(LocalDate.now()).size();
            
            status.put("totalEvents", totalEvents);
            status.put("currentSeasonEvents", currentSeasonEvents);
            status.put("upcomingEvents", upcomingEvents);
            
            // Detailed info for admins
            if (user.getUser().getRole().equals(org.frcpm.models.UserRole.ADMIN)) {
                status.put("lastSyncTime", "Not implemented yet"); // Would track last sync
                status.put("syncErrors", 0); // Would track sync errors
            }
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            LOGGER.severe("Error getting FRC API status: " + e.getMessage());
            status.put("error", "Failed to get API status");
            return ResponseEntity.status(500).body(status);
        }
    }
    
    /**
     * Search events by name or location.
     */
    @GetMapping("/events/search")
    @ResponseBody
    public ResponseEntity<List<FrcEvent>> searchEvents(@RequestParam String query,
                                                      @RequestParam(defaultValue = "10") Integer limit) {
        try {
            // Search by location
            List<FrcEvent> events = frcEventRepository.findByLocationContaining(query);
            
            // Limit results
            if (events.size() > limit) {
                events = events.subList(0, limit);
            }
            
            return ResponseEntity.ok(events);
            
        } catch (Exception e) {
            LOGGER.severe("Error searching events: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}