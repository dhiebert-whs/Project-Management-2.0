package org.frcpm.services;

import org.frcpm.services.impl.*;

/**
 * Factory class for creating service instances.
 * This class provides centralized access to all service implementations.
 */
public class ServiceFactory {
    
    private static ProjectService projectService;
    private static TaskService taskService;
    private static TeamMemberService teamMemberService;
    private static SubteamService subteamService;
    private static SubsystemService subsystemService;
    private static ComponentService componentService;
    private static AttendanceService attendanceService;
    private static MilestoneService milestoneService;
    private static MeetingService meetingService;
    
    /**
     * Gets the ProjectService instance.
     *
     * @return The ProjectService
     */
    public static ProjectService getProjectService() {
        if (projectService == null) {
            projectService = new ProjectServiceImpl();
        }
        return projectService;
    }
    
    /**
     * Gets the TaskService instance.
     *
     * @return The TaskService
     */
    public static TaskService getTaskService() {
        if (taskService == null) {
            taskService = new TaskServiceImpl();
        }
        return taskService;
    }
    
    /**
     * Gets the TeamMemberService instance.
     *
     * @return The TeamMemberService
     */
    public static TeamMemberService getTeamMemberService() {
        if (teamMemberService == null) {
            teamMemberService = new TeamMemberServiceImpl();
        }
        return teamMemberService;
    }
    
    /**
     * Gets the SubteamService instance.
     *
     * @return The SubteamService
     */
    public static SubteamService getSubteamService() {
        if (subteamService == null) {
            subteamService = new SubteamServiceImpl();
        }
        return subteamService;
    }
    
    /**
     * Gets the SubsystemService instance.
     *
     * @return The SubsystemService
     */
    public static SubsystemService getSubsystemService() {
        if (subsystemService == null) {
            subsystemService = new SubsystemServiceImpl();
        }
        return subsystemService;
    }
    
    /**
     * Gets the ComponentService instance.
     *
     * @return The ComponentService
     */
    public static ComponentService getComponentService() {
        if (componentService == null) {
            componentService = new ComponentServiceImpl();
        }
        return componentService;
    }
    
    /**
     * Gets the AttendanceService instance.
     *
     * @return The AttendanceService
     */
    public static AttendanceService getAttendanceService() {
        if (attendanceService == null) {
            attendanceService = new AttendanceServiceImpl();
        }
        return attendanceService;
    }
    
    /**
     * Gets the MilestoneService instance.
     *
     * @return The MilestoneService
     */
    public static MilestoneService getMilestoneService() {
        if (milestoneService == null) {
            milestoneService = new MilestoneServiceImpl();
        }
        return milestoneService;
    }
    
    /**
     * Gets the MeetingService instance.
     *
     * @return The MeetingService
     */
    public static MeetingService getMeetingService() {
        if (meetingService == null) {
            meetingService = new MeetingServiceImpl();
        }
        return meetingService;
    }
}