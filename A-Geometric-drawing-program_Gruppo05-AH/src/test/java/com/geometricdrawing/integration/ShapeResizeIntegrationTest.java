package com.geometricdrawing.integration;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.command.ChangeHeightCommand;
import com.geometricdrawing.command.ChangeWidthCommand;
import com.geometricdrawing.command.Command;
import com.geometricdrawing.command.CommandManager;
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

@DisplayName("Test di Integrazione per il Ridimensionamento delle Figure")
public class ShapeResizeIntegrationTest {

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
        // Considera Platform.exit(); se necessario, ma può interferire con altri test suite
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        final CountDownLatch setupLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                model = new DrawingModel();
                commandManager = new CommandManager();
                controller = new DrawingController();

                drawingCanvas = new Canvas(800, 600);
                canvasContainer = new Pane(drawingCanvas);
                rootPane = new AnchorPane();
                fillColorPicker = new ColorPicker(Color.LIGHTGREEN);
                borderColorPicker = new ColorPicker(Color.ORANGE);

                heightSpinner = new Spinner<>();
                SpinnerValueFactory<Double> heightFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, ShapeFactory.DEFAULT_HEIGHT, 1.0);
                heightSpinner.setValueFactory(heightFactory);
                heightSpinner.setEditable(true);

                widthSpinner = new Spinner<>();
                SpinnerValueFactory<Double> widthFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, ShapeFactory.DEFAULT_WIDTH, 1.0);
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

    // Metodi helper (setPrivateField, getPrivateField, getUndoStack, runOnFxThreadAndWait)
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
            } finally {
                actionLatch.countDown();
            }
        });
        if (!actionLatch.await(5, TimeUnit.SECONDS)) {
            throw new InterruptedException("Timeout durante l'esecuzione dell'azione sul thread JavaFX.");
        }
    }
    // FINE METODI HELPER

    private AbstractShape insertAndGetCurrentShape(String shapeType, double x, double y) throws Exception {
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
        });
        AbstractShape currentDecoratedShape = (AbstractShape) getPrivateField(controller, "currentShape");
        assertNotNull(currentDecoratedShape, "Nessuna forma corrente selezionata dopo l'inserimento.");
        return currentDecoratedShape;
    }

    private AbstractShape getBaseShape(AbstractShape decoratedShape) {
        AbstractShape baseShape = decoratedShape;
        while (baseShape instanceof ShapeDecorator) {
            baseShape = ((ShapeDecorator) baseShape).getInnerShape();
        }
        return baseShape;
    }


    @Test
    @DisplayName("Modifica larghezza di un Rettangolo tramite Spinner")
    void testResizeRectangleWidthViaSpinner() throws Exception {
        final double initialWidth = ShapeFactory.DEFAULT_WIDTH;
        final double newWidth = 200.0;

        AbstractShape decoratedRectangle = insertAndGetCurrentShape("Rectangle", 100, 100);
        AbstractShape baseRectangle = getBaseShape(decoratedRectangle);

        assertEquals(initialWidth, baseRectangle.getWidth(), "Larghezza iniziale del rettangolo non corretta.");

        runOnFxThreadAndWait(() -> widthSpinner.getValueFactory().setValue(newWidth));

        assertEquals(newWidth, baseRectangle.getWidth(), "Larghezza del rettangolo non aggiornata dopo modifica spinner.");
        assertEquals(newWidth, widthSpinner.getValueFactory().getValue(), "Valore dello spinner larghezza non corretto.");

        Stack<Command> undoStack = getUndoStack(commandManager);
        // Stack atteso: AddShape, ChangeWidth (iniziale), ChangeHeight (iniziale), ChangeWidth (da spinner)
        assertEquals(4, undoStack.size(), "Numero comandi errato.");
        assertTrue(undoStack.peek() instanceof ChangeWidthCommand, "Ultimo comando non è ChangeWidthCommand.");
    }

    @Test
    @DisplayName("Modifica altezza di un Ellisse tramite Spinner")
    void testResizeEllipseHeightViaSpinner() throws Exception {
        final double initialHeight = ShapeFactory.DEFAULT_HEIGHT;
        final double newHeight = 150.0;

        AbstractShape decoratedEllipse = insertAndGetCurrentShape("Ellipse", 150, 150);
        AbstractShape baseEllipse = getBaseShape(decoratedEllipse);

        assertEquals(initialHeight, baseEllipse.getHeight(), "Altezza iniziale dell'ellisse non corretta.");

        runOnFxThreadAndWait(() -> heightSpinner.getValueFactory().setValue(newHeight));

        assertEquals(newHeight, baseEllipse.getHeight(), "Altezza dell'ellisse non aggiornata dopo modifica spinner.");
        assertEquals(newHeight, heightSpinner.getValueFactory().getValue(), "Valore dello spinner altezza non corretto.");

        Stack<Command> undoStack = getUndoStack(commandManager);
        // Stack atteso: AddShape, ChangeWidth (iniziale), ChangeHeight (iniziale), ChangeHeight (da spinner)
        assertEquals(4, undoStack.size(), "Numero comandi errato.");
        assertTrue(undoStack.peek() instanceof ChangeHeightCommand, "Ultimo comando non è ChangeHeightCommand.");
    }

    @Test
    @DisplayName("Modifica 'larghezza' (lunghezza) di una Linea tramite Spinner")
    void testResizeLineWidthViaSpinner() throws Exception {
        final double factoryDefaultWidthForLine = ShapeFactory.DEFAULT_LINE_LENGTH; // Es. 100.0
        final double newSpinnerSetting = 180.0;

        AbstractShape decoratedLine = insertAndGetCurrentShape("Line", 50, 50);
        AbstractShape baseLine = getBaseShape(decoratedLine);

        assertEquals(factoryDefaultWidthForLine, baseLine.getWidth(), 0.0001, "Proprietà 'width' della linea dopo l'inserimento non corretta.");
        assertEquals(1.0, baseLine.getHeight(), 0.0001, "Proprietà 'height' della linea dopo l'inserimento non corretta.");

        double geometricLengthAfterHeightChange = Math.sqrt(Math.pow(factoryDefaultWidthForLine, 2) + Math.pow(1.0, 2)); // Es. 100.00499...
        assertEquals(geometricLengthAfterHeightChange, ((Line) baseLine).getLength(), 0.0001, "Lunghezza geometrica della linea dopo l'inserimento non corretta.");

        assertEquals(factoryDefaultWidthForLine, widthSpinner.getValueFactory().getValue(), 0.0001, "Valore iniziale spinner larghezza per linea non corretto."); // <<< QUESTA È LA CORREZIONE CHIAVE

        assertTrue(heightSpinner.isDisabled(), "Spinner altezza dovrebbe essere disabilitato per la linea.");

        // Adesso simula la modifica dello spinner a un nuovo valore
        runOnFxThreadAndWait(() -> widthSpinner.getValueFactory().setValue(newSpinnerSetting));

        assertEquals(newSpinnerSetting, widthSpinner.getValueFactory().getValue(), 0.0001, "Valore dello spinner larghezza per linea non corretto dopo modifica.");
        assertEquals(newSpinnerSetting, baseLine.getWidth(), 0.0001, "Proprietà 'width' della linea non aggiornata dopo modifica spinner.");
    }

    @Test
    @DisplayName("Spinners disabilitati se nessuna figura è selezionata")
    void testSpinnersDisabledWhenNoShapeSelected() throws Exception {
        // All'inizio, nessuna forma è selezionata, quindi gli spinner dovrebbero essere disabilitati
        // come da logica in controller.initialize() -> updateControlState(null)
        assertTrue(widthSpinner.isDisabled(), "Spinner larghezza dovrebbe essere disabilitato all'avvio.");
        assertTrue(heightSpinner.isDisabled(), "Spinner altezza dovrebbe essere disabilitato all'avvio.");

        // Inserisci una forma e selezionala (automaticamente dopo l'inserimento)
        insertAndGetCurrentShape("Rectangle", 100, 100);
        assertFalse(widthSpinner.isDisabled(), "Spinner larghezza dovrebbe essere abilitato dopo selezione.");
        assertFalse(heightSpinner.isDisabled(), "Spinner altezza dovrebbe essere abilitato dopo selezione.");

        // Deseleziona la forma (cliccando sul canvas vuoto)
        runOnFxThreadAndWait(() -> {
            MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_PRESSED, 1, 1, 1, 1, MouseButton.PRIMARY, 1,
                    false, false, false, false, false, false, false, true, false, false, null);
            // handleMousePressed dovrebbe deselezionare la forma e chiamare updateControlState(null)
            drawingCanvas.getOnMousePressed().handle(clickEvent);
        });

        // Verifica che currentShape sia null nel controller
        assertNull(getPrivateField(controller, "currentShape"), "Nessuna forma dovrebbe essere selezionata.");
        assertTrue(widthSpinner.isDisabled(), "Spinner larghezza dovrebbe essere disabilitato dopo deselezione.");
        assertTrue(heightSpinner.isDisabled(), "Spinner altezza dovrebbe essere disabilitato dopo deselezione.");
    }
}