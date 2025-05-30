package com.geometricdrawing.command;

import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.TextShape;

/**
 * Command per cambiare la grandezza del font di un TextShape.
 */
public class ChangeFontSizeCommand implements Command {
    private final DrawingModel model;
    private final TextShape shape;
    private final int oldSize;
    private final int newSize;

    public ChangeFontSizeCommand(DrawingModel model, TextShape shape, int newSize) {
        this.model = model;
        this.shape = shape;
        this.newSize = newSize;
        this.oldSize = shape.getFontSize();
    }

    @Override
    public void execute() {
        if (model != null && shape != null) {
            model.setFontSize(shape, newSize);
        }
    }

    @Override
    public void undo() {
        if (model != null && shape != null) {
            model.setFontSize(shape, oldSize);
        }
    }
}