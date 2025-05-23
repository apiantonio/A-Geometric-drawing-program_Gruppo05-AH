package com.geometricdrawing.command;

/**
 * Autore: Gruppo05
 * Scopo: funge da interfaccia per i comandi
 */

public interface Command {

    void execute();

    // metodo per l'annullamento delle operazioni effettuate
    void undo();
}
