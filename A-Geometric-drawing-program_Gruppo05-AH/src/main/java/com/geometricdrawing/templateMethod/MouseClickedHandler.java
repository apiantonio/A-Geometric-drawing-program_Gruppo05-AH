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

        // Gestione speciale per il poligono
        if (currentShapeFactory instanceof PolygonFactory) {
            handlePolygonClick();
            return;
        }

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

    @Override
    protected void postProcess(MouseEvent event) {
        controller.setCurrentShape(currentShape);

        // Aggiorna l'interfaccia
        controller.updateControlState(currentShape);
        controller.updateSpinners(currentShape);

        // Reset della factory solo se la figura è stata effettivamente creata
        if (currentShape != null && currentShapeFactory != null) {
            controller.setCurrentShapeFactory(null);
        }

        super.postProcess(event); // Aggiorna il canvas
    }

    private void handlePolygonClick() {
        if (!controller.isDrawingPolygon()) {
            // Primo click: inizia un nuovo poligono
            controller.setTempPolygonPoints(new ArrayList<>());
            controller.getTempPolygonPoints().add(new Point2D(worldX, worldY));
            controller.setIsDrawingPolygon(true);
        } else {
            // Click successivi: aggiungi vertici
            Point2D firstPoint = controller.getTempPolygonPoints().getFirst();

            // Se il click è vicino al punto iniziale, chiudi il poligono
            if (isCloseToFirstPoint(worldX, worldY, firstPoint)) {
                finishPolygon();
            } else {
                // Aggiungi un nuovo vertice
                controller.getTempPolygonPoints().add(new Point2D(worldX, worldY));
                controller.redrawCanvas();
            }
        }
    }

    private boolean isCloseToFirstPoint(double x, double y, Point2D firstPoint) {
        double distance = Math.sqrt(
                Math.pow(x - firstPoint.getX(), 2) +
                Math.pow(y - firstPoint.getY(), 2)
        );
        return distance < 5.0; // 5.0 è la soglia di distanza in pixel
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

    private void finishPolygon() {
        if (controller.getTempPolygonPoints().size() >= 3) {
            Polygon polygon = new Polygon(
                    controller.getTempPolygonPoints().getFirst().getX(),
                    controller.getTempPolygonPoints().getFirst().getY()
            );
            polygon.setVertices(controller.getTempPolygonPoints());

            // Applica decoratori come per altre figure
            AbstractShape styledPolygon = applyDecorations(polygon);

            AddShapeCommand addCmd = new AddShapeCommand(controller.getModel(), styledPolygon);
            controller.getCommandManager().executeCommand(addCmd);

            controller.setCurrentShape(styledPolygon);
        }

        // Reset dello stato
        controller.setIsDrawingPolygon(false);
        controller.setTempPolygonPoints(null);
        controller.setCurrentShapeFactory(null);
        controller.redrawCanvas();
    }
}