package com.geometricdrawing.command;

import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.TextShape;

public class ChangeTextContentCommand implements Command {
    private final DrawingModel model;
    private final TextShape shape;
    private final String oldText;
    private final String newText;

    public ChangeTextContentCommand(DrawingModel model, TextShape shape, String newText) {
        this.model = model;
        this.shape = shape;
        this.newText = newText;
        this.oldText = shape.getText();
    }

    @Override
    public void execute() {
        if (model != null && shape != null) {
            model.setText(shape, newText);
        }
    }
    @Override
    public void undo() {
        if (model != null && shape != null) {
            model.setText(shape, oldText);
        }
    }
}
