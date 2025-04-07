package org.frcpm.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utility class for testing with reflection.
 */
public class TestUtils {
    
    /**
     * Gets the value of a private field from an object.
     * 
     * @param <T> the type of the field
     * @param object the object to get the field from
     * @param fieldName the name of the field
     * @return the value of the field
     * @throws Exception if an error occurs
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPrivateField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }
    
    /**
     * Sets the value of a private field on an object.
     * 
     * @param object the object to set the field on
     * @param fieldName the name of the field
     * @param value the value to set
     * @throws Exception if an error occurs
     */
    public static void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
    
    /**
     * Sets the value of a private static field on a class.
     * 
     * @param clazz the class to set the static field on
     * @param fieldName the name of the field
     * @param value the value to set
     * @throws Exception if an error occurs
     */
    public static void setPrivateStaticField(Class<?> clazz, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        // Remove final modifier from field
        modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
        field.set(null, value);
    }
    
    /**
     * Invokes a private method on an object.
     * 
     * @param <T> the return type of the method
     * @param object the object to invoke the method on
     * @param methodName the name of the method
     * @param parameterTypes the parameter types of the method
     * @param args the arguments to pass to the method
     * @return the result of the method invocation
     * @throws Exception if an error occurs
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokePrivateMethod(Object object, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = object.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(object, args);
    }
    
    /**
     * Invokes a private method on an object with no parameters.
     * 
     * @param <T> the return type of the method
     * @param object the object to invoke the method on
     * @param methodName the name of the method
     * @return the result of the method invocation
     * @throws Exception if an error occurs
     */
    public static <T> T invokePrivateMethod(Object object, String methodName) throws Exception {
        return invokePrivateMethod(object, methodName, new Class<?>[0]);
    }
}