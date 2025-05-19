package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RectangleTest {

    @Mock
    private GraphicsContext mockGc;

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
    void drawShouldCallCorrectGraphicsContextMethods() {
        rectangle.draw(mockGc);

        verify(mockGc).fillRect(10, 20, 100, 80);
        verify(mockGc).strokeRect(10, 20, 100, 80);
    }

}