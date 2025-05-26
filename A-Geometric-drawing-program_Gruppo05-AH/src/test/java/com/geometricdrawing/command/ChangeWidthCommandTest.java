package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle; // Usa una forma concreta per il test
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChangeWidthCommandTest {
    private DrawingModel drawingModel;
    private AbstractShape shapeToResize;
    private double newWidth;

    @BeforeEach
    void setUp() {
        drawingModel = new DrawingModel();
        // Inizializza la forma da ridimensionare e la nuova larghezza
        shapeToResize = new Rectangle(10, 10, 50, 70);
        newWidth = 75.0;
    }

    @Test
    void executeShouldCallSetShapeWidthOnModel() {
        ChangeWidthCommand command = new ChangeWidthCommand(drawingModel, shapeToResize, newWidth);
        command.execute();

        // Verifico che la larghezza sia stata cambiata correttamente
        assertEquals(75.0, shapeToResize.getWidth());
    }

    @Test
    void resetOldWidthShapeWithUndo() {
        ChangeWidthCommand command = new ChangeWidthCommand(drawingModel, shapeToResize, newWidth);
        command.execute();

        // Effettua l'undo per il ripristino della larghezza di partenza
        command.undo();

        // Verifico che la larghezza sia quella di partenza
        assertEquals(50.0, shapeToResize.getWidth());
    }
}