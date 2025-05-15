package com.geometricdrawing.model;

import javafx.scene.paint.Color;

public abstract class AbstractShape implements Shape {
    // ... (campi x, y, width, height) ...
    protected Color borderColor = Color.BLACK; // Default per US-3
    protected Color fillColor = Color.TRANSPARENT; // Default per US-3 (o un colore solido per forme chiuse)

    /*
    public AbstractShape(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

     */

}
