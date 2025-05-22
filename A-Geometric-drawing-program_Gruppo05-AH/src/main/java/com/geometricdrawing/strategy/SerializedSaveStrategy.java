package com.geometricdrawing.strategy;

import com.geometricdrawing.model.DrawingModel;
import javafx.scene.canvas.Canvas;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class SerializedSaveStrategy implements SaveStrategy {
    @Override
    public void save(File file, DrawingModel model, Canvas canvas) throws IOException {
        if (model == null) {
            throw new IOException("DrawingModel é nullo, impossibile salvare il file serializzato.");
        }
        model.saveToFile(file);
    }

    @Override
    public FileChooser.ExtensionFilter getExtensionFilter() {
        return new FileChooser.ExtensionFilter("Disegno serializzato (*.ser)", "*.ser");
    }

    @Override
    public String getDialogTitle() {
        return "Salva Disegno.";
    }
}
