package com.geometricdrawing.model;

import com.geometricdrawing.decorator.ShapeDecorator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DrawingModel {
    // Mark shapes as transient because ObservableList is not reliably serializable by default.
    // We will handle its serialization manually.
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

    public void removeShape(AbstractShape s) {
        this.shapes.remove(s);
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

    public ObservableList<AbstractShape> getShapes() {
        return this.shapes;
    }

    public void clear() {
        this.shapes.clear();
    }
    // restituisce le figure in ordine decrescente di z
    public ObservableList<AbstractShape> getShapesOrderedByZ() {
        return FXCollections.observableArrayList(
                shapes.stream()
                        .sorted((s1, s2) -> Integer.compare(((AbstractShape) s2).getZ(), ((AbstractShape) s1).getZ()))
                        .toList()
        );
    }

    // Method to save shapes to a file
    public void saveToFile(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            // Convert ObservableList to ArrayList for serialization
            oos.writeObject(new ArrayList<>(shapes));
        }
    }

    // Method to load shapes from a file
    @SuppressWarnings("unchecked")
    public void loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<AbstractShape> loadedShapes = (List<AbstractShape>) ois.readObject();
            shapes.clear();
            if (loadedShapes != null) {
                shapes.addAll(loadedShapes);
            }
        }
        // Note: If shapes were not transient and you tried to serialize ObservableList directly,
        // you might need custom readObject/writeObject for DrawingModel as well.
        // For simplicity, we re-initialize if it was null (e.g. after deserialization of DrawingModel itself)
        if (this.shapes == null) {
            this.shapes = FXCollections.observableArrayList();
        }
    }

}