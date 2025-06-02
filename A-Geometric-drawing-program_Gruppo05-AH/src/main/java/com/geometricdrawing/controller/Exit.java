package com.geometricdrawing.controller;

import com.geometricdrawing.strategy.SaveContext;
import com.geometricdrawing.strategy.SerializedSaveStrategy;
import javafx.application.Platform;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class Exit {
    private final DrawingController drawingController;

    public Exit(DrawingController drawingController) {
        this.drawingController = drawingController;
    }

    public boolean exit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chiudi Applicazione");
        alert.setContentText("Vuoi salvare le modifiche prima di chiudere?");
        SaveContext saveContext = drawingController.getSaveContext();
        ButtonType buttonTypeSave = new ButtonType("Salva");
        ButtonType buttonTypeDontSave = new ButtonType("Non Salvare");
        ButtonType buttonTypeCancel = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeDontSave, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == buttonTypeSave) {
                // L'utente vuole salvare. Mostra il dialogo di salvataggio.
                // Usiamo lo strategy per il salvataggio serializzato.
                if (saveContext != null) {
                    saveContext.setStrategy(new SerializedSaveStrategy());
                    boolean prova = saveContext.execute();
                    if(prova) {
                        Platform.exit();
                        return true;
                    }else return false;
                }
            }else if (result.get() == buttonTypeDontSave) {
                Platform.exit();
                return true;
            }else {
                return false;
            }
        }
        return false;
    }
}
