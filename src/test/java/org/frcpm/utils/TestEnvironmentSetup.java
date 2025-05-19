// src/test/java/org/frcpm/utils/TestEnvironmentSetup.java
package org.frcpm.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.frcpm.config.DatabaseConfig;
import org.frcpm.di.ServiceLocator;
import org.frcpm.di.TestModule;
import org.frcpm.mvvm.MvvmConfig;
import org.frcpm.testfx.TestFXHeadlessConfig;
import org.testfx.util.WaitForAsyncUtils;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class providing setup and teardown functionality for testing environments.
 * Handles initialization of the database, services, and JavaFX components.
 */
public class TestEnvironmentSetup {
    
    private static final Logger LOGGER = Logger.getLogger(TestEnvironmentSetup.class.getName());
    
    /** Default timeout for operations in milliseconds */
    private static final int DEFAULT_TIMEOUT = 10000;
    
    /** Flag to track if the test database has been initialized */
    private static boolean databaseInitialized = false;
    
    /** Flag to track if the JavaFX toolkit has been initialized */
    private static boolean javaFxInitialized = false;
    
    /**
     * Private constructor to prevent instantiation.
     */
    private TestEnvironmentSetup() {
        // Utility class, do not instantiate
    }
    
    /**
     * Initializes the test environment including database, services, and JavaFX toolkit.
     * 
     * @param initializeJavaFx whether to initialize the JavaFX toolkit
     * @param inMemoryDatabase whether to use an in-memory database
     * @param useMockServices whether to use mock services via TestModule
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initialize(boolean initializeJavaFx, boolean inMemoryDatabase, boolean useMockServices) {
        LOGGER.info("Initializing test environment: JavaFX=" + initializeJavaFx + 
                   ", InMemoryDB=" + inMemoryDatabase + ", MockServices=" + useMockServices);
        
        try {
            // Configure headless mode if needed
            if (initializeJavaFx) {
                TestFXHeadlessConfig.configureHeadlessMode();
            }
            
            // Initialize database
            if (inMemoryDatabase) {
                initializeInMemoryDatabase();
            } else {
                initializeTestDatabase();
            }
            
            // Initialize services
            if (useMockServices) {
                TestModule.initialize();
            } else {
                ServiceLocator.initialize();
                MvvmConfig.initialize();
            }
            
            // Initialize JavaFX toolkit if needed
            if (initializeJavaFx && !javaFxInitialized) {
                initializeJavaFxToolkit();
            }
            
            LOGGER.info("Test environment initialization complete");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing test environment", e);
            return false;
        }
    }
    
    /**
     * Initializes an in-memory H2 database for tests.
     */
    private static void initializeInMemoryDatabase() throws Exception {
        if (databaseInitialized) {
            LOGGER.info("In-memory database already initialized");
            return;
        }
        
        LOGGER.info("Initializing in-memory database for tests");
        
        // Set system property to use in-memory database
        System.setProperty("db.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        System.setProperty("db.username", "sa");
        System.setProperty("db.password", "");
        System.setProperty("db.dialect", "org.hibernate.dialect.H2Dialect");
        System.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        
        // Initialize database
        DatabaseConfig.initialize();
        
        // Verify database connection
        EntityManagerFactory emf = DatabaseConfig.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNativeQuery("SELECT 1").getResultList();
            em.getTransaction().commit();
            LOGGER.info("In-memory database initialized successfully");
            databaseInitialized = true;
        } finally {
            em.close();
        }
    }
    
    /**
     * Initializes a test database with a test configuration.
     */
    private static void initializeTestDatabase() throws Exception {
        if (databaseInitialized) {
            LOGGER.info("Test database already initialized");
            return;
        }
        
        LOGGER.info("Initializing test database");
        
        // Set system property to use test database
        System.setProperty("db.url", "jdbc:h2:./data/testdb;DB_CLOSE_DELAY=-1");
        System.setProperty("db.username", "sa");
        System.setProperty("db.password", "");
        System.setProperty("db.dialect", "org.hibernate.dialect.H2Dialect");
        System.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        
        // Initialize database
        DatabaseConfig.initialize();
        
        // Clear test database
        TestDatabaseCleaner.clearTestDatabase();
        
        databaseInitialized = true;
        LOGGER.info("Test database initialized successfully");
    }
    
    /**
     * Initializes the JavaFX toolkit.
     */
    private static void initializeJavaFxToolkit() throws Exception {
        if (javaFxInitialized) {
            LOGGER.info("JavaFX toolkit already initialized");
            return;
        }
        
        LOGGER.info("Initializing JavaFX toolkit");
        
        // Check if we're running in headless mode
        boolean headless = TestFXHeadlessConfig.isHeadlessMode();
        if (headless) {
            LOGGER.info("Running in headless mode, configuring JavaFX accordingly");
            TestFXHeadlessConfig.configureHeadlessMode();
        }
        
        // Initialize JavaFX toolkit
        final CountDownLatch latch = new CountDownLatch(1);
        
        // Launch JavaFX toolkit in a separate thread
        Thread javafxThread = new Thread(() -> {
            try {
                // Initialize the toolkit
                com.sun.javafx.application.PlatformImpl.startup(() -> {
                    // Signal that toolkit is initialized
                    latch.countDown();
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error initializing JavaFX toolkit", e);
            }
        });
        
        javafxThread.setDaemon(true);
        javafxThread.start();
        
        // Wait for initialization to complete
        if (!latch.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException("Timeout waiting for JavaFX toolkit initialization");
        }
        
        javaFxInitialized = true;
        LOGGER.info("JavaFX toolkit initialized successfully");
    }
    
    /**
     * Creates a test Stage with a simple Pane as the root.
     * 
     * @return the created Stage
     */
    public static Stage createTestStage() throws Exception {
        if (!javaFxInitialized) {
            initializeJavaFxToolkit();
        }
        
        LOGGER.info("Creating test Stage");
        
        AtomicReference<Stage> stageRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                stage.setTitle("Test Stage");
                stage.setWidth(800);
                stage.setHeight(600);
                stage.setScene(new Scene(new Pane(), 800, 600));
                stage.show();
                stageRef.set(stage);
            } finally {
                latch.countDown();
            }
        });
        
        // Wait for stage creation to complete
        if (!latch.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException("Timeout waiting for test Stage creation");
        }
        
        // Wait for JavaFX events to process
        WaitForAsyncUtils.waitForFxEvents();
        
        return stageRef.get();
    }
    
    /**
     * Shuts down the test environment including database, services, and JavaFX toolkit.
     */
    public static void shutdown() {
        LOGGER.info("Shutting down test environment");
        
        // Shutdown services
        if (TestModule.isInitialized()) {
            TestModule.shutdown();
        }
        
        if (MvvmConfig.isInitialized()) {
            MvvmConfig.shutdown();
        }
        
        if (ServiceLocator.isInitialized()) {
            ServiceLocator.shutdown();
        }
        
        // Shutdown database
        if (databaseInitialized) {
            try {
                DatabaseConfig.shutdown();
                databaseInitialized = false;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error shutting down database", e);
            }
        }
        
        // Cleanup JavaFX
        if (javaFxInitialized) {
            try {
                Platform.exit();
                javaFxInitialized = false;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error exiting JavaFX platform", e);
            }
        }
        
        LOGGER.info("Test environment shutdown complete");
    }
    
    /**
     * Clears the test database by truncating all tables.
     */
    public static void clearDatabase() {
        if (!databaseInitialized) {
            LOGGER.warning("Cannot clear database - not initialized");
            return;
        }
        
        LOGGER.info("Clearing test database");
        TestDatabaseCleaner.clearTestDatabase();
    }
    
    /**
     * Checks if the database is initialized.
     * 
     * @return true if the database is initialized, false otherwise
     */
    public static boolean isDatabaseInitialized() {
        return databaseInitialized;
    }
    
    /**
     * Checks if JavaFX is initialized.
     * 
     * @return true if JavaFX is initialized, false otherwise
     */
    public static boolean isJavaFxInitialized() {
        return javaFxInitialized;
    }
    
    /**
     * Runs a database test with proper error handling.
     * 
     * @param test the test to run
     * @return true if the test succeeded, false otherwise
     */
    public static boolean runDatabaseTest(Runnable test) {
        LOGGER.info("Running database test");
        
        if (!databaseInitialized) {
            try {
                initializeTestDatabase();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error initializing test database", e);
                return false;
            }
        }
        
        try {
            test.run();
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database test failed", e);
            return false;
        }
    }
    
    /**
     * Runs a JavaFX test with proper error handling.
     * 
     * @param test the test to run
     * @return true if the test succeeded, false otherwise
     */
    public static boolean runJavaFxTest(Runnable test) {
        LOGGER.info("Running JavaFX test");
        
        if (!javaFxInitialized) {
            try {
                initializeJavaFxToolkit();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error initializing JavaFX toolkit", e);
                return false;
            }
        }
        
        try {
            test.run();
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "JavaFX test failed", e);
            return false;
        }
    }
}