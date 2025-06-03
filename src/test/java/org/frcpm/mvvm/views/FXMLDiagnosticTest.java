// src/test/java/org/frcpm/mvvm/views/FXMLDiagnosticTest.java

package org.frcpm.mvvm.views;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Diagnostic test to examine the actual FXML files being loaded.
 */
public class FXMLDiagnosticTest {
    
    @Test
    public void diagnoseFXMLFiles() {
        System.out.println("=== FXML DIAGNOSTIC TEST ===");
        
        // Check ProjectListMvvmView.fxml
        diagnoseFXMLFile("ProjectListMvvmView", ProjectListMvvmView.class);
        
        // Check TeamMemberListMvvmView.fxml  
        diagnoseFXMLFile("TeamMemberListMvvmView", TeamMemberListMvvmView.class);
    }
    
    private void diagnoseFXMLFile(String viewName, Class<?> viewClass) {
        System.out.println("\n=== DIAGNOSING " + viewName + " ===");
        
        // Check source file location
        String packagePath = viewClass.getPackage().getName().replace('.', '/');
        String resourcePath = "/" + packagePath + "/" + viewName + ".fxml";
        
        System.out.println("Expected resource path: " + resourcePath);
        
        URL resourceUrl = viewClass.getResource(resourcePath);
        if (resourceUrl != null) {
            System.out.println("Resource URL: " + resourceUrl);
            
            try {
                // Read the FXML file content
                InputStream inputStream = resourceUrl.openStream();
                String content = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
                
                System.out.println("FXML Content:");
                System.out.println("----------------------------------------");
                
                // Show first 30 lines with line numbers
                String[] lines = content.split("\n");
                for (int i = 0; i < Math.min(30, lines.length); i++) {
                    System.out.printf("%3d: %s%n", i + 1, lines[i]);
                }
                
                if (lines.length > 30) {
                    System.out.println("... (truncated)");
                }
                
                System.out.println("----------------------------------------");
                
                // Check for fx:controller attribute
                boolean hasController = content.contains("fx:controller");
                System.out.println("Has fx:controller attribute: " + hasController);
                
                if (hasController) {
                    // Extract the controller line
                    for (String line : lines) {
                        if (line.contains("fx:controller")) {
                            System.out.println("Controller line: " + line.trim());
                        }
                    }
                } else {
                    System.out.println("❌ MISSING fx:controller attribute!");
                }
                
                // Check for onAction attributes (which require fx:controller)
                boolean hasOnAction = content.contains("onAction");
                System.out.println("Has onAction attributes: " + hasOnAction);
                
                if (hasOnAction && !hasController) {
                    System.out.println("⚠️  WARNING: onAction attributes without fx:controller will fail!");
                }
                
            } catch (Exception e) {
                System.err.println("Error reading FXML file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("❌ FXML file not found at: " + resourcePath);
        }
    }
}