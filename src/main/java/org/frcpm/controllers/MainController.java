package org.frcpm.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.frcpm.models.Project;
import org.frcpm.services.ProjectService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.utils.ShortcutManager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the main application view.
 */
public class MainController {
    
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private final ShortcutManager shortcutManager = new ShortcutManager();
    private final ProjectService projectService = ServiceFactory.getProjectService();
    private ObservableList<Project> projectList = FXCollections.observableArrayList();

    @FXML
    private TableView<Project> projectsTable;
    
    @FXML
    private TableColumn<Project, String> projectNameColumn;
    
    @FXML
    private TableColumn<Project, LocalDate> projectStartColumn;
    
    @FXML
    private TableColumn<Project, LocalDate> projectGoalColumn;
    
    @FXML
    private TableColumn<Project, LocalDate> projectDeadlineColumn;
    
    @FXML
    private Tab projectTab;
    
    @FXML
    private Menu recentProjectsMenu;
    
    /**
     * Helper method to create a date cell factory that works with any entity type.
     * This allows us to reuse the same formatting logic across different table columns.
     * 
     * @param <T> the entity type for the table row
     * @return a callback that creates properly formatted date cells
     */
    private <T> Callback<TableColumn<T, LocalDate>, TableCell<T, LocalDate>> createDateCellFactory() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return column -> new TableCell<T, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        };
    }

    /**
     * Initializes the controller. This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing MainController");
        
        // Set up the table columns
        projectNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        projectStartColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        projectGoalColumn.setCellValueFactory(new PropertyValueFactory<>("goalEndDate"));
        projectDeadlineColumn.setCellValueFactory(new PropertyValueFactory<>("hardDeadline"));
        
        // Apply the date cell factory to all date columns
        projectStartColumn.setCellFactory(createDateCellFactory());
        projectGoalColumn.setCellFactory(createDateCellFactory());
        projectDeadlineColumn.setCellFactory(createDateCellFactory());
        
        // Set up row double-click handler
        projectsTable.setRowFactory(tv -> {
            TableRow<Project> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Project project = row.getItem();
                    handleOpenProject(project);
                }
            });
            return row;
        });
        
        // Load project data
        loadProjects();
    }
    
    /**
     * Loads projects from the database into the table.
     */
    private void loadProjects() {
        try {
            List<Project> projects = projectService.findAll();
            projectList.setAll(projects);
            projectsTable.setItems(projectList);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading projects", e);
            showErrorAlert("Error Loading Projects", "Failed to load projects from the database.");
        }
    }
    
    /**
     * Handles opening a project.
     * 
     * @param project the project to open
     */
    private void handleOpenProject(Project project) {
        if (project == null) {
            return;
        }
        
        try {
            // Load the project view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProjectView.fxml"));
            Parent projectView = loader.load();
            
            // Get the controller and set the project
            ProjectController controller = loader.getController();
            controller.setProject(project);
            
            // Set the project view in the project tab
            projectTab.setContent(projectView);
            projectTab.setText(project.getName());
            projectTab.setDisable(false);
            
            // Switch to the project tab
            TabPane tabPane = projectTab.getTabPane();
            tabPane.getSelectionModel().select(projectTab);
            
            // Enable project-specific menu items
            Menu projectMenu = getMenuById("projectMenu");
            if (projectMenu != null) {
                projectMenu.setDisable(false);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading project view", e);
            showErrorAlert("Error Opening Project", "Failed to open the project view.");
        }
    }
    
    @FXML
    private void handleNewProject(ActionEvent event) {
        try {
            // Load the new project dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NewProjectDialog.fxml"));
            Parent dialogView = loader.load();
            
            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Project");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));
            
            // Get the controller
            NewProjectController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            
            // Show the dialog and wait for result
            dialogStage.showAndWait();
            
            // Check if a new project was created
            Project newProject = controller.getCreatedProject();
            if (newProject != null) {
                // Reload the projects and open the new one
                loadProjects();
                handleOpenProject(newProject);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading new project dialog", e);
            showNotImplementedAlert("New Project");
        }
    }
    
    // Helper method to find a menu by ID
    private Menu getMenuById(String menuId) {
        Scene scene = projectsTable.getScene();
        if (scene == null) {
            return null;
        }
        
        MenuBar menuBar = (MenuBar) scene.lookup(".menu-bar");
        if (menuBar == null) {
            return null;
        }
        
        for (Menu menu : menuBar.getMenus()) {
            if (menuId.equals(menu.getId())) {
                return menu;
            }
        }
        
        return null;
    }
    
    // Alert helper methods
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
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