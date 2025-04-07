package org.frcpm.services;

import org.frcpm.services.impl.*;

/**
 * Factory class for creating service instances.
 * This follows the factory pattern to centralize service creation.
 */
public class ServiceFactory {
    
    private static final ProjectService projectService = new ProjectServiceImpl();
    private static final TaskService taskService = new TaskServiceImpl();
    private static final TeamMemberService teamMemberService = new TeamMemberServiceImpl();
    private static final SubteamService subteamService = new SubteamServiceImpl();
    private static final SubsystemService subsystemService = new SubsystemServiceImpl();
    private static final ComponentService componentService = new ComponentServiceImpl();
    private static final MeetingService meetingService = new MeetingServiceImpl();
    private static final AttendanceService attendanceService = new AttendanceServiceImpl();
    private static final MilestoneService milestoneService = new MilestoneServiceImpl();
    
    /**
     * Gets the project service instance.
     * 
     * @return the project service
     */
    public static ProjectService getProjectService() {
        return projectService;
    }
    
    /**
     * Gets the task service instance.
     * 
     * @return the task service
     */
    public static TaskService getTaskService() {
        return taskService;
    }
    
    /**
     * Gets the team member service instance.
     * 
     * @return the team member service
     */
    public static TeamMemberService getTeamMemberService() {
        return teamMemberService;
    }
    
    /**
     * Gets the subteam service instance.
     * 
     * @return the subteam service
     */
    public static SubteamService getSubteamService() {
        return subteamService;
    }
    
    /**
     * Gets the subsystem service instance.
     * 
     * @return the subsystem service
     */
    public static SubsystemService getSubsystemService() {
        return subsystemService;
    }
    
    /**
     * Gets the component service instance.
     * 
     * @return the component service
     */
    public static ComponentService getComponentService() {
        return componentService;
    }
    
    /**
     * Gets the meeting service instance.
     * 
     * @return the meeting service
     */
    public static MeetingService getMeetingService() {
        return meetingService;
    }
    
    /**
     * Gets the attendance service instance.
     * 
     * @return the attendance service
     */
    public static AttendanceService getAttendanceService() {
        return attendanceService;
    }
    
    /**
     * Gets the milestone service instance.
     * 
     * @return the milestone service
     */
    public static MilestoneService getMilestoneService() {
        return milestoneService;
    }

    /**
     * Gets the dialog service.
     * 
     * @return the dialog service
     */
    public static DialogService getDialogService() {
        return new JavaFXDialogService();
}
}