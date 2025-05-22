package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;

/**
 * @Autore: Gruppo05
 * @Scopo: Gestisce una clipboard interna per le operazioni di copia, taglio e incolla delle figure.
 */
public class ClipboardManager {
    private AbstractShape clipboardShape;

    public ClipboardManager() {
        this.clipboardShape = null;
    }

    /**
     * Copia la figura specificata nella clipboard interna.
     * Preferibilmente, la figura dovrebbe essere clonata per evitare effetti collaterali.
     * @param shape La figura da copiare.
     */
    public void copyToClipboard(AbstractShape shape) {
        if (shape != null) {
            // Utilizza il metodo deepClone() da AbstractShape
            this.clipboardShape = shape.deepClone();
        } else {
            this.clipboardShape = null;
        }
    }

    /**
     * Restituisce una copia della figura attualmente nella clipboard.
     * Restituire una copia previene modifiche accidentali alla forma in clipboard
     * se viene incollata e poi modificata, e poi incollata di nuovo.
     * @return Una copia della figura nella clipboard, o null se la clipboard è vuota.
     */
    public AbstractShape getFromClipboard() {
        if (this.clipboardShape != null) {
            return this.clipboardShape.deepClone(); // Restituisce sempre un nuovo clone per il paste
        }
        return null;
    }

    /**
     * Pulisce la clipboard interna.
     */
    public void clearClipboard() {
        this.clipboardShape = null;
    }

    /**
     * Controlla se la clipboard contiene una figura.
     * @return true se la clipboard non è vuota, false altrimenti.
     */
    public boolean hasContent() {
        return this.clipboardShape != null;
    }
}