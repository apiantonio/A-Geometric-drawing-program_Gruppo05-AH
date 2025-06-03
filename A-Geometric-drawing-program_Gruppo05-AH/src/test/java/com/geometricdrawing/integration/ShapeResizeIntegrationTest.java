package com.geometricdrawing.integration;

import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.command.ChangeHeightCommand;
import com.geometricdrawing.command.ChangeWidthCommand;
import com.geometricdrawing.command.Command;
import com.geometricdrawing.command.CommandManager;
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

@DisplayName("Test di Integrazione per il Ridimensionamento delle Figure")
public class ShapeResizeIntegrationTest {

    private static volatile boolean fxInitialized = false;

    private DrawingController controller;
    private DrawingModel model;
    private CommandManager commandManager;

    private Canvas drawingCanvas;
    private Pane canvasContainer;
    private AnchorPane rootPane;
    private ColorPicker fillColorPicker;
    private ColorPicker borderColorPicker;
    private Spinner<Double> heightSpinner;
    private Spinner<Double> widthSpinner;
    private Button deleteButton;
    private Button copyButton;
    private ScrollBar horizontalScrollBar;
    private ScrollBar verticalScrollBar;
    private MenuItem mirrorHorizontal;
    private MenuItem mirrorVertical;
    private TextField textField;

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
            fxInitialized = true;
        }
    }

    @AfterAll
    public static void cleanupFX() {
        // Platform.exit(); // Generally not recommended in JUnit tests if JVM needs to persist
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

                horizontalScrollBar = new ScrollBar();
                horizontalScrollBar.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
                
                verticalScrollBar = new ScrollBar();
                verticalScrollBar.setOrientation(javafx.geometry.Orientation.VERTICAL);
                
                mirrorHorizontal = new MenuItem("Specchia Orizzontalmente");
                mirrorVertical = new MenuItem("Specchia Verticalmente");
                textField = new TextField();

                CheckMenuItem toggleGrid = new CheckMenuItem("Mostra Griglia");
                toggleGrid.setSelected(false);
                MenuButton gridOptions = new MenuButton("Opzioni Griglia");
                gridOptions.setDisable(true);

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
                setPrivateField(controller, "mirrorHorizontal", mirrorHorizontal);
                setPrivateField(controller, "mirrorVertical", mirrorVertical);
                setPrivateField(controller, "textField", textField);
                setPrivateField(controller, "toggleGrid", toggleGrid);
                setPrivateField(controller, "gridOptions", gridOptions);

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
            controller.getDrawingCanvas().fireEvent(clickEvent);
            currentShapeRef.set((AbstractShape) getPrivateFieldNonFailing(controller, "currentShape"));
        });
        assertNotNull(currentShapeRef.get(), "Nessuna forma corrente selezionata dopo l'inserimento (letto da controller). Controllare i log del thread FX.");
        return currentShapeRef.get();
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
        final double initialWidthFromFactory = ShapeFactory.DEFAULT_WIDTH;
        final double newWidth = 200.0;

        AbstractShape decoratedRectangle = insertAndGetSelectedShapeFromController("Rectangle", 100, 100);
        AbstractShape baseRectangle = getBaseShape(decoratedRectangle);

        assertEquals(initialWidthFromFactory, baseRectangle.getWidth(), "Larghezza iniziale del rettangolo errata.");
        assertEquals(initialWidthFromFactory, widthSpinner.getValueFactory().getValue(), "Valore iniziale dello spinner larghezza errato.");

        Stack<Command> undoStackBeforeResize = getUndoStack(commandManager);
        assertEquals(1, undoStackBeforeResize.size(), "Undo stack dovrebbe avere 1 comando (AddShape) prima del ridimensionamento.");

        runOnFxThreadAndWait(() -> widthSpinner.getValueFactory().setValue(newWidth));

        assertEquals(newWidth, baseRectangle.getWidth(), "Larghezza del rettangolo non aggiornata dopo modifica spinner.");
        assertEquals(newWidth, widthSpinner.getValueFactory().getValue(), "Valore spinner larghezza non aggiornato.");

        Stack<Command> undoStackAfterResize = getUndoStack(commandManager);
        assertEquals(2, undoStackAfterResize.size(), "Undo stack dovrebbe avere 2 comandi dopo il ridimensionamento.");
        assertTrue(undoStackAfterResize.peek() instanceof ChangeWidthCommand, "L'ultimo comando dovrebbe essere ChangeWidthCommand.");
    }

    @Test
    @DisplayName("Modifica altezza di un Ellisse tramite Spinner")
    void testResizeEllipseHeightViaSpinner() throws Exception {
        final double initialHeightFromFactory = ShapeFactory.DEFAULT_HEIGHT;
        final double newHeight = 150.0;

        AbstractShape decoratedEllipse = insertAndGetSelectedShapeFromController("Ellipse", 150, 150);
        AbstractShape baseEllipse = getBaseShape(decoratedEllipse);

        assertEquals(initialHeightFromFactory, baseEllipse.getHeight(), "Altezza iniziale ellisse errata.");
        assertEquals(initialHeightFromFactory, heightSpinner.getValueFactory().getValue(), "Valore iniziale spinner altezza errato.");

        Stack<Command> undoStackBeforeResize = getUndoStack(commandManager);
        assertEquals(1, undoStackBeforeResize.size(), "Undo stack dovrebbe avere 1 comando (AddShape) prima del ridimensionamento.");

        runOnFxThreadAndWait(() -> heightSpinner.getValueFactory().setValue(newHeight));

        assertEquals(newHeight, baseEllipse.getHeight(), "Altezza ellisse non aggiornata dopo modifica spinner.");
        assertEquals(newHeight, heightSpinner.getValueFactory().getValue(), "Valore spinner altezza non aggiornato.");

        Stack<Command> undoStackAfterResize = getUndoStack(commandManager);
        assertEquals(2, undoStackAfterResize.size(), "Undo stack dovrebbe avere 2 comandi dopo il ridimensionamento.");
        assertTrue(undoStackAfterResize.peek() instanceof ChangeHeightCommand, "L'ultimo comando dovrebbe essere ChangeHeightCommand.");
    }

    @Test
    @DisplayName("Modifica 'larghezza' (lunghezza) di una Linea tramite Spinner")
    void testResizeLineWidthViaSpinner() throws Exception {
        final double factoryDefaultLineLength = ShapeFactory.DEFAULT_LINE_LENGTH;
        final double newSpinnerSetting = 180.0;

        AbstractShape decoratedLine = insertAndGetSelectedShapeFromController("Line", 50, 50);
        AbstractShape baseLine = getBaseShape(decoratedLine);

        assertTrue(baseLine instanceof Line, "La forma base dovrebbe essere una Linea.");
        assertEquals(factoryDefaultLineLength, baseLine.getWidth(), 0.0001, "Larghezza iniziale linea (lunghezza) errata.");
        assertEquals(0.0, baseLine.getHeight(), 0.0001, "Altezza iniziale di una linea orizzontale di default dovrebbe essere 0.");
        assertTrue(heightSpinner.isDisabled(), "Spinner altezza dovrebbe essere disabilitato per la linea.");

        Stack<Command> undoStackBeforeResize = getUndoStack(commandManager);
        assertEquals(1, undoStackBeforeResize.size(), "Undo stack dovrebbe avere 1 comando prima del ridimensionamento manuale.");

        runOnFxThreadAndWait(() -> widthSpinner.getValueFactory().setValue(newSpinnerSetting));

        assertEquals(newSpinnerSetting, widthSpinner.getValueFactory().getValue(), 0.0001, "Valore spinner larghezza non aggiornato.");
        assertEquals(newSpinnerSetting, baseLine.getWidth(), 0.0001, "Larghezza linea (lunghezza) non aggiornata.");
        assertEquals(0.0, baseLine.getHeight(), 0.0001, "Altezza linea non dovrebbe cambiare con la modifica della larghezza.");
        assertEquals(newSpinnerSetting, ((Line)baseLine).getLength(), 0.0001, "Lunghezza geometrica linea errata.");

        Stack<Command> undoStackAfterResize = getUndoStack(commandManager);
        assertEquals(2, undoStackAfterResize.size(), "Undo stack dovrebbe avere 2 comandi dopo il ridimensionamento manuale.");
        assertTrue(undoStackAfterResize.peek() instanceof ChangeWidthCommand, "L'ultimo comando dovrebbe essere ChangeWidthCommand.");
    }

    @Test
    @DisplayName("Spinners e Picker disabilitati/abilitati correttamente")
    void testControlsStateWhenNoShapeSelectedAndWhenShapeSelected() throws Exception {
        assertTrue(widthSpinner.isDisabled(), "Spinner larghezza dovrebbe essere disabilitato all'avvio.");
        assertTrue(heightSpinner.isDisabled(), "Spinner altezza dovrebbe essere disabilitato all'avvio.");
        assertTrue(fillColorPicker.isDisabled(), "Fill picker dovrebbe essere disabilitato all'avvio.");
        assertTrue(borderColorPicker.isDisabled(), "Border picker dovrebbe essere disabilitato all'avvio.");
        assertTrue(deleteButton.isDisabled(), "Delete button dovrebbe essere disabilitato all'avvio.");
        assertTrue(copyButton.isDisabled(), "Copy button dovrebbe essere disabilitato all'avvio.");

        insertAndGetSelectedShapeFromController("Rectangle", 100, 100);

        assertFalse(widthSpinner.isDisabled(), "Spinner larghezza dovrebbe essere abilitato per Rettangolo.");
        assertFalse(heightSpinner.isDisabled(), "Spinner altezza dovrebbe essere abilitato per Rettangolo.");
        assertFalse(deleteButton.isDisabled(), "Delete button dovrebbe essere abilitato per Rettangolo.");
        assertFalse(copyButton.isDisabled(), "Copy button dovrebbe essere abilitato per Rettangolo.");
        assertFalse(fillColorPicker.isDisabled(), "Fill picker ABILITATO per Rettangolo (logica UI).");
        assertFalse(borderColorPicker.isDisabled(), "Border picker ABILITATO per Rettangolo (logica UI).");

        runOnFxThreadAndWait(() -> {
            MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_PRESSED, 1, 1, 1, 1, MouseButton.PRIMARY, 1,
                    false, false, false, false, false, false, false, true, false, false, null);
            drawingCanvas.getOnMousePressed().handle(clickEvent);
        });

        assertNull(getPrivateField(controller, "currentShape"), "Nessuna forma corrente selezionata dopo click su area vuota.");
        assertTrue(widthSpinner.isDisabled(), "Spinner larghezza disabilitato dopo deselezione.");
        assertTrue(heightSpinner.isDisabled(), "Spinner altezza disabilitato dopo deselezione.");
        assertTrue(fillColorPicker.isDisabled(), "Fill picker disabilitato dopo deselezione.");
        assertTrue(borderColorPicker.isDisabled(), "Border picker disabilitato dopo deselezione.");
        assertTrue(deleteButton.isDisabled(), "Delete button disabilitato dopo deselezione.");
        assertTrue(copyButton.isDisabled(), "Copy button disabilitato dopo deselezione.");
    }
}