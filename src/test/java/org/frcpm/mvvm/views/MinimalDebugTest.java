// src/test/java/org/frcpm/mvvm/views/MinimalDebugTest.java

package org.frcpm.mvvm.views;

import org.frcpm.di.TestModule;
import org.frcpm.mvvm.MvvmConfig;
import org.frcpm.services.ProjectService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Minimal test to check if our services are working.
 */
public class MinimalDebugTest {
    
    @Test
    public void testBasicSetup() {
        System.out.println("=== MINIMAL DEBUG TEST START ===");
        
        try {
            System.out.println("1. Initializing TestModule...");
            TestModule.initialize();
            System.out.println("   TestModule initialized: " + TestModule.isInitialized());
            
            System.out.println("2. Initializing MvvmConfig...");
            MvvmConfig.initialize();
            System.out.println("   MvvmConfig initialized: " + MvvmConfig.isInitialized());
            
            System.out.println("3. Getting ProjectService...");
            ProjectService projectService = TestModule.getService(ProjectService.class);
            System.out.println("   ProjectService: " + (projectService != null ? "SUCCESS" : "NULL"));
            
            // If we get here, basic setup works
            System.out.println("=== BASIC SETUP WORKS ===");
            
        } catch (Exception e) {
            System.err.println("SETUP FAILED: " + e.getMessage());
            e.printStackTrace();
            fail("Setup failed: " + e.getMessage());
        }
        
        System.out.println("=== MINIMAL DEBUG TEST END ===");
    }
}