// src/main/java/org/frcpm/web/controllers/DependencyViewController.java
// Phase 2E-D: Dependency Management View Controller
// ✅ NEW: View controller for dependency management UI templates

package org.frcpm.web.controllers;

import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.TaskDependency;
import org.frcpm.services.ProjectService;
import org.frcpm.services.TaskService;
import org.frcpm.services.TaskDependencyService;
import org.frcpm.security.UserPrincipal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * DependencyViewController - Phase 2E-D Implementation
 * 
 * View controller for rendering dependency management UI templates.
 * Handles page navigation and template data preparation for the
 * dependency management interface.
 * 
 * Features:
 * - Main dependency management page
 * - Project selection and filtering
 * - Critical path visualization
 * - Task dependency analysis
 * - Integration with TaskDependencyController REST API
 * 
 * @author FRC Project Management Team
 * @version 2.0.0-2E-D
 */
@Controller
@RequestMapping("/dependencies")
@PreAuthorize("isAuthenticated()")
public class DependencyViewController {
    
    private static final Logger LOGGER = Logger.getLogger(DependencyViewController.class.getName());
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private TaskDependencyService dependencyService;
    
    /**
     * Main dependency management page.
     * 
     * @param model Spring model for template data
     * @param projectId Optional project ID filter
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping
    public String dependencyManagement(
            Model model,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading dependency management page for user: " + user.getUsername());
            
            // Get all projects for selection
            List<Project> projects = projectService.findAll();
            model.addAttribute("projects", projects);
            
            // If project is selected, load project-specific data
            if (projectId != null) {
                Project selectedProject = projectService.findById(projectId);
                if (selectedProject != null) {
                    model.addAttribute("selectedProject", selectedProject);
                    
                    // Load tasks for the project
                    List<Task> tasks = taskService.findByProject(selectedProject);
                    model.addAttribute("tasks", tasks);
                    
                    // Load dependencies
                    List<TaskDependency> dependencies = dependencyService.getProjectDependencies(selectedProject, true);
                    model.addAttribute("dependencies", dependencies);
                    
                    // Calculate critical path
                    TaskDependencyService.CriticalPathResult criticalPath = dependencyService.calculateCriticalPath(selectedProject);
                    model.addAttribute("criticalPath", criticalPath);
                    
                    // Get blocked and ready tasks
                    Map<Task, List<TaskDependency>> blockedTasks = dependencyService.getBlockedTasks(selectedProject);
                    List<Task> readyTasks = dependencyService.getTasksReadyToStart(selectedProject);
                    
                    model.addAttribute("blockedTasks", blockedTasks.keySet());
                    model.addAttribute("readyTasks", readyTasks);
                }
            }
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "dependencies");
            
            return "dependencies/index";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading dependency management page: " + e.getMessage());
            model.addAttribute("errorMessage", "Error loading dependency management page");
            return "error";
        }
    }
    
    /**
     * Critical path visualization page.
     * 
     * @param model Spring model for template data
     * @param projectId Project ID for analysis
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping("/critical-path")
    public String criticalPath(
            Model model,
            @RequestParam("projectId") Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading critical path page for project: " + projectId);
            
            Project project = projectService.findById(projectId);
            if (project == null) {
                model.addAttribute("errorMessage", "Project not found");
                return "error";
            }
            
            model.addAttribute("project", project);
            
            // Calculate critical path
            TaskDependencyService.CriticalPathResult criticalPath = dependencyService.calculateCriticalPath(project);
            model.addAttribute("criticalPath", criticalPath);
            
            // Get all project tasks for context
            List<Task> tasks = taskService.findByProject(project);
            model.addAttribute("tasks", tasks);
            
            // Get all dependencies for visualization
            List<TaskDependency> dependencies = dependencyService.getProjectDependencies(project, true);
            model.addAttribute("dependencies", dependencies);
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "critical-path");
            
            return "dependencies/critical-path";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading critical path page: " + e.getMessage());
            model.addAttribute("errorMessage", "Error loading critical path analysis");
            return "error";
        }
    }
    
    /**
     * Project dependency analysis page.
     * 
     * @param model Spring model for template data
     * @param projectId Project ID for analysis
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping("/project/{projectId}/analysis")
    public String projectAnalysis(
            Model model,
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading project analysis page for project: " + projectId);
            
            Project project = projectService.findById(projectId);
            if (project == null) {
                model.addAttribute("errorMessage", "Project not found");
                return "error";
            }
            
            model.addAttribute("project", project);
            
            // Load comprehensive project analysis
            List<TaskDependency> dependencies = dependencyService.getProjectDependencies(project, true);
            model.addAttribute("dependencies", dependencies);
            
            // Dependency validation
            TaskDependencyService.DependencyValidationResult validation = dependencyService.validateDependencyGraph(project);
            model.addAttribute("validation", validation);
            
            // Blocked tasks analysis
            Map<Task, List<TaskDependency>> blockedTasks = dependencyService.getBlockedTasks(project);
            model.addAttribute("blockedTasks", blockedTasks);
            
            // Ready tasks
            List<Task> readyTasks = dependencyService.getTasksReadyToStart(project);
            model.addAttribute("readyTasks", readyTasks);
            
            // Schedule optimization
            TaskDependencyService.ScheduleOptimizationResult optimization = dependencyService.optimizeSchedule(project);
            model.addAttribute("optimization", optimization);
            
            // Statistics
            var statistics = dependencyService.getDependencyStatistics(project);
            model.addAttribute("statistics", statistics);
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "dependencies");
            
            return "dependencies/analysis";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading project analysis page: " + e.getMessage());
            model.addAttribute("errorMessage", "Error loading project analysis");
            return "error";
        }
    }
    
    /**
     * Task dependency details page.
     * 
     * @param model Spring model for template data
     * @param taskId Task ID for dependency analysis
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping("/task/{taskId}")
    public String taskDependencies(
            Model model,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading task dependencies page for task: " + taskId);
            
            Task task = taskService.findById(taskId);
            if (task == null) {
                model.addAttribute("errorMessage", "Task not found");
                return "error";
            }
            
            model.addAttribute("task", task);
            model.addAttribute("project", task.getProject());
            
            // Get task dependencies
            List<TaskDependency> dependencies = dependencyService.getTaskDependencies(task);
            List<TaskDependency> dependents = dependencyService.getTaskDependents(task);
            
            model.addAttribute("dependencies", dependencies);
            model.addAttribute("dependents", dependents);
            
            // Task analysis
            model.addAttribute("canStart", dependencyService.canTaskStart(task));
            model.addAttribute("blockingDependencies", dependencyService.getBlockingDependencies(task));
            
            // Task float calculation
            Double taskFloat = dependencyService.calculateTaskFloat(task);
            model.addAttribute("taskFloat", taskFloat);
            model.addAttribute("isCritical", taskFloat != null && taskFloat == 0.0);
            
            // Get all project tasks for potential new dependencies
            List<Task> allTasks = taskService.findByProject(task.getProject());
            model.addAttribute("allTasks", allTasks);
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "dependencies");
            
            return "dependencies/task-detail";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading task dependencies page: " + e.getMessage());
            model.addAttribute("errorMessage", "Error loading task dependencies");
            return "error";
        }
    }
    
    /**
     * Dependency creation form page.
     * 
     * @param model Spring model for template data
     * @param projectId Project ID for new dependency
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping("/create")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public String createDependency(
            Model model,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "predecessorTaskId", required = false) Long predecessorTaskId,
            @RequestParam(value = "successorTaskId", required = false) Long successorTaskId,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading dependency creation form");
            
            // Get all projects for selection
            List<Project> projects = projectService.findAll();
            model.addAttribute("projects", projects);
            
            // If project is specified, load project tasks
            if (projectId != null) {
                Project project = projectService.findById(projectId);
                if (project != null) {
                    model.addAttribute("selectedProject", project);
                    List<Task> tasks = taskService.findByProject(project);
                    model.addAttribute("tasks", tasks);
                    
                    // Pre-select tasks if provided
                    if (predecessorTaskId != null) {
                        model.addAttribute("preSelectedPredecessor", predecessorTaskId);
                    }
                    if (successorTaskId != null) {
                        model.addAttribute("preSelectedSuccessor", successorTaskId);
                    }
                }
            }
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "dependencies");
            
            return "dependencies/create";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading dependency creation form: " + e.getMessage());
            model.addAttribute("errorMessage", "Error loading dependency creation form");
            return "error";
        }
    }
}