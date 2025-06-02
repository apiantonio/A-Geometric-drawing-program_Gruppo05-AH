package com.geometricdrawing.integration;

import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.controller.ZoomHandler;
import com.geometricdrawing.command.*;
import com.geometricdrawing.decorator.ShapeDecorator;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
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

@DisplayName("Test di Integrazione per le Funzionalità degli Appunti")
public class ClipboardFunctionalitiesIntegrationTest {

    private static volatile boolean fxInitialized = false;

    private DrawingController controller;
    private DrawingModel model;
    private CommandManager commandManager;
    private ClipboardManager clipboardManager;

    private Canvas drawingCanvas;
    private AnchorPane canvasContainer; // Modificato da Pane ad AnchorPane
    private AnchorPane rootPane;

    private ColorPicker fillColorPicker;
    private ColorPicker borderColorPicker;
    private Spinner<Double> heightSpinner;
    private Spinner<Double> widthSpinner;
    private Button deleteButton;
    private Button copyButton;
    private Button cutButton;
    private Button pasteButton;
    private Button undoButton;

    private static final double CANVAS_WIDTH_FOR_TEST = 800;
    private static final double CANVAS_HEIGHT_FOR_TEST = 600;
    private static final double DEFAULT_PASTE_OFFSET = 10.0;

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

                new Scene(rootPane, CANVAS_WIDTH_FOR_TEST + 100, CANVAS_HEIGHT_FOR_TEST + 100);
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
                cutButton = new Button("Taglia");
                pasteButton = new Button("Incolla");
                undoButton = new Button("Annulla");

                MenuItem mirrorHorizontal = new MenuItem("Specchia Orizzontalmente");
                MenuItem mirrorVertical = new MenuItem("Specchia Verticalmente");

                TextField textField = new TextField();
                Spinner<Integer> fontSizeSpinner = new Spinner<>();
                SpinnerValueFactory<Integer> fontSizeFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(6, 72, 12, 1);
                fontSizeSpinner.setValueFactory(fontSizeFactory);
                Spinner<Double> rotationSpinner = new Spinner<>();
                SpinnerValueFactory<Double> rotationFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0, 1);
                rotationSpinner.setValueFactory(rotationFactory);

                CheckMenuItem toggleGrid = new CheckMenuItem("Mostra Griglia");
                MenuButton gridOptions = new MenuButton();

                setPrivateField(controller, "drawingCanvas", drawingCanvas);
                setPrivateField(controller, "canvasContainer", canvasContainer);
                setPrivateField(controller, "rootPane", rootPane);
                setPrivateField(controller, "fillPicker", fillColorPicker);
                setPrivateField(controller, "borderPicker", borderColorPicker);
                setPrivateField(controller, "heightSpinner", heightSpinner);
                setPrivateField(controller, "widthSpinner", widthSpinner);
                setPrivateField(controller, "deleteButton", deleteButton);
                setPrivateField(controller, "copyButton", copyButton);
                setPrivateField(controller, "cutButton", cutButton);
                setPrivateField(controller, "pasteButton", pasteButton);
                setPrivateField(controller, "undoButton", undoButton);
                setPrivateField(controller, "cutCopyLabel", new Label());
                setPrivateField(controller, "emptyClipboardLabel", new Label());
                setPrivateField(controller, "mirrorHorizontal", mirrorHorizontal);
                setPrivateField(controller, "mirrorVertical", mirrorVertical);
                setPrivateField(controller, "horizontalScrollBar", new ScrollBar());
                setPrivateField(controller, "verticalScrollBar", new ScrollBar());
                setPrivateField(controller, "textField", textField);
                setPrivateField(controller, "fontSizeSpinner", fontSizeSpinner);
                setPrivateField(controller, "rotationSpinner", rotationSpinner);
                setPrivateField(controller, "gridOptions", gridOptions);
                setPrivateField(controller, "toggleGrid", toggleGrid);

                controller.setModel(model);
                controller.setCommandManager(commandManager);
                controller.initialize();

                clipboardManager = (ClipboardManager) getPrivateField(controller, "clipboardManager");

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
            } finally {
                actionLatch.countDown();
            }
        });
        if (!actionLatch.await(5, TimeUnit.SECONDS)) {
            throw new InterruptedException("Timeout durante l'esecuzione dell'azione sul thread JavaFX.");
        }
    }

    private AbstractShape insertAndSelectShape(String shapeType, double x, double y) throws Exception {
        AtomicReference<AbstractShape> shapeRef = new AtomicReference<>();
        runOnFxThreadAndWait(() -> {
            if ("Rectangle".equalsIgnoreCase(shapeType)) {
                controller.handleSelectRettangolo(new ActionEvent());
            } else if ("Ellipse".equalsIgnoreCase(shapeType)) {
                controller.handleSelectEllisse(new ActionEvent());
            }

            MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, x, y, x, y, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, true, false, false, null);
            controller.getDrawingCanvas().fireEvent(clickEvent);
            shapeRef.set(controller.getCurrentShape());
        });
        assertNotNull(shapeRef.get(), "La forma non è stata inserita o selezionata correttamente.");
        return shapeRef.get();
    }

    private AbstractShape getBaseShape(AbstractShape decoratedShape) {
        AbstractShape current = decoratedShape;
        while (current instanceof ShapeDecorator) {
            current = ((ShapeDecorator) current).getInnerShape();
        }
        return current;
    }

    @Test
    @DisplayName("Copia e Incolla di una forma con offset di default")
    void testCopyAndPasteShapeWithDefaultOffset() throws Exception {
        final double initialX = 50.0;
        final double initialY = 60.0;

        AbstractShape originalDecoratedShape = insertAndSelectShape("Rectangle", initialX, initialY);
        AbstractShape originalBaseShape = getBaseShape(originalDecoratedShape);
        assertEquals(1, model.getShapes().size(), "Il modello dovrebbe contenere una forma dopo l'inserimento.");

        runOnFxThreadAndWait(() -> controller.handleCopyShape(new ActionEvent()));
        assertTrue(clipboardManager.hasContent(), "Gli appunti dovrebbero avere contenuto dopo la copia.");

        runOnFxThreadAndWait(() -> controller.handlePasteShape(new ActionEvent()));
        assertEquals(2, model.getShapes().size(), "Il modello dovrebbe contenere due forme dopo l'incolla.");

        AbstractShape pastedDecoratedShape = controller.getCurrentShape();
        assertNotNull(pastedDecoratedShape, "Una forma dovrebbe essere selezionata dopo l'incolla.");
        assertNotSame(originalDecoratedShape, pastedDecoratedShape, "La forma incollata dovrebbe essere un'istanza diversa.");

        AbstractShape pastedBaseShape = getBaseShape(pastedDecoratedShape);
        assertTrue(pastedBaseShape instanceof Rectangle, "La forma incollata dovrebbe essere un Rettangolo.");
        assertEquals(originalBaseShape.getWidth(), pastedBaseShape.getWidth(), "Larghezza non corrispondente.");
        assertEquals(originalBaseShape.getHeight(), pastedBaseShape.getHeight(), "Altezza non corrispondente.");

        assertEquals(initialX + DEFAULT_PASTE_OFFSET, pastedBaseShape.getX(), 0.001, "Coordinata X incollata errata.");
        assertEquals(initialY + DEFAULT_PASTE_OFFSET, pastedBaseShape.getY(), 0.001, "Coordinata Y incollata errata.");

        Stack<Command> undoStack = getUndoStack(commandManager);
        assertEquals(3, undoStack.size(), "Undo stack dovrebbe contenere Add, Copy, Paste.");
        assertTrue(undoStack.get(0) instanceof AddShapeCommand);
        assertTrue(undoStack.get(1) instanceof CopyShapeCommand);
        assertTrue(undoStack.get(2) instanceof PasteShapeCommand);
    }

    @Test
    @DisplayName("Taglia e Incolla di una forma con offset di default")
    void testCutAndPasteShapeWithDefaultOffset() throws Exception {
        final double initialX = 70.0;
        final double initialY = 80.0;

        AbstractShape originalDecoratedShape = insertAndSelectShape("Rectangle", initialX, initialY);
        final double originalWidth = getBaseShape(originalDecoratedShape).getWidth();
        final double originalHeight = getBaseShape(originalDecoratedShape).getHeight();
        assertEquals(1, model.getShapes().size());

        runOnFxThreadAndWait(() -> controller.handleCutShape(new ActionEvent()));
        assertTrue(clipboardManager.hasContent(), "Gli appunti dovrebbero avere contenuto dopo il taglio.");
        assertEquals(0, model.getShapes().size(), "Il modello non dovrebbe contenere forme dopo il taglio.");
        assertNull(controller.getCurrentShape(), "Nessuna forma dovrebbe essere selezionata dopo il taglio.");

        runOnFxThreadAndWait(() -> controller.handlePasteShape(new ActionEvent()));
        assertEquals(1, model.getShapes().size(), "Il modello dovrebbe contenere una forma dopo l'incolla.");

        AbstractShape pastedDecoratedShape = controller.getCurrentShape();
        assertNotNull(pastedDecoratedShape, "Una forma dovrebbe essere selezionata dopo l'incolla.");
        AbstractShape pastedBaseShape = getBaseShape(pastedDecoratedShape);

        assertTrue(pastedBaseShape instanceof Rectangle);
        assertEquals(originalWidth, pastedBaseShape.getWidth());
        assertEquals(originalHeight, pastedBaseShape.getHeight());

        assertEquals(initialX + DEFAULT_PASTE_OFFSET, pastedBaseShape.getX(), 0.001, "Coordinata X incollata errata dopo taglio.");
        assertEquals(initialY + DEFAULT_PASTE_OFFSET, pastedBaseShape.getY(), 0.001, "Coordinata Y incollata errata dopo taglio.");

        Stack<Command> undoStack = getUndoStack(commandManager);
        assertEquals(3, undoStack.size(), "Undo stack dovrebbe contenere Add, Cut, Paste.");
        assertTrue(undoStack.get(0) instanceof AddShapeCommand);
        assertTrue(undoStack.get(1) instanceof CutShapeCommand);
        assertTrue(undoStack.get(2) instanceof PasteShapeCommand);
    }

    @Test
    @DisplayName("Incolla forma in una posizione specifica tramite menu contestuale del canvas")
    void testPasteShapeAtSpecificLocationViaContextMenuLogic() throws Exception {
        AbstractShape originalShape = insertAndSelectShape("Rectangle", 50, 50);
        AbstractShape originalBaseShape = getBaseShape(originalShape);

        runOnFxThreadAndWait(() -> controller.handleCopyShape(new ActionEvent()));
        assertTrue(clipboardManager.hasContent());
        assertEquals(1, model.getShapes().size());

        final double targetCanvasX = 200.0;
        final double targetCanvasY = 220.0;

        runOnFxThreadAndWait(() -> {
            try {
                setPrivateField(controller, "lastCanvasMouseX", targetCanvasX);
                setPrivateField(controller, "lastCanvasMouseY", targetCanvasY);
            } catch (Exception e) { fail(e); }
            controller.handlePasteShape(new ActionEvent(), targetCanvasX, targetCanvasY);
        });

        assertEquals(2, model.getShapes().size(), "Dovrebbero esserci due forme dopo l'incolla.");
        AbstractShape pastedDecoratedShape = controller.getCurrentShape();
        assertNotNull(pastedDecoratedShape);
        AbstractShape pastedBaseShape = getBaseShape(pastedDecoratedShape);

        ZoomHandler zoomHandler = controller.getZoomHandler();
        assertNotNull(zoomHandler, "ZoomHandler non dovrebbe essere nullo.");
        Point2D worldPastedClickPoint = zoomHandler.screenToWorld(targetCanvasX, targetCanvasY);

        double expectedWorldX = worldPastedClickPoint.getX() - (originalBaseShape.getWidth() / 2.0);
        double expectedWorldY = worldPastedClickPoint.getY() - (originalBaseShape.getHeight() / 2.0);

        assertEquals(expectedWorldX, pastedBaseShape.getX(), 0.001, "Coordinata X incollata errata per 'Incolla qui'.");
        assertEquals(expectedWorldY, pastedBaseShape.getY(), 0.001, "Coordinata Y incollata errata per 'Incolla qui'.");
    }

    @Test
    @DisplayName("Annulla dopo Incolla")
    void testUndoAfterPaste() throws Exception {
        insertAndSelectShape("Rectangle", 50, 50);
        runOnFxThreadAndWait(() -> controller.handleCopyShape(new ActionEvent()));
        runOnFxThreadAndWait(() -> controller.handlePasteShape(new ActionEvent()));
        assertEquals(2, model.getShapes().size());

        runOnFxThreadAndWait(() -> controller.handleUndo(new ActionEvent()));
        assertEquals(1, model.getShapes().size(), "Il modello dovrebbe contenere una forma dopo l'annullamento dell'incolla.");
        assertTrue(clipboardManager.hasContent(), "Gli appunti dovrebbero ancora avere contenuto.");
        assertNull(controller.getCurrentShape(), "La forma corrente dovrebbe essere nulla dopo l'undo che ha rimosso la forma incollata.");
    }

    @Test
    @DisplayName("Annulla dopo Taglia")
    void testUndoAfterCut() throws Exception {
        AbstractShape originalShape = insertAndSelectShape("Rectangle", 50, 50);
        clipboardManager.copyToClipboard(new Rectangle(1,1,1,1)); // Contenuto fittizio precedente

        runOnFxThreadAndWait(() -> controller.handleCutShape(new ActionEvent()));
        assertEquals(0, model.getShapes().size());
        assertTrue(clipboardManager.hasContent());
        AbstractShape contentAfterCut = clipboardManager.getFromClipboard();
        assertNotNull(contentAfterCut);
        assertEquals(getBaseShape(originalShape).getWidth(), contentAfterCut.getWidth());

        runOnFxThreadAndWait(() -> controller.handleUndo(new ActionEvent()));
        assertEquals(1, model.getShapes().size(), "Il modello dovrebbe contenere la forma originale dopo l'annullamento del taglio.");
        // Dato che CutShapeCommand.undo() aggiunge la figura al modello ma non la imposta come currentShape nel controller,
        // e handleUndo() nel controller imposta currentShape a null, ci aspettiamo che currentShape sia null.
        // L'importante è che la forma sia tornata nel modello.
        assertTrue(model.getShapes().contains(originalShape),"La forma originale non è stata ripristinata nel modello");


        assertTrue(clipboardManager.hasContent(), "Gli appunti dovrebbero avere il contenuto precedente.");
        AbstractShape contentAfterUndoCut = clipboardManager.getFromClipboard();
        assertNotNull(contentAfterUndoCut);
        assertEquals(1.0, contentAfterUndoCut.getWidth(), "Il contenuto degli appunti non è stato ripristinato correttamente.");
    }

    @Test
    @DisplayName("Annulla dopo Copia")
    void testUndoAfterCopy() throws Exception {
        insertAndSelectShape("Rectangle", 50, 50);
        runOnFxThreadAndWait(() -> controller.handleCopyShape(new ActionEvent()));
        assertTrue(clipboardManager.hasContent(), "Gli appunti dovrebbero avere contenuto dopo la copia.");

        runOnFxThreadAndWait(() -> controller.handleUndo(new ActionEvent()));
        assertFalse(clipboardManager.hasContent(), "Gli appunti dovrebbero essere vuoti dopo l'annullamento della copia.");
    }
}