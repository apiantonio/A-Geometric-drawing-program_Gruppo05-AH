package com.geometricdrawing;

import com.geometricdrawing.command.AddShapeCommand;
import com.geometricdrawing.command.CommandManager;
import com.geometricdrawing.factory.EllipseFactory;
import com.geometricdrawing.factory.LineFactory;
import com.geometricdrawing.factory.RectangleFactory;
import com.geometricdrawing.factory.ShapeFactory;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import com.geometricdrawing.model.DrawingModel;
import com.geometricdrawing.model.Shape;
import com.geometricdrawing.model.Line;
import com.geometricdrawing.model.AbstractShape;

import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Spinner;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;


public class DrawingController {


    @FXML private Canvas drawingCanvas;
    @FXML private Pane canvasContainer;
    @FXML private Button deleteButton;
    @FXML private ColorPicker fillPicker;
    @FXML private ColorPicker borderPicker;
    @FXML private Spinner<Double> heightSpinner;
    @FXML private Spinner<Double> widthSpinner;


    private BooleanProperty canDelete = new SimpleBooleanProperty(false);

    private DrawingModel model;
    private ShapeFactory currentShapeFactory;
    private GraphicsContext gc;
    private CommandManager commandManager;

    public void setModel(DrawingModel model) {
        this.model = model;
        if (this.model != null && this.model.getShapes() != null) {
            this.model.getShapes().addListener((ListChangeListener.Change<? extends Shape> c) -> {
                redrawCanvas();
            });
        }
        redrawCanvas();
    }

    //Metodo per iniezione del CommandManager
    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @FXML
    public void initialize() {
        if (drawingCanvas != null) {
            gc = drawingCanvas.getGraphicsContext2D();

            drawingCanvas.setOnMouseClicked(this::handleCanvasClick);

            //per il binding all'avvio, solo una nuova forma può essere premuto come bottone nella barra degli strumenti
            deleteButton.disableProperty().bind(canDelete.not());
            fillPicker.disableProperty().bind(canDelete.not());
            borderPicker.disableProperty().bind(canDelete.not());
            widthSpinner.disableProperty().bind(canDelete.not());
            heightSpinner.disableProperty().bind(canDelete.not());

            /*
            nel momento in cui si allarga la finestra, il pane che contiene il canvas (che non è estensibile di suo)
            deve estendersi a sua volta
             */
            drawingCanvas.widthProperty().bind(canvasContainer.widthProperty());
            drawingCanvas.heightProperty().bind(canvasContainer.heightProperty());
        } else {
            System.err.println("Errore: drawingCanvas non è stato iniettato!");
        }

        if (heightSpinner != null) {
            SpinnerValueFactory<Double> heightFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 40.0, 1.0); // min, max, initial, step
            heightSpinner.setValueFactory(heightFactory);
            heightSpinner.setEditable(false);
        }
        if (widthSpinner != null) {
            SpinnerValueFactory<Double> widthFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 60.0, 1.0); // min, max, initial, step
            widthSpinner.setValueFactory(widthFactory);
            widthSpinner.setEditable(false);
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
        if (model == null || commandManager == null || heightSpinner == null || widthSpinner == null) {
            System.err.println("Errore: Componenti non inizializzati (model, commandManager o spinners).");
            return;
        }

        double x = event.getX();
        double y = event.getY();

        Shape newShape = currentShapeFactory.createShape(x, y);

        AddShapeCommand addCmd = new AddShapeCommand(model, newShape);
        commandManager.executeCommand(addCmd);

        // Dopo aver aggiunto la forma, aggiorna gli spinner
        if (newShape != null) {
            if (newShape instanceof Line line) {
                // Per la linea, mostriamo la sua lunghezza effettiva nello spinner della larghezza
                widthSpinner.getValueFactory().setValue(line.getLength());
                heightSpinner.getValueFactory().setValue(line.getHeight()); //sarà 1

            } else if (newShape instanceof AbstractShape shapeWithDims) { // Per Rectangle, Ellipse

                widthSpinner.getValueFactory().setValue(shapeWithDims.getWidth());
                heightSpinner.getValueFactory().setValue(shapeWithDims.getHeight());

            }
        }
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