package com.geometricdrawing.factory;

import com.geometricdrawing.model.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RectangleFactoryTest {

    private RectangleFactory factory;

    @BeforeEach
    void setUp() {
        factory = new RectangleFactory();
    }

    @Test
    void createShapeShouldReturnRectangle() {
        Shape shape = factory.createShape(10, 20);
        assertNotNull(shape);
        assertInstanceOf(Rectangle.class, shape, "Factory should create an instance of Rectangle.");
    }

    @Test
    void createShapeShouldSetCorrectCoordinates() {
        double x = 50.5;
        double y = 100.1;
        Shape shape = factory.createShape(x, y);
        assertEquals(x, shape.getX(), "X coordinate should match input.");
        assertEquals(y, shape.getY(), "Y coordinate should match input.");
    }

    @Test
    void createShapeShouldUseDefaultDimensions() {
        Shape shape = factory.createShape(0, 0);

        assertEquals(ShapeFactory.DEFAULT_WIDTH, shape.getWidth(), "Width should be default.");
        assertEquals(ShapeFactory.DEFAULT_HEIGHT, shape.getHeight(), "Height should be default.");
    }
}