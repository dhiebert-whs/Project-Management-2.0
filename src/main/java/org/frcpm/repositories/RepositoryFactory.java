package org.frcpm.repositories;

import org.frcpm.repositories.impl.*;
import org.frcpm.repositories.specific.*;

/**
 * Factory class for creating repository instances.
 * This follows the factory pattern to centralize repository creation.
 */
public class RepositoryFactory {
    
    private static final ProjectRepository projectRepository = new ProjectRepositoryImpl();
    private static final TaskRepository taskRepository = new TaskRepositoryImpl();
    private static final TeamMemberRepository teamMemberRepository = new TeamMemberRepositoryImpl();
    private static final SubteamRepository subteamRepository = new SubteamRepositoryImpl();
    private static final SubsystemRepository subsystemRepository = new SubsystemRepositoryImpl();
    private static final ComponentRepository componentRepository = new ComponentRepositoryImpl();
    private static final MeetingRepository meetingRepository = new MeetingRepositoryImpl();
    private static final AttendanceRepository attendanceRepository = new AttendanceRepositoryImpl();
    private static final MilestoneRepository milestoneRepository = new MilestoneRepositoryImpl();
    
    /**
     * Gets the project repository instance.
     * 
     * @return the project repository
     */
    public static ProjectRepository getProjectRepository() {
        return projectRepository;
    }
    
    /**
     * Gets the task repository instance.
     * 
     * @return the task repository
     */
    public static TaskRepository getTaskRepository() {
        return taskRepository;
    }
    
    /**
     * Gets the team member repository instance.
     * 
     * @return the team member repository
     */
    public static TeamMemberRepository getTeamMemberRepository() {
        return teamMemberRepository;
    }
    
    /**
     * Gets the subteam repository instance.
     * 
     * @return the subteam repository
     */
    public static SubteamRepository getSubteamRepository() {
        return subteamRepository;
    }
    
    /**
     * Gets the subsystem repository instance.
     * 
     * @return the subsystem repository
     */
    public static SubsystemRepository getSubsystemRepository() {
        return subsystemRepository;
    }
    
    /**
     * Gets the component repository instance.
     * 
     * @return the component repository
     */
    public static ComponentRepository getComponentRepository() {
        return componentRepository;
    }
    
    /**
     * Gets the meeting repository instance.
     * 
     * @return the meeting repository
     */
    public static MeetingRepository getMeetingRepository() {
        return meetingRepository;
    }
    
    /**
     * Gets the attendance repository instance.
     * 
     * @return the attendance repository
     */
    public static AttendanceRepository getAttendanceRepository() {
        return attendanceRepository;
    }
    
    /**
     * Gets the milestone repository instance.
     * 
     * @return the milestone repository
     */
    public static MilestoneRepository getMilestoneRepository() {
        return milestoneRepository;
    }
}