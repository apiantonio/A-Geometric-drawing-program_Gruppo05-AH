package com.geometricdrawing.command;

import com.geometricdrawing.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeleteShapeCommandTest {

    private DrawingModel drawingModel;
    private AbstractShape shapeToRemove;

    @BeforeEach
    void setUp() {
        drawingModel = new DrawingModel(); // Init del modello di disegno
        shapeToRemove = new Rectangle(10, 10, 50, 50);
        drawingModel.addShape(shapeToRemove);

        assertEquals(1, drawingModel.getShapes().size());
    }

    @Test
    void executeShouldRemoveShapeFromModel() {
        DeleteShapeCommand deleteCommand = new DeleteShapeCommand(drawingModel, shapeToRemove);

        deleteCommand.execute();

        // La shape deve essere rimossa dal modello
        assertEquals(0, drawingModel.getShapes().size());
        assertFalse(drawingModel.getShapes().contains(shapeToRemove));
    }

    @Test
    void executeWithNullShapeShouldNotThrow() {
        // Verifica comportamento con una shape null
        shapeToRemove = null;
        DeleteShapeCommand command = new DeleteShapeCommand(drawingModel, shapeToRemove);

        command.execute();

        // Non deve lanciare eccezioni, e lo stato del modello deve rimanere invariato
        assertEquals(1, drawingModel.getShapes().size());
    }

    @Test
    void undoShouldReinsertShapeIntoModel() {
        DeleteShapeCommand command = new DeleteShapeCommand(drawingModel, shapeToRemove);
        command.execute();

        assertEquals(0, drawingModel.getShapes().size());

        command.undo();

        assertEquals(1, drawingModel.getShapes().size());
        assertTrue(drawingModel.getShapes().contains(shapeToRemove));
    }
}
