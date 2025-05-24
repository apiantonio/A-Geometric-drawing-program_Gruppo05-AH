package com.geometricdrawing.command;

import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;

/**
 * Autore: Gruppo05
 * Scopo: Command per spostare la figura in secondo piano
 */
public class BringToBackgroundCommand implements Command{
    private final DrawingModel model;
    private final AbstractShape shape;
    private final int oldZ; // utile per undo

    public BringToBackgroundCommand(DrawingModel model, AbstractShape shape) {
        this.model = model;
        this.shape = shape;
        this.oldZ = shape.getZ();  // per tenere traccia della posizione nella lista di shapes precedente
    }

    @Override
    public void execute() {
        if (shape != null) {
            model.changeZOrder(shape, 0);
        }
    }

    @Override
    public void undo() {
        if (shape != null) {
            // ripristina l'ordine di disposizione originale
            model.changeZOrder(shape, oldZ);
        }
    }
}
