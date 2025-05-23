package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class MouseMovedHandler extends AbstractMouseHandler {
    private double x;
    private double y;
    private boolean isOverShape;

    public MouseMovedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        x = event.getX();
        y = event.getY();

        // Controlla se il mouse è sopra una figura
        isOverShape = controller.getModel().getShapesOrderedByZ().stream()
                .anyMatch(shape -> shape.containsPoint(x, y, SELECTION_THRESHOLD));

    }

    @Override
    protected void processEvent(MouseEvent event) {
        if (isOverShape) {
            canvas.setCursor(Cursor.HAND); // Cambia il cursore in una mano
        } else {
            canvas.setCursor(Cursor.DEFAULT); // Ripristina il cursore predefinito
        }
    }
}
