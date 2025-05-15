package com.geometricdrawing.model;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface Shape {

    void draw(GraphicsContext gc);

}