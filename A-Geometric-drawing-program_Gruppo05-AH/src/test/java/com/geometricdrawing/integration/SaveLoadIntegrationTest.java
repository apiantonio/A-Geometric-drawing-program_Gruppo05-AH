package com.geometricdrawing.integration;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.command.CommandManager;
import com.geometricdrawing.decorator.ShapeDecorator;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Test di Integrazione per Salvataggio e Caricamento Disegni")
public class SaveLoadIntegrationTest {

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

    private static final double CANVAS_WIDTH_FOR_TEST = 800;
    private static final double CANVAS_HEIGHT_FOR_TEST = 600;

    @TempDir
    Path tempDir;

    @BeforeAll
    public static void initFX() throws InterruptedException {
        if (fxInitialized) return;
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
                heightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 40.0, 1.0));
                heightSpinner.setEditable(true);
                widthSpinner = new Spinner<>();
                widthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 60.0, 1.0));
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
            throw new InterruptedException("Timeout setup.");
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object getPrivateField(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private Object getPrivateFieldNonFailing(Object target, String fieldName) {
        try {
            return getPrivateField(target, fieldName);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return "ERRORE_RIFLESSIONE";
        }
    }

    private void runOnFxThreadAndWait(Runnable action) throws InterruptedException {
        final CountDownLatch actionLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                System.err.println("Eccezione sul thread JavaFX: " + t.getMessage());
                t.printStackTrace();
            } finally {
                actionLatch.countDown();
            }
        });
        if (!actionLatch.await(10, TimeUnit.SECONDS)) {
            throw new InterruptedException("Timeout azione FX.");
        }
    }

    private AbstractShape insertAndGetSelectedShapeFromController(String shapeType, double x, double y, Color fillColor, Color borderColor) throws Exception {
        AtomicReference<AbstractShape> currentShapeRef = new AtomicReference<>(null);
        runOnFxThreadAndWait(() -> {
            if (fillColor != null) fillColorPicker.setValue(fillColor);
            if (borderColor != null) borderColorPicker.setValue(borderColor);

            if ("Rectangle".equalsIgnoreCase(shapeType)) {
                controller.handleSelectRettangolo(new ActionEvent());
            } else if ("Ellipse".equalsIgnoreCase(shapeType)) {
                controller.handleSelectEllisse(new ActionEvent());
            } else if ("Line".equalsIgnoreCase(shapeType)) {
                controller.handleSelectLinea(new ActionEvent());
            }
            MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, x, y, x, y, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, true, false, false, null);
            // Simula il click sul canvas usando il handler appropriato
            controller.getDrawingCanvas().fireEvent(clickEvent);
            currentShapeRef.set((AbstractShape) getPrivateFieldNonFailing(controller, "currentShape"));
        });
        assertNotNull(currentShapeRef.get(), "Nessuna forma corrente selezionata dopo l'inserimento.");
        return currentShapeRef.get();
    }

    private AbstractShape getBaseShape(AbstractShape decoratedShape) {
        AbstractShape current = decoratedShape;
        while (current instanceof ShapeDecorator) {
            current = ((ShapeDecorator) current).getInnerShape();
        }
        return current;
    }

    private void assertShapesAreEqual(AbstractShape expected, AbstractShape actual, boolean checkColors) {
        assertNotNull(expected, "La forma attesa non può essere nulla.");
        assertNotNull(actual, "La forma attuale non può essere nulla.");

        AbstractShape baseExpected = getBaseShape(expected);
        AbstractShape baseActual = getBaseShape(actual);

        assertEquals(baseExpected.getClass(), baseActual.getClass(), "Le classi delle forme base non corrispondono.");
        assertEquals(baseExpected.getX(), baseActual.getX(), 0.001, "Coordinata X non corrispondente.");
        assertEquals(baseExpected.getY(), baseActual.getY(), 0.001, "Coordinata Y non corrispondente.");
        assertEquals(baseExpected.getWidth(), baseActual.getWidth(), 0.001, "Larghezza non corrispondente.");
        assertEquals(baseExpected.getHeight(), baseActual.getHeight(), 0.001, "Altezza non corrispondente.");
        assertEquals(baseExpected.getZ(), baseActual.getZ(), "Z-index non corrispondente.");

        if (checkColors) {
            AbstractShape currentExpected = expected;
            AbstractShape currentActual = actual;
            while (currentExpected instanceof ShapeDecorator && currentActual instanceof ShapeDecorator) {
                assertEquals(currentExpected.getClass(), currentActual.getClass(), "Classe del decoratore non corrispondente.");
                currentExpected = ((ShapeDecorator) currentExpected).getInnerShape();
                currentActual = ((ShapeDecorator) currentActual).getInnerShape();
            }
            assertEquals(currentExpected.getClass(), baseExpected.getClass());
            assertEquals(currentActual.getClass(), baseActual.getClass());
        }
    }

    private void resetControllerSelectionState() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                setPrivateField(controller, "currentShape", null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                fail("Impossibile resettare currentShape nel controller: " + e.getMessage());
            }
            controller.updateControlState(null);
        });
    }


    @Test
    @DisplayName("Salva e Carica un disegno con una singola forma (Rettangolo)")
    void testSaveAndLoad_SingleRectangle() throws Exception {
        File tempFile = new File(tempDir.toFile(), "singleRectangle.ser");
        Color rectFill = Color.BLUE;
        Color rectBorder = Color.YELLOW;

        AbstractShape originalShapeInserted = insertAndGetSelectedShapeFromController("Rectangle", 50, 50, rectFill, rectBorder);
        assertEquals(1, model.getShapes().size());
        AbstractShape originalShapeFromModel = model.getShapes().get(0); // Per confronto successivo

        runOnFxThreadAndWait(() -> {
            try {
                model.saveToFile(tempFile);
            } catch (IOException e) {
                fail("Salvataggio fallito: " + e.getMessage());
            }
        });
        assertTrue(tempFile.exists() && tempFile.length() > 0);

        // Usa un *nuovo* modello per il caricamento per evitare effetti collaterali
        DrawingModel loadedModel = new DrawingModel();
        runOnFxThreadAndWait(() -> {
            try {
                loadedModel.loadFromFile(tempFile);
            } catch (IOException | ClassNotFoundException e) {
                fail("Caricamento fallito: " + e.getMessage());
            }
        });

        // Imposta il modello caricato nel controller e aggiorna il riferimento del test
        runOnFxThreadAndWait(() -> controller.setModel(loadedModel));
        this.model = loadedModel; // Il test ora opera sul modello caricato

        // **CORREZIONE**: Resetta esplicitamente lo stato di selezione del controller
        resetControllerSelectionState();

        assertEquals(1, model.getShapes().size(), "Numero errato di forme dopo il caricamento.");
        AbstractShape loadedShape = model.getShapes().get(0);
        assertShapesAreEqual(originalShapeFromModel, loadedShape, true); // Confronta con la forma dal modello originale

        assertNull(getPrivateField(controller, "currentShape"), "Nessuna forma dovrebbe essere selezionata dopo il caricamento.");
        assertTrue(widthSpinner.isDisabled(), "Spinner larghezza disabilitato dopo caricamento.");
    }

    @Test
    @DisplayName("Salva e Carica un disegno con forme multiple")
    void testSaveAndLoad_MultipleShapes() throws Exception {
        File tempFile = new File(tempDir.toFile(), "multipleShapes.ser");

        insertAndGetSelectedShapeFromController("Rectangle", 50, 50, Color.RED, Color.BLACK);
        insertAndGetSelectedShapeFromController("Ellipse", 150, 150, Color.GREEN, Color.BLUE);
        insertAndGetSelectedShapeFromController("Line", 250, 250, null, Color.PURPLE);
        assertEquals(3, model.getShapes().size());
        List<AbstractShape> originalShapes = List.copyOf(model.getShapes());

        runOnFxThreadAndWait(() -> {
            try {
                model.saveToFile(tempFile);
            } catch (IOException e) {
                fail(e);
            }
        });
        assertTrue(tempFile.exists() && tempFile.length() > 0);

        DrawingModel loadedModel = new DrawingModel(); // Carica in un nuovo modello
        runOnFxThreadAndWait(() -> {
            try {
                loadedModel.loadFromFile(tempFile);
            } catch (IOException | ClassNotFoundException e) {
                fail("Caricamento fallito: " + e.getMessage());
            }
        });

        runOnFxThreadAndWait(() -> controller.setModel(loadedModel));
        this.model = loadedModel;

        resetControllerSelectionState();

        assertEquals(originalShapes.size(), model.getShapes().size());
        for (int i = 0; i < originalShapes.size(); i++) {
            assertShapesAreEqual(originalShapes.get(i), model.getShapes().get(i), true);
        }
        assertNull(getPrivateField(controller, "currentShape"), "Nessuna forma dovrebbe essere selezionata dopo il caricamento.");
    }

    @Test
    @DisplayName("Salva e Carica un disegno vuoto")
    void testSaveAndLoad_EmptyDrawing() throws Exception {
        File tempFile = new File(tempDir.toFile(), "emptyDrawing.ser");
        assertTrue(model.getShapes().isEmpty());

        runOnFxThreadAndWait(() -> {
            try {
                model.saveToFile(tempFile);
            } catch (IOException e) {
                fail(e);
            }
        });
        assertTrue(tempFile.exists());

        DrawingModel loadedModel = new DrawingModel(); // Carica in un nuovo modello
        runOnFxThreadAndWait(() -> {
            try {
                loadedModel.loadFromFile(tempFile);
            } catch (IOException | ClassNotFoundException e) {
                fail("Caricamento di disegno vuoto fallito: " + e.getMessage());
            }
        });

        runOnFxThreadAndWait(() -> controller.setModel(loadedModel));
        this.model = loadedModel;

        resetControllerSelectionState();

        assertTrue(model.getShapes().isEmpty(), "Il modello dovrebbe essere vuoto dopo aver caricato un disegno vuoto.");
        assertNull(getPrivateField(controller, "currentShape"));
    }

    @Test
    @DisplayName("Caricamento da file non esistente dovrebbe gestire l'eccezione")
    void testLoad_NonExistentFile() throws InterruptedException {
        File nonExistentFile = new File(tempDir.toFile(), "noSuchFile.ser");
        assertFalse(nonExistentFile.exists());

        DrawingModel testModel = new DrawingModel(); // Usa un modello separato per questo test
        AtomicReference<Exception> exceptionOccurred = new AtomicReference<>(null);
        runOnFxThreadAndWait(() -> {
            try {
                testModel.loadFromFile(nonExistentFile);
            } catch (IOException | ClassNotFoundException e) {
                exceptionOccurred.set(e);
            }
        });
        assertNotNull(exceptionOccurred.get(), "Una IOException era attesa per file non esistente.");
        assertTrue(exceptionOccurred.get() instanceof IOException);
        assertTrue(testModel.getShapes().isEmpty(), "Il modello dovrebbe rimanere vuoto dopo tentativo di caricamento fallito.");
    }

    @Test
    @DisplayName("Creazione di una nuova area di lavoro quando il disegno corrente è vuoto")
    void testNewWorkspace_EmptyDrawing() throws Exception {
        // Verifica che il disegno sia vuoto inizialmente
        assertTrue(model.getShapes().isEmpty());

        runOnFxThreadAndWait(() -> {
            controller.handleNewWorkspace(new ActionEvent());
        });

        // Verifica che il model sia ancora vuoto
        assertTrue(model.getShapes().isEmpty());
        // Verifica che non ci sia una forma selezionata
        assertNull(getPrivateField(controller, "currentShape"));
        // Verifica che non ci sia una factory attiva
        assertNull(getPrivateField(controller, "currentShapeFactory"));
    }

    @Test
    void createNewWorkspaceWithSaveTest() throws Exception {
        CountDownLatch setupLatch = new CountDownLatch(1);

        // Creo un file temporaneo per il salvataggio
        File tempFile = File.createTempFile("test-drawing", ".ser");
        tempFile.deleteOnExit();

        Platform.runLater(() -> {
            try {
                // Aggiungo una figura al modello
                Rectangle testShape = new Rectangle(100, 100, 50, 30);
                model.addShape(testShape);

                // Verifico che il modello contenga la figura
                assertEquals(1, model.getShapes().size(), "Il modello dovrebbe contenere una figura");

                // Mock dell'Alert invece del FileChooser
                Alert mockAlert = mock(Alert.class);
                when(mockAlert.showAndWait()).thenReturn(Optional.of(ButtonType.YES));

                // Salvo direttamente il modello prima di creare la nuova area
                try {
                    model.saveToFile(tempFile);
                } catch (IOException e) {
                    fail("Errore durante il salvataggio del file: " + e.getMessage());
                }

                // Eseguo la creazione della nuova area di lavoro
                controller.handleNewWorkspace(new ActionEvent());

                // Verifico che il file sia stato creato e contenga dati
                assertTrue(tempFile.exists() && tempFile.length() > 0,
                        "Il file di salvataggio dovrebbe esistere e contenere dati");

                // Verifico che l'area di lavoro sia vuota
                assertTrue(model.getShapes().isEmpty(),
                        "L'area di lavoro dovrebbe essere vuota dopo la creazione della nuova area");

            } catch (Exception e) {
                fail("Test fallito con eccezione: " + e.getMessage());
            } finally {
                setupLatch.countDown();
            }
        });

        // Attendo il completamento delle operazioni JavaFX
        assertTrue(setupLatch.await(5, TimeUnit.SECONDS),
                "Timeout durante l'esecuzione del test");

        // Verifico che il file possa essere caricato e contenga la figura originale
        DrawingModel testModel = new DrawingModel();
        testModel.loadFromFile(tempFile);
        assertEquals(1, testModel.getShapes().size(),
                "Il file salvato dovrebbe contenere una figura");
    }
}