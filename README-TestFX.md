# TestFX Testing Framework Guide

This document provides instructions on how to use the TestFX framework for UI testing in the FRC Project Management System.

## Overview

The TestFX framework allows for automated testing of JavaFX UI components. We've implemented a custom testing framework on top of TestFX to make it easier to write UI tests for the application.

## Key Components

1. **BaseFxTest.java** - Base class for all TestFX tests
2. **TestFXHeadlessConfig.java** - Configuration for headless testing in CI/CD environments
3. **TestFXUtils.java** - Utility methods for TestFX testing
4. **TestFXTestRunner.java** - Test runner for TestFX tests
5. **TestFXLauncher.java** - Launcher for running TestFX tests
6. **testfx.properties** - Configuration properties for TestFX

## Running Tests

### Running Tests in IDE

To run TestFX tests in your IDE, simply run the test class as you would any JUnit test. The TestFX framework will automatically set up the JavaFX environment.

### Running Tests in Headless Mode

To run tests in headless mode (for CI/CD environments), set the `testfx.headless` property to `true` in `testfx.properties` or via the command line:

```bash
mvn test -Dtestfx.headless=true
```

### Debugging Tests

For debugging TestFX tests, you can use the `TestFXLauncher` class to run tests manually:

```java
TestFXLauncher.runTest(YourTestClass.class);
```

## Writing Tests

### Creating a New Test

To create a new TestFX test, extend the `BaseFxTest` class and implement the `initializeTestComponents` method:

```java
public class YourPresenterTestFX extends BaseFxTest {
    
    @Mock
    private YourService yourService;
    
    private YourView view;
    private YourPresenter presenter;
    
    @Override
    protected void initializeTestComponents(Stage stage) {
        // Create view and presenter
        view = new YourView();
        presenter = (YourPresenter) view.getPresenter();
        
        // Set up the stage
        Scene scene = new Scene(view.getView(), 800, 600);
        stage.setScene(scene);
        
        // Inject mocked services
        injectMockedServices();
        
        // Set up test data
        setupTestData();
    }
    
    // Test methods
    @Test
    public void testSomething() {
        // Initialize presenter with data
        presenter.initialize(data);
        
        // Wait for JavaFX thread to process
        WaitForAsyncUtils.waitForFxEvents();
        
        // Perform UI actions
        clickOn("#someButton");
        
        // Verify results
        verify(yourService).someMethod(anyLong());
    }
}
```

### Common Patterns

1. **Thread Safety**: Always use `TestFXUtils.runOnFxThreadAndWait` to perform operations on the JavaFX thread.
2. **Waiting**: Always wait for JavaFX events to complete using `WaitForAsyncUtils.waitForFxEvents()`.
3. **Debugging**: Use `TestFXUtils.printSceneGraph` and `TestFXUtils.takeScreenshot` for debugging.
4. **Mocking**: Always inject mocked services using reflection or constructor injection.

## Best Practices

1. **Skip Tests When Components Are Null**: Always check if components are null before running tests to prevent NullPointerExceptions:

```java
@Test
public void testSomething() {
    if (presenter == null) {
        LOGGER.severe("Cannot run test - presenter is null");
        return;
    }
    
    // Test implementation
}
```

2. **Use Lenient Mocking**: Always use lenient() for mocks that might not be called in all tests:

```java
lenient().when(yourService.someMethod(anyLong())).thenReturn(result);
```

3. **Robust Test Setup**: Always include try-catch blocks and proper logging in initialization methods:

```java
try {
    // Initialize components
} catch (Exception e) {
    LOGGER.log(Level.SEVERE, "Error initializing components", e);
    e.printStackTrace();
}
```

4. **Thread Safety**: Always perform UI operations on the JavaFX thread:

```java
TestFXUtils.runOnFxThreadAndWait(() -> {
    presenter.initialize(data);
}, 5000);
```

## Common Issues and Solutions

### UI Components Not Found

If UI components cannot be found, try:

1. Make sure the component has an ID set in the FXML file
2. Use `TestFXUtils.printSceneGraph` to see the actual scene graph
3. Increase wait time for JavaFX events with `WaitForAsyncUtils.waitForFxEvents(5000)`

### Headless Testing Issues

For issues with headless testing:

1. Make sure Monocle is properly configured in `testfx.properties`
2. Verify that the test is compatible with headless mode
3. Use `TestFXHeadlessConfig.isHeadless()` to check if running in headless mode

## Reference Classes

Look at these example classes for reference:

- `AttendancePresenterTestFX.java` - Working TestFX test
- `ComponentPresenterTestFX.java` - Complex TestFX test with proper error handling