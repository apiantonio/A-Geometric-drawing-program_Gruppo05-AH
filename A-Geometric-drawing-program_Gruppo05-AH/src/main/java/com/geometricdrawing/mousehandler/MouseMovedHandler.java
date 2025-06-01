package com.geometricdrawing.mousehandler;

import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.controller.HandleType;
import com.geometricdrawing.controller.ZoomHandler;
import com.geometricdrawing.model.AbstractShape;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class MouseMovedHandler extends AbstractMouseHandler {
    private double worldX;
    private double worldY;

    public MouseMovedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        ZoomHandler zoomHandler = controller.getZoomHandler();
        if (zoomHandler == null) {
            this.worldX = event.getX();
            this.worldY = event.getY();
        } else {
            Point2D worldCoords = zoomHandler.screenToWorld(event.getX(), event.getY());
            this.worldX = worldCoords.getX();
            this.worldY = worldCoords.getY();
        }
    }

    @Override
    protected void processEvent(MouseEvent event) {
        // se il mouse è in movimento ma non si sta trascinando o ridimensionando, aggiorna il cursore
        if (controller.isDragging() || controller.getActiveResizeHandle() != null) {
            return;
        }

        AbstractShape currentSelectedShape = controller.getCurrentShape();
        HandleType handleUnderMouse = null;

        if (currentSelectedShape != null) {
            // Controlla se il mouse è sopra un handle della shape selezionata
            handleUnderMouse = controller.getHandleAtScreenPoint(currentSelectedShape, event.getX(), event.getY());
        }

        if (handleUnderMouse != null) {
            canvas.setCursor(controller.getCursorForHandle(handleUnderMouse, currentSelectedShape));
        } else {
            // Se non c'è un handle sotto il mouse, controlla se il mouse è sopra una shape
            boolean isOverAnyShape = false;
            if (controller.getModel() != null) {
                isOverAnyShape = controller.getModel().getShapesOrderedByZ().stream()
                        .anyMatch(shape -> shape.containsPoint(this.worldX, this.worldY, SELECTION_THRESHOLD));
            }

            if (isOverAnyShape) {
                canvas.setCursor(Cursor.HAND);
            } else {
                canvas.setCursor(Cursor.DEFAULT);
            }
        }
    }

}