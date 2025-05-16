package com.geometricdrawing;

import com.geometricdrawing.factory.EllipseFactory;
import com.geometricdrawing.factory.LineFactory;
import com.geometricdrawing.factory.RectangleFactory;
import com.geometricdrawing.factory.ShapeFactory;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Shape;

public class DrawingController {


    @FXML private Canvas drawingCanvas;
    @FXML private Pane canvasContainer;

    private DrawingModel model;
    private ShapeFactory currentShapeFactory;
    private GraphicsContext gc;

    public void setModel(DrawingModel model) {
        this.model = model;
        if (this.model != null && this.model.getShapes() != null) {
            this.model.getShapes().addListener((ListChangeListener.Change<? extends Shape> c) -> {
                redrawCanvas();
            });
        }
        redrawCanvas();
    }

    @FXML
    public void initialize() {
        if (drawingCanvas != null) {
            gc = drawingCanvas.getGraphicsContext2D();

            drawingCanvas.setOnMouseClicked(this::handleCanvasClick);

            /*
            nel momento in cui si allarga la finestra, il pane che contiene il canvas (che non è estensibile di suo)
            deve estendersi a sua volta
             */
            drawingCanvas.widthProperty().bind(canvasContainer.widthProperty());
            drawingCanvas.heightProperty().bind(canvasContainer.heightProperty());
        } else {
            System.err.println("Errore: drawingCanvas non è stato iniettato!");
        }
        currentShapeFactory = null; // Nessuna forma selezionata all'inizio
    }

    @FXML
    private void handleSelectLinea(ActionEvent event) {
        currentShapeFactory = new LineFactory();
    }

    @FXML
    private void handleSelectRettangolo(ActionEvent event) {
        currentShapeFactory = new RectangleFactory();
    }

    @FXML
    private void handleSelectEllisse(ActionEvent event) {
        currentShapeFactory = new EllipseFactory();
    }

    private void handleCanvasClick(MouseEvent event) {
        if (currentShapeFactory == null) {
            System.out.println("Seleziona una forma prima di disegnare.");
            return;
        }
        if (model == null) {
            System.err.println("Errore: DrawingModel non inizializzato.");
            return;
        }

        double x = event.getX();
        double y = event.getY();

        Shape newShape = currentShapeFactory.createShape(x, y);
        // I colori sono definiti come default nelle classi Shape/AbstractShape.

        model.addShape(newShape);
    }


    private void redrawCanvas() {
        if (gc == null || drawingCanvas == null || model == null) {
            return;
        }
        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        for (Shape shape : model.getShapes()) {
            if (shape != null) {
                shape.draw(gc);
            }
        }
    }
}
