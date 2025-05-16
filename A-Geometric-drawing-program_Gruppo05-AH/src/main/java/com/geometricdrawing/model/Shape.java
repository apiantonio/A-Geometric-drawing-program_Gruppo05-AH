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
    double getY();
    void setX(double x);
    void setY(double y);


    //aggiunti getter e setter per il colore di riempimento e bordo
    void setFillColor(Color c);
    void setBorderColor(Color c);
    Color getFillColor();
    Color getBorderColor();
}