// src/main/java/org/frcpm/web/controllers/ComponentController.java
// Component Management Controller for FRC Project Management

package org.frcpm.web.controllers;

import org.frcpm.models.Project;
import org.frcpm.security.UserPrincipal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

/**
 * ComponentController - Component Management
 * 
 * Controller for managing FRC robot components and inventory tracking.
 * Handles component lifecycle, inventory management, and integration
 * with project planning.
 * 
 * Features:
 * - Component inventory management
 * - Component status tracking
 * - Integration with projects and tasks
 * - Supplier and ordering management
 * - Component specifications and documentation
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 */
@Controller
@RequestMapping("/components")
@PreAuthorize("isAuthenticated()")
public class ComponentController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(ComponentController.class.getName());
    
    /**
     * Main components listing page.
     * 
     * @param model Spring model for template data
     * @param projectId Optional project filter
     * @param category Optional category filter
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping
    public String listComponents(
            Model model,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "category", required = false) String category,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading components page for user: " + user.getUsername());
            
            // Add navigation data
            addNavigationData(model);
            addBreadcrumbs(model, "Components", "/components");
            
            // Get all projects for filtering
            List<Project> projects = projectService.findAll();
            model.addAttribute("projects", projects);
            
            // Mock component data - in a real implementation, this would come from a ComponentService
            model.addAttribute("components", createMockComponents());
            model.addAttribute("categories", createMockCategories());
            model.addAttribute("selectedCategory", category);
            model.addAttribute("selectedProjectId", projectId);
            
            // Add statistics
            model.addAttribute("totalComponents", 45);
            model.addAttribute("availableComponents", 32);
            model.addAttribute("orderedComponents", 8);
            model.addAttribute("criticalComponents", 5);
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "components");
            
            return "components/list";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading components page: " + e.getMessage());
            addErrorMessage(model, "Error loading components");
            return "error/general";
        }
    }
    
    /**
     * Component details page.
     * 
     * @param id Component ID
     * @param model Spring model for template data
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping("/{id}")
    public String componentDetails(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading component details for ID: " + id);
            
            // Add navigation data
            addNavigationData(model);
            addBreadcrumbs(model, "Components", "/components", "Component Details", "/components/" + id);
            
            // Mock component data
            model.addAttribute("component", createMockComponent(id));
            model.addAttribute("relatedTasks", createMockRelatedTasks());
            model.addAttribute("usageHistory", createMockUsageHistory());
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "components");
            
            return "components/detail";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading component details: " + e.getMessage());
            addErrorMessage(model, "Error loading component details");
            return "error/general";
        }
    }
    
    /**
     * Create new component form.
     * 
     * @param model Spring model for template data
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public String newComponent(
            Model model,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading new component form");
            
            // Add navigation data
            addNavigationData(model);
            addBreadcrumbs(model, "Components", "/components", "New Component", "/components/new");
            
            // Add form data
            model.addAttribute("categories", createMockCategories());
            model.addAttribute("suppliers", createMockSuppliers());
            model.addAttribute("projects", projectService.findAll());
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "components");
            
            return "components/form";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading new component form: " + e.getMessage());
            addErrorMessage(model, "Error loading component form");
            return "error/general";
        }
    }
    
    /**
     * Edit component form.
     * 
     * @param id Component ID
     * @param model Spring model for template data
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public String editComponent(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading edit component form for ID: " + id);
            
            // Add navigation data
            addNavigationData(model);
            addBreadcrumbs(model, "Components", "/components", "Edit Component", "/components/" + id + "/edit");
            
            // Add form data
            model.addAttribute("component", createMockComponent(id));
            model.addAttribute("categories", createMockCategories());
            model.addAttribute("suppliers", createMockSuppliers());
            model.addAttribute("projects", projectService.findAll());
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "components");
            
            return "components/form";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading edit component form: " + e.getMessage());
            addErrorMessage(model, "Error loading component form");
            return "error/general";
        }
    }
    
    /**
     * Component inventory page.
     * 
     * @param model Spring model for template data
     * @param user Current authenticated user
     * @return Template name
     */
    @GetMapping("/inventory")
    public String inventory(
            Model model,
            @AuthenticationPrincipal UserPrincipal user) {
        
        try {
            LOGGER.info("Loading components inventory page");
            
            // Add navigation data
            addNavigationData(model);
            addBreadcrumbs(model, "Components", "/components", "Inventory", "/components/inventory");
            
            // Mock inventory data
            model.addAttribute("inventoryItems", createMockInventory());
            model.addAttribute("lowStockItems", createMockLowStockItems());
            model.addAttribute("reorderSuggestions", createMockReorderSuggestions());
            
            // Add user context
            model.addAttribute("currentUser", user);
            model.addAttribute("currentSection", "components");
            
            return "components/inventory";
            
        } catch (Exception e) {
            LOGGER.severe("Error loading inventory page: " + e.getMessage());
            addErrorMessage(model, "Error loading inventory");
            return "error/general";
        }
    }
    
    // Helper methods for mock data
    private java.util.List<java.util.Map<String, Object>> createMockComponents() {
        return java.util.List.of(
            java.util.Map.of("id", 1L, "name", "Drive Motor", "category", "Motors", "status", "Available", "quantity", 4, "project", "Robot 2024"),
            java.util.Map.of("id", 2L, "name", "Chassis Frame", "category", "Mechanical", "status", "In Use", "quantity", 1, "project", "Robot 2024"),
            java.util.Map.of("id", 3L, "name", "Control System", "category", "Electronics", "status", "Ordered", "quantity", 2, "project", "Robot 2024"),
            java.util.Map.of("id", 4L, "name", "Pneumatic Cylinder", "category", "Pneumatics", "status", "Available", "quantity", 3, "project", "Robot 2024")
        );
    }
    
    private java.util.List<String> createMockCategories() {
        return java.util.List.of("Motors", "Mechanical", "Electronics", "Pneumatics", "Sensors", "Hardware");
    }
    
    private java.util.Map<String, Object> createMockComponent(Long id) {
        java.util.Map<String, Object> component = new java.util.HashMap<>();
        component.put("id", id);
        component.put("name", "Drive Motor");
        component.put("category", "Motors");
        component.put("status", "Available");
        component.put("quantity", 4);
        component.put("description", "High-torque motor for drivetrain");
        component.put("specifications", "12V, 1000 RPM, 10A stall current");
        component.put("supplier", "VEX Robotics");
        component.put("partNumber", "VEX-276-2177");
        component.put("unitCost", 89.99);
        component.put("project", "Robot 2024");
        return component;
    }
    
    private java.util.List<String> createMockSuppliers() {
        return java.util.List.of("VEX Robotics", "AndyMark", "REV Robotics", "FIRST Choice", "McMaster-Carr", "Amazon");
    }
    
    private java.util.List<java.util.Map<String, Object>> createMockRelatedTasks() {
        return java.util.List.of(
            java.util.Map.of("id", 1L, "title", "Install Drive Motors", "status", "In Progress", "assignee", "John Doe"),
            java.util.Map.of("id", 2L, "title", "Test Motor Performance", "status", "Pending", "assignee", "Jane Smith")
        );
    }
    
    private java.util.List<java.util.Map<String, Object>> createMockUsageHistory() {
        return java.util.List.of(
            java.util.Map.of("date", "2024-01-15", "action", "Added to inventory", "user", "Admin", "quantity", 4),
            java.util.Map.of("date", "2024-01-20", "action", "Assigned to task", "user", "John Doe", "quantity", 2)
        );
    }
    
    private java.util.List<java.util.Map<String, Object>> createMockInventory() {
        return java.util.List.of(
            java.util.Map.of("name", "Drive Motor", "currentStock", 4, "minStock", 2, "maxStock", 8, "status", "Good"),
            java.util.Map.of("name", "Chassis Frame", "currentStock", 1, "minStock", 1, "maxStock", 2, "status", "Good"),
            java.util.Map.of("name", "Control System", "currentStock", 0, "minStock", 1, "maxStock", 3, "status", "Low")
        );
    }
    
    private java.util.List<java.util.Map<String, Object>> createMockLowStockItems() {
        return java.util.List.of(
            java.util.Map.of("name", "Control System", "currentStock", 0, "minStock", 1, "urgency", "Critical"),
            java.util.Map.of("name", "Pneumatic Fittings", "currentStock", 2, "minStock", 5, "urgency", "Medium")
        );
    }
    
    private java.util.List<java.util.Map<String, Object>> createMockReorderSuggestions() {
        return java.util.List.of(
            java.util.Map.of("name", "Control System", "suggestedQuantity", 2, "estimatedCost", 150.00, "supplier", "VEX Robotics"),
            java.util.Map.of("name", "Pneumatic Fittings", "suggestedQuantity", 10, "estimatedCost", 25.00, "supplier", "McMaster-Carr")
        );
    }
}