// src/test/java/org/frcpm/di/TestModule.java
package org.frcpm.di;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Test module that extends FrcpmModule for easier testing with mocked dependencies.
 */
public class TestModule {
    
    private static final Logger LOGGER = Logger.getLogger(TestModule.class.getName());
    private static final Map<Class<?>, Object> TEST_SERVICES = new HashMap<>();
    
    /**
     * Initializes the test module with no default services.
     */
    public static void initialize() {
        LOGGER.info("Initializing TestModule for testing");
        
        // Clear any existing services
        TEST_SERVICES.clear();
        
        LOGGER.info("TestModule initialization complete");
    }
    
    /**
     * Registers a test implementation for a service or repository.
     * 
     * @param <T> the type of the service
     * @param serviceClass the class of the service
     * @param implementation the implementation to use for testing
     */
    public static <T> void registerMock(Class<T> serviceClass, T implementation) {
        TEST_SERVICES.put(serviceClass, implementation);
        LOGGER.fine("Registered test mock: " + serviceClass.getSimpleName());
    }
    
    /**
     * Gets a registered mock for testing.
     * 
     * @param <T> the type of the service
     * @param serviceClass the class of the service
     * @return the registered mock or null if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T getRegisteredMock(Class<T> serviceClass) {
        return (T) TEST_SERVICES.get(serviceClass);
    }
    
    /**
     * Cleans up the test module.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down TestModule");
        TEST_SERVICES.clear();
    }
}