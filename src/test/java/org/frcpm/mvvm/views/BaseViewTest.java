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

import java.util.logging.Logger;

/**
 * Base class for View tests using TestFX.
 * Provides common setup and teardown functionality for View testing.
 */
public abstract class BaseViewTest<V extends FxmlView<VM>, VM extends ViewModel> extends BaseFxTest {
    
    protected static final Logger LOGGER = Logger.getLogger(BaseViewTest.class.getName());
    
    private AutoCloseable mockitoCloseable;
    protected V view;
    protected VM viewModel;
    protected ViewTuple<V, VM> viewTuple;
    
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
        
        // Initialize TestModule and MvvmConfig
        TestModule.initialize();
        if (!MvvmConfig.isInitialized()) {
            MvvmConfig.initialize();
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
     * 
     * @param stage the stage to show the view in
     */
    protected void createAndShowView(Stage stage) {
        // Load the view with FluentViewLoader
        viewTuple = FluentViewLoader.fxmlView(getViewClass()).load();
        view = viewTuple.getCodeBehind();
        viewModel = viewTuple.getViewModel();
        
        // Create scene with view root
        Parent root = viewTuple.getView();
        Scene scene = new Scene(root);
        
        // Set scene in stage and show
        stage.setScene(scene);
        stage.setTitle(getViewClass().getSimpleName());
        stage.show();
        stage.toFront();
        
        // Wait for the scene to be shown
        waitForFxEvents();
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