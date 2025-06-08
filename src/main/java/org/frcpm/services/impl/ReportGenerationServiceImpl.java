// src/main/java/org/frcpm/services/impl/ReportGenerationServiceImpl.java
package org.frcpm.services.impl;

import org.frcpm.models.*;
import org.frcpm.repositories.spring.*;
import org.frcpm.services.GanttDataService;
import org.frcpm.services.MetricsCalculationService;
import org.frcpm.services.ReportGenerationService;
import org.frcpm.services.VisualizationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Spring Boot implementation of the ReportGenerationService interface.
 * Provides methods to generate various reports for project management and analysis.
 * Converted from ServiceLocator pattern to Spring dependency injection.
 */
@Service("reportGenerationServiceImpl")
@Transactional
public class ReportGenerationServiceImpl implements ReportGenerationService {
    
    private static final Logger LOGGER = Logger.getLogger(ReportGenerationServiceImpl.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MilestoneRepository milestoneRepository;
    private final AttendanceRepository attendanceRepository;
    private final MeetingRepository meetingRepository;
    private final SubsystemRepository subsystemRepository;
    private final MetricsCalculationService metricsService;
    private final GanttDataService ganttDataService;
    private final VisualizationService visualizationService;
    
    /**
     * Constructor with dependency injection for Spring Boot.
     * NO @Autowired annotation needed - Spring automatically injects since 4.3+
     */
    public ReportGenerationServiceImpl(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TeamMemberRepository teamMemberRepository,
            MilestoneRepository milestoneRepository,
            AttendanceRepository attendanceRepository,
            MeetingRepository meetingRepository,
            SubsystemRepository subsystemRepository,
            MetricsCalculationService metricsService,
            GanttDataService ganttDataService,
            VisualizationService visualizationService) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.milestoneRepository = milestoneRepository;
        this.attendanceRepository = attendanceRepository;
        this.meetingRepository = meetingRepository;
        this.subsystemRepository = subsystemRepository;
        this.metricsService = metricsService;
        this.ganttDataService = ganttDataService;
        this.visualizationService = visualizationService;
    }
    
    @Override
    public Map<String, Object> generateProjectSummaryReport(Long projectId) {
        LOGGER.info("Generating project summary report for project ID: " + projectId);
        
        Map<String, Object> report = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return report;
            }
            
            Project project = projectOpt.get();
            
            // Basic project information
            report.put("projectId", project.getId());
            report.put("projectName", project.getName());
            report.put("description", project.getDescription());
            report.put("startDate", project.getStartDate().format(DATE_FORMATTER));
            report.put("goalEndDate", project.getGoalEndDate().format(DATE_FORMATTER));
            report.put("hardDeadline", project.getHardDeadline().format(DATE_FORMATTER));
            
            // Get metrics from metrics service
            Map<String, Object> progressMetrics = metricsService.calculateProjectProgressMetrics(projectId);
            report.put("progressMetrics", progressMetrics);
            
            // Task summary
            List<Task> tasks = taskRepository.findByProject(project);
            report.put("totalTasks", tasks.size());
            report.put("completedTasks", tasks.stream().filter(Task::isCompleted).count());
            
            // Milestone summary
            List<Milestone> milestones = milestoneRepository.findByProject(project);
            report.put("totalMilestones", milestones.size());
            report.put("passedMilestones", milestones.stream().filter(Milestone::isPassed).count());
            
            // Team size (unique members from attendance records)
            Set<TeamMember> uniqueMembers = new HashSet<>();
            List<Meeting> meetings = meetingRepository.findByProject(project);
            for (Meeting meeting : meetings) {
                for (Attendance attendance : meeting.getAttendances()) {
                    uniqueMembers.add(attendance.getMember());
                }
            }
            report.put("teamSize", uniqueMembers.size());
            
            // Recent activity
            LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);
            List<Meeting> recentMeetings = meetings.stream()
                .filter(m -> m.getDate().isAfter(oneWeekAgo))
                .collect(Collectors.toList());
            report.put("recentMeetingsCount", recentMeetings.size());
            
            // Risk indicators
            LocalDate today = LocalDate.now();
            long overdueTasks = tasks.stream()
                .filter(t -> !t.isCompleted() && t.getEndDate() != null && t.getEndDate().isBefore(today))
                .count();
            report.put("overdueTasks", overdueTasks);
            
            // Report metadata
            report.put("generatedDate", LocalDate.now().format(DATE_FORMATTER));
            report.put("reportType", "PROJECT_SUMMARY");
            
            return report;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating project summary report", e);
            return report;
        }
    }
    
    @Override
    public Map<String, Object> generateTeamPerformanceReport(Long projectId, LocalDate startDate, LocalDate endDate) {
        LOGGER.info("Generating team performance report for project ID: " + projectId);
        
        Map<String, Object> report = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return report;
            }
            
            Project project = projectOpt.get();
            
            // Apply date filtering
            LocalDate effectiveStartDate = startDate != null ? startDate : project.getStartDate();
            LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
            
            // Basic report information
            report.put("projectId", project.getId());
            report.put("projectName", project.getName());
            report.put("startDate", effectiveStartDate.format(DATE_FORMATTER));
            report.put("endDate", effectiveEndDate.format(DATE_FORMATTER));
            
            // Get metrics from metrics service
            Map<String, Object> teamMetrics = metricsService.calculateTeamPerformanceMetrics(projectId, effectiveStartDate, effectiveEndDate);
            report.put("teamMetrics", teamMetrics);
            
            // Attendance metrics
            Map<String, Object> attendanceMetrics = metricsService.calculateAttendanceMetrics(projectId, effectiveStartDate, effectiveEndDate);
            report.put("attendanceMetrics", attendanceMetrics);
            
            // Individual performance summaries
            List<Map<String, Object>> individualSummaries = new ArrayList<>();
            
            // Get all team members from attendance records
            Set<TeamMember> teamMembers = new HashSet<>();
            List<Meeting> meetings = meetingRepository.findByProject(project);
            for (Meeting meeting : meetings) {
                if (!meeting.getDate().isBefore(effectiveStartDate) && !meeting.getDate().isAfter(effectiveEndDate)) {
                    for (Attendance attendance : meeting.getAttendances()) {
                        teamMembers.add(attendance.getMember());
                    }
                }
            }
            
            // Generate individual summaries
            for (TeamMember member : teamMembers) {
                Map<String, Object> memberSummary = new HashMap<>();
                memberSummary.put("memberId", member.getId());
                memberSummary.put("memberName", member.getFullName());
                
                // Get individual metrics
                Map<String, Object> individualMetrics = metricsService.calculateIndividualPerformanceMetrics(
                    member.getId(), effectiveStartDate, effectiveEndDate);
                memberSummary.put("metrics", individualMetrics);
                
                individualSummaries.add(memberSummary);
            }
            
            report.put("individualSummaries", individualSummaries);
            
            // Report metadata
            report.put("generatedDate", LocalDate.now().format(DATE_FORMATTER));
            report.put("reportType", "TEAM_PERFORMANCE");
            
            return report;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating team performance report", e);
            return report;
        }
    }
    
    @Override
    public Map<String, Object> generateMilestoneStatusReport(Long projectId) {
        LOGGER.info("Generating milestone status report for project ID: " + projectId);
        
        Map<String, Object> report = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return report;
            }
            
            Project project = projectOpt.get();
            
            // Basic report information
            report.put("projectId", project.getId());
            report.put("projectName", project.getName());
            
            // Get all milestones
            List<Milestone> milestones = milestoneRepository.findByProject(project);
            
            // Milestone summary
            report.put("totalMilestones", milestones.size());
            
            // Categorize milestones
            LocalDate today = LocalDate.now();
            List<Map<String, Object>> milestoneDetails = new ArrayList<>();
            
            int passedCount = 0;
            int upcomingCount = 0;
            int overdueCount = 0;
            
            for (Milestone milestone : milestones) {
                Map<String, Object> milestoneData = new HashMap<>();
                milestoneData.put("id", milestone.getId());
                milestoneData.put("name", milestone.getName());
                milestoneData.put("description", milestone.getDescription());
                milestoneData.put("date", milestone.getDate().format(DATE_FORMATTER));
                milestoneData.put("passed", milestone.isPassed());
                
                if (milestone.isPassed()) {
                    milestoneData.put("status", "PASSED");
                    passedCount++;
                } else if (milestone.getDate().isBefore(today)) {
                    milestoneData.put("status", "OVERDUE");
                    overdueCount++;
                } else {
                    milestoneData.put("status", "UPCOMING");
                    upcomingCount++;
                }
                
                // Calculate days until/since milestone
                long daysDifference = today.until(milestone.getDate()).getDays();
                milestoneData.put("daysDifference", daysDifference);
                
                milestoneDetails.add(milestoneData);
            }
            
            // Sort milestones by date
            milestoneDetails.sort((a, b) -> {
                String dateA = (String) a.get("date");
                String dateB = (String) b.get("date");
                return dateA.compareTo(dateB);
            });
            
            report.put("milestoneDetails", milestoneDetails);
            report.put("passedCount", passedCount);
            report.put("upcomingCount", upcomingCount);
            report.put("overdueCount", overdueCount);
            
            // Calculate completion percentage
            double completionPercentage = milestones.size() > 0 ? 
                (double) passedCount / milestones.size() * 100 : 0;
            report.put("completionPercentage", completionPercentage);
            
            // Report metadata
            report.put("generatedDate", LocalDate.now().format(DATE_FORMATTER));
            report.put("reportType", "MILESTONE_STATUS");
            
            return report;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating milestone status report", e);
            return report;
        }
    }
    
    @Override
    public Map<String, Object> generateAttendanceReport(Long projectId, LocalDate startDate, LocalDate endDate) {
        LOGGER.info("Generating attendance report for project ID: " + projectId);
        
        Map<String, Object> report = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return report;
            }
            
            Project project = projectOpt.get();
            
            // Apply date filtering
            LocalDate effectiveStartDate = startDate != null ? startDate : project.getStartDate();
            LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
            
            // Basic report information
            report.put("projectId", project.getId());
            report.put("projectName", project.getName());
            report.put("startDate", effectiveStartDate.format(DATE_FORMATTER));
            report.put("endDate", effectiveEndDate.format(DATE_FORMATTER));
            
            // Get attendance metrics
            Map<String, Object> attendanceMetrics = metricsService.calculateAttendanceMetrics(projectId, effectiveStartDate, effectiveEndDate);
            report.put("attendanceMetrics", attendanceMetrics);
            
            // Get detailed meeting data
            List<Meeting> meetings = meetingRepository.findByProject(project)
                .stream()
                .filter(m -> !m.getDate().isBefore(effectiveStartDate) && !m.getDate().isAfter(effectiveEndDate))
                .collect(Collectors.toList());
            
            List<Map<String, Object>> meetingDetails = new ArrayList<>();
            for (Meeting meeting : meetings) {
                Map<String, Object> meetingData = new HashMap<>();
                meetingData.put("id", meeting.getId());
                meetingData.put("date", meeting.getDate().format(DATE_FORMATTER));
                meetingData.put("type", "TEAM_MEETING"); // Use default type since getType() doesn't exist
                meetingData.put("totalInvited", meeting.getAttendances().size());
                meetingData.put("totalPresent", meeting.getAttendances().stream().filter(Attendance::isPresent).count());
                meetingData.put("attendancePercentage", meeting.getAttendancePercentage());
                
                meetingDetails.add(meetingData);
            }
            
            // Sort meetings by date
            meetingDetails.sort((a, b) -> {
                String dateA = (String) a.get("date");
                String dateB = (String) b.get("date");
                return dateA.compareTo(dateB);
            });
            
            report.put("meetingDetails", meetingDetails);
            
            // Report metadata
            report.put("generatedDate", LocalDate.now().format(DATE_FORMATTER));
            report.put("reportType", "ATTENDANCE");
            
            return report;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating attendance report", e);
            return report;
        }
    }
    
    @Override
    public Map<String, Object> generateSubsystemProgressReport(Long projectId) {
        LOGGER.info("Generating subsystem progress report for project ID: " + projectId);
        
        Map<String, Object> report = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return report;
            }
            
            Project project = projectOpt.get();
            
            // Basic report information
            report.put("projectId", project.getId());
            report.put("projectName", project.getName());
            
            // Get subsystem metrics
            Map<String, Object> subsystemMetrics = metricsService.calculateSubsystemPerformanceMetrics(projectId);
            report.put("subsystemMetrics", subsystemMetrics);
            
            // Get visualization data
            Map<String, Double> subsystemProgress = visualizationService.getSubsystemProgress(projectId);
            report.put("subsystemProgress", subsystemProgress);
            
            // Get all subsystems
            List<Subsystem> subsystems = subsystemRepository.findAll();
            List<Map<String, Object>> subsystemDetails = new ArrayList<>();
            
            for (Subsystem subsystem : subsystems) {
                Map<String, Object> subsystemData = new HashMap<>();
                subsystemData.put("id", subsystem.getId());
                subsystemData.put("name", subsystem.getName());
                subsystemData.put("description", subsystem.getDescription());
                subsystemData.put("status", subsystem.getStatus().toString());
                
                if (subsystem.getResponsibleSubteam() != null) {
                    subsystemData.put("responsibleSubteam", subsystem.getResponsibleSubteam().getName());
                }
                
                // Get tasks for this subsystem
                List<Task> subsystemTasks = taskRepository.findByProject(project)
                    .stream()
                    .filter(t -> subsystem.equals(t.getSubsystem()))
                    .collect(Collectors.toList());
                
                subsystemData.put("totalTasks", subsystemTasks.size());
                subsystemData.put("completedTasks", subsystemTasks.stream().filter(Task::isCompleted).count());
                
                // Calculate progress
                double progress = subsystemProgress.getOrDefault(subsystem.getName(), 0.0);
                subsystemData.put("progress", progress);
                
                subsystemDetails.add(subsystemData);
            }
            
            // Sort by progress (highest first)
            subsystemDetails.sort((a, b) -> 
                Double.compare((Double) b.get("progress"), (Double) a.get("progress")));
            
            report.put("subsystemDetails", subsystemDetails);
            
            // Report metadata
            report.put("generatedDate", LocalDate.now().format(DATE_FORMATTER));
            report.put("reportType", "SUBSYSTEM_PROGRESS");
            
            return report;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating subsystem progress report", e);
            return report;
        }
    }
    
    @Override
    public Map<String, Object> generateTeamMemberReport(Long teamMemberId, LocalDate startDate, LocalDate endDate) {
        LOGGER.info("Generating team member report for member ID: " + teamMemberId);
        
        Map<String, Object> report = new HashMap<>();
        
        try {
            // Get team member
            Optional<TeamMember> memberOpt = teamMemberRepository.findById(teamMemberId);
            if (memberOpt.isEmpty()) {
                LOGGER.warning("Team member not found with ID: " + teamMemberId);
                return report;
            }
            
            TeamMember member = memberOpt.get();
            
            // Apply date filtering
            LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now().minusMonths(3);
            LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
            
            // Basic report information
            report.put("memberId", member.getId());
            report.put("memberName", member.getFullName());
            report.put("startDate", effectiveStartDate.format(DATE_FORMATTER));
            report.put("endDate", effectiveEndDate.format(DATE_FORMATTER));
            
            // Get individual performance metrics
            Map<String, Object> performanceMetrics = metricsService.calculateIndividualPerformanceMetrics(
                teamMemberId, effectiveStartDate, effectiveEndDate);
            report.put("performanceMetrics", performanceMetrics);
            
            // Get assigned tasks
            List<Task> assignedTasks = taskRepository.findByAssignedMember(member);
            List<Map<String, Object>> taskDetails = new ArrayList<>();
            
            for (Task task : assignedTasks) {
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("id", task.getId());
                taskData.put("title", task.getTitle());
                taskData.put("progress", task.getProgress());
                taskData.put("completed", task.isCompleted());
                taskData.put("priority", task.getPriority().toString());
                
                if (task.getStartDate() != null) {
                    taskData.put("startDate", task.getStartDate().format(DATE_FORMATTER));
                }
                if (task.getEndDate() != null) {
                    taskData.put("endDate", task.getEndDate().format(DATE_FORMATTER));
                }
                if (task.getSubsystem() != null) {
                    taskData.put("subsystem", task.getSubsystem().getName());
                }
                
                taskDetails.add(taskData);
            }
            
            report.put("taskDetails", taskDetails);
            
            // Get attendance records
            List<Attendance> attendances = attendanceRepository.findByMember(member);
            
            // Filter by date range
            attendances = attendances.stream()
                .filter(a -> {
                    LocalDate meetingDate = a.getMeeting().getDate();
                    return !meetingDate.isBefore(effectiveStartDate) && !meetingDate.isAfter(effectiveEndDate);
                })
                .collect(Collectors.toList());
            
            List<Map<String, Object>> attendanceDetails = new ArrayList<>();
            for (Attendance attendance : attendances) {
                Map<String, Object> attendanceData = new HashMap<>();
                attendanceData.put("meetingDate", attendance.getMeeting().getDate().format(DATE_FORMATTER));
                attendanceData.put("meetingType", "TEAM_MEETING"); // Use default type since getType() doesn't exist
                attendanceData.put("present", attendance.isPresent());
                
                attendanceDetails.add(attendanceData);
            }
            
            report.put("attendanceDetails", attendanceDetails);
            
            // Report metadata
            report.put("generatedDate", LocalDate.now().format(DATE_FORMATTER));
            report.put("reportType", "TEAM_MEMBER");
            
            return report;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating team member report", e);
            return report;
        }
    }
    
    @Override
    public Map<String, Object> generateProjectTimelineReport(Long projectId) {
        LOGGER.info("Generating project timeline report for project ID: " + projectId);
        
        Map<String, Object> report = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return report;
            }
            
            Project project = projectOpt.get();
            
            // Basic report information
            report.put("projectId", project.getId());
            report.put("projectName", project.getName());
            report.put("startDate", project.getStartDate().format(DATE_FORMATTER));
            report.put("goalEndDate", project.getGoalEndDate().format(DATE_FORMATTER));
            report.put("hardDeadline", project.getHardDeadline().format(DATE_FORMATTER));
            
            // Get timeline deviation metrics
            Map<String, Object> timelineMetrics = metricsService.calculateTimelineDeviationMetrics(projectId);
            report.put("timelineMetrics", timelineMetrics);
            
            // Get Gantt data for visualization
            Map<String, Object> ganttData = ganttDataService.formatTasksForGantt(projectId, null, null);
            report.put("ganttData", ganttData);
            
            // Critical path analysis
            List<Long> criticalPath = ganttDataService.calculateCriticalPath(projectId);
            report.put("criticalPath", criticalPath);
            
            // Task dependencies
            Map<Long, List<Long>> dependencies = ganttDataService.getTaskDependencies(projectId);
            report.put("taskDependencies", dependencies);
            
            // Bottleneck analysis
            List<Long> bottlenecks = ganttDataService.identifyBottlenecks(projectId);
            report.put("bottlenecks", bottlenecks);
            
            // Timeline visualization data
            Map<String, Object> timelineVisualization = new HashMap<>();
            timelineVisualization.put("startDate", project.getStartDate().format(DATE_FORMATTER));
            timelineVisualization.put("endDate", project.getHardDeadline().format(DATE_FORMATTER));
            timelineVisualization.put("currentDate", LocalDate.now().format(DATE_FORMATTER));
            
            // Calculate project phases (if applicable)
            List<Milestone> milestones = milestoneRepository.findByProject(project);
            milestones.sort(Comparator.comparing(Milestone::getDate));
            
            List<Map<String, Object>> phases = new ArrayList<>();
            LocalDate phaseStart = project.getStartDate();
            
            for (int i = 0; i < milestones.size(); i++) {
                Milestone milestone = milestones.get(i);
                Map<String, Object> phase = new HashMap<>();
                phase.put("name", "Phase " + (i + 1) + " - " + milestone.getName());
                phase.put("startDate", phaseStart.format(DATE_FORMATTER));
                phase.put("endDate", milestone.getDate().format(DATE_FORMATTER));
                phase.put("milestone", milestone.getName());
                phase.put("completed", milestone.isPassed());
                
                phases.add(phase);
                phaseStart = milestone.getDate().plusDays(1);
            }
            
            timelineVisualization.put("phases", phases);
            report.put("timelineVisualization", timelineVisualization);
            
            // Report metadata
            report.put("generatedDate", LocalDate.now().format(DATE_FORMATTER));
            report.put("reportType", "PROJECT_TIMELINE");
            
            return report;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating project timeline report", e);
            return report;
        }
    }
    
    @Override
    public byte[] exportReportToPdf(Map<String, Object> reportData, String reportType) {
        LOGGER.info("Exporting report to PDF format: " + reportType);
        
        try {
            // For now, this is a placeholder implementation
            // In a full implementation, you would use a PDF library like iText or Apache PDFBox
            // to generate actual PDF content from the report data
            
            StringBuilder pdfContent = new StringBuilder();
            pdfContent.append("PDF Report Export\n");
            pdfContent.append("Report Type: ").append(reportType).append("\n");
            pdfContent.append("Generated: ").append(reportData.get("generatedDate")).append("\n\n");
            
            // Add basic report data
            for (Map.Entry<String, Object> entry : reportData.entrySet()) {
                if (!entry.getKey().equals("generatedDate") && !entry.getKey().equals("reportType")) {
                    pdfContent.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }
            
            // Convert to bytes (this would be actual PDF bytes in real implementation)
            return pdfContent.toString().getBytes();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting report to PDF", e);
            return new byte[0];
        }
    }
    
    @Override
    public String exportReportToCsv(Map<String, Object> reportData, String reportType) {
        LOGGER.info("Exporting report to CSV format: " + reportType);
        
        try {
            StringBuilder csvContent = new StringBuilder();
            
            // CSV Header
            csvContent.append("Report Type,").append(reportType).append("\n");
            csvContent.append("Generated Date,").append(reportData.get("generatedDate")).append("\n\n");
            
            // Export based on report type
            switch (reportType.toUpperCase()) {
                case "PROJECT_SUMMARY":
                    exportProjectSummaryToCsv(reportData, csvContent);
                    break;
                case "TEAM_PERFORMANCE":
                    exportTeamPerformanceToCsv(reportData, csvContent);
                    break;
                case "ATTENDANCE":
                    exportAttendanceToCsv(reportData, csvContent);
                    break;
                case "MILESTONE_STATUS":
                    exportMilestoneStatusToCsv(reportData, csvContent);
                    break;
                default:
                    // Generic export for other report types
                    exportGenericToCsv(reportData, csvContent);
                    break;
            }
            
            return csvContent.toString();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting report to CSV", e);
            return "";
        }
    }
    
    @Override
    public Map<String, Object> generateCustomReport(Long projectId, List<String> metrics, LocalDate startDate, LocalDate endDate) {
        LOGGER.info("Generating custom report for project ID: " + projectId);
        
        Map<String, Object> report = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return report;
            }
            
            Project project = projectOpt.get();
            
            // Apply date filtering
            LocalDate effectiveStartDate = startDate != null ? startDate : project.getStartDate();
            LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
            
            // Basic report information
            report.put("projectId", project.getId());
            report.put("projectName", project.getName());
            report.put("startDate", effectiveStartDate.format(DATE_FORMATTER));
            report.put("endDate", effectiveEndDate.format(DATE_FORMATTER));
            report.put("requestedMetrics", metrics);
            
            // Generate requested metrics
            Map<String, Object> customMetrics = new HashMap<>();
            
            for (String metric : metrics) {
                switch (metric.toLowerCase()) {
                    case "project_progress":
                        customMetrics.put("projectProgress", 
                            metricsService.calculateProjectProgressMetrics(projectId));
                        break;
                    case "team_performance":
                        customMetrics.put("teamPerformance", 
                            metricsService.calculateTeamPerformanceMetrics(projectId, effectiveStartDate, effectiveEndDate));
                        break;
                    case "task_completion":
                        customMetrics.put("taskCompletion", 
                            metricsService.calculateTaskCompletionMetrics(projectId));
                        break;
                    case "attendance":
                        customMetrics.put("attendance", 
                            metricsService.calculateAttendanceMetrics(projectId, effectiveStartDate, effectiveEndDate));
                        break;
                    case "timeline_deviation":
                        customMetrics.put("timelineDeviation", 
                            metricsService.calculateTimelineDeviationMetrics(projectId));
                        break;
                    case "subsystem_performance":
                        customMetrics.put("subsystemPerformance", 
                            metricsService.calculateSubsystemPerformanceMetrics(projectId));
                        break;
                    case "project_health":
                        customMetrics.put("projectHealth", 
                            metricsService.generateProjectHealthDashboard(projectId));
                        break;
                    case "visualization_data":
                        Map<String, Object> vizData = new HashMap<>();
                        vizData.put("projectCompletion", visualizationService.getProjectCompletionData(projectId));
                        vizData.put("taskStatusSummary", visualizationService.getTaskStatusSummary(projectId));
                        vizData.put("upcomingDeadlines", visualizationService.getUpcomingDeadlines(projectId, 14));
                        vizData.put("subsystemProgress", visualizationService.getSubsystemProgress(projectId));
                        vizData.put("atRiskTasks", visualizationService.getAtRiskTasks(projectId));
                        customMetrics.put("visualizationData", vizData);
                        break;
                    default:
                        LOGGER.warning("Unknown metric requested: " + metric);
                        break;
                }
            }
            
            report.put("customMetrics", customMetrics);
            
            // Report metadata
            report.put("generatedDate", LocalDate.now().format(DATE_FORMATTER));
            report.put("reportType", "CUSTOM");
            
            return report;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating custom report", e);
            return report;
        }
    }
    
    // Spring @Async methods for background processing
    
    @Async
    public CompletableFuture<Map<String, Object>> generateProjectSummaryReportAsync(Long projectId) {
        return CompletableFuture.completedFuture(generateProjectSummaryReport(projectId));
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> generateTeamPerformanceReportAsync(Long projectId, LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.completedFuture(generateTeamPerformanceReport(projectId, startDate, endDate));
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> generateMilestoneStatusReportAsync(Long projectId) {
        return CompletableFuture.completedFuture(generateMilestoneStatusReport(projectId));
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> generateAttendanceReportAsync(Long projectId, LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.completedFuture(generateAttendanceReport(projectId, startDate, endDate));
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> generateSubsystemProgressReportAsync(Long projectId) {
        return CompletableFuture.completedFuture(generateSubsystemProgressReport(projectId));
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> generateTeamMemberReportAsync(Long teamMemberId, LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.completedFuture(generateTeamMemberReport(teamMemberId, startDate, endDate));
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> generateProjectTimelineReportAsync(Long projectId) {
        return CompletableFuture.completedFuture(generateProjectTimelineReport(projectId));
    }
    
    @Async
    public CompletableFuture<byte[]> exportReportToPdfAsync(Map<String, Object> reportData, String reportType) {
        return CompletableFuture.completedFuture(exportReportToPdf(reportData, reportType));
    }
    
    @Async
    public CompletableFuture<String> exportReportToCsvAsync(Map<String, Object> reportData, String reportType) {
        return CompletableFuture.completedFuture(exportReportToCsv(reportData, reportType));
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> generateCustomReportAsync(Long projectId, List<String> metrics, LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.completedFuture(generateCustomReport(projectId, metrics, startDate, endDate));
    }
    
    // Private helper methods for CSV export
    
    private void exportProjectSummaryToCsv(Map<String, Object> reportData, StringBuilder csvContent) {
        csvContent.append("Project Summary\n");
        csvContent.append("Field,Value\n");
        csvContent.append("Project Name,").append(reportData.get("projectName")).append("\n");
        csvContent.append("Start Date,").append(reportData.get("startDate")).append("\n");
        csvContent.append("Goal End Date,").append(reportData.get("goalEndDate")).append("\n");
        csvContent.append("Hard Deadline,").append(reportData.get("hardDeadline")).append("\n");
        csvContent.append("Total Tasks,").append(reportData.get("totalTasks")).append("\n");
        csvContent.append("Completed Tasks,").append(reportData.get("completedTasks")).append("\n");
        csvContent.append("Total Milestones,").append(reportData.get("totalMilestones")).append("\n");
        csvContent.append("Passed Milestones,").append(reportData.get("passedMilestones")).append("\n");
        csvContent.append("Team Size,").append(reportData.get("teamSize")).append("\n");
        csvContent.append("Overdue Tasks,").append(reportData.get("overdueTasks")).append("\n");
    }
    
    private void exportTeamPerformanceToCsv(Map<String, Object> reportData, StringBuilder csvContent) {
        csvContent.append("Team Performance Summary\n");
        csvContent.append("Member Name,Tasks Assigned,Tasks Completed,Completion Rate,Attendance Rate\n");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> individualSummaries = (List<Map<String, Object>>) reportData.get("individualSummaries");
        
        if (individualSummaries != null) {
            for (Map<String, Object> summary : individualSummaries) {
                csvContent.append(summary.get("memberName")).append(",");
                
                @SuppressWarnings("unchecked")
                Map<String, Object> metrics = (Map<String, Object>) summary.get("metrics");
                if (metrics != null) {
                    csvContent.append(metrics.get("totalAssignedTasks")).append(",");
                    csvContent.append(metrics.get("completedTasks")).append(",");
                    csvContent.append(metrics.get("taskCompletionRate")).append(",");
                    csvContent.append(metrics.get("attendanceRate"));
                }
                csvContent.append("\n");
            }
        }
    }
    
    private void exportAttendanceToCsv(Map<String, Object> reportData, StringBuilder csvContent) {
        csvContent.append("Attendance Report\n");
        csvContent.append("Meeting Date,Meeting Type,Total Invited,Total Present,Attendance Percentage\n");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> meetingDetails = (List<Map<String, Object>>) reportData.get("meetingDetails");
        
        if (meetingDetails != null) {
            for (Map<String, Object> meeting : meetingDetails) {
                csvContent.append(meeting.get("date")).append(",");
                csvContent.append("TEAM_MEETING").append(","); // Use consistent default type
                csvContent.append(meeting.get("totalInvited")).append(",");
                csvContent.append(meeting.get("totalPresent")).append(",");
                csvContent.append(meeting.get("attendancePercentage")).append("\n");
            }
        }
    }
    
    private void exportMilestoneStatusToCsv(Map<String, Object> reportData, StringBuilder csvContent) {
        csvContent.append("Milestone Status Report\n");
        csvContent.append("Milestone Name,Date,Status,Days Difference\n");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> milestoneDetails = (List<Map<String, Object>>) reportData.get("milestoneDetails");
        
        if (milestoneDetails != null) {
            for (Map<String, Object> milestone : milestoneDetails) {
                csvContent.append(milestone.get("name")).append(",");
                csvContent.append(milestone.get("date")).append(",");
                csvContent.append(milestone.get("status")).append(",");
                csvContent.append(milestone.get("daysDifference")).append("\n");
            }
        }
    }
    
    private void exportGenericToCsv(Map<String, Object> reportData, StringBuilder csvContent) {
        csvContent.append("Generic Report Data\n");
        csvContent.append("Field,Value\n");
        
        for (Map.Entry<String, Object> entry : reportData.entrySet()) {
            if (!entry.getKey().equals("generatedDate") && !entry.getKey().equals("reportType")) {
                csvContent.append(entry.getKey()).append(",");
                csvContent.append(entry.getValue() != null ? entry.getValue().toString() : "").append("\n");
            }
        }
    }
}