// src/main/java/org/frcpm/web/controllers/TaskDependencyController.java
// Phase 2E-D: Task Dependency Management Controller
// ✅ NEW: REST API for advanced task dependency operations

package org.frcpm.web.controllers;

import org.frcpm.models.Task;
import org.frcpm.models.TaskDependency;
import org.frcpm.models.Project;
import org.frcpm.models.DependencyType;
import org.frcpm.services.TaskDependencyService;
import org.frcpm.services.TaskService;
import org.frcpm.services.ProjectService;
import org.frcpm.web.dto.TaskDependencyDto;
import org.frcpm.web.dto.CriticalPathDto;
import org.frcpm.web.dto.TaskUpdateMessage;
import org.frcpm.events.WebSocketEventPublisher;
import org.frcpm.security.UserPrincipal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TaskDependencyController - Phase 2E-D Implementation
 * 
 * REST controller for advanced task dependency management with real-time
 * WebSocket integration, critical path analysis, and comprehensive
 * dependency operations.
 * 
 * Features:
 * - Full CRUD operations for task dependencies
 * - Real-time WebSocket updates for dependency changes
 * - Critical path analysis and visualization
 * - Cycle detection and prevention
 * - Bulk dependency operations
 * - Build season optimization features
 * - Role-based access control
 * 
 * @author FRC Project Management Team
 * @version 2.0.0-2E-D
 */
@Controller
@RequestMapping("/api/dependencies")
@PreAuthorize("isAuthenticated()")
public class TaskDependencyController {
    
    // =========================================================================
    // CONSTANTS AND DEPENDENCIES
    // =========================================================================
    
    private static final Logger LOGGER = Logger.getLogger(TaskDependencyController.class.getName());
    
    @Autowired
    private TaskDependencyService dependencyService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private WebSocketEventPublisher webSocketEventPublisher;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // =========================================================================
    // BASIC DEPENDENCY OPERATIONS
    // =========================================================================
    
    /**
     * Get all dependencies for a project.
     * 
     * @param projectId Project ID to get dependencies for
     * @param activeOnly Whether to include only active dependencies
     * @param user Current authenticated user
     * @return JSON response with dependencies list
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDependencies(
            @RequestParam Long projectId,
            @RequestParam(value = "activeOnly", defaultValue = "true") boolean activeOnly,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            LOGGER.info("Getting dependencies for project: " + projectId);
            
            Project project = projectService.findById(projectId);
            if (project == null) {
                response.put("success", false);
                response.put("message", "Project not found");
                return ResponseEntity.notFound().build();
            }
            
            List<TaskDependency> dependencies = dependencyService.getProjectDependencies(project, activeOnly);
            
            // Convert to DTOs
            List<TaskDependencyDto> dependencyDtos = dependencies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("dependencies", dependencyDtos);
            response.put("totalCount", dependencies.size());
            response.put("projectId", projectId);
            response.put("activeOnly", activeOnly);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting dependencies", e);
            response.put("success", false);
            response.put("message", "Error retrieving dependencies: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get dependencies for a specific task.
     * 
     * @param taskId Task ID to get dependencies for
     * @param user Current authenticated user
     * @return JSON response with task dependencies
     */
    @GetMapping("/task/{taskId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTaskDependencies(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Task task = taskService.findById(taskId);
            if (task == null) {
                response.put("success", false);
                response.put("message", "Task not found");
                return ResponseEntity.notFound().build();
            }
            
            List<TaskDependency> dependencies = dependencyService.getTaskDependencies(task);
            List<TaskDependency> dependents = dependencyService.getTaskDependents(task);
            
            response.put("success", true);
            response.put("taskId", taskId);
            response.put("dependencies", dependencies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
            response.put("dependents", dependents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
            response.put("canStart", dependencyService.canTaskStart(task));
            response.put("blockingDependencies", dependencyService.getBlockingDependencies(task).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting task dependencies", e);
            response.put("success", false);
            response.put("message", "Error retrieving task dependencies: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Create a new task dependency.
     * 
     * @param dependencyDto Dependency data from request
     * @param user Current authenticated user
     * @return JSON response with created dependency
     */
    @PostMapping
    @ResponseBody
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createDependency(
            @Valid @RequestBody TaskDependencyDto dependencyDto,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            LOGGER.info("Creating dependency: " + dependencyDto);
            
            // Validate tasks exist
            Task dependentTask = taskService.findById(dependencyDto.getSuccessorTaskId());
            Task prerequisiteTask = taskService.findById(dependencyDto.getPredecessorTaskId());
            
            if (dependentTask == null || prerequisiteTask == null) {
                response.put("success", false);
                response.put("message", "One or more tasks not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check for cycle
            if (dependencyService.wouldCreateCycle(dependentTask, prerequisiteTask)) {
                response.put("success", false);
                response.put("message", "Cannot create dependency: would create a cycle");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create dependency
            TaskDependency dependency = dependencyService.createDependency(
                dependentTask, 
                prerequisiteTask, 
                dependencyDto.getType(),
                dependencyDto.getLagDays() != null ? dependencyDto.getLagDays() * 24 : null,
                dependencyDto.getNotes()
            );
            
            // Publish WebSocket event
            publishDependencyEvent(dependency, "CREATED", user);
            
            response.put("success", true);
            response.put("message", "Dependency created successfully");
            response.put("dependency", convertToDto(dependency));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid dependency creation", e);
            response.put("success", false);
            response.put("message", "Invalid dependency: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating dependency", e);
            response.put("success", false);
            response.put("message", "Error creating dependency: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Update an existing dependency.
     * 
     * @param dependencyId Dependency ID to update
     * @param dependencyDto Updated dependency data
     * @param user Current authenticated user
     * @return JSON response with updated dependency
     */
    @PutMapping("/{dependencyId}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateDependency(
            @PathVariable Long dependencyId,
            @Valid @RequestBody TaskDependencyDto dependencyDto,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            TaskDependency dependency = dependencyService.updateDependency(
                dependencyId,
                dependencyDto.getType(),
                dependencyDto.getLagDays() != null ? dependencyDto.getLagDays() * 24 : null,
                dependencyDto.getNotes()
            );
            
            // Publish WebSocket event
            publishDependencyEvent(dependency, "UPDATED", user);
            
            response.put("success", true);
            response.put("message", "Dependency updated successfully");
            response.put("dependency", convertToDto(dependency));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid dependency update", e);
            response.put("success", false);
            response.put("message", "Invalid dependency: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating dependency", e);
            response.put("success", false);
            response.put("message", "Error updating dependency: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Delete a dependency.
     * 
     * @param dependencyId Dependency ID to delete
     * @param user Current authenticated user
     * @return JSON response with deletion result
     */
    @DeleteMapping("/{dependencyId}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteDependency(
            @PathVariable Long dependencyId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<TaskDependency> dependencyOpt = dependencyService.findDependencyById(dependencyId);
            if (!dependencyOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Dependency not found");
                return ResponseEntity.notFound().build();
            }
            
            TaskDependency dependency = dependencyOpt.get();
            Long projectId = dependency.getDependentTask().getProject().getId();
            
            boolean deleted = dependencyService.removeDependency(dependencyId);
            
            if (deleted) {
                // Publish WebSocket event
                publishDependencyDeletionEvent(dependencyId, projectId, user);
                
                response.put("success", true);
                response.put("message", "Dependency deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete dependency");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting dependency", e);
            response.put("success", false);
            response.put("message", "Error deleting dependency: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // =========================================================================
    // CRITICAL PATH ANALYSIS
    // =========================================================================
    
    /**
     * Get critical path analysis for a project.
     * 
     * @param projectId Project ID to analyze
     * @param user Current authenticated user
     * @return JSON response with critical path data
     */
    @GetMapping("/critical-path")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCriticalPath(
            @RequestParam Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            LOGGER.info("Calculating critical path for project: " + projectId);
            
            Project project = projectService.findById(projectId);
            if (project == null) {
                response.put("success", false);
                response.put("message", "Project not found");
                return ResponseEntity.notFound().build();
            }
            
            TaskDependencyService.CriticalPathResult result = dependencyService.calculateCriticalPath(project);
            
            CriticalPathDto criticalPathDto = new CriticalPathDto();
            criticalPathDto.setProjectId(projectId);
            criticalPathDto.setTotalDuration(result.getTotalDuration());
            criticalPathDto.setCriticalTasks(result.getCriticalPath().stream()
                .map(task -> convertTaskToDto(task))
                .collect(Collectors.toList()));
            criticalPathDto.setCriticalDependencies(result.getCriticalDependencies().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
            
            // Convert task floats to DTO format
            Map<String, Double> taskFloats = new HashMap<>();
            for (Map.Entry<Task, Double> entry : result.getTaskFloats().entrySet()) {
                taskFloats.put(entry.getKey().getId().toString(), entry.getValue());
            }
            criticalPathDto.setTaskFloats(taskFloats);
            
            response.put("success", true);
            response.put("criticalPath", criticalPathDto);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating critical path", e);
            response.put("success", false);
            response.put("message", "Error calculating critical path: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get task float (slack) for a specific task.
     * 
     * @param taskId Task ID to calculate float for
     * @param user Current authenticated user
     * @return JSON response with task float
     */
    @GetMapping("/task/{taskId}/float")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTaskFloat(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Task task = taskService.findById(taskId);
            if (task == null) {
                response.put("success", false);
                response.put("message", "Task not found");
                return ResponseEntity.notFound().build();
            }
            
            Double taskFloat = dependencyService.calculateTaskFloat(task);
            
            response.put("success", true);
            response.put("taskId", taskId);
            response.put("taskFloat", taskFloat);
            response.put("isCritical", taskFloat != null && taskFloat == 0.0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating task float", e);
            response.put("success", false);
            response.put("message", "Error calculating task float: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // =========================================================================
    // PROJECT ANALYSIS AND OPTIMIZATION
    // =========================================================================
    
    /**
     * Get blocked tasks for a project.
     * 
     * @param projectId Project ID to analyze
     * @param user Current authenticated user
     * @return JSON response with blocked tasks
     */
    @GetMapping("/blocked-tasks")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBlockedTasks(
            @RequestParam Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.findById(projectId);
            if (project == null) {
                response.put("success", false);
                response.put("message", "Project not found");
                return ResponseEntity.notFound().build();
            }
            
            Map<Task, List<TaskDependency>> blockedTasks = dependencyService.getBlockedTasks(project);
            
            Map<String, Object> blockedTasksData = new HashMap<>();
            for (Map.Entry<Task, List<TaskDependency>> entry : blockedTasks.entrySet()) {
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("task", convertTaskToDto(entry.getKey()));
                taskData.put("blockingDependencies", entry.getValue().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList()));
                blockedTasksData.put(entry.getKey().getId().toString(), taskData);
            }
            
            response.put("success", true);
            response.put("projectId", projectId);
            response.put("blockedTasks", blockedTasksData);
            response.put("totalBlocked", blockedTasks.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting blocked tasks", e);
            response.put("success", false);
            response.put("message", "Error getting blocked tasks: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get tasks ready to start for a project.
     * 
     * @param projectId Project ID to analyze
     * @param user Current authenticated user
     * @return JSON response with ready tasks
     */
    @GetMapping("/ready-tasks")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReadyTasks(
            @RequestParam Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.findById(projectId);
            if (project == null) {
                response.put("success", false);
                response.put("message", "Project not found");
                return ResponseEntity.notFound().build();
            }
            
            List<Task> readyTasks = dependencyService.getTasksReadyToStart(project);
            
            response.put("success", true);
            response.put("projectId", projectId);
            response.put("readyTasks", readyTasks.stream()
                .map(this::convertTaskToDto)
                .collect(Collectors.toList()));
            response.put("totalReady", readyTasks.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting ready tasks", e);
            response.put("success", false);
            response.put("message", "Error getting ready tasks: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get schedule optimization recommendations.
     * 
     * @param projectId Project ID to optimize
     * @param user Current authenticated user
     * @return JSON response with optimization recommendations
     */
    @GetMapping("/optimize-schedule")
    @ResponseBody
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> optimizeSchedule(
            @RequestParam Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.findById(projectId);
            if (project == null) {
                response.put("success", false);
                response.put("message", "Project not found");
                return ResponseEntity.notFound().build();
            }
            
            TaskDependencyService.ScheduleOptimizationResult result = dependencyService.optimizeSchedule(project);
            
            Map<String, Object> optimization = new HashMap<>();
            optimization.put("recommendations", result.getRecommendations());
            optimization.put("potentialTimeReduction", result.getPotentialTimeReduction());
            
            Map<String, Integer> adjustments = new HashMap<>();
            for (Map.Entry<Task, Integer> entry : result.getSuggestedAdjustments().entrySet()) {
                adjustments.put(entry.getKey().getId().toString(), entry.getValue());
            }
            optimization.put("suggestedAdjustments", adjustments);
            
            response.put("success", true);
            response.put("projectId", projectId);
            response.put("optimization", optimization);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error optimizing schedule", e);
            response.put("success", false);
            response.put("message", "Error optimizing schedule: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================
    
    /**
     * Create multiple dependencies at once.
     * 
     * @param dependencies List of dependencies to create
     * @param user Current authenticated user
     * @return JSON response with created dependencies
     */
    @PostMapping("/bulk")
    @ResponseBody
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createBulkDependencies(
            @Valid @RequestBody List<TaskDependencyDto> dependencies,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            LOGGER.info("Creating bulk dependencies: " + dependencies.size() + " items");
            
            // Convert DTOs to dependency specs
            List<TaskDependencyService.DependencySpec> specs = new ArrayList<>();
            for (TaskDependencyDto dto : dependencies) {
                Task dependentTask = taskService.findById(dto.getSuccessorTaskId());
                Task prerequisiteTask = taskService.findById(dto.getPredecessorTaskId());
                
                if (dependentTask == null || prerequisiteTask == null) {
                    response.put("success", false);
                    response.put("message", "Invalid task IDs in dependency specification");
                    return ResponseEntity.badRequest().body(response);
                }
                
                specs.add(new TaskDependencyService.DependencySpec(
                    dependentTask, 
                    prerequisiteTask, 
                    dto.getType(),
                    dto.getLagDays() != null ? dto.getLagDays() * 24 : null,
                    dto.getNotes()
                ));
            }
            
            List<TaskDependency> createdDependencies = dependencyService.createBulkDependencies(specs);
            
            // Publish bulk WebSocket event
            publishBulkDependencyEvent(createdDependencies, "BULK_CREATED", user);
            
            response.put("success", true);
            response.put("message", "Bulk dependencies created successfully");
            response.put("created", createdDependencies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
            response.put("totalCreated", createdDependencies.size());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid bulk dependency creation", e);
            response.put("success", false);
            response.put("message", "Invalid dependencies: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating bulk dependencies", e);
            response.put("success", false);
            response.put("message", "Error creating bulk dependencies: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Delete multiple dependencies at once.
     * 
     * @param dependencyIds List of dependency IDs to delete
     * @param user Current authenticated user
     * @return JSON response with deletion results
     */
    @DeleteMapping("/bulk")
    @ResponseBody
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBulkDependencies(
            @RequestBody List<Long> dependencyIds,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            LOGGER.info("Deleting bulk dependencies: " + dependencyIds.size() + " items");
            
            int deletedCount = dependencyService.removeBulkDependencies(dependencyIds);
            
            // Publish bulk WebSocket event (approximate project ID)
            if (deletedCount > 0) {
                publishBulkDependencyDeletionEvent(dependencyIds, user);
            }
            
            response.put("success", true);
            response.put("message", "Bulk dependencies deleted successfully");
            response.put("deletedCount", deletedCount);
            response.put("requestedCount", dependencyIds.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting bulk dependencies", e);
            response.put("success", false);
            response.put("message", "Error deleting bulk dependencies: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // =========================================================================
    // VALIDATION AND ANALYSIS
    // =========================================================================
    
    /**
     * Validate dependency graph for a project.
     * 
     * @param projectId Project ID to validate
     * @param user Current authenticated user
     * @return JSON response with validation results
     */
    @GetMapping("/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateDependencyGraph(
            @RequestParam Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.findById(projectId);
            if (project == null) {
                response.put("success", false);
                response.put("message", "Project not found");
                return ResponseEntity.notFound().build();
            }
            
            TaskDependencyService.DependencyValidationResult result = dependencyService.validateDependencyGraph(project);
            
            Map<String, Object> validation = new HashMap<>();
            validation.put("valid", result.isValid());
            validation.put("issues", result.getIssues());
            
            List<List<Map<String, Object>>> cycles = new ArrayList<>();
            for (List<Task> cycle : result.getCycles()) {
                List<Map<String, Object>> cycleData = cycle.stream()
                    .map(this::convertTaskToDto)
                    .collect(Collectors.toList());
                cycles.add(cycleData);
            }
            validation.put("cycles", cycles);
            
            response.put("success", true);
            response.put("projectId", projectId);
            response.put("validation", validation);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error validating dependency graph", e);
            response.put("success", false);
            response.put("message", "Error validating dependency graph: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get dependency statistics for a project.
     * 
     * @param projectId Project ID to analyze
     * @param user Current authenticated user
     * @return JSON response with statistics
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDependencyStatistics(
            @RequestParam Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.findById(projectId);
            if (project == null) {
                response.put("success", false);
                response.put("message", "Project not found");
                return ResponseEntity.notFound().build();
            }
            
            Map<DependencyType, Long> stats = dependencyService.getDependencyStatistics(project);
            
            Map<String, Object> statistics = new HashMap<>();
            long totalDependencies = 0;
            
            for (Map.Entry<DependencyType, Long> entry : stats.entrySet()) {
                statistics.put(entry.getKey().name(), entry.getValue());
                totalDependencies += entry.getValue();
            }
            
            statistics.put("TOTAL", totalDependencies);
            
            response.put("success", true);
            response.put("projectId", projectId);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting dependency statistics", e);
            response.put("success", false);
            response.put("message", "Error getting dependency statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // =========================================================================
    // HELPER METHODS - DATA CONVERSION
    // =========================================================================
    
    /**
     * Convert TaskDependency entity to DTO.
     * 
     * @param dependency Task dependency to convert
     * @return TaskDependencyDto
     */
    private TaskDependencyDto convertToDto(TaskDependency dependency) {
        TaskDependencyDto dto = new TaskDependencyDto();
        dto.setId(dependency.getId());
        dto.setPredecessorTaskId(dependency.getPrerequisiteTask().getId());
        dto.setPredecessorTaskTitle(dependency.getPrerequisiteTask().getTitle());
        dto.setSuccessorTaskId(dependency.getDependentTask().getId());
        dto.setSuccessorTaskTitle(dependency.getDependentTask().getTitle());
        dto.setType(dependency.getDependencyType());
        dto.setLagDays(dependency.getLagHours() != null ? dependency.getLagHours() / 24 : null);
        dto.setActive(dependency.isActive());
        dto.setNotes(dependency.getNotes());
        dto.setCreatedAt(dependency.getCreatedAt());
        dto.setUpdatedAt(dependency.getUpdatedAt());
        dto.setOnCriticalPath(dependency.isCriticalPath());
        return dto;
    }
    
    /**
     * Convert Task entity to simple DTO for JSON responses.
     * 
     * @param task Task to convert
     * @return Simple task DTO map
     */
    private Map<String, Object> convertTaskToDto(Task task) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", task.getId());
        dto.put("title", task.getTitle());
        dto.put("description", task.getDescription());
        dto.put("progress", task.getProgress());
        dto.put("completed", task.isCompleted());
        dto.put("priority", task.getPriority().name());
        dto.put("priorityDisplay", task.getPriority().getDisplayName());
        dto.put("projectId", task.getProject().getId());
        dto.put("projectName", task.getProject().getName());
        dto.put("subsystemId", task.getSubsystem().getId());
        dto.put("subsystemName", task.getSubsystem().getName());
        
        if (task.getStartDate() != null) {
            dto.put("startDate", task.getStartDate().toString());
        }
        if (task.getEndDate() != null) {
            dto.put("endDate", task.getEndDate().toString());
        }
        
        return dto;
    }
    
    // =========================================================================
    // HELPER METHODS - WEBSOCKET EVENTS
    // =========================================================================
    
    /**
     * Publish dependency event via WebSocket.
     * 
     * @param dependency The dependency that changed
     * @param action The action performed (CREATED, UPDATED, DELETED)
     * @param user User who performed the action
     */
    private void publishDependencyEvent(TaskDependency dependency, String action, UserPrincipal user) {
        try {
            TaskUpdateMessage message = new TaskUpdateMessage();
            message.setTaskId(dependency.getDependentTask().getId());
            message.setProjectId(dependency.getDependentTask().getProject().getId());
            message.setTaskTitle(dependency.getDependentTask().getTitle());
            message.setChangeType("DEPENDENCY_" + action);
            message.setStatus("DEPENDENCY_" + action);
            message.setUpdatedBy(user.getFullName());
            message.setTimestamp(LocalDateTime.now());
            
            String destination = "/topic/project/" + dependency.getDependentTask().getProject().getId() + "/dependencies";
            messagingTemplate.convertAndSend(destination, message);
            
            LOGGER.info(String.format("Published dependency %s event for task %d", action, dependency.getDependentTask().getId()));
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error publishing dependency event", e);
        }
    }
    
    /**
     * Publish dependency deletion event via WebSocket.
     * 
     * @param dependencyId The deleted dependency ID
     * @param projectId The project ID
     * @param user User who performed the deletion
     */
    private void publishDependencyDeletionEvent(Long dependencyId, Long projectId, UserPrincipal user) {
        try {
            TaskUpdateMessage message = new TaskUpdateMessage();
            message.setTaskId(dependencyId); // Using dependency ID as task ID for deletion
            message.setProjectId(projectId);
            message.setTaskTitle("Dependency deleted");
            message.setChangeType("DEPENDENCY_DELETED");
            message.setStatus("DEPENDENCY_DELETED");
            message.setUpdatedBy(user.getFullName());
            message.setTimestamp(LocalDateTime.now());
            
            String destination = "/topic/project/" + projectId + "/dependencies";
            messagingTemplate.convertAndSend(destination, message);
            
            LOGGER.info(String.format("Published dependency deletion event for dependency %d", dependencyId));
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error publishing dependency deletion event", e);
        }
    }
    
    /**
     * Publish bulk dependency event via WebSocket.
     * 
     * @param dependencies List of dependencies affected
     * @param action The bulk action performed
     * @param user User who performed the action
     */
    private void publishBulkDependencyEvent(List<TaskDependency> dependencies, String action, UserPrincipal user) {
        try {
            if (dependencies.isEmpty()) return;
            
            Long projectId = dependencies.get(0).getDependentTask().getProject().getId();
            
            TaskUpdateMessage message = new TaskUpdateMessage();
            message.setProjectId(projectId);
            message.setTaskTitle(String.format("Bulk dependency operation: %d dependencies", dependencies.size()));
            message.setChangeType(action);
            message.setStatus(action);
            message.setUpdatedBy(user.getFullName());
            message.setTimestamp(LocalDateTime.now());
            
            String destination = "/topic/project/" + projectId + "/dependencies";
            messagingTemplate.convertAndSend(destination, message);
            
            LOGGER.info(String.format("Published bulk dependency %s event for %d dependencies", action, dependencies.size()));
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error publishing bulk dependency event", e);
        }
    }
    
    /**
     * Publish bulk dependency deletion event via WebSocket.
     * 
     * @param dependencyIds List of deleted dependency IDs
     * @param user User who performed the deletion
     */
    private void publishBulkDependencyDeletionEvent(List<Long> dependencyIds, UserPrincipal user) {
        try {
            // Note: Without the actual dependencies, we can't get the exact project ID
            // This is a simplified implementation - in a real scenario, you might want to
            // query the dependencies before deletion to get the project ID
            
            TaskUpdateMessage message = new TaskUpdateMessage();
            message.setTaskTitle(String.format("Bulk dependency deletion: %d dependencies", dependencyIds.size()));
            message.setChangeType("BULK_DEPENDENCY_DELETED");
            message.setStatus("BULK_DEPENDENCY_DELETED");
            message.setUpdatedBy(user.getFullName());
            message.setTimestamp(LocalDateTime.now());
            
            // Broadcast to general dependencies topic
            messagingTemplate.convertAndSend("/topic/dependencies", message);
            
            LOGGER.info(String.format("Published bulk dependency deletion event for %d dependencies", dependencyIds.size()));
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error publishing bulk dependency deletion event", e);
        }
    }
}