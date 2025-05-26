package com.geometricdrawing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EllipseTest {
    private Ellipse ellipse;

    @BeforeEach
    void setUp() {
        ellipse = new Ellipse(5, 15, 25, 35);
    }

    @Test
    void constructorShouldSetPropertiesAndDefaultColors() {
        assertEquals(5, ellipse.getX());
        assertEquals(15, ellipse.getY());
        assertEquals(25, ellipse.getWidth());
        assertEquals(35, ellipse.getHeight());
    }

    @Test
    void setWidthShouldUpdateWidthOfEllipse() {
        double newWidth = 50.0;
        ellipse.setWidth(newWidth);
        assertEquals(newWidth, ellipse.getWidth(), "La larghezza dell'ellisse non è stata aggiornata correttamente.");
    }

    @Test
    void setHeightShouldUpdateHeightOfEllipse() {
        double newHeight = 60.0;
        ellipse.setHeight(newHeight);
        assertEquals(newHeight, ellipse.getHeight(), "L'altezza dell'ellisse non è stata aggiornata correttamente.");
    }
}
