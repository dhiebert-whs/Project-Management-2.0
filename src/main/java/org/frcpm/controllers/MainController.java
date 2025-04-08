package org.frcpm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.ServiceFactory;
import org.frcpm.viewmodels.MainViewModel;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the main application view.
 * Follows the MVVM pattern by delegating business logic to the MainViewModel.
 */
public class MainController {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    
    // MainViewModel
    private final MainViewModel viewModel = new MainViewModel();

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
    
    @FXML
    private Menu projectMenu;

    /**
     * Initializes the controller. This method is automatically called after the
     * FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.info("Initializing MainController with MVVM pattern");

        // Set this instance as the singleton
        setInstance();

        // Set up table columns
        setupTableColumns();
        
        // Bind the table to the ViewModel's project list
        projectsTable.setItems(viewModel.getProjectList());
        
        // Bind selected project
        projectsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldValue, newValue) -> viewModel.setSelectedProject(newValue));
        
        // Bind project tab properties
        projectTab.disableProperty().bind(viewModel.projectTabDisabledProperty());
        projectTab.textProperty().bind(viewModel.projectTabTitleProperty());
        
        // Bind error message to show alerts
        viewModel.errorMessageProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                showErrorAlert("Error", newValue);
                viewModel.errorMessageProperty().set("");
            }
        });
        
        // Set up row double-click handler
        setupTableRowHandler();
    }

    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        projectNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        projectStartColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        projectGoalColumn.setCellValueFactory(new PropertyValueFactory<>("goalEndDate"));
        projectDeadlineColumn.setCellValueFactory(new PropertyValueFactory<>("hardDeadline"));

        // Apply the date cell factory to all date columns
        projectStartColumn.setCellFactory(createDateCellFactory());
        projectGoalColumn.setCellFactory(createDateCellFactory());
        projectDeadlineColumn.setCellFactory(createDateCellFactory());
    }
    
    /**
     * Sets up the double-click handler for table rows.
     */
    private void setupTableRowHandler() {
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
    }

    /**
     * Helper method to create a date cell factory that works with any entity type.
     * 
     * @param <T> the entity type for the table row
     * @return a callback that creates properly formatted date cells
     */
    private <T> Callback<TableColumn<T, LocalDate>, TableCell<T, LocalDate>> createDateCellFactory() {
        return column -> new TableCell<T, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(viewModel.formatDate(date));
                }
            }
        };
    }

    /**
     * Handles opening a project.
     * This overloaded version is used when a project is already selected.
     * 
     * @param project the project to open
     */
    private void handleOpenProject(Project project) {
        if (project == null) {
            return;
        }

        try {
            // Pass the project to the ViewModel
            viewModel.openProject(project);
            
            // Load the project view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProjectView.fxml"));
            Parent projectView = loader.load();

            // Get the controller and set the project
            ProjectController controller = loader.getController();
            controller.setProject(project);

            // Set the project view in the project tab
            projectTab.setContent(projectView);

            // Enable project-specific menu items
            if (projectMenu != null) {
                projectMenu.setDisable(false);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading project view", e);
            showErrorAlert("Error Opening Project", "Failed to open the project view.");
        }
    }

    /**
     * Sets up the scene with shortcuts after the scene is loaded.
     * This method should be called after the scene is set for the controller.
     */
    public void setupShortcuts() {
        LOGGER.info("Setting up shortcuts");
        viewModel.getShortcutManager().setScene(projectsTable.getScene());
    }

    // ---- File Menu Handlers ----

    /**
     * Handles creating a new project.
     * 
     * @param event the action event
     */
    @FXML
    private void handleNewProject(javafx.event.ActionEvent event) {
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
                viewModel.loadProjects();
                handleOpenProject(newProject);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading new project dialog", e);
            showNotImplementedAlert("New Project");
        }
    }

    /**
     * Handles opening a project.
     * This overloaded version is used from UI event handlers.
     * 
     * @param event the action event
     */
    @FXML
    private void handleOpenProject(javafx.event.ActionEvent event) {
        // Implementation for the menu action
        // This would typically show a file chooser dialog
        showNotImplementedAlert("Open Project");
    }

    /**
     * Handles closing the current project.
     * 
     * @param event the action event
     */
    @FXML
    private void handleCloseProject(javafx.event.ActionEvent event) {
        viewModel.handleCloseProject();
    }

    /**
     * Handles saving the current project.
     * 
     * @param event the action event
     */
    @FXML
    private void handleSave(javafx.event.ActionEvent event) {
        viewModel.handleSave();
    }

    /**
     * Handles saving the current project with a new name.
     * 
     * @param event the action event
     */
    @FXML
    private void handleSaveAs(javafx.event.ActionEvent event) {
        viewModel.handleSaveAs();
    }

    /**
     * Handles importing a project from a file.
     * 
     * @param event the action event
     */
    @FXML
    private void handleImportProject(javafx.event.ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Project File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File selectedFile = fileChooser.showOpenDialog(projectsTable.getScene().getWindow());
        if (selectedFile != null) {
            viewModel.importProject(selectedFile);
        }
    }

    /**
     * Handles exporting the current project to a file.
     * 
     * @param event the action event
     */
    @FXML
    private void handleExportProject(javafx.event.ActionEvent event) {
        viewModel.handleExportProject();
    }

    /**
     * Handles exiting the application.
     * 
     * @param event the action event
     */
    @FXML
    private void handleExit(javafx.event.ActionEvent event) {
        viewModel.handleExit();
    }

    // ---- Edit Menu Handlers ----

    /**
     * Handles the undo action.
     * 
     * @param event the action event
     */
    @FXML
    private void handleUndo(javafx.event.ActionEvent event) {
        viewModel.handleUndo();
    }

    /**
     * Handles the redo action.
     * 
     * @param event the action event
     */
    @FXML
    private void handleRedo(javafx.event.ActionEvent event) {
        viewModel.handleRedo();
    }

    /**
     * Handles the cut action.
     * 
     * @param event the action event
     */
    @FXML
    private void handleCut(javafx.event.ActionEvent event) {
        viewModel.handleCut();
    }

    /**
     * Handles the copy action.
     * 
     * @param event the action event
     */
    @FXML
    private void handleCopy(javafx.event.ActionEvent event) {
        viewModel.handleCopy();
    }

    /**
     * Handles the paste action.
     * 
     * @param event the action event
     */
    @FXML
    private void handlePaste(javafx.event.ActionEvent event) {
        viewModel.handlePaste();
    }

    /**
     * Handles the delete action.
     * 
     * @param event the action event
     */
    @FXML
    private void handleDelete(javafx.event.ActionEvent event) {
        viewModel.handleDelete();
    }

    /**
     * Handles the select all action.
     * 
     * @param event the action event
     */
    @FXML
    private void handleSelectAll(javafx.event.ActionEvent event) {
        viewModel.handleSelectAll();
    }

    /**
     * Handles the find action.
     * 
     * @param event the action event
     */
    @FXML
    private void handleFind(javafx.event.ActionEvent event) {
        viewModel.handleFind();
    }

    // ---- View Menu Handlers ----

    /**
     * Handles switching to the dashboard view.
     * 
     * @param event the action event
     */
    @FXML
    private void handleViewDashboard(javafx.event.ActionEvent event) {
        viewModel.handleViewDashboard();
    }

    /**
     * Handles switching to the Gantt chart view.
     * 
     * @param event the action event
     */
    @FXML
    private void handleViewGantt(javafx.event.ActionEvent event) {
        viewModel.handleViewGantt();
    }

    /**
     * Handles switching to the calendar view.
     * 
     * @param event the action event
     */
    @FXML
    private void handleViewCalendar(javafx.event.ActionEvent event) {
        viewModel.handleViewCalendar();
    }

    /**
     * Handles switching to the daily view.
     * 
     * @param event the action event
     */
    @FXML
    private void handleViewDaily(javafx.event.ActionEvent event) {
        viewModel.handleViewDaily();
    }

    /**
     * Handles refreshing the current view.
     * 
     * @param event the action event
     */
    @FXML
    private void handleRefresh(javafx.event.ActionEvent event) {
        viewModel.handleRefresh();
    }

    // ---- Project Menu Handlers ----

    /**
     * Handles showing the project properties dialog.
     * 
     * @param event the action event
     */
    @FXML
    private void handleProjectProperties(javafx.event.ActionEvent event) {
        viewModel.handleProjectProperties();
    }

    /**
     * Handles adding a milestone to the current project.
     * 
     * @param event the action event
     */
    @FXML
    private void handleAddMilestone(javafx.event.ActionEvent event) {
        viewModel.handleAddMilestone();
    }

    /**
     * Handles scheduling a meeting for the current project.
     * 
     * @param event the action event
     */
    @FXML
    private void handleScheduleMeeting(javafx.event.ActionEvent event) {
        viewModel.handleScheduleMeeting();
    }

    /**
     * Handles adding a task to the current project.
     * 
     * @param event the action event
     */
    @FXML
    private void handleAddTask(javafx.event.ActionEvent event) {
        viewModel.handleAddTask();
    }

    /**
     * Handles showing project statistics.
     * 
     * @param event the action event
     */
    @FXML
    private void handleProjectStatistics(javafx.event.ActionEvent event) {
        viewModel.handleProjectStatistics();
    }

    // ---- Team Menu Handlers ----

    /**
     * Handles showing the subteams dialog.
     * 
     * @param event the action event
     */
    @FXML
    private void handleSubteams(javafx.event.ActionEvent event) {
        try {
            // Load the team management view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TeamView.fxml"));
            Parent teamView = loader.load();

            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Team Management");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(teamView));

            // Show the dialog
            dialogStage.showAndWait();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading team management view", e);
            showErrorAlert("Error", "Failed to open team management.");
        }
    }

    /**
     * Handles showing the members dialog.
     * 
     * @param event the action event
     */
    @FXML
    private void handleMembers(javafx.event.ActionEvent event) {
        // We can reuse the same view as handleSubteams, but select the members tab
        handleSubteams(event);
    }

    /**
     * Handles taking attendance for a meeting.
     * 
     * @param event the action event
     */
    @FXML
    private void handleTakeAttendance(javafx.event.ActionEvent event) {
        // First, select a meeting
        List<Meeting> meetings = ServiceFactory.getMeetingService().findByDateAfter(LocalDate.now().minusDays(7));

        if (meetings.isEmpty()) {
            showErrorAlert("No Recent Meetings",
                    "No meetings found in the past week. Please schedule a meeting first.");
            return;
        }

        // Show meeting selection dialog
        ChoiceDialog<Meeting> meetingDialog = new ChoiceDialog<>(meetings.get(0), meetings);
        meetingDialog.setTitle("Select Meeting");
        meetingDialog.setHeaderText("Select a meeting to take attendance");
        meetingDialog.setContentText("Meeting:");

        Optional<Meeting> meetingResult = meetingDialog.showAndWait();
        if (!meetingResult.isPresent()) {
            return; // User canceled
        }

        Meeting selectedMeeting = meetingResult.get();

        try {
            // Load the attendance view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AttendanceView.fxml"));
            Parent attendanceView = loader.load();

            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Take Attendance");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(attendanceView));

            // Get the controller
            AttendanceController controller = loader.getController();
            controller.setMeeting(selectedMeeting);

            // Show the dialog
            dialogStage.showAndWait();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading attendance view", e);
            showErrorAlert("Error", "Failed to open attendance view.");
        }
    }

    /**
     * Handles showing the attendance history dialog.
     * 
     * @param event the action event
     */
    @FXML
    private void handleAttendanceHistory(javafx.event.ActionEvent event) {
        viewModel.handleAttendanceHistory();
    }

    /**
     * Handles showing the subsystems dialog.
     * 
     * @param event the action event
     */
    @FXML
    private void handleSubsystems(javafx.event.ActionEvent event) {
        try {
            // Load the subsystem management view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SubsystemManagementView.fxml"));
            Parent subsystemView = loader.load();

            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Subsystem Management");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(subsystemView));

            // Show the dialog
            dialogStage.showAndWait();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystem management view", e);
            showErrorAlert("Error", "Failed to open subsystem management.");
        }
    }

    // ---- Tools Menu Handlers ----

    /**
     * Handles showing the settings dialog.
     * 
     * @param event the action event
     */
    @FXML
    private void handleSettings(javafx.event.ActionEvent event) {
        viewModel.handleSettings();
    }

    /**
     * Handles showing the database management dialog.
     * 
     * @param event the action event
     */
    @FXML
    private void handleDatabaseManagement(javafx.event.ActionEvent event) {
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

    /**
     * Handles showing the user guide.
     * 
     * @param event the action event
     */
    @FXML
    private void handleUserGuide(javafx.event.ActionEvent event) {
        viewModel.handleUserGuide();
    }

    /**
     * Handles showing the about dialog.
     * 
     * @param event the action event
     */
    @FXML
    private void handleAbout(javafx.event.ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("FRC Project Management System");
        alert.setContentText(
                "A comprehensive project management tool designed specifically for FIRST Robotics Competition teams.\n\nVersion: 0.1.0");
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

    /**
     * Helper method to show an error alert dialog.
     * 
     * @param title   the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper method to show an information alert dialog.
     * 
     * @param title   the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Singleton instance
    private static MainController instance;

    /**
     * Gets the singleton instance of the MainController.
     * 
     * @return the singleton instance
     */
    public static MainController getInstance() {
        return instance;
    }

    /**
     * Sets the singleton instance of the MainController.
     * This should be called after the controller is initialized.
     */
    private void setInstance() {
        instance = this;
    }

    /**
     * Shows the task dialog for creating or editing a task.
     * 
     * @param task      the task to edit, or null to create a new task
     * @param subsystem the subsystem for a new task, or null if editing an existing
     *                  task
     */
    public void showTaskDialog(Task task, Subsystem subsystem) {
        try {
            // Load the task dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskView.fxml"));
            Parent dialogView = loader.load();

            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(task == null ? "New Task" : "Edit Task");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(projectsTable.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));

            // Get the controller
            TaskController controller = loader.getController();

            // Initialize controller based on whether we're creating or editing
            if (task != null) {
                controller.initExistingTask(task);
            } else if (subsystem != null) {
                // For a new task, we need the current project
                Project currentProject = viewModel.getSelectedProject();
                
                if (currentProject == null) {
                    // If we can't determine the current project, show an error
                    showErrorAlert("Error", "No active project found.");
                    return;
                }

                controller.initNewTask(new Task("", currentProject, subsystem));
            } else {
                // Both task and subsystem are null - invalid state
                showErrorAlert("Error", "Invalid parameters for task dialog.");
                return;
            }

            // Show the dialog
            dialogStage.showAndWait();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading task dialog", e);
            showErrorAlert("Error", "Failed to open task dialog: " + e.getMessage());
        }
    }

    /**
     * Shows the subsystem dialog for creating or editing a subsystem.
     * 
     * @param subsystem the subsystem to edit, or null to create a new subsystem
     */
    public void showSubsystemDialog(Subsystem subsystem) {
        try {
            // Load the subsystem dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SubsystemView.fxml"));
            Parent dialogView = loader.load();

            // Create the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(subsystem == null ? "New Subsystem" : "Edit Subsystem");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(projectsTable.getScene().getWindow());
            dialogStage.setScene(new Scene(dialogView));

            // Get the controller
            SubsystemController controller = loader.getController();

            // Initialize controller based on whether we're creating or editing
            if (subsystem != null) {
                controller.initExistingSubsystem(subsystem);
            } else {
                controller.initNewSubsystem();
            }

            // Show the dialog
            dialogStage.showAndWait();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading subsystem dialog", e);
            showErrorAlert("Error", "Failed to open subsystem dialog: " + e.getMessage());
        }
    }

    /**
     * Gets the view model.
     * 
     * @return the view model
     */
    public MainViewModel getViewModel() {
        return viewModel;
    }

    // Getter methods for UI components (for testing)
    
    public TableView<Project> getProjectsTable() {
        return projectsTable;
    }

    public TableColumn<Project, String> getProjectNameColumn() {
        return projectNameColumn;
    }
    
    public TableColumn<Project, LocalDate> getProjectStartColumn() {
        return projectStartColumn;
    }
    
    public TableColumn<Project, LocalDate> getProjectGoalColumn() {
        return projectGoalColumn;
    }
    
    public TableColumn<Project, LocalDate> getProjectDeadlineColumn() {
        return projectDeadlineColumn;
    }
    
    public Tab getProjectTab() {
        return projectTab;
    }
    
    public Menu getRecentProjectsMenu() {
        return recentProjectsMenu;
    }
    
    public Menu getProjectMenu() {
        return projectMenu;
    }
}