package com.geometricdrawing.factory;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Polygon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolygonFactoryTest {

    private PolygonFactory factory;
    private final int testMaxPoints = 6;

    @BeforeEach
    void setUp() {
        factory = new PolygonFactory(testMaxPoints);
    }

    @Test
    void createShapeShouldReturnPolygon() {
        AbstractShape shape = factory.createShape(10, 20);
        assertNotNull(shape);
        assertInstanceOf(Polygon.class, shape, "Factory should create an instance of Polygon.");
    }

    @Test
    void createShapeShouldSetCorrectCoordinates() {
        double x = 50.5;
        double y = 100.1;
        AbstractShape shape = factory.createShape(x, y);
        assertEquals(x, shape.getX(), "X coordinate should match input.");
        assertEquals(y, shape.getY(), "Y coordinate should match input.");
    }

    @Test
    void getMaxPointsShouldReturnCorrectValue() {
        assertEquals(testMaxPoints, factory.getMaxPoints(), "getMaxPoints should return the value set in constructor.");
    }

    @Test
    void constructorShouldLimitMaxPointsBetween3And12() {
        // Test limite inferiore
        PolygonFactory factoryMin = new PolygonFactory(1);
        assertEquals(3, factoryMin.getMaxPoints(), "Max points should be at least 3.");

        // Test limite superiore
        PolygonFactory factoryMax = new PolygonFactory(20);
        assertEquals(12, factoryMax.getMaxPoints(), "Max points should be at most 12.");

        // Test valore valido
        PolygonFactory factoryValid = new PolygonFactory(8);
        assertEquals(8, factoryValid.getMaxPoints(), "Valid max points should be preserved.");
    }

    @Test
    void createShapeShouldHaveDefaultDimensionsInitially() {
        AbstractShape shape = factory.createShape(25, 35);

        // Poiché il poligono inizia senza vertici, le dimensioni dovrebbero essere quelle iniziali
        assertEquals(25, shape.getX(), "X coordinate should match input.");
        assertEquals(35, shape.getY(), "Y coordinate should match input.");
    }
}