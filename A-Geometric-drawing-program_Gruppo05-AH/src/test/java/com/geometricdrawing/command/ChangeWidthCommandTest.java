package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle; // Usa una forma concreta per il test
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChangeWidthCommandTest {

    @Mock
    private DrawingModel mockDrawingModel;
    @Captor
    private ArgumentCaptor<Double> widthCaptor;

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
        AbstractShape shapeToResize = new Rectangle(10, 10, 50, 50); // Larghezza iniziale 50
        double newWidth = 75.0;
        ChangeWidthCommand command = new ChangeWidthCommand(mockDrawingModel, shapeToResize, newWidth);

        command.execute();

        verify(mockDrawingModel, times(1)).setShapeWidth(eq(shapeToResize), widthCaptor.capture());

        // Asserisci esplicitamente sul valore catturato.
        assertEquals(newWidth, widthCaptor.getValue(), "Il valore della larghezza passato al modello non è corretto.");
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
        assertEquals(shapeToResize.getWidth(), 50.0);
    }


}