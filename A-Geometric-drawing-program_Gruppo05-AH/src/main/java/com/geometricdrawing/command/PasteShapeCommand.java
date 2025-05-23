package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

/**
 * Scopo: Command per incollare una figura dalla clipboard nel modello.
 */
public class PasteShapeCommand implements Command {
    private final DrawingModel model;
    private final ClipboardManager clipboardManager;
    private AbstractShape pastedShape; // To store the shape for undo
    private final double offsetX; // Offset X for pasting
    private final double offsetY; // Offset Y for pasting

    private static final double DEFAULT_OFFSET = 10.0;

    public PasteShapeCommand(DrawingModel model, ClipboardManager clipboardManager, double offsetX, double offsetY) {
        this.model = model;
        this.clipboardManager = clipboardManager;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public PasteShapeCommand(DrawingModel model, ClipboardManager clipboardManager) {
        this(model, clipboardManager, DEFAULT_OFFSET, DEFAULT_OFFSET);
    }

    @Override
    public void execute() {
        if (clipboardManager.hasContent()) {
            this.pastedShape = clipboardManager.getFromClipboard();
            if (this.pastedShape != null) {
                // Applicare offset alla figura rispetto alle coordinate iniziali
                this.pastedShape.moveTo(this.pastedShape.getX() + offsetX, this.pastedShape.getY() + offsetY);
                model.addShape(this.pastedShape);
            }
        } else {
            this.pastedShape = null;
        }
    }

    // Getter utile per il controller
    public AbstractShape getPastedShape() {
        return pastedShape;
    }

}