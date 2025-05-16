// src/test/java/org/frcpm/utils/TestUtils.java
package org.frcpm.utils;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import org.frcpm.di.ServiceLocator;
import org.frcpm.mvvm.BaseMvvmViewModel;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class providing helper methods for testing.
 */
public final class TestUtils {
    
    private static final Logger LOGGER = Logger.getLogger(TestUtils.class.getName());
    
    /** Default timeout for operations in milliseconds */
    private static final int DEFAULT_TIMEOUT = 5000;
    
    /**
     * Private constructor to prevent instantiation.
     */
    private TestUtils() {
        // Utility class, do not instantiate
    }
    
    /**
     * Executes code on the JavaFX application thread and waits for completion.
     * 
     * @param runnable the code to run
     */
    public static void runOnFxThreadAndWait(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }
        
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                runnable.run();
            } finally {
                latch.countDown();
            }
        });
        
        try {
            boolean finished = latch.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!finished) {
                LOGGER.warning("Timeout waiting for JavaFX operation to complete");
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted while waiting for JavaFX operation", e);
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Executes a function on the JavaFX application thread and returns the result.
     * 
     * @param <T> the result type
     * @param callable the function to call
     * @return the function result
     * @throws Exception if the function throws an exception
     */
    public static <T> T callOnFxThreadAndWait(Callable<T> callable) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return callable.call();
        }
        
        final CountDownLatch latch = new CountDownLatch(1);
        final T[] result = (T[]) new Object[1];
        final Exception[] exception = new Exception[1];
        
        Platform.runLater(() -> {
            try {
                result[0] = callable.call();
            } catch (Exception e) {
                exception[0] = e;
            } finally {
                latch.countDown();
            }
        });
        
        boolean finished = latch.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        if (!finished) {
            throw new RuntimeException("Timeout waiting for JavaFX operation to complete");
        }
        
        if (exception[0] != null) {
            throw exception[0];
        }
        
        return result[0];
    }
    
    /**
     * Waits for the JavaFX application thread to process all events.
     */
    public static void waitForFxEvents() {
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    /**
     * Waits for a condition to be true with a default timeout.
     * 
     * @param condition the condition to check
     */
    public static void waitUntil(Callable<Boolean> condition) {
        waitUntil(condition, DEFAULT_TIMEOUT);
    }
    
    /**
     * Waits for a condition to be true with a specified timeout.
     * 
     * @param condition the condition to check
     * @param timeoutInMillis the timeout in milliseconds
     */
    public static void waitUntil(Callable<Boolean> condition, int timeoutInMillis) {
        long endTime = System.currentTimeMillis() + timeoutInMillis;
        
        while (System.currentTimeMillis() < endTime) {
            try {
                if (Boolean.TRUE.equals(condition.call())) {
                    return;
                }
            } catch (Exception e) {
                // Ignore exceptions and continue waiting
            }
            
            try {
                // Wait a bit before checking again
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        
        throw new RuntimeException("Condition not met within timeout: " + timeoutInMillis + " ms");
    }
    
    /**
     * Accesses a private field.
     * 
     * @param <T> the field type
     * @param object the object containing the field
     * @param fieldName the field name
     * @return the field value
     * @throws Exception if the field cannot be accessed
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPrivateField(Object object, String fieldName) throws Exception {
        Field field = findField(object.getClass(), fieldName);
        if (field == null) {
            throw new NoSuchFieldException("Field not found: " + fieldName);
        }
        
        field.setAccessible(true);
        return (T) field.get(object);
    }
    
    /**
     * Sets a private field.
     * 
     * @param object the object containing the field
     * @param fieldName the field name
     * @param value the value to set
     * @throws Exception if the field cannot be accessed
     */
    public static void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = findField(object.getClass(), fieldName);
        if (field == null) {
            throw new NoSuchFieldException("Field not found: " + fieldName);
        }
        
        field.setAccessible(true);
        field.set(object, value);
    }
    
    /**
     * Invokes a private method.
     * 
     * @param <T> the return type
     * @param object the object containing the method
     * @param methodName the method name
     * @param parameterTypes the parameter types
     * @param args the method arguments
     * @return the method result
     * @throws Exception if the method cannot be invoked
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokePrivateMethod(Object object, String methodName, 
                                            Class<?>[] parameterTypes, Object[] args) throws Exception {
        Method method = findMethod(object.getClass(), methodName, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("Method not found: " + methodName);
        }
        
        method.setAccessible(true);
        return (T) method.invoke(object, args);
    }
    
    /**
     * Finds a field in a class or its superclasses.
     * 
     * @param clazz the class to search
     * @param fieldName the field name
     * @return the field or null if not found
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // Try superclass if exists
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return findField(superClass, fieldName);
            }
            return null;
        }
    }
    
    /**
     * Finds a method in a class or its superclasses.
     * 
     * @param clazz the class to search
     * @param methodName the method name
     * @param parameterTypes the parameter types
     * @return the method or null if not found
     */
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            // Try superclass if exists
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && superClass != Object.class) {
                return findMethod(superClass, methodName, parameterTypes);
            }
            return null;
        }
    }
    
    /**
     * Initializes ServiceLocator for tests.
     */
    public static void initServiceLocator() {
        if (!ServiceLocator.isInitialized()) {
            ServiceLocator.initialize();
        }
    }
    
    /**
     * Clears ServiceLocator state.
     */
    public static void clearServiceLocator() {
        ServiceLocator.clear();
    }
    
    /**
     * Gets a list of all field values from an object.
     * 
     * @param object the object to get fields from
     * @return a list of field values
     */
    public static List<Object> getAllFieldValues(Object object) {
        try {
            Field[] fields = object.getClass().getDeclaredFields();
            Object[] values = new Object[fields.length];
            
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                values[i] = field.get(object);
            }
            
            return Arrays.asList(values);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting field values", e);
            return List.of();
        }
    }
    
    /**
     * Sets a value on a StringProperty.
     * 
     * @param property the property to set
     * @param value the value to set
     */
    public static void setStringProperty(StringProperty property, String value) {
        runOnFxThreadAndWait(() -> property.set(value));
        waitForFxEvents();
    }
    
    /**
     * Sets a value on a BooleanProperty.
     * 
     * @param property the property to set
     * @param value the value to set
     */
    public static void setBooleanProperty(BooleanProperty property, boolean value) {
        runOnFxThreadAndWait(() -> property.set(value));
        waitForFxEvents();
    }
    
    /**
     * Sets a value on any property.
     * 
     * @param <T> the property value type
     * @param property the property to set
     * @param value the value to set
     */
    public static <T> void setProperty(Property<T> property, T value) {
        runOnFxThreadAndWait(() -> property.setValue(value));
        waitForFxEvents();
    }
    
    /**
     * Sets text in a text input control.
     * 
     * @param control the control to set text in
     * @param text the text to set
     */
    public static void setTextInControl(TextInputControl control, String text) {
        runOnFxThreadAndWait(() -> {
            control.clear();
            control.setText(text);
        });
        waitForFxEvents();
    }
    
    /**
     * Sets a value in a ComboBox.
     * 
     * @param <T> the value type
     * @param comboBox the ComboBox to set
     * @param value the value to set
     */
    public static <T> void setComboBoxValue(ComboBox<T> comboBox, T value) {
        runOnFxThreadAndWait(() -> comboBox.setValue(value));
        waitForFxEvents();
    }
    
    /**
     * Sets a date in a DatePicker.
     * 
     * @param datePicker the DatePicker to set
     * @param date the date to set
     */
    public static void setDatePickerValue(DatePicker datePicker, LocalDate date) {
        runOnFxThreadAndWait(() -> datePicker.setValue(date));
        waitForFxEvents();
    }
    
    /**
     * Selects an item in a TableView.
     * 
     * @param <T> the item type
     * @param tableView the TableView
     * @param index the index to select
     */
    public static <T> void selectTableViewItem(TableView<T> tableView, int index) {
        runOnFxThreadAndWait(() -> tableView.getSelectionModel().select(index));
        waitForFxEvents();
    }
    
    /**
     * Selects a toggle in a ToggleGroup.
     * 
     * @param toggleGroup the ToggleGroup
     * @param toggle the Toggle to select
     */
    public static void selectToggle(ToggleGroup toggleGroup, Toggle toggle) {
        runOnFxThreadAndWait(() -> toggleGroup.selectToggle(toggle));
        waitForFxEvents();
    }
    
    /**
     * Gets the validation state of a ViewModel.
     * 
     * @param viewModel the ViewModel
     * @return true if valid, false otherwise
     */
    public static boolean isViewModelValid(BaseMvvmViewModel viewModel) {
        try {
            return invokePrivateMethod(viewModel, "isValid", new Class<?>[]{}, new Object[]{});
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking ViewModel validity", e);
            return false;
        }
    }
    
    /**
     * Gets the error message from a ViewModel.
     * 
     * @param viewModel the ViewModel
     * @return the error message or null if no error
     */
    public static String getViewModelErrorMessage(BaseMvvmViewModel viewModel) {
        return viewModel.getErrorMessage();
    }
    
    /**
     * Checks if a node exists in the current scene.
     * 
     * @param node the node to check
     * @return true if the node exists, false otherwise
     */
    public static boolean nodeExists(Node node) {
        if (node == null) {
            return false;
        }
        
        try {
            return node.getScene() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Creates a test instance of a view model with mocked services.
     * 
     * @param <T> the view model type
     * @param viewModelClass the view model class
     * @param mockServices the mock services to inject
     * @return the view model instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends BaseMvvmViewModel> T createTestViewModel(
            Class<T> viewModelClass, Object... mockServices) {
        try {
            // Create instance using default constructor
            T viewModel = viewModelClass.getDeclaredConstructor().newInstance();
            
            // Inject mock services
            Field[] fields = viewModelClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                for (Object mockService : mockServices) {
                    if (field.getType().isAssignableFrom(mockService.getClass())) {
                        field.set(viewModel, mockService);
                        break;
                    }
                }
            }
            
            return viewModel;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating test view model", e);
            throw new RuntimeException("Failed to create test view model", e);
        }
    }
}