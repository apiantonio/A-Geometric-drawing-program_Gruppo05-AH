package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

/**
 * Autore: Gruppo05
 * Scopo: Command per lo spopstamento di una figura
 */
public class MoveShapeCommand implements Command {
    private final DrawingModel model;
    private final AbstractShape shape;
    private final double newX; // Nuova coordinata X
    private final double newY; // Nuova coordinata Y
    private double oldX;
    private double oldY;

    public MoveShapeCommand(DrawingModel model, AbstractShape shape, double newX, double newY) {
        this.shape = shape;
        this.model = model;
        this.newX = newX;
        this.newY = newY;
        this.oldX = shape != null ? shape.getX() : 0.0;
        this.oldY = shape != null ? shape.getY() : 0.0;
    }

    @Override
    public void execute() {
        // Muove la figura
        model.moveShapeTo(shape, newX, newY);
    }

    @Override
    public void undo() {
         model.moveShapeTo(shape, oldX, oldY);
    }

    public void setOldY(double oldY) {
        this.oldY = oldY;
    }

    public void setOldX(double oldX) {
        this.oldX = oldX;
    }
}
