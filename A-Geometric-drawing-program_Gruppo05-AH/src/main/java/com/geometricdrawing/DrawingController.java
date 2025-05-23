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
import javafx.scene.input.KeyCombination;
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
    @FXML private Button copyButton;
    @FXML private Button pasteButton;
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
    private ClipboardManager clipboardManager; // Gestore per la clipboard


    public void setModel(DrawingModel model) {
        this.model = model;
        if (this.model != null && this.model.getShapes() != null) {
            this.model.getShapes().addListener((ListChangeListener.Change<? extends AbstractShape> c) -> redrawCanvas());
        }
        // redrawCanvas(); // Ridisegna se il modello cambia
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
            // Inizializza model, commandManager e clipboardManager se non sono già stati iniettati o creati
            if (this.model == null) this.model = new DrawingModel();
            if (this.commandManager == null) this.commandManager = new CommandManager();
            if (this.clipboardManager == null) this.clipboardManager = new ClipboardManager(); // Inizializza ClipboardManager

            setModel(this.model); // Imposta il modello e aggiungi listener

            this.fileOperationContext = new FileOperationContext(this);

            // Al click col tasto destro richiama la creazione del ContextMenu
            shapeMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Elimina");
            deleteItem.setOnAction(e -> handleDeleteShape(new ActionEvent()));
            MenuItem copyItem = new MenuItem("Copia"); // Voce di menu per Copia
            copyItem.setOnAction(e -> handleCopyShape(new ActionEvent())); // Associa l'handler
            MenuItem pasteItem = new MenuItem("Incolla");
            pasteItem.setOnAction(e -> handlePasteShape(new ActionEvent()));

            pasteItem.setDisable(!clipboardManager.hasContent());
            shapeMenu.getItems().addAll(deleteItem, copyItem, pasteItem); // Aggiungi "Copia" al context menu

            drawingCanvas.setOnMouseClicked(new MouseClickedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMousePressed(new MousePressedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseDragged(new MouseDraggedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseReleased(new MouseReleasedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseMoved(new MouseMovedHandler(drawingCanvas, this)::handleMouseEvent); // per il cambio cursore

            // colore di partenza dei colorPicker
            fillPicker.setValue(Color.LIGHTGREEN);
            borderPicker.setValue(Color.ORANGE);

            updateControlState(null); // Stato iniziale dei controlli

            // affinché rootPane possa ricevere focus
            rootPane.setFocusTraversable(true);
            rootPane.setOnKeyPressed(this::onRootKeyPressed); // Assicura che onRootKeyPressed sia chiamato

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

        // Inizializzazione Spinner Altezza
        if (heightSpinner != null) {
            SpinnerValueFactory<Double> heightFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, ShapeFactory.DEFAULT_HEIGHT, 1.0);
            heightSpinner.setValueFactory(heightFactory);
            heightSpinner.setEditable(true);

            // listener per ridimensionamento con freccette
            heightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null) handleDimensionChange(false, newValue);
            });
            configureSpinnerFocusListener(heightSpinner);
            configureNumericTextFormatter(heightSpinner);
        }

        // Inizializzazione Spinner Larghezza
        if (widthSpinner != null) {
            SpinnerValueFactory<Double> widthFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, ShapeFactory.DEFAULT_WIDTH, 1.0);
            widthSpinner.setValueFactory(widthFactory);
            widthSpinner.setEditable(true);

            widthSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null) handleDimensionChange(true, newValue);
            });
            configureSpinnerFocusListener(widthSpinner);
            configureNumericTextFormatter(widthSpinner);
        }
        currentShapeFactory = null;
        updatePasteControlsState();
        redrawCanvas(); // Prima ridisegnata
    }

    private void configureNumericTextFormatter(Spinner<Double> spinner) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?\\d*([.,]\\d*)?")) { // Accetta punto o virgola come separatore decimale
                return change;
            }
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        spinner.getEditor().setTextFormatter(textFormatter);

        textFormatter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                try {
                    double value = Double.parseDouble(newVal.replace(',', '.'));
                    if (spinner.getValueFactory() != null && spinner.getValueFactory().getValue() != value) {
                        spinner.getValueFactory().setValue(value);
                    }
                } catch (NumberFormatException e) {
                    // Ignora se il formato non è ancora valido
                }
            }
        });
    }


    private void configureSpinnerFocusListener(Spinner<Double> spinner) {
        spinner.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && spinner.getValueFactory() != null) {
                try {
                    String text = spinner.getEditor().getText().replace(',', '.');
                    double value = Double.parseDouble(text);
                    SpinnerValueFactory.DoubleSpinnerValueFactory factory = (SpinnerValueFactory.DoubleSpinnerValueFactory) spinner.getValueFactory();
                    if (value < factory.getMin()) value = factory.getMin();
                    if (value > factory.getMax()) value = factory.getMax();
                    factory.setValue(value);
                } catch (NumberFormatException e) {
                    spinner.getEditor().setText(String.valueOf(spinner.getValue()).replace('.', ','));
                }
            }
        });
    }


    @FXML
    private void onRootKeyPressed(KeyEvent event) {
        // la cancellazione da tastiera può avvenire con backspace o delete
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            // richiama il metodo per la cancellazione tramite bottone
            handleDeleteShape(new ActionEvent());
            event.consume();
        }
        // Scorciatoia per Copia (CTRL+C)
        if (KeyCombination.keyCombination("CTRL+C").match(event)) {
            handleCopyShape(new ActionEvent());
            event.consume();
        }

        if (KeyCombination.keyCombination("CTRL+V").match(event)) {
            if (clipboardManager.hasContent()) { // Only handle if there's something to paste
                handlePasteShape(new ActionEvent());
            }
            event.consume();
        }
    }

    // Metodo di utilità per inizializzare la selezione della figura
    private void initializeShapeSelection(ShapeFactory factory, boolean disableFillPicker, boolean disableBorderPicker) {
        currentShape = null;  // deseleziona la figura corrente
        updateControlState(null);
        redrawCanvas();

        fillPicker.setDisable(disableFillPicker);
        borderPicker.setDisable(disableBorderPicker);
        currentShapeFactory = factory;
    }

    @FXML
    public void handleSelectLinea(ActionEvent event) {
        initializeShapeSelection(new LineFactory(), true, false);
    }

    @FXML
    public void handleSelectRettangolo(ActionEvent event) {
        initializeShapeSelection(new RectangleFactory(), false, false);
    }

    @FXML
    public void handleSelectEllisse(ActionEvent event) {
        initializeShapeSelection(new EllipseFactory(), false, false);
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
        if (currentShape == null || newValue == null || model == null || commandManager == null) {
            return;
        }

        if (newValue <= 0) {
            if (isWidth) {
                widthSpinner.getValueFactory().setValue(currentShape.getWidth());
            } else {
                heightSpinner.getValueFactory().setValue(currentShape.getHeight());
            }
            return;
        }

        if (isWidth) {
            ChangeWidthCommand cmd = new ChangeWidthCommand(model, currentShape, newValue);
            commandManager.executeCommand(cmd);
        } else {
            if (!(getBaseShape(currentShape) instanceof Line)) {
                ChangeHeightCommand cmd = new ChangeHeightCommand(model, currentShape, newValue);
                commandManager.executeCommand(cmd);
            }
        }

        redrawCanvas();
    }

    private AbstractShape getBaseShape(AbstractShape shape) {
        AbstractShape base = shape;
        while (base instanceof ShapeDecorator) {
            base = ((ShapeDecorator) base).getInnerShape();
        }
        return base;
    }

    public void updateControlState(AbstractShape shape) {
        boolean enableWidth = false;
        boolean enableHeight = false;
        boolean enableFill = false;
        boolean enableBorder = false;
        boolean enableDelete = false;
        boolean enableCopy = false; // Solo per copia

        if (shape != null) {
            AbstractShape baseShape = getBaseShape(shape);
            enableWidth = true;
            enableDelete = true;
            enableCopy = true; // Abilita copia se una forma è selezionata

            if (!(baseShape instanceof Line)) {
                enableHeight = true;
                enableFill = false;
                enableBorder = false;
            } else {
                enableHeight = false;
                enableFill = false;
                enableBorder = false;
            }

            enableWidth = true;
            enableBorder = true;
            enableDelete = true;
        }

        if (widthSpinner != null) widthSpinner.setDisable(!enableWidth);
        if (heightSpinner != null) heightSpinner.setDisable(!enableHeight);
        if (fillPicker != null) fillPicker.setDisable(!enableFill);
        if (borderPicker != null) borderPicker.setDisable(!enableBorder);
        if (deleteButton != null) deleteButton.setDisable(!enableDelete);
        if (copyButton != null) copyButton.setDisable(!enableCopy);
        if (pasteButton != null) {pasteButton.setDisable(!clipboardManager.hasContent());}

        boolean enablePaste = clipboardManager != null && clipboardManager.hasContent();
        if (pasteButton != null) {
            pasteButton.setDisable(!enablePaste);
        }
        // Update context menu item for paste as well
        if (shapeMenu != null) {
            shapeMenu.getItems().stream()
                    .filter(item -> "Incolla".equals(item.getText()))
                    .findFirst()
                    .ifPresent(item -> item.setDisable(!enablePaste));
        }
    }

    private void updatePasteControlsState() {
        boolean hasContent = clipboardManager != null && clipboardManager.hasContent();
        if (pasteButton != null) {
            pasteButton.setDisable(!hasContent);
        }
        if (shapeMenu != null) {
            shapeMenu.getItems().stream()
                    .filter(item -> "Incolla".equals(item.getText()))
                    .findFirst()
                    .ifPresent(item -> item.setDisable(!hasContent));
        }
    }

    /**
     * Metodo per la gestione dell'eliminazione di una figura selezionata
     */
    @FXML
    public void handleDeleteShape(ActionEvent event) {
        if (currentShape != null && model != null && commandManager != null) {
            if(shapeMenu != null) shapeMenu.hide();
            DeleteShapeCommand deleteCmd = new DeleteShapeCommand(model, currentShape);
            commandManager.executeCommand(deleteCmd);

            // successivamente deseleziono la forma, ripristino i binding e aggiorno il canvas
            currentShape = null;
            updateControlState(null);
            redrawCanvas();
        }
    }

    @FXML
    public void handleCopyShape(ActionEvent event) {
        if (currentShape != null && commandManager != null && clipboardManager != null) {
            CopyShapeCommand copyCmd = new CopyShapeCommand(currentShape, clipboardManager);
            commandManager.executeCommand(copyCmd);
            System.out.println("DEBUG: Figura copiata nella clipboard interna.");
            // La figura rimane selezionata dopo la copia. Non è necessario ridisegnare o aggiornare lo stato dei controlli
            // a meno che non si voglia dare un feedback visivo specifico per la copia.
            updatePasteControlsState();
        }
    }

    @FXML
    public void handlePasteShape(ActionEvent event) {
        if (model != null && commandManager != null && clipboardManager != null && clipboardManager.hasContent()) {

            PasteShapeCommand pasteCmd = new PasteShapeCommand(model, clipboardManager);
            commandManager.executeCommand(pasteCmd);

            AbstractShape pastedShape = pasteCmd.getPastedShape();
            if (pastedShape != null) {
                setCurrentShape(pastedShape); // Select the newly pasted shape
                updateControlState(pastedShape);
                updateSpinners(pastedShape); // Update spinners to reflect the new shape's dimensions
            }
            redrawCanvas();
            updatePasteControlsState(); // Update paste button/menu item state
        }
    }

    /**
     * Metodo per selezionare una figura al click del mouse
     * @param x coordinata x del click
     * @param y coordinata y del click
     * @return la figura selezionata, null se non c'è nessuna figura
     */
    public AbstractShape selectShapeAt(double x, double y) {
        if (model == null) return null;
        for (AbstractShape shape : model.getShapesOrderedByZ()) {
            if (shape.containsPoint(x, y, SELECTION_THRESHOLD)) {
                currentShape = shape;
                updateSpinners(currentShape);
                updateControlState(currentShape);

                // Logica "MOMENTANEE PER LA PRIMA SPRINT"
                if (fillPicker != null) fillPicker.setDisable(true);
                if (borderPicker != null) borderPicker.setDisable(true);

                System.out.println("DEBUG: Figura selezionata: " + currentShape + " z: " + currentShape.getZ());
                return shape;
            }
        }
        currentShape = null;
        updateSpinners(null);
        updateControlState(null);
        System.out.println("DEBUG: Nessuna figura selezionata.");
        return null;
    }

    // Metodo aggiornare gli spinner quando la figura corrente cambia
    public void updateSpinners(AbstractShape shape) {
        if (widthSpinner == null || heightSpinner == null || widthSpinner.getValueFactory() == null || heightSpinner.getValueFactory() == null) return;

        if (shape != null) {
            AbstractShape baseShape = getBaseShape(shape);
            if (baseShape instanceof Line line) {
                widthSpinner.getValueFactory().setValue(line.getWidth());
                heightSpinner.getValueFactory().setValue(line.getHeight());
                heightSpinner.setDisable(true);
            } else {
                widthSpinner.getValueFactory().setValue(baseShape.getWidth());
                heightSpinner.getValueFactory().setValue(baseShape.getHeight());
                heightSpinner.setDisable(false);
            }
        } else {
            widthSpinner.getValueFactory().setValue(ShapeFactory.DEFAULT_WIDTH);
            heightSpinner.getValueFactory().setValue(ShapeFactory.DEFAULT_HEIGHT);
        }
    }

    public void showContextMenu(MouseEvent event) {
        if (shapeMenu != null) {
            shapeMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY());
        }
    }

    /**
     * Metodo per disegnare il canvas
     * Cancella tutto ciò che è sul canvas e ridisegna le figure
     */
    public void redrawCanvas() {
        if (gc == null || drawingCanvas == null || model == null) {
            return;
        }

        // Cancella tutto ciò che è sul canvas
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        // Disegna tutte le figure nel modello
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
        AbstractShape baseShape = getBaseShape(shape);

        // Disegna il bordo di selezione
        gc.setStroke(Color.SKYBLUE);
        gc.setLineWidth(1);
        gc.setLineDashes(5);
        if (baseShape instanceof Line line) {
            gc.strokeLine(line.getX(), line.getY(), line.getEndX(), line.getEndY());
            drawHandle(line.getX(), line.getY());
            drawHandle(line.getEndX(), line.getEndY());
        } else {
            gc.strokeRect(baseShape.getX(), baseShape.getY(), baseShape.getWidth(), baseShape.getHeight());
            drawHandle(baseShape.getX(), baseShape.getY());
            drawHandle(baseShape.getX() + baseShape.getWidth(), baseShape.getY());
            drawHandle(baseShape.getX(), baseShape.getY() + baseShape.getHeight());
            drawHandle(baseShape.getX() + baseShape.getWidth(), baseShape.getY() + baseShape.getHeight());
        }
        gc.setLineDashes(0);
    }

    @FXML
    public void handleSaveSerialized(ActionEvent event) {
        if (fileOperationContext != null) fileOperationContext.executeSave(new SerializedSaveStrategy());
    }

    @FXML
    public void handleLoadSerialized(ActionEvent event) {
        if (fileOperationContext != null) fileOperationContext.executeLoad(new SerializedLoadStrategy());
    }

    @FXML
    public void handleSaveAsPng(ActionEvent event) {
        if (fileOperationContext != null) fileOperationContext.executeSave(new PngSaveStrategy());
    }

    @FXML
    public void handleSaveAsPdf(ActionEvent event) {
        if (fileOperationContext != null) fileOperationContext.executeSave(new PdfSaveStrategy());
    }

    public Window getWindow() {
        return (drawingCanvas != null && drawingCanvas.getScene() != null) ? drawingCanvas.getScene().getWindow() : null;
    }

    public void showAlertDialog(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public AbstractShape getCurrentShape() { return currentShape; }

    public void setCurrentShape(Object o) {
        if (o instanceof AbstractShape || o == null) {
            this.currentShape = (AbstractShape) o;
        } else {
            throw new IllegalArgumentException("Tentativo di impostare currentShape con un tipo non valido: " + o.getClass().getName());
        }
    }

    public void setCurrentShape(AbstractShape shape) { this.currentShape = shape; }

    public ContextMenu getShapeMenu() { return shapeMenu; }

    public double getDragOffsetX() { return dragOffsetX; }

    public void setDragOffsetX(double dragOffsetX) { this.dragOffsetX = dragOffsetX; }

    public double getDragOffsetY() { return dragOffsetY; }

    public void setDragOffsetY(double dragOffsetY) { this.dragOffsetY = dragOffsetY; }

    public Canvas getDrawingCanvas() { return drawingCanvas; }

    public DrawingModel getModel() { return model; }

    public AnchorPane getRootPane() { return rootPane; }

    public CommandManager getCommandManager() { return commandManager; }

    public ColorPicker getBorderPicker() { return borderPicker; }

    public ColorPicker getFillPicker() { return fillPicker; }

    public Spinner<Double> getHeightSpinner() { return heightSpinner; }

    public Spinner<Double> getWidthSpinner() { return widthSpinner; }

    public ShapeFactory getCurrentShapeFactory() { return currentShapeFactory; }

    public void setCurrentShapeFactory(ShapeFactory currentShapeFactory) {
        this.currentShapeFactory = currentShapeFactory;
    }
}