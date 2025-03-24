package org.frcpm.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.frcpm.utils.ShortcutManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the main application view.
 */
public class MainController {
    
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private final ShortcutManager shortcutManager = new ShortcutManager();
    
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
    
    @FXML
    private Menu recentProjectsMenu;
    

    
    /**
     * Initializes the controller. This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing MainController");
        // TODO: Set up the table columns and cell factories
        
        // TODO: Load project data into the table
    }
    
    /**
     * Sets up the scene with shortcuts after the scene is loaded.
     * This method should be called after the scene is set for the controller.
     */
    public void setupShortcuts() {
        // This will be implemented in Phase 2
        LOGGER.info("Setting up shortcuts");
    }
    
    // ---- File Menu Handlers ----
    
    @FXML
    private void handleNewProject(ActionEvent event) {
        showNotImplementedAlert("New Project");
    }
    
    @FXML
    private void handleOpenProject(ActionEvent event) {
        showNotImplementedAlert("Open Project");
    }
    
    @FXML
    private void handleCloseProject(ActionEvent event) {
        showNotImplementedAlert("Close Project");
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        showNotImplementedAlert("Save Project");
    }
    
    @FXML
    private void handleSaveAs(ActionEvent event) {
        showNotImplementedAlert("Save Project As");
    }
    
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
    
    @FXML
    private void handleExportProject(ActionEvent event) {
        showNotImplementedAlert("Export Project");
    }
    
    @FXML
    private void handleExit(ActionEvent event) {
        System.exit(0);
    }
    
    // ---- Edit Menu Handlers ----
    
    @FXML
    private void handleUndo(ActionEvent event) {
        showNotImplementedAlert("Undo");
    }
    
    @FXML
    private void handleRedo(ActionEvent event) {
        showNotImplementedAlert("Redo");
    }
    
    @FXML
    private void handleCut(ActionEvent event) {
        showNotImplementedAlert("Cut");
    }
    
    @FXML
    private void handleCopy(ActionEvent event) {
        showNotImplementedAlert("Copy");
    }
    
    @FXML
    private void handlePaste(ActionEvent event) {
        showNotImplementedAlert("Paste");
    }
    
    @FXML
    private void handleDelete(ActionEvent event) {
        showNotImplementedAlert("Delete");
    }
    
    @FXML
    private void handleSelectAll(ActionEvent event) {
        showNotImplementedAlert("Select All");
    }
    
    @FXML
    private void handleFind(ActionEvent event) {
        showNotImplementedAlert("Find");
    }
    
    // ---- View Menu Handlers ----
    
    @FXML
    private void handleViewDashboard(ActionEvent event) {
        // Dashboard is the default view
        LOGGER.info("Switching to Dashboard view");
    }
    
    @FXML
    private void handleViewGantt(ActionEvent event) {
        showNotImplementedAlert("Gantt Chart View");
    }
    
    @FXML
    private void handleViewCalendar(ActionEvent event) {
        showNotImplementedAlert("Calendar View");
    }
    
    @FXML
    private void handleViewDaily(ActionEvent event) {
        showNotImplementedAlert("Daily View");
    }
    
    @FXML
    private void handleRefresh(ActionEvent event) {
        showNotImplementedAlert("Refresh View");
    }
    
    // ---- Project Menu Handlers ----
    
    @FXML
    private void handleProjectProperties(ActionEvent event) {
        showNotImplementedAlert("Project Properties");
    }
    
    @FXML
    private void handleAddMilestone(ActionEvent event) {
        showNotImplementedAlert("Add Milestone");
    }
    
    @FXML
    private void handleScheduleMeeting(ActionEvent event) {
        showNotImplementedAlert("Schedule Meeting");
    }
    
    @FXML
    private void handleAddTask(ActionEvent event) {
        showNotImplementedAlert("Add Task");
    }
    
    @FXML
    private void handleProjectStatistics(ActionEvent event) {
        showNotImplementedAlert("Project Statistics");
    }
    
    // ---- Team Menu Handlers ----
    
    @FXML
    private void handleSubteams(ActionEvent event) {
        showNotImplementedAlert("Subteams Management");
    }
    
    @FXML
    private void handleMembers(ActionEvent event) {
        showNotImplementedAlert("Team Members Management");
    }
    
    @FXML
    private void handleTakeAttendance(ActionEvent event) {
        showNotImplementedAlert("Take Attendance");
    }
    
    @FXML
    private void handleAttendanceHistory(ActionEvent event) {
        showNotImplementedAlert("Attendance History");
    }
    
    // ---- Tools Menu Handlers ----
    
    @FXML
    private void handleSettings(ActionEvent event) {
        showNotImplementedAlert("Settings");
    }

    @FXML
    private void handleDatabaseManagement(ActionEvent event) {
        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DatabaseMigrationView.fxml"));
            Parent root = loader.load();
            
            // Get the controller
            DatabaseMigrationController controller = loader.getController();
            
            // Create the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Database Management");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            // Set the controller's dialog stage
            controller.setDialogStage(dialogStage);
            
            // Show the dialog
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading database migration view", e);
            showNotImplementedAlert("Database Management");
        }
    }
    // ---- Help Menu Handlers ----
    
    @FXML
    private void handleUserGuide(ActionEvent event) {
        showNotImplementedAlert("User Guide");
    }
    
    @FXML
    private void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("FRC Project Management System");
        alert.setContentText("A comprehensive project management tool designed specifically for FIRST Robotics Competition teams.\n\nVersion: 0.1.0");
        alert.showAndWait();
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