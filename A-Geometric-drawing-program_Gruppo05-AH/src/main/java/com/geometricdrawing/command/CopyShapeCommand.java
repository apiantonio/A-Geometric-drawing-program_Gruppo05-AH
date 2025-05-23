package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;

/**
 * @Scopo: Command per la copia di una figura (copia in clipboard interna)
 */
public class CopyShapeCommand implements Command {
    private final AbstractShape shapeToCopy;
    private final ClipboardManager clipboardManager;

    public CopyShapeCommand(AbstractShape shapeToCopy, ClipboardManager clipboardManager) {
        this.shapeToCopy = shapeToCopy;
        this.clipboardManager = clipboardManager;
    }

    @Override
    public void execute() {
        if (shapeToCopy != null && clipboardManager != null) {
            clipboardManager.copyToClipboard(shapeToCopy);
        }
    }

    @Override
    public void undo() {
        // Non è necessario implementare undo per la copia
        // La copia non modifica lo stato del modello, quindi non c'è nulla da annullare
    }
}