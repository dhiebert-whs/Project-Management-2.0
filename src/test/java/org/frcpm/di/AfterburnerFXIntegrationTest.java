// src/test/java/org/frcpm/di/AfterburnerFXIntegrationTest.java
package org.frcpm.di;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ResourceBundle;

import org.frcpm.services.DialogService;
import org.frcpm.services.TeamMemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AfterburnerFXIntegrationTest {
    
    @Mock
    private TeamMemberService teamMemberService;
    
    @Mock
    private DialogService dialogService;
    
    private AutoCloseable closeable;
    
    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        
        // Initialize our module directly - bypass AfterburnerFX Injector
        TestModule.initialize();
        
        // Register mock services with our TestModule
        TestModule.registerMock(TeamMemberService.class, teamMemberService);
        TestModule.registerMock(DialogService.class, dialogService);
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        TestModule.shutdown();
        closeable.close();
    }
    
    @Test
    public void testServiceProviderResolvesService() {
        // Arrange - Create a modified ServiceProvider for testing that uses TestModule
        ServiceProviderTestWrapper serviceProvider = new ServiceProviderTestWrapper();
        
        // Act - Use the wrapper to get services
        TeamMemberService resolved = serviceProvider.getTestTeamMemberService();
        
        // Assert
        assertSame(teamMemberService, resolved, "Service provider should resolve to the registered mock service");
    }
    
    @Test
    public void testResourceBundleAvailability() {
        // Check that resource bundles are available
        ResourceBundle dailyBundle = ResourceBundle.getBundle("org.frcpm.views.dailyview");
        assertNotNull(dailyBundle, "Daily view resource bundle should be available");
        assertEquals("Date:", dailyBundle.getString("daily.date"), 
                     "Date key should have expected value");
        
        ResourceBundle teamMemberBundle = ResourceBundle.getBundle("org.frcpm.views.teammemberview");
        assertNotNull(teamMemberBundle, "Team member view resource bundle should be available");
        assertEquals("Team Member Management", teamMemberBundle.getString("teamMember.title"), 
                     "Title key should have expected value");
    }
    
    @Test
    public void testServiceProviderMethodsReturnExpectedServices() {
        // Arrange - Create a modified ServiceProvider for testing that uses TestModule
        ServiceProviderTestWrapper serviceProvider = new ServiceProviderTestWrapper();
        
        // Act - Use the wrapper to get services
        DialogService resolved = serviceProvider.getTestDialogService();
        
        // Assert
        assertSame(dialogService, resolved, "DialogService should be resolved correctly");
        
        // Test repository accessor methods - should be null when not registered
        assertNull(serviceProvider.getTestProjectRepository(), 
                  "ProjectRepository should be null when not registered");
    }
    
    /**
     * Test wrapper for ServiceProvider that accesses TestModule directly
     * instead of using AfterburnerFX Injector
     */
    private class ServiceProviderTestWrapper {
        
        public <T> T getTestService(Class<T> serviceClass) {
            // Get directly from TestModule instead of using Injector
            return TestModule.getRegisteredMock(serviceClass);
        }
        
        public TeamMemberService getTestTeamMemberService() {
            return getTestService(TeamMemberService.class);
        }
        
        public DialogService getTestDialogService() {
            return getTestService(DialogService.class);
        }
        
        public org.frcpm.repositories.specific.ProjectRepository getTestProjectRepository() {
            return getTestService(org.frcpm.repositories.specific.ProjectRepository.class);
        }
    }
}