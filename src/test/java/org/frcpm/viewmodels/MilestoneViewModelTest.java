package org.frcpm.viewmodels;

import org.frcpm.models.Milestone;
import org.frcpm.models.Project;
import org.frcpm.services.MilestoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the MilestoneViewModel.
 */
@ExtendWith(ApplicationExtension.class)
public class MilestoneViewModelTest {
    
    @Mock
    private MilestoneService milestoneService;
    
    private MilestoneViewModel viewModel;
    private Project testProject;
    private Milestone testMilestone;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up test data
        testProject = new Project(
            "Test Project", 
            LocalDate.now().minusDays(30), 
            LocalDate.now().plusDays(30), 
            LocalDate.now().plusDays(60)
        );
        testProject.setId(1L);
        
        testMilestone = new Milestone("Test Milestone", LocalDate.now(), testProject);
        testMilestone.setId(1L);
        testMilestone.setDescription("Test Description");
        
        // Set up service mocks
        when(milestoneService.createMilestone(anyString(), any(LocalDate.class), anyLong(), anyString()))
            .thenReturn(testMilestone);
        when(milestoneService.updateMilestoneDate(anyLong(), any(LocalDate.class)))
            .thenReturn(testMilestone);
        when(milestoneService.updateDescription(anyLong(), anyString()))
            .thenReturn(testMilestone);
        
        // Create view model with mocked service
        viewModel = new MilestoneViewModel(milestoneService);
    }
    
    @Test
    public void testInitNewMilestone() {
        // Act
        viewModel.initNewMilestone(testProject);
        
        // Assert
        assertEquals(testProject, viewModel.getProject());
        assertNull(viewModel.getMilestone());
        assertTrue(viewModel.isNewMilestone());
        assertEquals("", viewModel.getName());
        assertEquals("", viewModel.getDescription());
        assertEquals(LocalDate.now(), viewModel.getDate());
        assertFalse(viewModel.isDirty());
    }
    
    @Test
    public void testInitExistingMilestone() {
        // Act
        viewModel.initExistingMilestone(testMilestone);
        
        // Assert
        assertEquals(testMilestone, viewModel.getMilestone());
        assertEquals(testProject, viewModel.getProject());
        assertFalse(viewModel.isNewMilestone());
        assertEquals("Test Milestone", viewModel.getName());
        assertEquals("Test Description", viewModel.getDescription());
        assertEquals(testMilestone.getDate(), viewModel.getDate());
        assertFalse(viewModel.isDirty());
    }
    
    @Test
    public void testValidation_RequiredFields() {
        // Arrange
        viewModel.initNewMilestone(testProject);
        viewModel.setName("");
        
        // Act & Assert
        assertFalse(viewModel.isValid());
        assertTrue(viewModel.getErrorMessage().contains("name cannot be empty"));
    }
    
    @Test
    public void testValidation_DateBeforeProjectStart() {
        // Arrange
        viewModel.initNewMilestone(testProject);
        viewModel.setName("Valid Name");
        viewModel.setDate(testProject.getStartDate().minusDays(1));
        
        // Act & Assert
        assertFalse(viewModel.isValid());
        assertTrue(viewModel.getErrorMessage().contains("before project start date"));
    }
    
    @Test
    public void testValidation_DateAfterProjectDeadline() {
        // Arrange
        viewModel.initNewMilestone(testProject);
        viewModel.setName("Valid Name");
        viewModel.setDate(testProject.getHardDeadline().plusDays(1));
        
        // Act & Assert
        assertFalse(viewModel.isValid());
        assertTrue(viewModel.getErrorMessage().contains("after project hard deadline"));
    }
    
    @Test
    public void testValidation_ValidData() {
        // Arrange
        viewModel.initNewMilestone(testProject);
        viewModel.setName("Valid Name");
        viewModel.setDate(testProject.getStartDate().plusDays(5));
        
        // Act & Assert
        assertTrue(viewModel.isValid());
    }
    
    @Test
    public void testSaveCommand_NewMilestone() {
        // Arrange
        viewModel.initNewMilestone(testProject);
        viewModel.setName("New Milestone");
        viewModel.setDescription("New Description");
        viewModel.setDate(LocalDate.now());
        
        // Act
        viewModel.getSaveCommand().execute();
        
        // Assert
        verify(milestoneService).createMilestone(
            eq("New Milestone"),
            eq(LocalDate.now()),
            eq(1L),
            eq("New Description")
        );
        assertEquals(testMilestone, viewModel.getMilestone());
        assertFalse(viewModel.isDirty());
    }
    
    @Test
    public void testSaveCommand_ExistingMilestone() {
        // Arrange
        viewModel.initExistingMilestone(testMilestone);
        LocalDate newDate = LocalDate.now().plusDays(5);
        viewModel.setDate(newDate);
        viewModel.setDescription("Updated Description");
        
        // Act
        viewModel.getSaveCommand().execute();
        
        // Assert
        verify(milestoneService).updateMilestoneDate(eq(1L), eq(newDate));
        verify(milestoneService).updateDescription(eq(1L), eq("Updated Description"));
        assertEquals(testMilestone, viewModel.getMilestone());
        assertFalse(viewModel.isDirty());
    }
    
    @Test
    public void testDirtyFlag_NameChange() {
        // Arrange
        viewModel.initNewMilestone(testProject);
        
        // Act
        viewModel.setName("New Name");
        
        // Assert
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    public void testDirtyFlag_DescriptionChange() {
        // Arrange
        viewModel.initNewMilestone(testProject);
        
        // Act
        viewModel.setDescription("New Description");
        
        // Assert
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    public void testDirtyFlag_DateChange() {
        // Arrange
        viewModel.initNewMilestone(testProject);
        
        // Act
        viewModel.setDate(LocalDate.now().plusDays(1));
        
        // Assert
        assertTrue(viewModel.isDirty());
    }
    
    @Test
    public void testInitExistingMilestone_NullMilestone() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            viewModel.initExistingMilestone(null);
        });
    }
    
    @Test
    public void testCanExecuteSaveCommand() {
        // Arrange
        viewModel.initNewMilestone(testProject);
        viewModel.setName("");
        
        // Act & Assert
        assertFalse(viewModel.getSaveCommand().canExecute());
        
        viewModel.setName("Valid Name");
        assertTrue(viewModel.getSaveCommand().canExecute());
    }
}