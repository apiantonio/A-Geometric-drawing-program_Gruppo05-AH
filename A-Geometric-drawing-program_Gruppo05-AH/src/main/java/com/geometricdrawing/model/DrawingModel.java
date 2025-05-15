package com.geometricdrawing.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DrawingModel {
    private final ObservableList<Shape> shapes;

    public DrawingModel() {
        this.shapes = FXCollections.observableArrayList();
    }

    public void addShape(Shape s) {
        if (s != null) {
            this.shapes.add(s);
        }
    }

    // removeShape e clear non sono strettamente necessarie per US-2 e US-3,
    // ma sono fondamentali per un modello di disegno. Le lasciamo per completezza base.
    public void removeShape(Shape s) {
        this.shapes.remove(s);
    }

    public ObservableList<Shape> getShapes() {
        return this.shapes;
    }

    public void clear() {
        this.shapes.clear();
    }
}