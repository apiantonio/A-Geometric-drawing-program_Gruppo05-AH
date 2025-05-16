package com.geometricdrawing;

import com.geometricdrawing.command.AddShapeCommand;
import com.geometricdrawing.command.CommandManager;
import com.geometricdrawing.factory.EllipseFactory;
import com.geometricdrawing.factory.LineFactory;
import com.geometricdrawing.factory.RectangleFactory;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Line;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Shape;
import javafx.scene.paint.Color;
import com.geometricdrawing.model.Line;
import com.geometricdrawing.model.AbstractShape;

import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Spinner;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

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
    @FXML private Button deleteButton;
    @FXML private ColorPicker fillPicker;
    @FXML private ColorPicker borderPicker;
    @FXML private Spinner<Double> heightSpinner;
    @FXML private Spinner<Double> widthSpinner;

    private static final double HANDLE_RADIUS = 3.0;        // raggio del cerchietto che ocmpare alla selezione di una figura
    private static final double SELECTION_THRESHOLD = 5.0;  // treshold per la selezione

    private BooleanProperty canDelete = new SimpleBooleanProperty(false);

    private DrawingModel model;
    private GraphicsContext gc;
    private ShapeFactory currentShapeFactory;               // factory per la creazione della figura
    private Shape currentShape;                             // figura selezionata
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

            //per il binding all'avvio, solo una nuova forma può essere premuto come bottone nella barra degli strumenti
            deleteButton.disableProperty().bind(canDelete.not());
            fillPicker.disableProperty().bind(canDelete.not());
            borderPicker.disableProperty().bind(canDelete.not());
            widthSpinner.disableProperty().bind(canDelete.not());
            heightSpinner.disableProperty().bind(canDelete.not());

            /*
            nel momento in cui si allarga la finestra, il pane che contiene il canvas (che non è estensibile di suo)
            deve estendersi a sua volta
             */
            drawingCanvas.widthProperty().bind(canvasContainer.widthProperty());
            drawingCanvas.heightProperty().bind(canvasContainer.heightProperty());
        } else {
            System.err.println("Errore: drawingCanvas non è stato iniettato!");
        }

        if (heightSpinner != null) {
            SpinnerValueFactory<Double> heightFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 40.0, 1.0); // min, max, initial, step
            heightSpinner.setValueFactory(heightFactory);
            heightSpinner.setEditable(false);
        }
        if (widthSpinner != null) {
            SpinnerValueFactory<Double> widthFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 60.0, 1.0); // min, max, initial, step
            widthSpinner.setValueFactory(widthFactory);
            widthSpinner.setEditable(false);
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
        if (model == null || commandManager == null || heightSpinner == null || widthSpinner == null) {
            System.err.println("Errore: Componenti non inizializzati (model, commandManager o spinners).");
            return;
        }
        if (currentShapeFactory == null) {
            System.out.println("Seleziona una forma prima di disegnare.");
        }

        double x = event.getX();
        double y = event.getY();

        // la shape factory non null significa che l'utente ha selezionato l'inserimento di una figura
        if (currentShapeFactory != null) {
            Shape newShape = currentShapeFactory.createShape(x, y);
            AddShapeCommand addCmd = new AddShapeCommand(model, newShape);
            commandManager.executeCommand(addCmd);

            model.addShape(newShape);
            currentShape = newShape; // La forma corrente è quella appena inserita
            currentShapeFactory = null; // Resetta la factory per richiedere una nuova selezione

            // Dopo aver aggiunto la forma, aggiorna gli spinner
            updateSpinners(currentShape);

            redrawCanvas();
            return;
        }

        // se non è stato selezionato l'inserimento di una figura allora controllo se il click è su una figura esistente
        for (Shape shape : model.getShapes()) {
            if (isPointInsideShape(x, y, shape)) {
                currentShape = shape; // seleziona la figura cliccata
                updateSpinners(shape); // aggiorna gli spinner con le dimensioni della figura selezionata
                redrawCanvas();
                return;
            }
        }

        // se il click è su uno spazio vuoto deseleziona la figura corrente
        currentShape = null;

        redrawCanvas();
    }

    private boolean isPointInsideShape(double clickX, double clickY, Shape shape) {
        if (shape instanceof AbstractShape abstractShape) {
            // Controlla se il punto (x, y) è all'interno della figura considerando il treshold
            return  clickX >= abstractShape.getX() - SELECTION_THRESHOLD &&
                    clickX <= abstractShape.getX() + abstractShape.getWidth() + SELECTION_THRESHOLD &&
                    clickY >= abstractShape.getY() - SELECTION_THRESHOLD &&
                    clickY <= abstractShape.getY() + abstractShape.getHeight() + SELECTION_THRESHOLD;
        }
        return false;
    }

    // Metodo aggiornare gli spinner quando la figura corrente cambia
    private void updateSpinners(Shape shape) {
        if (shape == null) {
            widthSpinner.getValueFactory().setValue(0.0);
            heightSpinner.getValueFactory().setValue(0.0);
        } else if (shape instanceof Line line) {
            widthSpinner.getValueFactory().setValue(line.getLength());
            heightSpinner.getValueFactory().setValue(1.0); // Altezza fissa per le linee
        } else if (shape instanceof AbstractShape abstractShape) {
            widthSpinner.getValueFactory().setValue(abstractShape.getWidth());
            heightSpinner.getValueFactory().setValue(abstractShape.getHeight());
        }
    }

    // Metodo per disegnare un cerchietto
    private void drawHandle(double x, double y) {
        gc.setFill(Color.SKYBLUE);
        gc.fillOval(x - HANDLE_RADIUS, y - HANDLE_RADIUS, HANDLE_RADIUS * 2, HANDLE_RADIUS * 2);
    }

    private void redrawCanvas() {
        if (gc == null || drawingCanvas == null || model == null) {
            return;
        }

        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        for (Shape shape : model.getShapes()) {
            if (shape != null) {
                shape.draw(gc);
                // Se la figura è quella selezionata
                if (shape == currentShape) {
                    if (shape instanceof Line line) {
                        // Disegna i cerchietti alle estremità della linea
                        drawHandle(line.getX(), line.getY()); // Inizio linea
                        drawHandle(line.getX() + line.getWidth(), line.getY() + line.getHeight()); // Fine linea
                    } else if (shape instanceof AbstractShape abstractShape) {
                        // Disegna il bordo tratteggiato rettangolare
                        gc.setStroke(Color.SKYBLUE);
                        gc.setLineWidth(1);
                        gc.setLineDashes(5); // linea tratteggiata
                        gc.strokeRect(abstractShape.getX(), abstractShape.getY(), abstractShape.getWidth(), abstractShape.getHeight());
                        gc.setLineDashes(0); // ripristina linea continua

                        // Disegna i cerchietti agli angoli
                        drawHandle(abstractShape.getX(), abstractShape.getY()); // Angolo superiore sinistro
                        drawHandle(abstractShape.getX() + abstractShape.getWidth(), abstractShape.getY()); // Angolo superiore destro
                        drawHandle(abstractShape.getX(), abstractShape.getY() + abstractShape.getHeight()); // Angolo inferiore sinistro
                        drawHandle(abstractShape.getX() + abstractShape.getWidth(), abstractShape.getY() + abstractShape.getHeight()); // Angolo inferiore destro
                    }
                }
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
