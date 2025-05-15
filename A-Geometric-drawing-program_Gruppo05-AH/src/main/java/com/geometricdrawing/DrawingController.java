package com.geometricdrawing;

import com.geometricdrawing.factory.EllipseFactory;
import com.geometricdrawing.factory.LineFactory;
import com.geometricdrawing.factory.RectangleFactory;
import com.geometricdrawing.factory.ShapeFactory;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
// Non servono ColorPicker qui per US-2/3
// import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseEvent;

// import javafx.scene.layout.AnchorPane;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Shape;

public class DrawingController {

    // @FXML private AnchorPane drawingAreaPane; // Non strettamente necessario per US-2/3 se canvas è fisso
    @FXML private Canvas drawingCanvas;
    // I ColorPicker non sono usati per US-2 e US-3
    // @FXML private ColorPicker fillColorPicker;
    // @FXML private ColorPicker borderColorPicker;

    private DrawingModel model;
    private ShapeFactory currentShapeFactory; // Per US-2 e US-3
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
            // Il binding delle dimensioni del canvas all'AnchorPane è una feature aggiuntiva.
            // Per US-2/3, potremmo assumere dimensioni fisse o che l'FXML le gestisca.
            // Se l'FXML imposta width/height per il Canvas, questo è sufficiente.
            // drawingCanvas.widthProperty().bind(drawingAreaPane.widthProperty());
            // drawingCanvas.heightProperty().bind(drawingAreaPane.heightProperty());
            // drawingCanvas.widthProperty().addListener(observable -> redrawCanvas());
            // drawingCanvas.heightProperty().addListener(observable -> redrawCanvas());

            drawingCanvas.setOnMouseClicked(this::handleCanvasClick);
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
        // Non usiamo i ColorPicker per US-3.

        model.addShape(newShape);
        // Il listener sull'ObservableList del model chiamerà redrawCanvas.
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