package com.geometricdrawing.command;

import com.geometricdrawing.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Per usare @Mock
class DeleteShapeCommandTest {

    @Mock
    private DrawingModel mockDrawingModel; // Mock del receiver

    private AbstractShape shapeToRemove;

    @BeforeEach
    void setUp() {
        // Inizializza la forma da rimuovere, ad esempio un rettangolo
        shapeToRemove = new Rectangle(10, 10, 50, 50);
        mockDrawingModel.addShape(shapeToRemove);
        assertEquals(1, mockDrawingModel.getShapes().size());
    }

    @Test
    void executeShouldCallRemoveShapeOnModelWithRectangleShape() {
        DeleteShapeCommand deleteCommand = new DeleteShapeCommand(mockDrawingModel, shapeToRemove);

        deleteCommand.execute();

        // il metodo nel DrawingModel deve essere chiamato una volta e deve rimuovere la figura corretta
        verify(mockDrawingModel, times(1)).removeShape(shapeToRemove);
        assertEquals(0, mockDrawingModel.getShapes().size());   // verifica che non ci siano forme nel modello
    }

    @Test
    void executeWithNullShapeShouldCallRemoveShapeWithNull() {
        // Questo test verifica come il comando si comporta se, per qualche motivo,
        // gli viene passata una forma null.
        shapeToRemove = null;
        DeleteShapeCommand command = new DeleteShapeCommand(mockDrawingModel, shapeToRemove);

        command.execute();

        verify(mockDrawingModel, times(1)).removeShape(null);
        assertEquals(0, mockDrawingModel.getShapes().size());   // verifica che non ci siano forme nel modello
    }

    @Test
    void insertCorrectShapeInTheModelWithUndo() {
        DeleteShapeCommand command = new DeleteShapeCommand(mockDrawingModel, shapeToRemove);
        command.execute();

        verify(mockDrawingModel, times(1)).removeShape(shapeToRemove);
        assertEquals(0, mockDrawingModel.getShapes().size());   // verifica che non ci siano forme nel modello

        command.undo();
        assertEquals(1, mockDrawingModel.getShapes().size());   // verifica che la forma sia stata reinserita nel modello
        assertTrue(mockDrawingModel.getShapes().contains(shapeToRemove), "La forma rimossa in precedenza dovrebbe essere presente nel model post undo");
    }
}