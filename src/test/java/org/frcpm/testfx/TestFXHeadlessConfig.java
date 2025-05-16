// src/test/java/org/frcpm/testfx/TestFXHeadlessConfig.java
package org.frcpm.testfx;

import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration for headless testing with TestFX.
 * Provides methods to detect CI environments and configure properties accordingly.
 */
public class TestFXHeadlessConfig {
    
    private static final Logger LOGGER = Logger.getLogger(TestFXHeadlessConfig.class.getName());
    
    private static final String TESTFX_PROP_FILE = "testfx.properties";
    private static final String CONFIG_PROP_FILE = "config.properties";
    
    private static boolean headlessMode = false;
    private static boolean configured = false;
    
    /**
     * Private constructor to prevent instantiation.
     */
    private TestFXHeadlessConfig() {
        // Utility class, do not instantiate
    }
    
    /**
     * Configures headless mode for TestFX.
     * This method sets system properties needed for headless testing.
     */
    public static void configureHeadlessMode() {
        if (configured) {
            return;
        }
        
        LOGGER.info("Configuring TestFX headless mode");
        
        // Load properties
        Properties properties = loadProperties();
        
        // Check if running in CI environment
        boolean isCi = detectCiEnvironment();
        
        // Override property from file if CI is detected
        boolean useHeadless = properties.getProperty("testfx.headless", "false").equalsIgnoreCase("true") || isCi;
        
        if (useHeadless) {
            LOGGER.info("Enabling headless mode for TestFX tests");
            
            // Set system properties for headless mode
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
            System.setProperty("headless.geometry", "1280x720-32");
            
            // Set glass robot properties
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.platform", "Headless");
            
            // For Java 17+
            System.setProperty("javafx.animation.fullspeed", "true");
            System.setProperty("javafx.animation.framerate", "1");
            
            headlessMode = true;
        } else {
            LOGGER.info("Using standard mode for TestFX tests");
            headlessMode = false;
        }
        
        // Apply test-specific property overrides from file
        for (String propertyName : properties.stringPropertyNames()) {
            if (propertyName.startsWith("testfx.") && !propertyName.equals("testfx.headless")) {
                System.setProperty(propertyName, properties.getProperty(propertyName));
                LOGGER.fine("Set TestFX property: " + propertyName + " = " + properties.getProperty(propertyName));
            }
        }
        
        configured = true;
    }
    
    /**
     * Loads properties from testfx.properties file.
     * 
     * @return the loaded properties
     */
    private static Properties loadProperties() {
        Properties properties = new Properties();
        
        // First try to load from classpath
        try (InputStream is = TestFXHeadlessConfig.class.getClassLoader().getResourceAsStream(TESTFX_PROP_FILE)) {
            if (is != null) {
                properties.load(is);
                LOGGER.fine("Loaded TestFX properties from classpath");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading TestFX properties from classpath", e);
        }
        
        // Then try to load from file system (project root)
        if (properties.isEmpty()) {
            Path propPath = Paths.get(TESTFX_PROP_FILE);
            if (Files.exists(propPath)) {
                try (InputStream is = Files.newInputStream(propPath)) {
                    properties.load(is);
                    LOGGER.fine("Loaded TestFX properties from file system");
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error loading TestFX properties from file system", e);
                }
            }
        }
        
        // Try to load config.properties as fallback
        if (properties.isEmpty()) {
            try (InputStream is = TestFXHeadlessConfig.class.getClassLoader().getResourceAsStream(CONFIG_PROP_FILE)) {
                if (is != null) {
                    properties.load(is);
                    LOGGER.fine("Loaded config properties as fallback");
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error loading config properties", e);
            }
        }
        
        return properties;
    }
    
    /**
     * Detects if the test is running in a CI environment.
     * 
     * @return true if running in CI, false otherwise
     */
    private static boolean detectCiEnvironment() {
        // Check for common CI environment variables
        String[] ciEnvVars = {
            "CI",
            "TRAVIS",
            "CIRCLECI",
            "JENKINS_URL",
            "GITHUB_ACTIONS",
            "GITLAB_CI",
            "BUILDKITE",
            "TF_BUILD" // Azure DevOps
        };
        
        for (String var : ciEnvVars) {
            if (System.getenv(var) != null) {
                LOGGER.info("Detected CI environment: " + var);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets the screen capture utility.
     * Uses a different implementation for headless mode.
     * 
     * @return the screen capture utility
     */
    public static Object getScreenCaptureUtility() {
        if (isHeadlessMode()) {
            // Return a headless-compatible screen capture utility
            // This is a stub - in a real implementation, you would return
            // a proper headless screen capture utility
            LOGGER.info("Using headless screen capture utility");
            return new HeadlessScreenCaptureUtility();
        } else {
            // Return the standard screen capture utility
            LOGGER.info("Using standard screen capture utility");
            return new StandardScreenCaptureUtility();
        }
    }
    
    /**
     * Creates a test-specific environment configuration.
     * 
     * @param windowWidth the window width
     * @param windowHeight the window height
     * @param timeout the timeout in milliseconds
     */
    public static void configureTestEnvironment(int windowWidth, int windowHeight, int timeout) {
        LOGGER.info("Configuring test environment: " + windowWidth + "x" + windowHeight + ", timeout=" + timeout);
        
        // Configure window dimensions
        System.setProperty("testfx.window.width", String.valueOf(windowWidth));
        System.setProperty("testfx.window.height", String.valueOf(windowHeight));
        
        // Configure timeout
        System.setProperty("testfx.timeout", String.valueOf(timeout));
        
        // Configure headless mode geometry if needed
        if (isHeadlessMode()) {
            System.setProperty("headless.geometry", windowWidth + "x" + windowHeight + "-32");
        }
    }
    
    /**
     * Checks if headless mode is enabled.
     * 
     * @return true if headless mode is enabled, false otherwise
     */
    public static boolean isHeadlessMode() {
        if (!configured) {
            configureHeadlessMode();
        }
        return headlessMode;
    }
    
    /**
     * Resets headless configuration.
     * Useful for tests that need to change the configuration.
     */
    public static void resetConfiguration() {
        configured = false;
        headlessMode = false;
    }
    
    /**
     * Stub class for headless screen capture utility.
     */
    private static class HeadlessScreenCaptureUtility {
        public void captureScreen(String filename) {
            LOGGER.info("Headless screen capture: " + filename);
            // In a real implementation, this would use a headless-compatible
            // way to capture the screen
        }
    }
    
    /**
     * Stub class for standard screen capture utility.
     */
    private static class StandardScreenCaptureUtility {
        public void captureScreen(String filename) {
            LOGGER.info("Standard screen capture: " + filename);
            try {
                // Create screenshots directory if it doesn't exist
                Path screenshotsDir = Paths.get("target/screenshots");
                if (!Files.exists(screenshotsDir)) {
                    Files.createDirectories(screenshotsDir);
                }
                
                // In a real implementation, this would capture a screenshot
                // using JavaFX or AWT robot capabilities
                javafx.stage.Window window = javafx.stage.Window.getWindows().stream()
                    .filter(javafx.stage.Window::isShowing)
                    .findFirst()
                    .orElse(null);
                
                if (window != null) {
                    javafx.scene.Scene scene = window instanceof javafx.stage.Stage ? 
                        ((javafx.stage.Stage) window).getScene() : null;
                    
                    if (scene != null) {
                        Path file = screenshotsDir.resolve(filename + ".png");
                        
                        // Create screenshot
                        javafx.scene.image.WritableImage image = 
                            new javafx.scene.image.WritableImage(
                                (int) scene.getWidth(),
                                (int) scene.getHeight()
                            );
                        scene.snapshot(image);
                        
                        // Save to file on JavaFX thread
                        Platform.runLater(() -> {
                            try {
                                // Convert to AWT BufferedImage for saving
                                java.awt.image.BufferedImage bufferedImage = 
                                    new java.awt.image.BufferedImage(
                                        (int) image.getWidth(),
                                        (int) image.getHeight(),
                                        java.awt.image.BufferedImage.TYPE_INT_ARGB
                                    );
                                
                                // Copy pixels
                                for (int x = 0; x < image.getWidth(); x++) {
                                    for (int y = 0; y < image.getHeight(); y++) {
                                        javafx.scene.paint.Color color = image.getPixelReader().getColor(x, y);
                                        int argb = (
                                            (int) (color.getOpacity() * 255) << 24 |
                                            (int) (color.getRed() * 255) << 16 |
                                            (int) (color.getGreen() * 255) << 8 |
                                            (int) (color.getBlue() * 255)
                                        );
                                        bufferedImage.setRGB(x, y, argb);
                                    }
                                }
                                
                                // Save to file
                                javax.imageio.ImageIO.write(bufferedImage, "png", file.toFile());
                                LOGGER.info("Screenshot saved: " + file.toString());
                            } catch (Exception e) {
                                LOGGER.log(Level.WARNING, "Error saving screenshot", e);
                            }
                        });
                    } else {
                        LOGGER.warning("No scene found for capturing screenshot");
                    }
                } else {
                    LOGGER.warning("No window found for capturing screenshot");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during screen capture", e);
            }
        }
    }
    
    /**
     * Takes a screenshot of the current scene.
     * Works in both headless and standard mode.
     * 
     * @param name the screenshot name (without extension)
     */
    public static void takeScreenshot(String name) {
        try {
            Object captureUtility = getScreenCaptureUtility();
            if (captureUtility instanceof HeadlessScreenCaptureUtility) {
                ((HeadlessScreenCaptureUtility) captureUtility).captureScreen(name);
            } else if (captureUtility instanceof StandardScreenCaptureUtility) {
                ((StandardScreenCaptureUtility) captureUtility).captureScreen(name);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error taking screenshot: " + name, e);
        }
    }
}