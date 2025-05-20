package com.geometricdrawing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DrawingModelTest {

    private DrawingModel model;
    private AbstractShape rect;

    @BeforeEach
    void setUp() {
        model = new DrawingModel();
        rect = new Rectangle(10, 10, 50, 50);
        model.addShape(rect);
    }

    @Test
    void newModelShouldBeEmptyBeforeAddingShapes() {
        DrawingModel newModel = new DrawingModel();
        assertTrue(newModel.getShapes().isEmpty(), "Un nuovo modello dovrebbe essere vuoto.");
        assertEquals(0, newModel.getShapes().size(), "Un nuovo modello dovrebbe avere dimensione 0.");
    }

    @Test
    void modelShouldContainInitiallyAddedShape() {
        assertEquals(1, model.getShapes().size(), "Il modello dovrebbe contenere la forma aggiunta nel setup.");
        assertTrue(model.getShapes().contains(rect), "Il modello dovrebbe contenere il rettangolo aggiunto nel setup.");
    }


    @Test
    void addShapeShouldIncreaseSizeAndContainShape() {
        AbstractShape ellipse = new Ellipse(20, 20, 8, 8);
        model.addShape(ellipse);
        assertEquals(2, model.getShapes().size(), "La dimensione del modello dovrebbe essere 2 dopo l'aggiunta di una seconda forma.");
        assertTrue(model.getShapes().contains(rect), "Il modello dovrebbe ancora contenere la forma iniziale.");
        assertTrue(model.getShapes().contains(ellipse), "Il modello dovrebbe contenere la nuova forma aggiunta.");
    }

    @Test
    void addMultipleShapesShouldIncreaseSizeCorrectly() {
        AbstractShape ellipse1 = new Ellipse(20, 20, 8, 8);
        AbstractShape line1 = new Line(30,30,35,35);

        model.addShape(ellipse1);
        model.addShape(line1);
        assertEquals(3, model.getShapes().size(), "La dimensione del modello dovrebbe essere 3 dopo due ulteriori aggiunte.");
        assertTrue(model.getShapes().contains(rect));
        assertTrue(model.getShapes().contains(ellipse1));
        assertTrue(model.getShapes().contains(line1));
    }

    @Test
    void addNullShapeShouldNotChangeModel() {
        assertEquals(1, model.getShapes().size());
        model.addShape(null); // Poi aggiungi null
        assertEquals(1, model.getShapes().size(), "Aggiungere null non dovrebbe cambiare un modello che già contiene forme.");
    }

    @Test
    void addMultipleShapesShouldAssignIncreasingZOrder() {
        AbstractShape ellipse1 = new Ellipse(20, 20, 8, 8);
        AbstractShape line1 = new Line(0,0,1,1);

        model.addShape(ellipse1);
        model.addShape(line1);

        assertEquals(0, rect.getZ());
        assertEquals(1, ellipse1.getZ());
        assertEquals(2, line1.getZ());
    }
    @Test
    void setShapeWidthShouldUpdateShapeWidthInModel() {
        double newWidth = 80.0;
        // 'rect' è già stato aggiunto al modello nel metodo setUp()
        model.setShapeWidth(rect, newWidth);
        assertEquals(newWidth, rect.getWidth(), "La larghezza della forma nel modello non è stata aggiornata.");
    }

    @Test
    void setShapeHeightShouldUpdateShapeHeightInModel() {
        double newHeight = 90.0;
        // 'rect' è già stato aggiunto al modello nel metodo setUp()
        model.setShapeHeight(rect, newHeight);
        assertEquals(newHeight, rect.getHeight(), "L'altezza della forma nel modello non è stata aggiornata.");
    }

}