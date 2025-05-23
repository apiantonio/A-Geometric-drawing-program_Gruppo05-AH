package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

/**
 * Autore: Gruppo05
 * Scopo: Command per il cambio altezza di una figura
 */
public class ChangeHeightCommand implements Command {
    private final DrawingModel model;
    private final AbstractShape shape;
    private final double newHeight;

    public ChangeHeightCommand(DrawingModel model, AbstractShape shape, double newHeight) {
        this.shape = shape;
        this.newHeight = newHeight;
        this.model = model;
    }

    @Override
    public void execute() {
        // Cambia la larghezza della figura
        model.setShapeHeight(shape, newHeight);
    }

    // TODO: Implementare il metodo undo() per annullare l'azione del comando
    @Override
    public void undo() {
        // TOOD
    }
}
