// src/test/java/org/frcpm/presenters/TeamMemberPresenterAfterburnerTest.java
package org.frcpm.presenters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.ResourceBundle;

import org.frcpm.di.TestModule;
import org.frcpm.models.Project;
import org.frcpm.services.DialogService;
import org.frcpm.services.SubteamService;
import org.frcpm.services.TeamMemberService;
import org.frcpm.viewmodels.TeamMemberViewModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import javafx.collections.FXCollections;

@ExtendWith(MockitoExtension.class)
public class TeamMemberPresenterAfterburnerTest {
    
    @Mock
    private TeamMemberService teamMemberService;
    
    @Mock
    private SubteamService subteamService;
    
    @Mock
    private DialogService dialogService;
    
    @Mock
    private TeamMemberViewModel viewModel;
    
    @Mock
    private ResourceBundle resources;
    
    @InjectMocks
    private TeamMemberPresenter presenter;
    
    private AutoCloseable closeable;
    
    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        
        // Setup resource bundle mock
        when(resources.getString(anyString())).thenReturn("MOCKED_STRING");
        
        // Setup viewModel mock
        when(viewModel.getTeamMembers()).thenReturn(FXCollections.observableArrayList());
        when(viewModel.getSubteams()).thenReturn(FXCollections.observableArrayList());
        
        // Use TestModule to register services
        TestModule.initialize();
        TestModule.registerMock(TeamMemberService.class, teamMemberService);
        TestModule.registerMock(SubteamService.class, subteamService);
        TestModule.registerMock(DialogService.class, dialogService);
        TestModule.registerMock(TeamMemberViewModel.class, viewModel);
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        TestModule.shutdown();
        closeable.close();
    }
    
    @Test
    public void testInitialize() {
        // Act
        presenter.initialize(mock(URL.class), resources);
        
        // Assert
        assertSame(viewModel, presenter.getViewModel(), 
                  "ViewModel should be injected correctly");
    }
    
    @Test
    public void testInitProject() {
        // Arrange
        Project mockProject = new Project();
        mockProject.setName("Test Project");
        presenter.initialize(mock(URL.class), resources);
        
        // Act
        presenter.initProject(mockProject);
        
        // Assert
        verify(viewModel).initProject(mockProject);
    }
    
    @Test
    public void testHandleAddTeamMember() {
        // Arrange
        presenter.initialize(mock(URL.class), resources);
        
        // Act
        presenter.handleAddTeamMember();
        
        // Assert
        verify(viewModel).initNewTeamMember();
        verify(dialogService).showInfoAlert(anyString(), anyString());
    }
    
    @Test
    public void testHandleEditTeamMemberWithNoSelection() {
        // Arrange
        presenter.initialize(mock(URL.class), resources);
        
        // Act
        presenter.handleEditTeamMember();
        
        // Assert
        verify(dialogService).showErrorAlert(anyString(), anyString());
    }
    
    @Test
    public void testErrorHandlingWithNullResources() {
        // Arrange - set resources to null for this test
        TeamMemberPresenter testPresenter = new TeamMemberPresenter();
        testPresenter.setViewModel(viewModel);
        testPresenter.setDialogService(dialogService);
        
        // Act - initialize with null resources
        testPresenter.initialize(mock(URL.class), null);
        
        // Try operations that need resources
        testPresenter.handleAddTeamMember();
        
        // Assert - dialog service should be called with default titles
        verify(dialogService).showInfoAlert(anyString(), anyString());
    }
}