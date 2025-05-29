package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
// Rimuovi com.geometricdrawing.command.MoveShapeCommand se non più usato direttamente
// import com.geometricdrawing.ZoomHandler; // Rimuovi se non più usato direttamente
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

        // Imposta il cursore a mano chiusa
//        canvas.setCursor(javafx.scene.Cursor.CLOSED_HAND);
    }


    @Override
    protected void processEvent(MouseEvent event) {
        if (currentShape == null) { // Rimosso controllo su zoomHandler qui, il controller gestirà la conversione
            return;
        }

        // Utilizza il metodo centralizzato del controller per la conversione delle coordinate
        Point2D worldMouseCoords = controller.canvasToWorldCoordinates(event.getX(), event.getY());

        //calcola le nuove coordinate del mondo
        // dragOffsetX e dragOffsetY sono già in coordinate del mondo, calcolati in MousePressedHandler
        double newWorldX = worldMouseCoords.getX() - controller.getDragOffsetX();
        double newWorldY = worldMouseCoords.getY() - controller.getDragOffsetY();

        // Use base world dimensions from controller for boundary checks
        double baseWorldWidth = controller.getBaseWorldWidth();
        double baseWorldHeight = controller.getBaseWorldHeight();

        // La logica di clamping rimane la stessa, poiché opera su coordinate del mondo
        newWorldX = Math.max(
                AbstractMouseHandler.BORDER_MARGIN - shapeWidth * AbstractMouseHandler.VISIBLE_SHAPE_PORTION,
                Math.min(newWorldX, baseWorldWidth - shapeWidth * AbstractMouseHandler.HIDDEN_SHAPE_PORTION)
        );

        newWorldY = Math.max(
                AbstractMouseHandler.BORDER_MARGIN - shapeHeight * AbstractMouseHandler.VISIBLE_SHAPE_PORTION,
                Math.min(newWorldY, baseWorldHeight - shapeHeight * AbstractMouseHandler.HIDDEN_SHAPE_PORTION )
        );

        if (!controller.isDragging()) {
            controller.setStartDragX(event.getX()); // Queste sono coordinate schermo, usate per determinare se un drag è iniziato
            controller.setStartDragY(event.getY()); // Potrebbero non essere necessarie se 'isDragging' è gestito diversamente
            // o se le posizioni iniziali sono memorizzate in termini di mondo.
            // initialDragShapeX_world è già impostato in MousePressedHandler.
        }

        controller.getModel().moveShapeTo(currentShape, newWorldX, newWorldY);
        // redrawCanvas è chiamato in postProcess
        // controller.redrawCanvas(); // Spostato in postProcess da AbstractMouseHandler
    }

    // postProcess è ereditato da AbstractMouseHandler (chiama redrawCanvas)
}