// src/test/java/org/frcpm/services/TestServiceFactory.java (continued)

package org.frcpm.services;

import org.frcpm.di.TestModule;
import org.frcpm.repositories.specific.*;
import org.frcpm.services.impl.*;

/**
 * A factory for creating service instances specifically for testing.
 * This factory injects mock repositories into services for better isolation.
 */
public class TestServiceFactory {
    
    /**
     * Creates a TaskService with mock repositories for testing.
     * 
     * @return a TaskService instance with mock repositories
     */
    public static TaskService createTaskService() {
        // Initialize TestModule if needed
        if (!TestModule.isInitialized()) {
            TestModule.initialize();
        }
        
        // Get mock repositories from TestModule
        TaskRepository taskRepo = TestModule.getRepository(TaskRepository.class);
        ProjectRepository projectRepo = TestModule.getRepository(ProjectRepository.class);
        ComponentRepository componentRepo = TestModule.getRepository(ComponentRepository.class);
        
        // Create service with injected repositories
        return new TestableTaskServiceImpl(taskRepo, projectRepo, componentRepo);
    }
    
    /**
     * Creates a ProjectService with mock repositories for testing.
     * 
     * @return a ProjectService instance with mock repositories
     */
    public static ProjectService createProjectService() {
        // Initialize TestModule if needed
        if (!TestModule.isInitialized()) {
            TestModule.initialize();
        }
        
        // Get mock repositories from TestModule
        ProjectRepository projectRepo = TestModule.getRepository(ProjectRepository.class);
        
        // Create a testable service instance with injected repository
        ProjectService service = new ProjectServiceImpl();
        try {
            org.frcpm.utils.TestUtils.setPrivateField(service, "repository", projectRepo);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject repository", e);
        }
        
        return service;
    }
    
    /**
     * Creates a TeamMemberService with mock repositories for testing.
     * 
     * @return a TeamMemberService instance with mock repositories
     */
    public static TeamMemberService createTeamMemberService() {
        // Initialize TestModule if needed
        if (!TestModule.isInitialized()) {
            TestModule.initialize();
        }
        
        // Get mock repositories from TestModule
        TeamMemberRepository teamMemberRepo = TestModule.getRepository(TeamMemberRepository.class);
        SubteamRepository subteamRepo = TestModule.getRepository(SubteamRepository.class);
        
        // Create a testable service instance with injected repositories
        TeamMemberService service = new TeamMemberServiceImpl();
        try {
            org.frcpm.utils.TestUtils.setPrivateField(service, "repository", teamMemberRepo);
            // Inject subteam repository if the service has such a field
            try {
                org.frcpm.utils.TestUtils.setPrivateField(service, "subteamRepository", subteamRepo);
            } catch (Exception e) {
                // Ignore if field doesn't exist
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject repository", e);
        }
        
        return service;
    }
    
    /**
     * A helper method to create any service with properly injected repositories.
     * 
     * @param <T> the service type
     * @param serviceClass the service class to instantiate
     * @param repositoryClass the repository class to inject
     * @return the service instance with injected repository
     */
    @SuppressWarnings("unchecked")
    public static <T, R> T createService(Class<T> serviceClass, Class<R> repositoryClass) {
        // Initialize TestModule if needed
        if (!TestModule.isInitialized()) {
            TestModule.initialize();
        }
        
        try {
            // Get repository from TestModule
            R repository = TestModule.getRepository(repositoryClass);
            
            // Create service instance
            T service = serviceClass.getDeclaredConstructor().newInstance();
            
            // Inject repository
            org.frcpm.utils.TestUtils.setPrivateField(service, "repository", repository);
            
            return service;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create service: " + e.getMessage(), e);
        }
    }
}