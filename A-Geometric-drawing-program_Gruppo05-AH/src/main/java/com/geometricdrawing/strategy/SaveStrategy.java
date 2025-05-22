package com.geometricdrawing.strategy;

import com.geometricdrawing.model.DrawingModel;
import javafx.scene.canvas.Canvas;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public interface SaveStrategy {
    void save(File file, DrawingModel model, Canvas canvas) throws IOException;
    FileChooser.ExtensionFilter getExtensionFilter();
    String getDialogTitle();
}
