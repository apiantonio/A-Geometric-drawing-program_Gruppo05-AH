package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle; // Usa una forma concreta per il test
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChangeWidthCommandTest {

    @Mock
    private DrawingModel mockDrawingModel;

    private AbstractShape shapeToResize;
    private double newWidth;

    @BeforeEach
    void setUp() {
        // Inizializza la forma da ridimensionare e la nuova larghezza
        shapeToResize = new Rectangle(10, 10, 50, 70);
        newWidth = 75.0;
    }

    @Test
    void executeShouldCallSetShapeWidthOnModel() {
        ChangeWidthCommand command = new ChangeWidthCommand(mockDrawingModel, shapeToResize, newWidth);
        command.execute();

        // Verifica che il metodo setShapeWidth del DrawingModel sia stato chiamato
        // esattamente una volta con la forma e la nuova larghezza corrette.
        verify(mockDrawingModel, times(1)).setShapeWidth(shapeToResize, newWidth);
        // Verifico che la larghezza sia stata cambiata correttamente
        assertEquals(75.0, shapeToResize.getWidth());
    }

    @Test
    void resetOldWidthShapeWithUndo() {
        ChangeWidthCommand command = new ChangeWidthCommand(mockDrawingModel, shapeToResize, newWidth);
        command.execute();

        // Effettua l'undo per il ripristino della larghezza di partenza
        command.undo();

        // Verifica che il metodo setShapeWidth sia stato chiamato con la larghezza originale
        verify(mockDrawingModel, times(1)).setShapeWidth(shapeToResize, shapeToResize.getWidth());
        // Verifico che la larghezza sia quella di partenza
        assertEquals(50.0, shapeToResize.getWidth());
    }


}