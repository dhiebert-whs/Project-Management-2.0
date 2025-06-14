// src/main/java/org/frcpm/services/impl/VisualizationServiceImpl.java
package org.frcpm.services.impl;

import org.frcpm.models.GanttChartData;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.repositories.spring.MilestoneRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.repositories.spring.TaskRepository;
import org.frcpm.services.GanttDataService;
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
 * Spring Boot implementation of the VisualizationService interface.
 * This service provides methods to generate visualization data for
 * various charts and dashboard components.
 * Converted from ServiceLocator pattern to Spring dependency injection.
 */
@Service("visualizationServiceImpl")
@Transactional
public class VisualizationServiceImpl implements VisualizationService {
    
    private static final Logger LOGGER = Logger.getLogger(VisualizationServiceImpl.class.getName());
    
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;
    private final GanttDataService ganttDataService;
    
    /**
     * Constructor with dependency injection for Spring Boot.
     */
    public VisualizationServiceImpl(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            MilestoneRepository milestoneRepository,
            GanttDataService ganttDataService) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.milestoneRepository = milestoneRepository;
        this.ganttDataService = ganttDataService;
    }
    
    @Override
    public Map<String, Object> createGanttChartPane(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate,
            String viewMode,
            boolean showMilestones,
            boolean showDependencies) {
        
        LOGGER.info("Creating Gantt chart data for project " + projectId);
        
        try {
            // Get Gantt data from service
            Map<String, Object> ganttData = ganttDataService.formatTasksForGantt(projectId, startDate, endDate);
            
            if (ganttData == null || ganttData.isEmpty()) {
                LOGGER.warning("No Gantt data available for project " + projectId);
                return new HashMap<>(); // Return empty map if no data
            }
            
            // Add chart configuration for web display
            Map<String, Object> chartConfig = new HashMap<>(ganttData);
            chartConfig.put("viewMode", viewMode);
            chartConfig.put("showMilestones", showMilestones);
            chartConfig.put("showDependencies", showDependencies);
            chartConfig.put("chartType", "gantt");
            
            LOGGER.info("Gantt chart data prepared successfully");
            return chartConfig;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating Gantt chart data", e);
            return new HashMap<>(); // Return empty map on error
        }
    }

    @Override
    public Map<String, Object> createDailyChartPane(Long projectId, LocalDate date) {
        LOGGER.info("Creating daily chart data for project " + projectId + " on date " + date);
        
        try {
            // Use current date if not specified
            LocalDate targetDate = (date != null) ? date : LocalDate.now();
            
            // Get Gantt data for the specific date
            Map<String, Object> dailyData = ganttDataService.getGanttDataForDate(projectId, targetDate);
            
            if (dailyData == null || dailyData.isEmpty()) {
                LOGGER.warning("No daily data available for project " + projectId + " on date " + targetDate);
                Map<String, Object> emptyData = new HashMap<>();
                emptyData.put("date", targetDate.toString());
                emptyData.put("tasks", new ArrayList<>());
                emptyData.put("milestones", new ArrayList<>());
                emptyData.put("chartType", "daily");
                return emptyData;
            }
            
            // Add chart configuration for web display
            Map<String, Object> chartConfig = new HashMap<>(dailyData);
            chartConfig.put("date", targetDate.toString());
            chartConfig.put("chartType", "daily");
            
            LOGGER.info("Daily chart data prepared successfully");
            return chartConfig;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating daily chart data", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("date", (date != null ? date : LocalDate.now()).toString());
            errorData.put("tasks", new ArrayList<>());
            errorData.put("milestones", new ArrayList<>());
            errorData.put("chartType", "daily");
            return errorData;
        }
    }
    
    @Override
    public Map<String, Object> getProjectCompletionData(Long projectId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Find project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return result;
            }
            
            Project project = projectOpt.get();
            
            // Get tasks for project
            List<Task> tasks = taskRepository.findByProject(project);
            
            // Calculate overall completion
            int totalTasks = tasks.size();
            int completedTasks = (int) tasks.stream().filter(Task::isCompleted).count();
            int overallPercentage = totalTasks > 0 ? (completedTasks * 100 / totalTasks) : 0;
            
            result.put("overall", overallPercentage);
            
            // Calculate completion by subsystem
            Map<String, Integer> subsystemCompletion = new HashMap<>();
            
            // Group tasks by subsystem
            Map<Subsystem, List<Task>> tasksBySubsystem = tasks.stream()
                .filter(task -> task.getSubsystem() != null)
                .collect(Collectors.groupingBy(Task::getSubsystem));
            
            // Calculate completion percentage for each subsystem
            for (Map.Entry<Subsystem, List<Task>> entry : tasksBySubsystem.entrySet()) {
                Subsystem subsystem = entry.getKey();
                List<Task> subsystemTasks = entry.getValue();
                
                int subsystemTotal = subsystemTasks.size();
                int subsystemCompleted = (int) subsystemTasks.stream().filter(Task::isCompleted).count();
                int subsystemPercentage = subsystemTotal > 0 ? (subsystemCompleted * 100 / subsystemTotal) : 0;
                
                subsystemCompletion.put(subsystem.getName(), subsystemPercentage);
            }
            
            result.put("bySubsystem", subsystemCompletion);
            
            // Add project timeline information
            LocalDate startDate = project.getStartDate();
            LocalDate endDate = project.getHardDeadline();
            LocalDate today = LocalDate.now();
            
            long totalDays = startDate.until(endDate).getDays();
            long daysPassed = startDate.until(today).getDays();
            
            // Calculate time elapsed percentage (capped at 100%)
            int timePercentage = 0;
            if (totalDays > 0) {
                timePercentage = (int) Math.min(100, (daysPassed * 100 / totalDays));
            }
            result.put("timeElapsed", timePercentage);
            
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting project completion data", e);
            return result;
        }
    }
    
    @Override
    public Map<String, Integer> getTaskStatusSummary(Long projectId) {
        Map<String, Integer> result = new HashMap<>();
        
        try {
            // Find project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return result;
            }
            
            Project project = projectOpt.get();
            
            // Get tasks for project
            List<Task> tasks = taskRepository.findByProject(project);
            
            // Count tasks by status
            int notStarted = (int) tasks.stream().filter(t -> t.getProgress() == 0).count();
            int inProgress = (int) tasks.stream().filter(t -> t.getProgress() > 0 && t.getProgress() < 100).count();
            int completed = (int) tasks.stream().filter(t -> t.getProgress() == 100).count();
            
            result.put("notStarted", notStarted);
            result.put("inProgress", inProgress);
            result.put("completed", completed);
            
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting task status summary", e);
            return result;
        }
    }
    
    @Override
    public List<Map<String, Object>> getUpcomingDeadlines(Long projectId, int daysAhead) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // Find project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return result;
            }
            
            Project project = projectOpt.get();
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(daysAhead);
            
            // Get tasks with deadlines in the next n days
            List<Task> tasks = taskRepository.findByProject(project);
            List<Task> upcomingTasks = tasks.stream()
                .filter(t -> !t.isCompleted())
                .filter(t -> t.getEndDate() != null)
                .filter(t -> !t.getEndDate().isBefore(today) && !t.getEndDate().isAfter(endDate))
                .collect(Collectors.toList());
            
            // Get milestones in the next n days
            List<Milestone> milestones = milestoneRepository.findByProject(project);
            List<Milestone> upcomingMilestones = milestones.stream()
                .filter(m -> !m.isPassed())
                .filter(m -> m.getDate() != null)
                .filter(m -> !m.getDate().isBefore(today) && !m.getDate().isAfter(endDate))
                .collect(Collectors.toList());
            
            // Add tasks to result
            for (Task task : upcomingTasks) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "task");
                item.put("id", task.getId());
                item.put("title", task.getTitle());
                item.put("date", task.getEndDate());
                item.put("progress", task.getProgress());
                if (task.getSubsystem() != null) {
                    item.put("subsystem", task.getSubsystem().getName());
                }
                
                result.add(item);
            }
            
            // Add milestones to result
            for (Milestone milestone : upcomingMilestones) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "milestone");
                item.put("id", milestone.getId());
                item.put("title", milestone.getName());
                item.put("date", milestone.getDate());
                
                result.add(item);
            }
            
            // Sort by date
            result.sort((a, b) -> {
                LocalDate dateA = (LocalDate) a.get("date");
                LocalDate dateB = (LocalDate) b.get("date");
                return dateA.compareTo(dateB);
            });
            
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting upcoming deadlines", e);
            return result;
        }
    }
    
    @Override
    public Map<String, Double> getSubsystemProgress(Long projectId) {
        Map<String, Double> result = new HashMap<>();
        
        try {
            // Find project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return result;
            }
            
            Project project = projectOpt.get();
            
            // Get tasks for project
            List<Task> allTasks = taskRepository.findByProject(project);
            
            // Extract all unique subsystems from the tasks
            Set<Subsystem> subsystems = allTasks.stream()
                .map(Task::getSubsystem)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            // Calculate progress for each subsystem
            for (Subsystem subsystem : subsystems) {
                List<Task> subsystemTasks = allTasks.stream()
                    .filter(t -> subsystem.equals(t.getSubsystem()))
                    .collect(Collectors.toList());
                
                if (subsystemTasks.isEmpty()) {
                    result.put(subsystem.getName(), 0.0);
                    continue;
                }
                
                // Calculate average progress
                double totalProgress = subsystemTasks.stream()
                    .mapToInt(Task::getProgress)
                    .sum();
                
                double averageProgress = totalProgress / subsystemTasks.size();
                result.put(subsystem.getName(), averageProgress);
            }
            
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting subsystem progress", e);
            return result;
        }
    }
    
    @Override
    public List<Map<String, Object>> getAtRiskTasks(Long projectId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // Find project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return result;
            }
            
            Project project = projectOpt.get();
            LocalDate today = LocalDate.now();
            
            // Get tasks for project
            List<Task> tasks = taskRepository.findByProject(project);
            
            // Find at-risk tasks (not completed, past due date or progress behind schedule)
            List<Task> atRiskTasks = tasks.stream()
                .filter(t -> !t.isCompleted())
                .filter(t -> {
                    // Past due date
                    if (t.getEndDate() != null && t.getEndDate().isBefore(today)) {
                        return true;
                    }
                    
                    // Progress behind schedule
                    if (t.getStartDate() != null && t.getEndDate() != null) {
                        long totalDays = t.getStartDate().until(t.getEndDate()).getDays();
                        if (totalDays > 0) {
                            long daysPassed = t.getStartDate().until(today).getDays();
                            // Only count if task has actually started
                            if (daysPassed >= 0) {
                                int expectedProgress = (int) (daysPassed * 100 / totalDays);
                                return t.getProgress() < expectedProgress - 10; // More than 10% behind
                            }
                        }
                    }
                    
                    return false;
                })
                .collect(Collectors.toList());
            
            // Convert to result format
            for (Task task : atRiskTasks) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", task.getId());
                item.put("title", task.getTitle());
                item.put("progress", task.getProgress());
                item.put("dueDate", task.getEndDate());
                
                if (task.getEndDate() != null && task.getEndDate().isBefore(today)) {
                    item.put("reason", "Past due date");
                    item.put("daysOverdue", today.until(task.getEndDate()).getDays() * -1);
                } else {
                    item.put("reason", "Behind schedule");
                    if (task.getStartDate() != null && task.getEndDate() != null) {
                        long totalDays = task.getStartDate().until(task.getEndDate()).getDays();
                        if (totalDays > 0) {
                            long daysPassed = task.getStartDate().until(today).getDays();
                            int expectedProgress = (int) (daysPassed * 100 / totalDays);
                            item.put("expectedProgress", expectedProgress);
                            item.put("progressGap", expectedProgress - task.getProgress());
                        }
                    }
                }
                
                if (task.getSubsystem() != null) {
                    item.put("subsystem", task.getSubsystem().getName());
                }
                
                result.add(item);
            }
            
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting at-risk tasks", e);
            return result;
        }
    }
    
    @Override
    public String generateSvgExport(Map<String, Object> chartData, String chartType) {
        // This method generates SVG export for web-based charts
        
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"800\" height=\"600\">\n");
        
        // Add SVG content based on chartData and chartType
        svg.append("<rect width=\"800\" height=\"600\" fill=\"#f5f5f5\"/>\n");
        svg.append("<text x=\"400\" y=\"300\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"20\">Chart Export - " + chartType + "</text>\n");
        
        // If it's a Gantt chart, try to draw some representation
        if ("gantt".equalsIgnoreCase(chartType) && chartData != null) {
            @SuppressWarnings("unchecked")
            List<GanttChartData> tasks = (List<GanttChartData>) chartData.get("tasks");
            
            if (tasks != null && !tasks.isEmpty()) {
                // Find date range
                LocalDate minDate = tasks.stream()
                        .map(GanttChartData::getStartDate)
                        .filter(Objects::nonNull)
                        .min(LocalDate::compareTo)
                        .orElse(LocalDate.now().minusMonths(1));
                
                LocalDate maxDate = tasks.stream()
                        .map(GanttChartData::getEndDate)
                        .filter(Objects::nonNull)
                        .max(LocalDate::compareTo)
                        .orElse(LocalDate.now().plusMonths(1));
                
                long totalDays = minDate.until(maxDate).getDays() + 1;
                double dayWidth = 700.0 / totalDays;
                
                // Add timeline
                svg.append("<line x1=\"50\" y1=\"50\" x2=\"750\" y2=\"50\" stroke=\"#333333\" stroke-width=\"2\"/>\n");
                
                // Add a few date markers
                for (int i = 0; i <= totalDays; i += totalDays / 5) {
                    LocalDate date = minDate.plusDays(i);
                    double x = 50 + (i * dayWidth);
                    svg.append("<line x1=\"" + x + "\" y1=\"45\" x2=\"" + x + "\" y2=\"55\" stroke=\"#333333\" stroke-width=\"1\"/>\n");
                    svg.append("<text x=\"" + x + "\" y=\"70\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"10\">" + 
                              date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "</text>\n");
                }
                
                // Add some task bars (simplified)
                int taskIndex = 0;
                for (GanttChartData task : tasks) {
                    if (task.getStartDate() != null && task.getEndDate() != null) {
                        long startDays = minDate.until(task.getStartDate()).getDays();
                        long endDays = minDate.until(task.getEndDate()).getDays();
                        
                        double x = 50 + (startDays * dayWidth);
                        double width = (endDays - startDays + 1) * dayWidth;
                        double y = 100 + (taskIndex * 30);
                        
                        // Add task bar
                        svg.append("<rect x=\"" + x + "\" y=\"" + y + "\" width=\"" + width + "\" height=\"20\" " +
                                  "fill=\"" + (task.getColor() != null ? task.getColor() : "#4285f4") + "\" " +
                                  "stroke=\"#333333\" stroke-width=\"1\"/>\n");
                        
                        // Add task title
                        svg.append("<text x=\"" + (x - 5) + "\" y=\"" + (y + 15) + "\" text-anchor=\"end\" " +
                                  "font-family=\"Arial\" font-size=\"12\">" + task.getTitle() + "</text>\n");
                        
                        taskIndex++;
                    }
                }
            }
        }
        
        svg.append("</svg>");
        return svg.toString();
    }
    
    @Override
    public byte[] generatePdfReport(Long projectId, String reportType) {
        // For now, we'll just return a stub implementation
        LOGGER.info("PDF report generation requested for project " + projectId + " with type " + reportType);
        
        try {
            // Find project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                LOGGER.warning("Project not found for ID: " + projectId);
                return new byte[0];
            }
            
            // A complete implementation would generate a PDF here
            // For now, return an empty byte array
            LOGGER.info("PDF report generation not yet implemented");
            return new byte[0];
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating PDF report", e);
            return new byte[0];
        }
    }
    
    // Spring @Async methods for background processing
    
    @Async
    public CompletableFuture<Map<String, Object>> createGanttChartPaneAsync(Long projectId, LocalDate startDate, LocalDate endDate, String viewMode, boolean showMilestones, boolean showDependencies) {
        return CompletableFuture.completedFuture(createGanttChartPane(projectId, startDate, endDate, viewMode, showMilestones, showDependencies));
    }

    @Async
    public CompletableFuture<Map<String, Object>> createDailyChartPaneAsync(Long projectId, LocalDate date) {
        return CompletableFuture.completedFuture(createDailyChartPane(projectId, date));
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> getProjectCompletionDataAsync(Long projectId) {
        return CompletableFuture.completedFuture(getProjectCompletionData(projectId));
    }
    
    @Async
    public CompletableFuture<Map<String, Integer>> getTaskStatusSummaryAsync(Long projectId) {
        return CompletableFuture.completedFuture(getTaskStatusSummary(projectId));
    }
    
    @Async
    public CompletableFuture<List<Map<String, Object>>> getUpcomingDeadlinesAsync(Long projectId, int daysAhead) {
        return CompletableFuture.completedFuture(getUpcomingDeadlines(projectId, daysAhead));
    }
    
    @Async
    public CompletableFuture<Map<String, Double>> getSubsystemProgressAsync(Long projectId) {
        return CompletableFuture.completedFuture(getSubsystemProgress(projectId));
    }
    
    @Async
    public CompletableFuture<List<Map<String, Object>>> getAtRiskTasksAsync(Long projectId) {
        return CompletableFuture.completedFuture(getAtRiskTasks(projectId));
    }
    
    @Async
    public CompletableFuture<String> generateSvgExportAsync(Map<String, Object> chartData, String chartType) {
        return CompletableFuture.completedFuture(generateSvgExport(chartData, chartType));
    }
    
    @Async
    public CompletableFuture<byte[]> generatePdfReportAsync(Long projectId, String reportType) {
        return CompletableFuture.completedFuture(generatePdfReport(projectId, reportType));
    }
}