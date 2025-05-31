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

    // Costruttore per il ridimensionamento
     public ChangeWidthCommand(DrawingModel model, AbstractShape shape, double newWidth) {
         this.model = model;
         this.shape = shape;
         this.newWidth = newWidth;
         this.oldWidth = shape.getWidth();
     }

    //per lo stretch
    public ChangeWidthCommand(DrawingModel model, AbstractShape shape, double newWidth, double actualOldWidth) {
        this.model = model;
        this.shape = shape;
        this.newWidth = newWidth;
        this.oldWidth = actualOldWidth;
    }

    @Override
    public void execute() {
        if (model != null && shape != null) {
            model.setShapeWidth(shape, newWidth);
        }
    }

    @Override
    public void undo() {
        if (model != null && shape != null) {
            model.setShapeWidth(shape, oldWidth);
        }
    }
}