package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.command.MoveShapeCommand;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class MouseDraggedHandler extends AbstractMouseHandler {
    private double canvasWidth;
    private double canvasHeight;
    private double shapeWidth;
    private double shapeHeight;

    public MouseDraggedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        currentShape = controller.getCurrentShape();

        if (currentShape == null) {
            return;
        }

        // Calcola le dimensioni del canvas e della forma
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        shapeWidth = currentShape.getWidth();
        shapeHeight = currentShape.getHeight();

        // Imposta il cursore a mano chiusa
//        canvas.setCursor(javafx.scene.Cursor.CLOSED_HAND);
    }

    @Override
    protected void processEvent(MouseEvent event) {
        if (currentShape == null) {
            return;
        }

        // Calcola le nuove coordinate considerando l'offset di trascinamento
        double newX = event.getX() - controller.getDragOffsetX();
        double newY = event.getY() - controller.getDragOffsetY();

        // Limita le coordinate per mantenere la figura all'interno del canvas
        newX = Math.max(BORDER_MARGIN - shapeWidth * VISIBLE_SHAPE_PORTION,
                Math.min(newX, canvasWidth - shapeWidth * HIDDEN_SHAPE_PORTION));
        newY = Math.max(BORDER_MARGIN - shapeHeight * VISIBLE_SHAPE_PORTION,
                Math.min(newY, canvasHeight - shapeHeight * HIDDEN_SHAPE_PORTION));

        // se il drag è appena cominciato imposta le variabili del controller
        if (!controller.isDragging()) {
            controller.setStartDragX(event.getX());
            controller.setStartDragY(event.getY());
        }

        controller.getModel().moveShapeTo(currentShape, newX, newY);
        controller.redrawCanvas();
    }
}