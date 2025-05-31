package com.geometricdrawing.templateMethod;

import com.geometricdrawing.DrawingController;
import com.geometricdrawing.command.AddShapeCommand;
import com.geometricdrawing.command.Command;
import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.factory.PolygonFactory;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.factory.TextFactory;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Line;
import com.geometricdrawing.ZoomHandler;
import com.geometricdrawing.model.Polygon;
import com.geometricdrawing.model.TextShape;
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

        this.currentShapeFactory = controller.getCurrentShapeFactory();
        this.border = controller.getBorderPicker().getValue();
        this.fill = controller.getFillPicker().getValue();
    }

    @Override
    protected void processEvent(MouseEvent event) {
        if (controller.isDrawingPolygon()) {
            handlePolygonCreation();
        } else if (currentShapeFactory != null) {
            handleRegularShapeCreation();
        } else {
            currentShape = controller.selectShapeAt(this.worldX, this.worldY);
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

    // All'interno della classe MouseClickedHandler.java
// in main/java/com/geometricdrawing/templateMethod/MouseClickedHandler.java

    private void handleRegularShapeCreation() {
        AbstractShape newShape = currentShapeFactory.createShape(this.worldX, this.worldY);

        if (newShape instanceof TextShape && currentShapeFactory instanceof TextFactory) {
            TextShape textShape = (TextShape) newShape; // Dichiara e casta newShape a TextShape

            String userText = controller.getTextField(); // Il metodo getTextField() del controller restituisce il testo.
            // Vedi DrawingController.java

            // Debug: Stampa il testo ottenuto dal TextField
            System.out.println("[MouseClickedHandler] Testo letto da TextField: '" + userText + "'");

            if (userText != null) {
                textShape.setText(userText);
            } else {
                textShape.setText(""); // Imposta una stringa vuota di default se userText è null
            }

            // Debug: Stampa il testo impostato nella TextShape
            System.out.println("[MouseClickedHandler] Testo impostato in TextShape: '" + textShape.getText() + "'");

            if (controller.getFontSizeSpinner() != null) { // Controlla se lo spinner esiste
                Integer userFontSize = controller.getFontSizeSpinner().getValue();
                if (userFontSize != null) {
                    textShape.setFontSize(userFontSize);
                    // Debug: Stampa la dimensione del font impostata
                    System.out.println("[MouseClickedHandler] Dimensione font impostata in TextShape: " + userFontSize);
                }
            }
        }

        AbstractShape styledShape = applyDecorations(newShape);
        currentShape = styledShape;

        Command addCmd = new AddShapeCommand(controller.getModel(), styledShape);
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

            // Se si è raggiunto il numero di punti richiesto per il poligono
            if (controller.getTempPolygonPoints().size() == polygonFactory.getMaxPoints()) {
                AbstractShape polygon = currentShapeFactory.createShape(worldX, worldY);
                ((Polygon) polygon).setVertices(new ArrayList<>(controller.getTempPolygonPoints()));

                currentShape = applyDecorations(polygon);

                Command cmd = new AddShapeCommand(controller.getModel(), currentShape);
                controller.getCommandManager().executeCommand(cmd);

                // Reset completo dello stato del poligono
                controller.setIsDrawingPolygon(false);
                controller.getTempPolygonPoints().clear();

                canvas.setCursor(Cursor.DEFAULT);

                System.out.println("DEBUG: Poligono completato e stato resettato.");
            }
        }
    }

    private AbstractShape applyDecorations(AbstractShape shape) {
        AbstractShape styledShape = shape;

        if (styledShape instanceof TextShape) {
            if (fill != null) {
                styledShape = new FillColorDecorator(styledShape, fill);
            }
        } else if (styledShape instanceof Line) {
            if (border != null) {
                styledShape = new BorderColorDecorator(styledShape, border);
            }
        } else {
            if (fill != null) {
                styledShape = new FillColorDecorator(styledShape, fill);
            }
            if (border != null) {
                styledShape = new BorderColorDecorator(styledShape, border);
            }
        }
        return styledShape;
    }
}