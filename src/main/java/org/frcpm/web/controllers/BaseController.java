// src/main/java/org/frcpm/web/controllers/BaseController.java

package org.frcpm.web.controllers;

import org.frcpm.services.ProjectService;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Base controller providing common functionality for all web controllers.
 * 
 * This class establishes consistent patterns for:
 * - Common model attributes
 * - Error handling
 * - Service injection
 * - Utility methods
 * 
 * Following the proven composition pattern from Phase 1 service implementations.
 * 
 * @author FRC Project Management Team
 * @version 2.0.0
 * @since Phase 2 - Web Features Implementation
 */
public abstract class BaseController {
    
    protected static final Logger LOGGER = Logger.getLogger(BaseController.class.getName());
    
    // Common date formatter for consistent date display
    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    protected static final DateTimeFormatter DATE_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Core services available to all controllers
    @Autowired
    protected ProjectService projectService;
    
    @Autowired
    protected SubteamService subteamService;
    
    @Autowired
    protected TeamMemberService teamMemberService;
    
    /**
     * Adds common model attributes to all views.
     * This method is automatically called for every request.
     */
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        // Add current date information
        LocalDate today = LocalDate.now();
        model.addAttribute("currentDate", today);
        model.addAttribute("currentDateFormatted", today.format(DATE_FORMATTER));
        model.addAttribute("currentYear", today.getYear());
        
        // Add application information
        model.addAttribute("appName", "FRC Project Management");
        model.addAttribute("appVersion", "2.0.0");
        model.addAttribute("phase", "Phase 2 - Web Features");
        
        // Add navigation flags (will be used by templates)
        model.addAttribute("isDevelopment", isDevelopmentMode());
        model.addAttribute("isProduction", !isDevelopmentMode());
    }
    
    /**
     * Utility method to build view names with consistent prefix.
     * 
     * @param viewName the base view name (e.g., "list", "detail")
     * @return the full view path
     */
    protected String view(String viewName) {
        return viewName; // Templates will be organized in folders by controller
    }
    
    /**
     * Utility method to build redirect URLs.
     * 
     * @param path the redirect path
     * @return the redirect URL
     */
    protected String redirect(String path) {
        return "redirect:" + path;
    }
    
    /**
     * Adds success message to the model.
     * 
     * @param model the model
     * @param message the success message
     */
    protected void addSuccessMessage(Model model, String message) {
        model.addAttribute("successMessage", message);
        model.addAttribute("hasSuccessMessage", true);
    }
    
    /**
     * Adds error message to the model.
     * 
     * @param model the model
     * @param message the error message
     */
    protected void addErrorMessage(Model model, String message) {
        model.addAttribute("errorMessage", message);
        model.addAttribute("hasErrorMessage", true);
    }
    
    /**
     * Adds warning message to the model.
     * 
     * @param model the model
     * @param message the warning message
     */
    protected void addWarningMessage(Model model, String message) {
        model.addAttribute("warningMessage", message);
        model.addAttribute("hasWarningMessage", true);
    }
    
    /**
     * Adds info message to the model.
     * 
     * @param model the model
     * @param message the info message
     */
    protected void addInfoMessage(Model model, String message) {
        model.addAttribute("infoMessage", message);
        model.addAttribute("hasInfoMessage", true);
    }
    
    /**
     * Formats a date for display in templates.
     * 
     * @param date the date to format
     * @return formatted date string, or empty string if date is null
     */
    protected String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }
    
    /**
     * Formats a date for HTML date input fields.
     * 
     * @param date the date to format
     * @return formatted date string for input, or empty string if date is null
     */
    protected String formatDateForInput(LocalDate date) {
        return date != null ? date.format(DATE_INPUT_FORMATTER) : "";
    }
    
    /**
     * Parses a date from HTML date input.
     * 
     * @param dateString the date string from input
     * @return parsed LocalDate, or null if parsing fails
     */
    protected LocalDate parseDateFromInput(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_INPUT_FORMATTER);
        } catch (Exception e) {
            LOGGER.warning("Failed to parse date: " + dateString);
            return null;
        }
    }
    
    /**
     * Validates that required parameters are not null or empty.
     * 
     * @param paramName the parameter name (for error messages)
     * @param paramValue the parameter value
     * @throws IllegalArgumentException if parameter is invalid
     */
    protected void validateRequired(String paramName, String paramValue) {
        if (paramValue == null || paramValue.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " is required");
        }
    }
    
    /**
     * Validates that required parameters are not null.
     * 
     * @param paramName the parameter name (for error messages)
     * @param paramValue the parameter value
     * @throws IllegalArgumentException if parameter is null
     */
    protected void validateRequired(String paramName, Object paramValue) {
        if (paramValue == null) {
            throw new IllegalArgumentException(paramName + " is required");
        }
    }
    
    /**
     * Safely gets a Long ID from a string parameter.
     * 
     * @param idString the ID string
     * @return the parsed Long ID, or null if parsing fails
     */
    protected Long parseId(String idString) {
        if (idString == null || idString.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(idString.trim());
        } catch (NumberFormatException e) {
            LOGGER.warning("Failed to parse ID: " + idString);
            return null;
        }
    }
    
    /**
     * Determines if the application is running in development mode.
     * 
     * @return true if development mode, false otherwise
     */
    protected boolean isDevelopmentMode() {
        // Check for development profile or development indicators
        String activeProfile = System.getProperty("spring.profiles.active", "");
        return activeProfile.contains("development") || activeProfile.contains("dev");
    }
    
    /**
     * Adds pagination information to the model.
     * 
     * @param model the model
     * @param currentPage the current page number (0-based)
     * @param totalItems the total number of items
     * @param itemsPerPage the number of items per page
     */
    protected void addPaginationToModel(Model model, int currentPage, long totalItems, int itemsPerPage) {
        long totalPages = (totalItems + itemsPerPage - 1) / itemsPerPage;
        
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("itemsPerPage", itemsPerPage);
        model.addAttribute("hasPrevious", currentPage > 0);
        model.addAttribute("hasNext", currentPage < totalPages - 1);
        model.addAttribute("previousPage", Math.max(0, currentPage - 1));
        model.addAttribute("nextPage", Math.min(totalPages - 1, currentPage + 1));
    }
    
    /**
     * Handles common exceptions and returns appropriate error view.
     * 
     * @param e the exception
     * @param model the model
     * @return error view name
     */
    protected String handleException(Exception e, Model model) {
        LOGGER.severe("Controller exception: " + e.getMessage());
        
        // Add error details to model
        addErrorMessage(model, "An error occurred: " + e.getMessage());
        model.addAttribute("exceptionType", e.getClass().getSimpleName());
        
        if (isDevelopmentMode()) {
            // In development, show stack trace
            model.addAttribute("stackTrace", getStackTrace(e));
            model.addAttribute("showTechnicalDetails", true);
        }
        
        return "error/general";
    }
    
    /**
     * Gets stack trace as string for development error display.
     * 
     * @param e the exception
     * @return stack trace string
     */
    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Adds breadcrumb navigation to the model.
     * 
     * @param model the model
     * @param breadcrumbs array of breadcrumb items (name, url pairs)
     */
    protected void addBreadcrumbs(Model model, String... breadcrumbs) {
        java.util.List<java.util.Map<String, String>> breadcrumbList = new java.util.ArrayList<>();
        
        // Add Home breadcrumb
        java.util.Map<String, String> home = new java.util.HashMap<>();
        home.put("name", "Dashboard");
        home.put("url", "/dashboard");
        breadcrumbList.add(home);
        
        // Add provided breadcrumbs
        for (int i = 0; i < breadcrumbs.length; i += 2) {
            if (i + 1 < breadcrumbs.length) {
                java.util.Map<String, String> crumb = new java.util.HashMap<>();
                crumb.put("name", breadcrumbs[i]);
                crumb.put("url", breadcrumbs[i + 1]);
                breadcrumbList.add(crumb);
            }
        }
        
        model.addAttribute("breadcrumbs", breadcrumbList);
    }
    
    /**
     * Checks if the current user has the specified role.
     * This is a placeholder for Phase 2B security implementation.
     * 
     * @param role the role to check
     * @return true if user has role (currently always true)
     */
    protected boolean hasRole(String role) {
        // TODO: Phase 2B - Implement proper role checking
        // For now, return true to allow all operations during Phase 2A
        return true;
    }
    
    /**
     * Gets the current user's display name.
     * This is a placeholder for Phase 2B security implementation.
     * 
     * @return current user's name
     */
    protected String getCurrentUserName() {
        // TODO: Phase 2B - Get from Spring Security context
        return isDevelopmentMode() ? "Development User" : "Admin";
    }
    
    /**
     * Adds common navigation data to the model.
     * This includes counts and active flags for navigation elements.
     * 
     * @param model the model
     */
    protected void addNavigationData(Model model) {
        try {
            // Add entity counts for navigation badges
            model.addAttribute("projectCount", projectService.count());
            model.addAttribute("subteamCount", subteamService.count());
            model.addAttribute("memberCount", teamMemberService.count());
            
            // Add current user info
            model.addAttribute("currentUser", getCurrentUserName());
            model.addAttribute("isAdmin", hasRole("ADMIN"));
            model.addAttribute("isMentor", hasRole("MENTOR"));
            model.addAttribute("isStudent", hasRole("STUDENT"));
            
        } catch (Exception e) {
            LOGGER.warning("Failed to load navigation data: " + e.getMessage());
            // Set default values to prevent template errors
            model.addAttribute("projectCount", 0L);
            model.addAttribute("subteamCount", 0L);
            model.addAttribute("memberCount", 0L);
        }
    }
}