package com.geometricdrawing.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Rectangle extends AbstractShape {

    public Rectangle(double x, double y, double width, double height) {
        super(x, y, width, height);

        super.fillColor = Color.LIGHTGRAY; // Colore di default
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(getFillColorInternal()); // Usa il colore di riempimento interno
        gc.fillRect(this.x, this.y, this.width, this.height);

        gc.setStroke(getBorderColorInternal()); // Usa il colore del bordo interno
        gc.strokeRect(this.x, this.y, this.width, this.height);
    }


}