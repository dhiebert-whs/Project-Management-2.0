// src/test/java/org/frcpm/mvvm/viewmodels/BaseViewModelTest.java
package org.frcpm.mvvm.viewmodels;

import de.saxsys.mvvmfx.MvvmFX;
import javafx.application.Platform;
import org.frcpm.di.TestModule;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.frcpm.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Base class for ViewModel tests.
 * Provides common setup and teardown functionality for ViewModel testing.
 */
public abstract class BaseViewModelTest<T extends BaseMvvmViewModel> {
    
    protected static final Logger LOGGER = Logger.getLogger(BaseViewModelTest.class.getName());
    
    private AutoCloseable mockitoCloseable;
    protected T viewModel;
    
    /**
     * Sets up the test environment before each test.
     * Initializes Mockito, TestModule, and JavaFX toolkit.
     */
    @BeforeEach
    public void setUp() throws Exception {
        LOGGER.info("Setting up ViewModel test environment");
        
        // Initialize Mockito annotations
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        
        // Initialize TestModule to provide mock services
        TestModule.initialize();
        
        // Initialize JavaFX toolkit if needed
        initializeJavaFxToolkit();
        
        // Create ViewModel
        TestUtils.runOnFxThreadAndWait(() -> {
            viewModel = createViewModel();
        });
        
        // Additional setup
        setupTestData();
    }
    
    /**
     * Tears down the test environment after each test.
     * Cleans up resources and resets TestModule.
     */
    @AfterEach
    public void tearDown() throws Exception {
        LOGGER.info("Tearing down ViewModel test environment");
        
        // Dispose ViewModel
        if (viewModel != null) {
            TestUtils.runOnFxThreadAndWait(() -> {
                viewModel.dispose();
            });
        }
        
        // Reset all mocks
        TestModule.resetMocks();
        
        // Close Mockito resources
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }
    
    /**
     * Creates the ViewModel instance to be tested.
     * Override this method to create a specific ViewModel for each test class.
     * 
     * @return the ViewModel instance
     */
    protected abstract T createViewModel();
    
    /**
     * Sets up test data for the test.
     * Override this method to configure specific test data for each test class.
     */
    protected abstract void setupTestData();
    
    /**
     * Initializes the JavaFX toolkit if needed.
     */
    private void initializeJavaFxToolkit() throws Exception {
        // Check if JavaFX toolkit is already initialized
        try {
            Platform.runLater(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX toolkit not initialized, initialize it
            final CountDownLatch latch = new CountDownLatch(1);
            
            // Initialize JavaFX toolkit
            Thread thread = new Thread(() -> {
                try {
                    // Initialize JavaFX toolkit
                    com.sun.javafx.application.PlatformImpl.startup(() -> {});
                    // Signal that initialization is complete
                    latch.countDown();
                } catch (Exception e1) {
                    LOGGER.severe("Failed to initialize JavaFX toolkit: " + e1.getMessage());
                }
            });
            
            thread.setDaemon(true);
            thread.start();
            
            // Wait for initialization to complete
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout waiting for JavaFX toolkit initialization");
            }
        }
    }
    
    /**
     * Gets a mock service from TestModule.
     * 
     * @param <S> the service type
     * @param serviceClass the service interface class
     * @return the mock service
     */
    protected <S> S getService(Class<S> serviceClass) {
        return TestModule.getService(serviceClass);
    }
    
    /**
     * Gets a mock repository from TestModule.
     * 
     * @param <R> the repository type
     * @param repositoryClass the repository interface class
     * @return the mock repository
     */
    protected <R> R getRepository(Class<R> repositoryClass) {
        return TestModule.getRepository(repositoryClass);
    }
}