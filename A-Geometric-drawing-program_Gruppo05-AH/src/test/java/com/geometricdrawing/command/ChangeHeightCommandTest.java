package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle; // Usa una forma concreta
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ChangeHeightCommandTest {

    private DrawingModel drawingModel;
    private AbstractShape shapeToResize;
    private double newHeight;

    @BeforeEach
    void setUp() {
        drawingModel = new DrawingModel(); // Usa una vera implementazione
        shapeToResize = new Rectangle(10, 10, 50, 50);
        newHeight = 60.0;
    }

    @Test
    void executeShouldChangeShapeHeight() {
        ChangeHeightCommand command = new ChangeHeightCommand(drawingModel, shapeToResize, newHeight);

        command.execute();

        // Si controlla che l'altezza sia stata davvero modificata
        assertEquals(60.0, shapeToResize.getHeight());
    }

    @Test
    void undoShouldRestoreOldHeight() {
        ChangeHeightCommand command = new ChangeHeightCommand(drawingModel, shapeToResize, newHeight);
        command.execute();
        command.undo();

        // L'altezza originale è stata ripristinata
        assertEquals(50.0, shapeToResize.getHeight());
    }
}