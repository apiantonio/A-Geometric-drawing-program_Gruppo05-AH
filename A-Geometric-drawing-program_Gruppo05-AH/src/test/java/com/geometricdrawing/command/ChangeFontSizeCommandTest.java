package com.geometricdrawing.command;

import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.TextShape;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChangeFontSizeCommandTest {

    private DrawingModel drawingModel;
    private TextShape textShape;
    private int initialFontSize;
    private int newFontSize;

    @BeforeEach
    void setUp() {
        drawingModel = new DrawingModel();
        initialFontSize = 12;
        newFontSize = 24;
        textShape = new TextShape(10, 10, 100, 50, "Test", initialFontSize);
        drawingModel.addShape(textShape);
    }

    @Test
    void executeShouldChangeFontSizeInModel() {
        ChangeFontSizeCommand command = new ChangeFontSizeCommand(drawingModel, textShape, newFontSize);
        command.execute();

        assertEquals(newFontSize, textShape.getFontSize());
    }

    @Test
    void undoShouldRestoreOldFontSizeInModel() {
        ChangeFontSizeCommand command = new ChangeFontSizeCommand(drawingModel, textShape, newFontSize);
        command.execute();

        command.undo();

        assertEquals(initialFontSize, textShape.getFontSize());
    }

    @Test
    void executeWithNullModelShouldNotThrowAndDoNothing() {
        ChangeFontSizeCommand command = new ChangeFontSizeCommand(null, textShape, newFontSize);

        assertDoesNotThrow(command::execute);
        assertEquals(initialFontSize, textShape.getFontSize());
    }

}