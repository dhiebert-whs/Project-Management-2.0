package org.frcpm.test.util;

import org.frcpm.models.*;
import org.frcpm.services.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for generating test data for the FRC Project Management System.
 */
public class TestDataGenerator {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final TeamMemberService teamMemberService;
    private final SubteamService subteamService;
    private final SubsystemService subsystemService;
    private final MeetingService meetingService;
    private final ComponentService componentService;
    private final MilestoneService milestoneService;
    private final AttendanceService attendanceService;

    public TestDataGenerator() {
        this.projectService = ServiceFactory.getProjectService();
        this.taskService = ServiceFactory.getTaskService();
        this.teamMemberService = ServiceFactory.getTeamMemberService();
        this.subteamService = ServiceFactory.getSubteamService();
        this.subsystemService = ServiceFactory.getSubsystemService();
        this.meetingService = ServiceFactory.getMeetingService();
        this.componentService = ServiceFactory.getComponentService();
        this.milestoneService = ServiceFactory.getMilestoneService();
        this.attendanceService = ServiceFactory.getAttendanceService();
    }

    /**
     * Generates a complete test project with subteams, members, subsystems, tasks,
     * and meetings.
     * 
     * @param projectName the name of the test project
     * @return the generated project
     */
    public Project generateTestProject(String projectName) {
        // Create project
        Project project = projectService.createProject(
                projectName,
                LocalDate.now(),
                LocalDate.now().plusWeeks(6),
                LocalDate.now().plusWeeks(8));
        project.setDescription("Test project description for " + projectName);
        projectService.save(project);

        // Create subteams
        Subteam programmingTeam = subteamService.createSubteam(
                "Programming",
                "#3366CC",
                "Java, Vision Processing, Autonomous Routines");

        Subteam mechanicalTeam = subteamService.createSubteam(
                "Mechanical",
                "#CC3333",
                "CAD, Fabrication, Assembly");

        Subteam electricalTeam = subteamService.createSubteam(
                "Electrical",
                "#FFCC00",
                "Wiring, Electronics, Control Systems");

        // Create members
        List<TeamMember> members = new ArrayList<>();

        TeamMember member1 = teamMemberService.createTeamMember(
                "jsmith",
                "John",
                "Smith",
                "jsmith@example.com",
                "555-1234",
                true);
        teamMemberService.assignToSubteam(member1.getId(), programmingTeam.getId());
        members.add(member1);

        TeamMember member2 = teamMemberService.createTeamMember(
                "ajones",
                "Alice",
                "Jones",
                "ajones@example.com",
                "555-5678",
                false);
        teamMemberService.assignToSubteam(member2.getId(), mechanicalTeam.getId());
        members.add(member2);

        TeamMember member3 = teamMemberService.createTeamMember(
                "bwilliams",
                "Bob",
                "Williams",
                "bwilliams@example.com",
                "555-9012",
                false);
        teamMemberService.assignToSubteam(member3.getId(), electricalTeam.getId());
        members.add(member3);

        // Create subsystems
        Subsystem drivetrainSubsystem = subsystemService.createSubsystem(
                "Drivetrain",
                "Tank drive system with 6 wheels",
                Subsystem.Status.IN_PROGRESS,
                mechanicalTeam.getId());

        Subsystem armSubsystem = subsystemService.createSubsystem(
                "Arm",
                "Game piece manipulator arm",
                Subsystem.Status.NOT_STARTED,
                mechanicalTeam.getId());

        Subsystem visionSubsystem = subsystemService.createSubsystem(
                "Vision Processing",
                "Target detection and tracking",
                Subsystem.Status.NOT_STARTED,
                programmingTeam.getId());

        // Create components
        Component motor = componentService.createComponent(
                "CIM Motor",
                "am-0255",
                "AndyMark CIM Motor",
                LocalDate.now().plusDays(3));

        Component encoder = componentService.createComponent(
                "Encoder",
                "am-3314",
                "E4T Optical Encoder",
                LocalDate.now().plusDays(5));

        Component controller = componentService.createComponent(
                "Motor Controller",
                "217-8080",
                "Talon SRX Motor Controller",
                LocalDate.now().plusDays(2));

        // Create tasks
        Task drivetrainDesignTask = createTask(
                "Design Drivetrain",
                project,
                drivetrainSubsystem,
                10.0, // estimated hours
                Task.Priority.HIGH,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                new TeamMember[] { member2 } // assigned to Alice
        );

        Task motorControlTask = createTask(
                "Implement Motor Control",
                project,
                drivetrainSubsystem,
                15.0, // estimated hours
                Task.Priority.MEDIUM,
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(14),
                new TeamMember[] { member1, member3 } // assigned to John and Bob
        );

        // Add dependency - motor control depends on drivetrain design
        taskService.addDependency(motorControlTask.getId(), drivetrainDesignTask.getId());

        // Associate components with tasks
        Set<Long> motorTaskComponents = new HashSet<>();
        motorTaskComponents.add(motor.getId());
        motorTaskComponents.add(controller.getId());
        motorTaskComponents.add(encoder.getId());
        componentService.associateComponentsWithTask(motorControlTask.getId(), motorTaskComponents);

        // Create milestones
        milestoneService.createMilestone(
                "Drivetrain Complete",
                LocalDate.now().plusWeeks(2),
                project.getId(),
                "Completed drivetrain including mechanical assembly and controls");

        milestoneService.createMilestone(
                "Design Review",
                LocalDate.now().plusWeeks(1),
                project.getId(),
                "Review full robot design with mentors");

        milestoneService.createMilestone(
                "Competition Ready",
                LocalDate.now().plusWeeks(5),
                project.getId(),
                "Robot ready for competition with all features implemented");

        // Create meetings
        Meeting kickoffMeeting = meetingService.createMeeting(
                LocalDate.now(),
                LocalTime.of(18, 0), // 6:00 PM
                LocalTime.of(20, 0), // 8:00 PM
                project.getId(),
                "Project kickoff meeting");

        Meeting designReviewMeeting = meetingService.createMeeting(
                LocalDate.now().plusWeeks(1),
                LocalTime.of(18, 0), // 6:00 PM
                LocalTime.of(21, 0), // 9:00 PM
                project.getId(),
                "Design review meeting");

        // Record attendance
        for (TeamMember member : members) {
            attendanceService.createAttendance(kickoffMeeting.getId(), member.getId(), true);
        }

        return project;
    }

    /**
     * Helper method to create a task.
     */
    private Task createTask(
            String title,
            Project project,
            Subsystem subsystem,
            double estimatedHours,
            Task.Priority priority,
            LocalDate startDate,
            LocalDate endDate,
            TeamMember[] assignedMembers) {

        Task task = taskService.createTask(
                title,
                project,
                subsystem,
                estimatedHours,
                priority,
                startDate,
                endDate);

        // Assign members
        Set<TeamMember> memberSet = new HashSet<>();
        for (TeamMember member : assignedMembers) {
            memberSet.add(member);
        }
        taskService.assignMembers(task.getId(), memberSet);

        return task;
    }

    /**
     * Generates a small test dataset with a single project.
     * 
     * @return the generated project
     */
    public Project generateSmallTestDataset() {
        return generateTestProject("Test Project");
    }

    /**
     * Generates a large test dataset with multiple projects.
     * 
     * @param projectCount the number of projects to generate
     * @return a list of the generated projects
     */
    public List<Project> generateLargeTestDataset(int projectCount) {
        List<Project> projects = new ArrayList<>();

        for (int i = 1; i <= projectCount; i++) {
            Project project = generateTestProject("Test Project " + i);
            projects.add(project);
        }

        return projects;
    }

    /**
     * Cleans up all test data.
     */
    public void cleanupTestData() {
        // Get all projects
        List<Project> projects = projectService.findAll();

        // Delete projects that start with "Test Project"
        for (Project project : projects) {
            if (project.getName().startsWith("Test Project")) {
                projectService.deleteById(project.getId());
            }
        }

        // Get all team members
        List<TeamMember> members = teamMemberService.findAll();

        // Delete team members created for testing
        for (TeamMember member : members) {
            if (member.getUsername().equals("jsmith") ||
                    member.getUsername().equals("ajones") ||
                    member.getUsername().equals("bwilliams")) {
                teamMemberService.deleteById(member.getId());
            }
        }

        // Get all subteams
        List<Subteam> subteams = subteamService.findAll();

        // Delete subteams created for testing
        for (Subteam subteam : subteams) {
            if (subteam.getName().equals("Programming") ||
                    subteam.getName().equals("Mechanical") ||
                    subteam.getName().equals("Electrical")) {
                subteamService.deleteById(subteam.getId());
            }
        }
    }
}