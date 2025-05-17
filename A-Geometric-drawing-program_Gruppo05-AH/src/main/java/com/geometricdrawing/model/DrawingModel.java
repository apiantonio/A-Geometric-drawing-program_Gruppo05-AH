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
    private transient ObservableList<Shape> shapes;

    public DrawingModel() {
        this.shapes = FXCollections.observableArrayList();
    }

    public void addShape(Shape s) {
        if (s != null) {
            Shape unwrappedShape = s instanceof ShapeDecorator decorator ? decorator.unwrap() : s;
            if (unwrappedShape instanceof AbstractShape abstractShape) {
                abstractShape.setZ(shapes.size()); // La nuova figura ha lo Z più alto
            }
            this.shapes.add(s);
        }
    }

    public void removeShape(Shape s) {
        this.shapes.remove(s);
    }

    public ObservableList<Shape> getShapes() {
        return this.shapes;
    }

    public void clear() {
        this.shapes.clear();
    }

    // restituisce le figure in ordine decrescente di z
    public ObservableList<Shape> getShapesOrderedByZ() {
        return FXCollections.observableArrayList(
                shapes.stream()
                        .map(shape -> shape instanceof ShapeDecorator decorator ? decorator.unwrap() : shape) // rimuove i decoratori
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
            List<Shape> loadedShapes = (List<Shape>) ois.readObject();
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

    // Optional: If DrawingModel itself needs to be serializable (e.g. part of a larger state)
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject(); // For any other fields in DrawingModel
        out.writeObject(new ArrayList<>(shapes)); // Serialize shapes as ArrayList
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // For any other fields
        List<Shape> loadedShapes = (List<Shape>) in.readObject();
        this.shapes = FXCollections.observableArrayList(); // Re-initialize
        if (loadedShapes != null) {
            this.shapes.addAll(loadedShapes);
        }
    }
}