// src/test/java/org/frcpm/services/BaseServiceTest.java
package org.frcpm.services;

import org.frcpm.di.TestModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import java.util.logging.Logger;

/**
 * Base class for service tests.
 * Provides common setup and teardown functionality for service testing.
 */
public abstract class BaseServiceTest {
    
    protected static final Logger LOGGER = Logger.getLogger(BaseServiceTest.class.getName());
    
    private AutoCloseable mockitoCloseable;
    
    /**
     * Sets up the test environment before each test.
     * Initializes Mockito and TestModule.
     */
    @BeforeEach
    public void setUp() {
        LOGGER.info("Setting up service test environment");
        
        // Initialize Mockito annotations
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        
        // Initialize TestModule to provide mock repositories
        TestModule.initialize();
        
        // Additional setup
        setupTestData();
    }
    
    /**
     * Tears down the test environment after each test.
     * Cleans up Mockito resources and resets TestModule.
     */
    @AfterEach
    public void tearDown() throws Exception {
        LOGGER.info("Tearing down service test environment");
        
        // Reset all mocks
        TestModule.resetMocks();
        
        // Close Mockito resources
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }
    
    /**
     * Sets up test data for the test.
     * Override this method to configure specific mocks for each test class.
     */
    protected abstract void setupTestData();
    
    /**
     * Gets a mock service from TestModule.
     * 
     * @param <T> the service type
     * @param serviceClass the service interface class
     * @return the mock service
     */
    protected <T> T getService(Class<T> serviceClass) {
        return TestModule.getService(serviceClass);
    }
    
    /**
     * Gets a mock repository from TestModule.
     * 
     * @param <T> the repository type
     * @param repositoryClass the repository interface class
     * @return the mock repository
     */
    protected <T> T getRepository(Class<T> repositoryClass) {
        return TestModule.getRepository(repositoryClass);
    }
}