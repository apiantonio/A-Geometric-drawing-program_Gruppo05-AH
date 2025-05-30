package com.geometricdrawing;

import com.geometricdrawing.strategy.FileOperationContext;
import com.geometricdrawing.strategy.SerializedSaveStrategy;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;


public class NewWorkspace {
    private final DrawingController controller;

    public NewWorkspace(DrawingController controller) {
        this.controller = controller;
    }

    public void handleNewWorkspace() {
        if (controller.getModel() != null && !controller.getModel().getShapes().isEmpty()) { // Aggiunto controllo per model != null
            showConfirmationDialog();
        } else {
            createNewWorkspace();
        }
    }

    private void showConfirmationDialog() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Nuova Area di Lavoro");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Vuoi salvare il lavoro corrente prima di creare una nuova area?");

        ButtonType buttonTypeSave = new ButtonType("Salva");
        ButtonType buttonTypeNoSave = new ButtonType("Non salvare");
        ButtonType buttonTypeCancel = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmAlert.getButtonTypes().setAll(buttonTypeSave, buttonTypeNoSave, buttonTypeCancel);

        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == buttonTypeSave) {
                FileOperationContext foc =  controller.getFileOperationContext();
                if (foc != null) {
                    foc.setStrategySave(new SerializedSaveStrategy());
                    if (foc.executeSave()) { // Procedi solo se il salvataggio ha avuto successo
                        createNewWorkspace();
                    }
                    // Se il salvataggio fallisce o viene annullato, non creare una nuova area.
                } else {
                    createNewWorkspace(); // Fallback o gestisci errore se foc è critico
                }
            } else if (result == buttonTypeNoSave) {
                createNewWorkspace();
            }
            // Se Annulla, non fare nulla.
        });
    }

    protected void createNewWorkspace() {
        if (controller.getModel() != null) {
            controller.getModel().clear(); // Questo attiverà i listener nel DrawingController
        }
        controller.clearCommands();    // Questo ora chiama anche updateScrollBars

        controller.setCurrentShape(null);
        controller.setCurrentShapeFactory(null);

        controller.updateControlState(null);
        controller.updateSpinners(null);

        // Assicura che le scrollbar siano resettate per una nuova area di lavoro che inizia da (0,0)
        if (controller.getHorizontalScrollBar() != null) controller.getHorizontalScrollBar().setValue(0);
        if (controller.getVerticalScrollBar() != null) controller.getVerticalScrollBar().setValue(0);

        controller.updateScrollBars(); // Chiamata esplicita per lo stato fresco
        controller.redrawCanvas();     // Ridisegna il canvas vuoto
    }

    public DrawingController getDrawingController() {
        return controller;
    }
}