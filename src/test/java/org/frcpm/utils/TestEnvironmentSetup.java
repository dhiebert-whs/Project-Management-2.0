package org.frcpm.utils;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension to set up test environment properties.
 */
public class TestEnvironmentSetup implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) {
        // Set test environment system property
        System.setProperty("test.environment", "true");
        
        // Initialize h2 database with unique name for tests
        System.setProperty("h2.database.name", "test_db_" + System.currentTimeMillis());
    }
}
