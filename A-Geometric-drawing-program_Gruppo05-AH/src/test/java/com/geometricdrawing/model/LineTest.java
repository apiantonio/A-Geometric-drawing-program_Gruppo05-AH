package com.geometricdrawing.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LineTest {
    private Line line;

    @BeforeEach
    void setUp() {
        line = new Line(10, 20, 110, 20);
    }

    @Test
    void constructorShouldSetPropertiesAndDefaultColors() {
        assertEquals(10, line.getX(), "Start X should be set correctly via getX().");
        assertEquals(20, line.getY(), "Start Y should be set correctly via getY().");
        assertEquals(110, line.getEndX(), "End X should be set.");
        assertEquals(20, line.getEndY(), "End Y should be set.");
    }

    @Test
    void setWidthShouldUpdateEndXForHorizontalLine() {
        double newWidth = 150.0;
        line.setWidth(newWidth);
        assertEquals(10 + newWidth, line.getEndX(), "EndX non aggiornato correttamente dopo setWidth.");
        assertEquals(150, line.getLength(), "La lunghezza della linea dovrebbe essere 150.");
    }
}
