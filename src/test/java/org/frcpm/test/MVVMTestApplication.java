// Path: src/test/java/org/frcpm/test/MVVMTestApplication.java
package org.frcpm.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.frcpm.controllers.MeetingController;
import org.frcpm.controllers.TaskController;
import org.frcpm.models.Meeting;
import org.frcpm.models.Project;
import org.frcpm.models.Subsystem;
import org.frcpm.models.Task;
import org.frcpm.services.ProjectService;
import org.frcpm.services.ServiceFactory;
import org.frcpm.services.SubsystemService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple test application for manual testing of the MVVM implementation.
 */
public class MVVMTestApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(MVVMTestApplication.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create test data
            Project testProject = createTestProject();
            Subsystem testSubsystem = createTestSubsystem(testProject);
            Meeting testMeeting = createTestMeeting(testProject);
            Task testTask = createTestTask(testProject, testSubsystem);

            // Launch meeting test view
            launchMeetingTest(testMeeting);

            // Launch task test view
            launchTaskTest(testTask);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error launching test application", e);
        }
    }

    /**
     * Creates a test project.
     * 
     * @return a test project
     */
    private Project createTestProject() {
        Project project = new Project();
        project.setName("Test MVVM Project");
        project.setDescription("Project for testing MVVM implementation");
        project.setStartDate(LocalDate.now().minusDays(30));
        project.setGoalEndDate(LocalDate.now().plusDays(30));
        project.setHardDeadline(LocalDate.now().plusDays(60));

        ProjectService projectService = ServiceFactory.getProjectService();
        return projectService.save(project);
    }

    /**
     * Creates a test subsystem.
     * 
     * @param project the parent project
     * @return a test subsystem
     */
    private Subsystem createTestSubsystem(Project project) {
        Subsystem subsystem = new Subsystem();
        subsystem.setName("Test Subsystem");
        subsystem.setDescription("Subsystem for testing MVVM implementation");

        // Project is not directly set on the Subsystem
        // In the actual implementation, the project association is managed through the
        // Task

        SubsystemService subsystemService = ServiceFactory.getSubsystemService();
        return subsystemService.save(subsystem);
    }

    /**
     * Creates a test meeting.
     * 
     * @param project the parent project
     * @return a test meeting
     */
    private Meeting createTestMeeting(Project project) {
        Meeting meeting = new Meeting();
        meeting.setDate(LocalDate.now());
        meeting.setStartTime(LocalTime.of(16, 0));
        meeting.setEndTime(LocalTime.of(18, 0));
        meeting.setNotes("Test meeting notes for MVVM testing");
        meeting.setProject(project);

        return meeting;
    }

    /**
     * Creates a test task.
     * 
     * @param project   the parent project
     * @param subsystem the parent subsystem
     * @return a test task
     */
    private Task createTestTask(Project project, Subsystem subsystem) {
        Task task = new Task();
        task.setTitle("Test MVVM Task");
        task.setDescription("Task for testing MVVM implementation");
        task.setProject(project);
        task.setSubsystem(subsystem);
        task.setStartDate(LocalDate.now());
        task.setEndDate(LocalDate.now().plusDays(7));
        task.setPriority(Task.Priority.HIGH);
        task.setProgress(25);

        return task;
    }

    /**
     * Launches the meeting test view.
     * 
     * @param meeting the test meeting
     * @throws Exception if an error occurs
     */
    private void launchMeetingTest(Meeting meeting) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MeetingView.fxml"));
        Parent root = loader.load();

        MeetingController controller = loader.getController();
        controller.setMeeting(meeting);

        Stage stage = new Stage();
        stage.setTitle("Meeting View Test");
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Launches the task test view.
     * 
     * @param task the test task
     * @throws Exception if an error occurs
     */
    private void launchTaskTest(Task task) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TaskView.fxml"));
        Parent root = loader.load();

        TaskController controller = loader.getController();
        controller.setTask(task);

        Stage stage = new Stage();
        stage.setTitle("Task View Test");
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Main method to launch the application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}