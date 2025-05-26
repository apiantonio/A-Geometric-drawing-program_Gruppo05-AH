package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Line;
import com.geometricdrawing.model.Ellipse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveShapeCommandTest {

    private DrawingModel drawingModel;
    private double newX;
    private double newY;

    @BeforeEach
    void setUp() {
        newX = 50.0;
        newY = 60.0;
    }

    @Test
    void executeShouldCallMoveShapeToWithLineShape() {
        AbstractShape shapeToMove = new Line(100, 100, 200, 200);
        MoveShapeCommand command = new MoveShapeCommand(drawingModel, shapeToMove, newX, newY);

        command.execute();

        assertEquals(shapeToMove.getX(), newX);
        assertEquals(shapeToMove.getY(), newY);
    }

    @Test
    void executeShouldCallMoveShapeToWithEllipseShape() {
        AbstractShape shapeToMove = new Ellipse(120, 120, 80, 40);
        MoveShapeCommand command = new MoveShapeCommand(drawingModel, shapeToMove, newX, newY);

        command.execute();

        assertEquals(shapeToMove.getX(), newX);
        assertEquals(shapeToMove.getY(), newY);
    }


    @Test
    void executeWithNullShapeShouldCallMoveShapeToWithNull() {
        // verifica come il command si comporta se, per qualche motivo,
        // gli viene passata una forma null.
        MoveShapeCommand command = new MoveShapeCommand(drawingModel, null, 30.0, 40.0);

        command.execute();

        assertDoesNotThrow(command::execute, "Il comando non dovrebbe lanciare eccezioni se la shape è null");
    }

    @Test
    void undoShouldResetShapePosition() {
        AbstractShape shapeToMove = new Line(100, 100, 200, 200);
        MoveShapeCommand command = new MoveShapeCommand(drawingModel, shapeToMove, newX, newY);

        // Salva le coordinate originali
        double originalX = shapeToMove.getX();
        double originalY = shapeToMove.getY();

        command.execute();

        // Verifica che le coordinate siano state aggiornate
        assertEquals(newX, shapeToMove.getX());
        assertEquals(newY, shapeToMove.getY());

        // Esegui l'undo
        command.undo();

        // Verifica che le coordinate siano tornate a quelle originali
        assertEquals(originalX, shapeToMove.getX());
        assertEquals(originalY, shapeToMove.getY());
    }

}
