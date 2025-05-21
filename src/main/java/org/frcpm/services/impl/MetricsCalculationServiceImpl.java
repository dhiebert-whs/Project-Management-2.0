// src/main/java/org/frcpm/services/impl/MetricsCalculationServiceImpl.java
package org.frcpm.services.impl;

import org.frcpm.models.*;
import org.frcpm.repositories.specific.*;
import org.frcpm.services.MetricsCalculationService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the MetricsCalculationService interface.
 * Provides methods to calculate performance metrics for
 * project management purposes.
 */
public class MetricsCalculationServiceImpl implements MetricsCalculationService {
    
    private static final Logger LOGGER = Logger.getLogger(MetricsCalculationServiceImpl.class.getName());
    
    private ProjectRepository projectRepository;
    private TaskRepository taskRepository;
    private TeamMemberRepository teamMemberRepository;
    private MilestoneRepository milestoneRepository;
    private AttendanceRepository attendanceRepository;
    private MeetingRepository meetingRepository;
    private SubsystemRepository subsystemRepository;
    
    /**
     * Default constructor for MVVMFx dependency injection.
     */
    public MetricsCalculationServiceImpl() {
        // Default constructor for dependency injection
    }
    
    /**
     * Constructor with repository injection for testing.
     */
    public MetricsCalculationServiceImpl(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TeamMemberRepository teamMemberRepository,
            MilestoneRepository milestoneRepository,
            AttendanceRepository attendanceRepository,
            MeetingRepository meetingRepository,
            SubsystemRepository subsystemRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.milestoneRepository = milestoneRepository;
        this.attendanceRepository = attendanceRepository;
        this.meetingRepository = meetingRepository;
        this.subsystemRepository = subsystemRepository;
    }
    
    @Override
    public Map<String, Object> calculateProjectProgressMetrics(Long projectId) {
        LOGGER.info("Calculating project progress metrics for project ID: " + projectId);
        
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (!projectOpt.isPresent()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return metrics;
            }
            
            Project project = projectOpt.get();
            
            // Get all tasks for the project
            List<Task> tasks = taskRepository.findByProject(project);
            
            // Calculate overall completion percentage
            double completionPercentage = calculateTaskCompletionPercentage(tasks);
            metrics.put("completionPercentage", completionPercentage);
            
            // Calculate days remaining
            long daysTotal = ChronoUnit.DAYS.between(project.getStartDate(), project.getGoalEndDate());
            long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), project.getGoalEndDate());
            daysRemaining = Math.max(0, daysRemaining); // Ensure not negative
            
            metrics.put("daysTotal", daysTotal);
            metrics.put("daysRemaining", daysRemaining);
            metrics.put("daysUsed", daysTotal - daysRemaining);
            
            // Calculate time progress percentage
            double timeProgressPercentage = 100.0 * (daysTotal - daysRemaining) / daysTotal;
            metrics.put("timeProgressPercentage", timeProgressPercentage);
            
            // Calculate schedule variance
            double scheduleVariance = completionPercentage - timeProgressPercentage;
            metrics.put("scheduleVariance", scheduleVariance);
            
            // Calculate task status counts
            Map<TaskStatus, Long> taskStatusCounts = calculateTaskStatusCounts(tasks);
            metrics.put("taskStatusCounts", taskStatusCounts);
            
            // Calculate task status by priority
            Map<Task.Priority, Map<TaskStatus, Long>> taskStatusByPriority = calculateTaskStatusByPriority(tasks);
            metrics.put("taskStatusByPriority", taskStatusByPriority);
            
            // Get milestone status
            List<Milestone> milestones = milestoneRepository.findByProject(project);
            int totalMilestones = milestones.size();
            long passedMilestones = milestones.stream().filter(Milestone::isPassed).count();
            
            metrics.put("totalMilestones", totalMilestones);
            metrics.put("passedMilestones", passedMilestones);
            metrics.put("milestonesPercentage", totalMilestones > 0 ? 100.0 * passedMilestones / totalMilestones : 0);
            
            // Project health indicators
            boolean isBehindSchedule = scheduleVariance < -10; // More than 10% behind schedule
            boolean isOnTrack = Math.abs(scheduleVariance) <= 10; // Within 10% of schedule
            boolean isAheadOfSchedule = scheduleVariance > 10; // More than 10% ahead of schedule
            
            metrics.put("isBehindSchedule", isBehindSchedule);
            metrics.put("isOnTrack", isOnTrack);
            metrics.put("isAheadOfSchedule", isAheadOfSchedule);
            
            return metrics;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating project progress metrics", e);
            return metrics;
        }
    }
    
    @Override
    public Map<String, Object> calculateTeamPerformanceMetrics(Long projectId, LocalDate startDate, LocalDate endDate) {
        LOGGER.info("Calculating team performance metrics for project ID: " + projectId);
        
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (!projectOpt.isPresent()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return metrics;
            }
            
            Project project = projectOpt.get();
            
            // Apply date filtering
            LocalDate effectiveStartDate = startDate != null ? startDate : project.getStartDate();
            LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
            
            // Get meetings within date range
            List<Meeting> meetings = meetingRepository.findByProject(project)
                    .stream()
                    .filter(m -> !m.getDate().isBefore(effectiveStartDate) && !m.getDate().isAfter(effectiveEndDate))
                    .collect(Collectors.toList());
            
            // Get team members through attendance records
            Set<TeamMember> teamMembers = new HashSet<>();
            for (Meeting meeting : meetings) {
                for (Attendance attendance : meeting.getAttendances()) {
                    teamMembers.add(attendance.getMember());
                }
            }
            
            // Calculate team-wide metrics
            int totalTeamMembers = teamMembers.size();
            metrics.put("totalTeamMembers", totalTeamMembers);
            
            // Calculate average attendance percentage
            double avgAttendancePercentage = meetings.stream()
                    .mapToDouble(Meeting::getAttendancePercentage)
                    .average()
                    .orElse(0.0);
            metrics.put("averageAttendancePercentage", avgAttendancePercentage);
            
            // Calculate tasks per team member
            Map<TeamMember, Long> tasksPerMember = new HashMap<>();
            for (TeamMember member : teamMembers) {
                long taskCount = taskRepository.findByAssignedMember(member).size();
                tasksPerMember.put(member, taskCount);
            }
            
            // Convert to a format suitable for return (using member IDs as keys)
            Map<Long, Long> tasksPerMemberId = new HashMap<>();
            for (Map.Entry<TeamMember, Long> entry : tasksPerMember.entrySet()) {
                tasksPerMemberId.put(entry.getKey().getId(), entry.getValue());
            }
            metrics.put("tasksPerMember", tasksPerMemberId);
            
            // Calculate average tasks per member
            double avgTasksPerMember = tasksPerMember.values().stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            metrics.put("averageTasksPerMember", avgTasksPerMember);
            
            // Calculate completed tasks per member
            Map<TeamMember, Long> completedTasksPerMember = new HashMap<>();
            for (TeamMember member : teamMembers) {
                long completedCount = taskRepository.findByAssignedMember(member).stream()
                        .filter(Task::isCompleted)
                        .count();
                completedTasksPerMember.put(member, completedCount);
            }
            
            // Convert to a format suitable for return (using member IDs as keys)
            Map<Long, Long> completedTasksPerMemberId = new HashMap<>();
            for (Map.Entry<TeamMember, Long> entry : completedTasksPerMember.entrySet()) {
                completedTasksPerMemberId.put(entry.getKey().getId(), entry.getValue());
            }
            metrics.put("completedTasksPerMember", completedTasksPerMemberId);
            
            // Calculate completion rate per member
            Map<Long, Double> completionRatePerMemberId = new HashMap<>();
            for (TeamMember member : teamMembers) {
                if (tasksPerMember.getOrDefault(member, 0L) > 0) {
                    double completionRate = 100.0 * completedTasksPerMember.getOrDefault(member, 0L) / 
                                          tasksPerMember.getOrDefault(member, 1L);
                    completionRatePerMemberId.put(member.getId(), completionRate);
                }
            }
            metrics.put("completionRatePerMember", completionRatePerMemberId);
            
            return metrics;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating team performance metrics", e);
            return metrics;
        }
    }
    
    @Override
    public Map<String, Object> calculateTaskCompletionMetrics(Long projectId) {
        LOGGER.info("Calculating task completion metrics for project ID: " + projectId);
        
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (!projectOpt.isPresent()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return metrics;
            }
            
            Project project = projectOpt.get();
            
            // Get all tasks for the project
            List<Task> tasks = taskRepository.findByProject(project);
            
            // Calculate task counts by status
            Map<TaskStatus, Long> taskCountsByStatus = calculateTaskStatusCounts(tasks);
            metrics.put("taskCountsByStatus", taskCountsByStatus);
            
            // Calculate task counts by priority
            Map<Task.Priority, Long> taskCountsByPriority = tasks.stream()
                    .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
            metrics.put("taskCountsByPriority", taskCountsByPriority);
            
            // Calculate task completion by subsystem
            Map<Subsystem, Double> completionBySubsystem = new HashMap<>();
            Map<Subsystem, List<Task>> tasksBySubsystem = tasks.stream()
                    .collect(Collectors.groupingBy(Task::getSubsystem));
            
            for (Map.Entry<Subsystem, List<Task>> entry : tasksBySubsystem.entrySet()) {
                double completion = calculateTaskCompletionPercentage(entry.getValue());
                completionBySubsystem.put(entry.getKey(), completion);
            }
            
            // Convert to a format suitable for return (using subsystem IDs as keys)
            Map<Long, Double> completionBySubsystemId = new HashMap<>();
            for (Map.Entry<Subsystem, Double> entry : completionBySubsystem.entrySet()) {
                completionBySubsystemId.put(entry.getKey().getId(), entry.getValue());
            }
            metrics.put("completionBySubsystem", completionBySubsystemId);
            
            // Calculate average task duration
            OptionalDouble avgEstimatedDurationSeconds = tasks.stream()
                    .mapToLong(t -> t.getEstimatedDuration().getSeconds())
                    .average();
            
            OptionalDouble avgActualDurationSeconds = tasks.stream()
                    .filter(t -> t.getActualDuration() != null)
                    .mapToLong(t -> t.getActualDuration().getSeconds())
                    .average();
            
            metrics.put("averageEstimatedDurationHours", avgEstimatedDurationSeconds.isPresent() ? 
                        avgEstimatedDurationSeconds.getAsDouble() / 3600 : 0);
            metrics.put("averageActualDurationHours", avgActualDurationSeconds.isPresent() ? 
                        avgActualDurationSeconds.getAsDouble() / 3600 : 0);
            
            // Calculate duration variance
            List<Task> completedTasks = tasks.stream()
                    .filter(t -> t.isCompleted() && t.getActualDuration() != null)
                    .collect(Collectors.toList());
            
            double avgDurationVariance = 0.0;
            if (!completedTasks.isEmpty()) {
                avgDurationVariance = completedTasks.stream()
                        .mapToDouble(t -> {
                            double estimated = t.getEstimatedDuration().getSeconds();
                            double actual = t.getActualDuration().getSeconds();
                            return (actual - estimated) / estimated * 100.0; // Percentage variance
                        })
                        .average()
                        .orElse(0.0);
            }
            metrics.put("averageDurationVariancePercentage", avgDurationVariance);
            
            // Calculate bottleneck tasks
            List<Task> bottleneckTasks = identifyBottleneckTasks(tasks);
            metrics.put("bottleneckTaskCount", bottleneckTasks.size());
            
            // Convert to task IDs for the return value
            List<Long> bottleneckTaskIds = bottleneckTasks.stream()
                    .map(Task::getId)
                    .collect(Collectors.toList());
            metrics.put("bottleneckTaskIds", bottleneckTaskIds);
            
            return metrics;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating task completion metrics", e);
            return metrics;
        }
    }
    
    @Override
    public Map<String, Object> calculateAttendanceMetrics(Long projectId, LocalDate startDate, LocalDate endDate) {
        LOGGER.info("Calculating attendance metrics for project ID: " + projectId);
        
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (!projectOpt.isPresent()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return metrics;
            }
            
            Project project = projectOpt.get();
            
            // Apply date filtering
            LocalDate effectiveStartDate = startDate != null ? startDate : project.getStartDate();
            LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
            
            // Get meetings within date range
            List<Meeting> meetings = meetingRepository.findByProject(project)
                    .stream()
                    .filter(m -> !m.getDate().isBefore(effectiveStartDate) && !m.getDate().isAfter(effectiveEndDate))
                    .collect(Collectors.toList());
            
            // Calculate overall attendance statistics
            int totalMeetings = meetings.size();
            int totalAttendanceRecords = 0;
            int totalPresent = 0;
            
            for (Meeting meeting : meetings) {
                List<Attendance> attendances = meeting.getAttendances();
                totalAttendanceRecords += attendances.size();
                totalPresent += attendances.stream().filter(Attendance::isPresent).count();
            }
            
            metrics.put("totalMeetings", totalMeetings);
            metrics.put("totalAttendanceRecords", totalAttendanceRecords);
            metrics.put("totalPresent", totalPresent);
            
            double overallAttendanceRate = totalAttendanceRecords > 0 ? 
                    100.0 * totalPresent / totalAttendanceRecords : 0.0;
            metrics.put("overallAttendanceRate", overallAttendanceRate);
            
            // Calculate attendance by team member
            Map<TeamMember, Integer> meetingsAttendedByMember = new HashMap<>();
            Map<TeamMember, Integer> meetingsInvitedByMember = new HashMap<>();
            
            for (Meeting meeting : meetings) {
                for (Attendance attendance : meeting.getAttendances()) {
                    TeamMember member = attendance.getMember();
                    
                    // Update invited count
                    meetingsInvitedByMember.put(member, 
                            meetingsInvitedByMember.getOrDefault(member, 0) + 1);
                    
                    // Update attended count if present
                    if (attendance.isPresent()) {
                        meetingsAttendedByMember.put(member, 
                                meetingsAttendedByMember.getOrDefault(member, 0) + 1);
                    }
                }
            }
            
            // Calculate attendance rates by member
            Map<Long, Double> attendanceRateByMemberId = new HashMap<>();
            for (TeamMember member : meetingsInvitedByMember.keySet()) {
                int invited = meetingsInvitedByMember.getOrDefault(member, 0);
                int attended = meetingsAttendedByMember.getOrDefault(member, 0);
                
                if (invited > 0) {
                    double rate = 100.0 * attended / invited;
                    attendanceRateByMemberId.put(member.getId(), rate);
                }
            }
            metrics.put("attendanceRateByMember", attendanceRateByMemberId);
            
            // Calculate attendance trends over time
            Map<LocalDate, Double> attendanceRateByDate = new HashMap<>();
            for (Meeting meeting : meetings) {
                List<Attendance> attendances = meeting.getAttendances();
                long presentCount = attendances.stream().filter(Attendance::isPresent).count();
                double rate = attendances.size() > 0 ? 100.0 * presentCount / attendances.size() : 0.0;
                attendanceRateByDate.put(meeting.getDate(), rate);
            }
            metrics.put("attendanceRateByDate", attendanceRateByDate);
            
            return metrics;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating attendance metrics", e);
            return metrics;
        }
    }
    
    @Override
    public Map<String, Object> calculateTimelineDeviationMetrics(Long projectId) {
        LOGGER.info("Calculating timeline deviation metrics for project ID: " + projectId);
        
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (!projectOpt.isPresent()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return metrics;
            }
            
            Project project = projectOpt.get();
            
            // Get all tasks for the project
            List<Task> tasks = taskRepository.findByProject(project);
            
            // Get completed tasks
            List<Task> completedTasks = tasks.stream()
                    .filter(Task::isCompleted)
                    .collect(Collectors.toList());
            
            // Calculate average delay for completed tasks
            double avgDelay = 0.0;
            if (!completedTasks.isEmpty()) {
                avgDelay = completedTasks.stream()
                        .mapToLong(task -> {
                            if (task.getEndDate() != null) {
                                LocalDate plannedEndDate = task.getEndDate();
                                // Use task's actual completion date (not tracked in the model, so end date is assumed)
                                LocalDate actualEndDate = task.getEndDate();
                                return ChronoUnit.DAYS.between(plannedEndDate, actualEndDate);
                            }
                            return 0;
                        })
                        .average()
                        .orElse(0.0);
            }
            metrics.put("averageTaskDelay", avgDelay);
            
            // Calculate tasks at risk (incomplete tasks with end date in the past)
            LocalDate today = LocalDate.now();
            List<Task> tasksAtRisk = tasks.stream()
                    .filter(task -> !task.isCompleted() && task.getEndDate() != null && task.getEndDate().isBefore(today))
                    .collect(Collectors.toList());
            
            metrics.put("tasksAtRiskCount", tasksAtRisk.size());
            
            // Convert to task IDs for the return value
            List<Long> tasksAtRiskIds = tasksAtRisk.stream()
                    .map(Task::getId)
                    .collect(Collectors.toList());
            metrics.put("tasksAtRiskIds", tasksAtRiskIds);
            
            // Calculate milestone deviations
            List<Milestone> milestones = milestoneRepository.findByProject(project);
            List<Milestone> passedMilestones = milestones.stream()
                    .filter(m -> m.getDate().isBefore(today))
                    .collect(Collectors.toList());
            
            // Count milestones that passed but have incomplete dependencies
            int milestonesWithIncompleteDependencies = 0;
            // Note: This would require specific knowledge of which tasks are tied to milestones,
            // which isn't directly modeled in the current entities. This is a placeholder.
            metrics.put("milestonesWithIncompleteDependencies", milestonesWithIncompleteDependencies);
            
            // Calculate overall project timeline status
            LocalDate originalEndDate = project.getGoalEndDate();
            LocalDate projectedEndDate = estimateProjectEndDate(project, tasks);
            
            long projectedDelay = ChronoUnit.DAYS.between(originalEndDate, projectedEndDate);
            metrics.put("projectedDelay", projectedDelay);
            
            boolean isOnSchedule = projectedDelay <= 0;
            metrics.put("isOnSchedule", isOnSchedule);
            
            double timelineDeviation = originalEndDate.isEqual(project.getStartDate()) ? 0 : 
                    100.0 * projectedDelay / ChronoUnit.DAYS.between(project.getStartDate(), originalEndDate);
            metrics.put("timelineDeviationPercentage", timelineDeviation);
            
            return metrics;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating timeline deviation metrics", e);
            return metrics;
        }
    }
    
    @Override
    public Map<String, Object> calculateIndividualPerformanceMetrics(Long teamMemberId, LocalDate startDate, LocalDate endDate) {
        LOGGER.info("Calculating individual performance metrics for team member ID: " + teamMemberId);
        
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get team member
            Optional<TeamMember> memberOpt = teamMemberRepository.findById(teamMemberId);
            if (!memberOpt.isPresent()) {
                LOGGER.warning("Team member not found with ID: " + teamMemberId);
                return metrics;
            }
            
            TeamMember member = memberOpt.get();
            
            // Add basic member information
            metrics.put("memberId", member.getId());
            metrics.put("memberName", member.getFullName());
            
            // Apply date filtering
            LocalDate effectiveStartDate = startDate != null ? startDate : LocalDate.now().minusMonths(3);
            LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
            
            // Get tasks assigned to this member
            List<Task> assignedTasks = new ArrayList<>(member.getAssignedTasks());
            
            // Task metrics
            int totalTasks = assignedTasks.size();
            long completedTasks = assignedTasks.stream().filter(Task::isCompleted).count();
            long inProgressTasks = assignedTasks.stream()
                    .filter(t -> !t.isCompleted() && t.getProgress() > 0).count();
                    
            metrics.put("totalAssignedTasks", totalTasks);
            metrics.put("completedTasks", completedTasks);
            metrics.put("inProgressTasks", inProgressTasks);
            metrics.put("notStartedTasks", totalTasks - completedTasks - inProgressTasks);
            
            double taskCompletionRate = totalTasks > 0 ? 100.0 * completedTasks / totalTasks : 0.0;
            metrics.put("taskCompletionRate", taskCompletionRate);
            
            // Get task progress average
            OptionalDouble avgProgress = assignedTasks.stream()
                    .mapToInt(Task::getProgress)
                    .average();
            metrics.put("averageTaskProgress", avgProgress.orElse(0.0));
            
            // Attendance metrics
            List<Attendance> attendances = attendanceRepository.findByMember(member);
            
            // Filter by date range
            attendances = attendances.stream()
                    .filter(a -> {
                        LocalDate meetingDate = a.getMeeting().getDate();
                        return !meetingDate.isBefore(effectiveStartDate) && !meetingDate.isAfter(effectiveEndDate);
                    })
                    .collect(Collectors.toList());
            
            int totalMeetings = attendances.size();
            long attendedMeetings = attendances.stream().filter(Attendance::isPresent).count();
            
            metrics.put("totalMeetings", totalMeetings);
            metrics.put("attendedMeetings", attendedMeetings);
            
            double attendanceRate = totalMeetings > 0 ? 100.0 * attendedMeetings / totalMeetings : 0.0;
            metrics.put("attendanceRate", attendanceRate);
            
            // Task duration metrics
            List<Task> completedAssignedTasks = assignedTasks.stream()
                    .filter(t -> t.isCompleted() && t.getActualDuration() != null)
                    .collect(Collectors.toList());
            
            double avgDurationAccuracy = 0.0;
            if (!completedAssignedTasks.isEmpty()) {
                avgDurationAccuracy = completedAssignedTasks.stream()
                        .mapToDouble(t -> {
                            double estimatedHours = t.getEstimatedDuration().toHours();
                            double actualHours = t.getActualDuration().toHours();
                            return estimatedHours > 0 ? 100.0 * (1.0 - Math.abs(actualHours - estimatedHours) / estimatedHours) : 0.0;
                        })
                        .average()
                        .orElse(0.0);
            }
            metrics.put("averageDurationAccuracy", avgDurationAccuracy);
            
            // Task priority distribution
            Map<Task.Priority, Long> tasksByPriority = assignedTasks.stream()
                    .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
            metrics.put("tasksByPriority", tasksByPriority);
            
            // Calculate performance trend over time
            // Group completed tasks by week
            Map<Integer, Long> completedTasksByWeek = completedAssignedTasks.stream()
                    .collect(Collectors.groupingBy(
                            t -> getWeekOfYear(t.getEndDate()),
                            Collectors.counting()));
            metrics.put("completedTasksByWeek", completedTasksByWeek);
            
            return metrics;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating individual performance metrics", e);
            return metrics;
        }
    }
    
    @Override
    public Map<String, Object> calculateSubsystemPerformanceMetrics(Long projectId) {
        LOGGER.info("Calculating subsystem performance metrics for project ID: " + projectId);
        
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (!projectOpt.isPresent()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return metrics;
            }
            
            Project project = projectOpt.get();
            
            // Get all tasks for the project
            List<Task> tasks = taskRepository.findByProject(project);
            
            // Group tasks by subsystem
            Map<Subsystem, List<Task>> tasksBySubsystem = tasks.stream()
                    .collect(Collectors.groupingBy(Task::getSubsystem));
            
            // Calculate metrics for each subsystem
            Map<Long, Map<String, Object>> subsystemMetrics = new HashMap<>();
            
            for (Map.Entry<Subsystem, List<Task>> entry : tasksBySubsystem.entrySet()) {
                Subsystem subsystem = entry.getKey();
                List<Task> subsystemTasks = entry.getValue();
                
                Map<String, Object> subsystemMetric = new HashMap<>();
                
                // Task counts
                int totalTasks = subsystemTasks.size();
                subsystemMetric.put("totalTasks", totalTasks);
                
                long completedTasks = subsystemTasks.stream().filter(Task::isCompleted).count();
                subsystemMetric.put("completedTasks", completedTasks);
                
                double completionRate = totalTasks > 0 ? 100.0 * completedTasks / totalTasks : 0.0;
                subsystemMetric.put("completionRate", completionRate);
                
                // Task status distribution
                Map<TaskStatus, Long> taskStatusCounts = calculateTaskStatusCounts(subsystemTasks);
                subsystemMetric.put("taskStatusCounts", taskStatusCounts);
                
                // Priority distribution
                Map<Task.Priority, Long> priorityCounts = subsystemTasks.stream()
                        .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
                subsystemMetric.put("priorityCounts", priorityCounts);
                
                // Calculate average task durations
                OptionalDouble avgEstimatedDuration = subsystemTasks.stream()
                        .mapToLong(t -> t.getEstimatedDuration().toHours())
                        .average();
                subsystemMetric.put("averageEstimatedDurationHours", avgEstimatedDuration.orElse(0.0));
                
                // Check subsystem status
                subsystemMetric.put("status", subsystem.getStatus().getDisplayName());
                
                // Add to the map of all subsystem metrics
                subsystemMetrics.put(subsystem.getId(), subsystemMetric);
            }
            
            metrics.put("subsystemMetrics", subsystemMetrics);
            
            // Calculate overall subsystem statistics
            int totalSubsystems = tasksBySubsystem.size();
            metrics.put("totalSubsystems", totalSubsystems);
            
            long completedSubsystems = tasksBySubsystem.entrySet().stream()
                    .filter(entry -> entry.getKey().getStatus() == Subsystem.Status.COMPLETED)
                    .count();
            metrics.put("completedSubsystems", completedSubsystems);
            
            double subsystemCompletionRate = totalSubsystems > 0 ? 
                    100.0 * completedSubsystems / totalSubsystems : 0.0;
            metrics.put("subsystemCompletionRate", subsystemCompletionRate);
            
            return metrics;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating subsystem performance metrics", e);
            return metrics;
        }
    }
    
    @Override
    public Map<String, Object> generateProjectHealthDashboard(Long projectId) {
        LOGGER.info("Generating project health dashboard for project ID: " + projectId);
        
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            // Get project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (!projectOpt.isPresent()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return dashboard;
            }
            
            Project project = projectOpt.get();
            
            // Add basic project information
            dashboard.put("projectId", project.getId());
            dashboard.put("projectName", project.getName());
            dashboard.put("startDate", project.getStartDate());
            dashboard.put("goalEndDate", project.getGoalEndDate());
            dashboard.put("hardDeadline", project.getHardDeadline());
            
            // Calculate project progress metrics
            Map<String, Object> progressMetrics = calculateProjectProgressMetrics(projectId);
            dashboard.put("progressMetrics", progressMetrics);
            
            // Calculate timeline deviation metrics
            Map<String, Object> timelineMetrics = calculateTimelineDeviationMetrics(projectId);
            dashboard.put("timelineMetrics", timelineMetrics);
            
            // Calculate task completion metrics
            Map<String, Object> taskMetrics = calculateTaskCompletionMetrics(projectId);
            dashboard.put("taskMetrics", taskMetrics);
            
            // Calculate attendance metrics
            Map<String, Object> attendanceMetrics = calculateAttendanceMetrics(projectId, null, null);
            dashboard.put("attendanceMetrics", attendanceMetrics);
            
            // Calculate subsystem performance metrics
            Map<String, Object> subsystemMetrics = calculateSubsystemPerformanceMetrics(projectId);
            dashboard.put("subsystemMetrics", subsystemMetrics);
            
            // Calculate team performance metrics
            Map<String, Object> teamMetrics = calculateTeamPerformanceMetrics(projectId, null, null);
            dashboard.put("teamMetrics", teamMetrics);
            
            // Overall project health score (0-100)
            double healthScore = calculateProjectHealthScore(
                    progressMetrics,
                    timelineMetrics,
                    taskMetrics,
                    attendanceMetrics
            );
            dashboard.put("healthScore", healthScore);
            
            // Health status
            String healthStatus = determineHealthStatus(healthScore);
            dashboard.put("healthStatus", healthStatus);
            
            // Add list of at-risk tasks
            List<Long> tasksAtRiskIds = (List<Long>) timelineMetrics.getOrDefault("tasksAtRiskIds", new ArrayList<>());
            dashboard.put("tasksAtRiskIds", tasksAtRiskIds);
            
            // Add list of bottleneck tasks
            List<Long> bottleneckTaskIds = (List<Long>) taskMetrics.getOrDefault("bottleneckTaskIds", new ArrayList<>());
            dashboard.put("bottleneckTaskIds", bottleneckTaskIds);
            
            return dashboard;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating project health dashboard", e);
            return dashboard;
        }
    }
    
    // Helper methods
    
    /**
     * Calculates the completion percentage for a list of tasks.
     * 
     * @param tasks the list of tasks
     * @return the completion percentage (0-100)
     */
    private double calculateTaskCompletionPercentage(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0.0;
        }
        
        double totalProgress = tasks.stream().mapToInt(Task::getProgress).sum();
        return totalProgress / tasks.size();
    }
    
    /**
     * Determines the task status based on the progress.
     * 
     * @param task the task to check
     * @return the task status
     */
    private TaskStatus determineTaskStatus(Task task) {
        if (task.isCompleted()) {
            return TaskStatus.COMPLETED;
        } else if (task.getProgress() > 0) {
            return TaskStatus.IN_PROGRESS;
        } else {
            return TaskStatus.NOT_STARTED;
        }
    }
    
    /**
     * Calculates the count of tasks by status.
     * 
     * @param tasks the list of tasks
     * @return map of status to count
     */
    private Map<TaskStatus, Long> calculateTaskStatusCounts(List<Task> tasks) {
        Map<TaskStatus, Long> counts = new HashMap<>();
        
        // Initialize with all status types
        for (TaskStatus status : TaskStatus.values()) {
            counts.put(status, 0L);
        }
        
        // Count tasks by status
        for (Task task : tasks) {
            TaskStatus status = determineTaskStatus(task);
            counts.put(status, counts.getOrDefault(status, 0L) + 1);
        }
        
        return counts;
    }
    
    /**
     * Calculates task status counts by priority.
     * 
     * @param tasks the list of tasks
     * @return nested map of priority to status counts
     */
    private Map<Task.Priority, Map<TaskStatus, Long>> calculateTaskStatusByPriority(List<Task> tasks) {
        Map<Task.Priority, Map<TaskStatus, Long>> result = new HashMap<>();
        
        // Initialize with all priority types
        for (Task.Priority priority : Task.Priority.values()) {
            Map<TaskStatus, Long> statusMap = new HashMap<>();
            
            for (TaskStatus status : TaskStatus.values()) {
                statusMap.put(status, 0L);
            }
            
            result.put(priority, statusMap);
        }
        
        // Count tasks by priority and status
        for (Task task : tasks) {
            TaskStatus status = determineTaskStatus(task);
            Task.Priority priority = task.getPriority();
            
            Map<TaskStatus, Long> statusMap = result.get(priority);
            statusMap.put(status, statusMap.getOrDefault(status, 0L) + 1);
        }
        
        return result;
    }
    
    /**
     * Identifies bottleneck tasks in a project.
     * 
     * @param tasks the list of tasks
     * @return list of bottleneck tasks
     */
    private List<Task> identifyBottleneckTasks(List<Task> tasks) {
        // Define criteria for bottleneck tasks:
        // 1. Not completed
        // 2. Has dependencies on it (other tasks depend on it)
        // 3. Progress is below average for the project
        
        double avgProgress = tasks.stream()
                .filter(t -> !t.isCompleted())
                .mapToInt(Task::getProgress)
                .average()
                .orElse(0.0);
        
        return tasks.stream()
                .filter(task -> 
                    !task.isCompleted() && 
                    !task.getPostDependencies().isEmpty() && 
                    task.getProgress() < avgProgress)
                .collect(Collectors.toList());
    }
    
    /**
     * Estimates the project end date based on current progress.
     * 
     * @param project the project
     * @param tasks the list of tasks
     * @return the estimated end date
     */
    private LocalDate estimateProjectEndDate(Project project, List<Task> tasks) {
        LocalDate today = LocalDate.now();
        
        // If project hasn't started or has no tasks, return the planned end date
        if (today.isBefore(project.getStartDate()) || tasks.isEmpty()) {
            return project.getGoalEndDate();
        }
        
        // Calculate overall progress
        double progress = calculateTaskCompletionPercentage(tasks);
        
        // If no progress, return the planned end date
        if (progress <= 0) {
            return project.getGoalEndDate();
        }
        
        // Calculate elapsed time since project start
        long elapsedDays = ChronoUnit.DAYS.between(project.getStartDate(), today);
        
        // Estimate total days needed based on current progress rate
        long estimatedTotalDays = progress > 0 ? Math.round(elapsedDays * 100 / progress) : 0;
        
        // Calculate remaining days
        long remainingDays = estimatedTotalDays - elapsedDays;
        
        // Estimate end date
        return today.plusDays(remainingDays);
    }
    
    /**
     * Gets the week of year for a date.
     * 
     * @param date the date
     * @return the week of year
     */
    private int getWeekOfYear(LocalDate date) {
        return date.get(java.time.temporal.WeekFields.of(Locale.getDefault()).weekOfYear());
    }
    
    /**
     * Calculates the overall project health score based on various metrics.
     * 
     * @param progressMetrics project progress metrics
     * @param timelineMetrics timeline deviation metrics
     * @param taskMetrics task completion metrics
     * @param attendanceMetrics attendance metrics
     * @return the health score (0-100)
     */
    private double calculateProjectHealthScore(
            Map<String, Object> progressMetrics,
            Map<String, Object> timelineMetrics,
            Map<String, Object> taskMetrics,
            Map<String, Object> attendanceMetrics) {
        
        // Extract key metrics with default values
        double completionPercentage = getDoubleValue(progressMetrics, "completionPercentage", 0.0);
        double timeProgressPercentage = getDoubleValue(progressMetrics, "timeProgressPercentage", 0.0);
        double scheduleVariance = getDoubleValue(progressMetrics, "scheduleVariance", 0.0);
        double projectedDelay = getDoubleValue(timelineMetrics, "projectedDelay", 0.0);
        double overallAttendanceRate = getDoubleValue(attendanceMetrics, "overallAttendanceRate", 0.0);
        
        // Calculate schedule performance score (40%)
        double scheduleScore = 40.0;
        if (timeProgressPercentage > 0) {
            // Penalize if behind schedule, reward if ahead
            scheduleScore = Math.max(0, Math.min(50, 40 + 0.5 * scheduleVariance));
        }
        
        // Calculate task completion score (30%)
        double taskScore = 30.0 * (completionPercentage / 100.0);
        
        // Calculate timeline adherence score (20%)
        double timelineScore = 20.0;
        if (projectedDelay > 0) {
            // Reduce score based on projected delay (up to 10 days)
            timelineScore = Math.max(0, 20.0 - 2.0 * Math.min(10, projectedDelay));
        }
        
        // Calculate attendance score (10%)
        double attendanceScore = 10.0 * (overallAttendanceRate / 100.0);
        
        // Calculate overall health score
        return scheduleScore + taskScore + timelineScore + attendanceScore;
    }
    
    /**
     * Determines the health status based on the health score.
     * 
     * @param healthScore the health score
     * @return the health status
     */
    private String determineHealthStatus(double healthScore) {
        if (healthScore >= 80) {
            return "Healthy";
        } else if (healthScore >= 60) {
            return "Moderate";
        } else if (healthScore >= 40) {
            return "At Risk";
        } else {
            return "Critical";
        }
    }
    
    /**
     * Safely gets a double value from a map with a default.
     * 
     * @param map the map
     * @param key the key
     * @param defaultValue the default value
     * @return the value, or default if not found or not a number
     */
    private double getDoubleValue(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
}