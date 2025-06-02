package com.geometricdrawing.strategy;

import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.model.DrawingModel;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;

public class LoadContext {

    private final DrawingController controller;
    private LoadStrategy loadStrategy;

    public LoadContext(DrawingController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("DrawingController non può essere null per LoadContext");
        }
        this.controller = controller;
    }
    
    public void setStrategy(LoadStrategy loadStrategy) {
        this.loadStrategy = loadStrategy;
    }

    public void execute() {
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
