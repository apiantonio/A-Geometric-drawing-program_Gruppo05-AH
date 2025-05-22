package com.geometricdrawing.strategy;

import com.geometricdrawing.model.DrawingModel;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class SerializedLoadStrategy implements LoadStrategy {
    @Override
    public void load(File file, DrawingModel model) throws IOException, ClassNotFoundException {
        if (model == null) {
            throw new IOException("DrawingModel é nullo, impossibile effettuare il caricamento.");
        }
        model.loadFromFile(file);
    }

    @Override
    public FileChooser.ExtensionFilter getExtensionFilter() {
        return new FileChooser.ExtensionFilter("Disegno serializzato (*.ser)", "*.ser");
    }

    @Override
    public String getDialogTitle() {
        return "Carica disegno.";
    }
}
