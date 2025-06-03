// src/test/java/org/frcpm/mvvm/views/MvvmfxSimulationTest.java

package org.frcpm.mvvm.views;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import org.frcpm.di.TestModule;
import org.frcpm.mvvm.MvvmConfig;
import org.frcpm.testfx.BaseFxTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test that simulates exactly what MVVMFx is trying to do.
 */
public class MvvmfxSimulationTest extends BaseFxTest {
    
    @BeforeEach
    public void setUp() {
        // Initialize test framework
        TestModule.initialize();
        MvvmConfig.initialize();
    }
    
    @Test
    public void testDirectFXMLLoading() throws InterruptedException {
        System.out.println("=== MVVMFX SIMULATION TEST ===");
        
        // Use CountDownLatch to wait for FX thread
        CountDownLatch latch = new CountDownLatch(1);
        Exception[] fxException = new Exception[1];
        
        Platform.runLater(() -> {
            try {
                System.out.println("1. Testing direct FXML loading (what MVVMFx does internally)...");
                
                // Get the FXML resource URL
                String fxmlPath = "/org/frcpm/mvvm/views/ProjectListMvvmView.fxml";
                URL fxmlUrl = ProjectListMvvmView.class.getResource(fxmlPath);
                
                System.out.println("2. FXML URL: " + fxmlUrl);
                
                if (fxmlUrl == null) {
                    throw new RuntimeException("FXML file not found: " + fxmlPath);
                }
                
                // Try to load with FXMLLoader (what MVVMFx uses internally)
                System.out.println("3. Creating FXMLLoader...");
                FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
                
                // This is the critical step - can we load the FXML with the controller?
                System.out.println("4. Loading FXML...");
                Object root = fxmlLoader.load();
                
                System.out.println("5. FXML loaded successfully!");
                System.out.println("   Root: " + (root != null ? root.getClass().getName() : "null"));
                
                // Check if controller was created
                Object controller = fxmlLoader.getController();
                System.out.println("   Controller: " + (controller != null ? controller.getClass().getName() : "null"));
                
                if (controller != null) {
                    System.out.println("   ✅ Controller instantiated successfully");
                    
                    // Check if it's the right type
                    if (controller instanceof ProjectListMvvmView) {
                        System.out.println("   ✅ Controller is correct type");
                    } else {
                        System.out.println("   ❌ Controller is wrong type: " + controller.getClass().getName());
                    }
                } else {
                    System.out.println("   ❌ No controller was created");
                }
                
            } catch (Exception e) {
                System.err.println("❌ Direct FXML loading failed: " + e.getMessage());
                e.printStackTrace();
                fxException[0] = e;
            } finally {
                latch.countDown();
            }
        });
        
        // Wait for FX thread to complete (with timeout)
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        if (!completed) {
            throw new RuntimeException("FX thread operation timed out");
        }
        
        // Check if there was an exception
        if (fxException[0] != null) {
            throw new RuntimeException("Direct FXML loading failed", fxException[0]);
        }
        
        System.out.println("=== DIRECT FXML LOADING SUCCESSFUL ===");
    }

    @Override
    protected void setupTestEnvironment(Stage stage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setupTestEnvironment'");
    }
}