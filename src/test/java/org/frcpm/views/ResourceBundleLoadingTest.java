// src/test/java/org/frcpm/views/ResourceBundleLoadingTest.java
package org.frcpm.views;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ResourceBundleLoadingTest {
    
    @Test
    public void testDailyViewResourceBundle() {
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
}