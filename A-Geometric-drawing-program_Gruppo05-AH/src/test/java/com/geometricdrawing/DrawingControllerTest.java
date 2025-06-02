package com.geometricdrawing;

import com.geometricdrawing.command.CommandManager;
import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle;
import com.geometricdrawing.mousehandler.MousePressedHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.Spinner;

class DrawingControllerTest {

    private static volatile boolean fxInitialized = false;
    private DrawingController controller;
    private DrawingModel model;
    private CommandManager commandManager;
    private ColorPicker fillPicker;
    private ColorPicker borderPicker;
    private Spinner<Double> heightSpinner;
    private Spinner<Double> widthSpinner;
    private Button deleteButton;
    private Canvas canvas;

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
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new InterruptedException("Timeout: JavaFX Toolkit non inizializzato.");
            }
        } catch (IllegalStateException e) {
            // Se il toolkit è già inizializzato imposto il flag
            fxInitialized = true;
        }
    }

    private Object getPrivateField(DrawingController controller, String currentShape) {
        try {
            Field field = DrawingController.class.getDeclaredField(currentShape);
            field.setAccessible(true);
            return field.get(controller);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        CountDownLatch setupLatch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                controller = new DrawingController();
                model = new DrawingModel();
                commandManager = new CommandManager();

                fillPicker = new ColorPicker();
                borderPicker = new ColorPicker();
                heightSpinner = new Spinner<>(1.0, 1000.0, 40.0);
                widthSpinner = new Spinner<>(1.0, 1000.0, 60.0);
                deleteButton = new Button();
                this.canvas = new Canvas(800, 600);
                Pane canvasContainer = new AnchorPane();
                AnchorPane rootPane = new AnchorPane();

                // Imposta i campi privati prima di inizializzare
                setPrivateField("model", model);
                setPrivateField("commandManager", commandManager);
                setPrivateField("fillPicker", fillPicker);
                setPrivateField("borderPicker", borderPicker);
                setPrivateField("heightSpinner", heightSpinner);
                setPrivateField("widthSpinner", widthSpinner);
                setPrivateField("deleteButton", deleteButton);
                setPrivateField("drawingCanvas", canvas);
                setPrivateField("canvasContainer", canvasContainer);
                setPrivateField("rootPane", rootPane);
                if (controller.getClass().getDeclaredField("gc") != null) {
                    Field gcField = controller.getClass().getDeclaredField("gc");
                    gcField.setAccessible(true);
                    gcField.set(controller, canvas.getGraphicsContext2D());
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                setupLatch.countDown();
            }
        });

        if (!setupLatch.await(10, TimeUnit.SECONDS)) { // Aumentato timeout
            throw new InterruptedException("Timeout durante il setup.");
        }
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = DrawingController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @Test
    void handleCanvasClickShouldSelectExistingShape() throws Exception {
        DrawingModel model = new DrawingModel();
        controller.setModel(model);
        controller.setCommandManager(new CommandManager());

        // crep e aggiungo una figura al model
        Rectangle existingShape = new Rectangle(100, 100, 50, 30);
        CountDownLatch addShapeLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            model.addShape(existingShape);
            addShapeLatch.countDown();
        });
        addShapeLatch.await(5, TimeUnit.SECONDS);

        // evento di click sulla figura
        MouseEvent clickEvent = new MouseEvent(MouseEvent.MOUSE_PRESSED,
                125, 115, // Coordinate all'interno della figura
                0, 0, MouseButton.PRIMARY, 1,
                false, false, false, false,
                true, false, false, false, false,
                true, null);

        MousePressedHandler handler = new MousePressedHandler(controller.getDrawingCanvas(), controller);
        // simulo mousePressed sulla figura usando il metodo privato handleMousePressed
        CountDownLatch pressedLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            handler.handleMouseEvent(clickEvent);
            pressedLatch.countDown();
        });
        pressedLatch.await(5, TimeUnit.SECONDS);

        Thread.sleep(100);

        // Verifica che la figura sia stata selezionata
        Platform.runLater(() -> {
            AbstractShape selectedShape = (AbstractShape) getPrivateField(controller, "currentShape");
            assertNotNull(selectedShape, "Una figura dovrebbe essere selezionata");
            assertEquals(existingShape, selectedShape, "La figura selezionata dovrebbe essere quella esistente");

            // Verifica lo stato dei controlli UI
            assertFalse(widthSpinner.isDisabled(), "Width spinner dovrebbe essere abilitato");
            assertFalse(heightSpinner.isDisabled(), "Height spinner dovrebbe essere abilitato");
            assertFalse(fillPicker.isDisabled(), "Fill picker dovrebbe essere abilitato");
            assertFalse(borderPicker.isDisabled(), "Border picker dovrebbe essere abilitato");
            assertFalse(deleteButton.isDisabled(), "Delete button dovrebbe essere abilitato");

            // Verifica che gli spinner mostrino le dimensioni corrette
            assertEquals(existingShape.getWidth(), widthSpinner.getValue(),
                    "Width spinner dovrebbe mostrare la larghezza corretta");
            assertEquals(existingShape.getHeight(), heightSpinner.getValue(),
                    "Height spinner dovrebbe mostrare l'altezza corretta");
        });
    }

    // Se il model è null, il metodo dovrebbe salvare comunque un file (potenzialmente vuoto) senza lanciare eccezioni.
    @Test
    void handleSaveSerialized_ModelIsNull() throws Exception {
        setPrivateField("model", null);
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> controller.handleSaveSerialized(new ActionEvent()));
        });
    }

    // Se il model è inizialmente null, il metodo dovrebbe inizializzarne uno nuovo e vuoto durante il caricamento.
    @Test
    void handleLoadSerialized_WhenModelIsInitiallyNull_ShouldCreateNewModel() throws Exception {
        setPrivateField("model", null);
        Platform.runLater(() -> {
            controller.handleLoadSerialized(new ActionEvent());

            DrawingModel currentModel = (DrawingModel) getPrivateField(controller, "model");
            assertNotNull(currentModel, "Model should have been initialized");
            assertTrue(currentModel.getShapes().isEmpty(), "Newly initialized model should be empty");
        });
    }

    // Quando il canvas è vuoto, il metodo dovrebbe comunque permettere il salvataggio in PNG senza lanciare eccezioni.
    @Test
    void handleSaveAsPng_CanvasIsEmpty() throws Exception {
        setPrivateField("drawingCanvas", new Canvas(600, 600));
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> controller.handleSaveAsPng(new ActionEvent()));
        });
    }
    
    // Quando il canvas è vuoto, il metodo dovrebbe comunque permettere il salvataggio in PDF senza lanciare eccezioni.
    @Test
    void handleSaveAsPdf_CanvasIsEmpty() throws Exception {
        setPrivateField("drawingCanvas", new Canvas(600, 600));
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> controller.handleSaveAsPdf(new ActionEvent()));
        });
    }
}