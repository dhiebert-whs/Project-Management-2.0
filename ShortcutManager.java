package org.frcpm.utils;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for managing keyboard shortcuts throughout the application.
 * This class provides a centralized way to register and handle keyboard shortcuts.
 */
public class ShortcutManager {
    
    private static final Logger LOGGER = Logger.getLogger(ShortcutManager.class.getName());
    
    // Map to store shortcuts and their actions
    private final Map<KeyCombination, Runnable> shortcuts = new HashMap<>();
    
    // The scene to which shortcuts are attached
    private Scene scene;
    
    /**
     * Sets the scene to which shortcuts will be attached.
     * 
     * @param scene the JavaFX scene
     */
    public void setScene(Scene scene) {
        this.scene = scene;
        registerShortcuts();
    }
    
    /**
     * Registers a keyboard shortcut.
     * 
     * @param keyCombination the key combination (e.g., Ctrl+S)
     * @param action the action to execute when the shortcut is triggered
     */
    public void registerShortcut(KeyCombination keyCombination, Runnable action) {
        shortcuts.put(keyCombination, action);
        LOGGER.log(Level.FINE, "Registered shortcut: {0}", keyCombination);
        
        // If scene is already set, apply the shortcut
        if (scene != null) {
            scene.getAccelerators().put(keyCombination, action);
        }
    }
    
    /**
     * Registers a keyboard shortcut using KeyCode and modifiers.
     * 
     * @param keyCode the key code (e.g., KeyCode.S)
     * @param modifiers the modifiers (e.g., KeyCombination.CONTROL_DOWN)
     * @param action the action to execute when the shortcut is triggered
     */
    public void registerShortcut(KeyCode keyCode, KeyCombination.Modifier modifier, Runnable action) {
        KeyCombination keyCombination = new KeyCodeCombination(keyCode, modifier);
        registerShortcut(keyCombination, action);
    }
    
    /**
     * Unregisters a keyboard shortcut.
     * 
     * @param keyCombination the key combination to unregister
     */
    public void unregisterShortcut(KeyCombination keyCombination) {
        shortcuts.remove(keyCombination);
        
        if (scene != null) {
            scene.getAccelerators().remove(keyCombination);
        }
        
        LOGGER.log(Level.FINE, "Unregistered shortcut: {0}", keyCombination);
    }
    
    /**
     * Applies all registered shortcuts to the current scene.
     */
    private void registerShortcuts() {
        if (scene == null) {
            LOGGER.warning("Cannot register shortcuts: scene is null");
            return;
        }
        
        // Clear existing accelerators
        scene.getAccelerators().clear();
        
        // Register all shortcuts
        for (Map.Entry<KeyCombination, Runnable> entry : shortcuts.entrySet()) {
            scene.getAccelerators().put(entry.getKey(), entry.getValue());
        }
        
        LOGGER.log(Level.INFO, "Registered {0} shortcuts to scene", shortcuts.size());
    }
    
    /**
     * Creates common shortcuts for the application.
     * This method should be called once during application initialization.
     * 
     * @param controller the controller that will handle the shortcut actions
     */
    public void createCommonShortcuts(Object controller) {
        // This will be expanded in Phase 2 with actual implementation
        // For now, it's a placeholder for the structure
        LOGGER.info("Common shortcuts initialized for controller: " + controller.getClass().getSimpleName());
    }
}
