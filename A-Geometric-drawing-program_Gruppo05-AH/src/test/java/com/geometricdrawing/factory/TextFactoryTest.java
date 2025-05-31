package com.geometricdrawing.factory;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.TextShape;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextFactoryTest {

    private TextFactory factory;

    @BeforeEach
    void setUp() {
        factory = new TextFactory();
    }

    @Test
    void createShapeShouldReturnTextShapeInstance() {
        AbstractShape shape = factory.createShape(10, 20);
        assertNotNull(shape, "La forma non dovrebbe essere null");
        assertInstanceOf(TextShape.class, shape, "Factory dovrebbe creare un'istanza di TextShape.");
    }

    @Test
    void createShapeShouldSetCorrectInitialCoordinates() {
        double x = 30.5;
        double y = 40.5;
        AbstractShape shape = factory.createShape(x, y);
        assertEquals(x, shape.getX());
        assertEquals(y, shape.getY());
    }

    @Test
    void createShapeShouldSetDefaultDimensionsAndTextProperties() {
        AbstractShape shape = factory.createShape(0, 0);
        assertInstanceOf(TextShape.class, shape);
        TextShape textShape = (TextShape) shape;

        assertEquals(ShapeFactory.DEFAULT_WIDTH, textShape.getWidth());
        assertEquals(ShapeFactory.DEFAULT_HEIGHT, textShape.getHeight());
        assertEquals("", textShape.getText());
        assertEquals(12, textShape.getFontSize());
    }
}