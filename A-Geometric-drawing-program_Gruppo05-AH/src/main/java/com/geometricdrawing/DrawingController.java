package com.geometricdrawing;

import com.geometricdrawing.command.AddShapeCommand;
import com.geometricdrawing.command.CommandManager;
import com.geometricdrawing.command.DeleteShapeCommand;
import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.decorator.ShapeDecorator;
import com.geometricdrawing.factory.EllipseFactory;
import com.geometricdrawing.factory.LineFactory;
import com.geometricdrawing.factory.RectangleFactory;
import com.geometricdrawing.factory.ShapeFactory;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import com.geometricdrawing.model.DrawingModel;
import javafx.scene.paint.Color;
import com.geometricdrawing.model.Line;
import com.geometricdrawing.model.AbstractShape;

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
import java.util.function.UnaryOperator;

public class DrawingController {

    @FXML private AnchorPane rootPane;
    @FXML private Canvas drawingCanvas;
    @FXML private Pane canvasContainer;

    @FXML private Button deleteButton;
    @FXML private ColorPicker fillPicker;
    @FXML private ColorPicker borderPicker;
    @FXML private Spinner<Double> heightSpinner;
    @FXML private Spinner<Double> widthSpinner;
    private ContextMenu shapeMenu;

    private static final double HANDLE_RADIUS = 3.0;        // raggio del cerchietto che compare alla selezione di una figura
    private static final double SELECTION_THRESHOLD = 5.0;  // threshold per la selezione

    private DrawingModel model;
    private GraphicsContext gc;
    private ShapeFactory currentShapeFactory;               // factory per la creazione della figura
    private AbstractShape currentShape;                             // figura selezionata
    private CommandManager commandManager;
    private double dragOffsetX;
    private double dragOffsetY;


    public void setModel(DrawingModel model) {
        this.model = model;
        if (this.model != null && this.model.getShapes() != null) {
            this.model.getShapes().addListener((ListChangeListener.Change<? extends AbstractShape> c) -> redrawCanvas());
        }
        redrawCanvas();
    }

    //Metodo per iniezione del CommandManager
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    // la creazione del ContextMenu al tasto destro dà problemi con sceneBuilder e quindi si procede a inserirla qui

    @FXML
    public void initialize() {
        if (drawingCanvas != null) {
            gc = drawingCanvas.getGraphicsContext2D();

            // Al click col tasto destro richiama la creazione del ContextMenu
            shapeMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Elimina");
            deleteItem.setOnAction(e -> handleDeleteShape(new ActionEvent()));
            shapeMenu.getItems().add(deleteItem);
            // contextMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY());
            // TO ADD ALTRI PER LE ALTRE FUNZIONALITA'

            drawingCanvas.setOnMouseClicked(this::handleCanvasClick); //
            drawingCanvas.setOnMousePressed(this::handleMousePressed);
            drawingCanvas.setOnMouseDragged(this::handleMouseDragged);
            drawingCanvas.setOnMouseReleased(this::handleMouseReleased);
            drawingCanvas.setOnMouseMoved(this::handleMouseMoved); // per il cambio cursore

            // colore di partenza dei colorPicker
            fillPicker.setValue(Color.LIGHTGREEN);
            borderPicker.setValue(Color.ORANGE);

            //per il binding all'avvio, solo una nuova forma può essere premuto come bottone nella barra degli strumenti
            updateControlState(null);

            // affinchè rootPane possa ricevere focus
            rootPane.setFocusTraversable(true);

            /*
            nel momento in cui si allarga la finestra, il pane che contiene il canvas (che non è estensibile di suo)
            deve estendersi a sua volta
             */
            drawingCanvas.widthProperty().bind(canvasContainer.widthProperty());
            drawingCanvas.heightProperty().bind(canvasContainer.heightProperty());

            // ogni volta che cambia altezza e larghezza, senza attendere un click nel canvas si aggiornano le figure
            drawingCanvas.widthProperty().addListener((obs, oldVal, newVal) -> redrawCanvas());
            drawingCanvas.heightProperty().addListener((obs, oldVal, newVal) -> redrawCanvas());
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


        if (heightSpinner != null) {
            SpinnerValueFactory<Double> heightFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 40.0, 1.0);
            heightSpinner.setValueFactory(heightFactory);
            heightSpinner.setEditable(true);

            // listener per ridimensionamento con freccette
            heightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                handleDimensionChange(false, newValue);
            });

            // listener per input da tastiera
            UnaryOperator<TextFormatter.Change> filter = change -> {
                String newText = change.getControlNewText();
                // Accetta solo numeri validi (inclusi decimali con punto o virgola)
                if (newText.matches("-?\\d*(\\.\\d*)?")) {
                    return change;
                }
                return null; // blocca il cambiamento
            };
            TextFormatter<String> textFormatter = new TextFormatter<>(filter);
            heightSpinner.getEditor().setTextFormatter(textFormatter);
            textFormatter.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    try {
                        double value = Double.parseDouble(newVal);
                        heightSpinner.getValueFactory().setValue(value);
                    } catch (NumberFormatException ignored) {
                        // Questo blocco non dovrebbe mai attivarsi grazie al filtro
                    }
                }
            });

            heightSpinner.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    try {
                        double value = Double.parseDouble(heightSpinner.getEditor().getText());
                        heightSpinner.getValueFactory().setValue(value);
                    } catch (NumberFormatException e) {
                        heightSpinner.getEditor().setText(heightSpinner.getValue().toString());
                    }
                }
            });
        }

        if (widthSpinner != null) {
            SpinnerValueFactory<Double> widthFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 60.0, 1.0);
            widthSpinner.setValueFactory(widthFactory);
            widthSpinner.setEditable(true);

            widthSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                handleDimensionChange(true, newValue);
            });

            UnaryOperator<TextFormatter.Change> filter = change -> {
                String newText = change.getControlNewText();
                // Accetta solo numeri validi (inclusi decimali con punto o virgola)
                if (newText.matches("-?\\d*(\\.\\d*)?")) {
                    return change;
                }
                return null; // blocca il cambiamento
            };
            TextFormatter<String> textFormatter = new TextFormatter<>(filter);
            widthSpinner.getEditor().setTextFormatter(textFormatter);
            textFormatter.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    try {
                        double value = Double.parseDouble(newVal);
                        widthSpinner.getValueFactory().setValue(value);
                    } catch (NumberFormatException ignored) {
                        // Questo blocco non dovrebbe mai attivarsi grazie al filtro
                    }
                }
            });

            widthSpinner.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    try {
                        double value = Double.parseDouble(widthSpinner.getEditor().getText());
                        widthSpinner.getValueFactory().setValue(value);
                    } catch (NumberFormatException e) {
                        widthSpinner.getEditor().setText(widthSpinner.getValue().toString());
                    }
                }
            });
        }
    }

    @FXML
    private void onRootKeyPressed(KeyEvent event) {
        // la cancellazione da tastiera può avvenire con backspace o delete
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            // richiama il metodo per la cancellazione tramite bottone
            handleDeleteShape(new ActionEvent());
            event.consume();
        }
    }

    @FXML
    private void handleSelectLinea(ActionEvent event) {
        currentShape = null;  // se è selezionata una figura deseleziona
        // i controlli abilitati per la selezione, vengono disabilitati
        updateControlState(null);
        // aggiornamento del canvas
        redrawCanvas();

        // al momento della creazione potresti selezionare il colore di bordo della linea
        borderPicker.setDisable(false);
        currentShapeFactory = new LineFactory();
    }

    @FXML
    private void handleSelectRettangolo(ActionEvent event) {
        currentShape = null;  // se è selezionata una figura deseleziona
        // i controlli abilitati per la selezione, vengono disabilitati
        updateControlState(null);
        // aggiornamento del canvas
        redrawCanvas();

        // al momento della creazione potresti selezionare il colore di bordo e riempimento del rettangolo
        fillPicker.setDisable(false);
        borderPicker.setDisable(false);
        currentShapeFactory = new RectangleFactory();
    }

    @FXML
    private void handleSelectEllisse(ActionEvent event) {
        currentShape = null;  // se è selezionata una figura deseleziona
        // i controlli abilitati per la selezione, vengono disabilitati
        updateControlState(null);
        // aggiornamento del canvas
        redrawCanvas();

        // al momento della creazione potresti selezionare il colore di bordo e riempimento dell'ellisse
        fillPicker.setDisable(false);
        borderPicker.setDisable(false);
        currentShapeFactory = new EllipseFactory();
    }

    private void handleCanvasClick(MouseEvent event) {
        if (model == null || commandManager == null || heightSpinner == null || widthSpinner == null) {
            System.err.println("Errore: Componenti non inizializzati (model, commandManager o spinners).");
            return;
        }

        double x = event.getX();
        double y = event.getY();

        // l'utente ha scelto una figura da voler inserire
        // (il caso in cui l'utente ha cliccato su una figura esistente/spazio vuoto è gestito nel mousePressed)
        if (currentShapeFactory != null) {

            // la shape factory non null significa che l'utente ha selezionato l'inserimento di una figura
            AbstractShape newShape = currentShapeFactory.createShape(x, y);
            AbstractShape styledShape = newShape;

            // prelevo il colore di bordo e riempimento
            Color border = borderPicker.getValue();
            Color fill = fillPicker.getValue();

            // Applica i decorator solo se non già applicati (evita stacking)
            if (newShape instanceof Line && border != null) {
                styledShape = new BorderColorDecorator(newShape, border);
            } else if (border != null && fill != null) { // se newShape non è una linea
                styledShape = new FillColorDecorator(newShape, fill);
                styledShape = new BorderColorDecorator(styledShape, border);
            }

            AddShapeCommand addCmd = new AddShapeCommand(model, styledShape);
            commandManager.executeCommand(addCmd);

            currentShape = styledShape; // La forma corrente è quella appena inserita
            currentShapeFactory = null; // Resetta la factory per richiedere una nuova selezione

            // Dopo aver aggiunto la forma, aggiorna gli spinner
            updateControlState(currentShape);
            updateSpinners(currentShape);
            redrawCanvas();
        }
    }

    private void handleDimensionChange(boolean isWidth, Double newValue) {
        if (currentShape == null || newValue == null) {
            return;
        }

        if (isWidth) {
            currentShape.setWidth(newValue);
        } else {
            currentShape.setHeight(newValue);
        }

        redrawCanvas();
    }

    private void updateControlState(AbstractShape shape) {
        boolean enableWidth = false;
        boolean enableHeight = false;
        boolean enableFill = false;
        boolean enableBorder = false;
        boolean enableDelete = false;

        if (shape != null) {
            // Ottieni la forma base (non decorata)
            AbstractShape baseShape = shape;
            while (baseShape instanceof ShapeDecorator decorator) {
                baseShape = decorator.getInnerShape();
            }
            if (!(baseShape instanceof Line)) {
                enableFill = true;
            }

            enableWidth = true;
            enableHeight = true;
            enableBorder = true;
            enableDelete = true;
        }

        widthSpinner.setDisable(!enableWidth);
        heightSpinner.setDisable(!enableHeight);
        fillPicker.setDisable(!enableFill);
        borderPicker.setDisable(!enableBorder);
        deleteButton.setDisable(!enableDelete);
    }

    /*
     * Metodo per la gestione dell'eliminazione di una figura selezionata
     */
    @FXML
    private void handleDeleteShape(ActionEvent event) {
        if (currentShape != null && model != null) {
            // si crea l'oggetto command responsabile della cancellazione e si chiede al commandManager di eseguirlo
            DeleteShapeCommand deleteCmd = new DeleteShapeCommand(model, currentShape);
            commandManager.executeCommand(deleteCmd);

            // successivamente deseleziono la forma, ripristino i binding e aggiorno il canvas
            currentShape = null;
            updateControlState(null);
            redrawCanvas();
        }
    }

    private AbstractShape selectShapeAt(double x, double y) {
        for (AbstractShape shape : model.getShapesOrderedByZ()) { // Ordina per z decrescente
            if (shape.containsPoint(x, y, SELECTION_THRESHOLD)) {
                currentShape = shape; // Imposta la figura selezionata
                updateSpinners(currentShape);
                updateControlState(currentShape);
                System.out.println("DEBUG: Figura selezionata: " + currentShape + " z: " + currentShape.getZ());
                return shape; // Restituisci la figura selezionata
            }
        }
        currentShape = null; // Deseleziona se nessuna figura è cliccata
        System.out.println("DEBUG: Nessuna figura selezionata.");
        return null;
    }

    // Metodo aggiornare gli spinner quando la figura corrente cambia
    private void updateSpinners(AbstractShape shape) {
        if (shape instanceof Line line) {
            widthSpinner.getValueFactory().setValue(line.getLength());
            heightSpinner.setDisable(true);
            heightSpinner.getValueFactory().setValue(1.0); // Altezza fissa per le linee4
        } else if (shape instanceof AbstractShape abstractShape) {
            widthSpinner.getValueFactory().setValue(abstractShape.getWidth());
            heightSpinner.getValueFactory().setValue(abstractShape.getHeight());
        } else {
            widthSpinner.getValueFactory().setValue(0.0);
            heightSpinner.getValueFactory().setValue(0.0);
        }
    }

    private void handleMousePressed(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        shapeMenu.hide();

        // Se non c'è una figura selezionata o il mouse non è sopra la figura corrente
        if (currentShape == null || !currentShape.containsPoint(x, y, SELECTION_THRESHOLD)) {
            currentShape = selectShapeAt(x, y); // Seleziona la figura sotto il mouse
        }

        if (currentShape != null && currentShape.containsPoint(x, y, SELECTION_THRESHOLD)) {
            // Calcola l'offset tra il mouse e la posizione della figura
            dragOffsetX = x - currentShape.getX();
            dragOffsetY = y - currentShape.getY();
            drawingCanvas.setCursor(Cursor.CLOSED_HAND);
            // appena l’utente clicca, fai prendere il focus al rootPane
            rootPane.requestFocus();
            // se fai click col tasto destro sulla figura, devi mostrare il contextMenu
            if (event.getButton() == MouseButton.SECONDARY) {
                shapeMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY());
            }
        } else {
            currentShape = null; // Deseleziona se il mouse è troppo lontano
            updateControlState(currentShape);
        }

        updateSpinners(currentShape);
        redrawCanvas();
    }

    private void handleMouseDragged(MouseEvent event) {

        if (currentShape == null) {
            return;
        }

        System.out.println("DEBUG: shape dragged");

        double newX = event.getX() - dragOffsetX;
        double newY = event.getY() - dragOffsetY;
        currentShape.moveTo(newX, newY);

        redrawCanvas();
    }

    private void handleMouseReleased(MouseEvent event) {
        drawingCanvas.setCursor(Cursor.DEFAULT);
        redrawCanvas();
    }

    private void handleMouseMoved(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        // Controlla se il mouse è sopra una figura
        boolean isOverShape = model.getShapesOrderedByZ().stream()
                .anyMatch(shape -> shape.containsPoint(x, y, SELECTION_THRESHOLD));

        if (isOverShape) {
            drawingCanvas.setCursor(Cursor.HAND); // Cambia il cursore in una mano
        } else {
            drawingCanvas.setCursor(Cursor.DEFAULT); // Ripristina il cursore predefinito
        }
    }

    private void redrawCanvas() {
        if (gc == null || drawingCanvas == null || model == null) {
            return;
        }

        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        for (AbstractShape shape : model.getShapes()) {
            if (shape != null) {
                shape.draw(gc);
                if (shape == currentShape) {
                    drawHighlightBorder(shape); // Disegna il bordo di selezione solo per la figura corrente
                }
            }
        }
    }

    // Metodo per disegnare un cerchietto
    private void drawHandle(double x, double y) {
        gc.setFill(Color.SKYBLUE);
        gc.fillOval(x - HANDLE_RADIUS, y - HANDLE_RADIUS, HANDLE_RADIUS * 2, HANDLE_RADIUS * 2);
    }

    // Metodo per disegnare il bordo di selezione e i manici
    private void drawHighlightBorder(AbstractShape shape) {
        // Ottieni la figura base (non decorata)
        while (shape instanceof ShapeDecorator decorator) {
            shape = decorator.getInnerShape();
        }

        // Disegna il bordo di selezione
        gc.setStroke(Color.SKYBLUE);
        gc.setLineWidth(1);
        gc.setLineDashes(5);
        if (shape instanceof Line line) {
            gc.strokeLine(line.getX(), line.getY(), line.getEndX(), line.getEndY()); // Linea tratteggiata
            gc.setLineDashes(0);
            // Disegna i manici agli estremi della linea
            drawHandle(line.getX(), line.getY()); // Estremo sinistro
            drawHandle(line.getEndX(), line.getEndY()); // Estremo destro
        } else {
            gc.strokeRect(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
            gc.setLineDashes(0);
            // Disegna i manici agli angoli del rettangolo/ellisse
            drawHandle(shape.getX(), shape.getY());
            drawHandle(shape.getX() + shape.getWidth(), shape.getY());
            drawHandle(shape.getX(), shape.getY() + shape.getHeight());
            drawHandle(shape.getX() + shape.getWidth(), shape.getY() + shape.getHeight());
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

