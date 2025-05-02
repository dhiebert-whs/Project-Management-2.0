// src/test/java/org/frcpm/views/ResourceBundleLoadingTest.java
package org.frcpm.views;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.ResourceBundle;

import org.frcpm.di.FrcpmModule;
import org.frcpm.presenters.DailyPresenter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airhacks.afterburner.injection.Injector;

@ExtendWith(MockitoExtension.class)
public class ResourceBundleLoadingTest {
    
    @BeforeEach
    public void setUp() {
        FrcpmModule.initialize();
    }
    
    @AfterEach
    public void tearDown() {
        Injector.forgetAll();
        FrcpmModule.shutdown();
    }
    
    @Test
    public void testDailyViewResourceBundle() {
        // Arrange - set the default locale
        Locale defaultLocale = Locale.getDefault();
        
        // Act
        ResourceBundle bundle = ResourceBundle.getBundle("org.frcpm.views.dailyview");
        
        // Assert
        assertNotNull(bundle, "Daily view resource bundle should exist");
        
        // Test some specific keys
        assertTrue(bundle.containsKey("daily.date"), 
                  "Resource bundle should contain daily.date key");
        assertEquals("Date:", bundle.getString("daily.date"));
        
        assertTrue(bundle.containsKey("error.title"), 
                  "Resource bundle should contain standard error.title key");
        assertEquals("Error", bundle.getString("error.title"));
    }
    
    @Test
    public void testTeamMemberViewResourceBundle() {
        // Arrange - set the default locale
        Locale defaultLocale = Locale.getDefault();
        
        // Act
        ResourceBundle bundle = ResourceBundle.getBundle("org.frcpm.views.teammemberview");
        
        // Assert
        assertNotNull(bundle, "Team member view resource bundle should exist");
        
        // Test some specific keys
        assertTrue(bundle.containsKey("teamMember.title"), 
                  "Resource bundle should contain teamMember.title key");
        assertEquals("Team Member Management", bundle.getString("teamMember.title"));
        
        assertTrue(bundle.containsKey("error.title"), 
                  "Resource bundle should contain standard error.title key");
        assertEquals("Error", bundle.getString("error.title"));
    }
    
    @Test
    public void testConsistentErrorTitlesAcrossBundles() {
        // Arrange
        String[] bundleNames = {
            "org.frcpm.views.dailyview",
            "org.frcpm.views.teammemberview"
            // Add more as needed
        };
        
        // Act & Assert
        for (String bundleName : bundleNames) {
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
            assertTrue(bundle.containsKey("error.title"), 
                      "Bundle " + bundleName + " should contain error.title key");
            assertEquals("Error", bundle.getString("error.title"), 
                        "Bundle " + bundleName + " should have standard error title");
            
            assertTrue(bundle.containsKey("info.title"), 
                      "Bundle " + bundleName + " should contain info.title key");
            assertEquals("Information", bundle.getString("info.title"), 
                        "Bundle " + bundleName + " should have standard info title");
        }
    }
    
    @Test
    public void testAfterburnerFXResourceBundleAccess() {
        // Create a test view
        DailyView view = new DailyView();
        
        // Get the presenter
        DailyPresenter presenter = (DailyPresenter) view.getPresenter();
        
        // Check if the presenter is created
        assertNotNull(presenter, "Presenter should be created by AfterburnerFX");
        
        // Check if the presenter can access resources
        // This is an indirect test since we can't directly access the resources field
        // Just check that the view model was created, which requires resources
        assertNotNull(presenter.getViewModel(), "ViewModel should be created which requires resources");
    }
}