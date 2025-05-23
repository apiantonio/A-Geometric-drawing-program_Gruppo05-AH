package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.command.AddShapeCommand;
import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Line;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class MouseClickedHandler extends AbstractMouseHandler {
    private double x;
    private double y;
    private ShapeFactory currentShapeFactory;
    Color border;
    Color fill;

    public MouseClickedHandler(Canvas canvas, DrawingController controller) {
        super(canvas, controller);
    }

    @Override
    protected void preProcess(MouseEvent event) {
        if (controller.getModel() == null || controller.getCommandManager() == null ||
                controller.getHeightSpinner() == null || controller.getWidthSpinner() == null) {
            System.err.println("Errore: Componenti non inizializzati (model, commandManager o spinners).");
            return;
        }

        x = event.getX();
        y = event.getY();
        currentShapeFactory = controller.getCurrentShapeFactory();
        border = controller.getBorderPicker().getValue();
        fill = controller.getFillPicker().getValue();
    }

    @Override
    protected void processEvent(MouseEvent event) {
        if (currentShapeFactory == null) {
            // Se non c'è una factory attiva, controlla se il click è su una figura esistente
            currentShape = controller.selectShapeAt(event.getX(), event.getY());
            return;
        }

        // Crea la nuova figura usando la factory corrente
        AbstractShape newShape = currentShapeFactory.createShape(x, y);

        if (controller.isTooClose(newShape, x, y)) {
            // se la figura è troppo vicina ai bordi, non fare nulla
            // e aggiorna la figura corrente
            currentShape = controller.selectShapeAt(event.getX(), event.getY());
            return;
        }

        AbstractShape styledShape = newShape;

        // Applica il colore di riempimento e il bordo se specificati
        if (newShape instanceof Line && border != null) {
            styledShape = new BorderColorDecorator(newShape, border);
        } else if (border != null && fill != null) {
            styledShape = new FillColorDecorator(newShape, fill);
            styledShape = new BorderColorDecorator(styledShape, border);
        }

        // aggiorna la shape corrente con la nuova creata
        controller.setCurrentShape(styledShape);
        // Aggiorna l'interfaccia
        controller.updateSpinners(styledShape);
        controller.updateControlState(styledShape);

        // Crea ed esegui il comando per aggiungere la figura
        AddShapeCommand addCmd = new AddShapeCommand(controller.getModel(), styledShape);
        controller.getCommandManager().executeCommand(addCmd);
    }

    @Override
    protected void postProcess(MouseEvent event) {
        // Resetta la factory dopo aver creato la figura
        controller.setCurrentShapeFactory(null);
        // Aggiorna il canvas
        super.postProcess(event);
    }
}