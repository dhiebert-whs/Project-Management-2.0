// src/main/java/org/frcpm/web/controllers/ReportsController.java
// Reports Controller for FRC Project Management

package org.frcpm.web.controllers;

import org.frcpm.models.Project;
import org.frcpm.security.UserPrincipal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ReportsController - Reports and Analytics
 * 
 * Controller for generating and viewing various reports and analytics
 * for FRC project management. Provides insights into project progress,
 * team performance, and resource utilization.
 * 
 * Features:
 * - Project progress reports
 * - Team performance analytics
 * - Component usage reports
 * - Timeline and milestone reports
 * - Custom report generation
 * - Export capabilities
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 */
@Controller
@RequestMapping("/reports")
@PreAuthorize("isAuthenticated()")
public class ReportsController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(ReportsController.class.getName());
    
    /**
     * Main reports dashboard page.
     * 
     * @param model Spring model for template data
     * @param type Optional report type filter
     * @param projectId Optional project filter
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping
    public String reportsDashboard(
            Model model,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading reports dashboard for user: " + user.getUsername());
            
            // Add navigation data
            addNavigationData(model);
            addBreadcrumbs(model, "Reports", "/reports");
            
            // Get all projects for filtering
            List<Project> projects = projectService.findAll();
            model.addAttribute("projects", projects);
            
            // Add report types
            model.addAttribute("reportTypes", createReportTypes());
            model.addAttribute("selectedType", type);
            model.addAttribute("selectedProjectId", projectId);
            
            // Generate summary statistics
            model.addAttribute("totalProjects", projects.size());
            model.addAttribute("activeProjects", getActiveProjectsCount(projects));
            model.addAttribute("completedProjects", getCompletedProjectsCount(projects));
            model.addAttribute("overdueProjects", getOverdueProjectsCount(projects));
            
            // Recent reports
            model.addAttribute("recentReports", createRecentReports());
            
            // Popular reports
            model.addAttribute("popularReports", createPopularReports());
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "reports");
            
            return "reports/dashboard";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading reports dashboard: " + e.getMessage());
            addErrorMessage(model, "Error loading reports dashboard");
            return "error/general";
        }
    }
    
    /**
     * Project progress report.
     * 
     * @param model Spring model for template data
     * @param projectId Optional project filter
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping("/project-progress")
    public String projectProgressReport(
            Model model,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading project progress report");
            
            // Add navigation data
            addNavigationData(model);
            addBreadcrumbs(model, "Reports", "/reports", "Project Progress", "/reports/project-progress");
            
            // Get projects data
            List<Project> projects = projectService.findAll();
            model.addAttribute("projects", projects);
            model.addAttribute("selectedProjectId", projectId);
            
            // Generate project progress data
            model.addAttribute("projectProgressData", createProjectProgressData(projects));
            model.addAttribute("timelineData", createTimelineData());
            model.addAttribute("milestoneData", createMilestoneData());
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "reports");
            
            return "reports/project-progress";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading project progress report: " + e.getMessage());
            addErrorMessage(model, "Error loading project progress report");
            return "error/general";
        }
    }
    
    /**
     * Team performance report.
     * 
     * @param model Spring model for template data
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping("/team-performance")
    public String teamPerformanceReport(
            Model model,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading team performance report");
            
            // Add navigation data
            addNavigationData(model);
            addBreadcrumbs(model, "Reports", "/reports", "Team Performance", "/reports/team-performance");
            
            // Generate team performance data
            model.addAttribute("teamStats", createTeamStats());
            model.addAttribute("memberPerformance", createMemberPerformance());
            model.addAttribute("subteamProgress", createSubteamProgress());
            model.addAttribute("activityData", createActivityData());
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "reports");
            
            return "reports/team-performance";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading team performance report: " + e.getMessage());
            addErrorMessage(model, "Error loading team performance report");
            return "error/general";
        }
    }
    
    /**
     * Component usage report.
     * 
     * @param model Spring model for template data
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping("/component-usage")
    public String componentUsageReport(
            Model model,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading component usage report");
            
            // Add navigation data
            addNavigationData(model);
            addBreadcrumbs(model, "Reports", "/reports", "Component Usage", "/reports/component-usage");
            
            // Generate component usage data
            model.addAttribute("componentStats", createComponentStats());
            model.addAttribute("usageByCategory", createUsageByCategory());
            model.addAttribute("inventoryStatus", createInventoryStatus());
            model.addAttribute("costAnalysis", createCostAnalysis());
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "reports");
            
            return "reports/component-usage";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading component usage report: " + e.getMessage());
            addErrorMessage(model, "Error loading component usage report");
            return "error/general";
        }
    }
    
    /**
     * Custom report builder.
     * 
     * @param model Spring model for template data
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping("/custom")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public String customReportBuilder(
            Model model,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading custom report builder");
            
            // Add navigation data
            addNavigationData(model);
            addBreadcrumbs(model, "Reports", "/reports", "Custom Report", "/reports/custom");
            
            // Add available data sources
            model.addAttribute("dataSources", createDataSources());
            model.addAttribute("chartTypes", createChartTypes());
            model.addAttribute("filterOptions", createFilterOptions());
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "reports");
            
            return "reports/custom";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading custom report builder: " + e.getMessage());
            addErrorMessage(model, "Error loading custom report builder");
            return "error/general";
        }
    }
    
    // Helper methods for mock data generation
    private List<Map<String, Object>> createReportTypes() {
        return List.of(
            Map.of("id", "project-progress", "name", "Project Progress", "icon", "fas fa-chart-line", "description", "Track project completion and milestones"),
            Map.of("id", "team-performance", "name", "Team Performance", "icon", "fas fa-users", "description", "Analyze team productivity and engagement"),
            Map.of("id", "component-usage", "name", "Component Usage", "icon", "fas fa-cogs", "description", "Monitor component inventory and usage"),
            Map.of("id", "timeline", "name", "Timeline Report", "icon", "fas fa-calendar", "description", "View project timelines and deadlines"),
            Map.of("id", "budget", "name", "Budget Analysis", "icon", "fas fa-dollar-sign", "description", "Track project costs and spending"),
            Map.of("id", "custom", "name", "Custom Report", "icon", "fas fa-tools", "description", "Build custom reports with specific criteria")
        );
    }
    
    private List<Map<String, Object>> createRecentReports() {
        return List.of(
            Map.of("name", "Robot 2024 Progress", "type", "Project Progress", "date", "2024-01-15", "size", "2.3 MB"),
            Map.of("name", "Team Performance Q1", "type", "Team Performance", "date", "2024-01-10", "size", "1.8 MB"),
            Map.of("name", "Component Inventory", "type", "Component Usage", "date", "2024-01-08", "size", "945 KB")
        );
    }
    
    private List<Map<String, Object>> createPopularReports() {
        return List.of(
            Map.of("name", "Weekly Progress Summary", "type", "Project Progress", "downloads", 45),
            Map.of("name", "Component Cost Analysis", "type", "Component Usage", "downloads", 32),
            Map.of("name", "Team Activity Report", "type", "Team Performance", "downloads", 28)
        );
    }
    
    private int getActiveProjectsCount(List<Project> projects) {
        LocalDate today = LocalDate.now();
        return (int) projects.stream()
            .filter(p -> p.getStartDate() != null && p.getGoalEndDate() != null)
            .filter(p -> !p.getStartDate().isAfter(today) && !p.getGoalEndDate().isBefore(today))
            .count();
    }
    
    private int getCompletedProjectsCount(List<Project> projects) {
        LocalDate today = LocalDate.now();
        return (int) projects.stream()
            .filter(p -> p.getGoalEndDate() != null && p.getGoalEndDate().isBefore(today))
            .count();
    }
    
    private int getOverdueProjectsCount(List<Project> projects) {
        LocalDate today = LocalDate.now();
        return (int) projects.stream()
            .filter(p -> p.getHardDeadline() != null && p.getHardDeadline().isBefore(today))
            .count();
    }
    
    private List<Map<String, Object>> createProjectProgressData(List<Project> projects) {
        return projects.stream()
            .limit(5) // Show top 5 projects
            .map(project -> {
                Map<String, Object> progressData = new java.util.HashMap<>();
                progressData.put("name", project.getName());
                progressData.put("completion", Math.random() * 100); // Mock completion percentage
                progressData.put("tasksCompleted", (int)(Math.random() * 20));
                progressData.put("totalTasks", (int)(Math.random() * 30) + 20);
                progressData.put("daysRemaining", (int)(Math.random() * 60));
                return progressData;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    private List<Map<String, Object>> createTimelineData() {
        return List.of(
            Map.of("date", "2024-01-01", "event", "Project Start", "type", "milestone"),
            Map.of("date", "2024-01-15", "event", "Design Phase Complete", "type", "milestone"),
            Map.of("date", "2024-02-01", "event", "Prototype Testing", "type", "task"),
            Map.of("date", "2024-02-15", "event", "Final Assembly", "type", "task"),
            Map.of("date", "2024-03-01", "event", "Competition", "type", "deadline")
        );
    }
    
    private List<Map<String, Object>> createMilestoneData() {
        return List.of(
            Map.of("name", "Design Review", "date", "2024-01-20", "status", "Completed", "completion", 100),
            Map.of("name", "Prototype Build", "date", "2024-02-05", "status", "In Progress", "completion", 65),
            Map.of("name", "Testing Phase", "date", "2024-02-20", "status", "Pending", "completion", 0),
            Map.of("name", "Competition Prep", "date", "2024-03-10", "status", "Pending", "completion", 0)
        );
    }
    
    private Map<String, Object> createTeamStats() {
        return Map.of(
            "totalMembers", 25,
            "activeMembers", 22,
            "leaders", 5,
            "averageHours", 12.5,
            "attendanceRate", 88.5
        );
    }
    
    private List<Map<String, Object>> createMemberPerformance() {
        return List.of(
            Map.of("name", "John Doe", "role", "Team Captain", "tasksCompleted", 15, "hoursLogged", 45, "rating", 4.8),
            Map.of("name", "Jane Smith", "role", "Lead Programmer", "tasksCompleted", 12, "hoursLogged", 38, "rating", 4.6),
            Map.of("name", "Mike Johnson", "role", "Mechanical Lead", "tasksCompleted", 18, "hoursLogged", 52, "rating", 4.9)
        );
    }
    
    private List<Map<String, Object>> createSubteamProgress() {
        return List.of(
            Map.of("name", "Programming", "members", 6, "tasksCompleted", 45, "totalTasks", 60, "completion", 75),
            Map.of("name", "Mechanical", "members", 8, "tasksCompleted", 32, "totalTasks", 50, "completion", 64),
            Map.of("name", "Electrical", "members", 4, "tasksCompleted", 28, "totalTasks", 35, "completion", 80),
            Map.of("name", "Strategy", "members", 3, "tasksCompleted", 15, "totalTasks", 20, "completion", 75)
        );
    }
    
    private List<Map<String, Object>> createActivityData() {
        return List.of(
            Map.of("date", "2024-01-15", "activities", 25),
            Map.of("date", "2024-01-16", "activities", 32),
            Map.of("date", "2024-01-17", "activities", 28),
            Map.of("date", "2024-01-18", "activities", 41),
            Map.of("date", "2024-01-19", "activities", 35)
        );
    }
    
    private Map<String, Object> createComponentStats() {
        return Map.of(
            "totalComponents", 125,
            "inUse", 89,
            "available", 28,
            "ordered", 8,
            "totalValue", 15420.50
        );
    }
    
    private List<Map<String, Object>> createUsageByCategory() {
        return List.of(
            Map.of("category", "Motors", "count", 12, "cost", 2400.00, "utilization", 85),
            Map.of("category", "Electronics", "count", 25, "cost", 4200.00, "utilization", 72),
            Map.of("category", "Mechanical", "count", 45, "cost", 3800.00, "utilization", 91),
            Map.of("category", "Pneumatics", "count", 18, "cost", 1850.00, "utilization", 67)
        );
    }
    
    private List<Map<String, Object>> createInventoryStatus() {
        return List.of(
            Map.of("component", "Drive Motors", "current", 4, "minimum", 2, "status", "Good"),
            Map.of("component", "Control System", "current", 0, "minimum", 1, "status", "Critical"),
            Map.of("component", "Pneumatic Cylinders", "current", 2, "minimum", 3, "status", "Low")
        );
    }
    
    private Map<String, Object> createCostAnalysis() {
        return Map.of(
            "budgetTotal", 25000.00,
            "spent", 15420.50,
            "remaining", 9579.50,
            "projectedOverrun", 0.0,
            "categories", List.of(
                Map.of("name", "Electronics", "budget", 8000.00, "spent", 4200.00),
                Map.of("name", "Mechanical", "budget", 10000.00, "spent", 6800.00),
                Map.of("name", "Programming", "budget", 2000.00, "spent", 1200.00),
                Map.of("name", "Miscellaneous", "budget", 5000.00, "spent", 3220.50)
            )
        );
    }
    
    private List<Map<String, Object>> createDataSources() {
        return List.of(
            Map.of("id", "projects", "name", "Projects", "fields", List.of("name", "status", "completion", "deadline")),
            Map.of("id", "tasks", "name", "Tasks", "fields", List.of("title", "status", "priority", "assignee", "completion")),
            Map.of("id", "team", "name", "Team Members", "fields", List.of("name", "role", "subteam", "hours", "tasks")),
            Map.of("id", "components", "name", "Components", "fields", List.of("name", "category", "status", "cost", "usage"))
        );
    }
    
    private List<Map<String, Object>> createChartTypes() {
        return List.of(
            Map.of("id", "bar", "name", "Bar Chart", "icon", "fas fa-chart-bar"),
            Map.of("id", "line", "name", "Line Chart", "icon", "fas fa-chart-line"),
            Map.of("id", "pie", "name", "Pie Chart", "icon", "fas fa-chart-pie"),
            Map.of("id", "table", "name", "Data Table", "icon", "fas fa-table")
        );
    }
    
    private List<Map<String, Object>> createFilterOptions() {
        return List.of(
            Map.of("id", "dateRange", "name", "Date Range", "type", "daterange"),
            Map.of("id", "project", "name", "Project", "type", "select"),
            Map.of("id", "status", "name", "Status", "type", "multiselect"),
            Map.of("id", "team", "name", "Team Member", "type", "select")
        );
    }
}