package org.frcpm.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.frcpm.viewmodels.MainViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Unit tests for the MainController class.
 * These tests use reflection to access private methods and verify proper delegation
 * to the ViewModel without requiring JavaFX components.
 */
@ExtendWith(MockitoExtension.class)
public class MainControllerTest {
    
    @Mock
    private MainViewModel viewModel;
    
    private MainController controller;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Create a controller without initialization
        controller = new MainController();
        
        // Inject the mocked view model using reflection
        Field viewModelField = MainController.class.getDeclaredField("viewModel");
        viewModelField.setAccessible(true);
        viewModelField.set(controller, viewModel);
    }
    
    /**
     * Helper method to invoke a private method on the controller.
     * 
     * @param methodName the name of the method to invoke
     * @param parameterTypes the parameter types of the method
     * @param args the arguments to pass to the method
     * @return the result of the method invocation
     * @throws Exception if an error occurs
     */
    private Object invokePrivateMethod(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = MainController.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(controller, args);
    }
    
    @Test
    public void testGetViewModel() {
        // Act
        MainViewModel result = controller.getViewModel();
        
        // Assert
        assertEquals(viewModel, result);
    }
    
    @Test
    public void testHandleCloseProject() throws Exception {
        // Act
        invokePrivateMethod("handleCloseProject", new Class<?>[]{javafx.event.ActionEvent.class}, (Object) null);
        
        // Assert
        verify(viewModel).handleCloseProject();
    }
    
    @Test
    public void testHandleSave() throws Exception {
        // Act
        invokePrivateMethod("handleSave", new Class<?>[]{javafx.event.ActionEvent.class}, (Object) null);
        
        // Assert
        verify(viewModel).handleSave();
    }
    
    @Test
    public void testHandleSaveAs() throws Exception {
        // Act
        invokePrivateMethod("handleSaveAs", new Class<?>[]{javafx.event.ActionEvent.class}, (Object) null);
        
        // Assert
        verify(viewModel).handleSaveAs();
    }
    
    @Test
    public void testHandleExportProject() throws Exception {
        // Act
        invokePrivateMethod("handleExportProject", new Class<?>[]{javafx.event.ActionEvent.class}, (Object) null);
        
        // Assert
        verify(viewModel).handleExportProject();
    }
    
    @Test
    public void testHandleRefresh() throws Exception {
        // Act
        invokePrivateMethod("handleRefresh", new Class<?>[]{javafx.event.ActionEvent.class}, (Object) null);
        
        // Assert
        verify(viewModel).handleRefresh();
    }
    
    @Test
    public void testHandleProjectProperties() throws Exception {
        // Act
        invokePrivateMethod("handleProjectProperties", new Class<?>[]{javafx.event.ActionEvent.class}, (Object) null);
        
        // Assert
        verify(viewModel).handleProjectProperties();
    }
    
    @Test
    public void testHandleAddMilestone() throws Exception {
        // Act
        invokePrivateMethod("handleAddMilestone", new Class<?>[]{javafx.event.ActionEvent.class}, (Object) null);
        
        // Assert
        verify(viewModel).handleAddMilestone();
    }
    
    @Test
    public void testHandleAddTask() throws Exception {
        // Act
        invokePrivateMethod("handleAddTask", new Class<?>[]{javafx.event.ActionEvent.class}, (Object) null);
        
        // Assert
        verify(viewModel).handleAddTask();
    }
    
    @Test
    public void testHandleScheduleMeeting() throws Exception {
        // Act
        invokePrivateMethod("handleScheduleMeeting", new Class<?>[]{javafx.event.ActionEvent.class}, (Object) null);
        
        // Assert
        verify(viewModel).handleScheduleMeeting();
    }
    
    @Test
    public void testHandleProjectStatistics() throws Exception {
        // Act
        invokePrivateMethod("handleProjectStatistics", new Class<?>[]{javafx.event.ActionEvent.class}, (Object) null);
        
        // Assert
        verify(viewModel).handleProjectStatistics();
    }
}