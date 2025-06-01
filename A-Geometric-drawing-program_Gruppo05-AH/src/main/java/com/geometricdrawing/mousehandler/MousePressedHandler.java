package com.geometricdrawing.mousehandler;

import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.controller.HandleType; // Import HandleType
import com.geometricdrawing.controller.ZoomHandler;
import com.geometricdrawing.model.AbstractShape;
import javafx.geometry.Point2D;
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
        currentShape = controller.getCurrentShape();

        ZoomHandler zoomHandler = controller.getZoomHandler();
        if (zoomHandler == null) {
            this.worldX = event.getX();
            this.worldY = event.getY();
        } else {
            Point2D worldCoords = zoomHandler.screenToWorld(event.getX(), event.getY());
            this.worldX = worldCoords.getX();
            this.worldY = worldCoords.getY();
        }

        if (controller.getShapeMenu() != null) {
            controller.getShapeMenu().hide();
        }

        // --- controllo resize handle ---
        if (currentShape != null) { // Controllo se c'è una shape selezionata
            HandleType E_activeResizeHandle = controller.getHandleAtScreenPoint(currentShape, event.getX(), event.getY());
            if (E_activeResizeHandle != null) {
                controller.setActiveResizeHandle(E_activeResizeHandle);
                controller.setResizeStartMousePos_screen(new Point2D(event.getX(), event.getY()));
                controller.storeInitialResizeState(currentShape); // Salva lo stato iniziale della shape per il resize

                if (controller.getRootPane() != null) controller.getRootPane().requestFocus();
                return; // operazione di resize in corso
            }
        }


        if (controller.getCurrentShapeFactory() != null) {
            return;
        }

        // Prova a selezionare una shape sotto il mouse
        AbstractShape shapeUnderMouse = controller.selectShapeAt(this.worldX, this.worldY);
        // selectShapeAt updates controller.currentShape and UI.

        currentShape = controller.getCurrentShape(); // Aggiorna la variabile locale currentShape dopo selectShapeAt

        if (currentShape != null) { // Se ora una shape è selezionata (nuova o già selezionata)
            // Se il click è col tasto primario sulla shape (non su un handle), prepara il trascinamento dell'intera shape.
            if (event.getButton() == MouseButton.PRIMARY && currentShape.containsPoint(this.worldX, this.worldY, SELECTION_THRESHOLD)) {
                controller.setInitialDragShapeX_world(currentShape.getX());
                controller.setInitialDragShapeY_world(currentShape.getY());

                // dragOffset è la differenza tra mouse_world e origine della shape nel mondo
                dragOffsetX = this.worldX - currentShape.getX();
                controller.setDragOffsetX(dragOffsetX);
                dragOffsetY = this.worldY - currentShape.getY();
                controller.setDragOffsetY(dragOffsetY);
            }
            // Richiede il focus sul rootPane se presente
            if (controller.getRootPane() != null) controller.getRootPane().requestFocus();
            // Se il click è col tasto secondario, mostra il menu contestuale per la shape
            if (event.getButton() == MouseButton.SECONDARY) {
                controller.showContextMenu(event);
            }
        }
        // Se shapeUnderMouse era null, currentShape nel controller ora è null (deselezionata).
    }

    @Override
    protected void processEvent(MouseEvent event) {
        // La maggiore parte della logica è già gestita in preProcess.
    }

    @Override
    protected void postProcess(MouseEvent event) {
        // Aggiorna
        if (controller.getActiveResizeHandle() != null && controller.getShapeBeingResized() != null) {
            controller.updateControlState(controller.getShapeBeingResized());
            controller.updateSpinners(controller.getShapeBeingResized());
        } else if (controller.getCurrentShapeFactory() == null) {
            controller.updateControlState(controller.getCurrentShape());
            controller.updateSpinners(controller.getCurrentShape());
        } else {
            controller.updateControlState(null);
            controller.updateSpinners(null);
        }
        super.postProcess(event); // chiama redrawCanvas
    }
}