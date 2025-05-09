// src/main/java/org/frcpm/mvvm/MvvmDialogHelper.java

package org.frcpm.mvvm;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.ViewModel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Helper class for creating dialogs with MVVMFx.
 */
public class MvvmDialogHelper {
    
    private static final Logger LOGGER = Logger.getLogger(MvvmDialogHelper.class.getName());
    
    /**
     * Shows a dialog with the specified view and view model.
     *
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the view class
     * @param title the dialog title
     * @param owner the owner window
     * @return the view tuple containing the loaded view and view model
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> ViewTuple<V, VM> showDialog(
            Class<V> viewClass, String title, Window owner) {
        
        LOGGER.fine("Showing dialog: " + viewClass.getSimpleName());
        
        ViewTuple<V, VM> viewTuple = FluentViewLoader.fxmlView(viewClass).load();
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        if (owner != null) {
            dialog.initOwner(owner);
        }
        
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(viewTuple.getView());
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setDialogPane(dialogPane);
        dialog.showAndWait();
        
        return viewTuple;
    }
    
    /**
     * Shows a dialog with the specified view and view model and resource bundle.
     *
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the view class
     * @param title the dialog title
     * @param owner the owner window
     * @param resources the resource bundle
     * @return the view tuple containing the loaded view and view model
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> ViewTuple<V, VM> showDialog(
            Class<V> viewClass, String title, Window owner, ResourceBundle resources) {
        
        LOGGER.fine("Showing dialog with resources: " + viewClass.getSimpleName());
        
        ViewTuple<V, VM> viewTuple = FluentViewLoader.fxmlView(viewClass)
                .resourceBundle(resources)
                .load();
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        if (owner != null) {
            dialog.initOwner(owner);
        }
        
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(viewTuple.getView());
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setDialogPane(dialogPane);
        dialog.showAndWait();
        
        return viewTuple;
    }
    
    /**
     * Shows a custom dialog with the specified view and view model.
     *
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the view class
     * @param title the dialog title
     * @param owner the owner window
     * @return the view tuple containing the loaded view and view model
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> ViewTuple<V, VM> showCustomDialog(
            Class<V> viewClass, String title, Window owner) {
        
        LOGGER.fine("Showing custom dialog: " + viewClass.getSimpleName());
        
        ViewTuple<V, VM> viewTuple = FluentViewLoader.fxmlView(viewClass).load();
        
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        
        if (owner != null) {
            stage.initOwner(owner);
        }
        
        stage.setScene(new Scene(viewTuple.getView()));
        stage.showAndWait();
        
        return viewTuple;
    }
    
    /**
     * Shows a custom dialog with the specified view and view model and resource bundle.
     *
     * @param <V> the view type
     * @param <VM> the view model type
     * @param viewClass the view class
     * @param title the dialog title
     * @param owner the owner window
     * @param resources the resource bundle
     * @return the view tuple containing the loaded view and view model
     */
    public static <V extends FxmlView<VM>, VM extends ViewModel> ViewTuple<V, VM> showCustomDialog(
            Class<V> viewClass, String title, Window owner, ResourceBundle resources) {
        
        LOGGER.fine("Showing custom dialog with resources: " + viewClass.getSimpleName());
        
        ViewTuple<V, VM> viewTuple = FluentViewLoader.fxmlView(viewClass)
                .resourceBundle(resources)
                .load();
        
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        
        if (owner != null) {
            stage.initOwner(owner);
        }
        
        stage.setScene(new Scene(viewTuple.getView()));
        stage.showAndWait();
        
        return viewTuple;
    }
}