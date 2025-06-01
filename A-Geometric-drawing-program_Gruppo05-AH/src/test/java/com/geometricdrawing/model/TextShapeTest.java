package com.geometricdrawing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextShapeTest {
    private TextShape textShape;
    @BeforeEach
    void setUp() {
        textShape = new TextShape(10, 20, 100, 50, "Hello World!", 12);
    }

    @Test
    void constructorShouldSetPropertiesCorrectly() {
        assertEquals(10, textShape.getX());
        assertEquals(20, textShape.getY());
        assertEquals(100, textShape.getWidth());
        assertEquals(50, textShape.getHeight());
        assertEquals("Hello World!", textShape.getText());
        assertEquals(12, textShape.getFontSize());
        assertEquals("System", textShape.getFontFamily(), "Default font.");
    }

    @Test
    void constructorWithNullTextShouldSetEmptyString() {
        TextShape shapeWithNullText = new TextShape(0, 0, 10, 10, null, 10);
        assertEquals("", shapeWithNullText.getText(), "Testo nullo viene convertito in una stringa vuota.");
    }

    @Test
    void setTextShouldUpdateText() {
        textShape.setText("New Text");
        assertEquals("New Text", textShape.getText());
    }

    @Test
    void setTextWithNullShouldSetEmptyString() {
        textShape.setText(null);
        assertEquals("", textShape.getText(), "Settare testo a null dovrebbe convertirlo ad una stringa vuota.");
    }

    @Test
    void setFontSizeShouldUpdateFontSize() {
        textShape.setFontSize(24);
        assertEquals(24, textShape.getFontSize());
    }

    @Test
    void getFontFamilyShouldReturnDefault() {
        assertEquals("System", textShape.getFontFamily());
    }
}