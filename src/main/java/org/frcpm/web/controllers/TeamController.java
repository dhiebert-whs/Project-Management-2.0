// src/main/java/org/frcpm/web/controllers/TeamController.java

package org.frcpm.web.controllers;

import org.frcpm.models.Subteam;
import org.frcpm.models.TeamMember;
import org.frcpm.models.Task;
import org.frcpm.services.TaskService;
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
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.logging.Level;

/**
 * Team controller handling comprehensive team member and subteam management.
 * 
 * Features:
 * - Complete CRUD operations for team members
 * - Subteam management and member assignment
 * - Skill tracking and leader designation
 * - Contact information management
 * - Team member search and filtering
 * - Performance analytics and reporting
 * - CSV export capabilities
 * - Team organization and role management
 * 
 * Following the proven patterns established in TaskController and ProjectController
 * with comprehensive error handling and professional user experience.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2A - Web Controllers Implementation
 */
@Controller
@RequestMapping("/team")
public class TeamController extends BaseController {
    
    @Autowired
    private TaskService taskService;
    
    // =========================================================================
    // TEAM OVERVIEW AND DASHBOARD
    // =========================================================================
    
    /**
     * Display team overview with members and subteams.
     * 
     * @param model the Spring MVC model
     * @param view optional view type (members, subteams, overview)
     * @param subteamId optional subteam filter
     * @param role optional role filter (leader, student, mentor)
     * @param sort optional sort parameter
     * @return team overview view
     */
    @GetMapping
    public String teamOverview(Model model,
                              @RequestParam(value = "view", required = false, defaultValue = "overview") String view,
                              @RequestParam(value = "subteamId", required = false) Long subteamId,
                              @RequestParam(value = "role", required = false) String role,
                              @RequestParam(value = "sort", required = false, defaultValue = "name") String sort) {
        
        try {
            addNavigationData(model);
            addBreadcrumbs(model, "Team", "/team");
            
            // Load team members and subteams
            List<TeamMember> allMembers = teamMemberService.findAll();
            List<Subteam> allSubteams = subteamService.findAll();
            
            // Apply filters
            List<TeamMember> filteredMembers = filterTeamMembers(allMembers, subteamId, role);
            
            // Apply sorting
            sortTeamMembers(filteredMembers, sort);
            
            // Add to model
            model.addAttribute("teamMembers", filteredMembers);
            model.addAttribute("subteams", allSubteams);
            model.addAttribute("totalMembers", allMembers.size());
            model.addAttribute("filteredCount", filteredMembers.size());
            
            // Current filter values
            model.addAttribute("currentView", view);
            model.addAttribute("currentSubteamId", subteamId);
            model.addAttribute("currentRole", role != null ? role : "all");
            model.addAttribute("currentSort", sort);
            
            // Add statistics
            addTeamStatistics(model, allMembers, allSubteams);
            
            // Add role counts for filter badges
            addRoleCounts(model, allMembers);
            
            // Determine view template based on view parameter
            switch (view.toLowerCase()) {
                case "members":
                    return view("team/members");
                case "subteams":
                    loadSubteamData(model, allSubteams);
                    return view("team/subteams");
                case "overview":
                default:
                    loadOverviewData(model, allMembers, allSubteams);
                    return view("team/overview");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading team overview", e);
            return handleException(e, model);
        }
    }
    
    // =========================================================================
    // TEAM MEMBER MANAGEMENT
    // =========================================================================
    
    /**
     * Display detailed view of a specific team member.
     * 
     * @param id the team member ID
     * @param model the model
     * @return member detail view
     */
    @GetMapping("/members/{id}")
    public String viewMember(@PathVariable Long id, Model model) {
        try {
            TeamMember member = teamMemberService.findById(id);
            if (member == null) {
                addErrorMessage(model, "Team member not found");
                return redirect("/team");
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Team", "/team", 
                          member.getFullName(), "/team/members/" + id);
            
            model.addAttribute("member", member);
            
            // Load member's tasks and performance data
            loadMemberDetailData(model, member);
            
            // Load available actions based on user role
            loadMemberActions(model, member);
            
            return view("team/member-detail");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading member detail", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Show new team member creation form.
     * 
     * @param model the model
     * @param subteamId optional pre-selected subteam
     * @return new member form view
     */
    @GetMapping("/members/new")
    public String newMemberForm(Model model,
                               @RequestParam(value = "subteamId", required = false) Long subteamId) {
        try {
            // Check permissions - only mentors and admins can add members
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                addErrorMessage(model, "Only mentors and admins can add team members");
                return redirect("/team");
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Team", "/team", "New Member", "/team/members/new");
            
            // Create empty member for form binding
            TeamMember member = new TeamMember();
            
            // Pre-select subteam if provided
            if (subteamId != null) {
                Subteam subteam = subteamService.findById(subteamId);
                if (subteam != null) {
                    member.setSubteam(subteam);
                }
            }
            
            model.addAttribute("member", member);
            model.addAttribute("isEdit", false);
            
            // Load form options
            loadMemberFormOptions(model);
            
            // Add helpful defaults
            addMemberFormHelpers(model);
            
            return view("team/member-form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading new member form", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process new team member creation.
     * 
     * @param member the member data from form
     * @param result binding result for validation
     * @param model the model
     * @param redirectAttributes for redirect messages
     * @return redirect to member detail or back to form with errors
     */
    @PostMapping("/members/new")
    public String createMember(@Valid @ModelAttribute TeamMember member,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        try {
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                addErrorMessage(model, "Only mentors and admins can add team members");
                return redirect("/team");
            }
            
            // Validate form data
            validateMemberData(member, result, false);
            
            if (result.hasErrors()) {
                addNavigationData(model);
                addBreadcrumbs(model, "Team", "/team", "New Member", "/team/members/new");
                model.addAttribute("isEdit", false);
                loadMemberFormOptions(model);
                addMemberFormHelpers(model);
                addErrorMessage(model, "Please correct the errors below");
                return view("team/member-form");
            }
            
            // Create the team member using the service
            TeamMember savedMember = teamMemberService.createTeamMember(
                member.getUsername(),
                member.getFirstName(),
                member.getLastName(),
                member.getEmail(),
                member.getPhone(),
                member.isLeader()
            );
            
            // Set skills if provided
            if (member.getSkills() != null && !member.getSkills().trim().isEmpty()) {
                savedMember = teamMemberService.updateSkills(savedMember.getId(), member.getSkills());
            }
            
            // Assign to subteam if selected
            if (member.getSubteam() != null) {
                savedMember = teamMemberService.assignToSubteam(savedMember.getId(), member.getSubteam().getId());
            }
            
            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", 
                "Team member '" + savedMember.getFullName() + "' added successfully!");
            
            // Redirect to the new member
            return redirect("/team/members/" + savedMember.getId());
            
        } catch (IllegalArgumentException e) {
            addNavigationData(model);
            addBreadcrumbs(model, "Team", "/team", "New Member", "/team/members/new");
            model.addAttribute("isEdit", false);
            loadMemberFormOptions(model);
            addMemberFormHelpers(model);
            addErrorMessage(model, e.getMessage());
            return view("team/member-form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating team member", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Show team member edit form.
     * 
     * @param id the member ID
     * @param model the model
     * @return edit member form view
     */
    @GetMapping("/members/{id}/edit")
    public String editMemberForm(@PathVariable Long id, Model model) {
        try {
            TeamMember member = teamMemberService.findById(id);
            if (member == null) {
                addErrorMessage(model, "Team member not found");
                return redirect("/team");
            }
            
            // Check permissions
            if (!canEditMember(member)) {
                addErrorMessage(model, "You don't have permission to edit this team member");
                return redirect("/team/members/" + id);
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Team", "/team", 
                          member.getFullName(), "/team/members/" + id,
                          "Edit", "/team/members/" + id + "/edit");
            
            model.addAttribute("member", member);
            model.addAttribute("isEdit", true);
            
            // Load form options
            loadMemberFormOptions(model);
            
            // Add edit warnings
            addMemberEditWarnings(model, member);
            
            return view("team/member-form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading member edit form", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process team member update.
     * 
     * @param id the member ID
     * @param member the updated member data
     * @param result binding result
     * @param model the model
     * @param redirectAttributes for redirect messages
     * @return redirect to member detail or back to form with errors
     */
    @PostMapping("/members/{id}/edit")
    public String updateMember(@PathVariable Long id,
                              @Valid @ModelAttribute TeamMember member,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        try {
            // Validate member exists
            TeamMember existingMember = teamMemberService.findById(id);
            if (existingMember == null) {
                addErrorMessage(model, "Team member not found");
                return redirect("/team");
            }
            
            // Check permissions
            if (!canEditMember(existingMember)) {
                addErrorMessage(model, "You don't have permission to edit this team member");
                return redirect("/team/members/" + id);
            }
            
            // Validate form data
            validateMemberData(member, result, true);
            
            if (result.hasErrors()) {
                addNavigationData(model);
                addBreadcrumbs(model, "Team", "/team", 
                              existingMember.getFullName(), "/team/members/" + id,
                              "Edit", "/team/members/" + id + "/edit");
                model.addAttribute("isEdit", true);
                loadMemberFormOptions(model);
                addMemberEditWarnings(model, existingMember);
                addErrorMessage(model, "Please correct the errors below");
                return view("team/member-form");
            }
            
            // Update the member properties
            existingMember.setFirstName(member.getFirstName());
            existingMember.setLastName(member.getLastName());
            existingMember.setLeader(member.isLeader());
            
            // Update contact info
            TeamMember updatedMember = teamMemberService.updateContactInfo(
                id, member.getEmail(), member.getPhone());
            
            // Update skills
            if (member.getSkills() != null) {
                updatedMember = teamMemberService.updateSkills(id, member.getSkills());
            }
            
            // Update subteam assignment
            Long newSubteamId = member.getSubteam() != null ? member.getSubteam().getId() : null;
            Long currentSubteamId = existingMember.getSubteam() != null ? existingMember.getSubteam().getId() : null;
            
            if (!java.util.Objects.equals(newSubteamId, currentSubteamId)) {
                updatedMember = teamMemberService.assignToSubteam(id, newSubteamId);
            }
            
            // Save final updates
            if (updatedMember != null) {
                updatedMember.setFirstName(member.getFirstName());
                updatedMember.setLastName(member.getLastName());
                updatedMember.setLeader(member.isLeader());
                updatedMember = teamMemberService.save(updatedMember);
            }
            
            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", 
                "Team member '" + (updatedMember != null ? updatedMember.getFullName() : member.getFullName()) + "' updated successfully!");
            
            return redirect("/team/members/" + id);
            
        } catch (IllegalArgumentException e) {
            addNavigationData(model);
            addBreadcrumbs(model, "Team", "/team", 
                          member.getFirstName() + " " + member.getLastName(), "/team/members/" + id,
                          "Edit", "/team/members/" + id + "/edit");
            model.addAttribute("isEdit", true);
            loadMemberFormOptions(model);
            addErrorMessage(model, e.getMessage());
            return view("team/member-form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating team member", e);
            return handleException(e, model);
        }
    }
    
    // =========================================================================
    // SUBTEAM MANAGEMENT
    // =========================================================================
    
    /**
     * Display detailed view of a specific subteam.
     * 
     * @param id the subteam ID
     * @param model the model
     * @return subteam detail view
     */
    @GetMapping("/subteams/{id}")
    public String viewSubteam(@PathVariable Long id, Model model) {
        try {
            Subteam subteam = subteamService.findById(id);
            if (subteam == null) {
                addErrorMessage(model, "Subteam not found");
                return redirect("/team?view=subteams");
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Team", "/team", 
                          "Subteams", "/team?view=subteams",
                          subteam.getName(), "/team/subteams/" + id);
            
            model.addAttribute("subteam", subteam);
            
            // Load subteam members
            List<TeamMember> members = teamMemberService.findBySubteam(subteam);
            model.addAttribute("members", members);
            
            // Load subteam performance data
            loadSubteamDetailData(model, subteam, members);
            
            return view("team/subteam-detail");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading subteam detail", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Show new subteam creation form.
     * 
     * @param model the model
     * @return new subteam form view
     */
    @GetMapping("/subteams/new")
    public String newSubteamForm(Model model) {
        try {
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                addErrorMessage(model, "Only mentors and admins can create subteams");
                return redirect("/team?view=subteams");
            }
            
            addNavigationData(model);
            addBreadcrumbs(model, "Team", "/team", 
                          "Subteams", "/team?view=subteams",
                          "New Subteam", "/team/subteams/new");
            
            // Create empty subteam for form binding
            Subteam subteam = new Subteam();
            
            // Set default color
            subteam.setColor("#007bff");
            
            model.addAttribute("subteam", subteam);
            model.addAttribute("isEdit", false);
            
            // Add form helpers
            addSubteamFormHelpers(model);
            
            return view("team/subteam-form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading new subteam form", e);
            return handleException(e, model);
        }
    }
    
    /**
     * Process new subteam creation.
     * 
     * @param subteam the subteam data from form
     * @param result binding result for validation
     * @param model the model
     * @param redirectAttributes for redirect messages
     * @return redirect to subteam detail or back to form with errors
     */
    @PostMapping("/subteams/new")
    public String createSubteam(@Valid @ModelAttribute Subteam subteam,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        try {
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                addErrorMessage(model, "Only mentors and admins can create subteams");
                return redirect("/team?view=subteams");
            }
            
            // Validate form data
            validateSubteamData(subteam, result, false);
            
            if (result.hasErrors()) {
                addNavigationData(model);
                addBreadcrumbs(model, "Team", "/team", 
                              "Subteams", "/team?view=subteams",
                              "New Subteam", "/team/subteams/new");
                model.addAttribute("isEdit", false);
                addSubteamFormHelpers(model);
                addErrorMessage(model, "Please correct the errors below");
                return view("team/subteam-form");
            }
            
            // Create the subteam using the service
            Subteam savedSubteam = subteamService.createSubteam(
                subteam.getName(),
                subteam.getColor(),
                subteam.getDescription()
            );
            
            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", 
                "Subteam '" + savedSubteam.getName() + "' created successfully!");
            
            // Redirect to the new subteam
            return redirect("/team/subteams/" + savedSubteam.getId());
            
        } catch (IllegalArgumentException e) {
            addNavigationData(model);
            addBreadcrumbs(model, "Team", "/team", 
                          "Subteams", "/team?view=subteams",
                          "New Subteam", "/team/subteams/new");
            model.addAttribute("isEdit", false);
            addSubteamFormHelpers(model);
            addErrorMessage(model, e.getMessage());
            return view("team/subteam-form");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating subteam", e);
            return handleException(e, model);
        }
    }
    
    // =========================================================================
    // MEMBER ASSIGNMENT AND MANAGEMENT
    // =========================================================================
    
    /**
     * Assign member to subteam (AJAX endpoint).
     * 
     * @param memberId the member ID
     * @param subteamId the subteam ID (null to unassign)
     * @return JSON response
     */
    @PostMapping("/members/{memberId}/assign")
    @ResponseBody
    public Map<String, Object> assignMemberToSubteam(@PathVariable Long memberId,
                                                     @RequestParam(required = false) Long subteamId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                response.put("success", false);
                response.put("message", "Permission denied");
                return response;
            }
            
            TeamMember member = teamMemberService.findById(memberId);
            if (member == null) {
                response.put("success", false);
                response.put("message", "Team member not found");
                return response;
            }
            
            // Assign to subteam
            TeamMember updatedMember = teamMemberService.assignToSubteam(memberId, subteamId);
            
            if (updatedMember != null) {
                response.put("success", true);
                response.put("memberId", updatedMember.getId());
                response.put("memberName", updatedMember.getFullName());
                
                if (updatedMember.getSubteam() != null) {
                    response.put("subteamId", updatedMember.getSubteam().getId());
                    response.put("subteamName", updatedMember.getSubteam().getName());
                    response.put("message", "Member assigned to " + updatedMember.getSubteam().getName());
                } else {
                    response.put("subteamId", null);
                    response.put("subteamName", null);
                    response.put("message", "Member unassigned from subteam");
                }
            } else {
                response.put("success", false);
                response.put("message", "Failed to assign member");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error assigning member to subteam", e);
            response.put("success", false);
            response.put("message", "Error assigning member: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Toggle team member leadership status.
     * 
     * @param id the member ID
     * @param redirectAttributes for redirect messages
     * @return redirect back to referring page
     */
    @PostMapping("/members/{id}/toggle-leadership")
    public String toggleLeadership(@PathVariable Long id,
                                  @RequestHeader(value = "Referer", required = false) String referer,
                                  RedirectAttributes redirectAttributes) {
        
        try {
            // Check permissions
            if (!hasRole("MENTOR") && !hasRole("ADMIN")) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Only mentors and admins can change leadership status");
                return redirect("/team/members/" + id);
            }
            
            TeamMember member = teamMemberService.findById(id);
            if (member == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Team member not found");
                return redirect("/team");
            }
            
            // Toggle leadership status
            member.setLeader(!member.isLeader());
            TeamMember updatedMember = teamMemberService.save(member);
            
            if (updatedMember != null) {
                String statusMessage = updatedMember.isLeader() ? 
                    "Member promoted to leader" : "Leadership status removed";
                redirectAttributes.addFlashAttribute("successMessage", statusMessage);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update leadership status");
            }
            
            // Redirect back to referring page or member detail
            if (referer != null && !referer.isEmpty()) {
                return "redirect:" + referer;
            } else {
                return redirect("/team/members/" + id);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error toggling leadership status", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating leadership status");
            return redirect("/team/members/" + id);
        }
    }
    
    // =========================================================================
    // SEARCH AND EXPORT
    // =========================================================================
    
    /**
     * Search team members (AJAX endpoint).
     * 
     * @param query search query
     * @param limit maximum number of results
     * @return JSON search results
     */
    @GetMapping("/members/search")
    @ResponseBody
    public Map<String, Object> searchMembers(@RequestParam String query,
                                            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (query == null || query.trim().length() < 2) {
                response.put("success", false);
                response.put("message", "Query must be at least 2 characters");
                return response;
            }
            
            List<TeamMember> allMembers = teamMemberService.findAll();
            String searchQuery = query.toLowerCase().trim();
            
            List<Map<String, Object>> results = allMembers.stream()
                .filter(member -> 
                    member.getFullName().toLowerCase().contains(searchQuery) ||
                    member.getUsername().toLowerCase().contains(searchQuery) ||
                    (member.getEmail() != null && member.getEmail().toLowerCase().contains(searchQuery)) ||
                    (member.getSkills() != null && member.getSkills().toLowerCase().contains(searchQuery)) ||
                    (member.getSubteam() != null && member.getSubteam().getName().toLowerCase().contains(searchQuery))
                )
                .limit(limit)
                .map(member -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", member.getId());
                    result.put("name", member.getFullName());
                    result.put("username", member.getUsername());
                    result.put("email", member.getEmail());
                    result.put("isLeader", member.isLeader());
                    result.put("subteam", member.getSubteam() != null ? member.getSubteam().getName() : "Unassigned");
                    result.put("url", "/team/members/" + member.getId());
                    return result;
                })
                .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("results", results);
            response.put("totalFound", results.size());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error searching team members", e);
            response.put("success", false);
            response.put("message", "Error performing search: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Export team members to CSV format.
     * 
     * @param subteamId optional subteam filter
     * @param role optional role filter
     * @param response HTTP response for file download
     */
    @GetMapping("/members/export/csv")
    public void exportMembersToCsv(@RequestParam(required = false) Long subteamId,
                                  @RequestParam(required = false) String role,
                                  HttpServletResponse response) {
        
        try {
            // Set response headers for file download
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"team_members_export.csv\"");
            
            // Get members to export
            List<TeamMember> members = teamMemberService.findAll();
            
            // Apply filters
            members = filterTeamMembers(members, subteamId, role);
            
            // Write CSV headers
            PrintWriter writer = response.getWriter();
            writer.println("ID,Username,First Name,Last Name,Email,Phone,Skills,Subteam,Is Leader,Task Count");
            
            // Write member data
            for (TeamMember member : members) {
                StringBuilder line = new StringBuilder();
                line.append(escapeCSV(member.getId().toString())).append(",");
                line.append(escapeCSV(member.getUsername())).append(",");
                line.append(escapeCSV(member.getFirstName())).append(",");
                line.append(escapeCSV(member.getLastName())).append(",");
                line.append(escapeCSV(member.getEmail())).append(",");
                line.append(escapeCSV(member.getPhone())).append(",");
                line.append(escapeCSV(member.getSkills())).append(",");
                line.append(escapeCSV(member.getSubteam() != null ? member.getSubteam().getName() : "")).append(",");
                line.append(member.isLeader()).append(",");
                
                // Count assigned tasks
                try {
                    int taskCount = member.getAssignedTasks().size();
                    line.append(taskCount);
                } catch (Exception e) {
                    line.append("0");
                }
                
                writer.println(line.toString());
            }
            
            writer.flush();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting team members to CSV", e);
            try {
                response.sendError(500, "Error exporting team members");
            } catch (IOException ioException) {
                LOGGER.log(Level.SEVERE, "Error sending error response", ioException);
            }
        }
    }
    
    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================
    
    /**
     * Filter team members by subteam and role.
     */
    private List<TeamMember> filterTeamMembers(List<TeamMember> members, Long subteamId, String role) {
        return members.stream()
            .filter(member -> subteamId == null || 
                (member.getSubteam() != null && member.getSubteam().getId().equals(subteamId)))
            .filter(member -> filterByRole(member, role))
            .collect(Collectors.toList());
    }
    
    /**
     * Filter member by role.
     */
    private boolean filterByRole(TeamMember member, String role) {
        if (role == null || "all".equals(role)) {
            return true;
        }
        
        switch (role.toLowerCase()) {
            case "leader":
                return member.isLeader();
            case "student":
                return !member.isLeader(); // Assuming non-leaders are students
            case "mentor":
                return member.isLeader(); // Assuming leaders are mentors
            case "unassigned":
                return member.getSubteam() == null;
            default:
                return true;
        }
    }
    
    /**
     * Sort team members by specified criteria.
     */
    private void sortTeamMembers(List<TeamMember> members, String sort) {
        switch (sort.toLowerCase()) {
            case "name":
                members.sort((m1, m2) -> m1.getFullName().compareToIgnoreCase(m2.getFullName()));
                break;
            case "username":
                members.sort((m1, m2) -> m1.getUsername().compareToIgnoreCase(m2.getUsername()));
                break;
            case "subteam":
                members.sort((m1, m2) -> {
                    String s1 = m1.getSubteam() != null ? m1.getSubteam().getName() : "";
                    String s2 = m2.getSubteam() != null ? m2.getSubteam().getName() : "";
                    return s1.compareToIgnoreCase(s2);
                });
                break;
            case "role":
                members.sort((m1, m2) -> Boolean.compare(m2.isLeader(), m1.isLeader())); // Leaders first
                break;
            default:
                // Default: name
                members.sort((m1, m2) -> m1.getFullName().compareToIgnoreCase(m2.getFullName()));
                break;
        }
    }
    
    /**
     * Add team statistics to model.
     */
    private void addTeamStatistics(Model model, List<TeamMember> members, List<Subteam> subteams) {
        // Basic counts
        model.addAttribute("totalMembers", members.size());
        model.addAttribute("totalSubteams", subteams.size());
        
        // Leadership count
        long leaderCount = members.stream().filter(TeamMember::isLeader).count();
        model.addAttribute("leaderCount", leaderCount);
        
        // Unassigned members
        long unassignedCount = members.stream().filter(m -> m.getSubteam() == null).count();
        model.addAttribute("unassignedCount", unassignedCount);
        
        // Average members per subteam
        double avgMembersPerSubteam = subteams.isEmpty() ? 0 : 
            (double) (members.size() - unassignedCount) / subteams.size();
        model.addAttribute("avgMembersPerSubteam", Math.round(avgMembersPerSubteam * 10) / 10.0);
        
        // Skills analysis
        addSkillsAnalysis(model, members);
    }
    
    /**
     * Add skills analysis to model.
     */
    private void addSkillsAnalysis(Model model, List<TeamMember> members) {
        Map<String, Integer> skillCounts = new HashMap<>();
        
        for (TeamMember member : members) {
            if (member.getSkills() != null && !member.getSkills().trim().isEmpty()) {
                String[] skills = member.getSkills().split(",");
                for (String skill : skills) {
                    String trimmedSkill = skill.trim().toLowerCase();
                    if (!trimmedSkill.isEmpty()) {
                        skillCounts.put(trimmedSkill, skillCounts.getOrDefault(trimmedSkill, 0) + 1);
                    }
                }
            }
        }
        
        // Get top 5 skills
        List<Map.Entry<String, Integer>> topSkills = skillCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());
        
        model.addAttribute("topSkills", topSkills);
        model.addAttribute("totalUniqueSkills", skillCounts.size());
    }
    
    /**
     * Add role counts for filter badges.
     */
    private void addRoleCounts(Model model, List<TeamMember> members) {
        long leaderCount = members.stream().filter(TeamMember::isLeader).count();
        long studentCount = members.stream().filter(m -> !m.isLeader()).count();
        long unassignedCount = members.stream().filter(m -> m.getSubteam() == null).count();
        
        model.addAttribute("leaderFilterCount", leaderCount);
        model.addAttribute("studentFilterCount", studentCount);
        model.addAttribute("unassignedFilterCount", unassignedCount);
    }
    
    /**
     * Load overview data for team dashboard.
     */
    private void loadOverviewData(Model model, List<TeamMember> members, List<Subteam> subteams) {
        // Recent additions
        List<TeamMember> recentMembers = members.stream()
            .limit(5) // Assuming newest first, would need created date in real implementation
            .collect(Collectors.toList());
        model.addAttribute("recentMembers", recentMembers);
        
        // Subteam summary
        List<Map<String, Object>> subteamSummary = subteams.stream()
            .map(subteam -> {
                Map<String, Object> summary = new HashMap<>();
                summary.put("subteam", subteam);
                
                List<TeamMember> subteamMembers = teamMemberService.findBySubteam(subteam);
                summary.put("memberCount", subteamMembers.size());
                summary.put("leaderCount", subteamMembers.stream().filter(TeamMember::isLeader).count());
                
                return summary;
            })
            .collect(Collectors.toList());
        model.addAttribute("subteamSummary", subteamSummary);
        
        // Performance metrics
        addPerformanceMetrics(model, members);
    }
    
    /**
     * Load subteam-specific data.
     */
    private void loadSubteamData(Model model, List<Subteam> subteams) {
        List<Map<String, Object>> subteamData = subteams.stream()
            .map(subteam -> {
                Map<String, Object> data = new HashMap<>();
                data.put("subteam", subteam);
                
                List<TeamMember> members = teamMemberService.findBySubteam(subteam);
                data.put("members", members);
                data.put("memberCount", members.size());
                data.put("leaderCount", members.stream().filter(TeamMember::isLeader).count());
                
                // Calculate task statistics for this subteam
                try {
                    int totalTasks = 0;
                    int completedTasks = 0;
                    
                    for (TeamMember member : members) {
                        totalTasks += member.getAssignedTasks().size();
                        completedTasks += (int) member.getAssignedTasks().stream()
                            .filter(Task::isCompleted)
                            .count();
                    }
                    
                    data.put("totalTasks", totalTasks);
                    data.put("completedTasks", completedTasks);
                    data.put("completionRate", totalTasks > 0 ? 
                        Math.round((double) completedTasks / totalTasks * 100) : 0);
                        
                } catch (Exception e) {
                    data.put("totalTasks", 0);
                    data.put("completedTasks", 0);
                    data.put("completionRate", 0);
                }
                
                return data;
            })
            .collect(Collectors.toList());
        
        model.addAttribute("subteamData", subteamData);
    }
    
    /**
     * Load member detail data including tasks and performance.
     */
    private void loadMemberDetailData(Model model, TeamMember member) {
        try {
            // Get assigned tasks
            List<Task> assignedTasks = member.getAssignedTasks().stream().collect(Collectors.toList());
            model.addAttribute("assignedTasks", assignedTasks);
            
            // Task statistics
            int totalTasks = assignedTasks.size();
            int completedTasks = (int) assignedTasks.stream().filter(Task::isCompleted).count();
            int overdueTasks = (int) assignedTasks.stream()
                .filter(task -> !task.isCompleted() && task.getEndDate() != null && 
                               task.getEndDate().isBefore(java.time.LocalDate.now()))
                .count();
            
            model.addAttribute("totalTasks", totalTasks);
            model.addAttribute("completedTasks", completedTasks);
            model.addAttribute("overdueTasks", overdueTasks);
            model.addAttribute("completionRate", totalTasks > 0 ? 
                Math.round((double) completedTasks / totalTasks * 100) : 0);
            
            // Recent task activity
            List<Task> recentTasks = assignedTasks.stream()
                .filter(Task::isCompleted)
                .limit(5)
                .collect(Collectors.toList());
            model.addAttribute("recentCompletedTasks", recentTasks);
            
            // Skills breakdown
            if (member.getSkills() != null && !member.getSkills().trim().isEmpty()) {
                String[] skills = member.getSkills().split(",");
                List<String> skillList = java.util.Arrays.stream(skills)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
                model.addAttribute("skillList", skillList);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load member detail data", e);
            model.addAttribute("assignedTasks", List.of());
            model.addAttribute("totalTasks", 0);
            model.addAttribute("completedTasks", 0);
            model.addAttribute("overdueTasks", 0);
            model.addAttribute("completionRate", 0);
        }
    }
    
    /**
     * Load subteam detail data including performance metrics.
     */
    private void loadSubteamDetailData(Model model, Subteam subteam, List<TeamMember> members) {
        // Leadership information
        List<TeamMember> leaders = members.stream()
            .filter(TeamMember::isLeader)
            .collect(Collectors.toList());
        model.addAttribute("leaders", leaders);
        
        // Task performance
        try {
            int totalTasks = 0;
            int completedTasks = 0;
            int overdueTasks = 0;
            
            for (TeamMember member : members) {
                totalTasks += member.getAssignedTasks().size();
                completedTasks += (int) member.getAssignedTasks().stream()
                    .filter(Task::isCompleted)
                    .count();
                overdueTasks += (int) member.getAssignedTasks().stream()
                    .filter(task -> !task.isCompleted() && task.getEndDate() != null && 
                                   task.getEndDate().isBefore(java.time.LocalDate.now()))
                    .count();
            }
            
            model.addAttribute("subteamTotalTasks", totalTasks);
            model.addAttribute("subteamCompletedTasks", completedTasks);
            model.addAttribute("subteamOverdueTasks", overdueTasks);
            model.addAttribute("subteamCompletionRate", totalTasks > 0 ? 
                Math.round((double) completedTasks / totalTasks * 100) : 0);
                
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load subteam performance data", e);
            model.addAttribute("subteamTotalTasks", 0);
            model.addAttribute("subteamCompletedTasks", 0);
            model.addAttribute("subteamOverdueTasks", 0);
            model.addAttribute("subteamCompletionRate", 0);
        }
        
        // Skills summary for subteam
        Map<String, Integer> subteamSkills = new HashMap<>();
        for (TeamMember member : members) {
            if (member.getSkills() != null && !member.getSkills().trim().isEmpty()) {
                String[] skills = member.getSkills().split(",");
                for (String skill : skills) {
                    String trimmedSkill = skill.trim();
                    if (!trimmedSkill.isEmpty()) {
                        subteamSkills.put(trimmedSkill, subteamSkills.getOrDefault(trimmedSkill, 0) + 1);
                    }
                }
            }
        }
        model.addAttribute("subteamSkills", subteamSkills);
    }
    
    /**
     * Load available actions based on user role.
     */
    private void loadMemberActions(Model model, TeamMember member) {
        // Check what actions current user can perform
        model.addAttribute("canEdit", canEditMember(member));
        model.addAttribute("canDelete", hasRole("MENTOR") || hasRole("ADMIN"));
        model.addAttribute("canAssign", hasRole("MENTOR") || hasRole("ADMIN"));
        model.addAttribute("canChangeLeadership", hasRole("MENTOR") || hasRole("ADMIN"));
        
        // Available subteams for assignment
        if (hasRole("MENTOR") || hasRole("ADMIN")) {
            try {
                List<Subteam> availableSubteams = subteamService.findAll();
                model.addAttribute("availableSubteams", availableSubteams);
            } catch (Exception e) {
                model.addAttribute("availableSubteams", List.of());
            }
        }
    }
    
    /**
     * Load form options for member creation/editing.
     */
    private void loadMemberFormOptions(Model model) {
        try {
            // Subteams
            List<Subteam> subteams = subteamService.findAll();
            model.addAttribute("subteamOptions", subteams);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load member form options", e);
            model.addAttribute("subteamOptions", List.of());
        }
    }
    
    /**
     * Add helpful defaults and suggestions for member forms.
     */
    private void addMemberFormHelpers(Model model) {
        model.addAttribute("commonSkills", List.of(
            "Programming", "CAD Design", "Electronics", "Mechanical Design",
            "3D Printing", "Machining", "Welding", "Project Management",
            "Documentation", "Testing", "Assembly", "Troubleshooting"
        ));
        
        model.addAttribute("usernameHelp", "Username should be unique and easy to remember");
        model.addAttribute("skillsHelp", "Enter skills separated by commas (e.g., Programming, CAD, Electronics)");
    }
    
    /**
     * Add helpful defaults for subteam forms.
     */
    private void addSubteamFormHelpers(Model model) {
        model.addAttribute("colorOptions", List.of(
            Map.of("color", "#007bff", "name", "Blue"),
            Map.of("color", "#28a745", "name", "Green"),
            Map.of("color", "#dc3545", "name", "Red"),
            Map.of("color", "#ffc107", "name", "Yellow"),
            Map.of("color", "#6f42c1", "name", "Purple"),
            Map.of("color", "#fd7e14", "name", "Orange"),
            Map.of("color", "#20c997", "name", "Teal"),
            Map.of("color", "#e83e8c", "name", "Pink")
        ));
        
        model.addAttribute("specialtyExamples", List.of(
            "Software Development & Programming",
            "Mechanical Design & Engineering",
            "Electrical Systems & Controls",
            "Drive Train & Chassis",
            "Game Strategy & Scouting",
            "Business & Outreach"
        ));
    }
    
    /**
     * Add performance metrics for overview.
     */
    private void addPerformanceMetrics(Model model, List<TeamMember> members) {
        try {
            // Most active members (by task count)
            List<Map<String, Object>> topPerformers = members.stream()
                .map(member -> {
                    Map<String, Object> performance = new HashMap<>();
                    performance.put("member", member);
                    performance.put("taskCount", member.getAssignedTasks().size());
                    performance.put("completedCount", (int) member.getAssignedTasks().stream()
                        .filter(Task::isCompleted).count());
                    return performance;
                })
                .sorted((p1, p2) -> Integer.compare((Integer) p2.get("taskCount"), (Integer) p1.get("taskCount")))
                .limit(5)
                .collect(Collectors.toList());
            
            model.addAttribute("topPerformers", topPerformers);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load performance metrics", e);
            model.addAttribute("topPerformers", List.of());
        }
    }
    
    /**
     * Validate member data for creation/editing.
     */
    private void validateMemberData(TeamMember member, BindingResult result, boolean isEdit) {
        // Username validation
        if (member.getUsername() == null || member.getUsername().trim().isEmpty()) {
            result.rejectValue("username", "required", "Username is required");
        } else if (!isEdit) {
            // Check for duplicate username only on creation
            try {
                if (teamMemberService.findByUsername(member.getUsername()).isPresent()) {
                    result.rejectValue("username", "duplicate", "Username already exists");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error checking username uniqueness", e);
            }
        }
        
        // Name validation
        if (member.getFirstName() == null || member.getFirstName().trim().isEmpty()) {
            result.rejectValue("firstName", "required", "First name is required");
        }
        
        if (member.getLastName() == null || member.getLastName().trim().isEmpty()) {
            result.rejectValue("lastName", "required", "Last name is required");
        }
        
        // Email validation (basic)
        if (member.getEmail() != null && !member.getEmail().trim().isEmpty()) {
            String email = member.getEmail().trim();
            if (!email.contains("@") || !email.contains(".")) {
                result.rejectValue("email", "invalid", "Please enter a valid email address");
            }
        }
    }
    
    /**
     * Validate subteam data for creation/editing.
     */
    private void validateSubteamData(Subteam subteam, BindingResult result, boolean isEdit) {
        // Name validation
        if (subteam.getName() == null || subteam.getName().trim().isEmpty()) {
            result.rejectValue("name", "required", "Subteam name is required");
        } else if (!isEdit) {
            // Check for duplicate name only on creation
            try {
                if (subteamService.findByName(subteam.getName()).isPresent()) {
                    result.rejectValue("name", "duplicate", "Subteam name already exists");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error checking subteam name uniqueness", e);
            }
        }
        
        // Color code validation
        if (subteam.getColor() == null || subteam.getColor().trim().isEmpty()) {
            result.rejectValue("colorCode", "required", "Color code is required");
        } else {
            String colorCode = subteam.getColor().trim();
            if (!colorCode.matches("^#[0-9A-Fa-f]{6}$")) {
                result.rejectValue("colorCode", "invalid", "Color code must be in hex format (e.g., #007bff)");
            }
        }
    }
    
    /**
     * Add warnings for member editing.
     */
    private void addMemberEditWarnings(Model model, TeamMember member) {
        try {
            int assignedTasksCount = member.getAssignedTasks().size();
            if (assignedTasksCount > 0) {
                model.addAttribute("hasAssignedTasks", true);
                model.addAttribute("assignedTaskCount", assignedTasksCount);
                
                int completedTasks = (int) member.getAssignedTasks().stream()
                    .filter(Task::isCompleted).count();
                if (completedTasks > 0) {
                    addWarningMessage(model, 
                        "This member has " + completedTasks + " completed tasks. " +
                        "Changing information may affect reporting and metrics.");
                }
            }
            
            if (member.isLeader()) {
                addInfoMessage(model, "This member is currently designated as a team leader.");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load member edit warnings", e);
        }
    }
    
    /**
     * Check if current user can edit the specified member.
     */
    private boolean canEditMember(TeamMember member) {
        // TODO: Phase 2B - Implement proper permission checking
        // For Phase 2A, allow all operations for development
        if (isDevelopmentMode()) {
            return true;
        }
        
        // Mentors and admins can edit any member
        if (hasRole("MENTOR") || hasRole("ADMIN")) {
            return true;
        }
        
        // Students can edit their own profile (would need current user context)
        // TODO: Check if current user is this member
        
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