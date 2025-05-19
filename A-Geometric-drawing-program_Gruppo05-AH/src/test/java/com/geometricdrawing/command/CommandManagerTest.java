package com.geometricdrawing.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field; // Per accedere a campi privati (undoStack)
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommandManagerTest {

    private CommandManager commandManager;

    @Mock
    private Command mockCommand1; // Mock di un comando generico

    @Mock
    private Command mockCommand2;

    @BeforeEach
    void setUp() {
        commandManager = new CommandManager();
    }

    @Test
    void executeCommandShouldCallExecuteOnCommand() {
        commandManager.executeCommand(mockCommand1);

        // Verifica che il metodo execute() del mockCommand1 sia stato chiamato una volta.
        verify(mockCommand1, times(1)).execute();
    }

    @Test
    void executeCommandShouldAddCommandToUndoStack() throws NoSuchFieldException, IllegalAccessException {
        commandManager.executeCommand(mockCommand1);

        // Accedi a undoStack tramite reflection per verificare il suo contenuto.
        Field undoStackField = CommandManager.class.getDeclaredField("undoStack");
        undoStackField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Stack<Command> undoStack = (Stack<Command>) undoStackField.get(commandManager);

        assertNotNull(undoStack);
        assertEquals(1, undoStack.size(), "Lo stack undo dovrebbe contenere un comando.");
        assertSame(mockCommand1, undoStack.peek(), "Il comando in cima allo stack undo non è quello corretto.");
    }

    @Test
    void executeMultipleCommandsShouldAddAllToUndoStackInOrder() throws NoSuchFieldException, IllegalAccessException {
        // Act
        commandManager.executeCommand(mockCommand1);
        commandManager.executeCommand(mockCommand2);

        // Assert
        Field undoStackField = CommandManager.class.getDeclaredField("undoStack");
        undoStackField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Stack<Command> undoStack = (Stack<Command>) undoStackField.get(commandManager);

        assertNotNull(undoStack);
        assertEquals(2, undoStack.size(), "Lo stack undo dovrebbe contenere due comandi.");
        // L'ultimo comando eseguito è in cima allo stack (peek)
        assertSame(mockCommand2, undoStack.pop(), "Il secondo comando non è corretto o non è in cima.");
        assertSame(mockCommand1, undoStack.pop(), "Il primo comando non è corretto o non è nella posizione corretta.");
    }

}