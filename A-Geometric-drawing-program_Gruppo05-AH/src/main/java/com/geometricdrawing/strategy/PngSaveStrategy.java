package com.geometricdrawing.strategy;

import com.geometricdrawing.model.DrawingModel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PngSaveStrategy implements SaveStrategy {
    @Override
    public void save(File file, DrawingModel model, Canvas canvas) throws IOException {
        if (canvas == null) {
            throw new IOException("Canvas é nullo, impossibile salvare il png.");
        }
        WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(new SnapshotParameters(), writableImage);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
        ImageIO.write(bufferedImage, "png", file);
    }

    @Override
    public FileChooser.ExtensionFilter getExtensionFilter() {
        return new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png");
    }

    @Override
    public String getDialogTitle() {
        return "Salva come PNG.";
    }
}
