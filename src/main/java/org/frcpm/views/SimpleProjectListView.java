// src/main/java/org/frcpm/views/SimpleProjectListView.java
package org.frcpm.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simpler implementation of ProjectListView that loads FXML directly.
 */
public class SimpleProjectListView {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleProjectListView.class.getName());
    
    /**
     * Loads the view directly without using AfterburnerFX.
     * 
     * @return the loaded view or null if loading fails
     */
    public Parent getView() {
        LOGGER.info("Loading ProjectListView directly");
        try {
            // Get the resource URL for the FXML file
            URL location = getClass().getResource("projectlistview.fxml");
            if (location == null) {
                // Try alternate name if the first one fails
                location = getClass().getResource("/org/frcpm/views/projectlistview.fxml");
            }
            
            if (location == null) {
                LOGGER.severe("Could not find projectlistview.fxml");
                return null;
            }
            
            // Load resource bundle
            ResourceBundle resources = ResourceBundle.getBundle("org.frcpm.views.projectlistview");
            
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(location, resources);
            return loader.load();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading view", e);
            return null;
        }
    }
}