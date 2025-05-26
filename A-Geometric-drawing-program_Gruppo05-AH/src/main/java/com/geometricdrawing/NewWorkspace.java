package com.geometricdrawing;

import com.geometricdrawing.strategy.FileOperationContext;
import com.geometricdrawing.strategy.SerializedSaveStrategy;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/**
 * Classe responsabile della gestione della creazione di una nuova area di lavoro.
 * Implementa la logica per verificare se ci sono modifiche non salvate e gestire il salvataggio.
 */
public class NewWorkspace {
    private final DrawingController controller;

    public NewWorkspace(DrawingController controller) {
        this.controller = controller;
    }

    /**
     * Gestisce la richiesta di creazione di una nuova area di lavoro.
     * Se ci sono figure nel modello, chiede all'utente se vuole salvare prima di procedere.
     */
    public void handleNewWorkspace() {
        // Verifica se ci sono figure nel modello corrente
        if (!controller.getModel().getShapes().isEmpty()) {
            showConfirmationDialog();
        } else {
            // Se non ci sono figure, crea direttamente una nuova area di lavoro
            createNewWorkspace();
        }
    }

    /**
     * Mostra il dialogo di conferma per salvare il lavoro corrente.
     */
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
                // Salva il lavoro corrente come serializzato
                FileOperationContext foc =  controller.getFileOperationContext();
                foc.setStrategySave(new SerializedSaveStrategy());
                foc.executeSave();
                // Procede con la creazione della nuova area solo se il salvataggio è andato a buon fine
                createNewWorkspace();
            } else if (result == buttonTypeNoSave) {
                // Procede direttamente con la creazione della nuova area
                createNewWorkspace();
            }
        });
    }

    /**
     * Crea una nuova area di lavoro vuota
     */
    private void createNewWorkspace() {
        // Resetta il modello
        controller.getModel().clear();

        // Resetta lo stato del controller
        controller.setCurrentShape(null);
        controller.setCurrentShapeFactory(null);

        // Aggiorna l'interfaccia
        controller.updateControlState(null);
        controller.updateSpinners(null);
        controller.redrawCanvas();
    }
}
