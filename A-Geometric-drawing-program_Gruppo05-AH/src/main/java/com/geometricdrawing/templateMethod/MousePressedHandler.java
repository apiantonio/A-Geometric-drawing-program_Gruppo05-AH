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

    private double lastContextMouseX;
    private double lastContextMouseY;

    public MousePressedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        x = event.getX();
        y = event.getY();

        controller.getShapeMenu().hide();

        currentShape = controller.getCurrentShape();
        if (currentShape == null || !currentShape.containsPoint(x, y, SELECTION_THRESHOLD)) {
            currentShape = controller.selectShapeAt(x, y);
        }
    }

    @Override
    protected void processEvent(MouseEvent event) {
        // currentShape qui si riferisce alla variabile di istanza di MousePressedHandler,
        // che è stata impostata in preProcess.
        if (currentShape != null && currentShape.containsPoint(x, y, SELECTION_THRESHOLD)) { //
            // Il clic è su una forma esistente (o quella appena selezionata in preProcess)

            // Calcola l'offset per il trascinamento
            dragOffsetX = x - currentShape.getX(); //
            controller.setDragOffsetX(dragOffsetX); //
            dragOffsetY = y - currentShape.getY(); //
            controller.setDragOffsetY(dragOffsetY); //
            canvas.setCursor(Cursor.CLOSED_HAND); //

            controller.getRootPane().requestFocus(); //

            // Se è un clic con il pulsante secondario, mostra il menu contestuale della forma
            if (event.getButton() == MouseButton.SECONDARY) { //
                // Memorizza le coordinate del clic destro, potrebbero servire per "Incolla qui" dal menu della forma
                // controller.setLastContextMousePosition(x, y); // Se vuoi usare questo per il menu delle forme
                controller.showContextMenu(event); //
            }
            // Aggiorna la forma corrente nel controller con quella identificata
            controller.setCurrentShape(currentShape);

        } else {
            // Il clic NON è su una forma esistente (o nessuna forma è stata identificata in preProcess).
            // Potrebbe essere un clic su un'area vuota.
            // Deseleziona la figura corrente nel controller SOLO SE non siamo in modalità
            // di creazione di una nuova forma (cioè, se currentShapeFactory nel controller è null).
            // Se una factory è attiva, il MouseClickedHandler gestirà la creazione e la selezione.
            if (controller.getCurrentShapeFactory() == null) {
                currentShape = null; // Imposta la currentShape locale del handler a null
                controller.setCurrentShape(null); // Aggiorna il controller deselezionando la figura
                // controller.updateSpinners(null); // updateSpinners sarà chiamato da postProcess o da updateControlState
            }
            // Se una factory è attiva, non facciamo nulla qui riguardo la currentShape,
            // perché MouseClickedHandler la imposterà alla nuova forma creata.
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
