package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

/**
 * Autore: Gruppo05
 * Scopo: Command per l'aggiunta di una nuova figura
 */
public class AddShapeCommand implements Command {
    private final DrawingModel model; // Il "receiver" del comando
    private final AbstractShape shape;   // Lo stato necessario per eseguire/annullare

    public AddShapeCommand(DrawingModel model, AbstractShape shapeToAdd) {
        this.model = model;
        this.shape = shapeToAdd;
    }

    @Override
    public void execute() {
        // L'azione effettiva del comando
        model.addShape(shape);
    }

    @Override
    public void undo() {
        // L'azione effettiva del comando
        model.removeShape(shape);
    }

}