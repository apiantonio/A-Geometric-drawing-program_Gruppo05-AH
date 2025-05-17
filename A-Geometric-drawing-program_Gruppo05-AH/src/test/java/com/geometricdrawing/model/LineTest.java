package com.geometricdrawing.model;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
;

@ExtendWith(MockitoExtension.class)
class LineTest {

    @Mock
    private GraphicsContext mockGc;

    private Line line;

    @BeforeEach
    void setUp() {
        line = new Line(10, 20, 110, 120); // startX, startY, endX, endY
    }

    @Test
    void constructorShouldSetPropertiesAndDefaultColors() {
        assertEquals(10, line.getX(), "Start X should be set correctly via getX().");
        assertEquals(20, line.getY(), "Start Y should be set correctly via getY().");
        assertEquals(110, line.getEndX(), "End X should be set.");
        assertEquals(120, line.getEndY(), "End Y should be set.");
        assertEquals(Color.TRANSPARENT, line.getFillColor(), "Default fill color for Line should be TRANSPARENT.");
        assertEquals(Color.BLACK, line.getBorderColor(), "Default border color should be BLACK.");
    }

    @Test
    void drawShouldCallCorrectGraphicsContextMethods() {
        line.draw(mockGc);

        verify(mockGc).setStroke(Color.BLACK);
        verify(mockGc).strokeLine(10, 20, 110, 120);
    }
}