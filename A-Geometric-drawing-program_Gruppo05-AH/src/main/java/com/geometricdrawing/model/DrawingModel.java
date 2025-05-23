package com.geometricdrawing.model;

import com.geometricdrawing.decorator.BorderColorDecorator;
import com.geometricdrawing.decorator.FillColorDecorator;
import com.geometricdrawing.decorator.ShapeDecorator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Autore: Gruppo05
 * @Scopo: Modello dell'applicazione, contiene le figure e gestisce le operazioni fondamentali ssu di esse.
 */
public class DrawingModel {
    private transient ObservableList<AbstractShape> shapes;

    public DrawingModel() {
        this.shapes = FXCollections.observableArrayList();
    }

    public void addShape(AbstractShape s) {
        if (s != null) {
            s.setZ(shapes.size()); // La nuova figura ha lo Z più alto
            this.shapes.add(s);
        }
    }

    /**
     * @param s rimuove una figura dalla lista delle figure del modello
     */
    public void removeShape(AbstractShape s) {
        if (s != null) {
            this.shapes.remove(s);
        }
    }

    public void setShapeWidth(AbstractShape shape, double width) {
        if (shape != null) {
            shape.setWidth(width);
        }
    }

    public void setShapeHeight(AbstractShape shape, double newHeight) {
        if (shape != null) {
            shape.setHeight(newHeight);
        }
    }

    public void moveShapeTo(AbstractShape shape, double newX, double newY) {
        if (shape != null) {
            shape.moveTo(newX, newY);
        }
    }

    public void setBorderColor(BorderColorDecorator decorator, Color color) {
        if (decorator != null) {
            decorator.setBorderColor(color);
        }
    }

    public void setFillColor(FillColorDecorator decorator, Color color) {
        if (decorator != null) {
            decorator.setFillColor(color);
        }
    }

    public ObservableList<AbstractShape> getShapes() {
        return this.shapes;
    }

    public void clear() {
        this.shapes.clear();
    }

    /**
     * Metodo che restituisce le figure in ordine decrescente di z
     */
    public ObservableList<AbstractShape> getShapesOrderedByZ() {
        return FXCollections.observableArrayList(
                shapes.stream()
                        .sorted((s1, s2) -> Integer.compare(((AbstractShape) s2).getZ(), ((AbstractShape) s1).getZ()))
                        .toList()
        );
    }

    /**
     * @param file  per il salvataggio di figure su un file
     */
    public void saveToFile(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            // Convert ObservableList to ArrayList for serialization
            oos.writeObject(new ArrayList<>(shapes));
        }
    }

    /**
     * @param file  per il caricameento di figure da un file
     */
    @SuppressWarnings("unchecked")
    public void loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<AbstractShape> loadedShapes = (List<AbstractShape>) ois.readObject();
            shapes.clear();
            if (loadedShapes != null) {
                shapes.addAll(loadedShapes);
            }
        }
        if (this.shapes == null) {
            this.shapes = FXCollections.observableArrayList();
        }
    }

}