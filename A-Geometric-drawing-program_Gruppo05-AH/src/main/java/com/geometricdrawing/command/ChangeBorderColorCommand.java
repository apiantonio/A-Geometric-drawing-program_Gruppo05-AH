package com.geometricdrawing.command;

import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.model.DrawingModel;
import javafx.scene.paint.Color;

public class ChangeBorderColorCommand implements Command {
    private final BorderColorDecorator decorator;
    private final Color oldColor;
    private final Color newColor;
    private final DrawingModel model;

    public ChangeBorderColorCommand(DrawingModel model, BorderColorDecorator decorator, Color newColor) {
        this.model = model;
        this.decorator = decorator;
        this.oldColor = decorator != null ? decorator.getBorderColor() : null;
        this.newColor = newColor;
    }

    @Override
    public void execute() {
        if (model != null) {
            model.setBorderColor(decorator, newColor);
        }
    }

    @Override
    public void undo() {
        if (model != null) {
            model.setBorderColor(decorator, oldColor);
        }
    }
}