package org.frcpm.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controller for the main application view.
 */
public class MainController {
    
    @FXML
    private TableView<?> projectsTable;
    
    @FXML
    private TableColumn<?, ?> projectNameColumn;
    
    @FXML
    private TableColumn<?, ?> projectStartColumn;
    
    @FXML
    private TableColumn<?, ?> projectGoalColumn;
    
    @FXML
    private TableColumn<?, ?> projectDeadlineColumn;
    
    @FXML
    private Tab projectTab;
    
    /**
     * Initializes the controller. This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        // TODO: Set up the table columns and cell factories
        
        // TODO: Load project data into the table
    }
    
    /**
     * Handles the new project menu item and button.
     */
    @FXML
    private void handleNewProject(ActionEvent event) {
        showNotImplementedAlert("New Project");
    }
    
    /**
     * Handles the open project menu item and button.
     */
    @FXML
    private void handleOpenProject(ActionEvent event) {
        showNotImplementedAlert("Open Project");
    }
    
    /**
     * Handles the import project button.
     */
    @FXML
    private void handleImportProject(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Project File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File selectedFile = fileChooser.showOpenDialog(projectsTable.getScene().getWindow());
        if (selectedFile != null) {
            showNotImplementedAlert("Import Project from " + selectedFile.getName());
        }
    }
    
    /**
     * Handles the subteams menu item.
     */
    @FXML
    private void handleSubteams(ActionEvent event) {
        showNotImplementedAlert("Subteams Management");
    }
    
    /**
     * Handles the members menu item.
     */
    @FXML
    private void handleMembers(ActionEvent event) {
        showNotImplementedAlert("Team Members Management");
    }
    
    /**
     * Handles the user guide menu item.
     */
    @FXML
    private void handleUserGuide(ActionEvent event) {
        showNotImplementedAlert("User Guide");
    }
    
    /**
     * Handles the about menu item.
     */
    @FXML
    private void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("FRC Project Management System");
        alert.setContentText("A comprehensive project management tool designed specifically for FIRST Robotics Competition teams.\n\nVersion: 0.1.0");
        alert.showAndWait();
    }
    
    /**
     * Handles the exit menu item.
     */
    @FXML
    private void handleExit(ActionEvent event) {
        System.exit(0);
    }
    
    /**
     * Helper method to show a "Not Implemented" alert.
     */
    private void showNotImplementedAlert(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Not Implemented");
        alert.setHeaderText(feature);
        alert.setContentText("This feature is not yet implemented in the current version.");
        alert.showAndWait();
    }
}