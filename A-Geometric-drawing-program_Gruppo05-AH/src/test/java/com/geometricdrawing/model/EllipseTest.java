package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() {
        ellipse = new Ellipse(5, 15, 25, 35);
    }
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
    @Test
    void setWidthShouldUpdateWidthOfEllipse() {
        double newWidth = 50.0;
        ellipse.setWidth(newWidth);
        assertEquals(newWidth, ellipse.getWidth(), "La larghezza dell'ellisse (diametro orizzontale del bbox) non è stata aggiornata correttamente.");
    }

    @Test
    void setHeightShouldUpdateHeightOfEllipse() {
        double newHeight = 60.0;
        ellipse.setHeight(newHeight);
        assertEquals(newHeight, ellipse.getHeight(), "L'altezza dell'ellisse (diametro verticale del bbox) non è stata aggiornata correttamente.");
    }
}