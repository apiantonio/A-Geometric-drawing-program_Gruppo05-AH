package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.command.MoveShapeCommand;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class MouseReleasedHandler extends AbstractMouseHandler {
    private double x;
    private double y;

    public MouseReleasedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        currentShape = controller.getCurrentShape();
    }

    @Override
    protected void processEvent(MouseEvent event) {
        if (currentShape != null && controller.isDragging()) {
            // Usa le coordinate attuali della figura come posizione finale
            x = currentShape.getX();
            y = currentShape.getY();

            // Crea il comando usando la posizione iniziale del drag e la posizione finale della figura
            MoveShapeCommand moveCmd = new MoveShapeCommand(controller.getModel(), currentShape, x, y);

            moveCmd.setOldX(controller.getStartDragX() - controller.getDragOffsetX());
            moveCmd.setOldY(controller.getStartDragY() - controller.getDragOffsetY());

            controller.getCommandManager().executeCommand(moveCmd);
        }
    }

        @Override
    protected void postProcess(MouseEvent event) {
        controller.resetDrag(); // resetta il dragging
        canvas.setCursor(Cursor.DEFAULT);
        super.postProcess(event);
    }
}
