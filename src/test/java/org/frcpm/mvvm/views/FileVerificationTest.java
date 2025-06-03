// src/test/java/org/frcpm/mvvm/views/FileVerificationTest.java

package org.frcpm.mvvm.views;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test to verify that FXML files are properly updated in both source and target directories.
 */
public class FileVerificationTest {
    
    @Test
    public void verifyFXMLFiles() {
        System.out.println("=== FILE VERIFICATION TEST ===");
        
        // Check source files
        verifyFile("src/main/resources/org/frcpm/mvvm/views/ProjectListMvvmView.fxml", "SOURCE");
        verifyFile("src/main/resources/org/frcpm/mvvm/views/TeamMemberListMvvmView.fxml", "SOURCE");
        
        // Check compiled files
        verifyFile("target/classes/org/frcpm/mvvm/views/ProjectListMvvmView.fxml", "COMPILED");
        verifyFile("target/classes/org/frcpm/mvvm/views/TeamMemberListMvvmView.fxml", "COMPILED");
    }
    
    private void verifyFile(String filePath, String type) {
        System.out.println("\n=== VERIFYING " + type + " FILE: " + filePath + " ===");
        
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            System.out.println("❌ FILE DOES NOT EXIST: " + filePath);
            return;
        }
        
        try {
            String content = Files.readString(path);
            
            // Check basic properties
            System.out.println("✅ File exists and is readable");
            System.out.println("File size: " + content.length() + " characters");
            
            // Check for fx:controller
            boolean hasController = content.contains("fx:controller");
            System.out.println("Has fx:controller: " + (hasController ? "✅" : "❌"));
            
            if (hasController) {
                // Extract controller line
                String[] lines = content.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.contains("fx:controller")) {
                        System.out.println("Controller line (" + (i + 1) + "): " + line.trim());
                    }
                }
            }
            
            // Check for onAction attributes
            boolean hasOnAction = content.contains("onAction");
            System.out.println("Has onAction attributes: " + (hasOnAction ? "✅" : "❌"));
            
            if (hasOnAction && !hasController) {
                System.out.println("⚠️  WARNING: Has onAction but no fx:controller - this will cause errors!");
            }
            
            // Show first few lines for verification
            String[] lines = content.split("\n");
            System.out.println("First 10 lines:");
            for (int i = 0; i < Math.min(10, lines.length); i++) {
                System.out.printf("%3d: %s%n", i + 1, lines[i]);
            }
            
        } catch (IOException e) {
            System.out.println("❌ ERROR READING FILE: " + e.getMessage());
        }
    }
}