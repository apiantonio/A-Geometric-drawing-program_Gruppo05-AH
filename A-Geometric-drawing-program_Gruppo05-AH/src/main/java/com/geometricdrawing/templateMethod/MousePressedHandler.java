package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.ZoomHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.canvas.Canvas;

public class MousePressedHandler extends AbstractMouseHandler {
    private double worldX;
    private double worldY;

    public MousePressedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        if (controller.getZoomHandler() == null) {
            System.err.println("ZoomHandler non inizializzato in MousePressedHandler!");
            this.worldX = event.getX();
            this.worldY = event.getY();
        } else {
            ZoomHandler zoomHandler = controller.getZoomHandler();
            Point2D worldCoords = zoomHandler.screenToWorld(event.getX(), event.getY());
            this.worldX = worldCoords.getX();
            this.worldY = worldCoords.getY();
        }

        controller.getShapeMenu().hide();

        currentShape = controller.getCurrentShape();
        if (currentShape == null || !currentShape.containsPoint(this.worldX, this.worldY, SELECTION_THRESHOLD)) {
            currentShape = controller.selectShapeAt(this.worldX, this.worldY);
            controller.setCurrentShape(currentShape);
        }
    }

    @Override
    protected void processEvent(MouseEvent event) {
        // currentShape è già stato determinato in preProcess e trasformato in coordinate del mondo
        if (currentShape != null && currentShape.containsPoint(this.worldX, this.worldY, SELECTION_THRESHOLD)) {
            dragOffsetX = this.worldX - currentShape.getX();
            controller.setDragOffsetX(dragOffsetX);
            dragOffsetY = this.worldY - currentShape.getY();
            controller.setDragOffsetY(dragOffsetY);
            canvas.setCursor(Cursor.CLOSED_HAND);

            controller.getRootPane().requestFocus();

            if (event.getButton() == MouseButton.SECONDARY) {
                controller.showContextMenu(event);
            }
        } else {
            // Deseleziona la forma corrente nel controller
            currentShape = null; // Aggiorna la copia locale
            controller.setCurrentShape(null);
            // updateSpinners e updateControlState verranno chiamati in postProcess
        }
    }

    @Override
    protected void postProcess(MouseEvent event) {
        controller.updateControlState(controller.getCurrentShape());
        controller.updateSpinners(controller.getCurrentShape());
        super.postProcess(event); // Questo chiama redrawCanvas
    }
}