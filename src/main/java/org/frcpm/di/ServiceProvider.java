package org.frcpm.di;

import com.airhacks.afterburner.injection.Injector;
import org.frcpm.services.*;
import org.frcpm.repositories.*;
import org.frcpm.repositories.specific.*;

import java.util.logging.Logger;

/**
 * Service provider for dependency injection using AfterburnerFX.
 * This class should be used instead of ServiceFactory after migration.
 */
public class ServiceProvider {
    
    private static final Logger LOGGER = Logger.getLogger(ServiceProvider.class.getName());
    
    /**
     * Gets a service instance by its interface class.
     * 
     * @param <T> the type of service
     * @param serviceClass the interface class of the service
     * @return the service instance
     */
    public static <T> T getService(Class<T> serviceClass) {
        LOGGER.fine("Getting service: " + serviceClass.getSimpleName());
        return Injector.instantiatePresenter(serviceClass);
    }
    
    /**
     * Gets the project service.
     * 
     * @return the project service
     */
    public static ProjectService getProjectService() {
        return getService(ProjectService.class);
    }
    
    /**
     * Gets the task service.
     * 
     * @return the task service
     */
    public static TaskService getTaskService() {
        return getService(TaskService.class);
    }
    
    /**
     * Gets the team member service.
     * 
     * @return the team member service
     */
    public static TeamMemberService getTeamMemberService() {
        return getService(TeamMemberService.class);
    }
    
    /**
     * Gets the subteam service.
     * 
     * @return the subteam service
     */
    public static SubteamService getSubteamService() {
        return getService(SubteamService.class);
    }
    
    /**
     * Gets the subsystem service.
     * 
     * @return the subsystem service
     */
    public static SubsystemService getSubsystemService() {
        return getService(SubsystemService.class);
    }
    
    /**
     * Gets the component service.
     * 
     * @return the component service
     */
    public static ComponentService getComponentService() {
        return getService(ComponentService.class);
    }
    
    /**
     * Gets the meeting service.
     * 
     * @return the meeting service
     */
    public static MeetingService getMeetingService() {
        return getService(MeetingService.class);
    }
    
    /**
     * Gets the attendance service.
     * 
     * @return the attendance service
     */
    public static AttendanceService getAttendanceService() {
        return getService(AttendanceService.class);
    }
    
    /**
     * Gets the milestone service.
     * 
     * @return the milestone service
     */
    public static MilestoneService getMilestoneService() {
        return getService(MilestoneService.class);
    }
    
    /**
     * Gets the dialog service.
     * 
     * @return the dialog service
     */
    public static DialogService getDialogService() {
        return getService(DialogService.class);
    }
    
    /**
     * Gets the Gantt data service.
     * 
     * @return the Gantt data service
     */
    public static GanttDataService getGanttDataService() {
        return getService(GanttDataService.class);
    }
    
    /**
     * Gets the WebView bridge service.
     * 
     * @return the WebView bridge service
     */
    public static WebViewBridgeService getWebViewBridgeService() {
        return getService(WebViewBridgeService.class);
    }
    
    // Repository accessor methods
    
    /**
     * Gets a repository instance by its interface class.
     * 
     * @param <T> the type of repository
     * @param repositoryClass the interface class of the repository
     * @return the repository instance
     */
    public static <T> T getRepository(Class<T> repositoryClass) {
        LOGGER.fine("Getting repository: " + repositoryClass.getSimpleName());
        return Injector.instantiatePresenter(repositoryClass);
    }
    
    /**
     * Gets the project repository.
     * 
     * @return the project repository
     */
    public static ProjectRepository getProjectRepository() {
        return getRepository(ProjectRepository.class);
    }
    
    /**
     * Gets the task repository.
     * 
     * @return the task repository
     */
    public static TaskRepository getTaskRepository() {
        return getRepository(TaskRepository.class);
    }
    
    /**
     * Gets the team member repository.
     * 
     * @return the team member repository
     */
    public static TeamMemberRepository getTeamMemberRepository() {
        return getRepository(TeamMemberRepository.class);
    }
    
    /**
     * Gets the subteam repository.
     * 
     * @return the subteam repository
     */
    public static SubteamRepository getSubteamRepository() {
        return getRepository(SubteamRepository.class);
    }
    
    /**
     * Gets the subsystem repository.
     * 
     * @return the subsystem repository
     */
    public static SubsystemRepository getSubsystemRepository() {
        return getRepository(SubsystemRepository.class);
    }
    
    /**
     * Gets the component repository.
     * 
     * @return the component repository
     */
    public static ComponentRepository getComponentRepository() {
        return getRepository(ComponentRepository.class);
    }
    
    /**
     * Gets the meeting repository.
     * 
     * @return the meeting repository
     */
    public static MeetingRepository getMeetingRepository() {
        return getRepository(MeetingRepository.class);
    }
    
    /**
     * Gets the attendance repository.
     * 
     * @return the attendance repository
     */
    public static AttendanceRepository getAttendanceRepository() {
        return getRepository(AttendanceRepository.class);
    }
    
    /**
     * Gets the milestone repository.
     * 
     * @return the milestone repository
     */
    public static MilestoneRepository getMilestoneRepository() {
        return getRepository(MilestoneRepository.class);
    }
}