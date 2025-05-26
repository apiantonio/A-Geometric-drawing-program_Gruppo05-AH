package com.geometricdrawing.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RectangleTest {
    private Rectangle rectangle;

    @BeforeEach
    void setUp() {
        rectangle = new Rectangle(10, 20, 100, 80);
    }

    @Test
    void constructorShouldSetPropertiesAndDefaultColors() {
        assertEquals(10, rectangle.getX());
        assertEquals(20, rectangle.getY());
        assertEquals(100, rectangle.getWidth());
        assertEquals(80, rectangle.getHeight());
    }

    @Test
    void setWidthShouldUpdateWidth() {
        double newWidth = 150.0;
        rectangle.setWidth(newWidth);
        assertEquals(newWidth, rectangle.getWidth(), "La larghezza del rettangolo non è stata aggiornata correttamente.");
    }

    @Test
    void setHeightShouldUpdateHeight() {
        double newHeight = 120.0;
        rectangle.setHeight(newHeight);
        assertEquals(newHeight, rectangle.getHeight(), "L'altezza del rettangolo non è stata aggiornata correttamente.");
    }
}
