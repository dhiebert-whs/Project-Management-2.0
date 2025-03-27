package org.frcpm.binding;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Utility class for binding JavaFX controls to ViewModel properties.
 */
public class ViewModelBinding {
    
    /**
     * Binds a TextField to a String property bidirectionally.
     * 
     * @param textField the TextField to bind
     * @param property the String property to bind to
     */
    public static void bindTextField(TextField textField, Property<String> property) {
        textField.textProperty().bindBidirectional(property);
    }
    
    /**
     * Binds a TextArea to a String property bidirectionally.
     * 
     * @param textArea the TextArea to bind
     * @param property the String property to bind to
     */
    public static void bindTextArea(TextArea textArea, Property<String> property) {
        textArea.textProperty().bindBidirectional(property);
    }
    
    /**
     * Binds a DatePicker to a LocalDate property bidirectionally.
     * 
     * @param datePicker the DatePicker to bind
     * @param property the LocalDate property to bind to
     */
    public static void bindDatePicker(DatePicker datePicker, Property<java.time.LocalDate> property) {
        datePicker.valueProperty().bindBidirectional(property);
    }
    
    /**
     * Binds a ComboBox to an object property bidirectionally.
     * 
     * @param <T> the type of the property
     * @param comboBox the ComboBox to bind
     * @param property the object property to bind to
     */
    public static <T> void bindComboBox(ComboBox<T> comboBox, Property<T> property) {
        comboBox.valueProperty().bindBidirectional(property);
    }
    
    /**
     * Binds a Button to a Command.
     * The Button will execute the Command when clicked and will be disabled when the Command cannot execute.
     * 
     * @param button the Button to bind
     * @param command the Command to bind to
     */
    public static void bindCommandButton(Button button, Command command) {
        button.setOnAction(event -> command.execute());
        button.disableProperty().bind(Bindings.createBooleanBinding(
            () -> !command.canExecute(),
            button.sceneProperty() // Use scene property as a dependency
        ));
    }
}