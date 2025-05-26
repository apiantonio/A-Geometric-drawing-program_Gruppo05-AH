package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle; // Usa una forma concreta
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
class ChangeHeightCommandTest {

    @Mock
    private DrawingModel mockDrawingModel;
    @Captor
    private ArgumentCaptor<Double> heightCaptor;

    private AbstractShape shapeToResize;
    private double newHeight;

    @BeforeEach
    public void setUp() {
        shapeToResize = new Rectangle(10, 10, 50, 50);
        newHeight = 60.0;
    }

    @Test
    void executeShouldCallSetShapeHeightOnModel() {
        AbstractShape shapeToResize = new Rectangle(10, 10, 50, 50); // Altezza iniziale 50
        double newHeight = 60.0;
        ChangeHeightCommand command = new ChangeHeightCommand(mockDrawingModel, shapeToResize, newHeight);

        command.execute();

        // Verifica che il metodo setShapeHeight sia stato chiamato una volta.
        // Usa eq() per l'oggetto shape e cattura l'argomento double.
        verify(mockDrawingModel, times(1)).setShapeHeight(eq(shapeToResize), heightCaptor.capture());

        // Asserisci esplicitamente sul valore catturato.
        assertEquals(newHeight, heightCaptor.getValue(), "Il valore dell'altezza passato al modello non è corretto.");
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
        assertEquals(shapeToResize.getHeight(), 50.0);
    }
}