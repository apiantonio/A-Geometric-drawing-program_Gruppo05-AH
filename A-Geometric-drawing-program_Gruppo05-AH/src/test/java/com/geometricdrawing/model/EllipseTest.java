package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EllipseTest {

    @Mock
    private GraphicsContext mockGc;

    private Ellipse ellipse;

    @Test
    void constructorShouldSetPropertiesAndDefaultColors() {
        ellipse = new Ellipse(5, 15, 25, 35);
        assertEquals(5, ellipse.getX());
        assertEquals(15, ellipse.getY());
        assertEquals(25, ellipse.getWidth());
        assertEquals(35, ellipse.getHeight());
    }

    @Test
    void drawShouldCallCorrectGraphicsContextMethods() {
        ellipse = new Ellipse(5, 15, 25, 35);

        ellipse.draw(mockGc);

        verify(mockGc).fillOval(5, 15, 25, 35);
        verify(mockGc).strokeOval(5, 15, 25, 35);
    }
}