package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Ellipse;
import com.geometricdrawing.model.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BringToForegroundCommandTest {

    private DrawingModel drawingModel;
    private AbstractShape shapeTest;
    private int oldZ;

    @BeforeEach
    void setUp() {
        drawingModel = new DrawingModel(); // Usa la vera implementazione
        shapeTest = new Rectangle(10, 10, 50, 80);
        drawingModel.addShape(shapeTest); // Aggiungi la forma al modello
        oldZ = shapeTest.getZ();// Crea una forma di test
    }

    @Test
    void constructorShouldStoreModelAndShape() {
        BringToForegroundCommand command = new BringToForegroundCommand(drawingModel, shapeTest);
        assertNotNull(command); // Verifica che il comando sia stato istanziato correttamente
    }

    @Test
    void executeShouldNotChangeZOfTheSingleShape() {
        BringToForegroundCommand command = new BringToForegroundCommand(drawingModel, shapeTest);
        command.execute();

        // Verifica che la forma sia stata portata in primo piano
        assertNotNull(drawingModel.getShapes());
        assertNotNull(shapeTest);
        assertEquals(oldZ, shapeTest.getZ(), "Lo Z non dovrebbe essere cambiato");
    }

    @Test
    void executeShouldChangeZOfTwoShapes() {
        AbstractShape ellTest = new Ellipse(20, 20, 30, 50);
        drawingModel.addShape(ellTest); // Aggiungi la seconda forma al modello
        assertEquals(1, ellTest.getZ(), "La forma ellittica dovrebbe avere Z=1 inizialmente");

        BringToForegroundCommand command = new BringToForegroundCommand(drawingModel, shapeTest);
        command.execute();

        assertNotNull(drawingModel.getShapes());
        assertEquals(2, drawingModel.getShapes().size());
        assertEquals(1, shapeTest.getZ());
        assertEquals(0, ellTest.getZ());
    }

    @Test
    void undoShouldRestoreZOfTwoShapes() {
        AbstractShape ellTest = new Ellipse(20, 20, 30, 50);
        drawingModel.addShape(ellTest); // Aggiungi la seconda forma al modello
        assertEquals(1, ellTest.getZ(), "La forma ellittica dovrebbe avere Z=1 inizialmente");

        BringToForegroundCommand command = new BringToForegroundCommand(drawingModel, shapeTest);
        command.execute();

        // Verifica che le Z siano cambiate
        assertEquals(1, shapeTest.getZ());
        assertEquals(0, ellTest.getZ());

        // Esegui l'undo
        command.undo();

        // Verifica che le Z siano state ripristinate
        assertEquals(oldZ, shapeTest.getZ());
        assertEquals(1, ellTest.getZ());
    }

}
