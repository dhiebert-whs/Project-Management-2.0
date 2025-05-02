package org.frcpm.testfx;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Simple example test class to verify TestFX framework setup.
 */
public class BaseFxTestExample extends BaseFxTest {

    private Label resultLabel;
    private TextField inputField;
    private Button testButton;

    @Override
    protected void initializeTestComponents(Stage stage) {
        // Create a simple UI for testing
        resultLabel = new Label("Initial State");
        inputField = new TextField();
        testButton = new Button("Test Button");
        
        // Set IDs for TestFX to find
        resultLabel.setId("resultLabel");
        inputField.setId("inputField");
        testButton.setId("testButton");
        
        // Add a simple action to the button
        testButton.setOnAction(e -> {
            String input = inputField.getText();
            if (input != null && !input.trim().isEmpty()) {
                resultLabel.setText("Input: " + input);
            } else {
                resultLabel.setText("No input provided");
            }
        });
        
        // Create layout and scene
        VBox root = new VBox(10, inputField, testButton, resultLabel);
        root.setPadding(new javafx.geometry.Insets(20));
        
        // Set the scene
        Scene scene = new Scene(root, 400, 200);
        stage.setScene(scene);
        stage.setTitle("TestFX Example");
        
        // Wait for JavaFX to process
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    @Test
    public void testButtonClick() {
        // Verify the initial state
        Label label = lookup("#resultLabel").queryAs(Label.class);
        assertEquals("Initial State", label.getText(), "Initial label text should be 'Initial State'");
        
        // Enter text in the input field
        clickOn("#inputField");
        write("TestFX is working");
        
        // Click the button
        clickOn("#testButton");
        
        // Verify the result
        assertEquals("Input: TestFX is working", label.getText(), "Label should display the input text");
    }
    
    @Test
    public void testEmptyInput() {
        // Verify the initial state
        Label label = lookup("#resultLabel").queryAs(Label.class);
        assertEquals("Initial State", label.getText(), "Initial label text should be 'Initial State'");
        
        // Click the button without entering text
        clickOn("#testButton");
        
        // Verify the result
        assertEquals("No input provided", label.getText(), "Label should display 'No input provided'");
    }
}