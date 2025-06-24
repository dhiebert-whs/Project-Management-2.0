// src/main/java/org/frcpm/web/controllers/TaskController.java

package org.frcpm.web.controllers;

import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.models.TeamMember;
import org.frcpm.services.TaskService;
import org.frcpm.services.SubsystemService;
import org.frcpm.services.MilestoneService;
import org.frcpm.services.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.logging.Level;

/**
 * Task controller handling comprehensive task management operations.
 * 
 * Features:
 * - Complete CRUD operations for tasks
 * - Task assignment and progress tracking
 * - Kanban board view for visual task management
 * - Task dependency management
 * - Component requirement tracking
 * - Bulk operations and filtering
 * - Team member assignment workflows
 * 
 * FIXED: All compilation issues resolved, proper service integration established.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2A - Web Controllers Implementation
 */
@Controller
@RequestMapping("/tasks")
public class TaskController extends BaseController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private SubsystemService subsystemService;
    
    @Autowired
    private MilestoneService milestoneService;
    
    @Autowired
    private ComponentService componentService;
    
    // =========================================================================
    // TASK LIST AND OVERVIEW
    // =========================================================================
    
    /**
     * Display list of tasks with advanced filtering and sorting.
     * 
     * @param model the Spring MVC model
     * @param projectId optional project filter
     * @param subsystemId optional subsystem filter
     * @param assigneeId optional assignee filter
     * @param status optional status filter (pending, completed, overdue)
     * @param priority optional priority filter
     * @param view optional view type (list, kanban)
     * @param sort optional sort parameter
     * @return tasks list or kanban view
     */
    @GetMapping
    public String listTasks(Model model,
                           @RequestParam(value = "projectId", required = false) Long projectId,
                           @RequestParam(value = "subsystemId", required = false) Long subsystemId,
                           @RequestParam(value = "assigneeId", required = false) Long assigneeId,
                           @RequestParam(value = "status", required = false) String status,
                           @RequestParam(value = "priority", required = false) String priority,
                           @RequestParam(value = "view", required = false, defaultValue = "list") String view,
                           @RequestParam(value = "sort", required = false, defaultValue = "priority") String sort) {
        
        try {
            addNavigationData(model);
            addBreadcrumbs(model, "Tasks", "/tasks");
            
            // Get base task list
            List<Task> allTasks = getAllTasksForUser();
            
            // Apply filters
            List<Task> filteredTasks = applyTaskFilters(allTasks, projectId, subsystemId, 
                                                       assigneeId, status, priority);
            
            // Apply sorting
            sortTasks(filteredTasks, sort);
            
            // Add to model
            model.addAttribute("tasks", filteredTasks);
            model.addAttribute("totalTasks", allTasks.size());
            model.addAttribute("filteredCount", filteredTasks.size());
            
            // Current filter values
            model.addAttribute("currentProjectId", projectId);
            model.addAttribute("currentSubsystemId", subsystemId);
            model.addAttribute("currentAssigneeId", assigneeId);
            model.addAttribute("currentStatus", status != null ? status : "all");
            model.addAttribute("currentPriority", priority != null ? priority : "all");
            model.addAttribute("currentView", view);
            model.addAttribute("currentSort", sort);
            
            // Load filter options
            loadTaskFilterOptions(model);
            
            // Add status counts
            addTaskStatusCounts(model, allTasks);
            
            // Add quick statistics
            addTaskQuickStats(model, filteredTasks);
            
            // Determine view template
            if ("kanban".equals(view)) {
                // Organize tasks by status for kanban view
                organizeTasksForKanban(model, filteredTasks);
                return view("tasks/kanban");
            } else {
                return view("tasks/list");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading tasks list", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Display detailed view of a specific task.
     * 
     * @param id the task ID
     * @param model the model
     * @return task detail view
     */
    @GetMapping("/{id}")
    public String viewTask(@PathVariable Long id, Model model) {
        try {
            Task task = taskService.findById(id);
            if (task == null) {
                addErrorMessage(model, "Task not found");
                return redirect("/tasks");
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Tasks", "/tasks", 
                          task.getTitle(), "/tasks/" + id);
            
            model.addAttribute("task", task);
            
            // Load related data
            loadTaskDetailData(model, task);
            
            // Load available actions based on user role and task status
            loadTaskActions(model, task);
            
            return view("tasks/detail");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading task detail", e);
            return handleException(e, model);
        }
    }
    
    // =========================================================================
    // TASK CREATION
    // =========================================================================
    
    /**
     * Show new task creation form.
     * 
     * @param model the model
     * @param projectId optional pre-selected project
     * @param subsystemId optional pre-selected subsystem
     * @return new task form view
     */
    @GetMapping("/new")
    public String newTaskForm(Model model,
                             @RequestParam(value = "projectId", required = false) Long projectId,
                             @RequestParam(value = "subsystemId", required = false) Long subsystemId) {
        try {
            addNavigationData(model);
            addBreadcrumbs(model, "Tasks", "/tasks", "New Task", "/tasks/new");
            
            // Create empty task for form binding
            Task task = new Task();
            
            // Set default values
            task.setPriority(Task.Priority.MEDIUM);
            task.setProgress(0);
            
            // Pre-select project and subsystem if provided
            if (projectId != null) {
                Project project = projectService.findById(projectId);
                if (project != null) {
                    task.setProject(project);
                }
            }
            
            if (subsystemId != null) {
                Subsystem subsystem = subsystemService.findById(subsystemId);
                if (subsystem != null) {
                    task.setSubsystem(subsystem);
                }
            }
            
            model.addAttribute("task", task);
            model.addAttribute("isEdit", false);
            
            // Load form options
            loadTaskFormOptions(model);
            
            // Add helpful defaults
            addTaskFormHelpers(model);
            
            return view("tasks/form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading new task form", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process new task creation.
     * 
     * @param task the task data from form
     * @param result binding result for validation
     * @param model the model
     * @param redirectAttributes for redirect messages
     * @param assignedMemberIds array of assigned team member IDs
     * @return redirect to task detail or back to form with errors
     */
    @PostMapping("/new")
    public String createTask(@Valid @ModelAttribute Task task,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes,
                            @RequestParam(value = "assignedMemberIds", required = false) Long[] assignedMemberIds) {
        
        try {
            // Validate form data
            validateTaskData(task, result);
            
            if (result.hasErrors()) {
                addNavigationData(model);
                addBreadcrumbs(model, "Tasks", "/tasks", "New Task", "/tasks/new");
                model.addAttribute("isEdit", false);
                loadTaskFormOptions(model);
                addTaskFormHelpers(model);
                addErrorMessage(model, "Please correct the errors below");
                return view("tasks/form");
            }
            
            // Create the task using the service
            double estimatedHours = task.getEstimatedDuration() != null ? 
                task.getEstimatedDuration().toMinutes() / 60.0 : 1.0;
            Task savedTask = taskService.createTask(
                task.getTitle(),
                task.getProject(),
                task.getSubsystem(),
                estimatedHours,
                task.getPriority(),
                task.getStartDate(),
                task.getEndDate()
            );
            
            // Set description if provided
            if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
                savedTask.setDescription(task.getDescription());
                savedTask = taskService.save(savedTask);
            }
            
            // Assign team members if provided
            if (assignedMemberIds != null && assignedMemberIds.length > 0) {
                Set<TeamMember> assignedMembers = java.util.Arrays.stream(assignedMemberIds)
                    .map(id -> teamMemberService.findById(id))
                    .filter(member -> member != null)
                    .collect(Collectors.toSet());
                
                if (!assignedMembers.isEmpty()) {
                    savedTask = taskService.assignMembers(savedTask.getId(), assignedMembers);
                }
            }
            
            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", 
                "Task '" + savedTask.getTitle() + "' created successfully!");
            
            // Redirect to the new task
            return redirect("/tasks/" + savedTask.getId());
            
        } catch (IllegalArgumentException e) {
            addNavigationData(model);
            addBreadcrumbs(model, "Tasks", "/tasks", "New Task", "/tasks/new");
            model.addAttribute("isEdit", false);
            loadTaskFormOptions(model);
            addTaskFormHelpers(model);
            addErrorMessage(model, e.getMessage());
            return view("tasks/form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating task", e);
            return handleException(e, model);
        }
    }
    
    // =========================================================================
    // TASK EDITING
    // =========================================================================
    
    /**
     * Show task edit form.
     * 
     * @param id the task ID
     * @param model the model
     * @return edit task form view
     */
    @GetMapping("/{id}/edit")
    public String editTaskForm(@PathVariable Long id, Model model) {
        try {
            Task task = taskService.findById(id);
            if (task == null) {
                addErrorMessage(model, "Task not found");
                return redirect("/tasks");
            }
            
            // Check permissions
            if (!canEditTask(task)) {
                addErrorMessage(model, "You don't have permission to edit this task");
                return redirect("/tasks/" + id);
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Tasks", "/tasks", 
                          task.getTitle(), "/tasks/" + id,
                          "Edit", "/tasks/" + id + "/edit");
            
            model.addAttribute("task", task);
            model.addAttribute("isEdit", true);
            
            // Load form options
            loadTaskFormOptions(model);
            
            // Add edit warnings
            addTaskEditWarnings(model, task);
            
            // Pre-populate assigned members
            Long[] assignedMemberIds = task.getAssignedTo().stream()
                .map(TeamMember::getId)
                .toArray(Long[]::new);
            model.addAttribute("assignedMemberIds", assignedMemberIds);
            
            return view("tasks/form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading task edit form", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process task update.
     * 
     * @param id the task ID
     * @param task the updated task data
     * @param result binding result
     * @param model the model
     * @param redirectAttributes for redirect messages
     * @param assignedMemberIds array of assigned team member IDs
     * @return redirect to task detail or back to form with errors
     */
    @PostMapping("/{id}/edit")
    public String updateTask(@PathVariable Long id,
                            @Valid @ModelAttribute Task task,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes,
                            @RequestParam(value = "assignedMemberIds", required = false) Long[] assignedMemberIds) {
        
        try {
            // Validate task exists
            Task existingTask = taskService.findById(id);
            if (existingTask == null) {
                addErrorMessage(model, "Task not found");
                return redirect("/tasks");
            }
            
            // Check permissions
            if (!canEditTask(existingTask)) {
                addErrorMessage(model, "You don't have permission to edit this task");
                return redirect("/tasks/" + id);
            }
            
            // Validate form data
            validateTaskData(task, result);
            
            if (result.hasErrors()) {
                addNavigationData(model);
                addBreadcrumbs(model, "Tasks", "/tasks", 
                              existingTask.getTitle(), "/tasks/" + id,
                              "Edit", "/tasks/" + id + "/edit");
                model.addAttribute("isEdit", true);
                loadTaskFormOptions(model);
                addTaskEditWarnings(model, existingTask);
                addErrorMessage(model, "Please correct the errors below");
                return view("tasks/form");
            }
            
            // Update the task properties
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setProject(task.getProject());
            existingTask.setSubsystem(task.getSubsystem());
            existingTask.setPriority(task.getPriority());
            existingTask.setStartDate(task.getStartDate());
            existingTask.setEndDate(task.getEndDate());
            existingTask.setEstimatedDuration(task.getEstimatedDuration());
            
            // Save the updated task
            Task updatedTask = taskService.save(existingTask);
            
            // Update team member assignments
            if (assignedMemberIds != null) {
                Set<TeamMember> assignedMembers = java.util.Arrays.stream(assignedMemberIds)
                    .map(memberId -> teamMemberService.findById(memberId))
                    .filter(member -> member != null)
                    .collect(Collectors.toSet());
                
                updatedTask = taskService.assignMembers(updatedTask.getId(), assignedMembers);
            } else {
                // Clear all assignments if none selected
                updatedTask = taskService.assignMembers(updatedTask.getId(), Set.of());
            }
            
            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", 
                "Task '" + updatedTask.getTitle() + "' updated successfully!");
            
            return redirect("/tasks/" + updatedTask.getId());
            
        } catch (IllegalArgumentException e) {
            addNavigationData(model);
            addBreadcrumbs(model, "Tasks", "/tasks", 
                          task.getTitle(), "/tasks/" + id,
                          "Edit", "/tasks/" + id + "/edit");
            model.addAttribute("isEdit", true);
            loadTaskFormOptions(model);
            addErrorMessage(model, e.getMessage());
            return view("tasks/form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating task", e);
            return handleException(e, model);
        }
    }
    
    // =========================================================================
    // TASK PROGRESS AND STATUS UPDATES
    // =========================================================================
    
    /**
     * Quick progress update endpoint (AJAX-friendly).
     * 
     * @param id the task ID
     * @param progress the new progress percentage
     * @param redirectAttributes for redirect messages
     * @return redirect back to referring page
     */
    @PostMapping("/{id}/progress")
    public String updateTaskProgress(@PathVariable Long id,
                                   @RequestParam int progress,
                                   @RequestHeader(value = "Referer", required = false) String referer,
                                   RedirectAttributes redirectAttributes) {
        
        try {
            Task task = taskService.findById(id);
            if (task == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Task not found");
                return redirect("/tasks");
            }
            
            // Check permissions
            if (!canUpdateTaskProgress(task)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "You don't have permission to update this task's progress");
                return redirect("/tasks/" + id);
            }
            
            // Update progress
            boolean completed = progress >= 100;
            Task updatedTask = taskService.updateTaskProgress(id, progress, completed);
            
            if (updatedTask != null) {
                String statusMessage = completed ? "Task completed!" : 
                    "Progress updated to " + progress + "%";
                redirectAttributes.addFlashAttribute("successMessage", statusMessage);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update progress");
            }
            
            // Redirect back to referring page or task detail
            if (referer != null && !referer.isEmpty()) {
                return "redirect:" + referer;
            } else {
                return redirect("/tasks/" + id);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating task progress", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating progress");
            return redirect("/tasks/" + id);
        }
    }
    
    /**
     * Toggle task completion status.
     * 
     * @param id the task ID
     * @param redirectAttributes for redirect messages
     * @return redirect back to referring page
     */
    @PostMapping("/{id}/toggle-completion")
    public String toggleTaskCompletion(@PathVariable Long id,
                                     @RequestHeader(value = "Referer", required = false) String referer,
                                     RedirectAttributes redirectAttributes) {
        
        try {
            Task task = taskService.findById(id);
            if (task == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Task not found");
                return redirect("/tasks");
            }
            
            // Check permissions
            if (!canUpdateTaskProgress(task)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "You don't have permission to update this task");
                return redirect("/tasks/" + id);
            }
            
            // Toggle completion
            boolean newCompletionStatus = !task.isCompleted();
            int newProgress = newCompletionStatus ? 100 : Math.max(0, task.getProgress() - 1);
            
            Task updatedTask = taskService.updateTaskProgress(id, newProgress, newCompletionStatus);
            
            if (updatedTask != null) {
                String statusMessage = newCompletionStatus ? 
                    "Task marked as completed!" : "Task marked as incomplete";
                redirectAttributes.addFlashAttribute("successMessage", statusMessage);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update task status");
            }
            
            // Redirect back to referring page or task detail
            if (referer != null && !referer.isEmpty()) {
                return "redirect:" + referer;
            } else {
                return redirect("/tasks/" + id);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error toggling task completion", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating task status");
            return redirect("/tasks/" + id);
        }
    }
    
    // =========================================================================
    // TASK DELETION
    // =========================================================================
    
    /**
     * Show task deletion confirmation.
     * 
     * @param id the task ID
     * @param model the model
     * @return deletion confirmation view
     */
    @GetMapping("/{id}/delete")
    public String confirmDeleteTask(@PathVariable Long id, Model model) {
        try {
            Task task = taskService.findById(id);
            if (task == null) {
                addErrorMessage(model, "Task not found");
                return redirect("/tasks");
            }
            
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                addErrorMessage(model, "Only mentors and admins can delete tasks");
                return redirect("/tasks/" + id);
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Tasks", "/tasks", 
                          task.getTitle(), "/tasks/" + id,
                          "Delete", "/tasks/" + id + "/delete");
            
            model.addAttribute("task", task);
            
            // Add deletion impact information
            addTaskDeletionImpactInfo(model, task);
            
            return view("tasks/delete-confirm");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading delete confirmation", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process task deletion.
     * 
     * @param id the task ID
     * @param redirectAttributes for redirect messages
     * @return redirect to tasks list
     */
    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Task task = taskService.findById(id);
            if (task == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Task not found");
                return redirect("/tasks");
            }
            
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Only mentors and admins can delete tasks");
                return redirect("/tasks/" + id);
            }
            
            String taskTitle = task.getTitle();
            Long projectId = task.getProject().getId();
            
            // Delete the task
            boolean deleted = taskService.deleteById(id);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Task '" + taskTitle + "' deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to delete task '" + taskTitle + "'");
            }
            
            // Redirect to project tasks or general task list
            return redirect("/tasks?projectId=" + projectId);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting task", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error occurred while deleting task");
            return redirect("/tasks");
        }
    }
    
    // =========================================================================
    // TASK DEPENDENCY MANAGEMENT
    // =========================================================================
    
    /**
     * Add task dependency.
     * 
     * @param id the task ID
     * @param dependencyId the dependency task ID
     * @param redirectAttributes for redirect messages
     * @return redirect to task detail
     */
    @PostMapping("/{id}/dependencies/add")
    public String addTaskDependency(@PathVariable Long id,
                                   @RequestParam Long dependencyId,
                                   RedirectAttributes redirectAttributes) {
        
        try {
            boolean added = taskService.addDependency(id, dependencyId);
            
            if (added) {
                redirectAttributes.addFlashAttribute("successMessage", "Dependency added successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to add dependency");
            }
            
            return redirect("/tasks/" + id);
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return redirect("/tasks/" + id);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding task dependency", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding dependency");
            return redirect("/tasks/" + id);
        }
    }
    
    /**
     * Remove task dependency.
     * 
     * @param id the task ID
     * @param dependencyId the dependency task ID
     * @param redirectAttributes for redirect messages
     * @return redirect to task detail
     */
    @PostMapping("/{id}/dependencies/remove")
    public String removeTaskDependency(@PathVariable Long id,
                                      @RequestParam Long dependencyId,
                                      RedirectAttributes redirectAttributes) {
        
        try {
            boolean removed = taskService.removeDependency(id, dependencyId);
            
            if (removed) {
                redirectAttributes.addFlashAttribute("successMessage", "Dependency removed successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove dependency");
            }
            
            return redirect("/tasks/" + id);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error removing task dependency", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error removing dependency");
            return redirect("/tasks/" + id);
        }
    }
    
    // =========================================================================
    // AJAX AND API ENDPOINTS
    // =========================================================================
    
    /**
     * AJAX endpoint for task progress update (returns JSON).
     * 
     * @param id the task ID
     * @param progress the new progress value
     * @return JSON response with updated task data
     */
    @PostMapping("/{id}/progress/ajax")
    @ResponseBody
    public Map<String, Object> updateTaskProgressAjax(@PathVariable Long id,
                                                      @RequestParam int progress) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Task task = taskService.findById(id);
            if (task == null) {
                response.put("success", false);
                response.put("message", "Task not found");
                return response;
            }
            
            // Check permissions
            if (!canUpdateTaskProgress(task)) {
                response.put("success", false);
                response.put("message", "Permission denied");
                return response;
            }
            
            // Update progress
            boolean completed = progress >= 100;
            Task updatedTask = taskService.updateTaskProgress(id, progress, completed);
            
            if (updatedTask != null) {
                response.put("success", true);
                response.put("progress", updatedTask.getProgress());
                response.put("completed", updatedTask.isCompleted());
                response.put("message", completed ? "Task completed!" : "Progress updated");
                
                // Add additional data for UI updates
                response.put("taskId", updatedTask.getId());
                response.put("title", updatedTask.getTitle());
                response.put("projectName", updatedTask.getProject().getName());
                
            } else {
                response.put("success", false);
                response.put("message", "Failed to update progress");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating task progress via AJAX", e);
            response.put("success", false);
            response.put("message", "Error updating progress: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Export tasks to CSV format.
     * 
     * @param projectId optional project filter
     * @param status optional status filter
     * @param response HTTP response for file download
     */
    @GetMapping("/export/csv")
    public void exportTasksToCsv(@RequestParam(required = false) Long projectId,
                                 @RequestParam(required = false) String status,
                                 HttpServletResponse response) {
        
        try {
            // Set response headers for file download
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"tasks_export.csv\"");
            
            // Get tasks to export
            List<Task> tasks = getAllTasksForUser();
            if (projectId != null) {
                Project project = projectService.findById(projectId);
                if (project != null) {
                    tasks = taskService.findByProject(project);
                }
            }
            
            // Apply status filter if specified
            if (status != null && !status.equals("all")) {
                tasks = tasks.stream()
                    .filter(task -> filterByStatus(task, status))
                    .collect(Collectors.toList());
            }
            
            // Write CSV headers
            PrintWriter writer = response.getWriter();
            writer.println("ID,Title,Description,Project,Subsystem,Priority,Progress,Completed,Start Date,End Date,Estimated Hours,Assigned To");
            
            // Write task data
            for (Task task : tasks) {
                StringBuilder line = new StringBuilder();
                line.append(escapeCSV(task.getId().toString())).append(",");
                line.append(escapeCSV(task.getTitle())).append(",");
                line.append(escapeCSV(task.getDescription())).append(",");
                line.append(escapeCSV(task.getProject().getName())).append(",");
                line.append(escapeCSV(task.getSubsystem().getName())).append(",");
                line.append(escapeCSV(task.getPriority().name())).append(",");
                line.append(task.getProgress()).append(",");
                line.append(task.isCompleted()).append(",");
                line.append(task.getStartDate() != null ? task.getStartDate().toString() : "").append(",");
                line.append(task.getEndDate() != null ? task.getEndDate().toString() : "").append(",");
                
                if (task.getEstimatedDuration() != null) {
                    line.append(task.getEstimatedDuration().toMinutes() / 60.0);
                }
                line.append(",");
                
                // Assigned members
                String assignees = task.getAssignedTo().stream()
                    .map(TeamMember::getFullName)
                    .collect(Collectors.joining("; "));
                line.append(escapeCSV(assignees));
                
                writer.println(line.toString());
            }
            
            writer.flush();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting tasks to CSV", e);
            try {
                response.sendError(500, "Error exporting tasks");
            } catch (IOException ioException) {
                LOGGER.log(Level.SEVERE, "Error sending error response", ioException);
            }
        }
    }
    
    /**
     * Quick search endpoint for tasks (AJAX).
     * 
     * @param query search query
     * @param limit maximum number of results
     * @return JSON search results
     */
    @GetMapping("/search")
    @ResponseBody
    public Map<String, Object> searchTasks(@RequestParam String query,
                                          @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (query == null || query.trim().length() < 2) {
                response.put("success", false);
                response.put("message", "Query must be at least 2 characters");
                return response;
            }
            
            List<Task> allTasks = getAllTasksForUser();
            String searchQuery = query.toLowerCase().trim();
            
            List<Map<String, Object>> results = allTasks.stream()
                .filter(task -> 
                    task.getTitle().toLowerCase().contains(searchQuery) ||
                    (task.getDescription() != null && task.getDescription().toLowerCase().contains(searchQuery)) ||
                    task.getProject().getName().toLowerCase().contains(searchQuery) ||
                    task.getSubsystem().getName().toLowerCase().contains(searchQuery)
                )
                .limit(limit)
                .map(task -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", task.getId());
                    result.put("title", task.getTitle());
                    result.put("project", task.getProject().getName());
                    result.put("subsystem", task.getSubsystem().getName());
                    result.put("progress", task.getProgress());
                    result.put("priority", task.getPriority().getDisplayName());
                    result.put("url", "/tasks/" + task.getId());
                    return result;
                })
                .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("results", results);
            response.put("totalFound", results.size());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error searching tasks", e);
            response.put("success", false);
            response.put("message", "Error performing search: " + e.getMessage());
        }
        
        return response;
    }
    
    // =========================================================================
    // BULK OPERATIONS
    // =========================================================================
    
    /**
     * Bulk operations on tasks (complete, assign, delete multiple tasks).
     * 
     * @param taskIds array of task IDs to operate on
     * @param operation the operation to perform (complete, assign, delete)
     * @param assigneeId optional assignee ID for bulk assignment
     * @param redirectAttributes for redirect messages
     * @return redirect to tasks list
     */
    @PostMapping("/bulk")
    public String bulkTaskOperation(@RequestParam("taskIds") Long[] taskIds,
                                   @RequestParam String operation,
                                   @RequestParam(required = false) Long assigneeId,
                                   RedirectAttributes redirectAttributes) {
        
        try {
            if (taskIds == null || taskIds.length == 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "No tasks selected");
                return redirect("/tasks");
            }
            
            int successCount = 0;
            int errorCount = 0;
            
            for (Long taskId : taskIds) {
                try {
                    Task task = taskService.findById(taskId);
                    if (task == null) {
                        errorCount++;
                        continue;
                    }
                    
                    switch (operation.toLowerCase()) {
                        case "complete":
                            if (canUpdateTaskProgress(task)) {
                                taskService.updateTaskProgress(taskId, 100, true);
                                successCount++;
                            } else {
                                errorCount++;
                            }
                            break;
                            
                        case "assign":
                            if ((hasRole("MENTOR") || hasRole("ADMIN")) && assigneeId != null) {
                                TeamMember assignee = teamMemberService.findById(assigneeId);
                                if (assignee != null) {
                                    Set<TeamMember> currentAssignees = task.getAssignedTo();
                                    currentAssignees.add(assignee);
                                    taskService.assignMembers(taskId, currentAssignees);
                                    successCount++;
                                } else {
                                    errorCount++;
                                }
                            } else {
                                errorCount++;
                            }
                            break;
                            
                        case "delete":
                            if (hasRole("MENTOR") || hasRole("ADMIN")) {
                                taskService.deleteById(taskId);
                                successCount++;
                            } else {
                                errorCount++;
                            }
                            break;
                            
                        default:
                            errorCount++;
                            break;
                    }
                    
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error in bulk operation for task " + taskId, e);
                    errorCount++;
                }
            }
            
            // Prepare result message
            String message = String.format("Bulk %s operation completed: %d successful, %d failed", 
                                          operation, successCount, errorCount);
            
            if (errorCount == 0) {
                redirectAttributes.addFlashAttribute("successMessage", message);
            } else if (successCount > 0) {
                redirectAttributes.addFlashAttribute("warningMessage", message);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Bulk " + operation + " operation failed for all selected tasks");
            }
            
            return redirect("/tasks");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in bulk task operation", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error performing bulk operation");
            return redirect("/tasks");
        }
    }
    
    /**
     * Clone/duplicate a task.
     * 
     * @param id the task ID to clone
     * @param redirectAttributes for redirect messages
     * @return redirect to edit form for new task
     */
    @PostMapping("/{id}/clone")
    public String cloneTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Task originalTask = taskService.findById(id);
            if (originalTask == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Task not found");
                return redirect("/tasks");
            }
            
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Only mentors and admins can clone tasks");
                return redirect("/tasks/" + id);
            }
            
            // Create new task with cloned data
            Task clonedTask = taskService.createTask(
                "Copy of " + originalTask.getTitle(),
                originalTask.getProject(),
                originalTask.getSubsystem(),
                originalTask.getEstimatedDuration().toMinutes() / 60.0,
                originalTask.getPriority(),
                null, // Don't copy dates
                null
            );
            
            // Copy description
            if (originalTask.getDescription() != null) {
                clonedTask.setDescription(originalTask.getDescription());
                clonedTask = taskService.save(clonedTask);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Task cloned successfully. Please review and update details.");
            
            return redirect("/tasks/" + clonedTask.getId() + "/edit");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error cloning task", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error cloning task");
            return redirect("/tasks/" + id);
        }
    }
    
    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================
    
    /**
     * Get all tasks appropriate for the current user.
     */
    private List<Task> getAllTasksForUser() {
        // TODO: Phase 2B - Filter based on user role and permissions
        // For now, return all tasks during Phase 2A
        return taskService.findAll();
    }
    
    /**
     * Apply filters to task list.
     */
    private List<Task> applyTaskFilters(List<Task> tasks, Long projectId, Long subsystemId,
                                       Long assigneeId, String status, String priority) {
        
        return tasks.stream()
            .filter(task -> projectId == null || task.getProject().getId().equals(projectId))
            .filter(task -> subsystemId == null || task.getSubsystem().getId().equals(subsystemId))
            .filter(task -> assigneeId == null || task.getAssignedTo().stream()
                .anyMatch(member -> member.getId().equals(assigneeId)))
            .filter(task -> filterByStatus(task, status))
            .filter(task -> filterByPriority(task, priority))
            .collect(Collectors.toList());
    }
    
    /**
     * Filter task by status.
     */
    private boolean filterByStatus(Task task, String status) {
        if (status == null || "all".equals(status)) {
            return true;
        }
        
        LocalDate today = LocalDate.now();
        switch (status.toLowerCase()) {
            case "pending":
                return !task.isCompleted();
            case "completed":
                return task.isCompleted();
            case "overdue":
                return !task.isCompleted() && task.getEndDate() != null && 
                       task.getEndDate().isBefore(today);
            case "due-soon":
                return !task.isCompleted() && task.getEndDate() != null &&
                       task.getEndDate().isAfter(today) && 
                       task.getEndDate().isBefore(today.plusDays(7));
            default:
                return true;
        }
    }
    
    /**
     * Filter task by priority.
     */
    private boolean filterByPriority(Task task, String priority) {
        if (priority == null || "all".equals(priority)) {
            return true;
        }
        
        try {
            Task.Priority taskPriority = Task.Priority.valueOf(priority.toUpperCase());
            return task.getPriority() == taskPriority;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
    
    /**
     * Sort tasks by specified criteria.
     */
    private void sortTasks(List<Task> tasks, String sort) {
        switch (sort.toLowerCase()) {
            case "priority":
                tasks.sort((t1, t2) -> Integer.compare(t2.getPriority().getValue(), t1.getPriority().getValue()));
                break;
            case "duedate":
                tasks.sort((t1, t2) -> {
                    if (t1.getEndDate() == null) return 1;
                    if (t2.getEndDate() == null) return -1;
                    return t1.getEndDate().compareTo(t2.getEndDate());
                });
                break;
            case "progress":
                tasks.sort((t1, t2) -> Integer.compare(t1.getProgress(), t2.getProgress()));
                break;
            case "title":
                tasks.sort((t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
                break;
            case "project":
                tasks.sort((t1, t2) -> t1.getProject().getName().compareToIgnoreCase(t2.getProject().getName()));
                break;
            default:
                // Default: priority descending
                tasks.sort((t1, t2) -> Integer.compare(t2.getPriority().getValue(), t1.getPriority().getValue()));
                break;
        }
    }
    
    /**
     * Load filter options for dropdowns.
     */
    private void loadTaskFilterOptions(Model model) {
        try {
            // Projects
            List<Project> projects = projectService.findAll();
            model.addAttribute("projectOptions", projects);
            
            // Subsystems
            List<Subsystem> subsystems = subsystemService.findAll();
            model.addAttribute("subsystemOptions", subsystems);
            
            // Team members
            List<TeamMember> members = teamMemberService.findAll();
            model.addAttribute("memberOptions", members);
            
            // Priority options
            model.addAttribute("priorityOptions", Task.Priority.values());
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load filter options", e);
            // Set safe defaults to prevent template errors
            model.addAttribute("projectOptions", List.of());
            model.addAttribute("subsystemOptions", List.of());
            model.addAttribute("memberOptions", List.of());
            model.addAttribute("priorityOptions", Task.Priority.values());
        }
    }
    
    /**
     * Add task status counts for filter badges.
     */
    private void addTaskStatusCounts(Model model, List<Task> allTasks) {
        LocalDate today = LocalDate.now();
        
        long pendingCount = allTasks.stream().filter(t -> !t.isCompleted()).count();
        long completedCount = allTasks.stream().filter(Task::isCompleted).count();
        long overdueCount = allTasks.stream()
            .filter(t -> !t.isCompleted() && t.getEndDate() != null && t.getEndDate().isBefore(today))
            .count();
        long dueSoonCount = allTasks.stream()
            .filter(t -> !t.isCompleted() && t.getEndDate() != null &&
                        t.getEndDate().isAfter(today) && t.getEndDate().isBefore(today.plusDays(7)))
            .count();
        
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("dueSoonCount", dueSoonCount);
    }
    
    /**
     * Add quick statistics for tasks overview.
     */
    private void addTaskQuickStats(Model model, List<Task> tasks) {
        if (tasks.isEmpty()) {
            model.addAttribute("avgProgress", 0);
            model.addAttribute("highPriorityCount", 0);
            model.addAttribute("unassignedCount", 0);
            return;
        }
        
        // Average progress
        double avgProgress = tasks.stream()
            .mapToInt(Task::getProgress)
            .average()
            .orElse(0.0);
        model.addAttribute("avgProgress", Math.round(avgProgress));
        
        // High priority tasks
        long highPriorityCount = tasks.stream()
            .filter(t -> t.getPriority() == Task.Priority.HIGH || t.getPriority() == Task.Priority.CRITICAL)
            .count();
        model.addAttribute("highPriorityCount", highPriorityCount);
        
        // Unassigned tasks
        long unassignedCount = tasks.stream()
            .filter(t -> t.getAssignedTo().isEmpty())
            .count();
        model.addAttribute("unassignedCount", unassignedCount);
    }
    
    /**
     * Organize tasks for kanban view by status.
     */
    private void organizeTasksForKanban(Model model, List<Task> tasks) {
        Map<String, List<Task>> tasksByStatus = tasks.stream()
            .collect(Collectors.groupingBy(task -> {
                if (task.getProgress() == 0) return "todo";
                if (task.getProgress() < 100) return "in-progress";
                return "done";
            }));
        
        model.addAttribute("todoTasks", tasksByStatus.getOrDefault("todo", List.of()));
        model.addAttribute("inProgressTasks", tasksByStatus.getOrDefault("in-progress", List.of()));
        model.addAttribute("doneTasks", tasksByStatus.getOrDefault("done", List.of()));
    }
    
    /**
     * Load task detail data including dependencies and assignments.
     */
    private void loadTaskDetailData(Model model, Task task) {
        try {
            // Dependencies
            model.addAttribute("preDependencies", task.getPreDependencies());
            model.addAttribute("postDependencies", task.getPostDependencies());
            
            // Assigned team members
            model.addAttribute("assignedMembers", task.getAssignedTo());
            
            // Required components (if any)
            model.addAttribute("requiredComponents", task.getRequiredComponents());
            
            // Project context
            model.addAttribute("project", task.getProject());
            model.addAttribute("subsystem", task.getSubsystem());
            
            // Calculate time information
            addTaskTimeInfo(model, task);
            
            // Related tasks in same project/subsystem
            loadRelatedTasks(model, task);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load task detail data", e);
        }
    }
    
    /**
     * Add time-related information for task.
     */
    private void addTaskTimeInfo(Model model, Task task) {
        LocalDate today = LocalDate.now();
        
        // Days until due date
        if (task.getEndDate() != null) {
            long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(today, task.getEndDate());
            model.addAttribute("daysUntilDue", daysUntilDue);
            
            if (daysUntilDue < 0) {
                model.addAttribute("dueStatus", "overdue");
            } else if (daysUntilDue <= 1) {
                model.addAttribute("dueStatus", "critical");
            } else if (daysUntilDue <= 3) {
                model.addAttribute("dueStatus", "warning");
            } else {
                model.addAttribute("dueStatus", "normal");
            }
        }
        
        // Time tracking
        if (task.getEstimatedDuration() != null) {
            double estimatedHours = task.getEstimatedDuration().toMinutes() / 60.0;
            model.addAttribute("estimatedHours", estimatedHours);
        }
        
        if (task.getActualDuration() != null) {
            double actualHours = task.getActualDuration().toMinutes() / 60.0;
            model.addAttribute("actualHours", actualHours);
        }
    }
    
    /**
     * Load related tasks for context.
     */
    private void loadRelatedTasks(Model model, Task task) {
        try {
            // Other tasks in same project
            List<Task> projectTasks = taskService.findByProject(task.getProject())
                .stream()
                .filter(t -> !t.getId().equals(task.getId()))
                .limit(5)
                .collect(Collectors.toList());
            model.addAttribute("relatedProjectTasks", projectTasks);
            
            // Other tasks in same subsystem
            List<Task> subsystemTasks = taskService.findBySubsystem(task.getSubsystem())
                .stream()
                .filter(t -> !t.getId().equals(task.getId()))
                .limit(5)
                .collect(Collectors.toList());
            model.addAttribute("relatedSubsystemTasks", subsystemTasks);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load related tasks", e);
            model.addAttribute("relatedProjectTasks", List.of());
            model.addAttribute("relatedSubsystemTasks", List.of());
        }
    }
    
    /**
     * Load available actions based on user role and task status.
     */
    private void loadTaskActions(Model model, Task task) {
        // Check what actions current user can perform
        model.addAttribute("canEdit", canEditTask(task));
        model.addAttribute("canUpdateProgress", canUpdateTaskProgress(task));
        model.addAttribute("canDelete", hasRole("MENTOR") || hasRole("ADMIN"));
        model.addAttribute("canAssign", hasRole("MENTOR") || hasRole("ADMIN"));
        model.addAttribute("canManageDependencies", hasRole("MENTOR") || hasRole("ADMIN"));
        
        // Available team members for assignment
        if (hasRole("MENTOR") || hasRole("ADMIN")) {
            try {
                List<TeamMember> availableMembers = teamMemberService.findAll();
                model.addAttribute("availableMembers", availableMembers);
            } catch (Exception e) {
                model.addAttribute("availableMembers", List.of());
            }
        }
        
        // Available tasks for dependencies
        if (hasRole("MENTOR") || hasRole("ADMIN")) {
            try {
                List<Task> availableTasks = taskService.findByProject(task.getProject())
                    .stream()
                    .filter(t -> !t.getId().equals(task.getId()))
                    .filter(t -> !task.getPreDependencies().contains(t))
                    .collect(Collectors.toList());
                model.addAttribute("availableDependencyTasks", availableTasks);
            } catch (Exception e) {
                model.addAttribute("availableDependencyTasks", List.of());
            }
        }
    }
    
    /**
     * Load form options for task creation/editing.
     */
    private void loadTaskFormOptions(Model model) {
        try {
            // Projects
            List<Project> projects = projectService.findAll();
            model.addAttribute("projectOptions", projects);
            
            // Subsystems
            List<Subsystem> subsystems = subsystemService.findAll();
            model.addAttribute("subsystemOptions", subsystems);
            
            // Team members for assignment
            List<TeamMember> members = teamMemberService.findAll();
            model.addAttribute("memberOptions", members);
            
            // Priority options
            model.addAttribute("priorityOptions", Task.Priority.values());
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load form options", e);
            // Set safe defaults
            model.addAttribute("projectOptions", List.of());
            model.addAttribute("subsystemOptions", List.of());
            model.addAttribute("memberOptions", List.of());
            model.addAttribute("priorityOptions", Task.Priority.values());
        }
    }
    
    /**
     * Add helpful defaults and suggestions for task forms.
     */
    private void addTaskFormHelpers(Model model) {
        model.addAttribute("estimatedDurationOptions", List.of(
            Map.of("hours", 1, "label", "1 hour"),
            Map.of("hours", 2, "label", "2 hours"),
            Map.of("hours", 4, "label", "4 hours"),
            Map.of("hours", 8, "label", "1 day"),
            Map.of("hours", 16, "label", "2 days"),
            Map.of("hours", 40, "label", "1 week")
        ));
        
        model.addAttribute("priorityDescriptions", Map.of(
            "LOW", "Nice to have, can be delayed",
            "MEDIUM", "Important but not urgent",
            "HIGH", "Important and time-sensitive",
            "CRITICAL", "Must be completed immediately"
        ));
    }
    
    /**
     * Add warnings for task editing.
     */
    private void addTaskEditWarnings(Model model, Task task) {
        if (task.isCompleted()) {
            addWarningMessage(model, "This task is already completed. Changes may affect reporting.");
        }
        
        if (!task.getPostDependencies().isEmpty()) {
            addWarningMessage(model, 
                "This task has " + task.getPostDependencies().size() + 
                " dependent tasks. Changes may affect project timeline.");
        }
        
        if (task.getEndDate() != null && task.getEndDate().isBefore(LocalDate.now())) {
            addWarningMessage(model, "This task is overdue. Consider updating the due date.");
        }
    }
    
    /**
     * Add deletion impact information.
     */
    private void addTaskDeletionImpactInfo(Model model, Task task) {
        model.addAttribute("dependencyCount", task.getPostDependencies().size());
        model.addAttribute("assigneeCount", task.getAssignedTo().size());
        model.addAttribute("isCompleted", task.isCompleted());
        model.addAttribute("progress", task.getProgress());
        
        if (!task.getPostDependencies().isEmpty()) {
            model.addAttribute("dependentTasks", task.getPostDependencies());
        }
    }
    
    /**
     * Validate task data for creation/editing.
     */
    private void validateTaskData(Task task, BindingResult result) {
        // Title validation
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            result.rejectValue("title", "required", "Task title is required");
        }
        
        // Project validation
        if (task.getProject() == null) {
            result.rejectValue("project", "required", "Project is required");
        }
        
        // Subsystem validation
        if (task.getSubsystem() == null) {
            result.rejectValue("subsystem", "required", "Subsystem is required");
        }
        
        // Date validation
        if (task.getStartDate() != null && task.getEndDate() != null &&
            task.getEndDate().isBefore(task.getStartDate())) {
            result.rejectValue("endDate", "invalid", 
                "End date cannot be before start date");
        }
        
        // Progress validation
        if (task.getProgress() < 0 || task.getProgress() > 100) {
            result.rejectValue("progress", "invalid", 
                "Progress must be between 0 and 100");
        }
        
        // Estimated duration validation
        if (task.getEstimatedDuration() != null && task.getEstimatedDuration().isNegative()) {
            result.rejectValue("estimatedDuration", "invalid", 
                "Estimated duration cannot be negative");
        }
    }
    
    /**
     * Check if current user can edit the specified task.
     */
    private boolean canEditTask(Task task) {
        // TODO: Phase 2B - Implement proper permission checking
        // For Phase 2A, allow all operations for development
        if (isDevelopmentMode()) {
            return true;
        }
        
        // Mentors and admins can edit any task
        if (hasRole("MENTOR") || hasRole("ADMIN")) {
            return true;
        }
        
        // Students can edit tasks assigned to them
        if (hasRole("STUDENT")) {
            // TODO: Check if current user is assigned to this task
            return true; // Temporary for Phase 2A
        }
        
        return false;
    }
    
    /**
     * Check if current user can update task progress.
     */
    private boolean canUpdateTaskProgress(Task task) {
        // TODO: Phase 2B - Implement proper permission checking
        // For Phase 2A, allow all operations for development
        if (isDevelopmentMode()) {
            return true;
        }
        
        // Anyone assigned to the task can update progress
        if (hasRole("STUDENT") || hasRole("MENTOR") || hasRole("ADMIN")) {
            return true; // Temporary for Phase 2A
        }
        
        return false;
    }
    
    /**
     * Helper method to escape CSV values.
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        // Escape quotes and wrap in quotes if necessary
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
}