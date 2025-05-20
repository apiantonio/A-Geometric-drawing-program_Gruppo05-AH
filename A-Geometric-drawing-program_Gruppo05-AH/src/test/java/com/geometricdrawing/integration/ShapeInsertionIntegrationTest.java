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
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        final CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                model = new DrawingModel();
                commandManager = new CommandManager();
                controller = new DrawingController();

                // Istanziazione dei componenti UI reali SUL THREAD JAVAFX
                drawingCanvas = new Canvas(800, 600);
                canvasContainer = new Pane(drawingCanvas);
                rootPane = new AnchorPane(); // Necessario per il controller.initialize()
                fillColorPicker = new ColorPicker(Color.LIGHTGREEN);
                borderColorPicker = new ColorPicker(Color.ORANGE);

                heightSpinner = new Spinner<>(); // Istanziazione base
                SpinnerValueFactory<Double> heightFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 40.0, 1.0);
                heightSpinner.setValueFactory(heightFactory);
                heightSpinner.setEditable(true); // Come nel controller

                widthSpinner = new Spinner<>(); // Istanziazione base
                SpinnerValueFactory<Double> widthFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 60.0, 1.0);
                widthSpinner.setValueFactory(widthFactory);
                widthSpinner.setEditable(true); // Come nel controller

                deleteButton = new Button("Elimina");

                // Iniezione dei campi FXML nel controller
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

                // Chiama initialize DOPO che tutti i campi e le dipendenze sono stati impostati
                controller.initialize();

            } catch (Exception e) { // Cattura eccezioni generiche per il logging
                e.printStackTrace();
                fail("Setup fallito sul thread JavaFX: " + e.getMessage());
            } finally {
                setupLatch.countDown();
            }
        });

        if (!setupLatch.await(10, TimeUnit.SECONDS)) { // Aumentato timeout per sicurezza
            throw new InterruptedException("Timeout durante l'attesa del setup sul thread JavaFX.");
        }
    }

    // Helper per impostare campi privati (usare con cautela)
    private void setPrivateField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // Helper per leggere campi privati (usare con cautela)
    private Object getPrivateField(Object target, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    // Helper per ottenere lo stack undo (richiede accesso al campo privato)
    @SuppressWarnings("unchecked")
    private Stack<Command> getUndoStack(CommandManager cm)
            throws NoSuchFieldException, IllegalAccessException {
        Field stackField = CommandManager.class.getDeclaredField("undoStack");
        stackField.setAccessible(true);
        return (Stack<Command>) stackField.get(cm);
    }

    // Helper per eseguire codice sul thread JavaFX e attendere
    private void runOnFxThreadAndWait(Runnable action) throws InterruptedException {
        final CountDownLatch actionLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                actionLatch.countDown();
            }
        });
        if (!actionLatch.await(5, TimeUnit.SECONDS)) {
            throw new InterruptedException("Timeout durante l'esecuzione dell'azione sul thread JavaFX.");
        }
    }


    @Test
    @DisplayName("Inserimento Rettangolo con proprietà di default")
    void testInsertRectangleWithDefaultProperties() throws Exception {
        final double clickX = 100.0;
        final double clickY = 150.0;

        runOnFxThreadAndWait(() -> {
            controller.handleSelectRettangolo(new ActionEvent());
            MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, clickX, clickY, clickX, clickY, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, true, false, false, null);
            // Assicurati che handleCanvasClick sia accessibile (non private) in DrawingController
            controller.handleCanvasClick(clickEvent);
        });

        assertEquals(1, model.getShapes().size(), "Il modello dovrebbe contenere una forma.");
        AbstractShape decoratedShape = model.getShapes().get(0);
        assertTrue(decoratedShape instanceof BorderColorDecorator, "La forma dovrebbe essere decorata con BorderColorDecorator.");
        AbstractShape shapeAfterBorder = ((ShapeDecorator) decoratedShape).getInnerShape();
        assertTrue(shapeAfterBorder instanceof FillColorDecorator, "Dopo BorderColor, ci si aspetta FillColorDecorator.");
        AbstractShape baseShape = ((ShapeDecorator) shapeAfterBorder).getInnerShape();

        assertTrue(baseShape instanceof Rectangle, "La forma base aggiunta dovrebbe essere un Rettangolo.");
        assertEquals(clickX, baseShape.getX(), "La coordinata X non corrisponde.");
        assertEquals(clickY, baseShape.getY(), "La coordinata Y non corrisponde.");
        assertEquals(ShapeFactory.DEFAULT_WIDTH, baseShape.getWidth(), "La larghezza di default non corrisponde.");
        assertEquals(ShapeFactory.DEFAULT_HEIGHT, baseShape.getHeight(), "L'altezza di default non corrisponde.");
        assertEquals(0, baseShape.getZ(), "Lo Z-order dovrebbe essere 0 per la prima forma.");

        Stack<Command> undoStack = getUndoStack(commandManager);
        assertFalse(undoStack.isEmpty(), "Lo stack undo non dovrebbe essere vuoto.");
        assertEquals(3, undoStack.size(), "Dovrebbero esserci 3 comandi nello stack undo.");
        assertTrue(undoStack.get(0) instanceof AddShapeCommand, "Il primo comando dovrebbe essere AddShapeCommand.");
        assertTrue(undoStack.get(1) instanceof com.geometricdrawing.command.ChangeWidthCommand, "Il secondo comando dovrebbe essere ChangeWidthCommand."); // Usa il nome completo se non importato
        assertTrue(undoStack.peek() instanceof com.geometricdrawing.command.ChangeHeightCommand, "Il comando in cima allo stack undo dovrebbe essere ChangeHeightCommand."); // Usa il nome completo se non importato

        assertNull(getPrivateField(controller, "currentShapeFactory"), "currentShapeFactory dovrebbe essere resettata.");
        assertEquals(decoratedShape, getPrivateField(controller, "currentShape"), "La forma corrente nel controller non è quella inserita.");
        assertFalse(widthSpinner.isDisabled(), "Width spinner dovrebbe essere abilitato.");
        assertFalse(heightSpinner.isDisabled(), "Height spinner dovrebbe essere abilitato.");
        assertFalse(deleteButton.isDisabled(), "Delete button dovrebbe essere abilitato.");
        assertEquals(baseShape.getWidth(), widthSpinner.getValueFactory().getValue(), "Valore errato nello spinner larghezza.");
        assertEquals(baseShape.getHeight(), heightSpinner.getValueFactory().getValue(), "Valore errato nello spinner altezza.");
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
            controller.handleSelectEllisse(new ActionEvent());
            MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, clickX, clickY, clickX, clickY, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, true, false, false, null);
            controller.handleCanvasClick(clickEvent);
        });

        assertEquals(1, model.getShapes().size());
        AbstractShape decoratedShape = model.getShapes().get(0);

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
    }

    @Test
    @DisplayName("Inserimento Linea disabilita FillPicker e HeightSpinner")
    void testInsertLineDisablesFillPickerAndHeightSpinner() throws Exception {
        final double clickX = 200.0;
        final double clickY = 250.0;

        runOnFxThreadAndWait(() -> controller.handleSelectLinea(new ActionEvent()));

        // Verifica stato UI *dopo* la selezione del tipo Linea, ma *prima* dell'inserimento
        final boolean[] fillDisabledInitial = new boolean[1];
        runOnFxThreadAndWait(() -> fillDisabledInitial[0] = fillColorPicker.isDisabled());
        assertTrue(fillDisabledInitial[0], "Fill picker dovrebbe essere disabilitato dopo aver selezionato Linea.");

        runOnFxThreadAndWait(() -> {
            MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, clickX, clickY, clickX, clickY, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, true, false, false, null);
            controller.handleCanvasClick(clickEvent);
        });

        assertEquals(1, model.getShapes().size());
        AbstractShape decoratedShape = model.getShapes().get(0);
        assertTrue(decoratedShape instanceof BorderColorDecorator);
        AbstractShape baseShape = ((ShapeDecorator) decoratedShape).getInnerShape();

        assertTrue(baseShape instanceof Line);
        assertEquals(clickX, baseShape.getX());
        assertEquals(clickY, baseShape.getY());
        assertEquals(ShapeFactory.DEFAULT_LINE_LENGTH, baseShape.getWidth());
        assertEquals(1.0, baseShape.getHeight());

        assertEquals(decoratedShape, getPrivateField(controller, "currentShape"));
        assertFalse(widthSpinner.isDisabled());
        assertTrue(heightSpinner.isDisabled());
        assertTrue(fillColorPicker.isDisabled());
        assertTrue(borderColorPicker.isDisabled());
        assertFalse(deleteButton.isDisabled());
        assertEquals(ShapeFactory.DEFAULT_LINE_LENGTH, widthSpinner.getValueFactory().getValue(), "Lo spinner larghezza dovrebbe mostrare la lunghezza di default iniziale della linea.");
    }

    @Test
    @DisplayName("Z-order per inserimenti consecutivi")
    void testZOrderOnConsecutiveInsertions() throws Exception {
        runOnFxThreadAndWait(() -> {
            controller.handleSelectRettangolo(new ActionEvent());
            controller.handleCanvasClick(new MouseEvent(MouseEvent.MOUSE_CLICKED, 10, 10, 10, 10, MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, true, false, false, null));
        });
        AbstractShape baseShape1 = ((ShapeDecorator)((ShapeDecorator)model.getShapes().get(0)).getInnerShape()).getInnerShape();
        assertEquals(0, baseShape1.getZ());

        runOnFxThreadAndWait(() -> {
            controller.handleSelectLinea(new ActionEvent());
            controller.handleCanvasClick(new MouseEvent(MouseEvent.MOUSE_CLICKED, 20, 20, 20, 20, MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, true, false, false, null));
        });
        assertEquals(2, model.getShapes().size());
        AbstractShape baseShape2 = ((ShapeDecorator)model.getShapes().get(1)).getInnerShape();
        assertEquals(1, baseShape2.getZ());

        runOnFxThreadAndWait(() -> {
            controller.handleSelectEllisse(new ActionEvent());
            controller.handleCanvasClick(new MouseEvent(MouseEvent.MOUSE_CLICKED, 30, 30, 30, 30, MouseButton.PRIMARY, 1, false, false, false, false, true, false, false, true, false, false, null));
        });
        assertEquals(3, model.getShapes().size());
        AbstractShape baseShape3 = ((ShapeDecorator)((ShapeDecorator)model.getShapes().get(2)).getInnerShape()).getInnerShape();
        assertEquals(2, baseShape3.getZ());
    }
}
