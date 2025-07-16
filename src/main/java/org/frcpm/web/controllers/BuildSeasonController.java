package org.frcpm.web.controllers;

import org.frcpm.models.*;
import org.frcpm.services.*;
import org.frcpm.integration.tba.TheBlueAllianceService;
import org.frcpm.integration.tba.dto.TBAEventDto;
import org.frcpm.integration.frc.FRCEventsService;
import org.frcpm.integration.frc.dto.FRCEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/build-season")
public class BuildSeasonController {
    
    private static final Logger logger = LoggerFactory.getLogger(BuildSeasonController.class);
    
    private final ProjectService projectService;
    private final TaskService taskService;
    private final TeamMemberService teamMemberService;
    private final WorkshopSessionService workshopSessionService;
    private final TheBlueAllianceService tbaService;
    private final FRCEventsService frcEventsService;
    
    public BuildSeasonController(ProjectService projectService,
                                TaskService taskService,
                                TeamMemberService teamMemberService,
                                WorkshopSessionService workshopSessionService,
                                TheBlueAllianceService tbaService,
                                FRCEventsService frcEventsService) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.teamMemberService = teamMemberService;
        this.workshopSessionService = workshopSessionService;
        this.tbaService = tbaService;
        this.frcEventsService = frcEventsService;
    }
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String buildSeasonDashboard(Model model) {
        try {
            // Get current season information
            CompetitionSeason currentSeason = getCurrentSeason();
            model.addAttribute("currentSeason", currentSeason);
            
            // Calculate days until kickoff and competition
            if (currentSeason != null) {
                model.addAttribute("daysUntilKickoff", calculateDaysUntilKickoff(currentSeason));
                model.addAttribute("daysUntilCompetition", calculateDaysUntilCompetition(currentSeason));
                model.addAttribute("buildDaysRemaining", calculateBuildDaysRemaining(currentSeason));
            }
            
            // Get active projects
            List<Project> activeProjects = projectService.findAll().stream()
                .filter(p -> !p.isCompleted())
                .collect(Collectors.toList());
            model.addAttribute("activeProjects", activeProjects);
            
            // Get critical tasks across all projects
            List<Task> criticalTasks = getCriticalTasks(activeProjects);
            model.addAttribute("criticalTasks", criticalTasks);
            
            // Get upcoming milestones
            List<Milestone> upcomingMilestones = getUpcomingMilestones();
            model.addAttribute("upcomingMilestones", upcomingMilestones);
            
            // Get team velocity metrics
            TeamVelocityMetrics velocityMetrics = calculateTeamVelocity();
            model.addAttribute("velocityMetrics", velocityMetrics);
            
            // Get robot status
            Robot currentRobot = getCurrentRobot();
            model.addAttribute("currentRobot", currentRobot);
            
            // Get recent workshop activity
            List<WorkshopSession> recentSessions = workshopSessionService.findAll().stream()
                .filter(s -> s.getStartTime().isAfter(LocalDateTime.now().minusDays(7)))
                .sorted((s1, s2) -> s2.getStartTime().compareTo(s1.getStartTime()))
                .limit(10)
                .collect(Collectors.toList());
            model.addAttribute("recentSessions", recentSessions);
            
            // Get upcoming competitions from TBA/FRC APIs
            List<Competition> upcomingCompetitions = getUpcomingCompetitions();
            model.addAttribute("upcomingCompetitions", upcomingCompetitions);
            
            return "build-season/dashboard";
            
        } catch (Exception e) {
            logger.error("Error loading build season dashboard", e);
            model.addAttribute("error", "Failed to load dashboard data");
            return "error/general";
        }
    }
    
    @GetMapping("/milestones")
    @PreAuthorize("isAuthenticated()")
    public String milestones(@RequestParam(required = false) String filter, Model model) {
        List<Milestone> milestones = getAllMilestones();
        
        // Apply filters
        if ("upcoming".equals(filter)) {
            milestones = milestones.stream()
                .filter(m -> m.getTargetDate().isAfter(LocalDate.now()))
                .collect(Collectors.toList());
        } else if ("overdue".equals(filter)) {
            milestones = milestones.stream()
                .filter(m -> m.getTargetDate().isBefore(LocalDate.now()) && !m.isCompleted())
                .collect(Collectors.toList());
        } else if ("completed".equals(filter)) {
            milestones = milestones.stream()
                .filter(Milestone::isCompleted)
                .collect(Collectors.toList());
        }
        
        model.addAttribute("milestones", milestones);
        model.addAttribute("filter", filter);
        model.addAttribute("currentSeason", getCurrentSeason());
        
        return "build-season/milestones";
    }
    
    @GetMapping("/robot-status")
    @PreAuthorize("isAuthenticated()")
    public String robotStatus(Model model) {
        Robot currentRobot = getCurrentRobot();
        model.addAttribute("currentRobot", currentRobot);
        
        if (currentRobot != null) {
            // Get robot subsystems and their completion status
            List<Subsystem> subsystems = getSubsystemsByRobot(currentRobot);
            model.addAttribute("subsystems", subsystems);
            
            // Calculate robot completion percentage
            double completionPercentage = calculateRobotCompletion(currentRobot);
            model.addAttribute("completionPercentage", completionPercentage);
            
            // Get critical path for robot completion
            List<Task> criticalPath = getCriticalPathForRobot(currentRobot);
            model.addAttribute("criticalPath", criticalPath);
            
            // Get weight and measurement data
            RobotMetrics metrics = calculateRobotMetrics(currentRobot);
            model.addAttribute("metrics", metrics);
        }
        
        return "build-season/robot-status";
    }
    
    @GetMapping("/competition-prep")
    @PreAuthorize("isAuthenticated()")
    public String competitionPrep(@RequestParam(required = false) Long competitionId, Model model) {
        List<Competition> upcomingCompetitions = getUpcomingCompetitions();
        model.addAttribute("upcomingCompetitions", upcomingCompetitions);
        
        Competition selectedCompetition = null;
        if (competitionId != null) {
            selectedCompetition = upcomingCompetitions.stream()
                .filter(c -> c.getId().equals(competitionId))
                .findFirst()
                .orElse(null);
        } else if (!upcomingCompetitions.isEmpty()) {
            selectedCompetition = upcomingCompetitions.get(0); // Default to first upcoming
        }
        
        if (selectedCompetition != null) {
            model.addAttribute("selectedCompetition", selectedCompetition);
            
            // Get competition checklist
            CompetitionChecklist checklist = getCompetitionChecklist(selectedCompetition);
            model.addAttribute("checklist", checklist);
            
            // Get packing list
            List<PackingItem> packingList = getPackingList(selectedCompetition);
            model.addAttribute("packingList", packingList);
            
            // Calculate readiness percentage
            double readinessPercentage = calculateCompetitionReadiness(selectedCompetition);
            model.addAttribute("readinessPercentage", readinessPercentage);
        }
        
        return "build-season/competition-prep";
    }
    
    // API Endpoints
    
    @PostMapping("/api/milestone/{id}/complete")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> completeMilestone(@PathVariable Long id) {
        try {
            // TODO: Implement milestone completion logic
            // This would involve updating milestone status and potentially triggering notifications
            
            logger.info("Milestone {} marked as complete", id);
            return ResponseEntity.ok(Map.of("status", "completed", "milestoneId", id));
            
        } catch (Exception e) {
            logger.error("Error completing milestone {}", id, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to complete milestone"));
        }
    }
    
    @PostMapping("/api/robot/weight")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> updateRobotWeight(@RequestBody Map<String, Object> weightData) {
        try {
            Robot currentRobot = getCurrentRobot();
            if (currentRobot == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No current robot found"));
            }
            
            Double weight = Double.parseDouble(weightData.get("weight").toString());
            String unit = (String) weightData.get("unit");
            String notes = (String) weightData.get("notes");
            
            // TODO: Implement robot weight update logic
            // This would involve saving the weight measurement with timestamp
            
            logger.info("Robot weight updated: {} {}", weight, unit);
            return ResponseEntity.ok(Map.of(
                "status", "updated",
                "weight", weight,
                "unit", unit,
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            logger.error("Error updating robot weight", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update robot weight"));
        }
    }
    
    @GetMapping("/api/progress/summary")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<?> getProgressSummary() {
        try {
            CompetitionSeason currentSeason = getCurrentSeason();
            Robot currentRobot = getCurrentRobot();
            
            Map<String, Object> summary = Map.of(
                "seasonProgress", currentSeason != null ? calculateSeasonProgress(currentSeason) : 0,
                "robotCompletion", currentRobot != null ? calculateRobotCompletion(currentRobot) : 0,
                "tasksCompleted", getCompletedTasksCount(),
                "milestonesAchieved", getCompletedMilestonesCount(),
                "daysRemaining", currentSeason != null ? calculateBuildDaysRemaining(currentSeason) : 0,
                "criticalTasksCount", getCriticalTasksCount(),
                "teamVelocity", calculateTeamVelocity().getTasksPerWeek()
            );
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            logger.error("Error getting progress summary", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get progress summary"));
        }
    }
    
    @PostMapping("/api/checklist/{itemId}/toggle")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> toggleChecklistItem(@PathVariable Long itemId) {
        try {
            // TODO: Implement checklist item toggle logic
            // This would involve updating the completion status of a specific checklist item
            
            logger.info("Checklist item {} toggled", itemId);
            return ResponseEntity.ok(Map.of("status", "toggled", "itemId", itemId));
            
        } catch (Exception e) {
            logger.error("Error toggling checklist item {}", itemId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to toggle checklist item"));
        }
    }
    
    // Helper methods
    
    private CompetitionSeason getCurrentSeason() {
        // TODO: Implement logic to get current competition season
        // This would query the database for the active season
        return createMockSeason(); // Placeholder
    }
    
    private CompetitionSeason createMockSeason() {
        CompetitionSeason season = new CompetitionSeason();
        season.setYear(2024);
        season.setGameName("CRESCENDO");
        season.setKickoffDate(LocalDate.of(2024, 1, 6));
        season.setStopBuildDate(LocalDate.of(2024, 2, 20));
        season.setStatus(SeasonStatus.ACTIVE);
        return season;
    }
    
    private Robot getCurrentRobot() {
        // TODO: Implement logic to get current robot
        // This would query for the robot associated with the current season
        return createMockRobot(); // Placeholder
    }
    
    private Robot createMockRobot() {
        Robot robot = new Robot();
        robot.setName("CRESCENDO Bot");
        robot.setStatus(RobotStatus.IN_DEVELOPMENT);
        robot.setType(RobotType.COMPETITION);
        return robot;
    }
    
    private long calculateDaysUntilKickoff(CompetitionSeason season) {
        if (season.getKickoffDate().isBefore(LocalDate.now())) {
            return 0; // Kickoff has passed
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), season.getKickoffDate());
    }
    
    private long calculateDaysUntilCompetition(CompetitionSeason season) {
        // TODO: Get first competition date from competitions list
        // For now, assume 8 weeks after kickoff
        LocalDate firstCompetition = season.getKickoffDate().plusWeeks(8);
        if (firstCompetition.isBefore(LocalDate.now())) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), firstCompetition);
    }
    
    private long calculateBuildDaysRemaining(CompetitionSeason season) {
        if (season.getStopBuildDate().isBefore(LocalDate.now())) {
            return 0; // Build season has ended
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), season.getStopBuildDate());
    }
    
    private List<Task> getCriticalTasks(List<Project> projects) {
        return projects.stream()
            .flatMap(p -> taskService.findByProject(p).stream())
            .filter(t -> !t.isCompleted())
            .filter(t -> t.getPriority() == Task.Priority.CRITICAL || t.getPriority() == Task.Priority.HIGH)
            .filter(t -> t.getEndDate() != null && t.getEndDate().isBefore(LocalDate.now().plusDays(7)))
            .sorted((t1, t2) -> {
                // Sort by priority first, then by due date
                int priorityCompare = t2.getPriority().getValue() - t1.getPriority().getValue();
                if (priorityCompare != 0) return priorityCompare;
                
                if (t1.getEndDate() == null && t2.getEndDate() == null) return 0;
                if (t1.getEndDate() == null) return 1;
                if (t2.getEndDate() == null) return -1;
                return t1.getEndDate().compareTo(t2.getEndDate());
            })
            .limit(10)
            .collect(Collectors.toList());
    }
    
    private List<Milestone> getUpcomingMilestones() {
        // TODO: Implement milestone retrieval from database
        return List.of(); // Placeholder
    }
    
    private List<Milestone> getAllMilestones() {
        // TODO: Implement comprehensive milestone retrieval
        return List.of(); // Placeholder
    }
    
    private TeamVelocityMetrics calculateTeamVelocity() {
        // TODO: Implement team velocity calculation
        // This would analyze completed tasks over time periods
        TeamVelocityMetrics metrics = new TeamVelocityMetrics();
        metrics.setTasksPerWeek(12.5);
        metrics.setTrend("increasing");
        return metrics;
    }
    
    private List<Subsystem> getSubsystemsByRobot(Robot robot) {
        // TODO: Implement subsystem retrieval for robot
        return List.of(); // Placeholder
    }
    
    private double calculateRobotCompletion(Robot robot) {
        // TODO: Calculate robot completion based on subsystem completion
        return 65.0; // Placeholder
    }
    
    private List<Task> getCriticalPathForRobot(Robot robot) {
        // TODO: Implement critical path calculation for robot completion
        return List.of(); // Placeholder
    }
    
    private RobotMetrics calculateRobotMetrics(Robot robot) {
        // TODO: Calculate robot metrics (weight, dimensions, etc.)
        RobotMetrics metrics = new RobotMetrics();
        metrics.setCurrentWeight(45.2);
        metrics.setWeightLimit(125.0);
        metrics.setWeightUnit("lbs");
        return metrics;
    }
    
    private List<Competition> getUpcomingCompetitions() {
        // TODO: Implement competition retrieval from TBA/FRC APIs
        return List.of(); // Placeholder
    }
    
    private CompetitionChecklist getCompetitionChecklist(Competition competition) {
        // TODO: Implement competition checklist retrieval
        return new CompetitionChecklist(); // Placeholder
    }
    
    private List<PackingItem> getPackingList(Competition competition) {
        // TODO: Implement packing list retrieval
        return List.of(); // Placeholder
    }
    
    private double calculateCompetitionReadiness(Competition competition) {
        // TODO: Calculate readiness percentage based on checklist completion
        return 78.5; // Placeholder
    }
    
    private double calculateSeasonProgress(CompetitionSeason season) {
        long totalDays = ChronoUnit.DAYS.between(season.getKickoffDate(), season.getStopBuildDate());
        long daysPassed = ChronoUnit.DAYS.between(season.getKickoffDate(), LocalDate.now());
        return Math.max(0, Math.min(100, (double) daysPassed / totalDays * 100));
    }
    
    private int getCompletedTasksCount() {
        // TODO: Implement completed tasks count
        return 47; // Placeholder
    }
    
    private int getCompletedMilestonesCount() {
        // TODO: Implement completed milestones count
        return 8; // Placeholder
    }
    
    private int getCriticalTasksCount() {
        // TODO: Implement critical tasks count
        return 5; // Placeholder
    }
    
    // Inner classes for data transfer
    
    public static class TeamVelocityMetrics {
        private double tasksPerWeek;
        private String trend;
        
        // Getters and setters
        public double getTasksPerWeek() { return tasksPerWeek; }
        public void setTasksPerWeek(double tasksPerWeek) { this.tasksPerWeek = tasksPerWeek; }
        
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
    }
    
    public static class RobotMetrics {
        private double currentWeight;
        private double weightLimit;
        private String weightUnit;
        
        // Getters and setters
        public double getCurrentWeight() { return currentWeight; }
        public void setCurrentWeight(double currentWeight) { this.currentWeight = currentWeight; }
        
        public double getWeightLimit() { return weightLimit; }
        public void setWeightLimit(double weightLimit) { this.weightLimit = weightLimit; }
        
        public String getWeightUnit() { return weightUnit; }
        public void setWeightUnit(String weightUnit) { this.weightUnit = weightUnit; }
        
        public double getWeightPercentage() {
            return (currentWeight / weightLimit) * 100;
        }
    }
    
    public static class CompetitionChecklist {
        private List<ChecklistItem> items;
        
        public List<ChecklistItem> getItems() { return items; }
        public void setItems(List<ChecklistItem> items) { this.items = items; }
        
        public static class ChecklistItem {
            private Long id;
            private String description;
            private boolean completed;
            private String category;
            
            // Getters and setters
            public Long getId() { return id; }
            public void setId(Long id) { this.id = id; }
            
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            
            public boolean isCompleted() { return completed; }
            public void setCompleted(boolean completed) { this.completed = completed; }
            
            public String getCategory() { return category; }
            public void setCategory(String category) { this.category = category; }
        }
    }
    
    public static class PackingItem {
        private String name;
        private int quantity;
        private String category;
        private boolean packed;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public boolean isPacked() { return packed; }
        public void setPacked(boolean packed) { this.packed = packed; }
    }
}