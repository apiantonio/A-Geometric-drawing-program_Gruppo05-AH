package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Ellipse extends AbstractShape {

    public Ellipse(double x, double y, double width, double height) {
        super(x, y, width, height); // x,y sono angolo sup-sx del bbox, width/height sono i diametri
        super.fillColor = Color.LIGHTBLUE; // Esempio di default diverso per l'ellisse
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(getFillColorInternal());
        gc.fillOval(this.x, this.y, this.width, this.height);

        gc.setStroke(getBorderColorInternal());
        gc.strokeOval(this.x, this.y, this.width, this.height);
    }
}