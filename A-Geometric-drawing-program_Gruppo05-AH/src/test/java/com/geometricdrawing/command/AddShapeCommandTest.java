package com.geometricdrawing.command;

import com.geometricdrawing.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddShapeCommandTest {

    private DrawingModel drawingModel;

    @BeforeEach
    void setUp() {
        drawingModel = new DrawingModel(); // Usa la vera implementazione
    }

    @Test
    void constructorShouldStoreModelAndShape() {
        AbstractShape shape = new Rectangle(10, 10, 50, 50);
        AddShapeCommand command = new AddShapeCommand(drawingModel, shape);
        assertNotNull(command); // Verifica che il comando sia stato istanziato correttamente
    }

    @Test
    void executeShouldAddShapeToModel() {
        AbstractShape rectangle = new Rectangle(10, 10, 50, 50);
        AddShapeCommand command = new AddShapeCommand(drawingModel, rectangle);

        command.execute();

        assertEquals(1, drawingModel.getShapes().size());
        assertTrue(drawingModel.getShapes().contains(rectangle));
    }

    @Test
    void executeWithNullShapeShouldNotThrow() {
        AddShapeCommand command = new AddShapeCommand(drawingModel, null);

        assertDoesNotThrow(command::execute);
        assertEquals(0, drawingModel.getShapes().size(), "Nessuna forma dovrebbe essere aggiunta al modello");
    }

    @Test
    void undoShouldRemoveShapeFromModel() {
        AbstractShape ellipse = new Ellipse(30, 30, 30, 50);
        AddShapeCommand command = new AddShapeCommand(drawingModel, ellipse);

        command.execute();
        assertEquals(1, drawingModel.getShapes().size());
        assertTrue(drawingModel.getShapes().contains(ellipse));

        command.undo();
        assertEquals(0, drawingModel.getShapes().size());
        assertFalse(drawingModel.getShapes().contains(ellipse));
    }
}
