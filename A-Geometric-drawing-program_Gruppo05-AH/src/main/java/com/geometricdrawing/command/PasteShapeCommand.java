package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

public class PasteShapeCommand implements Command {
    private static final double DEFAULT_OFFSET = 10.0;
    private static int pasteCount = 0; // Contatore statico per tenere traccia delle incollature
    private final DrawingModel model;
    private final ClipboardManager clipboardManager;
    private AbstractShape pastedShape;
    private double targetX; // Usato solo se useAbsoluteCoordinates è true
    private double targetY; // Usato solo se useAbsoluteCoordinates è true
    private final boolean useAbsoluteCoordinates;

    // Costruttore per incollare con offset di default
    public PasteShapeCommand(DrawingModel model, ClipboardManager clipboardManager) {
        this.model = model;
        this.clipboardManager = clipboardManager;
        this.useAbsoluteCoordinates = false;
    }

    // Costruttore per incollare a coordinate assolute
    public PasteShapeCommand(DrawingModel model, ClipboardManager clipboardManager, double targetX, double targetY, boolean useAbsoluteCoordinates) {
        this.model = model;
        this.clipboardManager = clipboardManager;
        this.targetX = targetX;
        this.targetY = targetY;
        this.useAbsoluteCoordinates = useAbsoluteCoordinates;
    }

    @Override
    public void execute() {
        if (clipboardManager.hasContent()) { //
            this.pastedShape = clipboardManager.getFromClipboard(); // Già un clone profondo
            if (this.pastedShape != null) {
                if (useAbsoluteCoordinates) {
                    model.moveShapeTo(pastedShape, targetX, targetY);
                } else {
                    // Applica offset cumulativo alla posizione originale della forma incollata
                    pasteCount++;
                    double offsetX = this.pastedShape.getX() + (DEFAULT_OFFSET * pasteCount);
                    double offsetY = this.pastedShape.getY() + (DEFAULT_OFFSET * pasteCount);
                    model.moveShapeTo(this.pastedShape, offsetX, offsetY);
                }
                model.addShape(this.pastedShape); //
            }
        } else {
            this.pastedShape = null;
        }
    }

    // Metodo per resettare il contatore (ad esempio quando viene copiata una nuova forma)
    public static void resetPasteCount() {
        pasteCount = 0;
    }

    public AbstractShape getPastedShape() {
        return pastedShape;
    }

    @Override
    public void undo() {
        if (pastedShape != null && model != null) {
            model.removeShape(pastedShape);
        }
    }
}