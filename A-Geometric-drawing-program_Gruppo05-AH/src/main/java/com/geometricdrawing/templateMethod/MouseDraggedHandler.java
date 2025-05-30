package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.command.MoveShapeCommand;
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
        if (currentShape == null || controller.getZoomHandler() == null || controller.getHorizontalScrollBar() == null || controller.getVerticalScrollBar() == null) {
            return;
        }

        ZoomHandler zoomHandler = controller.getZoomHandler();
        Point2D worldMouseCoords = zoomHandler.screenToWorld(event.getX(), event.getY());

        // Calcola le nuove coordinate del mondo per l'angolo in alto a sinistra della forma
        double newWorldX = worldMouseCoords.getX() - controller.getDragOffsetX();
        double newWorldY = worldMouseCoords.getY() - controller.getDragOffsetY();

        // Larghezza e altezza del viewport in unità del mondo
        double worldCanvasWidth = canvas.getWidth() / zoomHandler.getZoomFactor();
        double worldCanvasHeight = canvas.getHeight() / zoomHandler.getZoomFactor();

        // Posizione corrente dello scroll in unità del mondo
        double currentScrollX = controller.getHorizontalScrollBar().getValue();
        double currentScrollY = controller.getVerticalScrollBar().getValue();

        // Margine del bordo convertito in unità del mondo (BORDER_MARGIN è 5.0)
        double effectiveBorderMargin = AbstractMouseHandler.BORDER_MARGIN / zoomHandler.getZoomFactor();

        // Calcola i limiti minimi e massimi consentiti per newWorldX in coordinate assolute del mondo,
        // preservando la logica originale di VISIBLE_SHAPE_PORTION e HIDDEN_SHAPE_PORTION
        // e l'asimmetria nell'applicazione del margine.

        // Limite inferiore: consente a VISIBLE_SHAPE_PORTION della larghezza della forma di essere a sinistra
        // del bordo sinistro del viewport (currentScrollX + effectiveBorderMargin).
        // Quindi, la coordinata X della forma (newWorldX) può arrivare fino a:
        // currentScrollX + effectiveBorderMargin - (shapeWidth * VISIBLE_SHAPE_PORTION)
        double minClampedX = currentScrollX + effectiveBorderMargin - shapeWidth * AbstractMouseHandler.VISIBLE_SHAPE_PORTION;

        // Limite superiore: consente a VISIBLE_SHAPE_PORTION della larghezza della forma di essere a destra
        // del bordo destro del viewport (currentScrollX + worldCanvasWidth).
        // Il che significa che HIDDEN_SHAPE_PORTION della larghezza della forma è visibile dal lato destro.
        // Quindi, la coordinata X della forma (newWorldX) può arrivare fino a:
        // currentScrollX + worldCanvasWidth - (shapeWidth * HIDDEN_SHAPE_PORTION)
        // La logica originale non sottraeva effectiveBorderMargin da questo limite superiore.
        double maxClampedX = currentScrollX + worldCanvasWidth - shapeWidth * AbstractMouseHandler.HIDDEN_SHAPE_PORTION;

        // Applica i limiti calcolati a newWorldX
        newWorldX = Math.max(minClampedX, Math.min(newWorldX, maxClampedX));

        // Fai lo stesso per newWorldY
        double minClampedY = currentScrollY + effectiveBorderMargin - shapeHeight * AbstractMouseHandler.VISIBLE_SHAPE_PORTION;
        double maxClampedY = currentScrollY + worldCanvasHeight - shapeHeight * AbstractMouseHandler.HIDDEN_SHAPE_PORTION;

        newWorldY = Math.max(minClampedY, Math.min(newWorldY, maxClampedY));

        // Se il drag è appena cominciato imposta le variabili del controller per isDragging()
        if (!controller.isDragging()) {
            controller.setStartDragX(event.getX()); // Screen coordinates
            controller.setStartDragY(event.getY()); // Screen coordinates
        }

        controller.getModel().moveShapeTo(currentShape, newWorldX, newWorldY);
        controller.redrawCanvas();
    }
}