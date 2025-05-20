package com.geometricdrawing.command;


import java.util.Stack;

/**
 * Autore: Gruppo05
 * Scopo: Gestione dei comandi eseguiti, permette eventualmente l'annullamento degli stessi
 */

public class CommandManager {
    private final Stack<Command> undoStack = new Stack<>();

    public void executeCommand(Command command) {

        command.execute();
        undoStack.push(command); // Aggiungi allo stack undo per possibile annullamento

    }

    // TODO implementare undo
}