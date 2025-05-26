package com.geometricdrawing;

import com.geometricdrawing.strategy.FileOperationContext;
import com.geometricdrawing.strategy.SerializedSaveStrategy;
import javafx.application.Platform;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class Exit {
    private final DrawingController drawingController;
    public Exit(DrawingController drawingController) {
        this.drawingController = drawingController;
    }

    public void exit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chiudi Applicazione");
        alert.setContentText("Vuoi salvare le modifiche prima di chiudere?");
        FileOperationContext foc = drawingController.getFileOperationContext();
        ButtonType buttonTypeSave = new ButtonType("Salva");
        ButtonType buttonTypeDontSave = new ButtonType("Non Salvare");
        ButtonType buttonTypeCancel = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeDontSave, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == buttonTypeSave) {
                // L'utente vuole salvare. Mostra il dialogo di salvataggio.
                // Usiamo lo strategy per il salvataggio serializzato.
                if (foc != null) {
                    foc.executeSave(new SerializedSaveStrategy());
                    Platform.exit();
                }
            }else if (result.get() == buttonTypeDontSave) {
                Platform.exit();
            }else {
                return;
            }
        }
    }
}
