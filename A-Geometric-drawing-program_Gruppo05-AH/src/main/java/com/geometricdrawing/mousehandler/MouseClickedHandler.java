package com.geometricdrawing.mousehandler;

import com.geometricdrawing.controller.DrawingController;
import com.geometricdrawing.command.AddShapeCommand;
import com.geometricdrawing.command.Command;
import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.factory.PolygonFactory;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.factory.TextFactory;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Line;
import com.geometricdrawing.controller.ZoomHandler;
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

    /**
     * Gestisce la creazione di una figura regolare (non poligono).
     * Se la figura è di tipo TextShape, imposta il testo e la dimensione del font.
     */
    private void handleRegularShapeCreation() {
        AbstractShape newShape = currentShapeFactory.createShape(this.worldX, this.worldY);

        if (newShape instanceof TextShape && currentShapeFactory instanceof TextFactory) {
            TextShape textShape = (TextShape) newShape; // Dichiara e casta newShape a TextShape
            String userText = controller.getTextField(); // dal controller ricavo il testo inserito dall'utente

            if (userText != null) {
                textShape.setText(userText);
            } else {
                textShape.setText(""); // Imposta una stringa vuota di default se userText è null
            }

            if (controller.getFontSizeSpinner() != null) { // Se lo spinner per la dimensione del font è stato inizializzato
                int fontSize = controller.getFontSizeSpinner().getValue();
                textShape.setFontSize(fontSize);
                // Debug: Stampa la dimensione del font impostata
                System.out.println("[MouseClickedHandler] Dimensione font impostata in TextShape: " + fontSize);
            }

            // Le dimensioni del rettangolo attorno al testo vengono calcolate in base al testo inserito
            Point2D naturalSize = textShape.getNaturalTextBlockDimensions(Double.MAX_VALUE);
            textShape.setWidth(naturalSize.getX());
            textShape.setHeight(naturalSize.getY());
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

                // Riabilita i controlli dopo il completamento del poligono
                controller.enableControlsAfterPolygonDrawing();

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