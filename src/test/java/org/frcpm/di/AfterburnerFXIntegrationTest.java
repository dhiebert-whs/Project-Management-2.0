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

import com.airhacks.afterburner.injection.Injector;

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
        
        // Register mock services with the module
        FrcpmModule.initialize();
        FrcpmModule.register(TeamMemberService.class, teamMemberService);
        FrcpmModule.register(DialogService.class, dialogService);
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        // Reset AfterburnerFX state
        Injector.forgetAll();
        FrcpmModule.shutdown();
        closeable.close();
    }
    
    @Test
    public void testServiceProviderResolvesService() {
        // Act
        TeamMemberService resolved = ServiceProvider.getTeamMemberService();
        
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
        // Register mock services with the module for testing various service provider methods
        DialogService mockDialogService = dialogService;
        FrcpmModule.register(DialogService.class, mockDialogService);
        
        // Test multiple service provider methods
        assertSame(mockDialogService, ServiceProvider.getDialogService(), 
                  "DialogService should be resolved correctly");
        
        // Test repository accessor methods
        assertNull(ServiceProvider.getProjectRepository(), 
                  "ProjectRepository should be null when not registered");
    }
}