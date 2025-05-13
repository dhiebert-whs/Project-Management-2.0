// src/main/java/org/frcpm/mvvm/views/MeetingListMvvmView.java
package org.frcpm.mvvm.views;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.mvvm.CommandAdapter;
import org.frcpm.mvvm.viewmodels.MeetingDetailMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MeetingListMvvmViewModel;
import org.frcpm.mvvm.viewmodels.MeetingListMvvmViewModel.MeetingFilter;
import org.frcpm.mvvm.viewmodels.AttendanceMvvmViewModel;
import org.frcpm.mvvm.views.AttendanceMvvmView;

/**
 * View for the meeting list using MVVMFx.
 */
public class MeetingListMvvmView implements FxmlView<MeetingListMvvmViewModel>, Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(MeetingListMvvmView.class.getName());
    
    @FXML
    private BorderPane mainPane;
    
    @FXML
    private Label projectLabel;
    
    @FXML
    private TableView<Meeting> meetingsTable;
    
    @FXML
    private TableColumn<Meeting, LocalDate> dateColumn;
    
    @FXML
    private TableColumn<Meeting, LocalTime> startTimeColumn;
    
    @FXML
    private TableColumn<Meeting, LocalTime> endTimeColumn;
    
    @FXML
    private TableColumn<Meeting, String> notesColumn;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button attendanceButton;
    
    @FXML
    private ComboBox<MeetingFilter> filterComboBox;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @InjectViewModel
    private MeetingListMvvmViewModel viewModel;
    
    private ResourceBundle resources;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing MeetingListMvvmView");
        this.resources = resources;
        
        // Initialize table columns
        setupTableColumns();
        
        // Set up filtered list
        setupFilterComboBox();
        
        // Set up table view
        meetingsTable.setItems(viewModel.getMeetings());
        
        // Bind selected meeting
        meetingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.setSelectedMeeting(newVal);
        });
        
        // Bind project label
        viewModel.currentProjectProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                projectLabel.setText(newVal.getName());
            } else {
                projectLabel.setText("");
            }
        });
        
        // Set up row double-click handler
        meetingsTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Meeting> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Meeting meeting = row.getItem();
                    handleEditMeeting(meeting);
                }
            });
            return row;
        });
        
        // Bind command buttons using CommandAdapter
        CommandAdapter.bindCommandButton(addButton, viewModel.getNewMeetingCommand());
        CommandAdapter.bindCommandButton(editButton, viewModel.getEditMeetingCommand());
        CommandAdapter.bindCommandButton(deleteButton, viewModel.getDeleteMeetingCommand());
        CommandAdapter.bindCommandButton(refreshButton, viewModel.getRefreshMeetingsCommand());
        CommandAdapter.bindCommandButton(attendanceButton, viewModel.getViewAttendanceCommand());
        
        // Override button actions to handle dialogs
        addButton.setOnAction(e -> handleAddMeeting());
        editButton.setOnAction(e -> handleEditMeeting(viewModel.getSelectedMeeting()));
        attendanceButton.setOnAction(e -> handleViewAttendance(viewModel.getSelectedMeeting()));
        
        // Bind error message
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        
        // Bind loading indicator
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        // Set up the date formatter
        dateColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Meeting, LocalDate>() {
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
        
        // Set up the start time formatter
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Meeting, LocalTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(formatter.format(time));
                }
            }
        });
        
        // Set up the end time formatter
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        endTimeColumn.setCellFactory(column -> new javafx.scene.control.TableCell<Meeting, LocalTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(formatter.format(time));
                }
            }
        });
        
        // Set up the notes column
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
    }
    
    /**
     * Sets up the filter combo box.
     */
    private void setupFilterComboBox() {
        // Add filter options
        filterComboBox.getItems().clear();
        filterComboBox.getItems().addAll(MeetingFilter.values());
        filterComboBox.setValue(MeetingFilter.ALL);
        
        // Set up listener
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.setFilter(newVal);
            }
        });
    }
    
    /**
     * Sets the project for the meeting list.
     * 
     * @param project the project
     */
    public void setProject(Project project) {
        viewModel.setCurrentProject(project);
        viewModel.getLoadMeetingsCommand().execute();
    }
    
    /**
     * Handle add meeting button click.
     */
    private void handleAddMeeting() {
        try {
            // Show meeting dialog
            openMeetingDetailDialog(null, true);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating new meeting", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.meeting.create.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle edit meeting button click.
     */
    private void handleEditMeeting(Meeting meeting) {
        if (meeting == null) {
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.meeting.select"));
            return;
        }
        
        try {
            // Show meeting dialog
            openMeetingDetailDialog(meeting, false);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error editing meeting", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.meeting.edit.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Opens a meeting detail dialog.
     * 
     * @param meeting the meeting to edit or create
     * @param isNew true if creating a new meeting, false if editing
     */
    private void openMeetingDetailDialog(Meeting meeting, boolean isNew) {
        try {
            // Load the meeting detail view
            ViewTuple<MeetingDetailMvvmView, MeetingDetailMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(MeetingDetailMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            MeetingDetailMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize the controller with the meeting
            if (isNew) {
                viewController.initNewMeeting(viewModel.getCurrentProject());
            } else {
                viewController.initExistingMeeting(meeting);
            }
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(isNew ? resources.getString("meeting.new.title") : 
                                        resources.getString("meeting.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainPane.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            
            // Show the dialog and wait for it to close
            dialogStage.showAndWait();
            
            // Refresh meetings after dialog closes
            viewModel.getRefreshMeetingsCommand().execute();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening meeting dialog", e);
            showErrorAlert(resources.getString("error.title"), 
                           resources.getString("error.meeting.dialog.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle view attendance button click.
     */
    private void handleViewAttendance(Meeting meeting) {
        if (meeting == null) {
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.meeting.select"));
            return;
        }
        
        try {
            // Load the attendance view
            ViewTuple<AttendanceMvvmView, AttendanceMvvmViewModel> viewTuple = 
                FluentViewLoader.fxmlView(AttendanceMvvmView.class)
                    .resourceBundle(resources)
                    .load();
            
            // Get the controller and view model
            AttendanceMvvmView viewController = viewTuple.getCodeBehind();
            
            // Initialize the controller with the meeting
            viewController.initWithMeeting(meeting);
            
            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("attendance.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainPane.getScene().getWindow());
            dialogStage.setScene(new Scene(viewTuple.getView()));
            dialogStage.setWidth(800);
            dialogStage.setHeight(600);
            
            // Show the dialog
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error viewing attendance", e);
            showErrorAlert(resources.getString("error.title"), 
                        resources.getString("error.attendance.view.failed") + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle delete meeting button click.
     */
    @FXML
    private void onDeleteMeetingAction() {
        if (viewModel.getSelectedMeeting() == null) {
            // Show alert about no selection
            showErrorAlert(resources.getString("error.title"),
                           resources.getString("info.no.selection.meeting"));
            return;
        }
        
        // Confirm deletion
        String confirmMessage = resources.getString("meeting.delete.confirm") + 
            " on '" + viewModel.getSelectedMeeting().getDate() + "'?";
        
        if (showConfirmationAlert(resources.getString("confirm.title"), confirmMessage)) {
            // Execute delete command
            viewModel.getDeleteMeetingCommand().execute();
        }
    }
    
    /**
     * Handle refresh button click.
     */
    @FXML
    private void onRefreshAction() {
        viewModel.getRefreshMeetingsCommand().execute();
    }
    
    /**
     * Shows an error alert dialog.
     * 
     * @param title the title
     * @param message the message
     */
    private void showErrorAlert(String title, String message) {
        // This would use an AlertHelper or DialogService in a full implementation
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
        // This would use an AlertHelper or DialogService in a full implementation
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
        // This would use an AlertHelper or DialogService in a full implementation
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