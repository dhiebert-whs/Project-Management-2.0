// src/main/java/org/frcpm/services/impl/ReportGenerationServiceImpl.java
package org.frcpm.services.impl;

import org.frcpm.models.*;
import org.frcpm.repositories.specific.*;
import org.frcpm.services.GanttDataService;
import org.frcpm.services.MetricsCalculationService;
import org.frcpm.services.ReportGenerationService;
import org.frcpm.services.VisualizationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the ReportGenerationService interface.
 * Provides methods to generate reports for project management.
 */
public class ReportGenerationServiceImpl implements ReportGenerationService {
    
    private static final Logger LOGGER = Logger.getLogger(ReportGenerationServiceImpl.class.getName());
    
    private ProjectRepository projectRepository;
    private TaskRepository taskRepository;
    private TeamMemberRepository teamMemberRepository;
    private MilestoneRepository milestoneRepository;
    private AttendanceRepository attendanceRepository;
    private MeetingRepository meetingRepository;
    private SubsystemRepository subsystemRepository;
    private MetricsCalculationService metricsService;
    private GanttDataService ganttDataService;
    private VisualizationService visualizationService;
    
    /**
     * Default constructor for MVVMFx dependency injection.
     */
    public ReportGenerationServiceImpl() {
        // Default constructor for dependency injection
    }
    
    /**
     * Constructor with repository and service injection for testing.
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

        // Add report metadata
        report.put("reportType", "Project Summary");
        report.put("generatedDate", LocalDate.now());
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (!projectOpt.isPresent()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return report;
            }
            
            Project project = projectOpt.get();
                        
            // Add project details
            Map<String, Object> projectDetails = new HashMap<>();
            projectDetails.put("name", project.getName());
            projectDetails.put("description", project.getDescription());
            projectDetails.put("startDate", project.getStartDate());
            projectDetails.put("goalEndDate", project.getGoalEndDate());
            projectDetails.put("hardDeadline", project.getHardDeadline());
            
            report.put("projectDetails", projectDetails);
            
            // Get tasks for the project
            List<Task> tasks = taskRepository.findByProject(project);
            
            // Add task statistics
            Map<String, Object> taskStats = new HashMap<>();
            taskStats.put("totalTasks", tasks.size());
            
            long completedTasks = tasks.stream().filter(Task::isCompleted).count();
            taskStats.put("completedTasks", completedTasks);
            
            long inProgressTasks = tasks.stream()
                    .filter(t -> !t.isCompleted() && t.getProgress() > 0).count();
            taskStats.put("inProgressTasks", inProgressTasks);
            
            long notStartedTasks = tasks.size() - completedTasks - inProgressTasks;
            taskStats.put("notStartedTasks", notStartedTasks);
            
            // Calculate completion percentage
            double completionPercentage = tasks.isEmpty() ? 0.0 :
                    tasks.stream().mapToInt(Task::getProgress).sum() / (double) tasks.size();
            taskStats.put("completionPercentage", completionPercentage);
            
            report.put("taskStatistics", taskStats);
            
            // Get milestones for the project
            List<Milestone> milestones = milestoneRepository.findByProject(project);
            
            // Add milestone statistics
            Map<String, Object> milestoneStats = new HashMap<>();
            milestoneStats.put("totalMilestones", milestones.size());
            
            long passedMilestones = milestones.stream().filter(Milestone::isPassed).count();
            milestoneStats.put("passedMilestones", passedMilestones);
            
            // Add upcoming milestones
            LocalDate today = LocalDate.now();
            List<Map<String, Object>> upcomingMilestones = milestones.stream()
                    .filter(m -> !m.getDate().isBefore(today))
                    .sorted(Comparator.comparing(Milestone::getDate))
                    .limit(5)
                    .map(m -> {
                        Map<String, Object> milestone = new HashMap<>();
                        milestone.put("id", m.getId());
                        milestone.put("name", m.getName());
                        milestone.put("date", m.getDate());
                        milestone.put("daysUntil", m.getDaysUntil());
                        return milestone;
                    })
                    .collect(Collectors.toList());
            
            milestoneStats.put("upcomingMilestones", upcomingMilestones);
            
            report.put("milestoneStatistics", milestoneStats);
            
            // Add team statistics
            Map<String, Object> teamStats = new HashMap<>();
            
            // Get all team members by looking at task assignments
            Set<TeamMember> teamMembers = new HashSet<>();
            for (Task task : tasks) {
                teamMembers.addAll(task.getAssignedTo());
            }
            
            teamStats.put("totalTeamMembers", teamMembers.size());
            
            // Get task distribution by team member
            Map<Long, Integer> tasksByMember = new HashMap<>();
            for (TeamMember member : teamMembers) {
                int memberTasks = 0;
                for (Task task : tasks) {
                    if (task.getAssignedTo().contains(member)) {
                        memberTasks++;
                    }
                }
                tasksByMember.put(member.getId(), memberTasks);
            }
            
            teamStats.put("tasksByMember", tasksByMember);
            
            report.put("teamStatistics", teamStats);
            
            // Get timeline statistics
            Map<String, Object> timelineStats = new HashMap<>();
            
            LocalDate currentDate = LocalDate.now();
            long totalDays = project.getStartDate().until(project.getGoalEndDate()).getDays();
            long elapsedDays = project.getStartDate().until(currentDate).getDays();
            long remainingDays = Math.max(0, project.getGoalEndDate().until(currentDate).getDays());
            
            timelineStats.put("totalDays", totalDays);
            timelineStats.put("elapsedDays", elapsedDays);
            timelineStats.put("remainingDays", remainingDays);
            
            // Calculate time progression percentage
            double timeProgressPercentage = totalDays > 0 ? 100.0 * elapsedDays / totalDays : 0.0;
            timelineStats.put("timeProgressPercentage", timeProgressPercentage);
            
            // Calculate schedule variance
            double scheduleVariance = completionPercentage - timeProgressPercentage;
            timelineStats.put("scheduleVariance", scheduleVariance);
            
            String scheduleStatus;
            if (Math.abs(scheduleVariance) <= 5) {
                scheduleStatus = "On Schedule";
            } else if (scheduleVariance < 0) {
                scheduleStatus = "Behind Schedule";
            } else {
                scheduleStatus = "Ahead of Schedule";
            }
            timelineStats.put("scheduleStatus", scheduleStatus);
            
            report.put("timelineStatistics", timelineStats);
            
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
            if (!projectOpt.isPresent()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return report;
            }
            
            Project project = projectOpt.get();
            
            // Add report metadata
            report.put("reportType", "Team Performance");
            report.put("generatedDate", LocalDate.now());
            report.put("startDate", startDate);
            report.put("endDate", endDate);
            
            // Add project details
            Map<String, Object> projectDetails = new HashMap<>();
            projectDetails.put("id", project.getId());
            projectDetails.put("name", project.getName());
            
            report.put("projectDetails", projectDetails);
            
            // Get team performance metrics
            Map<String, Object> teamMetrics = metricsService.calculateTeamPerformanceMetrics(projectId, startDate, endDate);
            report.put("teamMetrics", teamMetrics);
            
            // Get attendance metrics
            Map<String, Object> attendanceMetrics = metricsService.calculateAttendanceMetrics(projectId, startDate, endDate);
            report.put("attendanceMetrics", attendanceMetrics);
            
            // Get task completion metrics
            Map<String, Object> taskCompletionMetrics = metricsService.calculateTaskCompletionMetrics(projectId);
            report.put("taskCompletionMetrics", taskCompletionMetrics);
            
            // Get individual member performance
            Map<String, Object> individualPerformance = new HashMap<>();
            
            // Get all team members involved in the project
            Set<TeamMember> teamMembers = new HashSet<>();
            List<Task> tasks = taskRepository.findByProject(project);
            for (Task task : tasks) {
                teamMembers.addAll(task.getAssignedTo());
            }
            
            // Calculate metrics for each team member
            for (TeamMember member : teamMembers) {
                Map<String, Object> memberMetrics = metricsService.calculateIndividualPerformanceMetrics(
                        member.getId(), startDate, endDate);
                individualPerformance.put(member.getId().toString(), memberMetrics);
            }
            
            report.put("individualPerformance", individualPerformance);
            
            return report;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating team performance report", e);
            return report;
        }
    }
    
// Continuing src/main/java/org/frcpm/services/impl/ReportGenerationServiceImpl.java

@Override
public Map<String, Object> generateMilestoneStatusReport(Long projectId) {
    LOGGER.info("Generating milestone status report for project ID: " + projectId);
    
    Map<String, Object> report = new HashMap<>();
    
    try {
        // Get project
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            LOGGER.warning("Project not found with ID: " + projectId);
            return report;
        }
        
        Project project = projectOpt.get();
        
        // Add report metadata
        report.put("reportType", "Milestone Status");
        report.put("generatedDate", LocalDate.now());
        
        // Add project details
        Map<String, Object> projectDetails = new HashMap<>();
        projectDetails.put("id", project.getId());
        projectDetails.put("name", project.getName());
        projectDetails.put("startDate", project.getStartDate());
        projectDetails.put("goalEndDate", project.getGoalEndDate());
        projectDetails.put("hardDeadline", project.getHardDeadline());
        
        report.put("projectDetails", projectDetails);
        
        // Get all milestones for the project
        List<Milestone> milestones = milestoneRepository.findByProject(project);
        
        // Add milestone statistics
        Map<String, Object> milestoneStats = new HashMap<>();
        milestoneStats.put("totalMilestones", milestones.size());
        
        LocalDate today = LocalDate.now();
        
        long passedMilestones = milestones.stream()
                .filter(m -> m.getDate().isBefore(today))
                .count();
        milestoneStats.put("passedMilestones", passedMilestones);
        
        long upcomingMilestones = milestones.stream()
                .filter(m -> m.getDate().isEqual(today) || m.getDate().isAfter(today))
                .count();
        milestoneStats.put("upcomingMilestones", upcomingMilestones);
        
        // Calculate milestone completion percentage
        double milestoneCompletionPercentage = milestones.isEmpty() ? 0.0 :
                100.0 * passedMilestones / milestones.size();
        milestoneStats.put("milestoneCompletionPercentage", milestoneCompletionPercentage);
        
        report.put("milestoneStatistics", milestoneStats);
        
        // Add detailed milestone data
        List<Map<String, Object>> milestoneDetails = new ArrayList<>();
        
        for (Milestone milestone : milestones) {
            Map<String, Object> milestoneDetail = new HashMap<>();
            milestoneDetail.put("id", milestone.getId());
            milestoneDetail.put("name", milestone.getName());
            milestoneDetail.put("description", milestone.getDescription());
            milestoneDetail.put("date", milestone.getDate());
            milestoneDetail.put("isPassed", milestone.isPassed());
            milestoneDetail.put("daysUntil", milestone.getDaysUntil());
            
            // Status calculation
            String status;
            if (milestone.isPassed()) {
                status = "Completed";
            } else {
                long daysUntil = milestone.getDaysUntil();
                if (daysUntil < 0) {
                    status = "Overdue";
                } else if (daysUntil == 0) {
                    status = "Due Today";
                } else if (daysUntil <= 7) {
                    status = "Upcoming (< 1 week)";
                } else if (daysUntil <= 30) {
                    status = "Upcoming (< 1 month)";
                } else {
                    status = "Future";
                }
            }
            milestoneDetail.put("status", status);
            
            milestoneDetails.add(milestoneDetail);
        }
        
        // Sort milestones by date
        milestoneDetails.sort(Comparator.comparing(m -> ((LocalDate) m.get("date"))));
        
        report.put("milestones", milestoneDetails);
        
        // Add timeline visualization data
        List<Map<String, Object>> timelineData = new ArrayList<>();
        
        for (Milestone milestone : milestones) {
            Map<String, Object> timelineItem = new HashMap<>();
            timelineItem.put("date", milestone.getDate());
            timelineItem.put("name", milestone.getName());
            timelineItem.put("isPassed", milestone.isPassed());
            
            timelineData.add(timelineItem);
        }
        
        report.put("timelineData", timelineData);
        
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
        if (!projectOpt.isPresent()) {
            LOGGER.warning("Project not found with ID: " + projectId);
            return report;
        }
        
        Project project = projectOpt.get();
        
        // Add report metadata
        report.put("reportType", "Attendance");
        report.put("generatedDate", LocalDate.now());
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        
        // Add project details
        Map<String, Object> projectDetails = new HashMap<>();
        projectDetails.put("id", project.getId());
        projectDetails.put("name", project.getName());
        
        report.put("projectDetails", projectDetails);
        
        // Apply date filtering
        LocalDate effectiveStartDate = startDate != null ? startDate : project.getStartDate();
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        
        // Get meetings within date range
        List<Meeting> meetings = meetingRepository.findByProject(project)
                .stream()
                .filter(m -> !m.getDate().isBefore(effectiveStartDate) && !m.getDate().isAfter(effectiveEndDate))
                .collect(Collectors.toList());
        
        // Add meeting statistics
        Map<String, Object> meetingStats = new HashMap<>();
        meetingStats.put("totalMeetings", meetings.size());
        
        // Get all attendance records for these meetings
        List<Attendance> allAttendances = new ArrayList<>();
        for (Meeting meeting : meetings) {
            allAttendances.addAll(meeting.getAttendances());
        }
        
        // Calculate overall attendance rate
        long presentCount = allAttendances.stream().filter(Attendance::isPresent).count();
        double overallAttendanceRate = allAttendances.isEmpty() ? 0.0 :
                100.0 * presentCount / allAttendances.size();
        
        meetingStats.put("totalAttendanceRecords", allAttendances.size());
        meetingStats.put("presentCount", presentCount);
        meetingStats.put("overallAttendanceRate", overallAttendanceRate);
        
        report.put("meetingStatistics", meetingStats);
        
        // Generate detailed meeting data
        List<Map<String, Object>> meetingDetails = new ArrayList<>();
        
        for (Meeting meeting : meetings) {
            Map<String, Object> meetingDetail = new HashMap<>();
            meetingDetail.put("id", meeting.getId());
            meetingDetail.put("date", meeting.getDate());
            meetingDetail.put("startTime", meeting.getStartTime());
            meetingDetail.put("endTime", meeting.getEndTime());
            
            List<Attendance> attendances = meeting.getAttendances();
            long meetingPresentCount = attendances.stream().filter(Attendance::isPresent).count();
            double attendanceRate = attendances.isEmpty() ? 0.0 :
                    100.0 * meetingPresentCount / attendances.size();
            
            meetingDetail.put("totalInvited", attendances.size());
            meetingDetail.put("presentCount", meetingPresentCount);
            meetingDetail.put("attendanceRate", attendanceRate);
            
            meetingDetails.add(meetingDetail);
        }
        
        // Sort meetings by date
        meetingDetails.sort(Comparator.comparing(m -> ((LocalDate) m.get("date"))));
        
        report.put("meetings", meetingDetails);
        
        // Generate individual member attendance data
        List<Map<String, Object>> memberAttendance = new ArrayList<>();
        
        // Get all team members through attendance records
        Set<TeamMember> teamMembers = new HashSet<>();
        for (Attendance attendance : allAttendances) {
            teamMembers.add(attendance.getMember());
        }
        
        for (TeamMember member : teamMembers) {
            Map<String, Object> memberDetail = new HashMap<>();
            memberDetail.put("id", member.getId());
            memberDetail.put("name", member.getFullName());
            
            // Count meetings attended
            int meetingsInvited = 0;
            int meetingsAttended = 0;
            
            for (Meeting meeting : meetings) {
                for (Attendance attendance : meeting.getAttendances()) {
                    if (attendance.getMember().equals(member)) {
                        meetingsInvited++;
                        if (attendance.isPresent()) {
                            meetingsAttended++;
                        }
                        break;
                    }
                }
            }
            
            memberDetail.put("meetingsInvited", meetingsInvited);
            memberDetail.put("meetingsAttended", meetingsAttended);
            
            double attendanceRate = meetingsInvited > 0 ?
                    100.0 * meetingsAttended / meetingsInvited : 0.0;
            memberDetail.put("attendanceRate", attendanceRate);
            
            memberAttendance.add(memberDetail);
        }
        
        // Sort by attendance rate (descending)
        memberAttendance.sort(Comparator.comparing(m -> ((Double) ((Map<String, Object>) m).get("attendanceRate"))).reversed());
        
        report.put("memberAttendance", memberAttendance);
        
        // Generate attendance trends over time
        Map<LocalDate, Double> attendanceTrends = new TreeMap<>(); // TreeMap for date ordering
        
        for (Meeting meeting : meetings) {
            List<Attendance> attendances = meeting.getAttendances();
            double rate = attendances.isEmpty() ? 0.0 :
                    100.0 * attendances.stream().filter(Attendance::isPresent).count() / attendances.size();
            
            attendanceTrends.put(meeting.getDate(), rate);
        }
        
        report.put("attendanceTrends", attendanceTrends);
        
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
        if (!projectOpt.isPresent()) {
            LOGGER.warning("Project not found with ID: " + projectId);
            return report;
        }
        
        Project project = projectOpt.get();
        
        // Add report metadata
        report.put("reportType", "Subsystem Progress");
        report.put("generatedDate", LocalDate.now());
        
        // Add project details
        Map<String, Object> projectDetails = new HashMap<>();
        projectDetails.put("id", project.getId());
        projectDetails.put("name", project.getName());
        
        report.put("projectDetails", projectDetails);
        
        // Get subsystem performance metrics
        Map<String, Object> subsystemMetrics = metricsService.calculateSubsystemPerformanceMetrics(projectId);
        
        // Get subsystems involved in the project
        List<Task> tasks = taskRepository.findByProject(project);
        Set<Subsystem> subsystems = new HashSet<>();
        for (Task task : tasks) {
            subsystems.add(task.getSubsystem());
        }
        
        // Add subsystem statistics
        Map<String, Object> subsystemStats = new HashMap<>();
        subsystemStats.put("totalSubsystems", subsystems.size());
        
        // Count subsystems by status
        Map<Subsystem.Status, Long> statusCounts = subsystems.stream()
                .collect(Collectors.groupingBy(Subsystem::getStatus, Collectors.counting()));
        subsystemStats.put("statusCounts", statusCounts);
        
        // Calculate completed subsystems percentage
        long completedSubsystems = statusCounts.getOrDefault(Subsystem.Status.COMPLETED, 0L);
        double completionPercentage = subsystems.isEmpty() ? 0.0 :
                100.0 * completedSubsystems / subsystems.size();
        subsystemStats.put("completionPercentage", completionPercentage);
        
        report.put("subsystemStatistics", subsystemStats);
        
        // Generate detailed subsystem data
        List<Map<String, Object>> subsystemDetails = new ArrayList<>();
        
        for (Subsystem subsystem : subsystems) {
            Map<String, Object> subsystemDetail = new HashMap<>();
            subsystemDetail.put("id", subsystem.getId());
            subsystemDetail.put("name", subsystem.getName());
            subsystemDetail.put("description", subsystem.getDescription());
            subsystemDetail.put("status", subsystem.getStatus().getDisplayName());
            
            // Get the responsible subteam if exists
            if (subsystem.getResponsibleSubteam() != null) {
                Map<String, Object> subteamInfo = new HashMap<>();
                subteamInfo.put("id", subsystem.getResponsibleSubteam().getId());
                subteamInfo.put("name", subsystem.getResponsibleSubteam().getName());
                subsystemDetail.put("responsibleSubteam", subteamInfo);
            }
            
            // Get tasks for this subsystem
            List<Task> subsystemTasks = tasks.stream()
                    .filter(t -> t.getSubsystem().equals(subsystem))
                    .collect(Collectors.toList());
            
            // Calculate task statistics
            int totalTasks = subsystemTasks.size();
            long completedTasks = subsystemTasks.stream().filter(Task::isCompleted).count();
            long inProgressTasks = subsystemTasks.stream()
                    .filter(t -> !t.isCompleted() && t.getProgress() > 0).count();
            long notStartedTasks = totalTasks - completedTasks - inProgressTasks;
            
            Map<String, Object> taskStats = new HashMap<>();
            taskStats.put("totalTasks", totalTasks);
            taskStats.put("completedTasks", completedTasks);
            taskStats.put("inProgressTasks", inProgressTasks);
            taskStats.put("notStartedTasks", notStartedTasks);
            
            // Calculate completion percentage
            double taskCompletionPercentage = subsystemTasks.isEmpty() ? 0.0 :
                    subsystemTasks.stream().mapToInt(Task::getProgress).sum() / (double) subsystemTasks.size();
            taskStats.put("completionPercentage", taskCompletionPercentage);
            
            subsystemDetail.put("taskStatistics", taskStats);
            
            subsystemDetails.add(subsystemDetail);
        }
        
        // Sort by completion percentage (descending)
        subsystemDetails.sort(Comparator.comparing(s -> 
            ((Double) ((Map<String, Object>) ((Map<String, Object>) s).get("taskStatistics")).get("completionPercentage"))).reversed());
        
        report.put("subsystems", subsystemDetails);
        
        // Generate visualization data
        Map<String, Double> subsystemCompletion = new HashMap<>();
        
        for (Map<String, Object> subsystemDetail : subsystemDetails) {
            String name = (String) subsystemDetail.get("name");
            double completion = (Double) ((Map<String, Object>) subsystemDetail.get("taskStatistics")).get("completionPercentage");
            subsystemCompletion.put(name, completion);
        }
        
        report.put("subsystemCompletion", subsystemCompletion);
        
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
        if (!memberOpt.isPresent()) {
            LOGGER.warning("Team member not found with ID: " + teamMemberId);
            return report;
        }
        
        TeamMember member = memberOpt.get();
        
        // Add report metadata
        report.put("reportType", "Team Member Performance");
        report.put("generatedDate", LocalDate.now());
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        
        // Add member details
        Map<String, Object> memberDetails = new HashMap<>();
        memberDetails.put("id", member.getId());
        memberDetails.put("name", member.getFullName());
        memberDetails.put("username", member.getUsername());
        memberDetails.put("email", member.getEmail());
        memberDetails.put("isLeader", member.isLeader());
        
        // Add subteam if exists
        if (member.getSubteam() != null) {
            Map<String, Object> subteamInfo = new HashMap<>();
            subteamInfo.put("id", member.getSubteam().getId());
            subteamInfo.put("name", member.getSubteam().getName());
            memberDetails.put("subteam", subteamInfo);
        }
        
        report.put("memberDetails", memberDetails);
        
        // Get individual performance metrics
        Map<String, Object> performanceMetrics = metricsService.calculateIndividualPerformanceMetrics(
                teamMemberId, startDate, endDate);
        report.put("performanceMetrics", performanceMetrics);
        
        // Extract assigned tasks information
        List<Task> assignedTasks = new ArrayList<>(member.getAssignedTasks());
        
        // Generate detailed task data
        List<Map<String, Object>> taskDetails = new ArrayList<>();
        
        for (Task task : assignedTasks) {
            Map<String, Object> taskDetail = new HashMap<>();
            taskDetail.put("id", task.getId());
            taskDetail.put("title", task.getTitle());
            taskDetail.put("description", task.getDescription());
            taskDetail.put("priority", task.getPriority().getDisplayName());
            taskDetail.put("startDate", task.getStartDate());
            taskDetail.put("endDate", task.getEndDate());
            taskDetail.put("progress", task.getProgress());
            taskDetail.put("isCompleted", task.isCompleted());
            
            // Add subsystem info
            Map<String, Object> subsystemInfo = new HashMap<>();
            subsystemInfo.put("id", task.getSubsystem().getId());
            subsystemInfo.put("name", task.getSubsystem().getName());
            taskDetail.put("subsystem", subsystemInfo);
            
            // Add project info
            Map<String, Object> projectInfo = new HashMap<>();
            projectInfo.put("id", task.getProject().getId());
            projectInfo.put("name", task.getProject().getName());
            taskDetail.put("project", projectInfo);
            
            taskDetails.add(taskDetail);
        }
        
        // Sort by completion status and then by end date
        taskDetails.sort(Comparator
                .comparing((Map<String, Object> t) -> (Boolean) t.get("isCompleted"))
                .thenComparing(t -> ((LocalDate) t.get("endDate")), Comparator.nullsLast(Comparator.naturalOrder())));
        
        report.put("assignedTasks", taskDetails);
        
        // Get attendance data
        List<Attendance> attendances = attendanceRepository.findByMember(member);
        
        // Apply date filtering
        LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now().minusMonths(3);
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        
        attendances = attendances.stream()
                .filter(a -> {
                    LocalDate meetingDate = a.getMeeting().getDate();
                    return !meetingDate.isBefore(effectiveStartDate) && !meetingDate.isAfter(effectiveEndDate);
                })
                .collect(Collectors.toList());
        
        // Generate attendance statistics
        Map<String, Object> attendanceStats = new HashMap<>();
        attendanceStats.put("totalMeetings", attendances.size());
        
        long attendedMeetings = attendances.stream().filter(Attendance::isPresent).count();
        attendanceStats.put("attendedMeetings", attendedMeetings);
        
        double attendanceRate = attendances.isEmpty() ? 0.0 :
                100.0 * attendedMeetings / attendances.size();
        attendanceStats.put("attendanceRate", attendanceRate);
        
        report.put("attendanceStatistics", attendanceStats);
        
        // Generate detailed attendance data
        List<Map<String, Object>> attendanceDetails = new ArrayList<>();
        
        for (Attendance attendance : attendances) {
            Meeting meeting = attendance.getMeeting();
            
            Map<String, Object> attendanceDetail = new HashMap<>();
            attendanceDetail.put("meetingId", meeting.getId());
            attendanceDetail.put("date", meeting.getDate());
            attendanceDetail.put("startTime", meeting.getStartTime());
            attendanceDetail.put("endTime", meeting.getEndTime());
            attendanceDetail.put("isPresent", attendance.isPresent());
            
            if (attendance.isPresent()) {
                attendanceDetail.put("arrivalTime", attendance.getArrivalTime());
                attendanceDetail.put("departureTime", attendance.getDepartureTime());
                attendanceDetail.put("durationMinutes", attendance.getDurationMinutes());
            }
            
            // Add project info
            Map<String, Object> projectInfo = new HashMap<>();
            projectInfo.put("id", meeting.getProject().getId());
            projectInfo.put("name", meeting.getProject().getName());
            attendanceDetail.put("project", projectInfo);
            
            attendanceDetails.add(attendanceDetail);
        }
        
        // Sort by date (descending)
        attendanceDetails.sort(Comparator.comparing(a -> ((LocalDate) ((Map<String, Object>) a).get("date"))).reversed());
        
        report.put("attendanceDetails", attendanceDetails);
        
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
        if (!projectOpt.isPresent()) {
            LOGGER.warning("Project not found with ID: " + projectId);
            return report;
        }
        
        Project project = projectOpt.get();
        
        // Add report metadata
        report.put("reportType", "Project Timeline");
        report.put("generatedDate", LocalDate.now());
        
        // Add project details
        Map<String, Object> projectDetails = new HashMap<>();
        projectDetails.put("id", project.getId());
        projectDetails.put("name", project.getName());
        projectDetails.put("startDate", project.getStartDate());
        projectDetails.put("goalEndDate", project.getGoalEndDate());
        projectDetails.put("hardDeadline", project.getHardDeadline());
        
        // Calculate date ranges
        LocalDate today = LocalDate.now();
        long totalDays = project.getStartDate().until(project.getGoalEndDate()).getDays();
        long elapsedDays = project.getStartDate().until(today).getDays();
        elapsedDays = Math.max(0, Math.min(totalDays, elapsedDays));
        
        projectDetails.put("totalDays", totalDays);
        projectDetails.put("elapsedDays", elapsedDays);
        projectDetails.put("progressPercentage", totalDays > 0 ? 100.0 * elapsedDays / totalDays : 0.0);
        
        report.put("projectDetails", projectDetails);
        
        // Get timeline deviation metrics
        Map<String, Object> timelineMetrics = metricsService.calculateTimelineDeviationMetrics(projectId);
        report.put("timelineMetrics", timelineMetrics);
        
        // Get tasks and milestones
        List<Task> tasks = taskRepository.findByProject(project);
        List<Milestone> milestones = milestoneRepository.findByProject(project);
        
        // Generate Gantt chart data
        Map<String, Object> ganttData = ganttDataService.formatTasksForGantt(projectId, null, null);
        report.put("ganttChartData", ganttData);
        
        // Generate timeline events
        List<Map<String, Object>> timelineEvents = new ArrayList<>();
        
        // Add project start and end
        Map<String, Object> startEvent = new HashMap<>();
        startEvent.put("date", project.getStartDate());
        startEvent.put("type", "Project Start");
        startEvent.put("description", "Project began");
        timelineEvents.add(startEvent);
        
        Map<String, Object> endEvent = new HashMap<>();
        endEvent.put("date", project.getGoalEndDate());
        endEvent.put("type", "Planned End");
        endEvent.put("description", "Planned project completion");
        timelineEvents.add(endEvent);
        
        Map<String, Object> hardDeadlineEvent = new HashMap<>();
        hardDeadlineEvent.put("date", project.getHardDeadline());
        hardDeadlineEvent.put("type", "Hard Deadline");
        hardDeadlineEvent.put("description", "Final project deadline");
        timelineEvents.add(hardDeadlineEvent);
        
        // Add milestones
        for (Milestone milestone : milestones) {
            Map<String, Object> milestoneEvent = new HashMap<>();
            milestoneEvent.put("date", milestone.getDate());
            milestoneEvent.put("type", "Milestone");
            milestoneEvent.put("description", milestone.getName());
            milestoneEvent.put("id", milestone.getId());
            milestoneEvent.put("isPassed", milestone.isPassed());
            timelineEvents.add(milestoneEvent);
        }
        
        // Sort events by date
        timelineEvents.sort(Comparator.comparing(e -> ((LocalDate) e.get("date"))));
        
        report.put("timelineEvents", timelineEvents);
        
        // Calculate critical path
        List<Long> criticalPath = ganttDataService.calculateCriticalPath(projectId);
        report.put("criticalPath", criticalPath);
        
        // Identify bottlenecks
        List<Long> bottlenecks = ganttDataService.identifyBottlenecks(projectId);
        report.put("bottlenecks", bottlenecks);
        
        return report;
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error generating project timeline report", e);
        return report;
    }
}

@Override
public byte[] exportReportToPdf(Map<String, Object> reportData, String reportType) {
    LOGGER.info("Exporting report to PDF. Type: " + reportType);
    
    try {
        // Placeholder implementation
        // In a real implementation, this would use a PDF generation library like iText or Apache PDFBox
        
        // Convert report data to a byte array representing a PDF
        String reportText = generateTextReport(reportData, reportType);
        
        // Here we're just returning the bytes of the text for placeholder purposes
        // A real implementation would create a properly formatted PDF
        return reportText.getBytes();
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error exporting report to PDF", e);
        return new byte[0];
    }
}

@Override
public String exportReportToCsv(Map<String, Object> reportData, String reportType) {
    LOGGER.info("Exporting report to CSV. Type: " + reportType);
    
    try {
        StringBuilder csv = new StringBuilder();
        
        // Create CSV based on report type
        switch (reportType) {
            case "Project Summary":
                csv.append(generateProjectSummaryCsv(reportData));
                break;
            case "Team Performance":
                csv.append(generateTeamPerformanceCsv(reportData));
                break;
            case "Milestone Status":
                csv.append(generateMilestoneStatusCsv(reportData));
                break;
            case "Attendance":
                csv.append(generateAttendanceCsv(reportData));
                break;
            case "Subsystem Progress":
                csv.append(generateSubsystemProgressCsv(reportData));
                break;
            case "Team Member Performance":
                csv.append(generateTeamMemberCsv(reportData));
                break;
            case "Project Timeline":
                csv.append(generateProjectTimelineCsv(reportData));
                break;
            default:
                csv.append("Unknown report type: ").append(reportType);
                break;
        }
        
        return csv.toString();
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error exporting report to CSV", e);
        return "Error generating CSV: " + e.getMessage();
    }
}

@Override
public Map<String, Object> generateCustomReport(Long projectId, List<String> metrics, LocalDate startDate, LocalDate endDate) {
    LOGGER.info("Generating custom report for project ID: " + projectId);
    
    Map<String, Object> report = new HashMap<>();
    
    try {
        // Get project
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (!projectOpt.isPresent()) {
            LOGGER.warning("Project not found with ID: " + projectId);
            return report;
        }
        
        Project project = projectOpt.get();
        
        // Add report metadata
        report.put("reportType", "Custom Report");
        report.put("generatedDate", LocalDate.now());
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("selectedMetrics", metrics);
        // Continuing src/main/java/org/frcpm/services/impl/ReportGenerationServiceImpl.java

            // Add project details
            Map<String, Object> projectDetails = new HashMap<>();
            projectDetails.put("id", project.getId());
            projectDetails.put("name", project.getName());
            projectDetails.put("startDate", project.getStartDate());
            projectDetails.put("goalEndDate", project.getGoalEndDate());
            
            report.put("projectDetails", projectDetails);
            
            // Add selected metrics based on the requested types
            for (String metricType : metrics) {
                switch (metricType) {
                    case "projectProgress":
                        Map<String, Object> progressMetrics = metricsService.calculateProjectProgressMetrics(projectId);
                        report.put("projectProgressMetrics", progressMetrics);
                        break;
                        
                    case "teamPerformance":
                        Map<String, Object> teamMetrics = metricsService.calculateTeamPerformanceMetrics(projectId, startDate, endDate);
                        report.put("teamPerformanceMetrics", teamMetrics);
                        break;
                        
                    case "taskCompletion":
                        Map<String, Object> taskMetrics = metricsService.calculateTaskCompletionMetrics(projectId);
                        report.put("taskCompletionMetrics", taskMetrics);
                        break;
                        
                    case "attendance":
                        Map<String, Object> attendanceMetrics = metricsService.calculateAttendanceMetrics(projectId, startDate, endDate);
                        report.put("attendanceMetrics", attendanceMetrics);
                        break;
                        
                    case "timelineDeviation":
                        Map<String, Object> timelineMetrics = metricsService.calculateTimelineDeviationMetrics(projectId);
                        report.put("timelineDeviationMetrics", timelineMetrics);
                        break;
                        
                    case "subsystemPerformance":
                        Map<String, Object> subsystemMetrics = metricsService.calculateSubsystemPerformanceMetrics(projectId);
                        report.put("subsystemPerformanceMetrics", subsystemMetrics);
                        break;
                        
                    case "projectHealth":
                        Map<String, Object> healthDashboard = metricsService.generateProjectHealthDashboard(projectId);
                        report.put("projectHealthMetrics", healthDashboard);
                        break;
                        
                    default:
                        LOGGER.warning("Unknown metric type: " + metricType);
                        break;
                }
            }
            
            // Add custom summary
            report.put("summary", generateCustomReportSummary(report));
            
            return report;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating custom report", e);
            return report;
        }
    }
    
    // Helper methods for CSV generation
    
    /**
     * Generates a text-based representation of a report.
     * 
     * @param reportData the report data
     * @param reportType the type of report
     * @return a text representation of the report
     */
    private String generateTextReport(Map<String, Object> reportData, String reportType) {
        StringBuilder text = new StringBuilder();
        
        // Add report header
        text.append("REPORT: ").append(reportType).append("\n");
        text.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ISO_DATE)).append("\n\n");
        
        // Add project details
        Map<String, Object> projectDetails = (Map<String, Object>) reportData.get("projectDetails");
        if (projectDetails != null) {
            text.append("PROJECT: ").append(projectDetails.get("name")).append("\n");
            if (projectDetails.get("startDate") != null) {
                text.append("Start Date: ").append(projectDetails.get("startDate")).append("\n");
            }
            if (projectDetails.get("goalEndDate") != null) {
                text.append("End Date: ").append(projectDetails.get("goalEndDate")).append("\n");
            }
            text.append("\n");
        }
        
        // Add report-specific sections
        switch (reportType) {
            case "Project Summary":
                appendProjectSummaryText(text, reportData);
                break;
            case "Team Performance":
                appendTeamPerformanceText(text, reportData);
                break;
            case "Milestone Status":
                appendMilestoneStatusText(text, reportData);
                break;
            case "Attendance":
                appendAttendanceText(text, reportData);
                break;
            case "Subsystem Progress":
                appendSubsystemProgressText(text, reportData);
                break;
            case "Team Member Performance":
                appendTeamMemberText(text, reportData);
                break;
            case "Project Timeline":
                appendProjectTimelineText(text, reportData);
                break;
            default:
                text.append("Unknown report type: ").append(reportType);
                break;
        }
        
        return text.toString();
    }
    
    /**
     * Generates a Project Summary CSV.
     * 
     * @param reportData the report data
     * @return a CSV string
     */
    private String generateProjectSummaryCsv(Map<String, Object> reportData) {
        StringBuilder csv = new StringBuilder();
        
        // Project details
        csv.append("Project Name,Start Date,Goal End Date,Hard Deadline\n");
        Map<String, Object> projectDetails = (Map<String, Object>) reportData.get("projectDetails");
        csv.append(projectDetails.get("name")).append(",");
        csv.append(projectDetails.get("startDate")).append(",");
        csv.append(projectDetails.get("goalEndDate")).append(",");
        csv.append(projectDetails.get("hardDeadline")).append("\n\n");
        
        // Task statistics
        csv.append("Task Statistics\n");
        csv.append("Total Tasks,Completed Tasks,In Progress Tasks,Not Started Tasks,Completion Percentage\n");
        Map<String, Object> taskStats = (Map<String, Object>) reportData.get("taskStatistics");
        csv.append(taskStats.get("totalTasks")).append(",");
        csv.append(taskStats.get("completedTasks")).append(",");
        csv.append(taskStats.get("inProgressTasks")).append(",");
        csv.append(taskStats.get("notStartedTasks")).append(",");
        csv.append(taskStats.get("completionPercentage")).append("%\n\n");
        
        // Milestone statistics
        csv.append("Milestone Statistics\n");
        csv.append("Total Milestones,Passed Milestones\n");
        Map<String, Object> milestoneStats = (Map<String, Object>) reportData.get("milestoneStatistics");
        csv.append(milestoneStats.get("totalMilestones")).append(",");
        csv.append(milestoneStats.get("passedMilestones")).append("\n\n");
        
        // Upcoming milestones
        csv.append("Upcoming Milestones\n");
        csv.append("Name,Date,Days Until\n");
        List<Map<String, Object>> upcomingMilestones = (List<Map<String, Object>>) milestoneStats.get("upcomingMilestones");
        for (Map<String, Object> milestone : upcomingMilestones) {
            csv.append(milestone.get("name")).append(",");
            csv.append(milestone.get("date")).append(",");
            csv.append(milestone.get("daysUntil")).append("\n");
        }
        csv.append("\n");
        
        return csv.toString();
    }
    
    /**
     * Generates a Team Performance CSV.
     * 
     * @param reportData the report data
     * @return a CSV string
     */
    private String generateTeamPerformanceCsv(Map<String, Object> reportData) {
        StringBuilder csv = new StringBuilder();
        
        // Team Metrics
        csv.append("Team Metrics\n");
        csv.append("Total Team Members,Average Attendance Percentage,Average Tasks Per Member\n");
        Map<String, Object> teamMetrics = (Map<String, Object>) reportData.get("teamMetrics");
        csv.append(teamMetrics.get("totalTeamMembers")).append(",");
        csv.append(teamMetrics.get("averageAttendancePercentage")).append("%,");
        csv.append(teamMetrics.get("averageTasksPerMember")).append("\n\n");
        
        // Tasks per Member
        csv.append("Tasks Per Member\n");
        csv.append("Member ID,Tasks Count,Completed Tasks,Completion Rate\n");
        Map<Long, Long> tasksPerMember = (Map<Long, Long>) teamMetrics.get("tasksPerMember");
        Map<Long, Long> completedTasksPerMember = (Map<Long, Long>) teamMetrics.get("completedTasksPerMember");
        Map<Long, Double> completionRatePerMember = (Map<Long, Double>) teamMetrics.get("completionRatePerMember");
        
        for (Map.Entry<Long, Long> entry : tasksPerMember.entrySet()) {
            Long memberId = entry.getKey();
            Long taskCount = entry.getValue();
            Long completedCount = completedTasksPerMember.getOrDefault(memberId, 0L);
            Double completionRate = completionRatePerMember.getOrDefault(memberId, 0.0);
            
            csv.append(memberId).append(",");
            csv.append(taskCount).append(",");
            csv.append(completedCount).append(",");
            csv.append(completionRate).append("%\n");
        }
        csv.append("\n");
        
        return csv.toString();
    }
    
    /**
     * Generates a Milestone Status CSV.
     * 
     * @param reportData the report data
     * @return a CSV string
     */
    private String generateMilestoneStatusCsv(Map<String, Object> reportData) {
        StringBuilder csv = new StringBuilder();
        
        // Milestone statistics
        csv.append("Milestone Statistics\n");
        csv.append("Total Milestones,Passed Milestones,Upcoming Milestones,Milestone Completion Percentage\n");
        Map<String, Object> milestoneStats = (Map<String, Object>) reportData.get("milestoneStatistics");
        csv.append(milestoneStats.get("totalMilestones")).append(",");
        csv.append(milestoneStats.get("passedMilestones")).append(",");
        csv.append(milestoneStats.get("upcomingMilestones")).append(",");
        csv.append(milestoneStats.get("milestoneCompletionPercentage")).append("%\n\n");
        
        // Detailed milestones
        csv.append("Milestone Details\n");
        csv.append("ID,Name,Date,Status,Days Until\n");
        List<Map<String, Object>> milestones = (List<Map<String, Object>>) reportData.get("milestones");
        for (Map<String, Object> milestone : milestones) {
            csv.append(milestone.get("id")).append(",");
            
            // Escape commas in name
            String name = (String) milestone.get("name");
            if (name.contains(",")) {
                name = "\"" + name + "\"";
            }
            csv.append(name).append(",");
            
            csv.append(milestone.get("date")).append(",");
            csv.append(milestone.get("status")).append(",");
            csv.append(milestone.get("daysUntil")).append("\n");
        }
        csv.append("\n");
        
        return csv.toString();
    }
    
    /**
     * Generates an Attendance CSV.
     * 
     * @param reportData the report data
     * @return a CSV string
     */
    private String generateAttendanceCsv(Map<String, Object> reportData) {
        StringBuilder csv = new StringBuilder();
        
        // Meeting statistics
        csv.append("Attendance Statistics\n");
        csv.append("Total Meetings,Total Attendance Records,Present Count,Overall Attendance Rate\n");
        Map<String, Object> meetingStats = (Map<String, Object>) reportData.get("meetingStatistics");
        csv.append(meetingStats.get("totalMeetings")).append(",");
        csv.append(meetingStats.get("totalAttendanceRecords")).append(",");
        csv.append(meetingStats.get("presentCount")).append(",");
        csv.append(meetingStats.get("overallAttendanceRate")).append("%\n\n");
        
        // Meeting details
        csv.append("Meeting Details\n");
        csv.append("ID,Date,Start Time,End Time,Total Invited,Present Count,Attendance Rate\n");
        List<Map<String, Object>> meetings = (List<Map<String, Object>>) reportData.get("meetings");
        for (Map<String, Object> meeting : meetings) {
            csv.append(meeting.get("id")).append(",");
            csv.append(meeting.get("date")).append(",");
            csv.append(meeting.get("startTime")).append(",");
            csv.append(meeting.get("endTime")).append(",");
            csv.append(meeting.get("totalInvited")).append(",");
            csv.append(meeting.get("presentCount")).append(",");
            csv.append(meeting.get("attendanceRate")).append("%\n");
        }
        csv.append("\n");
        
        // Member attendance
        csv.append("Member Attendance\n");
        csv.append("ID,Name,Meetings Invited,Meetings Attended,Attendance Rate\n");
        List<Map<String, Object>> memberAttendance = (List<Map<String, Object>>) reportData.get("memberAttendance");
        for (Map<String, Object> member : memberAttendance) {
            csv.append(member.get("id")).append(",");
            
            // Escape commas in name
            String name = (String) member.get("name");
            if (name.contains(",")) {
                name = "\"" + name + "\"";
            }
            csv.append(name).append(",");
            
            csv.append(member.get("meetingsInvited")).append(",");
            csv.append(member.get("meetingsAttended")).append(",");
            csv.append(member.get("attendanceRate")).append("%\n");
        }
        csv.append("\n");
        
        return csv.toString();
    }
    
    /**
     * Generates a Subsystem Progress CSV.
     * 
     * @param reportData the report data
     * @return a CSV string
     */
    private String generateSubsystemProgressCsv(Map<String, Object> reportData) {
        StringBuilder csv = new StringBuilder();
        
        // Subsystem statistics
        csv.append("Subsystem Statistics\n");
        csv.append("Total Subsystems,Completed Subsystems,Subsystem Completion Rate\n");
        Map<String, Object> subsystemStats = (Map<String, Object>) reportData.get("subsystemStatistics");
        csv.append(subsystemStats.get("totalSubsystems")).append(",");
        csv.append(subsystemStats.get("completedSubsystems")).append(",");
        csv.append(subsystemStats.get("subsystemCompletionRate")).append("%\n\n");
        
        // Status counts
        csv.append("Status Counts\n");
        csv.append("Status,Count\n");
        Map<String, Long> statusCounts = (Map<String, Long>) subsystemStats.get("statusCounts");
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            csv.append(entry.getKey()).append(",");
            csv.append(entry.getValue()).append("\n");
        }
        csv.append("\n");
        
        // Subsystem details
        csv.append("Subsystem Details\n");
        csv.append("ID,Name,Status,Total Tasks,Completed Tasks,Completion Percentage\n");
        List<Map<String, Object>> subsystems = (List<Map<String, Object>>) reportData.get("subsystems");
        for (Map<String, Object> subsystem : subsystems) {
            csv.append(subsystem.get("id")).append(",");
            
            // Escape commas in name
            String name = (String) subsystem.get("name");
            if (name.contains(",")) {
                name = "\"" + name + "\"";
            }
            csv.append(name).append(",");
            
            csv.append(subsystem.get("status")).append(",");
            
            Map<String, Object> taskStats = (Map<String, Object>) subsystem.get("taskStatistics");
            csv.append(taskStats.get("totalTasks")).append(",");
            csv.append(taskStats.get("completedTasks")).append(",");
            csv.append(taskStats.get("completionPercentage")).append("%\n");
        }
        csv.append("\n");
        
        return csv.toString();
    }
    
    /**
     * Generates a Team Member CSV.
     * 
     * @param reportData the report data
     * @return a CSV string
     */
    private String generateTeamMemberCsv(Map<String, Object> reportData) {
        StringBuilder csv = new StringBuilder();
        
        // Member details
        csv.append("Member Details\n");
        csv.append("ID,Name,Username,Email,Is Leader\n");
        Map<String, Object> memberDetails = (Map<String, Object>) reportData.get("memberDetails");
        csv.append(memberDetails.get("id")).append(",");
        
        // Escape commas in name
        String name = (String) memberDetails.get("name");
        if (name.contains(",")) {
            name = "\"" + name + "\"";
        }
        csv.append(name).append(",");
        
        csv.append(memberDetails.get("username")).append(",");
        csv.append(memberDetails.get("email")).append(",");
        csv.append(memberDetails.get("isLeader")).append("\n\n");
        
        // Performance metrics
        csv.append("Performance Metrics\n");
        csv.append("Total Assigned Tasks,Completed Tasks,Task Completion Rate,Average Task Progress\n");
        Map<String, Object> metrics = (Map<String, Object>) reportData.get("performanceMetrics");
        csv.append(metrics.get("totalAssignedTasks")).append(",");
        csv.append(metrics.get("completedTasks")).append(",");
        csv.append(metrics.get("taskCompletionRate")).append("%,");
        csv.append(metrics.get("averageTaskProgress")).append("%\n\n");
        
        // Attendance statistics
        csv.append("Attendance Statistics\n");
        csv.append("Total Meetings,Attended Meetings,Attendance Rate\n");
        csv.append(metrics.get("totalMeetings")).append(",");
        csv.append(metrics.get("attendedMeetings")).append(",");
        csv.append(metrics.get("attendanceRate")).append("%\n\n");
        
        // Task details
        csv.append("Assigned Tasks\n");
        csv.append("ID,Title,Progress,Priority,Start Date,End Date,Completed\n");
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) reportData.get("assignedTasks");
        for (Map<String, Object> task : tasks) {
            csv.append(task.get("id")).append(",");
            
            // Escape commas in title
            String title = (String) task.get("title");
            if (title.contains(",")) {
                title = "\"" + title + "\"";
            }
            csv.append(title).append(",");
            
            csv.append(task.get("progress")).append("%,");
            csv.append(task.get("priority")).append(",");
            csv.append(task.get("startDate")).append(",");
            csv.append(task.get("endDate")).append(",");
            csv.append(task.get("isCompleted")).append("\n");
        }
        csv.append("\n");
        
        return csv.toString();
    }
    
    /**
     * Generates a Project Timeline CSV.
     * 
     * @param reportData the report data
     * @return a CSV string
     */
    private String generateProjectTimelineCsv(Map<String, Object> reportData) {
        StringBuilder csv = new StringBuilder();
        
        // Timeline statistics
        csv.append("Timeline Statistics\n");
        csv.append("Projected Delay,Is On Schedule,Timeline Deviation Percentage\n");
        Map<String, Object> timelineMetrics = (Map<String, Object>) reportData.get("timelineMetrics");
        csv.append(timelineMetrics.get("projectedDelay")).append(" days,");
        csv.append(timelineMetrics.get("isOnSchedule")).append(",");
        csv.append(timelineMetrics.get("timelineDeviationPercentage")).append("%\n\n");
        
        // Timeline events
        csv.append("Timeline Events\n");
        csv.append("Date,Type,Description\n");
        List<Map<String, Object>> events = (List<Map<String, Object>>) reportData.get("timelineEvents");
        for (Map<String, Object> event : events) {
            csv.append(event.get("date")).append(",");
            csv.append(event.get("type")).append(",");
            
            // Escape commas in description
            String description = (String) event.get("description");
            if (description.contains(",")) {
                description = "\"" + description + "\"";
            }
            csv.append(description).append("\n");
        }
        csv.append("\n");
        
        return csv.toString();
    }
    
    /**
     * Generates a summary for the custom report.
     * 
     * @param reportData the report data
     * @return a summary map
     */
    private Map<String, Object> generateCustomReportSummary(Map<String, Object> reportData) {
        Map<String, Object> summary = new HashMap<>();
        
        // Project details
        Map<String, Object> projectDetails = (Map<String, Object>) reportData.get("projectDetails");
        summary.put("projectName", projectDetails.get("name"));
        
        // Project progress metrics
        if (reportData.containsKey("projectProgressMetrics")) {
            Map<String, Object> progressMetrics = (Map<String, Object>) reportData.get("projectProgressMetrics");
            summary.put("completionPercentage", progressMetrics.get("completionPercentage"));
            summary.put("scheduleVariance", progressMetrics.get("scheduleVariance"));
        }
        
        // Timeline metrics
        if (reportData.containsKey("timelineDeviationMetrics")) {
            Map<String, Object> timelineMetrics = (Map<String, Object>) reportData.get("timelineDeviationMetrics");
            summary.put("projectedDelay", timelineMetrics.get("projectedDelay"));
            summary.put("isOnSchedule", timelineMetrics.get("isOnSchedule"));
        }
        
        // Task metrics
        if (reportData.containsKey("taskCompletionMetrics")) {
            Map<String, Object> taskMetrics = (Map<String, Object>) reportData.get("taskCompletionMetrics");
            if (taskMetrics.containsKey("taskCountsByStatus")) {
                Map<String, Long> taskCountsByStatus = (Map<String, Long>) taskMetrics.get("taskCountsByStatus");
                summary.put("completedTasks", taskCountsByStatus.getOrDefault("COMPLETED", 0L));
                summary.put("inProgressTasks", taskCountsByStatus.getOrDefault("IN_PROGRESS", 0L));
                summary.put("notStartedTasks", taskCountsByStatus.getOrDefault("NOT_STARTED", 0L));
                
                long totalTasks = taskCountsByStatus.values().stream().mapToLong(Long::longValue).sum();
                summary.put("totalTasks", totalTasks);
            }
        }
        
        // Team metrics
        if (reportData.containsKey("teamPerformanceMetrics")) {
            Map<String, Object> teamMetrics = (Map<String, Object>) reportData.get("teamPerformanceMetrics");
            summary.put("totalTeamMembers", teamMetrics.get("totalTeamMembers"));
            summary.put("averageAttendancePercentage", teamMetrics.get("averageAttendancePercentage"));
        }
        
        // Attendance metrics
        if (reportData.containsKey("attendanceMetrics")) {
            Map<String, Object> attendanceMetrics = (Map<String, Object>) reportData.get("attendanceMetrics");
            summary.put("overallAttendanceRate", attendanceMetrics.get("overallAttendanceRate"));
        }
        
        // Project health
        if (reportData.containsKey("projectHealthMetrics")) {
            Map<String, Object> healthMetrics = (Map<String, Object>) reportData.get("projectHealthMetrics");
            summary.put("healthScore", healthMetrics.get("healthScore"));
            summary.put("healthStatus", healthMetrics.get("healthStatus"));
        }
        
        return summary;
    }
    
    /**
     * Appends project summary text to a StringBuilder.
     * 
     * @param text the StringBuilder to append to
     * @param reportData the report data
     */
    private void appendProjectSummaryText(StringBuilder text, Map<String, Object> reportData) {
        // Task statistics
        text.append("TASK STATISTICS:\n");
        Map<String, Object> taskStats = (Map<String, Object>) reportData.get("taskStatistics");
        text.append("  Total Tasks: ").append(taskStats.get("totalTasks")).append("\n");
        text.append("  Completed Tasks: ").append(taskStats.get("completedTasks")).append("\n");
        text.append("  In Progress Tasks: ").append(taskStats.get("inProgressTasks")).append("\n");
        text.append("  Not Started Tasks: ").append(taskStats.get("notStartedTasks")).append("\n");
        text.append("  Completion Percentage: ").append(taskStats.get("completionPercentage")).append("%\n\n");
        
        // Milestone statistics
        text.append("MILESTONE STATISTICS:\n");
        Map<String, Object> milestoneStats = (Map<String, Object>) reportData.get("milestoneStatistics");
        text.append("  Total Milestones: ").append(milestoneStats.get("totalMilestones")).append("\n");
        text.append("  Passed Milestones: ").append(milestoneStats.get("passedMilestones")).append("\n\n");
        
        // Upcoming milestones
        text.append("UPCOMING MILESTONES:\n");
        List<Map<String, Object>> upcomingMilestones = (List<Map<String, Object>>) milestoneStats.get("upcomingMilestones");
        for (Map<String, Object> milestone : upcomingMilestones) {
            text.append("  - ").append(milestone.get("name")).append(" (");
            text.append(milestone.get("date")).append(", ");
            text.append(milestone.get("daysUntil")).append(" days away)\n");
        }
        text.append("\n");
        
        // Timeline statistics
        text.append("TIMELINE STATISTICS:\n");
        Map<String, Object> timelineStats = (Map<String, Object>) reportData.get("timelineStatistics");
        text.append("  Total Days: ").append(timelineStats.get("totalDays")).append("\n");
        text.append("  Elapsed Days: ").append(timelineStats.get("elapsedDays")).append("\n");
        text.append("  Remaining Days: ").append(timelineStats.get("remainingDays")).append("\n");
        text.append("  Time Progress: ").append(timelineStats.get("timeProgressPercentage")).append("%\n");
        text.append("  Schedule Variance: ").append(timelineStats.get("scheduleVariance")).append("%\n");
        text.append("  Schedule Status: ").append(timelineStats.get("scheduleStatus")).append("\n");
    }
    
    // Add similar methods for the other report types
    private void appendTeamPerformanceText(StringBuilder text, Map<String, Object> reportData) {
        // Method implementation would be similar to the above
    }
    
    private void appendMilestoneStatusText(StringBuilder text, Map<String, Object> reportData) {
        // Method implementation would be similar to the above
    }
    
    private void appendAttendanceText(StringBuilder text, Map<String, Object> reportData) {
        // Method implementation would be similar to the above
    }
    
    private void appendSubsystemProgressText(StringBuilder text, Map<String, Object> reportData) {
        // Method implementation would be similar to the above
    }
    
    private void appendTeamMemberText(StringBuilder text, Map<String, Object> reportData) {
        // Method implementation would be similar to the above
    }
    
    private void appendProjectTimelineText(StringBuilder text, Map<String, Object> reportData) {
        // Method implementation would be similar to the above
    }
}