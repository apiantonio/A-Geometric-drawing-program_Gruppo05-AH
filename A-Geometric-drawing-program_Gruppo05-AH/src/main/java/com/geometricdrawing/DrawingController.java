package com.geometricdrawing;

import com.geometricdrawing.command.AddShapeCommand;
import com.geometricdrawing.command.CommandManager;
import com.geometricdrawing.factory.EllipseFactory;
import com.geometricdrawing.factory.LineFactory;
import com.geometricdrawing.factory.RectangleFactory;
import com.geometricdrawing.factory.ShapeFactory;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Shape;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class DrawingController {


    @FXML private Canvas drawingCanvas;
    @FXML private Pane canvasContainer;

    private DrawingModel model;
    private ShapeFactory currentShapeFactory;
    private GraphicsContext gc;
    private CommandManager commandManager;

    public void setModel(DrawingModel model) {
        this.model = model;
        if (this.model != null && this.model.getShapes() != null) {
            this.model.getShapes().addListener((ListChangeListener.Change<? extends Shape> c) -> {
                redrawCanvas();
            });
        }
        redrawCanvas();
    }

    //Metodo per iniezione del CommandManager
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @FXML
    public void initialize() {
        if (drawingCanvas != null) {
            gc = drawingCanvas.getGraphicsContext2D();

            drawingCanvas.setOnMouseClicked(this::handleCanvasClick);

            /*
            nel momento in cui si allarga la finestra, il pane che contiene il canvas (che non è estensibile di suo)
            deve estendersi a sua volta
             */
            drawingCanvas.widthProperty().bind(canvasContainer.widthProperty());
            drawingCanvas.heightProperty().bind(canvasContainer.heightProperty());
        } else {
            System.err.println("Errore: drawingCanvas non è stato iniettato!");
        }
        currentShapeFactory = null; // Nessuna forma selezionata all'inizio
    }

    @FXML
    private void handleSelectLinea(ActionEvent event) {
        currentShapeFactory = new LineFactory();
    }

    @FXML
    private void handleSelectRettangolo(ActionEvent event) {
        currentShapeFactory = new RectangleFactory();
    }

    @FXML
    private void handleSelectEllisse(ActionEvent event) {
        currentShapeFactory = new EllipseFactory();
    }

    private void handleCanvasClick(MouseEvent event) {
        if (currentShapeFactory == null) {
            System.out.println("Seleziona una forma prima di disegnare.");
            return;
        }
        if (model == null) {
            System.err.println("Errore: DrawingModel non inizializzato.");
            return;
        }
        if (commandManager == null) {
            System.err.println("Errore: CommandManager non inizializzato nel controller.");
            return;
        }

        double x = event.getX();
        double y = event.getY();

        Shape newShape = currentShapeFactory.createShape(x, y);

        AddShapeCommand addCmd = new AddShapeCommand(model, newShape);
        commandManager.executeCommand(addCmd);
        
    }


    private void redrawCanvas() {
        if (gc == null || drawingCanvas == null || model == null) {
            return;
        }
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        for (Shape shape : model.getShapes()) {
            if (shape != null) {
                shape.draw(gc);
            }
        }
    }
    @FXML
    private void handleSaveSerialized(ActionEvent event) {
        if (model == null) {
            System.err.println("Model not initialized. Cannot save.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Drawing");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Serialized Drawing (*.ser)", "*.ser"));
        File file = fileChooser.showSaveDialog(getWindow());

        if (file != null) {
            try {
                model.saveToFile(file);
                System.out.println("Drawing saved to " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving drawing: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLoadSerialized(ActionEvent event) {
        if (model == null) {
            this.model = new DrawingModel();
            setModel(this.model); // Make sure listeners are (re)attached
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Drawing");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Serialized Drawing (*.ser)", "*.ser"));
        File file = fileChooser.showOpenDialog(getWindow());

        if (file != null) {
            try {
                model.loadFromFile(file); // This should clear and add shapes
                redrawCanvas(); // Redraw with loaded shapes
                System.out.println("Drawing loaded from " + file.getAbsolutePath());
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading drawing: " + e.getMessage());
                e.printStackTrace();
                // Show error dialog to user
            }
        }
    }

    @FXML
    private void handleSaveAsPng(ActionEvent event) {
        if (drawingCanvas == null) {
            System.err.println("Canvas not available. Cannot save as PNG.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save as PNG");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png"));
        File file = fileChooser.showSaveDialog(getWindow());

        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());
                drawingCanvas.snapshot(new SnapshotParameters(), writableImage);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(bufferedImage, "png", file);
                System.out.println("Canvas saved as PNG to " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving as PNG: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void handleSaveAsPdf(ActionEvent event) {
        if (drawingCanvas == null || drawingCanvas.getWidth() == 0 || drawingCanvas.getHeight() == 0) {
            System.err.println("Canvas non disponibile o dimensioni nulle. Impossibile salvare come PDF.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossibile salvare come PDF,il canvas non è pronto o non ha dimensioni valide per l'esportazione. ");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva Disegno come PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document (*.pdf)", "*.pdf"));
        File file = fileChooser.showSaveDialog(getWindow());

        if (file != null) {
            System.out.println("Salvataggio PDF tramite snapshot e Apache PDFBox...");
            try {
                WritableImage writableImage = new WritableImage(
                        (int) Math.round(drawingCanvas.getWidth()),  // Usa Math.round per sicurezza
                        (int) Math.round(drawingCanvas.getHeight()));
                drawingCanvas.snapshot(new SnapshotParameters(), writableImage);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
                if (bufferedImage == null) {
                    System.err.println("Errore: la conversione dello snapshot in BufferedImage è fallita.");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Errore Esportazione PDF, impossibile eseguire la immagine dal canvas.");
                    return;
                }
                try (PDDocument document = new PDDocument()) {
                    PDRectangle pageSize = new PDRectangle(bufferedImage.getWidth(), bufferedImage.getHeight());
                    PDPage page = new PDPage(pageSize);
                    document.addPage(page);
                    PDImageXObject pdImage = LosslessFactory.createFromImage(document, bufferedImage);

                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                        // Disegna l'immagine sulla pagina, riempiendo tutta la pagina
                        contentStream.drawImage(pdImage, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
                    }

                    try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
                        document.save(outputStream);
                    }
                    System.out.println("Canvas salvato come PDF in: " + file.getAbsolutePath());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Esportazione PDF Completata\", \"Il disegno è stato salvato come PDF:\\n\"" + file.getName());

                } catch (IOException pdfEx) {
                    System.err.println("Errore durante la creazione o scrittura del PDF con Apache PDFBox: " + pdfEx.getMessage());
                    pdfEx.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Impossibile scrivere il file PDF: " + pdfEx.getMessage());
                }

            } catch (NoClassDefFoundError e) {
                // Questo errore è comune se manca javafx.swing o la dipendenza PDFBox non è configurata correttamente
                System.err.println("Errore di dipendenza: classe non trovata. " + e.getMessage());
                e.printStackTrace();
                if (e.getMessage() != null && e.getMessage().contains("SwingFXUtils")) {
                } else if (e.getMessage() != null && e.getMessage().toLowerCase().contains("pdfbox")) {
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Errore Dipendenza\", \"Una classe necessaria non è stata trovata. Controlla le dipendenze del progetto.");
                }
            } catch (Exception e) { // Catch generico per altri errori imprevisti durante lo snapshot
                System.err.println("Errore imprevisto durante l'esportazione in PDF: " + e.getMessage());
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Errore Imprevisto PDF\", \"Si è verificato un errore imprevisto: \"" + e.getMessage());
            }
        } else {
            System.out.println("Salvataggio PDF annullato dall'utente.");
        }
    }

    private Window getWindow() {
        return drawingCanvas != null ? drawingCanvas.getScene().getWindow() : null;
    }
}