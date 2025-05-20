package com.geometricdrawing.command;

import com.geometricdrawing.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Per usare @Mock
class DeleteShapeCommandTest {

    @Mock
    private DrawingModel mockDrawingModel; // Mock del receiver

    @Mock
    private AbstractShape mockShapeToRemove; // Mock della forma da rimuovere

    @Test
    void constructorShouldDeleteShape() {
        DeleteShapeCommand command = new DeleteShapeCommand(mockDrawingModel, mockShapeToRemove);
        assertNotNull(command, "L'oggetto commando NON dovrebbe essere null");
    }

    @Test
    void executeShouldCallAddShapeOnModelWithRectangleShape() {
        // Il metodo testa che venga richiamato removeShape del model
        AbstractShape shapeToDeleteRec = new Rectangle(10, 10, 50, 50);
        DeleteShapeCommand deleteCommand = new DeleteShapeCommand(mockDrawingModel, shapeToDeleteRec);

        deleteCommand.execute();

        // il metodo nel DrawingModel deve essere chiamato una volta e deve rimuovere la figura corretta
        verify(mockDrawingModel, times(1)).removeShape(shapeToDeleteRec);
    }

    @Test
    void executeShouldCallAddShapeOnModelWithEllipseShape() {
        // Il metodo testa che venga richiamato removeShape del model
        AbstractShape shapeToDeleteEll = new Ellipse(10, 10, 50, 50);
        DeleteShapeCommand deleteCommand = new DeleteShapeCommand(mockDrawingModel, shapeToDeleteEll);

        deleteCommand.execute();

        // il metodo nel DrawingModel deve essere chiamato una volta e deve rimuovere la figura corretta
        verify(mockDrawingModel, times(1)).removeShape(shapeToDeleteEll);
    }

    @Test
    void executeShouldCallAddShapeOnModelWithLineShape() {
        // Il metodo testa che venga richiamato removeShape del model
        AbstractShape shapeToDeleteLine = new Line(10 , 10, 50, 50);
        DeleteShapeCommand deleteCommand = new DeleteShapeCommand(mockDrawingModel, shapeToDeleteLine);

        deleteCommand.execute();

        // il metodo nel DrawingModel deve essere chiamato una volta e deve rimuovere la figura corretta
        verify(mockDrawingModel, times(1)).removeShape(shapeToDeleteLine);
    }

    @Test
    void executeWithNullShapeShouldCallRemoveShapeWithNull() {
        // Questo test verifica come il comando si comporta se, per qualche motivo,
        // gli viene passata una forma null.

        DeleteShapeCommand command = new DeleteShapeCommand(mockDrawingModel, null);

        command.execute();

        verify(mockDrawingModel, times(1)).removeShape(null);
    }
}