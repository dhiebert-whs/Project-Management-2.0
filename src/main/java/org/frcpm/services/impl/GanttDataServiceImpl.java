// src/main/java/org/frcpm/services/impl/GanttDataServiceImpl.java
package org.frcpm.services.impl;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.MilestoneRepository;
import org.frcpm.repositories.specific.ProjectRepository;
import org.frcpm.repositories.specific.TaskRepository;
import org.frcpm.services.GanttDataService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the GanttDataService interface.
 * Provides methods for formatting and analyzing task data for Gantt chart visualization.
 */
public class GanttDataServiceImpl implements GanttDataService {
    
    private static final Logger LOGGER = Logger.getLogger(GanttDataServiceImpl.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final MilestoneRepository milestoneRepository;
    
    /**
     * Creates a new GanttDataServiceImpl with default repositories.
     */
    public GanttDataServiceImpl() {
        this.projectRepository = RepositoryFactory.getProjectRepository();
        this.taskRepository = RepositoryFactory.getTaskRepository();
        this.milestoneRepository = RepositoryFactory.getMilestoneRepository();
    }
    
    /**
     * Creates a new GanttDataServiceImpl with specified repositories.
     * This constructor is mainly used for testing.
     * 
     * @param projectRepository the project repository
     * @param taskRepository the task repository
     * @param milestoneRepository the milestone repository
     */
    public GanttDataServiceImpl(ProjectRepository projectRepository, TaskRepository taskRepository, 
                               MilestoneRepository milestoneRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.milestoneRepository = milestoneRepository;
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
            
            // Format tasks for Gantt chart
            List<Map<String, Object>> taskItems = formatTasks(tasks, chartStartDate, chartEndDate);
            
            // Format milestones for Gantt chart
            List<Map<String, Object>> milestoneItems = formatMilestones(milestones, chartStartDate, chartEndDate);
            
            // Format dependencies
            List<Map<String, Object>> dependencies = formatDependencies(tasks);
            
            // Create result map
            Map<String, Object> result = new HashMap<>();
            result.put("tasks", taskItems);
            result.put("milestones", milestoneItems);
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
            if (filterType == null) {
                return ganttData;
            }
            
            // Create a copy of the input data
            Map<String, Object> result = new HashMap<>(ganttData);
            
            // Get tasks
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) ganttData.get("tasks");
            if (tasks == null) {
                return result;
            }
            
            // Apply filter based on type
            List<Map<String, Object>> filteredTasks;
            
            switch (filterType) {
                case "My Tasks":
                    // For demo purposes, just show a subset (in a real app, would filter by assigned user)
                    filteredTasks = tasks.stream()
                        .filter(task -> task.containsKey("assignedTo") && !((List<?>) task.get("assignedTo")).isEmpty())
                        .collect(Collectors.toList());
                    break;
                    
                case "Critical Path":
                    // For demo purposes, just show tasks with highest priority
                    filteredTasks = tasks.stream()
                        .filter(task -> "CRITICAL".equals(task.get("priority")))
                        .collect(Collectors.toList());
                    break;
                    
                case "Behind Schedule":
                    // Show tasks that are behind schedule (progress < expected progress)
                    LocalDate today = LocalDate.now();
                    filteredTasks = tasks.stream()
                        .filter(task -> {
                            int progress = (int) task.get("progress");
                            LocalDate startDate = LocalDate.parse((String) task.get("startDate"), DATE_FORMATTER);
                            LocalDate endDate = LocalDate.parse((String) task.get("endDate"), DATE_FORMATTER);
                            
                            if (today.isBefore(startDate) || today.isAfter(endDate)) {
                                return false;
                            }
                            
                            long totalDays = startDate.until(endDate).getDays();
                            long daysPassed = startDate.until(today).getDays();
                            
                            if (totalDays == 0) {
                                return progress < 100;
                            }
                            
                            int expectedProgress = (int) (daysPassed * 100 / totalDays);
                            return progress < expectedProgress;
                        })
                        .collect(Collectors.toList());
                    break;
                    
                default:
                    // All Tasks - no filtering
                    filteredTasks = tasks;
                    break;
            }
            
            // Update tasks in result
            result.put("tasks", filteredTasks);
            
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
            
            // Note: A real critical path algorithm would be more complex,
            // involving topological sorting and calculating the longest path
            // through the dependency graph
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
        
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        try {
            // Get full Gantt data
            Map<String, Object> fullData = formatTasksForGantt(projectId, null, null);
            
            // Filter to just the specified date
            LocalDate startDate = date;
            LocalDate endDate = date.plusDays(1);
            
            // Update date range in result
            fullData.put("startDate", startDate.format(DATE_FORMATTER));
            fullData.put("endDate", endDate.format(DATE_FORMATTER));
            
            return fullData;
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
     * Formats tasks for Gantt chart visualization.
     * 
     * @param tasks the tasks to format
     * @param startDate the start date of the chart
     * @param endDate the end date of the chart
     * @return a list of formatted task objects
     */
    private List<Map<String, Object>> formatTasks(List<Task> tasks, LocalDate startDate, LocalDate endDate) {
        return tasks.stream()
            .filter(task -> isTaskInDateRange(task, startDate, endDate))
            .map(this::formatTaskForGantt)
            .collect(Collectors.toList());
    }
    
    /**
     * Formats milestones for Gantt chart visualization.
     * 
     * @param milestones the milestones to format
     * @param startDate the start date of the chart
     * @param endDate the end date of the chart
     * @return a list of formatted milestone objects
     */
    private List<Map<String, Object>> formatMilestones(List<Milestone> milestones, LocalDate startDate, LocalDate endDate) {
        return milestones.stream()
            .filter(milestone -> isMilestoneInDateRange(milestone, startDate, endDate))
            .map(this::formatMilestoneForGantt)
            .collect(Collectors.toList());
    }
    
    /**
     * Formats task dependencies for Gantt chart visualization.
     * 
     * @param tasks the tasks with dependencies
     * @return a list of formatted dependency objects
     */
    private List<Map<String, Object>> formatDependencies(List<Task> tasks) {
        List<Map<String, Object>> dependencies = new ArrayList<>();
        
        for (Task task : tasks) {
            for (Task dependency : task.getPreDependencies()) {
                Map<String, Object> dep = new HashMap<>();
                dep.put("id", task.getId() + "_" + dependency.getId());
                dep.put("source", dependency.getId().toString());
                dep.put("target", task.getId().toString());
                dep.put("type", "finish-to-start");
                dependencies.add(dep);
            }
        }
        
        return dependencies;
    }
    
    /**
     * Formats a task for Gantt chart visualization.
     * 
     * @param task the task to format
     * @return a map containing the formatted task data
     */
    private Map<String, Object> formatTaskForGantt(Task task) {
        Map<String, Object> formatted = new HashMap<>();
        
        formatted.put("id", task.getId().toString());
        formatted.put("name", task.getTitle());
        formatted.put("progress", task.getProgress());
        formatted.put("priority", task.getPriority().toString());
        formatted.put("completed", task.isCompleted());
        
        // Handle dates
        LocalDate taskStartDate = task.getStartDate();
        LocalDate taskEndDate = task.getEndDate();
        
        if (taskStartDate == null) {
            taskStartDate = LocalDate.now();
        }
        
        if (taskEndDate == null) {
            // Default to start date + estimated duration (or 1 day if no duration)
            long days = task.getEstimatedDuration().toDays();
            taskEndDate = taskStartDate.plusDays(days > 0 ? days : 1);
        }
        
        formatted.put("startDate", taskStartDate.format(DATE_FORMATTER));
        formatted.put("endDate", taskEndDate.format(DATE_FORMATTER));
        
        // Additional information
        formatted.put("description", task.getDescription());
        formatted.put("subsystem", task.getSubsystem().getName());
        
        // Handle assigned team members
        List<Map<String, Object>> assignedMembers = task.getAssignedTo().stream()
            .map(member -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", member.getId().toString());
                m.put("name", member.getFullName());
                return m;
            })
            .collect(Collectors.toList());
        
        formatted.put("assignedTo", assignedMembers);
        
        return formatted;
    }
    
    /**
     * Formats a milestone for Gantt chart visualization.
     * 
     * @param milestone the milestone to format
     * @return a map containing the formatted milestone data
     */
    private Map<String, Object> formatMilestoneForGantt(Milestone milestone) {
        Map<String, Object> formatted = new HashMap<>();
        
        formatted.put("id", "m_" + milestone.getId().toString());
        formatted.put("name", milestone.getName());
        formatted.put("date", milestone.getDate().format(DATE_FORMATTER));
        formatted.put("passed", milestone.isPassed());
        formatted.put("description", milestone.getDescription());
        
        return formatted;
    }
    
    /**
     * Checks if a task is within the specified date range.
     * 
     * @param task the task to check
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return true if the task is within the range, false otherwise
     */
    private boolean isTaskInDateRange(Task task, LocalDate startDate, LocalDate endDate) {
        LocalDate taskStartDate = task.getStartDate();
        LocalDate taskEndDate = task.getEndDate();
        
        if (taskStartDate == null && taskEndDate == null) {
            return true; // Include tasks without dates
        }
        
        if (taskStartDate == null) {
            taskStartDate = LocalDate.now();
        }
        
        if (taskEndDate == null) {
            // Default to start date + estimated duration (or 1 day if no duration)
            long days = task.getEstimatedDuration().toDays();
            taskEndDate = taskStartDate.plusDays(days > 0 ? days : 1);
        }
        
        // Check if task overlaps with date range
        return !(taskEndDate.isBefore(startDate) || taskStartDate.isAfter(endDate));
    }
    
    /**
     * Checks if a milestone is within the specified date range.
     * 
     * @param milestone the milestone to check
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return true if the milestone is within the range, false otherwise
     */
    private boolean isMilestoneInDateRange(Milestone milestone, LocalDate startDate, LocalDate endDate) {
        LocalDate milestoneDate = milestone.getDate();
        
        if (milestoneDate == null) {
            return false;
        }
        
        // Check if milestone is within date range
        return !(milestoneDate.isBefore(startDate) || milestoneDate.isAfter(endDate));
    }
}