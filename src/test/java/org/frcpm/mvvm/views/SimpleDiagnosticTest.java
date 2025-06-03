// src/test/java/org/frcpm/mvvm/views/SimpleDiagnosticTest.java

package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Platform;
import javafx.stage.Stage;

import org.frcpm.di.TestModule;
import org.frcpm.mvvm.MvvmConfig;
import org.frcpm.mvvm.viewmodels.ProjectListMvvmViewModel;
import org.frcpm.testfx.BaseFxTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

/**
 * Simple diagnostic test to identify where MVVMFx view loading is failing.
 */
public class SimpleDiagnosticTest extends BaseFxTest {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleDiagnosticTest.class.getName());
    
    @BeforeEach
    public void setUp() {
        // Initialize test framework
        TestModule.initialize();
        if (!MvvmConfig.isInitialized()) {
            MvvmConfig.initialize();
        }
    }
    
    @Test
    public void testBasicMvvmfxSetup() {
        LOGGER.info("=== DIAGNOSTIC TEST START ===");
        
        // Test 1: Check if TestModule initialized properly
        LOGGER.info("Test 1: TestModule initialization");
        System.out.println("=== DIAGNOSTIC: Test 1 - TestModule ===");
        try {
            boolean initialized = TestModule.isInitialized();
            LOGGER.info("TestModule initialized: " + initialized);
            System.out.println("TestModule initialized: " + initialized);
        } catch (Exception e) {
            LOGGER.severe("TestModule initialization failed: " + e.getMessage());
            System.err.println("TestModule initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test 2: Check if MvvmConfig initialized properly
        LOGGER.info("Test 2: MvvmConfig initialization");
        System.out.println("=== DIAGNOSTIC: Test 2 - MvvmConfig ===");
        try {
            boolean initialized = MvvmConfig.isInitialized();
            LOGGER.info("MvvmConfig initialized: " + initialized);
            System.out.println("MvvmConfig initialized: " + initialized);
            
            // Print diagnostic report
            String diagnostics = MvvmConfig.getDiagnosticReport();
            LOGGER.info("MVVMFx Diagnostics:\n" + diagnostics);
            System.out.println("MVVMFx Diagnostics:\n" + diagnostics);
        } catch (Exception e) {
            LOGGER.severe("MvvmConfig initialization failed: " + e.getMessage());
            System.err.println("MvvmConfig initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test 3: Try to create ViewModel directly
        LOGGER.info("Test 3: Direct ViewModel creation");
        System.out.println("=== DIAGNOSTIC: Test 3 - Direct ViewModel ===");
        try {
            ProjectListMvvmViewModel viewModel = new ProjectListMvvmViewModel(
                TestModule.getService(org.frcpm.services.ProjectService.class)
            );
            boolean success = viewModel != null;
            LOGGER.info("Direct ViewModel creation: " + (success ? "SUCCESS" : "FAILED"));
            System.out.println("Direct ViewModel creation: " + (success ? "SUCCESS" : "FAILED"));
        } catch (Exception e) {
            LOGGER.severe("Direct ViewModel creation failed: " + e.getMessage());
            System.err.println("Direct ViewModel creation failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test 4: Try FluentViewLoader on FX thread
        LOGGER.info("Test 4: FluentViewLoader test");
        System.out.println("=== DIAGNOSTIC: Test 4 - FluentViewLoader ===");
        Platform.runLater(() -> {
            try {
                LOGGER.info("Attempting FluentViewLoader.fxmlView()...");
                System.out.println("Attempting FluentViewLoader.fxmlView()...");
                ViewTuple<ProjectListMvvmView, ProjectListMvvmViewModel> viewTuple = 
                    FluentViewLoader.fxmlView(ProjectListMvvmView.class).load();
                
                LOGGER.info("FluentViewLoader result:");
                System.out.println("FluentViewLoader result:");
                String tupleResult = viewTuple != null ? "SUCCESS" : "NULL";
                LOGGER.info("  ViewTuple: " + tupleResult);
                System.out.println("  ViewTuple: " + tupleResult);
                
                if (viewTuple != null) {
                    String viewResult = viewTuple.getCodeBehind() != null ? "SUCCESS" : "NULL";
                    String vmResult = viewTuple.getViewModel() != null ? "SUCCESS" : "NULL";
                    String rootResult = viewTuple.getView() != null ? "SUCCESS" : "NULL";
                    
                    LOGGER.info("  View: " + viewResult);
                    LOGGER.info("  ViewModel: " + vmResult);
                    LOGGER.info("  Root: " + rootResult);
                    
                    System.out.println("  View: " + viewResult);
                    System.out.println("  ViewModel: " + vmResult);
                    System.out.println("  Root: " + rootResult);
                } else {
                    LOGGER.severe("ViewTuple is NULL - MVVMFx loading failed completely");
                    System.err.println("ViewTuple is NULL - MVVMFx loading failed completely");
                }
                
            } catch (Exception e) {
                LOGGER.severe("FluentViewLoader failed: " + e.getMessage());
                System.err.println("FluentViewLoader failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        // Wait for FX thread
        waitForFxEvents();
        sleep(1000);
        
        LOGGER.info("=== DIAGNOSTIC TEST END ===");
    }

    @Override
    protected void setupTestEnvironment(Stage stage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setupTestEnvironment'");
    }
}