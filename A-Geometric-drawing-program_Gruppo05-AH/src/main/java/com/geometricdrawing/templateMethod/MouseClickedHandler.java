package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.command.AddShapeCommand;
import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Line;
import com.geometricdrawing.ZoomHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

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

        AbstractShape newShape = currentShapeFactory.createShape(this.worldX, this.worldY);

        AbstractShape styledShape = newShape;
        //applica il colore di riempimento e il bordo se specificati
        if (newShape instanceof Line && border != null) {
            styledShape = new BorderColorDecorator(newShape, border);
        } else if (!(newShape instanceof Line) && border != null && fill != null) {
            styledShape = new FillColorDecorator(newShape, fill); // Applica prima il riempimento
            styledShape = new BorderColorDecorator(styledShape, border); // Poi il bordo
        } else if (border != null) { // Caso generico per solo bordo se il riempimento non è applicabile/selezionato
            styledShape = new BorderColorDecorator(styledShape, border);
        }

        // aggiorna la shape corrente con la nuova creata
        currentShape = styledShape; // La nuova forma creata è ora quella corrente

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
}