// src/test/java/org/frcpm/di/TestModule.java
package org.frcpm.di;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.airhacks.afterburner.injection.Injector;

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
        
        // Reset AfterburnerFX state
        Injector.forgetAll();
        
        // Configure afterburner for testing
        Map<String, Object> testConfig = new HashMap<>();
        testConfig.put("test.mode", true);
        testConfig.put("application.name", "FRC Project Management System - TEST");
        testConfig.put("application.version", "TEST");
        
        Injector.setConfigurationSource(testConfig::get);
        Injector.setInstanceSupplier(TestModule::getTestImplementation);
        
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
     * Implementation supplier for AfterburnerFX.
     * 
     * @param clazz the class to get an implementation for
     * @return the test implementation or null if not found
     */
    private static Object getTestImplementation(Class<?> clazz) {
        return TEST_SERVICES.get(clazz);
    }
    
    /**
     * Cleans up the test module.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down TestModule");
        TEST_SERVICES.clear();
        Injector.forgetAll();
    }
}