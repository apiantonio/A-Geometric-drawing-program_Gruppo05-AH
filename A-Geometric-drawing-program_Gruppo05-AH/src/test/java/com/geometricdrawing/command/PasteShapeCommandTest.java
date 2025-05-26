package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasteShapeCommandTest {

    private DrawingModel model;
    private ClipboardManager clipboardManager;
    private AbstractShape originalShapeInClipboardSetup;
    private AbstractShape shapeToPaste; // Questo sarà il clone ottenuto dal clipboardManager

    @BeforeEach
    void setUp() {
        model = new DrawingModel();
        clipboardManager = new ClipboardManager();
        originalShapeInClipboardSetup = new Rectangle(50, 60, 100, 80);

        clipboardManager.copyToClipboard(originalShapeInClipboardSetup);
        // shapeToPaste sarà il clone che ci aspettiamo venga manipolato
        shapeToPaste = clipboardManager.getFromClipboard(); // Otteniamo un clone per il setup, come farebbe il comando
    }

    @Test
    void execute_whenClipboardHasContent_withDefaultOffset_shouldMoveAndAddShape() {
        // Arrange
        PasteShapeCommand command = new PasteShapeCommand(model, clipboardManager);

        // Act
        command.execute();

        // Assert
        assertNotNull(command.getPastedShape(), "Pasted shape in command should not be null.");
        assertEquals(1, model.getShapes().size(), "Model should contain one shape after paste.");
        AbstractShape pastedShapeInModel = model.getShapes().get(0);

        assertNotSame(originalShapeInClipboardSetup, pastedShapeInModel, "Pasted shape in model should be a clone.");
        assertNotSame(shapeToPaste, pastedShapeInModel, "Pasted shape in model should be a new clone from the command's execution.");

        double expectedX = originalShapeInClipboardSetup.getX() + 10.0; // 50.0 + 10.0 = 60.0
        double expectedY = originalShapeInClipboardSetup.getY() + 10.0; // 60.0 + 10.0 = 70.0

        assertEquals(expectedX, pastedShapeInModel.getX(), "X coordinate for pasted shape (default offset) is incorrect.");
        assertEquals(expectedY, pastedShapeInModel.getY(), "Y coordinate for pasted shape (default offset) is incorrect.");
        assertEquals(originalShapeInClipboardSetup.getWidth(), pastedShapeInModel.getWidth(), "Width of pasted shape is incorrect.");
        assertEquals(originalShapeInClipboardSetup.getHeight(), pastedShapeInModel.getHeight(), "Height of pasted shape is incorrect.");

        assertSame(pastedShapeInModel, command.getPastedShape(), "The command's getPastedShape should return the shape added to the model.");
    }

    @Test
    void execute_whenClipboardHasContent_withAbsoluteCoordinates_shouldMoveAndAddShapeAtTarget() {
        // Arrange
        double targetX = 200.0;
        double targetY = 250.0;
        PasteShapeCommand command = new PasteShapeCommand(model, clipboardManager, targetX, targetY, true);

        // Act
        command.execute();

        // Assert
        assertNotNull(command.getPastedShape(), "Pasted shape in command should not be null.");
        assertEquals(1, model.getShapes().size(), "Model should contain one shape after paste.");
        AbstractShape pastedShapeInModel = model.getShapes().get(0);

        assertNotSame(originalShapeInClipboardSetup, pastedShapeInModel, "Pasted shape in model should be a clone.");

        assertEquals(targetX, pastedShapeInModel.getX(), "X coordinate for pasted shape (absolute) is incorrect.");
        assertEquals(targetY, pastedShapeInModel.getY(), "Y coordinate for pasted shape (absolute) is incorrect.");
        assertEquals(originalShapeInClipboardSetup.getWidth(), pastedShapeInModel.getWidth(), "Width of pasted shape is incorrect.");
        assertEquals(originalShapeInClipboardSetup.getHeight(), pastedShapeInModel.getHeight(), "Height of pasted shape is incorrect.");
        assertSame(pastedShapeInModel, command.getPastedShape());
    }

    @Test
    void execute_whenClipboardIsEmpty_shouldNotAddShapeToModel() {
        // Arrange
        clipboardManager.clearClipboard(); // Svuota la clipboard
        PasteShapeCommand command = new PasteShapeCommand(model, clipboardManager);

        // Act
        command.execute();

        // Assert
        assertTrue(model.getShapes().isEmpty(), "Model should be empty if clipboard was empty.");
        assertNull(command.getPastedShape(), "Pasted shape in command should be null if clipboard was empty.");
    }

    @Test
    void undo_afterSuccessfulExecute_shouldRemovePastedShapeFromModel() {
        // Arrange
        PasteShapeCommand command = new PasteShapeCommand(model, clipboardManager);
        command.execute(); // Esegue il paste, aggiungendo una forma al modello

        assertNotNull(command.getPastedShape(), "Pasted shape in command should not be null after execute.");
        assertEquals(1, model.getShapes().size(), "Model should have 1 shape after execute.");
        AbstractShape pastedShapeInstance = command.getPastedShape(); // La forma che è stata incollata

        // Act
        command.undo();

        // Assert
        assertTrue(model.getShapes().isEmpty(), "Model should be empty after undo.");
        assertFalse(model.getShapes().contains(pastedShapeInstance), "Model should not contain the pasted shape after undo.");
    }

    @Test
    void undo_whenExecuteDidNotPasteShape_shouldNotChangeModelState() {
        // Arrange
        clipboardManager.clearClipboard(); // Assicura che execute() non incolli nulla
        PasteShapeCommand command = new PasteShapeCommand(model, clipboardManager);
        command.execute();

        assertTrue(model.getShapes().isEmpty(), "Model should be empty if execute did not paste.");
        assertNull(command.getPastedShape(), "Pasted shape in command should be null.");

        // Act
        command.undo();

        // Assert
        assertTrue(model.getShapes().isEmpty(), "Model should still be empty after undo if nothing was pasted.");
    }

    @Test
    void undo_whenPastedShapeIsNullInternallyInCommand_shouldNotThrowAndNotChangeModel() {
        // Questo scenario si verifica se execute() non è mai stato chiamato in modo che
        // command.pastedShape rimanga null, o se clipboardManager.getFromClipboard()
        // avesse restituito null e execute() avesse impostato command.pastedShape a null.
        PasteShapeCommand command = new PasteShapeCommand(model, clipboardManager);
        // Non chiamiamo execute(), quindi command.pastedShape è null.

        // Act & Assert
        assertDoesNotThrow(() -> command.undo());
        assertTrue(model.getShapes().isEmpty(), "Model should remain empty if undo is called on a command that didn't set a pastedShape.");
    }
}