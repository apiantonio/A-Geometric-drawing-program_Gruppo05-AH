package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;


public class CopyShapeCommand implements Command {
    private final AbstractShape shapeToCopy;
    private final ClipboardManager clipboardManager;
    private boolean operationPerformed = false; // Traccia se execute() ha fatto qualcosa

    public CopyShapeCommand(AbstractShape shapeToCopy, ClipboardManager clipboardManager) {
        this.shapeToCopy = shapeToCopy;
        this.clipboardManager = clipboardManager;
    }

    @Override
    public void execute() {
        if (shapeToCopy != null && clipboardManager != null) {
            clipboardManager.copyToClipboard(shapeToCopy);
            operationPerformed = true; // L'operazione è stata eseguita
        } else {
            operationPerformed = false; // Nessuna operazione eseguita
        }
    }

    @Override
    public void undo() {
        // Svuota la clipboard solo se execute() ha effettivamente eseguito un'operazione
        // e clipboardManager non è nullo.
        if (operationPerformed && clipboardManager != null) {
            clipboardManager.clearClipboard();
        }
    }
}