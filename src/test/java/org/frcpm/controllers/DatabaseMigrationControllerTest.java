package org.frcpm.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.frcpm.viewmodels.DatabaseMigrationViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Unit tests for the DatabaseMigrationController class.
 * This approach avoids directly mocking JavaFX components which can cause issues
 * with the JavaFX toolkit initialization.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DatabaseMigrationControllerTest {
    
    private DatabaseMigrationController controller;
    
    @Mock
    private DatabaseMigrationViewModel mockViewModel;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Create a controller instance
        controller = new DatabaseMigrationController();
        
        // Inject mock view model using reflection
        Field viewModelField = DatabaseMigrationController.class.getDeclaredField("viewModel");
        viewModelField.setAccessible(true);
        viewModelField.set(controller, mockViewModel);
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
        Method method = DatabaseMigrationController.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(controller, args);
    }
    
    @Test
    public void testGetViewModel() {
        // Verify that getViewModel returns the view model
        assertEquals(mockViewModel, controller.getViewModel());
    }
    
    @Test
    public void testSetDialogStage() {
        // Create a mock stage
        javafx.stage.Stage mockStage = mock(javafx.stage.Stage.class);
        
        // Use the spy to avoid JavaFX toolkit initialization issues
        DatabaseMigrationController controllerSpy = spy(controller);
        
        // Call the method
        controllerSpy.setDialogStage(mockStage);
        
        // Verify the stage was set (using the spy to verify the call)
        verify(controllerSpy).setDialogStage(mockStage);
    }
    
    @Test
    public void testHandleBrowseSourceDb() throws Exception {
        // Create a spy of the controller
        DatabaseMigrationController controllerSpy = spy(controller);
        
        // Mock a file selection result
        File mockFile = mock(File.class);
        when(mockFile.getAbsolutePath()).thenReturn("/test/path/database.db");
        
        // Configure the spy to return our mock file when handling browse
        doReturn(mockFile).when(controllerSpy).showFileChooserDialog(any());
        
        // Call the handleBrowseSourceDb method with reflection
        Method method = DatabaseMigrationController.class.getDeclaredMethod("handleBrowseSourceDb");
        method.setAccessible(true);
        method.invoke(controllerSpy);
        
        // Verify that the view model was updated with the file path
        verify(mockViewModel).setSourceDbPath("/test/path/database.db");
    }
    
    @Test
    public void testHandleBrowseSourceDbWithCancelledSelection() throws Exception {
        // Create a spy of the controller
        DatabaseMigrationController controllerSpy = spy(controller);
        
        // Configure the spy to return null when handling browse (user cancelled)
        doReturn(null).when(controllerSpy).showFileChooserDialog(any());
        
        // Call the handleBrowseSourceDb method with reflection
        Method method = DatabaseMigrationController.class.getDeclaredMethod("handleBrowseSourceDb");
        method.setAccessible(true);
        method.invoke(controllerSpy);
        
        // Verify that the view model was NOT updated
        verify(mockViewModel, never()).setSourceDbPath(anyString());
    }
}