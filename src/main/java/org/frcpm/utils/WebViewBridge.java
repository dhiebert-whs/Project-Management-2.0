// src/main/java/org/frcpm/utils/WebViewBridge.java
package org.frcpm.utils;

import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for creating bidirectional bridges between JavaFX and JavaScript.
 * This facilitates communication between the Java application and JavaScript code
 * running in a WebView.
 */
public class WebViewBridge {
    
    private static final Logger LOGGER = Logger.getLogger(WebViewBridge.class.getName());
    
    /**
     * Creates a bridge between Java and JavaScript in a WebView.
     * 
     * @param engine the WebEngine to connect to
     * @param bridgeName the name to use for the bridge in JavaScript
     * @param bridgeObject the Java object to expose to JavaScript
     * @return true if the bridge was created successfully, false otherwise
     */
    public static boolean createBridge(WebEngine engine, String bridgeName, Object bridgeObject) {
        if (engine == null) {
            LOGGER.warning("Cannot create bridge: WebEngine is null");
            return false;
        }
        
        try {
            // Get the JavaScript window object
            JSObject window = (JSObject) engine.executeScript("window");
            
            // Add the bridge object to the JavaScript window
            window.setMember(bridgeName, bridgeObject);
            
            LOGGER.info("Bridge created successfully with name: " + bridgeName);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating bridge", e);
            return false;
        }
    }
    
    /**
     * Executes a JavaScript function in the WebView.
     * 
     * @param engine the WebEngine to execute on
     * @param functionName the name of the JavaScript function to call
     * @param args the arguments to pass to the function
     * @return the result of the JavaScript function, or null if an error occurred
     */
    public static Object callJavaScriptFunction(WebEngine engine, String functionName, Object... args) {
        if (engine == null) {
            LOGGER.warning("Cannot call function: WebEngine is null");
            return null;
        }
        
        try {
            // Create function call string
            StringBuilder call = new StringBuilder(functionName).append("(");
            
            // Add arguments
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    call.append(",");
                }
                
                // Handle different types of arguments
                if (args[i] == null) {
                    call.append("null");
                } else if (args[i] instanceof String) {
                    // Escape quotes in strings
                    String escaped = ((String) args[i]).replace("'", "\\'");
                    call.append("'").append(escaped).append("'");
                } else if (args[i] instanceof Boolean) {
                    call.append(args[i].toString());
                } else if (args[i] instanceof Number) {
                    call.append(args[i].toString());
                } else {
                    // For other objects, try to use JSON
                    call.append("JSON.parse('")
                        .append(args[i].toString().replace("'", "\\'"))
                        .append("')");
                }
            }
            
            call.append(")");
            
            // Execute the function
            return engine.executeScript(call.toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calling JavaScript function: " + functionName, e);
            return null;
        }
    }
    
    /**
     * Sets a variable in the JavaScript context.
     * 
     * @param engine the WebEngine to set the variable in
     * @param varName the name of the variable to set
     * @param value the value to set
     * @return true if the variable was set successfully, false otherwise
     */
    public static boolean setJavaScriptVariable(WebEngine engine, String varName, Object value) {
        if (engine == null) {
            LOGGER.warning("Cannot set variable: WebEngine is null");
            return false;
        }
        
        try {
            // Create variable assignment string
            StringBuilder assignment = new StringBuilder("window.").append(varName).append(" = ");
            
            // Handle different types of values
            if (value == null) {
                assignment.append("null");
            } else if (value instanceof String) {
                // Escape quotes in strings
                String escaped = ((String) value).replace("'", "\\'");
                assignment.append("'").append(escaped).append("'");
            } else if (value instanceof Boolean) {
                assignment.append(value.toString());
            } else if (value instanceof Number) {
                assignment.append(value.toString());
            } else {
                // For other objects, try to use JSON
                assignment.append("JSON.parse('")
                    .append(value.toString().replace("'", "\\'"))
                    .append("')");
            }
            
            // Execute the assignment
            engine.executeScript(assignment.toString());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting JavaScript variable: " + varName, e);
            return false;
        }
    }
    
    /**
     * Evaluates a JavaScript expression in the WebView.
     * 
     * @param engine the WebEngine to evaluate in
     * @param expression the JavaScript expression to evaluate
     * @return the result of the evaluation, or null if an error occurred
     */
    public static Object evaluateJavaScript(WebEngine engine, String expression) {
        if (engine == null) {
            LOGGER.warning("Cannot evaluate expression: WebEngine is null");
            return null;
        }
        
        try {
            return engine.executeScript(expression);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error evaluating JavaScript expression", e);
            return null;
        }
    }
}