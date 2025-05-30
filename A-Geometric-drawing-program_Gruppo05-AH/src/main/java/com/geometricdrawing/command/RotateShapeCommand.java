package com.geometricdrawing.command;

import com.geometricdrawing.command.Command;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

public class RotateShapeCommand implements Command {
    private final AbstractShape shape;
    private final double deltaAngle;
    private final DrawingModel model; // Assuming 'model' is the shape itself for rotation

    public RotateShapeCommand(DrawingModel model, AbstractShape shape, double deltaAngle) {
        this.shape = shape;
        this.model = model;
        this.deltaAngle = deltaAngle;
    }

    @Override
    public void execute() {
        model.rotateShape(shape, deltaAngle);
    }

    @Override
    public void undo() {
        model.rotateShape(shape, -deltaAngle);
    }
}
