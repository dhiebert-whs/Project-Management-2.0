// src/test/java/org/frcpm/mvvm/views/ViewClassDiagnosticTest.java

package org.frcpm.mvvm.views;

import org.junit.jupiter.api.Test;

/**
 * Diagnostic test to check if View classes exist and can be instantiated.
 */
public class ViewClassDiagnosticTest {
    
    @Test
    public void diagnoseViewClasses() {
        System.out.println("=== VIEW CLASS DIAGNOSTIC TEST ===");
        
        // Check ProjectListMvvmView
        diagnoseViewClass("ProjectListMvvmView", ProjectListMvvmView.class);
        
        // Check TeamMemberListMvvmView
        diagnoseViewClass("TeamMemberListMvvmView", TeamMemberListMvvmView.class);
    }
    
    private void diagnoseViewClass(String viewName, Class<?> viewClass) {
        System.out.println("\n=== DIAGNOSING " + viewName + " CLASS ===");
        
        // Check if class exists
        System.out.println("Class exists: " + (viewClass != null));
        System.out.println("Class name: " + viewClass.getName());
        System.out.println("Package: " + viewClass.getPackage().getName());
        
        // Check if it's an FxmlView
        boolean isFxmlView = de.saxsys.mvvmfx.FxmlView.class.isAssignableFrom(viewClass);
        System.out.println("Is FxmlView: " + isFxmlView);
        
        // Check constructors
        System.out.println("Constructors:");
        java.lang.reflect.Constructor<?>[] constructors = viewClass.getConstructors();
        for (java.lang.reflect.Constructor<?> constructor : constructors) {
            System.out.println("  " + constructor);
        }
        
        // Try to instantiate (if has default constructor)
        try {
            Object instance = viewClass.getDeclaredConstructor().newInstance();
            System.out.println("Default constructor works: ✅");
            System.out.println("Instance: " + instance.getClass().getName());
        } catch (NoSuchMethodException e) {
            System.out.println("No default constructor: ❌");
        } catch (Exception e) {
            System.out.println("Instantiation failed: ❌ " + e.getMessage());
            e.printStackTrace();
        }
        
        // Check for required methods (if FxmlView)
        if (isFxmlView) {
            try {
                // Check if it has initialize method
                java.lang.reflect.Method initMethod = viewClass.getMethod("initialize");
                System.out.println("Has initialize() method: ✅");
            } catch (NoSuchMethodException e) {
                System.out.println("No initialize() method: ⚠️");
            }
        }
    }
}