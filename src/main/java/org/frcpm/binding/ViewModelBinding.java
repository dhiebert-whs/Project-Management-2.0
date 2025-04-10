package org.frcpm.binding;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.NumberStringConverter;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Utility class for binding JavaFX controls to ViewModel properties.
 * Provides comprehensive binding support for various controls and property types.
 */
public class ViewModelBinding {
    
    //----------------------------------------------------------------------------
    // TEXT FIELD BINDINGS
    //----------------------------------------------------------------------------
    
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
     * Binds a TextField to an Integer property bidirectionally.
     * 
     * @param textField the TextField to bind
     * @param property the Integer property to bind to
     */
    public static void bindIntegerField(TextField textField, Property<Integer> property) {
        textField.textProperty().bindBidirectional(property, new IntegerStringConverter());
    }
    
    /**
     * Binds a TextField to a Double property bidirectionally.
     * 
     * @param textField the TextField to bind
     * @param property the Double property to bind to
     */
    public static void bindDoubleField(TextField textField, Property<Double> property) {
        textField.textProperty().bindBidirectional(property, new DoubleStringConverter());
    }
    
    /**
     * Binds a TextField to a Number property bidirectionally.
     * 
     * @param textField the TextField to bind
     * @param property the Number property to bind to
     */
    public static void bindNumberField(TextField textField, Property<Number> property) {
        textField.textProperty().bindBidirectional(property, new NumberStringConverter());
    }
    
    //----------------------------------------------------------------------------
    // TEXT AREA BINDINGS
    //----------------------------------------------------------------------------
    
    /**
     * Binds a TextArea to a String property bidirectionally.
     * 
     * @param textArea the TextArea to bind
     * @param property the String property to bind to
     */
    public static void bindTextArea(TextArea textArea, Property<String> property) {
        textArea.textProperty().bindBidirectional(property);
    }
    
    //----------------------------------------------------------------------------
    // LABEL BINDINGS
    //----------------------------------------------------------------------------
    
    /**
     * Binds a Label to a String property unidirectionally.
     * 
     * @param label the Label to bind
     * @param property the String property to bind to
     */
    public static void bindLabel(Label label, ObservableValue<String> property) {
        label.textProperty().bind(property);
    }
    
    /**
     * Binds a Label to any property unidirectionally, using the provided converter.
     * 
     * @param <T> the type of the property
     * @param label the Label to bind
     * @param property the property to bind to
     * @param converter the converter to use for converting the property value to String
     */
    public static <T> void bindLabel(Label label, ObservableValue<T> property, StringConverter<T> converter) {
        label.textProperty().bind(Bindings.createStringBinding(
            () -> property.getValue() == null ? "" : converter.toString(property.getValue()),
            property
        ));
    }
    
    //----------------------------------------------------------------------------
    // DATE PICKER BINDINGS
    //----------------------------------------------------------------------------
    
    /**
     * Binds a DatePicker to a LocalDate property bidirectionally.
     * 
     * @param datePicker the DatePicker to bind
     * @param property the LocalDate property to bind to
     */
    public static void bindDatePicker(DatePicker datePicker, Property<java.time.LocalDate> property) {
        datePicker.valueProperty().bindBidirectional(property);
    }
    
    //----------------------------------------------------------------------------
    // CHECKBOX AND TOGGLE BUTTON BINDINGS
    //----------------------------------------------------------------------------
    
    /**
     * Binds a CheckBox to a Boolean property bidirectionally.
     * 
     * @param checkBox the CheckBox to bind
     * @param property the Boolean property to bind to
     */
    public static void bindCheckBox(CheckBox checkBox, Property<Boolean> property) {
        checkBox.selectedProperty().bindBidirectional(property);
    }
    
    /**
     * Binds a ToggleButton to a Boolean property bidirectionally.
     * 
     * @param toggleButton the ToggleButton to bind
     * @param property the Boolean property to bind to
     */
    public static void bindToggleButton(ToggleButton toggleButton, Property<Boolean> property) {
        toggleButton.selectedProperty().bindBidirectional(property);
    }
    
    //----------------------------------------------------------------------------
    // COMBO BOX BINDINGS
    //----------------------------------------------------------------------------
    
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
     * Binds a ComboBox items to an observable list.
     * 
     * @param <T> the type of the items
     * @param comboBox the ComboBox to bind
     * @param items the ObservableList to bind to
     */
    public static <T> void bindComboBoxItems(ComboBox<T> comboBox, ObservableList<T> items) {
        comboBox.setItems(items);
    }
    
    //----------------------------------------------------------------------------
    // PROGRESS BAR AND SLIDER BINDINGS
    //----------------------------------------------------------------------------
    
    /**
     * Binds a ProgressBar to a Double property unidirectionally.
     * 
     * @param progressBar the ProgressBar to bind
     * @param property the Double property to bind to
     */
    public static void bindProgressBar(ProgressBar progressBar, ObservableValue<Number> property) {
        progressBar.progressProperty().bind(property);
    }
    
    /**
     * Binds a Slider to a Double property bidirectionally.
     * 
     * @param slider the Slider to bind
     * @param property the Double property to bind to
     */
    public static void bindSlider(Slider slider, DoubleProperty property) {
        slider.valueProperty().bindBidirectional(property);
    }
    
    /**
     * Binds a Spinner to a value property bidirectionally.
     * 
     * @param <T> the type of the value
     * @param spinner the Spinner to bind
     * @param property the property to bind to
     */
    public static <T> void bindSpinner(Spinner<T> spinner, Property<T> property) {
        spinner.getValueFactory().valueProperty().bindBidirectional(property);
    }
    
    //----------------------------------------------------------------------------
    // LIST VIEW BINDINGS
    //----------------------------------------------------------------------------
    
    /**
     * Binds a ListView to an observable list.
     * 
     * @param <T> the type of the items
     * @param listView the ListView to bind
     * @param items the ObservableList to bind to
     */
    public static <T> void bindListView(ListView<T> listView, ObservableList<T> items) {
        listView.setItems(items);
    }
    
    /**
     * Binds a ListView to an observable list and binds its selection model to an object property.
     * 
     * @param <T> the type of the items
     * @param listView the ListView to bind
     * @param items the ObservableList to bind to
     * @param selectedItem the property to bind the selected item to
     */
    public static <T> void bindListViewWithSelection(ListView<T> listView, ObservableList<T> items, 
                                                   ObjectProperty<T> selectedItem) {
        listView.setItems(items);
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedItem.set(newVal);
        });
        
        selectedItem.addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(listView.getSelectionModel().getSelectedItem())) {
                listView.getSelectionModel().select(newVal);
            }
        });
    }
    
    //----------------------------------------------------------------------------
    // TABLE VIEW BINDINGS
    //----------------------------------------------------------------------------
    
    /**
     * Binds a TableView to an observable list.
     * 
     * @param <T> the type of the items
     * @param tableView the TableView to bind
     * @param items the ObservableList to bind to
     */
    public static <T> void bindTableView(TableView<T> tableView, ObservableList<T> items) {
        tableView.setItems(items);
    }
    
    /**
     * Binds a TableView to an observable list and binds its selection model to an object property.
     * 
     * @param <T> the type of the items
     * @param tableView the TableView to bind
     * @param items the ObservableList to bind to
     * @param selectedItem the property to bind the selected item to
     */
    public static <T> void bindTableViewWithSelection(TableView<T> tableView, ObservableList<T> items, 
                                                    ObjectProperty<T> selectedItem) {
        tableView.setItems(items);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedItem.set(newVal);
        });
        
        selectedItem.addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(tableView.getSelectionModel().getSelectedItem())) {
                tableView.getSelectionModel().select(newVal);
                tableView.scrollTo(newVal);
            }
        });
    }
    
    /**
     * Sets up a TableColumn with a PropertyValueFactory.
     * 
     * @param <S> the type of the TableView generic type
     * @param <T> the type of the column's cell data
     * @param column the TableColumn to set up
     * @param propertyName the name of the property to use from the objects in the TableView
     */
    public static <S, T> void setupTableColumn(TableColumn<S, T> column, String propertyName) {
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
    }
    
    /**
     * Sets up an editable TableColumn with a PropertyValueFactory.
     * 
     * @param <S> the type of the TableView generic type
     * @param column the TableColumn to set up
     * @param propertyName the name of the property to use from the objects in the TableView
     */
    public static <S> void setupEditableStringColumn(TableColumn<S, String> column, String propertyName) {
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
    }
    
    /**
     * Sets up an editable boolean TableColumn with a CheckBoxTableCell.
     * 
     * @param <S> the type of the TableView generic type
     * @param column the TableColumn to set up
     * @param propertyName the name of the property to use from the objects in the TableView
     */
    public static <S> void setupEditableBooleanColumn(TableColumn<S, Boolean> column, String propertyName) {
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
    }
    
    /**
     * Sets up a custom TableColumn with a provided cell value factory and cell factory.
     * 
     * @param <S> the type of the TableView generic type
     * @param <T> the type of the column's cell data
     * @param column the TableColumn to set up
     * @param valueExtractor a function to extract the value from the table row item
     * @param cellFactory a function to create the cell factory
     */
    public static <S, T> void setupCustomColumn(TableColumn<S, T> column, 
                                               Function<S, ObservableValue<T>> valueExtractor,
                                               BiConsumer<TableColumn<S, T>, Function<S, ObservableValue<T>>> cellFactory) {
        column.setCellValueFactory(cellData -> valueExtractor.apply(cellData.getValue()));
        cellFactory.accept(column, valueExtractor);
    }
    
    //----------------------------------------------------------------------------
    // COMMAND BUTTON BINDINGS
    //----------------------------------------------------------------------------
    
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
    
    /**
     * Binds a Button to a Command and a text property.
     * The Button will execute the Command when clicked, will be disabled when the Command cannot execute,
     * and will display the text from the provided property.
     * 
     * @param button the Button to bind
     * @param command the Command to bind to
     * @param textProperty the text property to bind to
     */
    public static void bindCommandButton(Button button, Command command, ObservableValue<String> textProperty) {
        bindCommandButton(button, command);
        button.textProperty().bind(textProperty);
    }
    
    /**
     * Binds a Button to a Command with custom enabled/disabled binding logic.
     * 
     * @param button the Button to bind
     * @param command the Command to bind to
     * @param additionalDisableCondition an additional ObservableValue<Boolean> that should disable the button when true
     */
    public static void bindCommandButtonWithCondition(Button button, Command command, 
                                                    ObservableValue<Boolean> additionalDisableCondition) {
        button.setOnAction(event -> command.execute());
        button.disableProperty().bind(
            Bindings.createBooleanBinding(
                () -> !command.canExecute() || additionalDisableCondition.getValue(),
                button.sceneProperty(), additionalDisableCondition
            )
        );
    }
    
    //----------------------------------------------------------------------------
    // STYLE BINDINGS
    //----------------------------------------------------------------------------
    
    /**
     * Binds a control's style class based on a boolean property.
     * Adds the true style class when the property is true, and the false style class when it's false.
     * 
     * @param control the control to add/remove style classes to/from
     * @param property the boolean property to bind to
     * @param trueStyleClass the style class to add when the property is true
     * @param falseStyleClass the style class to add when the property is false
     */
    public static void bindStyleClass(javafx.scene.control.Control control, 
                                     ObservableValue<Boolean> property,
                                     String trueStyleClass, 
                                     String falseStyleClass) {
        property.addListener((obs, oldVal, newVal) -> {
            if (Boolean.TRUE.equals(newVal)) {
                control.getStyleClass().remove(falseStyleClass);
                if (!control.getStyleClass().contains(trueStyleClass)) {
                    control.getStyleClass().add(trueStyleClass);
                }
            } else {
                control.getStyleClass().remove(trueStyleClass);
                if (!control.getStyleClass().contains(falseStyleClass)) {
                    control.getStyleClass().add(falseStyleClass);
                }
            }
        });
        
        // Initial state
        if (Boolean.TRUE.equals(property.getValue())) {
            control.getStyleClass().remove(falseStyleClass);
            if (!control.getStyleClass().contains(trueStyleClass)) {
                control.getStyleClass().add(trueStyleClass);
            }
        } else {
            control.getStyleClass().remove(trueStyleClass);
            if (!control.getStyleClass().contains(falseStyleClass)) {
                control.getStyleClass().add(falseStyleClass);
            }
        }
    }
    
    /**
     * Binds a control's style class to a boolean property.
     * Adds the style class when the property is true, removes it when false.
     * 
     * @param control the control to add/remove style class to/from
     * @param property the boolean property to bind to
     * @param styleClass the style class to add/remove
     */
    public static void bindStyleClass(javafx.scene.control.Control control, 
                                     ObservableValue<Boolean> property,
                                     String styleClass) {
        property.addListener((obs, oldVal, newVal) -> {
            if (Boolean.TRUE.equals(newVal)) {
                if (!control.getStyleClass().contains(styleClass)) {
                    control.getStyleClass().add(styleClass);
                }
            } else {
                control.getStyleClass().remove(styleClass);
            }
        });
        
        // Initial state
        if (Boolean.TRUE.equals(property.getValue())) {
            if (!control.getStyleClass().contains(styleClass)) {
                control.getStyleClass().add(styleClass);
            }
        } else {
            control.getStyleClass().remove(styleClass);
        }
    }
}