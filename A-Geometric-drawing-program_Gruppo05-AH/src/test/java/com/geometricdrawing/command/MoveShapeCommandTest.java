package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle;
import com.geometricdrawing.model.Line;
import com.geometricdrawing.model.Ellipse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MoveShapeCommandTest {

    @Mock
    private DrawingModel mockDrawingModel;

    @Test
    void executeShouldCallMoveShapeToWithLineShape() {
        AbstractShape shapeToMove = new Line(100, 100, 200, 200);
        double newX = 50.0;
        double newY = 60.0;
        MoveShapeCommand command = new MoveShapeCommand(mockDrawingModel, shapeToMove, newX, newY);

        command.execute();

        verify(mockDrawingModel, times(1)).moveShapeTo(shapeToMove, newX, newY);
    }

    @Test
    void executeShouldCallMoveShapeToWithEllipseShape() {
        AbstractShape shapeToMove = new Ellipse(120, 120, 80, 40);
        double newX = 70.0;
        double newY = 80.0;
        MoveShapeCommand command = new MoveShapeCommand(mockDrawingModel, shapeToMove, newX, newY);

        command.execute();

        verify(mockDrawingModel, times(1)).moveShapeTo(shapeToMove, newX, newY);
    }


    @Test
    void executeWithNullShapeShouldCallMoveShapeToWithNull() {
        // verifica come il command si comporta se, per qualche motivo,
        // gli viene passata una forma null.
        MoveShapeCommand command = new MoveShapeCommand(mockDrawingModel, null, 30.0, 40.0);

        command.execute();

        verify(mockDrawingModel, times(1)).moveShapeTo(null, 30.0, 40.0);
    }

}
