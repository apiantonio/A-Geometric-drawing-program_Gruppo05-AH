package com.geometricdrawing.strategy;

import com.geometricdrawing.model.DrawingModel;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public interface LoadStrategy {
    void load(File file, DrawingModel model) throws IOException, ClassNotFoundException;
    FileChooser.ExtensionFilter getExtensionFilter();
    String getDialogTitle();
}
