package org.frcpm.utils;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.*;
import org.frcpm.repositories.RepositoryFactory;
import org.frcpm.repositories.specific.*;
import org.frcpm.services.ServiceFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for initializing the database with schema and seed data.
 */
public class DatabaseInitializer {
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());
    
    /**
     * Initializes the database with schema and optional sample data.
     * 
     * @param createSampleData whether to create sample data
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initialize(boolean createSampleData) {
        LOGGER.info("Initializing database...");
        
        try {
            // Initialize database configuration
            DatabaseConfig.initialize();
            
            // Verify database schema
            verifyDatabaseSchema();
            
            if (createSampleData) {
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
            }
            
            // Log a success message
            LOGGER.info("Database initialization completed successfully");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing database: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Verifies that the database schema exists and is correctly configured.
     * Creates tables if they don't exist.
     * 
     * @throws Exception if verification fails
     */
    private static void verifyDatabaseSchema() throws Exception {
        LOGGER.info("Verifying database schema...");
        
        EntityManager em = null;
        try {
            em = DatabaseConfig.getEntityManager();
            em.getTransaction().begin();
            
            // Check if core tables exist by running queries against each entity
            String[] entityNames = {
                "Project", "Subteam", "TeamMember", "Subsystem", 
                "Task", "Component", "Meeting", "Attendance", "Milestone"
            };
            
            for (String entityName : entityNames) {
                try {
                    Query query = em.createQuery("SELECT COUNT(e) FROM " + entityName + " e");
                    Long count = (Long) query.getSingleResult();
                    LOGGER.info(entityName + " table exists with " + count + " records");
                } catch (Exception e) {
                    LOGGER.warning("Error verifying " + entityName + " table: " + e.getMessage());
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    throw new Exception("Database schema verification failed for " + entityName, e);
                }
            }
            
            em.getTransaction().commit();
            LOGGER.info("Database schema verification completed successfully");
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.severe("Database schema verification failed: " + e.getMessage());
            throw e;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Creates comprehensive sample data for testing and demonstration purposes.
     */
    private static void createSampleData() {
        LOGGER.info("Creating sample data...");
        
        try {
            // Get repositories directly instead of services for more control
            ProjectRepository projectRepo = RepositoryFactory.getProjectRepository();
            SubteamRepository subteamRepo = RepositoryFactory.getSubteamRepository();
            TeamMemberRepository memberRepo = RepositoryFactory.getTeamMemberRepository();
            SubsystemRepository subsystemRepo = RepositoryFactory.getSubsystemRepository();
            TaskRepository taskRepo = RepositoryFactory.getTaskRepository();
            MilestoneRepository milestoneRepo = RepositoryFactory.getMilestoneRepository();
            MeetingRepository meetingRepo = RepositoryFactory.getMeetingRepository();
            AttendanceRepository attendanceRepo = RepositoryFactory.getAttendanceRepository();
            ComponentRepository componentRepo = RepositoryFactory.getComponentRepository();
            
            EntityManager em = DatabaseConfig.getEntityManager();
            try {
                em.getTransaction().begin();
                
                // 1. Create Subteams
                LOGGER.info("Creating subteams...");
                Subteam mechanicalTeam = new Subteam("Mechanical", "#FF5733");
                mechanicalTeam.setSpecialties("Design, CAD, Fabrication, Assembly");
                
                Subteam electricalTeam = new Subteam("Electrical", "#33A8FF");
                electricalTeam.setSpecialties("Wiring, Electronics, Sensors, Power Systems");
                
                Subteam programmingTeam = new Subteam("Programming", "#33FF57");
                programmingTeam.setSpecialties("Java, Vision, Control Systems, Autonomous");
                
                Subteam businessTeam = new Subteam("Business", "#FF33A8");
                businessTeam.setSpecialties("Fundraising, Marketing, Outreach, Awards");
                
                // Save subteams
                subteamRepo.save(mechanicalTeam);
                subteamRepo.save(electricalTeam);
                subteamRepo.save(programmingTeam);
                subteamRepo.save(businessTeam);
                
                // 2. Create Team Members
                LOGGER.info("Creating team members...");
                TeamMember member1 = new TeamMember("jsmith", "John", "Smith", "jsmith@example.com");
                member1.setPhone("555-1234");
                member1.setSkills("Java, Python, Control Systems");
                member1.setLeader(true);
                member1.setSubteam(programmingTeam);
                
                TeamMember member2 = new TeamMember("agarcia", "Ana", "Garcia", "agarcia@example.com");
                member2.setPhone("555-5678");
                member2.setSkills("CAD, Design, Machining");
                member2.setLeader(true);
                member2.setSubteam(mechanicalTeam);
                
                TeamMember member3 = new TeamMember("twong", "Tyler", "Wong", "twong@example.com");
                member3.setPhone("555-9012");
                member3.setSkills("Electronics, Wiring, Soldering");
                member3.setLeader(true);
                member3.setSubteam(electricalTeam);
                
                TeamMember member4 = new TeamMember("lchen", "Lisa", "Chen", "lchen@example.com");
                member4.setPhone("555-3456");
                member4.setSkills("Marketing, Social Media, Presentations");
                member4.setLeader(true);
                member4.setSubteam(businessTeam);
                
                TeamMember member5 = new TeamMember("mjohnson", "Mike", "Johnson", "mjohnson@example.com");
                member5.setPhone("555-7890");
                member5.setSkills("CAD, 3D Printing, Prototyping");
                member5.setLeader(false);
                member5.setSubteam(mechanicalTeam);
                
                TeamMember member6 = new TeamMember("sdavis", "Sarah", "Davis", "sdavis@example.com");
                member6.setPhone("555-2345");
                member6.setSkills("Computer Vision, Machine Learning");
                member6.setLeader(false);
                member6.setSubteam(programmingTeam);
                
                // Save team members
                memberRepo.save(member1);
                memberRepo.save(member2);
                memberRepo.save(member3);
                memberRepo.save(member4);
                memberRepo.save(member5);
                memberRepo.save(member6);
                
                // 3. Create Project
                LOGGER.info("Creating project...");
                LocalDate today = LocalDate.now();
                LocalDate kickoff = today.plusDays(7);
                LocalDate endDate = kickoff.plusWeeks(6);
                LocalDate competition = kickoff.plusWeeks(8);
                
                Project frcProject = new Project(
                    "FRC 2025 Robot", 
                    kickoff, 
                    endDate, 
                    competition
                );
                frcProject.setDescription("Competition robot for the 2025 FIRST Robotics Competition");
                projectRepo.save(frcProject);
                
                // 4. Create Subsystems
                LOGGER.info("Creating subsystems...");
                Subsystem drivetrainSubsystem = new Subsystem("Drivetrain");
                drivetrainSubsystem.setDescription("Robot movement and navigation system");
                drivetrainSubsystem.setStatus(Subsystem.Status.IN_PROGRESS);
                drivetrainSubsystem.setResponsibleSubteam(mechanicalTeam);
                
                Subsystem intakeSubsystem = new Subsystem("Intake");
                intakeSubsystem.setDescription("Mechanism for collecting game pieces");
                intakeSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
                intakeSubsystem.setResponsibleSubteam(mechanicalTeam);
                
                Subsystem shooterSubsystem = new Subsystem("Shooter");
                shooterSubsystem.setDescription("Mechanism for scoring game pieces");
                shooterSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
                shooterSubsystem.setResponsibleSubteam(mechanicalTeam);
                
                Subsystem visionSubsystem = new Subsystem("Vision");
                visionSubsystem.setDescription("Camera and processing system for targeting");
                visionSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
                visionSubsystem.setResponsibleSubteam(programmingTeam);
                
                Subsystem controlsSubsystem = new Subsystem("Controls");
                controlsSubsystem.setDescription("Wiring, electronics, and sensors");
                controlsSubsystem.setStatus(Subsystem.Status.NOT_STARTED);
                controlsSubsystem.setResponsibleSubteam(electricalTeam);
                
                // Save subsystems
                subsystemRepo.save(drivetrainSubsystem);
                subsystemRepo.save(intakeSubsystem);
                subsystemRepo.save(shooterSubsystem);
                subsystemRepo.save(visionSubsystem);
                subsystemRepo.save(controlsSubsystem);
                
                // 5. Create Components
                LOGGER.info("Creating components...");
                Component motorController = new Component("SPARK MAX", "REV-11-2158");
                motorController.setDescription("Motor controller for NEO brushless motors");
                motorController.setExpectedDelivery(kickoff.plusDays(3));
                motorController.setDelivered(false);
                
                Component motor = new Component("NEO Brushless Motor", "REV-21-1650");
                motor.setDescription("High performance brushless DC motor");
                motor.setExpectedDelivery(kickoff.plusDays(3));
                motor.setDelivered(false);
                
                Component wheels = new Component("4\" Omni Wheels", "AM-0016");
                wheels.setDescription("Omni-directional wheels for holonomic drive");
                wheels.setExpectedDelivery(kickoff.plusDays(1));
                wheels.setDelivered(true);
                wheels.setActualDelivery(kickoff);
                
                Component camera = new Component("Limelight 3", "LL3-1");
                camera.setDescription("Smart camera for vision tracking");
                camera.setExpectedDelivery(kickoff.plusWeeks(1));
                camera.setDelivered(false);
                
                Component pneumatics = new Component("Pneumatic Control Module", "CTRE-PCM");
                pneumatics.setDescription("Control module for pneumatic systems");
                pneumatics.setExpectedDelivery(kickoff.plusDays(5));
                pneumatics.setDelivered(false);
                
                // Save components
                componentRepo.save(motorController);
                componentRepo.save(motor);
                componentRepo.save(wheels);
                componentRepo.save(camera);
                componentRepo.save(pneumatics);
                
                // 6. Create Milestones
                LOGGER.info("Creating milestones...");
                Milestone kickoffMilestone = new Milestone("Kickoff", kickoff, frcProject);
                kickoffMilestone.setDescription("Season kickoff - game reveal");
                
                Milestone designReviewMilestone = new Milestone("Design Review", kickoff.plusWeeks(1), frcProject);
                designReviewMilestone.setDescription("Complete robot design review with mentors");
                
                Milestone prototypesMilestone = new Milestone("Prototypes Complete", kickoff.plusWeeks(2), frcProject);
                prototypesMilestone.setDescription("All subsystem prototypes complete and tested");
                
                Milestone integrationMilestone = new Milestone("System Integration", kickoff.plusWeeks(3), frcProject);
                integrationMilestone.setDescription("All subsystems integrated into robot");
                
                Milestone codeMilestone = new Milestone("Code Complete", kickoff.plusWeeks(4), frcProject);
                codeMilestone.setDescription("All robot code complete and tested");
                
                Milestone driverPracticeMilestone = new Milestone("Driver Practice", kickoff.plusWeeks(5), frcProject);
                driverPracticeMilestone.setDescription("Driver practice starts");
                
                Milestone competitionMilestone = new Milestone("Competition", competition, frcProject);
                competitionMilestone.setDescription("First regional competition");
                
                // Save milestones
                milestoneRepo.save(kickoffMilestone);
                milestoneRepo.save(designReviewMilestone);
                milestoneRepo.save(prototypesMilestone);
                milestoneRepo.save(integrationMilestone);
                milestoneRepo.save(codeMilestone);
                milestoneRepo.save(driverPracticeMilestone);
                milestoneRepo.save(competitionMilestone);
                
                // 7. Create Tasks
                LOGGER.info("Creating tasks...");
                // Drivetrain tasks
                Task drivetrainDesignTask = new Task("Drivetrain Design", frcProject, drivetrainSubsystem);
                drivetrainDesignTask.setDescription("Finalize drivetrain design in CAD");
                drivetrainDesignTask.setEstimatedDuration(Duration.ofHours(12));
                drivetrainDesignTask.setPriority(Task.Priority.HIGH);
                drivetrainDesignTask.setStartDate(kickoff.plusDays(1));
                drivetrainDesignTask.setEndDate(kickoff.plusDays(5));
                drivetrainDesignTask.setProgress(50);
                drivetrainDesignTask.assignMember(member2);
                drivetrainDesignTask.assignMember(member5);
                drivetrainDesignTask.addRequiredComponent(wheels);
                drivetrainDesignTask.addRequiredComponent(motor);
                taskRepo.save(drivetrainDesignTask);
                
                Task drivetrainFabricationTask = new Task("Drivetrain Fabrication", frcProject, drivetrainSubsystem);
                drivetrainFabricationTask.setDescription("Cut and machine drivetrain parts");
                drivetrainFabricationTask.setEstimatedDuration(Duration.ofHours(16));
                drivetrainFabricationTask.setPriority(Task.Priority.HIGH);
                drivetrainFabricationTask.setStartDate(kickoff.plusDays(6));
                drivetrainFabricationTask.setEndDate(kickoff.plusDays(10));
                drivetrainFabricationTask.setProgress(0);
                drivetrainFabricationTask.assignMember(member2);
                drivetrainFabricationTask.assignMember(member5);
                drivetrainFabricationTask.addPreDependency(drivetrainDesignTask);
                taskRepo.save(drivetrainFabricationTask);
                
                Task drivetrainCodeTask = new Task("Drivetrain Code", frcProject, drivetrainSubsystem);
                drivetrainCodeTask.setDescription("Create drivetrain control code");
                drivetrainCodeTask.setEstimatedDuration(Duration.ofHours(8));
                drivetrainCodeTask.setPriority(Task.Priority.MEDIUM);
                drivetrainCodeTask.setStartDate(kickoff.plusDays(6));
                drivetrainCodeTask.setEndDate(kickoff.plusDays(12));
                drivetrainCodeTask.setProgress(0);
                drivetrainCodeTask.assignMember(member1);
                drivetrainCodeTask.addPreDependency(drivetrainDesignTask);
                taskRepo.save(drivetrainCodeTask);
                
                // Vision tasks
                Task visionSetupTask = new Task("Vision System Setup", frcProject, visionSubsystem);
                visionSetupTask.setDescription("Set up Limelight camera and calibrate");
                visionSetupTask.setEstimatedDuration(Duration.ofHours(6));
                visionSetupTask.setPriority(Task.Priority.MEDIUM);
                visionSetupTask.setStartDate(kickoff.plusWeeks(1));
                visionSetupTask.setEndDate(kickoff.plusWeeks(1).plusDays(3));
                visionSetupTask.setProgress(0);
                visionSetupTask.assignMember(member6);
                visionSetupTask.addRequiredComponent(camera);
                taskRepo.save(visionSetupTask);
                
                Task visionCodeTask = new Task("Vision Processing Code", frcProject, visionSubsystem);
                visionCodeTask.setDescription("Develop vision processing code for target tracking");
                visionCodeTask.setEstimatedDuration(Duration.ofHours(12));
                visionCodeTask.setPriority(Task.Priority.MEDIUM);
                visionCodeTask.setStartDate(kickoff.plusWeeks(1).plusDays(4));
                visionCodeTask.setEndDate(kickoff.plusWeeks(2).plusDays(2));
                visionCodeTask.setProgress(0);
                visionCodeTask.assignMember(member1);
                visionCodeTask.assignMember(member6);
                visionCodeTask.addPreDependency(visionSetupTask);
                taskRepo.save(visionCodeTask);
                
                // Controls tasks
                Task controlsLayoutTask = new Task("Electronics Layout", frcProject, controlsSubsystem);
                controlsLayoutTask.setDescription("Design electronics layout for the robot");
                controlsLayoutTask.setEstimatedDuration(Duration.ofHours(4));
                controlsLayoutTask.setPriority(Task.Priority.MEDIUM);
                controlsLayoutTask.setStartDate(kickoff.plusDays(5));
                controlsLayoutTask.setEndDate(kickoff.plusDays(8));
                controlsLayoutTask.setProgress(0);
                controlsLayoutTask.assignMember(member3);
                taskRepo.save(controlsLayoutTask);
                
                Task wiringTask = new Task("Robot Wiring", frcProject, controlsSubsystem);
                wiringTask.setDescription("Wire motors, sensors, and control system");
                wiringTask.setEstimatedDuration(Duration.ofHours(10));
                wiringTask.setPriority(Task.Priority.HIGH);
                wiringTask.setStartDate(kickoff.plusWeeks(2));
                wiringTask.setEndDate(kickoff.plusWeeks(2).plusDays(5));
                wiringTask.setProgress(0);
                wiringTask.assignMember(member3);
                wiringTask.addRequiredComponent(motorController);
                wiringTask.addRequiredComponent(pneumatics);
                wiringTask.addPreDependency(drivetrainFabricationTask);
                wiringTask.addPreDependency(controlsLayoutTask);
                taskRepo.save(wiringTask);
                
                // 8. Create Meetings
                LOGGER.info("Creating meetings...");
                Meeting kickoffMeeting = new Meeting(
                    kickoff,
                    LocalTime.of(9, 0), // 9:00 AM
                    LocalTime.of(16, 0), // 4:00 PM
                    frcProject
                );
                kickoffMeeting.setNotes("Season kickoff meeting. Watch game reveal, analyze rules, brainstorm strategies.");
                meetingRepo.save(kickoffMeeting);
                
                Meeting designMeeting = new Meeting(
                    kickoff.plusDays(2),
                    LocalTime.of(18, 0), // 6:00 PM
                    LocalTime.of(21, 0), // 9:00 PM
                    frcProject
                );
                designMeeting.setNotes("Initial design meeting. Subteams will develop preliminary designs.");
                meetingRepo.save(designMeeting);
                
                Meeting buildMeeting = new Meeting(
                    kickoff.plusDays(7),
                    LocalTime.of(18, 0), // 6:00 PM
                    LocalTime.of(21, 0), // 9:00 PM
                    frcProject
                );
                buildMeeting.setNotes("Build kickoff. Start fabrication of initial components.");
                meetingRepo.save(buildMeeting);
                
                // 9. Create Attendance Records
                LOGGER.info("Creating attendance records...");
                // Kickoff attendance
                Attendance att1 = new Attendance(kickoffMeeting, member1, true);
                att1.setArrivalTime(LocalTime.of(9, 0));
                att1.setDepartureTime(LocalTime.of(16, 0));
                attendanceRepo.save(att1);
                
                Attendance att2 = new Attendance(kickoffMeeting, member2, true);
                att2.setArrivalTime(LocalTime.of(9, 0));
                att2.setDepartureTime(LocalTime.of(16, 0));
                attendanceRepo.save(att2);
                
                Attendance att3 = new Attendance(kickoffMeeting, member3, true);
                att3.setArrivalTime(LocalTime.of(9, 15));
                att3.setDepartureTime(LocalTime.of(16, 0));
                attendanceRepo.save(att3);
                
                Attendance att4 = new Attendance(kickoffMeeting, member4, true);
                att4.setArrivalTime(LocalTime.of(9, 0));
                att4.setDepartureTime(LocalTime.of(16, 0));
                attendanceRepo.save(att4);
                
                Attendance att5 = new Attendance(kickoffMeeting, member5, true);
                att5.setArrivalTime(LocalTime.of(9, 30));
                att5.setDepartureTime(LocalTime.of(15, 30));
                attendanceRepo.save(att5);
                
                Attendance att6 = new Attendance(kickoffMeeting, member6, false);
                attendanceRepo.save(att6);
                
                // Design meeting attendance
                Attendance att7 = new Attendance(designMeeting, member1, true);
                att7.setArrivalTime(LocalTime.of(18, 0));
                att7.setDepartureTime(LocalTime.of(21, 0));
                attendanceRepo.save(att7);
                
                Attendance att8 = new Attendance(designMeeting, member2, true);
                att8.setArrivalTime(LocalTime.of(18, 0));
                att8.setDepartureTime(LocalTime.of(21, 0));
                attendanceRepo.save(att8);
                
                Attendance att9 = new Attendance(designMeeting, member3, true);
                att9.setArrivalTime(LocalTime.of(18, 15));
                att9.setDepartureTime(LocalTime.of(20, 30));
                attendanceRepo.save(att9);
                
                Attendance att10 = new Attendance(designMeeting, member4, false);
                attendanceRepo.save(att10);
                
                Attendance att11 = new Attendance(designMeeting, member5, true);
                att11.setArrivalTime(LocalTime.of(18, 0));
                att11.setDepartureTime(LocalTime.of(21, 0));
                attendanceRepo.save(att11);
                
                Attendance att12 = new Attendance(designMeeting, member6, true);
                att12.setArrivalTime(LocalTime.of(18, 30));
                att12.setDepartureTime(LocalTime.of(21, 0));
                attendanceRepo.save(att12);
                
                em.getTransaction().commit();
                LOGGER.info("Sample data created successfully");
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            } finally {
                em.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating sample data: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Performs a quick check to verify database connectivity and configuration.
     * 
     * @return true if database is properly configured, false otherwise
     */
    public static boolean checkDatabaseConnection() {
        LOGGER.info("Checking database connection...");
        
        try {
            // Initialize configuration
            DatabaseConfig.initialize();
            
            // Get an EntityManager and try a simple query
            EntityManager em = DatabaseConfig.getEntityManager();
            try {
                // Try to query metadata
                em.getMetamodel().getEntities();
                LOGGER.info("Database connection successful");
                return true;
            } finally {
                em.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database connection check failed: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates the database schema mode from "create" to "update" for production use.
     * This should be called after the initial database setup is complete.
     * 
     * @return true if the update was successful, false otherwise
     */
    public static boolean switchToUpdateMode() {
        LOGGER.info("Switching database to update mode...");
        
        // In a real implementation, this would modify persistence.xml or configuration properties
        // For now, we'll just log the intent, as this would typically be a one-time operation
        // done manually or by a deployment script
        
        LOGGER.info("To switch to update mode, modify the hibernate.hbm2ddl.auto property in:");
        LOGGER.info("1. DatabaseConfig.java - Change 'create-drop' to 'update'");
        LOGGER.info("2. persistence.xml - Ensure it's set to 'update'");
        
        return true;
    }
}