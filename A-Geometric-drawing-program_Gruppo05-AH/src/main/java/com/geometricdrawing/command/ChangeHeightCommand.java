package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

public class ChangeHeightCommand implements Command {
    private final DrawingModel model;
    private final AbstractShape shape;
    private final double newHeight;

    public ChangeHeightCommand(DrawingModel model, AbstractShape shape, double newHeight) {
        this.shape = shape;
        this.newHeight = newHeight;
        this.model = model;
    }

    @Override
    public void execute() {
        // Cambia la larghezza della figura
        model.setShapeHeight(shape, newHeight);
    }

    // TODO: Implementare il metodo undo() per annullare l'azione del comando
}
