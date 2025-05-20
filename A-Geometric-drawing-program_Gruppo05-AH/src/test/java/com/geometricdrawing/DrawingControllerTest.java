package com.geometricdrawing;

import com.geometricdrawing.command.CommandManager;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
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


    @BeforeAll
    static void initFX() throws InterruptedException {
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
                Canvas canvas = new Canvas();
                Pane canvasContainer = new Pane();
                AnchorPane rootPane = new AnchorPane();

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

                // Inizializza il controller
                controller.initialize();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                setupLatch.countDown();
            }
        });

        if (!setupLatch.await(5, TimeUnit.SECONDS)) {
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

        // simulo mousePressed sulla figura usando il metodo privato handleMousePressed
        CountDownLatch pressedLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
           try {
               java.lang.reflect.Method method = DrawingController.class.getDeclaredMethod("handleMousePressed", MouseEvent.class);
               method.setAccessible(true);
               method.invoke(controller, clickEvent);
           } catch (Exception e) {
               e.printStackTrace();
           }
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
            assertTrue(fillPicker.isDisabled(), "Fill picker dovrebbe essere disabilitato (MOMENTANEE)");
            assertTrue(borderPicker.isDisabled(), "Border picker dovrebbe essere disabilitato (MOMENTANEE)");
            assertFalse(deleteButton.isDisabled(), "Delete button dovrebbe essere abilitato");

            // Verifica che gli spinner mostrino le dimensioni corrette
            assertEquals(existingShape.getWidth(), widthSpinner.getValue(),
                    "Width spinner dovrebbe mostrare la larghezza corretta");
            assertEquals(existingShape.getHeight(), heightSpinner.getValue(),
                    "Height spinner dovrebbe mostrare l'altezza corretta");
        });
    }
}