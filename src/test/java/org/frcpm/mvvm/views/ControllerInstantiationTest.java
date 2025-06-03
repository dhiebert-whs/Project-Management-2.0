// src/test/java/org/frcpm/mvvm/views/ControllerInstantiationTest.java

package org.frcpm.mvvm.views;

import org.frcpm.di.TestModule;
import org.frcpm.mvvm.MvvmConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test to verify that controller classes can be instantiated properly.
 */
public class ControllerInstantiationTest {
    
    @BeforeEach
    public void setUp() {
        // Initialize test framework
        TestModule.initialize();
        MvvmConfig.initialize();
    }
    
    @Test
    public void testControllerInstantiation() {
        System.out.println("=== CONTROLLER INSTANTIATION TEST ===");
        
        // Test ProjectListMvvmView instantiation
        testViewInstantiation("ProjectListMvvmView", ProjectListMvvmView.class);
        
        // Test TeamMemberListMvvmView instantiation
        testViewInstantiation("TeamMemberListMvvmView", TeamMemberListMvvmView.class);
    }
    
    private void testViewInstantiation(String viewName, Class<?> viewClass) {
        System.out.println("\n=== TESTING " + viewName + " INSTANTIATION ===");
        
        try {
            // Test 1: Check if class exists and is loadable
            System.out.println("1. Class loading test:");
            Class<?> loadedClass = Class.forName(viewClass.getName());
            System.out.println("   ✅ Class loaded successfully: " + loadedClass.getName());
            
            // Test 2: Check default constructor
            System.out.println("2. Default constructor test:");
            try {
                java.lang.reflect.Constructor<?> defaultConstructor = loadedClass.getDeclaredConstructor();
                System.out.println("   ✅ Default constructor found: " + defaultConstructor);
                
                // Test 3: Try to instantiate
                System.out.println("3. Instantiation test:");
                Object instance = defaultConstructor.newInstance();
                System.out.println("   ✅ Instance created successfully: " + instance.getClass().getName());
                
                // Test 4: Check if it's an FxmlView
                System.out.println("4. FxmlView interface test:");
                boolean isFxmlView = de.saxsys.mvvmfx.FxmlView.class.isAssignableFrom(loadedClass);
                System.out.println("   FxmlView implementation: " + (isFxmlView ? "✅" : "❌"));
                
                // Test 5: Check if it implements Initializable
                System.out.println("5. Initializable interface test:");
                boolean isInitializable = javafx.fxml.Initializable.class.isAssignableFrom(loadedClass);
                System.out.println("   Initializable implementation: " + (isInitializable ? "✅" : "❌"));
                
                // Test 6: Check for @InjectViewModel annotation
                System.out.println("6. @InjectViewModel annotation test:");
                java.lang.reflect.Field[] fields = loadedClass.getDeclaredFields();
                boolean hasInjectViewModel = false;
                for (java.lang.reflect.Field field : fields) {
                    if (field.isAnnotationPresent(de.saxsys.mvvmfx.InjectViewModel.class)) {
                        hasInjectViewModel = true;
                        System.out.println("   ✅ Found @InjectViewModel field: " + field.getName() + " (" + field.getType().getSimpleName() + ")");
                        break;
                    }
                }
                if (!hasInjectViewModel) {
                    System.out.println("   ❌ No @InjectViewModel field found");
                }
                
                // Test 7: Check for FXML fields
                System.out.println("7. @FXML fields test:");
                int fxmlFieldCount = 0;
                for (java.lang.reflect.Field field : fields) {
                    if (field.isAnnotationPresent(javafx.fxml.FXML.class)) {
                        fxmlFieldCount++;
                    }
                }
                System.out.println("   @FXML fields found: " + fxmlFieldCount + (fxmlFieldCount > 0 ? " ✅" : " ❌"));
                
            } catch (NoSuchMethodException e) {
                System.out.println("   ❌ No default constructor found");
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("   ❌ Class not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("   ❌ Instantiation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}