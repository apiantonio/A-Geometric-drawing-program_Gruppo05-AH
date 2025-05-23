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
import javafx.geometry.Point2D;
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
    @FXML private ColorPicker fillPicker;
    @FXML private ColorPicker borderPicker;
    @FXML private Spinner<Double> heightSpinner;
    @FXML private Spinner<Double> widthSpinner;
    private ContextMenu shapeMenu;

    private static final double HANDLE_RADIUS = 3.0;
    // SELECTION_THRESHOLD è definito in AbstractMouseHandler, lo useremo da lì.

    private DrawingModel model;
    private GraphicsContext gc;
    private ShapeFactory currentShapeFactory;
    private AbstractShape currentShape;
    private CommandManager commandManager;
    private double dragOffsetX;
    private double dragOffsetY;
    private FileOperationContext fileOperationContext;
    private ClipboardManager clipboardManager;
    private ZoomHandler zoomHandler; // <-- NUOVO CAMPO

    public void setModel(DrawingModel model) {
        this.model = model;
        if (this.model != null && this.model.getShapes() != null) {
            this.model.getShapes().addListener((ListChangeListener.Change<? extends AbstractShape> c) -> redrawCanvas());
        }
    }

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @FXML
    public void initialize() {
        if (drawingCanvas != null) {
            gc = drawingCanvas.getGraphicsContext2D();
            if (this.model == null) this.model = new DrawingModel();
            if (this.commandManager == null) this.commandManager = new CommandManager();
            if (this.clipboardManager == null) this.clipboardManager = new ClipboardManager();

            setModel(this.model);

            this.fileOperationContext = new FileOperationContext(this);
            this.zoomHandler = new ZoomHandler(this, drawingCanvas);

            shapeMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Elimina");
            deleteItem.setOnAction(e -> handleDeleteShape(new ActionEvent()));
            MenuItem copyItem = new MenuItem("Copia");
            copyItem.setOnAction(e -> handleCopyShape(new ActionEvent()));
            shapeMenu.getItems().addAll(deleteItem, copyItem);

            drawingCanvas.setOnMouseClicked(new MouseClickedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMousePressed(new MousePressedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseDragged(new MouseDraggedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseReleased(new MouseReleasedHandler(drawingCanvas, this)::handleMouseEvent);
            drawingCanvas.setOnMouseMoved(new MouseMovedHandler(drawingCanvas, this)::handleMouseEvent);

            fillPicker.setValue(Color.LIGHTGREEN);
            borderPicker.setValue(Color.ORANGE);

            updateControlState(null);

            rootPane.setFocusTraversable(true);
            rootPane.setOnKeyPressed(this::onRootKeyPressed);

            drawingCanvas.widthProperty().bind(canvasContainer.widthProperty());
            drawingCanvas.heightProperty().bind(canvasContainer.heightProperty());

            drawingCanvas.widthProperty().addListener((obs, oldVal, newVal) -> redrawCanvas());
            drawingCanvas.heightProperty().addListener((obs, oldVal, newVal) -> redrawCanvas());
        } else {
            System.err.println("Errore: drawingCanvas non è stato iniettato!");
        }

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
        redrawCanvas();
    }

    private void configureNumericTextFormatter(Spinner<Double> spinner) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?\\d*([.,]\\d*)?")) {
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
                    // Ignora
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
        if (KeyCombination.keyCombination("CTRL+C").match(event)) {
            handleCopyShape(new ActionEvent());
            event.consume();
        }
    }

    private void initializeShapeSelection(ShapeFactory factory, boolean disableFillPicker, boolean disableBorderPicker) {
        currentShape = null;
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

    public boolean isTooClose(AbstractShape newShape, double worldX, double worldY) {
        // worldX, worldY sono le coordinate del mondo del punto di inserimento
        if (newShape == null || drawingCanvas == null || zoomHandler == null) return true;

        double shapeWidth = newShape.getWidth();
        double shapeHeight = newShape.getHeight();

        double worldCanvasWidth = drawingCanvas.getWidth() / zoomHandler.getZoomFactor();
        double worldCanvasHeight = drawingCanvas.getHeight() / zoomHandler.getZoomFactor();

        // Verifica se la figura, posizionata in worldX, worldY, uscirebbe dai limiti del mondo del canvas
        return worldX + shapeWidth > worldCanvasWidth ||
                worldY + shapeHeight > worldCanvasHeight ||
                worldX < 0 || worldY < 0; // Aggiunto controllo per coordinate negative
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
        boolean enableCopy = false;

        if (shape != null) {
            AbstractShape baseShape = getBaseShape(shape);
            enableWidth = true;
            enableDelete = true;
            enableCopy = true;  //abilita copia se é una forma selezionata
            enableBorder = true;

            if (!(baseShape instanceof Line)) {
                enableHeight = true;
                enableFill = true;
            } else {
                enableHeight = false;
                enableFill = false;
            }
        }

        if (widthSpinner != null) widthSpinner.setDisable(!enableWidth);
        if (heightSpinner != null) heightSpinner.setDisable(!enableHeight);
        if (fillPicker != null) fillPicker.setDisable(!enableFill);
        if (borderPicker != null) borderPicker.setDisable(!enableBorder);
        if (deleteButton != null) deleteButton.setDisable(!enableDelete);
        if (copyButton != null) copyButton.setDisable(!enableCopy);
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

    @FXML
    public void handleCopyShape(ActionEvent event) {
        if (currentShape != null && commandManager != null && clipboardManager != null) {
            CopyShapeCommand copyCmd = new CopyShapeCommand(currentShape, clipboardManager);
            commandManager.executeCommand(copyCmd);
            System.out.println("DEBUG: Figura copiata nella clipboard interna.");
        }
    }

    public AbstractShape selectShapeAt(double worldX, double worldY) {
        if (model == null) return null;
        double thresholdInWorld = AbstractMouseHandler.SELECTION_THRESHOLD;
        if (zoomHandler != null && zoomHandler.getZoomFactor() != 0) {
        }


        for (AbstractShape shape : model.getShapesOrderedByZ()) {
            if (shape.containsPoint(worldX, worldY, thresholdInWorld)) {
                currentShape = shape;
                updateSpinners(currentShape);
                updateControlState(currentShape);
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
            heightSpinner.setDisable(true); // Disabilita se nessuna forma è selezionata
            widthSpinner.setDisable(true);  // Disabilita se nessuna forma è selezionata
        }
    }

    public void showContextMenu(MouseEvent event) {
        if (shapeMenu != null) {
            shapeMenu.show(drawingCanvas, event.getScreenX(), event.getScreenY());
        }
    }

    public void redrawCanvas() {
        if (gc == null || drawingCanvas == null || model == null || zoomHandler == null) {
            return;
        }
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        gc.save();
        zoomHandler.applyZoomTransformation(gc); // Applica lo zoom
        for (AbstractShape shape : model.getShapes()) {
            if (shape != null) {
                shape.draw(gc);
                if (shape == currentShape) {
                    drawHighlightBorder(shape);
                }
            }
        }
        gc.restore();
    }

    private void drawHandle(double worldX, double worldY) {
        gc.setFill(Color.SKYBLUE);
        gc.fillOval(worldX - HANDLE_RADIUS, worldY - HANDLE_RADIUS, HANDLE_RADIUS * 2, HANDLE_RADIUS * 2);
    }

    private void drawHighlightBorder(AbstractShape shape) {
        AbstractShape baseShape = getBaseShape(shape);
        gc.setStroke(Color.SKYBLUE);
        gc.setLineWidth(1.0 / zoomHandler.getZoomFactor()); // Per mantenere lo spessore della linea costante su schermo
        gc.setLineDashes(5 / zoomHandler.getZoomFactor());

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
        gc.setLineWidth(1.0); // Ripristina lo spessore linea
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

    // Metodi per i pulsanti di zoom
    @FXML private void handleZoom25() { if (zoomHandler != null) zoomHandler.setZoom25(); }
    @FXML private void handleZoom50() { if (zoomHandler != null) zoomHandler.setZoom50(); }
    @FXML private void handleZoom75() { if (zoomHandler != null) zoomHandler.setZoom75(); }
    @FXML private void handleZoom100() { if (zoomHandler != null) zoomHandler.setZoom100(); }
    @FXML private void handleZoom150() { if (zoomHandler != null) zoomHandler.setZoom150(); }


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
    public ZoomHandler getZoomHandler() { return zoomHandler; } // <-- NUOVO GETTER
}