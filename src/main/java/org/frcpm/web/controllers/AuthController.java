// File: src/main/java/org/frcpm/web/controllers/AuthController.java
package org.frcpm.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Authentication controller for login and basic routing.
 * 
 * ✅ FIXED: Removed conflicting /dashboard mapping - handled by DashboardController
 */
@Controller
public class AuthController {
    
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
    
    // ✅ REMOVED: /dashboard mapping (now handled by DashboardController)
}