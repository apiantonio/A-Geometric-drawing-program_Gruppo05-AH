package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.DrawingModel;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.canvas.Canvas;

public class MousePressedHandler extends AbstractMouseHandler {
    private double x;
    private double y;

    public MousePressedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        x = event.getX();
        y = event.getY();

        controller.getShapeMenu().hide();

        if (currentShape == null || !currentShape.containsPoint(x, y, SELECTION_THRESHOLD)) {
            currentShape = controller.selectShapeAt(x, y);
        }
    }

    @Override
    protected void processEvent(MouseEvent event) {
        if (currentShape != null && currentShape.containsPoint(x, y, SELECTION_THRESHOLD)) {
            // Calcola l'offset usando direttamente il punto cliccato (x, y)
            dragOffsetX = x - currentShape.getX();
            controller.setDragOffsetX(dragOffsetX);
            dragOffsetY = y - currentShape.getY();
            controller.setDragOffsetY(dragOffsetY);
            canvas.setCursor(Cursor.CLOSED_HAND);

            controller.getRootPane().requestFocus();

            if (event.getButton() == MouseButton.SECONDARY) {
                controller.showContextMenu(event);
            }
        } else {
            currentShape = null;
            controller.setCurrentShape(currentShape);
            controller.updateSpinners(currentShape);
        }
    }

    @Override
    protected void postProcess(MouseEvent event) {
        AbstractShape shape = controller.getCurrentShape();
        controller.updateControlState(shape);
        controller.updateSpinners(shape);
        super.postProcess(event);
    }
}
