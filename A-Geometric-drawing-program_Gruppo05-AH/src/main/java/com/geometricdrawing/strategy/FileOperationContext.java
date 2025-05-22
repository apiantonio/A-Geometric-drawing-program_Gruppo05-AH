package com.geometricdrawing.strategy;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.model.DrawingModel;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;

public class FileOperationContext {

    private final DrawingController controller;

    public FileOperationContext(DrawingController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("DrawingController non può essere null per FileOperationContext");
        }
        this.controller = controller;
    }

    public void executeSave(SaveStrategy saveStrategy) {
        if (saveStrategy == null) {
            System.err.println("Strategia di salvataggio non fornita al contesto.");
            controller.showAlertDialog(Alert.AlertType.ERROR, "Errore Interno", "Strategia di salvataggio non specificata.");
            return;
        }

        DrawingModel currentModel = controller.getModel();
        Canvas currentCanvas = controller.getDrawingCanvas();
        Window currentWindow = controller.getWindow();

        // Controlli pre-condizione
        if (saveStrategy instanceof SerializedSaveStrategy && currentModel == null) {
            System.err.println("Modello non inizializzato. Impossibile salvare il file serializzato.");
            controller.showAlertDialog(Alert.AlertType.ERROR, "Errore Salvataggio", "Modello non inizializzato. Impossibile salvare.");
            return;
        }
        if (!(saveStrategy instanceof SerializedSaveStrategy) && (currentCanvas == null || currentCanvas.getWidth() == 0 || currentCanvas.getHeight() == 0)) {
            System.err.println("Canvas non disponibile o dimensioni nulle. Impossibile salvare file basato su immagine.");
            controller.showAlertDialog(Alert.AlertType.ERROR, "Errore Salvataggio", "Il canvas non è pronto per il salvataggio.");
            return;
        }
        if (currentWindow == null) {
            System.err.println("Finestra non disponibile per FileChooser.");
            controller.showAlertDialog(Alert.AlertType.ERROR, "Errore Interno", "Impossibile visualizzare la finestra di dialogo.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(saveStrategy.getDialogTitle());
        fileChooser.getExtensionFilters().add(saveStrategy.getExtensionFilter());
        File file = fileChooser.showSaveDialog(currentWindow);

        if (file != null) {
            try {
                saveStrategy.save(file, currentModel, currentCanvas);
                System.out.println("File salvato con successo in " + file.getAbsolutePath());
                controller.showAlertDialog(Alert.AlertType.INFORMATION, "Salvataggio Riuscito", "File salvato in:\n" + file.getName());
            } catch (IOException e) {
                System.err.println("Errore durante il salvataggio del file: " + e.getMessage());
                e.printStackTrace();
                controller.showAlertDialog(Alert.AlertType.ERROR, "Errore di Salvataggio", "Impossibile salvare il file:\n" + e.getMessage());
            } catch (Exception e) { // Cattura altri potenziali errori (es. NoClassDefFoundError per PDFBox)
                System.err.println("Errore imprevisto durante l'operazione di salvataggio: " + e.getMessage());
                e.printStackTrace();
                controller.showAlertDialog(Alert.AlertType.ERROR, "Errore Imprevisto", "Si è verificato un errore imprevisto durante il salvataggio:\n" + e.getMessage());
            }
        } else {
            System.out.println("Operazione di salvataggio annullata dall'utente.");
        }
    }

    public void executeLoad(LoadStrategy loadStrategy) {
        if (loadStrategy == null) {
            System.err.println("Strategia di caricamento non fornita al contesto.");
            controller.showAlertDialog(Alert.AlertType.ERROR, "Errore Interno", "Strategia di caricamento non specificata.");
            return;
        }
        Window currentWindow = controller.getWindow();
        if (currentWindow == null) {
            System.err.println("Finestra non disponibile per FileChooser.");
            controller.showAlertDialog(Alert.AlertType.ERROR, "Errore Interno", "Impossibile visualizzare la finestra di dialogo.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(loadStrategy.getDialogTitle());
        fileChooser.getExtensionFilters().add(loadStrategy.getExtensionFilter());
        File file = fileChooser.showOpenDialog(currentWindow);

        if (file != null) {
            try {
                DrawingModel newModel = new DrawingModel(); // Carica sempre in una nuova istanza del modello
                loadStrategy.load(file, newModel);

                // Delega gli aggiornamenti di stato al controller
                controller.setModel(newModel);
                controller.setCurrentShape(null);
                controller.updateControlState(null);
                controller.redrawCanvas();

                System.out.println("File caricato con successo da " + file.getAbsolutePath());
                controller.showAlertDialog(Alert.AlertType.INFORMATION, "Caricamento Riuscito", "Disegno caricato da:\n" + file.getName());
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Errore durante il caricamento del file: " + e.getMessage());
                e.printStackTrace();
                controller.showAlertDialog(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile caricare il file:\n" + e.getMessage());
            } catch (Exception e) {
                System.err.println("Errore imprevisto durante l'operazione di caricamento: " + e.getMessage());
                e.printStackTrace();
                controller.showAlertDialog(Alert.AlertType.ERROR, "Errore Imprevisto", "Si è verificato un errore imprevisto durante il caricamento:\n" + e.getMessage());
            }
        } else {
            System.out.println("Operazione di caricamento annullata dall'utente.");
        }
    }
}
