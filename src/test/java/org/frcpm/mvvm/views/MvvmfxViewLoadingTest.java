// src/test/java/org/frcpm/mvvm/views/MvvmfxViewLoadingTest.java

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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test specifically focused on MVVMFx View loading issues.
 */
public class MvvmfxViewLoadingTest extends BaseFxTest {
    
    private static final Logger LOGGER = Logger.getLogger(MvvmfxViewLoadingTest.class.getName());
    
    @BeforeEach
    public void setUp() {
        // Initialize test framework
        TestModule.initialize();
        MvvmConfig.initialize();
        System.out.println("Setup complete - TestModule: " + TestModule.isInitialized() + 
                          ", MvvmConfig: " + MvvmConfig.isInitialized());
    }
    
    @Test
    public void testMvvmfxViewLoading() throws InterruptedException {
        System.out.println("=== MVVMFX VIEW LOADING TEST START ===");
        
        // Use CountDownLatch to wait for FX thread
        CountDownLatch latch = new CountDownLatch(1);
        Exception[] fxException = new Exception[1];
        ViewTuple<ProjectListMvvmView, ProjectListMvvmViewModel>[] result = new ViewTuple[1];
        
        Platform.runLater(() -> {
            try {
                System.out.println("Running on FX Thread - attempting FluentViewLoader...");
                
                // Try to load the view
                ViewTuple<ProjectListMvvmView, ProjectListMvvmViewModel> viewTuple = 
                    FluentViewLoader.fxmlView(ProjectListMvvmView.class).load();
                
                result[0] = viewTuple;
                
                System.out.println("FluentViewLoader completed");
                System.out.println("ViewTuple: " + (viewTuple != null ? "NOT NULL" : "NULL"));
                
                if (viewTuple != null) {
                    ProjectListMvvmView view = viewTuple.getCodeBehind();
                    ProjectListMvvmViewModel viewModel = viewTuple.getViewModel();
                    javafx.scene.Parent root = viewTuple.getView();
                    
                    System.out.println("View (CodeBehind): " + (view != null ? "NOT NULL" : "NULL"));
                    System.out.println("ViewModel: " + (viewModel != null ? "NOT NULL" : "NULL"));
                    System.out.println("Root (FXML): " + (root != null ? "NOT NULL" : "NULL"));
                    
                    if (view != null) {
                        System.out.println("View class: " + view.getClass().getName());
                    }
                    if (viewModel != null) {
                        System.out.println("ViewModel class: " + viewModel.getClass().getName());
                    }
                    if (root != null) {
                        System.out.println("Root class: " + root.getClass().getName());
                    }
                } else {
                    System.err.println("ViewTuple is NULL - FluentViewLoader failed");
                }
                
            } catch (Exception e) {
                System.err.println("Exception in FX Thread: " + e.getMessage());
                e.printStackTrace();
                fxException[0] = e;
            } finally {
                latch.countDown();
            }
        });
        
        // Wait for FX thread to complete (with timeout)
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "FX thread operation timed out");
        
        // Check if there was an exception
        if (fxException[0] != null) {
            System.err.println("FX Thread threw exception: " + fxException[0].getMessage());
            fail("FX Thread exception: " + fxException[0].getMessage());
        }
        
        // Check the result
        ViewTuple<ProjectListMvvmView, ProjectListMvvmViewModel> viewTuple = result[0];
        
        if (viewTuple == null) {
            fail("ViewTuple is null - MVVMFx failed to load view");
        }
        
        // Check individual components
        assertNotNull(viewTuple.getCodeBehind(), "View (CodeBehind) should not be null");
        assertNotNull(viewTuple.getViewModel(), "ViewModel should not be null");
        assertNotNull(viewTuple.getView(), "Root (FXML) should not be null");
        
        System.out.println("=== ALL CHECKS PASSED ===");
        System.out.println("=== MVVMFX VIEW LOADING TEST END ===");
    }

    @Override
    protected void setupTestEnvironment(Stage stage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setupTestEnvironment'");
    }
}