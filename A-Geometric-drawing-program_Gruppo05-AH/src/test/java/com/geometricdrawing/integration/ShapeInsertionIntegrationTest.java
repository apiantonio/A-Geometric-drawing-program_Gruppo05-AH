package com.geometricdrawing.integration;

import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.command.AddShapeCommand;
import com.geometricdrawing.command.Command;
import com.geometricdrawing.command.CommandManager;
import com.geometricdrawing.command.ChangeWidthCommand; // Keep for stream().anyMatch if needed, but not for stack size
import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.decorator.ShapeDecorator;
import com.geometricdrawing.factory.PolygonFactory;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.model.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test di Integrazione per Inserimento Figure")
public class ShapeInsertionIntegrationTest {

    private static volatile boolean fxInitialized = false;

    private DrawingController controller;
    private DrawingModel model;
    private CommandManager commandManager;

    private Canvas drawingCanvas;
    private AnchorPane canvasContainer;
    private AnchorPane rootPane;
    private ColorPicker fillColorPicker;
    private ColorPicker borderColorPicker;
    private Spinner<Double> heightSpinner;
    private Spinner<Double> widthSpinner;
    private Button deleteButton;
    private Button copyButton;

    private static final double CANVAS_WIDTH_FOR_TEST = 800;
    private static final double CANVAS_HEIGHT_FOR_TEST = 600;

    @BeforeAll
    public static void initFX() throws InterruptedException {
        if (Platform.isFxApplicationThread()) {
            fxInitialized = true;
            return;
        }

        if (fxInitialized) {
            return;
        }

        try {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(() -> {
                fxInitialized = true;
                latch.countDown();
            });
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new InterruptedException("Timeout: JavaFX Toolkit non inizializzato.");
            }
        } catch (IllegalStateException e) {
            // Se il toolkit è già inizializzato imposto il flag
            fxInitialized = true;
        }

    }

    @AfterAll
    public static void cleanupFX() {
        // Platform.exit();
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        final CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                model = new DrawingModel();
                commandManager = new CommandManager();
                controller = new DrawingController();

                drawingCanvas = new Canvas();
                canvasContainer = new AnchorPane(drawingCanvas);
                canvasContainer.setPrefSize(CANVAS_WIDTH_FOR_TEST, CANVAS_HEIGHT_FOR_TEST);

                rootPane = new AnchorPane(canvasContainer);
                AnchorPane.setTopAnchor(canvasContainer, 0.0);
                AnchorPane.setBottomAnchor(canvasContainer, 0.0);
                AnchorPane.setLeftAnchor(canvasContainer, 0.0);
                AnchorPane.setRightAnchor(canvasContainer, 0.0);

                new Scene(rootPane, CANVAS_WIDTH_FOR_TEST, CANVAS_HEIGHT_FOR_TEST);
                rootPane.applyCss();
                rootPane.layout();

                // Componenti UI principali
                fillColorPicker = new ColorPicker(Color.LIGHTGREEN);
                borderColorPicker = new ColorPicker(Color.ORANGE);

                heightSpinner = new Spinner<>();
                SpinnerValueFactory<Double> heightFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        1.0, 1000.0, ShapeFactory.DEFAULT_HEIGHT, 1.0);
                heightSpinner.setValueFactory(heightFactory);
                heightSpinner.setEditable(true);

                widthSpinner = new Spinner<>();
                SpinnerValueFactory<Double> widthFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        1.0, 1000.0, ShapeFactory.DEFAULT_WIDTH, 1.0);
                widthSpinner.setValueFactory(widthFactory);
                widthSpinner.setEditable(true);

                deleteButton = new Button("Elimina");
                copyButton = new Button("Copia");

                // ScrollBars
                ScrollBar horizontalScrollBar = new ScrollBar();
                horizontalScrollBar.setOrientation(Orientation.HORIZONTAL);
                horizontalScrollBar.setMin(0.0);
                horizontalScrollBar.setMax(100.0);
                horizontalScrollBar.setValue(0.0);

                ScrollBar verticalScrollBar = new ScrollBar();
                verticalScrollBar.setOrientation(Orientation.VERTICAL);
                verticalScrollBar.setMin(0.0);
                verticalScrollBar.setMax(100.0);
                verticalScrollBar.setValue(0.0);

                // Altri componenti necessari per initialize()
                Button pasteButton = new Button("Incolla");
                Button undoButton = new Button("Annulla");
                Button cutButton = new Button("Taglia");
                Button foregroundButton = new Button("In primo piano");
                Button backgroundButton = new Button("In secondo piano");

                Label cutCopyLabel = new Label();
                Label emptyClipboardLabel = new Label();

                CheckMenuItem toggleGrid = new CheckMenuItem("Mostra griglia");
                MenuButton gridOptions = new MenuButton("Griglia");

                // Spinner per rotazione
                Spinner<Double> rotationSpinner = new Spinner<>();
                SpinnerValueFactory<Double> rotationFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        -360.0, 360.0, 0.0, 1.0);
                rotationSpinner.setValueFactory(rotationFactory);

                Spinner<Integer> fontSizeSpinner = new Spinner<>();
                SpinnerValueFactory<Integer> fontSizeFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        8, 72, 12, 1);
                fontSizeSpinner.setValueFactory(fontSizeFactory);

                TextField textField = new TextField();

                MenuItem mirrorHorizontal = new MenuItem("Specchia Orizzontalmente");
                MenuItem mirrorVertical = new MenuItem("Specchia Verticalmente");
                MenuButton mirrorMenu = new MenuButton("Specchia");

                MenuButton shapeMenuButton = new MenuButton("Seleziona Forma");
                Button textButton = new Button("Testo");

                // Imposta tutti i campi privati del controller
                setPrivateField(controller, "drawingCanvas", drawingCanvas);
                setPrivateField(controller, "canvasContainer", canvasContainer);
                setPrivateField(controller, "rootPane", rootPane);
                setPrivateField(controller, "fillPicker", fillColorPicker);
                setPrivateField(controller, "borderPicker", borderColorPicker);
                setPrivateField(controller, "heightSpinner", heightSpinner);
                setPrivateField(controller, "widthSpinner", widthSpinner);
                setPrivateField(controller, "deleteButton", deleteButton);
                setPrivateField(controller, "copyButton", copyButton);
                setPrivateField(controller, "horizontalScrollBar", horizontalScrollBar);
                setPrivateField(controller, "verticalScrollBar", verticalScrollBar);
                setPrivateField(controller, "pasteButton", pasteButton);
                setPrivateField(controller, "undoButton", undoButton);
                setPrivateField(controller, "cutButton", cutButton);
                setPrivateField(controller, "foregroundButton", foregroundButton);
                setPrivateField(controller, "backgroundButton", backgroundButton);
                setPrivateField(controller, "cutCopyLabel", cutCopyLabel);
                setPrivateField(controller, "emptyClipboardLabel", emptyClipboardLabel);
                setPrivateField(controller, "toggleGrid", toggleGrid);
                setPrivateField(controller, "gridOptions", gridOptions);
                setPrivateField(controller, "rotationSpinner", rotationSpinner);
                setPrivateField(controller, "fontSizeSpinner", fontSizeSpinner);
                setPrivateField(controller, "textField", textField);
                setPrivateField(controller, "mirrorHorizontal", mirrorHorizontal);
                setPrivateField(controller, "mirrorVertical", mirrorVertical);
                setPrivateField(controller, "mirrorMenu", mirrorMenu);
                setPrivateField(controller, "shapeMenuButton", shapeMenuButton);
                setPrivateField(controller, "textButton", textButton);

                controller.setModel(model);
                controller.setCommandManager(commandManager);
                controller.initialize();

            } catch (Exception e) {
                e.printStackTrace();
                fail("Setup fallito sul thread JavaFX: " + e.getMessage());
            } finally {
                setupLatch.countDown();
            }
        });

        if (!setupLatch.await(10, TimeUnit.SECONDS)) {
            throw new InterruptedException("Timeout durante l'attesa del setup sul thread JavaFX.");
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object getPrivateField(Object target, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
    private Object getPrivateFieldNonFailing(Object target, String fieldName) {
        try {
            return getPrivateField(target, fieldName);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Errore riflessione in getPrivateFieldNonFailing per il campo " + fieldName + ": " + e.getMessage());
            return "ERRORE_RIFLESSIONE";
        }
    }

    @SuppressWarnings("unchecked")
    private Stack<Command> getUndoStack(CommandManager cm)
            throws NoSuchFieldException, IllegalAccessException {
        Field stackField = CommandManager.class.getDeclaredField("undoStack");
        stackField.setAccessible(true);
        return (Stack<Command>) stackField.get(cm);
    }

    private void runOnFxThreadAndWait(Runnable action) throws InterruptedException {
        final CountDownLatch actionLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                System.err.println("Eccezione imprevista sul thread JavaFX durante action.run():");
                t.printStackTrace();
            }
            finally {
                actionLatch.countDown();
            }
        });
        if (!actionLatch.await(5, TimeUnit.SECONDS)) {
            throw new InterruptedException("Timeout durante l'esecuzione dell'azione sul thread JavaFX.");
        }
    }
    private AbstractShape insertAndGetSelectedShapeFromController(String shapeType, double x, double y) throws Exception {
        AtomicReference<AbstractShape> currentShapeRef = new AtomicReference<>(null);

        runOnFxThreadAndWait(() -> {
            if ("Rectangle".equalsIgnoreCase(shapeType)) {
                controller.handleSelectRettangolo(new ActionEvent());
            } else if ("Ellipse".equalsIgnoreCase(shapeType)) {
                controller.handleSelectEllisse(new ActionEvent());
            } else if ("Line".equalsIgnoreCase(shapeType)) {
                controller.handleSelectLinea(new ActionEvent());
            } else if ("Polygon".equalsIgnoreCase(shapeType)) {
                controller.handleSelectPoligono(new ActionEvent());
            } else {
                throw new IllegalArgumentException("Tipo di forma non supportato: " + shapeType);
            }

            if ("Polygon".equalsIgnoreCase(shapeType)) {
                // Per i poligoni, simula multipli click
                PolygonFactory factory = (PolygonFactory) controller.getCurrentShapeFactory();
                int maxPoints = factory.getMaxPoints();

                // Simula i click necessari per completare il poligono
                for (int i = 0; i < maxPoints; i++) {
                    // Piccole variazioni per creare vertici distinti
                    double clickX = x + (i * 10); // Offset orizzontale
                    double clickY = y + (i * 10); // Offset verticale

                    MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED,
                            clickX, clickY, clickX, clickY, MouseButton.PRIMARY, 1,
                            false, false, false, false, true, false, false, true, false, false, null);
                    controller.getDrawingCanvas().fireEvent(clickEvent);
                }
            } else {
                // Per le altre forme, un singolo click
                MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED,
                        x, y, x, y, MouseButton.PRIMARY, 1,
                        false, false, false, false, true, false, false, true, false, false, null);
                controller.getDrawingCanvas().fireEvent(clickEvent);
            }

            currentShapeRef.set((AbstractShape) getPrivateFieldNonFailing(controller, "currentShape"));
        });

        assertNotNull(currentShapeRef.get(), "Nessuna forma corrente selezionata dopo l'inserimento (letto da controller).");
        return currentShapeRef.get();
    }


    @Test
    @DisplayName("Inserimento Rettangolo con proprietà di default")
    void testInsertRectangleWithDefaultProperties() throws Exception {
        final double clickX = 100.0;
        final double clickY = 150.0;

        AbstractShape decoratedShape = insertAndGetSelectedShapeFromController("Rectangle", clickX, clickY);

        assertEquals(1, model.getShapes().size(), "Il model dovrebbe contenere una forma.");
        assertSame(decoratedShape, model.getShapes().get(0), "La forma nel controller e nel model non corrispondono.");

        assertTrue(decoratedShape instanceof BorderColorDecorator, "La forma dovrebbe essere decorata con BorderColorDecorator.");
        AbstractShape shapeAfterBorder = ((ShapeDecorator) decoratedShape).getInnerShape();
        assertTrue(shapeAfterBorder instanceof FillColorDecorator, "Dopo BorderColor, ci si aspetta FillColorDecorator.");
        AbstractShape baseShape = getBaseShape(decoratedShape);

        assertTrue(baseShape instanceof Rectangle, "La forma base aggiunta dovrebbe essere un Rettangolo.");
        assertEquals(clickX, baseShape.getX(), "La coordinata X non corrisponde.");
        assertEquals(clickY, baseShape.getY(), "La coordinata Y non corrisponde.");
        assertEquals(ShapeFactory.DEFAULT_WIDTH, baseShape.getWidth(), "La larghezza non corrisponde.");
        assertEquals(ShapeFactory.DEFAULT_HEIGHT, baseShape.getHeight(), "L'altezza non corrisponde.");
        assertEquals(0, baseShape.getZ(), "Lo Z-order dovrebbe essere 0 per la prima forma.");

        Stack<Command> undoStack = getUndoStack(commandManager);
        assertEquals(1, undoStack.size(), "Solo AddShapeCommand dovrebbe essere nello stack undo perché le dimensioni di default della forma corrispondono a quelle iniziali degli spinner.");
        assertTrue(undoStack.get(0) instanceof AddShapeCommand, "Il comando dovrebbe essere AddShapeCommand.");


        assertNull(getPrivateField(controller, "currentShapeFactory"), "currentShapeFactory dovrebbe essere resettata dopo l'inserimento.");
        assertEquals(decoratedShape, getPrivateField(controller, "currentShape"), "La forma corrente nel controller non è corretta.");

        assertFalse(widthSpinner.isDisabled(), "Width spinner dovrebbe essere abilitato.");
        assertFalse(heightSpinner.isDisabled(), "Height spinner dovrebbe essere abilitato.");
        assertFalse(deleteButton.isDisabled(), "Delete button dovrebbe essere abilitato.");
        assertFalse(copyButton.isDisabled(), "Copy button dovrebbe essere abilitato.");

        assertFalse(fillColorPicker.isDisabled(),"Fill picker dovrebbe essere ABILITATO quando un Rettangolo è selezionato.");
        assertFalse(borderColorPicker.isDisabled(),"Border picker dovrebbe essere ABILITATO quando un Rettangolo è selezionato.");

        assertEquals(baseShape.getWidth(), widthSpinner.getValueFactory().getValue(), "Spinner larghezza non aggiornato.");
        assertEquals(baseShape.getHeight(), heightSpinner.getValueFactory().getValue(), "Spinner altezza non aggiornato.");
    }

    @Test
    @DisplayName("Inserimento Ellisse con colori personalizzati")
    void testInsertEllipseWithCustomColors() throws Exception {
        final Color customFill = Color.RED;
        final Color customBorder = Color.BLUE;
        final double clickX = 50.0;
        final double clickY = 75.0;

        runOnFxThreadAndWait(() -> {
            fillColorPicker.setValue(customFill);
            borderColorPicker.setValue(customBorder);
        });

        AbstractShape decoratedShape = insertAndGetSelectedShapeFromController("Ellipse", clickX, clickY);

        assertEquals(1, model.getShapes().size());
        assertTrue(decoratedShape instanceof BorderColorDecorator);
        AbstractShape shapeAfterBorder = ((ShapeDecorator) decoratedShape).getInnerShape();
        assertTrue(shapeAfterBorder instanceof FillColorDecorator);
        AbstractShape baseShape = getBaseShape(decoratedShape);

        assertTrue(baseShape instanceof Ellipse);
        assertEquals(clickX, baseShape.getX());
        assertEquals(clickY, baseShape.getY());
        assertEquals(ShapeFactory.DEFAULT_WIDTH, baseShape.getWidth());
        assertEquals(ShapeFactory.DEFAULT_HEIGHT, baseShape.getHeight());
        assertEquals(decoratedShape, getPrivateField(controller, "currentShape"));

        BorderColorDecorator bcd = (BorderColorDecorator) decoratedShape;
        assertEquals(customBorder, bcd.getBorderColor());

        FillColorDecorator fcd = (FillColorDecorator) shapeAfterBorder;
        assertEquals(customFill, fcd.getFillColor());

        assertFalse(fillColorPicker.isDisabled(),"Fill picker dovrebbe essere ABILITATO quando un'Ellisse è selezionata.");
        assertFalse(borderColorPicker.isDisabled(),"Border picker dovrebbe essere ABILITATO quando un'Ellisse è selezionata.");

        Stack<Command> undoStack = getUndoStack(commandManager);
        assertEquals(1, undoStack.size(), "Solo AddShapeCommand dovrebbe essere nello stack undo per l'ellisse se gli spinner sono inizializzati con i default.");
        assertTrue(undoStack.get(0) instanceof AddShapeCommand, "Il comando dovrebbe essere AddShapeCommand per l'ellisse.");
    }

    @Test
    @DisplayName("Inserimento Linea disabilita FillPicker e HeightSpinner, e lo stato dei ColorPicker")
    void testInsertLineDisablesControlsAndAppliesBorder() throws Exception {
        final double clickX = 200.0;
        final double clickY = 250.0;
        final Color lineBorderColor = borderColorPicker.getValue();

        runOnFxThreadAndWait(() -> controller.handleSelectLinea(new ActionEvent()));

        assertTrue(fillColorPicker.isDisabled(), "Fill picker dovrebbe essere disabilitato DOPO aver selezionato Linea (prima del click).");
        assertFalse(borderColorPicker.isDisabled(), "Border picker dovrebbe essere abilitato DOPO aver selezionato Linea (prima del click).");

        AbstractShape decoratedShape = insertAndGetSelectedShapeFromController("Line", clickX, clickY);

        assertEquals(1, model.getShapes().size());
        assertTrue(decoratedShape instanceof BorderColorDecorator, "La Linea dovrebbe essere decorata con BorderColorDecorator.");
        BorderColorDecorator bcd = (BorderColorDecorator) decoratedShape;
        assertEquals(lineBorderColor, bcd.getBorderColor(), "Colore bordo linea non corretto.");

        AbstractShape baseShape = getBaseShape(decoratedShape);
        assertFalse(baseShape instanceof FillColorDecorator, "La Linea base non dovrebbe avere FillColorDecorator.");

        assertTrue(baseShape instanceof Line, "La forma base dovrebbe essere una Linea.");
        assertEquals(clickX, baseShape.getX(), "X iniziale linea errato.");
        assertEquals(clickY, baseShape.getY(), "Y iniziale linea errato.");
        assertEquals(ShapeFactory.DEFAULT_LINE_LENGTH, baseShape.getWidth(), "Larghezza (lunghezza) linea errata.");
        assertEquals(0.0, baseShape.getHeight(), 0.001, "L'altezza di una linea orizzontale di default dovrebbe essere 0.0.");

        assertEquals(decoratedShape, getPrivateField(controller, "currentShape"), "Forma corrente nel controller errata.");

        assertFalse(widthSpinner.isDisabled(), "Spinner larghezza abilitato per Linea.");
        assertTrue(heightSpinner.isDisabled(), "Spinner altezza DISABILITATO per Linea.");
        assertTrue(fillColorPicker.isDisabled(), "Fill picker DISABILITATO per Linea selezionata.");
        assertFalse(borderColorPicker.isDisabled(), "Border picker ABILITATO per Linea selezionata.");
        assertFalse(deleteButton.isDisabled(), "Delete button abilitato per Linea.");

        assertEquals(ShapeFactory.DEFAULT_LINE_LENGTH, widthSpinner.getValueFactory().getValue(), "Spinner larghezza per Linea errato.");
        assertEquals(1.0, heightSpinner.getValueFactory().getValue(), 0.001, "Spinner altezza per Linea dovrebbe mostrare 1.0.");


        Stack<Command> undoStack = getUndoStack(commandManager);
        // CORREZIONE: Solo AddShapeCommand è atteso, poiché updateSpinners non genera un ChangeWidthCommand
        // se la larghezza della linea corrisponde già al valore che lo spinner sta per assumere.
        assertEquals(1, undoStack.size(), "Solo AddShapeCommand atteso per la Linea.");
        assertTrue(undoStack.stream().anyMatch(cmd -> cmd instanceof AddShapeCommand), "AddShapeCommand mancante per la linea.");
        // Non ci aspettiamo più un ChangeWidthCommand qui in automatico.
        assertFalse(undoStack.stream().anyMatch(cmd -> cmd instanceof ChangeWidthCommand), "ChangeWidthCommand NON dovrebbe essere presente dopo l'inserimento della Linea in questo scenario.");
    }
    @Test
    @DisplayName("Inserimento Poligono con proprietà di default")
    void testInsertPolygonWithDefaultProperties() throws Exception {
        final double clickX = 150.0;
        final double clickY = 200.0;

        AbstractShape decoratedShape = insertAndGetSelectedShapeFromController("Polygon", clickX, clickY);

        assertEquals(1, model.getShapes().size(), "Il model dovrebbe contenere una forma.");
        assertSame(decoratedShape, model.getShapes().get(0), "La forma nel controller e nel model non corrispondono.");

        assertTrue(decoratedShape instanceof BorderColorDecorator, "La forma dovrebbe essere decorata con BorderColorDecorator.");
        AbstractShape shapeAfterBorder = ((ShapeDecorator) decoratedShape).getInnerShape();
        assertTrue(shapeAfterBorder instanceof FillColorDecorator, "Dopo BorderColor, ci si aspetta FillColorDecorator.");
        AbstractShape baseShape = getBaseShape(decoratedShape);

        assertTrue(baseShape instanceof Polygon, "La forma base aggiunta dovrebbe essere un Poligono.");
        assertEquals(clickX, baseShape.getX(), "La coordinata X non corrisponde.");
        assertEquals(clickY, baseShape.getY(), "La coordinata Y non corrisponde.");
        assertEquals(0, baseShape.getZ(), "Lo Z-order dovrebbe essere 0 per la prima forma.");

        Stack<Command> undoStack = getUndoStack(commandManager);
        assertEquals(1, undoStack.size(), "Solo AddShapeCommand dovrebbe essere nello stack undo.");
        assertTrue(undoStack.get(0) instanceof AddShapeCommand, "Il comando dovrebbe essere AddShapeCommand.");

        assertNull(getPrivateField(controller, "currentShapeFactory"), "currentShapeFactory dovrebbe essere resettata dopo l'inserimento.");
        assertEquals(decoratedShape, getPrivateField(controller, "currentShape"), "La forma corrente nel controller non è corretta.");

        assertFalse(widthSpinner.isDisabled(), "Width spinner dovrebbe essere abilitato.");
        assertFalse(heightSpinner.isDisabled(), "Height spinner dovrebbe essere abilitato.");
        assertFalse(deleteButton.isDisabled(), "Delete button dovrebbe essere abilitato.");
        assertFalse(copyButton.isDisabled(), "Copy button dovrebbe essere abilitato.");
        assertFalse(fillColorPicker.isDisabled(), "Fill picker dovrebbe essere ABILITATO quando un Poligono è selezionato.");
        assertFalse(borderColorPicker.isDisabled(), "Border picker dovrebbe essere ABILITATO quando un Poligono è selezionato.");

        assertEquals(baseShape.getWidth(), widthSpinner.getValueFactory().getValue(), "Spinner larghezza non aggiornato.");
        assertEquals(baseShape.getHeight(), heightSpinner.getValueFactory().getValue(), "Spinner altezza non aggiornato.");
    }

    @Test
    @DisplayName("Z-order per inserimenti consecutivi")
    void testZOrderOnConsecutiveInsertions() throws Exception {
        AbstractShape shape1Decorated = insertAndGetSelectedShapeFromController("Rectangle", 10, 10);
        AbstractShape shape1Base = getBaseShape(shape1Decorated);
        assertEquals(0, shape1Base.getZ(), "Z-order prima figura errato.");

        AbstractShape shape2Decorated = insertAndGetSelectedShapeFromController("Line", 20, 20);
        assertEquals(2, model.getShapes().size(), "Numero forme errato.");
        AbstractShape shape2Base = getBaseShape(shape2Decorated);
        assertEquals(1, shape2Base.getZ(), "Z-order seconda figura errato.");

        AbstractShape shape3Decorated = insertAndGetSelectedShapeFromController("Ellipse", 30, 30);
        assertEquals(3, model.getShapes().size(), "Numero forme errato.");
        AbstractShape shape3Base = getBaseShape(shape3Decorated);
        assertEquals(2, shape3Base.getZ(), "Z-order terza figura errato.");
    }

    private AbstractShape getBaseShape(AbstractShape decoratedShape) {
        AbstractShape current = decoratedShape;
        while (current instanceof ShapeDecorator) {
            current = ((ShapeDecorator) current).getInnerShape();
        }
        return current;
    }
}