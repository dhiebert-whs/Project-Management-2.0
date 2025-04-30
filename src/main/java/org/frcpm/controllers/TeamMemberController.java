package org.frcpm.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;

import org.frcpm.di.ViewLoader;
import org.frcpm.models.Project;
import org.frcpm.presenters.TeamMemberPresenter;
import org.frcpm.views.TeamMemberView;

public class TeamMemberController implements Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(TeamMemberController.class.getName());
    
    protected TeamMemberPresenter presenter;
    private Parent view;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Use an instance of TeamMemberView
            TeamMemberView teamMemberView = new TeamMemberView();
            view = teamMemberView.getView();
            presenter = (TeamMemberPresenter) teamMemberView.getPresenter();
        } catch (Exception e) {
            LOGGER.severe("Error loading TeamMemberView: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void initProject(Project project) {
        if (presenter != null) {
            presenter.initProject(project);
        }
    }
    
    public Parent getView() {
        return view;
    }
    
    // For testing
    protected TeamMemberPresenter getPresenter() {
        return presenter;
    }
}