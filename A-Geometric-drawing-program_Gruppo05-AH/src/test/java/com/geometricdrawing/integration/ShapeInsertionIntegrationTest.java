package com.geometricdrawing.integration;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.command.AddShapeCommand;
import com.geometricdrawing.command.Command;
import com.geometricdrawing.command.CommandManager;
import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.decorator.ShapeDecorator;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.model.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ShapeInsertionIntegrationTest {

    private static volatile boolean fxInitialized = false;

    private DrawingController controller;
    private DrawingModel model;
    private CommandManager commandManager;

    // Componenti UI reali
    private Canvas drawingCanvas;
    private Pane canvasContainer;
    private AnchorPane rootPane;
    private ColorPicker fillColorPicker;
    private ColorPicker borderColorPicker;
    private Spinner<Double> heightSpinner;
    private Spinner<Double> widthSpinner;
    private Button deleteButton;

    private static final double CANVAS_WIDTH_FOR_TEST = 800;
    private static final double CANVAS_HEIGHT_FOR_TEST = 600;

    @BeforeAll
    public static void initFX() throws InterruptedException {
        if (fxInitialized) {
            return;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(() -> {
            fxInitialized = true;
            latch.countDown();
        });
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new InterruptedException("Timeout: JavaFX Toolkit non inizializzato.");
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
                canvasContainer = new Pane(drawingCanvas);
                canvasContainer.setPrefSize(CANVAS_WIDTH_FOR_TEST, CANVAS_HEIGHT_FOR_TEST);

                rootPane = new AnchorPane(canvasContainer);
                AnchorPane.setTopAnchor(canvasContainer, 0.0);
                AnchorPane.setBottomAnchor(canvasContainer, 0.0);
                AnchorPane.setLeftAnchor(canvasContainer, 0.0);
                AnchorPane.setRightAnchor(canvasContainer, 0.0);

                new Scene(rootPane, CANVAS_WIDTH_FOR_TEST, CANVAS_HEIGHT_FOR_TEST);
                rootPane.applyCss();
                rootPane.layout();

                fillColorPicker = new ColorPicker(Color.LIGHTGREEN);
                borderColorPicker = new ColorPicker(Color.ORANGE);

                heightSpinner = new Spinner<>();
                SpinnerValueFactory<Double> heightFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 40.0, 1.0);
                heightSpinner.setValueFactory(heightFactory);
                heightSpinner.setEditable(true);

                widthSpinner = new Spinner<>();
                SpinnerValueFactory<Double> widthFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 60.0, 1.0);
                widthSpinner.setValueFactory(widthFactory);
                widthSpinner.setEditable(true);

                deleteButton = new Button("Elimina");

                setPrivateField(controller, "drawingCanvas", drawingCanvas);
                setPrivateField(controller, "canvasContainer", canvasContainer);
                setPrivateField(controller, "rootPane", rootPane);
                setPrivateField(controller, "fillPicker", fillColorPicker);
                setPrivateField(controller, "borderPicker", borderColorPicker);
                setPrivateField(controller, "heightSpinner", heightSpinner);
                setPrivateField(controller, "widthSpinner", widthSpinner);
                setPrivateField(controller, "deleteButton", deleteButton);

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
            }
            MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, x, y, x, y, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, true, false, false, null);
            controller.handleCanvasClick(clickEvent);
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
        // La forma è già stata aggiunta al modello e currentShape è impostato nel controller

        assertEquals(1, model.getShapes().size(), "Il modello dovrebbe contenere una forma.");
        // decoratedShape è currentShape dal controller, che è anche nell'elenco del modello.
        assertSame(decoratedShape, model.getShapes().get(0), "La forma nel controller e nel modello non corrispondono.");


        assertTrue(decoratedShape instanceof BorderColorDecorator, "La forma dovrebbe essere decorata con BorderColorDecorator.");
        AbstractShape shapeAfterBorder = ((ShapeDecorator) decoratedShape).getInnerShape();
        assertTrue(shapeAfterBorder instanceof FillColorDecorator, "Dopo BorderColor, ci si aspetta FillColorDecorator.");
        AbstractShape baseShape = ((ShapeDecorator) shapeAfterBorder).getInnerShape();

        assertTrue(baseShape instanceof Rectangle, "La forma base aggiunta dovrebbe essere un Rettangolo.");
        assertEquals(clickX, baseShape.getX(), "La coordinata X non corrisponde.");
        assertEquals(clickY, baseShape.getY(), "La coordinata Y non corrisponde.");
        assertEquals(ShapeFactory.DEFAULT_WIDTH, baseShape.getWidth(), "La larghezza non corrisponde.");
        assertEquals(ShapeFactory.DEFAULT_HEIGHT, baseShape.getHeight(), "L'altezza non corrisponde.");
        assertEquals(0, baseShape.getZ(), "Lo Z-order dovrebbe essere 0 per la prima forma.");

        Stack<Command> undoStack = getUndoStack(commandManager);
        assertEquals(3, undoStack.size(), "Dovrebbero esserci 3 comandi nello stack undo: AddShape, ChangeWidth, ChangeHeight.");
        assertTrue(undoStack.get(0) instanceof AddShapeCommand);
        assertTrue(undoStack.get(1) instanceof com.geometricdrawing.command.ChangeWidthCommand);
        assertTrue(undoStack.get(2) instanceof com.geometricdrawing.command.ChangeHeightCommand);

        assertNull(getPrivateField(controller, "currentShapeFactory"), "currentShapeFactory dovrebbe essere resettata.");
        assertEquals(decoratedShape, getPrivateField(controller, "currentShape"));

        assertFalse(widthSpinner.isDisabled(), "Width spinner dovrebbe essere abilitato.");
        assertFalse(heightSpinner.isDisabled(), "Height spinner dovrebbe essere abilitato.");
        assertFalse(deleteButton.isDisabled(), "Delete button dovrebbe essere abilitato.");

        // Secondo il nuovo requisito, i picker sono disabilitati dopo la selezione
        assertTrue(fillColorPicker.isDisabled(),"Fill picker dovrebbe essere DISABILITATO (logica 'MOMENTANEE').");
        assertTrue(borderColorPicker.isDisabled(),"Border picker dovrebbe essere DISABILITATO (logica 'MOMENTANEE').");

        assertEquals(baseShape.getWidth(), widthSpinner.getValueFactory().getValue());
        assertEquals(baseShape.getHeight(), heightSpinner.getValueFactory().getValue());
    }

    @Test
    @DisplayName("Inserimento Ellisse con colori personalizzati")
    void testInsertEllipseWithCustomColors() throws Exception {
        final Color customFill = Color.RED; // Questi colori vengono usati per creare la forma
        final Color customBorder = Color.BLUE; // ma i picker saranno disabilitati dopo
        final double clickX = 50.0;
        final double clickY = 75.0;

        runOnFxThreadAndWait(() -> { // Imposta i valori dei picker PRIMA della selezione tipo e del click
            fillColorPicker.setValue(customFill);
            borderColorPicker.setValue(customBorder);
        });

        AbstractShape decoratedShape = insertAndGetSelectedShapeFromController("Ellipse", clickX, clickY);

        assertEquals(1, model.getShapes().size());
        assertTrue(decoratedShape instanceof BorderColorDecorator);
        AbstractShape shapeAfterBorder = ((ShapeDecorator) decoratedShape).getInnerShape();
        assertTrue(shapeAfterBorder instanceof FillColorDecorator);
        AbstractShape baseShape = ((ShapeDecorator) shapeAfterBorder).getInnerShape();

        assertTrue(baseShape instanceof Ellipse);
        assertEquals(clickX, baseShape.getX());
        assertEquals(clickY, baseShape.getY());
        assertEquals(ShapeFactory.DEFAULT_WIDTH, baseShape.getWidth());
        assertEquals(ShapeFactory.DEFAULT_HEIGHT, baseShape.getHeight());
        assertEquals(decoratedShape, getPrivateField(controller, "currentShape"));

        // I picker sono disabilitati dopo la selezione, anche se i loro valori sono stati usati per la creazione
        assertTrue(fillColorPicker.isDisabled(),"Fill picker dovrebbe essere DISABILITATO (logica 'MOMENTANEE').");
        assertTrue(borderColorPicker.isDisabled(),"Border picker dovrebbe essere DISABILITATO (logica 'MOMENTANEE').");
    }

    @Test
    @DisplayName("Inserimento Linea disabilita FillPicker, HeightSpinner e i ColorPicker")
    void testInsertLineDisablesControls() throws Exception {
        final double clickX = 200.0;
        final double clickY = 250.0;

        // handleSelectLinea abilita borderPicker e disabilita fillPicker (tramite updateControlState(null) e poi logica specifica)
        runOnFxThreadAndWait(() -> controller.handleSelectLinea(new ActionEvent()));

        // Verifica stato UI *dopo* la selezione del tipo Linea, ma *prima* dell'inserimento
        assertTrue(fillColorPicker.isDisabled(), "Fill picker dovrebbe essere disabilitato dopo aver selezionato Linea.");
        assertFalse(borderColorPicker.isDisabled(), "Border picker dovrebbe essere abilitato dopo aver selezionato Linea (prima del click).");

        AbstractShape decoratedShape = insertAndGetSelectedShapeFromController("Line", clickX, clickY);

        assertEquals(1, model.getShapes().size());
        assertTrue(decoratedShape instanceof BorderColorDecorator);
        AbstractShape baseShape = ((ShapeDecorator) decoratedShape).getInnerShape();
        assertFalse(baseShape instanceof FillColorDecorator); // Una linea non ha riempimento

        assertTrue(baseShape instanceof Line);
        assertEquals(clickX, baseShape.getX());
        assertEquals(clickY, baseShape.getY());
        assertEquals(ShapeFactory.DEFAULT_LINE_LENGTH, baseShape.getWidth());
        assertEquals(1.0, baseShape.getHeight()); // Altezza impostata a 1.0 dal ChangeHeightCommand iniziale

        assertEquals(decoratedShape, getPrivateField(controller, "currentShape"));

        // Stato UI dopo l'inserimento della linea:
        assertFalse(widthSpinner.isDisabled());
        assertTrue(heightSpinner.isDisabled()); // Specifico per la linea
        assertTrue(fillColorPicker.isDisabled()); // Specifico per la linea E per "MOMENTANEE"
        assertTrue(borderColorPicker.isDisabled());// Per "MOMENTANEE"
        assertFalse(deleteButton.isDisabled());
        assertEquals(ShapeFactory.DEFAULT_LINE_LENGTH, widthSpinner.getValueFactory().getValue());
    }

    @Test
    @DisplayName("Z-order per inserimenti consecutivi")
    void testZOrderOnConsecutiveInsertions() throws Exception {
        AbstractShape shape1Decorated = insertAndGetSelectedShapeFromController("Rectangle", 10, 10);
        AbstractShape shape1Base = ((ShapeDecorator)((ShapeDecorator)shape1Decorated).getInnerShape()).getInnerShape();
        assertEquals(0, shape1Base.getZ());

        AbstractShape shape2Decorated = insertAndGetSelectedShapeFromController("Line", 20, 20);
        assertEquals(2, model.getShapes().size()); // Verifica che la seconda forma sia stata aggiunta
        AbstractShape shape2Base = ((ShapeDecorator)shape2Decorated).getInnerShape();
        assertEquals(1, shape2Base.getZ());

        AbstractShape shape3Decorated = insertAndGetSelectedShapeFromController("Ellipse", 30, 30);
        assertEquals(3, model.getShapes().size()); // Verifica che la terza forma sia stata aggiunta
        AbstractShape shape3Base = ((ShapeDecorator)((ShapeDecorator)shape3Decorated).getInnerShape()).getInnerShape();
        assertEquals(2, shape3Base.getZ());
    }
}