// src/main/java/org/frcpm/web/controllers/AnalyticsController.java
// Phase 3B: Analytics Controller for Advanced Reporting & Analytics

package org.frcpm.web.controllers;

import org.frcpm.models.Project;
import org.frcpm.models.User;
import org.frcpm.services.ProjectService;
import org.frcpm.services.ReportingService;
import org.frcpm.services.UserService;
// import org.frcpm.web.websocket.WebSocketMessageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for analytics and reporting features.
 * 
 * Provides web endpoints for:
 * - Analytics dashboard
 * - Project reporting
 * - Team performance metrics
 * - Risk analysis
 * - Data export
 * - Custom reports
 * 
 * @author FRC Project Management Team
 * @version 3.0.0-3B
 * @since Phase 3B - Advanced Reporting & Analytics
 */
@Controller
@RequestMapping("/analytics")
@PreAuthorize("isAuthenticated()")
public class AnalyticsController {
    
    private static final Logger LOGGER = Logger.getLogger(AnalyticsController.class.getName());
    
    @Autowired
    private ReportingService reportingService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserService userService;
    
    // @Autowired
    // private WebSocketMessageController webSocketController;
    
    // =========================================================================
    // ANALYTICS DASHBOARD
    // =========================================================================
    
    /**
     * Display analytics dashboard.
     */
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long projectId,
                           Authentication auth,
                           Model model) {
        try {
            LOGGER.info("Loading analytics dashboard for user: " + auth.getName());
            
            // Get projects for user
            User user = userService.findByUsername(auth.getName()).orElse(null);
            List<Project> projects = user != null ? projectService.findAll() : List.of();
            
            // Select project
            Project selectedProject = null;
            if (projectId != null) {
                selectedProject = projectService.findById(projectId);
            } else if (!projects.isEmpty()) {
                selectedProject = projects.get(0);
            }
            
            model.addAttribute("projects", projects);
            model.addAttribute("selectedProject", selectedProject);
            model.addAttribute("lastUpdated", LocalDateTime.now());
            
            if (selectedProject != null) {
                // Load analytics data
                Map<String, Object> analytics = reportingService.getProjectAnalytics(selectedProject.getId());
                Map<String, Object> teamPerformance = reportingService.getTeamPerformanceMetrics(selectedProject.getId());
                Map<String, Object> riskAnalysis = reportingService.getProjectRiskAnalysis(selectedProject.getId());
                Map<String, Object> forecast = reportingService.getProjectCompletionForecast(selectedProject.getId());
                
                model.addAttribute("analytics", analytics);
                model.addAttribute("teamPerformance", teamPerformance);
                model.addAttribute("riskAnalysis", riskAnalysis);
                model.addAttribute("forecast", forecast);
                
                LOGGER.info("Loaded analytics for project: " + selectedProject.getName());
            } else {
                // Empty analytics for users with no projects
                model.addAttribute("analytics", new HashMap<>());
                model.addAttribute("teamPerformance", new HashMap<>());
                model.addAttribute("riskAnalysis", new HashMap<>());
                model.addAttribute("forecast", new HashMap<>());
            }
            
            return "analytics/dashboard";
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading analytics dashboard", e);
            model.addAttribute("error", "Error loading analytics dashboard");
            return "error";
        }
    }
    
    /**
     * Display executive dashboard.
     */
    @GetMapping("/executive")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public String executiveDashboard(@RequestParam(required = false) Long projectId,
                                   Authentication auth,
                                   Model model) {
        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            List<Project> projects = user != null ? projectService.findAll() : List.of();
            
            Project selectedProject = null;
            if (projectId != null) {
                selectedProject = projectService.findById(projectId);
            } else if (!projects.isEmpty()) {
                selectedProject = projects.get(0);
            }
            
            model.addAttribute("projects", projects);
            model.addAttribute("selectedProject", selectedProject);
            
            if (selectedProject != null) {
                Map<String, Object> executiveData = reportingService.getExecutiveDashboardData(selectedProject.getId());
                model.addAttribute("executiveData", executiveData);
            }
            
            return "analytics/executive";
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading executive dashboard", e);
            model.addAttribute("error", "Error loading executive dashboard");
            return "error";
        }
    }
    
    /**
     * Display project manager dashboard.
     */
    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public String managerDashboard(@RequestParam(required = false) Long projectId,
                                 Authentication auth,
                                 Model model) {
        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            List<Project> projects = user != null ? projectService.findAll() : List.of();
            
            Project selectedProject = null;
            if (projectId != null) {
                selectedProject = projectService.findById(projectId);
            } else if (!projects.isEmpty()) {
                selectedProject = projects.get(0);
            }
            
            model.addAttribute("projects", projects);
            model.addAttribute("selectedProject", selectedProject);
            
            if (selectedProject != null) {
                Map<String, Object> managerData = reportingService.getProjectManagerDashboardData(selectedProject.getId());
                model.addAttribute("managerData", managerData);
            }
            
            return "analytics/manager";
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading manager dashboard", e);
            model.addAttribute("error", "Error loading manager dashboard");
            return "error";
        }
    }
    
    /**
     * Display student dashboard.
     */
    @GetMapping("/student")
    @PreAuthorize("hasAnyRole('STUDENT', 'MENTOR', 'ADMIN')")
    public String studentDashboard(@RequestParam(required = false) Long projectId,
                                 Authentication auth,
                                 Model model) {
        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            List<Project> projects = user != null ? projectService.findAll() : List.of();
            
            Project selectedProject = null;
            if (projectId != null) {
                selectedProject = projectService.findById(projectId);
            } else if (!projects.isEmpty()) {
                selectedProject = projects.get(0);
            }
            
            model.addAttribute("projects", projects);
            model.addAttribute("selectedProject", selectedProject);
            
            if (selectedProject != null) {
                Map<String, Object> studentData = reportingService.getStudentDashboardData(selectedProject.getId(), user.getId());
                model.addAttribute("studentData", studentData);
            }
            
            return "analytics/student";
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading student dashboard", e);
            model.addAttribute("error", "Error loading student dashboard");
            return "error";
        }
    }
    
    // =========================================================================
    // REST API ENDPOINTS
    // =========================================================================
    
    /**
     * Get project analytics data.
     */
    @GetMapping("/api/project/{projectId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProjectAnalytics(@PathVariable Long projectId) {
        try {
            Map<String, Object> analytics = reportingService.getProjectAnalytics(projectId);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting project analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get team performance metrics.
     */
    @GetMapping("/api/team/{projectId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTeamPerformanceMetrics(@PathVariable Long projectId) {
        try {
            Map<String, Object> metrics = reportingService.getTeamPerformanceMetrics(projectId);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting team performance metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get risk analysis.
     */
    @GetMapping("/api/risk/{projectId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRiskAnalysis(@PathVariable Long projectId) {
        try {
            Map<String, Object> risk = reportingService.getProjectRiskAnalysis(projectId);
            return ResponseEntity.ok(risk);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting risk analysis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get completion forecast.
     */
    @GetMapping("/api/forecast/{projectId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCompletionForecast(@PathVariable Long projectId) {
        try {
            Map<String, Object> forecast = reportingService.getProjectCompletionForecast(projectId);
            return ResponseEntity.ok(forecast);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting completion forecast", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get progress analytics for date range.
     */
    @GetMapping("/api/progress/{projectId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProgressAnalytics(
            @PathVariable Long projectId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        try {
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            Map<String, Object> progress = reportingService.getProjectProgressAnalytics(projectId, startDate, endDate);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting progress analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get chart data for visualization.
     */
    @GetMapping("/api/chart/{projectId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getChartData(
            @PathVariable Long projectId,
            @RequestParam String chartType,
            @RequestParam(required = false) Map<String, Object> parameters) {
        try {
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            
            Map<String, Object> chartData = reportingService.getChartData(projectId, chartType, parameters);
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting chart data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // =========================================================================
    // CUSTOM REPORTS
    // =========================================================================
    
    /**
     * Display custom reports page.
     */
    @GetMapping("/reports")
    public String customReports(@RequestParam(required = false) Long projectId,
                               Authentication auth,
                               Model model) {
        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            List<Project> projects = user != null ? projectService.findAll() : List.of();
            
            Project selectedProject = null;
            if (projectId != null) {
                selectedProject = projectService.findById(projectId);
            } else if (!projects.isEmpty()) {
                selectedProject = projects.get(0);
            }
            
            model.addAttribute("projects", projects);
            model.addAttribute("selectedProject", selectedProject);
            
            // Get available report templates
            List<Map<String, Object>> templates = reportingService.getAvailableReportTemplates();
            model.addAttribute("reportTemplates", templates);
            
            // Get saved templates for user
            List<Map<String, Object>> savedTemplates = reportingService.getSavedReportTemplates(user.getId());
            model.addAttribute("savedTemplates", savedTemplates);
            
            return "analytics/reports";
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading custom reports", e);
            model.addAttribute("error", "Error loading custom reports");
            return "error";
        }
    }
    
    /**
     * Generate custom report.
     */
    @PostMapping("/api/reports/generate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateCustomReport(
            @RequestBody Map<String, Object> reportRequest) {
        try {
            String reportType = (String) reportRequest.get("reportType");
            Map<String, Object> parameters = (Map<String, Object>) reportRequest.get("parameters");
            
            Map<String, Object> report = reportingService.generateCustomReport(reportType, parameters);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating custom report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Generate report asynchronously.
     */
    @PostMapping("/api/reports/generate-async")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateReportAsync(
            @RequestBody Map<String, Object> reportRequest) {
        try {
            Long projectId = ((Number) reportRequest.get("projectId")).longValue();
            String reportType = (String) reportRequest.get("reportType");
            
            CompletableFuture<Map<String, Object>> future = 
                reportingService.generateAnalyticsReportAsync(projectId, reportType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "PROCESSING");
            response.put("message", "Report generation started");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting async report generation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // =========================================================================
    // DATA EXPORT
    // =========================================================================
    
    /**
     * Export project data to CSV.
     */
    @GetMapping("/export/{projectId}/csv")
    public ResponseEntity<byte[]> exportToCsv(@PathVariable Long projectId,
                                            @RequestParam(defaultValue = "false") boolean includeDetails) {
        try {
            byte[] csvData = reportingService.exportProjectDataToCsv(projectId, includeDetails);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "project-" + projectId + "-data.csv");
            
            return ResponseEntity.ok().headers(headers).body(csvData);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting to CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Export project data to Excel.
     */
    @GetMapping("/export/{projectId}/excel")
    public ResponseEntity<byte[]> exportToExcel(@PathVariable Long projectId,
                                              @RequestParam(defaultValue = "false") boolean includeDetails) {
        try {
            byte[] excelData = reportingService.exportProjectDataToExcel(projectId, includeDetails);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "project-" + projectId + "-data.xlsx");
            
            return ResponseEntity.ok().headers(headers).body(excelData);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting to Excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Export project data to PDF.
     */
    @GetMapping("/export/{projectId}/pdf")
    public ResponseEntity<byte[]> exportToPdf(@PathVariable Long projectId,
                                            @RequestParam(defaultValue = "project_overview") String reportType) {
        try {
            byte[] pdfData = reportingService.exportProjectDataToPdf(projectId, reportType);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "project-" + projectId + "-report.pdf");
            
            return ResponseEntity.ok().headers(headers).body(pdfData);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting to PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Export analytics data to JSON.
     */
    @GetMapping("/export/{projectId}/json")
    @ResponseBody
    public ResponseEntity<String> exportToJson(@PathVariable Long projectId,
                                             @RequestParam(defaultValue = "project_analytics") String analyticsType) {
        try {
            String jsonData = reportingService.exportAnalyticsToJson(projectId, analyticsType);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "project-" + projectId + "-analytics.json");
            
            return ResponseEntity.ok().headers(headers).body(jsonData);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting to JSON", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // =========================================================================
    // COMPARATIVE ANALYTICS
    // =========================================================================
    
    /**
     * Display comparative analytics page.
     */
    @GetMapping("/compare")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public String compareProjects(@RequestParam(required = false) Long projectId,
                                @RequestParam(required = false) List<Long> compareWith,
                                Authentication auth,
                                Model model) {
        try {
            User user = userService.findByUsername(auth.getName()).orElse(null);
            List<Project> projects = user != null ? projectService.findAll() : List.of();
            
            Project selectedProject = null;
            if (projectId != null) {
                selectedProject = projectService.findById(projectId);
            } else if (!projects.isEmpty()) {
                selectedProject = projects.get(0);
            }
            
            model.addAttribute("projects", projects);
            model.addAttribute("selectedProject", selectedProject);
            
            if (selectedProject != null && compareWith != null && !compareWith.isEmpty()) {
                Map<String, Object> comparison = reportingService.getComparativeProjectAnalytics(
                    selectedProject.getId(), compareWith);
                model.addAttribute("comparison", comparison);
            }
            
            return "analytics/compare";
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading comparative analytics", e);
            model.addAttribute("error", "Error loading comparative analytics");
            return "error";
        }
    }
    
    /**
     * Get comparative analytics data.
     */
    @GetMapping("/api/compare/{projectId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getComparativeAnalytics(
            @PathVariable Long projectId,
            @RequestParam List<Long> compareWith) {
        try {
            Map<String, Object> comparison = reportingService.getComparativeProjectAnalytics(projectId, compareWith);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting comparative analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // =========================================================================
    // REAL-TIME UPDATES
    // =========================================================================
    
    /**
     * Refresh analytics data and notify via WebSocket.
     */
    @PostMapping("/api/refresh/{projectId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> refreshAnalytics(@PathVariable Long projectId,
                                                              Authentication auth) {
        try {
            // Refresh analytics data
            Map<String, Object> analytics = reportingService.getProjectAnalytics(projectId);
            
            // Notify via WebSocket
            Map<String, Object> updateMessage = new HashMap<>();
            updateMessage.put("type", "ANALYTICS_UPDATED");
            updateMessage.put("projectId", projectId);
            updateMessage.put("updatedBy", auth.getName());
            updateMessage.put("timestamp", LocalDateTime.now());
            
            // webSocketController.sendToProject(projectId, "/topic/analytics", updateMessage);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Analytics refreshed successfully");
            response.put("analytics", analytics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error refreshing analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    
    /**
     * Get available chart types.
     */
    @GetMapping("/api/chart-types")
    @ResponseBody
    public ResponseEntity<List<String>> getAvailableChartTypes() {
        try {
            List<String> chartTypes = reportingService.getAvailableChartTypes();
            return ResponseEntity.ok(chartTypes);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting chart types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get chart configuration options.
     */
    @GetMapping("/api/chart-config/{chartType}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getChartConfigurationOptions(@PathVariable String chartType) {
        try {
            Map<String, Object> options = reportingService.getChartConfigurationOptions(chartType);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting chart configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}