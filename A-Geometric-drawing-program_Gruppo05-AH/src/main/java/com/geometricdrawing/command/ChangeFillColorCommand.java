package com.geometricdrawing.command;

import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.model.DrawingModel;
import javafx.scene.paint.Color;

public class ChangeFillColorCommand implements Command {
    private final FillColorDecorator decorator;
    private final Color oldColor;
    private final Color newColor;
    private final DrawingModel model;

    public ChangeFillColorCommand(DrawingModel model, FillColorDecorator decorator, Color newColor) {
        this.model = model;
        this.decorator = decorator;
        this.oldColor = decorator != null ? decorator.getFillColor() : null;
        this.newColor = newColor;
    }

    @Override
    public void execute() {
        if (model != null) {
            model.setFillColor(decorator, newColor);
        }
    }

    @Override
    public void undo() {
        if (model != null) {
            model.setFillColor(decorator, oldColor);
        }
    }
}
