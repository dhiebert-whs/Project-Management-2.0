package org.frcpm.config;

import org.frcpm.models.*;
import org.frcpm.repositories.spring.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Sample data loader for development environment.
 * Loads realistic FRC 2024 CRESCENDO competition data for testing.
 */
@Component
@Profile("development")
public class SampleDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SampleDataLoader.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private ComponentRepository componentRepository;
    
    @Autowired
    private TaskDependencyRepository taskDependencyRepository;
    
    @Autowired
    private SubteamRepository subteamRepository;
    
    @Autowired
    private SubsystemRepository subsystemRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("SampleDataLoader started - forcing reload for debug...");
        long userCount = userRepository.count();
        logger.info("Found {} existing users in database, but forcing reload for debug", userCount);

        logger.info("Loading comprehensive FRC team data for development...");

        // Create Users - Mentors
        User mentorSarah = createUser("mentor_sarah", "sarah@team1234.org", "Sarah", "Johnson", UserRole.MENTOR);
        User mentorMike = createUser("mentor_mike", "mike@team1234.org", "Mike", "Chen", UserRole.MENTOR);

        // Create Users - Students (Mechanical Subteam - 5 students)
        User mechStudent1 = createUser("student_alex", "alex@student.edu", "Alex", "Rivera", UserRole.STUDENT);
        User mechStudent2 = createUser("student_emma", "emma@student.edu", "Emma", "Thompson", UserRole.STUDENT);
        User mechStudent3 = createUser("student_jordan", "jordan@student.edu", "Jordan", "Kim", UserRole.STUDENT);
        User mechStudent4 = createUser("student_taylor", "taylor@student.edu", "Taylor", "Brown", UserRole.STUDENT);
        User mechStudent5 = createUser("student_casey", "casey@student.edu", "Casey", "Davis", UserRole.STUDENT);

        // Create Users - Students (Electrical Subteam - 3 students)
        User elecStudent1 = createUser("student_jamie", "jamie@student.edu", "Jamie", "Wilson", UserRole.STUDENT);
        User elecStudent2 = createUser("student_morgan", "morgan@student.edu", "Morgan", "Garcia", UserRole.STUDENT);
        User elecStudent3 = createUser("student_riley", "riley@student.edu", "Riley", "Martinez", UserRole.STUDENT);

        // Create Users - Students (Programming Subteam - 2 students)
        User progStudent1 = createUser("student_avery", "avery@student.edu", "Avery", "Anderson", UserRole.STUDENT);
        User progStudent2 = createUser("student_blake", "blake@student.edu", "Blake", "Taylor", UserRole.STUDENT);

        // Create Team Members - Mentors
        TeamMember mentorSarahMember = createTeamMember(mentorSarah, "Mechanical Engineering, Project Management, Safety");
        TeamMember mentorMikeMember = createTeamMember(mentorMike, "Electrical Engineering, Programming, Controls");

        // Create Team Members - Mechanical Subteam
        TeamMember alexMember = createTeamMember(mechStudent1, "CAD Design, 3D Printing, Machining");
        TeamMember emmaMember = createTeamMember(mechStudent2, "Fabrication, Welding, Assembly");
        TeamMember jordanMember = createTeamMember(mechStudent3, "CAD Design, Prototyping, Testing");
        TeamMember taylorMember = createTeamMember(mechStudent4, "Machining, Assembly, Quality Control");
        TeamMember caseyMember = createTeamMember(mechStudent5, "Design Analysis, Materials, Documentation");

        // Create Team Members - Electrical Subteam
        TeamMember jamieMember = createTeamMember(elecStudent1, "Wiring, Pneumatics, Sensor Integration");
        TeamMember morganMember = createTeamMember(elecStudent2, "Circuit Design, PCB Layout, Testing");
        TeamMember rileyMember = createTeamMember(elecStudent3, "Motor Controllers, Power Distribution, Troubleshooting");

        // Create Team Members - Programming Subteam
        TeamMember averyMember = createTeamMember(progStudent1, "Robot Code, Autonomous, Vision Processing");
        TeamMember blakeMember = createTeamMember(progStudent2, "Driver Station, Dashboard, Data Logging");

        // Create Project
        Project project = createProject();

        // Create Subteams
        Subteam mechanicalTeam = createSubteam("Mechanical", "Responsible for robot mechanisms, fabrication, and assembly", "#FF6B35");
        Subteam electricalTeam = createSubteam("Electrical", "Responsible for wiring, sensors, and electrical systems", "#F7931E");
        Subteam programmingTeam = createSubteam("Programming", "Responsible for robot code, autonomous, and controls", "#FFD23F");

        // Assign team members to subteams
        assignToSubteam(alexMember, mechanicalTeam);
        assignToSubteam(emmaMember, mechanicalTeam);
        assignToSubteam(jordanMember, mechanicalTeam);
        assignToSubteam(taylorMember, mechanicalTeam);
        assignToSubteam(caseyMember, mechanicalTeam);

        assignToSubteam(jamieMember, electricalTeam);
        assignToSubteam(morganMember, electricalTeam);
        assignToSubteam(rileyMember, electricalTeam);

        assignToSubteam(averyMember, programmingTeam);
        assignToSubteam(blakeMember, programmingTeam);

        // Create Subsystems
        Subsystem drivetrain = createSubsystem(project, "Drivetrain", mechanicalTeam, 
            "Robot mobility system with 6-wheel drop center drive", "#FF6B35");
        Subsystem shooter = createSubsystem(project, "Shooter", mechanicalTeam, 
            "Dual flywheel shooter for scoring notes", "#E74C3C");
        Subsystem intake = createSubsystem(project, "Intake", mechanicalTeam, 
            "Ground intake mechanism for collecting notes", "#8E44AD");

        // Create Components
        org.frcpm.models.Component driveKit = createComponent("West Coast Drive Kit", "WCD-2024", 
            "Complete drivetrain kit with 6-wheel drop center", 
            LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 12), true);
        org.frcpm.models.Component cimMotors = createComponent("CIM Motors (4x)", "CIM-4PK", 
            "Four CIM motors for drivetrain", 
            LocalDate.of(2024, 1, 8), LocalDate.of(2024, 1, 8), true);
        org.frcpm.models.Component neoMotors = createComponent("NEO Motors (2x)", "NEO-2PK", 
            "Two NEO motors for shooter", 
            LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 16), true);
        org.frcpm.models.Component roboRio = createComponent("RoboRIO 2.0", "ROBORIO-2", 
            "Main robot controller", 
            LocalDate.of(2024, 1, 8), LocalDate.of(2024, 1, 8), true);
        org.frcpm.models.Component limeLight = createComponent("LimeLight 3", "LIMELIGHT-3", 
            "Vision processing unit", 
            LocalDate.of(2024, 1, 25), null, false);
        org.frcpm.models.Component intakeWheels = createComponent("Intake Wheels", "INTAKE-WHEELS", 
            "Compliant wheels for note collection", 
            LocalDate.of(2024, 1, 20), LocalDate.of(2024, 1, 22), true);
        org.frcpm.models.Component pneumatics = createComponent("Pneumatic Kit", "PNEUM-KIT", 
            "Air compressor and solenoids for mechanisms", 
            LocalDate.of(2024, 1, 12), LocalDate.of(2024, 1, 14), true);

        // Create comprehensive task structure
        createComprehensiveTasks(project, drivetrain, shooter, intake, 
            mentorSarahMember, mentorMikeMember,
            alexMember, emmaMember, jordanMember, taylorMember, caseyMember,
            jamieMember, morganMember, rileyMember,
            averyMember, blakeMember);

        logger.info("Sample FRC data loaded successfully!");
        logger.info("Created {} users, {} team members, {} projects, {} components",
            userRepository.count(), teamMemberRepository.count(), projectRepository.count(),
            componentRepository.count());
    }

    private User createUser(String username, String email, String firstName, String lastName, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        String encodedPassword = passwordEncoder.encode("password"); // Default password for dev
        user.setPassword(encodedPassword);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setMfaEnabled(false);
        
        logger.info("Creating user: {} with encoded password length: {}", username, encodedPassword.length());
        User savedUser = userRepository.save(user);
        logger.info("Successfully saved user: {} with ID: {}", username, savedUser.getId());
        return savedUser;
    }

    private TeamMember createTeamMember(User user, String skills) {
        TeamMember member = new TeamMember();
        member.setUser(user);
        member.setUsername(user.getUsername());
        member.setFirstName(user.getFirstName());
        member.setLastName(user.getLastName());
        member.setEmail(user.getEmail());
        member.setSkills(skills);
        return teamMemberRepository.save(member);
    }

    private Project createProject() {
        Project project = new Project();
        project.setName("2024 Robot Build - CRESCENDO");
        project.setStartDate(LocalDate.of(2024, 1, 6));
        project.setGoalEndDate(LocalDate.of(2024, 2, 15));
        project.setHardDeadline(LocalDate.of(2024, 2, 19));
        project.setDescription("FRC 2024 CRESCENDO competition robot. Robot must score notes in speaker and amp, climb on stage chains, and work with alliance partners.");
        return projectRepository.save(project);
    }

    private org.frcpm.models.Component createComponent(String name, String partNumber, String description,
                                    LocalDate expectedDelivery, LocalDate actualDelivery, boolean isDelivered) {
        org.frcpm.models.Component component = new org.frcpm.models.Component();
        component.setName(name);
        component.setPartNumber(partNumber);
        component.setDescription(description);
        component.setExpectedDelivery(expectedDelivery);
        component.setActualDelivery(actualDelivery);
        component.setDelivered(isDelivered);
        return componentRepository.save(component);
    }

    private Subteam createSubteam(String name, String description, String color) {
        Subteam subteam = new Subteam();
        subteam.setName(name);
        subteam.setDescription(description);
        subteam.setColor(color);
        return subteamRepository.save(subteam);
    }

    private void assignToSubteam(TeamMember member, Subteam subteam) {
        member.setSubteam(subteam);
        teamMemberRepository.save(member);
    }

    private Subsystem createSubsystem(Project project, String name, Subteam ownerSubteam, 
                                    String description, String color) {
        Subsystem subsystem = new Subsystem();
        subsystem.setProject(project);
        subsystem.setName(name);
        subsystem.setOwnerSubteam(ownerSubteam);
        subsystem.setDescription(description);
        subsystem.setColor(color);
        return subsystemRepository.save(subsystem);
    }

    private void createComprehensiveTasks(Project project, Subsystem drivetrain, Subsystem shooter, Subsystem intake,
                                        TeamMember mentorSarah, TeamMember mentorMike,
                                        TeamMember alex, TeamMember emma, TeamMember jordan, TeamMember taylor, TeamMember casey,
                                        TeamMember jamie, TeamMember morgan, TeamMember riley,
                                        TeamMember avery, TeamMember blake) {
        
        // ===== PHASE 1: PLANNING AND ANALYSIS =====
        Task gameAnalysis = createTask(project, drivetrain, "Game Analysis and Strategy",
            "Analyze CRESCENDO game manual and develop robot strategy", 
            Task.Priority.HIGH, 16, LocalDate.of(2024, 1, 6), LocalDate.of(2024, 1, 8), true, mentorSarah);

        Task designReview = createTask(project, drivetrain, "Initial Design Review",
            "Review game strategy and establish robot requirements", 
            Task.Priority.HIGH, 8, LocalDate.of(2024, 1, 9), LocalDate.of(2024, 1, 10), true, mentorSarah);

        // ===== PHASE 2: DRIVETRAIN DEVELOPMENT =====
        Task drivetrainCAD = createTask(project, drivetrain, "Drivetrain CAD Design",
            "Design 6-wheel drop center drivetrain with motor mounts", 
            Task.Priority.HIGH, 24, LocalDate.of(2024, 1, 11), LocalDate.of(2024, 1, 15), true, alex);

        Task drivetrainFab = createTask(project, drivetrain, "Drivetrain Fabrication",
            "Machine and assemble drivetrain frame and components", 
            Task.Priority.HIGH, 32, LocalDate.of(2024, 1, 16), LocalDate.of(2024, 1, 20), true, emma);

        Task drivetrainWiring = createTask(project, drivetrain, "Drivetrain Electrical",
            "Wire motors, encoders, and motor controllers", 
            Task.Priority.HIGH, 12, LocalDate.of(2024, 1, 21), LocalDate.of(2024, 1, 22), true, jamie);

        Task drivetrainCode = createTask(project, drivetrain, "Drivetrain Programming",
            "Implement drive control with arcade and tank modes", 
            Task.Priority.HIGH, 20, LocalDate.of(2024, 1, 23), LocalDate.of(2024, 1, 25), true, avery);

        // ===== PHASE 3: SHOOTER DEVELOPMENT =====
        Task shooterCAD = createTask(project, shooter, "Shooter CAD Design",
            "Design dual flywheel shooter mechanism with adjustable angle", 
            Task.Priority.HIGH, 28, LocalDate.of(2024, 1, 26), LocalDate.of(2024, 2, 1), false, jordan);

        Task shooterPrototype = createTask(project, shooter, "Shooter Prototyping",
            "Build and test shooter prototype for optimal performance", 
            Task.Priority.HIGH, 24, LocalDate.of(2024, 2, 2), LocalDate.of(2024, 2, 6), false, taylor);

        Task shooterWiring = createTask(project, shooter, "Shooter Electrical",
            "Wire shooter motors, sensors, and pneumatic systems", 
            Task.Priority.HIGH, 16, LocalDate.of(2024, 2, 7), LocalDate.of(2024, 2, 8), false, morgan);

        Task shooterCode = createTask(project, shooter, "Shooter Programming",
            "Implement PID control and vision-assisted targeting", 
            Task.Priority.HIGH, 24, LocalDate.of(2024, 2, 9), LocalDate.of(2024, 2, 12), false, blake);

        // ===== PHASE 4: INTAKE DEVELOPMENT =====
        Task intakeCAD = createTask(project, intake, "Intake CAD Design",
            "Design ground intake with compliant wheels and sensors", 
            Task.Priority.MEDIUM, 20, LocalDate.of(2024, 1, 29), LocalDate.of(2024, 2, 2), false, casey);

        Task intakeFab = createTask(project, intake, "Intake Fabrication",
            "Machine and assemble intake mechanism components", 
            Task.Priority.MEDIUM, 20, LocalDate.of(2024, 2, 5), LocalDate.of(2024, 2, 8), false, emma);

        Task intakeWiring = createTask(project, intake, "Intake Electrical",
            "Wire intake motors, sensors, and limit switches", 
            Task.Priority.MEDIUM, 12, LocalDate.of(2024, 2, 9), LocalDate.of(2024, 2, 10), false, riley);

        Task intakeCode = createTask(project, intake, "Intake Programming",
            "Implement intake control and sensor feedback", 
            Task.Priority.MEDIUM, 16, LocalDate.of(2024, 2, 11), LocalDate.of(2024, 2, 13), false, avery);

        // ===== PHASE 5: INTEGRATION AND TESTING =====
        Task systemIntegration = createTask(project, drivetrain, "System Integration",
            "Integrate all subsystems and test robot functionality", 
            Task.Priority.CRITICAL, 32, LocalDate.of(2024, 2, 14), LocalDate.of(2024, 2, 17), false, mentorMike);

        Task autonomousCode = createTask(project, drivetrain, "Autonomous Programming",
            "Develop autonomous routines for competition", 
            Task.Priority.CRITICAL, 28, LocalDate.of(2024, 2, 15), LocalDate.of(2024, 2, 18), false, blake);

        Task finalTesting = createTask(project, drivetrain, "Final Testing and Tuning",
            "Complete robot testing, debugging, and performance optimization", 
            Task.Priority.CRITICAL, 40, LocalDate.of(2024, 2, 18), LocalDate.of(2024, 2, 19), false, mentorSarah);

        // ===== CREATE TASK DEPENDENCIES =====
        createDependency(gameAnalysis, designReview, DependencyType.FINISH_TO_START, 1,
            "Strategy analysis must be complete before design review");
        createDependency(designReview, drivetrainCAD, DependencyType.FINISH_TO_START, 1,
            "Design requirements needed before CAD work");
        
        // Drivetrain dependencies
        createDependency(drivetrainCAD, drivetrainFab, DependencyType.FINISH_TO_START, 1,
            "CAD must be complete before fabrication");
        createDependency(drivetrainFab, drivetrainWiring, DependencyType.FINISH_TO_START, 1,
            "Hardware must be ready before wiring");
        createDependency(drivetrainWiring, drivetrainCode, DependencyType.FINISH_TO_START, 1,
            "Electrical must be complete before programming");
        
        // Shooter dependencies
        createDependency(drivetrainCode, shooterCAD, DependencyType.FINISH_TO_START, 1,
            "Drivetrain must be functional before shooter design");
        createDependency(shooterCAD, shooterPrototype, DependencyType.FINISH_TO_START, 1,
            "Design needed before prototyping");
        createDependency(shooterPrototype, shooterWiring, DependencyType.FINISH_TO_START, 1,
            "Prototype must be ready for wiring");
        createDependency(shooterWiring, shooterCode, DependencyType.FINISH_TO_START, 1,
            "Electrical before programming");
        
        // Intake dependencies (parallel with shooter)
        createDependency(drivetrainCode, intakeCAD, DependencyType.FINISH_TO_START, 3,
            "Start intake design after drivetrain is working");
        createDependency(intakeCAD, intakeFab, DependencyType.FINISH_TO_START, 1,
            "CAD before fabrication");
        createDependency(intakeFab, intakeWiring, DependencyType.FINISH_TO_START, 1,
            "Hardware before wiring");
        createDependency(intakeWiring, intakeCode, DependencyType.FINISH_TO_START, 1,
            "Wiring before programming");
        
        // Integration dependencies
        createDependency(shooterCode, systemIntegration, DependencyType.FINISH_TO_START, 1,
            "All subsystems must be ready for integration");
        createDependency(intakeCode, systemIntegration, DependencyType.FINISH_TO_START, 1,
            "All subsystems must be ready for integration");
        createDependency(systemIntegration, autonomousCode, DependencyType.START_TO_START, 1,
            "Autonomous development can start with integration");
        createDependency(systemIntegration, finalTesting, DependencyType.FINISH_TO_START, 1,
            "Integration must be complete before final testing");
        createDependency(autonomousCode, finalTesting, DependencyType.FINISH_TO_START, 0,
            "Autonomous code needed for final testing");

        logger.info("Created comprehensive task structure with {} tasks and dependencies", taskRepository.count());
    }

    private Task createTask(Project project, Subsystem subsystem, String title, String description,
                          Task.Priority priority, int estimatedHours, LocalDate startDate, LocalDate endDate,
                          boolean completed, TeamMember assignedTo) {
        Task task = new Task();
        task.setProject(project);
        task.setSubsystem(subsystem);
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setEstimatedDuration(Duration.ofHours(estimatedHours));
        task.setStartDate(startDate);
        task.setEndDate(endDate);
        task.setCompleted(completed);
        
        if (completed) {
            task.setProgress(100);
            task.setActualDuration(Duration.ofHours(estimatedHours + 2));
        } else {
            task.setProgress(0);
        }
        
        // Set up many-to-many relationship
        Set<TeamMember> assignedMembers = new HashSet<>();
        assignedMembers.add(assignedTo);
        task.setAssignedTo(assignedMembers);
        
        return taskRepository.save(task);
    }

    private TaskDependency createDependency(Task predecessor, Task successor, DependencyType type,
                                          int delayDays, String description) {
        TaskDependency dependency = new TaskDependency();
        dependency.setPrerequisiteTask(predecessor);
        dependency.setDependentTask(successor);
        dependency.setDependencyType(type);
        dependency.setLagHours(delayDays * 24);
        dependency.setNotes(description);
        dependency.setActive(true);
        return taskDependencyRepository.save(dependency);
    }
}