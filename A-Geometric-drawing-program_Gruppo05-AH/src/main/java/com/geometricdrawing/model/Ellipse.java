package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Ellipse extends AbstractShape {

    public Ellipse(double x, double y, double width, double height) {
        super(x, y, width, height); // x,y sono angolo sup-sx del bbox, width/height sono i diametri
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(getFillColor());
        gc.fillOval(this.x, this.y, this.width, this.height);

        gc.setStroke(getBorderColor());
        gc.strokeOval(this.x, this.y, this.width, this.height);
    }
}