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
    private final double oldHeight; //per l'undo

    public ChangeHeightCommand(DrawingModel model, AbstractShape shape, double newHeight) {
        this.shape = shape;
        this.newHeight = newHeight;
        this.oldHeight = shape.getHeight();
        this.model = model;
    }

    @Override
    public void execute() {
        // Cambia la larghezza della figura
        model.setShapeHeight(shape, newHeight);
    }

    @Override
    public void undo() {
        model.setShapeHeight(shape, oldHeight);
    }
}
