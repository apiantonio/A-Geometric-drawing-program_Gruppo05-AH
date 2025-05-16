package com.geometricdrawing.model;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serializable;

public interface Shape extends Serializable {

    void draw(GraphicsContext gc);

    double getWidth();
    double getHeight();
    double getX();
    void setX(double deltaX);
    double getY();
    void setY(double deltaY);

    boolean containsPoint(double x, double y, double threshold);

}