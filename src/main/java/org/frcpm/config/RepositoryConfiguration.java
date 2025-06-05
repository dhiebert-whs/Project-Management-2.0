// src/main/java/org/frcpm/config/RepositoryConfiguration.java

package org.frcpm.config;

import org.frcpm.repositories.impl.*;
import org.frcpm.repositories.specific.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for repository beans.
 * This class provides the existing JPA repository implementations as Spring beans
 * to bridge the gap between the new Spring services and existing repository layer.
 */
@Configuration
public class RepositoryConfiguration {
    
    /**
     * Creates a ProjectRepository bean using the existing implementation.
     * 
     * @return the project repository implementation
     */
    @Bean("projectRepository")
    @Primary
    public ProjectRepository projectRepository() {
        return new ProjectRepositoryImpl();
    }
    
    /**
     * Creates a TaskRepository bean using the existing implementation.
     * 
     * @return the task repository implementation
     */
    @Bean("taskRepository")
    @Primary
    public TaskRepository taskRepository() {
        return new TaskRepositoryImpl();
    }
    
    /**
     * Creates a TeamMemberRepository bean using the existing implementation.
     * 
     * @return the team member repository implementation
     */
    @Bean("teamMemberRepository")
    @Primary
    public TeamMemberRepository teamMemberRepository() {
        return new TeamMemberRepositoryImpl();
    }
    
    /**
     * Creates a SubteamRepository bean using the existing implementation.
     * 
     * @return the subteam repository implementation
     */
    @Bean
    public SubteamRepository subteamRepository() {
        return new SubteamRepositoryImpl();
    }
    
    /**
     * Creates a SubsystemRepository bean using the existing implementation.
     * 
     * @return the subsystem repository implementation
     */
    @Bean
    public SubsystemRepository subsystemRepository() {
        return new SubsystemRepositoryImpl();
    }
    
    /**
     * Creates a ComponentRepository bean using the existing implementation.
     * 
     * @return the component repository implementation
     */
    @Bean
    public ComponentRepository componentRepository() {
        return new ComponentRepositoryImpl();
    }
    
    /**
     * Creates a MeetingRepository bean using the existing implementation.
     * 
     * @return the meeting repository implementation
     */
    @Bean
    public MeetingRepository meetingRepository() {
        return new MeetingRepositoryImpl();
    }
    
    /**
     * Creates an AttendanceRepository bean using the existing implementation.
     * 
     * @return the attendance repository implementation
     */
    @Bean
    public AttendanceRepository attendanceRepository() {
        return new AttendanceRepositoryImpl();
    }
    
    /**
     * Creates a MilestoneRepository bean using the existing implementation.
     * 
     * @return the milestone repository implementation
     */
    @Bean
    public MilestoneRepository milestoneRepository() {
        return new MilestoneRepositoryImpl();
    }
}