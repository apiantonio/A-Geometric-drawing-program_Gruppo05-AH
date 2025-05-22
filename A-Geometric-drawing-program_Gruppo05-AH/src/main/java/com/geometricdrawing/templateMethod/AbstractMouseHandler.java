package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Line;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public abstract class AbstractMouseHandler {
    protected static final double SELECTION_THRESHOLD = 5.0;
    protected static final double BORDER_MARGIN = 5.0;
    protected static final double VISIBLE_SHAPE_PORTION = 0.1;
    protected static final double HIDDEN_SHAPE_PORTION = 0.9;

    protected final Canvas canvas;
    protected AbstractShape currentShape;
    protected final DrawingController controller;
    protected double dragOffsetX;
    protected double dragOffsetY;

    public AbstractMouseHandler(Canvas canvas, DrawingController controller) {
        this.canvas = canvas;
        this.controller = controller;
        this.currentShape = controller.getCurrentShape();
    }

    // template method per gestire gli eventi del mouse
    public final void handleMouseEvent(MouseEvent event) {
        preProcess(event);
        processEvent(event);
        postProcess(event);
    }

    // Hook methods
    protected void preProcess(MouseEvent event) {
        // Implementazione di default vuota
    }

    // Metodo astratto che le sottoclassi devono implementare
    protected abstract void processEvent(MouseEvent event);

    protected void postProcess(MouseEvent event) {
        controller.redrawCanvas();
    }

    public AbstractShape getCurrentShape() {
        return currentShape;
    }

    public void setCurrentShape(AbstractShape shape) {
        this.currentShape = shape;
    }
}
