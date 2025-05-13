// src/main/java/org/frcpm/mvvm/views/MainMvvmView.java

package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Project;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.DailyMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MainMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MetricsMvvmViewModel;
import org.frcpm.mvvm.viewmodels.ProjectListMvvmViewModel;

/**
 * View for the main application using MVVMFx.
 */
public class MainMvvmView implements FxmlView<MainMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(MainMvvmView.class.getName());
    
    // Main layout components
    @FXML private BorderPane mainPane;
    @FXML private TabPane mainTabPane;
    @FXML private Tab dashboardTab;
    @FXML private Tab projectTab;
    
    // Menu items
    @FXML private MenuItem newProjectMenuItem;
    @FXML private MenuItem openProjectMenuItem;
    @FXML private MenuItem closeProjectMenuItem;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem importProjectMenuItem;
    @FXML private MenuItem exportProjectMenuItem;
    @FXML private MenuItem exitMenuItem;
    
    @FXML private MenuItem viewDashboardMenuItem;
    @FXML private MenuItem viewGanttMenuItem;
    @FXML private MenuItem viewCalendarMenuItem;
    @FXML private MenuItem viewDailyMenuItem;
    @FXML private MenuItem refreshMenuItem;
    
    @FXML private Menu projectMenu;
    @FXML private MenuItem projectPropertiesMenuItem;
    @FXML private MenuItem addMilestoneMenuItem;
    @FXML private MenuItem scheduleMeetingMenuItem;
    @FXML private MenuItem addTaskMenuItem;
    @FXML private MenuItem projectStatisticsMenuItem;
    
    @FXML private MenuItem subteamsMenuItem;
    @FXML private MenuItem membersMenuItem;
    @FXML private MenuItem takeAttendanceMenuItem;
    @FXML private MenuItem attendanceHistoryMenuItem;
    @FXML private MenuItem subsystemsMenuItem;
    
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem databaseManagementMenuItem;
    @FXML private MenuItem userGuideMenuItem;
    @FXML private MenuItem aboutMenuItem;
    
    // Toolbar buttons
    @FXML private Button newProjectButton;
    @FXML private Button openProjectButton;
    @FXML private Button importProjectButton;
    
    // Dashboard components
    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, String> projectNameColumn;
    @FXML private TableColumn<Project, LocalDate> projectStartColumn;
    @FXML private TableColumn<Project, LocalDate> projectGoalColumn;
    @FXML private TableColumn<Project, LocalDate> projectDeadlineColumn;
    
    @FXML private Button dashboardNewButton;
    @FXML private Button dashboardOpenButton;
    @FXML private Button dashboardImportButton;
    
    // Status bar
    @FXML private Label statusLabel;
    @FXML private Label versionLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private MenuItem viewMetricsMenuItem;
    
    @InjectViewModel
    private MainMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing MainMvvmView");
        this.resources = resources;
        
        // Set up table columns for projects
        setupProjectsTable();
        
        // Set up menu bindings
        setupMenuBindings();
        
        // Set up toolbar bindings
        setupToolbarBindings();
        
        // Set up dashboard bindings
        setupDashboardBindings();
        
        // Set up status bar bindings
        setupStatusBarBindings();
        
        // Set up tab pane
        setupTabPane();
        
        // Initial data load
        viewModel.getLoadProjectsCommand().execute();
    }
    
    /**
     * Sets up the projects table columns.
     */
    private void setupProjectsTable() {
        try {
            // Set up project name column
            projectNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            
            // Set up project start date column
            projectStartColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
            projectStartColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Project, LocalDate>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(date));
                    }
                }
            });
            
            // Set up project goal end date column
            projectGoalColumn.setCellValueFactory(new PropertyValueFactory<>("goalEndDate"));
            projectGoalColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Project, LocalDate>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(date));
                    }
                }
            });
            
            // Set up project deadline column
            projectDeadlineColumn.setCellValueFactory(new PropertyValueFactory<>("hardDeadline"));
            projectDeadlineColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Project, LocalDate>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                
                @Override
                protected void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(date));
                    }
                }
            });
            
            // Set up row selection handler
            projectsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedProject(newVal)
            );
            
            // Set up row double-click handler
            projectsTable.setRowFactory(tv -> {
                javafx.scene.control.TableRow<Project> row = new javafx.scene.control.TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        Project project = row.getItem();
                        viewModel.openProject(project);
                    }
                });
                return row;
            });
            
            // Bind table to ViewModel
            projectsTable.setItems(viewModel.getProjects());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up projects table", e);
            throw new RuntimeException("Failed to setup projects table", e);
        }
    }
    
    /**
     * Sets up menu bindings.
     */
    private void setupMenuBindings() {
        try {
            // File menu
            bindMenuItem(newProjectMenuItem, viewModel.getNewProjectCommand());
            bindMenuItem(openProjectMenuItem, viewModel.getOpenProjectCommand());
            bindMenuItem(closeProjectMenuItem, viewModel.getCloseProjectCommand());
            bindMenuItem(saveMenuItem, viewModel.getSaveProjectCommand());
            bindMenuItem(saveAsMenuItem, viewModel.getSaveProjectAsCommand());
            bindMenuItem(importProjectMenuItem, viewModel.getImportProjectCommand());
            bindMenuItem(exportProjectMenuItem, viewModel.getExportProjectCommand());
            bindMenuItem(exitMenuItem, viewModel.getExitCommand());
            
            // View menu
            bindMenuItem(viewDashboardMenuItem, viewModel.getViewDashboardCommand());
            bindMenuItem(viewGanttMenuItem, viewModel.getViewGanttCommand());
            bindMenuItem(viewCalendarMenuItem, viewModel.getViewCalendarCommand());
            bindMenuItem(viewDailyMenuItem, viewModel.getViewDailyCommand());
            bindMenuItem(refreshMenuItem, viewModel.getRefreshCommand());
            
            // Project menu
            projectMenu.disableProperty().bind(viewModel.projectTabDisabledProperty());
            bindMenuItem(projectPropertiesMenuItem, viewModel.getProjectPropertiesCommand());
            bindMenuItem(addMilestoneMenuItem, viewModel.getAddMilestoneCommand());
            bindMenuItem(scheduleMeetingMenuItem, viewModel.getScheduleMeetingCommand());
            bindMenuItem(addTaskMenuItem, viewModel.getAddTaskCommand());
            bindMenuItem(projectStatisticsMenuItem, viewModel.getProjectStatisticsCommand());
            
            // Team menu
            bindMenuItem(subteamsMenuItem, viewModel.getSubteamsCommand());
            bindMenuItem(membersMenuItem, viewModel.getMembersCommand());
            bindMenuItem(takeAttendanceMenuItem, viewModel.getTakeAttendanceCommand());
            bindMenuItem(attendanceHistoryMenuItem, viewModel.getAttendanceHistoryCommand());
            bindMenuItem(subsystemsMenuItem, viewModel.getSubsystemsCommand());
            
            // Tools menu
            bindMenuItem(settingsMenuItem, viewModel.getSettingsCommand());
            bindMenuItem(databaseManagementMenuItem, viewModel.getDatabaseManagementCommand());

            // Metrics menu
            bindMenuItem(viewMetricsMenuItem, viewModel.getViewMetricsCommand());
            viewMetricsMenuItem.setOnAction(e -> handleViewMetrics());
            
            // Help menu
            bindMenuItem(userGuideMenuItem, viewModel.getUserGuideCommand());
            bindMenuItem(aboutMenuItem, viewModel.getAboutCommand());
            
            // Set up command actions for dialogs
            newProjectMenuItem.setOnAction(e -> handleNewProject());
            openProjectMenuItem.setOnAction(e -> handleOpenProject());
            viewDailyMenuItem.setOnAction(e -> handleViewDaily());
            aboutMenuItem.setOnAction(e -> handleAbout());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up menu bindings", e);
            throw new RuntimeException("Failed to setup menu bindings", e);
        }
    }

    /**
     * Helper method to bind a menu item to a command.
     * 
     * @param menuItem the menu item to bind
     * @param command the command to bind to
     */
    private void bindMenuItem(MenuItem menuItem, de.saxsys.mvvmfx.utils.commands.Command command) {
        if (menuItem == null || command == null) {
            return;
        }
        
        // Disable the menu item when the command is not executable
        menuItem.disableProperty().bind(command.executableProperty().not());
        
        // Set the action to execute the command
        menuItem.setOnAction(event -> command.execute());
    }
    
    /**
     * Sets up toolbar bindings.
     */
    private void setupToolbarBindings() {
        try {
            // Bind toolbar buttons to commands
            CommandAdapter.bindCommandButton(newProjectButton, viewModel.getNewProjectCommand());
            CommandAdapter.bindCommandButton(openProjectButton, viewModel.getOpenProjectCommand());
            CommandAdapter.bindCommandButton(importProjectButton, viewModel.getImportProjectCommand());
            
            // Set up action handlers for dialogs
            newProjectButton.setOnAction(e -> handleNewProject());
            openProjectButton.setOnAction(e -> handleOpenProject());
            importProjectButton.setOnAction(e -> handleImportProject());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up toolbar bindings", e);
            throw new RuntimeException("Failed to setup toolbar bindings", e);
        }
    }
    
    /**
     * Sets up dashboard bindings.
     */
    private void setupDashboardBindings() {
        try {
            // Bind dashboard buttons to commands
            CommandAdapter.bindCommandButton(dashboardNewButton, viewModel.getNewProjectCommand());
            CommandAdapter.bindCommandButton(dashboardOpenButton, viewModel.getOpenProjectCommand());
            CommandAdapter.bindCommandButton(dashboardImportButton, viewModel.getImportProjectCommand());
            
            // Set up action handlers for dialogs
            dashboardNewButton.setOnAction(e -> handleNewProject());
            dashboardOpenButton.setOnAction(e -> handleOpenProject());
            dashboardImportButton.setOnAction(e -> handleImportProject());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up dashboard bindings", e);
            throw new RuntimeException("Failed to setup dashboard bindings", e);
        }
    }
    
    /**
     * Sets up status bar bindings.
     */
    private void setupStatusBarBindings() {
        try {
            // Bind status message
            statusLabel.textProperty().bind(viewModel.statusMessageProperty());
            
            // Bind version info
            versionLabel.textProperty().bind(viewModel.versionInfoProperty());
            
            // Bind loading indicator
            loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up status bar bindings", e);
            throw new RuntimeException("Failed to setup status bar bindings", e);
        }
    }
    
    /**
     * Sets up tab pane.
     */
    private void setupTabPane() {
        try {
            // Bind project tab title
            projectTab.textProperty().bind(viewModel.projectTabTitleProperty());
            
            // Bind project tab disabled state
            projectTab.disableProperty().bind(viewModel.projectTabDisabledProperty());
            
            // Bind selected tab index
            viewModel.selectedTabIndexProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    mainTabPane.getSelectionModel().select(newVal.intValue());
                }
            });
            
            // Listen for tab selection changes
            mainTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    viewModel.setSelectedTabIndex(newVal.intValue());
                }
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up tab pane", e);
            throw new RuntimeException("Failed to setup tab pane", e);
        }
    }
    
    /**
     * Handles creating a new project.
     */
    private void handleNewProject() {
        try {
            // Load the project list view to get a reference to its view model
            ViewTuple<ProjectListMvvmView, ProjectListMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(ProjectListMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Create a smaller dialog window
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("project.new.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(mainPane.getScene().getWindow());
            
            // Create the scene
            Scene scene = new Scene(viewTuple.getView(), 600, 400);
            
            // Add stylesheets if needed
            try {
                URL cssUrl = getClass().getResource("/css/styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not load CSS", e);
            }
            
            dialogStage.setScene(scene);
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh projects after dialog closes
            viewModel.getLoadProjectsCommand().execute();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating new project", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.project.create.failed") + ": " + e.getMessage());
        }
    }
    
    private void handleViewMetrics() {
        Project project = viewModel.getSelectedProject();
        if (project == null) {
            showInfoAlert(resources.getString("info.title"), 
                        resources.getString("info.no.selection.project"));
            return;
        }
        
        try {
            // Load the metrics view
            ViewTuple<MetricsMvvmView, MetricsMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(MetricsMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Initialize the view with the project
            MetricsMvvmView metricsView = viewTuple.getCodeBehind();
            metricsView.setProject(project);
            
            // Create a stage for the metrics view
            Stage metricsStage = new Stage();
            metricsStage.setTitle(resources.getString("metrics.title") + ": " + project.getName());
            metricsStage.initModality(Modality.WINDOW_MODAL);
            metricsStage.initOwner(mainPane.getScene().getWindow());
            
            // Create the scene
            Scene scene = new Scene(viewTuple.getView(), 900, 700);
            
            // Add stylesheets if needed
            try {
                URL cssUrl = getClass().getResource("/css/styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not load CSS", e);
            }
            
            metricsStage.setScene(scene);
            
            // Show the stage
            metricsStage.show();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error viewing metrics", e);
            showErrorAlert(resources.getString("error.title"), 
                        "Failed to open Metrics view: " + e.getMessage());
        }
    }


    /**
     * Handles opening an existing project.
     */
    private void handleOpenProject() {
        Project project = viewModel.getSelectedProject();
        if (project == null) {
            showInfoAlert(resources.getString("info.title"), 
                         resources.getString("info.no.selection.project"));
            return;
        }
        
        // Open the project
        viewModel.openProject(project);
    }
    
    /**
     * Handles importing a project.
     */
    private void handleImportProject() {
        // This would open a file chooser to import a project
        showInfoAlert(resources.getString("info.title"), 
                     resources.getString("info.not.implemented"));
    }
    
    /**
     * Handles viewing the daily task board.
     */
    private void handleViewDaily() {
        Project project = viewModel.getSelectedProject();
        if (project == null) {
            showInfoAlert(resources.getString("info.title"), 
                         resources.getString("info.no.selection.project"));
            return;
        }
        
        try {
            // Load the daily view
            ViewTuple<DailyMvvmView, DailyMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(DailyMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Initialize the view with the project
            DailyMvvmView dailyView = viewTuple.getCodeBehind();
            dailyView.setProject(project);
            
            // Create a stage for the daily view
            Stage dailyStage = new Stage();
            dailyStage.setTitle(resources.getString("daily.title") + ": " + project.getName());
            dailyStage.initModality(Modality.WINDOW_MODAL);
            dailyStage.initOwner(mainPane.getScene().getWindow());
            
            // Create the scene
            Scene scene = new Scene(viewTuple.getView(), 800, 600);
            
            // Add stylesheets if needed
            try {
                URL cssUrl = getClass().getResource("/css/styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not load CSS", e);
            }
            
            dailyStage.setScene(scene);
            
            // Show the stage
            dailyStage.show();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error viewing daily task board", e);
            showErrorAlert(resources.getString("error.title"), 
                          resources.getString("error.daily.view.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handles showing the about dialog.
     */
    private void handleAbout() {
        showInfoAlert(resources.getString("about.title"), 
                     resources.getString("about.message"));
    }
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an information alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showInfoAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows a confirmation alert dialog.
     * 
     * @param title the title
     * @param message the message
     * @return true if confirmed, false otherwise
     */
    private boolean showConfirmationAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        return alert.showAndWait()
            .filter(response -> response == javafx.scene.control.ButtonType.OK)
            .isPresent();
    }
}