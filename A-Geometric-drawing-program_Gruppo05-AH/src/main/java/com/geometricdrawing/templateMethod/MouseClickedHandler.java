package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.command.AddShapeCommand;
import com.geometricdrawing.command.Command;
import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.factory.PolygonFactory;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Line;
import com.geometricdrawing.ZoomHandler;
import com.geometricdrawing.model.Polygon;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class MouseClickedHandler extends AbstractMouseHandler {
    private double worldX; // Coordinate del mondo
    private double worldY;
    private ShapeFactory currentShapeFactory;
    private Color border;
    private Color fill;

    public MouseClickedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        if (controller.getModel() == null || controller.getCommandManager() == null ||
                controller.getHeightSpinner() == null || controller.getWidthSpinner() == null ||
                controller.getZoomHandler() == null) {
            System.err.println("Errore: Componenti non inizializzati (model, commandManager, spinners o zoomHandler).");
            this.worldX = event.getX();
            this.worldY = event.getY();
            return;
        }

        ZoomHandler zoomHandler = controller.getZoomHandler();
        Point2D worldCoords = zoomHandler.screenToWorld(event.getX(), event.getY());
        this.worldX = worldCoords.getX();
        this.worldY = worldCoords.getY();

        currentShapeFactory = controller.getCurrentShapeFactory();
        border = controller.getBorderPicker().getValue();
        fill = controller.getFillPicker().getValue();
    }

    @Override
    protected void processEvent(MouseEvent event) {
        if (currentShapeFactory == null) {
            currentShape = controller.selectShapeAt(this.worldX, this.worldY);
            return;
        }
    }

    @Override
    protected void postProcess(MouseEvent event) {
        if (!controller.isDrawingPolygon()) {
            controller.setCurrentShape(currentShape);
            controller.updateControlState(currentShape);
            controller.updateSpinners(currentShape);

            // Reset della factory per figure non poligoni
            if (currentShapeFactory != null && !(currentShapeFactory instanceof PolygonFactory)) {
                canvas.setCursor(Cursor.DEFAULT);
                controller.setCurrentShapeFactory(null);
                System.out.println("DEBUG: Factory resettata nel ClickHandler.");
            }
        } else {
            // Durante il disegno del poligono, aggiorna SOLO il canvas, NON lo stato UI
            System.out.println("DEBUG: Disegno poligono in corso, aggiornamento canvas.");
        }

        super.postProcess(event); // Aggiorna il canvas
    }

    private void handleRegularShapeCreation() {
        AbstractShape newShape = currentShapeFactory.createShape(this.worldX, this.worldY);

        if (controller.isTooClose(newShape, this.worldX, this.worldY)) {
            currentShape = controller.selectShapeAt(this.worldX, this.worldY);
            return;
        }

        AbstractShape styledShape = applyDecorations(newShape);
        currentShape = styledShape;

        AddShapeCommand addCmd = new AddShapeCommand(controller.getModel(), styledShape);
        controller.getCommandManager().executeCommand(addCmd);
    }

    private void handlePolygonCreation() {
        if (controller.getTempPolygonPoints().isEmpty()) {
            System.out.println("DEBUG: Inizio del disegno di un poligono.");
        }

        if (controller.isDrawingPolygon() && currentShapeFactory instanceof PolygonFactory polygonFactory) {
            System.out.println("DEBUG: Controller sta disegnando un poligono");

            Point2D point = new Point2D(worldX, worldY);
            controller.getTempPolygonPoints().add(point);

            System.out.println("DEBUG: Aggiunto punto al poligono: " + point + " (totale: " + controller.getTempPolygonPoints().size() + ")");

            // Se abbiamo raggiunto il numero di punti richiesto per il poligono
            if (controller.getTempPolygonPoints().size() == polygonFactory.getMaxPoints()) {
                // Crea e aggiunge il poligono
                AbstractShape polygon = currentShapeFactory.createShape(worldX, worldY);
                ((Polygon) polygon).setVertices(new ArrayList<>(controller.getTempPolygonPoints()));

                // Imposta i colori dal ColorPicker
                currentShape = applyDecorations(polygon);

                AddShapeCommand cmd = new AddShapeCommand(controller.getModel(), currentShape);
                controller.getCommandManager().executeCommand(cmd);

                // Reset completo dello stato del poligono
                controller.setIsDrawingPolygon(false);
                controller.getTempPolygonPoints().clear();

                // Reset della factory e del cursore per completare il ciclo
                controller.setCurrentShapeFactory(null);
                canvas.setCursor(Cursor.DEFAULT);

                System.out.println("DEBUG: Poligono completato e stato resettato.");

                // poligono è completo, aggiorna lo stato UI
                controller.setCurrentShape(currentShape);
                controller.updateControlState(currentShape);
                controller.updateSpinners(currentShape);
            }
        }

    private AbstractShape applyDecorations(AbstractShape shape) {
        AbstractShape styledShape = shape;
        //applica il colore di riempimento e il bordo se specificati
        if (styledShape instanceof Line && border != null) {
            styledShape = new BorderColorDecorator(styledShape, border);
        } else if (!(styledShape instanceof Line) && border != null && fill != null) {
            styledShape = new FillColorDecorator(styledShape, fill); // Applica prima il riempimento
            styledShape = new BorderColorDecorator(styledShape, border); // Poi il bordo
        } else if (border != null) { // Caso generico per solo bordo se il riempimento non è applicabile/selezionato
            styledShape = new BorderColorDecorator(styledShape, border);
        }

        return styledShape;
    }
}