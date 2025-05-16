package com.geometricdrawing;

import com.geometricdrawing.factory.EllipseFactory;
import com.geometricdrawing.factory.LineFactory;
import com.geometricdrawing.factory.RectangleFactory;
import com.geometricdrawing.factory.ShapeFactory;
import com.geometricdrawing.model.AbstractShape;
import com.geometricdrawing.model.Line;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Shape;
import javafx.scene.paint.Color;

public class DrawingController {

    @FXML private Canvas drawingCanvas;
    @FXML private Pane canvasContainer;

    private DrawingModel model;
    private GraphicsContext gc;
    private ShapeFactory currentShapeFactory;               // factory per la creazione della figura
    private Shape currentShape;                             // figura selezionata
    private static final double HANDLE_RADIUS = 3.0;        // raggio del cerchietto che ocmpare alla selezione di una figura
    private static final double SELECTION_THRESHOLD = 5.0;  // treshold per la selezione

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
        if (model == null) {
            System.err.println("Errore: DrawingModel non inizializzato.");
            return;
        }

        double x = event.getX();
        double y = event.getY();

        // la shape factory non null significa che l'utente ha selezionato l'inserimento di una figura
        if (currentShapeFactory != null) {
            Shape newShape = currentShapeFactory.createShape(x, y);
            model.addShape(newShape);
            currentShape = newShape; // La forma corrente è quella appena inserita
            currentShapeFactory = null; // Resetta la factory per richiedere una nuova selezione
            redrawCanvas();
            return;
        }

        // se non è stato selezionato l'inserimento di una figura allora controllo se il click è su una figura esistente
        for (Shape shape : model.getShapes()) {
            if (isPointInsideShape(x, y, shape)) {
                currentShape = shape; // Seleziona la figura cliccata
                redrawCanvas();
                return;
            }
        }

        // se il click è su uno spazio vuoto deseleziona la figura corrente
        currentShape = null;

        redrawCanvas();
    }

    private boolean isPointInsideShape(double clickX, double clickY, Shape shape) {
        if (shape instanceof AbstractShape abstractShape) {
            // Controlla se il punto (x, y) è all'interno della figura considerando il treshold
            return  clickX >= abstractShape.getX() - SELECTION_THRESHOLD &&
                    clickX <= abstractShape.getX() + abstractShape.getWidth() + SELECTION_THRESHOLD &&
                    clickY >= abstractShape.getY() - SELECTION_THRESHOLD &&
                    clickY <= abstractShape.getY() + abstractShape.getHeight() + SELECTION_THRESHOLD;
        }
        return false;
    }

    // Metodo per disegnare un cerchietto
    private void drawHandle(double x, double y) {
        gc.setFill(Color.SKYBLUE);
        gc.fillOval(x - HANDLE_RADIUS, y - HANDLE_RADIUS, HANDLE_RADIUS * 2, HANDLE_RADIUS * 2);
    }

    private void redrawCanvas() {
        if (gc == null || drawingCanvas == null || model == null) {
            return;
        }

        gc.clearRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        for (Shape shape : model.getShapes()) {
            if (shape != null) {
                shape.draw(gc);
                // Se la figura è quella selezionata
                if (shape == currentShape) {
                    if (shape instanceof Line line) {
                        // Disegna i cerchietti alle estremità della linea
                        drawHandle(line.getX(), line.getY()); // Inizio linea
                        drawHandle(line.getX() + line.getWidth(), line.getY() + line.getHeight()); // Fine linea
                    } else if (shape instanceof AbstractShape abstractShape) {
                        // Disegna il bordo tratteggiato rettangolare
                        gc.setStroke(Color.SKYBLUE);
                        gc.setLineWidth(1);
                        gc.setLineDashes(5); // linea tratteggiata
                        gc.strokeRect(abstractShape.getX(), abstractShape.getY(), abstractShape.getWidth(), abstractShape.getHeight());
                        gc.setLineDashes(0); // ripristina linea continua

                        // Disegna i cerchietti agli angoli
                        drawHandle(abstractShape.getX(), abstractShape.getY()); // Angolo superiore sinistro
                        drawHandle(abstractShape.getX() + abstractShape.getWidth(), abstractShape.getY()); // Angolo superiore destro
                        drawHandle(abstractShape.getX(), abstractShape.getY() + abstractShape.getHeight()); // Angolo inferiore sinistro
                        drawHandle(abstractShape.getX() + abstractShape.getWidth(), abstractShape.getY() + abstractShape.getHeight()); // Angolo inferiore destro
                    }
                }
            }
        }
    }
}
