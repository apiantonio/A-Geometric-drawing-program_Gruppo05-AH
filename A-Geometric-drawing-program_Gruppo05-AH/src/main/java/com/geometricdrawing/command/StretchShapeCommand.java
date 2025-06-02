package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

public class StretchShapeCommand implements Command {
    private final DrawingModel model;
    private final AbstractShape shape;

    private final double initialX;
    private final double initialY;
    private final double initialWidth;
    private final double initialHeight;

    private final double finalX;
    private final double finalY;
    private final double finalWidth;
    private final double finalHeight;

    public StretchShapeCommand(DrawingModel model, AbstractShape shape,
                               double initialX, double initialY, double initialWidth, double initialHeight,
                               double finalX, double finalY, double finalWidth, double finalHeight) {
        this.model = model;
        this.shape = shape;
        this.initialX = initialX;
        this.initialY = initialY;
        this.initialWidth = initialWidth;
        this.initialHeight = initialHeight;
        this.finalX = finalX;
        this.finalY = finalY;
        this.finalWidth = finalWidth;
        this.finalHeight = finalHeight;
    }

    @Override
    public void execute() {
        model.setShapeHeight(shape, finalHeight);   // Imposta prima le dimensioni
        model.setShapeWidth(shape, finalWidth);
        model.moveShapeTo(shape, finalX, finalY);     // Poi la posizione
    }

    @Override
    public void undo() {
        model.setShapeHeight(shape, initialHeight);
        model.setShapeWidth(shape, initialWidth);
        model.moveShapeTo(shape, initialX, initialY);
    }
}