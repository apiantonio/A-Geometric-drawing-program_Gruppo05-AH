package com.geometricdrawing.factory;

import static org.junit.jupiter.api.Assertions.*;

import com.geometricdrawing.model.Line;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LineFactoryTest {

    private LineFactory factory;

    @BeforeEach
    void setUp() {
        factory = new LineFactory();
    }

    @Test
    void createShapeShouldReturnLine() {
        Shape shape = factory.createShape(10, 20);
        assertNotNull(shape);
        assertInstanceOf(Line.class, shape, "Factory should create an instance of Line.");
    }

    @Test
    void createShapeShouldSetCorrectStartCoordinates() {
        double startX = 30.5;
        double startY = 70.2;
        Shape shape = factory.createShape(startX, startY);
        assertEquals(startX, shape.getX(), "Start X coordinate should match input.");
        assertEquals(startY, shape.getY(), "Start Y coordinate should match input.");
    }

    @Test
    void createShapeShouldSetCorrectEndCoordinatesBasedOnDefaultLength() {
        double startX = 30;
        double startY = 70;
        Shape shape = factory.createShape(startX, startY);
        assertInstanceOf(Line.class, shape);
        Line line = (Line) shape;

        double expectedEndX = startX + ShapeFactory.DEFAULT_LINE_LENGTH;
        double expectedEndY = startY; // Linea orizzontale di default

        assertEquals(expectedEndX, line.getEndX(), "End X should be startX + DEFAULT_LINE_LENGTH.");
        assertEquals(expectedEndY, line.getEndY(), "End Y should be startY for default horizontal line.");
    }
}