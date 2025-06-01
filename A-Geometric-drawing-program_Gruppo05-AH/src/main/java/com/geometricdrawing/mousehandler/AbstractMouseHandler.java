package com.geometricdrawing.mousehandler;

import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.model.AbstractShape;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public abstract class AbstractMouseHandler {
    public static final double SELECTION_THRESHOLD = 5.0;   // distanza per selezionare una figura
    protected static final double BORDER_MARGIN = 5.0;         // margine per il bordo del canvas
    protected static final double VISIBLE_SHAPE_PORTION = 0.1; // porzione visibile della figura quando viene darggata agli estremi
    protected static final double HIDDEN_SHAPE_PORTION = 0.9;  // porzione nascosta della figura quando viene darggata agli estremi

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
    protected abstract void preProcess(MouseEvent event);

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
