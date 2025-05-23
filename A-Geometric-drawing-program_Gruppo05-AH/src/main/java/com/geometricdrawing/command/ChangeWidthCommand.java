package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

/**
 * Autore: Gruppo05
 * Scopo: Command per il cambio larghezza di una figura
 */
public class ChangeWidthCommand implements Command {
    private final DrawingModel model;
    private final AbstractShape shape;
    private final double oldWidth;
    private final double newWidth;

    public ChangeWidthCommand(DrawingModel model, AbstractShape shape, double newWidth) {
        this.shape = shape;
        this.newWidth = newWidth;
        this.oldWidth = shape.getWidth();
        this.model = model;
    }

    @Override
    public void execute() {
        // Cambia la larghezza della figura
        model.setShapeWidth(shape, newWidth);
    }

    @Override
    public void undo() {
        model.setShapeWidth(shape, oldWidth);
    }
}
