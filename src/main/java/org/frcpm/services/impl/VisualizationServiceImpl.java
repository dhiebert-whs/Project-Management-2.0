// src/main/java/org/frcpm/services/impl/VisualizationServiceImpl.java
package org.frcpm.services.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.frcpm.charts.GanttChartFactory;
import org.frcpm.charts.TaskChartItem;
import org.frcpm.charts.ChartStyler;
import org.frcpm.models.GanttChartData;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.SubsystemRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.GanttChartTransformationService;
import org.frcpm.services.GanttDataService;
import org.frcpm.services.VisualizationService;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the VisualizationService interface.
 * This service provides methods to generate visualization data for
 * various charts and dashboard components.
 */
public class VisualizationServiceImpl implements VisualizationService {
    
    private static final Logger LOGGER = Logger.getLogger(VisualizationServiceImpl.class.getName());
    
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;
    private final SubsystemRepository subsystemRepository;
    private final GanttDataService ganttDataService;
    private final GanttChartTransformationService transformationService;
    
    /**
     * Creates a new VisualizationServiceImpl with default repositories and services.
     */
    public VisualizationServiceImpl() {
        this.projectRepository = RepositoryFactory.getProjectRepository();
        this.taskRepository = RepositoryFactory.getTaskRepository();
        this.milestoneRepository = RepositoryFactory.getMilestoneRepository();
        this.subsystemRepository = RepositoryFactory.getSubsystemRepository();
        this.ganttDataService = new GanttDataServiceImpl();
        this.transformationService = new GanttChartTransformationService();
    }
    
    /**
     * Creates a new VisualizationServiceImpl with specified repositories and services.
     * This constructor is mainly used for testing.
     */
    public VisualizationServiceImpl(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            MilestoneRepository milestoneRepository,
            SubsystemRepository subsystemRepository,
            GanttDataService ganttDataService) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.milestoneRepository = milestoneRepository;
        this.subsystemRepository = subsystemRepository;
        this.ganttDataService = ganttDataService;
        this.transformationService = new GanttChartTransformationService();
    }
    
    @Override
    public Pane createGanttChartPane(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate,
            String viewMode,
            boolean showMilestones,
            boolean showDependencies) {
        
        LOGGER.info("Creating Gantt chart pane for project " + projectId);
        
        try {
            // Get Gantt data from service
            Map<String, Object> ganttData = ganttDataService.formatTasksForGantt(projectId, startDate, endDate);
            
            if (ganttData == null || ganttData.isEmpty()) {
                LOGGER.warning("No Gantt data available for project " + projectId);
                return new Pane(); // Return empty pane if no data
            }
            
            // Extract tasks and milestones
            @SuppressWarnings("unchecked")
            List<GanttChartData> tasks = (List<GanttChartData>) ganttData.get("tasks");
            
            @SuppressWarnings("unchecked")
            List<GanttChartData> milestones = (List<GanttChartData>) ganttData.get("milestones");
            
            // Convert to TaskChartItem lists
            List<TaskChartItem> taskItems = convertGanttChartDataToTaskChartItems(tasks);
            List<TaskChartItem> milestoneItems = convertGanttChartDataToTaskChartItems(milestones);
            
            // Create chart with Chart-FX
            Pane ganttChart = GanttChartFactory.createGanttChart(
                taskItems,
                showMilestones ? milestoneItems : new ArrayList<>(),
                startDate,
                endDate,
                viewMode,
                showDependencies
            );
            
            // Apply styles
            ChartStyler.applyChartStyles(ganttChart);
            
            LOGGER.info("Gantt chart pane created successfully");
            return ganttChart;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating Gantt chart pane", e);
            return new Pane(); // Return empty pane on error
        }
    }
    
    @Override
    public Pane createDailyChartPane(Long projectId, LocalDate date) {
        LOGGER.info("Creating daily chart pane for project " + projectId + " on date " + date);
        
        try {
            // Get Gantt data for the specific date
            Map<String, Object> ganttData = ganttDataService.getGanttDataForDate(projectId, date);
            
            if (ganttData == null || ganttData.isEmpty()) {
                LOGGER.warning("No daily data available for project " + projectId + " on date " + date);
                return new Pane(); // Return empty pane if no data
            }
            
            // Extract tasks and milestones
            @SuppressWarnings("unchecked")
            List<GanttChartData> tasks = (List<GanttChartData>) ganttData.get("tasks");
            
            @SuppressWarnings("unchecked")
            List<GanttChartData> milestones = (List<GanttChartData>) ganttData.get("milestones");
            
            // Convert to TaskChartItem lists
            List<TaskChartItem> taskItems = convertGanttChartDataToTaskChartItems(tasks);
            List<TaskChartItem> milestoneItems = convertGanttChartDataToTaskChartItems(milestones);
            
            // Create daily chart
            Pane dailyChart = GanttChartFactory.createDailyChart(taskItems, milestoneItems, date);
            
            LOGGER.info("Daily chart pane created successfully");
            return dailyChart;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating daily chart pane", e);
            return new Pane(); // Return empty pane on error
        }
    }
    
    /**
     * Converts GanttChartData objects to TaskChartItem objects.
     *
     * @param chartDataList the list of GanttChartData objects
     * @return a list of TaskChartItem objects
     */
    public List<TaskChartItem> convertGanttChartDataToTaskChartItems(List<GanttChartData> chartDataList) {
        List<TaskChartItem> result = new ArrayList<>();
        
        if (chartDataList == null) {
            return result;
        }
        
        for (GanttChartData data : chartDataList) {
            TaskChartItem item = TaskChartItem.fromGanttChartData(data);
            if (item != null) {
                result.add(item);
            }
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getProjectCompletionData(Long projectId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Find project
            var projectOpt = projectRepository.findById(projectId);
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
            
            int timePercentage = totalDays > 0 ? (int) (daysPassed * 100 / totalDays) : 0;
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
            var projectOpt = projectRepository.findById(projectId);
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
            var projectOpt = projectRepository.findById(projectId);
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
            var projectOpt = projectRepository.findById(projectId);
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
            var projectOpt = projectRepository.findById(projectId);
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
                            int expectedProgress = (int) (daysPassed * 100 / totalDays);
                            return t.getProgress() < expectedProgress - 10; // More than 10% behind
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
        // This method can be implemented using Chart-FX's SVG export capabilities
        // For now, we'll just return a placeholder SVG
        
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"800\" height=\"600\">\n");
        
        // Add SVG content based on chartData and chartType
        svg.append("<rect width=\"800\" height=\"600\" fill=\"#f5f5f5\"/>\n");
        svg.append("<text x=\"400\" y=\"300\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"20\">Chart Export - " + chartType + "</text>\n");
        
        svg.append("</svg>");
        return svg.toString();
    }
    
    @Override
    public byte[] generatePdfReport(Long projectId, String reportType) {
        // PDF report generation would be implemented here
        // For now, return an empty byte array
        return new byte[0];
    }
}