package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.ZoomHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class MouseMovedHandler extends AbstractMouseHandler {
    private double worldX; // Coordinate del mondo
    private double worldY;
    private boolean isOverShape;

    public MouseMovedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        if (controller.getZoomHandler() == null || controller.getModel() == null) {
            System.err.println("ZoomHandler o Model non inizializzato in MouseMovedHandler!");
            this.worldX = event.getX();
            this.worldY = event.getY();
            isOverShape = false;
        } else {
            ZoomHandler zoomHandler = controller.getZoomHandler();
            Point2D worldCoords = zoomHandler.screenToWorld(event.getX(), event.getY());
            this.worldX = worldCoords.getX();
            this.worldY = worldCoords.getY();

            //controlla se il mouse é sopra una figura
            isOverShape = controller.getModel().getShapesOrderedByZ().stream()
                    .anyMatch(shape -> shape.containsPoint(this.worldX, this.worldY, SELECTION_THRESHOLD));
        }
    }

    @Override
    protected void processEvent(MouseEvent event) {
        if (isOverShape) {
            canvas.setCursor(Cursor.HAND);  //cambia il cursore in una mano
        } else {
            canvas.setCursor(Cursor.DEFAULT);   //Ripristina il cursore predefinito
        }
    }
    // postProcess è ereditato da AbstractMouseHandler (chiama redrawCanvas)
}