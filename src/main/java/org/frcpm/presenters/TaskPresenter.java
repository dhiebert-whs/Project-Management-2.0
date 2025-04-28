package org.frcpm.presenters;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.frcpm.binding.ViewModelBinding;
import org.frcpm.di.ViewLoader;
import org.frcpm.models.*;
import org.frcpm.services.*;
import org.frcpm.viewmodels.TaskViewModel;
import org.frcpm.views.ComponentView;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter for the Task view.
 * Follows the AfterburnerFX presenter convention.
 */
public class TaskPresenter implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(TaskPresenter.class.getName());

    // FXML controls
    @FXML private Label taskTitleLabel;
    @FXML private Label projectLabel;
    @FXML private Label subsystemLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<Task.Priority> priorityComboBox;
    @FXML private Slider progressSlider;
    @FXML private Label progressLabel;
    @FXML private CheckBox completedCheckBox;
    @FXML private TextField estimatedHoursField;
    @FXML private TextField actualHoursField;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Table views and buttons
    @FXML private TableView<TeamMember> assignedMembersTable;
    @FXML private TableColumn<TeamMember, String> memberNameColumn;
    @FXML private TableColumn<TeamMember, String> memberSubteamColumn;
    @FXML private Button addMemberButton;
    @FXML private Button removeMemberButton;

    @FXML private TableView<Component> requiredComponentsTable;
    @FXML private TableColumn<Component, String> componentNameColumn;
    @FXML private TableColumn<Component, String> componentPartNumberColumn;
    @FXML private TableColumn<Component, Boolean> componentDeliveredColumn;
    @FXML private Button addComponentButton;
    @FXML private Button removeComponentButton;

    @FXML private TableView<Task> dependenciesTable;
    @FXML private TableColumn<Task, String> dependencyTitleColumn;
    @FXML private TableColumn<Task, Integer> dependencyProgressColumn;
    @FXML private Button addDependencyButton;
    @FXML private Button removeDependencyButton;

    // Injected services
    @Inject
    private TaskService taskService;
    
    @Inject
    private ComponentService componentService;
    
    @Inject
    private TeamMemberService teamMemberService;
    
    @Inject
    private DialogService dialogService;

    // ViewModel
    private TaskViewModel viewModel;
    
    // Resource bundle for i18n
    private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing TaskPresenter with resource bundle");
        
        this.resources = resources;
        
        // Set up priority combo box
        if (priorityComboBox != null) {
            priorityComboBox.getItems().addAll(Task.Priority.values());
        }

        // Create view model with injected services
        viewModel = new TaskViewModel(taskService, teamMemberService, componentService);

        // Set up table columns
        setupTableColumns();

        // Set up bindings
        setupBindings();
    }

    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        // Check for null UI components for testability
        if (memberNameColumn == null || memberSubteamColumn == null || 
            componentNameColumn == null || componentPartNumberColumn == null || 
            componentDeliveredColumn == null || dependencyTitleColumn == null || 
            dependencyProgressColumn == null) {
            LOGGER.warning("Table columns not initialized - likely in test environment");
            return;
        }

        memberNameColumn
                .setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> 
                        cellData.getValue().getFullName()));
        memberSubteamColumn.setCellValueFactory(
                cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getSubteam() != null
                        ? cellData.getValue().getSubteam().getName()
                        : ""));

        componentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        componentPartNumberColumn.setCellValueFactory(new PropertyValueFactory<>("partNumber"));
        componentDeliveredColumn.setCellValueFactory(new PropertyValueFactory<>("delivered"));
        componentDeliveredColumn.setCellFactory(column -> new CheckBoxTableCell<>());

        dependencyTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dependencyProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        dependencyProgressColumn.setCellFactory(column -> new ProgressBarTableCell<>());
    }

    /**
     * Binds the view model to the UI controls.
     */
    private void setupBindings() {
        // Check for null UI components for testability
        if (taskTitleLabel == null || projectLabel == null || subsystemLabel == null || 
            startDatePicker == null || endDatePicker == null || priorityComboBox == null || 
            progressSlider == null || progressLabel == null || completedCheckBox == null || 
            estimatedHoursField == null || actualHoursField == null || descriptionArea == null || 
            saveButton == null || cancelButton == null || assignedMembersTable == null || 
            requiredComponentsTable == null || dependenciesTable == null || addMemberButton == null || 
            removeMemberButton == null || addComponentButton == null || removeComponentButton == null || 
            addDependencyButton == null || removeDependencyButton == null) {
            LOGGER.warning("UI components not initialized - likely in test environment");
            return;
        }

        // Bind text properties
        taskTitleLabel.textProperty().bind(viewModel.titleProperty());
        projectLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> viewModel.getProject() != null ? viewModel.getProject().getName() : "",
                        viewModel.projectProperty()));
        subsystemLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> viewModel.getSubsystem() != null ? viewModel.getSubsystem().getName() : "",
                        viewModel.subsystemProperty()));

        // Bind numeric fields with proper converters
        javafx.util.StringConverter<Number> numberConverter = new javafx.util.converter.NumberStringConverter();
        javafx.beans.binding.Bindings.bindBidirectional(estimatedHoursField.textProperty(), viewModel.estimatedHoursProperty(), numberConverter);
        javafx.beans.binding.Bindings.bindBidirectional(actualHoursField.textProperty(), viewModel.actualHoursProperty(), numberConverter);

        // Bind text area
        ViewModelBinding.bindTextArea(descriptionArea, viewModel.descriptionProperty());

        // Bind date pickers
        ViewModelBinding.bindDatePicker(startDatePicker, viewModel.startDateProperty());
        ViewModelBinding.bindDatePicker(endDatePicker, viewModel.endDateProperty());

        // Bind combo box
        ViewModelBinding.bindComboBox(priorityComboBox, viewModel.priorityProperty());

        // Bind progress slider and completion
        progressSlider.valueProperty().bindBidirectional(viewModel.progressProperty());
        progressLabel.textProperty().bind(javafx.beans.binding.Bindings.format("%d%%", viewModel.progressProperty()));
        completedCheckBox.selectedProperty().bindBidirectional(viewModel.completedProperty());

        // Bind tables to view model collections
        assignedMembersTable.setItems(viewModel.getAssignedMembers());
        requiredComponentsTable.setItems(viewModel.getRequiredComponents());
        dependenciesTable.setItems(viewModel.getPreDependencies());

        // Bind buttons to commands
        ViewModelBinding.bindCommandButton(saveButton, viewModel.getSaveCommand());
        ViewModelBinding.bindCommandButton(cancelButton, viewModel.getCancelCommand());
        ViewModelBinding.bindCommandButton(addMemberButton, viewModel.getAddMemberCommand());
        ViewModelBinding.bindCommandButton(removeMemberButton, viewModel.getRemoveMemberCommand());
        ViewModelBinding.bindCommandButton(addComponentButton, viewModel.getAddComponentCommand());
        ViewModelBinding.bindCommandButton(removeComponentButton, viewModel.getRemoveComponentCommand());
        ViewModelBinding.bindCommandButton(addDependencyButton, viewModel.getAddDependencyCommand());
        ViewModelBinding.bindCommandButton(removeDependencyButton, viewModel.getRemoveDependencyCommand());

        // Setup selection changes
        assignedMembersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedMember(newVal));

        requiredComponentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedComponent(newVal));

        dependenciesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedDependency(newVal));

        // Set up error message binding
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showErrorAlert("Validation Error", newVal);
                viewModel.errorMessageProperty().set("");
            }
        });

        // Set up cancel button override to close dialog
        cancelButton.setOnAction(event -> closeDialog());
    }

    /**
     * Initializes the presenter for a new task.
     * 
     * @param task the new task to create
     */
    public void initNewTask(Task task) {
        viewModel.initNewTask(task.getProject(), task.getSubsystem());
        viewModel.titleProperty().set(task.getTitle());
    }

    /**
     * Initializes the presenter for editing an existing task.
     * 
     * @param task the task to edit
     */
    public void initExistingTask(Task task) {
        viewModel.initExistingTask(task);
    }
    
    /**
     * Handler for the add component button.
     */
    @FXML
    private void handleAddComponent() {
        // Example of using ViewLoader with AfterburnerFX
        ComponentPresenter presenter = ViewLoader.showDialog(ComponentView.class, 
                resources.getString("component.select.title"), 
                saveButton.getScene().getWindow());
        
        if (presenter != null) {
            Component component = presenter.getComponent();
            if (component != null) {
                viewModel.addComponent(component);
            }
        }
    }

    /**
     * Shows an error alert with the given message.
     * 
     * @param title the title of the alert
     * @param message the error message
     */
    private void showErrorAlert(String title, String message) {
        try {
            dialogService.showErrorAlert(title, message);
        } catch (Exception e) {
            // This can happen in tests when not on FX thread
            LOGGER.log(Level.INFO, "Alert would show: {0} - {1}", new Object[] { title, message });
        }
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        try {
            if (saveButton != null && saveButton.getScene() != null) {
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error closing dialog", e);
        }
    }

    /**
     * Compatibility method for legacy code calling setTask
     * 
     * @param task the task to edit
     */
    public void setTask(Task task) {
        initExistingTask(task);
    }

    /**
     * Compatibility method for legacy code calling setNewTask
     * 
     * @param project   the project
     * @param subsystem the subsystem
     */
    public void setNewTask(Project project, Subsystem subsystem) {
        Task newTask = new Task("New Task", project, subsystem);
        initNewTask(newTask);
    }

    /**
     * Gets the task from the ViewModel.
     * 
     * @return the task
     */
    public Task getTask() {
        return viewModel.getTask();
    }

    /**
     * Gets the ViewModel.
     * 
     * @return the ViewModel
     */
    public TaskViewModel getViewModel() {
        return viewModel;
    }

    /**
     * CheckBox cell for Boolean properties in a TableView.
     */
    private static class CheckBoxTableCell<S, T> extends TableCell<S, T> {
        private final CheckBox checkBox = new CheckBox();

        public CheckBoxTableCell() {
            setGraphic(checkBox);
            checkBox.setDisable(true);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                checkBox.setSelected((Boolean) item);
                setGraphic(checkBox);
            }
        }
    }

    /**
     * ProgressBar cell for numeric progress properties in a TableView.
     */
    private static class ProgressBarTableCell<S, T> extends TableCell<S, T> {
        private final ProgressBar progressBar = new ProgressBar();

        public ProgressBarTableCell() {
            progressBar.setMaxWidth(Double.MAX_VALUE);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                int progress = (Integer) item;
                progressBar.setProgress(progress / 100.0);
                setText(progress + "%");
                setGraphic(progressBar);
            }
        }
    }
}