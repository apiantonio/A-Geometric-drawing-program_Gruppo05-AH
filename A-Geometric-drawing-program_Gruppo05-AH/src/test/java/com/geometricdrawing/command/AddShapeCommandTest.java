package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Ellipse;
import com.geometricdrawing.model.Rectangle; // Una forma concreta per il test
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class) // Per usare @Mock
class AddShapeCommandTest {

    @Mock
    private DrawingModel mockDrawingModel; // Mock del receiver

    @Mock
    private AbstractShape mockShapeToAdd; // Mock della forma da aggiungere

    @Test
    void constructorShouldStoreModelAndShape() {
        AddShapeCommand command = new AddShapeCommand(mockDrawingModel, mockShapeToAdd);
        assertNotNull(command); // Verifica base che l'oggetto sia creato
    }

    @Test
    void executeShouldCallAddShapeOnModelWithCorrectShape() {
        AbstractShape rectangle = new Rectangle(10, 10, 50, 50);
        AddShapeCommand command = new AddShapeCommand(mockDrawingModel, rectangle);

        command.execute();

        // Verifica che il metodo addShape del DrawingModel sia stato chiamato esattamente una volta
        // e che sia stato chiamato con l'oggetto 'rectangle' corretto.
        verify(mockDrawingModel, times(1)).addShape(rectangle);
    }

    @Test
    void executeWithNullShapeShouldCallAddShapeWithNull() {
        // Questo test verifica come il comando si comporta se, per qualche motivo,
        // gli viene passata una forma null.

        AddShapeCommand command = new AddShapeCommand(mockDrawingModel, null);

        command.execute();

        verify(mockDrawingModel, times(1)).addShape(null);
    }

    @Test
    void removeCorrectShapeFromModelWithUndo() {
        // Le prime 5 righe di codice sono di inserimento della figura - abbiamo già testato che funziona correttamente
        AbstractShape ellipse = new Ellipse(30, 30, 30, 50);
        AddShapeCommand command = new AddShapeCommand(mockDrawingModel, ellipse);
        command.execute();
        verify(mockDrawingModel, times(1)).addShape(ellipse); // anche se ridondante si decide di mantenere per chiarezza

        command.undo();

        assertNull(mockDrawingModel.getShapes(), "La forma non dovrebbe essere presente nel model post undo");
    }
}