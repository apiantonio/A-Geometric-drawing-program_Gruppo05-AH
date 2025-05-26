package com.geometricdrawing;

import com.geometricdrawing.model.DrawingModel;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.*;

class NewWorkspaceTest {

    private DrawingController drawingController;
    private NewWorkspace newWorkspace;

    @BeforeAll
    static void initJavaFX() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                new JFXPanel(); // Inizializza il toolkit JavaFX
            });
        } catch (InterruptedException | InvocationTargetException e) {
            fail("Impossibile inizializzare il toolkit JavaFX: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        drawingController = new DrawingController();
        drawingController.setModel(new DrawingModel());
        newWorkspace = new NewWorkspace(drawingController);
    }


    @Test
    void getDrawingControllerShouldReturnCorrectController() {
        assertSame(drawingController, newWorkspace.getDrawingController(),
                "getDrawingController dovrebbe restituire il controller impostato nel costruttore");
    }

    @Test
    void createNewWorkspaceShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            newWorkspace.createNewWorkspace();
        }, "createNewWorkspace non dovrebbe lanciare eccezioni");
    }

    @Test
    void createNewWorkspaceShouldClearModel() {
        newWorkspace.createNewWorkspace();
        assertTrue(drawingController.getModel().getShapes().isEmpty(),
                "Il modello dovrebbe essere vuoto dopo createNewWorkspace");
    }

    @Test
    void createNewWorkspaceShouldResetCurrentShape() {
        newWorkspace.createNewWorkspace();
        assertNull(drawingController.getCurrentShape(),
                "La figura corrente dovrebbe essere null dopo createNewWorkspace");
    }

    @Test
    void createNewWorkspaceShouldResetShapeFactory() {
        newWorkspace.createNewWorkspace();
        assertNull(drawingController.getCurrentShapeFactory(),
                "La factory corrente dovrebbe essere null dopo createNewWorkspace");
    }
}