package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle; // Usa una forma concreta per il test
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChangeWidthCommandTest {

    @Mock
    private DrawingModel mockDrawingModel;

    @Test
    void executeShouldCallSetShapeWidthOnModel() {
        AbstractShape shapeToResize = new Rectangle(10, 10, 50, 50);
        double newWidth = 75.0;
        ChangeWidthCommand command = new ChangeWidthCommand(mockDrawingModel, shapeToResize, newWidth);

        command.execute();

        // Verifica che il metodo setShapeWidth del DrawingModel sia stato chiamato
        // esattamente una volta con la forma e la nuova larghezza corrette.
        verify(mockDrawingModel, times(1)).setShapeWidth(shapeToResize, newWidth);
    }
}