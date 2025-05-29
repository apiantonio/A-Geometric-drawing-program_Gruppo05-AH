package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
// import com.geometricdrawing.ZoomHandler; // Rimuovi se non più usato direttamente
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

public class MouseMovedHandler extends AbstractMouseHandler {
    private double worldX; // Coordinate del mondo
    private double worldY;
    private boolean isOverShape;

    public MouseMovedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        if (controller.getModel() == null) { // Rimosso controllo zoomHandler qui
            System.err.println("Model non inizializzato in MouseMovedHandler!");
            // Gestione errore come in MouseClickedHandler.preProcess
            this.worldX = event.getX(); // Fallback, scorretto con zoom/scroll
            this.worldY = event.getY();
            isOverShape = false;
            return;
        }

        // Utilizza il metodo centralizzato del controller per la conversione delle coordinate
        Point2D worldCoords = controller.canvasToWorldCoordinates(event.getX(), event.getY());
        this.worldX = worldCoords.getX();
        this.worldY = worldCoords.getY();

        //controlla se il mouse é sopra una figura
        isOverShape = controller.getModel().getShapesOrderedByZ().stream()
                .anyMatch(shape -> shape.containsPoint(this.worldX, this.worldY, SELECTION_THRESHOLD)); // SELECTION_THRESHOLD è in AbstractMouseHandler
    }

    @Override
    protected void processEvent(MouseEvent event) {
        if (isOverShape) {
            canvas.setCursor(Cursor.HAND);  //cambia il cursore in una mano
        } else {
            // Se una factory di forme è attiva (modalità creazione), il cursore dovrebbe essere CROSSHAIR
            if (controller.getCurrentShapeFactory() != null) {
                canvas.setCursor(Cursor.CROSSHAIR);
            } else {
                canvas.setCursor(Cursor.DEFAULT);   //Ripristina il cursore predefinito
            }
        }
    }
    // postProcess è ereditato da AbstractMouseHandler (chiama redrawCanvas)
}