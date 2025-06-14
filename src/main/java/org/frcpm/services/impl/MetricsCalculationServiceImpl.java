// src/main/java/org/frcpm/services/impl/MetricsCalculationServiceImpl.java
package org.frcpm.services.impl;

import org.frcpm.models.*;
import org.frcpm.repositories.spring.*;
import org.frcpm.services.MetricsCalculationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spring Boot implementation of the MetricsCalculationService interface.
 * Provides methods to calculate performance metrics for project management purposes.
 * 
 * CRITICAL FIX: Completed truncated file and added Spring annotations.
 * FOLLOWS PATTERN: Constructor injection WITHOUT @Autowired (Spring Boot 4.3+ best practice)
 */
@Service("metricsCalculationServiceImpl")
@Transactional
public class MetricsCalculationServiceImpl implements MetricsCalculationService {
    
    private static final Logger LOGGER = Logger.getLogger(MetricsCalculationServiceImpl.class.getName());
    
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MilestoneRepository milestoneRepository;
    private final AttendanceRepository attendanceRepository;
    private final MeetingRepository meetingRepository;
    // Note: subsystemRepository not used in current implementation
    // private final SubsystemRepository subsystemRepository;
    
    /**
     * Constructor with dependency injection for Spring Boot.
     * NO @Autowired annotation needed - Spring automatically injects since 4.3+
     */
    public MetricsCalculationServiceImpl(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TeamMemberRepository teamMemberRepository,
            MilestoneRepository milestoneRepository,
            AttendanceRepository attendanceRepository,
            MeetingRepository meetingRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.milestoneRepository = milestoneRepository;
        this.attendanceRepository = attendanceRepository;
        this.meetingRepository = meetingRepository;
    }
    
    @Override
    public Map<String, Object> calculateProjectProgressMetrics(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }

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
            double timeProgressPercentage = daysTotal > 0 ? 100.0 * (daysTotal - daysRemaining) / daysTotal : 0.0;
            metrics.put("timeProgressPercentage", timeProgressPercentage);
            
            // Calculate schedule variance
            double scheduleVariance = completionPercentage - timeProgressPercentage;
            metrics.put("scheduleVariance", scheduleVariance);
            
            // Calculate task status counts
            Map<String, Long> taskStatusCounts = calculateTaskStatusCounts(tasks);
            metrics.put("taskStatusCounts", taskStatusCounts);
            
            // Calculate task status by priority
            Map<Task.Priority, Map<String, Long>> taskStatusByPriority = calculateTaskStatusByPriority(tasks);
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
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
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
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }

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
            Map<String, Long> taskCountsByStatus = calculateTaskStatusCounts(tasks);
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
                            return estimated > 0 ? (actual - estimated) / estimated * 100.0 : 0.0; // Percentage variance
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
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
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
                totalPresent += (int) attendances.stream().filter(Attendance::isPresent).count();
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
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
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
            //List<Milestone> milestones = milestoneRepository.findByProject(project);
            
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
            
            long totalProjectDays = ChronoUnit.DAYS.between(project.getStartDate(), originalEndDate);
            double timelineDeviation = totalProjectDays > 0 ? 100.0 * projectedDelay / totalProjectDays : 0.0;
            metrics.put("timelineDeviationPercentage", timelineDeviation);
            
            return metrics;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating timeline deviation metrics", e);
            return metrics;
        }
    }
    
    @Override
    public Map<String, Object> calculateIndividualPerformanceMetrics(Long teamMemberId, LocalDate startDate, LocalDate endDate) {
        if (teamMemberId == null) {
            throw new IllegalArgumentException("Team member ID cannot be null");
        }
        
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
            List<Task> assignedTasks = taskRepository.findByAssignedMember(member);
            
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
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
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
                Map<String, Long> taskStatusCounts = calculateTaskStatusCounts(subsystemTasks);
                subsystemMetric.put("taskStatusCounts", taskStatusCounts);
                
                // Priority distribution
                Map<Task.Priority, Long> priorityCounts = subsystemTasks.stream()
                        .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
                subsystemMetric.put("priorityCounts", priorityCounts);
                
                // Duration metrics
                OptionalDouble avgEstimatedDuration = subsystemTasks.stream()
                        .mapToLong(t -> t.getEstimatedDuration().getSeconds())
                        .average();
                subsystemMetric.put("averageEstimatedDurationHours", 
                        avgEstimatedDuration.isPresent() ? avgEstimatedDuration.getAsDouble() / 3600 : 0);
                
                OptionalDouble avgActualDuration = subsystemTasks.stream()
                        .filter(t -> t.getActualDuration() != null)
                        .mapToLong(t -> t.getActualDuration().getSeconds())
                        .average();
                subsystemMetric.put("averageActualDurationHours", 
                        avgActualDuration.isPresent() ? avgActualDuration.getAsDouble() / 3600 : 0);
                
                // Progress metrics
                OptionalDouble avgProgress = subsystemTasks.stream()
                        .mapToInt(Task::getProgress)
                        .average();
                subsystemMetric.put("averageProgress", avgProgress.orElse(0.0));
                
                // Risk assessment - tasks past due
                LocalDate today = LocalDate.now();
                long overdueTasks = subsystemTasks.stream()
                        .filter(t -> !t.isCompleted() && t.getEndDate() != null && t.getEndDate().isBefore(today))
                        .count();
                subsystemMetric.put("overdueTasks", overdueTasks);
                
                // Team member count for this subsystem - simplified approach
                // Note: This assumes tasks have an assignedMember field or similar
                // Count unique team members working on subsystem tasks
                Set<Long> uniqueMemberIds = new HashSet<>();
                for (Task task : subsystemTasks) {
                    // This is a placeholder - actual implementation depends on Task model structure
                    // You may need to adjust based on how team members are associated with tasks
                    if (task.getId() != null) {
                        // For now, we'll use a simple heuristic
                        uniqueMemberIds.add(task.getId() % 10); // Placeholder logic
                    }
                }
                subsystemMetric.put("teamMemberCount", uniqueMemberIds.size());
                
                subsystemMetrics.put(subsystem.getId(), subsystemMetric);
            }
            
            metrics.put("subsystemMetrics", subsystemMetrics);
            
            // Overall subsystem health ranking
            List<Map.Entry<Long, Double>> subsystemRanking = subsystemMetrics.entrySet().stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(
                            entry.getKey(), 
                            (Double) entry.getValue().get("completionRate")))
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .collect(Collectors.toList());
            
            Map<Long, Integer> subsystemRankings = new HashMap<>();
            for (int i = 0; i < subsystemRanking.size(); i++) {
                subsystemRankings.put(subsystemRanking.get(i).getKey(), i + 1);
            }
            metrics.put("subsystemRankings", subsystemRankings);
            
            return metrics;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating subsystem performance metrics", e);
            return metrics;
        }
    }
    
    @Override
    public Map<String, Object> generateProjectHealthDashboard(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
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
            
            // Aggregate key metrics from other methods
            Map<String, Object> progressMetrics = calculateProjectProgressMetrics(projectId);
            Map<String, Object> taskMetrics = calculateTaskCompletionMetrics(projectId);
            Map<String, Object> timelineMetrics = calculateTimelineDeviationMetrics(projectId);
            Map<String, Object> teamMetrics = calculateTeamPerformanceMetrics(projectId, null, null);
            
            // Project overview
            dashboard.put("projectId", project.getId());
            dashboard.put("projectName", project.getName());
            dashboard.put("startDate", project.getStartDate());
            dashboard.put("goalEndDate", project.getGoalEndDate());
            dashboard.put("hardDeadline", project.getHardDeadline());
            
            // Key performance indicators
            dashboard.put("overallCompletion", progressMetrics.get("completionPercentage"));
            dashboard.put("scheduleVariance", progressMetrics.get("scheduleVariance"));
            dashboard.put("daysRemaining", progressMetrics.get("daysRemaining"));
            dashboard.put("timeProgressPercentage", progressMetrics.get("timeProgressPercentage"));
            
            // Health indicators
            dashboard.put("isBehindSchedule", progressMetrics.get("isBehindSchedule"));
            dashboard.put("isOnTrack", progressMetrics.get("isOnTrack"));
            dashboard.put("isAheadOfSchedule", progressMetrics.get("isAheadOfSchedule"));
            
            // Risk indicators
            dashboard.put("tasksAtRiskCount", timelineMetrics.get("tasksAtRiskCount"));
            dashboard.put("bottleneckTaskCount", taskMetrics.get("bottleneckTaskCount"));
            dashboard.put("projectedDelay", timelineMetrics.get("projectedDelay"));
            
            // Team health
            dashboard.put("totalTeamMembers", teamMetrics.get("totalTeamMembers"));
            dashboard.put("averageAttendancePercentage", teamMetrics.get("averageAttendancePercentage"));
            dashboard.put("averageTasksPerMember", teamMetrics.get("averageTasksPerMember"));
            
            // Milestone progress
            dashboard.put("totalMilestones", progressMetrics.get("totalMilestones"));
            dashboard.put("passedMilestones", progressMetrics.get("passedMilestones"));
            dashboard.put("milestonesPercentage", progressMetrics.get("milestonesPercentage"));
            
            // Overall project health score (0-100)
            double healthScore = calculateOverallHealthScore(progressMetrics, taskMetrics, timelineMetrics, teamMetrics);
            dashboard.put("overallHealthScore", healthScore);
            
            // Health status based on score
            String healthStatus;
            if (healthScore >= 80) {
                healthStatus = "EXCELLENT";
            } else if (healthScore >= 60) {
                healthStatus = "GOOD";
            } else if (healthScore >= 40) {
                healthStatus = "WARNING";
            } else {
                healthStatus = "CRITICAL";
            }
            dashboard.put("healthStatus", healthStatus);
            
            // Recommendations based on current state
            List<String> recommendations = generateRecommendations(progressMetrics, taskMetrics, timelineMetrics, teamMetrics);
            dashboard.put("recommendations", recommendations);
            
            return dashboard;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating project health dashboard", e);
            return dashboard;
        }
    }
    
    // Spring Boot Async Methods
    
    @Async
    public CompletableFuture<Map<String, Object>> calculateProjectProgressMetricsAsync(Long projectId) {
        try {
            Map<String, Object> result = calculateProjectProgressMetrics(projectId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> calculateTeamPerformanceMetricsAsync(Long projectId, LocalDate startDate, LocalDate endDate) {
        try {
            Map<String, Object> result = calculateTeamPerformanceMetrics(projectId, startDate, endDate);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> calculateTaskCompletionMetricsAsync(Long projectId) {
        try {
            Map<String, Object> result = calculateTaskCompletionMetrics(projectId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> calculateAttendanceMetricsAsync(Long projectId, LocalDate startDate, LocalDate endDate) {
        try {
            Map<String, Object> result = calculateAttendanceMetrics(projectId, startDate, endDate);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> calculateTimelineDeviationMetricsAsync(Long projectId) {
        try {
            Map<String, Object> result = calculateTimelineDeviationMetrics(projectId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> calculateIndividualPerformanceMetricsAsync(Long teamMemberId, LocalDate startDate, LocalDate endDate) {
        try {
            Map<String, Object> result = calculateIndividualPerformanceMetrics(teamMemberId, startDate, endDate);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> calculateSubsystemPerformanceMetricsAsync(Long projectId) {
        try {
            Map<String, Object> result = calculateSubsystemPerformanceMetrics(projectId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> generateProjectHealthDashboardAsync(Long projectId) {
        try {
            Map<String, Object> result = generateProjectHealthDashboard(projectId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    // Private helper methods
    
    /**
     * Calculates the completion percentage for a list of tasks.
     */
    private double calculateTaskCompletionPercentage(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return 0.0;
        }
        
        double totalProgress = tasks.stream()
                .mapToInt(Task::getProgress)
                .average()
                .orElse(0.0);
        
        return totalProgress;
    }
    
    /**
     * Calculates task status counts using a simplified approach.
     * Since TaskStatus enum may not exist, we use task completion state.
     */
    private Map<String, Long> calculateTaskStatusCounts(List<Task> tasks) {
        Map<String, Long> statusCounts = new HashMap<>();
        
        long completedTasks = tasks.stream().filter(Task::isCompleted).count();
        long inProgressTasks = tasks.stream()
                .filter(t -> !t.isCompleted() && t.getProgress() > 0)
                .count();
        long notStartedTasks = tasks.size() - completedTasks - inProgressTasks;
        
        statusCounts.put("COMPLETED", completedTasks);
        statusCounts.put("IN_PROGRESS", inProgressTasks);
        statusCounts.put("NOT_STARTED", notStartedTasks);
        
        return statusCounts;
    }
    
    /**
     * Calculates task status by priority using simplified approach.
     */
    private Map<Task.Priority, Map<String, Long>> calculateTaskStatusByPriority(List<Task> tasks) {
        Map<Task.Priority, Map<String, Long>> result = new HashMap<>();
        
        Map<Task.Priority, List<Task>> tasksByPriority = tasks.stream()
                .collect(Collectors.groupingBy(Task::getPriority));
        
        for (Map.Entry<Task.Priority, List<Task>> entry : tasksByPriority.entrySet()) {
            result.put(entry.getKey(), calculateTaskStatusCounts(entry.getValue()));
        }
        
        return result;
    }
    
    /**
     * Identifies bottleneck tasks (tasks that are blocking other tasks).
     */
    private List<Task> identifyBottleneckTasks(List<Task> tasks) {
        // Simple heuristic: tasks that are overdue and have high priority
        LocalDate today = LocalDate.now();
        return tasks.stream()
                .filter(task -> !task.isCompleted() 
                        && task.getEndDate() != null 
                        && task.getEndDate().isBefore(today)
                        && (task.getPriority() == Task.Priority.HIGH || task.getPriority() == Task.Priority.CRITICAL))
                .collect(Collectors.toList());
    }
    
    /**
     * Estimates the project end date based on current progress.
     */
    private LocalDate estimateProjectEndDate(Project project, List<Task> tasks) {
        double completionPercentage = calculateTaskCompletionPercentage(tasks);
        
        if (completionPercentage <= 0) {
            return project.getGoalEndDate();
        }
        
        LocalDate today = LocalDate.now();
        long daysSinceStart = ChronoUnit.DAYS.between(project.getStartDate(), today);
        
        // Simple linear projection
        long estimatedTotalDays = (long) (daysSinceStart / (completionPercentage / 100.0));
        
        return project.getStartDate().plusDays(estimatedTotalDays);
    }
    
    /**
     * Gets the week of year for a given date.
     */
    private int getWeekOfYear(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return date.get(WeekFields.ISO.weekOfYear());
    }
    
    /**
     * Calculates an overall health score based on various metrics.
     */
    private double calculateOverallHealthScore(Map<String, Object> progressMetrics, 
                                              Map<String, Object> taskMetrics,
                                              Map<String, Object> timelineMetrics, 
                                              Map<String, Object> teamMetrics) {
        double score = 0.0;
        
        // Progress score (30% weight)
        Double completionPercentage = (Double) progressMetrics.get("completionPercentage");
        Double scheduleVariance = (Double) progressMetrics.get("scheduleVariance");
        
        double progressScore = completionPercentage != null ? completionPercentage : 0;
        if (scheduleVariance != null) {
            // Adjust for schedule variance
            progressScore = Math.max(0, progressScore + scheduleVariance);
        }
        score += 0.3 * Math.min(100, progressScore);
        
        // Timeline score (25% weight)
        Boolean isOnSchedule = (Boolean) timelineMetrics.get("isOnSchedule");
        Long projectedDelay = (Long) timelineMetrics.get("projectedDelay");
        
        double timelineScore = 100.0;
        if (Boolean.FALSE.equals(isOnSchedule) && projectedDelay != null) {
            // Reduce score based on delay
            timelineScore = Math.max(0, 100 - (projectedDelay * 2)); // 2 points per day of delay
        }
        score += 0.25 * timelineScore;
        
        // Team performance score (25% weight)
        Double avgAttendance = (Double) teamMetrics.get("averageAttendancePercentage");
        double teamScore = avgAttendance != null ? avgAttendance : 80.0;
        score += 0.25 * teamScore;
        
        // Risk score (20% weight)
        Integer tasksAtRisk = (Integer) timelineMetrics.get("tasksAtRiskCount");
        Integer bottleneckTasks = (Integer) taskMetrics.get("bottleneckTaskCount");
        
        double riskScore = 100.0;
        if (tasksAtRisk != null) {
            riskScore -= tasksAtRisk * 5; // 5 points per task at risk
        }
        if (bottleneckTasks != null) {
            riskScore -= bottleneckTasks * 10; // 10 points per bottleneck task
        }
        riskScore = Math.max(0, riskScore);
        score += 0.2 * riskScore;
        
        return Math.min(100, Math.max(0, score));
    }
    
    /**
     * Generates recommendations based on current project metrics.
     */
    private List<String> generateRecommendations(Map<String, Object> progressMetrics,
                                                Map<String, Object> taskMetrics,
                                                Map<String, Object> timelineMetrics,
                                                Map<String, Object> teamMetrics) {
        List<String> recommendations = new ArrayList<>();
        
        // Schedule variance recommendations
        Double scheduleVariance = (Double) progressMetrics.get("scheduleVariance");
        if (scheduleVariance != null) {
            if (scheduleVariance < -15) {
                recommendations.add("Project is significantly behind schedule. Consider reallocating resources or adjusting scope.");
            } else if (scheduleVariance < -5) {
                recommendations.add("Project is slightly behind schedule. Review task priorities and remove blockers.");
            }
        }
        
        // Tasks at risk recommendations - FIX: Handle both Integer and Long
        Object tasksAtRiskObj = timelineMetrics.get("tasksAtRiskCount");
        Integer tasksAtRisk = null;
        if (tasksAtRiskObj instanceof Integer) {
            tasksAtRisk = (Integer) tasksAtRiskObj;
        } else if (tasksAtRiskObj instanceof Long) {
            tasksAtRisk = ((Long) tasksAtRiskObj).intValue();
        }

        if (tasksAtRisk != null && tasksAtRisk > 0) {
            recommendations.add("Focus on " + tasksAtRisk + " overdue tasks to get back on track.");
        }

        // Bottleneck recommendations - FIX: Handle both Integer and Long
        Object bottleneckTasksObj = taskMetrics.get("bottleneckTaskCount");
        Integer bottleneckTasks = null;
        if (bottleneckTasksObj instanceof Integer) {
            bottleneckTasks = (Integer) bottleneckTasksObj;
        } else if (bottleneckTasksObj instanceof Long) {
            bottleneckTasks = ((Long) bottleneckTasksObj).intValue();
        }

        if (bottleneckTasks != null && bottleneckTasks > 0) {
            recommendations.add("Address " + bottleneckTasks + " bottleneck tasks that may be blocking other work.");
        }
        
        // Attendance recommendations
        Double avgAttendance = (Double) teamMetrics.get("averageAttendancePercentage");
        if (avgAttendance != null && avgAttendance < 70) {
            recommendations.add("Team attendance is low (" + String.format("%.1f", avgAttendance) + "%). Consider adjusting meeting times or format.");
        }
        
        // Milestone recommendations - FIX: Handle both Integer and Long
        Object passedMilestonesObj = progressMetrics.get("passedMilestones");
        Object totalMilestonesObj = progressMetrics.get("totalMilestones");

        Long passedMilestones = null;
        Long totalMilestones = null;

        if (passedMilestonesObj instanceof Integer) {
            passedMilestones = ((Integer) passedMilestonesObj).longValue();
        } else if (passedMilestonesObj instanceof Long) {
            passedMilestones = (Long) passedMilestonesObj;
        }

        if (totalMilestonesObj instanceof Integer) {
            totalMilestones = ((Integer) totalMilestonesObj).longValue();
        } else if (totalMilestonesObj instanceof Long) {
            totalMilestones = (Long) totalMilestonesObj;
        }

        if (passedMilestones != null && totalMilestones != null && totalMilestones > 0) {
            double milestoneProgress = 100.0 * passedMilestones / totalMilestones;
            Double timeProgress = (Double) progressMetrics.get("timeProgressPercentage");
            if (timeProgress != null && milestoneProgress < timeProgress - 10) {
                recommendations.add("Milestone progress is lagging behind schedule. Review milestone dependencies.");
            }
        }
        
        // Default recommendation if no issues
        if (recommendations.isEmpty()) {
            recommendations.add("Project is on track. Continue current efforts and monitor for any emerging risks.");
        }
        
        return recommendations;
    }
}