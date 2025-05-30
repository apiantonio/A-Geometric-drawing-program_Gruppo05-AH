package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.ZoomHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class MouseDraggedHandler extends AbstractMouseHandler {
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

        shapeWidth = currentShape.getWidth();
        shapeHeight = currentShape.getHeight();
    }

    @Override
    protected void processEvent(MouseEvent event) {
        if (currentShape == null || controller.getZoomHandler() == null) {
            return;
        }

        ZoomHandler zoomHandler = controller.getZoomHandler();
        Point2D worldMouseCoords = zoomHandler.screenToWorld(event.getX(), event.getY());

        //calcola le nuove coordinate del mondo
        double newWorldX = worldMouseCoords.getX() - controller.getDragOffsetX();
        double newWorldY = worldMouseCoords.getY() - controller.getDragOffsetY();

        double worldCanvasWidth = canvas.getWidth() / zoomHandler.getZoomFactor();
        double worldCanvasHeight = canvas.getHeight() / zoomHandler.getZoomFactor();

        newWorldX = Math.max(
                AbstractMouseHandler.BORDER_MARGIN - shapeWidth * AbstractMouseHandler.VISIBLE_SHAPE_PORTION,
                Math.min(newWorldX, worldCanvasWidth - shapeWidth * AbstractMouseHandler.HIDDEN_SHAPE_PORTION)
        );

        newWorldY = Math.max(
                AbstractMouseHandler.BORDER_MARGIN - shapeHeight * AbstractMouseHandler.VISIBLE_SHAPE_PORTION,
                Math.min(newWorldY, worldCanvasHeight - shapeHeight * AbstractMouseHandler.HIDDEN_SHAPE_PORTION )
        );
        //crea ed esegue il comando di spostamento
        //MoveShapeCommand moveCmd = new MoveShapeCommand(controller.getModel(), currentShape, newWorldX, newWorldY);
        //controller.getCommandManager().executeCommand(moveCmd);
        // se il drag è appena cominciato imposta le variabili del controller
        if (!controller.isDragging()) {
            controller.setStartDragX(event.getX());
            controller.setStartDragY(event.getY());
        }

        controller.getModel().moveShapeTo(currentShape, newWorldX, newWorldY);
        controller.redrawCanvas();
    }
}