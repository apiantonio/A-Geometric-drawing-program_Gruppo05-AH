package com.geometricdrawing.model;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LineTest {

    @Mock
    private GraphicsContext mockGc;

    private Line line;

    @BeforeEach
    void setUp() {
        line = new Line(10, 20, 110, 20); // startX, startY, endX, endY
    }

    @Test
    void constructorShouldSetPropertiesAndDefaultColors() {
        assertEquals(10, line.getX(), "Start X should be set correctly via getX().");
        assertEquals(20, line.getY(), "Start Y should be set correctly via getY().");
        assertEquals(110, line.getEndX(), "End X should be set.");
        assertEquals(20, line.getEndY(), "End Y should be set.");
    }

    @Test
    void drawShouldCallCorrectGraphicsContextMethods() {
        line.draw(mockGc);
        verify(mockGc).strokeLine(10, 20, 110, 20);
    }
    @Test
    void setWidthShouldUpdateEndXForHorizontalLine() {
        double newWidth = 150.0; // Cambia la lunghezza sull'asse x
        line.setWidth(newWidth);
        assertEquals(10 + newWidth, line.getEndX(), "EndX non aggiornato correttamente dopo setWidth.");
        assertEquals(150, line.getLength(), "La lunghezza della linea dovrebbe essere 100."); // La lunghezza iniziale della linea di default.
    }
}