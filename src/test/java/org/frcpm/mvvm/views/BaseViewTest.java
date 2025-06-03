// src/test/java/org/frcpm/mvvm/views/BaseViewTest.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.ViewModel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.frcpm.di.TestModule;
import org.frcpm.mvvm.MvvmConfig;
import org.frcpm.testfx.BaseFxTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Base class for View tests using TestFX.
 * Provides common setup and teardown functionality for View testing.
 * FIXED: Enhanced MVVMFx initialization and resource bundle handling.
 */
public abstract class BaseViewTest<V extends FxmlView<VM>, VM extends ViewModel> extends BaseFxTest {
    
    protected static final Logger LOGGER = Logger.getLogger(BaseViewTest.class.getName());
    
    private AutoCloseable mockitoCloseable;
    protected V view;
    protected VM viewModel;
    protected ViewTuple<V, VM> viewTuple;
    protected ResourceBundle resources;
    
    /**
     * Sets up the view for testing.
     * This method is called by TestFX after the JavaFX toolkit is initialized.
     * 
     * @param stage the primary stage for the test
     */
    @Override
    protected void setupTestEnvironment(Stage stage) {
        LOGGER.info("Setting up View test environment");
        
        // Additional setup
        setupTestData();
        
        // Create and show the view
        createAndShowView(stage);
    }
    
    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    public void setUp() {
        LOGGER.info("Setting up View test environment");
        
        // Initialize Mockito annotations
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        
        // Initialize TestModule FIRST
        TestModule.initialize();
        
        // Initialize MvvmConfig AFTER TestModule
        if (!MvvmConfig.isInitialized()) {
            MvvmConfig.initialize();
        }
        
        // Load resource bundle for the view
        loadResourceBundle();
    }
    
    /**
     * Loads the resource bundle for the view.
     */
    private void loadResourceBundle() {
        try {
            String viewClassName = getViewClass().getSimpleName();
            String bundleName = "org.frcpm.mvvm.views." + viewClassName;
            resources = ResourceBundle.getBundle(bundleName);
            LOGGER.info("Loaded resource bundle: " + bundleName);
        } catch (Exception e) {
            LOGGER.warning("Could not load resource bundle for " + getViewClass().getSimpleName() + ": " + e.getMessage());
            // Try to load common bundle as fallback
            try {
                resources = ResourceBundle.getBundle("org.frcpm.mvvm.views.common");
                LOGGER.info("Loaded fallback common resource bundle");
            } catch (Exception e2) {
                LOGGER.warning("Could not load common resource bundle: " + e2.getMessage());
                resources = null;
            }
        }
    }
    
    /**
     * Tears down the test environment after each test.
     */
    @AfterEach
    public void tearDown() throws Exception {
        LOGGER.info("Tearing down View test environment");
        
        // Take screenshot on failure if enabled
        if (Boolean.getBoolean("testfx.screenshot.onFailure")) {
            takeScreenshot(getClass().getSimpleName());
        }
        
        // Reset all mocks
        TestModule.resetMocks();
        
        // Close Mockito resources
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }
    
    /**
     * Creates and shows the view in the provided stage.
     * FIXED: Enhanced error handling and resource bundle support.
     * 
     * @param stage the stage to show the view in
     */
    protected void createAndShowView(Stage stage) {
        try {
            LOGGER.info("Creating view: " + getViewClass().getSimpleName());
            
            // Load the view with FluentViewLoader
            // Use the correct MVVMFx API without intermediate variables
            if (resources != null) {
                viewTuple = FluentViewLoader.fxmlView(getViewClass())
                    .resourceBundle(resources)
                    .load();
            } else {
                viewTuple = FluentViewLoader.fxmlView(getViewClass()).load();
            }
            
            if (viewTuple == null) {
                throw new RuntimeException("ViewTuple is null - MVVMFx failed to load view");
            }
            
            view = viewTuple.getCodeBehind();
            viewModel = viewTuple.getViewModel();
            
            if (view == null) {
                throw new RuntimeException("View is null - MVVMFx failed to create view instance");
            }
            
            if (viewModel == null) {
                throw new RuntimeException("ViewModel is null - MVVMFx failed to inject ViewModel");
            }
            
            LOGGER.info("Successfully created view and ViewModel: " + view.getClass().getSimpleName() + 
                       " with " + viewModel.getClass().getSimpleName());
            
            // Create scene with view root
            Parent root = viewTuple.getView();
            if (root == null) {
                throw new RuntimeException("View root is null - FXML loading failed");
            }
            
            Scene scene = new Scene(root);
            
            // Set scene in stage and show
            stage.setScene(scene);
            stage.setTitle(getViewClass().getSimpleName());
            stage.show();
            stage.toFront();
            
            // Wait for the scene to be shown
            waitForFxEvents();
            
            LOGGER.info("Successfully displayed view in stage");
            
        } catch (Exception e) {
            LOGGER.severe("Failed to create and show view: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create view: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets the view class to be tested.
     * Override this method to specify the view class for each test class.
     * 
     * @return the view class
     */
    protected abstract Class<V> getViewClass();
    
    /**
     * Sets up test data for the test.
     * Override this method to configure specific test data for each test class.
     */
    protected abstract void setupTestData();
    
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