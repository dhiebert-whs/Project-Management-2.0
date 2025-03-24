package org.frcpm.utils;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.models.*;
import org.frcpm.services.*;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for migrating data from Django SQLite database to H2 database.
 */
public class DatabaseMigrationUtil {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseMigrationUtil.class.getName());
    
    // Services
    private final ProjectService projectService;
    private final TaskService taskService;
    private final TeamMemberService teamMemberService;
    private final SubteamService subteamService;
    private final SubsystemService subsystemService;
    private final ComponentService componentService;
    private final MeetingService meetingService;
    private final AttendanceService attendanceService;
    private final MilestoneService milestoneService;
    
    // Source database connection
    private Connection sourceConnection;
    
    // Maps for ID mapping between databases
    private final Map<Long, Long> projectIdMap = new HashMap<>();
    private final Map<Long, Long> taskIdMap = new HashMap<>();
    private final Map<Long, Long> teamMemberIdMap = new HashMap<>();
    private final Map<Long, Long> subteamIdMap = new HashMap<>();
    private final Map<Long, Long> subsystemIdMap = new HashMap<>();
    private final Map<Long, Long> componentIdMap = new HashMap<>();
    private final Map<Long, Long> meetingIdMap = new HashMap<>();
    private final Map<Long, Long> milestoneIdMap = new HashMap<>();
    
    // Progress tracking
    private int totalEntities = 0;
    private int migratedEntities = 0;
    private final List<String> migrationErrors = new ArrayList<>();
    
    /**
     * Creates a new database migration utility.
     */
    public DatabaseMigrationUtil() {
        // Initialize services
        this.projectService = ServiceFactory.getProjectService();
        this.taskService = ServiceFactory.getTaskService();
        this.teamMemberService = ServiceFactory.getTeamMemberService();
        this.subteamService = ServiceFactory.getSubteamService();
        this.subsystemService = ServiceFactory.getSubsystemService();
        this.componentService = ServiceFactory.getComponentService();
        this.meetingService = ServiceFactory.getMeetingService();
        this.attendanceService = ServiceFactory.getAttendanceService();
        this.milestoneService = ServiceFactory.getMilestoneService();
    }
    
    /**
     * Migrates data from a Django SQLite database to the H2 database.
     * 
     * @param sqliteDbPath the path to the SQLite database file
     * @return true if the migration was successful, false otherwise
     */
    public boolean migrateFromSqlite(String sqliteDbPath) {
        LOGGER.info("Starting migration from SQLite database: " + sqliteDbPath);
        
        // Check if the SQLite database file exists
        File sqliteDb = new File(sqliteDbPath);
        if (!sqliteDb.exists() || !sqliteDb.isFile()) {
            LOGGER.severe("SQLite database file not found: " + sqliteDbPath);
            migrationErrors.add("SQLite database file not found: " + sqliteDbPath);
            return false;
        }
        
        try {
            // Connect to the SQLite database
            Class.forName("org.sqlite.JDBC");
            sourceConnection = DriverManager.getConnection("jdbc:sqlite:" + sqliteDbPath);
            
            // Start the migration
            boolean success = migrateData();
            
            // Close the source connection
            if (sourceConnection != null && !sourceConnection.isClosed()) {
                sourceConnection.close();
            }
            
            return success;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "SQLite JDBC driver not found", e);
            migrationErrors.add("SQLite JDBC driver not found: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to SQLite database", e);
            migrationErrors.add("Error connecting to SQLite database: " + e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during migration", e);
            migrationErrors.add("Unexpected error during migration: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Performs the actual data migration.
     * 
     * @return true if the migration was successful, false otherwise
     */
    private boolean migrateData() {
        try {
            // Calculate total entities for progress tracking
            totalEntities = countEntities();
            
            // Migrate entities in the correct order to maintain references
            migrateSubteams();
            migrateTeamMembers();
            migrateProjects();
            migrateSubsystems();
            migrateComponents();
            migrateMilestones();
            migrateTasks();
            migrateMeetings();
            migrateAttendance();
            
            // Migrate relationships that were not handled during entity migration
            migrateTaskDependencies();
            migrateTaskComponents();
            
            // Validate the migration
            boolean validationSuccess = validateMigration();
            
            if (validationSuccess) {
                LOGGER.info("Migration completed successfully");
                return true;
            } else {
                LOGGER.warning("Migration completed with validation errors");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during migration", e);
            migrationErrors.add("Error during migration: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Counts the total number of entities in the source database.
     * 
     * @return the total number of entities
     * @throws SQLException if a database access error occurs
     */
    private int countEntities() throws SQLException {
        int count = 0;
        
        count += countTable("mainapp_subteam");
        count += countTable("auth_user");
        count += countTable("mainapp_project");
        count += countTable("mainapp_subsystem");
        count += countTable("mainapp_component");
        count += countTable("mainapp_milestone");
        count += countTable("mainapp_task");
        count += countTable("mainapp_meeting");
        count += countTable("mainapp_attendance");
        
        return count;
    }
    
    /**
     * Counts the number of rows in a table.
     * 
     * @param tableName the name of the table
     * @return the number of rows in the table
     * @throws SQLException if a database access error occurs
     */
    private int countTable(String tableName) throws SQLException {
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    /**
     * Migrates subteams from the source database.
     * 
     * @throws SQLException if a database access error occurs
     */
    private void migrateSubteams() throws SQLException {
        LOGGER.info("Migrating subteams...");
        
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mainapp_subteam")) {
            
            while (rs.next()) {
                long sourceId = rs.getLong("id");
                String name = rs.getString("name");
                String colorCode = rs.getString("color_code");
                String specialties = rs.getString("specialties");
                
                try {
                    // Create subteam in target database
                    Subteam subteam = subteamService.createSubteam(name, colorCode, specialties);
                    
                    // Map the IDs
                    subteamIdMap.put(sourceId, subteam.getId());
                    
                    migratedEntities++;
                    updateProgress();
                } catch (Exception e) {
                    String error = "Error migrating subteam ID " + sourceId + ": " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
    }
    
    /**
     * Migrates team members from the source database.
     * 
     * @throws SQLException if a database access error occurs
     */
    private void migrateTeamMembers() throws SQLException {
        LOGGER.info("Migrating team members...");
        
        // Step 1: Get users from Django auth_user table
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM auth_user")) {
            
            while (rs.next()) {
                long sourceId = rs.getLong("id");
                String username = rs.getString("username");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                
                try {
                    // Create team member in target database
                    TeamMember member = teamMemberService.createTeamMember(
                        username, firstName, lastName, email, null, false
                    );
                    
                    // Map the IDs
                    teamMemberIdMap.put(sourceId, member.getId());
                    
                    migratedEntities++;
                    updateProgress();
                } catch (Exception e) {
                    String error = "Error migrating team member ID " + sourceId + ": " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
        
        // Step 2: Get additional team member info from profiles and assign to subteams
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT * FROM mainapp_userprofile WHERE user_id IN (" +
                 String.join(",", teamMemberIdMap.keySet().stream().map(String::valueOf).toArray(String[]::new)) +
                 ")"
             )) {
            
            while (rs.next()) {
                long sourceUserId = rs.getLong("user_id");
                long sourceSubteamId = rs.getLong("subteam_id");
                String phone = rs.getString("phone");
                String skills = rs.getString("skills");
                boolean isLeader = rs.getBoolean("is_leader");
                
                try {
                    // Get the mapped IDs
                    Long targetMemberId = teamMemberIdMap.get(sourceUserId);
                    Long targetSubteamId = subteamIdMap.get(sourceSubteamId);
                    
                    if (targetMemberId != null) {
                        TeamMember member = teamMemberService.findById(targetMemberId);
                        
                        // Update member info
                        member = teamMemberService.updateContactInfo(targetMemberId, null, phone);
                        member = teamMemberService.updateSkills(targetMemberId, skills);
                        
                        // Set leader status - requires direct access to the entity
                        member.setLeader(isLeader);
                        teamMemberService.save(member);
                        
                        // Assign to subteam if applicable
                        if (targetSubteamId != null) {
                            teamMemberService.assignToSubteam(targetMemberId, targetSubteamId);
                        }
                    }
                } catch (Exception e) {
                    String error = "Error updating team member ID " + sourceUserId + ": " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
    }
    
    /**
     * Migrates projects from the source database.
     * 
     * @throws SQLException if a database access error occurs
     */
    private void migrateProjects() throws SQLException {
        LOGGER.info("Migrating projects...");
        
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mainapp_project")) {
            
            while (rs.next()) {
                long sourceId = rs.getLong("id");
                String name = rs.getString("name");
                LocalDate startDate = getLocalDate(rs, "start_date");
                LocalDate goalEndDate = getLocalDate(rs, "goal_end_date");
                LocalDate hardDeadline = getLocalDate(rs, "hard_deadline");
                String description = rs.getString("description");
                
                try {
                    // Create project in target database
                    Project project = projectService.createProject(name, startDate, goalEndDate, hardDeadline);
                    project.setDescription(description);
                    project = projectService.save(project);
                    
                    // Map the IDs
                    projectIdMap.put(sourceId, project.getId());
                    
                    migratedEntities++;
                    updateProgress();
                } catch (Exception e) {
                    String error = "Error migrating project ID " + sourceId + ": " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
    }
    
    /**
     * Migrates subsystems from the source database.
     * 
     * @throws SQLException if a database access error occurs
     */
    private void migrateSubsystems() throws SQLException {
        LOGGER.info("Migrating subsystems...");
        
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mainapp_subsystem")) {
            
            while (rs.next()) {
                long sourceId = rs.getLong("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                String status = rs.getString("status");
                Long sourceSubteamId = getLongOrNull(rs, "responsible_subteam_id");
                
                try {
                    // Map status
                    Subsystem.Status subsystemStatus;
                    try {
                        subsystemStatus = Subsystem.Status.valueOf(status);
                    } catch (IllegalArgumentException e) {
                        subsystemStatus = Subsystem.Status.NOT_STARTED;
                    }
                    
                    // Map subteam ID
                    Long targetSubteamId = sourceSubteamId != null ? subteamIdMap.get(sourceSubteamId) : null;
                    
                    // Create subsystem in target database
                    Subsystem subsystem = subsystemService.createSubsystem(
                        name, description, subsystemStatus, targetSubteamId
                    );
                    
                    // Map the IDs
                    subsystemIdMap.put(sourceId, subsystem.getId());
                    
                    migratedEntities++;
                    updateProgress();
                } catch (Exception e) {
                    String error = "Error migrating subsystem ID " + sourceId + ": " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
    }
    
    /**
     * Migrates components from the source database.
     * 
     * @throws SQLException if a database access error occurs
     */
    private void migrateComponents() throws SQLException {
        LOGGER.info("Migrating components...");
        
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mainapp_component")) {
            
            while (rs.next()) {
                long sourceId = rs.getLong("id");
                String name = rs.getString("name");
                String partNumber = rs.getString("part_number");
                String description = rs.getString("description");
                LocalDate expectedDelivery = getLocalDate(rs, "expected_delivery");
                LocalDate actualDelivery = getLocalDate(rs, "actual_delivery");
                boolean isDelivered = rs.getBoolean("is_delivered");
                
                try {
                    // Create component in target database
                    Component component = componentService.createComponent(
                        name, partNumber, description, expectedDelivery
                    );
                    
                    if (isDelivered) {
                        componentService.markAsDelivered(component.getId(), actualDelivery);
                    }
                    
                    // Map the IDs
                    componentIdMap.put(sourceId, component.getId());
                    
                    migratedEntities++;
                    updateProgress();
                } catch (Exception e) {
                    String error = "Error migrating component ID " + sourceId + ": " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
    }
    
    /**
     * Migrates milestones from the source database.
     * 
     * @throws SQLException if a database access error occurs
     */
    private void migrateMilestones() throws SQLException {
        LOGGER.info("Migrating milestones...");
        
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mainapp_milestone")) {
            
            while (rs.next()) {
                long sourceId = rs.getLong("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                LocalDate date = getLocalDate(rs, "date");
                Long sourceProjectId = rs.getLong("project_id");
                
                try {
                    // Map project ID
                    Long targetProjectId = projectIdMap.get(sourceProjectId);
                    
                    if (targetProjectId != null) {
                        // Create milestone in target database
                        Milestone milestone = milestoneService.createMilestone(
                            name, date, targetProjectId, description
                        );
                        
                        // Map the IDs
                        milestoneIdMap.put(sourceId, milestone.getId());
                    } else {
                        String error = "Skipping milestone ID " + sourceId + ": project not found";
                        LOGGER.warning(error);
                        migrationErrors.add(error);
                    }
                    
                    migratedEntities++;
                    updateProgress();
                } catch (Exception e) {
                    String error = "Error migrating milestone ID " + sourceId + ": " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
    }
    
    /**
     * Migrates tasks from the source database.
     * 
     * @throws SQLException if a database access error occurs
     */
    private void migrateTasks() throws SQLException {
        LOGGER.info("Migrating tasks...");
        
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mainapp_task")) {
            
            while (rs.next()) {
                long sourceId = rs.getLong("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                long estimatedDurationSeconds = rs.getLong("estimated_duration_seconds");
                Long actualDurationSeconds = getLongOrNull(rs, "actual_duration_seconds");
                String priority = rs.getString("priority");
                int progress = rs.getInt("progress");
                LocalDate startDate = getLocalDate(rs, "start_date");
                LocalDate endDate = getLocalDate(rs, "end_date");
                boolean completed = rs.getBoolean("completed");
                Long sourceProjectId = rs.getLong("project_id");
                Long sourceSubsystemId = rs.getLong("subsystem_id");
                
                try {
                    // Map project and subsystem IDs
                    Long targetProjectId = projectIdMap.get(sourceProjectId);
                    Long targetSubsystemId = subsystemIdMap.get(sourceSubsystemId);
                    
                    if (targetProjectId != null && targetSubsystemId != null) {
                        Project project = projectService.findById(targetProjectId);
                        Subsystem subsystem = subsystemService.findById(targetSubsystemId);
                        
                        if (project != null && subsystem != null) {
                            // Map priority
                            Task.Priority taskPriority;
                            try {
                                taskPriority = Task.Priority.valueOf(priority);
                            } catch (IllegalArgumentException e) {
                                taskPriority = Task.Priority.MEDIUM;
                            }
                            
                            // Calculate estimated hours
                            double estimatedHours = estimatedDurationSeconds / 3600.0;
                            
                            // Create task in target database
                            Task task = taskService.createTask(
                                title, project, subsystem, estimatedHours, taskPriority, startDate, endDate
                            );
                            
                            // Update additional fields
                            task.setDescription(description);
                            
                            if (actualDurationSeconds != null) {
                                task.setActualDuration(java.time.Duration.ofSeconds(actualDurationSeconds));
                            }
                            
                            task = taskService.updateTaskProgress(task.getId(), progress, completed);
                            
                            // Map the IDs
                            taskIdMap.put(sourceId, task.getId());
                        }
                    } else {
                        String error = "Skipping task ID " + sourceId + ": project or subsystem not found";
                        LOGGER.warning(error);
                        migrationErrors.add(error);
                    }
                    
                    migratedEntities++;
                    updateProgress();
                } catch (Exception e) {
                    String error = "Error migrating task ID " + sourceId + ": " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
        
        // Migrate task assignments
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mainapp_task_assigned_to")) {
            
            while (rs.next()) {
                long sourceTaskId = rs.getLong("task_id");
                long sourceTeamMemberId = rs.getLong("teammember_id");
                
                try {
                    // Map IDs
                    Long targetTaskId = taskIdMap.get(sourceTaskId);
                    Long targetTeamMemberId = teamMemberIdMap.get(sourceTeamMemberId);
                    
                    if (targetTaskId != null && targetTeamMemberId != null) {
                        Task task = taskService.findById(targetTaskId);
                        TeamMember member = teamMemberService.findById(targetTeamMemberId);
                        
                        if (task != null && member != null) {
                            // Add member to task
                            Set<TeamMember> members = new HashSet<>(task.getAssignedTo());
                            members.add(member);
                            taskService.assignMembers(task.getId(), members);
                        }
                    }
                } catch (Exception e) {
                    String error = "Error migrating task assignment (task: " + sourceTaskId + 
                                   ", member: " + sourceTeamMemberId + "): " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
    }
    
    /**
     * Migrates meetings from the source database.
     * 
     * @throws SQLException if a database access error occurs
     */
    private void migrateMeetings() throws SQLException {
        LOGGER.info("Migrating meetings...");
        
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mainapp_meeting")) {
            
            while (rs.next()) {
                long sourceId = rs.getLong("id");
                LocalDate date = getLocalDate(rs, "date");
                LocalTime startTime = getLocalTime(rs, "start_time");
                LocalTime endTime = getLocalTime(rs, "end_time");
                String notes = rs.getString("notes");
                Long sourceProjectId = rs.getLong("project_id");
                
                try {
                    // Map project ID
                    Long targetProjectId = projectIdMap.get(sourceProjectId);
                    
                    if (targetProjectId != null) {
                        // Create meeting in target database
                        Meeting meeting = meetingService.createMeeting(
                            date, startTime, endTime, targetProjectId, notes
                        );
                        
                        // Map the IDs
                        meetingIdMap.put(sourceId, meeting.getId());
                    } else {
                        String error = "Skipping meeting ID " + sourceId + ": project not found";
                        LOGGER.warning(error);
                        migrationErrors.add(error);
                    }
                    
                    migratedEntities++;
                    updateProgress();
                } catch (Exception e) {
                    String error = "Error migrating meeting ID " + sourceId + ": " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
    }
    
    /**
     * Migrates attendance records from the source database.
     * 
     * @throws SQLException if a database access error occurs
     */
    private void migrateAttendance() throws SQLException {
        LOGGER.info("Migrating attendance records...");
        
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mainapp_attendance")) {
            
            while (rs.next()) {
                long sourceId = rs.getLong("id");
                boolean present = rs.getBoolean("present");
                LocalTime arrivalTime = getLocalTime(rs, "arrival_time");
                LocalTime departureTime = getLocalTime(rs, "departure_time");
                Long sourceMeetingId = rs.getLong("meeting_id");
                Long sourceTeamMemberId = rs.getLong("member_id");
                
                try {
                    // Map IDs
                    Long targetMeetingId = meetingIdMap.get(sourceMeetingId);
                    Long targetTeamMemberId = teamMemberIdMap.get(sourceTeamMemberId);
                    
                    if (targetMeetingId != null && targetTeamMemberId != null) {
                        // Create attendance record in target database
                        Attendance attendance = attendanceService.createAttendance(
                            targetMeetingId, targetTeamMemberId, present
                        );
                        
                        if (present && arrivalTime != null && departureTime != null) {
                            attendanceService.updateAttendance(
                                attendance.getId(), present, arrivalTime, departureTime
                            );
                        }
                    } else {
                        String error = "Skipping attendance ID " + sourceId + ": meeting or team member not found";
                        LOGGER.warning(error);
                        migrationErrors.add(error);
                    }
                    
                    migratedEntities++;
                    updateProgress();
                } catch (Exception e) {
                    String error = "Error migrating attendance ID " + sourceId + ": " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
    }
    
    /**
     * Migrates task dependencies from the source database.
     * 
     * @throws SQLException if a database access error occurs
     */
    private void migrateTaskDependencies() throws SQLException {
        LOGGER.info("Migrating task dependencies...");
        
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mainapp_task_pre_dependencies")) {
            
            while (rs.next()) {
                long sourceTaskId = rs.getLong("from_task_id");
                long sourceDependencyId = rs.getLong("to_task_id");
                
                try {
                    // Map IDs
                    Long targetTaskId = taskIdMap.get(sourceTaskId);
                    Long targetDependencyId = taskIdMap.get(sourceDependencyId);
                    
                    if (targetTaskId != null && targetDependencyId != null) {
                        // Add dependency in target database
                        taskService.addDependency(targetTaskId, targetDependencyId);
                    }
                } catch (Exception e) {
                    String error = "Error migrating task dependency (task: " + sourceTaskId + 
                                   ", dependency: " + sourceDependencyId + "): " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
    }
    
    /**
     * Migrates task-component associations from the source database.
     * 
     * @throws SQLException if a database access error occurs
     */
    private void migrateTaskComponents() throws SQLException {
        LOGGER.info("Migrating task-component associations...");
        
        try (Statement stmt = sourceConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mainapp_task_required_components")) {
            
            // Group by task ID
            Map<Long, Set<Long>> taskComponents = new HashMap<>();
            
            while (rs.next()) {
                long sourceTaskId = rs.getLong("task_id");
                long sourceComponentId = rs.getLong("component_id");
                
                taskComponents.computeIfAbsent(sourceTaskId, k -> new HashSet<>())
                              .add(sourceComponentId);
            }
            
            // Process each task's components
            for (Map.Entry<Long, Set<Long>> entry : taskComponents.entrySet()) {
                Long sourceTaskId = entry.getKey();
                Set<Long> sourceComponentIds = entry.getValue();
                
                try {
                    // Map task ID
                    Long targetTaskId = taskIdMap.get(sourceTaskId);
                    
                    if (targetTaskId != null) {
                        // Map component IDs
                        Set<Long> targetComponentIds = new HashSet<>();
                        for (Long sourceComponentId : sourceComponentIds) {
                            Long targetComponentId = componentIdMap.get(sourceComponentId);
                            if (targetComponentId != null) {
                                targetComponentIds.add(targetComponentId);
                            }
                        }
                        
                        if (!targetComponentIds.isEmpty()) {
                            // Associate components with task
                            componentService.associateComponentsWithTask(targetTaskId, targetComponentIds);
                        }
                    }
                } catch (Exception e) {
                    String error = "Error migrating task components for task ID " + sourceTaskId + 
                                   ": " + e.getMessage();
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                }
            }
        }
    }
    
   /**
     * Validates the migration by checking entity counts and relationships.
     * 
     * @return true if the validation was successful, false otherwise
     */
    private boolean validateMigration() {
        LOGGER.info("Validating migration...");
        
        boolean valid = true;
        
        // Check entity counts
        try {
            int sourceProjectCount = countTable("mainapp_project");
            int targetProjectCount = projectIdMap.size();
            
            if (targetProjectCount < sourceProjectCount) {
                String error = "Project count mismatch: " + targetProjectCount + 
                               " migrated out of " + sourceProjectCount;
                LOGGER.warning(error);
                migrationErrors.add(error);
                valid = false;
            }
            
            int sourceTaskCount = countTable("mainapp_task");
            int targetTaskCount = taskIdMap.size();
            
            if (targetTaskCount < sourceTaskCount) {
                String error = "Task count mismatch: " + targetTaskCount + 
                               " migrated out of " + sourceTaskCount;
                LOGGER.warning(error);
                migrationErrors.add(error);
                valid = false;
            }
            
            // Check more entity counts...
            
        } catch (SQLException e) {
            String error = "Error validating entity counts: " + e.getMessage();
            LOGGER.warning(error);
            migrationErrors.add(error);
            valid = false;
        }
        
        // Check critical relationships
        try {
            // Check task-project relationships
            for (Map.Entry<Long, Long> entry : taskIdMap.entrySet()) {
                Task task = taskService.findById(entry.getValue());
                if (task != null && task.getProject() == null) {
                    String error = "Task ID " + entry.getValue() + " has no project";
                    LOGGER.warning(error);
                    migrationErrors.add(error);
                    valid = false;
                }
            }
            
            // Check more relationships...
            
        } catch (Exception e) {
            String error = "Error validating relationships: " + e.getMessage();
            LOGGER.warning(error);
            migrationErrors.add(error);
            valid = false;
        }
        
        return valid;
    }
    
    /**
     * Updates the progress of the migration.
     */
    private void updateProgress() {
        double percentage = (double) migratedEntities / totalEntities * 100;
        LOGGER.info(String.format("Migration progress: %.1f%% (%d/%d entities)", 
                                  percentage, migratedEntities, totalEntities));
    }
    
    /**
     * Gets the migration errors.
     * 
     * @return the list of migration errors
     */
    public List<String> getMigrationErrors() {
        return Collections.unmodifiableList(migrationErrors);
    }
    
    /**
     * Gets the migration progress as a percentage.
     * 
     * @return the migration progress as a percentage
     */
    public double getMigrationProgress() {
        if (totalEntities == 0) {
            return 0;
        }
        return (double) migratedEntities / totalEntities * 100;
    }
    
    /**
     * Gets the number of migrated entities.
     * 
     * @return the number of migrated entities
     */
    public int getMigratedEntities() {
        return migratedEntities;
    }
    
    /**
     * Gets the total number of entities.
     * 
     * @return the total number of entities
     */
    public int getTotalEntities() {
        return totalEntities;
    }
    
    // Helper methods
    
    /**
     * Gets a LocalDate from a ResultSet.
     * 
     * @param rs the ResultSet
     * @param columnName the column name
     * @return the LocalDate, or null if the column is null
     * @throws SQLException if a database access error occurs
     */
    private LocalDate getLocalDate(ResultSet rs, String columnName) throws SQLException {
        String dateStr = rs.getString(columnName);
        if (dateStr == null) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            LOGGER.warning("Error parsing date: " + dateStr);
            return null;
        }
    }
    
    /**
     * Gets a LocalTime from a ResultSet.
     * 
     * @param rs the ResultSet
     * @param columnName the column name
     * @return the LocalTime, or null if the column is null
     * @throws SQLException if a database access error occurs
     */
    private LocalTime getLocalTime(ResultSet rs, String columnName) throws SQLException {
        String timeStr = rs.getString(columnName);
        if (timeStr == null) {
            return null;
        }
        
        try {
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            LOGGER.warning("Error parsing time: " + timeStr);
            return null;
        }
    }
    
    /**
     * Gets a Long from a ResultSet, or null if the column is null.
     * 
     * @param rs the ResultSet
     * @param columnName the column name
     * @return the Long, or null if the column is null
     * @throws SQLException if a database access error occurs
     */
    private Long getLongOrNull(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }
}