package org.frcpm.utils;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.*;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.ProjectService;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.logging.Logger;

/**
 * Utility for initializing the database with default data.
 */
public class DatabaseInitializer {
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());
    
    /**
     * Initializes the database with schema and optional sample data.
     * 
     * @param createSampleData whether to create sample data
     */
    public static void initialize(boolean createSampleData) {
        LOGGER.info("Initializing database...");
        
        // Initialize database configuration
        DatabaseConfig.initialize();
        
        if (createSampleData) {
            try {
                // Check if database already has data
                EntityManager em = DatabaseConfig.getEntityManager();
                try {
                    long projectCount = (long) em.createQuery("SELECT COUNT(p) FROM Project p").getSingleResult();
                    
                    if (projectCount == 0) {
                        LOGGER.info("No projects found. Creating sample data...");
                        createSampleData();
                    } else {
                        LOGGER.info("Database already contains " + projectCount + " projects. Skipping sample data creation.");
                    }
                } finally {
                    em.close();
                }
            } catch (Exception e) {
                LOGGER.severe("Error checking database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Creates sample data for testing and demonstration.
     */
    private static void createSampleData() {
        try {
            // Get services
            ProjectService projectService = ServiceFactory.getProjectService();
            SubteamService subteamService = ServiceFactory.getSubteamService();
            TeamMemberService teamMemberService = ServiceFactory.getTeamMemberService();
            
            // Create subteams
            Subteam mechanicalTeam = subteamService.createSubteam("Mechanical", "#FF5733", "Design, fabrication, assembly");
            Subteam electricalTeam = subteamService.createSubteam("Electrical", "#33A8FF", "Wiring, electronics, sensors");
            Subteam programmingTeam = subteamService.createSubteam("Programming", "#33FF57", "Software, control systems, vision");
            
            // Create team members
            TeamMember member1 = teamMemberService.createTeamMember("jsmith", "John", "Smith", "jsmith@example.com", "555-1234", true);
            TeamMember member2 = teamMemberService.createTeamMember("agarcia", "Ana", "Garcia", "agarcia@example.com", "555-5678", false);
            TeamMember member3 = teamMemberService.createTeamMember("twong", "Tyler", "Wong", "twong@example.com", "555-9012", true);
            
            // Assign to subteams
            teamMemberService.assignToSubteam(member1.getId(), mechanicalTeam.getId());
            teamMemberService.assignToSubteam(member2.getId(), electricalTeam.getId());
            teamMemberService.assignToSubteam(member3.getId(), programmingTeam.getId());
            
            // Create project
            LocalDate today = LocalDate.now();
            Project project = projectService.createProject(
                    "FRC 2025 Robot", 
                    today, 
                    today.plusMonths(2), 
                    today.plusMonths(3));
            project.setDescription("Competition robot for the 2025 FIRST Robotics Competition");
            projectService.save(project);
            
            LOGGER.info("Sample data created successfully!");
        } catch (Exception e) {
            LOGGER.severe("Error creating sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}