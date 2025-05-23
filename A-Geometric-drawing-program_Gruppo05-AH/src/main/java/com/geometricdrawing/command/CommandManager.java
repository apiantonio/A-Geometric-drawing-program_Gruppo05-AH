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

    // back all'operazione precedente
    public void undo() {
        // finchè ci sono operazioni su cui si può richiamare l'undo nello stack
        if(! undoStack.isEmpty()) {
            Command cmd = undoStack.pop();
            // richiama l'undo specifica per l'ultimo comando (es: se era aggiungi immagine la rimuove ecc...)
            cmd.undo();
        }else{
            System.out.println("Stack dei comandi vuoto");
        }
    }
}