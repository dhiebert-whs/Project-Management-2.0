// src/main/java/org/frcpm/web/controllers/ProjectController.java

package org.frcpm.web.controllers;

import org.frcpm.models.Project;
import org.frcpm.models.Task;
import org.frcpm.models.Milestone;
import org.frcpm.services.TaskService;
import org.frcpm.services.MilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Project controller handling all project-related operations.
 * 
 * Features:
 * - Complete CRUD operations for projects
 * - Project overview with tasks and milestones
 * - Project statistics and progress tracking
 * - Project timeline management
 * - Bulk operations and project archival
 * 
 * This controller provides comprehensive project management capabilities,
 * integrating with the proven service layer from Phase 1 to deliver
 * professional project lifecycle management.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2A - Web Controllers Implementation
 */
@Controller
@RequestMapping("/projects")
public class ProjectController extends BaseController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private MilestoneService milestoneService;
    
    // =========================================================================
    // PROJECT LIST AND OVERVIEW
    // =========================================================================
    
    /**
     * Display list of all projects with summary information.
     * 
     * @param model the Spring MVC model
     * @param status optional status filter (active, completed, overdue)
     * @param sort optional sort parameter (name, startDate, deadline)
     * @return projects list view
     */
    @GetMapping
    public String listProjects(Model model,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "sort", required = false, defaultValue = "name") String sort) {
        
        try {
            addNavigationData(model);
            addBreadcrumbs(model, "Projects", "/projects");
            
            // Get all projects
            List<Project> allProjects = projectService.findAll();
            
            // Apply status filter
            List<Project> filteredProjects = filterProjectsByStatus(allProjects, status);
            
            // Apply sorting
            sortProjects(filteredProjects, sort);
            
            // Add to model
            model.addAttribute("projects", filteredProjects);
            model.addAttribute("totalProjects", allProjects.size());
            model.addAttribute("filteredCount", filteredProjects.size());
            model.addAttribute("currentStatus", status != null ? status : "all");
            model.addAttribute("currentSort", sort);
            
            // Add status counts for filter badges
            addProjectStatusCounts(model, allProjects);
            
            // Add quick stats
            addProjectQuickStats(model, allProjects);
            
            return view("projects/list");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading projects list", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Display detailed view of a specific project.
     * 
     * @param id the project ID
     * @param model the model
     * @param tab optional tab selection (overview, tasks, milestones, team)
     * @return project detail view
     */
    @GetMapping("/{id}")
    public String viewProject(@PathVariable Long id, Model model,
                             @RequestParam(value = "tab", required = false, defaultValue = "overview") String tab) {
        
        try {
            Project project = projectService.findById(id);
            if (project == null) {
                addErrorMessage(model, "Project not found");
                return redirect("/projects");
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Projects", "/projects", project.getName(), "/projects/" + id);
            
            // Add project to model
            model.addAttribute("project", project);
            model.addAttribute("activeTab", tab);
            
            // Load project summary
            Map<String, Object> projectSummary = projectService.getProjectSummary(id);
            model.addAttribute("projectSummary", projectSummary);
            
            // Load tab-specific data
            switch (tab.toLowerCase()) {
                case "tasks":
                    loadTasksTabData(model, project);
                    break;
                case "milestones":
                    loadMilestonesTabData(model, project);
                    break;
                case "team":
                    loadTeamTabData(model, project);
                    break;
                case "overview":
                default:
                    loadOverviewTabData(model, project);
                    break;
            }
            
            // Calculate project health indicators
            addProjectHealthIndicators(model, project, projectSummary);
            
            return view("projects/detail");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading project detail", e);
            return handleException(e, model);
        }
    }
    
    // =========================================================================
    // PROJECT CREATION
    // =========================================================================
    
    /**
     * Show new project creation form.
     * 
     * @param model the model
     * @return new project form view
     */
    @GetMapping("/new")
    public String newProjectForm(Model model) {
        try {
            addNavigationData(model);
            addBreadcrumbs(model, "Projects", "/projects", "New Project", "/projects/new");
            
            // Create empty project for form binding
            Project project = new Project();
            
            // Set default dates (start today, goal in 6 weeks, deadline in 8 weeks)
            LocalDate today = LocalDate.now();
            project.setStartDate(today);
            project.setGoalEndDate(today.plusWeeks(6));
            project.setHardDeadline(today.plusWeeks(8));
            
            model.addAttribute("project", project);
            model.addAttribute("isEdit", false);
            
            // Add helpful defaults and suggestions
            addProjectFormHelpers(model);
            
            return view("projects/form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading new project form", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process new project creation.
     * 
     * @param project the project data from form
     * @param result binding result for validation
     * @param model the model
     * @param redirectAttributes for redirect messages
     * @return redirect to project detail or back to form with errors
     */
    @PostMapping("/new")
    public String createProject(@Valid @ModelAttribute Project project,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        try {
            // Validate form data
            validateProjectDates(project, result);
            
            if (result.hasErrors()) {
                addNavigationData(model);
                addBreadcrumbs(model, "Projects", "/projects", "New Project", "/projects/new");
                model.addAttribute("isEdit", false);
                addProjectFormHelpers(model);
                addErrorMessage(model, "Please correct the errors below");
                return view("projects/form");
            }
            
            // Create the project using the service
            Project savedProject = projectService.createProject(
                project.getName(),
                project.getStartDate(),
                project.getGoalEndDate(),
                project.getHardDeadline()
            );
            
            // Update description if provided
            if (project.getDescription() != null && !project.getDescription().trim().isEmpty()) {
                savedProject.setDescription(project.getDescription());
                savedProject = projectService.save(savedProject);
            }
            
            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", 
                "Project '" + savedProject.getName() + "' created successfully!");
            
            // Redirect to the new project
            return redirect("/projects/" + savedProject.getId());
            
        } catch (IllegalArgumentException e) {
            addNavigationData(model);
            addBreadcrumbs(model, "Projects", "/projects", "New Project", "/projects/new");
            model.addAttribute("isEdit", false);
            addProjectFormHelpers(model);
            addErrorMessage(model, e.getMessage());
            return view("projects/form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating project", e);
            return handleException(e, model);
        }
    }
    
    // =========================================================================
    // PROJECT EDITING
    // =========================================================================
    
    /**
     * Show project edit form.
     * 
     * @param id the project ID
     * @param model the model
     * @return edit project form view
     */
    @GetMapping("/{id}/edit")
    public String editProjectForm(@PathVariable Long id, Model model) {
        try {
            Project project = projectService.findById(id);
            if (project == null) {
                addErrorMessage(model, "Project not found");
                return redirect("/projects");
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Projects", "/projects", 
                          project.getName(), "/projects/" + id,
                          "Edit", "/projects/" + id + "/edit");
            
            model.addAttribute("project", project);
            model.addAttribute("isEdit", true);
            
            // Add form helpers and warnings
            addProjectFormHelpers(model);
            addEditWarnings(model, project);
            
            return view("projects/form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading project edit form", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process project update.
     * 
     * @param id the project ID
     * @param project the updated project data
     * @param result binding result
     * @param model the model
     * @param redirectAttributes for redirect messages
     * @return redirect to project detail or back to form with errors
     */
    @PostMapping("/{id}/edit")
    public String updateProject(@PathVariable Long id,
                               @Valid @ModelAttribute Project project,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        try {
            // Validate project exists
            Project existingProject = projectService.findById(id);
            if (existingProject == null) {
                addErrorMessage(model, "Project not found");
                return redirect("/projects");
            }
            
            // Validate form data
            validateProjectDates(project, result);
            
            if (result.hasErrors()) {
                addNavigationData(model);
                addBreadcrumbs(model, "Projects", "/projects", 
                              existingProject.getName(), "/projects/" + id,
                              "Edit", "/projects/" + id + "/edit");
                model.addAttribute("isEdit", true);
                addProjectFormHelpers(model);
                addEditWarnings(model, existingProject);
                addErrorMessage(model, "Please correct the errors below");
                return view("projects/form");
            }
            
            // Update the project using the service
            Project updatedProject = projectService.updateProject(
                id,
                project.getName(),
                project.getStartDate(),
                project.getGoalEndDate(),
                project.getHardDeadline(),
                project.getDescription()
            );
            
            if (updatedProject == null) {
                addErrorMessage(model, "Failed to update project");
                return redirect("/projects");
            }
            
            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", 
                "Project '" + updatedProject.getName() + "' updated successfully!");
            
            return redirect("/projects/" + updatedProject.getId());
            
        } catch (IllegalArgumentException e) {
            addNavigationData(model);
            addBreadcrumbs(model, "Projects", "/projects", 
                          project.getName(), "/projects/" + id,
                          "Edit", "/projects/" + id + "/edit");
            model.addAttribute("isEdit", true);
            addProjectFormHelpers(model);
            addErrorMessage(model, e.getMessage());
            return view("projects/form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating project", e);
            return handleException(e, model);
        }
    }
    
    // =========================================================================
    // PROJECT DELETION
    // =========================================================================
    
    /**
     * Show project deletion confirmation.
     * 
     * @param id the project ID
     * @param model the model
     * @return deletion confirmation view
     */
    @GetMapping("/{id}/delete")
    public String confirmDeleteProject(@PathVariable Long id, Model model) {
        try {
            Project project = projectService.findById(id);
            if (project == null) {
                addErrorMessage(model, "Project not found");
                return redirect("/projects");
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Projects", "/projects", 
                          project.getName(), "/projects/" + id,
                          "Delete", "/projects/" + id + "/delete");
            
            model.addAttribute("project", project);
            
            // Add deletion impact information
            addDeletionImpactInfo(model, project);
            
            return view("projects/delete-confirm");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading delete confirmation", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process project deletion.
     * 
     * @param id the project ID
     * @param redirectAttributes for redirect messages
     * @return redirect to projects list
     */
    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Project project = projectService.findById(id);
            if (project == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Project not found");
                return redirect("/projects");
            }
            
            String projectName = project.getName();
            
            // Delete the project
            boolean deleted = projectService.deleteById(id);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Project '" + projectName + "' deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Failed to delete project '" + projectName + "'");
            }
            
            return redirect("/projects");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting project", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error occurred while deleting project");
            return redirect("/projects");
        }
    }
    
    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================
    
    /**
     * Filter projects by status.
     */
    private List<Project> filterProjectsByStatus(List<Project> projects, String status) {
        if (status == null || "all".equals(status)) {
            return projects;
        }
        
        LocalDate today = LocalDate.now();
        return projects.stream().filter(project -> {
            switch (status.toLowerCase()) {
                case "active":
                    return isProjectActive(project, today);
                case "completed":
                    return isProjectCompleted(project, today);
                case "overdue":
                    return isProjectOverdue(project, today);
                case "upcoming":
                    return isProjectUpcoming(project, today);
                default:
                    return true;
            }
        }).toList();
    }
    
    /**
     * Sort projects by specified criteria.
     */
    private void sortProjects(List<Project> projects, String sort) {
        switch (sort.toLowerCase()) {
            case "startdate":
                projects.sort((p1, p2) -> {
                    if (p1.getStartDate() == null) return 1;
                    if (p2.getStartDate() == null) return -1;
                    return p2.getStartDate().compareTo(p1.getStartDate()); // Recent first
                });
                break;
            case "deadline":
                projects.sort((p1, p2) -> {
                    if (p1.getHardDeadline() == null) return 1;
                    if (p2.getHardDeadline() == null) return -1;
                    return p1.getHardDeadline().compareTo(p2.getHardDeadline()); // Soonest first
                });
                break;
            case "name":
            default:
                projects.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
                break;
        }
    }
    
    /**
     * Add project status counts for filter badges.
     */
    private void addProjectStatusCounts(Model model, List<Project> allProjects) {
        LocalDate today = LocalDate.now();
        
        long activeCount = allProjects.stream().filter(p -> isProjectActive(p, today)).count();
        long completedCount = allProjects.stream().filter(p -> isProjectCompleted(p, today)).count();
        long overdueCount = allProjects.stream().filter(p -> isProjectOverdue(p, today)).count();
        long upcomingCount = allProjects.stream().filter(p -> isProjectUpcoming(p, today)).count();
        
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("upcomingCount", upcomingCount);
    }
    
    /**
     * Add quick statistics for projects overview.
     */
    private void addProjectQuickStats(Model model, List<Project> allProjects) {
        if (allProjects.isEmpty()) {
            model.addAttribute("avgCompletionRate", 0);
            model.addAttribute("projectsOnTrack", 0);
            model.addAttribute("totalTasks", 0);
            return;
        }
        
        // Calculate average completion rate
        double avgCompletion = allProjects.stream()
            .mapToDouble(project -> {
                try {
                    Map<String, Object> summary = projectService.getProjectSummary(project.getId());
                    return (Double) summary.getOrDefault("completionPercentage", 0.0);
                } catch (Exception e) {
                    return 0.0;
                }
            })
            .average()
            .orElse(0.0);
        
        model.addAttribute("avgCompletionRate", Math.round(avgCompletion));
        
        // Count projects on track (not overdue)
        LocalDate today = LocalDate.now();
        long onTrackCount = allProjects.stream()
            .filter(p -> !isProjectOverdue(p, today))
            .count();
        model.addAttribute("projectsOnTrack", onTrackCount);
        
        // Total tasks across all projects
        long totalTasks = allProjects.stream()
            .mapToLong(project -> {
                try {
                    return taskService.findByProject(project).size();
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();
        model.addAttribute("totalTasks", totalTasks);
    }
    
    /**
     * Load overview tab data.
     */
    private void loadOverviewTabData(Model model, Project project) {
        // Recent tasks
        List<Task> recentTasks = taskService.findByProject(project)
            .stream()
            .filter(Task::isCompleted)
            .limit(5)
            .toList();
        model.addAttribute("recentTasks", recentTasks);
        
        // Upcoming tasks
        List<Task> upcomingTasks = taskService.getTasksDueSoon(project.getId(), 14);
        model.addAttribute("upcomingTasks", upcomingTasks);
    }
    
    /**
     * Load tasks tab data.
     */
    private void loadTasksTabData(Model model, Project project) {
        List<Task> allTasks = taskService.findByProject(project);
        model.addAttribute("allTasks", allTasks);
        
        // Group tasks by status
        Map<String, List<Task>> tasksByStatus = allTasks.stream()
            .collect(java.util.stream.Collectors.groupingBy(task -> 
                task.isCompleted() ? "completed" : "pending"));
        
        model.addAttribute("tasksByStatus", tasksByStatus);
    }
    
    /**
     * Load milestones tab data.
     */
    private void loadMilestonesTabData(Model model, Project project) {
        try {
            List<Milestone> milestones = milestoneService.findByProject(project);
            model.addAttribute("milestones", milestones);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load milestones", e);
            model.addAttribute("milestones", List.of());
        }
    }
    
    /**
     * Load team tab data.
     */
    private void loadTeamTabData(Model model, Project project) {
        try {
            // Get all team members working on this project
            List<Task> projectTasks = taskService.findByProject(project);
            var assignedMembers = projectTasks.stream()
                .flatMap(task -> task.getAssignedTo().stream())
                .distinct()
                .toList();
            
            model.addAttribute("assignedMembers", assignedMembers);
            model.addAttribute("memberCount", assignedMembers.size());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load team data", e);
            model.addAttribute("assignedMembers", List.of());
            model.addAttribute("memberCount", 0);
        }
    }
    
    /**
     * Add project health indicators.
     */
    private void addProjectHealthIndicators(Model model, Project project, Map<String, Object> summary) {
        LocalDate today = LocalDate.now();
        
        // Timeline health
        String timelineHealth = "good";
        if (project.getGoalEndDate() != null && project.getGoalEndDate().isBefore(today)) {
            timelineHealth = "critical";
        } else if (project.getGoalEndDate() != null && 
                  project.getGoalEndDate().isBefore(today.plusDays(7))) {
            timelineHealth = "warning";
        }
        model.addAttribute("timelineHealth", timelineHealth);
        
        // Progress health
        int completion = (int) summary.getOrDefault("completionPercentage", 0.0);
        String progressHealth = completion >= 80 ? "good" : 
                              completion >= 50 ? "warning" : "critical";
        model.addAttribute("progressHealth", progressHealth);
        
        // Overall project health
        String overallHealth = "good";
        if ("critical".equals(timelineHealth) || "critical".equals(progressHealth)) {
            overallHealth = "critical";
        } else if ("warning".equals(timelineHealth) || "warning".equals(progressHealth)) {
            overallHealth = "warning";
        }
        model.addAttribute("overallHealth", overallHealth);
    }
    
    /**
     * Validate project dates.
     */
    private void validateProjectDates(Project project, BindingResult result) {
        if (project.getStartDate() == null) {
            result.rejectValue("startDate", "required", "Start date is required");
        }
        
        if (project.getGoalEndDate() == null) {
            result.rejectValue("goalEndDate", "required", "Goal end date is required");
        }
        
        if (project.getHardDeadline() == null) {
            result.rejectValue("hardDeadline", "required", "Hard deadline is required");
        }
        
        if (project.getStartDate() != null && project.getGoalEndDate() != null &&
            project.getGoalEndDate().isBefore(project.getStartDate())) {
            result.rejectValue("goalEndDate", "invalid", 
                "Goal end date cannot be before start date");
        }
        
        if (project.getStartDate() != null && project.getHardDeadline() != null &&
            project.getHardDeadline().isBefore(project.getStartDate())) {
            result.rejectValue("hardDeadline", "invalid", 
                "Hard deadline cannot be before start date");
        }
    }
    
    /**
     * Add project form helpers (defaults, suggestions, etc.).
     */
    private void addProjectFormHelpers(Model model) {
        model.addAttribute("suggestedDurations", List.of(
            Map.of("name", "Sprint (2 weeks)", "weeks", 2),
            Map.of("name", "Mini Project (4 weeks)", "weeks", 4),
            Map.of("name", "Build Season (6 weeks)", "weeks", 6),
            Map.of("name", "Competition Prep (8 weeks)", "weeks", 8),
            Map.of("name", "Off-Season Project (12 weeks)", "weeks", 12)
        ));
    }
    
    /**
     * Add edit warnings for existing projects.
     */
    private void addEditWarnings(Model model, Project project) {
        try {
            List<Task> projectTasks = taskService.findByProject(project);
            if (!projectTasks.isEmpty()) {
                model.addAttribute("hasExistingTasks", true);
                model.addAttribute("taskCount", projectTasks.size());
                
                long completedTasks = projectTasks.stream().filter(Task::isCompleted).count();
                if (completedTasks > 0) {
                    model.addAttribute("hasCompletedTasks", true);
                    addWarningMessage(model, 
                        "This project has " + completedTasks + " completed tasks. " +
                        "Changing dates may affect reporting and metrics.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load edit warnings", e);
        }
    }
    
    /**
     * Add deletion impact information.
     */
    private void addDeletionImpactInfo(Model model, Project project) {
        try {
            List<Task> tasks = taskService.findByProject(project);
            model.addAttribute("taskCount", tasks.size());
            
            long completedTasks = tasks.stream().filter(Task::isCompleted).count();
            model.addAttribute("completedTaskCount", completedTasks);
            
            // Check for milestones
            try {
                List<Milestone> milestones = milestoneService.findByProject(project);
                model.addAttribute("milestoneCount", milestones.size());
            } catch (Exception e) {
                model.addAttribute("milestoneCount", 0);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load deletion impact info", e);
            model.addAttribute("taskCount", 0);
            model.addAttribute("completedTaskCount", 0);
            model.addAttribute("milestoneCount", 0);
        }
    }
    
    // Project status helper methods
    private boolean isProjectActive(Project project, LocalDate today) {
        return project.getStartDate() != null && project.getGoalEndDate() != null &&
               !project.getStartDate().isAfter(today) && !project.getGoalEndDate().isBefore(today);
    }
    
    private boolean isProjectCompleted(Project project, LocalDate today) {
        return project.getGoalEndDate() != null && project.getGoalEndDate().isBefore(today);
    }
    
    private boolean isProjectOverdue(Project project, LocalDate today) {
        return project.getHardDeadline() != null && project.getHardDeadline().isBefore(today);
    }
    
    private boolean isProjectUpcoming(Project project, LocalDate today) {
        return project.getStartDate() != null && project.getStartDate().isAfter(today);
    }
}