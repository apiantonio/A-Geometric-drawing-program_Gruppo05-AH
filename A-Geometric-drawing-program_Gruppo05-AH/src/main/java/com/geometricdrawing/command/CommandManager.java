package com.geometricdrawing.command;


import java.util.Stack;

public class CommandManager {
    private final Stack<Command> undoStack = new Stack<>();

    public void executeCommand(Command command) {

        command.execute();
        undoStack.push(command); // Aggiungi allo stack undo per possibile annullamento

    }

    // TODO implementare undo
}