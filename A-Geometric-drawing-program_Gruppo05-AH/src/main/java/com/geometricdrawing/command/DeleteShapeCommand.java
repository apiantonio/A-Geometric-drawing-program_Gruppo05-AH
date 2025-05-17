package com.geometricdrawing.command;

import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.AbstractShape;

public class DeleteShapeCommand implements Command {
    private final DrawingModel model; // Il "receiver" del comando è sempre il model
    private final AbstractShape shapeToRemove;   // La forma da rimuovere

    public DeleteShapeCommand(DrawingModel model, AbstractShape shapeToRemove) {
        this.model = model;
        this.shapeToRemove = shapeToRemove;
    }

    @Override
    public void execute() {
        // L'azione effettiva del comando è di responsabilità del model
        model.removeShape(shapeToRemove);
    }

}