package org.frcpm.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utility class for testing, providing methods to access and modify private fields.
 */
public class TestUtils {
    
    /**
     * Sets the value of a private static field.
     * 
     * @param clazz the class containing the field
     * @param fieldName the name of the field
     * @param value the value to set
     * @throws Exception if reflection access fails
     */
    public static void setPrivateStaticField(Class<?> clazz, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        
        // In Java 9+, we can't modify the modifiers field directly anymore.
        // Instead, we'll just set the field value directly.
        field.set(null, value);
    }
    
    /**
     * Gets the value of a private field.
     * 
     * @param object the object containing the field
     * @param fieldName the name of the field
     * @return the field value
     * @throws Exception if reflection access fails
     */
    public static Object getPrivateField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
    
    /**
     * Sets the value of a private field.
     * 
     * @param object the object containing the field
     * @param fieldName the name of the field
     * @param value the value to set
     * @throws Exception if reflection access fails
     */
    public static void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
    
    /**
     * Calls a private method.
     * 
     * @param object the object containing the method
     * @param methodName the name of the method
     * @param parameterTypes the parameter types
     * @param args the arguments
     * @return the method result
     * @throws Exception if reflection access fails
     */
    public static Object callPrivateMethod(Object object, String methodName, 
                                           Class<?>[] parameterTypes, Object[] args) throws Exception {
        Method method = object.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(object, args);
    }
}