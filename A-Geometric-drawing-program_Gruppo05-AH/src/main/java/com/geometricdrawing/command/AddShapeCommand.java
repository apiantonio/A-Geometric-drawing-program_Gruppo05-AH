package com.geometricdrawing.command;

import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Shape;

public class AddShapeCommand implements Command {
    private final DrawingModel model; // Il "receiver" del comando
    private final Shape shapeToAdd;   // Lo stato necessario per eseguire/annullare

    public AddShapeCommand(DrawingModel model, Shape shapeToAdd) {
        this.model = model;
        this.shapeToAdd = shapeToAdd;
    }

    @Override
    public void execute() {
        // L'azione effettiva del comando
        model.addShape(shapeToAdd);
    }

}