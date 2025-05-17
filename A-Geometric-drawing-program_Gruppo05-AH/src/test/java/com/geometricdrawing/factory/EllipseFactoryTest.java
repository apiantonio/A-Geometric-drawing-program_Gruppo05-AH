package com.geometricdrawing.factory;

import com.geometricdrawing.model.Ellipse;
import com.geometricdrawing.model.Shape;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EllipseFactoryTest {

    private EllipseFactory factory;

    @BeforeEach
    void setUp() {
        factory = new EllipseFactory();
    }

    @Test
    void createShapeShouldReturnEllipse() {
        Shape shape = factory.createShape(15, 25);
        assertNotNull(shape);
        assertInstanceOf(Ellipse.class, shape, "Factory should create an instance of Ellipse.");
    }

    @Test
    void createShapeShouldSetCorrectCoordinates() {
        double x = 55.5;
        double y = 105.1;
        Shape shape = factory.createShape(x, y);
        assertEquals(x, shape.getX(), "X coordinate (top-left of bbox) should match input.");
        assertEquals(y, shape.getY(), "Y coordinate (top-left of bbox) should match input.");
    }

    @Test
    void createShapeShouldUseDefaultDimensions() {
        Shape shape = factory.createShape(0, 0);
        assertEquals(ShapeFactory.DEFAULT_WIDTH, shape.getWidth(), "Width should be default.");
        assertEquals(ShapeFactory.DEFAULT_HEIGHT, shape.getHeight(), "Height should be default.");
    }
}