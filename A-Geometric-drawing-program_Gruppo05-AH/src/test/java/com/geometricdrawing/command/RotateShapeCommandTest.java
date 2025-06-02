package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RotateShapeCommandTest {

    private DrawingModel drawingModel;
    private AbstractShape shapeToRotate;
    private double deltaAngle;
    private double initialAngle;

    @BeforeEach
    void setUp() {
        drawingModel = new DrawingModel();
        shapeToRotate = new Rectangle(10, 10, 50, 50);
        deltaAngle = 45.0;
        initialAngle = shapeToRotate.getRotationAngle();
        drawingModel.addShape(shapeToRotate);
    }

    @Test
    void executeShouldRotateShapeByDeltaAngle() {
        RotateShapeCommand command = new RotateShapeCommand(drawingModel, shapeToRotate, deltaAngle);

        command.execute();

        assertEquals(initialAngle + deltaAngle, shapeToRotate.getRotationAngle(),
                "La forma dovrebbe essere ruotata dell'angolo specificato");
    }

    @Test
    void undoShouldRestoreOriginalAngle() {
        RotateShapeCommand command = new RotateShapeCommand(drawingModel, shapeToRotate, deltaAngle);
        command.execute();

        assertEquals(initialAngle + deltaAngle, shapeToRotate.getRotationAngle());

        command.undo();

        assertEquals(initialAngle, shapeToRotate.getRotationAngle(),
                "L'angolo originale dovrebbe essere ripristinato dopo l'undo");
    }

    @Test
    void executeWithNullShapeShouldNotThrow() {
        RotateShapeCommand command = new RotateShapeCommand(drawingModel, null, deltaAngle);

        assertDoesNotThrow(command::execute,
                "Il comando non dovrebbe lanciare eccezioni se la shape è null");
    }

    @Test
    void executeWithNullModelShouldNotThrow() {
        RotateShapeCommand command = new RotateShapeCommand(null, shapeToRotate, deltaAngle);

        assertThrows(NullPointerException.class, command::execute,
                "Il comando dovrebbe lanciare NullPointerException se il model è null");
    }

    @Test
    void executeWithNegativeAngleShouldRotateCorrectly() {
        double negativeAngle = -90.0;
        RotateShapeCommand command = new RotateShapeCommand(drawingModel, shapeToRotate, negativeAngle);

        command.execute();

        assertEquals(initialAngle + negativeAngle, shapeToRotate.getRotationAngle(),
                "La forma dovrebbe essere ruotata anche con angoli negativi");
    }

    @Test
    void multipleExecutionsAndUndosShouldWorkCorrectly() {
        RotateShapeCommand command = new RotateShapeCommand(drawingModel, shapeToRotate, deltaAngle);

        // Prima esecuzione
        command.execute();
        assertEquals(initialAngle + deltaAngle, shapeToRotate.getRotationAngle());

        // Seconda esecuzione
        command.execute();
        assertEquals(initialAngle + (2 * deltaAngle), shapeToRotate.getRotationAngle());

        // Primo undo
        command.undo();
        assertEquals(initialAngle + deltaAngle, shapeToRotate.getRotationAngle());

        // Secondo undo
        command.undo();
        assertEquals(initialAngle, shapeToRotate.getRotationAngle());
    }
}