// File: src/main/java/org/frcpm/web/controllers/AuthController.java
package org.frcpm.web.controllers;

import org.frcpm.repositories.spring.UserRepository;
import org.frcpm.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authentication controller for login and basic routing.
 * 
 * ✅ FIXED: Removed conflicting /dashboard mapping - handled by DashboardController
 */
@Controller
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
    
    // Debug endpoint to check if users exist
    @GetMapping("/debug/users")
    @ResponseBody
    public String debugUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return "No users found in database!";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("Found ").append(users.size()).append(" users:\n");
        
        for (User user : users) {
            result.append("- ").append(user.getUsername())
                  .append(" (").append(user.getRole()).append(") ")
                  .append("enabled=").append(user.isEnabled())
                  .append(" pwdHash=").append(user.getPassword().substring(0, Math.min(20, user.getPassword().length())))
                  .append("...\n");
        }
        
        return result.toString();
    }
    
    // ✅ REMOVED: /dashboard mapping (now handled by DashboardController)
}