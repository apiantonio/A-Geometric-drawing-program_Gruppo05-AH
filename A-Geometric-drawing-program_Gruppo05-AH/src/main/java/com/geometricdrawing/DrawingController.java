package com.geometricdrawing;

import com.geometricdrawing.command.*;
import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.decorator.ShapeDecorator;
import com.geometricdrawing.factory.EllipseFactory;
import com.geometricdrawing.factory.LineFactory;
import com.geometricdrawing.factory.RectangleFactory;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.templateMethod.*;
import com.geometricdrawing.strategy.*;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
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

import javafx.stage.Window;

import java.util.function.UnaryOperator;

/**
 * @Autore: Gruppo05
 * @Scopo: Controller dell'applicazione, gestisce gli eventi e le interazioni con l'interfaccia utente.
 */
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

    private static final double HANDLE_RADIUS = 3.0;         // raggio del cerchietto che compare alla selezione di una figura
    private static final double SELECTION_THRESHOLD = 5.0;   // threshold per la selezione

    private DrawingModel model;
    private GraphicsContext gc;
    private ShapeFactory currentShapeFactory;               // factory per la creazione della figura
    private AbstractShape currentShape;                     // figura selezionata
    private CommandManager commandManager;
    private double dragOffsetX;
    private double dragOffsetY;
    private FileOperationContext fileOperationContext;

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
            setModel(new DrawingModel());
            setCommandManager(new CommandManager());

            this.fileOperationContext = new FileOperationContext(this);

            // Al click col tasto destro richiama la creazione del ContextMenu
            shapeMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Elimina");
            deleteItem.setOnAction(e -> handleDeleteShape(new ActionEvent()));
            shapeMenu.getItems().add(deleteItem);
            // contextMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY());
            // TO ADD ALTRI PER LE ALTRE FUNZIONALITA'

            drawingCanvas.setOnMouseClicked(new MouseClickedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMousePressed(new MousePressedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseDragged(new MouseDraggedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseReleased(new MouseReleasedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseMoved(new MouseMovedHandler(drawingCanvas, this)::handleMouseEvent); // per il cambio cursore

            // colore di partenza dei colorPicker
            fillPicker.setValue(Color.LIGHTGREEN);
            borderPicker.setValue(Color.ORANGE);

            //per il binding all'avvio, solo una nuova forma può essere premuto come bottone nella barra degli strumenti
            updateControlState(null);

            // affinché rootPane possa ricevere focus
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
    public void handleSelectLinea(ActionEvent event) {
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
    public void handleSelectRettangolo(ActionEvent event) {
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
    public void handleSelectEllisse(ActionEvent event) {
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

    public boolean isTooClose(AbstractShape newShape, double x, double y) {
        double shapeWidth = newShape.getWidth();
        double shapeHeight = newShape.getHeight();

        // Verifica se la posizione è troppo vicina ai bordi
        boolean isTooClose = x + shapeWidth > drawingCanvas.getWidth() ||
                            y + shapeHeight > drawingCanvas.getHeight();

        return isTooClose;
    }

    private void handleDimensionChange(boolean isWidth, Double newValue) {
        if (currentShape == null || newValue == null) {
            return;
        }

        if (isWidth) {
            ChangeWidthCommand cmd = new ChangeWidthCommand(model, currentShape, newValue);
            commandManager.executeCommand(cmd);
        } else {
            ChangeHeightCommand cmd = new ChangeHeightCommand(model, currentShape, newValue);
            commandManager.executeCommand(cmd);
        }

        redrawCanvas();
    }

    public void updateControlState(AbstractShape shape) {
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
                enableHeight = true;
                enableFill = true;
            }

            enableWidth = true;
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
    public void handleDeleteShape(ActionEvent event) {
        if (currentShape != null && model != null) {
            // per gestire il caso in cui si clicca il tasto destro ma si cancella con canc e il context menu è ancora aperto
            shapeMenu.hide();
            // si crea l'oggetto command responsabile della cancellazione e si chiede al commandManager di eseguirlo
            DeleteShapeCommand deleteCmd = new DeleteShapeCommand(model, currentShape);
            commandManager.executeCommand(deleteCmd);

            // successivamente deseleziono la forma, ripristino i binding e aggiorno il canvas
            currentShape = null;
            updateControlState(null);
            redrawCanvas();
        }
    }

    public AbstractShape selectShapeAt(double x, double y) {
        for (AbstractShape shape : model.getShapesOrderedByZ()) { // Ordina per z decrescente
            if (shape.containsPoint(x, y, SELECTION_THRESHOLD)) {
                currentShape = shape; // Imposta la figura selezionata
                updateSpinners(currentShape);
                updateControlState(currentShape);

                // MOMENTANEE PER LA PRIMA SPRINT (da rimuovere poi)
                fillPicker.setDisable(true);
                borderPicker.setDisable(true);

                System.out.println("DEBUG: Figura selezionata: " + currentShape + " z: " + currentShape.getZ());
                return shape; // Restituisci la figura selezionata
            }
        }
        currentShape = null; // Deseleziona se nessuna figura è cliccata
        System.out.println("DEBUG: Nessuna figura selezionata.");
        return null;
    }

    // Metodo aggiornare gli spinner quando la figura corrente cambia
    public void updateSpinners(AbstractShape shape) {
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

    public void showContextMenu(MouseEvent event) {
        shapeMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY());
    }

    public void redrawCanvas() {
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
    public void handleSaveSerialized(ActionEvent event) {
        fileOperationContext.executeSave(new SerializedSaveStrategy());
    }

    @FXML
    public void handleLoadSerialized(ActionEvent event) {
        fileOperationContext.executeLoad(new SerializedLoadStrategy());
    }

    @FXML
    public void handleSaveAsPng(ActionEvent event) {
        fileOperationContext.executeSave(new PngSaveStrategy());
    }

    @FXML
    public void handleSaveAsPdf(ActionEvent event) {
        fileOperationContext.executeSave(new PdfSaveStrategy());
    }

    public Window getWindow() {
        if (drawingCanvas == null) {
            return null;
        }
        Scene scene = drawingCanvas.getScene();
        if (scene == null) {
            return null;
        }
        return scene.getWindow();
    }

    public void showAlertDialog(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public AbstractShape getCurrentShape() {
        return currentShape;
    }

    public void setCurrentShape(AbstractShape shape) {
        this.currentShape = shape;
    }

    public ContextMenu getShapeMenu() {
        return shapeMenu;
    }

    public double getDragOffsetX() {
        return dragOffsetX;
    }

    public double getDragOffsetY() {
        return dragOffsetY;
    }

    public void setDragOffsetX(double dragOffsetX) {
        this.dragOffsetX = dragOffsetX;
    }

    public void setDragOffsetY(double dragOffsetY) {
        this.dragOffsetY = dragOffsetY;
    }

    public Canvas getDrawingCanvas() {
        return drawingCanvas;
    }

    public DrawingModel getModel() {
        return model;
    }

    public AnchorPane getRootPane() {
        return rootPane;
    }

    public void setCurrentShape(Object o) {
        this.currentShape = (AbstractShape) o;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public ColorPicker getBorderPicker() {
        return borderPicker;
    }

    public ColorPicker getFillPicker() {
        return fillPicker;
    }

    public Spinner<Double> getHeightSpinner() {
        return heightSpinner;
    }

    public Spinner<Double> getWidthSpinner() {
        return widthSpinner;
    }

    public ShapeFactory getCurrentShapeFactory() {
        return currentShapeFactory;
    }

    public void setCurrentShapeFactory(ShapeFactory currentShapeFactory) {
        this.currentShapeFactory = currentShapeFactory;
    }
}