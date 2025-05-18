package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

public class MoveShapeCommand implements Command {
    private final DrawingModel model;
    private final AbstractShape shape;
    private final double newX; // Nuova coordinata X
    private final double newY; // Nuova coordinata Y
    private final double oldX;
    private final double oldY;

    public MoveShapeCommand(DrawingModel model, AbstractShape shape, double newX, double newY) {
        this.shape = shape;
        this.model = model;
        this.newX = newX;
        this.newY = newY;
        this.oldX = shape.getX();
        this.oldY = shape.getY();
    }

    @Override
    public void execute() {
        // Muove la figura
        model.moveShapeTo(shape, newX, newY);
    }

//    @Override
//    public void undo() {
//        model.moveShapeTo(shape, oldX, oldY);
//    }

}
