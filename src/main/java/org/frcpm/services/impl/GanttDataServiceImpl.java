// src/main/java/org/frcpm/services/impl/GanttDataServiceImpl.java
package org.frcpm.services.impl;

import org.frcpm.models.GanttChartData;
import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.repositories.spring.MilestoneRepository;
import org.frcpm.repositories.spring.ProjectRepository;
import org.frcpm.repositories.spring.TaskRepository;
import org.frcpm.services.GanttChartTransformationService;
import org.frcpm.services.GanttDataService;
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
 * Spring Boot implementation of the GanttDataService interface.
 * Provides methods for formatting and analyzing task data for Gantt chart visualization.
 * Converted from ServiceLocator pattern to Spring dependency injection.
 */
@Service("ganttDataServiceImpl")
@Transactional
public class GanttDataServiceImpl implements GanttDataService {
    
    private static final Logger LOGGER = Logger.getLogger(GanttDataServiceImpl.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;
    private final GanttChartTransformationService transformationService;
    
    /**
     * Constructor with dependency injection for Spring Boot.
     * 
     * @param projectRepository the project repository
     * @param taskRepository the task repository
     * @param milestoneRepository the milestone repository
     * @param transformationService the transformation service
     */
    public GanttDataServiceImpl(ProjectRepository projectRepository, 
                               TaskRepository taskRepository, 
                               MilestoneRepository milestoneRepository,
                               GanttChartTransformationService transformationService) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.milestoneRepository = milestoneRepository;
        this.transformationService = transformationService;
    }
    

    
    @Override
    public Map<String, Object> formatTasksForGantt(Long projectId, LocalDate startDate, LocalDate endDate) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
        try {
            // Load project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return Collections.emptyMap();
            }
            
            Project project = projectOpt.get();
            
            // Use project dates if not specified
            LocalDate chartStartDate = startDate != null ? startDate : project.getStartDate();
            LocalDate chartEndDate = endDate != null ? endDate : project.getHardDeadline();
            
            // Load tasks for the project
            List<Task> tasks = taskRepository.findByProject(project);
            
            // Load milestones for the project
            List<Milestone> milestones = milestoneRepository.findByProject(project);
            
            // Transform tasks to GanttChartData
            List<GanttChartData> taskChartData = transformationService.transformTasksToChartData(tasks);
            
            // Transform milestones to GanttChartData
            List<GanttChartData> milestoneChartData = transformationService.transformMilestonesToChartData(milestones);
            
            // Format dependencies
            List<Map<String, Object>> dependencies = transformationService.createDependencyData(tasks);
            
            // Create result map
            Map<String, Object> result = new HashMap<>();
            result.put("tasks", taskChartData);
            result.put("milestones", milestoneChartData);
            result.put("dependencies", dependencies);
            result.put("startDate", chartStartDate.format(DATE_FORMATTER));
            result.put("endDate", chartEndDate.format(DATE_FORMATTER));
            result.put("projectName", project.getName());
            
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error formatting tasks for Gantt chart", e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public Map<String, Object> applyFiltersToGanttData(Map<String, Object> ganttData, Map<String, Object> filterCriteria) {
        if (ganttData == null || ganttData.isEmpty() || filterCriteria == null || filterCriteria.isEmpty()) {
            return ganttData;
        }
        
        try {
            String filterType = (String) filterCriteria.get("filterType");
            String subsystem = (String) filterCriteria.get("subsystem");
            String subteam = (String) filterCriteria.get("subteam");
            LocalDate filterStartDate = null;
            LocalDate filterEndDate = null;
            
            // Parse date strings if provided
            if (filterCriteria.containsKey("startDate") && filterCriteria.get("startDate") != null) {
                String startDateStr = (String) filterCriteria.get("startDate");
                filterStartDate = LocalDate.parse(startDateStr);
            }
            
            if (filterCriteria.containsKey("endDate") && filterCriteria.get("endDate") != null) {
                String endDateStr = (String) filterCriteria.get("endDate");
                filterEndDate = LocalDate.parse(endDateStr);
            }
            
            // Create a copy of the input data
            Map<String, Object> result = new HashMap<>(ganttData);
            
            // Get tasks and milestones
            @SuppressWarnings("unchecked")
            List<GanttChartData> tasks = (List<GanttChartData>) ganttData.get("tasks");
            
            @SuppressWarnings("unchecked")
            List<GanttChartData> milestones = (List<GanttChartData>) ganttData.get("milestones");
            
            if (tasks == null) {
                tasks = new ArrayList<>();
            }
            
            if (milestones == null) {
                milestones = new ArrayList<>();
            }
            
            // Apply filters
            List<GanttChartData> filteredTasks = transformationService.filterChartData(
                tasks, filterType, subteam, subsystem, filterStartDate, filterEndDate
            );
            
            List<GanttChartData> filteredMilestones = transformationService.filterChartData(
                milestones, null, null, null, filterStartDate, filterEndDate
            );
            
            // Update result
            result.put("tasks", filteredTasks);
            result.put("milestones", filteredMilestones);
            
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error applying filters to Gantt data", e);
            return ganttData;
        }
    }
    
    @Override
    public List<Long> calculateCriticalPath(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
        try {
            // Load project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return Collections.emptyList();
            }
            
            Project project = projectOpt.get();
            
            // Load tasks for the project
            List<Task> tasks = taskRepository.findByProject(project);
            
            // Simplified critical path calculation - just return the IDs of tasks with critical priority
            return tasks.stream()
                .filter(task -> Task.Priority.CRITICAL.equals(task.getPriority()))
                .map(Task::getId)
                .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating critical path", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Map<String, Object> getGanttDataForDate(Long projectId, LocalDate date) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
        // Use current date if not specified
        LocalDate effectiveDate = (date != null) ? date : LocalDate.now();
        
        try {
            // Get full Gantt data
            Map<String, Object> fullData = formatTasksForGantt(projectId, null, null);
            
            // Create filter criteria for just the specified date
            Map<String, Object> filterCriteria = new HashMap<>();
            filterCriteria.put("startDate", effectiveDate.format(DATE_FORMATTER));
            filterCriteria.put("endDate", effectiveDate.plusDays(1).format(DATE_FORMATTER));
            
            // Apply the filter
            return applyFiltersToGanttData(fullData, filterCriteria);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting Gantt data for date", e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public Map<Long, List<Long>> getTaskDependencies(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
        try {
            // Load project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return Collections.emptyMap();
            }
            
            Project project = projectOpt.get();
            
            // Load tasks for the project
            List<Task> tasks = taskRepository.findByProject(project);
            
            // Create dependency map
            Map<Long, List<Long>> dependencies = new HashMap<>();
            
            for (Task task : tasks) {
                List<Long> dependencyIds = task.getPreDependencies().stream()
                    .map(Task::getId)
                    .collect(Collectors.toList());
                
                dependencies.put(task.getId(), dependencyIds);
            }
            
            return dependencies;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting task dependencies", e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public List<Long> identifyBottlenecks(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        
        try {
            // Load project
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                LOGGER.warning("Project not found with ID: " + projectId);
                return Collections.emptyList();
            }
            
            Project project = projectOpt.get();
            
            // Load tasks for the project
            List<Task> tasks = taskRepository.findByProject(project);
            
            // Simplified bottleneck identification - tasks with the most dependencies
            Map<Task, Integer> dependencyCounts = new HashMap<>();
            
            for (Task task : tasks) {
                int incomingDependencies = 0;
                int outgoingDependencies = task.getPreDependencies().size();
                
                for (Task otherTask : tasks) {
                    if (otherTask.getPreDependencies().contains(task)) {
                        incomingDependencies++;
                    }
                }
                
                dependencyCounts.put(task, incomingDependencies + outgoingDependencies);
            }
            
            // Sort tasks by dependency count and take the top 25%
            int threshold = tasks.size() / 4;
            if (threshold == 0) {
                threshold = 1;
            }
            
            return dependencyCounts.entrySet().stream()
                .sorted(Map.Entry.<Task, Integer>comparingByValue().reversed())
                .limit(threshold)
                .map(entry -> entry.getKey().getId())
                .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error identifying bottlenecks", e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets the transformation service used by this data service.
     * 
     * @return the transformation service
     */
    @Override
    public GanttChartTransformationService getTransformationService() {
        return this.transformationService;
    }
    
    // Spring @Async methods for background processing
    
    @Async
    public CompletableFuture<Map<String, Object>> formatTasksForGanttAsync(Long projectId, LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.completedFuture(formatTasksForGantt(projectId, startDate, endDate));
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> applyFiltersToGanttDataAsync(Map<String, Object> ganttData, Map<String, Object> filterCriteria) {
        return CompletableFuture.completedFuture(applyFiltersToGanttData(ganttData, filterCriteria));
    }
    
    @Async
    public CompletableFuture<List<Long>> calculateCriticalPathAsync(Long projectId) {
        return CompletableFuture.completedFuture(calculateCriticalPath(projectId));
    }
    
    @Async
    public CompletableFuture<Map<String, Object>> getGanttDataForDateAsync(Long projectId, LocalDate date) {
        return CompletableFuture.completedFuture(getGanttDataForDate(projectId, date));
    }
    
    @Async
    public CompletableFuture<Map<Long, List<Long>>> getTaskDependenciesAsync(Long projectId) {
        return CompletableFuture.completedFuture(getTaskDependencies(projectId));
    }
    
    @Async
    public CompletableFuture<List<Long>> identifyBottlenecksAsync(Long projectId) {
        return CompletableFuture.completedFuture(identifyBottlenecks(projectId));
    }
}