package com.geometricdrawing.strategy;

import com.geometricdrawing.model.DrawingModel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class PdfSaveStrategy implements SaveStrategy {
    @Override
    public void save(File file, DrawingModel model, Canvas canvas) throws IOException {
        if (canvas == null) {
            throw new IOException("Canvas nullo, impossibile salvare il Pdf.");
        }

        WritableImage writableImage = new WritableImage(
                (int) Math.round(canvas.getWidth()),
                (int) Math.round(canvas.getHeight()));
        canvas.snapshot(new SnapshotParameters(), writableImage);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

        if (bufferedImage == null) {
            throw new IOException("Impossibile convertire l’istantanea del canvas in BufferedImage per l’esportazione in PDF.");
        }

        try (PDDocument document = new PDDocument()) {
            PDRectangle pageSize = new PDRectangle(bufferedImage.getWidth(), bufferedImage.getHeight());
            PDPage page = new PDPage(pageSize);
            document.addPage(page);
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, bufferedImage);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
            }

            try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
                document.save(outputStream);
            }
        }
    }

    @Override
    public FileChooser.ExtensionFilter getExtensionFilter() {
        return new FileChooser.ExtensionFilter("PDF Document (*.pdf)", "*.pdf");
    }

    @Override
    public String getDialogTitle() {
        return "Salva come PDF.";
    }
}
