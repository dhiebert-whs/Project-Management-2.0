package org.frcpm.views;

import com.airhacks.afterburner.views.FXMLView;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * View for the project list using AfterburnerFX pattern.
 */
public class ProjectListView extends FXMLView {
    
    private static final Logger LOGGER = Logger.getLogger(ProjectListView.class.getName());
    
    public ProjectListView() {
        super();
        LOGGER.info("ProjectListView constructor called");
    }
    
    // AfterburnerFX v1.7.0 doesn't have these constructors, so remove them
    
    // Instead of overriding getFXMLName() which is private in AfterburnerFX,
    // we'll use a different approach
    
    // This method is used to get the name of the FXML file
    protected String getFxmlName() {
        return "projectlist";
    }
}