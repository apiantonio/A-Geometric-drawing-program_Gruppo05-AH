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

        // Verifica se siamo in modalità creazione
        if (controller.getCurrentShapeFactory() != null) {
            // Non selezionare figure esistenti se stiamo creando una nuova figura
            return;
        }

        currentShape = controller.getCurrentShape();
        if (currentShape == null || !currentShape.containsPoint(this.worldX, this.worldY, SELECTION_THRESHOLD)) {
            currentShape = controller.selectShapeAt(this.worldX, this.worldY);
            controller.setCurrentShape(currentShape);
        }
    }

    @Override
    protected void processEvent(MouseEvent event) {
        // currentShape qui si riferisce alla variabile di istanza di MousePressedHandler,
        // che è stata impostata in preProcess.
        if (currentShape != null && currentShape.containsPoint(worldX, worldY, SELECTION_THRESHOLD)) {
            // Il clic è su una forma esistente (o quella appena selezionata in preProcess)
            // currentShape è già stato determinato in preProcess e trasformato in coordinate del mondo
            controller.setInitialDragShapeX_world(currentShape.getX());
            controller.setInitialDragShapeY_world(currentShape.getY());

            dragOffsetX = this.worldX - currentShape.getX();
            controller.setDragOffsetX(dragOffsetX);
            dragOffsetY = this.worldY - currentShape.getY();
            controller.setDragOffsetY(dragOffsetY);
            canvas.setCursor(Cursor.CLOSED_HAND);

            controller.getRootPane().requestFocus();
            if (event.getButton() == MouseButton.SECONDARY) {
                controller.showContextMenu(event);
            }
            // Calcola l'offset per il trascinamento
            dragOffsetX = worldX - currentShape.getX();
            controller.setDragOffsetX(dragOffsetX);
            dragOffsetY = worldY - currentShape.getY();
            controller.setDragOffsetY(dragOffsetY);
            canvas.setCursor(Cursor.CLOSED_HAND);

            controller.getRootPane().requestFocus();

            // Se è un clic con il pulsante secondario, mostra il menu contestuale della forma
            if (event.getButton() == MouseButton.SECONDARY) {
                // Memorizza le coordinate del clic destro, potrebbero servire per "Incolla qui" dal menu della forma
                controller.showContextMenu(event);
            }
        }
    }

    @Override
    protected void postProcess(MouseEvent event) {
        controller.updateControlState(controller.getCurrentShape());
        controller.updateSpinners(controller.getCurrentShape());
        super.postProcess(event); // Questo chiama redrawCanvas
    }
}
