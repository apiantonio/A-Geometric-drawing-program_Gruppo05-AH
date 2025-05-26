package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle; // Usa una forma concreta
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChangeHeightCommandTest {

    @Mock
    private DrawingModel mockDrawingModel;

    private AbstractShape shapeToResize;
    private double newHeight;

    @BeforeEach
    public void setUp() {
        shapeToResize = new Rectangle(10, 10, 50, 50);
        newHeight = 60.0;
    }

    @Test
    void executeShouldCallSetShapeHeightOnModel() {
        ChangeHeightCommand command = new ChangeHeightCommand(mockDrawingModel, shapeToResize, newHeight);

        command.execute();

        verify(mockDrawingModel, times(1)).setShapeHeight(shapeToResize, newHeight);
        // verifica che l'altezza sia stata cambiata correttamente
        assertEquals(60.0, shapeToResize.getHeight());
    }

    @Test
    void resetOldHeightShapeWithUndo() {
        ChangeHeightCommand command = new ChangeHeightCommand(mockDrawingModel, shapeToResize, newHeight);
        command.execute();

        // Effettua l'undo per il ripristino dell'altezza di partenza
        command.undo();

        // Verifica che il metodo setShapeHeight sia stato chiamato con l'altezza originale
        verify(mockDrawingModel, times(1)).setShapeHeight(shapeToResize, shapeToResize.getHeight());
        // Verifico che l'altezza sia quella di partenza
        assertEquals(50.0, shapeToResize.getHeight());
    }
}