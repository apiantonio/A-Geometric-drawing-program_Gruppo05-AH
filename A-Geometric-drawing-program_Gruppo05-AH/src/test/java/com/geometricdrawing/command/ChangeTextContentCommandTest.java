package com.geometricdrawing.command;

import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.TextShape;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChangeTextContentCommandTest {

    private DrawingModel drawingModel;
    private TextShape textShape;
    private String initialText;
    private String newText;

    @BeforeEach
    void setUp() {
        drawingModel = new DrawingModel();
        initialText = "Testo iniziale";
        newText = "Testo cambiato";
        textShape = new TextShape(10, 10, 100, 50, initialText, 12);
        drawingModel.addShape(textShape);
    }

    @Test
    void executeShouldChangeTextContentInModel() {
        ChangeTextContentCommand command = new ChangeTextContentCommand(drawingModel, textShape, newText);
        command.execute();

        assertEquals(newText, textShape.getText());
    }

    @Test
    void undoShouldRestoreOldTextContentInModel() {
        ChangeTextContentCommand command = new ChangeTextContentCommand(drawingModel, textShape, newText);
        command.execute();

        command.undo();

        assertEquals(initialText, textShape.getText());
    }

    @Test
    void executeWithNullModelShouldNotThrowAndDoNothing() {
        ChangeTextContentCommand command = new ChangeTextContentCommand(null, textShape, newText);

        assertDoesNotThrow(command::execute);
        assertEquals(initialText, textShape.getText());
    }

    @Test
    void undoWithNullModelShouldNotThrowAndDoNothing() {
        ChangeTextContentCommand commandWithInitiallyNullModel = new ChangeTextContentCommand(null, textShape, newText);

        assertDoesNotThrow(commandWithInitiallyNullModel::undo);
        assertEquals(initialText, textShape.getText());
    }

    @Test
    void executeWithNullNewTextShouldSetEmptyString() {
        ChangeTextContentCommand command = new ChangeTextContentCommand(drawingModel, textShape, null);
        command.execute();
        assertEquals("", textShape.getText());
    }

    @Test
    void undoWhenNewTextWasNullShouldRestoreOldText() {
        ChangeTextContentCommand command = new ChangeTextContentCommand(drawingModel, textShape, null);
        command.execute();

        command.undo();
        assertEquals(initialText, textShape.getText());
    }
}