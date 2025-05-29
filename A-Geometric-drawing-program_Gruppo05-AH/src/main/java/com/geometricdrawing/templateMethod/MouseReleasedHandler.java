package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.command.MoveShapeCommand;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class MouseReleasedHandler extends AbstractMouseHandler {
    private double finalWorldX; // Coordinate finali del mondo della forma dopo il drag
    private double finalWorldY;

    public MouseReleasedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        currentShape = controller.getCurrentShape();
        if (currentShape != null && controller.isDragging()) {
            // Le coordinate finali della forma sono già in unità del mondo
            // perché currentShape.getX/Y sono coordinate del mondo.
            finalWorldX = currentShape.getX();
            finalWorldY = currentShape.getY();
        }
    }

    @Override
    protected void processEvent(MouseEvent event) {
        if (currentShape != null && controller.isDragging()) {
            // Crea il comando usando la posizione iniziale del drag (già in coordinate del mondo)
            // e la posizione finale della figura (anch'essa in coordinate del mondo).
            // finalWorldX e finalWorldY sono state catturate in preProcess.
            MoveShapeCommand moveCmd = new MoveShapeCommand(controller.getModel(), currentShape, finalWorldX, finalWorldY);

            // Le coordinate oldX/oldY per l'undo del comando sono state memorizzate
            // dal controller quando il drag è iniziato (in MousePressedHandler).
            double oldXVal = controller.getInitialDragShapeX_world();
            double oldYVal = controller.getInitialDragShapeY_world();

            moveCmd.setOldX(oldXVal);
            moveCmd.setOldY(oldYVal);

            controller.getCommandManager().executeCommand(moveCmd);
        }
    }

    @Override
    protected void postProcess(MouseEvent event) {
        controller.resetDrag(); // resetta il dragging

        // Ripristina il cursore in base allo stato corrente
        // (es. se il mouse è sopra una forma, dovrebbe essere HAND, altrimenti DEFAULT o CROSSHAIR)
        // Questa logica è simile a quella in MouseMovedHandler.processEvent
        Point2D worldMouseCoords = controller.canvasToWorldCoordinates(event.getX(), event.getY());
        boolean isOverShape = controller.getModel().getShapesOrderedByZ().stream()
                .anyMatch(shape -> shape.containsPoint(worldMouseCoords.getX(), worldMouseCoords.getY(), SELECTION_THRESHOLD));

        if (isOverShape && controller.getCurrentShapeFactory() == null) { // Solo se non in modalità creazione
            canvas.setCursor(Cursor.HAND);
        } else if (controller.getCurrentShapeFactory() != null) {
            canvas.setCursor(Cursor.CROSSHAIR);
        } else {
            canvas.setCursor(Cursor.DEFAULT);
        }

        super.postProcess(event); // Chiama redrawCanvas
    }
}