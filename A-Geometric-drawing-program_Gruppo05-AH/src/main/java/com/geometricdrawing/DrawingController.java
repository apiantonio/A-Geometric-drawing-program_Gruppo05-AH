package com.geometricdrawing;

import com.geometricdrawing.command.*;
import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.decorator.ShapeDecorator;
import com.geometricdrawing.factory.EllipseFactory;
import com.geometricdrawing.factory.LineFactory;
import com.geometricdrawing.factory.RectangleFactory;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.templateMethod.AbstractMouseHandler;
import com.geometricdrawing.templateMethod.MousePressedHandler;
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
    @FXML private Button copyButton; // Bottone per la Copia
    @FXML private ColorPicker fillPicker;
    @FXML private ColorPicker borderPicker;
    @FXML private Spinner<Double> heightSpinner;
    @FXML private Spinner<Double> widthSpinner;
    private ContextMenu shapeMenu;

    private static final double HANDLE_RADIUS = 3.0;
    private static final double SELECTION_THRESHOLD = 5.0;
    private static final double BORDER_MARGIN = 5.0;
    private static final double VISIBLE_SHAPE_PORTION = 0.1;
    private static final double HIDDEN_SHAPE_PORTION = 0.9;

    private DrawingModel model;
    private GraphicsContext gc;
    private ShapeFactory currentShapeFactory;
    private AbstractShape currentShape;
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

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

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

            shapeMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Elimina");
            deleteItem.setOnAction(e -> handleDeleteShape(new ActionEvent()));
            MenuItem copyItem = new MenuItem("Copia"); // Voce di menu per Copia
            copyItem.setOnAction(e -> handleCopyShape(new ActionEvent())); // Associa l'handler
            shapeMenu.getItems().addAll(deleteItem, copyItem); // Aggiungi "Copia" al context menu

            drawingCanvas.setOnMouseClicked(this::handleCanvasClick);
            drawingCanvas.setOnMousePressed(new MousePressedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseDragged(this::handleMouseDragged);
            drawingCanvas.setOnMouseReleased(this::handleMouseReleased);
            drawingCanvas.setOnMouseMoved(this::handleMouseMoved);

            fillPicker.setValue(Color.LIGHTGREEN);
            borderPicker.setValue(Color.ORANGE);

            updateControlState(null); // Stato iniziale dei controlli

            rootPane.setFocusTraversable(true);
            rootPane.setOnKeyPressed(this::onRootKeyPressed); // Assicura che onRootKeyPressed sia chiamato


            drawingCanvas.widthProperty().bind(canvasContainer.widthProperty());
            drawingCanvas.heightProperty().bind(canvasContainer.heightProperty());

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
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            handleDeleteShape(new ActionEvent());
            event.consume();
        }
        // Scorciatoia per Copia (CTRL+C)
        if (KeyCombination.keyCombination("CTRL+C").match(event)) {
            handleCopyShape(new ActionEvent());
            event.consume();
        }
    }

    @FXML
    public void handleSelectLinea(ActionEvent event) {
        currentShape = null;
        updateControlState(null);
        redrawCanvas();
        borderPicker.setDisable(false);
        fillPicker.setDisable(true);
        currentShapeFactory = new LineFactory();
    }

    @FXML
    public void handleSelectRettangolo(ActionEvent event) {
        currentShape = null;
        updateControlState(null);
        redrawCanvas();
        fillPicker.setDisable(false);
        borderPicker.setDisable(false);
        currentShapeFactory = new RectangleFactory();
    }

    @FXML
    public void handleSelectEllisse(ActionEvent event) {
        currentShape = null;
        updateControlState(null);
        redrawCanvas();
        fillPicker.setDisable(false);
        borderPicker.setDisable(false);
        currentShapeFactory = new EllipseFactory();
    }

    public void handleCanvasClick(MouseEvent event) {
        if (model == null || commandManager == null || heightSpinner == null || widthSpinner == null) {
            System.err.println("Errore: Componenti non inizializzati (model, commandManager o spinners).");
            return;
        }

        double x = event.getX();
        double y = event.getY();

        if (currentShapeFactory != null) {
            AbstractShape newShape = currentShapeFactory.createShape(x, y);
            if (isTooClose(newShape, x, y)) {
                return;
            }

            AbstractShape styledShape = newShape;
            Color border = borderPicker.getValue();
            Color fill = fillPicker.getValue();

            if (newShape instanceof Line) {
                if (border != null) styledShape = new BorderColorDecorator(newShape, border);
            } else {
                if (fill != null) styledShape = new FillColorDecorator(newShape, fill);
                if (border != null) styledShape = new BorderColorDecorator(styledShape, border);
            }


            AddShapeCommand addCmd = new AddShapeCommand(model, styledShape);
            commandManager.executeCommand(addCmd);

            currentShape = styledShape;
            currentShapeFactory = null;

            updateControlState(currentShape);
            fillPicker.setDisable(true);
            borderPicker.setDisable(true);

            updateSpinners(currentShape);
            redrawCanvas();
        }
    }

    private boolean isTooClose(AbstractShape newShape, double x, double y) {
        double shapeWidth = newShape.getWidth();
        double shapeHeight = newShape.getHeight();
        return x < BORDER_MARGIN ||
                y < BORDER_MARGIN ||
                x + shapeWidth > drawingCanvas.getWidth() - BORDER_MARGIN ||
                y + shapeHeight > drawingCanvas.getHeight() - BORDER_MARGIN;
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
        }

        if (widthSpinner != null) widthSpinner.setDisable(!enableWidth);
        if (heightSpinner != null) heightSpinner.setDisable(!enableHeight);
        if (fillPicker != null) fillPicker.setDisable(!enableFill);
        if (borderPicker != null) borderPicker.setDisable(!enableBorder);
        if (deleteButton != null) deleteButton.setDisable(!enableDelete);
        if (copyButton != null) copyButton.setDisable(!enableCopy); // Aggiorna stato bottone Copia
    }

    @FXML
    public void handleDeleteShape(ActionEvent event) {
        if (currentShape != null && model != null && commandManager != null) {
            if(shapeMenu != null) shapeMenu.hide();
            DeleteShapeCommand deleteCmd = new DeleteShapeCommand(model, currentShape);
            commandManager.executeCommand(deleteCmd);
            currentShape = null;
            updateControlState(null);
            redrawCanvas();
        }
    }

    /**
     * Gestisce l'azione di copia di una figura.
     * @param event L'evento che ha scatenato l'azione.
     */
    @FXML
    public void handleCopyShape(ActionEvent event) {
        if (currentShape != null && commandManager != null && clipboardManager != null) {
            CopyShapeCommand copyCmd = new CopyShapeCommand(currentShape, clipboardManager);
            commandManager.executeCommand(copyCmd);
            System.out.println("DEBUG: Figura copiata nella clipboard interna.");
            // La figura rimane selezionata dopo la copia. Non è necessario ridisegnare o aggiornare lo stato dei controlli
            // a meno che non si voglia dare un feedback visivo specifico per la copia.
        }
    }

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

    private void handleMouseDragged(MouseEvent event) {
        if (currentShape == null || model == null || commandManager == null) {
            return;
        }

        double newX = event.getX() - dragOffsetX;
        double newY = event.getY() - dragOffsetY;

        double canvasWidth = drawingCanvas.getWidth();
        double canvasHeight = drawingCanvas.getHeight();
        double shapeWidth = currentShape.getWidth();
        double shapeHeight = currentShape.getHeight();

        double minX = -shapeWidth * (1 - VISIBLE_SHAPE_PORTION);
        double maxX = canvasWidth - shapeWidth * VISIBLE_SHAPE_PORTION;
        double minY = -shapeHeight * (1 - VISIBLE_SHAPE_PORTION);
        double maxY = canvasHeight - shapeHeight * VISIBLE_SHAPE_PORTION;


        newX = Math.max(minX, Math.min(newX, maxX));
        newY = Math.max(minY, Math.min(newY, maxY));


        MoveShapeCommand moveCmd = new MoveShapeCommand(model, currentShape, newX, newY);
        commandManager.executeCommand(moveCmd);

    }

    private void handleMouseReleased(MouseEvent event) {
        if (drawingCanvas != null) drawingCanvas.setCursor(Cursor.DEFAULT);
    }

    private void handleMouseMoved(MouseEvent event) {
        if (model == null || drawingCanvas == null) return;
        double x = event.getX();
        double y = event.getY();

        boolean isOverShape = model.getShapesOrderedByZ().stream()
                .anyMatch(shape -> shape.containsPoint(x, y, SELECTION_THRESHOLD));

        drawingCanvas.setCursor(isOverShape ? Cursor.HAND : Cursor.DEFAULT);
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
                    drawHighlightBorder(shape);
                }
            }
        }
    }

    private void drawHandle(double x, double y) {
        gc.setFill(Color.SKYBLUE);
        gc.fillOval(x - HANDLE_RADIUS, y - HANDLE_RADIUS, HANDLE_RADIUS * 2, HANDLE_RADIUS * 2);
    }

    private void drawHighlightBorder(AbstractShape shape) {
        AbstractShape baseShape = shape;
        while (baseShape instanceof ShapeDecorator decorator) {
            baseShape = decorator.getInnerShape();
        }

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

    public AbstractShape getCurrentShape() {
        return currentShape;
    }

    public void setCurrentShape(AbstractShape shape) {
        this.currentShape = shape;
    }

    public ContextMenu getShapeMenu() {
        return shapeMenu;
    }

    public double getDragOffsetX() { return dragOffsetX; }
    public void setDragOffsetX(double dragOffsetX) { this.dragOffsetX = dragOffsetX; }
    public double getDragOffsetY() { return dragOffsetY; }
    public void setDragOffsetY(double dragOffsetY) { this.dragOffsetY = dragOffsetY; }

    public void showAlertDialog(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public Canvas getDrawingCanvas() { return drawingCanvas; }
    public DrawingModel getModel() { return model; }
    public AnchorPane getRootPane() { return rootPane; }

    public void setCurrentShape(Object o) {
        if (o instanceof AbstractShape || o == null) {
            this.currentShape = (AbstractShape) o;
        } else {
            throw new IllegalArgumentException("Tentativo di impostare currentShape con un tipo non valido: " + o.getClass().getName());
        }
    }
}