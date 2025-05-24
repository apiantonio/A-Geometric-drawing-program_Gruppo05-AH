package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

/**
 * @Scopo: Command per l'operazione di "Taglia" di una figura.
 * Copia la figura negli appunti e la rimuove dal modello.
 */
public class CutShapeCommand implements Command {
    private final DrawingModel model;
    private final AbstractShape shapeToCut;
    private final ClipboardManager clipboardManager;
    private AbstractShape shapePreviouslyInClipboard;
    private boolean shapeWasRemovedFromModel = false;

    public CutShapeCommand(DrawingModel model, AbstractShape shapeToCut, ClipboardManager clipboardManager) {
        this.model = model;
        this.shapeToCut = shapeToCut;
        this.clipboardManager = clipboardManager;
    }

    @Override
    public void execute() {
        if (model == null || shapeToCut == null || clipboardManager == null) {
            return;
        }
        // Salvare il contenuto della clipboard per undo
        if (clipboardManager.hasContent()) {
            this.shapePreviouslyInClipboard = clipboardManager.getFromClipboard();
        } else {
            this.shapePreviouslyInClipboard = null;
        }

        clipboardManager.copyToClipboard(shapeToCut); // Copia la figura nel clipboard
        model.removeShape(shapeToCut);
        shapeWasRemovedFromModel = true;
    }

    @Override
    public void undo() {
        if (shapeWasRemovedFromModel && model != null && shapeToCut != null && clipboardManager != null) {
            model.addShape(shapeToCut);

            clipboardManager.copyToClipboard(shapePreviouslyInClipboard);
        }
    }

    public AbstractShape getCutShape() {
        return shapeToCut;
    }
}