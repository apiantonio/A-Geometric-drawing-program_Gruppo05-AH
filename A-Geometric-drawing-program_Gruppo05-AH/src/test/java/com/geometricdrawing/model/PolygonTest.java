package com.geometricdrawing.model;

import javafx.geometry.Point2D;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class PolygonTest {
    private Polygon polygon;
    private List<Point2D> testVertices;

    @BeforeEach
    void setUp() {
        polygon = new Polygon(10, 20);

        // triangolo di test
        testVertices = new ArrayList<>();
        testVertices.add(new Point2D(10, 20));
        testVertices.add(new Point2D(50, 20));
        testVertices.add(new Point2D(30, 60));
    }

    @Test
    void constructorShouldSetInitialPropertiesCorrectly() {
        assertEquals(10, polygon.getX());
        assertEquals(20, polygon.getY());
        assertEquals(0.0, polygon.getWidth());
        assertEquals(0.0, polygon.getHeight());
        assertEquals(1, polygon.getVertices().size());
        assertEquals(new Point2D(10, 20), polygon.getVertices().get(0));
    }

    @Test
    void addVertexShouldUpdateVerticesAndBounds() {
        polygon.addVertex(50, 20);
        polygon.addVertex(30, 60);

        assertEquals(3, polygon.getVertices().size());
        assertEquals(new Point2D(50, 20), polygon.getVertices().get(1));
        assertEquals(new Point2D(30, 60), polygon.getVertices().get(2));

        // Verifica che i bounds siano aggiornati
        assertEquals(10, polygon.getX()); // minX
        assertEquals(20, polygon.getY()); // minY
        assertEquals(40, polygon.getWidth()); // maxX - minX = 50 - 10
        assertEquals(40, polygon.getHeight()); // maxY - minY = 60 - 20
    }

    @Test
    void removeVertexShouldUpdateVerticesAndBounds() {
        polygon.setVertices(testVertices);

        polygon.removeVertex(1); // Rimuovi il secondo vertice (50, 20)

        assertEquals(2, polygon.getVertices().size());
        assertEquals(new Point2D(10, 20), polygon.getVertices().get(0));
        assertEquals(new Point2D(30, 60), polygon.getVertices().get(1));

        // Verifica che i bounds siano aggiornati
        assertEquals(10, polygon.getX());
        assertEquals(20, polygon.getY());
        assertEquals(20, polygon.getWidth()); // 30 - 10
        assertEquals(40, polygon.getHeight()); // 60 - 20
    }

    @Test
    void removeVertexWithInvalidIndexShouldNotChangeVertices() {
        polygon.setVertices(testVertices);
        int originalSize = polygon.getVertices().size();

        polygon.removeVertex(-1);
        polygon.removeVertex(10);

        assertEquals(originalSize, polygon.getVertices().size());
    }

    @Test
    void setVerticesShouldUpdatePolygonAndBounds() {
        polygon.setVertices(testVertices);

        assertEquals(testVertices, polygon.getVertices());
        assertEquals(10, polygon.getX());
        assertEquals(20, polygon.getY());
        assertEquals(40, polygon.getWidth());
        assertEquals(40, polygon.getHeight());
    }

    @Test
    void clearVerticesShouldResetPolygon() {
        polygon.setVertices(testVertices);

        polygon.clearVertices();

        assertTrue(polygon.getVertices().isEmpty());
        assertEquals(0, polygon.getX());
        assertEquals(0, polygon.getY());
        assertEquals(0, polygon.getWidth());
        assertEquals(0, polygon.getHeight());
    }

    @Test
    void moveByShoudUpdateAllVerticesAndBounds() {
        polygon.setVertices(testVertices);
        double deltaX = 15;
        double deltaY = 25;

        polygon.moveBy(deltaX, deltaY);

        assertEquals(new Point2D(25, 45), polygon.getVertices().get(0));
        assertEquals(new Point2D(65, 45), polygon.getVertices().get(1));
        assertEquals(new Point2D(45, 85), polygon.getVertices().get(2));

        assertEquals(25, polygon.getX()); // 10 + 15
        assertEquals(45, polygon.getY()); // 20 + 25
    }

    @Test
    void setWidthShouldScalePolygonHorizontally() {
        polygon.setVertices(testVertices);
        double originalWidth = polygon.getWidth(); // 40
        double newWidth = 80;

        polygon.setWidth(newWidth);

        assertEquals(newWidth, polygon.getWidth(), 0.001);
        // Verifica che i vertici siano stati scalati proporzionalmente
        double scaleX = newWidth / originalWidth; // 2.0
        assertEquals(10, polygon.getVertices().get(0).getX(), 0.001); // punto di riferimento
        assertEquals(90, polygon.getVertices().get(1).getX(), 0.001); // 10 + (50-10)*2
    }

    @Test
    void setHeightShouldScalePolygonVertically() {
        polygon.setVertices(testVertices);
        double originalHeight = polygon.getHeight(); // 40
        double newHeight = 80;

        polygon.setHeight(newHeight);

        assertEquals(newHeight, polygon.getHeight(), 0.001);
        // Verifica che i vertici siano stati scalati proporzionalmente
        double scaleY = newHeight / originalHeight; // 2.0
        assertEquals(20, polygon.getVertices().get(0).getY(), 0.001); // punto di riferimento
        assertEquals(100, polygon.getVertices().get(2).getY(), 0.001); // 20 + (60-20)*2
    }

    @Test
    void setWidthOnEmptyPolygonShouldOnlyUpdateWidth() {
        polygon.clearVertices();
        double newWidth = 100;

        polygon.setWidth(newWidth);

        assertEquals(newWidth, polygon.getWidth());
    }

    @Test
    void setHeightOnEmptyPolygonShouldOnlyUpdateHeight() {
        polygon.clearVertices();
        double newHeight = 100;

        polygon.setHeight(newHeight);

        assertEquals(newHeight, polygon.getHeight());
    }

    @Test
    void containsPointShouldReturnTrueForPointInsidePolygon() {
        polygon.setVertices(testVertices);

        // Punto all'interno del triangolo
        assertTrue(polygon.containsPoint(30, 30, 1.0));
    }

    @Test
    void containsPointShouldReturnFalseForPointOutsidePolygon() {
        polygon.setVertices(testVertices);

        // Punto fuori dal triangolo
        assertFalse(polygon.containsPoint(100, 100, 1.0));
    }

    @Test
    void containsPointShouldReturnFalseForPointOutsideBoundingBox() {
        polygon.setVertices(testVertices);

        // Punto molto lontano dal bounding box
        assertFalse(polygon.containsPoint(200, 200, 1.0));
    }
}