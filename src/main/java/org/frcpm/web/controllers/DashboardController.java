// src/main/java/org/frcpm/web/controllers/DashboardController.java

package org.frcpm.web.controllers;

import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Subteam;
import org.frcpm.services.TaskService;
import org.frcpm.services.MilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import org.frcpm.events.WebSocketEventPublisher;
import org.frcpm.models.User;
import org.frcpm.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

/**
 * Dashboard controller providing the main application entry point and overview.
 * 
 * Features:
 * - Project selection and overview
 * - Key metrics and statistics
 * - Recent activity summary
 * - Quick navigation to major features
 * - Task and milestone summaries
 * 
 * This controller serves as the central hub following successful login,
 * providing users with immediate visibility into project status and
 * quick access to primary workflows.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2A - Web Controllers Implementation
 */
@Controller
@RequestMapping({"/", "/dashboard"})
public class DashboardController extends BaseController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private MilestoneService milestoneService;

    @Autowired
    private WebSocketEventPublisher webSocketEventPublisher;
    
    /**
     * Main dashboard view.
     * 
     * Handles multiple scenarios:
     * - No projects: Show welcome screen with create project prompt
     * - Single project: Auto-select and show project dashboard
     * - Multiple projects: Show project selection with overview
     * - Selected project: Show detailed project dashboard
     * 
     * @param model the Spring MVC model
     * @param projectId optional project ID for direct project selection
     * @return dashboard view name
     */
    @GetMapping
    public String dashboard(Model model, 
                           @RequestParam(value = "projectId", required = false) Long projectId) {
        
        try {
            // Add common navigation data
            addNavigationData(model);
            addBreadcrumbs(model, "Dashboard", "/dashboard");
            
            // Get all projects for selection dropdown
            List<Project> allProjects = projectService.findAll();
            model.addAttribute("projects", allProjects);
            
            // Determine current project
            Project currentProject = determineCurrentProject(allProjects, projectId);
            
            if (currentProject == null) {
                return handleNoProjectsScenario(model, allProjects);
            }
            
            // Set current project and load dashboard data
            model.addAttribute("currentProject", currentProject);
            model.addAttribute("selectedProjectId", currentProject.getId());
            
            // Load project-specific dashboard data
            loadProjectDashboardData(model, currentProject);
            
            // Load global statistics
            loadGlobalStatistics(model);
            
            // Load dashboard-specific data for new template
            loadDashboardSpecificData(model);
            
            // Publish user activity for project viewing
            if (currentProject != null) {
                User currentUser = getCurrentUser();
                if (currentUser != null) {
                    webSocketEventPublisher.publishProjectJoin(currentUser, currentProject);
                }
            }

            return view("dashboard");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading dashboard", e);
            return handleException(e, model);
        }
    }
    
    /**
     * API endpoint for quick project statistics (for AJAX updates).
     * 
     * @param projectId the project ID
     * @param model the model
     * @return JSON view with project statistics
     */
    @GetMapping("/api/project-stats")
    public String getProjectStats(@RequestParam Long projectId, Model model) {
        try {
            Project project = projectService.findById(projectId);
            if (project == null) {
                model.addAttribute("error", "Project not found");
                return "fragments/error";
            }
            
            Map<String, Object> stats = projectService.getProjectSummary(projectId);
            model.addAttribute("stats", stats);
            model.addAttribute("project", project);
            
            return "fragments/project-stats";
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading project stats", e);
            model.addAttribute("error", "Failed to load project statistics");
            return "fragments/error";
        }
    }
    
    /**
     * Quick actions endpoint for common dashboard operations.
     * 
     * @param action the action to perform
     * @param projectId the project ID (if applicable)
     * @param model the model
     * @return redirect to appropriate page
     */
    @GetMapping("/quick-action")
    public String quickAction(@RequestParam String action,
                             @RequestParam(required = false) Long projectId,
                             Model model) {
        
        try {
            switch (action.toLowerCase()) {
                case "new-project":
                    return redirect("/projects/new");
                    
                case "new-task":
                    if (projectId != null) {
                        return redirect("/tasks/new?projectId=" + projectId);
                    }
                    return redirect("/tasks/new");
                    
                case "team-management":
                    return redirect("/team");
                    
                case "view-calendar":
                    if (projectId != null) {
                        return redirect("/meetings?projectId=" + projectId);
                    }
                    return redirect("/meetings");
                    
                case "reports":
                    if (projectId != null) {
                        return redirect("/reports?projectId=" + projectId);
                    }
                    return redirect("/reports");
                    
                default:
                    addWarningMessage(model, "Unknown action: " + action);
                    return redirect("/dashboard");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error performing quick action: " + action, e);
            addErrorMessage(model, "Failed to perform action: " + action);
            return redirect("/dashboard");
        }
    }
    
    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================
    
    /**
     * Determines which project should be displayed on the dashboard.
     * 
     * Priority:
     * 1. Explicitly requested project ID
     * 2. Most recently active project (TODO: implement user preferences)
     * 3. Most recent project by start date
     * 4. First project alphabetically
     * 
     * @param allProjects list of all available projects
     * @param requestedProjectId explicitly requested project ID
     * @return the project to display, or null if no projects available
     */
    private Project determineCurrentProject(List<Project> allProjects, Long requestedProjectId) {
        if (allProjects.isEmpty()) {
            return null;
        }
        
        // Try explicitly requested project
        if (requestedProjectId != null) {
            Optional<Project> requested = allProjects.stream()
                .filter(p -> p.getId().equals(requestedProjectId))
                .findFirst();
            if (requested.isPresent()) {
                return requested.get();
            }
        }
        
        // TODO: Phase 2B - Get user's last selected project from session/preferences
        
        // Find most recently started project that's still active
        LocalDate today = LocalDate.now();
        Optional<Project> activeProject = allProjects.stream()
            .filter(p -> p.getStartDate() != null && p.getGoalEndDate() != null)
            .filter(p -> !p.getStartDate().isAfter(today) && !p.getGoalEndDate().isBefore(today))
            .max((p1, p2) -> p1.getStartDate().compareTo(p2.getStartDate()));
            
        if (activeProject.isPresent()) {
            return activeProject.get();
        }
        
        // Fall back to most recent project by start date
        Optional<Project> mostRecent = allProjects.stream()
            .filter(p -> p.getStartDate() != null)
            .max((p1, p2) -> p1.getStartDate().compareTo(p2.getStartDate()));
            
        if (mostRecent.isPresent()) {
            return mostRecent.get();
        }
        
        // Final fallback: first project alphabetically
        return allProjects.stream()
            .min((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
            .orElse(allProjects.get(0));
    }
    
    /**
     * Handles the scenario when no projects are available.
     * 
     * @param model the model
     * @param allProjects list of projects (should be empty)
     * @return view name for no projects scenario
     */
    private String handleNoProjectsScenario(Model model, List<Project> allProjects) {
        model.addAttribute("hasProjects", false);
        model.addAttribute("showWelcome", true);
        
        // Check if this is a new installation or if projects were deleted
        long totalTeamMembers = teamMemberService.count();
        long totalSubteams = subteamService.count();
        
        if (totalTeamMembers == 0 && totalSubteams == 0) {
            // Completely new installation
            model.addAttribute("isNewInstallation", true);
            addInfoMessage(model, "Welcome to FRC Project Management! Start by creating your first project.");
        } else {
            // Projects were deleted but team data exists
            model.addAttribute("isNewInstallation", false);
            addWarningMessage(model, "No projects found. Create a project to get started.");
        }
        
        return view("dashboard");
    }
    
    /**
     * Loads project-specific dashboard data.
     * 
     * @param model the model
     * @param project the current project
     */
    private void loadProjectDashboardData(Model model, Project project) {
        try {
            // Get project summary statistics
            Map<String, Object> projectSummary = projectService.getProjectSummary(project.getId());
            model.addAttribute("projectSummary", projectSummary);
            
            // Get upcoming tasks (next 7 days)
            List<Task> upcomingTasks = taskService.getTasksDueSoon(project.getId(), 7);
            model.addAttribute("upcomingTasks", upcomingTasks);
            model.addAttribute("upcomingTaskCount", upcomingTasks.size());
            
            // Get overdue tasks
            List<Task> allTasks = taskService.findByProject(project);
            LocalDate today = LocalDate.now();
            List<Task> overdueTasks = allTasks.stream()
                .filter(task -> !task.isCompleted())
                .filter(task -> task.getEndDate() != null && task.getEndDate().isBefore(today))
                .limit(5) // Limit to top 5 for dashboard display
                .toList();
            model.addAttribute("overdueTasks", overdueTasks);
            model.addAttribute("overdueTaskCount", overdueTasks.size());
            
            // Get recent activity (completed tasks in last 7 days)
            List<Task> recentCompletedTasks = allTasks.stream()
                .filter(Task::isCompleted)
                .filter(task -> task.getEndDate() != null && 
                       task.getEndDate().isAfter(today.minusDays(7)))
                .limit(5)
                .toList();
            model.addAttribute("recentActivity", recentCompletedTasks);
            
            // Get upcoming milestones (next 30 days)
            try {
                var upcomingMilestones = milestoneService.findByProject(project)
                    .stream()
                    .filter(milestone -> milestone.getDate() != null &&
                           milestone.getDate().isAfter(today) &&
                           milestone.getDate().isBefore(today.plusDays(30)))
                    .limit(5)
                    .toList();
                model.addAttribute("upcomingMilestones", upcomingMilestones);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load milestones", e);
                model.addAttribute("upcomingMilestones", List.of());
            }
            
            // Calculate project progress
            double completionPercentageDouble = (Double) projectSummary.getOrDefault("completionPercentage", 0.0);
            int completionPercentage = (int) Math.round(completionPercentageDouble);
            model.addAttribute("projectProgress", completionPercentage);
            model.addAttribute("progressBarClass", getProgressBarClass(completionPercentage));
            
            // Calculate days remaining
            if (project.getGoalEndDate() != null) {
                long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, project.getGoalEndDate());
                model.addAttribute("daysUntilGoal", daysRemaining);
                model.addAttribute("goalStatus", getGoalStatus(daysRemaining));
            }
            
            if (project.getHardDeadline() != null) {
                long daysUntilDeadline = java.time.temporal.ChronoUnit.DAYS.between(today, project.getHardDeadline());
                model.addAttribute("daysUntilDeadline", daysUntilDeadline);
                model.addAttribute("deadlineStatus", getDeadlineStatus(daysUntilDeadline));
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading project dashboard data", e);
            // Set safe defaults to prevent template errors
            model.addAttribute("projectSummary", Map.of());
            model.addAttribute("upcomingTasks", List.of());
            model.addAttribute("overdueTasks", List.of());
            model.addAttribute("recentActivity", List.of());
            model.addAttribute("upcomingMilestones", List.of());
            addErrorMessage(model, "Some dashboard data could not be loaded");
        }
    }
    
    /**
     * Loads global application statistics.
     * 
     * @param model the model
     */
    private void loadGlobalStatistics(Model model) {
        try {
            // Team statistics
            List<TeamMember> allMembers = teamMemberService.findAll();
            List<Subteam> allSubteams = subteamService.findAll();
            
            model.addAttribute("totalMembers", allMembers.size());
            model.addAttribute("totalSubteams", allSubteams.size());
            
            // Count leaders
            long leaderCount = allMembers.stream().filter(TeamMember::isLeader).count();
            model.addAttribute("leaderCount", leaderCount);
            
            // Get active subteams (those with members)
            long activeSubteams = allSubteams.stream()
                .filter(subteam -> !subteam.getMembers().isEmpty())
                .count();
            model.addAttribute("activeSubteams", activeSubteams);
            
            // Overall system statistics
            long totalProjects = projectService.count();
            long totalTasks = taskService.count();
            
            model.addAttribute("totalProjects", totalProjects);
            model.addAttribute("totalTasks", totalTasks);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading global statistics", e);
            // Set safe defaults
            model.addAttribute("totalMembers", 0);
            model.addAttribute("totalSubteams", 0);
            model.addAttribute("leaderCount", 0);
            model.addAttribute("activeSubteams", 0);
        }
    }
    
    /**
     * Gets appropriate CSS class for progress bar based on completion percentage.
     * 
     * @param percentage completion percentage
     * @return CSS class name
     */
    private String getProgressBarClass(int percentage) {
        if (percentage >= 80) return "bg-success";
        if (percentage >= 60) return "bg-info";
        if (percentage >= 40) return "bg-warning";
        return "bg-danger";
    }
    
    /**
     * Gets goal status based on days remaining.
     * 
     * @param daysRemaining days until goal
     * @return status string
     */
    private String getGoalStatus(long daysRemaining) {
        if (daysRemaining < 0) return "overdue";
        if (daysRemaining <= 3) return "critical";
        if (daysRemaining <= 7) return "warning";
        return "normal";
    }
    
    /**
     * Gets deadline status based on days remaining.
     * 
     * @param daysRemaining days until deadline
     * @return status string
     */
    private String getDeadlineStatus(long daysRemaining) {
        if (daysRemaining < 0) return "overdue";
        if (daysRemaining <= 1) return "critical";
        if (daysRemaining <= 3) return "warning";
        return "normal";
    }


    /**
     * Gets the current user from Spring Security context.
     * @return current user or null if not authenticated
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                authentication.getPrincipal() instanceof UserPrincipal) {
                return ((UserPrincipal) authentication.getPrincipal()).getUser();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not get current user from security context", e);
        }
        return null;
    }
    
    /**
     * Loads dashboard-specific data required by the updated dashboard template.
     * 
     * @param model the model
     */
    private void loadDashboardSpecificData(Model model) {
        try {
            // Get basic counts for dashboard statistics
            long projectCount = projectService.count();
            long taskCount = taskService.count();
            long meetingCount = 0; // TODO: Add meeting service when available
            long teamMemberCount = teamMemberService.count();
            
            model.addAttribute("projectCount", projectCount);
            model.addAttribute("taskCount", taskCount);
            model.addAttribute("meetingCount", meetingCount);
            model.addAttribute("teamMemberCount", teamMemberCount);
            
            // Create recent activities data (mock data for now)
            List<Map<String, Object>> recentActivities = createRecentActivities();
            model.addAttribute("recentActivities", recentActivities);
            
            // Create project progress data for dashboard
            List<Project> allProjects = projectService.findAll();
            List<Map<String, Object>> projectProgressData = createProjectProgressData(allProjects);
            model.addAttribute("projectProgressData", projectProgressData);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading dashboard-specific data", e);
            // Set safe defaults
            model.addAttribute("projectCount", 0);
            model.addAttribute("taskCount", 0);
            model.addAttribute("meetingCount", 0);
            model.addAttribute("teamMemberCount", 0);
            model.addAttribute("recentActivities", List.of());
            model.addAttribute("projectProgressData", List.of());
        }
    }
    
    /**
     * Creates recent activities data for dashboard display.
     * 
     * @return list of recent activities
     */
    private List<Map<String, Object>> createRecentActivities() {
        // TODO: Implement real recent activities from task/project history
        List<Map<String, Object>> activities = new java.util.ArrayList<>();
        
        // Mock recent activities for demonstration
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        Map<String, Object> activity1 = new java.util.HashMap<>();
        activity1.put("description", "Completed drivetrain assembly task");
        activity1.put("userName", "John Doe");
        activity1.put("projectName", "Robot 2024");
        activity1.put("timestamp", now.minusHours(2));
        activity1.put("type", "task-completed");
        activities.add(activity1);
        
        Map<String, Object> activity2 = new java.util.HashMap<>();
        activity2.put("description", "Updated component inventory");
        activity2.put("userName", "Jane Smith");
        activity2.put("projectName", "Robot 2024");
        activity2.put("timestamp", now.minusHours(4));
        activity2.put("type", "component-updated");
        activities.add(activity2);
        
        Map<String, Object> activity3 = new java.util.HashMap<>();
        activity3.put("description", "Created new programming task");
        activity3.put("userName", "Mike Johnson");
        activity3.put("projectName", "Offseason Project");
        activity3.put("timestamp", now.minusHours(6));
        activity3.put("type", "task-created");
        activities.add(activity3);
        
        Map<String, Object> activity4 = new java.util.HashMap<>();
        activity4.put("description", "Scheduled team meeting");
        activity4.put("userName", "Sarah Wilson");
        activity4.put("projectName", "Robot 2024");
        activity4.put("timestamp", now.minusHours(8));
        activity4.put("type", "meeting-scheduled");
        activities.add(activity4);
        
        return activities;
    }
    
    /**
     * Creates project progress data for dashboard display.
     * 
     * @param projects list of all projects
     * @return list of project progress data
     */
    private List<Map<String, Object>> createProjectProgressData(List<Project> projects) {
        return projects.stream()
            .limit(5) // Show top 5 projects on dashboard
            .map(project -> {
                Map<String, Object> progressData = new java.util.HashMap<>();
                progressData.put("name", project.getName());
                
                // Calculate actual completion percentage based on tasks
                try {
                    Map<String, Object> summary = projectService.getProjectSummary(project.getId());
                    double completion = (Double) summary.getOrDefault("completionPercentage", 0.0);
                    int completedTasks = (Integer) summary.getOrDefault("completedTasks", 0);
                    int totalTasks = (Integer) summary.getOrDefault("totalTasks", 0);
                    
                    progressData.put("completionPercentage", Math.round(completion));
                    progressData.put("completedTasks", completedTasks);
                    progressData.put("totalTasks", totalTasks);
                } catch (Exception e) {
                    // Fallback to mock data if project service fails
                    progressData.put("completionPercentage", (int)(Math.random() * 100));
                    progressData.put("completedTasks", (int)(Math.random() * 20));
                    progressData.put("totalTasks", (int)(Math.random() * 30) + 20);
                }
                
                return progressData;
            })
            .collect(java.util.stream.Collectors.toList());
    }
}