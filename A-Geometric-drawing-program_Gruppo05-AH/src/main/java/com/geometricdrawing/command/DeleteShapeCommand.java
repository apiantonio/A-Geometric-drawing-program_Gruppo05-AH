package com.geometricdrawing.command;

import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.AbstractShape;

/**
 * Autore: Gruppo05
 * Scopo: Command di cancellazione di una figura
 */

public class DeleteShapeCommand implements Command {
    private final DrawingModel model; // Il "receiver" del comando è sempre il model
    private final AbstractShape shape;   // La forma da rimuovere

    public DeleteShapeCommand(DrawingModel model, AbstractShape shapeToRemove) {
        this.model = model;
        this.shape = shapeToRemove;
    }

    @Override
    public void execute() {
        // L'azione effettiva del comando è di responsabilità del model
        model.removeShape(shape);
    }

}