package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

class CommandManagerTest {

    private CommandManager commandManager;
    private ClipboardManager clipboardManager;
    private Rectangle testShape;
    private CopyShapeCommand command1;
    private CopyShapeCommand command2;

    @BeforeEach
    void setUp() {
        commandManager = new CommandManager();
        clipboardManager = new ClipboardManager();
        testShape = new Rectangle(0, 0, 10, 10);
        // comado di test
        command1 = new CopyShapeCommand(testShape, clipboardManager);
        command2 = new CopyShapeCommand(testShape, clipboardManager);
    }

    @Test
    void executeCommandShouldExecuteCommand() {
        commandManager.executeCommand(command1);
        
        assertTrue(clipboardManager.hasContent(), "Il comando dovrebbe essere stato eseguito");
        AbstractShape copiedShape = clipboardManager.getFromClipboard();
        assertNotNull(copiedShape, "Dovrebbe esserci una forma nella clipboard");
        CopyShapeCommandTest.assertShapesAreEqualButNotSame(testShape, copiedShape);
    }

    @Test
    void executeCommandShouldAddCommandToUndoStack() throws NoSuchFieldException, IllegalAccessException {
        commandManager.executeCommand(command1);

        Field undoStackField = CommandManager.class.getDeclaredField("undoStack");
        undoStackField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Stack<Command> undoStack = (Stack<Command>) undoStackField.get(commandManager);

        assertNotNull(undoStack);
        assertEquals(1, undoStack.size(), "Lo stack undo dovrebbe contenere un comando");
        assertSame(command1, undoStack.peek(), "Il comando in cima allo stack undo non è quello corretto");
    }

    @Test
    void executeMultipleCommandsShouldAddAllToUndoStackInOrder() throws NoSuchFieldException, IllegalAccessException {
        commandManager.executeCommand(command1);
        commandManager.executeCommand(command2);

        Field undoStackField = CommandManager.class.getDeclaredField("undoStack");
        undoStackField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Stack<Command> undoStack = (Stack<Command>) undoStackField.get(commandManager);

        assertNotNull(undoStack);
        assertEquals(2, undoStack.size(), "Lo stack undo dovrebbe contenere due comandi");
        assertSame(command2, undoStack.pop(), "Il secondo comando non è corretto o non è in cima");
        assertSame(command1, undoStack.pop(), "Il primo comando non è corretto o non è nella posizione corretta");
    }
}
