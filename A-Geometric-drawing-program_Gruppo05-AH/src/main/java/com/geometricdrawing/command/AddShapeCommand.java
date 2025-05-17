package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

public class AddShapeCommand implements Command {
    private final DrawingModel model; // Il "receiver" del comando
    private final AbstractShape shapeToAdd;   // Lo stato necessario per eseguire/annullare

    public AddShapeCommand(DrawingModel model, AbstractShape shapeToAdd) {
        this.model = model;
        this.shapeToAdd = shapeToAdd;
    }

    @Override
    public void execute() {
        // L'azione effettiva del comando
        model.addShape(shapeToAdd);
    }

}