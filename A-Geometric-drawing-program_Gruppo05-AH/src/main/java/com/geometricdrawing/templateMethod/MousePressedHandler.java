package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
// import com.geometricdrawing.ZoomHandler; // Rimuovi se non più usato direttamente
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.canvas.Canvas;

public class MousePressedHandler extends AbstractMouseHandler {
    private double worldX; // Coordinate del mondo del mouse press
    private double worldY;

    public MousePressedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        // Non è necessario controllare zoomHandler qui se canvasToWorldCoordinates lo gestisce
        // o se il controller non è utilizzabile senza di esso.
        // Il controller dovrebbe garantire che i suoi componenti siano pronti.
        if (controller == null) { // Controllo base
            System.err.println("DrawingController nullo in MousePressedHandler!");
            this.worldX = event.getX(); // Fallback
            this.worldY = event.getY();
            return;
        }

        // Utilizza il metodo centralizzato del controller per la conversione delle coordinate
        Point2D worldCoords = controller.canvasToWorldCoordinates(event.getX(), event.getY());
        this.worldX = worldCoords.getX();
        this.worldY = worldCoords.getY();

        if (controller.getShapeMenu() != null) { // Controlla se shapeMenu è inizializzato
            controller.getShapeMenu().hide();
        }


        // Verifica se siamo in modalità creazione
        if (controller.getCurrentShapeFactory() != null) {
            // Non selezionare figure esistenti se stiamo creando una nuova figura
            return;
        }

        // Tenta di selezionare una figura solo se non siamo in modalità creazione.
        // currentShape è la variabile di istanza di AbstractMouseHandler.
        // La logica di selezione è già in worldX, worldY.
        currentShape = controller.getCurrentShape();
        if (currentShape == null || !currentShape.containsPoint(this.worldX, this.worldY, SELECTION_THRESHOLD)) {
            currentShape = controller.selectShapeAt(this.worldX, this.worldY);
            // controller.setCurrentShape(currentShape); // selectShapeAt dovrebbe già impostare la currentShape nel controller
        }
        // Se currentShape è ancora null dopo il tentativo di selezione, non c'è altro da fare qui.
    }

    @Override
    protected void processEvent(MouseEvent event) {
        // currentShape si riferisce alla variabile di istanza (impostata in preProcess o già esistente).
        // this.worldX e this.worldY sono le coordinate del mondo del mouse press.
        if (currentShape != null && currentShape.containsPoint(worldX, worldY, SELECTION_THRESHOLD)) {
            // Il clic è su una forma esistente (o quella appena selezionata in preProcess)

            // Salva le coordinate iniziali della forma (in coordinate del mondo) per il comando Move
            controller.setInitialDragShapeX_world(currentShape.getX());
            controller.setInitialDragShapeY_world(currentShape.getY());

            // Calcola l'offset per il trascinamento (in coordinate del mondo)
            dragOffsetX = this.worldX - currentShape.getX();
            controller.setDragOffsetX(dragOffsetX);
            dragOffsetY = this.worldY - currentShape.getY();
            controller.setDragOffsetY(dragOffsetY);

            canvas.setCursor(Cursor.CLOSED_HAND);
            if (controller.getRootPane() != null) { // Controlla se rootPane è inizializzato
                controller.getRootPane().requestFocus();
            }


            if (event.getButton() == MouseButton.SECONDARY) {
                if (controller.getShapeMenu() != null) { // Controlla prima di usare
                    controller.showContextMenu(event);
                }
            }
        } else {
            // Clic su un'area vuota (e non in modalità creazione)
            // Deseleziona la forma corrente se si clicca fuori.
            // Questo comportamento è gestito da selectShapeAt(this.worldX, this.worldY) in preProcess
            // se nessuna forma viene trovata, currentShape diventerà null e il controller verrà aggiornato.
            // Non è necessario deselezionare esplicitamente qui se preProcess lo gestisce.
            // controller.setCurrentShape(null); // Potrebbe essere ridondante se selectShapeAt(..) lo fa
        }
    }

    @Override
    protected void postProcess(MouseEvent event) {
        // currentShape qui è quello determinato in preProcess.
        // selectShapeAt (chiamato in preProcess) già aggiorna lo stato del controller.
        // Quindi updateControlState e updateSpinners dovrebbero riflettere la selezione corrente (o la sua assenza).
        controller.updateControlState(controller.getCurrentShape()); // Usa la shape dal controller
        controller.updateSpinners(controller.getCurrentShape());   // Usa la shape dal controller
        super.postProcess(event); // Questo chiama redrawCanvas
    }
}