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
        Shape rect = new Rectangle(10, 10, 5, 5);
        model.addShape(rect);
        assertEquals(1, model.getShapes().size(), "La dimensione del modello dovrebbe essere 1 dopo l'aggiunta.");
        assertTrue(model.getShapes().contains(rect), "Il modello dovrebbe contenere la forma aggiunta.");
    }

    @Test
    void addMultipleShapesShouldIncreaseSizeCorrectly() {
        Shape rect1 = new Rectangle(10, 10, 5, 5);
        Shape ellipse1 = new Ellipse(20, 20, 8, 8);
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

        Shape rect = new Rectangle(10, 10, 5, 5);
        model.addShape(rect); // Aggiungi una forma
        model.addShape(null); // Poi aggiungi null
        assertEquals(1, model.getShapes().size(), "Aggiungere null non dovrebbe cambiare un modello con una forma.");
    }

    // Test per clear() e removeShape() sono utili per la completezza del DrawingModel
    // anche se non direttamente usati da US-3. Se preferisci ometterli per ora, puoi farlo.
    @Test
    void clearShouldRemoveAllShapes() {
        model.addShape(new Rectangle(10, 10, 5, 5));
        model.addShape(new Ellipse(20, 20, 8, 8));
        model.clear();
        assertTrue(model.getShapes().isEmpty(), "Il modello dovrebbe essere vuoto dopo clear().");
        assertEquals(0, model.getShapes().size());
    }

    @Test
    void removeShapeShouldDecreaseSizeAndNotContainShape() {
        Shape rect = new Rectangle(10, 10, 5, 5);
        Shape ellipse = new Ellipse(20, 20, 8, 8);
        model.addShape(rect);
        model.addShape(ellipse);

        model.removeShape(rect);
        assertEquals(1, model.getShapes().size());
        assertFalse(model.getShapes().contains(rect));
        assertTrue(model.getShapes().contains(ellipse));
    }
}