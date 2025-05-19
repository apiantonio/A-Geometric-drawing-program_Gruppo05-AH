package com.geometricdrawing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DrawingModelTest {

    private DrawingModel model;

    @BeforeEach
    void setUp() {
        model = new DrawingModel();
    }

    @Test
    void newModelShouldBeEmpty() {
        assertTrue(model.getShapes().isEmpty(), "Un nuovo modello dovrebbe essere vuoto.");
        assertEquals(0, model.getShapes().size(), "Un nuovo modello dovrebbe avere dimensione 0.");
    }

    @Test
    void addShapeShouldIncreaseSizeAndContainShape() {
        // Usiamo una forma concreta con valori qualsiasi, dato che le factory non sono usate qui
        AbstractShape rect = new Rectangle(10, 10, 5, 5);
        model.addShape(rect);
        assertEquals(1, model.getShapes().size(), "La dimensione del modello dovrebbe essere 1 dopo l'aggiunta.");
        assertTrue(model.getShapes().contains(rect), "Il modello dovrebbe contenere la forma aggiunta.");
    }

    @Test
    void addMultipleShapesShouldIncreaseSizeCorrectly() {
        AbstractShape rect1 = new Rectangle(10, 10, 5, 5);
        AbstractShape ellipse1 = new Ellipse(20, 20, 8, 8);
        model.addShape(rect1);
        model.addShape(ellipse1);
        assertEquals(2, model.getShapes().size(), "La dimensione del modello dovrebbe essere 2 dopo due aggiunte.");
        assertTrue(model.getShapes().contains(rect1));
        assertTrue(model.getShapes().contains(ellipse1));
    }

    @Test
    void addNullShapeShouldNotChangeModel() {
        model.addShape(null); // A un modello vuoto
        assertTrue(model.getShapes().isEmpty());
        assertEquals(0, model.getShapes().size());

        AbstractShape rect = new Rectangle(10, 10, 5, 5);
        model.addShape(rect); // Aggiungi una forma
        model.addShape(null); // Poi aggiungi null
        assertEquals(1, model.getShapes().size(), "Aggiungere null non dovrebbe cambiare un modello con una forma.");
    }

    @Test
    void addMultipleShapesShouldAssignIncreasingZOrder() {
        AbstractShape rect1 = new Rectangle(10, 10, 5, 5);
        AbstractShape ellipse1 = new Ellipse(20, 20, 8, 8);
        AbstractShape line1 = new Line(0,0,1,1);

        model.addShape(rect1);
        model.addShape(ellipse1);
        model.addShape(line1);

        assertEquals(0, rect1.getZ());
        assertEquals(1, ellipse1.getZ());
        assertEquals(2, line1.getZ());
    }
}