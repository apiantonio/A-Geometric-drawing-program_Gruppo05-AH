package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

/**
 * Autore: Gruppo05
 * Scopo: Classe necessaria per il comando di mirroring di una figura.
 */
public class MirrorShapeCommand implements Command {
    private final DrawingModel model;
    private final AbstractShape shape;
    private final boolean horizontal; // true per mirroring orizzontale, false per verticale

    public MirrorShapeCommand(DrawingModel model, AbstractShape shapeToMirror, boolean horizontal) {
        this.model = model;
        this.shape = shapeToMirror;
        this.horizontal = horizontal;
    }

    @Override
    public void execute() {
        model.mirrorShape(shape, horizontal);
    }

    @Override
    public void undo() {
        model.mirrorShape(shape, horizontal);
    }

}
